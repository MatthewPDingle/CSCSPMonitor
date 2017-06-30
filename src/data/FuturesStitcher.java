package data;

import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants.BAR_SIZE;
import dbio.QueryManager;
import utils.CalendarUtils;

public class FuturesStitcher {

	public static void main(String[] args) {
		process("ZN", BAR_SIZE.BAR_30M);
		process("ZN", BAR_SIZE.BAR_1H);
		process("ZN", BAR_SIZE.BAR_2H);
	}

	public static void process(String baseSymbol, BAR_SIZE duration) {
		try {
			// Get list of future contract names including the expiry part.  Ordered new to old.
			ArrayList<String> futuresContractNames = QueryManager.getFutureContractNames(baseSymbol, duration);
			
			String newestContractName = "";
			String oldestContractName = "";
			if (futuresContractNames != null && futuresContractNames.size() > 0) {
				newestContractName = futuresContractNames.get(0);
				oldestContractName = futuresContractNames.get(futuresContractNames.size() - 1);
			}
			
			// Get max start from the most recent futures contract name
			Calendar cStart = QueryManager.getMaxStart(newestContractName, duration);
			Calendar cMinStart = QueryManager.getMinStart(oldestContractName, duration);

			String lastBestExpiry = CalendarUtils.getFuturesContractBasedOnRolloverDate(baseSymbol, cStart);
			double adjustment = 0;
			while (cStart.after(cMinStart)) {
				
				// Get all the bars for the different contracts that might have this exact start
				ArrayList<Bar> barsAtTime = QueryManager.getAllDatedFuturesBarsAtTimeForBaseSymbol(baseSymbol, duration, cStart);
					
				// Find the best (most appropriate) one and make adjustments to make a new bar with continuity
				String bestExpiry = CalendarUtils.getFuturesContractBasedOnRolloverDate(baseSymbol, cStart);
				
//				System.out.println(bestExpiry + "\t\t" + cStart.getTime().toString());
				
				long totalVolume = 0;
				Bar bestBar = null;
				for (Bar bar : barsAtTime) {
					totalVolume += bar.volume;
					if (bar.symbol.contains(bestExpiry)) {
						bestBar = new Bar(bar);
					}
				}
				if (bestBar != null) {	
					if (!bestExpiry.equals(lastBestExpiry)) {
						// Get the price ratio between the new contract and the old contract.
						// lastBestExpiry is the newer contract, and bestBar is now for the older contract (the one we're going to use)
						double lastBestExpiryClose = 0f;
						for (Bar bar : barsAtTime) {
							if (bar.symbol.contains(lastBestExpiry)) {
								lastBestExpiryClose = bar.close;
							}
						}
						if (lastBestExpiryClose != 0) {
							adjustment = lastBestExpiryClose - bestBar.close;
						}
						
						System.out.println("-----");
						System.out.println("Switching from " + lastBestExpiry + " to " + bestExpiry);
						System.out.println(cStart.getTime().toString() + "\t" + adjustment);
					}
					
					// Make adjustments
					bestBar.symbol = baseSymbol;
					bestBar.volume = totalVolume;
					bestBar.close += adjustment;
					bestBar.open += adjustment;
					bestBar.high += adjustment;
					bestBar.low += adjustment;
//					bestBar.change += adjustment;
//					bestBar.gap += adjustment;
					
					QueryManager.insertOrUpdateIntoBar(bestBar);
					
					lastBestExpiry = bestExpiry;
				}
				
				// Go back one bar
				cStart.setTimeInMillis(CalendarUtils.addBars(cStart, duration, -1).getTimeInMillis());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Combines two or more dated futures contracts at a specific barStart into the continuous contract.
	 * All it does is use the data from the most recent dated contract but sums up the volume and
	 * changes the symbol to drop the date suffix part.
	 * 
	 * @param baseSymbol
	 * @param duration
	 * @param barStart
	 */
	public static void processOneBar(String baseSymbol, BAR_SIZE duration, Calendar barStart) {
		try {
			// Get all the bars for the different contracts that might have this exact start
			ArrayList<Bar> barsAtTime = QueryManager.getAllDatedFuturesBarsAtTimeForBaseSymbol(baseSymbol, duration, barStart);
			
			if (barsAtTime == null || barsAtTime.size() == 0) {
				throw new Exception ("No bars from dated contracts to combine into continuous contract!");
			}
			
			Bar newBar = new Bar(barsAtTime.get(barsAtTime.size() - 1));
			newBar.symbol = newBar.symbol.substring(0, newBar.symbol.indexOf(" "));
			double volumeSum = 0;
			boolean partial = false;
			for (Bar bar : barsAtTime) {
				volumeSum += bar.volume;
				if (bar.partial == true) {
					partial = true;
				}
			}
			newBar.volume = volumeSum;
			newBar.partial = partial;
			QueryManager.insertOrUpdateIntoBar(newBar);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
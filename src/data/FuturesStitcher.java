package data;

import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants.BAR_SIZE;
import dbio.QueryManager;
import utils.CalendarUtils;

public class FuturesStitcher {

	public static void main(String[] args) {
		process("ES", BAR_SIZE.BAR_1H);
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

			String lastBestExpiry = CalendarUtils.getFuturesContractBasedOnRolloverDate(cStart);
			float adjustment = 0;
			while (cStart.after(cMinStart)) {
				// Get all the bars for the different contracts that might have this exact start
				ArrayList<Bar> barsAtTime = QueryManager.getAllBarsAtTimeForBaseSymbol(baseSymbol, duration, cStart);
					
				// Find the best (most appropriate) one and make adjustments to make a new bar with continuity
				String bestExpiry = CalendarUtils.getFuturesContractBasedOnRolloverDate(cStart);
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
						float lastBestExpiryClose = 0f;
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
					bestBar.symbol = baseSymbol + " C";
					bestBar.volume = totalVolume;
					bestBar.close += adjustment;
					bestBar.open += adjustment;
					bestBar.high += adjustment;
					bestBar.low += adjustment;
//					bestBar.change += adjustment;
//					bestBar.gap += adjustment;
					
					QueryManager.insertOrUpdateIntoBar(bestBar);
				}
				
				// Go back one bar
				lastBestExpiry = bestExpiry;
				cStart.setTimeInMillis(CalendarUtils.addBars(cStart, duration, -1).getTimeInMillis());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
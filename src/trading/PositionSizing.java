package trading;

import dbio.QueryManager;

public class PositionSizing {

	public static final int NUM_STANDARD_POSITIONS = 10; // One standard position has a volatility equal to the SP500
	public static final int MIN_POSITION_VALUE = 500;
	
	/**
	 * MVOL Metric = Relative volatility is the mvol of the symbol / the mvol of SPY.
	 * Standard Position Size = Account Value / NUM_STANDARD_POSITIONS
	 * 
	 * Position size gets adjusted from a "standard position size" to a new position size that takes
	 * the relative volatility into account.  So if the standard position was $5000 and the 
	 * MVOL relative volatility of the stock was 2, then the adjusted position size would be
	 * $5000 / 2 = $2500.  
	 * 
	 * @param symbol
	 * @param price
	 * @param commission
	 * @return
	 */
	public static float getPositionSize(String symbol, float price) {
		try {
			float tradingAccountValue = QueryManager.getTradingAccountValue();
			float standardPositionValue = tradingAccountValue / (float)NUM_STANDARD_POSITIONS;
			float cashAvailable = QueryManager.getTradingAccountCash();
			float relativeVolatility = QueryManager.getSymbolRelativeVolatility(symbol);
			float adjustedPositionValue = standardPositionValue / relativeVolatility;
			float estMaxCommission = adjustedPositionValue / 200f;
			
			// Lower the size if it's greater than 15% of the value of my account
			if (adjustedPositionValue > (tradingAccountValue * .15)) {
				adjustedPositionValue = tradingAccountValue * .15f;
			}
			
			// Lower the size if we don't have enough cash
			if (adjustedPositionValue > (cashAvailable - estMaxCommission)) {
				adjustedPositionValue = cashAvailable - estMaxCommission;
			}
			float numShares = adjustedPositionValue / price;
			return numShares;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static float getPositionSizeIgnoreCash(String symbol, float price) {
		try {
			float tradingAccountValue = QueryManager.getTradingAccountValue();
			float standardPositionValue = tradingAccountValue / (float)NUM_STANDARD_POSITIONS;
			float relativeVolatility = QueryManager.getSymbolRelativeVolatility(symbol);
			float adjustedPositionValue = standardPositionValue / relativeVolatility;

			// Lower the size if it's greater than 15% of the value of my account
			if (adjustedPositionValue > (tradingAccountValue * .15)) {
				adjustedPositionValue = tradingAccountValue * .15f;
			}
			
			float numShares = adjustedPositionValue / price;
			return numShares;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
}
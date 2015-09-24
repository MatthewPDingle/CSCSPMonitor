package trading;

public class Commission {

	public static float getIBEstimatedCommission(float numShares, float sharePrice) {
		try {
			float commission = 0f;
			
			// Base commission
			if (numShares > 500) {
				commission = .008f * numShares;
			}
			else {
				commission = .013f * numShares;
			}
			
			// $1.30 minimum
			if (commission < 1.30) commission = 1.30f;
			
			// .5% maximum
			float tradeValue = numShares * sharePrice;
			float maxValue = tradeValue / 200f;
			if (commission > maxValue) commission = maxValue;
			
			return commission;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public  static float getOKCoinEstimatedCommission() {
		return 0f;
	}
}
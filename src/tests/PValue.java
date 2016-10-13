package tests;

public class PValue {

	/**
	 * 2/12 Week .5000	Trade .5000
	 * 2/19 Week .2500	Trade .1190
	 * 2/26 Week .1250	Trade .0948
	 * 3/4	Week .3130	Trade .1147
	 * 3/11 Week .5000	Trade .0274
	 */
	public static void main(String[] args) {
		PValue pValue = new PValue();
		double answer = PValue.calculate(313, 573, .5);
		System.out.println(answer);
	}

	public static double calculate(int iWinners, int numTrades, double expectedWinPercent) {
		int numIterations = 10000;
		
		double winningPercent = iWinners / (double)numTrades;
		
		int randomWinners = 0;
		int randomLosers = 0;
		for (int a = 0; a < numIterations; a++) {
			int numWinners = 0;
			int numLosers = 0;
			for (int t = 0; t < numTrades; t++) {
				if (Math.random() > expectedWinPercent) {
					numWinners++;
				}
				else {
					numLosers++;
				}
			}
			double winRate = numWinners / (double)numTrades;
			if (winRate >= winningPercent) {
				randomWinners++;
			}
			else {
				randomLosers++;
			}
		}
		
		double pValue = randomWinners / (double)numIterations;
		return pValue;
	}
}
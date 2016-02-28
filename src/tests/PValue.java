package tests;

public class PValue {

	public static void main(String[] args) {
		PValue pValue = new PValue();
		double answer = pValue.calculate(21, .6666);
		System.out.println(answer);
	}

	public double calculate(int numTrades, double winningPercent) {
		int numIterations = 1000000;
		
		int randomWinners = 0;
		int randomLosers = 0;
		for (int a = 0; a < numIterations; a++) {
			int numWinners = 0;
			int numLosers = 0;
			for (int t = 0; t < numTrades; t++) {
				if (Math.random() > .5) {
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
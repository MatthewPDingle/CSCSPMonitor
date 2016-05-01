package tests;

import java.util.Calendar;

public class DateRangeChooser {

	private static long MS_WEEK = 604800000l;
	private static long MS_90DAYS = 7776000000l;
	private static long MS_360DAYS = 31104000000l;
	
	public static void main(String[] args) {

		long baseTime = Calendar.getInstance().getTimeInMillis();
		
		long[] trainEnds = new long[10];
		long[] trainStarts = new long[10];
		long[] testEnds = new long[10];
		long[] testStarts = new long[10];
		int[] mods = new int[10];
		
		for (int a = 0; a < 10; a++) {
			testEnds[a] = baseTime - (a * MS_WEEK);
			testStarts[a] = (baseTime - MS_90DAYS) - (a * 4 * MS_WEEK);
			
			trainEnds[a] = testStarts[a] - MS_WEEK;
			trainStarts[a] = (baseTime - MS_360DAYS) - (a * 24 * MS_WEEK);
			
			
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(trainStarts[a]);
			
			Calendar c2 = Calendar.getInstance();
			c2.setTimeInMillis(trainEnds[a]);
			
		
			
			System.out.println(c.getTime().toString() + "\t" + c2.getTime().toString());
			int duration = daysBetween(c, c2);
			int mod = duration / 4;
			mod = 5 * (int)(Math.ceil(Math.abs(mod / 5)));
			mods[a] = mod;
			
			System.out.println(duration + ", " + mod);
		}
	}

	public static int daysBetween(Calendar d1, Calendar d2){
        return (int)( (d2.getTime().getTime() - d1.getTime().getTime()) / (1000 * 60 * 60 * 24));
	}
}
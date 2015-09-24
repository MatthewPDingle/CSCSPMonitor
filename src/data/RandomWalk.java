package data;

import java.util.Calendar;

import constants.Constants.BAR_SIZE;
import dbio.QueryManager;
import utils.CalendarUtils;

public class RandomWalk {

	private static final double CONST = .5d;
	
	private static double e0 = 0;
	private static double e1 = 0;
	private static double e2 = 0;
	private static double e3 = 0;
	private static double e4 = 0;
	
	public static void main(String[] args) {

		double lastClose = 100;
		Calendar start = Calendar.getInstance();
		start.set(Calendar.YEAR, 2014);
		start.set(Calendar.MONTH, 0);
		start.set(Calendar.DAY_OF_MONTH, 1);
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		start.set(Calendar.MILLISECOND, 0);
		
		for (int a = 0; a < 60000; a++) {
			double close = getClose(lastClose);
			double open = getOpen(lastClose);
			double high = getHigh(open, close);
			double low = getLow(open, close);
			double vwap = (open + close + high + low) / 4f;
			double volume = getVolume();
			double gap = open - lastClose;
			double change = close - lastClose;
			int numTrades = getNumTrades(volume);
			
			Calendar end = Calendar.getInstance();
			end.setTimeInMillis(start.getTimeInMillis());
			end = CalendarUtils.addBars(start, BAR_SIZE.BAR_15M, 1);

			Bar bar = new Bar("RandomWalk", (float)open, (float)close, (float)high, (float)low, (float)vwap, 
					(float)volume, numTrades, (float)change, (float)gap, start, end, BAR_SIZE.BAR_15M, false);
			System.out.println(bar);		
//			QueryManager.insertOrUpdateIntoBar(bar);
			
			start = CalendarUtils.addBars(start, BAR_SIZE.BAR_15M, 1);
			lastClose = close;
		}
	}
	
	private static double getClose(double lastClose) {
		e0 = 0;
		for (int b = 0; b < 3; b++) {
			e0 += ((Math.random() - CONST) * 10);
		}
		
//		double re1 = Math.random();
//		if (re1 < .1) {
//			e1 = 0;
//			for (int b = 0; b < 10; b++) {
//				e1 += Math.random() - CONST;
//			}
//		}
//		double re2 = Math.random();
//		if (re2 < .01) {
//			e2 = 0;
//			for (int b = 0; b < 10; b++) {
//				e2 += Math.random() - CONST;
//			}
//		}
//		double re3 = Math.random();
//		if (re3 < .001) {
//			e3 = 0;
//			for (int b = 0; b < 10; b++) {
//				e3 += (Math.random() - CONST) * .5d;
//			}
//		}
//		double re4 = Math.random();
//		if (re4 < .0001) {
//			e4 = 0;
//			for (int b = 0; b < 10; b++) {
//				e4 += (Math.random() - CONST) * .2d;
//			}
//		}
		
		double rb1 = Math.random();
		double b1 = 0;
		if (rb1 < .1) {
			b1 = (Math.random() - CONST) * 10d;
		}
		
		double rb2 = Math.random();
		double b2 = 0;
		if (rb2 < .01) {
			b2 = (Math.random() - CONST) * 50d;
		}
		
		double rb3 = Math.random();
		double b3 = 0;
		if (rb3 < .001) {
			b3 = (Math.random() - CONST) * 200d;
		}
		
		double rb4 = Math.random();
		double b4 = 0;
		if (rb4 < .0001) {
			b4 = (Math.random() - CONST) * 2000d;
		}
		
		double s = e0 + e1 + e2 + e3 + e4 + b1 + b2 + b3 + b4;
		
		double close = lastClose + (lastClose * s / 10000d);
	
		return close;
	}

	private static double getOpen(double lastClose) {
		double open = lastClose;

		double re1 = Math.random();
		if (re1 < .4) {
			double t1 = 0;
			for (int b = 0; b < 10; b++) {
				t1 += Math.random() - .5;
			}
			open = lastClose + (lastClose * t1 / 1000d);
		}
		
		return open;
	}
	
	private static double getHigh(double open, double close) {
		double high = close;
		if (open > high) {
			high = open;
		}

		double re1 = Math.random();
		if (re1 < .9) {
			double t1 = 0;
			for (int b = 0; b < 2; b++) {
				t1 += Math.random();
			}
			high = high + (high * t1 / 1000d);
		}
		
		return high;
	}
	
	private static double getLow(double open, double close) {
		double low = close;
		if (open < low) {
			low = open;
		}

		double re1 = Math.random();
		if (re1 < .9) {
			double t1 = 0;
			for (int b = 0; b < 2; b++) {
				t1 += Math.random() - 1;
			}
			low = low + (low * t1 / 1000d);
		}
		
		return low;
	}
	
	private static double getVolume() {
		double volume = 0;
		double re1 = Math.random();
		if (re1 < .99) {
			for (int b = 0; b < 10; b++) {
				volume += Math.pow(Math.random() * 10, Math.random() * 3.5);
			}
		}
		return volume;
	}
	
	private static int getNumTrades(double volume) {
		int numTrades = (int)Math.floor(volume / (Math.random() * 20d));
		return numTrades;
	}
}
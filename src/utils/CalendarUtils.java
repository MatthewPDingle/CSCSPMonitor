package utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.Constants;
import constants.Constants.BAR_SIZE;

public class CalendarUtils {

	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		Calendar c2 = addBars(c, BAR_SIZE.BAR_15M, 1);

		int numBars = getNumBars(c, c2, BAR_SIZE.BAR_15M);
		System.out.println(numBars);
	}
	
	public static long difference(Calendar c1, Calendar c2, int unit) { 
		differenceCheckUnit(unit); 
		Map<Integer, Long> unitEstimates = differenceGetUnitEstimates(); 
		Calendar first = (Calendar) c1.clone(); 
		Calendar last = (Calendar) c2.clone(); 
		long difference = c2.getTimeInMillis() - c1.getTimeInMillis(); 
		long unitEstimate = unitEstimates.get(unit).longValue(); 
		long increment = (long) Math.floor((double) difference / (double) unitEstimate); 
		increment = Math.max(increment, 1); long total = 0; 
		while (increment > 0) { 
			add(first, unit, increment); 
			if (first.after(last)) { 
				add(first, unit, increment * -1); 
				increment = (long) Math.floor(increment / 2); 
			} 
			else { 
				total += increment; 
			} 
		} 
		return total; 
	} 
	
	private static Map<Integer, Long> differenceGetUnitEstimates() { 
		Map<Integer, Long> unitEstimates = new HashMap<Integer, Long>(); 
		unitEstimates.put(Calendar.YEAR, 1000l * 60 * 60 * 24 * 365); 
		unitEstimates.put(Calendar.MONTH, 1000l * 60 * 60 * 24 * 30); 
		unitEstimates.put(Calendar.DAY_OF_MONTH, 1000l * 60 * 60 * 24); 
		unitEstimates.put(Calendar.HOUR_OF_DAY, 1000l * 60 * 60); 
		unitEstimates.put(Calendar.MINUTE, 1000l * 60); 
		unitEstimates.put(Calendar.SECOND, 1000l); 
		unitEstimates.put(Calendar.MILLISECOND, 1l); 
		return unitEstimates; 
	} 

	private static void differenceCheckUnit(int unit) { 
		List<Integer> validUnits = new ArrayList<Integer>(); 
		validUnits.add(Calendar.YEAR); 
		validUnits.add(Calendar.MONTH); 
		validUnits.add(Calendar.DAY_OF_MONTH); 
		validUnits.add(Calendar.HOUR_OF_DAY); 
		validUnits.add(Calendar.MINUTE); 
		validUnits.add(Calendar.SECOND); 
		validUnits.add(Calendar.MILLISECOND); 
		if (!validUnits.contains(unit)) { 
			throw new RuntimeException( "CalendarUtils.difference one of these units Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND." ); 
		} 
	} 

	public static void add(Calendar c, int unit, long increment) { 
		while (increment > Integer.MAX_VALUE) { 
			c.add(unit, Integer.MAX_VALUE); 
			increment -= Integer.MAX_VALUE; 
		} 
		c.add(unit, (int) increment); 
	} 
	
	/**
	 * Adds or Subtracts a given number of bars to a calendar and returns a new calendar.
	 * 
	 * @param cIn
	 * @param barSize
	 * @param numBars - How many bars you want to add.  Use a negative number to subtract
	 * @return
	 */
	public static Calendar addBars(Calendar cIn, Constants.BAR_SIZE barSize, int numBars) {
		Calendar cOut = Calendar.getInstance();
		cOut.setTime(cIn.getTime());

		switch (barSize) {
			case BAR_1M:
				cOut.add(Calendar.MINUTE, numBars);
				break;
			case BAR_2M:
				cOut.add(Calendar.MINUTE, 2 * numBars);
				break;
			case BAR_5M:
				cOut.add(Calendar.MINUTE, 5 * numBars);
				break;
			case BAR_10M:
				cOut.add(Calendar.MINUTE, 10 * numBars);
				break;
			case BAR_15M:
				cOut.add(Calendar.MINUTE, 15 * numBars);
				break;
			case BAR_30M:
				cOut.add(Calendar.MINUTE, 30 * numBars);
				break;
			case BAR_1H:
				cOut.add(Calendar.HOUR_OF_DAY, 1 * numBars);
				break;
			case BAR_2H:
				cOut.add(Calendar.HOUR_OF_DAY, 2 * numBars);
				break;
			case BAR_4H:
				cOut.add(Calendar.HOUR_OF_DAY, 4 * numBars);
				break;
			case BAR_6H:
				cOut.add(Calendar.HOUR_OF_DAY, 6 * numBars);
				break;
			case BAR_8H:
				cOut.add(Calendar.HOUR_OF_DAY, 8 * numBars);
				break;
			case BAR_12H:
				cOut.add(Calendar.HOUR_OF_DAY, 12 * numBars);
				break;
			case BAR_1D:
				cOut.add(Calendar.HOUR_OF_DAY, 24 * numBars);
				break;
		}

		return cOut;
	}
	
	public static Calendar getBarStart(Calendar c, Constants.BAR_SIZE barSize) {
		Calendar periodStart = Calendar.getInstance();
		periodStart.setTime(c.getTime());
		periodStart.set(Calendar.SECOND, 0);
		periodStart.set(Calendar.MILLISECOND, 0);
		Calendar periodEnd = Calendar.getInstance();
		periodEnd.setTime(periodStart.getTime());
		
		try {
			int unroundedMinute = 0;
			int unroundedHour = 0;
			int remainder = 0;
			switch (barSize) {
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_2M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 2;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 2);
					break;
				case BAR_5M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 5;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 5);
					break;
				case BAR_10M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 10;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 10);
					break;
				case BAR_15M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 15;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 15);
					break;
				case BAR_30M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 30;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 30);
					break;
				case BAR_1H:
					periodStart.set(Calendar.MINUTE, 0);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case BAR_2H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 2;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 2);
					break;
				case BAR_4H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 4;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 4);
					break;
				case BAR_6H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 6;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 6);
					break;
				case BAR_8H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 8;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 8);
					break;
				case BAR_12H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 12;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 12);
					break;
				case BAR_1D:
					periodStart.set(Calendar.MINUTE, 0);
					periodStart.set(Calendar.HOUR_OF_DAY, 0);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 24);
					break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return periodStart;
	}
	
	public static Calendar getBarEnd(Calendar c, Constants.BAR_SIZE barSize) {
		Calendar periodStart = Calendar.getInstance();
		periodStart.setTime(c.getTime());
		periodStart.set(Calendar.SECOND, 0);
		periodStart.set(Calendar.MILLISECOND, 0);	
		Calendar periodEnd = Calendar.getInstance();
		periodEnd.setTime(periodStart.getTime());
		
		try {
			int unroundedMinute = 0;
			int unroundedHour = 0;
			int remainder = 0;
			switch (barSize) {
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_2M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 2;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 2);
					break;
				case BAR_5M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 5;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 5);
					break;
				case BAR_10M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 10;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 10);
					break;
				case BAR_15M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 15;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 15);
					break;
				case BAR_30M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 30;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 30);
					break;
				case BAR_1H:
					periodStart.set(Calendar.MINUTE, 0);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case BAR_2H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 2;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 2);
					break;
				case BAR_4H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 4;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 4);
					break;
				case BAR_6H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 6;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 6);
					break;
				case BAR_8H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 8;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 8);
					break;
				case BAR_12H:
					periodStart.set(Calendar.MINUTE, 0);
					unroundedHour = periodStart.get(Calendar.HOUR_OF_DAY);
					remainder = unroundedHour % 12;
					periodStart.add(Calendar.HOUR_OF_DAY, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 12);
					break;
				case BAR_1D:
					periodStart.set(Calendar.MINUTE, 0);
					periodStart.set(Calendar.HOUR_OF_DAY, 0);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.HOUR_OF_DAY, 24);
					break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return periodEnd;
	}
	
	/**
	 * c1 and c2 are rounded to their respective bar start times when doing this calculation.
	 * 
	 * @param c1
	 * @param c2
	 * @param barSize
	 * @return
	 */
	public static int getNumBars(Calendar c1, Calendar c2, Constants.BAR_SIZE barSize) {
		try {
			Calendar c1Start = getBarStart(c1, barSize);
			Calendar c2Start = getBarStart(c2, barSize);
			long d = difference(c1Start, c2Start, Calendar.MINUTE);
			
			Calendar now = Calendar.getInstance();
			Calendar nowStart = getBarStart(now, barSize);
			Calendar endStart = getBarEnd(now, barSize);
			long barD = difference(nowStart, endStart, Calendar.MINUTE);
			
			return (int)(d / barD);
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static boolean areSame(Calendar c1, Calendar c2) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
			String c1s = sdf.format(c1.getTime());
			String c2s = sdf.format(c2.getTime());
			if (c1s.equals(c2s)) {
				return true;
			}
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * Converts a SQL Date object into yyyy-MM-dd format
	 * (usually so it can be inserted into a DB table.)
	 * 
	 * @param c
	 * @return
	 */
	public static String getSqlDateString(Calendar c) {
		try {
			String year = new Integer(c.get(Calendar.YEAR)).toString();
			String month = new Integer(c.get(Calendar.MONTH) + 1).toString();
			String day = new Integer(c.get(Calendar.DATE)).toString();
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1) 
				day = "0" + day;
			String date = year + "-" + month + "-" + day;
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Converts a SQL Date object into yyyy-MM-dd hh:mm:ss format
	 * (usually so it can be inserted into a DB table.)
	 * 
	 * @param c
	 * @return
	 */
	public static String getSqlDateTimeString(Calendar c) {
		try {
			String year = new Integer(c.get(Calendar.YEAR)).toString();
			String month = new Integer(c.get(Calendar.MONTH) + 1).toString();
			String day = new Integer(c.get(Calendar.DATE)).toString();
			String hour = new Integer(c.get(Calendar.HOUR_OF_DAY)).toString();
			String minute = new Integer(c.get(Calendar.MINUTE)).toString();
			String second = new Integer(c.get(Calendar.SECOND)).toString();
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1) 
				day = "0" + day;
			if (hour.length() == 1) 
				hour = "0" + hour;
			if (minute.length() == 1)
				minute = "0" + minute;
			if (second.length() == 1)
				second = "0" + second;
			String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Returns MM-dd-yyyy
	 * 
	 * @param c
	 * @return
	 */
	public static String getGUIDateString(Calendar c) {
		try {
			String year = new Integer(c.get(Calendar.YEAR)).toString();
			String month = new Integer(c.get(Calendar.MONTH) + 1).toString();
			String day = new Integer(c.get(Calendar.DATE)).toString();
			if (month.length() == 1)
				month = "0" + month;
			if (day.length() == 1) 
				day = "0" + day;
			String date = month + "-" + day + "-" + year;
			return date;
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
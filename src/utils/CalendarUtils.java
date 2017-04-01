package utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import constants.Constants;

public class CalendarUtils {

	public static void main(String[] args) {
//		Calendar c = Calendar.getInstance();
//		c.set(Calendar.MONTH, 0);
//		c.set(Calendar.DAY_OF_MONTH, 1);
//		
//		for (int a = 0; a <= 365; a++) {
//			System.out.println(c.getTime().toString() + "\t\t" + getFuturesContractExpiry(c));
//			c.add(Calendar.DATE, 1);
//		}
		
		String expiry = "201603";
		System.out.println(getFuturesStart("ES", expiry).getTime().toString());
		System.out.println(getFuturesEnd("ES", expiry).getTime().toString());
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
			case BAR_30S:
				cOut.add(Calendar.SECOND, 30 * numBars);
				break;
			case BAR_1M:
				cOut.add(Calendar.MINUTE, numBars);
				break;
			case BAR_3M:
				cOut.add(Calendar.MINUTE, 3 * numBars);
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
			int unroundedSecond = 0;
			int unroundedMinute = 0;
			int unroundedHour = 0;
			int remainder = 0;
			switch (barSize) {
				case BAR_30S:
					unroundedSecond = periodStart.get(Calendar.SECOND);
					remainder = unroundedSecond % 30;
					periodStart.add(Calendar.SECOND, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.SECOND, 30);
					break;
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_3M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 3;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 3);
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
			int unroundedSecond = 0;
			int unroundedMinute = 0;
			int unroundedHour = 0;
			int remainder = 0;
			switch (barSize) {
				case BAR_15S:
					unroundedSecond = periodStart.get(Calendar.SECOND);
					remainder = unroundedSecond % 15;
					periodStart.add(Calendar.SECOND, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.SECOND, 15);
					break;
				case BAR_30S:
					unroundedSecond = periodStart.get(Calendar.SECOND);
					remainder = unroundedSecond % 30;
					periodStart.add(Calendar.SECOND, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.SECOND, 30);
					break;
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_3M:
					unroundedMinute = periodStart.get(Calendar.MINUTE);
					remainder = unroundedMinute % 3;
					periodStart.add(Calendar.MINUTE, -remainder);
					periodEnd.setTime(periodStart.getTime());
					periodEnd.add(Calendar.MINUTE, 3);
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
	
	public static int daysBetween(Calendar d1, Calendar d2){
        return (int)( (d2.getTime().getTime() - d1.getTime().getTime()) / (1000 * 60 * 60 * 24));
	}
	
	/**
	 * Returns YYYYMM
	 * 
	 * For e-mini, rollover is 2nd Thursday of March, June, Sept, Dec
	 * 
	 * @param c
	 * @return
	 */
	public static String getFuturesContractExpiry(Calendar c) {
		String expiry = "";
		try {	
			int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
			int month = c.get(Calendar.MONTH) + 1;
			int year = c.get(Calendar.YEAR);
			
			if (month == 1 || month == 2) {
				expiry = "" + year + "03";
			}
			if (month == 3) {
				LocalDate ldMarchRollover = getNDayOfMonth(DateTimeConstants.THURSDAY, 2, month, year); // 2nd Thursday of March
				if (dayOfMonth <= ldMarchRollover.getDayOfMonth()) {
					expiry = "" + year + "03";
				}
				else {
					expiry = "" + year + "06";
				}
			}
			if (month == 4 || month == 5) {
				expiry = "" + year + "06";
			}
			if (month == 6) {
				LocalDate ldMarchRollover = getNDayOfMonth(DateTimeConstants.THURSDAY, 2, month, year); // 2nd Thursday of June
				if (dayOfMonth <= ldMarchRollover.getDayOfMonth()) {
					expiry = "" + year + "06";
				}
				else {
					expiry = "" + year + "09";
				}
			}
			if (month == 7 || month == 8) {
				expiry = "" + year + "09";
			}
			if (month == 9) {
				LocalDate ldMarchRollover = getNDayOfMonth(DateTimeConstants.THURSDAY, 2, month, year); // 2nd Thursday of Sept
				if (dayOfMonth <= ldMarchRollover.getDayOfMonth()) {
					expiry = "" + year + "09";
				}
				else {
					expiry = "" + year + "12";
				}
			}
			if (month == 10 || month == 11) {
				expiry = "" + year + "12";
			}
			if (month == 12) {
				LocalDate ldMarchRollover = getNDayOfMonth(DateTimeConstants.THURSDAY, 2, month, year); // 2nd Thursday of Dec
				if (dayOfMonth <= ldMarchRollover.getDayOfMonth()) {
					expiry = "" + year + "12";
				}
				else {
					expiry = "" + (year + 1) + "03";
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return expiry;
	}
	
	/**
	 * @param expiry	YYYYMM
	 * @return
	 */
	public static Calendar getFuturesStart(String contract, String expiry) {
		try {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MILLISECOND, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);
			
			String sYear = expiry.substring(0, 4);
			String sMonth = expiry.substring(4);
			
			int year = Integer.parseInt(sYear);
			int month = Integer.parseInt(sMonth);
			
			month = month - 6; // 12 for quarterlies, 6 for monthlies
			if (month <= 0) {
				month += 12;
				year--;
			}
			
			c.set(Calendar.MONTH, (month - 1));
			c.set(Calendar.YEAR, year);
			
			return c;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param expiry	YYYYMM
	 * @return
	 */
	public static Calendar getFuturesEnd(String contract, String expiry) {
		try {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MILLISECOND, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);
			
			String sYear = expiry.substring(0, 4);
			String sMonth = expiry.substring(4);
			
			int year = Integer.parseInt(sYear);
			int month = Integer.parseInt(sMonth);
			
			month = month + 1;
			if (month == 13) {
				month = 1;
				year++;
			}
			
			c.set(Calendar.MONTH, (month - 1));
			c.set(Calendar.YEAR, year);
			
			return c;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static LocalDate getNDayOfMonth(int dayweek,int nthweek,int month,int year)  {
	   LocalDate d = new LocalDate(year, month, 1).withDayOfWeek(dayweek);
	   if(d.getMonthOfYear() != month) d = d.plusWeeks(1);
	   return d.plusWeeks(nthweek-1);
	}

}
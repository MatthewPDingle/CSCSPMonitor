package tasks;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

import dbio.QueryManager;
import utils.Formatting;

public class ModelSelecting {

	public static void main(String[] args) {
		try {
			// Set these variables
			String start = "1/5/2014 00:00:00";
//			String start = "07/24/2016 00:00:00"; // Make sure dates are Sundays
			String end = "07/19/2015 00:00:00";
			int maxNumTopModels = 10;
			double minSellMetricValue = 0.5d;
			double maxSellMetricValue = 0.9d;
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			startC.setTimeInMillis(Formatting.sdfMMDDYYYY_HHMMSS.parse(start).getTime());
			endC.setTimeInMillis(Formatting.sdfMMDDYYYY_HHMMSS.parse(end).getTime());
			Calendar baseDateStart = Calendar.getInstance();
			baseDateStart.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDateEnd = Calendar.getInstance();
			baseDateEnd.setTimeInMillis(endC.getTimeInMillis());
			
			while (baseDateStart.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
				HashSet<Integer> topModelIDs = new HashSet<Integer>();
				// Add up to one model per sellmetricvalue
				for (double d = minSellMetricValue; d <= maxSellMetricValue + .01; d += .1d) {
					d = new Double(Formatting.df2.format(d));
					topModelIDs.addAll(QueryManager.selectTopModels(baseDateStart, d, d, .01, "", 2));
				}
				// Then add more up to X within the range of allowable sellmetricvalues
				HashSet<Integer> topIDs = QueryManager.selectTopModels(baseDateStart, minSellMetricValue, maxSellMetricValue, .01, "", maxNumTopModels);
				for (Integer id : topIDs) {
					if (topModelIDs.size() < maxNumTopModels) {
						topModelIDs.add(id);
					}
				}
				System.out.println(Formatting.sdfMMDDYYYY_HHMMSS.format(baseDateStart.getTime()) + " adding " + topModelIDs.size());
				QueryManager.setModelsToUseInBacktest(topModelIDs);
				
				baseDateStart.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
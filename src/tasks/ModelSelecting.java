package tasks;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

import dbio.QueryManager;

public class ModelSelecting {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	public static void main(String[] args) {
		try {
			// Set these variables
			String start = "07/10/2016 00:00:00";
			String end = "07/10/2016 00:00:00";
			int maxNumTopModels = 10;
			double minSellMetricValue = 0.5d;
			double maxSellMetricValue = 0.9d;
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			startC.setTimeInMillis(sdf.parse(start).getTime());
			endC.setTimeInMillis(sdf.parse(end).getTime());
			Calendar baseDateStart = Calendar.getInstance();
			baseDateStart.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDateEnd = Calendar.getInstance();
			baseDateEnd.setTimeInMillis(endC.getTimeInMillis());
			
			while (baseDateStart.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
				HashSet<Integer> topModelIDs = new HashSet<Integer>();
				// Add up to one model per sellmetricvalue
				for (double d = minSellMetricValue; d <= maxSellMetricValue + .01; d += .1d) {
					d = new Double(df2.format(d));
					topModelIDs.addAll(QueryManager.selectTopModels(baseDateStart, d, d, .01, 1));
				}
				// Then add more up to X within the range of allowable sellmetricvalues
				HashSet<Integer> topIDs = QueryManager.selectTopModels(baseDateStart, minSellMetricValue, maxSellMetricValue, .01, maxNumTopModels);
				for (Integer id : topIDs) {
					if (topModelIDs.size() < maxNumTopModels) {
						topModelIDs.add(id);
					}
				}
				
				QueryManager.setModelsToUseInBacktest(topModelIDs);
				
				baseDateStart.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package tasks;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ml.ARFF;

public class ModelBuilding {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	public static void main(String[] args) {
		try {
			// Set time period
			String start = "07/10/2016 00:00:00";
			String end = "07/10/2016 00:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(sdf.parse(start).getTime());
			endC.setTimeInMillis(sdf.parse(end).getTime());
	
			// Setup base dates for backtests
			Calendar baseDateStart = Calendar.getInstance();
			baseDateStart.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDateEnd = Calendar.getInstance();
			baseDateEnd.setTimeInMillis(endC.getTimeInMillis());
			
			// Build historical models
			while (baseDateStart.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
				ARFF.buildBacktestModels(baseDateStart);
				baseDateStart.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
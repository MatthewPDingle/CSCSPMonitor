package tasks;

import java.util.Calendar;

import ml.ARFF;
import utils.Formatting;

public class ModelBuilding {

	public static void main(String[] args) {
		try {
			// Set time period (The end of the test period)
			String start = "10/01/2012 00:00:00"; // "10/01/2012 00:00:00"; // 01/05/2014
			String end = "07/31/2016 00:00:00"; //"07/31/2016 00:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(start).getTime());
			endC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(end).getTime());
	
			// Setup base dates for backtests
			Calendar baseDateStart = Calendar.getInstance();
			baseDateStart.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDateEnd = Calendar.getInstance();
			baseDateEnd.setTimeInMillis(endC.getTimeInMillis());
			
			// Load a bunch of shit in memory so I don't have to keep loading it.
			String rawStart = "01/01/2009 00:00:00"; // 06/01/2010
			Calendar rawStartC = Calendar.getInstance();
			rawStartC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(rawStart).getTime());
			ARFF arff = new ARFF();
			arff.loadRawCompleteSet(rawStartC, endC);
			
			// Build historical models
			while (baseDateStart.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
				System.out.println("Building Models For BaseDate: " + baseDateStart.getTime().toString());
				arff.buildBacktestModels(baseDateStart);
				baseDateStart.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
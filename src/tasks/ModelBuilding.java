package tasks;

import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import ml.ARFF;
import utils.Formatting;

public class ModelBuilding {

	public static void main(String[] args) {
		try {
			// Set time period (The end of the test period)
			String start = "09/29/2012 00:00:00"; 
			String end = "02/11/2017 00:00:00"; 
			
//			String start = "01/28/2017 00:00:00"; 
//			String end = "01/28/2017 00:00:00";
			
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
			
			// Set metricSetName
			String metricSetName = "Test 23.12752";
//			String metricSetName = "Test 29.4190";
			Constants.setMetricSet(metricSetName);
			
			// Setup BarKeys
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bkEURUSD1H = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bkGBPUSD1H = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			BarKey bkEURGBP1H = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			
			BarKey bkEURUSD2H = new BarKey("EUR.USD", BAR_SIZE.BAR_2H);
			BarKey bkGBPUSD2H = new BarKey("GBP.USD", BAR_SIZE.BAR_2H);
			BarKey bkEURGBP2H = new BarKey("EUR.GBP", BAR_SIZE.BAR_2H);
			
			barKeys.add(bkEURUSD1H);
//			barKeys.add(bkGBPUSD1H);
//			barKeys.add(bkEURGBP1H);
			
			ARFF arff = new ARFF();
			arff.loadRawCompleteSet(rawStartC, endC, barKeys);
			
			// Build historical models
			while (baseDateStart.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
				System.out.println("Building Models For BaseDate: " + baseDateStart.getTime().toString());
				arff.buildBacktestModels(baseDateStart, metricSetName, barKeys);
				baseDateStart.add(Calendar.WEEK_OF_YEAR, 1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
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
//			String start = "09/29/2012 00:00:00"; 
//			String end = "03/18/2017 00:00:00"; 
			
			String start = "08/14/2016 00:00:00"; 
			String end = "05/06/2017 00:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(Formatting.sdfMMDDYYYY_HHMMSS.parse(start).getTime());
			endC.setTimeInMillis(Formatting.sdfMMDDYYYY_HHMMSS.parse(end).getTime());
	
			// Setup base dates for backtests
			Calendar baseDateStart = Calendar.getInstance();
			baseDateStart.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDateEnd = Calendar.getInstance();
			baseDateEnd.setTimeInMillis(endC.getTimeInMillis());
			
			// Load a bunch of shit in memory so I don't have to keep loading it.
			String rawStart = "01/01/2009 00:00:00"; // 06/01/2010
			Calendar rawStartC = Calendar.getInstance();
			rawStartC.setTimeInMillis(Formatting.sdfMMDDYYYY_HHMMSS.parse(rawStart).getTime());
			
			// Set metricSetName
//			String metricSetName = "Test 23.12752"; // Evenly distributed Base Dates for 1H Bars
//			String metricSetName = "Test 28.2990"; // 2013 Base Dates for 1H Bars
//			String metricSetName = "Test 29.4190"; // 2016 Base Dates for 1H Bars
//			String metricSetName = "Test 30.3394"; // For 2/2017 Testing for 1H Bars
//			String metricSetName = "Test 31.5828"; // 2013 Base Dates for 2H Bars (don't use)
//			String metricSetName = "Test 32.110504"; // ES C 1H
//			String metricSetName = "Test 33.45060"; // ZN C 1H
//			String metricSetName = "Test 34.22962"; // CL C 1H
			String metricSetName = "Test 35.2299"; // ZN C 2H
			Constants.setMetricSet(metricSetName);
			
			// Setup BarKeys
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bkEURUSD1H = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bkGBPUSD1H = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			BarKey bkEURGBP1H = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			
			BarKey bkEURUSD2H = new BarKey("EUR.USD", BAR_SIZE.BAR_2H);
			BarKey bkGBPUSD2H = new BarKey("GBP.USD", BAR_SIZE.BAR_2H);
			BarKey bkEURGBP2H = new BarKey("EUR.GBP", BAR_SIZE.BAR_2H);
			
			BarKey bkESC1H = new BarKey("ES C", BAR_SIZE.BAR_1H);
			BarKey bkZNC1H = new BarKey("ZN C", BAR_SIZE.BAR_1H);
			BarKey bkCLC1H = new BarKey("CL C", BAR_SIZE.BAR_1H);
			
			BarKey bkESC2H = new BarKey("ES C", BAR_SIZE.BAR_2H);
			BarKey bkZNC2H = new BarKey("ZN C", BAR_SIZE.BAR_2H);
			BarKey bkCLC2H = new BarKey("CL C", BAR_SIZE.BAR_2H);
			
//			barKeys.add(bkEURUSD2H);
//			barKeys.add(bkGBPUSD1H);
//			barKeys.add(bkEURGBP1H);
			barKeys.add(bkZNC2H);
			
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
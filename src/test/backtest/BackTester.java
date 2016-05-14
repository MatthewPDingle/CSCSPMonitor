package test.backtest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.BarWithMetricData;
import data.Model;
import dbio.QueryManager;
import trading.TradingSingleton;

public class BackTester {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	public static void main(String[] args) {
		try {
			// Set time period
			String start = "05/01/2016 00:00:00";
			String end = "05/12/2016 16:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(sdf.parse(start).getTime());
			endC.setTimeInMillis(sdf.parse(end).getTime());
			
			// Set BarKey(s) on which this backtest will run
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bk = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
			barKeys.add(bk);
			
			// Load bar & metric data
			ArrayList<BarWithMetricData> barWMDList = QueryManager.loadMetricSequenceHashForBackTests(barKeys, startC, endC);
			
			// Load models
			ArrayList<Model> models = QueryManager.getModels("WHERE useinbacktests = true");

			// Setup the TradingSingleton and IBEngine1
			TradingSingleton ts = TradingSingleton.getInstance();
			ts.setModelsPath("weka/models");
			for (Model model : models) {
				ts.addModel(model);
			}
			
			ts.setBacktestBarWMDList(bk, barWMDList);
			
			ts.setRunning(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadBarAndMetricData(BarKey bk) {
		try {
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
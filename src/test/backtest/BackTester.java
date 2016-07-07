package test.backtest;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.BarWithMetricData;
import data.MetricKey;
import data.Model;
import data.downloaders.interactivebrokers.IBConstants;
import dbio.QueryManager;
import ml.ARFF;
import trading.TradingSingleton;

public class BackTester {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	private static DecimalFormat df5 = new DecimalFormat("#.#####");
	
	private static ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
	private static ArrayList<BarWithMetricData> barWMDList = null;
	private static int barWMDIndex = 0;
	private static String runName = null;
	private static boolean adjustStops = false;
	private static Calendar currentBaseDate = Calendar.getInstance();
	
	private static Double minSellMetricValue = null;
	private static Double maxSellMetricValue = null;
	private static int maxNumTopModels = 10;
	
	public static double CHANCE_OF_OPEN_ORDER_BEING_FILLED = .8d;
	
	public static void main(String[] args) {
		try {
			System.out.println("Loading data...");
			
			// Set time period
			String start = "1/5/2014 00:00:00";
			String end = "7/3/2016 00:00:00";
//			String start = "07/3/2016 00:00:00";
//			String end = "07/3/2016 00:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(sdf.parse(start).getTime());
			endC.setTimeInMillis(sdf.parse(end).getTime());

			currentBaseDate.setTimeInMillis(startC.getTimeInMillis());
			
			// Setup base dates for backtests
			Calendar baseDate1 = Calendar.getInstance();
			baseDate1.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDate2 = Calendar.getInstance();
			baseDate2.setTimeInMillis(startC.getTimeInMillis());
			Calendar baseDateEnd = Calendar.getInstance();
			baseDateEnd.setTimeInMillis(endC.getTimeInMillis());
			
			// Build historical models
//			while (baseDate1.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
//				ARFF.buildBacktestModels(baseDate1);
//				baseDate1.add(Calendar.WEEK_OF_YEAR, 1);
//			}
			
			// Select top historical models
//			maxNumTopModels = 10;
//			minSellMetricValue = 0.5d;
//			maxSellMetricValue = 0.9d;
//			while (baseDate2.getTimeInMillis() <= baseDateEnd.getTimeInMillis()) {
//				HashSet<Integer> topModelIDs = new HashSet<Integer>();
//				// Add up to one model per sellmetricvalue
//				for (double d = minSellMetricValue; d <= maxSellMetricValue + .01; d += .1d) {
//					d = new Double(df2.format(d));
//					topModelIDs.addAll(QueryManager.selectTopModels(baseDate2, d, d, .01, 1));
//				}
//				// Then add more up to X
//				HashSet<Integer> topIDs = QueryManager.selectTopModels(baseDate2, minSellMetricValue, maxSellMetricValue, .01, maxNumTopModels);
//				for (Integer id : topIDs) {
//					if (topModelIDs.size() < maxNumTopModels) {
//						topModelIDs.add(id);
//					}
//				}
//				
//				QueryManager.setModelsToUseInBacktest(topModelIDs);
//				
//				baseDate2.add(Calendar.WEEK_OF_YEAR, 1);
//			}
	
			// Run Backtest
			// Set the backtest info
//			adjustStops = false;
//			maxNumTopModels = 10;
//			minSellMetricValue = 0.5d;
//			maxSellMetricValue = 0.9d;
//			runName = "060 - Rolling " + maxNumTopModels + " Models - 30 Month - .53 - No Stop Adjust - Realistic Positioning - 0.5 - 0.9 SMV - No Closeout 15D Expiration";
//			
//			// Set BarKey(s) on which this backtest will run
//			BarKey bk = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
//			barKeys.add(bk);
//
//			// Load bar & metric data
//			barWMDList = QueryManager.loadMetricSequenceHashForBackTests(barKeys, startC, endC);
//
//			// Setup the TradingSingleton and IBEngine1
//			TradingSingleton ts = TradingSingleton.getInstance();
//			ts.setModelsPath("weka/models");
//			ts.setBacktestBarWMDList(bk, barWMDList);
//		
//			// Setup initial top models
//			HashSet<Integer> topModelIDs = new HashSet<Integer>();
//			// Add up to one model per sellmetricvalue
//			for (double d = minSellMetricValue; d <= maxSellMetricValue + .01; d += .1d) {
//				d = new Double(df2.format(d));
//				topModelIDs.addAll(QueryManager.selectTopModels(baseDate2, d, d, .01, 1));
//			}
//			// Then add more up to x
//			HashSet<Integer> top15IDs = QueryManager.selectTopModels(baseDate2, minSellMetricValue, maxSellMetricValue, .01, maxNumTopModels);
//			for (Integer id : top15IDs) {
//				if (topModelIDs.size() < maxNumTopModels) {
//					topModelIDs.add(id);
//				}
//			}
//			
//			// Set the top models in the DB
//			QueryManager.setModelsToUseInBacktest(topModelIDs);
//			ArrayList<Model> models = QueryManager.getModels("WHERE useinbacktests = true");
//			
//			// Set the top models in the TradingSingleton
//			ts.clearBKModelHash();
//			ts.clearWekaClassifierHash();
//			for (Model model : models) {
//				ts.addModel(model);
//			}
//			
//			// Start TradeSingleton
//			ts.setRunning(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param periodStart
	 * @param periodEnd
	 * @param bk
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createUnlabeledWekaArffData(Calendar periodStart, BarKey bk, boolean useNormalizedNumericValues, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			if (barWMDIndex >= barWMDList.size()) {
				// Stop trading
				TradingSingleton ts = TradingSingleton.getInstance();
				ts.setRunning(false);
				System.out.println("Done");
				return null;
			}
			// This is newest to oldest ordered
			BarWithMetricData barWMD = barWMDList.get(barWMDIndex);
			ArrayList<HashMap<String, Object>> rawTrainingSet = new ArrayList<HashMap<String, Object>>();
			HashMap<String, Object> instanceMap = new HashMap<String, Object>();
			instanceMap.put("open", barWMD.open);
			instanceMap.put("close", barWMD.close);
			instanceMap.put("high", barWMD.high);
			instanceMap.put("low", barWMD.low);
			instanceMap.put("hour", barWMD.periodStart.get(Calendar.HOUR));
			instanceMap.put("start", new Timestamp(barWMD.periodStart.getTimeInMillis()));
			instanceMap.put("symbol", barWMD.symbol);
			instanceMap.put("duration", barWMD.duration.toString());
			for (Entry<String, Double> entry : barWMD.getMetricData().entrySet()) {
				instanceMap.put(entry.getKey(), entry.getValue().floatValue());
			}
			rawTrainingSet.add(instanceMap);
			
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>(); 
			for (HashMap<String, Object> record : rawTrainingSet) {
				float close = (float)record.get("close");
				float hour = (int)record.get("hour");
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					if (!metricName.equals("close") && !metricName.equals("hour") && !metricName.equals("symbol") && !metricName.equals("class")) {
						// Regular metrics are looked up via the MetricDiscreteValueHash
						MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
						ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
						if (bucketCutoffValues != null) {
							if (record.get(metricName) == null) {
								System.out.println("eh");
							}
							float metricValue = (float)record.get(metricName);
							
							int bucketNum = 0;
							for (int a = bucketCutoffValues.size() - 1; a >= 0; a--) {
								float bucketCutoffValue = bucketCutoffValues.get(a);
								if (metricValue < bucketCutoffValue) {
									break;
								}
								bucketNum++;
							}
							if (useNormalizedNumericValues) {
								metricPart += String.format("%.5f", metricValue) + ", ";
							}
							else {
								metricPart += ("B" + bucketNum + ", ");
							}
						}
					}
					else {
						// Other metrics (close, hour, symbol) are already known
						if (metricName.equals("close")) {
							metricPart += close + ", ";
						}
						if (metricName.equals("hour")) {
							metricPart += hour + ", ";
						}
						if (metricName.equals("symbol")) {
							metricPart += bk.symbol + ", ";
						}
						if (metricName.equals("class")) {
							metricPart += "?, ";
						}
					}
				}
		
				if (!metricPart.equals("")) {
					if (metricPart.endsWith(", ")) {
						metricPart = metricPart.substring(0, metricPart.length() - 2);
					}
					String recordLine = metricPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					valuesList.add(valueList);
				}
			}
			return valuesList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static double getCurrentBid(String symbol) {
		double close = barWMDList.get(barWMDIndex).close;
		double pipSize = IBConstants.TICKER_PIP_SIZE_HASH.get(symbol);
		double fakeBid = close - (pipSize / 2d);
		return Double.parseDouble(df5.format(fakeBid));
	}
	
	public static double getCurrentAsk(String symbol) {
		double close = barWMDList.get(barWMDIndex).close;
		double pipSize = IBConstants.TICKER_PIP_SIZE_HASH.get(symbol);
		double fakeAsk = close + (pipSize / 2d);
		return Double.parseDouble(df5.format(fakeAsk));
	}
	
	public static Calendar getCurrentPeriodStart() {
		return barWMDList.get(barWMDIndex).periodStart;
	}
	
	public static Calendar getCurrentPeriodEnd() {
		return barWMDList.get(barWMDIndex).periodEnd;
	}
	
	public static void incrementBarWMDIndex() {
		barWMDIndex++;

		// If we've reached the end, exit.
		if (barWMDIndex >= barWMDList.size()) {
			System.out.println("End of barWMDList");
			System.exit(0);
		}
		// Else if the index has moved more than a week past the currentBaseDate, move the currentBaseDate forward a week, and set the top models for that week
		else if (barWMDList.get(barWMDIndex).periodStart.getTimeInMillis() >= (currentBaseDate.getTimeInMillis() + (7 * 24 * 60 * 60 * 1000))) {
			System.out.println("Switching models for new week...");
			currentBaseDate.add(Calendar.WEEK_OF_YEAR, 1);
			
			TradingSingleton ts = TradingSingleton.getInstance();
			
			HashSet<Integer> topModelIDs = new HashSet<Integer>();
			// Add up to one model per sellmetricvalue
			for (double d = minSellMetricValue; d <= maxSellMetricValue + .01; d += .1d) {
				d = new Double(df2.format(d));
				topModelIDs.addAll(QueryManager.selectTopModels(currentBaseDate, d, d, .01, 1));
			}
			// Then add more up to x
			HashSet<Integer> top15IDs = QueryManager.selectTopModels(currentBaseDate, minSellMetricValue, maxSellMetricValue, .01, maxNumTopModels);
			for (Integer id : top15IDs) {
				if (topModelIDs.size() < maxNumTopModels) {
					topModelIDs.add(id);
				}
			}
			
			// Set the top models in the DB
			QueryManager.setModelsToUseInBacktest(topModelIDs);
			ArrayList<Model> models = QueryManager.getModels("WHERE useinbacktests = true");
			
			// Set the top models in the TradingSingleton
			ts.clearBKModelHash();
			ts.clearWekaClassifierHash();
			for (Model model : models) {
				ts.addModel(model);
			}
			
			// Tell the engine about the new models too
			ts.refreshEngineModels();
		}
	}

	public static String getRunName() {
		return runName;
	}

	public static boolean isAdjustStops() {
		return adjustStops;
	}
}
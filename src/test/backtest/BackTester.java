package test.backtest;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.BarWithMetricData;
import data.MetricKey;
import data.Model;
import data.downloaders.interactivebrokers.IBConstants;
import dbio.QueryManager;
import trading.TradingSingleton;

public class BackTester {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static DecimalFormat df5 = new DecimalFormat("#.#####");
	
	private static ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
	private static ArrayList<BarWithMetricData> barWMDList = null;
	private static int barWMDIndex = 0;
	
	public static void main(String[] args) {
		try {
			System.out.println("Loading data...");
			
			// Set time period
			String start = "05/01/2016 00:00:00";
			String end = "05/12/2016 16:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(sdf.parse(start).getTime());
			endC.setTimeInMillis(sdf.parse(end).getTime());
			
			// Set BarKey(s) on which this backtest will run
			BarKey bk = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
			barKeys.add(bk);
			
			// Load bar & metric data
			barWMDList = QueryManager.loadMetricSequenceHashForBackTests(barKeys, startC, endC);
			
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
			System.out.println("Starting backtest...");
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
	
	public static Calendar getCurrentStart() {
		return barWMDList.get(barWMDIndex).periodStart;
	}
	
	public static void incrementBarWMDIndex() {
		barWMDIndex++;
		if (barWMDIndex >= barWMDList.size()) {
			System.out.println("End of barWMDList");
			System.exit(0);
		}
	}
}
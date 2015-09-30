package ml;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;

public class ARFF {

	public static void main(String[] args) {
		try {
			// 1/12/2015 - 6/3/2015 is pretty clean for training
			// 1/1/2014 - 3/31/2015 for training and 4/1/2015 - 6/3/2015 for testing was the original set of dates I used
			
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			
			String sTrainStart = "01/12/2015 00:00:00";
			String sTrainEnd = "06/03/2015 00:00:00";
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTime(sdf.parse(sTrainStart));
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTime(sdf.parse(sTrainEnd));
			
			String sTestStart = "08/11/2015 00:00:00";
			String sTestEnd = "09/15/2015 00:00:00";
			Calendar testStart = Calendar.getInstance();
			testStart.setTime(sdf.parse(sTestStart));
			Calendar testEnd = Calendar.getInstance();
			testEnd.setTime(sdf.parse(sTestEnd));
			
			BarKey bk = new BarKey("okcoinBTCCNY", BAR_SIZE.BAR_3M);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
			System.out.println(trainStart.getTime().toString());
			System.out.println(trainEnd.getTime().toString());
			System.out.println(testStart.getTime().toString());
			System.out.println(testEnd.getTime().toString());
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
	
			String optionsRandomForest = "-I 100 -K 4 -S 1"; // I = # Trees, K = # Features, S = Seed
			// RandomForest works best with 10 or 30 trees (but will barely signal anything) on ambitious setups.
			
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1";
			
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
		
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 10 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, .3f, 0.1f, 10, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 30 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, .3f, 0.1f, 10, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest",		 	"-I 100 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, .3f, 0.1f, 10, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 300 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, .5f, 0.2f, 10, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 10 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 20, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 30 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 20, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 100 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 20, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 300 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 20, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 10 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, .5f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 30 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, .5f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest",		 	"-I 100 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, .5f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 300 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, .5f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 10 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 3.0f, 1f, 180, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 30 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 3.0f, 1f, 180, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 100 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 3.0f, 1f, 180, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("RandomForest", 		"-I 300 -K 4 -S 1", "bull", trainStart, trainEnd, testStart, testEnd, 3.0f, 1f, 180, bk, true, Constants.METRICS, metricDiscreteValueHash);
					
			// For use with 5m bars
			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.1f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.2f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.3f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.4f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.5f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.1f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.2f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.3f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.4f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.5f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.1f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.2f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.3f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.4f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bear", trainStart, trainEnd, testStart, testEnd, 0.5f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//	
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, .1f, 1, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, .1f, 3, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, .1f, 6, bk, true, Constants.METRICS, metricDiscreteValueHash);
			
			
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .2f, .2f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .1f, .1f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .2f, .2f, 20, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .1f, .1f, 20, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .2f, .2f, 10, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .1f, .1f, 10, bk, true, Constants.METRICS, metricDiscreteValueHash);
	
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, .5f, .5f, 120, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bear", trainStart, trainEnd, testStart, testEnd, .5f, .5f, 120, bk, true, Constants.METRICS, metricDiscreteValueHash);
			
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1f, 1f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1.5f, 1f, 60, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 90, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2.5f, 1f, 120, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1f, 150, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 30, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 1.5f, .5f, 60, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2f, .5f, 90, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 2.5f, .5f, 120, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("NaiveBayes", 		null, "bull", trainStart, trainEnd, testStart, testEnd, 3f, .5f, 150, bk, true, Constants.METRICS, metricDiscreteValueHash);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param type - Either "bull" or "bear"
	 * @param periodStart
	 * @param periodEnd
	 * @param targetGain - %
	 * @param minLoss - %
	 * @param numPeriods
	 * @param bk
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * 
	 * Returns a list that looks exactly like the @data section of a WEKA .arff file
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffData(String type, Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, int numPeriods, BarKey bk, boolean useInterBarData, ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<Float> nextXHighs = new ArrayList<Float>();
			ArrayList<Float> nextXLows = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				float close = (float)record.get("close");
				float high = (float)record.get("high");
				float low = (float)record.get("low");
				float hour = (int)record.get("hour");
				nextXCloses.add(close);
				nextXHighs.add(high);
				nextXLows.add(low);
				if (nextXCloses.size() > numPeriods) {
					nextXCloses.remove(0);
				}
				if (nextXHighs.size() > numPeriods) {
					nextXHighs.remove(0);
				}
				if (nextXLows.size() > numPeriods) {
					nextXLows.remove(0);
				}
		
				boolean targetOK = false;
				int targetIndex = -1;
				if (type.equals("bull")) {
					if (useInterBarData) {
						targetIndex = findTargetGainIndex(nextXHighs, close, targetGain);
					}
					else {
						targetIndex = findTargetGainIndex(nextXCloses, close, targetGain);
					}
				}
				else if (type.equals("bear")) {
					if (useInterBarData) {
						targetIndex = findTargetLossIndex(nextXLows, close, targetGain); // This can be thought of as targetLoss in the bear case
					}
					else {
						targetIndex = findTargetLossIndex(nextXCloses, close, targetGain);
					}
				}

				boolean stopOK = false;
				if (targetIndex != -1) {
					targetOK = true;
					if (type.equals("bull")) {
						if (useInterBarData) {
							float minPrice = findMin(nextXLows, targetIndex);
							if (minPrice > low * (100f - minLoss) / 100f) {
								stopOK = true;
							}
						}
						else {
							float minPrice = findMin(nextXCloses, targetIndex);
							if (minPrice > close * (100f - minLoss) / 100f) {
								stopOK = true;
							}
						}
					}
					else if (type.equals("bear")) {
						if (useInterBarData) {
							float maxPrice = findMax(nextXHighs, targetIndex);
							if (maxPrice < high * (100f + minLoss) / 100f) {
								stopOK = true;
							}
						}
						else {
							float maxPrice = findMax(nextXCloses, targetIndex);
							if (maxPrice < close * (100f + minLoss) / 100f) {
								stopOK = true;
							}
						}
					}
				}

				// Non-Metric Values
				String refrencePart = close + ", " + hour + ", ";

				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
					if (bucketCutoffValues != null) {
						float metricValue = (float)record.get(metricName);
						
						int bucketNum = 0;
						for (int a = bucketCutoffValues.size() - 1; a >= 0; a--) {
							float bucketCutoffValue = bucketCutoffValues.get(a);
							if (metricValue < bucketCutoffValue) {
								break;
							}
							bucketNum++;
						}
						
						metricPart += ("BUCKET" + bucketNum + ", ");
//						metricPart += metricValue + ", ";
					}
				}
				
				// Class
				String classPart = "";
				if (stopOK && targetOK) {
					classPart = "Buy";
				}
				else {
					classPart = "No";
				}
				
				if (!metricPart.equals("")) {
					String recordLine = refrencePart + metricPart + classPart;
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
	
	/**
	 * 
	 * @param periodStart
	 * @param periodEnd
	 * @param bk
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createUnlabeledWekaArffData(Calendar periodStart, Calendar periodEnd, BarKey bk, ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				// Non-Metric Values
				float close = (float)record.get("close");
				float hour = (int)record.get("hour");
				String refrencePart = close + ", " + hour + ", ";
				
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
					if (bucketCutoffValues != null) {
						float metricValue = (float)record.get(metricName);
						
						int bucketNum = 0;
						for (int a = bucketCutoffValues.size() - 1; a >= 0; a--) {
							float bucketCutoffValue = bucketCutoffValues.get(a);
							if (metricValue < bucketCutoffValue) {
								break;
							}
							bucketNum++;
						}
						
						metricPart += ("BUCKET" + bucketNum + ", ");
//						metricPart += metricValue + ", ";
					}
				}
				// Class
				String classPart = "?";
				
				if (!metricPart.equals("")) {
					String recordLine = refrencePart + metricPart + classPart;
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
	
	private static float findMin(ArrayList<Float> list, int targetIndex) {
		float min = 1000000000f;
		for (int a = targetIndex; a < list.size(); a++) {
			if (list.get(a) < min) {
				min = list.get(a);
			}
		}
		return min;
	}
	
	private static float findMax(ArrayList<Float> list, int targetIndex) {
		float max = -1f;
		for (int a = targetIndex; a < list.size(); a++) {
			if (list.get(a) > max) {
				max = list.get(a);
			}
		}
		return max;
	}
	
	private static int findTargetGainIndex(ArrayList<Float> nextXPrices, float close, float targetGain) {
		float targetClose = close * (100f + targetGain) / 100f;
		for (int a = 0; a < nextXPrices.size(); a++) {
			if (nextXPrices.get(a) >= targetClose) {
				return a;
			}
		}
		return -1;
	}
	
	/**
	 * @param nextXPrices
	 * @param close
	 * @param targetLoss - Positive number
	 * @return
	 */
	private static int findTargetLossIndex(ArrayList<Float> nextXPrices, float close, float targetLoss) {
		float targetClose = close * (100f - targetLoss) / 100f;
		for (int a = 0; a < nextXPrices.size(); a++) {
			if (nextXPrices.get(a) <= targetClose) {
				return a;
			}
		}
		return -1;
	}
	
	private static int findMaxIndex(ArrayList<Float> list) {
		float max = -1f;
		int maxIndex = -1;
		for (int a = 0; a < list.size(); a++) {
			if (list.get(a) > max) {
				max = list.get(a);
				maxIndex = a;
			}
		}
		return maxIndex;
	}
	
	private static int findMinIndex(ArrayList<Float> list) {
		float min = 1000000;
		int minIndex = -1;
		for (int a = 0; a < list.size(); a++) {
			if (list.get(a) < min) {
				min = list.get(a);
				minIndex = a;
			}
		}
		return minIndex;
	}
}
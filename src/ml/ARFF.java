package ml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.DecimalFormat;
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

	private static ArrayList<HashMap<String, Object>> rawTrainingSet = new ArrayList<HashMap<String, Object>>();
	private static ArrayList<HashMap<String, Object>> rawTestSet = new ArrayList<HashMap<String, Object>>();
	
	public static void main(String[] args) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			DecimalFormat df2 = new DecimalFormat("#.##");
			
			String sTrainStart = "05/25/2010 00:00:00"; // 1/12/2015
			String sTrainEnd = "01/05/2015 16:00:00"; // 11/02/2015
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTime(sdf.parse(sTrainStart));
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTime(sdf.parse(sTrainEnd));
			
			String sTestStart = "02/01/2015 16:15:00"; // 11/8/2015
			String sTestEnd = "01/13/2016 16:00:00"; // 12/22/2015
			Calendar testStart = Calendar.getInstance();
			testStart.setTime(sdf.parse(sTestStart));
			Calendar testEnd = Calendar.getInstance();
			testEnd.setTime(sdf.parse(sTestEnd));
			
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
			BarKey bk2 = new BarKey("GBP.USD", BAR_SIZE.BAR_5M);
			BarKey bk3 = new BarKey("EUR.GBP", BAR_SIZE.BAR_5M);
			
			barKeys.add(bk1);
			barKeys.add(bk2);
			barKeys.add(bk3);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
			for (String metricName : metricNames) {
				System.out.println("@attribute " + metricName + " {B0,B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11,B12,B13}");
			}

			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
	
			System.out.println("Loading training data...");
			for (BarKey bk : barKeys) {
				rawTrainingSet.addAll(QueryManager.getTrainingSet(bk, trainStart, trainEnd, metricNames));
			}
			System.out.println("Complete.");
			System.out.println("Loding test data...");
			for (BarKey bk : barKeys) {
				rawTestSet.addAll(QueryManager.getTrainingSet(bk, testStart, testEnd, metricNames));
			}
			System.out.println("Complete.");
			
			String optionsRandomForest = "-I 160 -K 24 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 4096.0 -C 1000 -E 0.001 -P 0.1 -seed 1";
			String optionsStacking = "weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1\" -S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.RandomForest -I 30 -K 0 -S 1\" -B \"weka.classifiers.bayes.RandomForest \"";
//			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsMetaCost = "weka.classifiers.meta.MetaCost -cost-matrix \"[0.0 30.0 1.0; 10.0 0.0 1.0; 4.0 16.0 0.0]\" -I 2 -P 100 -S 1 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsBagging = "weka.classifiers.meta.Bagging -P 100 -S 1 -I 3 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsJ48 = "weka.classifiers.trees.J48 -C 0.5 -M 1";
			
			// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
			
			for (float b = 0.08f; b <= .73; b += .08f) {
//				for (int d = 1; d <= 10; d++) {
//					b = Float.parseFloat(df2.format(b));
					Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, b, b, 100, barKeys, true, false, false, false, true, false, true, "Unbounded", metricNames, metricDiscreteValueHash);	
//				}	
			}
//			for (float b = 0.08f; b <= .73; b += .08f) {
////				for (int d = 11; d <= 20; d++) {
////					b = Float.parseFloat(df2.format(b));
//					Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, b, b, d, barKeys, true, false, false, false, true, true, false, "Bounded", metricNames, metricDiscreteValueHash);	
////				}	
//			}
//			for (float b = 0.08f; b <= .73; b += .08f) {
////				for (int d = 21; d <= 30; d++) {
////					b = Float.parseFloat(df2.format(b));
//					Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, b, b, d, barKeys, true, false, false, false, true, true, false, "Bounded", metricNames, metricDiscreteValueHash);	
////				}	
//			}
	
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 100, barKeys, true, false, false, false, true, false, true, "Unbounded", metricNames, metricDiscreteValueHash);
			
																																	/**    IBD, Weights, NNum, Close, Hour, Draw, Symbol **/
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, 0.2f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, 0.3f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, 0.5f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.7f, 0.7f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.8f, 0.8f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.9f, 0.9f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, 1.0f, 2, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, 0.2f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, 0.3f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, 0.5f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.7f, 0.7f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.8f, 0.8f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.9f, 0.9f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, 1.0f, 4, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, 0.2f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, 0.3f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, 0.5f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.7f, 0.7f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.8f, 0.8f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.9f, 0.9f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, 1.0f, 6, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, 0.2f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, 0.3f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, 0.5f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.7f, 0.7f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.8f, 0.8f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.9f, 0.9f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, 1.0f, 8, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, 0.2f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, 0.3f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, 0.5f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.7f, 0.7f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.8f, 0.8f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.9f, 0.9f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, 1.0f, 10, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.2f, 0.2f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.3f, 0.3f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.4f, 0.4f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.5f, 0.5f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.7f, 0.7f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.8f, 0.8f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 0.9f, 0.9f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("AdaBoostM1", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1.0f, 1.0f, 12, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Classifies as Win, Lose, or Draw.  Takes a bar and looks ahead for x periods to see if Close or Stop conditions are met.  If neither are met, it is a Draw
	 * 
	 * @param algo
	 * @param periodStart
	 * @param periodEnd
	 * @param targetGain
	 * @param minLoss
	 * @param numPeriods
	 * @param bk
	 * @param useInterBarData
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest (train or test)
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataPeriodBounded(String algo, Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, int numPeriods, 
			boolean useInterBarData, boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeDraw, boolean includeSymbol,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<Float> nextXHighs = new ArrayList<Float>();
			ArrayList<Float> nextXLows = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			
			ArrayList<HashMap<String, Object>> dataset = new ArrayList<HashMap<String, Object>>();
			if (trainOrTest.equals("train")) {
				dataset.addAll(rawTrainingSet);
			}
			else if (trainOrTest.equals("test")) {
				dataset.addAll(rawTestSet);
			}
			
			// These are always ordered newest to oldest
			for (HashMap<String, Object> record : trainOrTest.equals("train") ? rawTrainingSet : rawTestSet) {
				float close = (float)record.get("close");
				float high = (float)record.get("high");
				float low = (float)record.get("low");
				float hour = (int)record.get("hour");
				String symbol = record.get("symbol").toString();
				String duration = record.get("duration").toString();
				
				if (nextXCloses.size() > numPeriods) {
					nextXCloses.remove(nextXCloses.size() - 1);
				}
				if (nextXHighs.size() > numPeriods) {
					nextXHighs.remove(nextXHighs.size() - 1);
				}
				if (nextXLows.size() > numPeriods) {
					nextXLows.remove(nextXLows.size() - 1);
				}
		
				boolean targetOK = false;
				int targetIndex = -1;
				if (useInterBarData) {
					targetIndex = findTargetGainIndex(nextXHighs, close, targetGain);
				}
				else {
					targetIndex = findTargetGainIndex(nextXCloses, close, targetGain);
				}

				boolean fullDurationStopOK = false;
				boolean upToLastStopOK = false;
				boolean durationOK = false;
				if (targetIndex != -1) {
					targetOK = true;
					if (useInterBarData) {
						float minPrice = findMin(nextXLows, targetIndex); // This checks up through the bar where the successful exit would be made.
						if (minPrice > close * (100f - minLoss) / 100f) {
							fullDurationStopOK = true;
						}
						float minPrice2 = findMin(nextXLows, targetIndex - 1); // This checks up through the bar BEFORE the successful exit would be made.  Because if the last bar contains a price range that triggers both the successful exit and the stop, I guess I'll call it a draw.
						if (minPrice2 > close * (100f - minLoss) / 100f) {
							upToLastStopOK = true;
						}
					}
					else {
						float minPrice = findMin(nextXCloses, targetIndex);
						if (minPrice > close * (100f - minLoss) / 100f) {
							fullDurationStopOK = true;
						}
						float minPrice2 = findMin(nextXCloses, targetIndex - 1);
						if (minPrice2 > close * (100f - minLoss) / 100f) {
							upToLastStopOK = true;
						}
					}
				}
				else {
					if (useInterBarData) {
						float priceMinWhole = findMin(nextXLows, nextXLows.size() - 1);
						if (priceMinWhole > close * (100f - minLoss) / 100f) {
							durationOK = true;
						}
					}
					else {
						float priceMinWhole = findMin(nextXCloses, nextXCloses.size() - 1);
						if (priceMinWhole > close * (100f - minLoss) / 100f) {
							durationOK = true;
						}
					}
				}

				// Non-Metric Optional Features
				String referencePart = "";
				if (includeClose) {
					referencePart = close + ", ";
				}
				if (includeHour) {
					referencePart += hour + ", ";
				}
				if (includeSymbol) {
					referencePart += symbol + ", ";
				}
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
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
						
						if (useNormalizedNumericValues) {
							metricPart += String.format("%.5f", metricValue) + ", ";
						}
						else {
							metricPart += ("B" + bucketNum + ", ");
						}
					}
				}
				
				// Class
				String classPart = "";
				if (fullDurationStopOK && targetOK) {
					classPart = "Win";
				}
				else {
					if (durationOK || upToLastStopOK) {
						classPart = "Draw";
					}
					else {
						classPart = "Lose";
					}
				}
				
//				System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
				
				if (!metricPart.equals("")) {
					String recordLine = referencePart + metricPart + classPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					
					if (includeDraw) {
						valuesList.add(valueList);
					}
					else if (!classPart.equals("Draw")) { 
						valuesList.add(valueList);
					}
				}
				
				nextXCloses.add(0, close);
				nextXHighs.add(0, high);
				nextXLows.add(0, low);
			}
			
			// Add weights after the fact - I think this will help when there is a heavy skew towards one classification over the other
			if (useWeights) {
				if (!algo.equals("LibSVM")) { // Other algos besides LibSVM use traditional Weka weights.  LibSVM gets its weights as a parameter and are added in Modelling.
					int numNo = 0;
					int numTotal = valuesList.size();
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString().trim();
						if (classification.equals("No")) {
							numNo++;
						}
					}
					float yesWeight = (numNo / (float)(numTotal - numNo));
					yesWeight = Math.round(yesWeight * 10f) / 10f;
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString();
						if (classification.equals("No")) {
							record.add("{1}");
						}
						else {
							record.add("{" + yesWeight + "}");
						}
					}
				}
			}
			
//			for (ArrayList<Object> valueList : valuesList) {
//				String s = valueList.toString();
//				s = s.replace("]", "").replace("[", "").replace("  ", " ").trim();
//				System.out.println(s);
//			}
			
			return valuesList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Classifies as Win or Lose.  Takes a bar and looks as far ahead as needed until the close or stop criteria are met.  
	 * 
	 * @param algo
	 * @param periodStart
	 * @param periodEnd
	 * @param targetGain
	 * @param minLoss
	 * @param useInterBarData
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataPeriodUnbounded(String algo, Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, 
			boolean useInterBarData, boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<Float> futureCloses = new ArrayList<Float>();
			ArrayList<Float> futureHighs = new ArrayList<Float>();
			ArrayList<Float> futureLows = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
	
			// Both are ordered newest to oldest
			for (HashMap<String, Object> record : trainOrTest.equals("train") ? rawTrainingSet : rawTestSet) {
				float close = (float)record.get("close");
				float high = (float)record.get("high");
				float low = (float)record.get("low");
				float hour = (int)record.get("hour");
				String symbol = record.get("symbol").toString();
				String duration = record.get("duration").toString();
				
				boolean targetOK = false;
				int targetIndex = -1;
				if (useInterBarData) {
					targetIndex = findTargetGainIndex(futureHighs, close, targetGain);
				}
				else {
					targetIndex = findTargetGainIndex(futureCloses, close, targetGain);
				}
				
				boolean fullDurationStopOK = false;
				boolean upToLastStopOK = false;
				boolean durationOK = false;
				if (targetIndex != -1) {
					targetOK = true;
					if (useInterBarData) {
						float minPrice = findMin(futureLows, targetIndex); // This checks up through the bar where the successful exit would be made.
						if (minPrice > close * (100f - minLoss) / 100f) {
							fullDurationStopOK = true;
						}
						float minPrice2 = findMin(futureLows, targetIndex - 1); // This checks up through the bar BEFORE the successful exit would be made.  Because if the last bar contains a price range that triggers both the successful exit and the stop, I guess I'll call it a draw.
						if (minPrice2 > close * (100f - minLoss) / 100f) {
							upToLastStopOK = true;
						}
					}
					else {
						float minPrice = findMin(futureCloses, targetIndex);
						if (minPrice > close * (100f - minLoss) / 100f) {
							fullDurationStopOK = true;
						}
						float minPrice2 = findMin(futureCloses, targetIndex - 1);
						if (minPrice2 > close * (100f - minLoss) / 100f) {
							upToLastStopOK = true;
						}
					}
				}
				else {
					if (useInterBarData) {
						float priceMinWhole = findMin(futureLows, futureLows.size() - 1);
						if (priceMinWhole > close * (100f - minLoss) / 100f) {
							durationOK = true;
						}
					}
					else {
						float priceMinWhole = findMin(futureCloses, futureCloses.size() - 1);
						if (priceMinWhole > close * (100f - minLoss) / 100f) {
							durationOK = true;
						}
					}
				}

				// Non-Metric Optional Features
				String referencePart = "";
				if (includeClose) {
					referencePart = close + ", ";
				}
				if (includeHour) {
					referencePart += hour + ", ";
				}
				if (includeSymbol) {
					referencePart += symbol + ", ";
				}
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
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
						
						if (useNormalizedNumericValues) {
							metricPart += String.format("%.5f", metricValue) + ", ";
						}
						else {
							metricPart += ("B" + bucketNum + ", ");
						}
					}
				}
				
				// Class
				String classPart = "";
				if (fullDurationStopOK && targetOK) {
					classPart = "Win";
				}
				else {
					if (durationOK || upToLastStopOK) {
						classPart = "Draw";
					}
					else {
						classPart = "Lose";
					}
				}
				
//				System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
				
				if (!metricPart.equals("")) {
					String recordLine = referencePart + metricPart + classPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					if (!classPart.equals("Draw")) // Don't use Draw instances in unbounded models - these happen towards the end of the time period where it finishes before resolving.
						valuesList.add(valueList);
				}
				
				futureCloses.add(0, close);
				futureHighs.add(0, high);
				futureLows.add(0, low);
			}
			
			// Add weights after the fact - I think this will help when there is a heavy skew towards one classification over the other
			if (useWeights) {
				if (!algo.equals("LibSVM")) { // Other algos besides LibSVM use traditional Weka weights.  LibSVM gets its weights as a parameter and are added in Modelling.
					int numNo = 0;
					int numTotal = valuesList.size();
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString().trim();
						if (classification.equals("No")) {
							numNo++;
						}
					}
					float yesWeight = (numNo / (float)(numTotal - numNo));
					yesWeight = Math.round(yesWeight * 10f) / 10f;
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString();
						if (classification.equals("No")) {
							record.add("{1}");
						}
						else {
							record.add("{" + yesWeight + "}");
						}
					}
				}
			}
			
			boolean writeFile = false;
			
			if (writeFile) {
				File f = new File("out.arff");
				if (!f.exists()) {
					f.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(f, true);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				
				for (ArrayList<Object> valueList : valuesList) {
					String s = valueList.toString();
					s = s.replace("]", "").replace("[", "").replace("  ", " ").trim();
					System.out.println(s);
					
					bw.write(s);
					bw.newLine();
				}
				
				bw.close();
				fos.close();
			}
			return valuesList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Classifies as Win or Lose.  Looks ahead X number of bars and sees if the price has risen or fallen.
	 * @param algo
	 * @param periodStart
	 * @param periodEnd
	 * @param numPeriods
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataFixedInterval(String algo, Calendar periodStart, Calendar periodEnd, int numPeriods,
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();

			// These are always ordered newest to oldest.
			for (int a = numPeriods; a < (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).size(); a++) {
				HashMap<String, Object> thisInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a);
				HashMap<String, Object> futureInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a - numPeriods);
				
				float close = (float)thisInstance.get("close");
				float hour = (int)thisInstance.get("hour");
				String symbol = thisInstance.get("symbol").toString();
				String duration = thisInstance.get("duration").toString();
				
				// Class
				String classPart = "Lose";
				if ((float)futureInstance.get("close") > close) {
					classPart = "Win";
				}
				
				// Non-Metric Optional Features
				String referencePart = "";
				if (includeClose) {
					referencePart = close + ", ";
				}
				if (includeHour) {
					referencePart += hour + ", ";
				}
				if (includeSymbol) {
					referencePart += symbol + ", ";
				}
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
					if (bucketCutoffValues != null) {
						float metricValue = (float)thisInstance.get(metricName);
						
						int bucketNum = 0;
						for (int b = bucketCutoffValues.size() - 1; b >= 0; b--) {
							float bucketCutoffValue = bucketCutoffValues.get(b);
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
				
//				System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
				
				if (!metricPart.equals("")) {
					String recordLine = referencePart + metricPart + classPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					valuesList.add(valueList);
				}
			}

			// Add weights after the fact - I think this will help when there is a heavy skew towards one classification over the other
			if (useWeights) {
				if (!algo.equals("LibSVM")) { // Other algos besides LibSVM use traditional Weka weights.  LibSVM gets its weights as a parameter and are added in Modelling.
					int numNo = 0;
					int numTotal = valuesList.size();
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString().trim();
						if (classification.equals("No")) {
							numNo++;
						}
					}
					float yesWeight = (numNo / (float)(numTotal - numNo));
					yesWeight = Math.round(yesWeight * 10f) / 10f;
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString();
						if (classification.equals("No")) {
							record.add("{1}");
						}
						else {
							record.add("{" + yesWeight + "}");
						}
					}
				}
			}
			
//			for (ArrayList<Object> valueList : valuesList) {
//				String s = valueList.toString();
//				s = s.replace("]", "").replace("[", "").replace("  ", " ").trim();
//				System.out.println(s);
//			}
			
			return valuesList;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Looks ahead X number of bars and outputs the % distance the price has moved from the base bar.
	 * @param algo
	 * @param periodStart
	 * @param periodEnd
	 * @param numPeriods
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataFixedIntervalRegression(String algo, Calendar periodStart, Calendar periodEnd, int numPeriods,
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			
			// These are always ordered newest to oldest
			for (int a = numPeriods; a < (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).size(); a++) {
				HashMap<String, Object> thisInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a);
				HashMap<String, Object> futureInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a - numPeriods);
				
				float close = (float)thisInstance.get("close");
				float hour = (int)thisInstance.get("hour");
				String symbol = thisInstance.get("symbol").toString();
				String duration = thisInstance.get("duration").toString();
				
				// Class
				float change = (float)futureInstance.get("close") - close;
				float changeP = change / close * 100f;
				String classPart = String.format("%.8f", changeP);
				
				// Non-Metric Optional Features
				String referencePart = "";
				if (includeClose) {
					referencePart = close + ", ";
				}
				if (includeHour) {
					referencePart += hour + ", ";
				}
				if (includeSymbol) {
					referencePart += symbol + ", ";
				}
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
					if (bucketCutoffValues != null) {
						float metricValue = (float)thisInstance.get(metricName);
						
						int bucketNum = 0;
						for (int b = bucketCutoffValues.size() - 1; b >= 0; b--) {
							float bucketCutoffValue = bucketCutoffValues.get(b);
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
				
//				System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
				
				if (!metricPart.equals("")) {
					String recordLine = referencePart + metricPart + classPart;
					ArrayList<Object> valueList = new ArrayList<Object>();
					String[] values = recordLine.split(",");
					valueList.addAll(Arrays.asList(values));
					valuesList.add(valueList);
				}
			}

			// Add weights after the fact - I think this will help when there is a heavy skew towards one classification over the other
			if (useWeights) {
				if (!algo.equals("LibSVM")) { // Other algos besides LibSVM use traditional Weka weights.  LibSVM gets its weights as a parameter and are added in Modelling.
					int numNo = 0;
					int numTotal = valuesList.size();
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString().trim();
						if (classification.equals("No")) {
							numNo++;
						}
					}
					float yesWeight = (numNo / (float)(numTotal - numNo));
					yesWeight = Math.round(yesWeight * 10f) / 10f;
					for (ArrayList<Object> record : valuesList) {
						String classification = record.get(record.size() - 1).toString();
						if (classification.equals("No")) {
							record.add("{1}");
						}
						else {
							record.add("{" + yesWeight + "}");
						}
					}
				}
			}
			
			for (ArrayList<Object> valueList : valuesList) {
				String s = valueList.toString();
				s = s.replace("]", "").replace("[", "").replace("  ", " ").trim();
				System.out.println(s);
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
	public static ArrayList<ArrayList<Object>> createUnlabeledWekaArffData(Calendar periodStart, Calendar periodEnd, BarKey bk, 
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				float close = (float)record.get("close");
				float hour = (int)record.get("hour");
				
				// Non-Metric Optional Features
				String referencePart = "";
				if (includeClose) {
					referencePart = close + ", ";
				}
				if (includeHour) {
					referencePart += hour + ", ";
				}
				if (includeSymbol) {
					referencePart += bk.symbol + ", ";
				}
				
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
						if (useNormalizedNumericValues) {
							metricPart += String.format("%.5f", metricValue) + ", ";
						}
						else {
							metricPart += ("B" + bucketNum + ", ");
						}
					}
				}
				// Class
				String classPart = "?";
				
				if (!metricPart.equals("")) {
					String recordLine = referencePart + metricPart + classPart;
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
		for (int a = 0; a <= targetIndex; a++) {
			if (list.get(a) < min) {
				min = list.get(a);
			}
		}
		return min;
	}
	
	private static float findMax(ArrayList<Float> list, int targetIndex) {
		float max = -1f;
		for (int a = 0; a <= targetIndex; a++) {
			if (list.get(a) > max) {
				max = list.get(a);
			}
		}
		return max;
	}
	
	private static int findTargetGainIndex(ArrayList<Float> nextXPrices, float close, float targetGain) {
		float targetClose = close * (100f + targetGain) / 100f;
		int listSize = nextXPrices.size();
		for (int a = 0; a < listSize; a++) {
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
		int listSize = nextXPrices.size();
		for (int a = 0; a < listSize; a++) {
			if (nextXPrices.get(a) <= targetClose) {
				return a;
			}
		}
		return -1;
	}
	
	private static int findMaxIndex(ArrayList<Float> list) {
		float max = -1f;
		int maxIndex = -1;
		int listSize = list.size();
		for (int a = 0; a < listSize; a++) {
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
		int listSize = list.size();
		for (int a = 0; a < listSize; a++) {
			if (list.get(a) < min) {
				min = list.get(a);
				minIndex = a;
			}
		}
		return minIndex;
	}
	
	public static ArrayList<ArrayList<Object>> removeDuplicates(ArrayList<ArrayList<Object>> instanceList) {
		ArrayList<ArrayList<Object>> uniqueList = new ArrayList<ArrayList<Object>>();
		try {
			for (ArrayList<Object> instance : instanceList) {
				boolean inUniqueList = false;
				for (ArrayList<Object> uniqueInstance : uniqueList) {
					boolean sameInstance = true;
					for (int a = 0; a < instance.size(); a++) {
						Object io = instance.get(a);
						Object uo = uniqueInstance.get(a);
						if (!io.equals(uo)) {
							sameInstance = false;
							break;
						}
					}
					
					if (sameInstance) {
						inUniqueList = true;
					}
				}
				
				if (inUniqueList == false) {
					ArrayList<Object> newInstance = new ArrayList<Object>();
					newInstance.addAll(instance);
					uniqueList.add(newInstance);
				}
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return uniqueList;
	}
}
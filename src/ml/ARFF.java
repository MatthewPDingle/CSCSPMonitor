package ml;

import java.sql.Timestamp;
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
			
//			String sTrainStart = "01/12/2015 00:00:00";
//			String sTrainEnd = "10/01/2015 00:00:00";
			String sTrainStart = "03/15/2015 16:15:00";
			String sTrainEnd = "03/20/2015 16:00:00";
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTime(sdf.parse(sTrainStart));
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTime(sdf.parse(sTrainEnd));
			
//			String sTestStart = "10/01/2015 00:00:00";
//			String sTestEnd = "11/20/2015 00:00:00";
			String sTestStart = "03/22/2015 16:15:00";
			String sTestEnd = "03/27/2015 16:00:00";
			Calendar testStart = Calendar.getInstance();
			testStart.setTime(sdf.parse(sTestStart));
			Calendar testEnd = Calendar.getInstance();
			testEnd.setTime(sdf.parse(sTestEnd));
			
//			BarKey bk = new BarKey("okcoinBTCCNY", BAR_SIZE.BAR_1M);
			BarKey bk = new BarKey("EUR.USD", BAR_SIZE.BAR_1M);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			for (String metricName : metricNames) {
				System.out.println("@attribute " + metricName + " {B0,B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11,B12,B13}");
			}
			
			System.out.println(trainStart.getTime().toString());
			System.out.println(trainEnd.getTime().toString());
			System.out.println(testStart.getTime().toString());
			System.out.println(testEnd.getTime().toString());
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
	
			String optionsRandomForest = "-I 100 -K 5 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 4096.0 -C 1.0 -E 0.001 -P 0.1 -W \"4.0 4.0 1.0\" -seed 1";
			String optionsStacking = "weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1\" -S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.RandomForest -I 30 -K 0 -S 1\" -B \"weka.classifiers.bayes.RandomForest \"";
			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.bayes.NaiveBayes --";
//			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -I 100 -K 0 -S 1";
			String optionsMetaCost = "weka.classifiers.meta.MetaCost -cost-matrix \"[0.0 30.0 1.0; 10.0 0.0 1.0; 4.0 16.0 0.0]\" -I 2 -P 100 -S 1 -W weka.classifiers.bayes.NaiveBayes --";
			
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
		
			
//			for (float p = 0.1f; p <= 0.8f; p += .1f) {
//				for (int numBars = 5; numBars <= 30; numBars += 5) {
//					Modelling.buildAndEvaluateModel("MetaCost", 		optionsMetaCost, "bull", trainStart, trainEnd, testStart, testEnd, p, p, numBars, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//				}
//			}
//			for (float p = 0.1f; p <= 0.8f; p += .1f) {
//				for (int numBars = 35; numBars <= 60; numBars += 5) {
//					Modelling.buildAndEvaluateModel("MetaCost", 		optionsMetaCost, "bull", trainStart, trainEnd, testStart, testEnd, p, p, numBars, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//				}
//			}
//			for (float p = 0.1f; p <= 0.8f; p += .1f) {
//				for (int numBars = 65; numBars <= 90; numBars += 5) {
//					Modelling.buildAndEvaluateModel("MetaCost", 		optionsMetaCost, "bull", trainStart, trainEnd, testStart, testEnd, p, p, numBars, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
//				}
//			}
	
			Modelling.buildAndEvaluateModel("MetaCost", 		optionsMetaCost, "bull", trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 60, bk, true, false, false, false, true, metricNames, metricDiscreteValueHash);
			
																																	/**    IBD, Weights, NNum, Close, Hour **/
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
	 * @param algo
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
	public static ArrayList<ArrayList<Object>> createWekaArffData(String algo, String type, Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, int numPeriods, BarKey bk, 
			boolean useInterBarData, boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<Float> nextXHighs = new ArrayList<Float>();
			ArrayList<Float> nextXLows = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				float open = (float)record.get("open");
				float close = (float)record.get("close");
				float high = (float)record.get("high");
				float low = (float)record.get("low");
				float hour = (int)record.get("hour");
				Timestamp startTS = (Timestamp)record.get("start");
				
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

				boolean fullDurationStopOK = false;
				boolean upToLastStopOK = false;
				boolean durationOK = false;
				if (targetIndex != -1) {
					targetOK = true;
					if (type.equals("bull")) {
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
					else if (type.equals("bear")) {
						if (useInterBarData) {
							float maxPrice = findMax(nextXHighs, targetIndex);
							if (maxPrice < close * (100f + minLoss) / 100f) {
								fullDurationStopOK = true;
							}
							float maxPrice2 = findMax(nextXHighs, targetIndex - 1);
							if (maxPrice2 < close * (100f + minLoss) / 100f) {
								upToLastStopOK = true;
							}
						}
						else {
							float maxPrice = findMax(nextXCloses, targetIndex);
							if (maxPrice < close * (100f + minLoss) / 100f) {
								fullDurationStopOK = true;
							}
							float maxPrice2 = findMax(nextXCloses, targetIndex - 1);
							if (maxPrice2 < close * (100f + minLoss) / 100f) {
								upToLastStopOK = true;
							}
						}
					}
				}
				else {
					if (type.equals("bull")) {
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
					else if (type.equals("bear")) {
						if (useInterBarData) {
							float priceMaxWhole = findMax(nextXHighs, nextXHighs.size() - 1);
							if (priceMaxWhole < close * (100f + minLoss) / 100f) {
								durationOK = true;
							}
						}
						else {
							float priceMaxWhole = findMax(nextXCloses, nextXCloses.size() - 1);
							if (priceMaxWhole < close * (100f + minLoss) / 100f) {
								durationOK = true;
							}
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
							metricPart += ("BUCKET" + bucketNum + ", ");
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
					valuesList.add(valueList);
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
			
			for (ArrayList<Object> valueList : valuesList) {
				String s = valueList.toString();
				s = s.replace("]", "").replace("[", "").replace("BUCKET", "B").replace("  ", " ").trim();
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
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, 
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
							metricPart += ("BUCKET" + bucketNum + ", ");
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
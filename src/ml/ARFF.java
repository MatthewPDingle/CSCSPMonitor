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
			
			String sTrainStart = "01/12/2015 00:00:00"; // 1/12/2015
			String sTrainEnd = "11/02/2015 16:00:00"; // 11/02/2015
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTime(sdf.parse(sTrainStart));
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTime(sdf.parse(sTrainEnd));
			
			String sTestStart = "11/08/2015 16:15:00"; // 11/8/2015
			String sTestEnd = "12/22/2015 16:00:00"; // 12/22/2015
			Calendar testStart = Calendar.getInstance();
			testStart.setTime(sdf.parse(sTestStart));
			Calendar testEnd = Calendar.getInstance();
			testEnd.setTime(sdf.parse(sTestEnd));
			
//			BarKey bk = new BarKey("okcoinBTCCNY", BAR_SIZE.BAR_1M);
			BarKey bk = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
//			metricNames.add("atr10");
//			metricNames.add("atr20");
//			metricNames.add("atr40");
//			metricNames.add("atr60");
//			metricNames.add("dvol5ema");
//			metricNames.add("dvol10ema");
//			metricNames.add("dvol25ema");
//			metricNames.add("dvol50ema");
//			metricNames.add("dvol75ema");
//			metricNames.add("mvol10");
//			metricNames.add("mvol20");
//			metricNames.add("mvol50");
//			metricNames.add("mvol100");
//			metricNames.add("mvol200");
//			metricNames.add("ppo3_10");
//			metricNames.add("pricebolls10");
//			metricNames.add("psar");
//			metricNames.add("timerange2");
//			metricNames.add("timerange5");
//			metricNames.add("tsf10");
//			metricNames.add("tsf20");
//			metricNames.add("tsfdydx40");
//			metricNames.add("tsfdydx60");
			
			
			for (String metricName : metricNames) {
				System.out.println("@attribute " + metricName + " {B0,B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11,B12,B13}");
			}
			
			System.out.println(trainStart.getTime().toString());
			System.out.println(trainEnd.getTime().toString());
			System.out.println(testStart.getTime().toString());
			System.out.println(testEnd.getTime().toString());
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
	
			String optionsRandomForest = "-I 160 -K 24 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 4096.0 -C 1000 -E 0.001 -P 0.1 -seed 1";
			String optionsStacking = "weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1\" -S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.RandomForest -I 30 -K 0 -S 1\" -B \"weka.classifiers.bayes.RandomForest \"";
//			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsMetaCost = "weka.classifiers.meta.MetaCost -cost-matrix \"[0.0 30.0 1.0; 10.0 0.0 1.0; 4.0 16.0 0.0]\" -I 2 -P 100 -S 1 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsBagging = "weka.classifiers.meta.Bagging -P 100 -S 1 -I 3 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			
			// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
			
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 2f, 1f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 3f, 1.5f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 4, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 8, bk, true, Constants.METRICS, metricDiscreteValueHash);
	//		Modelling.buildAndEvaluateModel("LibSVM", 		optionsAdaBoostM1, "bull", trainStart, trainEnd, testStart, testEnd, 1f, .5f, 12, bk, true, Constants.METRICS, metricDiscreteValueHash);
		
			
//			for (float b = 0.04f; b <= 1.01; b += .04f) {
//				for (int d = 16; d <= 64; d += 16) {
//					Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, b, b, d, bk, true, false, false, false, true, true, "Bounded", metricNames, metricDiscreteValueHash);	
//				}	
//			}
//			for (float b = 0.04f; b <= 1.01; b += .04f) {
//				for (int d = 80; d <= 128; d += 16) {
//					Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, b, b, d, bk, true, false, false, false, true, true, "Bounded", metricNames, metricDiscreteValueHash);	
//				}	
//			}
//			for (float b = 0.04f; b <= 1.01; b += .04f) {
//				for (int d = 144; d <= 192; d += 16) {
//					Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, b, b, d, bk, true, false, false, false, true, true, "Bounded", metricNames, metricDiscreteValueHash);	
//				}	
//			}
	
			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, "bull", trainStart, trainEnd, testStart, testEnd, 0.6f, 0.6f, 12, bk, true, false, false, false, true, true, "FixedIntervalRegression", metricNames, metricDiscreteValueHash);
			
																																	/**    IBD, Weights, NNum, Close, Hour, Draw **/
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
	 * @param type
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
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataPeriodBounded(String algo, String type, Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, int numPeriods, BarKey bk, 
			boolean useInterBarData, boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeDraw,
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
	 * @param type
	 * @param periodStart
	 * @param periodEnd
	 * @param targetGain
	 * @param minLoss
	 * @param bk
	 * @param useInterBarData
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataPeriodUnbounded(String algo, String type, Calendar periodStart, Calendar periodEnd, float targetGain, float minLoss, BarKey bk, 
			boolean useInterBarData, boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<Float> futureCloses = new ArrayList<Float>();
			ArrayList<Float> futureHighs = new ArrayList<Float>();
			ArrayList<Float> futureLows = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				float open = (float)record.get("open");
				float close = (float)record.get("close");
				float high = (float)record.get("high");
				float low = (float)record.get("low");
				float hour = (int)record.get("hour");
				Timestamp startTS = (Timestamp)record.get("start");
				
				boolean targetOK = false;
				int targetIndex = -1;
				if (type.equals("bull")) {
					if (useInterBarData) {
						targetIndex = findTargetGainIndex(futureHighs, close, targetGain);
					}
					else {
						targetIndex = findTargetGainIndex(futureCloses, close, targetGain);
					}
				}
				else if (type.equals("bear")) {
					if (useInterBarData) {
						targetIndex = findTargetLossIndex(futureLows, close, targetGain); // This can be thought of as targetLoss in the bear case
					}
					else {
						targetIndex = findTargetLossIndex(futureCloses, close, targetGain);
					}
				}

				boolean fullDurationStopOK = false;
				boolean upToLastStopOK = false;
				boolean durationOK = false;
				if (targetIndex != -1) {
					targetOK = true;
					if (type.equals("bull")) {
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
					else if (type.equals("bear")) {
						if (useInterBarData) {
							float maxPrice = findMax(futureHighs, targetIndex);
							if (maxPrice < close * (100f + minLoss) / 100f) {
								fullDurationStopOK = true;
							}
							float maxPrice2 = findMax(futureHighs, targetIndex - 1);
							if (maxPrice2 < close * (100f + minLoss) / 100f) {
								upToLastStopOK = true;
							}
						}
						else {
							float maxPrice = findMax(futureCloses, targetIndex);
							if (maxPrice < close * (100f + minLoss) / 100f) {
								fullDurationStopOK = true;
							}
							float maxPrice2 = findMax(futureCloses, targetIndex - 1);
							if (maxPrice2 < close * (100f + minLoss) / 100f) {
								upToLastStopOK = true;
							}
						}
					}
				}
				else {
					if (type.equals("bull")) {
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
					else if (type.equals("bear")) {
						if (useInterBarData) {
							float priceMaxWhole = findMax(futureHighs, futureHighs.size() - 1);
							if (priceMaxWhole < close * (100f + minLoss) / 100f) {
								durationOK = true;
							}
						}
						else {
							float priceMaxWhole = findMax(futureCloses, futureCloses.size() - 1);
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
	 * Classifies as Win or Lose.  Looks ahead X number of bars and sees if the price has risen or fallen.
	 * @param algo
	 * @param type
	 * @param periodStart
	 * @param periodEnd
	 * @param numPeriods
	 * @param bk
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataFixedInterval(String algo, String type, Calendar periodStart, Calendar periodEnd, int numPeriods, BarKey bk, 
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (int a = numPeriods; a < rawTrainingSet.size(); a++) {
				HashMap<String, Object> thisInstance = rawTrainingSet.get(a);
				HashMap<String, Object> futureInstance = rawTrainingSet.get(a - numPeriods);
				
				float open = (float)thisInstance.get("open");
				float close = (float)thisInstance.get("close");
				float high = (float)thisInstance.get("high");
				float low = (float)thisInstance.get("low");
				float hour = (int)thisInstance.get("hour");
				Timestamp startTS = (Timestamp)thisInstance.get("start");
				
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
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
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
	 * @param type
	 * @param periodStart
	 * @param periodEnd
	 * @param numPeriods
	 * @param bk
	 * @param useWeights
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataFixedIntervalRegression(String algo, String type, Calendar periodStart, Calendar periodEnd, int numPeriods, BarKey bk, 
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames);
			
			ArrayList<Float> nextXCloses = new ArrayList<Float>();
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (int a = numPeriods; a < rawTrainingSet.size(); a++) {
				HashMap<String, Object> thisInstance = rawTrainingSet.get(a);
				HashMap<String, Object> futureInstance = rawTrainingSet.get(a - numPeriods);
				
				float open = (float)thisInstance.get("open");
				float close = (float)thisInstance.get("close");
				float high = (float)thisInstance.get("high");
				float low = (float)thisInstance.get("low");
				float hour = (int)thisInstance.get("hour");
				Timestamp startTS = (Timestamp)thisInstance.get("start");
				
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
	
				// Metric Buckets (or values)
				String metricPart = "";
				for (String metricName : metricNames) {
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
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
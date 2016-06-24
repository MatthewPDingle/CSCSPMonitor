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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;
import utils.CalendarUtils;

public class ARFF {

	// Outer ArrayList = BarKey, Inner ArrayList = days newest to oldest, HashMap = bar & metric key/values
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawTrainingSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawTestSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	
	private static boolean saveARFF = false;
	
	private static int dateSet = 0;
	
	private static long MS_WEEK = 604800000l;
	private static long MS_90DAYS = 7776000000l;
	private static long MS_360DAYS = 31104000000l;
	
	private static int numDateSets = 5;
	private static Calendar[] trainEnds = new Calendar[numDateSets];
	private static Calendar[] trainStarts = new Calendar[numDateSets];
	private static Calendar[] testEnds = new Calendar[numDateSets];
	private static Calendar[] testStarts = new Calendar[numDateSets];
	private static int[] mods = new int[numDateSets];
	
	public static void main(String[] args) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");
			DecimalFormat df2 = new DecimalFormat("#.##");

			// Load date ranges for Train & Test sets
			long baseTime = Calendar.getInstance().getTimeInMillis();
			
			for (int a = 0; a < 10; a++) {
				Calendar c1 = Calendar.getInstance();
				c1.setTimeInMillis(baseTime - (a * MS_WEEK));
				testEnds[a] = c1;
				
				Calendar c2 = Calendar.getInstance();
				c2.setTimeInMillis((baseTime - MS_90DAYS) - (a * 4 * MS_WEEK));
				testStarts[a] = c2;
				
				Calendar c3 = Calendar.getInstance();
				c3.setTimeInMillis(testStarts[a].getTimeInMillis() - MS_WEEK);
				trainEnds[a] = c3;
				
				Calendar c4 = Calendar.getInstance();
				c4.setTimeInMillis((baseTime - MS_360DAYS) - (a * 24 * MS_WEEK));
				trainStarts[a] = c4;
				
				int duration = CalendarUtils.daysBetween(trainStarts[a], trainEnds[a]);
				int mod = duration / 3;
				mod = 5 * (int)(Math.ceil(Math.abs(mod / 5)));
				mods[a] = mod;
			}
		
			// Setup
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
			BarKey bk2 = new BarKey("GBP.USD", BAR_SIZE.BAR_5M);
			BarKey bk3 = new BarKey("EUR.GBP", BAR_SIZE.BAR_5M);
			
			barKeys.add(bk1);
			barKeys.add(bk2);
			barKeys.add(bk3);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
//			for (String metricName : metricNames) {
//				System.out.println("@attribute " + metricName + " {B0,B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11,B12,B13}");
//			}
			
//			System.out.println("Selecting Attributes...");
//			float gainAndLoss = .1f;
//			int numBars = 100;
//			ArrayList<String> selectedMetrics = Modelling.selectAttributes(gainAndLoss, gainAndLoss, numBars, false, false, true, false, true, 30, .0005f, "Unbounded", metricDiscreteValueHash);
//			System.out.println("Selecting Attributes Complete.");
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
			
			// Hyper-parameter options
			String[] oRandomForest = new String[21];
			oRandomForest[0] = "-I 128 -K 4 -S 1";
			oRandomForest[1] = "-I 128 -K 5 -S 1";
			oRandomForest[2] = "-I 128 -K 6 -S 1";
			oRandomForest[3] = "-I 128 -K 7 -S 1";
			oRandomForest[4] = "-I 128 -K 8 -S 1";
			oRandomForest[5] = "-I 128 -K 10 -S 1";
			oRandomForest[6] = "-I 128 -K 12 -S 1";
			oRandomForest[7] = "-I 160 -K 4 -S 1";
			oRandomForest[8] = "-I 160 -K 5 -S 1";
			oRandomForest[9] = "-I 160 -K 6 -S 1";
			oRandomForest[10] = "-I 160 -K 7 -S 1";
			oRandomForest[11] = "-I 160 -K 8 -S 1";
			oRandomForest[12] = "-I 160 -K 10 -S 1";
			oRandomForest[13] = "-I 160 -K 12 -S 1";
			oRandomForest[14] = "-I 192 -K 4 -S 1";
			oRandomForest[15] = "-I 192 -K 5 -S 1";
			oRandomForest[16] = "-I 192 -K 6 -S 1";
			oRandomForest[17] = "-I 192 -K 7 -S 1";
			oRandomForest[18] = "-I 192 -K 8 -S 1";
			oRandomForest[19] = "-I 192 -K 10 -S 1";
			oRandomForest[20] = "-I 192 -K 12 -S 1";
			
			String[] oRBFNetwork = new String[16];
			oRBFNetwork[0] = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 0.1";
			oRBFNetwork[1] = "-B 2 -S 1 -R 1.0E-8 -M -1 -W 0.1";
			oRBFNetwork[2] = "-B 3 -S 1 -R 1.0E-8 -M -1 -W 0.1";
			oRBFNetwork[3] = "-B 4 -S 1 -R 1.0E-8 -M -1 -W 0.1";
			oRBFNetwork[4] = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 0.3";
			oRBFNetwork[5] = "-B 2 -S 1 -R 1.0E-8 -M -1 -W 0.3";
			oRBFNetwork[6] = "-B 3 -S 1 -R 1.0E-8 -M -1 -W 0.3";
			oRBFNetwork[7] = "-B 4 -S 1 -R 1.0E-8 -M -1 -W 0.3";
			oRBFNetwork[8] = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 1";
			oRBFNetwork[9] = "-B 2 -S 1 -R 1.0E-8 -M -1 -W 1";
			oRBFNetwork[10] = "-B 3 -S 1 -R 1.0E-8 -M -1 -W 1";
			oRBFNetwork[11] = "-B 4 -S 1 -R 1.0E-8 -M -1 -W 1";
			oRBFNetwork[12] = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 3";
			oRBFNetwork[13] = "-B 2 -S 1 -R 1.0E-8 -M -1 -W 3";
			oRBFNetwork[14] = "-B 3 -S 1 -R 1.0E-8 -M -1 -W 3";
			oRBFNetwork[15] = "-B 4 -S 1 -R 1.0E-8 -M -1 -W 3";
			
			String[] oMultilayerPerceptron = new String[81];
			oMultilayerPerceptron[0] = "-L 0.3 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[1] = "-L 0.3 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[2] = "-L 0.3 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[3] = "-L 0.1 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[4] = "-L 0.1 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[5] = "-L 0.1 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[6] = "-L 1.0 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[7] = "-L 1.0 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[8] = "-L 1.0 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			
			oMultilayerPerceptron[9] = "-L 0.3 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[10] = "-L 0.3 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[11] = "-L 0.3 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[12] = "-L 0.1 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[13] = "-L 0.1 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[14] = "-L 0.1 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[15] = "-L 1.0 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[16] = "-L 1.0 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[17] = "-L 1.0 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			
			oMultilayerPerceptron[18] = "-L 0.3 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[19] = "-L 0.3 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[20] = "-L 0.3 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[21] = "-L 0.1 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[22] = "-L 0.1 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[23] = "-L 0.1 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[24] = "-L 1.0 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[25] = "-L 1.0 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 3 -B -D";
			oMultilayerPerceptron[26] = "-L 1.0 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 3 -B -D";
			
			oMultilayerPerceptron[27] = "-L 0.3 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[28] = "-L 0.3 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[29] = "-L 0.3 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[30] = "-L 0.1 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[31] = "-L 0.1 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[32] = "-L 0.1 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[33] = "-L 1.0 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[34] = "-L 1.0 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[35] = "-L 1.0 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			
			oMultilayerPerceptron[36] = "-L 0.3 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[37] = "-L 0.3 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[38] = "-L 0.3 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[39] = "-L 0.1 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[40] = "-L 0.1 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[41] = "-L 0.1 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[42] = "-L 1.0 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[43] = "-L 1.0 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[44] = "-L 1.0 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			
			oMultilayerPerceptron[45] = "-L 0.3 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[46] = "-L 0.3 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[47] = "-L 0.3 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[48] = "-L 0.1 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[49] = "-L 0.1 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[50] = "-L 0.1 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[51] = "-L 1.0 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[52] = "-L 1.0 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 2 -B -D";
			oMultilayerPerceptron[53] = "-L 1.0 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 2 -B -D";
			
			oMultilayerPerceptron[54] = "-L 0.3 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[55] = "-L 0.3 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[56] = "-L 0.3 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[57] = "-L 0.1 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[58] = "-L 0.1 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[59] = "-L 0.1 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[60] = "-L 1.0 -M 0.2 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[61] = "-L 1.0 -M 0.2 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[62] = "-L 1.0 -M 0.2 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			
			oMultilayerPerceptron[63] = "-L 0.3 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[64] = "-L 0.3 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[65] = "-L 0.3 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[66] = "-L 0.1 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[67] = "-L 0.1 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[68] = "-L 0.1 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[69] = "-L 1.0 -M 0.1 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[70] = "-L 1.0 -M 0.1 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[71] = "-L 1.0 -M 0.1 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			
			oMultilayerPerceptron[72] = "-L 0.3 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[73] = "-L 0.3 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[74] = "-L 0.3 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[75] = "-L 0.1 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[76] = "-L 0.1 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[77] = "-L 0.1 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[78] = "-L 1.0 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[79] = "-L 1.0 -M 0.3 -N 400 -V 20 -S 0 -E 20 -H 4 -B -D";
			oMultilayerPerceptron[80] = "-L 1.0 -M 0.3 -N 500 -V 20 -S 0 -E 20 -H 4 -B -D";
			
			String optionsRandomForest = "-I 192 -K 7 -S 1"; // I = # Trees, K = # Features, S = Seed	
//			String optionsRandomForest = "-I 128 -K 5 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 8192.0 -C 10 -E 0.001 -P 0.1 -B -seed 1"; // "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 4096.0 -C 1000 -E 0.001 -P 0.1 -B -seed 1";
			String optionsMultilayerPerceptron = "-L 0.1 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D"; // H = # Hidden Layers, M = Momentum, N = Training Time, L = Learning Rate
			String optionsStacking = "weka.classifiers.meta.Stacking -X 100 -M \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1\" -S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.RandomForest -I 30 -K 0 -S 1\" -B \"weka.classifiers.bayes.RandomForest \"";
			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -I 128 -K 5 -S 1";
			String optionsMetaCost = "weka.classifiers.meta.MetaCost -cost-matrix \"[0.0 30.0 1.0; 10.0 0.0 1.0; 4.0 16.0 0.0]\" -I 2 -P 100 -S 1 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsBagging = "weka.classifiers.meta.Bagging -P 100 -S 1 -I 3 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsJ48 = "weka.classifiers.trees.J48 -C 0.25 -M 2";
			String optionsRBFNetwork = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 1.0";
			String optionsAttributeSelectedClassifierPCANaiveBayes = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.9 -A 15\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 30\" -W weka.classifiers.bayes.NaiveBayes --";
				
			String[] algos = new String[5];
			algos[0] = "NaiveBayes";
			algos[1] = "RandomForest";
			algos[2] = "RBFNetwork";
			algos[3] = "MultilayerPerceptron";
			algos[4] = "LibSVM";
			
			String[] algoOptions = new String[5];
			algoOptions[0] = null;
			algoOptions[1] = optionsRandomForest;
			algoOptions[2] = optionsRBFNetwork;
			algoOptions[3] = optionsMultilayerPerceptron;
			algoOptions[4] = optionsLibSVM;
			
			// STEP 1: Choose dateSet
			// STEP 2: Set the base date (run date or date this stuff is based off if doing this for backtest)
			// STEP 3: Set gain/lose % ratio
			// STEP 4: Set the number of attributes to select
			dateSet = 0;
			Calendar baseDate = Calendar.getInstance();
			int gainR = 1;
			int lossR = 1;
			int numAttributes = 30;
			
			// Data Caching
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTimeInMillis(trainStarts[dateSet].getTimeInMillis());
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTimeInMillis(trainEnds[dateSet].getTimeInMillis());
			
			Calendar testStart = Calendar.getInstance();
			testStart.setTimeInMillis(testStarts[dateSet].getTimeInMillis());
			Calendar testEnd = Calendar.getInstance();
			testEnd.setTimeInMillis(testEnds[dateSet].getTimeInMillis());
			
			System.out.println("Loading training data...");
			for (BarKey bk : barKeys) {
				rawTrainingSet.add(QueryManager.getTrainingSet(bk, trainStart, trainEnd, metricNames, null));
			}
			System.out.println("Complete.");
			System.out.println("Loading test data...");
			for (BarKey bk : barKeys) {
				rawTestSet.add(QueryManager.getTrainingSet(bk, testStart, testEnd, metricNames, null));
			}
			System.out.println("Complete.");
			
			// Run Time!
			for (int a = 0; a <= 3; a++) {
				String classifierName = algos[a];
				String classifierOptions = algoOptions[a];
				
				String notes = "AS-" + numAttributes + " 5M " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + mods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime());
			
				// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
				/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
				for (float b = 0.1f; b <= 1.21; b += .1f) {
					float gain = b;
					float loss = b * ((float)lossR / (float)gainR);
					if (lossR > gainR) {
						loss = b;
						gain = b * ((float)gainR / (float)lossR);
					}
					Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, gain, loss, 600, barKeys, false, false, true, false, true, true, numAttributes, "Unbounded", metricNames, metricDiscreteValueHash, notes, baseDate);
				}	
			}
			
			// Hyperparam tests
//			for (int a = 3; a <= 3; a++) {
//				String classifierName = algos[a];
//				for (int c = 0; c < 81; c++) {
//					String classifierOptions = oMultilayerPerceptron[c];
//					
//					String notes = "AS-" + numAttributes + " 5M " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + barMods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime());
//				
//					/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
//					for (float b = 0.1f; b <= 1.51; b += .1f) {
//						Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, b, b * ((float)lossR / (float)gainR), 600, barKeys, false, false, true, false, true, true, numAttributes, "Unbounded", metricNames, metricDiscreteValueHash, notes);
//					}	
//				}
//			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void buildBacktestModels(Calendar baseDate) {
		try {
			SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");

			// Load date ranges for Train & Test sets
			long baseTime = Calendar.getInstance().getTimeInMillis();
			
			for (int a = 0; a < numDateSets * 2; a += 2) {
				Calendar c1 = Calendar.getInstance();
				c1.setTimeInMillis(baseDate.getTimeInMillis());
				c1.setTimeInMillis(baseTime - (a * MS_WEEK));
				testEnds[a / 2] = c1;
				
				Calendar c2 = Calendar.getInstance();
				c2.setTimeInMillis(baseDate.getTimeInMillis());
				c2.setTimeInMillis((baseTime - MS_90DAYS) - (a * 4 * MS_WEEK));
				testStarts[a / 2] = c2;
				
				Calendar c3 = Calendar.getInstance();
				c3.setTimeInMillis(baseDate.getTimeInMillis());
				c3.setTimeInMillis(testStarts[a / 2].getTimeInMillis() - MS_WEEK);
				trainEnds[a / 2] = c3;
				
				Calendar c4 = Calendar.getInstance();
				c4.setTimeInMillis(baseDate.getTimeInMillis());
				c4.setTimeInMillis((baseTime - MS_360DAYS) - (a * 24 * MS_WEEK));
				trainStarts[a / 2] = c4;
				
				int duration = CalendarUtils.daysBetween(trainStarts[a / 2], trainEnds[a / 2]);
				int mod = duration / 3;
				mod = 5 * (int)(Math.ceil(Math.abs(mod / 5)));
				mods[a / 2] = mod;
			}
		
			// Setup
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
			BarKey bk2 = new BarKey("GBP.USD", BAR_SIZE.BAR_5M);
			BarKey bk3 = new BarKey("EUR.GBP", BAR_SIZE.BAR_5M);
			
			barKeys.add(bk1);
			barKeys.add(bk2);
			barKeys.add(bk3);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
			
			String optionsRandomForest = "-I 192 -K 7 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsMultilayerPerceptron = "-L 0.1 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D"; // H = # Hidden Layers, M = Momentum, N = Training Time, L = Learning Rate
			String optionsRBFNetwork = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 1.0";
				
			String[] algos = new String[4];
			algos[0] = "NaiveBayes";
			algos[1] = "RandomForest";
			algos[2] = "RBFNetwork";
			algos[3] = "MultilayerPerceptron";
			
			String[] algoOptions = new String[4];
			algoOptions[0] = null;
			algoOptions[1] = optionsRandomForest;
			algoOptions[2] = optionsRBFNetwork;
			algoOptions[3] = optionsMultilayerPerceptron;
			
			// STEP 1: Set gain/lose % ratio
			// STEP 2: Set the number of attributes to select
			int gainR = 1;
			int lossR = 1;
			int numAttributes = 30;
				
			for (dateSet = 0; dateSet < numDateSets; dateSet++) {
				// Data Caching
				Calendar trainStart = Calendar.getInstance();
				trainStart.setTimeInMillis(trainStarts[dateSet].getTimeInMillis());
				Calendar trainEnd = Calendar.getInstance();
				trainEnd.setTimeInMillis(trainEnds[dateSet].getTimeInMillis());
				
				Calendar testStart = Calendar.getInstance();
				testStart.setTimeInMillis(testStarts[dateSet].getTimeInMillis());
				Calendar testEnd = Calendar.getInstance();
				testEnd.setTimeInMillis(testEnds[dateSet].getTimeInMillis());
				
				System.out.println("Loading training data...");
				rawTrainingSet.clear();
				for (BarKey bk : barKeys) {
					rawTrainingSet.add(QueryManager.getTrainingSet(bk, trainStart, trainEnd, metricNames, null));
				}
				System.out.println("Complete.");
				System.out.println("Loading test data...");
				rawTestSet.clear();
				for (BarKey bk : barKeys) {
					rawTestSet.add(QueryManager.getTrainingSet(bk, testStart, testEnd, metricNames, null));
				}
				System.out.println("Complete.");
				
				// Run Time!
				for (int a = 0; a <= 3; a++) {
					String classifierName = algos[a];
					String classifierOptions = algoOptions[a];
					
					String notes = "AS-" + numAttributes + " 5M " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + mods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime());
				
					// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
					/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
					for (float b = 0.1f; b <= 1.21; b += .1f) {
						float gain = b;
						float loss = b * ((float)lossR / (float)gainR);
						if (lossR > gainR) {
							loss = b;
							gain = b * ((float)gainR / (float)lossR);
						}
						Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, gain, loss, 600, barKeys, false, false, true, false, true, true, numAttributes, "Unbounded", metricNames, metricDiscreteValueHash, notes, baseDate);
					}	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Classifies as Win, Lose, or Draw.  Takes a bar and looks ahead for x periods to see if Close or Stop conditions are met.  If neither are met, it is a Draw
	 * 
	 * @param targetGain
	 * @param minLoss
	 * @param numPeriods
	 * @param bk
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest (train or test)
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataPeriodBounded(float targetGain, float minLoss, int numPeriods, 
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeDraw, boolean includeSymbol,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			
			int winCount = 0;
			int lossCount = 0;
			int drawCount = 0;
			long startMS = Calendar.getInstance().getTimeInMillis();
			
			// These are always ordered newest to oldest
			for (ArrayList<HashMap<String, Object>> rawSet : trainOrTest.equals("train") ? rawTrainingSet : rawTestSet) {
				
				ArrayList<Float> nextXCloses = new ArrayList<Float>();
				ArrayList<Float> nextXHighs = new ArrayList<Float>();
				ArrayList<Float> nextXLows = new ArrayList<Float>();
				
				for (HashMap<String, Object> record : rawSet) {
					float close = (float)record.get("close");
					float high = (float)record.get("high");
					float low = (float)record.get("low");
					
					if (nextXCloses.size() > numPeriods) {
						nextXCloses.remove(0);
					}
					if (nextXHighs.size() > numPeriods) {
						nextXHighs.remove(0);
					}
					if (nextXLows.size() > numPeriods) {
						nextXLows.remove(0);
					}
			
					boolean gainOK = false;
					int targetGainIndex = findTargetGainIndex(nextXHighs, close, targetGain);
					
					boolean lossOK = false;
					int targetLossIndex = findTargetLossIndex(nextXLows, close, targetGain);

					boolean gainStopOK = false;
					if (targetGainIndex != -1) {
						gainOK = true;
						float minPrice = findMin(nextXLows, targetGainIndex); // This checks up through the bar where the successful exit would be made.
						if (minPrice > close * (100f - minLoss) / 100f) {
							gainStopOK = true;
						}
					}

					boolean lossStopOK = false;
					if (targetLossIndex != -1) {
						lossOK = true;
						float maxPrice = findMax(nextXHighs, targetLossIndex);
						if (maxPrice < close * (100f + minLoss) / 100f) {
							lossStopOK = true;
						}
					}

					// Class
					String classPart = "";
					if (gainOK && gainStopOK) {
						classPart = "Win";
						winCount++;
					}
					else if (lossStopOK && lossOK) {
						classPart = "Lose";
						lossCount++;
					}
					else {
						// Runs to end of data without resolving
						classPart = "Draw";
						drawCount++;
					}
					
//					System.out.println(close + ", " + gainOK + ", " + gainStopOK + "\t," + lossOK + ", " + lossStopOK + ", " + classPart);
					
					if (classPart.equals("Win") || classPart.equals("Lose") || (classPart.equals("Draw") && includeDraw)) {
						float hour = (int)record.get("hour");
						String symbol = record.get("symbol").toString();
						String duration = record.get("duration").toString();
						
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
			
//						System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
						
						if (!metricPart.equals("")) {
							String recordLine = referencePart + metricPart + classPart;
							ArrayList<Object> valueList = new ArrayList<Object>();
							String[] values = recordLine.split(",");
							valueList.addAll(Arrays.asList(values));
							valuesList.add(valueList);
						}
					}
					
					nextXCloses.add(close); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
					nextXHighs.add(high); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
					nextXLows.add(low); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
				}
			}
			
			long endMS = Calendar.getInstance().getTimeInMillis();
//			System.out.println("ms: " + (endMS - startMS));
//			System.out.println(trainOrTest + ": " + winCount + ", " + lossCount + ", " + drawCount);
			
			// Optional write to file
			if (saveARFF) {
				writeToFile(valuesList);
			}
			
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
	 * @param targetGain
	 * @param minLoss
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataPeriodUnbounded(float targetGain, float minLoss, 
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			ArrayList<ArrayList<Object>> valuesListW = new ArrayList<ArrayList<Object>>();
			ArrayList<ArrayList<Object>> valuesListL = new ArrayList<ArrayList<Object>>();
	
			int winCount = 0;
			int lossCount = 0;
			int drawCount = 0;
			long startMS = Calendar.getInstance().getTimeInMillis();
			
			// Both are ordered newest to oldest
			for (ArrayList<HashMap<String, Object>> rawSet : trainOrTest.equals("train") ? rawTrainingSet : rawTestSet) {
				
				ArrayList<Float> futureHighs = new ArrayList<Float>();
				ArrayList<Float> futureLows = new ArrayList<Float>();
				
				for (HashMap<String, Object> record : rawSet) {
					float open = (float)record.get("open");
					float close = (float)record.get("close");
					float high = (float)record.get("high");
					float low = (float)record.get("low");
					Timestamp startTS = (Timestamp)record.get("start");
					
					// See if this is a bar suitable to include in the final set
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(startTS.getTime());
					boolean suitableBar = false;
					int minuteOfDay = (c.get(Calendar.HOUR_OF_DAY) * 60) + c.get(Calendar.MINUTE);
					if (minuteOfDay % mods[dateSet] == 0) {
						suitableBar = true;
					}
					
//					System.out.println(close);
					
					if (suitableBar) {
						boolean gainHit = false;
						int targetGainIndex = findTargetGainIndex(futureHighs, close, targetGain);
						
						boolean lossHit = false;
						int targetLossIndex = findTargetLossIndex(futureLows, close, minLoss);
			 
						if (targetGainIndex != -1) {
							gainHit = true;
						}
						if (targetLossIndex != -1) {
							lossHit = true;
						}
						
						if (gainHit && lossHit) { // See which one came first
							if (targetGainIndex < targetLossIndex) { // Lower indexes are sooner
								lossHit = false;
							}
							else {
								gainHit = false;
							}
						}
						
						// Class
						String classPart = "";
						if (gainHit) {
							classPart = "Win";
							winCount++;
						}
						else if (lossHit) {
							classPart = "Lose";
							lossCount++;
						}
						else {
							// Runs to end of data without resolving
							classPart = "Draw";
							drawCount++;
						}
						
	//					System.out.println(close + ", " + gainOK + ", " + gainStopOK + "\t," + lossOK + ", " + lossStopOK + ", " + classPart);
						
						if (classPart.equals("Win") || classPart.equals("Lose")) {
							float hour = (int)record.get("hour");
							String symbol = record.get("symbol").toString();
							String duration = record.get("duration").toString();
							
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
						
//							System.out.println(symbol + ", " + classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
	
							if (!metricPart.equals("")) {
								String recordLine = referencePart + metricPart + classPart;
								ArrayList<Object> valueList = new ArrayList<Object>();
								String[] values = recordLine.split(",");
								valueList.addAll(Arrays.asList(values));
								if (classPart.equals("Win")) {
									valuesListW.add(valueList);
								}
								else if (classPart.equals("Lose")) {
									valuesListL.add(valueList);
								}
							}
						}
					}
					
					futureHighs.add(high); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
					futureLows.add(low); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
				}
			}
			
			
			// Even out the number of W & L instances on training sets so the models aren't trained to be biased one way or another.
			if (trainOrTest.equals("train")) {
				// Shuffle them so when we have to take a subset out of one of them, they're randomly distributed.
				Collections.shuffle(valuesListW, new Random(System.nanoTime()));
				Collections.shuffle(valuesListL, new Random(System.nanoTime()));

				int lowestCount = winCount;
				if (lossCount < winCount) {
					lowestCount = lossCount;
				}
				
				for (int a = 0; a < lowestCount; a++) {
					valuesList.add(valuesListW.get(a));
					valuesList.add(valuesListL.get(a));
				}
			}
			else if (trainOrTest.equals("test")) {
				valuesList.addAll(valuesListW);
				valuesList.addAll(valuesListL);
			}
		
			long endMS = Calendar.getInstance().getTimeInMillis();
//			System.out.println("ms: " + (endMS - startMS));
//			System.out.println(trainOrTest + ": " + winCount + ", " + lossCount + ", " + drawCount);
			
			// Optional write to file
			if (saveARFF) {
				writeToFile(valuesList);
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

	 * @param numPeriods
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataFixedInterval(int numPeriods,
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();

//			// These are always ordered newest to oldest.
//			for (int a = numPeriods; a < (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).size(); a++) {
//				HashMap<String, Object> thisInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a);
//				HashMap<String, Object> futureInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a - numPeriods);
//				
//				float close = (float)thisInstance.get("close");
//				float hour = (int)thisInstance.get("hour");
//				String symbol = thisInstance.get("symbol").toString();
//				String duration = thisInstance.get("duration").toString();
//				
//				// Class
//				String classPart = "Lose";
//				if ((float)futureInstance.get("close") > close) {
//					classPart = "Win";
//				}
//				
//				// Non-Metric Optional Features
//				String referencePart = "";
//				if (includeClose) {
//					referencePart = close + ", ";
//				}
//				if (includeHour) {
//					referencePart += hour + ", ";
//				}
//				if (includeSymbol) {
//					referencePart += symbol + ", ";
//				}
//	
//				// Metric Buckets (or values)
//				String metricPart = "";
//				for (String metricName : metricNames) {
//					MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
//					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
//					if (bucketCutoffValues != null) {
//						float metricValue = (float)thisInstance.get(metricName);
//						
//						int bucketNum = 0;
//						for (int b = bucketCutoffValues.size() - 1; b >= 0; b--) {
//							float bucketCutoffValue = bucketCutoffValues.get(b);
//							if (metricValue < bucketCutoffValue) {
//								break;
//							}
//							bucketNum++;
//						}
//						
//						if (useNormalizedNumericValues) {
//							metricPart += String.format("%.5f", metricValue) + ", ";
//						}
//						else {
//							metricPart += ("B" + bucketNum + ", ");
//						}
//					}
//				}
//				
////				System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
//				
//				if (!metricPart.equals("")) {
//					String recordLine = referencePart + metricPart + classPart;
//					ArrayList<Object> valueList = new ArrayList<Object>();
//					String[] values = recordLine.split(",");
//					valueList.addAll(Arrays.asList(values));
//					valuesList.add(valueList);
//				}
//			}
//			
//			// Optional write to file
//			boolean writeFile = false;
//			if (writeFile) {
//				writeToFile(valuesList);
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
	 * 
	 * @param numPeriods
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createWekaArffDataFixedIntervalRegression(int numPeriods,
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			
//			// These are always ordered newest to oldest
//			for (int a = numPeriods; a < (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).size(); a++) {
//				HashMap<String, Object> thisInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a);
//				HashMap<String, Object> futureInstance = (trainOrTest.equals("train") ? rawTrainingSet : rawTestSet).get(a - numPeriods);
//				
//				float close = (float)thisInstance.get("close");
//				float hour = (int)thisInstance.get("hour");
//				String symbol = thisInstance.get("symbol").toString();
//				String duration = thisInstance.get("duration").toString();
//				
//				// Class
//				float change = (float)futureInstance.get("close") - close;
//				float changeP = change / close * 100f;
//				String classPart = String.format("%.8f", changeP);
//				
//				// Non-Metric Optional Features
//				String referencePart = "";
//				if (includeClose) {
//					referencePart = close + ", ";
//				}
//				if (includeHour) {
//					referencePart += hour + ", ";
//				}
//				if (includeSymbol) {
//					referencePart += symbol + ", ";
//				}
//	
//				// Metric Buckets (or values)
//				String metricPart = "";
//				for (String metricName : metricNames) {
//					MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
//					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
//					if (bucketCutoffValues != null) {
//						float metricValue = (float)thisInstance.get(metricName);
//						
//						int bucketNum = 0;
//						for (int b = bucketCutoffValues.size() - 1; b >= 0; b--) {
//							float bucketCutoffValue = bucketCutoffValues.get(b);
//							if (metricValue < bucketCutoffValue) {
//								break;
//							}
//							bucketNum++;
//						}
//						
//						if (useNormalizedNumericValues) {
//							metricPart += String.format("%.5f", metricValue) + ", ";
//						}
//						else {
//							metricPart += ("B" + bucketNum + ", ");
//						}
//					}
//				}
//				
////				System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
//				
//				if (!metricPart.equals("")) {
//					String recordLine = referencePart + metricPart + classPart;
//					ArrayList<Object> valueList = new ArrayList<Object>();
//					String[] values = recordLine.split(",");
//					valueList.addAll(Arrays.asList(values));
//					valuesList.add(valueList);
//				}
//			}
//
//			// Optional write to file
//			boolean writeFile = false;
//			if (writeFile) {
//				writeToFile(valuesList);
//			}
			
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
			boolean useWeights, boolean useNormalizedNumericValues, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames, null);
			
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
	
	private static float findMin(ArrayList<Float> list, int targetIndex) {
		float min = 1000000000f;
		int listSize = list.size();
		
		for (int a = listSize - 1; a >= listSize - 1 - targetIndex; a--) {
			if (list.get(a) < min) {
				min = list.get(a);
			}
		}
		
		return min;
	}
	
	/**
	 * The list is ordered newest to oldest
	 * 
	 * @param list
	 * @param targetIndex
	 * @return
	 */
	private static float findMax(ArrayList<Float> list, int targetIndex) {
		float max = -1f;
		int listSize = list.size();
		
		for (int a = listSize - 1; a >= listSize - 1 - targetIndex; a--) {
			if (list.get(a) > max) {
				max = list.get(a);
			}
		}

		return max;
	}
	
	/**
	 * The nextXPrices are ordered newest to oldest.  Reversing the collection is not an option because it is needed one level down the stack and
	 * the list is often very big so putting it into a new ArrayList is too expensive (mostly for time, but also memory).
	 * 
	 * @param nextXPrices
	 * @param close
	 * @param targetGain
	 * @return
	 */
	private static int findTargetGainIndex(ArrayList<Float> nextXPrices, float close, float targetGain) {
		float targetClose = close * (100f + targetGain) / 100f;
		int listSize = nextXPrices.size();
		for (int a = listSize - 1; a >= 0; a--) {
			if (nextXPrices.get(a) >= targetClose) {
				return listSize - 1 - a;
			}
		}
	
		return -1;
	}
	
	/**
	 * The nextXPrices are ordered newest to oldest.  Reversing the collection is not an option because it is needed one level down the stack and
	 * the list is often very big so putting it into a new ArrayList is too expensive (mostly for time, but also memory).
	 * 
	 * @param nextXPrices
	 * @param close
	 * @param targetLoss - Positive number
	 * @return
	 */
	private static int findTargetLossIndex(ArrayList<Float> nextXPrices, float close, float targetLoss) {
		float targetClose = close * (100f - targetLoss) / 100f;
		int listSize = nextXPrices.size();
		
		for (int a = listSize - 1; a >= 0; a--) {
			if (nextXPrices.get(a) <= targetClose) {
				return listSize - 1 - a;
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
	
	private static void writeToFile(ArrayList<ArrayList<Object>> instances) {
		try {
			File f = new File("out.arff");
			if (!f.exists()) {
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f, true);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			for (ArrayList<Object> instance : instances) {
				String s = instance.toString();
				s = s.replace("]", "").replace("[", "").replace("  ", " ").trim();
//				System.out.println(s);
				
				bw.write(s);
				bw.newLine();
			}
			
			bw.close();
			fos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main2(String[] args) {
		LinkedList<Float> numbers = new LinkedList<Float>();
		float base = 80f;
		long start = Calendar.getInstance().getTimeInMillis();
		for (int a = 0; a < 1000000; a++) {
			base += (float)(Math.random() - .5f) / 10f;
			numbers.addFirst(base);
		}
		long end = Calendar.getInstance().getTimeInMillis();
		
		int index = findTargetGainIndex(new ArrayList<Float>(numbers), base, .1f);
		System.out.println(end - start);
		
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		
		list1.add("one");
		list1.add("two");
		list1.add("three");
		
		list2.add("four");
		list2.add("five");
		list2.add("six");
		
		String listName = "list2";
		
		for (String s : listName.equals("list1") ? list1 : list2) {
			System.out.println(s);
		}
	}
}
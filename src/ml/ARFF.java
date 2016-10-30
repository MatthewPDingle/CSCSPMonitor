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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;
import utils.CalendarUtils;

public class ARFF {

	// Outer ArrayList = BarKey, Inner ArrayList = days newest to oldest, HashMap = bar & metric key/values
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawTrainingSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawTestSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawCompleteSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	
	private static boolean saveARFF = false;
	
	private static int dateSet = 0;
	
	private static long MS_WEEK = 604800000l;
	private static long MS_90DAYS = 7776000000l;
	private static long MS_360DAYS = 31104000000l;
	
	private static int numDateSets = 6;
	private static Calendar[] trainEnds = new Calendar[numDateSets];
	private static Calendar[] trainStarts = new Calendar[numDateSets];
	private static Calendar[] testEnds = new Calendar[numDateSets];
	private static Calendar[] testStarts = new Calendar[numDateSets];
	private static int[] mods = new int[numDateSets];
	
	private static String[] oNaiveBayes = new String[1];
	private static String[] oRandomForest = new String[12];
	private static String[] oRBFNetwork = new String[16];
	private static String[] oMultilayerPerceptron = new String[81];
	private static String[] oASPCA = new String[6];
	private static String[] oNeuralNetwork = new String[36];
	private static String[] oLogitBoost = new String[9];
	private static String[] oLibSVM = new String[12];
	private static String[] oAdaBoost = new String[4];
	private static String[] oAttributeSelectedClassifier = new String[22];
	private static String[] oLMT = new String[24];
	
	static {
		// Hyper-parameter options
		oNaiveBayes[0] = null;
		
		oRandomForest[0] = "-P 100 -I 128 -num-slots 6 -K 4 -M 1.0 -V 0.001 -S 1";
		oRandomForest[1] = "-P 100 -I 128 -num-slots 6 -K 5 -M 1.0 -V 0.001 -S 1";
		oRandomForest[2] = "-P 100 -I 128 -num-slots 6 -K 6 -M 1.0 -V 0.001 -S 1";
		oRandomForest[3] = "-P 100 -I 128 -num-slots 6 -K 7 -M 1.0 -V 0.001 -S 1";
		oRandomForest[4] = "-P 100 -I 160 -num-slots 6 -K 4 -M 1.0 -V 0.001 -S 1";
		oRandomForest[5] = "-P 100 -I 160 -num-slots 6 -K 5 -M 1.0 -V 0.001 -S 1";
		oRandomForest[6] = "-P 100 -I 160 -num-slots 6 -K 6 -M 1.0 -V 0.001 -S 1";
		oRandomForest[7] = "-P 100 -I 160 -num-slots 6 -K 7 -M 1.0 -V 0.001 -S 1";
		oRandomForest[8] = "-P 100 -I 192 -num-slots 6 -K 4 -M 1.0 -V 0.001 -S 1";
		oRandomForest[9] = "-P 100 -I 192 -num-slots 6 -K 5 -M 1.0 -V 0.001 -S 1";
		oRandomForest[10] = "-P 100 -I 192 -num-slots 6 -K 6 -M 1.0 -V 0.001 -S 1";
		oRandomForest[11] = "-P 100 -I 192 -num-slots 6 -K 7 -M 1.0 -V 0.001 -S 1";
		
		oASPCA[0] = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.95 -A 5\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 5\" -W weka.classifiers.trees.RandomForest -- -I 192 -K 7 -S 1";
		oASPCA[1] = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.95 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.trees.RandomForest -- -I 192 -K 7 -S 1";
		oASPCA[2] = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.95 -A 15\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 15\" -W weka.classifiers.trees.RandomForest -- -I 192 -K 7 -S 1";
		oASPCA[3] = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.95 -A 20\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 20\" -W weka.classifiers.trees.RandomForest -- -I 192 -K 7 -S 1";
		oASPCA[4] = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.95 -A 25\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 25\" -W weka.classifiers.trees.RandomForest -- -I 192 -K 7 -S 1";
		oASPCA[5] = "weka.classifiers.meta.AttributeSelectedClassifier -E \"weka.attributeSelection.PrincipalComponents -R 0.95 -A 30\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 30\" -W weka.classifiers.trees.RandomForest -- -I 192 -K 7 -S 1";
		
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
		
		oNeuralNetwork[0] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.1 -dh 0.3 -iw 0";
		oNeuralNetwork[1] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.1 -dh 0.3 -iw 0";
		oNeuralNetwork[2] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.1 -dh 0.3 -iw 0";
		oNeuralNetwork[3] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.1 -dh 0.3 -iw 0";
		
		oNeuralNetwork[4] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.1 -dh 0.5 -iw 0";
		oNeuralNetwork[5] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.1 -dh 0.5 -iw 0";
		oNeuralNetwork[6] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.1 -dh 0.5 -iw 0";
		oNeuralNetwork[7] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.1 -dh 0.5 -iw 0";
		
		oNeuralNetwork[8] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.1 -dh 0.7 -iw 0";
		oNeuralNetwork[9] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.1 -dh 0.7 -iw 0";
		oNeuralNetwork[10] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.1 -dh 0.7 -iw 0";
		oNeuralNetwork[11] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.1 -dh 0.7 -iw 0";

		oNeuralNetwork[12] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.3 -dh 0.3 -iw 0";
		oNeuralNetwork[13] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.3 -dh 0.3 -iw 0";
		oNeuralNetwork[14] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.3 -dh 0.3 -iw 0";
		oNeuralNetwork[15] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.3 -dh 0.3 -iw 0";
		
		oNeuralNetwork[16] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.3 -dh 0.5 -iw 0";
		oNeuralNetwork[17] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.3 -dh 0.5 -iw 0";
		oNeuralNetwork[18] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.3 -dh 0.5 -iw 0";
		oNeuralNetwork[19] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.3 -dh 0.5 -iw 0";
		
		oNeuralNetwork[20] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.3 -dh 0.7 -iw 0";
		oNeuralNetwork[21] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.3 -dh 0.7 -iw 0";
		oNeuralNetwork[22] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.3 -dh 0.7 -iw 0"; // Best
		oNeuralNetwork[23] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.3 -dh 0.7 -iw 0";
		
		oNeuralNetwork[24] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.5 -dh 0.3 -iw 0";
		oNeuralNetwork[25] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.5 -dh 0.3 -iw 0";
		oNeuralNetwork[26] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.5 -dh 0.3 -iw 0";
		oNeuralNetwork[27] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.5 -dh 0.3 -iw 0";
		
		oNeuralNetwork[28] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.5 -dh 0.5 -iw 0";
		oNeuralNetwork[29] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.5 -dh 0.5 -iw 0";
		oNeuralNetwork[30] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.5 -dh 0.5 -iw 0";
		oNeuralNetwork[31] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.5 -dh 0.5 -iw 0";
		
		oNeuralNetwork[32] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.5 -dh 0.7 -iw 0";
		oNeuralNetwork[33] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.5 -dh 0.7 -iw 0";
		oNeuralNetwork[34] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.5 -dh 0.7 -iw 0";
		oNeuralNetwork[35] =  "-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 100 -di 0.5 -dh 0.7 -iw 0";
			
//		oLogitBoost[0] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 10.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[1] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 10.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[2] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 10.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[3] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 10.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[4] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 10.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[5] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 10.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[6] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 3.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[7] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 3.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[8] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[9] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[10] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[11] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 3.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[12] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 1.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[13] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 1.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[14] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 1.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[15] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 1.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[16] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 1.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[17] = "-P 100 -L -1.7976931348623157E308 -H 0.03 -Z 1.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[18] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 10.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[19] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 10.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[20] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 10.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[21] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 10.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[22] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 10.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[23] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 10.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[24] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[25] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[26] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[27] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[28] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[29] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[30] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 1.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[31] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 1.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[32] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 1.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[33] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 1.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[34] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 1.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[35] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 1.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[36] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[37] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[38] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[39] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[40] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[41] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[42] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[43] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[44] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[45] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[46] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[47] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[48] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 1.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[49] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 1.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[50] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 1.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[51] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 1.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[52] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 1.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[53] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 1.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[54] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 10.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[55] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 10.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[56] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 10.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[57] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 10.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[58] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 10.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[59] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 10.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[60] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 3.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[61] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 3.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[62] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[63] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[64] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[65] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 3.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[66] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 1.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[67] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 1.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[68] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 1.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[69] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 1.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[70] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 1.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[71] = "-P 100 -L -1.7976931348623157E308 -H 1 -Z 1.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[72] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 10.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[73] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 10.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[74] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 10.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[75] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 10.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[76] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 10.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[77] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 10.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[78] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 3.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[79] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 3.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[80] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[81] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[82] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[83] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 3.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		
//		oLogitBoost[84] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 1.0 -O 6 -E 6 -S 1 -I 10   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[85] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 1.0 -O 6 -E 6 -S 1 -I 30   -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[86] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 1.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[87] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 1.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[88] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 1.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
//		oLogitBoost[89] = "-P 100 -L -1.7976931348623157E308 -H 3 -Z 1.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
		
		
		oLogitBoost[0] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		oLogitBoost[1] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		oLogitBoost[2] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		
		oLogitBoost[3] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		oLogitBoost[4] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		oLogitBoost[5] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		
		oLogitBoost[6] = "-P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		oLogitBoost[7] = "-P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 300  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
		oLogitBoost[8] = "-P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0";
	
//		oLogitBoost[0] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 1 -E 1 -S 1 -I 100  -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		oLogitBoost[1] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 1 -E 1 -S 1 -I 300  -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		oLogitBoost[2] = "-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 1 -E 1 -S 1 -I 1000 -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		
//		oLogitBoost[3] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 1 -E 1 -S 1 -I 100  -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		oLogitBoost[4] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 1 -E 1 -S 1 -I 300  -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		oLogitBoost[5] = "-P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 1 -E 1 -S 1 -I 1000 -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		
//		oLogitBoost[6] = "-P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 1 -E 1 -S 1 -I 100  -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		oLogitBoost[7] = "-P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 1 -E 1 -S 1 -I 300  -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
//		oLogitBoost[8] = "-P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 1 -E 1 -S 1 -I 1000 -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1";
		
		
		oLibSVM[0] = 	"-S 0 -K 2 -D 3 -G 0.0  -R 0.0 -N 0.5 -M 16384   -C 1.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[1] = 	"-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 16384   -C 1.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[2] = 	"-S 0 -K 2 -D 3 -G 0.03 -R 0.0 -N 0.5 -M 16384   -C 1.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[3] = 	"-S 0 -K 2 -D 3 -G 0.1  -R 0.0 -N 0.5 -M 16384   -C 1.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[4] = 	"-S 0 -K 2 -D 3 -G 0.0  -R 0.0 -N 0.5 -M 16384   -C 3.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[5] = 	"-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 16384   -C 3.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[6] = 	"-S 0 -K 2 -D 3 -G 0.03 -R 0.0 -N 0.5 -M 16384   -C 3.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[7] = 	"-S 0 -K 2 -D 3 -G 0.1  -R 0.0 -N 0.5 -M 16384   -C 3.0  -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[8] = 	"-S 0 -K 2 -D 3 -G 0.0  -R 0.0 -N 0.5 -M 16384   -C 10.0 -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[9] = 	"-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 16384   -C 10.0 -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[10] = 	"-S 0 -K 2 -D 3 -G 0.03 -R 0.0 -N 0.5 -M 16384   -C 10.0 -E 0.001 -P 0.1 -B -seed 1";
		oLibSVM[11] = 	"-S 0 -K 2 -D 3 -G 0.1  -R 0.0 -N 0.5 -M 16384   -C 10.0 -E 0.001 -P 0.1 -B -seed 1";
	
//		oAdaBoost[0] = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
//		oAdaBoost[1] = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -batch-size 100 -P 100 -I 100 -num-slots 6 -K 5 -M 1.0 -V 0.001 -S 1";
//		oAdaBoost[2] = "-P 100 -S 1 -I 10 -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.2 -dh 0.5 -iw 0";
//		oAdaBoost[3] = "-P 100 -S 1 -I 10 -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
		oAdaBoost[0] = "-P 100 -S 1 -I 10 -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
		oAdaBoost[1] = "-P 100 -S 1 -I 10 -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
		oAdaBoost[2] = "-P 100 -S 1 -I 10 -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
		oAdaBoost[3] = "-P 100 -S 1 -I 10 -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 10.0 -O 6 -E 6 -S 1 -I 3000 -W weka.classifiers.trees.DecisionStump -- -batch-size 100";
		
		oLMT[0] = "-I -1 -M 5 -W 0.0 -do-not-check-capabilities";
		oLMT[1] = "-B -I -1 -M 5 -W 0.0 -do-not-check-capabilities";
		oLMT[2] = "-I -1 -M 5 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[3] = "-B -I -1 -M 5 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[4] = "-I -1 -M 15 -W 0.0 -do-not-check-capabilities";
		oLMT[5] = "-B -I -1 -M 15 -W 0.0 -do-not-check-capabilities";
		oLMT[6] = "-I -1 -M 15 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[7] = "-B -I -1 -M 15 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[8] = "-I -1 -M 30 -W 0.0 -do-not-check-capabilities";
		oLMT[9] = "-B -I -1 -M 30 -W 0.0 -do-not-check-capabilities";
		oLMT[10] = "-I -1 -M 30 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[11] = "-B -I -1 -M 30 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		
		oLMT[12] = "-I -R -1 -M 5 -W 0.0 -do-not-check-capabilities";
		oLMT[13] = "-B -R -I -1 -M 5 -W 0.0 -do-not-check-capabilities";
		oLMT[14] = "-I -R -1 -M 5 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[15] = "-B -R -I -1 -M 5 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[16] = "-I -R -1 -M 15 -W 0.0 -do-not-check-capabilities";
		oLMT[17] = "-B -R -I -1 -M 15 -W 0.0 -do-not-check-capabilities";
		oLMT[18] = "-I -R -1 -M 15 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[19] = "-B -R -I -1 -M 15 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[20] = "-I -R -1 -M 30 -W 0.0 -do-not-check-capabilities";
		oLMT[21] = "-B -R -I -1 -M 30 -W 0.0 -do-not-check-capabilities";
		oLMT[22] = "-I -R -1 -M 30 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		oLMT[23] = "-B -R -I -1 -M 30 -W 0.0 -doNotMakeSplitPointActualValue -do-not-check-capabilities";
		
		
//		oAttributeSelectedClassifier[0] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[1] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[2] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[3] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 30 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[4] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 30 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[5] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 30 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[6] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 100 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[7] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 100 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[8] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 100 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[9] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 300 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[10] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 300 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[11] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 300 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[12] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[13] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.3 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump";
//		oAttributeSelectedClassifier[14] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 1.0 -Z 3.0 -O 6 -E 6 -S 1 -I 1000 -W weka.classifiers.trees.DecisionStump";
		

		oAttributeSelectedClassifier[0] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.2 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[1] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.2 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[2] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.2 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[3] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 150 -di 0.2 -dh 0.5 -iw 0";
		
		oAttributeSelectedClassifier[4] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.1 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[5] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.3 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[6] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 150 -di 0.5 -dh 0.5 -iw 0";
		
		oAttributeSelectedClassifier[7] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10 -di 0.2 -dh 0.2 -iw 0";
		oAttributeSelectedClassifier[8] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30 -di 0.2 -dh 0.4 -iw 0";
		oAttributeSelectedClassifier[9] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60 -di 0.2 -dh 0.6 -iw 0";
		oAttributeSelectedClassifier[10] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 150 -di 0.2 -dh 0.8 -iw 0";
		
		oAttributeSelectedClassifier[11] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10,10 -di 0.2 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[12] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30,30 -di 0.2 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[13] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60,60 -di 0.2 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[14] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 150,150 -di 0.2 -dh 0.5 -iw 0";
		
		oAttributeSelectedClassifier[15] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10,10 -di 0.1 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[16] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60,60 -di 0.3 -dh 0.5 -iw 0";
		oAttributeSelectedClassifier[17] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 150,150 -di 0.5 -dh 0.5 -iw 0";
	
		oAttributeSelectedClassifier[18] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 10,10 -di 0.2 -dh 0.2 -iw 0";
		oAttributeSelectedClassifier[19] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 30,30 -di 0.2 -dh 0.4 -iw 0";
		oAttributeSelectedClassifier[20] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 60,60 -di 0.2 -dh 0.6 -iw 0";
		oAttributeSelectedClassifier[21] = "-E \"weka.attributeSelection.PrincipalComponents -R 0.85 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.functions.NeuralNetwork -- -lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 0 -hl 150,150 -di 0.2 -dh 0.8 -iw 0";
		
	}
	
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
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bk2 = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			BarKey bk3 = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			
			barKeys.add(bk1);
//			barKeys.add(bk2);
//			barKeys.add(bk3);
	
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
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDiscreteValueHash("Percentiles");
			
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
			int numAttributes = 6;
			
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
//			for (int a = 0; a <= 3; a++) {
//				String classifierName = algos[a];
//				String classifierOptions = algoOptions[a];
//				
//				String notes = "AS-" + numAttributes + " 5M " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + mods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime());
//			
//				// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
//				/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
//				for (float b = 0.1f; b <= 1.21; b += .1f) {
//					float gain = b;
//					float loss = b * ((float)lossR / (float)gainR);
//					if (lossR > gainR) {
//						loss = b;
//						gain = b * ((float)gainR / (float)lossR);
//					}
//					Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, gain, loss, 600, barKeys, false, false, true, false, true, true, numAttributes, "Unbounded", metricNames, metricDiscreteValueHash, notes, baseDate);
//				}	
//			}
			
			// Hyperparam tests
			for (int a = 0; a <= 9; a++) {
				String classifierName = "RandomForest";
				for (int c = 0; c < 21; c++) {
					String classifierOptions = oRandomForest[c];
					
					String notes = "AS-" + numAttributes + " 5M " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + mods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime());
				
					/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
					for (float b = 0.1f; b <= 1.51; b += .1f) {
						Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, b, b * ((float)lossR / (float)gainR), 600, barKeys, false, false, true, false, true, true, numAttributes, "Unbounded", metricNames, metricDiscreteValueHash, notes, baseDate);
					}	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadRawCompleteSet(Calendar start, Calendar end) {
		try {
			// Setup
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bk2 = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			BarKey bk3 = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			
			barKeys.add(bk1);
//			barKeys.add(bk2);
//			barKeys.add(bk3);
			
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
			System.out.println("Loading rawCompleteSet...");
			rawCompleteSet.clear();
			for (BarKey bk : barKeys) {
				rawCompleteSet.add(QueryManager.getTrainingSet(bk, start, end, metricNames, null));
			}
			System.out.println("Complete.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadTrainingSet(Calendar start, Calendar end) {
		try {
			rawTrainingSet.clear();
			
			// Go through each BarKey
			for (ArrayList<HashMap<String, Object>> bkRawCompleteSet : rawCompleteSet) {
				System.out.println("bkRawCompleteSet: " + bkRawCompleteSet.size());
				ArrayList<HashMap<String, Object>> bkRawTrainingSet = new ArrayList<HashMap<String, Object>>();
				
				// Go through each day (new to old)
				for (HashMap<String, Object> dayHash : bkRawCompleteSet) {
					Bar bar = (Bar)dayHash.get("bar");
					long startMS = bar.periodStart.getTimeInMillis();
					if (startMS >= start.getTimeInMillis() && startMS <= end.getTimeInMillis()) {
						bkRawTrainingSet.add(dayHash);
					}
				}
				
				rawTrainingSet.add(bkRawTrainingSet);
				System.out.println("bkRawTrainingSet: " + bkRawTrainingSet.size());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadTestSet(Calendar start, Calendar end) {
		try {
			rawTestSet.clear();
			
			// Go through each BarKey
			for (ArrayList<HashMap<String, Object>> bkRawCompleteSet : rawCompleteSet) {
				
				ArrayList<HashMap<String, Object>> bkRawTestSet = new ArrayList<HashMap<String, Object>>();
				
				// Go through each day (new to old)
				for (HashMap<String, Object> dayHash : bkRawCompleteSet) {
					Bar bar = (Bar)dayHash.get("bar");
					long startMS = bar.periodStart.getTimeInMillis();
					if (startMS >= start.getTimeInMillis() && startMS <= end.getTimeInMillis()) {
						bkRawTestSet.add(dayHash);
					}
				}
				
				rawTestSet.add(bkRawTestSet);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void buildBacktestModels(Calendar baseDate) {
		try {
			SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");

			System.out.println("*********************************");
			System.out.println("BaseDate: " + baseDate.getTime().toString() + " - " + baseDate.getTime().toString());
			
			// Load date ranges for Train & Test sets
			long baseTime = baseDate.getTimeInMillis();
			
			for (int a = 0; a < numDateSets * 2; a += 2) {
				Calendar c1 = Calendar.getInstance();
				c1.setTimeInMillis(baseTime - (0 * MS_WEEK));
				testEnds[a / 2] = c1;
				
				Calendar c2 = Calendar.getInstance();
				c2.setTimeInMillis((baseTime - MS_90DAYS) - (a * 2 * MS_WEEK)); // 2
				testStarts[a / 2] = c2;
				
				Calendar c3 = Calendar.getInstance();
				c3.setTimeInMillis(testStarts[a / 2].getTimeInMillis() - MS_WEEK);
				trainEnds[a / 2] = c3;
				
				Calendar c4 = Calendar.getInstance();
				c4.setTimeInMillis((baseTime - MS_360DAYS) - (a * 15 * MS_WEEK)); // 15
				trainStarts[a / 2] = c4;
				
				int duration = CalendarUtils.daysBetween(trainStarts[a / 2], trainEnds[a / 2]);
				int mod = duration / 2; // Originally / 3.  Higher numbers cause the temporal spacing between instances to be less
				mod = 5 * (int)(Math.ceil(Math.abs(mod / 5)));
				mods[a / 2] = 60; // mod; // mod was for 5M.  0 is for 1H
			}
		
			// Setup
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bk2 = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			BarKey bk3 = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			
			barKeys.add(bk1);
//			barKeys.add(bk2);
//			barKeys.add(bk3);
	
			ArrayList<String> metricNames = new ArrayList<String>();
			metricNames.addAll(Constants.METRICS);
			
//			for (String metricName : metricNames) {
//				System.out.println("@attribute " + metricName + " {B0,B1,B2,B3,B4,B5,B6,B7,B8,B9,B10,B11,B12,B13}");
//			}
			
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDiscreteValueHash("Percentiles Set 4");
			
			// Use these classifier options or the static lists at the top of this class.
			String[] optionsNaiveBayes = new String[] {""};
			String[] optionsRandomForest = new String[] {"-P 100 -I 128 -num-slots 6 -do-not-check-capabilities -K 0 -M 1.0 -V 0.001 -S 1"}; // I = # Trees, K = # Features, S = Seed	
			String[] optionsMultilayerPerceptron = new String[] {"-L 0.1 -M 0.3 -N 300 -V 20 -S 0 -E 20 -H 4 -B -D"}; // H = # Hidden Layers, M = Momentum, N = Training Time, L = Learning Rate
			String[] optionsRBFNetwork = new String[] {"-B 1 -S 1 -R 1.0E-8 -M -1 -W 1.0"};
			String[] optionsLogitBoost = new String[] {"-P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 100  -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0"};
			String[] optionsNN = new String[] {"-lr 0.0 -wp 1.0E-8 -mi 1000 -bs 0 -th 6 -hl 30,60 -di 0.2 -dh 0.5 -iw 0"};
			String[] optionsLibSVM = new String[] {"-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 16384 -C 3.0 -E 0.001 -P 0.1 -H -seed 1"};
			String[] optionsASC = new String[] {"-E \"weka.attributeSelection.GainRatioAttributeEval \" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 20\" -W weka.classifiers.functions.MLPClassifier -num-decimal-places 5 -- -N 2 -R 0.003 -O 1.0E-6 -P 6 -E 6 -S 1 -do-not-check-capabilities -num-decimal-places 5 -L weka.classifiers.functions.loss.SquaredError -A weka.classifiers.functions.activation.Sigmoid"};
			String[] optionsFC = new String[] {"-F \"weka.filters.unsupervised.attribute.Discretize -O -B 20 -M -1.0 -R first-last\" -W weka.classifiers.bayes.NaiveBayes -num-decimal-places 5"};
			HashMap<String, String[]> algos = new HashMap<String, String[]>(); // Algo, Options
			algos.put("NaiveBayes", 					optionsNaiveBayes);
//			algos.put("RandomForest", 					optionsRandomForest); // oRandomForest
//			algos.put("RBFNetwork",	 					optionsRBFNetwork); // oRBFNetwork
//			algos.put("MultilayerPerceptron", 			oMultilayerPerceptron);
//			algos.put("AttributeSelectedClassifier", 	optionsASC); // Also oAttributeSelectedClassifier
//			algos.put("NeuralNetwork", 					optionsNN); // or oNeuralNetwork
//			algos.put("LogitBoost", 					optionsLogitBoost); // or oLogitBoost
//			algos.put("LibSVM",							optionsLibSVM); // oLibSVM
//			algos.put("AdaBoostM1",						oAdaBoost);
//			algos.put("LMT", 							oLMT);
//			algos.put("FilteredClassifier", 			optionsFC);
			
			
			// STEP 1: Set gain/lose % ratio
			// STEP 2: Set the number of attributes to select
			int gainR = 1;
			int lossR = 1;
			int numAttributes = 100;
				
			for (dateSet = 5; dateSet < numDateSets; dateSet++) {
				// Data Caching
				Calendar trainStart = Calendar.getInstance();
				trainStart.setTimeInMillis(trainStarts[dateSet].getTimeInMillis());
				Calendar trainEnd = Calendar.getInstance();
				trainEnd.setTimeInMillis(trainEnds[dateSet].getTimeInMillis());
				
				Calendar testStart = Calendar.getInstance();
				testStart.setTimeInMillis(testStarts[dateSet].getTimeInMillis());
				Calendar testEnd = Calendar.getInstance();
				testEnd.setTimeInMillis(testEnds[dateSet].getTimeInMillis());
				
				System.out.println("------------------");
				System.out.println("Train: " + trainStart.getTime().toString() + " - " + trainEnd.getTime().toString());
				System.out.println("Test: " + testStart.getTime().toString() + " - " + testEnd.getTime().toString());
				
				System.out.println("Loading training data...");
				loadTrainingSet(trainStart, trainEnd);
				System.out.println("Complete.");
				System.out.println("Loading test data...");
				loadTestSet(testStart, testEnd);
				System.out.println("Complete.");
				
				// Run Time!
				for (Map.Entry<String, String[]> algo : algos.entrySet()) {
					String classifierName = algo.getKey();
					String[] classifierOptionList = algo.getValue();
					
					for (String classifierOption : classifierOptionList) {
						String notes = "AS-" + numAttributes + " 1H " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + mods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime()) + " " + barKeys.size() + " BKs";
						
						// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
						/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
						Modelling.buildAndEvaluateModel(classifierName, 		classifierOption, trainStart, trainEnd, testStart, testEnd, 1, 1, 1, barKeys, false, false, false, false, false, true, numAttributes, "FixedInterval", metricNames, metricDiscreteValueHash, notes, baseDate);
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
	 * Classifies as Win or Lose.  Takes a bar and looks X bars ahead to see if it finishes up or down.  Ties are not included.
	 * 
	 * @param xBarsAhead
	 * @param useNormalizedNumericValues
	 * @param includeClose
	 * @param includeHour
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @param trainOrTest
	 * @return Outer ArrayList is the instance, Inner LinkedHashMap is a Bar, Values pair for the instance
	 */
	public static ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> createWekaArffDataDirectionAfterXBars(int xBarsAhead,
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol, 
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String trainOrTest) {
		try {	
			ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> valuesList2 = new ArrayList<LinkedHashMap<Bar, ArrayList<Object>>>();
			ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> valuesListW2 = new ArrayList<LinkedHashMap<Bar, ArrayList<Object>>>();
			ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> valuesListL2 = new ArrayList<LinkedHashMap<Bar, ArrayList<Object>>>();
	
			int winCount = 0;
			int lossCount = 0;
			int drawCount = 0;
			double winTotalMovement = 0;
			double lossTotalMovement = 0;
			long startMS = Calendar.getInstance().getTimeInMillis();
			
			// Both are ordered newest to oldest
			for (ArrayList<HashMap<String, Object>> rawSet : trainOrTest.equals("train") ? rawTrainingSet : rawTestSet) {
							
				for (int i = xBarsAhead; i < rawSet.size(); i++) {
					HashMap<String, Object> thisInstance = rawSet.get(i);
					HashMap<String, Object> futureInstance = rawSet.get(i - xBarsAhead);

					Bar thisBar = (Bar)thisInstance.get("bar");
					Bar futureBar = (Bar)futureInstance.get("bar");
					double movement = futureBar.close - thisBar.close;
					
					// See if this is a bar suitable to include in the final set
					boolean suitableBar = false;
					int minuteOfDay = (thisBar.periodStart.get(Calendar.HOUR_OF_DAY) * 60) + thisBar.periodStart.get(Calendar.MINUTE);
					if (minuteOfDay % mods[dateSet] == 0) {
						suitableBar = true;
					}

					if (suitableBar) {						
						// Class
						String classPart = "Draw";
						if (futureBar.close > thisBar.close) {
							classPart = "Win";
							winCount++;
							winTotalMovement += movement;
						}
						else if (futureBar.close < thisBar.close) {
							classPart = "Lose";
							lossCount++;
							lossTotalMovement += movement;
						}
						else {
							drawCount++;
						}
						thisBar.changeAtTarget = futureBar.close - thisBar.close;
						
						if (classPart.equals("Win") || classPart.equals("Lose")) {
							float hour = (int)thisInstance.get("hour");

							// Non-Metric Optional Features
							String referencePart = "";
							if (includeClose) {
								referencePart = thisBar.close + ", ";
							}
							if (includeHour) {
								referencePart += hour + ", ";
							}
							if (includeSymbol) {
								referencePart += thisBar.symbol + ", ";
							}
				
							// Metric Buckets (or values)
							String metricPart = "";
							for (String metricName : metricNames) {
								MetricKey mk = new MetricKey(metricName, thisBar.symbol, thisBar.duration);
								ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
								if (bucketCutoffValues != null) {
									float metricValue = (float)thisInstance.get(metricName);
									
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

							if (!metricPart.equals("")) {
								String recordLine = referencePart + metricPart + classPart;
								ArrayList<Object> valueList = new ArrayList<Object>();
								String[] values = recordLine.split(",");
								valueList.addAll(Arrays.asList(values));
								LinkedHashMap<Bar, ArrayList<Object>> instanceData = new LinkedHashMap<Bar, ArrayList<Object>>();
								instanceData.put(thisBar, valueList);
								double averageWin = (winTotalMovement / winCount);
								double averageLoss = Math.abs((lossTotalMovement / lossCount));
								if (classPart.equals("Win")) {
//									if (trainOrTest.equals("train") && averageWin < averageLoss) {
									if (trainOrTest.equals("train") && movement >= .0003) {
										valuesListW2.add(instanceData);
									}
//									}
								}
								else if (classPart.equals("Lose")) {
//									if (trainOrTest.equals("train") && averageLoss < averageWin) {
									if (trainOrTest.equals("train") && movement <= -.0003) {
										valuesListL2.add(instanceData);
									}
//									}
								}
							}
						}
					}
				}
			}
			
			System.out.println("");
			System.out.println(winCount + "\t " + winTotalMovement + "\t " + valuesListW2.size());
			System.out.println(lossCount + "\t " + lossTotalMovement + "\t " + valuesListL2.size());
			
			// Even out the number of W & L instances on training sets so the models aren't trained to be biased one way or another.
			if (trainOrTest.equals("train")) {
				// Shuffle them so when we have to take a subset out of one of them, they're randomly distributed.
				Collections.shuffle(valuesListW2, new Random(System.nanoTime()));
				Collections.shuffle(valuesListL2, new Random(System.nanoTime()));

				int lowestCount = valuesListW2.size();
				if (valuesListL2.size() < valuesListW2.size()) {
					lowestCount = valuesListL2.size();
				}
				
				for (int a = 0; a < lowestCount; a++) {
					valuesList2.add(valuesListW2.get(a));
					valuesList2.add(valuesListL2.get(a));
				}
			}
			else if (trainOrTest.equals("test")) {
				valuesList2.addAll(valuesListW2);
				valuesList2.addAll(valuesListL2);
			}
		
			long endMS = Calendar.getInstance().getTimeInMillis();
//			System.out.println("ms: " + (endMS - startMS));
//			System.out.println(trainOrTest + ": " + winCount + ", " + lossCount + ", " + drawCount);
			
			// Optional write to file
			saveARFF = false;
			if (saveARFF) {
				writeToFile2(valuesList2);
			}
			
			return valuesList2;
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

	/**
	 * The nextXPrices are ordered newest to oldest.  Reversing the collection is not an option because it is needed one level down the stack and
	 * the list is often very big so putting it into a new ArrayList is too expensive (mostly for time, but also memory).
	 * 
	 * @param xBarsAhead
	 * @param close
	 * @param targetGain
	 * @return
	 */
	private static int isXBarsAheadHigherOrLower(ArrayList<Float> nextXPrices, float close, int xBarsAhead) {
//		float targetClose = close * (100f + targetGain) / 100f;
//		int listSize = nextXPrices.size();
//		for (int a = listSize - 1; a >= 0; a--) {
//			if (nextXPrices.get(a) >= targetClose) {
//				return listSize - 1 - a;
//			}
//		}
		
		if (nextXPrices.get(xBarsAhead) > close) {
			return 1;
		}
		else if (nextXPrices.get(xBarsAhead) < close) {
			return -1;
		}
	
		return 0;
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
	
	private static void writeToFile2(ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> instances) {
		try {
			File f = new File("out.arff");
			if (!f.exists()) {
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f, true);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			
			for (LinkedHashMap<Bar, ArrayList<Object>> instance : instances) {
				ArrayList<ArrayList<Object>> valueListWrapper = new ArrayList<ArrayList<Object>>(instance.values());
				ArrayList<Object> valueList = valueListWrapper.get(0);
				String s = valueList.toString();
				s = s.replace("]", "").replace("[", "").replace("  ", " ").trim();
				
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
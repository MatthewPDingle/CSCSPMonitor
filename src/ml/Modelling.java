package ml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.tuple.Pair;

import constants.Constants;
import data.BarKey;
import data.MetricKey;
import data.Model;
import dbio.QueryManager;
import tests.PValue;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IB1;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.ClassificationViaClustering;
import weka.classifiers.meta.MetaCost;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.FT;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.NBTree;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.PrincipalComponents;

public class Modelling {

	private static FastVector symbolBuckets = new FastVector();
	private static FastVector metricBuckets = new FastVector();
	private static FastVector classBuckets3 = new FastVector();
	private static FastVector classBuckets2 = new FastVector();
	
	static {
		for (int a = 0; a <= 13; a++) {
			metricBuckets.addElement("B" + a);
		}
		
		symbolBuckets.addElement("EUR.USD");
		symbolBuckets.addElement("EUR.GBP");
		symbolBuckets.addElement("GBP.USD");
		
		classBuckets2.addElement("Lose");
		classBuckets2.addElement("Win");
		
		classBuckets3.addElement("Lose");
		classBuckets3.addElement("Win");
		classBuckets3.addElement("Draw");
	}
	
	public static void main(String[] args) {
		long start = Calendar.getInstance().getTimeInMillis();
		Classifier classifier = loadZippedModel("RandomForest246.model", null);
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("Took " + (end - start) + "ms");
		System.out.println(classifier.toString());
	}

	public static Classifier loadModel(String modelName, String modelsPath) {
		try {
			ObjectInputStream ois = null;
			if (modelsPath == null || modelsPath.length() == 0) {
				FileInputStream fis = new FileInputStream("weka\\models\\" + modelName);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ois = new ObjectInputStream(bis);
			}
			else {
				FileInputStream fis = new FileInputStream(modelsPath + "\\" + modelName);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ois = new ObjectInputStream(bis);
			}
			Classifier classifier = (Classifier)ois.readObject();
			ois.close();
			return classifier;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Classifier loadZippedModel(String modelName, String modelsPath) {
		try {
			ObjectInputStream ois = null;
			
			if (modelsPath == null || modelsPath.length() == 0) {	
				new FileInputStream("weka\\models\\" + modelName + ".zip");
				FileInputStream fis = new FileInputStream("weka\\models\\" + modelName + ".zip"); 
				BufferedInputStream bis = new BufferedInputStream(fis);
				ZipInputStream zis = new ZipInputStream(bis);
				ZipEntry ze = zis.getNextEntry();
				BufferedInputStream bis2 = null;
				if (ze != null) {
					bis2 = new BufferedInputStream(zis);
					ois = new ObjectInputStream(bis2);
				}
			}
			else {
				FileInputStream fis = new FileInputStream(modelsPath + "\\" + modelName + ".zip");
				BufferedInputStream bis = new BufferedInputStream(fis);
				ZipInputStream zis = new ZipInputStream(bis);
				ZipEntry ze = zis.getNextEntry();
				BufferedInputStream bis2 = null;
				if (ze != null) {
					bis2 = new BufferedInputStream(zis);
					ois = new ObjectInputStream(bis2);
				}
			}
			Classifier classifier = (Classifier)ois.readObject();
			ois.close();
			return classifier;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Instances loadData(ArrayList<String> featureNames, ArrayList<ArrayList<Object>> valuesList, 
			boolean useNormalizedNumericValues, int numClasses) {
		try {
			// Setup the attributes / features
			FastVector attributes = new FastVector();
					
			int closeIndex = -1;
			int hourIndex = -1;
			int symbolIndex = -1;
			for (String featureName : featureNames) {
				if (featureName.equals("close")) {
					closeIndex = featureNames.indexOf(featureName);
					attributes.addElement(new Attribute("close"));
				}
				else if (featureName.equals("hour")) {
					hourIndex = featureNames.indexOf(featureName);
					attributes.addElement(new Attribute("hour"));
				}
				else if (featureName.equals("symbol")) {
					symbolIndex = featureNames.indexOf(featureName);
					attributes.addElement(new Attribute("symbol", symbolBuckets));
				}
				else if (!featureName.equals("class")) {
					if (useNormalizedNumericValues) {
						attributes.addElement(new Attribute(featureName)); // For numeric values
					}
					else {
						attributes.addElement(new Attribute(featureName, metricBuckets)); // For discretized values
					}
				}
			}
			// Add class
			int classIndex = featureNames.size() - 1;
			if (numClasses == 2) {
				attributes.addElement(new Attribute("class", classBuckets2));
			}
			else if (numClasses == 3) {
				attributes.addElement(new Attribute("class", classBuckets3));
			}

			// Setup the instances / values / data
			int capacity = 1000000;
			String type = "Training"; 
			Instances instances = new Instances(type, attributes, capacity);
			instances.setClassIndex(classIndex);
			
			for (ArrayList<Object> valueList : valuesList) {
				double[] values = new double[instances.numAttributes()];
				for (int i = 0; i < values.length; i++) {
					if (i == classIndex) {
						String featureBucket = valueList.get(i).toString().trim(); 
						if (!featureBucket.equals("?")) {
							values[i] = instances.attribute(i).indexOfValue(featureBucket); 
						}
						else {
							values[i] = 0;
						}
					}
					else if (i == closeIndex) {
						values[i] = Double.parseDouble(valueList.get(i).toString()); 
					}
					else if (i == hourIndex) {
						values[i] = (int)Double.parseDouble(valueList.get(i).toString()); 
					}
					else if (i == symbolIndex) {
						String symbolBucket = valueList.get(i).toString().trim();
						values[i] = instances.attribute(i).indexOfValue(symbolBucket);
					}
					else {
						if (useNormalizedNumericValues) {
							values[i] = Double.parseDouble(valueList.get(i).toString()); // This one is for all numeric values
						}
						else {
							String featureBucket = valueList.get(i).toString().trim(); // These two are for discretized values
							values[i] = instances.attribute(i).indexOfValue(featureBucket);
						}
					}
				}

				Instance instance = new Instance(1, values);
				instances.add(instance);
			}

			return instances;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
	}
	
	public static ArrayList<String> selectAttributes(float targetGain, float minLoss, int numBars, 
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeDraw, boolean includeSymbol,
			int maxNumDesiredAttributes, float minInfoGain,
			String strategy, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		ArrayList<String> metrics = new ArrayList<String>();
		try {
			float sellMetricValue = targetGain;
			float stopMetricValue = minLoss;
			
			int numClasses = 2;
			if (strategy.equals("Bounded") && includeDraw) {
				numClasses = 3;
			}
			
			System.out.print("Creating Train datasets...");
			ArrayList<ArrayList<Object>> trainValuesList = new ArrayList<ArrayList<Object>>();

			if (strategy.equals("Bounded")) {
				trainValuesList.addAll(ARFF.createWekaArffDataPeriodBounded(sellMetricValue, stopMetricValue, numBars, useNormalizedNumericValues, includeClose, includeHour, includeDraw, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
			}
			else if (strategy.equals("Unbounded")) {
				trainValuesList.addAll(ARFF.createWekaArffDataPeriodUnbounded(sellMetricValue, stopMetricValue, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
			}
			else if (strategy.equals("FixedInterval")) {
				trainValuesList.addAll(ARFF.createWekaArffDataFixedInterval(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
			}
			else if (strategy.equals("FixedIntervalRegression")) {
				trainValuesList.addAll(ARFF.createWekaArffDataFixedIntervalRegression(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
			}
			
			Instances trainInstances = Modelling.loadData(Constants.METRICS, trainValuesList, useNormalizedNumericValues, numClasses);
			System.out.println("Complete.");
			
			AttributeSelection attributeSelection = new AttributeSelection();
			InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
			Ranker ranker = new Ranker();
			ranker.setNumToSelect(Constants.METRICS.size());
			attributeSelection.setEvaluator(infoGain);
			attributeSelection.setSearch(ranker);
			attributeSelection.setInputFormat(trainInstances);
				
			ArrayList<Pair<Double, String>> metricScores = new ArrayList<Pair<Double, String>>();
			for (int a = 0; a < trainInstances.numAttributes(); a++) {
				Attribute attribute = trainInstances.attribute(a);
				String name = attribute.name();
				double infoGainScore = infoGain.evaluateAttribute(a);

				if (infoGainScore >= minInfoGain && !name.equals("hour") && !name.equals("symbol") && !name.equals("close") && !name.equals("class")) {	
					Pair pair = Pair.of(infoGainScore, name);
					
					metricScores.add(pair);
					if (metricScores.size() > maxNumDesiredAttributes) {
						Collections.sort(metricScores);
						metricScores.remove(0);
					}
				}
			}
			
			for (Pair p : metricScores) {
				metrics.add(p.getRight().toString());
				System.out.println(p.getRight().toString());
			}
			Collections.reverse(metrics); // So they're ordered best to worst
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metrics;
	}
	
	public static void buildAndEvaluateModel(String algo, String params, Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, 
			float targetGain, float minLoss, int numBars, ArrayList<BarKey> barKeys, 
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeDraw, boolean includeSymbol, boolean selectAttributes,
			int maxNumDesiredAttributes,
			String strategy, ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String notes, Calendar baseDate) {
		try {
			System.out.println("Starting " + algo);
			String sellMetric = Constants.OTHER_SELL_METRIC_PERCENT_UP;
			float sellMetricValue = targetGain;
			String stopMetric = Constants.STOP_METRIC_PERCENT_DOWN;
			float stopMetricValue = minLoss;
			
			int numClasses = 2;
			if (strategy.equals("Bounded") && includeDraw) {
				numClasses = 3;
			}
		
			System.out.print("Creating Train & Test datasets...");
			ArrayList<ArrayList<Object>> trainValuesList = new ArrayList<ArrayList<Object>>();
			ArrayList<ArrayList<Object>> testValuesList = new ArrayList<ArrayList<Object>>();

			if (strategy.equals("Bounded")) {
				trainValuesList.addAll(ARFF.createWekaArffDataPeriodBounded(sellMetricValue, stopMetricValue, numBars, useNormalizedNumericValues, includeClose, includeHour, includeDraw, includeSymbol, metricNames, metricDiscreteValueHash, "train"));
				testValuesList.addAll(ARFF.createWekaArffDataPeriodBounded(sellMetricValue, stopMetricValue, numBars, useNormalizedNumericValues, includeClose, includeHour, includeDraw, includeSymbol, metricNames, metricDiscreteValueHash, "test"));
			}
			else if (strategy.equals("Unbounded")) {
				trainValuesList.addAll(ARFF.createWekaArffDataPeriodUnbounded(sellMetricValue, stopMetricValue, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "train"));
				testValuesList.addAll(ARFF.createWekaArffDataPeriodUnbounded(sellMetricValue, stopMetricValue, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "test"));
			}
			else if (strategy.equals("FixedInterval")) {
				trainValuesList.addAll(ARFF.createWekaArffDataFixedInterval(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "train"));
				testValuesList.addAll(ARFF.createWekaArffDataFixedInterval(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "test"));
			}
			else if (strategy.equals("FixedIntervalRegression")) {
				trainValuesList.addAll(ARFF.createWekaArffDataFixedIntervalRegression(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "train"));
				testValuesList.addAll(ARFF.createWekaArffDataFixedIntervalRegression(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "test"));
			}
			
			if (includeSymbol) {
				if (!metricNames.contains("symbol")) {
					metricNames.add(0, "symbol");
				}
			}
			if (includeHour) {
				if (!metricNames.contains("hour")) {
					metricNames.add(0, "hour");
				}
			}
			if (includeClose) {
				if (!metricNames.contains("close")) {
					metricNames.add(0, "close");
				}
			}
			if (!metricNames.contains("class")) {
				metricNames.add("class");
			}

//			testValuesList = ARFF.removeDuplicates(testValuesList); // Takes too long as-is on 5 year train datasets.
			System.out.println("Complete.");
			
			// Training & Cross Validation Data
			System.out.print("Cross Validating...");
			Instances trainInstances = Modelling.loadData(metricNames, trainValuesList, useNormalizedNumericValues, numClasses);
			
			// Attribute Selection Train
			AttributeSelection attributeSelection = new AttributeSelection();
			ArrayList<String> selectedMetrics = new ArrayList<String>(metricNames);
			if (selectAttributes) {
				System.out.println("Selecting attributes...");
				InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
				Ranker ranker = new Ranker();
				ranker.setNumToSelect(maxNumDesiredAttributes);
				attributeSelection.setEvaluator(infoGain);
				attributeSelection.setSearch(ranker);
				attributeSelection.setInputFormat(trainInstances);
				
				trainInstances = Filter.useFilter(trainInstances, attributeSelection);
				
				// Get the names of the selected metrics
				ArrayList<Pair<Double, String>> metricScores = new ArrayList<Pair<Double, String>>();
				for (int a = 0; a < trainInstances.numAttributes(); a++) {
					Attribute attribute = trainInstances.attribute(a);
					String name = attribute.name();
					double infoGainScore = infoGain.evaluateAttribute(a);

					Pair pair = Pair.of(infoGainScore, name);
					metricScores.add(pair);
				}
				
				Collections.sort(metricScores);
				selectedMetrics.clear();
//				for (Pair p : metricScores) {
//					selectedMetrics.add(p.getRight().toString());
//				}
//				Collections.reverse(selectedMetrics); // So they're ordered best to worst
				
				for (int a = 0; a < trainInstances.numAttributes(); a++) {
					selectedMetrics.add(trainInstances.attribute(a).name());
				}
			} 
			
			Normalize normalize = new Normalize();
			PrincipalComponents pc = new PrincipalComponents();
			if (useNormalizedNumericValues) {
				normalize.setInputFormat(trainInstances);
				trainInstances = Filter.useFilter(trainInstances, normalize);

//				pc.setInputFormat(trainInstances);
//				trainInstances = Filter.useFilter(trainInstances, pc);
			}

			System.out.println(trainInstances.numInstances() + " instances of train data");
			
			Classifier classifier = null;
			if (algo.equals("NaiveBayes")) {
				classifier = new NaiveBayes();
			}
			else if (algo.equals("RandomForest")) {
				classifier = new RandomForest();
			}
			else if (algo.equals("J48")) {
				classifier = new J48();
			}
			else if (algo.equals("MultilayerPerceptron")) {
				classifier = new MultilayerPerceptron();
			}
			else if (algo.equals("SimpleLogistic")) {
				classifier = new SimpleLogistic();
			}
			else if (algo.equals("BayesNet")) {
				classifier = new BayesNet();
			}
			else if (algo.equals("LibSVM")) {
				classifier = new LibSVM();
			}
			else if (algo.equals("FT")) { // Slow
				classifier = new FT();
			}
			else if (algo.equals("NBTree")) {
				classifier = new NBTree();
			}
			else if (algo.equals("RandomTree")) {
				classifier = new RandomTree();
			}
			else if (algo.equals("REPTree")) {
				classifier = new REPTree();
			}
			else if (algo.equals("NaiveBayesSimple")) {
				classifier = new NaiveBayesSimple();
			}
			else if (algo.equals("NaiveBayesUpdateable")) {
				classifier = new NaiveBayesUpdateable();
			}
			else if (algo.equals("IB1")) {
				classifier = new IB1();
			}
			else if (algo.equals("RBFNetwork")) {
				classifier = new RBFNetwork();
			}
			else if (algo.equals("ClassificationViaClustering")) {
				classifier = new ClassificationViaClustering();
			}
			else if (algo.equals("Bagging")) { // Ensemble with separate samples that are combined
				classifier = new Bagging();
			}
			else if (algo.equals("Stacking")) { // Blending ensemble method using multiple algos.  Logistic Regression as meta classifier?
				classifier = new Stacking();
			}
			else if (algo.equals("AdaBoostM1")) { // Boosting ensemble starts with base classifier and other ones are created behind it to focus on missclassified instances
				classifier = new AdaBoostM1();
			}
			else if (algo.equals("MetaCost")) {
				classifier = new MetaCost();
			}
			else if (algo.equals("AttributeSelectedClassifier")) {
				classifier = new AttributeSelectedClassifier();
			}
			else {
				return;
			}
			if (params != null) {
				classifier.setOptions(weka.core.Utils.splitOptions(params));
			}
			Evaluation trainEval = new Evaluation(trainInstances);
			trainEval.crossValidateModel(classifier, trainInstances, 10 /*10*/, new Random(1)); // No need to do this if evaluating performance on test set
			System.out.println("Complete.");
			
			int trainDatasetSize = trainInstances.numInstances();
			double[][] trainConfusionMatrix = trainEval.confusionMatrix();
			int trainTrueNegatives = (int)trainConfusionMatrix[0][0];
			int trainFalseNegatives = (int)trainConfusionMatrix[1][0];
			int trainFalsePositives = (int)trainConfusionMatrix[0][1];
			int trainTruePositives = (int)trainConfusionMatrix[1][1];
			double trainTruePositiveRate = trainTruePositives / (double)(trainTruePositives + trainFalseNegatives);
			double trainFalsePositiveRate = trainFalsePositives / (double)(trainFalsePositives + trainTrueNegatives);
			double trainCorrectRate = trainEval.pctCorrect();
			double trainKappa = trainEval.kappa();
			double trainMeanAbsoluteError = trainEval.meanAbsoluteError();
			double trainRootMeanSquaredError = trainEval.rootMeanSquaredError();
			double trainRelativeAbsoluteError = trainEval.relativeAbsoluteError();
			double trainRootRelativeSquaredError = trainEval.rootRelativeSquaredError();
			
			ThresholdCurve trainCurve = new ThresholdCurve();
			Instances trainCurveInstances = trainCurve.getCurve(trainEval.predictions(), 0);
			double trainROCArea = trainCurve.getROCArea(trainCurveInstances);

			// Test Data
			System.out.print("Evaluating Test Data...");
			Instances testInstances = Modelling.loadData(metricNames, testValuesList, useNormalizedNumericValues, numClasses);
			System.out.println(testInstances.numInstances() + " instances of test data");
			
			if (selectAttributes) {
				testInstances = Filter.useFilter(testInstances, attributeSelection);
			}
			
			if (useNormalizedNumericValues) {
				testInstances = Filter.useFilter(testInstances, normalize);
				testInstances = Filter.useFilter(testInstances, pc);
			}
			
			classifier.buildClassifier(trainInstances);
			Evaluation testEval = new Evaluation(trainInstances);
			testEval.evaluateModel(classifier, testInstances);
			
			// Break the predictions up into buckets of size .1 each (.5 - 1.0) to get the percent correct per bucket.  We want to see higher accuracy in the more confident buckets.
			FastVector predictions = testEval.predictions();
			
			double[] correctCounts = new double[5];
			double[] incorrectCounts = new double[5];
			double[] testBucketPercentCorrect = new double[5];
			double[] testBucketDistribution = new double[5]; // What percent of the predictions fall in each bucket
			double[] testBucketPValues = new double[5];
		
			ArrayList<Double> predictionScores = new ArrayList<Double>();
			ArrayList<Boolean> predictionResults = new ArrayList<Boolean>();
			
			for (int a = 0; a < predictions.size(); a++) {
				NominalPrediction np = (NominalPrediction)predictions.elementAt(a);
				if (np.distribution().length == 2) {
					
					boolean correct = false;
					if (np.actual() == np.predicted()) {
						correct = true;
					}

					double maxDistribution = np.distribution()[0];
					if (np.distribution()[1] > np.distribution()[0]) {
						maxDistribution = np.distribution()[1];
					}
				
					int bucket = -1; // .5 - .6 = [0], .6 - .7 = [1], .7 - .8 = [2], .8 - .9 = [3], .9 - 1.0 = [4]
					if (maxDistribution >= .5 && maxDistribution < .6) {
						bucket = 0;
					}
					else if (maxDistribution >= .6 && maxDistribution < .7) {
						bucket = 1;
					}
					else if (maxDistribution >= .7 && maxDistribution < .8) {
						bucket = 2;
					}
					else if (maxDistribution >= .8 && maxDistribution < .9) {
						bucket = 3;
					}
					else if (maxDistribution >= .9) {
						bucket = 4;
					}
					
					if (correct) {
						correctCounts[bucket]++;
					}
					else {
						incorrectCounts[bucket]++;
					}
					
					predictionScores.add(a, np.distribution()[1]);
					predictionResults.add(a, correct);
				}
				else if (np.distribution().length == 3) {
					System.out.println(np.actual() + ", " + np.predicted() + ", " + np.distribution()[0] + ", " + np.distribution()[1] + ", " + np.distribution()[2]);
				}
			}

			DecimalFormat df5 = new DecimalFormat("#.#####");
			for (int a = 0; a < 5; a++) {
				if (correctCounts[a] + incorrectCounts[a] == 0) {
					testBucketPercentCorrect[a] = 0;
				}
				else {
					testBucketPercentCorrect[a] = Double.parseDouble(df5.format(correctCounts[a] / (correctCounts[a] + incorrectCounts[a])));
				}
				if (predictions.size() == 0) {
					testBucketDistribution[a] = 0;
				}
				else {
					testBucketDistribution[a] = Double.parseDouble(df5.format((correctCounts[a] + incorrectCounts[a]) / predictions.size()));
				}
				
				testBucketPValues[a] = PValue.calculate((int)correctCounts[a], (int)(correctCounts[a] + incorrectCounts[a]), sellMetricValue / (double)(sellMetricValue + stopMetricValue));
			}
			
			System.out.println("Complete.");
			
			int testDatasetSize = testInstances.numInstances();
			double[][] testConfusionMatrix = testEval.confusionMatrix();
			// These held true even when expanding the classes from 2 to 3
			int testTrueNegatives = (int)testConfusionMatrix[0][0]; // Don't buy, and shouldn't
			int testFalseNegatives = (int)testConfusionMatrix[1][0]; // Don't buy, but should
			int testFalsePositives = (int)testConfusionMatrix[0][1]; // Buy, but shouldn't
			int testTruePositives = (int)testConfusionMatrix[1][1]; // Buy, and should

			double testTruePositiveRate = testTruePositives / (double)(testTruePositives + testFalseNegatives); // Percentage of ones you should buy that you actually do
			double testFalsePositiveRate = testFalsePositives / (double)(testFalsePositives + testTrueNegatives); // Percentage of ones you shouldn't buy that you do anyways
			double testCorrectRate = testEval.pctCorrect();
			double testKappa = testEval.kappa();
			double testMeanAbsoluteError = testEval.meanAbsoluteError();
			double testRootMeanSquaredError = testEval.rootMeanSquaredError();
			double testRelativeAbsoluteError = testEval.relativeAbsoluteError();
			double testRootRelativeSquaredError = testEval.rootRelativeSquaredError();
			
			ThresholdCurve testCurve = new ThresholdCurve();
			Instances testCurveInstances = testCurve.getCurve(testEval.predictions(), 0);
			double testROCArea = testCurve.getROCArea(testCurveInstances);
		
			Model m = new Model("bull", "Temp Model File Name", algo, params, barKeys.get(0), true, selectedMetrics, trainStart, trainEnd, testStart, testEnd, 
					sellMetric, sellMetricValue, stopMetric, stopMetricValue, numBars, numClasses,
					trainDatasetSize, trainTrueNegatives, trainFalseNegatives, trainFalsePositives, trainTruePositives,
					trainTruePositiveRate, trainFalsePositiveRate, trainCorrectRate,
					trainKappa, trainMeanAbsoluteError, trainRootMeanSquaredError, trainRelativeAbsoluteError, trainRootRelativeSquaredError,
					trainROCArea,
					testDatasetSize, testTrueNegatives, testFalseNegatives, testFalsePositives, testTruePositives,
					testTruePositiveRate, testFalsePositiveRate, testCorrectRate,
					testKappa, testMeanAbsoluteError, testRootMeanSquaredError, testRelativeAbsoluteError, testRootRelativeSquaredError,
					testROCArea, testBucketPercentCorrect, testBucketDistribution, testBucketPValues, notes, false, false, false, false, baseDate);
			
			System.out.print("Saving Model to DB...");
			int modelID = QueryManager.insertModel(m);
			QueryManager.updateModelFileByID(modelID, algo + modelID + ".model"); // Have to set the modelFile name after the fact because we don't get the ID until the model record is inserted.
			System.out.println("Complete.");
			
			System.out.println("Saving ModelInstances to DB...");
			QueryManager.insertModelInstances(modelID, predictionScores, predictionResults);
			System.out.println("Complete.");
			
			// Save model file
			System.out.print("Saving model file...");
			String fileName = algo + modelID + ".model";
			String filePath = "weka/models/" + fileName;
			weka.core.SerializationHelper.write(filePath, classifier);
			System.out.print("Regular file complete...");
			
			// Save zipped version
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filePath + ".zip"));
			ZipEntry ze = new ZipEntry(fileName);
			zos.putNextEntry(ze);
			byte[] b = new byte[1024];
			int count;
			while ((count = bis.read(b)) > 0) {
				zos.write(b, 0, count);
			}
			zos.closeEntry();
			zos.close();
			bis.close();
			fis.close();
			file.delete();
			System.out.println("Zip file complete.");
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
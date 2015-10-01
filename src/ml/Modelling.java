package ml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import constants.Constants;
import data.BarKey;
import data.MetricKey;
import data.Model;
import dbio.QueryManager;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IB1;
import weka.classifiers.meta.Bagging;
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

public class Modelling {

	private static FastVector metricBuckets = new FastVector();
	private static FastVector bullClassBuckets = new FastVector();
	private static FastVector sellClassBuckets = new FastVector();
	
	static {
		for (int a = 0; a <= 13; a++) {
			metricBuckets.addElement("BUCKET" + a);
		}
		
		bullClassBuckets.addElement("No");
		bullClassBuckets.addElement("Buy");
		
		sellClassBuckets.addElement("No");
		sellClassBuckets.addElement("Sell");
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
	
	public static Instances loadData(ArrayList<String> featureNames, ArrayList<ArrayList<Object>> valuesList, boolean useWeights) {
		try {
			// Setup the attributes / features
			FastVector attributes = new FastVector();

			attributes.addElement(new Attribute("close"));
			attributes.addElement(new Attribute("hour"));
			
			for (String featureName : featureNames) {
				attributes.addElement(new Attribute(featureName, metricBuckets));
			}
			
			attributes.addElement(new Attribute("class", bullClassBuckets));
			
			// Setup the instances / values / data
			int capacity = 50000;
			String type = "Training";
			Instances instances = new Instances(type, attributes, capacity);
			
			for (int a = 0; a < valuesList.size() - 1; a++) {
				ArrayList<Object> valueList = valuesList.get(a);
			}
			for (ArrayList<Object> valueList : valuesList) {
				double[] values = new double[instances.numAttributes()];
				values[0] = Double.parseDouble(valueList.get(0).toString()); // close
				values[1] = (int)Double.parseDouble(valueList.get(1).toString()); // hour
				for (int i = 2; i < values.length; i++) {
					String featureBucket = valueList.get(i).toString().trim();
					values[i] = instances.attribute(i).indexOfValue(featureBucket);
				}
				Instance instance = new Instance(1, values);
				if (useWeights) {
					String sWeight = valueList.get(valueList.size() - 1).toString(); // The weights are handled a bit awkwardly because they come in the values list and the weight has to be parsed out.
					sWeight = sWeight.replace("{", "").replace("}", "");
					instance.setWeight(Double.parseDouble(sWeight));
				}
				instances.add(instance);
			}
			
			instances.setClassIndex(attributes.size() - 1);
			
			return instances;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
	}
	
	public static void buildAndEvaluateModel(String algo, String params, String type, Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, 
			float targetGain, float minLoss, int numBars, BarKey bk, boolean interBarData, boolean useWeights, ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			System.out.println("Starting " + algo);
			String sellMetric = Constants.OTHER_SELL_METRIC_PERCENT_UP;
			float sellMetricValue = targetGain;
			String stopMetric = Constants.STOP_METRIC_PERCENT_DOWN;
			float stopMetricValue = minLoss;
		
			System.out.print("Creating Train & Test datasets...");
			ArrayList<ArrayList<Object>> trainValuesList = ARFF.createWekaArffData(type, trainStart, trainEnd, sellMetricValue, stopMetricValue, numBars, bk, interBarData, useWeights, metricNames, metricDiscreteValueHash);
			ArrayList<ArrayList<Object>> testValuesList = ARFF.createWekaArffData(type, testStart, testEnd, sellMetricValue, stopMetricValue, numBars, bk, interBarData, useWeights, metricNames, metricDiscreteValueHash);
			System.out.println("Complete.");
			
			// Training & Cross Validation Data
			System.out.print("Cross Validating...");
			Instances trainInstances = Modelling.loadData(metricNames, trainValuesList, useWeights);
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
			else if (algo.equals("SimpleLogistic")) {
				classifier = new SimpleLogistic();
			}
			else if (algo.equals("Bagging")) {
				classifier = new Bagging();
			}
			else if (algo.equals("BayesNet")) {
				classifier = new BayesNet();
			}
			else if (algo.equals("LibSVM")) {
				classifier = new LibSVM();
			}
			else if (algo.equals("FT")) {
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
			else if (algo.equals("IB1")) {
				classifier = new IB1();
			}
			else {
				return;
			}
			if (params != null) {
				classifier.setOptions(weka.core.Utils.splitOptions(params));
			}
			Evaluation trainEval = new Evaluation(trainInstances);
			trainEval.crossValidateModel(classifier, trainInstances, 10, new Random(1));
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
			Instances testInstances = Modelling.loadData(metricNames, testValuesList, useWeights);
			classifier.buildClassifier(trainInstances);
			Evaluation testEval = new Evaluation(trainInstances);
			testEval.evaluateModel(classifier, testInstances);
			System.out.println("Complete.");
			
			int testDatasetSize = testInstances.numInstances();
			double[][] testConfusionMatrix = testEval.confusionMatrix();
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
		
			Model m = new Model(type, "Temp Model File Name", algo, params, bk, interBarData, metricNames, trainStart, trainEnd, testStart, testEnd, 
					sellMetric, sellMetricValue, stopMetric, stopMetricValue, numBars,
					trainDatasetSize, trainTrueNegatives, trainFalseNegatives, trainFalsePositives, trainTruePositives,
					trainTruePositiveRate, trainFalsePositiveRate, trainCorrectRate,
					trainKappa, trainMeanAbsoluteError, trainRootMeanSquaredError, trainRelativeAbsoluteError, trainRootRelativeSquaredError,
					trainROCArea,
					testDatasetSize, testTrueNegatives, testFalseNegatives, testFalsePositives, testTruePositives,
					testTruePositiveRate, testFalsePositiveRate, testCorrectRate,
					testKappa, testMeanAbsoluteError, testRootMeanSquaredError, testRelativeAbsoluteError, testRootRelativeSquaredError,
					testROCArea, false);
			
			System.out.print("Saving model to DB...");
			int modelID = QueryManager.insertModel(m);
			QueryManager.updateModelFileByID(modelID, algo + modelID + ".model"); // Have to set the modelFile name after the fact because we don't get the ID until the model record is inserted.
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
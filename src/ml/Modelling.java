package ml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.tuple.Pair;

import constants.Constants;
import data.Bar;
import data.BarKey;
import data.MetricKey;
import data.Model;
import dbio.QueryManager;
import tests.PValue;
import utils.Formatting;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.NeuralNetwork;
import weka.classifiers.functions.RBFClassifier;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.Stacking;
import weka.classifiers.misc.VFI;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
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
		symbolBuckets.addElement("ES C");
		symbolBuckets.addElement("ZN C");
		symbolBuckets.addElement("CL C");
		symbolBuckets.addElement("BTC_ETH");
		symbolBuckets.addElement("BTC_XMR");
		
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
	
	/**
	 * 
	 * @param periodStart
	 * @param periodEnd
	 * @param bk
	 * @param metricNames
	 * @param metricDiscreteValueHash
	 * @return
	 */
	public static ArrayList<ArrayList<Object>> createUnlabeledWekaArffData(BarKey bk, boolean useNormalizedNumericValues, HashMap<String, Object> thisInstance,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>(); 
		
			// Metric Buckets (or values)
			String metricPart = "";
			for (String metricName : metricNames) {
				if (!metricName.equals("close") && !metricName.equals("hour") && !metricName.equals("symbol") && !metricName.equals("class")) {
					// Regular metrics are looked up via the MetricDiscreteValueHash
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
					ArrayList<Float> bucketCutoffValues = metricDiscreteValueHash.get(mk);
					if (bucketCutoffValues != null) {
						if (thisInstance.get(metricName) == null) {
							System.out.println(metricName + " is missing data.");
							System.err.println(thisInstance.toString());
						}
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
				else {
					// Other metrics (close, hour, symbol) are already known
					if (metricName.equals("close")) {
						float close = (float)thisInstance.get("close");
						metricPart += close + ", ";
					}
					if (metricName.equals("hour")) {
						float hour = (int)thisInstance.get("hour");
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
			
			return valuesList;
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

				Instance instance = new DenseInstance(1, values);
				instances.add(instance);
			}

			return instances;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;	
		}
	}
	
	public static ArrayList<String> selectAttributes(ARFF arff, float targetGain, float minLoss, int numBars, 
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
				trainValuesList.addAll(arff.createWekaArffDataPeriodBounded(sellMetricValue, stopMetricValue, numBars, useNormalizedNumericValues, includeClose, includeHour, includeDraw, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
			}
			else if (strategy.equals("Unbounded")) {
				trainValuesList.addAll(arff.createWekaArffDataPeriodUnbounded(sellMetricValue, stopMetricValue, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
			}
			else if (strategy.equals("FixedInterval")) {
//				trainValuesList.addAll(ARFF.createWekaArffDataDirectionAfterXBars(numBars, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, Constants.METRICS, metricDiscreteValueHash, "train"));
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
	
	public double buildAndEvaluateModel(ARFF arff, String algo, String params, Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, 
			float targetGain, float minLoss, int numBars, ArrayList<BarKey> barKeys, 
			boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeDraw, boolean includeSymbol, boolean selectAttributes,
			int maxNumDesiredAttributes, double pipCutoff, 
			String strategy, ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash, String notes, Calendar baseDate,
			boolean saveModelInDB, boolean saveModelInstancesInDB, boolean saveModelFile) {
		try {
//			System.out.println("Starting " + algo);
			String sellMetric = Constants.OTHER_SELL_METRIC_PERCENT_UP;
			float sellMetricValue = targetGain;
			String stopMetric = Constants.STOP_METRIC_PERCENT_DOWN;
			float stopMetricValue = minLoss;
			
			int numClasses = 2;
			if (includeDraw) {
				numClasses = 3;
			}
		
//			System.out.print("Creating Train & Test datasets...");
			ArrayList<ArrayList<Object>> trainValuesList = new ArrayList<ArrayList<Object>>();
			ArrayList<ArrayList<Object>> testValuesList = new ArrayList<ArrayList<Object>>();
			ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> trainValuesListHash = new ArrayList<LinkedHashMap<Bar, ArrayList<Object>>>();
			ArrayList<LinkedHashMap<Bar, ArrayList<Object>>> testValuesListHash = new ArrayList<LinkedHashMap<Bar, ArrayList<Object>>>();
			ArrayList<Bar> trainBarList = new ArrayList<Bar>();
			ArrayList<Bar> testBarList = new ArrayList<Bar>();
	
//			if (strategy.equals("Bounded")) {
//				trainValuesList.addAll(ARFF.createWekaArffDataPeriodBounded(sellMetricValue, stopMetricValue, numBars, useNormalizedNumericValues, includeClose, includeHour, includeDraw, includeSymbol, metricNames, metricDiscreteValueHash, "train"));
//				testValuesList.addAll(ARFF.createWekaArffDataPeriodBounded(sellMetricValue, stopMetricValue, numBars, useNormalizedNumericValues, includeClose, includeHour, includeDraw, includeSymbol, metricNames, metricDiscreteValueHash, "test"));
//			}
//			else if (strategy.equals("Unbounded")) {
//				trainValuesList.addAll(ARFF.createWekaArffDataPeriodUnbounded(sellMetricValue, stopMetricValue, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "train"));
//				testValuesList.addAll(ARFF.createWekaArffDataPeriodUnbounded(sellMetricValue, stopMetricValue, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, metricNames, metricDiscreteValueHash, "test"));
//			}
//		}
			if (strategy.equals("FixedInterval")) {
				trainValuesListHash.addAll(arff.createWekaArffDataDirectionAfterXBars(numBars, pipCutoff, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, includeDraw, metricNames, metricDiscreteValueHash, "train"));
				testValuesListHash.addAll(arff.createWekaArffDataDirectionAfterXBars(numBars, pipCutoff, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, includeDraw, metricNames, metricDiscreteValueHash, "test"));
			}
			else if (strategy.equals("EnoughMovement")) {
				trainValuesListHash.addAll(arff.createWekaArffDataEnoughMovementAfterXBars(numBars, pipCutoff, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, includeDraw, metricNames, metricDiscreteValueHash, "train"));
				testValuesListHash.addAll(arff.createWekaArffDataEnoughMovementAfterXBars(numBars, pipCutoff, useNormalizedNumericValues, includeClose, includeHour, includeSymbol, includeDraw, metricNames, metricDiscreteValueHash, "test"));
			}
			
			// Copy just the ArrayList<ArrayList<Object>> out of the trainValuesListHash because Modelling.loadDat(...) needs it that way.
			for (LinkedHashMap<Bar, ArrayList<Object>> trainValueHash : trainValuesListHash) {
				ArrayList<ArrayList<Object>> valueListWrapper = new ArrayList<ArrayList<Object>>(trainValueHash.values());
				ArrayList<Object> valueList = valueListWrapper.get(0);
				trainValuesList.add(valueList);
				
				ArrayList<Bar> barWrapper = new ArrayList<Bar>(trainValueHash.keySet());
				trainBarList.add(barWrapper.get(0));
			}
			for (LinkedHashMap<Bar, ArrayList<Object>> testValueHash : testValuesListHash) {
				ArrayList<ArrayList<Object>> valueListWrapper = new ArrayList<ArrayList<Object>>(testValueHash.values());
				ArrayList<Object> valueList = valueListWrapper.get(0);
				testValuesList.add(valueList);
			
				ArrayList<Bar> barWrapper = new ArrayList<Bar>(testValueHash.keySet());
				testBarList.add(barWrapper.get(0));
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
//			System.out.println("Complete.");

			// Training & Cross Validation Data
//			System.out.print("Cross Validating...");
			Instances trainInstances = Modelling.loadData(metricNames, trainValuesList, useNormalizedNumericValues, numClasses);
		
			// Attribute Selection Train
			AttributeSelection attributeSelection = new AttributeSelection();
			ArrayList<String> selectedMetrics = new ArrayList<String>(metricNames);
			if (selectAttributes) {
//				System.out.println("Selecting attributes...");
				InfoGainAttributeEval attributeEval = new InfoGainAttributeEval();
//				CorrelationAttributeEval attributeEval = new CorrelationAttributeEval();
//				GainRatioAttributeEval attributeEval = new GainRatioAttributeEval();
				Ranker ranker = new Ranker();
				ranker.setNumToSelect(maxNumDesiredAttributes);
				attributeSelection.setEvaluator(attributeEval);
				attributeSelection.setSearch(ranker);
				attributeSelection.setInputFormat(trainInstances);
				trainInstances = Filter.useFilter(trainInstances, attributeSelection);
				
				// Get the names of the selected metrics
				ArrayList<Pair<Double, String>> metricScores = new ArrayList<Pair<Double, String>>();
				
//				infoGain.buildEvaluator(trainInstances);
//				LinkedHashMap<String, Double> attributeScoreMap = new LinkedHashMap<String, Double>();
//				for (int a = 0; a < trainInstances.numAttributes(); a++) {
//					double infoGainScore = infoGain.evaluateAttribute(a);
//					System.out.println(trainInstances.attribute(a).name() + ": " + Formatting.df5.format(infoGainScore));
//					attributeScoreMap.put(trainInstances.attribute(a).name(), infoGainScore);
//				}
//				attributeScoreMap = (LinkedHashMap<String, Double>)GeneralUtils.sortByValueDesc(attributeScoreMap);
//				for (Entry<String, Double> entry :attributeScoreMap.entrySet()) {
//					System.out.println(entry.getKey() + ": " + Formatting.df5.format(entry.getValue()));
//				}

				for (int a = 0; a < trainInstances.numAttributes(); a++) {
					Attribute attribute = trainInstances.attribute(a);
					String name = attribute.name();
					double infoGainScore = attributeEval.evaluateAttribute(a);

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

//			System.out.println(trainInstances.numInstances() + " instances of train data");
			
			Classifier classifier = null;
			if (algo.equals("NaiveBayes")) {
				classifier = new NaiveBayes();
				if (params != null) {
					((NaiveBayes)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("NaiveBayesUpdateable")) {
				classifier = new NaiveBayesUpdateable();
				if (params != null) {
					((NaiveBayesUpdateable)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("BayesNet")) {
				classifier = new BayesNet();
				if (params != null) {
					((BayesNet)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("RandomForest")) {
				classifier = new RandomForest();
				if (params != null) {
					((RandomForest)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("RandomTree")) {
				classifier = new RandomTree();
				if (params != null) {
					((RandomTree)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("REPTree")) {
				classifier = new REPTree();
				if (params != null) {
					((REPTree)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("J48")) {
				classifier = new J48();
				if (params != null) {
					((J48)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("LMT")) {
				classifier = new LMT();
				if (params != null) {
					((LMT)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("MultilayerPerceptron")) {
				classifier = new MultilayerPerceptron();
				if (params != null) {
					((MultilayerPerceptron)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("NeuralNetwork")) {
				classifier = new NeuralNetwork();
				if (params != null) {
					((NeuralNetwork)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("RBFNetwork")) {
				classifier = new RBFNetwork();
				if (params != null) {
					((RBFNetwork)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("RBFClassifier")) {
				classifier = new RBFClassifier();
				if (params != null) {
					((RBFClassifier)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("SimpleLogistic")) {
				classifier = new SimpleLogistic();
				if (params != null) {
					((SimpleLogistic)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("LogitBoost")) {
				classifier = new LogitBoost();
				if (params != null) {
					((LogitBoost)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("LibSVM")) {
				classifier = new LibSVM();
				if (params != null) {
					((LibSVM)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("VFI")) {
				classifier = new VFI();
				if (params != null) {
					((VFI)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("Bagging")) { // Ensemble with separate samples that are combined
				classifier = new Bagging();
				if (params != null) {
					((Bagging)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("Stacking")) { // Blending ensemble method using multiple algos.  Logistic Regression as meta classifier?
				classifier = new Stacking();
				if (params != null) {
					((Stacking)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("AdaBoostM1")) { // Boosting ensemble starts with base classifier and other ones are created behind it to focus on missclassified instances
				classifier = new AdaBoostM1();
				if (params != null) {
					((AdaBoostM1)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("AttributeSelectedClassifier")) {
				classifier = new AttributeSelectedClassifier();
				if (params != null) {
					
					//params = "-E \"weka.attributeSelection.PrincipalComponents -R 0.80 -A 10\" -S \"weka.attributeSelection.Ranker -T -1.7976931348623157E308 -N 10\" -W weka.classifiers.meta.LogitBoost -- -P 100 -L -1.7976931348623157E308 -H 0.1 -Z 3.0 -O 6 -E 6 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
					((AttributeSelectedClassifier)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else if (algo.equals("FilteredClassifier")) {
				classifier = new FilteredClassifier();
				if (params != null) {
					((FilteredClassifier)classifier).setOptions(weka.core.Utils.splitOptions(params));
				}
			}
			else {
				return 0;
			}
			
			Evaluation trainEval = new Evaluation(trainInstances);
			trainEval.crossValidateModel(classifier, trainInstances, 2 /*10*/, new Random(1)); // No need to do this if evaluating performance on test set
//			System.out.println("Complete.");
			
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
			ArrayList<Prediction> trainPredictions = trainEval.predictions();
			Instances trainCurveInstances = trainCurve.getCurve(trainPredictions, 0);
			double trainROCArea = trainCurve.getROCArea(trainCurveInstances);

			ArrayList<Double> trainPredictionScores = new ArrayList<Double>();
			ArrayList<Boolean> trainPredictionResults = new ArrayList<Boolean>();
			ArrayList<Calendar> trainPredictionTimes = new ArrayList<Calendar>();
			ArrayList<Integer> trainPredictionValues = new ArrayList<Integer>();
			ArrayList<Integer> trainActualValues = new ArrayList<Integer>();
			ArrayList<String> trainPredictionSymbols = new ArrayList<String>();
			ArrayList<String> trainPredictionDurations = new ArrayList<String>();
			ArrayList<Double> trainPredictionChangeAtTargets = new ArrayList<Double>();
			
			for (int a = 0; a < trainPredictions.size(); a++) {
				Bar bar = trainBarList.get(a);
				NominalPrediction np = (NominalPrediction)trainPredictions.get(a);
//				if (np.distribution().length == 2) {
					
					boolean correct = false;
					if (np.actual() == np.predicted()) {
						correct = true;
					}
					
					// When there's 3 classes (Up, Down, Draw), adjust the score just to reflect the ratio of up to down.
					if (np.distribution().length == 3) {
						double up = np.distribution()[0];
						double down = np.distribution()[1];
						double sum = up + down;
						
						double upScore = up / sum;
						double downScore = down / sum;
						
						trainPredictionScores.add(a, upScore);
					}
					else {
						trainPredictionScores.add(a, np.distribution()[1]);
					}
					
					trainPredictionResults.add(a, correct);
					trainPredictionTimes.add(bar.periodStart);
					trainPredictionValues.add((int)np.predicted());
					trainPredictionSymbols.add(bar.symbol);
					trainPredictionDurations.add(bar.duration.toString());
					trainActualValues.add((int)np.actual());
					trainPredictionChangeAtTargets.add((double)bar.changeAtTarget);
//				}
//				else if (np.distribution().length == 3) {
//					System.out.println(np.actual() + ", " + np.predicted() + ", " + np.distribution()[0] + ", " + np.distribution()[1] + ", " + np.distribution()[2]);
//				}
			}
			
			// Test Data
//			System.out.print("Evaluating Test Data...");
			Instances testInstances = Modelling.loadData(metricNames, testValuesList, useNormalizedNumericValues, numClasses);
//			System.out.println(testInstances.numInstances() + " instances of test data");
			
			if (selectAttributes) {
				testInstances = Filter.useFilter(testInstances, attributeSelection);
			}
			
			if (useNormalizedNumericValues) {
				testInstances = Filter.useFilter(testInstances, normalize);
//				testInstances = Filter.useFilter(testInstances, pc);
			}
			
			classifier.buildClassifier(trainInstances);
			Evaluation testEval = new Evaluation(trainInstances);
			testEval.evaluateModel(classifier, testInstances);
			
			// Break the predictions up into buckets of size .1 each (.5 - 1.0) to get the percent correct per bucket.  We want to see higher accuracy in the more confident buckets.
			ArrayList<Prediction> testPredictions = testEval.predictions();
		
			double[] correctCounts = new double[5];
			double[] incorrectCounts = new double[5];
			double[] testBucketPercentCorrect = new double[5];
			double[] testBucketDistribution = new double[5]; // What percent of the predictions fall in each bucket
			double[] testBucketPValues = new double[5];
		
			ArrayList<Double> testPredictionScores = new ArrayList<Double>();
			ArrayList<Boolean> testPredictionResults = new ArrayList<Boolean>();
			ArrayList<Calendar> testPredictionTimes = new ArrayList<Calendar>();
			ArrayList<Integer> testPredictionValues = new ArrayList<Integer>();
			ArrayList<Integer> testActualValues = new ArrayList<Integer>();
			ArrayList<String> testPredictionSymbols = new ArrayList<String>();
			ArrayList<String> testPredictionDurations = new ArrayList<String>();
			ArrayList<Double> testPredictionChangeAtTargets = new ArrayList<Double>();
			
			for (int a = 0; a < testPredictions.size(); a++) {
				Bar bar = testBarList.get(a);
				NominalPrediction np = (NominalPrediction)testPredictions.get(a);
//				if (np.distribution().length == 2) {
					
					boolean correct = false;
					if (np.actual() == np.predicted()) {
						correct = true;
					}

					double maxDistribution = np.distribution()[0];
					if (np.distribution()[1] > maxDistribution) {
						maxDistribution = np.distribution()[1];
					}
					if (np.distribution().length == 3) {
						if (np.distribution()[2] > maxDistribution) {
							maxDistribution = np.distribution()[2];
						}
					}
				
					int bucket = -1; // .5 - .6 = [0], .6 - .7 = [1], .7 - .8 = [2], .8 - .9 = [3], .9 - 1.0 = [4]
					if (maxDistribution >= .333 && maxDistribution < .5) {
						bucket = 0;
					}
					else if (maxDistribution >= .5 && maxDistribution < .666) {
						bucket = 1;
					}
					else if (maxDistribution >= .666 && maxDistribution < .833) {
						bucket = 2;
					}
					else if (maxDistribution >= .833) {
						bucket = 3;
					}
					
					if (correct) {
						correctCounts[bucket]++;
					}
					else {
						incorrectCounts[bucket]++;
					}
					
					// When there's 3 classes (Up, Down, Draw), adjust the score just to reflect the ratio of up to down.
					if (np.distribution().length == 3) {
						double up = np.distribution()[0];
						double down = np.distribution()[1];
						double sum = up + down;
						
						double upScore = up / sum;
						double downScore = down / sum;
						
						testPredictionScores.add(a, upScore);
					}
					else {
						testPredictionScores.add(a, np.distribution()[1]);
					}
					
					testPredictionResults.add(a, correct);
					testPredictionTimes.add(bar.periodStart);
					testPredictionValues.add((int)np.predicted());
					testPredictionSymbols.add(bar.symbol);
					testPredictionDurations.add(bar.duration.toString());
					testActualValues.add((int)np.actual());
					testPredictionChangeAtTargets.add((double)bar.changeAtTarget);
//				}
//				else if (np.distribution().length == 3) {
//					System.out.println(np.actual() + ", " + np.predicted() + ", " + np.distribution()[0] + ", " + np.distribution()[1] + ", " + np.distribution()[2]);
//				}
			}

			for (int a = 0; a < 5; a++) {
				if (correctCounts[a] + incorrectCounts[a] == 0) {
					testBucketPercentCorrect[a] = 0;
				}
				else {
					testBucketPercentCorrect[a] = Double.parseDouble(Formatting.df5.format(correctCounts[a] / (correctCounts[a] + incorrectCounts[a])));
				}
				if (testPredictions.size() == 0) {
					testBucketDistribution[a] = 0;
				}
				else {
					testBucketDistribution[a] = Double.parseDouble(Formatting.df5.format((correctCounts[a] + incorrectCounts[a]) / testPredictions.size()));
				}
				
				testBucketPValues[a] = PValue.calculate((int)correctCounts[a], (int)(correctCounts[a] + incorrectCounts[a]), sellMetricValue / (double)(sellMetricValue + stopMetricValue));
			}
			
//			System.out.println("Complete.");
			
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
					testROCArea, testBucketPercentCorrect, testBucketDistribution, testBucketPValues, notes, false, true, true, true, baseDate);
			
			int modelID = 0;
			String fileName = "";
			String filePath = "";
			if (saveModelInDB) {
//				System.out.print("Saving Model to DB...");
				modelID = QueryManager.insertModel(m);
				QueryManager.updateModelFileByID(modelID, algo + modelID + ".model"); // Have to set the modelFile name after the fact because we don't get the ID until the model record is inserted.
//				System.out.println("Complete.");
			}
			
			if (saveModelInDB && saveModelInstancesInDB) {
//				System.out.println("Saving ModelInstances to DB...");
				QueryManager.insertModelInstances("Train", modelID, trainPredictionScores, trainPredictionResults, trainPredictionValues, trainActualValues, trainPredictionTimes, trainPredictionSymbols, trainPredictionDurations, trainPredictionChangeAtTargets);
				QueryManager.insertModelInstances("Test", modelID, testPredictionScores, testPredictionResults, testPredictionValues, testActualValues, testPredictionTimes, testPredictionSymbols, testPredictionDurations, testPredictionChangeAtTargets);
//				System.out.println("Complete.");
			}
			
			if (saveModelInDB && saveModelFile) {
//				System.out.print("Saving model file...");
				fileName = algo + modelID + ".model";
				filePath = "weka/models/" + fileName;
				weka.core.SerializationHelper.write(filePath, classifier);
//				System.out.print("Regular file complete...");
				
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
//				System.out.println("Zip file complete.");
			}

			return trainCorrectRate;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
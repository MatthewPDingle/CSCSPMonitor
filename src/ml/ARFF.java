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

public class ARFF {

	// Outer ArrayList = BarKey, Inner ArrayList = days newest to oldest, HashMap = bar & metric key/values
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawTrainingSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawTestSet = new ArrayList<ArrayList<HashMap<String, Object>>>();
	
	private static boolean saveARFF = false;
	
	private static int dateSet = 0;
	private static int[] barMods = new int[10];
	
	public static void main(String[] args) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");
			DecimalFormat df2 = new DecimalFormat("#.##");

			// Train & Test Dates
			String[] sTrainStarts = new String[10];
			sTrainStarts[0] = "05/01/2015 00:00:00";
			sTrainStarts[1] = "01/01/2015 00:00:00";
			sTrainStarts[2] = "09/01/2014 00:00:00";
			sTrainStarts[3] = "05/01/2014 00:00:00";
			sTrainStarts[4] = "01/01/2014 00:00:00";
			sTrainStarts[5] = "09/01/2013 00:00:00";
			sTrainStarts[6] = "05/01/2013 00:00:00";
			sTrainStarts[7] = "01/01/2013 00:00:00";
			sTrainStarts[8] = "01/01/2012 00:00:00";
			sTrainStarts[9] = "01/01/2011 00:00:00";
			
			String[] sTrainEnds = new String[10];
			sTrainEnds[0] = "01/01/2016 16:00:00";
			sTrainEnds[1] = "11/01/2015 16:00:00";
			sTrainEnds[2] = "09/01/2015 16:00:00";
			sTrainEnds[3] = "07/01/2015 16:00:00";
			sTrainEnds[4] = "05/01/2015 16:00:00";
			sTrainEnds[5] = "03/01/2015 16:00:00";
			sTrainEnds[6] = "01/01/2015 16:00:00";
			sTrainEnds[7] = "11/01/2014 16:00:00";
			sTrainEnds[8] = "12/01/2014 16:00:00";
			sTrainEnds[9] = "12/01/2014 16:00:00";
			
			String[] sTestStarts = new String[10];
			sTestStarts[0] = "01/15/2016 00:00:00";
			sTestStarts[1] = "11/15/2015 00:00:00";
			sTestStarts[2] = "09/15/2015 00:00:00";
			sTestStarts[3] = "07/15/2015 00:00:00";
			sTestStarts[4] = "05/15/2015 00:00:00";
			sTestStarts[5] = "03/15/2015 00:00:00";
			sTestStarts[6] = "01/15/2015 00:00:00";
			sTestStarts[7] = "11/15/2014 00:00:00";
			sTestStarts[8] = "01/01/2015 00:00:00";
			sTestStarts[9] = "01/01/2015 00:00:00";
			
			String[] sTestEnds = new String[10];
			sTestEnds[0] = "04/02/2016 16:00:00";
			sTestEnds[1] = "03/26/2016 16:00:00";
			sTestEnds[2] = "03/19/2016 16:00:00";
			sTestEnds[3] = "03/12/2016 16:00:00";
			sTestEnds[4] = "03/05/2016 16:00:00";
			sTestEnds[5] = "02/27/2016 16:00:00";
			sTestEnds[6] = "02/20/2016 16:00:00";
			sTestEnds[7] = "02/13/2016 16:00:00";
			sTestEnds[8] = "04/02/2016 16:00:00";
			sTestEnds[9] = "04/02/2016 16:00:00";
		
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
			
			// Bar Modulus for selecting subsets of Train & Test data
			barMods[0] = 65;
			barMods[1] = 85;
			barMods[2] = 105;
			barMods[3] = 125;
			barMods[4] = 145;
			barMods[5] = 165;
			barMods[6] = 185;
			barMods[7] = 205;
			barMods[8] = 305;
			barMods[9] = 405;
			
			// Hyper-parameter options
//			String optionsRandomForest = "-I 160 -K 24 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsRandomForest = "-I 128 -K 5 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 8192.0 -C 10 -E 0.001 -P 0.1 -B -seed 1"; // "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 4096.0 -C 1000 -E 0.001 -P 0.1 -B -seed 1";
			String optionsMultilayerPerceptron = "-L 0.3 -M 0.2 -N 300 -V 0 -S 0 -E 20 -H 3 -B";
			String optionsStacking = "weka.classifiers.meta.Stacking -X 100 -M \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1\" -S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.RandomForest -I 30 -K 0 -S 1\" -B \"weka.classifiers.bayes.RandomForest \"";
			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -I 128 -K 5 -S 1";
			String optionsMetaCost = "weka.classifiers.meta.MetaCost -cost-matrix \"[0.0 30.0 1.0; 10.0 0.0 1.0; 4.0 16.0 0.0]\" -I 2 -P 100 -S 1 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsBagging = "weka.classifiers.meta.Bagging -P 100 -S 1 -I 3 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsJ48 = "weka.classifiers.trees.J48 -C 0.25 -M 2";
			String optionsRBFNetwork = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 0.1";
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
			// STEP 4: Set gain/lose % ratio
			// STEP 5: Set the number of attributes to select
			dateSet = 9;
			int gainR = 2;
			int lossR = 1;
			int numAttributes = 30;
			
			// Data Caching
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTime(sdf.parse(sTrainStarts[dateSet]));
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTime(sdf.parse(sTrainEnds[dateSet]));
			
			Calendar testStart = Calendar.getInstance();
			testStart.setTime(sdf.parse(sTestStarts[dateSet]));
			Calendar testEnd = Calendar.getInstance();
			testEnd.setTime(sdf.parse(sTestEnds[dateSet]));
			
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
			for (int a = 1; a <= 1; a++) {
				String classifierName = algos[a];
				String classifierOptions = algoOptions[a];
				
				String notes = "AS-" + numAttributes + " 5M " + gainR + ":" + lossR + " DateSet[" + dateSet + "] " + classifierName + " x" + barMods[dateSet] + " " + sdf2.format(Calendar.getInstance().getTime());
			
				// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
				/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
				for (float b = 0.1f; b <= 1.51; b += .1f) {
					Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, b, b * ((float)lossR / (float)gainR), 600, barKeys, false, false, true, false, true, true, numAttributes, "Unbounded", metricNames, metricDiscreteValueHash, notes);
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
					if (minuteOfDay % barMods[dateSet] == 0) {
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
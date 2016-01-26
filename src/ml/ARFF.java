package ml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

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
	
	public static void main(String[] args) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			DecimalFormat df2 = new DecimalFormat("#.##");
			
			String sTrainStart = "05/25/2013 00:00:00"; // 
			String sTrainEnd = "05/05/2015 16:00:00"; // 5/5/2015
			Calendar trainStart = Calendar.getInstance();
			trainStart.setTime(sdf.parse(sTrainStart));
			Calendar trainEnd = Calendar.getInstance();
			trainEnd.setTime(sdf.parse(sTrainEnd));
			
			String sTestStart = "06/01/2015 16:15:00"; //
			String sTestEnd = "01/22/2016 16:00:00"; // 1/22/2016
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
				rawTrainingSet.add(QueryManager.getTrainingSet(bk, trainStart, trainEnd, metricNames, 60));
			}
			System.out.println("Complete.");
			System.out.println("Loding test data...");
			for (BarKey bk : barKeys) {
				rawTestSet.add(QueryManager.getTrainingSet(bk, testStart, testEnd, metricNames, 60));
			}
			System.out.println("Complete.");
			
//			System.out.println("Selecting Attributes...");
//			float gainAndLoss = .1f;
//			int numBars = 100;
//			ArrayList<String> selectedMetrics = Modelling.selectAttributes(gainAndLoss, gainAndLoss, numBars, false, false, true, false, true, 30, .0005f, "Unbounded", metricDiscreteValueHash);
//			System.out.println("Selecting Attributes Complete.");
			
//			String optionsRandomForest = "-I 160 -K 24 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsRandomForest = "-I 128 -K 8 -S 1"; // I = # Trees, K = # Features, S = Seed	
			String optionsLibSVM = "-S 0 -K 2 -D 3 -G 0.01 -R 0.0 -N 0.5 -M 4096.0 -C 1000 -E 0.001 -P 0.1 -seed 1";
			String optionsStacking = "weka.classifiers.meta.Stacking -X 10 -M \"weka.classifiers.functions.Logistic -R 1.0E-8 -M -1\" -S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.RandomForest -I 30 -K 0 -S 1\" -B \"weka.classifiers.bayes.RandomForest \"";
//			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsAdaBoostM1 = "weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsMetaCost = "weka.classifiers.meta.MetaCost -cost-matrix \"[0.0 30.0 1.0; 10.0 0.0 1.0; 4.0 16.0 0.0]\" -I 2 -P 100 -S 1 -W weka.classifiers.bayes.NaiveBayes --";
			String optionsBagging = "weka.classifiers.meta.Bagging -P 100 -S 1 -I 3 -W weka.classifiers.trees.RandomForest -- -I 160 -K 24 -S 1";
			String optionsJ48 = "weka.classifiers.trees.J48 -C 0.5 -M 1";
			
			// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
			/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
			
//			for (float b = .1f; b <= 1.01; b += .1f) {
//				for (int d = 10; d <= 120; d+=10) {
//					b = Float.parseFloat(df2.format(b));
//					Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, b, b, d, barKeys, false, false, true, true, true, true, 30, "Bounded", metricNames, metricDiscreteValueHash);	
//				}	
//			}
//			for (float b = 0.1f; b <= 1.01; b += .1f) {
//				for (int d = 50; d <= 80; d+=10) {
//					b = Float.parseFloat(df2.format(b));
//					Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, trainStart, trainEnd, testStart, testEnd, b, b, d, barKeys, false, false, true, true, true, "Bounded", metricNames, metricDiscreteValueHash);	
//				}	
//			}
//			for (float b = 0.1f; b <= 1.01; b += .1f) {
//				for (int d = 90; d <= 120; d+=10) {
//					b = Float.parseFloat(df2.format(b));
//					Modelling.buildAndEvaluateModel("J48", 		optionsJ48, trainStart, trainEnd, testStart, testEnd, b, b, d, barKeys, false, false, true, true, true, "Bounded", metricNames, metricDiscreteValueHash);	
//				}	
//			}
			
			for (int numFeatures = 10; numFeatures <= 50; numFeatures+=5) { 
				for (float b = 0.1f; b <= 1.01; b += .1f) {
					Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, b, b, 100, barKeys, false, false, true, false, true, true, numFeatures, "Unbounded", metricNames, metricDiscreteValueHash);
				}
			}
	
//			Modelling.buildAndEvaluateModel("NaiveBayes", 		null, trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 100, barKeys, false, false, true, false, true, true, 30, "Unbounded", metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("J48", 		optionsJ48, trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 100, barKeys, false, false, true, false, true, "Unbounded", metricNames, metricDiscreteValueHash);
//			Modelling.buildAndEvaluateModel("RandomForest", 		optionsRandomForest, trainStart, trainEnd, testStart, testEnd, 0.1f, 0.1f, 100, barKeys, false, false, true, false, true, "Unbounded", metricNames, metricDiscreteValueHash);
			
		
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
	
			int winCount = 0;
			int lossCount = 0;
			int drawCount = 0;
			long startMS = Calendar.getInstance().getTimeInMillis();
			
			// Both are ordered newest to oldest
			for (ArrayList<HashMap<String, Object>> rawSet : trainOrTest.equals("train") ? rawTrainingSet : rawTestSet) {
				
				ArrayList<Float> futureHighs = new ArrayList<Float>();
				ArrayList<Float> futureLows = new ArrayList<Float>();
				
				for (HashMap<String, Object> record : rawSet) {
					float close = (float)record.get("close");
					float high = (float)record.get("high");
					float low = (float)record.get("low");
					
//					System.out.println(close);
					
					boolean gainOK = false;
					int targetGainIndex = findTargetGainIndex(futureHighs, close, targetGain);
					
					boolean lossOK = false;
					int targetLossIndex = findTargetLossIndex(futureLows, close, targetGain);
		 
					boolean gainStopOK = false;
					if (targetGainIndex != -1) {
						gainOK = true;
						float minPrice = findMin(futureLows, targetGainIndex); // This checks up through the bar where the successful exit would be made.
						if (minPrice > close * (100f - minLoss) / 100f) {
							gainStopOK = true;
						}
					}
					
					boolean lossStopOK = false;
					if (targetLossIndex != -1) {
						lossOK = true;
						float maxPrice = findMax(futureHighs, targetLossIndex);
						if (maxPrice < close * (100f + minLoss) / 100f) {
							lossStopOK = true;
						}
					}
					
					// Class
					String classPart = "";
					if (gainStopOK && gainOK) {
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
					
//						System.out.println(classPart + ", " + open + ", " + close + ", " + high + ", " + low + ", " + startTS.toString());
					
						if (!metricPart.equals("")) {
							String recordLine = referencePart + metricPart + classPart;
							ArrayList<Object> valueList = new ArrayList<Object>();
							String[] values = recordLine.split(",");
							valueList.addAll(Arrays.asList(values));
							valuesList.add(valueList);
						}
					}
					
					futureHighs.add(high); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
					futureLows.add(low); // Adding to the end, so this is newest to oldest.  Adding to the front becomes too expensive.
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
			boolean useWeights, boolean useNormalizedNumericValues, boolean includeClose, boolean includeHour, boolean includeSymbol,
			ArrayList<String> metricNames, HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		try {
			// This is newest to oldest ordered
			ArrayList<HashMap<String, Object>> rawTrainingSet = QueryManager.getTrainingSet(bk, periodStart, periodEnd, metricNames, null);
			
			ArrayList<ArrayList<Object>> valuesList = new ArrayList<ArrayList<Object>>();
			for (HashMap<String, Object> record : rawTrainingSet) {
				float close = (float)record.get("close");
				float hour = (int)record.get("hour");
				
//				// Non-Metric Optional Features
//				String referencePart = "";
//				if (includeClose) {
//					referencePart = close + ", ";
//				}
//				if (includeHour) {
//					referencePart += hour + ", ";
//				}
//				if (includeSymbol) {
//					referencePart += bk.symbol + ", ";
//				}
				
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
//				// Class
//				String classPart = "?";
				
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
		
//		for (int a = 0; a <= targetIndex; a++) {
//			if (list.get(a) < min) {
//				min = list.get(a);
//			}
//		}
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
		
//		for (int a = 0; a <= targetIndex; a++) {
//			if (list.get(a) > max) {
//				max = list.get(a);
//			}
//		}
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
		
//		for (int a = 0; a < listSize; a++) {
//			if (nextXPrices.get(a) >= targetClose) {
//				return a;
//			}
//		}
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
		
//		for (int a = 0; a < listSize; a++) {
//			if (nextXPrices.get(a) <= targetClose) {
//				return a;
//			}
//		}
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
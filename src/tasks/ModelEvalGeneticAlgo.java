package tasks;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import utils.Formatting;
import utils.GeneralUtils;

public class ModelEvalGeneticAlgo {

	private static final long MS_WEEK = 604800000l;
	private static final long MS_90DAYS = 7776000000l;
	private static final long MS_360DAYS = 31104000000l;
	
	private static final int NUM_1ST_EPOCHS = 200;	// Up until this point metrics are chosen at random.
	private static final int NUM_2ND_EPOCHS = 1000;	// After the 1st epoch number and before the 2nd epoch number, metrics are chosen based on metricga, after this 2nd epoch number, they're chosen based on metricga2

	private static final int NUM_THREADS = 1;
	private static final int NUM_METRICS = 20;
	private static final String NOTES = "EUR.USD 2H Test 21";
	private static final BarKey BK = new BarKey("EUR.USD", BAR_SIZE.BAR_2H);
	
	private Object lock = new Object();
	
	private static int epoch = 531;
	private static Calendar cal = Calendar.getInstance();
	private static double averageMetricGARunScore = 0;
	private static Calendar rawStartC = Calendar.getInstance();
	private static Calendar rawEndC = Calendar.getInstance();
	private static ArrayList<ArrayList<HashMap<String, Object>>> rawCompleteSet;
	
	public static void main(String[] args) {
		ModelEvalGeneticAlgo mega = new ModelEvalGeneticAlgo();
		mega.runMEGA();
	}
	
	public void runMEGA() {
		try {
			// Load a bunch of shit in memory so I don't have to keep loading it.
			String rawStart = "01/01/2009 00:00:00";
			String rawEnd = "07/31/2016 00:00:00";
			rawStartC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(rawStart).getTime());
			rawEndC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(rawEnd).getTime());
			ARFF arff = new ARFF();
			rawCompleteSet = arff.loadRawCompleteSet(rawStartC, rawEndC);
			
			// Start threads
			MEGAThread[] megaThreads = new MEGAThread[NUM_THREADS];
			for (int a = 0; a < NUM_THREADS; a++) {
				megaThreads[a] = new MEGAThread();
				megaThreads[a].setName("Thread " + a);
				megaThreads[a].start();
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized int incrementEpoch() {
		double ms = (Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis()) / 1000d;
		System.out.println(ms + "\t\t" + (ms / epoch));
		return epoch++;
	}
	
	public class MEGAThread extends Thread {
		@Override
		public void run() {
			try {
				super.run();
				
				while (true) {
					
					int thisEpoch = incrementEpoch();
					
					// Specific test dates
					String[] testDateStrings = new String[10];
					testDateStrings[0] = "01/14/2013 00:00:00";
					testDateStrings[1] = "06/17/2013 00:00:00";
					testDateStrings[2] = "03/31/2014 00:00:00";
					testDateStrings[3] = "10/20/2014 00:00:00";
					testDateStrings[4] = "05/25/2015 00:00:00";
					testDateStrings[5] = "01/11/2016 00:00:00";
					testDateStrings[7] = "05/16/2016 00:00:00";
					testDateStrings[8] = "08/29/2016 00:00:00";
					testDateStrings[9] = "01/16/2017 00:00:00";
					
					// Select Metrics
					System.out.println("Running epoch " + thisEpoch++);
						
					ArrayList<HashMap<String, Object>> metricGAList = new ArrayList<HashMap<String, Object>>();
					if (thisEpoch < NUM_1ST_EPOCHS) {
						metricGAList = QueryManager.selectMetricGA(NOTES, NUM_METRICS * 2);
					}
//					else if (thisEpoch < NUM_2ND_EPOCHS) {
					else {
						metricGAList = QueryManager.selectMetricGA(NOTES, 200);
					}
//					}
//					else {
//						metricGAList = QueryManager.selectMetricGA2(NOTES, 4950);
//					}
					ArrayList<String> metricNames = new ArrayList<String>(selectMetrics(NUM_METRICS, metricGAList, thisEpoch));
					
					if (thisEpoch == NUM_1ST_EPOCHS) {
						QueryManager.normalizeMetricGA(NOTES);
					}
//					if (thisEpoch == NUM_2ND_EPOCHS) {
//						QueryManager.normalizeMetricGA2(NOTES);
//					}
					if (thisEpoch > NUM_1ST_EPOCHS) {
						averageMetricGARunScore = QueryManager.selectMetricGARunScore(NOTES);
					}
	
					// Build Models
					double totalTestCorrectRate = 0;
					for (String testDateString : testDateStrings) {
						Calendar testDateC = Calendar.getInstance();
						testDateC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(testDateString).getTime());
						double testCorrectRate = buildModel(testDateC, metricNames);
						totalTestCorrectRate += testCorrectRate;
					}
					
					// Update Results
					double averageTestCorrectRate = totalTestCorrectRate / (double)testDateStrings.length;
					double score = (averageTestCorrectRate - 50) / 100d;
					double increment = (score - averageMetricGARunScore) * 50;
					HashSet<String> metricNamesHS = new HashSet<String>(metricNames);
					metricNamesHS.remove("class");
					HashSet<HashSet<String>> metricPowerset = GeneralUtils.powerset(metricNamesHS, 2);
					QueryManager.updateMetricGA(metricNames, increment, NOTES);
					QueryManager.updateMetricGA2(metricPowerset, increment, NOTES);
					QueryManager.insertMetricGARun(thisEpoch - 1, metricNames, score, NOTES);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		public double buildModel(Calendar baseDate, ArrayList<String> metricNames) {
			try {
				// Load date ranges for Train & Test sets
				long baseTime = baseDate.getTimeInMillis();
				
				Calendar trainStart = Calendar.getInstance();
				Calendar trainEnd = Calendar.getInstance();
				Calendar testStart = Calendar.getInstance();
				Calendar testEnd = Calendar.getInstance();
			
				testEnd.setTimeInMillis(baseTime - (0 * MS_WEEK));
				testStart.setTimeInMillis((baseTime - MS_90DAYS) - (10 * 2 * MS_WEEK)); // 2
				trainEnd.setTimeInMillis(testStart.getTimeInMillis() - MS_WEEK);
				trainStart.setTimeInMillis((baseTime - MS_360DAYS) - (10 * 10 * MS_WEEK)); // 15

				// Setup
				ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
				barKeys.add(BK);

				// Load Metric Discrete Values
				HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDiscreteValueHash("Percentiles Set 10");
				
				// STEP 1: Set gain/lose % ratio
				// STEP 2: Set the number of attributes to select
				int gainR = 1;
				int lossR = 1;
				double pipCutoff = .0000;

				ARFF arff = new ARFF();
				arff.setRawCompleteSet(rawCompleteSet);
				arff.loadTrainingSet(trainStart, trainEnd);
				arff.loadTestSet(testStart, testEnd);
				
				String classifierName = "RBFNetwork";
				String classifierOptions = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 1.0";
				
				String notes = "AS-" + NUM_METRICS + " " + BK.toString() + " " + classifierName + " " + metricNames.hashCode() + " Metric Hash " + Formatting.sdfMMDDYYYY.format(Calendar.getInstance().getTime());
				
				Modelling modelling = new Modelling();
				
				// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
				/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
				double testCorrectRate = modelling.buildAndEvaluateModel(arff, classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, 1, 1, 1, barKeys, 
						false, false, false, false, false, false, NUM_METRICS, pipCutoff, "FixedInterval", metricNames, metricDiscreteValueHash, notes, baseDate, false, false, false);
				
				return testCorrectRate;
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		public HashSet<String> selectMetrics(int num, ArrayList<HashMap<String, Object>> metricGAList, int epoch) {
			HashSet<String> metrics = new HashSet<String>();
			try {
				double totalScore = 0;
				if (epoch > NUM_1ST_EPOCHS) {
					for (HashMap<String, Object> metricGA : metricGAList) {
						double score = (double)metricGA.get("score");
						totalScore += score;
					}
				}
				else {
					totalScore = metricGAList.size();
				}
				
				while (metrics.size() < num) {
					Collections.shuffle(metricGAList, new Random(System.nanoTime()));
					
					for (HashMap<String, Object> metricGA : metricGAList) {
						double score = (double)metricGA.get("score");
						Object oName = metricGA.get("name");
						if (oName instanceof String) {
							String metric = metricGA.get("name").toString();
							
							double fraction = score / totalScore;
							if (Math.random() < fraction) {
								if (!metrics.contains(metric)) {
									metrics.add(metric);
									break;
								}
							}
						}
						else if (oName instanceof Array) {
							Array names = (Array)oName;
							String[] values = (String[])names.getArray();
							ArrayList<String> metricList = new ArrayList<String>(Arrays.asList((String[])values));
							
							double fraction = score / totalScore;
							if (Math.random() < fraction) {
								for (String m : metricList) {
									if (!metrics.contains(m)) {
										metrics.add(m);
										if (metrics.size() >= num) {
											return metrics;
										}
									}
								}
							}
						}
						
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return metrics;
		}
	}
}
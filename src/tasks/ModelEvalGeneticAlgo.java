package tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import utils.Formatting;

public class ModelEvalGeneticAlgo {

	private static long MS_WEEK = 604800000l;
	private static long MS_90DAYS = 7776000000l;
	private static long MS_360DAYS = 31104000000l;
	
	private static int NUM_BASE_EPOCHS = 200;
	
	public static void main(String[] args) {
		try {
			// Specific test dates
			String[] testDateStrings = new String[10];
			testDateStrings[0] = "02/14/2013 00:00:00";
			testDateStrings[1] = "06/17/2013 00:00:00";
			testDateStrings[2] = "03/31/2014 00:00:00";
			testDateStrings[3] = "10/20/2014 00:00:00";
			testDateStrings[4] = "03/30/2015 00:00:00";
			testDateStrings[5] = "07/06/2015 00:00:00";
			testDateStrings[6] = "10/26/2015 00:00:00";
			testDateStrings[7] = "12/28/2015 00:00:00";
			testDateStrings[8] = "03/21/2016 00:00:00";
			testDateStrings[9] = "05/23/2016 00:00:00";

			// Load a bunch of shit in memory so I don't have to keep loading it.
			String rawStart = "01/01/2009 00:00:00";
			String rawEnd = "07/31/2016 00:00:00";
			Calendar rawStartC = Calendar.getInstance();
			Calendar rawEndC = Calendar.getInstance();
			rawStartC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(rawStart).getTime());
			rawEndC.setTimeInMillis(Formatting.sdfMMDDYYYYHHMMSS.parse(rawEnd).getTime());
			ARFF.loadRawCompleteSet(rawStartC, rawEndC);
			
			String notes = "Test 6";
			int numMetrics = 10;
			
			int epoch = 4474;
			double averageMetricGARunScore = 0;
			while (true) {
				System.out.println("Running epoch " + epoch++);
				
				// Select Metrics
				ArrayList<HashMap<String, Object>> metricGAList = new ArrayList<HashMap<String, Object>>();
				if (epoch < NUM_BASE_EPOCHS) {
					metricGAList = QueryManager.selectMetricGA(notes, numMetrics * 2);
				}
				else {
					metricGAList = QueryManager.selectMetricGA(notes, 200);
				}
				ArrayList<String> metricNames = selectMetrics(numMetrics, metricGAList, epoch);
				
				if (epoch == NUM_BASE_EPOCHS) {
					QueryManager.normalizeMetricGA(notes);
				}
				else if (epoch > NUM_BASE_EPOCHS) {
					averageMetricGARunScore = QueryManager.selectMetricGARunScore(notes);
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
				QueryManager.updateMetricGA(metricNames, increment, notes);
				QueryManager.insertMetricGARun(epoch - 1, metricNames, score, notes);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double buildModel(Calendar baseDate, ArrayList<String> metricNames) {
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
			BarKey bkEURUSD1H = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			barKeys.add(bkEURUSD1H);

			// Load Metric Discrete Values
			HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = QueryManager.loadMetricDiscreteValueHash("Percentiles Set 10");
			
			// STEP 1: Set gain/lose % ratio
			// STEP 2: Set the number of attributes to select
			int gainR = 1;
			int lossR = 1;
			int numAttributes = 40;
			double pipCutoff = .0000;
				
			ARFF.loadTrainingSet(trainStart, trainEnd);
			ARFF.loadTestSet(testStart, testEnd);
			
			String classifierName = "RBFNetwork";
			String classifierOptions = "-B 1 -S 1 -R 1.0E-8 -M -1 -W 1.0";
			
			String notes = "AS-" + numAttributes + " EUR.USD 1H " + classifierName + " " + metricNames.hashCode() + " Metric Hash " + Formatting.sdfMMDDYYYY.format(Calendar.getInstance().getTime());
			
			// Strategies (Bounded, Unbounded, FixedInterval, FixedIntervalRegression)
			/**    NNum, Close, Hour, Draw, Symbol, Attribute Selection **/
			double testCorrectRate = Modelling.buildAndEvaluateModel(classifierName, 		classifierOptions, trainStart, trainEnd, testStart, testEnd, 1, 1, 1, barKeys, 
					false, false, false, false, false, false, numAttributes, pipCutoff, "FixedInterval", metricNames, metricDiscreteValueHash, notes, baseDate);
			
			return testCorrectRate;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static ArrayList<String> selectMetrics(int num, ArrayList<HashMap<String, Object>> metricGAList, int epoch) {
		ArrayList<String> metrics = new ArrayList<String>();
		try {
			double totalScore = 0;
			if (epoch > NUM_BASE_EPOCHS) {
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
					String metric = metricGA.get("name").toString();
					double fraction = score / totalScore;
					if (Math.random() < fraction) {
						if (!metrics.contains(metric)) {
							metrics.add(metric);
							break;
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
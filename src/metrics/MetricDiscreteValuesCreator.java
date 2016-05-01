package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;
import utils.CalcUtils;

public class MetricDiscreteValuesCreator {

	public static void main(String[] args) {
		run();
	}

	public static void run() {
		try {
			ArrayList<Float> percentiles = new ArrayList<Float>();
			percentiles.add(.5f);
			percentiles.add(1f);
			percentiles.add(2f);
			percentiles.add(5f);
			percentiles.add(10f);
			percentiles.add(50f);
			percentiles.add(90f);
			percentiles.add(95f);
			percentiles.add(98f);
			percentiles.add(99f);
			percentiles.add(99.5f);
			
			
			// Original [1, 2, 5, 10, 20, 35, 50, 65, 80, 90, 95, 98, 99]
			// Set 1 [.5, 1, 2, 5, 10, 30, 50, 70, 90, 95, 98, 99, 99.5]
			// Set 2 [.5, 1, 2, 5, 10, 50, 90, 95, 98, 99, 99.5] // Best
			// Set 3 [.5, 1, 2, 5, 15, 85, 95, 98, 99, 99.5]
			// Set 4 [.2, .5, 1, 2, 5, 10, 50, 90, 95, 98, 99, 99.5, 99.8]
			// Set 5 [.5, 1.5, 4, 10, 50, 90, 96, 98.5, 99.5]
			
//			ArrayList<BarKey> barKeys = QueryManager.getUniqueBarKeysWithMetrics();
			
			// EUR.USD
			// EUR.GBP
			// GBP.USD
			
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
			BarKey bk2 = new BarKey("EUR.GBP", BAR_SIZE.BAR_5M);
			BarKey bk3 = new BarKey("GBP.USD", BAR_SIZE.BAR_5M);
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			barKeys.add(bk1);
			barKeys.add(bk2);
			barKeys.add(bk3);
			
			ArrayList<String> newMetrics = Constants.METRICS;
//			ArrayList<String> newMetrics = new ArrayList<String>();
			
			ArrayList<Float> values = new ArrayList<Float>();
			for (String metric : newMetrics) {
				if (!metric.startsWith("cdl")) {
					for (BarKey bk : barKeys) {
						HashMap<String, Calendar> metricTimes = QueryManager.getMinMaxMetricStarts(bk);
						
						for (float percentile : percentiles) {
							float maxValue = QueryManager.getMetricValueAtPercentile(metric, bk, "max", percentile);	
							maxValue = CalcUtils.round(maxValue, 2);
							if (!values.contains(maxValue)) {
								values.add(maxValue);
							}
						}
						MetricKey mk = new MetricKey(metric, bk.symbol, bk.duration);
						System.out.println(metric + ", " + bk.toString());
						QueryManager.insertIntoMetricDiscreteValues(metric, bk, metricTimes.get("min"), metricTimes.get("max"), percentiles, values);
						values = new ArrayList<Float>();
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
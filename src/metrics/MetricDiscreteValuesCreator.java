package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import data.downloaders.okcoin.OKCoinConstants;
import dbio.QueryManager;
import utils.CalcUtils;

public class MetricDiscreteValuesCreator {

	public static void main(String[] args) {
		run();
	}

	public static void run() {
		try {
			ArrayList<Float> percentiles = new ArrayList<Float>();
			percentiles.add(1f);
			percentiles.add(2f);
			percentiles.add(5f);
			percentiles.add(10f);
			percentiles.add(20f);
			percentiles.add(35f);
			percentiles.add(50f);
			percentiles.add(65f);
			percentiles.add(80f);
			percentiles.add(90f);
			percentiles.add(95f);
			percentiles.add(98f);
			percentiles.add(99f);
			
//			ArrayList<BarKey> barKeys = QueryManager.getUniqueBarKeysWithMetrics();
			
			BarKey bk1 = new BarKey("okcoinBTCCNY", BAR_SIZE.BAR_1M);
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			barKeys.add(bk1);
			
			//ArrayList<String> newMetrics = Constants.METRICS;
			ArrayList<String> newMetrics = new ArrayList<String>();
			newMetrics.add("ppo3_10");
			newMetrics.add("ppo10_30");
			newMetrics.add("ppo30_100");
			newMetrics.add("ppodydx10_30");
			newMetrics.add("ppodydx30_100");
			
			
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
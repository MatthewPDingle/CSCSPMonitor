package data;

import java.util.Calendar;
import java.util.HashMap;

import constants.Constants.BAR_SIZE;

public class BarWithMetricData extends Bar {

	private HashMap<String, Double> metricData = new HashMap<String, Double>();
	
	public BarWithMetricData(Bar b) {
		super(b);
	}

	public BarWithMetricData(String symbol, float open, float close, float high, float low, Float vwap, float volume,
			Integer numTrades, Float change, Float gap, Calendar periodStart, Calendar periodEnd, BAR_SIZE duration, boolean partial) {
		
		super(symbol, open, close, high, low, vwap, volume, numTrades, change, gap, periodStart, periodEnd, duration, partial);
		
	}

	public HashMap<String, Double> getMetricData() {
		return metricData;
	}

	public void setMetricData(HashMap<String, Double> metricData) {
		this.metricData = metricData;
	}
}
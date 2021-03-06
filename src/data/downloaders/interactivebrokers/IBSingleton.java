package data.downloaders.interactivebrokers;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import data.Bar;
import data.BarKey;
import utils.Formatting;

public class IBSingleton {

	private static IBSingleton instance = null;
	
	private HashMap<String, Object> ibAccountInfoHash;
	private HashMap<BarKey, IBWorker> ibWorkerHash; // One worker per BarKey.  Responsible for API interactions.
	private HashMap<BarKey, HashMap<String, Double>> bkTickerDataHash; // Latest tick info for all BarKeys being used.
	private Bar realtimeBar = null; // IBWorker gets realtime bars and put them here.  StatusSingleton grabs them every second to update metrics. TODO: This is lazy and clumsy
	private Bar completeBar = null; // IBWorker gets realtime bars and puts only the complete one here.  IBEngine uses this to know which bar to evaluate.
	private boolean metricsUpdated = false; // StatusSingleton updates metrics, then sets this to true to signal that completeBar is good to be used for evaluations.
	private int clientID = 2; // Each request for a new IBWorker will increment this so that they're all unique.
	
	protected IBSingleton() {
		ibAccountInfoHash = new HashMap<String, Object>();
		ibWorkerHash = new HashMap<BarKey, IBWorker>();
		bkTickerDataHash = new HashMap<BarKey, HashMap<String, Double>>();
	}
	
	public static IBSingleton getInstance() {
		if (instance == null) {
			instance = new IBSingleton();
		}
		return instance;
	}

	public HashMap<BarKey, IBWorker> getIbWorkerHash() {
		return ibWorkerHash;
	}

	public IBWorker requestWorker(BarKey bk) {
		IBWorker ibWorker = ibWorkerHash.get(bk);
		if (ibWorker == null) {
			ibWorker = new IBWorker(clientID++, bk);
		}
		ibWorkerHash.put(bk, ibWorker);
		
		return ibWorker;
	}
	
	public void cancelWorkers() {
		for (Entry<BarKey, IBWorker> entry : ibWorkerHash.entrySet()) {
			entry.getValue().disconnect();
		}
	}

	public HashMap<BarKey, HashMap<String, Double>> getBkTickerDataHash() {
		return bkTickerDataHash;
	}
	
	public HashMap<String, Double> getTickerDataHash(BarKey bk) {
		return bkTickerDataHash.get(bk);
	}
	
	public Double getTickerFieldValue(BarKey bk, String tickField) {
		HashMap<String, Double> tickerDataHash = bkTickerDataHash.get(bk);
		if (tickerDataHash != null) {
			if (!tickField.equals(IBConstants.TICK_FIELD_MIDPOINT)) {
				return tickerDataHash.get(tickField);
			}
			else {
				Double bid = tickerDataHash.get(IBConstants.TICK_FIELD_BID_PRICE);
				Double ask = tickerDataHash.get(IBConstants.TICK_FIELD_ASK_PRICE);
				if (bid == null && ask == null) {
					return null;
				}
				if (ask == null) {
					return bid;
				}
				if (bid == null) {
					return ask;
				}
				return new Double(Formatting.df6.format((bid + ask) / 2d));
			}
		}
		return null;
	}
	
	public void updateBKTickerData(BarKey bk, String key, Double value) {
		HashMap<String, Double> tickerDataHash = bkTickerDataHash.get(bk);
		if (tickerDataHash == null) {
			tickerDataHash = new HashMap<String, Double>();
		}
		tickerDataHash.put(key, value);
		bkTickerDataHash.put(bk, tickerDataHash);
	}
	
	public void updateAccountInfo(String field, Object value) {
		ibAccountInfoHash.put(field, value);
	}
	
	public Double getAccountInfoValue(String field) {
		try {
			if (ibAccountInfoHash != null && ibAccountInfoHash.get(field) != null) {
				Object o  = ibAccountInfoHash.get(field);
				return Double.parseDouble(o.toString());
			}
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public HashMap<String, Object> getIbAccountInfoHash() {
		return ibAccountInfoHash;
	}

	public Bar getCompleteBarAndClear() {
		if (completeBar == null) {
			return null;
		}
		if (metricsUpdated) {
			Bar b = new Bar(completeBar);
			completeBar = null;
			metricsUpdated = false;
			System.out.println("getCompleteBarAndClear() " + Calendar.getInstance().getTime().toString());
			return b;
		}
		else {
			return null;
		}
	}

	public void setCompleteBar(Bar completeBar) {
		this.completeBar = new Bar(completeBar);
		metricsUpdated = false;
	}

	public void setRealtimeBar(Bar realtimeBar) {
		this.realtimeBar = realtimeBar;
	}	

	/**
	 * StatusSingleton calls this to calculate metrics
	 * @return
	 */
	public Bar getRealtimeBarAndClear() {
		if (realtimeBar == null) {
			return null;
		}
		Bar b = new Bar(realtimeBar);
		realtimeBar = null;
		return b;
	}

	public boolean isMetricsUpdated() {
		return metricsUpdated;
	}

	public void setMetricsUpdated(boolean completeBarMetricsUpdated) {
		if (completeBar != null) {
//			System.out.println("Complete Bar Metrics Updated " + Calendar.getInstance().getTime().toString());
			this.metricsUpdated = completeBarMetricsUpdated;
		}
	}
}
package data.downloaders.interactivebrokers;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import data.Bar;
import data.BarKey;

public class IBSingleton {

	private static IBSingleton instance = null;
	
	private HashMap<String, Object> ibAccountInfoHash;
	private HashMap<BarKey, IBWorker> ibWorkerHash; // One worker per BarKey.  Responsible for API interactions.
	private HashMap<BarKey, HashMap<String, Double>> bkTickerDataHash; // Latest tick info for all BarKeys being used.
	private Bar realtimeBar = null; // IBWorkers get realtime bars and put them here.  StatusSingleton grabs them every second to update metrics. TODO: This is lazy and clumsy
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
				DecimalFormat df = new DecimalFormat("#.######");
				return new Double(df.format((bid + ask) / 2d));
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

	public void setRealtimeBar(Bar realtimeBar) {
		this.realtimeBar = realtimeBar;
	}
	
	public Bar getRealtimeBarAndClear() {
		if (realtimeBar == null) {
			return null;
		}
		Bar b = new Bar(realtimeBar);
		realtimeBar = null;
		return b;
	}
}
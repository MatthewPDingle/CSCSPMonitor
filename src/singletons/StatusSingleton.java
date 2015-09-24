package singletons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.Bar;
import data.BarKey;
import data.downloaders.okcoin.websocket.OKCoinWebSocketSingleton;
import dbio.QueryManager;
import metrics.MetricCacheSingleton;

public class StatusSingleton {

	private static StatusSingleton instance = null;
	
	private boolean realtimeDownloaderRunning = false;
	private HashMap<BarKey, Calendar> lastDownloadHash = new HashMap<BarKey, Calendar>();
	private ArrayList<String> dataMessageQueue = new ArrayList<String>();
	private ArrayList<String> tradingMessageQueue = new ArrayList<String>();

	protected StatusSingleton() {
	}
	
	public static StatusSingleton getInstance() {
		if (instance == null) {
			instance = new StatusSingleton();
		}
		return instance;
	}
	
	public boolean isRealtimeDownloaderRunning() {
		return realtimeDownloaderRunning;
	}

	public void setRealtimeDownloaderRunning(boolean realtimeDownloaderRunning) {
		this.realtimeDownloaderRunning = realtimeDownloaderRunning;
	}

	public Calendar getLastDownload(BarKey bk) {
		return lastDownloadHash.get(bk);
	}

	public void recordLastDownload(BarKey bk, Calendar time) {
		lastDownloadHash.put(bk, time);
	}

	public HashMap<BarKey, Calendar> getLastDownloadHash() {
		return lastDownloadHash;
	}

	public void setLastDownloadHash(HashMap<BarKey, Calendar> lastDownloadHash) {
		this.lastDownloadHash = lastDownloadHash;
	}

	public ArrayList<String> getDataMessageQueue() {
		ArrayList<String> currentMessages = new ArrayList<String>(dataMessageQueue);
		dataMessageQueue = new ArrayList<String>();
		return currentMessages;
	}
	
	public void addMessageToDataMessageQueue(String message) {
		dataMessageQueue.add(message);
	}

	public ArrayList<String> getTradingMessageQueue() {
		ArrayList<String> currentMessages = new ArrayList<String>(tradingMessageQueue);
		tradingMessageQueue = new ArrayList<String>();
		return currentMessages;
	}

	public void addMessageToTradingMessageQueue(String message) {
		tradingMessageQueue.add(message);
	}
	
	public void processActionQueue() {
		OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
		
		ArrayList<Bar> latestBars = okss.getLatestBarsAndClear();
		if (latestBars != null) {
			for (Bar bar : latestBars) {
				QueryManager.insertOrUpdateIntoBar(bar);
				BarKey bk = new BarKey(bar.symbol, bar.duration);
				recordLastDownload(bk, Calendar.getInstance());
				addMessageToDataMessageQueue("OKCoin WebSocket API streaming " + bk.symbol);
				
				long start = Calendar.getInstance().getTimeInMillis();
				MetricCacheSingleton.getInstance().refreshMetricSequenceHash();
				long end = Calendar.getInstance().getTimeInMillis();
				long time = end - start;
				System.out.println("refreshMetricSequenceHash() took " + (time / 1000f) + " seconds");
				MetricSingleton.getInstance().setRunning(true);
			}
		}
		if (latestBars == null || latestBars.size() == 0) {
//			addMessageToDataMessageQueue("OKCoin WebSocket API did not receive any new data");
		}
	}
}
package status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;

import data.Bar;
import data.BarKey;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.okcoin.websocket.NIAStatusSingleton;
import dbio.QueryManager;
import metrics.MetricSingleton;

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

	public void addStatusMessageToTradingMessageQueue(String message) {
		Gson gson = new Gson();
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.put("Status", message);
		String json = gson.toJson(hash);
		tradingMessageQueue.add(json);
	}
	
	public void addJSONMessageToTradingMessageQueue(String json) {
		tradingMessageQueue.add(json);
	}
	
	public void processDataActionQueue() {
		NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
		IBSingleton ibs = IBSingleton.getInstance();
		MetricSingleton ms = MetricSingleton.getInstance();
		
		// OKCoin
		ArrayList<Bar> niassLatestBars = niass.getLatestBarsAndClear();
		if (niassLatestBars != null && niassLatestBars.size() > 0) {
			// Insert or update the latest bars.  There'll be as many as the WebSocket API has streamed in.
			long start = Calendar.getInstance().getTimeInMillis();
			for (Bar bar : niassLatestBars) {
				QueryManager.insertOrUpdateIntoBar(bar);
				BarKey bk = new BarKey(bar.symbol, bar.duration);
				recordLastDownload(bk, Calendar.getInstance());
				addMessageToDataMessageQueue("OKCoin WebSocket API streaming " + bk.symbol + " - " + bk.duration);
				ms.updateMetricSequenceHash(bar);
			}
			long end = Calendar.getInstance().getTimeInMillis();
			long time = end - start;

			// Recalculate metrics.
			if (!ms.areThreadsRunning()) {
				ms.startThreads(); // I think I want to start them and keep going probably?
				long metricEnd = Calendar.getInstance().getTimeInMillis();
				time = metricEnd - end;
				System.out.println("Metric threads took " + (time / 1000f) + " seconds");
			}
			else {
				System.out.println("Not calculating metrics because a calculation is already going.");
			}
		}
		
		// IB
		Bar realtimeBar = ibs.getRealtimeBarAndClear();
		if (realtimeBar != null) {
			long start = Calendar.getInstance().getTimeInMillis();
			ms.updateMetricSequenceHash(realtimeBar);
			long end = Calendar.getInstance().getTimeInMillis();
			long time = end - start;

			// Recalculate metrics.
			if (!ms.areThreadsRunning()) {
				ms.startThreads(); // I think I want to start them and keep going probably?
				long metricEnd = Calendar.getInstance().getTimeInMillis();
				time = metricEnd - end;
//				System.out.println("Metric threads took " + (time / 1000f) + " seconds");
				ibs.setMetricsUpdated(true);
				addMessageToDataMessageQueue("StatusSingleton requested metric update. Took " + (time / 1000f) + " seconds");
			}
			else {
				System.out.println("Not calculating metrics because a calculation is already going.");
			}
		}
	}
}
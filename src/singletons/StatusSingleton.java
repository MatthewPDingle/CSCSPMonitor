package singletons;

import java.util.ArrayList;
import java.util.Calendar;

public class StatusSingleton {

	private static StatusSingleton instance = null;
	
	private boolean realtimeDownloaderRunning = false;
	private Calendar lastRealtimeDownload = null;
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

	public Calendar getLastRealtimeDownload() {
		return lastRealtimeDownload;
	}

	public void setLastRealtimeDownload(Calendar lastRealtimeDownload) {
		this.lastRealtimeDownload = lastRealtimeDownload;
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
}
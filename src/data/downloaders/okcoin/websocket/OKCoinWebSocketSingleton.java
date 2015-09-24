package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.HashMap;

import data.Bar;

public class OKCoinWebSocketSingleton {

	private static OKCoinWebSocketSingleton instance = null;
	
	private OKCoinWebSocketThread okThread;
	private HashMap<String, HashMap<String, String>> symbolTickerDataHash; // Last Tick info - price, bid, ask, timestamp
	private ArrayList<Bar> latestBars;
	private boolean disconnected = false;
	
	protected OKCoinWebSocketSingleton() {
		okThread = new OKCoinWebSocketThread();
		symbolTickerDataHash = new HashMap<String, HashMap<String, String>>();
		latestBars = new ArrayList<Bar>();
	}
	
	public static OKCoinWebSocketSingleton getInstance() {
		if (instance == null) {
			instance = new OKCoinWebSocketSingleton();
		}
		return instance;
	}
	
	public void setRunning(boolean running) {
		try {
			if (running) {
				if (!okThread.isRunning()) {
					okThread = new OKCoinWebSocketThread();
					okThread.setRunning(true);
					okThread.start();
				}
			}
			else {
				okThread.removeAllChannels();
				okThread.setRunning(false);
				okThread.join();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addChannel(String channel) {
		okThread.addChannel(channel);
	}
	
	public void removeChannel(String channel) {
		okThread.removeChannel(channel);
	}
	
	public HashMap<String, HashMap<String, String>> getSymbolDataHash() {
		return symbolTickerDataHash;
	}

	public void putSymbolTickerDataHash(String symbol, HashMap<String, String> tickerDataHash) {
		this.symbolTickerDataHash.put(symbol, tickerDataHash);
	}

	public synchronized ArrayList<Bar> getLatestBarsAndClear() {
		ArrayList<Bar> returnList = new ArrayList<Bar>();
		returnList.addAll(latestBars);
		latestBars.clear();
		return returnList;
	}

	public synchronized void addLatestBars(ArrayList<Bar> latestBars) {
		if (latestBars.size() == 5) {
			this.latestBars = latestBars;
		}
		if (latestBars.size() == 2) {
			latestBars.get(0).partial = false;
			latestBars.get(1).partial = true;
			this.latestBars = latestBars;
		}
		if (latestBars.size() == 1) {
			latestBars.get(0).partial = true;
			if (this.latestBars.size() <= 1) {
				this.latestBars = latestBars;
			}
			else if (this.latestBars.size() == 2) {
				this.latestBars.get(0).partial = false;
				this.latestBars.set(1, latestBars.get(0));
			}
			else if (this.latestBars.size() == 5) {
				this.latestBars.get(0).partial = false;
				this.latestBars.get(1).partial = false;
				this.latestBars.get(2).partial = false;
				this.latestBars.get(3).partial = false;
				this.latestBars.set(4, latestBars.get(0));
			}
		}
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}
}
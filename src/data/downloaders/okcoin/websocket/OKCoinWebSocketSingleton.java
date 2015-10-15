package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.HashMap;

import data.Bar;

public class OKCoinWebSocketSingleton {

	private static OKCoinWebSocketSingleton instance = null;
	
	private OKCoinWebSocketThread okThread;
	private HashMap<String, HashMap<String, String>> symbolTickerDataHash; // Last Tick info - price, bid, ask, timestamp
	private HashMap<String, ArrayList<ArrayList<Double>>> symbolBidOrderBook;
	private HashMap<String, ArrayList<ArrayList<Double>>> symbolAskOrderBook;
	private double btcOnHand = 0;
	private double ltcOnHand = 0;
	private double cnyOnHand = 0;
	private ArrayList<Bar> latestBars;
	private boolean disconnected = false;
	
	protected OKCoinWebSocketSingleton() {
		okThread = new OKCoinWebSocketThread();
		symbolTickerDataHash = new HashMap<String, HashMap<String, String>>();
		symbolBidOrderBook = new HashMap<String, ArrayList<ArrayList<Double>>>();
		symbolAskOrderBook = new HashMap<String, ArrayList<ArrayList<Double>>>();
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
	
	public void orderInfo() {
		
	}
	
	public void spotTrade(String apiKey, String secretKey, String symbol, double price, double amount, String type) {
		if (!okThread.isRunning()) {
			System.err.println("okThread is not running so cannot spotTrade(...)");
		}
		else {	
			String sPrice = new Double(price).toString();
			String sAmount = new Double(amount).toString();
			okThread.spotTrade(apiKey, secretKey, symbol, sPrice, sAmount, type);
		}
	}
	
	public void getOrderInfo(String apiKey, String secretKey, String okCoinSymbol, long orderID) {
		if (!okThread.isRunning()) {
			System.err.println("okThread is not running so cannot getOrderInfo(...)");
		}
		else {	
			okThread.getOrderInfo(apiKey, secretKey, okCoinSymbol, orderID);
		}
	}
	
	public void getUserInfo(String apiKey, String secretKey) {
		if (!okThread.isRunning()) {
			System.err.println("okThread is not running so cannot getUserInfo(...)");
		}
		else {	
			okThread.getUserInfo(apiKey, secretKey);
		}
	}
	
	public void getRealTrades(String apiKey, String secretKey) {
		if (!okThread.isRunning()) {
			System.err.println("okThread is not running so cannot getRealTrades(...)");
		}
		else {	
			okThread.getRealTrades(apiKey, secretKey);
		}
	}
	
	public HashMap<String, HashMap<String, String>> getSymbolDataHash() {
		return symbolTickerDataHash;
	}

	public void putSymbolTickerDataHash(String symbol, HashMap<String, String> tickerDataHash) {
		this.symbolTickerDataHash.put(symbol, tickerDataHash);
	}
	
	public HashMap<String, ArrayList<ArrayList<Double>>> getSymbolBidOrderBook() {
		return symbolBidOrderBook;
	}

	public void putSymbolBidOrderBook(String symbol, ArrayList<ArrayList<Double>> orderBook) {
		this.symbolBidOrderBook.put(symbol, orderBook);
	}

	public HashMap<String, ArrayList<ArrayList<Double>>> getSymbolAskOrderBook() {
		return symbolAskOrderBook;
	}

	public void putSymbolAskOrderBook(String symbol, ArrayList<ArrayList<Double>> orderBook) {
		this.symbolAskOrderBook.put(symbol, orderBook);
	}
	
	public double getBtcOnHand() {
		return btcOnHand;
	}

	public void setBtcOnHand(double btcOnHand) {
		this.btcOnHand = btcOnHand;
	}

	public double getLtcOnHand() {
		return ltcOnHand;
	}

	public void setLtcOnHand(double ltcOnHand) {
		this.ltcOnHand = ltcOnHand;
	}

	public double getCnyOnHand() {
		return cnyOnHand;
	}

	public void setCnyOnHand(double cnyOnHand) {
		this.cnyOnHand = cnyOnHand;
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
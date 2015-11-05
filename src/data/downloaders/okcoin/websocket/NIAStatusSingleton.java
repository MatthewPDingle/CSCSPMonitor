package data.downloaders.okcoin.websocket;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.Bar;
import data.downloaders.okcoin.OKCoinConstants;

public class NIAStatusSingleton {
	private static NIAStatusSingleton instance = null;
	
	private boolean niaClientHandlerConnected = false;
	private NIAClient niaClient;
	private HashMap<String, HashMap<String, String>> symbolTickerDataHash; // Last Tick info - price, bid, ask, timestamp
	private HashMap<String, ArrayList<ArrayList<Double>>> symbolBidOrderBook;
	private HashMap<String, ArrayList<ArrayList<Double>>> symbolAskOrderBook;
	private Object requestedTradeLock;
	private double btcOnHand = 0;
	private double ltcOnHand = 0;
	private double cnyOnHand = 0;
	private ArrayList<Bar> latestBars;
	private Calendar lastActivity = null;
	private ArrayList<String> channels;
	private boolean keepAlive = false;
	private boolean okToWaitForConnection = true;
	
	protected NIAStatusSingleton() {
		niaClient = new NIAClient();
		symbolTickerDataHash = new HashMap<String, HashMap<String, String>>();
		symbolBidOrderBook = new HashMap<String, ArrayList<ArrayList<Double>>>();
		symbolAskOrderBook = new HashMap<String, ArrayList<ArrayList<Double>>>();
		latestBars = new ArrayList<Bar>();
		requestedTradeLock = new Object();
		lastActivity = Calendar.getInstance();
		channels = new ArrayList<String>();
		keepAlive = true;
	}
	
	public static NIAStatusSingleton getInstance() {
		if (instance == null) {
			instance = new NIAStatusSingleton();
		}
		return instance;
	}
	
	public void reinitClient() {
		try {
			System.out.println("NIAStatusSingleton reinitClient(...)");
			stopClient();
			niaClientHandlerConnected = false;
			Thread.sleep(5000);
			niaClient = new NIAClient();
			boolean success = startClient();
			if (success) {
				reloadChannels();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean startClient() {
		try {
			System.out.println("NIAStatusSingleton startClient(...)");
			okToWaitForConnection = true;

			// Connect
//			boolean connectSuccess = false;
//			while (!connectSuccess) {
//				try {
					boolean connectSuccess = niaClient.connect();
					if (!connectSuccess) {
						System.err.println("NIAStatusSingleton startClient(...) got back a unsuccessful NIAClient connect(...).  Will attempt reconnect...");
						return false;
//						Thread.sleep(2000);
					}
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
	
			// Wait until we get connected
			while (!NIAStatusSingleton.getInstance().isNiaClientHandlerConnected()) {
				if (!okToWaitForConnection) {
					System.err.println("NIAStatusSingleton startClient(...) abandoning attempt due to call to stopClient(...)");
					return false;
				}
				System.out.print(".");
				Thread.sleep(100);
			}
			System.out.println("NIAClient connected");
			noteActivity();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean stopClient() {
		try {
			System.out.println("NIAStatusSingleton stopClient(...)");
			okToWaitForConnection = false;
			if (niaClient.getHandler() != null) {
				niaClient.getHandler().getTimer().cancel();
			}
			niaClient.removeAllChannels();
			niaClient.disconnect();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public NIAClient getNiaClient() {
		return niaClient;
	}

	public void setNiaClient(NIAClient niaClient) {
		this.niaClient = niaClient;
	}

	public void reloadChannels() {
		for (String channel : channels) {
			addChannel(channel);
		}
	}
	
	public void addChannel(String channel) {
		if (!channels.contains(channel)) {
			channels.add(channel);
		}
		niaClient.addChannel(channel);
	}

	public void removeChannel(String channel) {
		niaClient.removeChannel(channel);
	}
	
	public void spotTrade(String symbol, double price, double amount, String type) {
		DecimalFormat df2 = new DecimalFormat("#.##");
		DecimalFormat df3 = new DecimalFormat("#.###");
		String sPrice = df2.format(price);
		String sAmount = df3.format(amount);
		niaClient.spotTrade(symbol, sPrice, sAmount, type);
	}

	public void cancelOrder(String symbol, Long orderId) {
		niaClient.cancelOrder(symbol, orderId);
	}
	
	public void getOrderInfo(String okCoinSymbol, long orderID) {
		niaClient.getOrderInfo(okCoinSymbol, orderID);
	}
	
	public void getUserInfo() {
		niaClient.getUserInfo();
	}

	public void getRealTrades() {
		niaClient.getRealTrades();
	}

	public void cancelOrders(ArrayList<Long> exchangeIDs) {
		for (long exchangeID : exchangeIDs) {
			niaClient.cancelOrder(OKCoinConstants.SYMBOL_BTCCNY, exchangeID);
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
	
	public void noteActivity() {
		lastActivity = Calendar.getInstance();
	}

	public Calendar getLastActivityTime() {
		return lastActivity;
	}

	public Object getRequestedTradeLock() {
		return requestedTradeLock;
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

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isNiaClientHandlerConnected() {
		return niaClientHandlerConnected;
	}

	public void setNiaClientHandlerConnected(boolean niaClientHandlerConnected) {
		this.niaClientHandlerConnected = niaClientHandlerConnected;
	}
}
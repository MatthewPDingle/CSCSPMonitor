package data.downloaders.okcoin.websocket;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.Bar;
import data.downloaders.okcoin.OKCoinConstants;
import dbio.QueryManager;
import utils.Formatting;

public class NIAStatusSingleton {
	private static NIAStatusSingleton instance = null;
	
	public static long USE_REST_IF_WEBSOCKET_DELAYED_MS = 5000;
	
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
	private boolean startup = true;
	private boolean niaClientHandlerConnected = false;
	private boolean keepAlive = false;
	private boolean okToWaitForConnection = true;
	private int disconnectCount = 0;
	
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
		
		noteActivity();
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
				// Cleanup any requested trades in the DB we won't be able to figure out.
				cleanHangingRequestsFromDB();
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
			boolean connectSuccess = niaClient.connect();
			if (!connectSuccess) {
				System.err.println("NIAStatusSingleton startClient(...) got back a unsuccessful NIAClient connect(...)");
				return false;
			}
			
			// Wait until we get connected
			System.out.print("~");
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
	
	public void cleanHangingRequestsFromDB() {
		int numRowsDeleted = QueryManager.deleteAllRequestedOrders();
		if (numRowsDeleted > 0) {
			System.err.println("Unfortunately we had to delete " + numRowsDeleted + " trades with requests in the DB during reinit(...)");
		}
		else {
			System.out.println("DB seems to be OK.  No hanging requests.");
		}
		
		reloadChannels();
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
		getRealTrades();
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
		String sPrice = Formatting.df2.format(price);
		String sAmount = Formatting.df3.format(amount);
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
		if (niaClientHandlerConnected) {
			startup = false;
		}
	}

	public boolean isStartup() {
		return startup;
	}

	public void setStartup(boolean startup) {
		this.startup = startup;
	}

	public void setOkToWaitForConnection(boolean okToWaitForConnection) {
		this.okToWaitForConnection = okToWaitForConnection;
	}
	
	public void recordDisconnect() {
		disconnectCount++;
	}

	public int getDisconnectCount() {
		return disconnectCount;
	}
}
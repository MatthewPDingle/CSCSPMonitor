package data.downloaders.interactivebrokers;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TagValue;
import com.ib.client.TickType;
import com.ib.client.UnderComp;
import com.ib.controller.OrderType;

import constants.APIKeys;
import constants.Constants;
import data.Bar;
import data.BarComparator;
import data.BarKey;
import data.FuturesStitcher;
import data.downloaders.interactivebrokers.IBConstants.ORDER_ACTION;
import dbio.IBQueryManager;
import dbio.QueryManager;
import metrics.MetricSingleton;
import status.StatusSingleton;
import utils.CalendarUtils;
import utils.Formatting;

public class IBWorker implements EWrapper {

	private EClientSocket client = new EClientSocket(this);
	private int clientID = 1;

	private ArrayList<Bar> historicalBars = new ArrayList<Bar>(); // Should come in oldest to newest
	private BarKey barKey;
	private int barSeconds;
	private Calendar fullBarStart = null;
	private Calendar fullBarEnd = null;
	private double realtimeBarOpen;
	private double realtimeBarClose;
	private double realtimeBarHigh;
	private double realtimeBarLow;
	private double realtimeBarVolume;
	private int realtimeBarSubBarCounter;
	private int realtimeBarNumSubBarsInFullBar;
	private double realtimeBarLastBarClose;
	private int lastProcessedRequestID;
	private boolean firstRealtimeBarCompleted;
	private HashMap<String, LinkedList<HashMap<String, Object>>> eventDataHash;
	private StatusSingleton ss;
	private IBSingleton ibs;
	private MetricSingleton ms;

	private static String expiry = "";
	
	public static void main(String[] args) {
		try {
			String symbol = IBConstants.TICK_NAME_CME_ECBOT_FUTURES_ZN;
			expiry = "201712";
			IBWorker ibdd = new IBWorker(2, new BarKey(symbol + " " + expiry, Constants.BAR_SIZE.BAR_30M));

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS zzz");
//			String sStart = "02/01/2015 00:00:00.000 EST";
//			String sEnd = 	"06/01/2017 00:00:00.000 EST";
//			Calendar start = Calendar.getInstance();
//			start.setTime(sdf.parse(sStart));
//			Calendar end = Calendar.getInstance();
//			end.setTime(sdf.parse(sEnd));
				
			Calendar start = CalendarUtils.getFuturesStart(symbol, expiry);
			Calendar end = CalendarUtils.getFuturesEnd(symbol, expiry);

			System.out.println("Start: " + start.getTime().toString());
			System.out.println("End: " + end.getTime().toString());

			ibdd.connect();
			ibdd.downloadHistoricalBars(start, end, false);
			ibdd.disconnect();
			for (Bar bar : ibdd.historicalBars) {
				QueryManager.insertOrUpdateIntoBar(bar);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param bk
	 */
	public IBWorker(int clientID, BarKey bk) {
		super();

		ss = StatusSingleton.getInstance();
		ibs = IBSingleton.getInstance();
		ms = MetricSingleton.getInstance();

		Formatting.df6.setRoundingMode(RoundingMode.HALF_UP);

		this.clientID = clientID;
		this.barKey = bk;

		initVariables();
	}

	public void setBarKey(BarKey barKey) {
		this.barKey = barKey;
	}

	private void initVariables() {
		this.historicalBars.clear();
		this.fullBarStart = null;
		this.fullBarEnd = null;
		this.realtimeBarOpen = 0;
		this.realtimeBarClose = 0;
		this.realtimeBarHigh = 0;
		this.realtimeBarLow = 1000000;
		this.realtimeBarVolume = -1;
		this.realtimeBarSubBarCounter = 0;
		this.realtimeBarNumSubBarsInFullBar = 1;
		this.realtimeBarLastBarClose = 0;
		this.firstRealtimeBarCompleted = false;
		this.eventDataHash = new HashMap<String, LinkedList<HashMap<String, Object>>>();
	}

	public boolean connect() {
		try {
			if (!client.isConnected()) {
				ss.addMessageToDataMessageQueue("IB Client connecting...");
				client.eConnect("localhost", IBConstants.IB_API_PORT, this.clientID);
				int waitTimeMS = 0;
				while (!client.isConnected()) {
					Thread.sleep(100);
					waitTimeMS += 100;
					if (waitTimeMS >= IBConstants.CONNECT_TIMEOUT_MS) {
						return false;
					}
				}
			}
			ss.addMessageToDataMessageQueue("IB Client connected!");
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			ss.addMessageToDataMessageQueue("IB Client failed to connect!");
			return false;
		}
	}

	public void disconnect() {
		if (client.isConnected()) {
			client.eDisconnect();
		}
	}

	public void requestAccountInfoSubscription() {
		try {
			if (!client.isConnected()) {
				ss.addMessageToDataMessageQueue(
						"IB Client not connected so attempting to connect before requestAccountInfoSubscription()");
				connect();
			}
			if (client.isConnected()) {
				client.reqAccountUpdates(true, APIKeys.IB_PAPER_ACCOUNT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void requestTickSubscription() {
		try {
			if (!client.isConnected()) {
				ss.addMessageToDataMessageQueue(
						"IB Client not connected so attempting to connect before requestTickSubscription()");
				connect();
			}
			if (client.isConnected()) {
				
				String symbol = barKey.symbol;
				if (symbol.matches(".* \\d{6}")) {
					symbol = barKey.symbol.substring(0, barKey.symbol.indexOf(" "));
					expiry = barKey.symbol.substring(barKey.symbol.indexOf(" ") + 1);
				}
				
				// Get Ticker ID
				int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(barKey);

				// Build Contract
				Contract contract = new Contract();
				contract.m_conId = 0;
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(symbol);
				if (securityType.equals("CASH")) {
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(symbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(symbol);
					contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				}
				else if (securityType.equals("FUT")) {
					contract.m_symbol = symbol;
					contract.m_multiplier = IBConstants.FUTURE_SYMBOL_MULTIPLIER_HASH.get(symbol);
					contract.m_expiry = expiry;
					contract.m_exchange = IBConstants.TICKER_EXCHANGE_HASH.get(symbol);
				}
				contract.m_secType = securityType;
				
				// Tick Type List -
				// https://www.interactivebrokers.com/en/software/api/apiguide/tables/generic_tick_types.htm
				String tickTypes = "233"; // Returns last trade price, size, time, volume

				// Use the client to request market data
				client.reqMktData(tickerID, contract, tickTypes, false, new Vector<TagValue>());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancelTickSubscription() {
		ss.addMessageToDataMessageQueue("IB Client cancelTickSubscription()");
		if (client.isConnected()) {
			int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(barKey);
			client.cancelMktData(tickerID);
		}
	}

	public void downloadRealtimeBars() {
		try {
			initVariables();
			if (!client.isConnected()) {
				ss.addMessageToDataMessageQueue("IB Client not connected so attempting to connect before downloadRealtimeBars()");
				connect();
			}
			if (client.isConnected()) {
				
				String baseSymbol = barKey.symbol;
				if (baseSymbol.matches(".* \\d{6}")) {
					baseSymbol = barKey.symbol.substring(0, barKey.symbol.indexOf(" "));
					expiry = barKey.symbol.substring(barKey.symbol.indexOf(" ") + 1);
				}
				
				// Figure out when to start the historical data download, and
				// make the end equal to right now.
				Calendar start = Calendar.getInstance();
				Bar mostRecentDBBar = QueryManager.getMostRecentBar(barKey, Calendar.getInstance());
				if (mostRecentDBBar == null) {
					start.set(Calendar.YEAR, 2015);
					start.set(Calendar.MONTH, 0);
					start.set(Calendar.DAY_OF_MONTH, 1);
					start.set(Calendar.HOUR, 0);
					start.set(Calendar.MINUTE, 0);
					start.set(Calendar.SECOND, 0);
					start.set(Calendar.MILLISECOND, 0);
				} 
				else {
					start.setTimeInMillis(mostRecentDBBar.periodStart.getTimeInMillis());
					start = CalendarUtils.addBars(start, barKey.duration, -2); // Go back 2 additional bars so we cover parital bars & get the 2nd to last one's open & close.
				}
				Calendar end = CalendarUtils.getBarEnd(Calendar.getInstance(), barKey.duration);
	
				// Download any needed historical data first so we're caught up
				// and ready for realtime bars
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString()+ ") downloading historical data to catch up to realtime bars...");
				System.out.println("Start: " + start.getTime().toString());
				System.out.println("End: " + end.getTime().toString());
				downloadHistoricalBars(start, end, false);
				if (this.historicalBars != null) {
					ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") downloaded " + this.historicalBars.size() + " historical bars.");
				}
				for (Bar bar : this.historicalBars) {
					QueryManager.insertOrUpdateIntoBar(bar);
					FuturesStitcher.processOneBar(baseSymbol, barKey.duration, bar.periodStart);
				}
				
				// Do a metric calculation update.
				ms.init();
				ms.startThreads();

				if (ms.getNeededMetrics() != null) {
					ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") updated " + ms.getNeededMetrics().size() + " metrics for the historical bars.");
				}

				// Setup the realtime bar variables with the latest
				// historicalbar data so they know how the bar started.
				preloadRealtimeBarWithLastHistoricalBar();

				// Now prepare for realtime bars
				switch (barKey.duration) {
				case BAR_30S:
					realtimeBarNumSubBarsInFullBar = 6;
					break;
				case BAR_1M:
					realtimeBarNumSubBarsInFullBar = 12;
					break;
				case BAR_3M:
					realtimeBarNumSubBarsInFullBar = 36;
					break;
				case BAR_5M:
					realtimeBarNumSubBarsInFullBar = 60;
					break;
				case BAR_15M:
					realtimeBarNumSubBarsInFullBar = 180;
					break;
				case BAR_30M:
					realtimeBarNumSubBarsInFullBar = 360;
					break;
				case BAR_1H:
					realtimeBarNumSubBarsInFullBar = 720;
					break;
				case BAR_2H:
					realtimeBarNumSubBarsInFullBar = 1440;
					break;
				default:
					throw new Exception("Bar size not supported");
				}

				// Build contract
				Contract contract = new Contract();
				contract.m_conId = 0;
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(baseSymbol);
				String whatToShow = "";
				if (securityType.equals("CASH")) {
					whatToShow = "MIDPOINT";
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(baseSymbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(baseSymbol);
					contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				}
				else if (securityType.equals("FUT")) {
					whatToShow = "TRADES";
					contract.m_symbol = baseSymbol;
					contract.m_multiplier = IBConstants.FUTURE_SYMBOL_MULTIPLIER_HASH.get(baseSymbol);
					contract.m_expiry = expiry;
					contract.m_exchange = IBConstants.TICKER_EXCHANGE_HASH.get(baseSymbol);
				}
				contract.m_secType = securityType;
				
				// Need to make this unique per ticker so I setup this hash
				int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(barKey);
				
//				int msToWait = 3000;
//				Calendar barEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), BAR_SIZE.BAR_15S);
//				long msToEndOfBar = barEnd.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
//				if (msToEndOfBar < msToWait) {
//					msToWait = (int)msToEndOfBar;
//				}
//				Thread.sleep(msToWait);
				
				// Only 5 sec real time bars are supported so I'll have to do post-processing to make my own size bars.
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") now starting realtime bars.");
				client.reqRealTimeBars(tickerID, contract, 5, whatToShow, false, new Vector<TagValue>());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancelRealtimeBars(BarKey bk) {
		try {
			ss.addMessageToDataMessageQueue("IB Client cancelRealtimeBars()");
			if (client.isConnected()) {
				// Need to make this unique per ticker so I setup this hash
				int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(bk);
				client.cancelRealTimeBars(tickerID);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downloadHistoricalBars(Calendar startDateTime, Calendar endDateTime, boolean regularTradingHoursOnly) {
		try {
			if (!client.isConnected()) {
				ss.addMessageToDataMessageQueue("IB Client not connected so attempting to connect before downloadHistoricalBars()");
				connect();
			}
			if (client.isConnected()) {
				String symbol = barKey.symbol;
				if (symbol.matches(".* \\d{6}")) {
					symbol = barKey.symbol.substring(0, barKey.symbol.indexOf(" "));
					expiry = barKey.symbol.substring(barKey.symbol.indexOf(" ") + 1);
				}
				
				// Build contract
				Contract contract = new Contract();
				contract.m_conId = 0;
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(symbol);
				String whatToShow = "";
				if (securityType.equals("CASH")) {
					whatToShow = "MIDPOINT";
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(symbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(symbol);
					contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				}
				else if (securityType.equals("FUT")) {
					whatToShow = "TRADES";
					contract.m_symbol = symbol;
					contract.m_multiplier = IBConstants.FUTURE_SYMBOL_MULTIPLIER_HASH.get(symbol);
					contract.m_expiry = expiry;
					contract.m_includeExpired = true;
					contract.m_exchange = IBConstants.TICKER_EXCHANGE_HASH.get(symbol);
				}
				contract.m_secType = securityType;

				switch (barKey.duration) {
					case BAR_30S:
						this.barSeconds = 30;
						break;
					case BAR_1M:
						this.barSeconds = 60;
						break;
					case BAR_3M:
						this.barSeconds = 180;
						break;
					case BAR_5M:
						this.barSeconds = 300;
						break;
					case BAR_15M:
						this.barSeconds = 900;
						break;
					case BAR_30M:
						this.barSeconds = 1800;
						break;
					case BAR_1H:
						this.barSeconds = 3600;
						break;
					case BAR_2H:
						this.barSeconds = 7200;
						break;
					default:
						break;
				}

				long periodMS = endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis();
				long periodS = periodMS / 1000;

				int requestCounter = 0;
				if (periodS > 60 * 60 * 24 * 1) { // More than 28 days of data.  Will have to make multiple requests.
					while (startDateTime.getTimeInMillis() < endDateTime.getTimeInMillis()) {
						String durationString = "7 D";
//						String durationString = "1 M";

						Calendar thisEndDateTime = Calendar.getInstance();
						thisEndDateTime.setTimeInMillis(startDateTime.getTimeInMillis());
						thisEndDateTime.add(Calendar.DATE, 1);
						String endDateTimeString = Formatting.sdfYYYYMMDD_HHMMSS.format(thisEndDateTime.getTime());

						// System.out.println(startDateTime.getTime().toString());
						client.reqHistoricalData(requestCounter++, contract, endDateTimeString, durationString,
								IBConstants.BAR_DURATION_IB_BAR_SIZE.get(barKey.duration), whatToShow,
								(regularTradingHoursOnly ? 1 : 0), 1, new Vector<TagValue>());
	
						// Wait half a sec to avoid pacing violations and set the timeframe forward "one duration".
						Thread.sleep(3000);
						
						startDateTime.add(Calendar.DATE, 1);
					}
				} 
				else { // Less than a day of data. Can do everything in one request
//					long durationS = (long)(endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis()) / 1000;
//					String durationString = "" + (durationS) + " S"; // I think I had this for forex
					String durationString = "1 D";
					
//					Calendar thisEndDateTime = Calendar.getInstance();
//					thisEndDateTime.setTimeInMillis(startDateTime.getTimeInMillis());
//					thisEndDateTime.add(Calendar.SECOND, (int)durationS);
//					thisEndDateTime = CalendarUtils.getBarEnd(thisEndDateTime, barKey.duration);
					String endDateTimeString = Formatting.sdfYYYYMMDD_HHMMSS.format(endDateTime.getTime());

					// System.out.println(startDateTime.getTime().toString());
					client.reqHistoricalData(requestCounter++, contract, endDateTimeString, durationString,
							IBConstants.BAR_DURATION_IB_BAR_SIZE.get(barKey.duration), whatToShow,
							(regularTradingHoursOnly ? 1 : 0), 1, new Vector<TagValue>());
				}

				Thread.sleep(10000);

				// We've downloaded all the data. Add in the change & gap values and return it
				Double previousClose = null;

				synchronized(this.historicalBars) {
					
					Collections.sort(this.historicalBars, new BarComparator());
					
					for (Bar bar : this.historicalBars) { // Oldest to newest
						Double change = null;
						Double gap = null;
						if (previousClose != null) {
							change = bar.close - previousClose;
							gap = bar.open - previousClose;
						}
						if (change != null) {
							bar.change = Double.parseDouble(Formatting.df6.format(change));
						}
						if (gap != null) {
							bar.gap = Double.parseDouble(Formatting.df6.format(gap));
						}
						// If this is the first historical bar, see if we can find the previous bar in the DB so we can calculate change & gap
						if (change == null || gap == null) {
							Bar previousBar = QueryManager.getMostRecentBar(barKey, bar.periodStart);
							if (previousBar != null) {
								if (change == null) {
									bar.change = new Double(Formatting.df6.format(bar.close - previousBar.close));
								}
								if (gap == null) {
									bar.gap = new Double(Formatting.df6.format(bar.open - previousBar.close));
								}
							}
						}
	
						if (this.historicalBars.indexOf(bar) == this.historicalBars.size() - 1) {
							bar.partial = true;
						}
						previousClose = bar.close;
					}
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void placeOrder(int orderID, Integer ocaGroup, OrderType orderType, ORDER_ACTION orderAction, int quantity,
			Double stopPrice, Double limitPrice, boolean allOrNone, Calendar goodTill) {
		try {
			// Build contract
			Contract contract = new Contract();
//			contract.m_conId = 0; // Possibly caused an Error 200, No security definition has been found for the request.
			String baseSymbol = barKey.symbol;
			String symbolExpiry = barKey.symbol;
			if (baseSymbol.contains(" ")) {
				baseSymbol = barKey.symbol.substring(0, baseSymbol.indexOf(" "));
				symbolExpiry = barKey.symbol.replace(baseSymbol, "").trim();
			}
			String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(baseSymbol);
			if (securityType.equals("CASH")) {
				contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(barKey.symbol);
				contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(barKey.symbol);
				contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
			}
			else if (securityType.equals("FUT")) {
				contract.m_symbol = baseSymbol;
				contract.m_expiry = symbolExpiry;
				contract.m_exchange = IBConstants.TICKER_EXCHANGE_HASH.get(baseSymbol);
			}
			contract.m_secType = securityType;
			
			// Build order
			Order order = new Order();
			order.m_action = orderAction.toString();
			order.m_orderType = orderType.toString();
			order.m_totalQuantity = quantity;
			if (stopPrice != null) {
				order.m_auxPrice = stopPrice;
			} 
			else {
				order.m_auxPrice = 0;
			}
			if (limitPrice != null) {
				order.m_lmtPrice = limitPrice;
			} 
			else {
				order.m_lmtPrice = 0;
			}
			order.m_allOrNone = allOrNone;
			if (goodTill != null) {
				order.m_goodTillDate = Formatting.sdfYYYYMMDD_HHMMSS.format(goodTill.getTime());
			} 
			else {
				order.m_goodTillDate = "";
			}
			if (ocaGroup != null) {
				order.m_ocaGroup = ocaGroup.toString();
				order.m_ocaType = 1; // 1 = Cancel all remaining orders in group
			}
//			order.m_outsideRth = true; // I think this results in an Error that doesn't really do anything - it doesn't seem to be used for forex
			order.m_tif = "GTD"; // Time In Force. Values are DAY, GTC, IOC, GTD
			order.m_transmit = true;
			order.m_triggerMethod = 2; // For Stop type orders. 2 = Based on last price

			// Place Order
			if (!client.isConnected()) {
				ss.addMessageToDataMessageQueue("IB Client not connected so attempting to connect before placeOrder()");
				connect();
			}
			if (client.isConnected()) {
				client.placeOrder(orderID, contract, order);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void preloadRealtimeBarWithLastHistoricalBar() {
		if (historicalBars.size() > 1) {

			Bar newestHistoricalBar = historicalBars.get(historicalBars.size() - 1);
			realtimeBarOpen = newestHistoricalBar.open;
			realtimeBarClose = newestHistoricalBar.close;
			realtimeBarHigh = newestHistoricalBar.high;
			realtimeBarLow = newestHistoricalBar.low;
			realtimeBarVolume = newestHistoricalBar.volume;

			Bar secondNewestHistoricalBar = historicalBars.get(historicalBars.size() - 2);
			realtimeBarLastBarClose = secondNewestHistoricalBar.close;

			// System.out.println("NHB Open: " + newestHistoricalBar.open);
			// System.out.println("NHB Close: " + newestHistoricalBar.close);
			// System.out.println("NHB High: " + newestHistoricalBar.high);
			// System.out.println("NHB Low: " + newestHistoricalBar.low);
			// System.out.println("NHB Volume: " + newestHistoricalBar.volume);
			// System.out.println("NHB Start: " +
			// newestHistoricalBar.periodStart.getTime().toString());
			// System.out.println("NHB End: " +
			// newestHistoricalBar.periodEnd.getTime().toString());
			// System.out.println("NHB Partial: " +
			// newestHistoricalBar.partial);
			// System.out.println("SHB Open: " +
			// secondNewestHistoricalBar.open);
			// System.out.println("SHB Close: " +
			// secondNewestHistoricalBar.close);
		}
	}

	public HashMap<String, LinkedList<HashMap<String, Object>>> getEventDataHash() {
		return eventDataHash;
	}

	public void setEventDataHash(HashMap<String, LinkedList<HashMap<String, Object>>> eventDataHash) {
		this.eventDataHash = eventDataHash;
	}

	public BarKey getBarKey() {
		return barKey;
	}

	/*
	 ************************************************** RESPONSES **************************************************
	 */
	public void cancelOrder(int orderID) {
		client.cancelOrder(orderID);
	}

	@Override
	public void error(Exception e) {
		System.out.println("error(...)");
		e.printStackTrace();
	}

	@Override
	public void error(String str) {
		System.out.println("error (str) " + str);
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		// 201 	= Order rejected - reason:
		// 202  = Order cancelled - reason:
		// 2104 = Market data farm connection is OK
		// 2106 = HMDS data farm connection is OK
		// 2108 = Market data farm connection is inactive but should be available upon demand
		// 505  = Unknown message id
		if (errorCode != 2104 && errorCode != 2106 && errorCode != 2108 && errorCode != 202 && errorCode != 505) {
			System.out.println("Error " + id + ", " + errorCode + ", " + errorMsg);
			ss.addMessageToDataMessageQueue(errorMsg);
		}
		if (errorCode == 201) {
			// Package this data so the trading engine can act on it. Want to keep trading logic inside trading engine.
			HashMap<String, Object> dataHash = new HashMap<String, Object>();
			dataHash.put("errorCode", errorCode);
			dataHash.put("id", id);
			if (eventDataHash.get("error") == null) {
				eventDataHash.put("error", new LinkedList<HashMap<String, Object>>());
			}
			eventDataHash.get("error").add(dataHash);
		}
	}

	@Override
	public void connectionClosed() {
		System.out.println("connectionClosed()");
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		String tickType = TickType.getField(field);
		// System.out.println("tickPrice(...) " + tickType + " - " + price);
		if (price >= 0) {
			ibs.updateBKTickerData(barKey, tickType, price);
		}
		else {
			System.err.println("tickPrice(...) price is less than zero - " + field + ", " + price + ", " + Calendar.getInstance().getTime().toString());
		}
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		String tickType = TickType.getField(field);
		// System.out.println("tickSize(...) " + tickType + " - " + size);
		ibs.updateBKTickerData(barKey, tickType, (double) size);
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
		System.out.println("tickOptionComputation(...)");
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		System.out.println("tickGeneric(...)");
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
//		System.out.println("tickString(...)");
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		System.out.println("tickEFP(...)");
	}

	@Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		System.out.println("orderStatus(...) " + orderId + ", " + status + ", " + filled + ", " + avgFillPrice + ", " + parentId + ", " + whyHeld + ", " + Calendar.getInstance().getTime().toString());
		// Package this data so the trading engine can act on it. Want to keep trading logic inside trading engine.
		HashMap<String, Object> dataHash = new HashMap<String, Object>();
		dataHash.put("orderId", orderId);
		dataHash.put("status", status);
		dataHash.put("filled", filled);
		dataHash.put("remaining", remaining);
		dataHash.put("avgFillPrice", avgFillPrice);
		dataHash.put("permId", permId);
		dataHash.put("parentId", parentId);
		dataHash.put("lastFillPrice", lastFillPrice);
		dataHash.put("clientId", clientId);
		dataHash.put("whyHeld", whyHeld);
		if (eventDataHash.get("orderStatus") == null) {
			eventDataHash.put("orderStatus", new LinkedList<HashMap<String, Object>>());
		}
		eventDataHash.get("orderStatus").add(dataHash);
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
//		System.out.println("openOrder(...)");
	}

	@Override
	public void openOrderEnd() {
		System.out.println("openOrderEnd()");
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
//		System.out.println("updateAccountValue(...) " + key + " - " + value);
		ibs.updateAccountInfo(key, value);
	}

	@Override
	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
//		System.out.println("updatePortfolio(...)");
	}

	@Override
	public void updateAccountTime(String timeStamp) {
//		System.out.println("updateAccountTime(...) " + timeStamp); // Example - 07:35
		Calendar c = Calendar.getInstance();
		String[] pieces = timeStamp.split(":");
		String hour = pieces[0];
		String minute = pieces[1];
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
		c.set(Calendar.MINUTE, Integer.parseInt(minute));
		ibs.updateAccountInfo(IBConstants.ACCOUNT_TIME, c);
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		System.out.println("accountDownloadEnd(...)");
	}

	@Override
	public void nextValidId(int orderId) {
		System.out.println("nextValidId(...) - " + orderId);
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println("contractDetails(...)");
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println("bondContractDetails(...)");
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		System.out.println("contractDetailsEnd(...)");
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		System.out.println("execDetails(...) " + execution.m_orderId);
//		System.out.println("reqId " + reqId);
//		System.out.println("execution m_orderId " + execution.m_orderId);
//		System.out.println("execution m_avgPrice " + execution.m_avgPrice);
//		System.out.println("execution m_cumQty " + execution.m_cumQty);
//		System.out.println("execution m_side " + execution.m_side);
//		System.out.println("execution m_execId " + execution.m_execId);
		String orderType = IBQueryManager.getOrderIDType(execution.m_orderId);
		IBQueryManager.updateExecID(orderType, execution.m_orderId, execution.m_execId);
	}

	@Override
	public void execDetailsEnd(int reqId) {
		System.out.println("execDetailsEnd(...)");
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		System.out.println("updateMktDepth(...)");
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
			int size) {
		System.out.println("updateMktDepthL2(...)");
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		System.out.println("updateNewsBulletin(...)");
	}

	@Override
	public void managedAccounts(String accountsList) {
		System.out.println("managedAccounts(...)");
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		System.out.println("receiveFA(...)");
	}

	@Override
	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
		try {
			if (date == null || date.contains("finished")) {
				if (date == null) {
					System.out.println("historicalData got a null date");
				}
				lastProcessedRequestID++;
				System.out.println("Batch " + lastProcessedRequestID + " done");
				return;
			}
			
			String symbol = barKey.symbol;
			if (symbol.matches(".* \\d{6}")) {
				symbol = barKey.symbol.substring(0, barKey.symbol.indexOf(" "));
				expiry = barKey.symbol.substring(barKey.symbol.indexOf(" ") + 1);
			}
			
			String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(symbol);
			lastProcessedRequestID = reqId;

			Calendar periodStart = Calendar.getInstance();
			try {
				periodStart.setTimeInMillis(Formatting.sdfYYYYMMDD__HHMMSS.parse(date).getTime());
			}
			catch (Exception e) {
				System.err.println(e.getMessage());
				System.err.println(date);
				return;
			}
			
			// Round the periodStart to the actual bar start.  ex) When you get bullshit bars starting at 4:15 CST on sundays
			periodStart.setTimeInMillis(CalendarUtils.getBarStart(periodStart, barKey.duration).getTimeInMillis());
			
			Calendar periodEnd = Calendar.getInstance();
			periodEnd.setTime(periodStart.getTime());
			periodEnd.add(Calendar.SECOND, barSeconds);
			
			Double vwap = null;
			if (securityType.equals("CASH")) {
				volume = -1; // No volume on FOREX
			}
			if (securityType.equals("FUT")) {
				vwap = (double)WAP;
			}

			// We'll fill in the change & gap later;
			Bar bar = new Bar(barKey.symbol, new Double(Formatting.df6.format(open)), new Double(Formatting.df6.format(close)),
					new Double(Formatting.df6.format(high)), new Double(Formatting.df6.format(low)), vwap, volume, null, null, null, periodStart, periodEnd, barKey.duration, false);
			synchronized(this.historicalBars) {
				if (!this.historicalBars.contains(bar)) {
					this.historicalBars.add(bar);
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scannerParameters(String xml) {
		System.out.println("scannerParameters(...)");
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		System.out.println("scannerData(...)");
	}

	@Override
	public void scannerDataEnd(int reqId) {
		System.out.println("scannerDataEnd(...)");
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
//		System.out.println("realtimeBar(...)");
//		System.out.println(close);
		try {
			Calendar c_m5 = Calendar.getInstance();
			c_m5.setTimeInMillis(time * 1000); // The subBar start (5s ago)
			Calendar c_m0 = Calendar.getInstance();
			c_m0.setTimeInMillis(time * 1000);
			c_m0.add(Calendar.SECOND, 5); // This should be virtually "now"
			
			String baseSymbol = barKey.symbol;
			if (baseSymbol.matches(".* \\d{6}")) {
				baseSymbol = barKey.symbol.substring(0, barKey.symbol.indexOf(" "));
			}
			
//			System.out.println("realtimeBar start " + c_m5.getTime().toString());
//			System.out.println("realtimeBar end   " + c_m0.getTime().toString());
			
			Calendar tempBarStart = CalendarUtils.getBarStart(c_m0, barKey.duration);
//			c_m0.set(Calendar.MILLISECOND, 0);
//			if (tempBarStart.getTimeInMillis() != c_m0.getTimeInMillis()) {
//				System.err.println("Not handling the bar correctly!");
//				System.err.println(Calendar.getInstance().getTime().toString() + ", " + c_m0.getTime().toString());
//			}

			if (fullBarStart == null) {
				fullBarStart = CalendarUtils.getBarStart(c_m5, barKey.duration);
				fullBarEnd = CalendarUtils.getBarEnd(c_m5, barKey.duration);
				return;
			}

			// Same bar
			if (fullBarStart.getTimeInMillis() == tempBarStart.getTimeInMillis()) {
//				System.out.println("Same Bar");
//				System.out.println("fullBarStart = " + fullBarStart.getTime().toString());
//				System.out.println("Current time is = " + Calendar.getInstance().getTime().toString());
				
				if (high > realtimeBarHigh) {
					realtimeBarHigh = (float) high;
				}
				if (low < realtimeBarLow) {
					realtimeBarLow = (float) low;
				}
				realtimeBarVolume += volume;

				realtimeBarSubBarCounter++;

				Calendar tempBarEnd = Calendar.getInstance();
				tempBarEnd.setTimeInMillis(fullBarStart.getTimeInMillis());
				tempBarEnd.add(Calendar.SECOND, 5);
				if (fullBarStart.getTimeInMillis() == CalendarUtils.getBarStart(tempBarEnd, barKey.duration).getTimeInMillis()) {
					// Last sub-bar in the bar
					realtimeBarClose = (float) close;
				}

				// Interim partial bar for the DB
				double gap = new Float(Formatting.df6.format(realtimeBarOpen - realtimeBarLastBarClose));
				double change = new Float(Formatting.df6.format(realtimeBarClose - realtimeBarLastBarClose));
				double vwap = new Float(Formatting.df6.format((realtimeBarOpen + realtimeBarClose + realtimeBarHigh + realtimeBarLow) / 4d));
				Bar bar = new Bar(barKey.symbol, realtimeBarOpen, realtimeBarClose, realtimeBarHigh, realtimeBarLow, vwap, realtimeBarVolume, null, change, gap, fullBarStart, fullBarEnd, barKey.duration, true);
				QueryManager.insertOrUpdateIntoBar(bar);
				
				// Interim futures bar - stitch the dated contracts into a continuous contract
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(baseSymbol);
				if (securityType.equals("FUT")) {
					FuturesStitcher.processOneBar(baseSymbol, barKey.duration, fullBarStart);
					bar.symbol = baseSymbol; // So metrics get calculated on the continuous contract instead of the dated one.
				}
				
				// System.out.println("----- PARTIAL BAR -----");
				// System.out.println(bar.toString());
				// System.out.println("---------- END --------");
				ibs.setRealtimeBar(bar);
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") received and processed realtime bar data.");
			} 
			// New bar
			else {
				System.out.println("IBWorker completes bar at " + Calendar.getInstance().getTime().toString());
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(baseSymbol);
				
				if (!firstRealtimeBarCompleted) {
					// If historical data ended on one bar, and the realtime data started on the next bar, the last historical data one would be partial, and needs to be set as complete.
					// System.out.println("Setting most recent bars complete");
					QueryManager.setMostRecentBarsComplete(barKey);
					
					// Bar is done, so stitch the dated contracts into a continuous contract
					if (securityType.equals("FUT")) {
						FuturesStitcher.processOneBar(baseSymbol, barKey.duration, fullBarStart);
					}
				}
				firstRealtimeBarCompleted = true;

				Calendar lastBarStart = Calendar.getInstance();
				lastBarStart.setTimeInMillis(fullBarStart.getTimeInMillis());

				fullBarStart.setTimeInMillis(tempBarStart.getTimeInMillis());
				fullBarEnd = CalendarUtils.getBarEnd(fullBarStart, barKey.duration);

				// System.out.println("fullBarStart: " + fullBarStart.getTime().toString());
				// System.out.println("fullBarEnd: " + fullBarEnd.getTime().toString());

				Calendar lastBarEnd = Calendar.getInstance();
				lastBarEnd.setTimeInMillis(fullBarStart.getTimeInMillis());

				double gap = new Float(Formatting.df6.format(realtimeBarOpen - realtimeBarLastBarClose));
				double change = new Float(Formatting.df6.format(realtimeBarClose - realtimeBarLastBarClose));
				double vwap = new Float(Formatting.df6.format((realtimeBarOpen + realtimeBarClose + realtimeBarHigh + realtimeBarLow) / 4d));

				Bar bar = new Bar(barKey.symbol, realtimeBarOpen, realtimeBarClose, realtimeBarHigh, realtimeBarLow,
						vwap, realtimeBarVolume, null, change, gap, lastBarStart, lastBarEnd, barKey.duration, false);
				QueryManager.insertOrUpdateIntoBar(bar);
				
				if (securityType.equals("FUT")) {
					bar.symbol = baseSymbol; // So metrics get calculated on the continuous contract instead of the dated one.
				}
				
				ibs.setRealtimeBar(bar);
				ibs.setCompleteBar(bar);
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") received and processed realtime bar data. " + barKey.duration + " bar complete.");

				realtimeBarLastBarClose = realtimeBarClose;
				realtimeBarOpen = (float) open;
				realtimeBarClose = (float) close;
				realtimeBarHigh = (float) high;
				realtimeBarLow = (float) low;
				realtimeBarVolume = (float) volume;
				realtimeBarSubBarCounter = 1;
			}
			
			// System.out.println(c.getTime().toString());
			// System.out.println(open + ", " + close + ", " + high + ", " + low);
			// System.out.println(reqId + ", " + volume + ", " + wap + ", " + count);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void currentTime(long time) {
		System.out.println("currentTime(...)");
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		System.out.println("fundamentalData(...)");
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		System.out.println("deltaNeutralValidation(...)");
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		System.out.println("tickSnapshotEnd(...)");
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		System.out.println("marketDataType(...)");
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		System.out.println("commissionReport(...) " + commissionReport.m_commission + ", " + commissionReport.m_currency
				+ ", " + commissionReport.m_execId + ", " + commissionReport.m_realizedPNL);
		 
		// Package this data so the trading engine can act on it. Want to keep trading logic inside trading engine.
		HashMap<String, Object> dataHash = new HashMap<String, Object>();
		dataHash.put("commission", commissionReport.m_commission);
		dataHash.put("execId", commissionReport.m_execId);
		if (eventDataHash.get("commissionReport") == null) {
			eventDataHash.put("commissionReport", new LinkedList<HashMap<String, Object>>());
		}
		eventDataHash.get("commissionReport").add(dataHash);
	}

	@Override
	public void position(String account, Contract contract, int pos, double avgCost) {
		System.out.println("position(...)");
	}

	@Override
	public void positionEnd() {
		System.out.println("positionEnd(...)");
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		System.out.println("accountSummary(...)");
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		System.out.println("accountSummaryEnd(...)");
	}

	@Override
	public void verifyMessageAPI(String apiData) {
		System.out.println("verifyMessageAPI(...)");
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		System.out.println("verifyCompleted(...)");
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		System.out.println("displayGroupList(...)");
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		System.out.println("displayGroupUpdated(...)");
	}
}
package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.downloaders.okcoin.OKCoinConstants;
import dbio.QueryManager;
import utils.CalendarUtils;
import utils.StringUtils;

public class NIAListener {

	public void onReceive(String msg) {
		try {
//			System.out.println(msg);
			
			Gson gson = new Gson();
			Object messageObject = gson.fromJson(msg, Object.class);
			if (messageObject instanceof ArrayList<?>) {
				ArrayList<LinkedTreeMap<String, Object>> messageList = gson.fromJson(msg, ArrayList.class);

				NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
				niass.noteActivity();
				
				for (LinkedTreeMap<String, Object> message : messageList) {
					String channel = message.get("channel").toString();
					
					// Price API
					if (channel.equals("ok_btccny_ticker")) {
						processTick(message);
					}
					else if (channel.contains("kline")) {
						processBar(message);
					}
					else if (channel.equals("ok_btccny_depth")) {
						processOrderBook(message);
					}
					// Trade API
					synchronized (niass.getRequestedTradeLock()) {
						if (channel.equals("ok_spotcny_trade")) {
							processTrade(message);
						}
						else if (channel.equals("ok_spotcny_cancel_order")) {
							processCancelOrder(message);
						}
						else if (channel.equals("ok_spotcny_userinfo")) {
							processUserInfo(message);
						}
						else if (channel.equals("ok_cny_realtrades")) {
							processRealTrades(message);
						}
						else if (channel.equals("ok_spotcny_order_info")) {
							processOrderInfo(message);
						}
					}
				}
				if (messageList == null || messageList.size() == 0) {
					System.out.println("." + msg);
				}
			}
			else {
				// {'event':'pong'} probably
				System.out.println(msg);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processTrade(LinkedTreeMap<String, Object> message) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>)message.get("data");
			if (ltm != null) {
//				long exchangeOrderID = StringUtils.getRegularLong(ltm.get("order_id").toString());
//				boolean success = Boolean.parseBoolean(ltm.get("result").toString());
//				
//				if (success) {
//					// This is what I need to figure out.
//					Integer tempID = null; 
//					String tradeType = null;
//					
//					System.out.println("processTrade(...) - exchangeOrderID " + exchangeOrderID + ".  Let's see what this is...");
//					Object[] nextRequestedTrade = QueryManager.getNextRequestedTrade();
//					if (nextRequestedTrade != null && nextRequestedTrade.length > 0) {
//						tempID = Integer.parseInt(nextRequestedTrade[0].toString());
//						tradeType = nextRequestedTrade[1].toString(); // Open Requested, Close Requested, Stop Requested, Expiration Requested
//						tradeType = tradeType.replace(" Requested", "");
//					}
//					else {
//						System.err.println("processTrade(...) - Couldn't even find a next requested trade!");
//					}
//					
//					// Assuming we know what it is...
//					if (tempID != null && tradeType != null) {
//						if (tradeType.equals("Open")) {
//							System.out.println("processTrade(...) sees " + exchangeOrderID + " on Open");
//						}
//						else if (tradeType.equals("Close")) {
//							System.out.println("processTrade(...) sees " + exchangeOrderID + " on CLOSE");
//						}
//						else if (tradeType.equals("Stop")) {
//							System.out.println("processTrade(...) sees " + exchangeOrderID + " on STOP");
//						}
//						else if (tradeType.equals("Expiration")) {
//							System.out.println("processTrade(...) sees " + exchangeOrderID + " on EXPIRATION");
//						}
//					}
//					else {
//						System.err.println("processTrade(...) - Couldn't figure out order at all.");
//					}
//				}
//				else {
//					System.err.println("processTrade(...) - Got a unsuccessful notice");
//				}
			}
			else {
				System.out.println("processTrade(...) - AN ERROR OCCURED...");
				String errorCode = message.get("errorcode").toString();
				if (errorCode.equals("10002")) {
					System.err.println("processTrade(...) - Authentication Problem");
				}
				else if (errorCode.equals("10009")) {
					System.err.println("processTrade(...) - Order Does Not Exist");
				}
				else if (errorCode.equals("10010")) {
					System.out.println("processTrade(...) - Insufficient Funds");
				}
				else if (errorCode.equals("10011")) {
					System.err.println("processTrade(...) - Order Quantity Too Low");
				}
				else if (errorCode.equals("10016")) {
					System.err.println("processTrade(...) - Insufficient Coins");
				}
				else {
					System.err.println("processTrade(...) - " + errorCode);
				}
				
				System.out.println("processTrade(...) - Searching for last requested trade and removing it...");
				Integer tempID = null; 
				String tradeType = null;
				
				Object[] nextRequestedTrade = QueryManager.getNextRequestedTrade();
				if (nextRequestedTrade != null && nextRequestedTrade.length > 0 && nextRequestedTrade[0] != null && nextRequestedTrade[1] != null) {
					tempID = Integer.parseInt(nextRequestedTrade[0].toString());
					tradeType = nextRequestedTrade[1].toString(); // Open Requested, Close Requested, Stop Requested, Expiration Requested
					tradeType = tradeType.replace(" Requested", "");
					
					if (tradeType.equals("Open")) {
						System.out.println("processTrade(...) - Cancelling " + tempID + " Open");
						QueryManager.cancelOpenTradeRequest(tempID);
					}
					else if (tradeType.equals("Close")) {
						System.out.println("processTrade(...) - Cancelling " + tempID + " Close");
						QueryManager.cancelCloseTradeRequest(tempID);
					}
					else if (tradeType.equals("Stop")) {
						System.out.println("processTrade(...) - Cancelling " + tempID + " Stop");
						QueryManager.cancelStopTradeRequest(tempID);
					}
					else if (tradeType.equals("Expiration")) {
						System.out.println("processTrade(...) - Cancelling " + tempID + " Expiration");
						QueryManager.cancelExpirationTradeRequest(tempID);
					}
				}
				else {
					System.err.println("processTrade(...) - Couldn't even find the next requested trade!");
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processRealTrades(LinkedTreeMap<String, Object> message) {
		try {
			Object oData = message.get("data");
			if (oData != null) {
				LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>)oData;
				if (ltm != null) { 
					long exchangeOrderID = StringUtils.getRegularLong(ltm.get("orderId").toString());
					int iStatus = (int)Double.parseDouble(ltm.get("status").toString()); // -1: Cancelled, 0: Pending, 1: Partially Filled, 2: Filled, 4: Cancel Request In Progress
					double amount = Double.parseDouble(ltm.get("tradeAmount").toString());
					double filledAmount = Double.parseDouble(ltm.get("completedTradeAmount").toString());
					double price = Double.parseDouble(ltm.get("tradePrice").toString()); // This is the CNY value
					double unitPrice = Double.parseDouble(ltm.get("tradeUnitPrice").toString()); // Unit price is CNY price
					long timestamp = StringUtils.getRegularLong(ltm.get("createdDate").toString());
					String symbol = ltm.get("symbol").toString();
					String type = ltm.get("tradeType").toString(); // buy, sell, buy_market, sell_market
	
					String status = "";
					if (iStatus == -1) {
						status = "Cancelled";
						return;
					}
					else if (iStatus == 0) {
						status = "Pending";
					}
					else if (iStatus == 1) {
						status = "Partially Filled";
					}
					else if (iStatus == 2) {
						status = "Filled";
					}
					else if (iStatus == 4) {
						status = "Cancel Request In Progress";
						return;
					}
					
					System.out.println("processRealTrades(...) " + exchangeOrderID + ", " + status + ", " + type + " " + amount + ", " + filledAmount + ", " + price + ", " + unitPrice);
					
					processTradeInfo(status, exchangeOrderID, timestamp,  unitPrice, filledAmount);
				} 
				else {
					System.out.println("processRealTrades(...) Don't know what oData is - " + oData.toString());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processCancelOrder(LinkedTreeMap<String, Object> message) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			
			LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>)message.get("data");
			if (ltm != null) {
				long exchangeOrderID = StringUtils.getRegularLong(ltm.get("order_id").toString());
				boolean success = Boolean.parseBoolean(ltm.get("result").toString());
				
				if (success) {
					Integer tempID = null; 
					String tradeType = null;

					// First see if it is in the DB.
					HashMap<String, Object> results = QueryManager.figureOutExchangeIdTradeType(exchangeOrderID);
					if (results.size() > 0) {
						System.out.println("processCancelOrder(...) - exchangeOrderID " + exchangeOrderID + " was found in the DB");
						tradeType = results.get("type").toString(); // Open, Close, Stop, Expiration
						tempID = Integer.parseInt(results.get("tempid").toString());
					}
					else {
						System.err.println("processCancelOrder(...) - Couldn't even find a next requested trade!");
					}
					
					// Assuming we know what it is, cancel accordingly
					if (tempID != null && tradeType != null) {
						if (tradeType.equals("Open")) {
							System.out.println("processCancelOrder(...) - Cancelling Open " + tempID);
							QueryManager.cancelOpenOrder(tempID);
						}
						else if (tradeType.equals("Close")) {
							// This will either set it to Cancelled if the close was totally filled (shouldn't happen?) or set it back to Open Filled if the close was partially filled or not filled.
							System.out.println("processCancelOrder(...) - Cancelling Close " + tempID);
							QueryManager.cancelCloseOrder(tempID);
						}
						else if (tradeType.equals("Stop")) {
							// This will either set it to Cancelled if the close was totally filled (shouldn't happen?) or set it back to null if the stop was partially filled.  monitorClose(...) should pick it up again for a replacement order
							System.out.println("processCancelOrder(...) - Cancelling Stop " + tempID);
							QueryManager.cancelStopOrder(tempID);
						}
						else if (tradeType.equals("Expiration")) {
							System.out.println("processCancelOrder(...) - Cancelling Expiration " + tempID);
							QueryManager.cancelExpirationOrder(tempID);
						}
					}
					else {
						System.out.println("processCancelOrder(...) - Cant figure out " + exchangeOrderID);
					}
				}
			}
			else {
				String errorCode = message.get("errorcode").toString();
				String success = message.get("success").toString();
				if (errorCode.equals("10002")) {
					System.err.println("processCancelOrder(...) - Authentication Problem");
				}
				else if (errorCode.equals("10009")) {
					System.err.println("processCancelOrder(...) - Order Does Not Exist");
					System.out.println(message.toString());
				}
				else if (errorCode.equals("10010")) {
					System.err.println("processCancelOrder(...) - Insufficient Funds");
				}
				else if (errorCode.equals("10011")) {
					System.err.println("processCancelOrder(...) - Order Quantity Too Low");
				}
				else {
					System.err.println("procesCancelOrder(...) - " + errorCode);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processOrderInfo(LinkedTreeMap<String, Object> message) {
		try { 
			Object oData = message.get("data");
			if (oData == null) {
				String errorCode = message.get("errorcode").toString();
				String success = message.get("success").toString();
				if (errorCode.equals("20024")) {
					System.out.println("Signature Does Not Match");
				}
			}
			else {
				LinkedTreeMap<String, Object> data = (LinkedTreeMap<String, Object>)oData;
				if (data != null) {
					ArrayList<Object> orders = (ArrayList<Object>)data.get("orders");
					if (orders != null) {
						for (Object oOrder : orders) {
							LinkedTreeMap<String, Object> order = (LinkedTreeMap<String, Object>)oOrder;
							double amount = new Double(order.get("amount").toString());
							double filledAmount = new Double(order.get("deal_amount").toString());
							double price = new Double(order.get("price").toString());
							String symbol = order.get("symbol").toString();  
							String type = order.get("type").toString(); // buy, sell, buy_market, sell_market
							long timestamp = StringUtils.getRegularLong(order.get("create_date").toString()); 
							long exchangeOrderID = StringUtils.getRegularLong(order.get("order_id").toString());
							int iStatus = (int)Double.parseDouble(order.get("status").toString()); // -1: Cancelled, 0: Pending, 1: Partially Filled, 2: Filled, 4: Cancel Request In Progress
							String status = "";
							if (iStatus == -1) {
								status = "Cancelled";
								continue;
							}
							else if (iStatus == 0) {
								status = "Pending";
							}
							else if (iStatus == 1) {
								status = "Partially Filled";
							}
							else if (iStatus == 2) {
								status = "Filled";
							}
							else if (iStatus == 4) {
								status = "Cancel Request In Progress";
								continue;
							}
							
							System.out.println("processOrderInfo(...) " + exchangeOrderID + ", " + type + " " + status + ", " + amount + ", " + filledAmount + ", " + price);
							
							processTradeInfo(status, exchangeOrderID, timestamp, price, filledAmount);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processUserInfo(LinkedTreeMap<String, Object> message) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			
			LinkedTreeMap<String, Object> data = (LinkedTreeMap<String, Object>)message.get("data");
			if (data != null) {
				LinkedTreeMap<String, Object> info = (LinkedTreeMap<String, Object>)data.get("info");
				if (info != null) {
					LinkedTreeMap<String, Object> funds = (LinkedTreeMap<String, Object>)info.get("funds");
					if (funds != null) {
						LinkedTreeMap<String, Object> free = (LinkedTreeMap<String, Object>)funds.get("free");
						double btc = new Double(free.get("btc").toString());
						double ltc = new Double(free.get("ltc").toString());
						double cny = new Double(free.get("cny").toString());
						
						niass.setBtcOnHand(btc);
						niass.setLtcOnHand(ltc);
						niass.setCnyOnHand(cny);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processOrderBook(LinkedTreeMap<String, Object> message) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			
			String channel = message.get("channel").toString();
			String channelPrefix = channel.replace("depth", "");
			String symbol = OKCoinConstants.WEBSOCKET_PREFIX_TO_TICK_SYMBOL_HASH.get(channelPrefix);
			
			LinkedTreeMap<String, Object> data = (LinkedTreeMap<String, Object>)message.get("data");
			if (data != null) {
				ArrayList<ArrayList<Double>> bids = (ArrayList<ArrayList<Double>>)data.get("bids");
				ArrayList<ArrayList<Double>> asks = (ArrayList<ArrayList<Double>>)data.get("asks");
				
				niass.putSymbolBidOrderBook(symbol, bids);
				niass.putSymbolAskOrderBook(symbol, asks);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processTick(LinkedTreeMap<String, Object> message) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			
			String channel = message.get("channel").toString();
			String symbol = OKCoinConstants.WEBSOCKET_SYMBOL_TO_TICK_SYMBOL_HASH.get(channel);
			LinkedTreeMap<String, String> data = (LinkedTreeMap<String, String>)message.get("data");
			HashMap<String, String> tickerDataHash = new HashMap<String, String>();
			
			tickerDataHash.putAll(data);
			niass.putSymbolTickerDataHash(symbol, tickerDataHash);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processBar(LinkedTreeMap<String, Object> message) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			
			ArrayList<Object> data = (ArrayList<Object>)message.get("data");
			
			String channel = message.get("channel").toString();
			String channelMinusDuration = channel.substring(0, channel.lastIndexOf("_"));
			String prefix = channelMinusDuration.substring(0, channelMinusDuration.lastIndexOf("_") + 1);
			String symbol = OKCoinConstants.WEBSOCKET_PREFIX_TO_TICK_SYMBOL_HASH.get(prefix);
		
			String channelDuration = channel.substring(channel.lastIndexOf("_") + 1);
			BAR_SIZE duration = OKCoinConstants.OKCOIN_BAR_DURATION_TO_BAR_SIZE_HASH.get(channelDuration);
			
			ArrayList<Bar> bars = new ArrayList<Bar>();
			
			// When the data is the bar itself, not a list of bars
			if (data.get(0) != null && !(data.get(0) instanceof ArrayList<?>)) {
				ArrayList<Object> tempBar = new ArrayList<Object>();
				tempBar.addAll(data);
				data.clear();
				data.add(tempBar);
			}
			
			// These go oldest to newest
			for (int a = 0; a < data.size(); a++) {
				if (data.get(a) instanceof ArrayList<?>) {
					ArrayList<Object> barJSON = (ArrayList<Object>)data.get(a);
					
					double timeMS = (double)barJSON.get(0);
					double open = (double)barJSON.get(1);
					double high = (double)barJSON.get(2);
					double low = (double)barJSON.get(3);
					double close = (double)barJSON.get(4);
					double volume = (double)barJSON.get(5);
					double vwap = (open + high + low + close) / 4d;
					
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis((long)timeMS);
					Calendar barStart = CalendarUtils.getBarStart(c, duration);
					Calendar barEnd = CalendarUtils.getBarEnd(c, duration);
					
					Bar mostRecentBarInDB = QueryManager.getMostRecentBar(new BarKey(symbol, duration), barStart);
					Double change = null;
					Double gap = null;
					if (mostRecentBarInDB != null) {
						change = (double)close - mostRecentBarInDB.close;
						gap = (double)open - mostRecentBarInDB.close;
					}
					
					boolean partial = false;
					if (data.size() == 1) {
						partial = true;
					}
					if (a == data.size() - 1 && data.size() > 1) {
						partial = true;
					}

					Bar bar = new Bar(symbol, open, close, high, low, vwap, volume, null, change, gap, barStart, barEnd, duration, partial);
					bars.add(bar);
				}
			}

			niass.addLatestBars(bars);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processTradeInfo(String status, long exchangeOrderID, long timestamp, double unitPrice, double filledAmount) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			
			// Now I need to get this trade from the DB and update it.  I'm not sure if the websockets are threaded in a way that could do this, but I cannot allow concurrency here.
			Integer tempID = null; 
			String tradeType = null;
			
			// First see if it is in the DB.
			HashMap<String, Object> results = QueryManager.figureOutExchangeIdTradeType(exchangeOrderID);
			if (results.size() > 0) {
				System.out.println("processTradeInfo(...) - exchangeOrderID " + exchangeOrderID + " was found in the DB");
				tradeType = results.get("type").toString(); // Open, Close, Stop, Expiration
				tempID = Integer.parseInt(results.get("tempid").toString());
			}
			
			// We didn't find it in the DB - get the next requested trade from the DB.  This should be it.
			else {
				System.out.println("processTradeInfo(...) - exchangeOrderID " + exchangeOrderID + " was not found in the DB!");
				Object[] nextRequestedTrade = QueryManager.getNextRequestedTrade();
				if (nextRequestedTrade != null && nextRequestedTrade.length > 0 && nextRequestedTrade[0] != null && nextRequestedTrade[1] != null) {
					tempID = Integer.parseInt(nextRequestedTrade[0].toString());
					tradeType = nextRequestedTrade[1].toString(); // Open Requested, Close Requested, Stop Requested, Expiration Requested
					tradeType = tradeType.replace(" Requested", "");
				}
				else {
					System.err.println("processTradeInfo(...) - Couldn't even find a next requested trade!");
				}
			}
			
			// Assuming we know what it is, process accordingly.
			if (tempID != null && tradeType != null) {
				if (tradeType.equals("Open")) {
					QueryManager.updateMostRecentOpenTradeWithExchangeData(tempID, exchangeOrderID, timestamp, unitPrice, filledAmount, "Open " + status);
					System.out.println("processTradeInfo(...) processing " + exchangeOrderID + " on Open " + status);
				}
				else if (tradeType.equals("Close")) {
					if (status.equals("Pending") || status.equals("Partially Filled")) {
						status = "Close " + status;
					}
					else if (status.equals("Filled")) {
						status = "Closed";
						// Cancel any exchange orders based on this tempid
						niass.cancelOrders(QueryManager.getExchangeOrders(exchangeOrderID, tempID));
					}
					QueryManager.updateMostRecentCloseTradeWithExchangeData(tempID, exchangeOrderID, timestamp, unitPrice, filledAmount, status);
					System.out.println("processTradeInfo(...) processing " + exchangeOrderID + " on CLOSE " + status);
				}
				else if (tradeType.equals("Stop")) {
					String stopStatus = null;
					if (status.equals("Pending") || status.equals("Partially Filled")) {
						stopStatus = "Stop " + status;
					}
					else if (status.equals("Filled")) {
						stopStatus = "Closed";
						status = "Closed";
						// Cancel any exchange orders based on this tempid
						niass.cancelOrders(QueryManager.getExchangeOrders(exchangeOrderID, tempID));
					}
					// Update order in DB
					QueryManager.updateMostRecentStopTradeWithExchangeData(tempID, exchangeOrderID, timestamp, unitPrice, filledAmount, status, stopStatus);
					System.out.println("processTradeInfo(...) processing " + exchangeOrderID + " on STOP " + status);
				}
				else if (tradeType.equals("Expiration")) {
					String expirationStatus = null;
					if (status.equals("Pending") || status.equals("Partially Filled")) {
						expirationStatus = "Expiration " + status;
					}
					else if (status.equals("Filled")) {
						expirationStatus = "Closed";
						status = "Closed";
						// Cancel any exchange orders based on this tempid
						niass.cancelOrders(QueryManager.getExchangeOrders(exchangeOrderID, tempID));
					}
					// Update order in DB
					QueryManager.updateMostRecentExpirationTradeWithExchangeData(tempID, exchangeOrderID, timestamp, unitPrice, filledAmount, status, expirationStatus);
					System.out.println("processTradeInfo(...) processing " + exchangeOrderID + " on EXPIRATION " + status);
				}
				
				// Calculate & Record Trade Profit/Loss
				if (status.equals("Closed")) {
					QueryManager.recordTradeProfit(tempID);
				}
			}
			else {
				System.err.println("processTradeInfo(...) - Couldn't figure out order at all.");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
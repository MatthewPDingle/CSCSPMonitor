package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.downloaders.okcoin.OKCoinConstants;
import dbio.QueryManager;
import utils.CalendarUtils;
import utils.StringUtils;

public class OKCoinWebSocketListener implements OKCoinWebSocketService {

	@Override
	public void onReceive(String msg) {
		try {
//			System.out.println(msg);
			
			Gson gson = new Gson();
			Object messageObject = gson.fromJson(msg, Object.class);
			if (messageObject instanceof ArrayList<?>) {
				ArrayList<LinkedTreeMap<String, Object>> messageList = gson.fromJson(msg, ArrayList.class);

				OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
				
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
					else if (channel.equals("ok_spotcny_trade")) {
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
			else {
				// {'event':'pong'} probably
				
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processTrade(LinkedTreeMap<String, Object> message) {
		try {
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>)message.get("data");
			if (ltm != null) {
				long orderId = StringUtils.getRegularLong(ltm.get("order_id").toString());
				boolean success = Boolean.parseBoolean(ltm.get("result").toString());
				
				if (success) {
					// Request order details
					System.out.println("processTrade(...) success - " + orderId);
					okss.getOrderInfo(OKCoinConstants.SYMBOL_BTCCNY, orderId);
				}
			}
			else {
				String errorCode = message.get("errorcode").toString();
				String success = message.get("success").toString();
				if (errorCode.equals("10002")) {
					System.out.println("processTrade(...) - Authentication Problem");
				}
				else if (errorCode.equals("10010")) {
					System.out.println("processTrade(...) - Insufficient Funds");
				}
				else if (errorCode.equals("10011")) {
					System.out.println("processTrade(...) - Order Quantity Too Low");
				}
				else {
					System.out.println("processTrade(...) - " + errorCode);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processRealTrades(LinkedTreeMap<String, Object> message) {
		try {
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			
			Object oData = message.get("data");
			if (oData == null) {
				boolean success = Boolean.parseBoolean(message.get("success").toString());
				String channel = message.get("channel").toString();
			}
			else {
				LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>)oData;
				if (ltm != null) { 
					long exchangeOrderID = StringUtils.getRegularLong(ltm.get("orderId").toString());
					int iStatus = (int)Double.parseDouble(ltm.get("status").toString()); // -1: Cancelled, 0: Pending, 1: Partially Filled, 2: Filled, 4: Cancel Request In Progress
					double amount = Double.parseDouble(ltm.get("tradeAmount").toString());
					double filledAmount = Double.parseDouble(ltm.get("completedTradeAmount").toString());
					double price = Double.parseDouble(ltm.get("tradePrice").toString()); // Price is something insane - 26.1.  makes no sense
					double unitPrice = Double.parseDouble(ltm.get("tradeUnitPrice").toString()); // Unit price seems correct
					long timestamp = StringUtils.getRegularLong(ltm.get("createdDate").toString());
					String symbol = ltm.get("symbol").toString();
					String type = ltm.get("tradeType").toString(); // buy, sell, buy_market, sell_market
	
					String status = "";
					if (iStatus == -1) {
						status = "Cancelled";
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
					
					System.out.println("processRealTrades(...) " + exchangeOrderID + ", " + status + ", " + amount + ", " + filledAmount + ", " + price + ", " + unitPrice);
					
					if (status.equals("Cancelled")) {
						HashMap<String, Object> results = QueryManager.figureOutExchangeIdTradeType(exchangeOrderID);
						if (results.size() > 0) {
							String exchangeIdTradeType = results.get("type").toString();
							Integer tempID = Integer.parseInt(results.get("tempid").toString());
							
							if (exchangeIdTradeType.equals("Open")) {
								if (filledAmount > 0) {
									QueryManager.cancelOpenOrder(tempID, true);
								}
								else {
									QueryManager.cancelOpenOrder(tempID, false);
								}
							}
							else if (exchangeIdTradeType.equals("Close")) {
								QueryManager.cancelCloseOrder(tempID);
							}
							else if (exchangeIdTradeType.equals("Stop")) {
								QueryManager.cancelStopOrder(tempID);
							}
							else if (exchangeIdTradeType.equals("Expiration")) {
								QueryManager.cancelExpirationOrder(tempID);
							}
						}
					}
					else {
						// Now I need to get this trade from the DB and update it.  I'm not sure if the websockets are threaded in a way that could do this, but I cannot allow concurrency here.
						synchronized (okss.getRequestedTradeLock()) {
							HashMap<String, Object> results = QueryManager.figureOutExchangeIdTradeType(exchangeOrderID);
							if (results.size() > 0) {
								String exchangeIdTradeType = results.get("type").toString();
								Integer tempID = Integer.parseInt(results.get("tempid").toString());
								
								if (exchangeIdTradeType.equals("Open")) {
									if (status.equals("Pending") || status.equals("Partially Filled") || status.equals("Filled")) {
										status = "Open " + status;
									}
									QueryManager.updateMostRecentOpenTradeWithExchangeData(exchangeOrderID, timestamp, unitPrice, filledAmount, status);
									System.out.println("processRealTrades(...) processing " + exchangeOrderID + " on OPEN " + status);
								}
								else if (exchangeIdTradeType.equals("Close")) {
									if (status.equals("Pending") || status.equals("Partially Filled")) {
										status = "Close " + status;
									}
									else if (status.equals("Filled")) {
										status = "Closed";
										// Cancel any exchange orders based on this tempid
										okss.cancelOrders(QueryManager.getExchangeOrders(tempID));
									}
									QueryManager.updateMostRecentCloseTradeWithExchangeData(exchangeOrderID, timestamp, unitPrice, filledAmount, status);
									System.out.println("processRealTrades(...) processing " + exchangeOrderID + " on CLOSE " + status);
								}
								else if (exchangeIdTradeType.equals("Stop")) {
									String stopStatus = null;
									if (status.equals("Pending") || status.equals("Partially Filled")) {
										stopStatus = "Stop " + status;
									}
									else if (status.equals("Filled")) {
										stopStatus = "Closed";
										status = "Closed";
										// Cancel any exchange orders based on this tempid
										okss.cancelOrders(QueryManager.getExchangeOrders(tempID));
									}
									// Update order in DB
									QueryManager.updateMostRecentStopTradeWithExchangeData(tempID, exchangeOrderID, timestamp, unitPrice, filledAmount, status, stopStatus);
									System.out.println("processRealTrades(...) processing " + exchangeOrderID + " on STOP " + status);
								}
								else if (exchangeIdTradeType.equals("Expiration")) {
									String expirationStatus = null;
									if (status.equals("Pending") || status.equals("Partially Filled")) {
										expirationStatus = "Expiration " + status;
									}
									else if (status.equals("Filled")) {
										expirationStatus = "Closed";
										status = "Closed";
										// Cancel any exchange orders based on this tempid
										okss.cancelOrders(QueryManager.getExchangeOrders(tempID));
									}
									// Update order in DB
									QueryManager.updateMostRecentExpirationTradeWithExchangeData(tempID, exchangeOrderID, timestamp, unitPrice, filledAmount, status, expirationStatus);
									System.out.println("processRealTrades(...) processing " + exchangeOrderID + " on EXPIRATION " + status);
								}
								
								// Calculate & Record Trade Profit/Loss
								if (status.equals("Closed")) {
									QueryManager.recordTradeProfit(tempID);
								}
							}
							else {
								System.err.println("Trade type is unknown!");
							}
						}
					}
				} 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processCancelOrder(LinkedTreeMap<String, Object> message) {
		try {
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			
			LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>)message.get("data");
			if (ltm != null) {
				long orderId = StringUtils.getRegularLong(ltm.get("order_id").toString());
				boolean success = Boolean.parseBoolean(ltm.get("result").toString());
				
				if (success) {
					System.out.println("processCancelOrder(...) - " + orderId);
					// Figure out what type of order this orderID corresponds to (Open, Close, Stop, Expiration)
					HashMap<String, Object> results = QueryManager.figureOutExchangeIdTradeType(orderId);
					if (results.size() > 0) {
						String exchangeIdTradeType = results.get("type").toString();
						Integer tempID = Integer.parseInt(results.get("tempid").toString());
						
						// Update trade record
						if (exchangeIdTradeType.equals("Open")) {
							QueryManager.cancelRemainderOfStaleOpenTrade(orderId);
						}
					}
					else {
						System.err.println("processCancelOrder(...) - Trade type is unknown!");
					}
				}
			}
			else {
				String errorCode = message.get("errorcode").toString();
				String success = message.get("success").toString();
				if (errorCode.equals("10002")) {
					System.out.println("processCancelOrder(...) - Authentication Problem");
				}
				else if (errorCode.equals("10009")) {
					System.out.println("processCancelOrder(...) - Order Does Not Exist");
				}
				else if (errorCode.equals("10010")) {
					System.out.println("processCancelOrder(...) - Insufficient Funds");
				}
				else if (errorCode.equals("10011")) {
					System.out.println("processCancelOrder(...) - Order Quantity Too Low");
				}
				else {
					System.out.println("procesCancelOrder(...) - " + errorCode);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processOrderInfo(LinkedTreeMap<String, Object> message) {
		try {
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			 
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
						System.out.println("processOrderInfo(...) on " + orders.size() + " orders");
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
							
							System.out.println("processOrderInfo(...) looking at " + exchangeOrderID + ", " + status + ", " + amount + ", " + filledAmount + ", " + price);
							
							if (status.equals("Cancelled")) {
								HashMap<String, Object> results = QueryManager.figureOutExchangeIdTradeType(exchangeOrderID);
								if (results.size() > 0) {
									String exchangeIdTradeType = results.get("type").toString();
									Integer tempID = Integer.parseInt(results.get("tempid").toString());
									
									if (exchangeIdTradeType.equals("Open")) {
										if (filledAmount > 0) {
											QueryManager.cancelOpenOrder(tempID, true);
										}
										else {
											QueryManager.cancelOpenOrder(tempID, false);
										}
									}
									else if (exchangeIdTradeType.equals("Close")) {
										QueryManager.cancelCloseOrder(tempID);
									}
									else if (exchangeIdTradeType.equals("Stop")) {
										QueryManager.cancelStopOrder(tempID);
									}
									else if (exchangeIdTradeType.equals("Expiration")) {
										QueryManager.cancelExpirationOrder(tempID);
									}
								}
							}
							else {
								// Now I need to get this trade from the DB and update it.  I'm not sure if the websockets are threaded in a way that could do this, but I cannot allow concurrency here.
								synchronized (okss.getRequestedTradeLock()) {
									Object[] nextRequestedTrade = QueryManager.getNextRequestedTrade();
									if (nextRequestedTrade != null && nextRequestedTrade.length > 0) {
										int nextRequestedTradeTempID = Integer.parseInt(nextRequestedTrade[0].toString());
										String nextRequestedTradeStatus = nextRequestedTrade[1].toString();
										
										// See what type of trade request it was - open, close, stop, expiration
										if (nextRequestedTradeStatus.equals("Open Requested")) {
											if (status.equals("Pending") || status.equals("Partially Filled") || status.equals("Filled")) {
												status = "Open " + status;
											}
											QueryManager.updateMostRecentOpenTradeWithExchangeData(nextRequestedTradeTempID, exchangeOrderID, timestamp, price, filledAmount, status);
											System.out.println("processOrderInfo(...) processing " + exchangeOrderID + " on OPEN " + status);
										}
										else if (nextRequestedTradeStatus.equals("Close Requested")) {
											if (status.equals("Pending") || status.equals("Partially Filled")) {
												status = "Close " + status;
											}
											else if (status.equals("Filled")) {
												status = "Closed";
												// Cancel any exchange orders based on this tempid
												okss.cancelOrders(QueryManager.getExchangeOrders(nextRequestedTradeTempID));
											}
											QueryManager.updateMostRecentCloseTradeWithExchangeData(nextRequestedTradeTempID, exchangeOrderID, timestamp, price, filledAmount, status);
											System.out.println("processOrderInfo(...) processing " + exchangeOrderID + " on CLOSE " + status);
										}
										else if (nextRequestedTradeStatus.equals("Stop Requested")) {
											String stopStatus = null;
											if (status.equals("Pending") || status.equals("Partially Filled")) {
												stopStatus = "Stop " + status;
											}
											else if (status.equals("Filled")) {
												stopStatus = "Closed";
												status = "Closed";
												// Cancel any exchange orders based on this tempid
												okss.cancelOrders(QueryManager.getExchangeOrders(nextRequestedTradeTempID));
											}
											// Update order in DB
											QueryManager.updateMostRecentStopTradeWithExchangeData(nextRequestedTradeTempID, exchangeOrderID, timestamp, price, filledAmount, status, stopStatus);
											System.out.println("processOrderInfo(...) processing " + exchangeOrderID + " on STOP " + status);
										}
										else if (nextRequestedTradeStatus.equals("Expiration Requested")) {
											String expirationStatus = null;
											if (status.equals("Pending") || status.equals("Partially Filled")) {
												expirationStatus = "Expiration " + status;
											}
											else if (status.equals("Filled")) {
												expirationStatus = "Closed";
												status = "Closed";
												// Cancel any exchange orders based on this tempid
												okss.cancelOrders(QueryManager.getExchangeOrders(nextRequestedTradeTempID));
											}
											// Update order in DB
											QueryManager.updateMostRecentExpirationTradeWithExchangeData(nextRequestedTradeTempID, exchangeOrderID, timestamp, price, filledAmount, status, expirationStatus);
											System.out.println("processOrderInfo(...) processing " + exchangeOrderID + " on EXPIRATION " + status);
										}
										
										// Calculate & Record Trade Profit/Loss
										if (status.equals("Closed")) {
											QueryManager.recordTradeProfit(nextRequestedTradeTempID);
										}
									}
								}
							}
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
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			
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
						
						okss.setBtcOnHand(btc);
						okss.setLtcOnHand(ltc);
						okss.setCnyOnHand(cny);
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
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			
			String channel = message.get("channel").toString();
			String channelPrefix = channel.replace("depth", "");
			String symbol = OKCoinConstants.WEBSOCKET_PREFIX_TO_TICK_SYMBOL_HASH.get(channelPrefix);
//			System.out.println("Order Book - " + symbol);
			LinkedTreeMap<String, Object> data = (LinkedTreeMap<String, Object>)message.get("data");
			if (data != null) {
				ArrayList<ArrayList<Double>> bids = (ArrayList<ArrayList<Double>>)data.get("bids");
				ArrayList<ArrayList<Double>> asks = (ArrayList<ArrayList<Double>>)data.get("asks");
				
				okss.putSymbolBidOrderBook(symbol, bids);
				okss.putSymbolAskOrderBook(symbol, asks);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processTick(LinkedTreeMap<String, Object> message) {
		try {
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			
			String channel = message.get("channel").toString();
			String symbol = OKCoinConstants.WEBSOCKET_SYMBOL_TO_TICK_SYMBOL_HASH.get(channel);
//			System.out.println("Tick - " + symbol);
			LinkedTreeMap<String, String> data = (LinkedTreeMap<String, String>)message.get("data");
			HashMap<String, String> tickerDataHash = new HashMap<String, String>();
			
			tickerDataHash.putAll(data);
			okss.putSymbolTickerDataHash(symbol, tickerDataHash);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processBar(LinkedTreeMap<String, Object> message) {
		try {
			OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
			
			ArrayList<Object> data = (ArrayList<Object>)message.get("data");
			
			String channel = message.get("channel").toString();
			String channelMinusDuration = channel.substring(0, channel.lastIndexOf("_"));
			String prefix = channelMinusDuration.substring(0, channelMinusDuration.lastIndexOf("_") + 1);
			String symbol = OKCoinConstants.WEBSOCKET_PREFIX_TO_TICK_SYMBOL_HASH.get(prefix);
//			System.out.println(("Bar -  " + symbol));
			
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
					Float change = null;
					Float gap = null;
					if (mostRecentBarInDB != null) {
						change = (float)close - mostRecentBarInDB.close;
						gap = (float)open - mostRecentBarInDB.close;
					}
					
					boolean partial = false;
					if (data.size() == 1) {
						partial = true;
					}
					if (a == data.size() - 1 && data.size() > 1) {
						partial = true;
					}

					Bar bar = new Bar(symbol, (float)open, (float)close, (float)high, (float)low, (float)vwap, (float)volume, null, change, gap, barStart, barEnd, duration, partial);
					//QueryManager.insertOrUpdateIntoBar(bar);
					bars.add(bar);
				}
			}

			okss.addLatestBars(bars);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
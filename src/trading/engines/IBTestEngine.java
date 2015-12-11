package trading.engines;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.ib.controller.OrderType;

import data.Bar;
import data.Model;
import data.downloaders.interactivebrokers.IBConstants;
import data.downloaders.interactivebrokers.IBConstants.ORDER_ACTION;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
import dbio.IBQueryManager;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import trading.TradingSingleton;
import utils.CalcUtils;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class IBTestEngine extends TradingEngineBase {

	private final int STALE_TRADE_SEC = 30; // How many seconds a trade can be open before it's considered "stale" and needs to be cancelled and re-issued.
	private final float MIN_TRADE_SIZE = 10f;
	private final int PIP_SPREAD_ON_EXPIRATION = 2; // If an close order expires, I set a tight limit & stop limit near the current price.  This is how many pips away from the bid & ask those orders are.
	
	private DecimalFormat df6;
	private DecimalFormat df5;
	
	private IBWorker ibWorker;
	private IBSingleton ibs;
	
	public IBTestEngine(IBWorker ibWorker) {
		super();
		
		df6 = new DecimalFormat("#.######");
		df5 = new DecimalFormat("#.#####");
		
		this.ibWorker = ibWorker;
		ibs = IBSingleton.getInstance();
	}
	
	public void setIbWorker(IBWorker ibWorker) {
		this.ibWorker = ibWorker;
	}

	@Override
	public void run() {
		try {
			while (running) {
				// Monitor Opens per model
				long totalMonitorOpenTime = 0;
				long totalMonitorCloseTime = 0;
				for (Model model : models) {
					try {
						long t1 = Calendar.getInstance().getTimeInMillis();
						
						HashMap<String, String> openMessages = new HashMap<String, String>();
						openMessages = monitorOpen(model);
						
						String jsonMessages = packageMessages(openMessages, new HashMap<String, String>());
						ss.addJSONMessageToTradingMessageQueue(jsonMessages);	
						
						long t2 = Calendar.getInstance().getTimeInMillis();
						totalMonitorOpenTime += (t2 - t1);	
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				// Monitor API events
				long startAPIMonitoringTime = Calendar.getInstance().getTimeInMillis();
				long totalAPIMonitoringTime = 0;
				while (totalAPIMonitoringTime < 1000) { // Monitor the API for up to 1 second
					monitorIBWorkerTradingEvents();
					Thread.sleep(10);
					totalAPIMonitoringTime = Calendar.getInstance().getTimeInMillis() - startAPIMonitoringTime;
				}
				
				// Monitor Closes
				long t1 = Calendar.getInstance().getTimeInMillis();
				
				HashMap<String, String> closeMessages = new HashMap<String, String>();
	//			closeMessages = monitorClose(null);
				String jsonMessages = packageMessages(new HashMap<String, String>(), closeMessages);
	//			ss.addJSONMessageToTradingMessageQueue(jsonMessages);
				
				long t2 = Calendar.getInstance().getTimeInMillis();
				totalMonitorCloseTime += (t2 - t1);
				
	//			System.out.println("monitorOpen x" + models.size() + " took " + totalMonitorOpenTime + "ms.");
	//			System.out.println("monitorClose x" + models.size() + " took " + totalMonitorCloseTime + "ms.");
			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public HashMap<String, String> monitorOpen(Model model) {
		HashMap<String, String> messages = new HashMap<String, String>();
		try {
			Calendar c = Calendar.getInstance();
			Calendar periodStart = CalendarUtils.getBarStart(c, model.getBk().duration);
			Calendar periodEnd = CalendarUtils.getBarEnd(c, model.getBk().duration);
		
			long barRemainingMS = periodEnd.getTimeInMillis() - c.getTimeInMillis();
			int secsUntilBarEnd = (int)barRemainingMS / 1000;
			int secsUntilNextSignal = secsUntilBarEnd - 5;
			if (secsUntilNextSignal < 0) {
				secsUntilNextSignal = 0;
			}

			Bar mostRecentBar = QueryManager.getMostRecentBar(model.getBk(), Calendar.getInstance());
			String priceString = df6.format(mostRecentBar.close);
			
			Calendar lastBarUpdate = ss.getLastDownload(model.getBk());
			String priceDelay = "";
			if (lastBarUpdate != null) {
				long timeSinceLastBarUpdate = c.getTimeInMillis() - lastBarUpdate.getTimeInMillis();
				priceDelay = new Double((double)Math.round((timeSinceLastBarUpdate / 1000d) * 100) / 100).toString();
			}
			
			boolean includeClose = false;
			boolean includeHour = true;
			
			// Load data for classification
			ArrayList<ArrayList<Object>> unlabeledList = ARFF.createUnlabeledWekaArffData(periodStart, periodEnd, model.getBk(), false, false, includeClose, includeHour, model.getMetrics(), metricDiscreteValueHash);
			Instances instances = Modelling.loadData(model.getMetrics(), unlabeledList, false, false, includeClose, includeHour); // I'm not sure if it's ok to not use weights here even if the model was built using weights.  I think it's ok because an instance you're evaluating is unclassified to begin with?
			
			// Try loading the classifier from the memory cache in TradingSingleton.  Otherwise load it from disk and store it in the cache.
			Classifier classifier = TradingSingleton.getInstance().getWekaClassifierHash().get(model.getModelFile());
			if (classifier == null) { // As long as the models are being cached correctly during TradingSingleton init, this should never happen.
				classifier = Modelling.loadZippedModel(model.getModelFile(), modelsPath);
				TradingSingleton.getInstance().addClassifierToHash(model.getModelFile(), classifier);
			}

			String action = "Waiting";
			if (instances != null && instances.firstInstance() != null) {
				// Make the prediction
				double label = classifier.classifyInstance(instances.firstInstance());
				instances.firstInstance().setClassValue(label);
				String prediction = instances.firstInstance().classAttribute().value((int)label);
				
//				if (prediction.equals("Draw")) {
//					if (Math.random() < .1) {
//						prediction = "Win";
//					}
//					else if (Math.random() > .9) {
//						prediction = "Lose";
//					}
//				}
				
				// See if enough time has passed and if we're in the trading window
				boolean timingOK = false;
				if (model.lastActionTime == null) {
					if (barRemainingMS < TRADING_WINDOW_MS) {
						timingOK = true;
					}
				}
				else {
					double msSinceLastTrade = c.getTimeInMillis() - model.lastActionTime.getTimeInMillis();
					if (msSinceLastTrade > TRADING_TIMEOUT) { // 30 seconds should do it (1/2 of 1 minute bar)
						if (barRemainingMS < TRADING_WINDOW_MS) {
							timingOK = true;
						}
					}
				}

				// Determine the action type (Buy, Buy Signal, Sell, Sell Signal)
				if ((model.type.equals("bull") && prediction.equals("Win") && model.tradeOffPrimary) ||
					(model.type.equals("bear") && prediction.equals("Lose") && model.tradeOffOpposite)) {
					double targetClose = (double)mostRecentBar.close * (1d + ((double)model.sellMetricValue / 100d));
					double targetStop = (double)mostRecentBar.close * (1d - ((double)model.stopMetricValue / 100d));
	
					if (timingOK) {
						action = "Buy";
						model.lastActionPrice = priceString;
						model.lastAction = action;
						model.lastActionTime = c;
						model.lastTargetClose = new Double((double)Math.round(targetClose * 100) / 100).toString();;
						model.lastStopClose = new Double((double)Math.round(targetStop * 100) / 100).toString();
					}
					else {
						action = "Buy Signal";
					}
				}
				if ((model.type.equals("bull") && prediction.equals("Lose") && model.tradeOffOpposite) ||
					(model.type.equals("bear") && prediction.equals("Win") && model.tradeOffPrimary)) {
					double targetClose = (double)mostRecentBar.close * (1d - ((double)model.sellMetricValue / 100d));
					double targetStop = (double)mostRecentBar.close * (1d + ((double)model.stopMetricValue / 100d));
					
					if (timingOK) {
						action = "Sell";
						model.lastActionPrice = priceString;
						model.lastAction = action;
						model.lastActionTime = c;
						model.lastTargetClose = new Double((double)Math.round(targetClose * 100) / 100).toString();
						model.lastStopClose = new Double((double)Math.round(targetStop * 100) / 100).toString();
					}
					else {
						action = "Sell Signal";
					}
				}

				// Model is firing - let's see if we can make a trade 
				if (action.equals("Buy") || action.equals("Sell")) {
					// Get the direction of the trade
					String direction = "";
					ORDER_ACTION orderAction = null;
					if (action.equals("Buy")) {
						direction = "bull";
						orderAction = ORDER_ACTION.BUY;
					}
					else if (action.equals("Sell")) {
						direction = "bear";
						 orderAction = ORDER_ACTION.SELL;
					}
					
					// Find a target price to submit a limit order.
					double modelPrice = Double.parseDouble(priceString);
					Double likelyFillPrice = modelPrice;
					if (direction.equals("bull")) {
						if (ibs.getTickerFieldValue(model.bk, IBConstants.TICK_FIELD_ASK_PRICE) != null) {
							likelyFillPrice = ibs.getTickerFieldValue(model.bk, IBConstants.TICK_FIELD_ASK_PRICE);
						}
					}
					else if (direction.equals("bear")) {
						if (ibs.getTickerFieldValue(model.bk, IBConstants.TICK_FIELD_BID_PRICE) != null) {
							likelyFillPrice = ibs.getTickerFieldValue(model.bk, IBConstants.TICK_FIELD_BID_PRICE);
						}
					}
					double suggestedEntryPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format(likelyFillPrice)));
					
					// Finalize the action based on whether it's a market or limit order
					action = action.toLowerCase();
					
					// Calculate position size.
					int positionSize = calculatePositionSize(direction, likelyFillPrice);
					
					// Calculate the exit target
					double suggestedExitPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice + (likelyFillPrice * model.getSellMetricValue() / 100d)))));
					double suggestedStopPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice - (likelyFillPrice * model.getStopMetricValue() / 100d)))));
					if ((model.type.equals("bear") && action.equals("buy")) || // Opposite trades
						(model.type.equals("bull") && action.equals("sell"))) {
						suggestedExitPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice - (likelyFillPrice * model.getStopMetricValue() / 100d)))));
						suggestedStopPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice + (likelyFillPrice * model.getSellMetricValue() / 100d)))));
					}

					// Calculate the trades expiration time
					Calendar tradeBarEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), model.bk.duration);
					Calendar expiration = CalendarUtils.addBars(tradeBarEnd, model.bk.duration, model.numBars);
					
					// Calculate the open order's expiration time
					Calendar openOrderExpiration = Calendar.getInstance();
					openOrderExpiration.add(Calendar.SECOND, STALE_TRADE_SEC);
					
					// Record the trade request in the DB
					if (positionSize >= MIN_TRADE_SIZE) {
						// Record order request in DB
						int orderID = IBQueryManager.recordTradeRequest(OrderType.LMT.toString(), orderAction.toString(), "Open Requested", 
								direction, model.bk, suggestedEntryPrice, suggestedExitPrice, suggestedStopPrice, positionSize, model.modelFile, expiration);
							
						// Send the trade order to IB
						ibWorker.placeOrder(orderID, null, OrderType.LMT, orderAction, positionSize, null, suggestedEntryPrice, false, openOrderExpiration);
					}
				}
			}
			
			messages.put("Action", action);
			messages.put("Time", sdf.format(c.getTime()));
			messages.put("SecondsRemaining", new Integer(secsUntilNextSignal).toString());
			messages.put("Model", model.getModelFile());
			messages.put("TestWinPercentage", new Double((double)Math.round(model.getTestWinPercent() * 1000) / 10).toString());
			messages.put("TestOppositeWinPercentage", new Double((double)Math.round(model.getTestOppositeWinPercent() * 1000) / 10).toString());
			messages.put("TestEstimatedAverageReturn", new Double((double)Math.round(model.getTestEstimatedAverageReturn() * 1000) / 1000).toString());
			messages.put("TestOppositeEstimatedAverageReturn", new Double((double)Math.round(model.getTestOppositeEstimatedAverageReturn() * 1000) / 1000).toString());
			messages.put("TestReturnPower", new Double((double)Math.round(model.getTestReturnPower() * 1000) / 1000).toString());
			messages.put("TestOppositeReturnPower", new Double((double)Math.round(model.getTestOppositeReturnPower() * 1000) / 1000).toString());
			messages.put("Type", model.type);
			messages.put("TradeOffPrimary", new Boolean(model.tradeOffPrimary).toString());
			messages.put("TradeOffOpposite", new Boolean(model.tradeOffOpposite).toString());
			String duration = model.bk.duration.toString();
			duration = duration.replace("BAR_", "");
			messages.put("Duration", duration);
			messages.put("Symbol", model.bk.symbol);
			messages.put("Price", priceString);
			messages.put("PriceDelay", priceDelay);
			
			messages.put("LastAction", model.lastAction);
			messages.put("LastTargetClose", model.lastTargetClose);
			messages.put("LastStopClose", model.lastStopClose);
			messages.put("LastActionPrice", model.lastActionPrice);
			String lastActionTime = "";
			if (model.lastActionTime != null) {
				lastActionTime = sdf.format(model.lastActionTime.getTime());
			}
			messages.put("LastActionTime", lastActionTime);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}

	@Override
	public HashMap<String, String> monitorClose(Model model) {
		HashMap<String, String> messages = new HashMap<String, String>();
		try {
			ArrayList<HashMap<String, Object>> openPositions = QueryManager.getOpenPositionsPossiblyNeedingCloseMonitoring("trades");
			for (HashMap<String, Object> openPosition : openPositions) {
				String direction = openPosition.get("direction").toString();
				int tempID = (int)openPosition.get("tempid");
				long exchangeOpenTradeID = (long)openPosition.get("exchangeopentradeid");
				long exchangeCloseTradeID = (long)openPosition.get("exchangeclosetradeid");
				String status = openPosition.get("status").toString();
				String stopStatus = null;
				Object oStopStatus = openPosition.get("stopstatus");
				if (oStopStatus != null) {
					stopStatus = oStopStatus.toString();
				}
				String expirationStatus = null;
				Object oExpirationStatus = openPosition.get("expirationstatus");
				if (oExpirationStatus != null) {
					expirationStatus = oExpirationStatus.toString();
				}
				String symbol = openPosition.get("symbol").toString();
				String duration = openPosition.get("duration").toString();
				String modelFile = openPosition.get("model").toString();
				int filledAmount = (int)openPosition.get("filledamount");
				int closeFilledAmount = 0;
				Object oCloseFilledAmount = openPosition.get("closefilledamount");
				if (oCloseFilledAmount != null) {
					closeFilledAmount = (int)oCloseFilledAmount;
				}
				float actualEntryPrice = (float)openPosition.get("actualentryprice");
				float suggestedStopPrice = (float)openPosition.get("suggestedstopprice");
				float commission = (float)openPosition.get("commission");
				java.sql.Timestamp expirationTimestamp = (java.sql.Timestamp)openPosition.get("expiration");
				Calendar expiration = Calendar.getInstance();
				expiration.setTimeInMillis(expirationTimestamp.getTime());
				
				// Get the current price for exit evaluation
				Double likelyFillPrice = null;
				if (direction.equals("bull")) {
					likelyFillPrice = ibs.getTickerFieldValue(model.bk, IBConstants.TICK_FIELD_ASK_PRICE);
				}
				else if (direction.equals("bear")) {
					likelyFillPrice = ibs.getTickerFieldValue(model.bk, IBConstants.TICK_FIELD_BID_PRICE);
				}
				if (likelyFillPrice == null) {
					throw new Exception ("No tick data to monitor close for " + model.bk.toString());
				}
				
				String exitReason = "";
	
				// Check if this trade has expired
				if (expirationStatus != null && expirationStatus.equals("Expiration Needed")) {
					exitReason = "Expiration";
				}
				if (Calendar.getInstance().after(expiration) && expirationStatus == null) {
					exitReason = "Expiration";
				}

				String closeType = "bull";
				String action = "buy"; // Make the action the opposite of the type because this is for closing the trade
				if (direction.equals("bull")) {
					action = "sell";
					closeType = "bear";
				}
				
				int requiredAmount = filledAmount - closeFilledAmount;
				if (requiredAmount < MIN_TRADE_SIZE) {
					System.err.println("Need to make a stop or expiration order but the size is too small to be possible.  Going to abandon the remainder.");
					QueryManager.abandonTooSmallPosition(tempID);
					continue;
				}
				
				if (exitReason.equals("Expiration")) {
					boolean enoughCash = true;
					if (action.equals("buy")) {
						// I need to have enough cash
//						if (niass.getCnyOnHand() > (requiredAmount * bestPrice)) {
//							niass.setCnyOnHand(niass.getCnyOnHand() - (requiredAmount * bestPrice));
//						}
//						else { // Not enough Cash
//							enoughCash = false;
//						}
					}
			
					if (enoughCash) {
						System.out.println("Enough cash so making Expiration Requested on " + tempID);
						System.out.println("makeExpirationTradeRequest(...) at " + Calendar.getInstance().getTime().toString());
						QueryManager.makeExpirationTradeRequest(exchangeOpenTradeID, "Expiration Requested");
//						niass.spotTrade(OKCoinConstants.SYMBOL_BTCCNY, bestPrice, requiredAmount, action);
					}
					else if (exchangeCloseTradeID != 0) {
//						niass.cancelOrder(OKCoinConstants.SYMBOL_BTCCNY, exchangeCloseTradeID);
						QueryManager.makeExpirationTradeRequest(exchangeOpenTradeID, "Expiration Needed");
						System.out.println("Not enough cash for expiration so cancelling the close " + exchangeCloseTradeID + "/" + tempID + " order first.");
					}
					else {
						System.out.println("Not enough cash for expiration and there's no close order to cancel. " + tempID);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	private void monitorIBWorkerTradingEvents() {
		try {
			HashMap<String, HashMap<String, Object>> tradingEventDataHash = ibWorker.getEventDataHash();
			if (tradingEventDataHash != null) {
				// orderStatus
				HashMap<String, Object> orderStatusDataHash = tradingEventDataHash.get("orderStatus");
				if (orderStatusDataHash != null) {
					processOrderStatusEvents(orderStatusDataHash);
				}
				
				
				// We're done processing all the events.  Clear it out so I don't keep processing the same events.
				ibWorker.setEventDataHash(new HashMap<String, HashMap<String, Object>>());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processOrderStatusEvents(HashMap<String, Object> orderStatusDataHash) {
		try {
			// Unpack the parameters
			int orderId = (int)orderStatusDataHash.get("orderId");
			String status = orderStatusDataHash.get("status").toString();
			int filled = (int)orderStatusDataHash.get("filled");
			int remaining = (int)orderStatusDataHash.get("remaining");
			double avgFillPrice = (double)orderStatusDataHash.get("avgFillPrice");
			avgFillPrice = CalcUtils.roundTo5DigitHalfPip(avgFillPrice);
			int permId = (int)orderStatusDataHash.get("permId");
			int parentId = (int)orderStatusDataHash.get("parentId");
			double lastFillPrice = (double)orderStatusDataHash.get("lastFillPrice");
			int clientId = (int)orderStatusDataHash.get("clientId");
			String whyHeld = null;
			if (orderStatusDataHash.get("whyHeld") != null) {
				whyHeld = orderStatusDataHash.get("whyHeld").toString();
			}
			
			// Get the needed fields from the order
			String orderType = IBQueryManager.getOrderIDType(orderId);
			HashMap<String, Object> fieldHash = IBQueryManager.getOrderInfo(orderType, orderId);
			String openAction = "";
			if (fieldHash.get("iborderaction") != null) {
				openAction = fieldHash.get("iborderaction").toString();
			}
			ORDER_ACTION closeAction = ORDER_ACTION.SELL;
			if (openAction.equals("SELL")) {
				closeAction = ORDER_ACTION.BUY;
			}
			String direction = fieldHash.get("direction").toString();
			int requestedAmount = 0;
			if (fieldHash.get("requestedamount") != null) {
				requestedAmount = ((BigDecimal)fieldHash.get("requestedamount")).intValue();
			}
			int filledAmount = 0;
			if (fieldHash.get("filledamount") != null) {
				filledAmount = ((BigDecimal)fieldHash.get("filledamount")).intValue();
			}
			int closeFilledAmount = 0;
			if (fieldHash.get("closefilledamount") != null) {
				closeFilledAmount = ((BigDecimal)fieldHash.get("closefilledamount")).intValue();
			}
			int remainingAmountNeededToClose = filledAmount - closeFilledAmount;
			double suggestedExitPrice = ((BigDecimal)fieldHash.get("suggestedexitprice")).doubleValue();
			double suggestedStopPrice = ((BigDecimal)fieldHash.get("suggestedstopprice")).doubleValue();
			Timestamp expirationTS = (Timestamp)fieldHash.get("expiration");
			Calendar expiration = Calendar.getInstance();
			expiration.setTimeInMillis(expirationTS.getTime());

			if (status.equals("Filled")) {
				// Open Filled.  Needs Close & Stop orders made.  This query only checks against OpenOrderIDs so I don't have to worry about it being for a different order type.
				if (orderType.equals("Open")) {
					// Update the trade in the DB
					IBQueryManager.updateOpen(orderId, status, filled, avgFillPrice, parentId);

					boolean needsCloseAndStop = IBQueryManager.checkIfNeedsCloseAndStopOrders(orderId);
					if (needsCloseAndStop) {
						// Get a One-Cancels-All group ID
						int ibOCAGroup = IBQueryManager.getIBOCAGroup();
						
						// Make the close trade
						int closeOrderID = IBQueryManager.recordCloseTradeRequest(orderId);		
						ibWorker.placeOrder(closeOrderID, ibOCAGroup, OrderType.LMT, closeAction, filled, null, suggestedExitPrice, false, expiration);
						
						// Make the stop trade
						int stopOrderID = IBQueryManager.recordStopTradeRequest(orderId);		
						ibWorker.placeOrder(stopOrderID, ibOCAGroup, OrderType.STP_LMT, closeAction, filled, suggestedStopPrice, suggestedStopPrice, false, expiration);
					}
				}
				// Close Filled.  Need to close out order
				if (orderType.equals("Close")) {
					double commission = 0;
					double netProfit = 0;
					double grossProfit = netProfit - commission;
					IBQueryManager.recordClose(orderId, avgFillPrice, "Target Hit", filled, commission, netProfit, grossProfit);
				}
				// Stop Filled.  Need to close out order
				if (orderType.equals("Stop")) {
					double commission = 0;
					double netProfit = 0;
					double grossProfit = netProfit - commission;
					IBQueryManager.recordClose(orderId, avgFillPrice, "Stop Hit", filled, commission, netProfit, grossProfit);
				}
			}
			else if (status.equals("Submitted")) { // Submitted includes partial fills
				if (orderType.equals("Open")) {
					IBQueryManager.updateOpen(orderId, status, filled, avgFillPrice, parentId);
				}
				if (orderType.equals("Close")) {
					IBQueryManager.updateClose(orderId, filled, avgFillPrice, parentId);
				}
				if (orderType.equals("Stop")) {
					IBQueryManager.updateStop(orderId, filled, avgFillPrice, parentId);
				}
			}
			else if (status.equals("Cancelled")) {
				// Open Cancelled.  Just never got filled
				if (orderType.equals("Open")) {
					IBQueryManager.cancelOpenOrder(orderId);
				}
				// Close Cancelled.  Check if it was an expiration
				if (orderType.equals("Close")) {
					boolean expired = IBQueryManager.checkIfCloseOrderExpired(orderId);
					if (expired) {
						// Make a new Limit Close & Stop Limit Stop in the same OCA group tight against the current price and don't worry about these expiring because they won't last long.
						
						// Get a One-Cancels-All group ID
						int ibOCAGroup = IBQueryManager.getIBOCAGroup();
						
						// Get prices a couple pips on each side of the bid/ask spread
						double ask = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
						double askPlus2Pips = ask + (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
						double bid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
						double bidMinus2Pips = bid - (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
						askPlus2Pips = CalcUtils.roundTo5DigitHalfPip(askPlus2Pips);
						bidMinus2Pips = CalcUtils.roundTo5DigitHalfPip(bidMinus2Pips);
						
						// Make a good-till-date far in the future
						Calendar gtd = Calendar.getInstance();
						gtd.add(Calendar.DATE, 1);
						
						if (direction.equals("bull")) {
							// Make the new close trade
							int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(orderId);
							ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, askPlus2Pips, false, gtd);
							System.out.println("Bull Close Expired.  Making new Close.  " + newCloseOrderID + ", " + askPlus2Pips);
							
							// Make the new stop trade
							int newStopOrderID = IBQueryManager.updateStopTradeRequest(orderId);
							ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP_LMT, closeAction, remainingAmountNeededToClose, bidMinus2Pips, bidMinus2Pips, false, gtd);
							System.out.println("Bull Stop Expired.  Making new Stop.  " + newStopOrderID + ", " + bidMinus2Pips);
						}
						else {
							// Make the new close trade
							int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(orderId);
							ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, bidMinus2Pips, false, gtd);
							System.out.println("Bear Close Expired.  Making new Close.  " + newCloseOrderID + ", " + bidMinus2Pips);
							
							// Make the new stop trade
							int newStopOrderID = IBQueryManager.updateStopTradeRequest(orderId);
							ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP_LMT, closeAction, remainingAmountNeededToClose, askPlus2Pips, askPlus2Pips, false, gtd);
							System.out.println("Bear Stop Expired.  Making new Stop.  " + newStopOrderID + ", " + askPlus2Pips);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int calculatePositionSize(String direction, Double bestPrice) {
		return 20000;
	}
}
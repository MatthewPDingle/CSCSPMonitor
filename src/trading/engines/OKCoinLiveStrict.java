package trading.engines;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.Bar;
import data.Model;
import data.downloaders.okcoin.OKCoinConstants;
import data.downloaders.okcoin.websocket.NIAStatusSingleton;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import servlets.trading.TradingSingleton;
import utils.CalcUtils;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class OKCoinLiveStrict extends TradingEngineBase {

	private final int STALE_TRADE_SEC = 30; // How many seconds a trade can be open before it's considered "stale" and needs to be cancelled and re-issued.
	private final float MIN_TRADE_SIZE = .012f;
	private final float ACCEPTABLE_SLIPPAGE = .0001f; // If market price is within .0x% of best price, make market order.
	private final String TRADES_TABLE = "trades";
	
	private NIAStatusSingleton niass = null;
	
	public OKCoinLiveStrict() {
		super();
		niass = NIAStatusSingleton.getInstance();
		niass.getRealTrades();
	}
	
	@Override
	public void run() {
		while (running) {
			// Get updated user info about funds
			niass.getUserInfo();
		
			// Check for orders that are stuck at partially filled.  Just need to cancel them and say they're filled
			ArrayList<Long> pendingOrPartiallyFilledStuckOrderExchangeIDs = QueryManager.getPendingOrPartiallyFilledStaleOpenOrderExchangeOpenTradeIDs(STALE_TRADE_SEC);
			QueryManager.makeCancelRequest(pendingOrPartiallyFilledStuckOrderExchangeIDs);
			cancelStaleOrders(pendingOrPartiallyFilledStuckOrderExchangeIDs);
			
			// Check for orders that never made it past Open Requested.  This could be if I thought I had the money to place the order but actually didn't.  Or it could be if I got disconnected during the callback. 
			QueryManager.cancelStuckOpenRequestedTempIDs(STALE_TRADE_SEC);
			
			// Get newly completed open trades that need close limit orders placed
			ArrayList<HashMap<String, Object>> tradesNeedingCloseOrders = QueryManager.getFilledTradesThatNeedCloseOrdersPlaced();
			placeCloseLimitOrdersForNewlyCompletedOpenTrades(tradesNeedingCloseOrders);
			
			// Check for orders that are stale, need to be cancelled, and re-issued at new price.
			ArrayList<Long> staleExchangeCloseTradeIDs = QueryManager.getClosePartiallyFilledOrderExchangeCloseTradeIDs(STALE_TRADE_SEC); // Close Partially Filled
			cancelStaleOrders(staleExchangeCloseTradeIDs);
			
			ArrayList<Long> staleStopIDs = QueryManager.getStaleStopOrders(STALE_TRADE_SEC); // Stop Pending or Stop Partially Filled
			cancelStaleOrders(staleStopIDs);
			
			ArrayList<Long> staleExpirationIDs = QueryManager.getStaleExpirationOrders(STALE_TRADE_SEC); // Expiration Pending or Expiration Partially Filled
			cancelStaleOrders(staleExpirationIDs);
					
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

			// Monitor Closes
			long t1 = Calendar.getInstance().getTimeInMillis();
			
			HashMap<String, String> closeMessages = new HashMap<String, String>();
			closeMessages = monitorClose(null);
			String jsonMessages = packageMessages(new HashMap<String, String>(), closeMessages);
			ss.addJSONMessageToTradingMessageQueue(jsonMessages);
			
			long t2 = Calendar.getInstance().getTimeInMillis();
			totalMonitorCloseTime += (t2 - t1);
			
//			System.out.println("monitorOpen x" + models.size() + " took " + totalMonitorOpenTime + "ms.");
//			System.out.println("monitorClose x" + models.size() + " took " + totalMonitorCloseTime + "ms.");
			
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {}
		}
	}
	
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
			String priceString = new Double((double)Math.round(mostRecentBar.close * 100) / 100).toString();
			
			Calendar lastBarUpdate = ss.getLastDownload(model.getBk());
			String priceDelay = "";
			if (lastBarUpdate != null) {
				long timeSinceLastBarUpdate = c.getTimeInMillis() - lastBarUpdate.getTimeInMillis();
				priceDelay = new Double((double)Math.round((timeSinceLastBarUpdate / 1000d) * 100) / 100).toString();
			}
			
			boolean includeClose = true;
			boolean includeHour = true;
			if (model.algo.equals("NaiveBayes")) {
				includeClose = false;
			}
			if (model.algo.equals("RandomForest")) {
				includeHour = false;
			}
			
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
					if (action.equals("Buy")) {
						direction = "bull";
					}
					else if (action.equals("Sell")) {
						direction = "bear";
					}
					
					// Find a target price to submit a limit order.
					boolean marketOrder = false;
					double modelPrice = Double.parseDouble(priceString);
					Double bestPrice = modelPrice;
					if (direction.equals("bull")) {
						bestPrice = findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
						double bestMarketPrice = findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
						if (Math.abs(bestPrice - bestMarketPrice) < (bestPrice * ACCEPTABLE_SLIPPAGE)) {
							marketOrder = true;
							bestPrice = bestMarketPrice;
						}
					}
					else if (direction.equals("bear")) {
						bestPrice = findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
						double bestMarketPrice = findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
						if (Math.abs(bestPrice - bestMarketPrice) < (bestPrice * ACCEPTABLE_SLIPPAGE)) {
							marketOrder = true;
							bestPrice = bestMarketPrice;
						}
					}
					
					// Finalize the action based on whether it's a market or limit order
					action = action.toLowerCase();
//					if (marketOrder) {
//						action = action + "_market";
//					}
					
					// Calculate position size.  This gets rounded to 3 decimal places
					double positionSize = calculatePositionSize(direction, bestPrice);
					
					// Calculate the exit target
					float suggestedExitPrice = (float)(bestPrice + (bestPrice * model.getSellMetricValue() / 100f));
					float suggestedStopPrice = (float)(bestPrice - (bestPrice * model.getStopMetricValue() / 100f));
					if ((model.type.equals("bear") && action.equals("buy")) || // Opposite trades
						(model.type.equals("bull") && action.equals("sell"))) {
						suggestedExitPrice = (float)(bestPrice - (bestPrice * model.getStopMetricValue() / 100f));
						suggestedStopPrice = (float)(bestPrice + (bestPrice * model.getSellMetricValue() / 100f));
					}

					// Calculate the trades expiration time
					Calendar tradeBarEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), model.bk.duration);
					Calendar expiration = CalendarUtils.addBars(tradeBarEnd, model.bk.duration, model.numBars);
					
					// Record the trade request in the DB
					if (positionSize >= MIN_TRADE_SIZE) {
						QueryManager.makeTradeRequest(TRADES_TABLE, "Open Requested", direction, (float)modelPrice, null, suggestedExitPrice, suggestedStopPrice, (float)positionSize, 0f, model.bk.symbol, model.bk.duration.toString(), model.modelFile, expiration);
					
						// Send the trade order to OKCoin
						String apiSymbol = OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(model.bk.symbol);
						niass.spotTrade(apiSymbol, bestPrice, positionSize, action);
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
	
	public HashMap<String, String> monitorClose(Model model) {
		HashMap<String, String> messages = new HashMap<String, String>();
		try {
			ArrayList<HashMap<String, Object>> openPositions = QueryManager.getOpenPositionsPossiblyNeedingCloseMonitoring(TRADES_TABLE);
			for (HashMap<String, Object> openPosition : openPositions) {
				String type = openPosition.get("type").toString();
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
				float filledAmount = (float)openPosition.get("filledamount");
				float closeFilledAmount = 0;
				Object oCloseFilledAmount = openPosition.get("closefilledamount");
				if (oCloseFilledAmount != null) {
					closeFilledAmount = (float)oCloseFilledAmount;
				}
				float actualEntryPrice = (float)openPosition.get("actualentryprice");
				float suggestedStopPrice = (float)openPosition.get("suggestedstopprice");
				float commission = (float)openPosition.get("commission");
				java.sql.Timestamp expirationTimestamp = (java.sql.Timestamp)openPosition.get("expiration");
				Calendar expiration = Calendar.getInstance();
				expiration.setTimeInMillis(expirationTimestamp.getTime());
				
				// Get the current price for exit evaluation
				HashMap<String, HashMap<String, String>> symbolDataHash = NIAStatusSingleton.getInstance().getSymbolDataHash();
				HashMap<String, String> tickHash = symbolDataHash.get(symbol);
				String lastTick = null;
				if (tickHash != null) {
					lastTick = tickHash.get("last");
				}
				if (lastTick == null) {
					throw new Exception("No tick data available to exit trade");
				}
				float currentPrice = 0;
				if (lastTick != null) {
					currentPrice = Float.parseFloat(lastTick);
				}
				
				String exitReason = "";
	
				// Check if this trade has expired
				if (Calendar.getInstance().after(expiration) || (expirationStatus != null && expirationStatus.equals("Expiration Needed"))) {
					exitReason = "Expiration";
				}
				// Check if the stop has been hit
				else if (type.equals("bull")) {
					if (currentPrice <= suggestedStopPrice) {
						exitReason = "Stop Hit";
					}
				}
				else if (type.equals("bear")) {
					if (currentPrice >= suggestedStopPrice) {
						exitReason = "Stop Hit";
					}
				}
				else if (stopStatus != null && stopStatus.equals("Stop Needed")) {
					exitReason = "Stop Hit";
				}

				String closeType = "bull";
				String action = "buy"; // Make the action the opposite of the type because this is for closing the trade
				if (type.equals("bull")) {
					action = "sell";
					closeType = "bear";
				}
				
				// Find the best price, looking on the opposite side of the order book because we're closing
				Float bestPrice = currentPrice;
				if (type.equals("bear")) {
					bestPrice = (float)findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(symbol), "bid", currentPrice);
				}
				else if (type.equals("bull")) {
					bestPrice = (float)findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(symbol), "ask", currentPrice);
				}
				
				float requiredAmount = filledAmount - closeFilledAmount;
				
				requiredAmount = CalcUtils.round(requiredAmount, 3);
				bestPrice = CalcUtils.round(bestPrice, 2);	
				
				if (exitReason.equals("Expiration")) {
					boolean enoughCash = true;
					if (action.equals("buy")) {
						// I need to have enough cash
						if (niass.getCnyOnHand() > (requiredAmount * bestPrice)) {
							niass.setCnyOnHand(niass.getCnyOnHand() - (requiredAmount * bestPrice));
						}
						else { // Not enough Cash
							enoughCash = false;
						}
					}
			
					if (enoughCash) {
						System.out.println("Enough cash so making Expiration Requested on " + tempID);
						QueryManager.makeExpirationTradeRequest(exchangeOpenTradeID, "Expiration Requested");
						niass.spotTrade(OKCoinConstants.SYMBOL_BTCCNY, bestPrice, requiredAmount, action);
					}
					else if (exchangeCloseTradeID != 0) {
						niass.cancelOrder(OKCoinConstants.SYMBOL_BTCCNY, exchangeCloseTradeID);
						QueryManager.makeExpirationTradeRequest(exchangeOpenTradeID, "Expiration Needed");
						System.out.println("Not enough cash for expiration so cancelling the close " + exchangeCloseTradeID + "/" + tempID + " order first.");
//						niass.spotTrade(OKCoinConstants.SYMBOL_BTCCNY, bestPrice, requiredAmount, action);
					}
					else {
						System.out.println("Not enough cash for expiration and there's no close order to cancel. " + tempID);
					}
				}
				else if (exitReason.equals("Stop Hit")) {
					boolean enoughCash = true;
					if (action.equals("buy")) {
						// I need to have enough cash
						if (niass.getCnyOnHand() > (requiredAmount * bestPrice)) {
							niass.setCnyOnHand(niass.getCnyOnHand() - (requiredAmount * bestPrice));
						}
						else { // Not enough cash
							enoughCash = false;
						}
					}
					
					if (enoughCash) {
						System.out.println("Enough cash so making Stop Requested on " + tempID);
						QueryManager.makeStopTradeRequest(exchangeOpenTradeID, "Stop Requested");
						niass.spotTrade(OKCoinConstants.SYMBOL_BTCCNY, bestPrice, requiredAmount, action);
					}
					else if (exchangeCloseTradeID != 0) {
						niass.cancelOrder(OKCoinConstants.SYMBOL_BTCCNY, exchangeCloseTradeID);
						QueryManager.makeStopTradeRequest(exchangeOpenTradeID, "Stop Needed");
						System.out.println("Not enough cash for stop so cancelling the close " + exchangeCloseTradeID + "/" + tempID + " order first");
//						niass.spotTrade(OKCoinConstants.SYMBOL_BTCCNY, bestPrice, requiredAmount, action);
					}
					else {
						System.out.println("Not enouch cash for stop and there's no clsoe order to cancel. " + tempID);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	private double calculatePositionSize(String direction, double bestPrice) {
		double amount = 0;
		double cnyOnHand = niass.getCnyOnHand();
		if (direction.equals("bull")) {
			// Buying BTC
			double btcCanAfford = cnyOnHand / bestPrice;
			if (btcCanAfford < MIN_TRADE_SIZE) {
				return 0; // Cannot afford the minimum amount
			}
			amount = btcCanAfford / 50d;
			if (amount < MIN_TRADE_SIZE) {
				amount = MIN_TRADE_SIZE; // Minimum size
			}
		}
		else if (direction.equals("bear")) {
			// Selling BTC
			double btcOnHand = niass.getBtcOnHand();
			amount = btcOnHand / 50d;
			if (amount < MIN_TRADE_SIZE) {
				amount = MIN_TRADE_SIZE;
			}
			if (amount >= btcOnHand) {
				return 0; // We don't have the minimum amount to sell
			}
		}
		amount = CalcUtils.round((float)amount, 3);
		niass.setCnyOnHand(cnyOnHand - (amount * bestPrice));
		return amount;
	}

	private void cancelStaleOrders(ArrayList<Long> exchangeIDs) {
		try {
			for (long exchangeID : exchangeIDs) {
				niass.cancelOrder(OKCoinConstants.SYMBOL_BTCCNY, exchangeID);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void placeCloseLimitOrdersForNewlyCompletedOpenTrades(ArrayList<HashMap<String, Object>> tradesNeedingExitOrders) {
		try {
			for (HashMap<String, Object> tradeHash : tradesNeedingExitOrders) {
				double suggestedExitPrice = (double)tradeHash.get("suggestedexitprice");
				double filledAmount = (double)tradeHash.get("filledamount");
				double closeFilledAmount = 0;
				Object oCloseFilledAmount = tradeHash.get("closedfilledamount");
				if (oCloseFilledAmount != null) {
					closeFilledAmount = (double)oCloseFilledAmount;
				}
				long exchangeOpenTradeID = (long)tradeHash.get("exchangeopentradeid");
				int tempid = (int)tradeHash.get("tempid");
				String type = tradeHash.get("type").toString();
				String modelFile = tradeHash.get("model").toString();
				String symbol = tradeHash.get("symbol").toString();
				String duration = tradeHash.get("duration").toString();
				
				String closeType = "bull";
				String action = "buy"; // Make the action the opposite of the type because this is for closing the trade
				if (type.equals("bull")) {
					action = "sell";
					closeType = "bear";
				}
				
				double amountNeeded = filledAmount - closeFilledAmount; // For when this trade had a close that was already partially filled, but then got stuck and needed re-issuing
				
				suggestedExitPrice = CalcUtils.round((float)suggestedExitPrice, 2);
				amountNeeded = CalcUtils.round((float)amountNeeded, 3);
				
				// Record and make the trade request
				QueryManager.makeCloseTradeRequest(tempid);
				niass.spotTrade(OKCoinConstants.SYMBOL_BTCCNY, suggestedExitPrice, amountNeeded, action);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
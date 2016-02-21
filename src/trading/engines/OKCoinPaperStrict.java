package trading.engines;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.Bar;
import data.Model;
import data.downloaders.okcoin.websocket.NIAStatusSingleton;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import trading.Commission;
import trading.TradingSingleton;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class OKCoinPaperStrict extends TradingEngineBase {

	private final String TRADES_TABLE = "tradespaper";
	private final float MIN_TRADE_SIZE = .012f;
	private final float IDEAL_POSITION_FRACTION = .015f; // Of either cash or BTC on hand
	private final float ACCEPTABLE_SLIPPAGE = .0008f; // If market price is within .0x% of best price, make market order.
	
	private NIAStatusSingleton niass = null;
	
	public OKCoinPaperStrict() {
		super();
		niass = NIAStatusSingleton.getInstance();
	}
	
	@Override
	public void run() {
		while (running) {
			// Go through models and monitor opens & closes
			for (Model model : models) {
				try {
					HashMap<String, String> openMessages = new HashMap<String, String>();
					openMessages = monitorOpen(model);

					HashMap<String, String> closeMessages = new HashMap<String, String>();
					closeMessages = monitorClose(model);

					String jsonMessages = packageMessages(openMessages, closeMessages);
					ss.addJSONMessageToTradingMessageQueue(jsonMessages);	
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
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
			boolean includeSymbol = false;
			if (model.algo.equals("NaiveBayes")) {
				includeClose = false;
			}
			if (model.algo.equals("RandomForest")) {
				includeHour = false;
			}
			
			// Load data for classification
			ArrayList<ArrayList<Object>> unlabeledList = ARFF.createUnlabeledWekaArffData(periodStart, periodEnd, model.getBk(), false, false, includeClose, includeHour, includeSymbol, model.getMetrics(), metricDiscreteValueHash);
			Instances instances = Modelling.loadData(model.getMetrics(), unlabeledList, false, includeClose, includeHour, includeSymbol, 3); // I'm not sure if it's ok to not use weights here even if the model was built using weights.  I think it's ok because an instance you're evaluating is unclassified to begin with?
			
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
					
					// Determine the price for the trade
					float suggestedTradePrice = Float.parseFloat(priceString);
					double modelPrice = Double.parseDouble(priceString);
					double bestPrice = modelPrice;
					
					// This block is like a market order
					float estimatedBTCDesired = 0;
					if (direction.equals("bull")) {
						estimatedBTCDesired = getPositionSizeForBuyingBTC(IDEAL_POSITION_FRACTION, (float)bestPrice);
						bestPrice = estimateMarketOrderVWAP(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", estimatedBTCDesired);
					}
					else if (direction.equals("bear")) {
						estimatedBTCDesired = getPositionSizeForSellingBTC(IDEAL_POSITION_FRACTION, (float)bestPrice);
						bestPrice = estimateMarketOrderVWAP(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", estimatedBTCDesired);
					}
					
					System.out.println("Market order bestPrice: " + bestPrice);
					
					if (Double.isNaN(bestPrice)) {
						System.out.println("USING LIMIT");
						// This block is like a limit order
						if (direction.equals("bull")) {
							bestPrice = findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
							double bestMarketPrice = findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
							if (Math.abs(bestPrice - bestMarketPrice) < (bestPrice * ACCEPTABLE_SLIPPAGE)) {
								bestPrice = bestMarketPrice;
							}
						}
						else if (direction.equals("bear")) {
							bestPrice = findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
							double bestMarketPrice = findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
							if (Math.abs(bestPrice - bestMarketPrice) < (bestPrice * ACCEPTABLE_SLIPPAGE)) {
								bestPrice = bestMarketPrice;
							}
						}
					}
					else {
						System.out.println("USING MARKET");
					}
					
					System.out.println("Limit order bestPrice: " + bestPrice);
					
					// If the actual price is within .01% of the suggested price.  In live trading, I think this would manifest itself by placing a bid in this range
					if (Math.abs((bestPrice - suggestedTradePrice) / suggestedTradePrice) < ACCEPTABLE_SLIPPAGE) {
						float changeInBTC = 0;
						float changeInCash = 0;
						if (action.equals("Buy")) {
							changeInBTC = getPositionSizeForBuyingBTC(IDEAL_POSITION_FRACTION, (float)bestPrice);
							changeInCash = -(changeInBTC * (float)bestPrice);
							
						}
						if (action.equals("Sell")) {
							changeInBTC = -getPositionSizeForSellingBTC(IDEAL_POSITION_FRACTION, (float)bestPrice);
							changeInCash = -(changeInBTC * (float)bestPrice);
						}
						
						// Figure out position size
//						float cash = QueryManager.getTradingAccountCash();
//						float numShares = 1; // PositionSizing.getPositionSize(model.bk.symbol, actualTradePrice);
//						float commission = Commission.getOKCoinEstimatedCommission();
//						float tradeCost = (numShares * Float.parseFloat(priceString)) + commission;
						
						// Calculate the exit target
						float suggestedExitPrice = (float)bestPrice + ((float)bestPrice * model.getSellMetricValue() / 100f);
						float suggestedStopPrice = (float)bestPrice - ((float)bestPrice * model.getStopMetricValue() / 100f);
						if ((model.type.equals("bear") && action.equals("Buy")) || // Opposite trades
							(model.type.equals("bull") && action.equals("Sell"))) {
							suggestedExitPrice = (float)bestPrice - ((float)bestPrice * model.getStopMetricValue() / 100f);
							suggestedStopPrice = (float)bestPrice + ((float)bestPrice * model.getSellMetricValue() / 100f);
						}
							
						// Calculate the trades expiration time
						Calendar tradeBarEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), model.bk.duration);
						Calendar expiration = CalendarUtils.addBars(tradeBarEnd, model.bk.duration, model.numBars);
						
						// Send trade signal
						System.out.println("Opening " + model.type + " position on " + model.bk.symbol);
						QueryManager.makeTradeRequest(TRADES_TABLE, "Close Requested", direction, suggestedTradePrice, (float)bestPrice, suggestedExitPrice, suggestedStopPrice, Math.abs(changeInBTC), 0, model.bk.symbol, model.bk.duration.toString(), model.modelFile, expiration);
						QueryManager.updateTradingAccount(changeInCash, changeInBTC);
						QueryManager.insertRecordIntoPaperLoose((float)bestPrice);
					}
				}
			}
			
			messages.put("Action", action);
			messages.put("Time", sdf.format(c.getTime()));
			messages.put("SecondsRemaining", new Integer(secsUntilNextSignal).toString());
			messages.put("Model", model.getModelFile());
			messages.put("TestWinPercentage", new Double((double)Math.round(model.getTestWinPercent() * 1000) / 10).toString());
			messages.put("TestOppositeWinPercentage", new Double((double)Math.round(model.getTestBearWinPercent() * 1000) / 10).toString());
			messages.put("TestEstimatedAverageReturn", new Double((double)Math.round(model.getTestEstimatedAverageReturn() * 1000) / 1000).toString());
			messages.put("TestOppositeEstimatedAverageReturn", new Double((double)Math.round(model.getTestBearEstimatedAverageReturn() * 1000) / 1000).toString());
			messages.put("TestReturnPower", new Double((double)Math.round(model.getTestBullReturnPower() * 1000) / 1000).toString());
			messages.put("TestOppositeReturnPower", new Double((double)Math.round(model.getTestBearReturnPower() * 1000) / 1000).toString());
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
				java.sql.Timestamp openTradeTime = (java.sql.Timestamp)openPosition.get("opentradetime");
				int tempID = (int)openPosition.get("tempid");
				String symbol = openPosition.get("symbol").toString();
				String duration = openPosition.get("duration").toString();
				float filledAmount = (float)openPosition.get("filledamount");
				float suggestedEntryPrice = (float)openPosition.get("suggestedentryprice");
				float actualEntryPrice = (float)openPosition.get("actualentryprice");
				float suggestedExitPrice = (float)openPosition.get("suggestedexitprice");
				float suggestedStopPrice = (float)openPosition.get("suggestedstopprice");
				float commission = (float)openPosition.get("commission");
				java.sql.Timestamp expirationTimestamp = (java.sql.Timestamp)openPosition.get("expiration");
				Calendar expiration = Calendar.getInstance();
				expiration.setTimeInMillis(expirationTimestamp.getTime());
				
				// Get the current price for exit evaluation - in live trading this will be hitting a bid or putting out an ask
				HashMap<String, HashMap<String, String>> symbolDataHash = NIAStatusSingleton.getInstance().getSymbolDataHash();
				HashMap<String, String> tickHash = symbolDataHash.get(model.bk.symbol);
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
				boolean exit = false;
				
				// Check if this trade has expired and we need to exit
				if (Calendar.getInstance().after(expiration)) {
					exit = true;
					exitReason = "Expiration";
				}
				else if (type.equals("bull")) {
					if (currentPrice >= suggestedExitPrice) {
						exit = true;
						exitReason = "Target Hit";
					}
					if (currentPrice <= suggestedStopPrice) {
						exit = true;
						exitReason = "Stop Hit";
					}
				}
				else if (type.equals("bear")) {
					if (currentPrice <= suggestedExitPrice) {
						exit = true;
						exitReason = "Target Hit";
					}
					if (currentPrice >= suggestedStopPrice) {
						exit = true;
						exitReason = "Stop Hit";
					}
				}
					
				if (exit) {
					// Calculate some final values for this trade
					float addedCommission = Commission.getOKCoinEstimatedCommission();
					float totalCommission = commission + addedCommission;
					float changePerShare = currentPrice - actualEntryPrice;
					float revenue = (currentPrice * filledAmount) - addedCommission;
					float grossProfit = changePerShare * filledAmount;
					if (type.equals("bear"))
						grossProfit = -grossProfit;
					float netProfit = grossProfit - totalCommission;

					System.out.println("Exiting " + model.type + " position on " + model.bk.symbol);
					// Close the position
					QueryManager.closePosition(TRADES_TABLE, tempID, exitReason, currentPrice, totalCommission, netProfit, grossProfit);

					// Update Trading Account
					float changeInBTC = -filledAmount;
					float changeInCash = -changeInBTC * currentPrice;
					if (type.equals("bear")) {
						changeInBTC = filledAmount;
						changeInCash = -changeInBTC * currentPrice;
					}
					QueryManager.updateTradingAccount(changeInCash, changeInBTC);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	private float getPositionSizeForBuyingBTC(float fractionOfCashOnHand, float price) {
		try {
			float cashOnHand = QueryManager.getTradingAccountCash();
			float cashToBeUsed = cashOnHand * fractionOfCashOnHand;
			float btcPositionSize = cashToBeUsed / price;
			if (btcPositionSize < MIN_TRADE_SIZE) {
				btcPositionSize = MIN_TRADE_SIZE;
			}
			if (btcPositionSize * price > cashOnHand) {
				btcPositionSize = 0;
			}
			return btcPositionSize;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private float getPositionSizeForSellingBTC(float fractionOfBTCOnHand, float price) {
		try {
			float btcOnHand = QueryManager.getTradingAccountBTC();
			float btcPositionSize = btcOnHand * fractionOfBTCOnHand;
			if (btcPositionSize < MIN_TRADE_SIZE) {
				btcPositionSize = MIN_TRADE_SIZE;
			}
			if (btcPositionSize > btcOnHand) {
				btcPositionSize = 0;
			}
			return btcPositionSize;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
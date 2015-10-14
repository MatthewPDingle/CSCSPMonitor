package trading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;

import data.Bar;
import data.MetricKey;
import data.Model;
import data.downloaders.okcoin.OKCoinConstants;
import data.downloaders.okcoin.websocket.OKCoinWebSocketSingleton;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import servlets.trading.TradingSingleton;
import status.StatusSingleton;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class TradingThread extends Thread {

	private final int TRADING_WINDOW_MS = 5000; // How many milliseconds before the end of a bar trading is evaluated for real
	private final int TRADING_TIMEOUT = 30000; // How many milliseconds have to pass after a specific model has traded before it is allowed to trade again
	
	private boolean running = false;
	private StatusSingleton ss = null;
	private OKCoinWebSocketSingleton okss = null;
	private ArrayList<Model> models = new ArrayList<Model>();
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = new HashMap<MetricKey, ArrayList<Float>>();
	private String modelsPath = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public TradingThread() {
		ss = StatusSingleton.getInstance();
		okss = OKCoinWebSocketSingleton.getInstance();
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void setModels(ArrayList<Model> models) {
		this.models = models;
	}

	public void setMetricDiscreteValueHash(HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		this.metricDiscreteValueHash = metricDiscreteValueHash;
	}

	public void setModelsPath(String modelsPath) {
		this.modelsPath = modelsPath;
	}

	@Override
	public void run() {
		while (running) {
			// Get updated user info about funds
			okss.getUserInfo(OKCoinConstants.APIKEY, OKCoinConstants.SECRETKEY);
			
			long totalMonitorOpenTime = 0;
			long totalMonitorCloseTime = 0;
			for (Model model : models) {
				try {
					long t1 = Calendar.getInstance().getTimeInMillis();
					HashMap<String, String> openMessages = monitorOpenPaper(model);
//					HashMap<String, String> openMessages = monitorOpenLive(model);
					long t2 = Calendar.getInstance().getTimeInMillis();
					totalMonitorOpenTime += (t2 - t1);
					
					HashMap<String, String> closeMessages = monitorClosePaper(model);
					long t3 = Calendar.getInstance().getTimeInMillis();
					totalMonitorCloseTime += (t3 - t2);
	
					String jsonMessages = packageMessages(openMessages, closeMessages);
					ss.addJSONMessageToTradingMessageQueue(jsonMessages);	
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
//			System.out.println("monitorOpen x" + models.size() + " took " + totalMonitorOpenTime + "ms.");
//			System.out.println("monitorClose x" + models.size() + " took " + totalMonitorCloseTime + "ms.");
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {}
		}
	}
	
	private String packageMessages(HashMap<String, String> openMessages, HashMap<String, String> closeMessages) {
		String json = "[]";
		try {
			HashMap<String, String> allMessages = new HashMap<String, String>();
			allMessages.putAll(openMessages);
			allMessages.putAll(closeMessages);
			Gson gson = new Gson();
			json = gson.toJson(allMessages);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	private HashMap<String, String> monitorOpenPaper(Model model) {
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
					
					// Check the suggested trade price with what we can actually get.
					float suggestedTradePrice = Float.parseFloat(priceString);
					HashMap<String, HashMap<String, String>> symbolDataHash = OKCoinWebSocketSingleton.getInstance().getSymbolDataHash();
					HashMap<String, String> tickHash = symbolDataHash.get(model.bk.symbol);
					String lastTick = null;
					if (tickHash != null) {
						lastTick = tickHash.get("last");
					}
					Float actualTradePrice = null;
					if (lastTick != null) {
						actualTradePrice = Float.parseFloat(lastTick);
					}
					
					// If the actual price is within .01% of the suggested price.  In live trading, I think this would manifest itself by placing a bid in this range
					if (Math.abs((actualTradePrice - suggestedTradePrice) / suggestedTradePrice * 100f) < .01) {
						// Figure out position size
						float cash = QueryManager.getTradingAccountCash();
						float numShares = 1; // PositionSizing.getPositionSize(model.bk.symbol, actualTradePrice);
						float commission = Commission.getOKCoinEstimatedCommission();
						float tradeCost = (numShares * Float.parseFloat(priceString)) + commission;
						
						// Calculate the exit target
						float suggestedExitPrice = actualTradePrice + (actualTradePrice * model.getSellMetricValue() / 100f);
						float suggestedStopPrice = actualTradePrice - (actualTradePrice * model.getStopMetricValue() / 100f);
						if ((model.type.equals("bear") && action.equals("Buy")) || // Opposite trades
							(model.type.equals("bull") && action.equals("Sell"))) {
							suggestedExitPrice = actualTradePrice - (actualTradePrice * model.getSellMetricValue() / 100f);
							suggestedStopPrice = actualTradePrice + (actualTradePrice * model.getStopMetricValue() / 100f);
						}
							
						// Calculate the trades expiration time
						Calendar tradeBarEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), model.bk.duration);
						Calendar expiration = CalendarUtils.addBars(tradeBarEnd, model.bk.duration, model.numBars);
						
						// Send trade signal
						System.out.println("Opening " + model.type + " position on " + model.bk.symbol);
						QueryManager.makeTrade(direction, suggestedTradePrice, actualTradePrice, suggestedExitPrice, suggestedStopPrice, numShares, commission, model, expiration);
						QueryManager.updateTradingAccountCash(cash - tradeCost);
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
	
	private HashMap<String, String> monitorClosePaper(Model model) {
		HashMap<String, String> messages = new HashMap<String, String>();
		try {
			ArrayList<HashMap<String, Object>> openPositions = QueryManager.getOpenPositions();
			for (HashMap<String, Object> openPosition : openPositions) {
				String type = openPosition.get("type").toString();
				java.sql.Timestamp entryTimestamp = (java.sql.Timestamp)openPosition.get("entry");
//				Calendar entry = Calendar.getInstance();
//				entry.setTimeInMillis(entryTimestamp.getTime());
				String symbol = openPosition.get("symbol").toString();
				String duration = openPosition.get("duration").toString();
				float shares = (float)openPosition.get("shares");
				float suggestedEntryPrice = (float)openPosition.get("suggestedentryprice");
				float actualEntryPrice = (float)openPosition.get("actualentryprice");
				float suggestedExitPrice = (float)openPosition.get("suggestedexitprice");
				float suggestedStopPrice = (float)openPosition.get("suggestedstopprice");
				float commission = (float)openPosition.get("commission");
				java.sql.Timestamp expirationTimestamp = (java.sql.Timestamp)openPosition.get("expiration");
				Calendar expiration = Calendar.getInstance();
				expiration.setTimeInMillis(expirationTimestamp.getTime());
				
				// Get the current price for exit evaluation - in live trading this will be hitting a bid or putting out an ask
				HashMap<String, HashMap<String, String>> symbolDataHash = OKCoinWebSocketSingleton.getInstance().getSymbolDataHash();
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
					float revenue = (currentPrice * shares) - addedCommission;
					float grossProfit = changePerShare * shares;
					if (type.equals("bear"))
						grossProfit = -grossProfit;
					float netProfit = grossProfit - totalCommission;

					System.out.println("Exiting " + model.type + " position on " + model.bk.symbol);
					// Close the position
					QueryManager.closePosition(symbol, duration, entryTimestamp, exitReason, currentPrice, totalCommission, netProfit, grossProfit);
					// Add/Subtract money to/from account
					float accountValuePreClose = QueryManager.getTradingAccountCash();
					QueryManager.updateTradingAccountCash(accountValuePreClose + revenue);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	private HashMap<String, String> monitorOpenLive(Model model) {
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
					double modelPrice = Double.parseDouble(priceString);
					Double bestPrice = modelPrice;
					if (direction.equals("bull")) {
						bestPrice = findBestOrderBookPrice(okss.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
					}
					else if (direction.equals("bear")) {
						bestPrice = findBestOrderBookPrice(okss.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
					}
					
					// Calculate position size
					double amount = .01;
					//okss.getCnyOnHand()
					
//					String apiSymbol = OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(model.bk.symbol);
//					okss.spotTrade(OKCoinConstants.APIKEY, OKCoinConstants.SECRETKEY, apiSymbol, bestPrice, amount, action.toLowerCase());
					
//					// Check the suggested trade price with what we can actually get.
//					HashMap<String, HashMap<String, String>> symbolDataHash = OKCoinWebSocketSingleton.getInstance().getSymbolDataHash();
//					HashMap<String, String> tickHash = symbolDataHash.get(model.bk.symbol);
//					String lastTick = null;
//					if (tickHash != null) {
//						lastTick = tickHash.get("last");
//					}
//					Float actualTradePrice = null;
//					if (lastTick != null) {
//						actualTradePrice = Float.parseFloat(lastTick);
//					}
//					
//					// If the actual price is within .01% of the suggested price.  In live trading, I think this would manifest itself by placing a bid in this range
//					if (Math.abs((actualTradePrice - suggestedTradePrice) / suggestedTradePrice * 100f) < .01) {
//						// Figure out position size
//						float cash = QueryManager.getTradingAccountCash();
//						float numShares = 1; // PositionSizing.getPositionSize(model.bk.symbol, actualTradePrice);
//						float commission = Commission.getOKCoinEstimatedCommission();
//						float tradeCost = (numShares * Float.parseFloat(priceString)) + commission;
//						
//						// Calculate the exit target
//						float suggestedExitPrice = actualTradePrice + (actualTradePrice * model.getSellMetricValue() / 100f);
//						float suggestedStopPrice = actualTradePrice - (actualTradePrice * model.getStopMetricValue() / 100f);
//						if ((model.type.equals("bear") && action.equals("Buy")) || // Opposite trades
//							(model.type.equals("bull") && action.equals("Sell"))) {
//							suggestedExitPrice = actualTradePrice - (actualTradePrice * model.getSellMetricValue() / 100f);
//							suggestedStopPrice = actualTradePrice + (actualTradePrice * model.getStopMetricValue() / 100f);
//						}
//							
//						// Calculate the trades expiration time
//						Calendar tradeBarEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), model.bk.duration);
//						Calendar expiration = CalendarUtils.addBars(tradeBarEnd, model.bk.duration, model.numBars);
//						
//						// Send trade signal
//						System.out.println("Opening " + model.type + " position on " + model.bk.symbol);
//						QueryManager.makeTrade(direction, suggestedTradePrice, actualTradePrice, suggestedExitPrice, suggestedStopPrice, numShares, commission, model, expiration);
//						QueryManager.updateTradingAccountCash(cash - tradeCost);
//					}
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
	
	/**
	 * Finds the best price to place a limit order at.  If the best price in the order book would be better than the model 
	 * price, then use the best price in the order book +/- 1 pip.  Otherwise use the model price.
	 * 
	 * @param orderBook
	 * @param orderBookType
	 * @param modelPrice
	 * @return
	 */
	private double findBestOrderBookPrice(ArrayList<ArrayList<Double>> orderBook, String orderBookType, double modelPrice) {
		if (orderBookType.equals("bid")) {
			double bestBid = orderBook.get(0).get(0);
			double bestOBPrice = bestBid + OKCoinConstants.PIP_SIZE;
			if (bestOBPrice < modelPrice) {
				return bestOBPrice;
			}
			else {
				return modelPrice;
			}
		}
		else if (orderBookType.equals("ask")) {
			double bestAsk = orderBook.get(orderBook.size() - 1).get(0);
			double bestOBPrice = bestAsk - OKCoinConstants.PIP_SIZE;
			if (bestOBPrice > modelPrice) {
				return bestOBPrice;
			}
			else {
				return modelPrice;
			}
		}
		return modelPrice;
	}
}
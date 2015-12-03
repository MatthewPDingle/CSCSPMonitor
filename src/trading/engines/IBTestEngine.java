package trading.engines;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.Bar;
import data.Model;
import data.downloaders.interactivebrokers.IBWorker;
import data.downloaders.okcoin.OKCoinConstants;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import trading.TradingSingleton;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class IBTestEngine extends TradingEngineBase {

	private final int STALE_TRADE_SEC = 30; // How many seconds a trade can be open before it's considered "stale" and needs to be cancelled and re-issued.
	private final float MIN_TRADE_SIZE = 10f;
	
	private IBWorker ibWorker;
	
	public IBTestEngine(IBWorker ibWorker) {
		super();
		
		this.ibWorker = ibWorker;
	}
	
	public void setIbWorker(IBWorker ibWorker) {
		this.ibWorker = ibWorker;
	}

	@Override
	public void run() {
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
			
			// Monitor Closes
			long t1 = Calendar.getInstance().getTimeInMillis();
			
			HashMap<String, String> closeMessages = new HashMap<String, String>();
			closeMessages = monitorClose(null);
			String jsonMessages = packageMessages(new HashMap<String, String>(), closeMessages);
//			ss.addJSONMessageToTradingMessageQueue(jsonMessages);
			
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
			String priceString = new Double((double)Math.round(mostRecentBar.close * 100) / 100).toString();
			
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
//					if (direction.equals("bull")) {
//						bestPrice = findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
//						double bestMarketPrice = findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
//						if (Math.abs(bestPrice - bestMarketPrice) < (bestPrice * ACCEPTABLE_SLIPPAGE)) {
//							bestPrice = bestMarketPrice;
//						}
//					}
//					else if (direction.equals("bear")) {
//						bestPrice = findBestOrderBookPrice(niass.getSymbolAskOrderBook().get(model.bk.symbol), "ask", modelPrice);
//						double bestMarketPrice = findBestOrderBookPrice(niass.getSymbolBidOrderBook().get(model.bk.symbol), "bid", modelPrice);
//						if (Math.abs(bestPrice - bestMarketPrice) < (bestPrice * ACCEPTABLE_SLIPPAGE)) {
//							bestPrice = bestMarketPrice;
//						}
//					}
					
					// Finalize the action based on whether it's a market or limit order
					action = action.toLowerCase();
					
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
//						QueryManager.makeTradeRequest("trades", "Open Requested", direction, (float)modelPrice, null, suggestedExitPrice, suggestedStopPrice, (float)positionSize, 0f, model.bk.symbol, model.bk.duration.toString(), model.modelFile, expiration);
					
						// Send the trade order to OKCoin
						String apiSymbol = OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(model.bk.symbol);
//						niass.spotTrade(apiSymbol, bestPrice, positionSize, action);
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

		return null;
	}
	
	private double calculatePositionSize(String direction, double bestPrice) {
		return 10;
	}
}
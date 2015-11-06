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
import servlets.trading.TradingSingleton;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class OKCoinPaperRESTLoose extends TradingEngineBase {

	private final float MIN_TRADE_SIZE = .012f;
	private final float IDEAL_POSITION_FRACTION = .01f; // Of either cash or BTC on hand
	
	@Override
	public void run() {
		while (running) {
			for (Model model : models) {
				try {
					HashMap<String, String> openMessages = new HashMap<String, String>();
					openMessages = monitorOpen(model);
	
					HashMap<String, String> closeMessages = new HashMap<String, String>();
	
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
					HashMap<String, HashMap<String, String>> symbolDataHash = NIAStatusSingleton.getInstance().getSymbolDataHash();
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
						float changeInBTC = 0;
						float changeInCash = 0;
						if (action.equals("Buy")) {
							changeInBTC = getPositionSizeForBuyingBTC(IDEAL_POSITION_FRACTION, actualTradePrice);
							changeInCash = -(changeInBTC * suggestedTradePrice);
							
						}
						if (action.equals("Sell")) {
							changeInBTC = -getPositionSizeForSellingBTC(IDEAL_POSITION_FRACTION, actualTradePrice);
							changeInCash = -(changeInBTC * suggestedTradePrice);
						}
						QueryManager.updateTradingAccount(changeInCash, changeInBTC);
						QueryManager.insertRecordIntoPaperLoose(suggestedTradePrice);
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
		// No need for this for this engine
		return null;
	}

	private float getPositionSizeForBuyingBTC(float fractionOfCashOnHand, float price) {
		try {
			float cashOnHand = QueryManager.getTradingAccountCash();
			float cashToBeUsed = cashOnHand * fractionOfCashOnHand;
			float positionSize = cashToBeUsed / price;
			if (positionSize < MIN_TRADE_SIZE) {
				positionSize = 0;
			}
			return positionSize;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private float getPositionSizeForSellingBTC(float fractionOfBTCOnHand, float price) {
		try {
			float cashOnHand = QueryManager.getTradingAccountCash();
			float cashToBeUsed = cashOnHand * fractionOfBTCOnHand;
			float positionSize = cashToBeUsed / price;
			if (positionSize < MIN_TRADE_SIZE) {
				positionSize = 0;
			}
			return positionSize;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
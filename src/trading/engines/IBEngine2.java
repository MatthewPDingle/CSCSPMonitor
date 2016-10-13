package trading.engines;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import com.ib.controller.OrderType;

import data.Bar;
import data.Model;
import data.downloaders.interactivebrokers.IBConstants;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
import data.downloaders.interactivebrokers.IBConstants.ORDER_ACTION;
import dbio.BacktestQueryManager;
import dbio.IBQueryManager;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import test.backtest.BackTester;
import trading.TradingSingleton;
import utils.CalcUtils;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class IBEngine2 extends TradingEngineBase {

	// Configuration Options
		private boolean optionBacktest = false;
		private boolean optionUseBankroll = true;
		private boolean optionFridayCloseout = false;
		private boolean optionAdjustStops = false;
		private int optionNumWPOBs = 12;
		
		// Timing Options
		private final int STALE_TRADE_SEC = 60; 						// How many seconds a trade can be open before it's considered "stale" and needs to be cancelled and re-issued.
		private final int MIN_MINUTES_BETWEEN_NEW_OPENS = 4; 			// This is to prevent many highly correlated trades being placed over a tight timespan.  6 hours ok?
		private final int DEFAULT_EXPIRATION_DAYS = 25; 				// How many days later the trade should expire if not explicitly defined by the model
		private final int MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF = 60; 	// No new trades can be started this many minutes before close on Fridays (4PM Central)
		private final int MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT = 15; 	// All open trades get closed this many minutes before close on Fridays (4PM Central)
		
		// Order Options
		private final float MIN_TRADE_SIZE = 60000f; 					// USD
		private final float MAX_TRADE_SIZE = 140000f;					// USD
		private final float BASE_TRADE_SIZE = 120000f;					// USD
		private final int MAX_OPEN_ORDERS = 1; 							// Max simultaneous open orders.  IB has a limit of 15 per pair/symbol.
		private final int PIP_SPREAD_ON_EXPIRATION = 1; 				// If an close order expires, I set a tight limit & stop limit near the current price.  This is how many pips away from the bid & ask those orders are.

		// Model Options
		private final float MIN_WIN_PERCENT_OVER_BENCHMARK = .02f;   	// What winning percentage a model needs to be over the benchmark (IE .50, .666, .75, .333, .25, etc) to show in order to make a trade
		private final float MIN_DISTRIBUTION_FRACTION = .001f; 			// What percentage of the test set instances fell in a specific bucket
		private final float MIN_AVERAGE_WIN_PERCENT_INCREMENT = .000f; 	// This gets added on top of MIN_AVERAGE_WIN_PERCENT when multiple trades are open.
		
		// Global Variables
		private Calendar mostRecentOpenTime = null;
		private boolean noTradesDuringRoundCheckOK = true; 					// Only one model can request a trade per round (to prevent multiple models from trading at the same time and going against the min minutes required between orders)
		private boolean averageWinPercentCheckOK = false;
		private double averageWPOverUnderBenchmark = 0;
		private LinkedList<Double> lastXWPOBs = new LinkedList<Double>();
		private int countOpenOrders = 0;
		private int bankRoll = 240000;
		private String currentSignal = "";
		
		// Needed objects
		private IBWorker ibWorker;
		private IBSingleton ibs;
		
		public IBEngine2(IBWorker ibWorker) {
			super();

			this.ibWorker = ibWorker;
			if (!optionBacktest) {
				this.ibWorker.requestAccountInfoSubscription();
			}
			ibs = IBSingleton.getInstance();
			countOpenOrders = IBQueryManager.selectCountOpenOrders();
		}
		
		public void setIbWorker(IBWorker ibWorker) {
			this.ibWorker = ibWorker;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
					noTradesDuringRoundCheckOK = true;
					if (running) {
						try {
							// Monitor Opens per model
							synchronized (this) {
								// Model Monitor Open
								for (Model model : models) {						
									HashMap<String, String> openMessages = new HashMap<String, String>();
									openMessages = monitorOpen(model);
									
									String jsonMessages = packageMessages(openMessages, new HashMap<String, String>());
									ss.addJSONMessageToTradingMessageQueue(jsonMessages);	
								}
							}
							
							// Monitor API events
							if (optionBacktest) {
								monitorBacktestEvents();
							}
							else {
								long startAPIMonitoringTime = Calendar.getInstance().getTimeInMillis();
								long totalAPIMonitoringTime = 0;
								while (totalAPIMonitoringTime < 1000) { // Monitor the API for up to 1 second
									monitorIBWorkerTradingEvents();
									if (!optionBacktest) {
										Thread.sleep(10);
									}
									totalAPIMonitoringTime = Calendar.getInstance().getTimeInMillis() - startAPIMonitoringTime;
								}
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					else {
						if (!optionBacktest) {
							Thread.sleep(1000);
						}
					}
					if (optionBacktest) {
						BackTester.incrementBarWMDIndex();
					}
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
				
				String action = "Waiting";
				String prediction = "";
				double modelScore = 1;
				double bucketWinningPercentage = 0;

				// Load data for classification
				ArrayList<ArrayList<Object>> unlabeledList = new ArrayList<ArrayList<Object>>();
				if (optionBacktest) {
					unlabeledList = BackTester.createUnlabeledWekaArffData(BackTester.getCurrentPeriodStart(), model.getBk(), false, model.getMetrics(), metricDiscreteValueHash);
				}
				else {
					unlabeledList = ARFF.createUnlabeledWekaArffData(periodStart, periodEnd, model.getBk(), false, false, model.getMetrics(), metricDiscreteValueHash);
				}
				Instances instances = null;
				if (unlabeledList != null) {
					instances = Modelling.loadData(model.getMetrics(), unlabeledList, false, model.getNumClasses());
				}
				
				// Try loading the classifier from the memory cache in TradingSingleton.  Otherwise load it from disk and store it in the cache.
				Classifier classifier = TradingSingleton.getInstance().getWekaClassifierHash().get(model.getModelFile());
				if (classifier == null) { // As long as the models are being cached correctly during TradingSingleton init, this should never happen.
					classifier = Modelling.loadZippedModel(model.getModelFile(), modelsPath);
					TradingSingleton.getInstance().addClassifierToHash(model.getModelFile(), classifier);
				}
				
				if (instances != null && instances.firstInstance() != null) {
					// Make the prediction 
					double[] distribution = classifier.distributionForInstance(instances.firstInstance());
					modelScore = distribution[1]; // 0 - 1 range.  This is the model score, not winning %.  < .5 means Lose and > .5 means Win
					
					if (distribution.length == 2) {
						if (distribution[0] > distribution[1]) {
							prediction = "Down";
						}
						else {
							prediction = "Up";
						}
					}
					
					// Determine what Winning Percentage is needed given the risk / reward ratio.  50% needed for 1:1 setups, 33% needed for 2:1 setups, 25% needed for 3:1 setups, etc.
					double benchmarkWP = model.sellMetricValue / (model.sellMetricValue + model.stopMetricValue); 
					if (prediction.equals("Up")) {
						benchmarkWP = 1 - benchmarkWP;
					}
					HashMap<String, Object> modelData = QueryManager.getModelDataFromScore(model.id, modelScore);
					bucketWinningPercentage = (double)modelData.get("PercentCorrect");
					double wpOverUnderBenchmark = 0;
					if (!Double.isNaN(bucketWinningPercentage)) {
						wpOverUnderBenchmark = bucketWinningPercentage - benchmarkWP;
					}
					
					// Calculate what percentage of the instances were used to calculate this data.
					double distributionFraction = (int)modelData.get("InstanceCount") / (double)model.getTestDatasetSize();
					
					// Calculate what the winning percentage over the benchmark has to be based on the number of open orders.
					float totalIncrement = countOpenOrders * MIN_AVERAGE_WIN_PERCENT_INCREMENT;
					float currentMinWinPercentOverBenchmark = MIN_WIN_PERCENT_OVER_BENCHMARK + totalIncrement;

					// WPOB Tracking
					if (optionBacktest) {
						while (lastXWPOBs.size() <= optionNumWPOBs) { // Fill the whole thing during backtests.
							lastXWPOBs.addFirst(wpOverUnderBenchmark);
						}
					}
					else {
						lastXWPOBs.addFirst(wpOverUnderBenchmark);
					}
					while (lastXWPOBs.size() > optionNumWPOBs) {
						lastXWPOBs.removeLast();
					}
					
					// Time Checks
					boolean timingOK = false;
					if (model.lastActionTime == null) {
						if (barRemainingMS < TRADING_WINDOW_MS) {
							timingOK = true;
						}
					}
					else {
						double msSinceLastTrade = c.getTimeInMillis() - model.lastActionTime.getTimeInMillis();
						if (msSinceLastTrade > TRADING_TIMEOUT) {
							if (barRemainingMS < TRADING_WINDOW_MS) {
								timingOK = true;
							}
						}
					}
					if (optionBacktest) {
						timingOK = true;
					}
					
					// Determine which action to take (Buy, Sell, Buy Signal, Sell Signal, Close Long, Close Short, Waiting)
					if (model.tradeOffPrimary || model.useInBackTests) {
						if (prediction.equals("Up")) {
							double targetClose = (double)mostRecentBar.close * (1d + ((double)model.sellMetricValue / 100d));
							double targetStop = (double)mostRecentBar.close * (1d - ((double)model.stopMetricValue / 100d));
							if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark >= currentMinWinPercentOverBenchmark && averageLastXWPOBs() >= currentMinWinPercentOverBenchmark) {
								action = "Buy";
								model.lastActionPrice = priceString;
								model.lastAction = action;
								model.lastActionTime = c;
								model.lastTargetClose = new Double((double)Math.round(targetClose * 100) / 100).toString();;
								model.lastStopClose = new Double((double)Math.round(targetStop * 100) / 100).toString();
							}
							else if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark >= 0 && averageLastXWPOBs() >= 0) {
								action = "Close Short";
							}
							else {
								action = "Buy Signal";
							}
						}
						else if (prediction.equals("Down")) {
							double targetClose = (double)mostRecentBar.close * (1d - ((double)model.sellMetricValue / 100d));
							double targetStop = (double)mostRecentBar.close * (1d + ((double)model.stopMetricValue / 100d));
							if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark >= currentMinWinPercentOverBenchmark && averageLastXWPOBs() >= currentMinWinPercentOverBenchmark) {
								action = "Sell";
								model.lastActionPrice = priceString;
								model.lastAction = action;
								model.lastActionTime = c;
								model.lastTargetClose = new Double((double)Math.round(targetClose * 100) / 100).toString();
								model.lastStopClose = new Double((double)Math.round(targetStop * 100) / 100).toString();
							}
							else if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark >= 0 && averageLastXWPOBs() >= 0) {
								action = "Close Long";
							}
							else {
								action = "Sell Signal";
							}
						}
					}
					
					if (action.startsWith("Buy")) {
						currentSignal = "Buy";
					}
					else if (action.startsWith("Sell")) {
						currentSignal = "Sell";
					}
					
					// Model says Buy or Sell - Do final checks to see if we can trade
					if (action.equals("Buy") || action.equals("Sell") || action.equals("Close Short") || action.equals("Close Long")) {					
						// Get the direction of the trade
						String direction = "";
						ORDER_ACTION orderAction = null;
						if (action.equals("Buy") || action.equals("Close Short")) {
							direction = "bull";
							orderAction = ORDER_ACTION.BUY;
						}
						else if (action.equals("Sell") || action.equals("Close Long")) {
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
						if (optionBacktest) {
							if (direction.equals("bull")) {
								likelyFillPrice = BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD);
							}
							else if (direction.equals("bear")) {
								likelyFillPrice = BackTester.getCurrentBid(IBConstants.TICK_NAME_FOREX_EUR_USD);
							}
						}
						double suggestedEntryPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format(likelyFillPrice)));
						
						// Finalize the action based on whether it's a market or limit order
						action = action.toLowerCase();
						
						// Calculate position size.
						int positionSize = calculatePositionSize(wpOverUnderBenchmark, distributionFraction, action);
						boolean positionSizeCheckOK = true;
						if (positionSize > 0) {
							positionSizeCheckOK = true;
						}
						
						// Calculate the exit target
						double suggestedExitPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice + (likelyFillPrice * model.getSellMetricValue() / 100d)))));
						double suggestedStopPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice - (likelyFillPrice * .5 / 100d)))));
						if ((model.type.equals("bear") && action.equals("buy")) || // Opposite trades
							(model.type.equals("bull") && action.equals("sell"))) {
							suggestedExitPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice - (likelyFillPrice * model.getSellMetricValue() / 100d)))));
							suggestedStopPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice + (likelyFillPrice * .5 / 100d)))));
						}
						
						// Calculate the trades expiration time
						Calendar expiration = Calendar.getInstance();
						if (optionBacktest) {
							expiration.setTimeInMillis(BackTester.getCurrentPeriodEnd().getTimeInMillis());
						}
						if (model.numClasses == 2) { // 2 classes = Win/Lose.  There shouldn't really be an expiration
							expiration.add(Calendar.DATE, DEFAULT_EXPIRATION_DAYS);
						}
						else {
							Calendar tradeBarEnd = CalendarUtils.getBarEnd(Calendar.getInstance(), model.bk.duration);
							expiration = CalendarUtils.addBars(tradeBarEnd, model.bk.duration, model.numBars);
						}
						
						// Calculate the open order's expiration time
						Calendar openOrderExpiration = Calendar.getInstance();
						if (optionBacktest) {
							openOrderExpiration.setTimeInMillis(BackTester.getCurrentPeriodEnd().getTimeInMillis());
						}
						openOrderExpiration.setTimeInMillis(openOrderExpiration.getTimeInMillis() + (STALE_TRADE_SEC * 1000));
						
						// Check how long it's been since the last open order
						boolean openRateLimitCheckOK = true;
						if (mostRecentOpenTime == null) {
							if (optionBacktest) {
								Calendar result = BacktestQueryManager.backtestGetMostRecentFilledTime();
								if (result != null) {
									mostRecentOpenTime = Calendar.getInstance();
									mostRecentOpenTime.setTimeInMillis(result.getTimeInMillis());
								}
							}
							else {
								Calendar result = IBQueryManager.getMostRecentFilledTime();
								if (result != null) {
									mostRecentOpenTime = Calendar.getInstance();
									mostRecentOpenTime.setTimeInMillis(result.getTimeInMillis());
								}
							}
							
						}
						if (mostRecentOpenTime != null) {
							long mostRecentOpenTimeMS = mostRecentOpenTime.getTimeInMillis();
							long nowMS = Calendar.getInstance().getTimeInMillis();
							if (optionBacktest) {
								nowMS = BackTester.getCurrentPeriodEnd().getTimeInMillis();
							}
							if (nowMS - mostRecentOpenTimeMS < (MIN_MINUTES_BETWEEN_NEW_OPENS * 60 * 1000)) {
								openRateLimitCheckOK = false;
								action = "Waiting";
							}
						}
						
						// Check to make sure there are fewer than 10 open orders (15 is the IB limit)
						if (optionBacktest) {
							countOpenOrders = BacktestQueryManager.backtestSelectCountOpenOrders();
						}
						else {
							countOpenOrders = IBQueryManager.selectCountOpenOrders();
						}
						boolean numOpenOrderCheckOK = true;
						if (countOpenOrders >= MAX_OPEN_ORDERS) {
							numOpenOrderCheckOK = false;
						}
						
						// Friday cutoff check
						boolean beforeFridayCutoffCheckOK = beforeFridayCutoff();
						if (optionBacktest) {
							beforeFridayCutoffCheckOK = beforeFridayCutoff(BackTester.getCurrentPeriodEnd());
						}
					
						// Final checks
						if (openRateLimitCheckOK && positionSizeCheckOK && beforeFridayCutoffCheckOK) {
							// Check to see if this model has an open opposite order that should simply be closed instead of 
							HashMap<String, Object> orderInfo;
							if (optionBacktest) {
								orderInfo = BacktestQueryManager.backtestFindOppositeOpenOrderToCancel(null, direction);
							}
							else {
								orderInfo = IBQueryManager.findOppositeOpenOrderToCancel(model, direction);
							}
							
							// No opposite side order to cancel.  Make new trade request in the DB
							if (orderInfo == null || orderInfo.size() == 0) {
								if (action.equals("buy") || action.equals("sell")) {
									// Record order request in DB
									Calendar statusTime = null;
									String runName = null;
									int orderID = -1;
									if (optionBacktest) {
										if (/*action.equals("buy") && */numOpenOrderCheckOK) { // I have this because my models on 9/17 are only good towards bull models.
											statusTime = BackTester.getCurrentPeriodEnd();
											runName = BackTester.getRunName();
											orderID = BacktestQueryManager.backtestRecordTradeRequest(OrderType.LMT.toString(), orderAction.toString(), "Open Requested", statusTime,
													direction, model.bk, suggestedEntryPrice, suggestedExitPrice, suggestedStopPrice, positionSize, model.modelFile, averageLastXWPOBs(), wpOverUnderBenchmark, expiration, runName);
										}
									}
									else {
										orderID = IBQueryManager.recordTradeRequest(OrderType.LMT.toString(), orderAction.toString(), "Open Requested", statusTime,
												direction, model.bk, suggestedEntryPrice, suggestedExitPrice, suggestedStopPrice, positionSize, model.modelFile, averageLastXWPOBs(), wpOverUnderBenchmark, expiration, runName);
									}
										
									// Send the trade order to IB
									if (!optionBacktest) {
										ibWorker.placeOrder(orderID, null, OrderType.LMT, orderAction, positionSize, null, suggestedEntryPrice, false, openOrderExpiration);
									}
									System.out.println(model.modelFile + " Placed order : " + orderAction + " " + positionSize + " at " + suggestedEntryPrice);
								}
							}
							// Opposite side order is available to cancel.  Cancel that instead by setting a tight close & stop.
							else {
								// Unpack some of the order data
								int openOrderID = Integer.parseInt(orderInfo.get("ibopenorderid").toString());
								int closeOrderID = Integer.parseInt(orderInfo.get("ibcloseorderid").toString());
								int stopOrderID = Integer.parseInt(orderInfo.get("ibstoporderid").toString());
								int filledAmount = 0;
								if (orderInfo.get("filledamount") != null) {
									filledAmount = Integer.parseInt(orderInfo.get("filledamount").toString());
								}
								int closeFilledAmount = 0;
								if (orderInfo.get("closefilledamount") != null) {
									closeFilledAmount = ((BigInteger)orderInfo.get("closefilledamount")).intValue();
								}
								int remainingAmountNeededToClose = filledAmount - closeFilledAmount;
								String existingOrderDirection = orderInfo.get("direction").toString();
								String openAction = orderInfo.get("iborderaction").toString();
								
								ORDER_ACTION closeAction = ORDER_ACTION.SELL;
								if (openAction.equals("SELL")) {
									closeAction = ORDER_ACTION.BUY;
								}
								
								// Cancel the existing close & stop
								if (!optionBacktest) {
									ibWorker.cancelOrder(closeOrderID);
									ibWorker.cancelOrder(stopOrderID);
								}
								
								// Update the note on the order to say it was cut short
								if (optionBacktest) {
									BacktestQueryManager.backtestUpdateOrderNote(openOrderID, "Cut Short");
								}
								else {
									IBQueryManager.updateOrderNote(openOrderID, "Cut Short");
								}
								
								// Cutting short an order because a model wants to fire in the opposite direction counts towards mostRecentOpenTime.
								if (optionBacktest) {
									mostRecentOpenTime = BackTester.getCurrentPeriodEnd();
								}
								else {
									mostRecentOpenTime = Calendar.getInstance();
								}
								
								// Make new tight close & stop to effectively cancel.
								
								// Get a One-Cancels-All group ID
								int ibOCAGroup = -1;
								if (optionBacktest) {
									ibOCAGroup = BacktestQueryManager.backtestGetIBOCAGroup();
								}
								else {
									ibOCAGroup = IBQueryManager.getIBOCAGroup();
								}
								
								// Get prices a couple pips on each side of the bid/ask spread
								double currentBid = 0;
								double currentAsk = 0;
								if (optionBacktest) {
									currentBid = BackTester.getCurrentBid(ibWorker.getBarKey().symbol);
									currentAsk = BackTester.getCurrentAsk(ibWorker.getBarKey().symbol);
								}
								else {
									Double rawCurrentBid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
									Double rawCurrentAsk = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
									currentBid = (rawCurrentBid != null ? Double.parseDouble(df5.format(rawCurrentBid)) : 0);
									currentAsk = (rawCurrentAsk != null ? Double.parseDouble(df5.format(rawCurrentAsk)) : 0);
								}
								double askPlus2Pips = currentAsk + (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
								double bidMinus2Pips = currentBid - (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
								currentAsk = CalcUtils.roundTo5DigitHalfPip(currentAsk);
								currentBid = CalcUtils.roundTo5DigitHalfPip(currentBid);
								askPlus2Pips = CalcUtils.roundTo5DigitHalfPip(askPlus2Pips);
								bidMinus2Pips = CalcUtils.roundTo5DigitHalfPip(bidMinus2Pips);
								double askPlus1p5Pips = askPlus2Pips -(IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol) / 2d);
								askPlus1p5Pips = CalcUtils.roundTo5DigitHalfPip(askPlus1p5Pips);
								double bidMinus1p5Pips = bidMinus2Pips +(IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol) / 2d);
								bidMinus1p5Pips = CalcUtils.roundTo5DigitHalfPip(bidMinus1p5Pips);
								
								// Make a good-till-date far in the future
								Calendar gtd = Calendar.getInstance();
								gtd.add(Calendar.DATE, 1);
								
								Calendar statusTime = null;
								if (optionBacktest) {
									// Close the opposite order
									statusTime = BackTester.getCurrentPeriodEnd();
									if (existingOrderDirection.equals("bull") && action.equals("close long")) {
										BacktestQueryManager.backtestRecordClose("Open", openOrderID, currentAsk, "Cut Short", filledAmount, existingOrderDirection, BackTester.getCurrentPeriodEnd());
										BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, currentAsk));
									}
									else if (existingOrderDirection.equals("bear") && action.equals("close short")) {
										BacktestQueryManager.backtestRecordClose("Open", openOrderID, currentBid, "Cut Short", filledAmount, existingOrderDirection, BackTester.getCurrentPeriodEnd());
										BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, currentBid));
									}
									Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(openOrderID);
									if (optionBacktest && proceeds != null) {
										bankRoll += proceeds;
									}	
								}
								else {
									if (existingOrderDirection.equals("bull")) {
										// Make the new close trade
										int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(closeOrderID, ibOCAGroup, statusTime);
										ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, askPlus2Pips, false, gtd);
										System.out.println("Bull Close cancelled due to opposite order being available.  Making new Close.  " + newCloseOrderID + " in place of " + closeOrderID + ", " + askPlus2Pips);
										System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
										
										// Make the new stop trade
										int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
										ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, bidMinus1p5Pips, bidMinus2Pips, false, gtd);
										System.out.println("Bull Stop cancelled due to opposite order being available.  Making new Stop.  " + newStopOrderID + " in place of " + closeOrderID + ", " + bidMinus2Pips);
										System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + gtd.getTime().toString());
									}
									else {
										// Make the new close trade
										int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(closeOrderID, ibOCAGroup, statusTime);
										ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, bidMinus2Pips, false, gtd);
										System.out.println("Bear Close cancelled due to opposite order being available.  Making new Close.  " + newCloseOrderID + " in place of " + closeOrderID + ", " + bidMinus2Pips);
										System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + gtd.getTime().toString());
										
										// Make the new stop trade
										int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
										ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, askPlus1p5Pips, askPlus2Pips, false, gtd);
										System.out.println("Bear Stop cancelled due to opposite order being available.  Making new Stop.  " + newStopOrderID + " in place of " + closeOrderID + ", " + askPlus2Pips);
										System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
									}
								}
							}
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
//				confidence = Math.random(); // This can be used for testing the GUI outside of trading hours.
				messages.put("Confidence", df5.format(modelScore));
				messages.put("WinningPercentage", df5.format(bucketWinningPercentage));
				messages.put("PredictionDistributionPercentage", df5.format(model.predictionDistributionPercentage));
				messages.put("TestBucketPercentCorrect", model.getTestBucketPercentCorrectJSON());
				messages.put("TestBucketDistribution", model.getTestBucketDistributionJSON());
				if (averageWPOverUnderBenchmark != 0 && models.indexOf(model) == 0) { // Only need to send this message once per round (not for every model) and not during that timeout period after the end of a bar.
					messages.put("AverageWinningPercentage", df5.format(averageWPOverUnderBenchmark));
				}
				messages.put("AverageLast500AWPs", /*df5.format(averageLastXAWPs())*/df5.format(bucketWinningPercentage) );
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
		
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return messages;
		}
		
		public void monitorBacktestEvents() {
			try {
				// Open Requested Events - Either to Filled or Cancelled
				ArrayList<HashMap<String, Object>> openRequestedHashList = BacktestQueryManager.backtestGetOpenRequestedOrders();
				for (HashMap<String, Object> orderHash : openRequestedHashList) {
					int openOrderID = Integer.parseInt(orderHash.get("ibopenorderid").toString());
					int requestedAmount = Integer.parseInt(orderHash.get("requestedamount").toString());
					double actualEntryPrice = Double.parseDouble(orderHash.get("suggestedentryprice").toString());
					
					if (Math.random() < BackTester.CHANCE_OF_OPEN_ORDER_BEING_FILLED) {
						BacktestQueryManager.backtestUpdateOpen(openOrderID, "Filled", requestedAmount, actualEntryPrice, -1, BackTester.getCurrentPeriodEnd());
						mostRecentOpenTime = BackTester.getCurrentPeriodEnd();
						bankRoll -= (requestedAmount * actualEntryPrice);
					}
					else {
						BacktestQueryManager.backtestCancelOpenOrder(openOrderID);
					}
				}
				
				// Filled Events - Either to Closed or staying at Filled
				ArrayList<HashMap<String, Object>> filledHashList = BacktestQueryManager.backtestGetFilledOrders(BackTester.getCurrentPeriodEnd());
				for (HashMap<String, Object> orderHash : filledHashList) {
					int openOrderID = Integer.parseInt(orderHash.get("ibopenorderid").toString());
					int filledAmount = Integer.parseInt(orderHash.get("filledamount").toString());
					double suggestedExitPrice = Double.parseDouble(orderHash.get("suggestedexitprice").toString());
					double suggestedStopPrice = Double.parseDouble(orderHash.get("suggestedstopprice").toString());
					double sellMetricValue = Double.parseDouble(orderHash.get("sellmetricvalue").toString());
					double stopMetricValue = Double.parseDouble(orderHash.get("stopmetricvalue").toString());
					String direction = orderHash.get("direction").toString();
					Calendar expirationC = (Calendar)orderHash.get("expiration");
					
					double currentBid = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentBid(IBConstants.TICK_NAME_FOREX_EUR_USD));
					double currentAsk = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD));
					double currentHigh = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentHigh(IBConstants.TICK_NAME_FOREX_EUR_USD));
					double currentLow = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentLow(IBConstants.TICK_NAME_FOREX_EUR_USD));
					
					// See if the target, stop, or both got hit.
					String event = "";
					if (	(direction.equals("bull") && currentHigh >= suggestedExitPrice && currentLow <= suggestedStopPrice) ||
							(direction.equals("bear") && currentLow <= suggestedExitPrice && currentHigh >= suggestedStopPrice)) {
						// Both the target and the stop got hit during the same bar, so estimate what the probability of each being hit first is and choose one at random
						double sellPercentChance = stopMetricValue / (double)(sellMetricValue + stopMetricValue);
						
						if (direction.equals("bull")) {
							if (Math.random() <= sellPercentChance) {
								event = "Target Hit";
							}
							else {
								event = "Stop Hit";
							}
						}
						if (direction.equals("bear")) {
							if (Math.random() >= sellPercentChance) {
								event = "Target Hit";
							}
							else {
								event = "Stop Hit";
							}
						}
						System.out.println("Random " + event);
					}
					else if ((direction.equals("bull") && currentHigh >= suggestedExitPrice) ||
							(direction.equals("bear") && currentLow <= suggestedExitPrice)) {	
						event = "Target Hit";
					}
					else if ((direction.equals("bull") && currentLow <= suggestedStopPrice) ||
							 (direction.equals("bear") && currentHigh >= suggestedStopPrice)) {
						event = "Stop Hit";
					}
							
					if (event.equals("Target Hit")) {	
						// Target Hit
					}
					else if (event.equals("Stop Hit")) {
						// Stop Hit
//						double tradePrice = 0d;
//						if (direction.equals("bull")) {
//							tradePrice = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD));
//						}
//						else if (direction.equals("bear")) {
//							tradePrice = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentBid(IBConstants.TICK_NAME_FOREX_EUR_USD));
//						}
//						BacktestQueryManager.backtestRecordClose("Open", openOrderID, suggestedStopPrice, "Stop Hit", filledAmount, direction, BackTester.getCurrentPeriodEnd());
//						BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, suggestedStopPrice));
//						Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(openOrderID);
//						if (optionBacktest && proceeds != null) {
//							bankRoll += proceeds;
//						}
					}
					else if (BackTester.getCurrentPeriodEnd().getTimeInMillis() > expirationC.getTimeInMillis()) {
						// Expiration
						double tradePrice = 0d;
						if (direction.equals("bull")) {
							tradePrice = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD));
						}
						else if (direction.equals("bear")) {
							tradePrice = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentBid(IBConstants.TICK_NAME_FOREX_EUR_USD));
						}
						BacktestQueryManager.backtestRecordClose("Open", openOrderID, tradePrice, "Expiration", filledAmount, direction, BackTester.getCurrentPeriodEnd());
						BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, tradePrice));
						Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(openOrderID);
						if (optionBacktest && proceeds != null) {
							bankRoll += proceeds;
						}
					}
					else if (fridayCloseoutTime(BackTester.getCurrentPeriodEnd())) {
						// Closeout
						double tradePrice = 0d;
						if (direction.equals("bull")) {
							tradePrice = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD));
						}
						else if (direction.equals("bear")) {
							tradePrice = CalcUtils.roundTo5DigitHalfPip(BackTester.getCurrentBid(IBConstants.TICK_NAME_FOREX_EUR_USD));
						}
						BacktestQueryManager.backtestRecordClose("Open", openOrderID, tradePrice, "Closeout", filledAmount, direction, BackTester.getCurrentPeriodEnd());
						BacktestQueryManager.backtestNoteCloseout("Open", openOrderID);
						BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, tradePrice));
						Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(openOrderID);
						if (optionBacktest && proceeds != null) {
							bankRoll += proceeds;
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void monitorIBWorkerTradingEvents() {
			try {
				HashMap<String, LinkedList<HashMap<String, Object>>> tradingEventDataHash = ibWorker.getEventDataHash();
				if (tradingEventDataHash != null) {
					// error
					LinkedList<HashMap<String, Object>> errorDataHashList = tradingEventDataHash.get("error");
					if (errorDataHashList != null) {
						while (errorDataHashList.size() > 0) {
							HashMap<String, Object> dataHash = errorDataHashList.pop();
							processErrorEvents(dataHash);
						}
					}
					// orderStatus
					LinkedList<HashMap<String, Object>> orderStatusDataHashList = tradingEventDataHash.get("orderStatus");
					if (orderStatusDataHashList != null) {
						while (orderStatusDataHashList.size() > 0) {
							HashMap<String, Object> dataHash = orderStatusDataHashList.pop();
							processOrderStatusEvents(dataHash);
						}
					}
					// commissionReport
					LinkedList<HashMap<String, Object>> commissionReportDataHashList = tradingEventDataHash.get("commissionReport");
					if (commissionReportDataHashList != null) {
						while (commissionReportDataHashList.size() > 0) {
							HashMap<String, Object> dataHash = commissionReportDataHashList.pop();
							processCommissionReportEvents(dataHash);
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/** 
		 * These events only come from IBWorker
		 * 
		 * @param orderStatusDataHash
		 */
		private void processOrderStatusEvents(HashMap<String, Object> orderStatusDataHash) {
			try {
				// Unpack the parameters
				int orderId = (int)orderStatusDataHash.get("orderId");
				String status = orderStatusDataHash.get("status").toString();
				int filled = (int)orderStatusDataHash.get("filled");
				double avgFillPrice = (double)orderStatusDataHash.get("avgFillPrice");
				avgFillPrice = CalcUtils.roundTo5DigitHalfPip(avgFillPrice);
				int parentId = (int)orderStatusDataHash.get("parentId");
				
				// Get the needed fields from the order
				String orderType = IBQueryManager.getOrderIDType(orderId);
				if (orderType.equals("Unknown")) {
					// This would happen when a close & stop expire, and the close comes through here first, cancels both, then makes new close & stop orders, then the original cancel comes through here and isn't found.
					System.out.println(orderId + " was not found in the DB.");
					return;
				}
				HashMap<String, Object> fieldHash = IBQueryManager.getOrderInfo(orderType, orderId);
				if (fieldHash.get("direction") == null) {
					System.out.println("inspect");
				}
				String openAction = "";
				if (fieldHash.get("iborderaction") != null) {
					openAction = fieldHash.get("iborderaction").toString();
				}
				ORDER_ACTION closeAction = ORDER_ACTION.SELL;
				if (openAction.equals("SELL")) {
					closeAction = ORDER_ACTION.BUY;
				}
				String direction = "";
				if (fieldHash.get("direction") != null) {
					direction = fieldHash.get("direction").toString();
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
						IBQueryManager.updateOpen(orderId, status, filled, avgFillPrice, parentId, null);
	 
						mostRecentOpenTime = Calendar.getInstance();
						
						boolean needsCloseAndStop = IBQueryManager.checkIfNeedsCloseAndStopOrders(orderId);
						if (needsCloseAndStop) {
							// Get a One-Cancels-All group ID
							int ibOCAGroup = IBQueryManager.getIBOCAGroup();
							
							// Get the stop price (either the bid or ask), to use to trigger the stop
							double stopTrigger = suggestedStopPrice - (IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol) / 2d);
							if (direction.equals("bull")) {
								stopTrigger = suggestedStopPrice + (IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol) / 2d);
							}
							stopTrigger = CalcUtils.roundTo5DigitHalfPip(stopTrigger);
							
							// Make the close trade
							int closeOrderID = IBQueryManager.recordCloseTradeRequest(orderId, ibOCAGroup);		
							ibWorker.placeOrder(closeOrderID, ibOCAGroup, OrderType.LMT, closeAction, filled, null, suggestedExitPrice, false, expiration);
							
							// Make the stop trade
							int stopOrderID = IBQueryManager.recordStopTradeRequest(orderId);		
							ibWorker.placeOrder(stopOrderID, ibOCAGroup, OrderType.STP, closeAction, filled, stopTrigger, suggestedStopPrice, false, expiration);
						}
					}
					// Close Filled.  Need to close out order
					if (orderType.equals("Close")) {
						System.out.println("Recording close : " + avgFillPrice);
						if (Calendar.getInstance().getTimeInMillis() > expiration.getTimeInMillis()) {
							IBQueryManager.recordClose(orderType, orderId, avgFillPrice, "Expiration", filled, direction, null);
						}
						else {
							IBQueryManager.recordClose(orderType, orderId, avgFillPrice, "Target Hit", filled, direction, null);
						}
					}
					// Stop Filled.  Need to close out order
					if (orderType.equals("Stop")) {
						System.out.println("Recording stop : " + avgFillPrice);
						if (Calendar.getInstance().getTimeInMillis() > expiration.getTimeInMillis()) {
							IBQueryManager.recordClose(orderType, orderId, avgFillPrice, "Expiration", filled, direction, null);
						}
						else {
							IBQueryManager.recordClose(orderType, orderId, avgFillPrice, "Stop Hit", filled, direction, null);
						}
					}
				}
				else if (status.equals("Submitted")) { // Submitted includes partial fills
					if (orderType.equals("Open")) {
						IBQueryManager.updateOpen(orderId, status, filled, avgFillPrice, parentId, null);
					}
					if (orderType.equals("Close")) {
						IBQueryManager.updateClose(orderId, filled, avgFillPrice, parentId, null);
					}
					if (orderType.equals("Stop")) {
						IBQueryManager.updateStop(orderId, filled, avgFillPrice, parentId, null);
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
						boolean fridayCloseout = fridayCloseoutTime();
						if ((expired || fridayCloseout) && remainingAmountNeededToClose > 0) {
							// Make a new Limit Close & Stop Limit Stop in the same OCA group tight against the current price and don't worry about these expiring because they won't last long.
							
							// Get a One-Cancels-All group ID
							int ibOCAGroup = IBQueryManager.getIBOCAGroup();
							
							// If this order was cancelled due to friday closeout, note it
							if (fridayCloseout) {
								IBQueryManager.noteCloseout("Close", orderId);
							}
							
							// Get prices a couple pips on each side of the bid/ask spread
							double ask = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
							double askPlus2Pips = ask + (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
							double bid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
							double bidMinus2Pips = bid - (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
							ask = CalcUtils.roundTo5DigitHalfPip(ask);
							bid = CalcUtils.roundTo5DigitHalfPip(bid);
							askPlus2Pips = CalcUtils.roundTo5DigitHalfPip(askPlus2Pips);
							bidMinus2Pips = CalcUtils.roundTo5DigitHalfPip(bidMinus2Pips);
							double askPlus1p5Pips = askPlus2Pips -(IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol) / 2d);
							askPlus1p5Pips = CalcUtils.roundTo5DigitHalfPip(askPlus1p5Pips);
							double bidMinus1p5Pips = bidMinus2Pips +(IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol) / 2d);
							bidMinus1p5Pips = CalcUtils.roundTo5DigitHalfPip(bidMinus1p5Pips);
							
							// Make a good-till-date far in the future
							Calendar gtd = Calendar.getInstance();
							gtd.add(Calendar.DATE, 100);
							
							Calendar statusTime = null;
							if (optionBacktest) {
								statusTime = BackTester.getCurrentPeriodEnd();
							}
							
							if (direction.equals("bull")) {
								// Make the new close trade
								int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(orderId, ibOCAGroup, statusTime);
								ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, askPlus2Pips, false, gtd);
								System.out.println("Bull Close Expired.  Making new Close.  " + newCloseOrderID + " in place of " + orderId + ", " + askPlus2Pips);
								System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
								
								// Make the new stop trade
								int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
								ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, bidMinus1p5Pips, bidMinus2Pips, false, gtd);
								System.out.println("Bull Stop Expired.  Making new Stop.  " + newStopOrderID + " in place of " + orderId + ", " + bidMinus2Pips);
								System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + gtd.getTime().toString());
							}
							else {
								// Make the new close trade
								int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(orderId, ibOCAGroup, statusTime);
								ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, bidMinus2Pips, false, gtd);
								System.out.println("Bear Close Expired.  Making new Close.  " + newCloseOrderID + " in place of " + orderId + ", " + bidMinus2Pips);
								System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + gtd.getTime().toString());
								
								// Make the new stop trade
								int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
								ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, askPlus1p5Pips, askPlus2Pips, false, gtd);
								System.out.println("Bear Stop Expired.  Making new Stop.  " + newStopOrderID + " in place of " + orderId + ", " + askPlus2Pips);
								System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
							}
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void processCommissionReportEvents(HashMap<String, Object> orderStatusDataHash) {
			try {
				String execID = orderStatusDataHash.get("execId").toString();
				double commission = Double.parseDouble(orderStatusDataHash.get("commission").toString());
				commission = CalcUtils.round(commission, 2);
				
				String orderType = IBQueryManager.getExecIDType(execID);
				
				if (orderType.equals("Unknown")) {
					System.out.println("commissionReport can't find " + execID);
					return;
				}
				 
				IBQueryManager.updateCommission(orderType, execID, commission);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * These events only come from IBEngine1
		 * 
		 * @param orderStatusDataHash
		 */
		private void processErrorEvents(HashMap<String, Object> orderStatusDataHash) {
			try {
				int errorCode = Integer.parseInt(orderStatusDataHash.get("errorCode").toString());
				int orderID = Integer.parseInt(orderStatusDataHash.get("id").toString());

				String orderType = IBQueryManager.getOrderIDType(orderID);
				
				if (orderType.equals("Unknown")) {
					System.err.println("processErrorEvents can't find " + orderID);
					return;
				}
				 
				// 201 = Order rejected
				if (errorCode == 201) {
					IBQueryManager.recordRejection(orderType, orderID, null);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private int calculatePositionSize(double wpOverUnderBenchmark, double distributionFraction, String action) {
			try {
				if (distributionFraction < MIN_DISTRIBUTION_FRACTION || wpOverUnderBenchmark < 0) {
					return 0;
				}
				
				// See what is the biggest position I could possibly do.  ALT = Other currency, not USD
				int bpPositionSizeALT = 0; 		// Based on buying power in USD
				int minPositionSizeALT = 0;		// Based on MIN_TRADE_SIZE (which is in USD)
				int maxPositionSizeALT = 0;		// Based on MAX_TRADE_SIZE (which is in USD)
				int basePositionSizeALT = 0; 	// Based on BASE_TRADE_SIZE (which is in USD)
				if (optionBacktest) {
					double buyingPower = bankRoll;
					if (action.equals("buy")) {
						double currentAsk = BackTester.getCurrentAsk(ibWorker.getBarKey().symbol);
						bpPositionSizeALT = (int)(buyingPower / currentAsk);
						basePositionSizeALT = (int)(BASE_TRADE_SIZE / currentAsk);
						minPositionSizeALT = (int)(MIN_TRADE_SIZE / currentAsk);
						maxPositionSizeALT = (int)(MAX_TRADE_SIZE / currentAsk);
					}
					if (action.equals("sell")) {
						double currentBid = BackTester.getCurrentBid(ibWorker.getBarKey().symbol);
						bpPositionSizeALT = (int)(buyingPower / currentBid);
						basePositionSizeALT = (int)(BASE_TRADE_SIZE / currentBid);
						minPositionSizeALT = (int)(MIN_TRADE_SIZE / currentBid);
						maxPositionSizeALT = (int)(MAX_TRADE_SIZE / currentBid);
					}
				}
				else {
					Object oBuyingPower = ibs.getAccountInfoValue(IBConstants.ACCOUNT_BUYING_POWER);
					Double buyingPower = null; 
					if (oBuyingPower != null && oBuyingPower instanceof Double) {
						buyingPower = (Double)oBuyingPower;
					}
					if (buyingPower != null) {
						if (action.equals("buy")) {
							Double rawCurrentAsk = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
							double currentAsk = (rawCurrentAsk != null ? Double.parseDouble(df5.format(rawCurrentAsk)) : 0);
							bpPositionSizeALT = (int)(buyingPower / currentAsk);
							basePositionSizeALT = (int)(BASE_TRADE_SIZE / currentAsk);
							minPositionSizeALT = (int)(MIN_TRADE_SIZE / currentAsk);
							maxPositionSizeALT = (int)(MAX_TRADE_SIZE / currentAsk);
						}
						if (action.equals("sell")) {
							Double rawCurrentBid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
							double currentBid = (rawCurrentBid != null ? Double.parseDouble(df5.format(rawCurrentBid)) : 0);
							bpPositionSizeALT = (int)(buyingPower / currentBid);
							basePositionSizeALT = (int)(BASE_TRADE_SIZE / currentBid);
							minPositionSizeALT = (int)(MIN_TRADE_SIZE / currentBid);
							maxPositionSizeALT = (int)(MAX_TRADE_SIZE / currentBid);
						}
					}
				}
				
				// Round to nearest 1000
				basePositionSizeALT = (int)(basePositionSizeALT / 1000) * 1000; 
				bpPositionSizeALT = (int)(bpPositionSizeALT / 1000) * 1000;
				minPositionSizeALT = (int)(minPositionSizeALT / 1000) * 1000; 
				maxPositionSizeALT = (int)(maxPositionSizeALT / 1000) * 1000; 
				
				// Don't let the position size be bigger or smaller than what is possible
				if (basePositionSizeALT > bpPositionSizeALT) {
					basePositionSizeALT = bpPositionSizeALT;
				}
				if (basePositionSizeALT < minPositionSizeALT) {
					basePositionSizeALT = 0;
				}
				if (basePositionSizeALT > maxPositionSizeALT) {
					basePositionSizeALT = maxPositionSizeALT;
				}
				
				return basePositionSizeALT;
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		private double calculateCommission(int positionSize, double price) {
			double commission = (positionSize * price) * .0001 * .2 * 2;
			if (commission < 4) {
				commission = 4;
			}
			return new Double(df2.format(commission)).doubleValue();
		}
		
		private boolean fridayCloseoutTime(Calendar c) {
			if (optionFridayCloseout) {
				if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					int minutesIntoDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
					int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT;
					if (minutesIntoDay >= closeOutMinute) {
						return true;
					}
				}
			}
			return false;
		}
		
		private boolean fridayCloseoutTime() {
			if (optionFridayCloseout) {
				if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					int minutesIntoDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
					int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT;
					if (minutesIntoDay >= closeOutMinute) {
						return true;
					}
				}	
			}
			return false;
		}
		
		private boolean beforeFridayCutoff(Calendar c) {
			if (optionFridayCloseout) {
				if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					int minutesIntoDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
					int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF;
					if (minutesIntoDay < closeOutMinute) {
						return true;
					}
					return false;
				}
			}
			return true;
		}
		
		private boolean beforeFridayCutoff() {
	 		if (optionFridayCloseout) {
				if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
					int minutesIntoDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
					int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF;
					if (minutesIntoDay < closeOutMinute) {
						return true;
					}
					return false;
				}
	 		}
			return true;
		}
	
		private double averageLastXWPOBs() {
			try {
				if (lastXWPOBs == null || lastXWPOBs.size() == 0) {
					return 0;
				}
				
				double sumAWPs = 0;
				for (double awp : lastXWPOBs) {
					sumAWPs += awp;
				}
				return sumAWPs / (double)lastXWPOBs.size();
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0d;
			}
		}

		public void setOptionBacktest(boolean optionBacktest) {
			this.optionBacktest = optionBacktest;
		}
}
package trading.engines;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

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
import test.backtest.BackTester;
import trading.TradingSingleton;
import trading.engines.paper.IBAdaptiveTest;
import utils.CalcUtils;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * 
 * 
 */
public class IBEngine1 extends TradingEngineBase {

	private final int STALE_TRADE_SEC = 60; // How many seconds a trade can be open before it's considered "stale" and needs to be cancelled and re-issued.
	private final int MIN_MINUTES_BETWEEN_NEW_OPENS = 179; // This is to prevent many highly correlated trades being placed over a tight timespan.  6 hours ok?
	private final int DEFAULT_EXPIRATION_DAYS = 5; // How many days later the trade should expire if not explicitly defined by the model
	
	private final float MIN_TRADE_SIZE = 10000f;
	private final float MAX_TRADE_SIZE = 150000f;
	
	private final int MAX_OPEN_ORDERS = 10; // Max simultaneous open orders.  IB has a limit of 15 per pair/symbol.
	
	private final int PIP_SPREAD_ON_EXPIRATION = 1; // If an close order expires, I set a tight limit & stop limit near the current price.  This is how many pips away from the bid & ask those orders are.

	private final float MIN_TRADE_WIN_PROBABILITY = .60f; // What winning percentage a model needs to show in order to make a trade
	private final float MIN_TRADE_VETO_PROBABILITY = .53f; // What winning percentage a model must show (in the opposite direction) in order to veto another trade
	private final float MIN_BUCKET_DISTRIBUTION = .001f; // What percentage of the test set instances fell in a specific decile bucket
	private final float MIN_AVERAGE_WIN_PERCENT = .535f; // What the average winning percentage of all models has to be in order for a trade to be made
	private final float MIN_AVERAGE_WIN_PERCENT_INCREMENT = .005f; // This gets added on top of MIN_AVERAGE_WIN_PERCENT when multiple trades are open.
	
	private final int MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF = 120; // No new trades can be started this many minutes before close on Fridays (4PM Central)
	private final int MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT = 15; // All open trades get closed this many minutes before close on Fridays (4PM Central)
	
	private Calendar mostRecentOpenTime = null;
	private boolean modelContradictionCheckOK = true;
	private boolean noTradesDuringRound = true; // Only one model can request a trade per round (to prevent multiple models from trading at the same time and going against the min minutes required between orders)
	private boolean averageWinPercentOK = false;
	private double averageWinningPercentage01 = 0;
	private LinkedList<Double> last600AWPs = new LinkedList<Double>();
	private int tradeModelID = 0; // For each round, the ID of the model that is firing best and meets the MIN_TRADE_WIN_PROBABILITY
	private int countOpenOrders = 0;
	
	private IBWorker ibWorker;
	private IBSingleton ibs;
//	private IBAdaptiveTest ibAdaptiveTest = new IBAdaptiveTest();
	
	public IBEngine1(IBWorker ibWorker) {
		super();

		this.ibWorker = ibWorker;
		ibs = IBSingleton.getInstance();
//		ibAdaptiveTest.loadStateFromDB();
		countOpenOrders = IBQueryManager.selectCountOpenOrders();
	}
	
	public void setIbWorker(IBWorker ibWorker) {
		this.ibWorker = ibWorker;
	}

	@Override
	public void run() {
		try {
			while (true) {
				noTradesDuringRound = true;
				if (running) {
					try {
						// Monitor Opens per model
						synchronized (this) {
							// Model prechecks - Check how all models are firing.  If there are any contradictions, we're not going to trade now.
							// Also get the ID of the model that is firing best - that'll be the only one allowed to trade.
							int sum = 0;
							int sumOfAbs = 0;
							double bestWinningPercentageForBullishModels = 0;
							double bestWinningPercentageForBearishModels = 0;
							double sumDistributionProduct = 0;
							double sumDistributionSize = 0;
							boolean anyPredictions = false;
							tradeModelID = 0;
							int bestBullModelID = 0;
							int bestBearModelID = 0;
							averageWinPercentOK = false;
							for (Model model : models) {
								HashMap<String, Double> infoHash = modelPreChecks(model, true);
								int preCheck = infoHash.get("Action").intValue();
								double prediction = infoHash.get("Prediction");
								double winningPercentage51 = infoHash.get("WinningPercentage51"); // Ranges from .5 to 1.0
								double winningPercentage01 = infoHash.get("WinningPercentage01"); // Ranges from 0 to 1.0
								double distributionSize = infoHash.get("DistributionSize");
								double distributionProduct = winningPercentage01 * distributionSize;
								sumDistributionProduct += distributionProduct;
								sumDistributionSize += distributionSize;
								
								model.setPredictionDistributionPercentage(distributionSize / (double)model.getTestDatasetSize());
								
								if (prediction == 1) {
									anyPredictions = true;
									if (winningPercentage51 > bestWinningPercentageForBullishModels) {
										bestWinningPercentageForBullishModels = winningPercentage51;
										if (bestWinningPercentageForBullishModels >= MIN_TRADE_WIN_PROBABILITY) {
											bestBullModelID = model.id;
										}
									}
								}
								else if (prediction == -1) {
									anyPredictions = true;
									if (winningPercentage51 > bestWinningPercentageForBearishModels) {
										bestWinningPercentageForBearishModels = winningPercentage51;
										if (bestWinningPercentageForBearishModels >= MIN_TRADE_WIN_PROBABILITY) {
											bestBearModelID = model.id;
										}
									}
								}
								
								sum += preCheck;
								sumOfAbs += Math.abs(preCheck);
							}
							
							// Model contradiction check (optional)
							modelContradictionCheckOK = true;
//							int absOfSum = Math.abs(sum); // These 4 lines do the contradiction check
//							if (absOfSum != sumOfAbs) { 
//								modelContradictionCheckOK = false;
//							}
							
							// Calculate AWP and store in last600AWPs
							averageWinningPercentage01 = sumDistributionProduct / sumDistributionSize; // Ranges from 0 to 1.0
							if (anyPredictions && !Double.isNaN(averageWinningPercentage01)) {
								if (backtestMode) {
									for (int a = 0; a < 300; a++) {
										last600AWPs.addFirst(averageWinningPercentage01);
									}
								}
								else {
									last600AWPs.addFirst(averageWinningPercentage01);
								}
							}
							while (last600AWPs.size() > 600) {
								last600AWPs.removeLast();
//								ibAdaptiveTest.runChecks(last600AWPs);
							}
				
							System.out.println(averageWinningPercentage01 + "\t" + averageLast600AWPs());
							
							// Set the model that can trade
							if (averageWinningPercentage01 >= .5) {
								tradeModelID = bestBullModelID;
							}
							else {
								tradeModelID = bestBearModelID;
							}
							
							// Check if AWP is ok given the count of open orders.
							float totalIncrement = countOpenOrders * MIN_AVERAGE_WIN_PERCENT_INCREMENT;
							float currentMinAverageWinPercent = MIN_AVERAGE_WIN_PERCENT + totalIncrement;
							if (	(Math.abs(.5 - averageLast600AWPs()) + .5) >= currentMinAverageWinPercent && 
									(Math.abs(.5 - averageWinningPercentage01) + .5) >= currentMinAverageWinPercent) {
								averageWinPercentOK = true;
							}
	
							// Model Monitor Open
							for (Model model : models) {						
								HashMap<String, String> openMessages = new HashMap<String, String>();
								openMessages = monitorOpen(model);
								
								String jsonMessages = packageMessages(openMessages, new HashMap<String, String>());
								ss.addJSONMessageToTradingMessageQueue(jsonMessages);	
							}
						}
						
						// Monitor API events
						if (backtestMode) {
							monitorBacktestEvents();
						}
						else {
							long startAPIMonitoringTime = Calendar.getInstance().getTimeInMillis();
							long totalAPIMonitoringTime = 0;
							while (totalAPIMonitoringTime < 1000) { // Monitor the API for up to 1 second
								monitorIBWorkerTradingEvents();
								if (!backtestMode) {
									Thread.sleep(10);
								}
								totalAPIMonitoringTime = Calendar.getInstance().getTimeInMillis() - startAPIMonitoringTime;
							}
						}
						
						// Check for stop adjustments - bull positions go based on bid price, bear positions go on ask price.
						double currentBid = 0;
						double currentAsk = 0;
						if (backtestMode) {
							currentBid = BackTester.getCurrentBid(ibWorker.getBarKey().symbol);
							currentAsk = BackTester.getCurrentAsk(ibWorker.getBarKey().symbol);
						}
						else {
							Double rawCurrentBid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
							Double rawCurrentAsk = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
							currentBid = (rawCurrentBid != null ? Double.parseDouble(df5.format(rawCurrentBid)) : 0);
							currentAsk = (rawCurrentAsk != null ? Double.parseDouble(df5.format(rawCurrentAsk)) : 0);
						}
						ArrayList<HashMap<String, Object>> stopHashList = IBQueryManager.updateStopsAndBestPricesForOpenOrders(currentBid, currentAsk);
						for (HashMap<String, Object> stopHash : stopHashList) {
							int stopID = Integer.parseInt(stopHash.get("ibstoporderid").toString());
							int openID = Integer.parseInt(stopHash.get("ibopenorderid").toString());
							int ocaGroup = Integer.parseInt(stopHash.get("ibocagroup").toString());
							String direction = stopHash.get("direction").toString();
							int remainingAmount = Integer.parseInt(stopHash.get("remainingamount").toString());
							Timestamp expiration = (Timestamp)stopHash.get("expiration");
							double newStop = Double.parseDouble(stopHash.get("newstop").toString());
							newStop = new Double(df5.format(newStop));
							double newLimit = newStop + (.5 * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
							ORDER_ACTION stopAction = ORDER_ACTION.BUY;
							if (direction.equals("bull")) {
								stopAction = ORDER_ACTION.SELL;
								newLimit = newStop - (.5 * IBConstants.TICKER_PIP_SIZE_HASH.get(ibWorker.getBarKey().symbol));
							}
							newLimit = new Double(df5.format(newLimit));
							
							Calendar gtd = Calendar.getInstance();
							gtd.setTimeInMillis(expiration.getTime());
							
							// Update the stop
							if (backtestMode) {
								IBQueryManager.backtestUpdateStop(openID, newStop);
							}
							else {
								ibWorker.placeOrder(stopID, ocaGroup, OrderType.STP, stopAction, remainingAmount, newStop, newLimit, false, gtd);
							}
							System.out.println("Updating stop for " + stopID + ". " + newStop + ", " + ocaGroup + ", " + newLimit + ", " + stopAction.toString() + ", " + remainingAmount);
						}
						
						
						// Monitor Fridays for trade closeout
						boolean fridayCloseout = fridayCloseoutTime();
						if (fridayCloseout) { 
							ArrayList<Integer> openCloseOrderIDs = IBQueryManager.getCloseOrderIDsNeedingCloseout();
							for (int closeOrderID : openCloseOrderIDs) {
								ibWorker.cancelOrder(closeOrderID); // processOrderStatusEvents(...) will see the cancellation, see that it was cancelled due to friday closeout, and make a new tight close & stop
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					if (!backtestMode) {
						Thread.sleep(1000);
					}
				}
				if (backtestMode) {
					BackTester.incrementBarWMDIndex();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method supports logic that says any one model can veto any others that wants to fire if the vetoing model 
	 * is showing a high probability (at least 5x% win rate) of a trade in the opposite direction.
	 * 
	 * @param model
	 * @param useConfidence - false is actually more strict because on a binary model everything will be Buy or Sell.  
	 * @return 1 if this model wants to buy, -1 if it wants to sell. 0 if it isn't confident enough to do either.
	 */
	public HashMap<String, Double> modelPreChecks(Model model, boolean useConfidence) {
		HashMap<String, Double> infoHash = new HashMap<String, Double>();
		try {
			Calendar c = Calendar.getInstance();
			Calendar periodStart = CalendarUtils.getBarStart(c, model.getBk().duration);
			Calendar periodEnd = CalendarUtils.getBarEnd(c, model.getBk().duration);

			double modelScore = 1;
			boolean confident = false;
			String prediction = "";
			String action = "";
			double bucketWinningPercentage = 0;
			double bucketDistribution = 0;
			double distributionSize = 0;
			
			// For testing outside of trading hours
//			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//			String testStart = "03/04/2016 09:00:00";
//			String testEnd = "03/04/2016 09:05:00";
//			periodStart.setTime(sdf.parse(testStart));
//			periodEnd.setTime(sdf.parse(testEnd));
			
			// Load data for classification
			ArrayList<ArrayList<Object>> unlabeledList = new ArrayList<ArrayList<Object>>();
			if (backtestMode) {
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
				modelScore = distribution[1];
				
				if (distribution.length == 2) {
					if (distribution[0] > distribution[1]) {
						prediction = "Lose";
						infoHash.put("Prediction", -1d);
					}
					else if (distribution[1] > distribution [0]) {
						prediction = "Win";
						infoHash.put("Prediction", 1d);
					}
					else {
						infoHash.put("Prediction", 0d);
					}
				}
				
				HashMap<String, Object> modelData = QueryManager.getModelDataFromScore(model.id, modelScore);
				bucketWinningPercentage = (double)modelData.get("PercentCorrect");
				bucketDistribution = (int)modelData.get("InstanceCount") / (double)model.getTestDatasetSize();
				distributionSize = model.getTestDatasetSize() * bucketDistribution;
				boolean vetoCheck = true;
				if (Double.isNaN(bucketWinningPercentage) || bucketDistribution < MIN_BUCKET_DISTRIBUTION || bucketWinningPercentage < (vetoCheck ? MIN_TRADE_VETO_PROBABILITY : MIN_TRADE_WIN_PROBABILITY)) {
					confident = false;
				}
			}
			else {
				infoHash.put("Prediction", 0d);
				infoHash.put("DistributionSize", distributionSize);
				infoHash.put("WinningPercentage01", .5d);
			}
			
			// Determine the action type (Buy, Buy Signal, Sell, Sell Signal)
			if ((model.type.equals("bull") && prediction.equals("Win") && (model.tradeOffPrimary || model.useInBackTests)) ||
				(model.type.equals("bear") && prediction.equals("Lose") && (model.tradeOffOpposite || model.useInBackTests))) {
					action = "Buy";
					infoHash.put("WinningPercentage01", bucketWinningPercentage); // Ranges from 0 - 1
			}
			if ((model.type.equals("bull") && prediction.equals("Lose") && (model.tradeOffOpposite || model.useInBackTests)) ||
				(model.type.equals("bear") && prediction.equals("Win") && (model.tradeOffPrimary || model.useInBackTests))) {
					action = "Sell";
					infoHash.put("WinningPercentage01", 1 - bucketWinningPercentage); // Ranges from 0 - 1
			}
			
			if (useConfidence == false) {
				confident = true;
			}
			
			if (confident && action.equals("Buy")) {
				infoHash.put("Action", 1d);
			}
			else if (confident && action.equals("Sell")) {
				infoHash.put("Action", -1d);
			}
			else {
				infoHash.put("Action", 0d);
			}
			
			infoHash.put("WinningPercentage51", bucketWinningPercentage); // Ranges from .5 - 1.0
			infoHash.put("DistributionSize", distributionSize);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return infoHash;
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
			
			double modelScore = 1;
			double winningPercentage = 0;
			
			// For testing outside of trading hours
//			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//			String testStart = "03/04/2016 09:00:00";
//			String testEnd = "03/04/2016 09:05:00";
//			periodStart.setTime(sdf.parse(testStart));
//			periodEnd.setTime(sdf.parse(testEnd));
			
			// Load data for classification
			ArrayList<ArrayList<Object>> unlabeledList = new ArrayList<ArrayList<Object>>();
			if (backtestMode) {
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

			String action = "Waiting";
			if (instances != null && instances.firstInstance() != null) {
				// Make the prediction
//				double label = classifier.classifyInstance(instances.firstInstance());
//				int predictionIndex = (int)label;
//				instances.firstInstance().setClassValue(label);
//				String prediction = instances.firstInstance().classAttribute().value(predictionIndex);
				
				double[] distribution = classifier.distributionForInstance(instances.firstInstance());
				modelScore = distribution[1];
//				confidence = distribution[predictionIndex];
				
				String prediction = "";
				if (distribution.length == 2) {
					if (distribution[0] > distribution[1]) {
						prediction = "Lose";
					}
					else {
						prediction = "Win";
					}
				}
				
				HashMap<String, Object> modelData = QueryManager.getModelDataFromScore(model.id, modelScore);
				winningPercentage = (double)modelData.get("PercentCorrect");
				double bucketDistribution = (int)modelData.get("InstanceCount") / (double)model.getTestDatasetSize();
				boolean vetoCheck = true;
				boolean confident = true;
				if (Double.isNaN(winningPercentage) || bucketDistribution < MIN_BUCKET_DISTRIBUTION || winningPercentage < (vetoCheck ? MIN_TRADE_VETO_PROBABILITY : MIN_TRADE_WIN_PROBABILITY)) {
					confident = false;
				}

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
				if (backtestMode) {
					timingOK = true;
				}

				// Determine the action type (Buy, Buy Signal, Sell, Sell Signal)
				if ((model.type.equals("bull") && prediction.equals("Win") && (model.tradeOffPrimary || model.useInBackTests)) ||
					(model.type.equals("bear") && prediction.equals("Lose") && (model.tradeOffOpposite || model.useInBackTests))) {
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
				if ((model.type.equals("bull") && prediction.equals("Lose") && (model.tradeOffOpposite || model.useInBackTests)) ||
					(model.type.equals("bear") && prediction.equals("Win") && (model.tradeOffPrimary || model.useInBackTests))) {
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
					if (backtestMode) {
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
					int positionSize = calculatePositionSize(winningPercentage, bucketDistribution);
					
					// Calculate the exit target
					double suggestedExitPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice + (likelyFillPrice * model.getSellMetricValue() / 100d)))));
					double suggestedStopPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice - (likelyFillPrice * model.getStopMetricValue() / 100d)))));
					if ((model.type.equals("bear") && action.equals("buy")) || // Opposite trades
						(model.type.equals("bull") && action.equals("sell"))) {
						suggestedExitPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice - (likelyFillPrice * model.getStopMetricValue() / 100d)))));
						suggestedStopPrice = CalcUtils.roundTo5DigitHalfPip(Double.parseDouble(df5.format((likelyFillPrice + (likelyFillPrice * model.getSellMetricValue() / 100d)))));
					}

					// Calculate the trades expiration time
					Calendar expiration = Calendar.getInstance();
					if (backtestMode) {
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
					if (backtestMode) {
						openOrderExpiration.setTimeInMillis(BackTester.getCurrentPeriodEnd().getTimeInMillis());
					}
					openOrderExpiration.setTimeInMillis(openOrderExpiration.getTimeInMillis() + (STALE_TRADE_SEC * 1000));
					
					// Check how long it's been since the last open order
					boolean openRateLimitCheckOK = true;
					if (mostRecentOpenTime == null) {
						Calendar result = IBQueryManager.getMostRecentFilledTime();
						if (result != null) {
							mostRecentOpenTime = Calendar.getInstance();
							mostRecentOpenTime.setTimeInMillis(result.getTimeInMillis());
						}
					}
					if (mostRecentOpenTime != null) {
						long mostRecentOpenTimeMS = mostRecentOpenTime.getTimeInMillis();
						long nowMS = Calendar.getInstance().getTimeInMillis();
						if (backtestMode) {
							nowMS = BackTester.getCurrentPeriodEnd().getTimeInMillis();
						}
						if (nowMS - mostRecentOpenTimeMS < (MIN_MINUTES_BETWEEN_NEW_OPENS * 60 * 1000)) {
							openRateLimitCheckOK = false;
							action = "Waiting";
						}
					}
				
					// Check to make sure there are fewer than 10 open orders (15 is the IB limit)
					countOpenOrders = IBQueryManager.selectCountOpenOrders();
					boolean numOpenOrderCheckOK = true;
					if (countOpenOrders >= MAX_OPEN_ORDERS) {
						numOpenOrderCheckOK = false;
					}
					
					// Check to see if this is the model that is allowed to trade
					boolean approvedModel = false;
					if (model.id == tradeModelID) {
						approvedModel = true;
					}
					
					boolean beforeFridayCutoff = beforeFridayCutoff();
					if (backtestMode) {
						beforeFridayCutoff = beforeFridayCutoff(BackTester.getCurrentPeriodEnd());
					}
					
//					System.out.println("----------- Final Checks -----------");
//					System.out.println("Model ID: 			" + model.id);
//					System.out.println("Confident: 			" + confident);
//					System.out.println("Approved Model:			" + approvedModel + " " + tradeModelID);
//					System.out.println("Average Win Percent: 		" + averageWinPercentOK + " " + df5.format(averageWinningPercentage) + " \t " + df5.format(averageLast600AWPs()));
//					System.out.println("Open Rate Limit: 		" + openRateLimitCheckOK);
//					System.out.println("Num Open Orders: 		" + numOpenOrderCheckOK);
//					System.out.println("Model Contradiction Check: 	" + modelContradictionCheckOK);
//					System.out.println("No Trades During Round: 	" + noTradesDuringRound);
//					System.out.println("Before Friday Cutoff: 		" + beforeFridayCutoff());

					// Final checks
					if (confident && approvedModel && averageWinPercentOK && openRateLimitCheckOK && numOpenOrderCheckOK && 
							modelContradictionCheckOK && noTradesDuringRound && beforeFridayCutoff && positionSize >= MIN_TRADE_SIZE && positionSize <= MAX_TRADE_SIZE) {
						// Check to see if this model has an open opposite order that should simply be closed instead of 
						HashMap<String, Object> orderInfo = IBQueryManager.findOppositeOpenOrderToCancel(model, direction);
						
						// Record that a model has attempted to trade during this round.  OK to do this here because it'll happen either way if it's making a new trade or cancelling an opposite side open order.
						noTradesDuringRound = false;
						
						// No opposite side order to cancel.  Make new trade request in the DB
						if (orderInfo == null || orderInfo.size() == 0) {
							// Record order request in DB
							Calendar statusTime = null;
							String runName = null;
							if (backtestMode) {
								statusTime = BackTester.getCurrentPeriodEnd();
								runName = BackTester.getRunName();
							}
							int orderID = IBQueryManager.recordTradeRequest(OrderType.LMT.toString(), orderAction.toString(), "Open Requested", statusTime,
									direction, model.bk, suggestedEntryPrice, suggestedExitPrice, suggestedStopPrice, positionSize, model.modelFile, averageLast600AWPs(), expiration, runName);
								
							// Send the trade order to IB
							System.out.println(openOrderExpiration.getTime().toString());
							if (!backtestMode) {
								ibWorker.placeOrder(orderID, null, OrderType.LMT, orderAction, positionSize, null, suggestedEntryPrice, false, openOrderExpiration);
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
							if (!backtestMode) {
								ibWorker.cancelOrder(closeOrderID);
								ibWorker.cancelOrder(stopOrderID);
							}
							
							// Update the note on the order to say it was cut short
							IBQueryManager.updateOrderNote(openOrderID, "Cut Short");
							
							// Cutting short an order because a model wants to fire in the opposite direction counts towards mostRecentOpenTime.
							if (backtestMode) {
								mostRecentOpenTime = BackTester.getCurrentPeriodEnd();
							}
							else {
								mostRecentOpenTime = Calendar.getInstance();
							}
							
							// Make new tight close & stop to effectively cancel.
							
							// Get a One-Cancels-All group ID
							int ibOCAGroup = IBQueryManager.getIBOCAGroup();
							
							// Get prices a couple pips on each side of the bid/ask spread
							double currentBid = 0;
							double currentAsk = 0;
							if (backtestMode) {
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
							if (backtestMode) {
								statusTime = BackTester.getCurrentPeriodEnd();
								if (existingOrderDirection.equals("bull")) {
									IBQueryManager.recordClose("Open", openOrderID, currentAsk, "Cut Short", filledAmount, existingOrderDirection, BackTester.getCurrentPeriodEnd());
								}
								else if (existingOrderDirection.equals("bear")) {
									IBQueryManager.recordClose("Open", openOrderID, currentBid, "Cut Short", filledAmount, existingOrderDirection, BackTester.getCurrentPeriodEnd());
								}
								IBQueryManager.backtestUpdateCommission(openOrderID, 4d);
							}
							else {
								if (existingOrderDirection.equals("bull")) {
									// Make the new close trade
									int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(closeOrderID, ibOCAGroup, statusTime);
									if (!backtestMode) {
										ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, askPlus2Pips, false, gtd);
									}
									System.out.println("Bull Close cancelled due to opposite order being available.  Making new Close.  " + newCloseOrderID + " in place of " + closeOrderID + ", " + askPlus2Pips);
									System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
									
									// Make the new stop trade
									int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
									if (!backtestMode) {
										ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, bidMinus1p5Pips, bidMinus2Pips, false, gtd);
									}
									System.out.println("Bull Stop cancelled due to opposite order being available.  Making new Stop.  " + newStopOrderID + " in place of " + closeOrderID + ", " + bidMinus2Pips);
									System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + gtd.getTime().toString());
								}
								else {
									// Make the new close trade
									int newCloseOrderID = IBQueryManager.updateCloseTradeRequest(closeOrderID, ibOCAGroup, statusTime);
									if (!backtestMode) {
										ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, bidMinus2Pips, false, gtd);
									}
									System.out.println("Bear Close cancelled due to opposite order being available.  Making new Close.  " + newCloseOrderID + " in place of " + closeOrderID + ", " + bidMinus2Pips);
									System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + gtd.getTime().toString());
									
									// Make the new stop trade
									int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
									if (!backtestMode) {
										ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, askPlus1p5Pips, askPlus2Pips, false, gtd);
									}
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
//			confidence = Math.random(); // This can be used for testing the GUI outside of trading hours.
			messages.put("Confidence", df5.format(modelScore));
			messages.put("WinningPercentage", df5.format(winningPercentage));
			messages.put("PredictionDistributionPercentage", df5.format(model.predictionDistributionPercentage));
			messages.put("TestBucketPercentCorrect", model.getTestBucketPercentCorrectJSON());
			messages.put("TestBucketDistribution", model.getTestBucketDistributionJSON());
			if (averageWinningPercentage01 != 0 && models.indexOf(model) == 0) { // Only need to send this message once per round (not for every model) and not during that timeout period after the end of a bar.
				messages.put("AverageWinningPercentage", df5.format(averageWinningPercentage01));
			}
			messages.put("AverageLast500AWPs", df5.format(averageLast600AWPs()));
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
			ArrayList<HashMap<String, Object>> openRequestedHashList = IBQueryManager.backtestGetOpenRequestedOrders();
			for (HashMap<String, Object> orderHash : openRequestedHashList) {
				int openOrderID = Integer.parseInt(orderHash.get("ibopenorderid").toString());
				int requestedAmount = Integer.parseInt(orderHash.get("requestedamount").toString());
				double actualEntryPrice = Double.parseDouble(orderHash.get("suggestedentryprice").toString());
				
				if (Math.random() < BackTester.CHANCE_OF_OPEN_ORDER_BEING_FILLED) {
					IBQueryManager.updateOpen(openOrderID, "Filled", requestedAmount, actualEntryPrice, -1, BackTester.getCurrentPeriodEnd());
					mostRecentOpenTime = BackTester.getCurrentPeriodEnd();
				}
				else {
					IBQueryManager.cancelOpenOrder(openOrderID);
				}
			}
			
			// Filled Events - Either to Closed or staying at Filled
			ArrayList<HashMap<String, Object>> filledHashList = IBQueryManager.backtestGetFilledOrders();
			for (HashMap<String, Object> orderHash : filledHashList) {
				int openOrderID = Integer.parseInt(orderHash.get("ibopenorderid").toString());
				int requestedAmount = Integer.parseInt(orderHash.get("requestedamount").toString());
				int filledAmount = Integer.parseInt(orderHash.get("filledamount").toString());
				double actualEntryPrice = Double.parseDouble(orderHash.get("suggestedentryprice").toString());
				double suggestedExitPrice = Double.parseDouble(orderHash.get("suggestedexitprice").toString());
				double suggestedStopPrice = Double.parseDouble(orderHash.get("suggestedstopprice").toString());
				String direction = orderHash.get("direction").toString();
				Calendar expirationC = (Calendar)orderHash.get("expiration");
				
				double currentPrice = 0d;
				if (direction.equals("bull")) {
					currentPrice = BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD);
				}
				else if (direction.equals("bear")) {
					currentPrice = BackTester.getCurrentBid(IBConstants.TICK_NAME_FOREX_EUR_USD);
				}
				currentPrice = CalcUtils.roundTo5DigitHalfPip(currentPrice);
			
				if (	(direction.equals("bull") && BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD) >= suggestedExitPrice) ||
						(direction.equals("bear") && BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD) <= suggestedExitPrice)) {	
					// Target Hit
					IBQueryManager.recordClose("Open", openOrderID, currentPrice, "Target Hit", filledAmount, direction, BackTester.getCurrentPeriodEnd());
					IBQueryManager.backtestUpdateCommission(openOrderID, 4d);
				}
				else if ((direction.equals("bull") && BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD) <= suggestedStopPrice) ||
						(direction.equals("bear") && BackTester.getCurrentAsk(IBConstants.TICK_NAME_FOREX_EUR_USD) >= suggestedStopPrice)) {
					// Stop Hit
					IBQueryManager.recordClose("Open", openOrderID, currentPrice, "Stop Hit", filledAmount, direction, BackTester.getCurrentPeriodEnd());
					IBQueryManager.backtestUpdateCommission(openOrderID, 4d);
				}
				else if (BackTester.getCurrentPeriodEnd().getTimeInMillis() > expirationC.getTimeInMillis()) {
					// Expiration
					IBQueryManager.recordClose("Open", openOrderID, currentPrice, "Expiration", filledAmount, direction, BackTester.getCurrentPeriodEnd());
					IBQueryManager.backtestUpdateCommission(openOrderID, 4d);
				}
				else if (fridayCloseoutTime(BackTester.getCurrentPeriodEnd())) {
					// Closeout
					IBQueryManager.recordClose("Open", openOrderID, currentPrice, "Closeout", filledAmount, direction, BackTester.getCurrentPeriodEnd());
					IBQueryManager.noteCloseout("Open", openOrderID);
					IBQueryManager.backtestUpdateCommission(openOrderID, 4d);
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
						if (backtestMode) {
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
	
	/**
	 * This old version relies on the model decile buckets
	 * 
	 * @param confidence
	 * @param testBucketPercentCorrect
	 * @param testBucketDistribution
	 * @return
	 */
	@Deprecated
	private int calculatePositionSize(double confidence, double[] testBucketPercentCorrect, double[] testBucketDistribution) {	
		try {
			int bucket = -1; // .5 - .6 = [0], .6 - .7 = [1], .7 - .8 = [2], .8 - .9 = [3], .9 - 1.0 = [4]
			if (confidence >= .5 && confidence < .6) {
				bucket = 0;
			}
			else if (confidence >= .6 && confidence < .7) {
				bucket = 1;
			}
			else if (confidence >= .7 && confidence < .8) {
				bucket = 2;
			}
			else if (confidence >= .8 && confidence < .9) {
				bucket = 3;
			}
			else if (confidence >= .9) {
				bucket = 4;
			}
			
			if (bucket == -1) {
				System.err.println("bad bucket");
				return 0;
			}
			
			double bucketPercentCorrect = testBucketPercentCorrect[bucket];
			double bucketDistribution = testBucketDistribution[bucket];
			
			if (Double.isNaN(bucketPercentCorrect) || bucketDistribution < MIN_BUCKET_DISTRIBUTION || bucketPercentCorrect < MIN_TRADE_WIN_PROBABILITY) {
				return 0;
			}

			double basePositionSize = 40000;
			double multiplier = (bucketPercentCorrect - .25) / .25d; // 1.2x multiplier for a .55 winner, add an additional .2 multiplier for each .05 that the winning percentage goes up.
			double adjPositionSize = basePositionSize * multiplier;
			
			int positionSize = (int)(adjPositionSize / 1000) * 1000;
			return positionSize;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private int calculatePositionSize(double percentCorrect, double distribution) {
		try {
			if (distribution < MIN_BUCKET_DISTRIBUTION || percentCorrect < MIN_TRADE_WIN_PROBABILITY) {
				return 0;
			}
			
			double basePositionSize = 40000;
			double multiplier = (percentCorrect - .25) / .25d; // 1.2x multiplier for a .55 winner, add an additional .2 multiplier for each .05 that the winning percentage goes up.
			double adjPositionSize = basePositionSize * multiplier;
			
			int positionSize = (int)(adjPositionSize / 1000) * 1000;
			return positionSize;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * This old version relies on the model decile buckets.  
	 * 
	 * @param confidence
	 * @param testBucketPercentCorrect
	 * @param testBucketDistribution
	 * @param vetoCheck
	 * @return
	 */
	@Deprecated
	private boolean checkConfidence(double confidence, double[] testBucketPercentCorrect, double[] testBucketDistribution, boolean vetoCheck) {	
		try {
			int bucket = -1; // .5 - .6 = [0], .6 - .7 = [1], .7 - .8 = [2], .8 - .9 = [3], .9 - 1.0 = [4]
			if (confidence >= .5 && confidence < .6) {
				bucket = 0;
			}
			else if (confidence >= .6 && confidence < .7) {
				bucket = 1;
			}
			else if (confidence >= .7 && confidence < .8) {
				bucket = 2;
			}
			else if (confidence >= .8 && confidence < .9) {
				bucket = 3;
			}
			else if (confidence >= .9) {
				bucket = 4;
			}
			
			if (bucket == -1) {
				System.err.println("bad bucket");
				return false;
			}
			
			double bucketPercentCorrect = testBucketPercentCorrect[bucket];
			double bucketDistribution = testBucketDistribution[bucket];
			
			if (Double.isNaN(bucketPercentCorrect) || bucketDistribution < MIN_BUCKET_DISTRIBUTION || bucketPercentCorrect < (vetoCheck ? MIN_TRADE_VETO_PROBABILITY : MIN_TRADE_WIN_PROBABILITY)) {
				return false;
			}
			
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * This old version relies on the model decile buckets
	 * 
	 * @param confidence
	 * @param testBucketPercentCorrect
	 * @param testBucketDistribution
	 * @return
	 */
	@Deprecated
	private double getModelWinProbability(double confidence, double[] testBucketPercentCorrect, double[] testBucketDistribution) {	
		try {
			int bucket = -1; // .5 - .6 = [0], .6 - .7 = [1], .7 - .8 = [2], .8 - .9 = [3], .9 - 1.0 = [4]
			if (confidence >= .5 && confidence < .6) {
				bucket = 0;
			}
			else if (confidence >= .6 && confidence < .7) {
				bucket = 1;
			}
			else if (confidence >= .7 && confidence < .8) {
				bucket = 2;
			}
			else if (confidence >= .8 && confidence < .9) {
				bucket = 3;
			}
			else if (confidence >= .9) {
				bucket = 4;
			}
			
			if (bucket == -1) {
				System.err.println("bad bucket");
				return 0;
			}
			
			double bucketPercentCorrect = testBucketPercentCorrect[bucket];
			double bucketDistribution = testBucketDistribution[bucket];
			
			if (bucketDistribution >= MIN_BUCKET_DISTRIBUTION) {
				return bucketPercentCorrect;
			}
			return 0;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * This old version relies on the model decile buckets
	 * 
	 * @param confidence
	 * @param testBucketPercentCorrect
	 * @param testBucketDistribution
	 * @return
	 */
	@Deprecated
	private double getModelDistribution(double confidence, double[] testBucketPercentCorrect, double[] testBucketDistribution) {	
		try {
			int bucket = -1; // .5 - .6 = [0], .6 - .7 = [1], .7 - .8 = [2], .8 - .9 = [3], .9 - 1.0 = [4]
			if (confidence >= .5 && confidence < .6) {
				bucket = 0;
			}
			else if (confidence >= .6 && confidence < .7) {
				bucket = 1;
			}
			else if (confidence >= .7 && confidence < .8) {
				bucket = 2;
			}
			else if (confidence >= .8 && confidence < .9) {
				bucket = 3;
			}
			else if (confidence >= .9) {
				bucket = 4;
			}
			
			if (bucket == -1) {
				System.err.println("bad bucket");
				return 0;
			}
			
			double bucketDistribution = testBucketDistribution[bucket];

			return bucketDistribution;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private boolean fridayCloseoutTime(Calendar c) {
		if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
			int minutesIntoDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
			int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT;
			if (minutesIntoDay >= closeOutMinute) {
				return true;
			}
		}
		return false;
	}
	
	private boolean fridayCloseoutTime() {
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
			int minutesIntoDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
			int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT;
			if (minutesIntoDay >= closeOutMinute) {
				return true;
			}
		}
		return false;
	}
	
	private boolean beforeFridayCutoff(Calendar c) {
 		if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
			int minutesIntoDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
			int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF;
			if (minutesIntoDay < closeOutMinute) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	private boolean beforeFridayCutoff() {
 		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
			int minutesIntoDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
			int closeOutMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF;
			if (minutesIntoDay < closeOutMinute) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	private double averageLast600AWPs() {
		try {
			if (last600AWPs == null || last600AWPs.size() == 0) {
				return 0;
			}
			
			double sumAWPs = 0;
			for (double awp : last600AWPs) {
				sumAWPs += awp;
			}
			return sumAWPs / (double)last600AWPs.size();
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0d;
		}
	}
}
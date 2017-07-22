package trading.engines;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import com.ib.controller.OrderType;

import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.Model;
import data.downloaders.interactivebrokers.IBConstants;
import data.downloaders.interactivebrokers.IBConstants.ORDER_ACTION;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
import dbio.BacktestQueryManager;
import dbio.IBQueryManager;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import test.backtest.BackTester;
import trading.TradingSingleton;
import utils.CalcUtils;
import utils.CalendarUtils;
import utils.Formatting;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class IBFutureZNEngine2 extends TradingEngineBase {

		// Configuration Options
		private boolean optionBacktest = false;
		private boolean optionUseRealisticBidAndAsk = true;
		private boolean optionUseBankroll = true;
		private boolean optionFridayCloseout = false;
		private int optionNumWPOBs = 1;
		
		// Timing Options
		private final int STALE_TRADE_SEC = 3540; 										// How many seconds a trade can be open before it's considered "stale" and needs to be cancelled and re-issued.
		private final int MIN_MINUTES_BETWEEN_NEW_OPENS = 90; 							// This is to prevent many highly correlated trades being placed over a tight timespan.  6 hours ok?
		private final int DEFAULT_EXPIRATION_HOURS = 8; 								// How many hours later the trade should expire if not explicitly defined by the model
		private final int MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF = 61; 					// No new trades can be started this many minutes before close on Fridays (4PM Central)
		private final int MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT = 61; 					// All open trades get closed this many minutes before close on Fridays (4PM Central)
		
		// Order Options
		private final float MIN_TRADE_SIZE = 60000f; 									// USD
		private final float MAX_TRADE_SIZE = 300000;									// USD
		private final float BASE_TRADE_SIZE = 300000;									// USD
		private final int MAX_OPEN_ORDERS = 1; 											// Max simultaneous open orders.  IB has a limit of 15 per pair/symbol.
		private final int PIP_SPREAD_ON_EXPIRATION = 1; 								// If an close order expires, I set a tight limit & stop limit near the current price.  This is how many pips away from the bid & ask those orders are.
		private final float PIP_REACH = .5f;											// How many extra pips I try to get on open.  Results in more orders not being filled.
		private final float CHANCE_OF_OPEN_ORDER_BEING_FILLED = 0.8f;					// 30M (.5 = .84, 1.0 = .56, 1.5 = .36, 2.0 = .23); 1H (.5 = .89, 1.0 = .67, 1.5 = .5, 2.0 = .36)
		private final float STOP_FRACTION = 0.05f;										// The percentage (expressed as a fraction) away from the entry price to place a disaster stop at.
		
		// Model Options
		private final float PERCENTAGE_OF_WORST_MODEL_INSTANCES_TO_EXCLUDE = .45f;		// Used to calculate model's min winning % required.
		private final float MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE = .00f;
		private final float MIN_DISTRIBUTION_FRACTION = .001f; 							// What percentage of the test set instances fell in a specific bucket
		
		// Global Variables
		private Calendar mostRecentOpenTime = null;
		private double averageWPOverUnderBenchmark = 0;
		private LinkedList<Double> lastXWPOBs = new LinkedList<Double>();
		private Calendar stopTimeoutEnd;												// Can only trade after this time
		private int countOpenOrders = 0;
		private int bankRoll = 300000;
		private String continuousContractName = "ZN";
		private BAR_SIZE barSize = BAR_SIZE.BAR_1H;
		private String datedContractName;
		private BarKey datedContractBK = null;
		
		// Needed objects
		private IBWorker ibWorker;
		private IBSingleton ibs;
		
		public IBFutureZNEngine2(IBWorker ibWorker) {
			super();

			this.ibWorker = ibWorker;
			if (!optionBacktest) {
				this.ibWorker.requestAccountInfoSubscription();
			}
			ibs = IBSingleton.getInstance();
			datedContractName = continuousContractName + " " + CalendarUtils.getFuturesContractBasedOnRolloverDate("ZN", Calendar.getInstance());
			datedContractBK = new BarKey(datedContractName, barSize);
			this.ibWorker.setBarKey(datedContractBK);
			countOpenOrders = IBQueryManager.selectCountOpenOrders();
			stopTimeoutEnd = Calendar.getInstance();
			stopTimeoutEnd.set(Calendar.YEAR, 2000);
		}
		
		@Override
		public String toString() {
			String engineInfo = IBFutureZNEngine2.class.getName() + " ";
			
			if (optionBacktest) {
				engineInfo += "oB-1 ";
			}
			else {
				engineInfo += "oB-0 ";
			}
			
			if (optionUseRealisticBidAndAsk) {
				engineInfo += "oURBAA-1 ";
			}
			else {
				engineInfo += "oURBAA-0 ";
			}
			
			if (optionUseBankroll) {
				engineInfo += "oUB-1 ";
			}
			else {
				engineInfo += "oUB-0 ";
			}
			
			if (optionFridayCloseout) {
				engineInfo += "oFC-1 ";
			}
			else {
				engineInfo += "oFC-0 ";
			}
			
			engineInfo += ", ";
			
			engineInfo += "sts-" + STALE_TRADE_SEC + ", ";
			engineInfo += "mmbno-" + MIN_MINUTES_BETWEEN_NEW_OPENS + ", ";
			engineInfo += "deh-" + DEFAULT_EXPIRATION_HOURS + ", ";
			engineInfo += "sf-" + STOP_FRACTION + ", ";
			engineInfo += "mbfctcu-" + MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF + ", ";
			engineInfo += "mbfctcl" + MIN_BEFORE_FRIDAY_CLOSE_TRADE_CLOSEOUT + ", ";
			engineInfo += "bts-" + BASE_TRADE_SIZE + ", ";
			engineInfo += "moo-" + MAX_OPEN_ORDERS + ", ";
			engineInfo += "powmite-" + Formatting.df2.format(PERCENTAGE_OF_WORST_MODEL_INSTANCES_TO_EXCLUDE) + ", ";
			engineInfo += "mwpobtrit-" + Formatting.df2.format(MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE) + ", ";
			engineInfo += "mdf-" + Formatting.df3.format(MIN_DISTRIBUTION_FRACTION) + ", ";
			engineInfo += "pr-" + Formatting.df2.format(PIP_REACH) + ", ";
			engineInfo += "cooobf-" + Formatting.df2.format(CHANCE_OF_OPEN_ORDER_BEING_FILLED);
			
			return engineInfo;
		}

		public void setIbWorker(IBWorker ibWorker) {
			this.ibWorker = ibWorker;
		}
		
		@Override
		public void run() {
			try {
				while (true) {
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
								int msToWait = 5000;
								int minute = Calendar.getInstance().get(Calendar.MINUTE);
								int second = Calendar.getInstance().get(Calendar.SECOND);
								if ((minute == 59 && second > 50) || (minute == 0 && second < 10)) {
									msToWait = 500;
								}
								while (totalAPIMonitoringTime < msToWait) { // Monitor the API for up to 3 seconds
									monitorIBWorkerTradingEvents();
									if (!optionBacktest) {
										Thread.sleep(25);
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
							Thread.sleep(3000);
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
				Calendar now = Calendar.getInstance();

				// Check to see if we have a new bar and set the period for the bar we want to use.
				Bar evaluationBar = ibs.getCompleteBarAndClear();
				boolean completeBar = true;
				if (evaluationBar == null) {
					completeBar = false;
					evaluationBar = QueryManager.getMostRecentBar(model.getBk(), Calendar.getInstance());
				}
				else {
//					System.out.println("IBEngine2 got complete Bar at " + Calendar.getInstance().getTime().toString());
//					System.out.println(evaluationBar.toString());
//					System.out.println("------");
				}
				
				// Calculate how delayed the price is - based off the rate I receive realtime bars and process metrics
				Calendar lastBarUpdate = ss.getLastDownload(model.getBk());
				String priceDelay = "";
				if (lastBarUpdate != null) {
					long timeSinceLastBarUpdate = now.getTimeInMillis() - lastBarUpdate.getTimeInMillis();
					priceDelay = new Double((double)Math.round((timeSinceLastBarUpdate / 1000d) * 100) / 100).toString();
				}
				
				String action = "Waiting";
				String prediction = "";
				double modelScore = 1;
				double bucketWinningPercentage = 0;

				// Load data for classification
				ArrayList<ArrayList<Object>> unlabeledList = new ArrayList<ArrayList<Object>>();
				if (optionBacktest) {
					unlabeledList = BackTester.createUnlabeledWekaArffData(model.getBk(), false, model.getMetrics(), metricDiscreteValueHash);
				}
				else {
					ARFF arff = new ARFF();
					unlabeledList = arff.createUnlabeledWekaArffData(evaluationBar.periodStart, evaluationBar.periodEnd, model.getBk(), false, false, model.getMetrics(), metricDiscreteValueHash);
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
				
				if (instances != null && instances.size() > 0 && instances.firstInstance() != null) {
					// Make the prediction 
					double[] distribution = classifier.distributionForInstance(instances.firstInstance());
					modelScore = distribution[1]; // 0 - 1 range.  This is the model score, not winning %.  < .5 means Lose and > .5 means Win
	
					if (distribution.length == 3) {
						double up = distribution[0];
						double down = distribution[1];
						double sum = up + down;
						
						double upScore = up / sum;
						double downScore = down / sum;
						
						modelScore = upScore;
					}
					
					if (distribution.length == 2) {
						if (distribution[0] > distribution[1]) {
							prediction = "Down";
						}
						else {
							prediction = "Up";
						}
					}
					else if (distribution.length == 3) {
						if (distribution[0] > distribution[1] && distribution[0] > distribution[2]) {
							prediction = "Down";
						}
						else if (distribution[1] > distribution[0] && distribution[1] > distribution[2]) {
							prediction = "Up";
						}
						else {
							prediction = "";
						}
					}
					
					// Determine what Winning Percentage is needed given the risk / reward ratio.  50% needed for 1:1 setups, 33% needed for 2:1 setups, 25% needed for 3:1 setups, etc.
					double benchmarkWP = model.sellMetricValue / (model.sellMetricValue + model.stopMetricValue); 
					if (distribution.length == 3) {
						benchmarkWP = .33333;
					}
					if (prediction.equals("Up") && distribution.length == 2) { // Only valid for binary classes
						benchmarkWP = 1 - benchmarkWP;
					}
					
					HashMap<String, Object> modelData = QueryManager.getModelDataFromScore(model.id, modelScore);
					bucketWinningPercentage = (double)modelData.get("PercentCorrect");
					double wpOverUnderBenchmark = 0;
					if (!Double.isNaN(bucketWinningPercentage)) {
						wpOverUnderBenchmark = bucketWinningPercentage - benchmarkWP;
					}
					
					if (!optionBacktest) {
						BacktestQueryManager.insertBacktestPredictions(modelScore, bucketWinningPercentage);
					}
					
					// Calculate what percentage of the instances were used to calculate this data.
					double distributionFraction = (int)modelData.get("InstanceCount") / (double)model.getTestDatasetSize();
					
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
					if (completeBar) {
						if (model.lastActionTime != null) {
							double msSinceLastTrade = now.getTimeInMillis() - model.lastActionTime.getTimeInMillis();
							if (msSinceLastTrade > TRADING_TIMEOUT) {
								timingOK = true;
							}
						}
						else {
							timingOK = true;
						}
					}
					if (optionBacktest) {
						timingOK = true;
//						boolean metricTest = QueryManager.metricAboveValue("mvol3", .03, BackTester.getCurrentPeriodStart(), model.getBk());
//						timingOK = metricTest;
					}
					
					// Determine which action to take (Buy, Sell, Buy Signal, Sell Signal, Close Long, Close Short, Waiting)
					boolean closeShort = false;
					boolean closeLong = false;
					if (prediction.equals("")) {
						closeShort = true;
						closeLong = true;
					}
					if (model.tradeOffPrimary || model.useInBackTests) {
						if (prediction.equals("Up")) {
							// Calculate what the winning percentage over the benchmark has to be
							float currentBullMWPOB = (float)QueryManager.getModelCutoffScore(model.id, PERCENTAGE_OF_WORST_MODEL_INSTANCES_TO_EXCLUDE, 1);
							
							if (completeBar) {
								System.out.println("Bull: " + wpOverUnderBenchmark + " / " + currentBullMWPOB);
							}
							
							closeShort = true;
							if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark >= currentBullMWPOB && averageLastXWPOBs() >= currentBullMWPOB) {
								action = "Buy";
								model.lastActionTime = Calendar.getInstance();
								model.lastActionTime.setTimeInMillis(now.getTimeInMillis());
							}
							else if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark < MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE && averageLastXWPOBs() < MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE) {
								closeLong = true;
							}
							else {
								action = "Buy Signal";
							}
						}
						else if (prediction.equals("Down")) {
							// Calculate what the winning percentage over the benchmark has to be
							float currentBearMWPOB = (float)QueryManager.getModelCutoffScore(model.id, PERCENTAGE_OF_WORST_MODEL_INSTANCES_TO_EXCLUDE, 0);
							
							if (completeBar) {
								System.out.println("Bear: " + wpOverUnderBenchmark + " / " + currentBearMWPOB);
							}
							
							closeLong = true;
							if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark >= currentBearMWPOB && averageLastXWPOBs() >= currentBearMWPOB) {
								action = "Sell";
								model.lastActionTime = Calendar.getInstance();
								model.lastActionTime.setTimeInMillis(now.getTimeInMillis());
							}
							else if (timingOK && distributionFraction >= MIN_DISTRIBUTION_FRACTION && wpOverUnderBenchmark < MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE && averageLastXWPOBs() < MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE) {
								closeShort = true;
							}
							else {
								action = "Sell Signal";
							}
						}
					}
					
					// Model says to Close something because the wpOverUnderBenchmark doesn't meet the MIN_WIN_PERCENT_OVER_BENCHMARK_TO_REMAIN_IN_TRADE
					if (closeShort || closeLong) {
						if (timingOK) {
							// Check what orders are currently open
							ArrayList<HashMap<String, Object>> orders = new ArrayList<HashMap<String, Object>>();
							if (optionBacktest) {
								orders = BacktestQueryManager.selectOpenOrders();
							}
							else {
								// Eligible for cutting short means they don't already have a close order ID.  
								orders = IBQueryManager.selectOpenOrdersEligibleForCuttingShort(model);
							}
							
							// Get prices a couple pips on each side of the bid/ask spread
							double currentBid = 0;
							double currentAsk = 0;
							if (optionBacktest) {
								currentBid = BackTester.getCurrentClose(continuousContractName);
								currentAsk = BackTester.getCurrentClose(continuousContractName);
							}
							else {
								Double rawCurrentBid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
								Double rawCurrentAsk = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
								currentBid = (rawCurrentBid != null ? Double.parseDouble(Formatting.df6.format(rawCurrentBid)) : 0);
								currentAsk = (rawCurrentAsk != null ? Double.parseDouble(Formatting.df6.format(rawCurrentAsk)) : 0);
							}
							currentAsk = CalcUtils.roundToHalfPip(continuousContractName, currentAsk);
							currentBid = CalcUtils.roundToHalfPip(continuousContractName, currentBid);
							
							// Straight close - no guessing bids/asks.
							if (!optionUseRealisticBidAndAsk) {
								currentAsk = BackTester.getCurrentClose(continuousContractName);
								currentBid = BackTester.getCurrentClose(continuousContractName);
							}
							
							// Check the total sizes of bear and bull orders we can close.  This is to aggregate closes to save on commission.
							int amountToCloseForBullOrders = 0;
							int amountToCloseForBearOrders = 0;
							ArrayList<Integer> bullOpenOrderIds = new ArrayList<Integer>();
							ArrayList<Integer> bearOpenOrderIds = new ArrayList<Integer>();
							ArrayList<Integer> bullStopOrderIds = new ArrayList<Integer>();
							ArrayList<Integer> bearStopOrderIds = new ArrayList<Integer>();
							ArrayList<Integer> bullFilledAmounts = new ArrayList<Integer>();
							ArrayList<Integer> bearFilledAmounts = new ArrayList<Integer>();
							for (HashMap<String, Object> orderInfo : orders) {
								int openOrderID = Integer.parseInt(orderInfo.get("ibopenorderid").toString());
								int stopOrderID = Integer.parseInt(orderInfo.get("ibstoporderid").toString());
								String direction = orderInfo.get("direction").toString();
								int filledAmount = 0;
								if (orderInfo.get("filledamount") != null) {
									filledAmount = Integer.parseInt(orderInfo.get("filledamount").toString());
								}
								int closeFilledAmount = 0;
								if (orderInfo.get("closefilledamount") != null) {
									closeFilledAmount = ((BigInteger)orderInfo.get("closefilledamount")).intValue();
								}
								int remainingAmountNeededToClose = filledAmount - closeFilledAmount;
								if (!optionBacktest) {
									remainingAmountNeededToClose /= 1000; // Live trading needs to transmit orders in lots of 1000.  DB already stores them in thousands, so divide here.
								}
								
								if (direction.equals("bull") && closeLong) {
									amountToCloseForBullOrders += remainingAmountNeededToClose;
									bullOpenOrderIds.add(openOrderID);
									bullStopOrderIds.add(stopOrderID);
									bullFilledAmounts.add(filledAmount);
								}
								else if (direction.equals("bear") && closeShort) {
									amountToCloseForBearOrders += remainingAmountNeededToClose;
									bearOpenOrderIds.add(openOrderID);
									bearStopOrderIds.add(stopOrderID);
									bearFilledAmounts.add(filledAmount);
								}
							}
							
							// Make the close
							if (optionBacktest) {
								double bullOpenOrderCommission = calculateCommission(amountToCloseForBullOrders, currentAsk);
								double bearOpenOrderCommission = calculateCommission(amountToCloseForBearOrders, currentBid);
								for (int i = 0; i < bullOpenOrderIds.size(); i++) {
									int bullOpenOrderId = bullOpenOrderIds.get(i);
									int filledAmount = bullFilledAmounts.get(i);
									BacktestQueryManager.backtestRecordClose("Open", bullOpenOrderId, currentAsk, "Target Hit", filledAmount, "bull", BackTester.getCurrentPeriodEnd());
									System.out.println(model.modelFile + " Cutting Short Bull Position " + filledAmount + " at " + currentAsk);
									double commissionPerOrder = bullOpenOrderCommission / (double)bullOpenOrderIds.size();
									BacktestQueryManager.backtestUpdateCommission(bullOpenOrderId, commissionPerOrder);
									Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(bullOpenOrderId);
									if (proceeds != null) {
										bankRoll += proceeds;
									}
									
								}
								for (int i = 0; i < bearOpenOrderIds.size(); i++) {
									int bearOpenOrderId = bearOpenOrderIds.get(i);
									int filledAmount = bearFilledAmounts.get(i);
									BacktestQueryManager.backtestRecordClose("Open", bearOpenOrderId, currentBid, "Target Hit", filledAmount, "bear", BackTester.getCurrentPeriodEnd());
									System.out.println(model.modelFile + " Cutting Short Bear Position " + filledAmount + " at " + currentBid);
									double commissionPerOrder = bearOpenOrderCommission / (double)bearOpenOrderIds.size();
									BacktestQueryManager.backtestUpdateCommission(bearOpenOrderId, commissionPerOrder);
									Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(bearOpenOrderId);
									if (proceeds != null) {
										bankRoll += proceeds;
									}
								}
							}
							else {
								// Make a good-till-date far in the future
								Calendar gtd = Calendar.getInstance();
								gtd.add(Calendar.DATE, 100);
								
								int newBullCloseOrderID = 0;
								for (int i = 0; i < bullOpenOrderIds.size(); i++) {
									newBullCloseOrderID = IBQueryManager.updateCloseTradeRequestWithOpenOrderID(bullOpenOrderIds.get(i), null, currentBid, Calendar.getInstance());
									ibWorker.cancelOrder(bullStopOrderIds.get(i));
								}
								int newBearCloseOrderID = 0;
								for (int i = 0; i < bearOpenOrderIds.size(); i++) {
									newBearCloseOrderID = IBQueryManager.updateCloseTradeRequestWithOpenOrderID(bearOpenOrderIds.get(i), null, currentAsk, Calendar.getInstance());
									ibWorker.cancelOrder(bearStopOrderIds.get(i));
								}
							
								// Close the whole amount using a market order
								if (amountToCloseForBullOrders > 0) {
									ibWorker.placeOrder(newBullCloseOrderID, null, OrderType.MKT, ORDER_ACTION.SELL, amountToCloseForBullOrders, null, null, false, gtd);
								}
								if (amountToCloseForBearOrders > 0) {
									ibWorker.placeOrder(newBearCloseOrderID, null, OrderType.MKT, ORDER_ACTION.BUY, amountToCloseForBearOrders, null, null, false, gtd);
								}
							}
						}
					}
			
					// Model says Buy or Sell - Do final checks to see if we can trade
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
						Double likelyFillPrice = 0d;
						double suggestedEntryPrice = 0d;
						double suggestedStopPrice = 0d;
						if (optionBacktest) {
							// Notice how I'm using the ask for buys and bid for sells for backtesting - this is basically worst-case market orders.
							if (direction.equals("bull")) {
								likelyFillPrice = BackTester.getCurrentClose(continuousContractName);
								likelyFillPrice = likelyFillPrice - (PIP_REACH * IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
								suggestedStopPrice = likelyFillPrice * (1 - STOP_FRACTION);
							}
							else if (direction.equals("bear")) {
								likelyFillPrice = BackTester.getCurrentClose(continuousContractName);
								likelyFillPrice = likelyFillPrice + (PIP_REACH * IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
								suggestedStopPrice = likelyFillPrice * (1 + STOP_FRACTION);
							}
							suggestedEntryPrice = CalcUtils.roundToHalfPip(continuousContractName, Double.parseDouble(Formatting.df6.format(likelyFillPrice)));
							suggestedStopPrice = CalcUtils.roundToHalfPip(continuousContractName, suggestedStopPrice);
							if (!optionUseRealisticBidAndAsk) {
								suggestedEntryPrice = BackTester.getCurrentClose(continuousContractName);
							}
						}
						else {
							// For real trading, try to use the more favorable entry prices.  Don't worry if order doesn't get filled.
							if (direction.equals("bull")) {
								if (ibs.getTickerFieldValue(datedContractBK, IBConstants.TICK_FIELD_BID_PRICE) != null) {
									likelyFillPrice = ibs.getTickerFieldValue(datedContractBK, IBConstants.TICK_FIELD_BID_PRICE);
									likelyFillPrice = likelyFillPrice - (PIP_REACH * IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
									suggestedStopPrice = likelyFillPrice * (1 - STOP_FRACTION);
								}
								else {
									System.err.println("IB doesn't have bid price!");
								}
							}
							else if (direction.equals("bear")) {
								if (ibs.getTickerFieldValue(datedContractBK, IBConstants.TICK_FIELD_ASK_PRICE) != null) {
									likelyFillPrice = ibs.getTickerFieldValue(datedContractBK, IBConstants.TICK_FIELD_ASK_PRICE);
									likelyFillPrice = likelyFillPrice + (PIP_REACH * IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
									suggestedStopPrice = likelyFillPrice * (1 + STOP_FRACTION);
								}
								else {
									System.err.println("IB doesn't have ask price!");
								}
							}
							suggestedEntryPrice = CalcUtils.roundToHalfPip(continuousContractName, likelyFillPrice); 
							suggestedStopPrice = CalcUtils.roundToHalfPip(continuousContractName, suggestedStopPrice);
						}

						// Finalize the action based on whether it's a market or limit order
						action = action.toLowerCase();
						
						// Calculate position size.
						int positionSize = calculatePositionSize(wpOverUnderBenchmark, distributionFraction, action);
						boolean positionSizeCheckOK = true;
						if (positionSize > 0) {
							positionSizeCheckOK = true;
						}
						
						// Calculate the trades expiration time
						Calendar expiration = Calendar.getInstance();
						if (optionBacktest) {
							expiration.setTimeInMillis(BackTester.getCurrentPeriodEnd().getTimeInMillis());
						}
						expiration.add(Calendar.HOUR, DEFAULT_EXPIRATION_HOURS);
						if (	(expiration.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && expiration.get(Calendar.HOUR_OF_DAY) > 16) ||
								(expiration.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
								(expiration.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && expiration.get(Calendar.HOUR_OF_DAY) <= 16)){
							expiration.add(Calendar.HOUR, 48);
						}
						
						// Calculate the open order's expiration time
						Calendar openOrderExpiration = Calendar.getInstance();
						if (optionBacktest) {
							openOrderExpiration.setTimeInMillis(BackTester.getCurrentPeriodEnd().getTimeInMillis());
						}
						else {
							// If it's 3:59 and the forex day is about to close, make the trade good for an additional 15m so it can be picked up when trading opens.
							Calendar cNow = Calendar.getInstance();
							int additionalSeconds = 0;
							if (cNow.get(Calendar.HOUR_OF_DAY) == 15 && cNow.get(Calendar.MINUTE) == 59) {
								additionalSeconds = 900;
							}
							openOrderExpiration.setTimeInMillis(openOrderExpiration.getTimeInMillis() + ((STALE_TRADE_SEC + additionalSeconds) * 1000));
						}
						
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
						if (openRateLimitCheckOK && beforeFridayCutoffCheckOK && positionSizeCheckOK && numOpenOrderCheckOK) {
							if (action.equals("buy") || action.equals("sell")) {
								// Record order request in DB
								Calendar statusTime = null;
								String runName = null;
								int orderID = -1;
								if (optionBacktest) {
									statusTime = BackTester.getCurrentPeriodEnd();
									runName = BackTester.getRunName();
									orderID = BacktestQueryManager.backtestRecordTradeRequest(OrderType.LMT.toString(), orderAction.toString(), "Open Requested", statusTime,
											direction, model.bk, suggestedEntryPrice, null, suggestedStopPrice, positionSize, model.modelFile, averageLastXWPOBs(), wpOverUnderBenchmark, expiration, runName);
									System.out.println(model.modelFile + " Placed new order : " + orderAction + " " + positionSize + " at " + suggestedEntryPrice);								
								}
								else {
									orderID = IBQueryManager.recordTradeRequest(OrderType.LMT.toString(), orderAction.toString(), "Open Requested", statusTime,
											direction, model.bk, suggestedEntryPrice, null, suggestedStopPrice, positionSize * 1000, model.modelFile, averageLastXWPOBs(), wpOverUnderBenchmark, expiration, runName);
									ibWorker.placeOrder(orderID, null, OrderType.LMT, orderAction, positionSize, null, suggestedEntryPrice, false, openOrderExpiration);
								}
							}
						}	
					}
				}
				
				messages.put("Action", action);
				messages.put("Time", Formatting.sdfHHMMSS.format(now.getTime()));
				messages.put("SecondsRemaining", "0");
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
				messages.put("Price", "0");// evaluationCloseString);
				messages.put("PriceDelay", priceDelay);
				messages.put("Confidence", Formatting.df6.format(modelScore));
				messages.put("WinningPercentage", Formatting.df6.format(bucketWinningPercentage));
				messages.put("PredictionDistributionPercentage", Formatting.df6.format(model.predictionDistributionPercentage));
				messages.put("TestBucketPercentCorrect", model.getTestBucketPercentCorrectJSON());
				messages.put("TestBucketDistribution", model.getTestBucketDistributionJSON());
				if (averageWPOverUnderBenchmark != 0 && models.indexOf(model) == 0) { // Only need to send this message once per round (not for every model) and not during that timeout period after the end of a bar.
					messages.put("AverageWinningPercentage", Formatting.df6.format(averageWPOverUnderBenchmark));
				}
				messages.put("AverageLast500AWPs", /*df6.format(averageLastXAWPs())*/Formatting.df6.format(bucketWinningPercentage) );
				messages.put("LastAction", model.lastAction);
				messages.put("LastTargetClose", model.lastTargetClose);
				messages.put("LastStopClose", model.lastStopClose);
				messages.put("LastActionPrice", model.lastActionPrice);
				String lastActionTime = "";
				if (model.lastActionTime != null) {
					lastActionTime = Formatting.sdfHHMMSS.format(model.lastActionTime.getTime());
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
					
					if (Math.random() < CHANCE_OF_OPEN_ORDER_BEING_FILLED) {
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
					String direction = orderHash.get("direction").toString();
					Calendar expirationC = (Calendar)orderHash.get("expiration");
					
					double currentBid = CalcUtils.roundToHalfPip(continuousContractName, BackTester.getCurrentClose(continuousContractName));
					double currentAsk = CalcUtils.roundToHalfPip(continuousContractName, BackTester.getCurrentClose(continuousContractName));

					// See if Expiration or Closeout happened.  
					if (BackTester.getCurrentPeriodEnd().getTimeInMillis() >= expirationC.getTimeInMillis()) {
						// Expiration
						double tradePrice = 0d;
						if (direction.equals("bull")) {
							tradePrice = CalcUtils.roundToHalfPip(continuousContractName, currentBid);
						}
						else if (direction.equals("bear")) {
							tradePrice = CalcUtils.roundToHalfPip(continuousContractName, currentAsk);
						}
						BacktestQueryManager.backtestRecordClose("Open", openOrderID, tradePrice, "Expiration", filledAmount, direction, BackTester.getCurrentPeriodEnd());
						BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, tradePrice));
						Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(openOrderID);
						if (optionBacktest && proceeds != null) {
							bankRoll += proceeds;
						}
						
						System.out.println("Expiration: " + direction + " " + filledAmount + " at " + tradePrice);
					}
					else if (fridayCloseoutTime(BackTester.getCurrentPeriodEnd())) {
						// Closeout
						double tradePrice = 0d;
						if (direction.equals("bull")) {
							tradePrice = CalcUtils.roundToHalfPip(continuousContractName, currentAsk);
						}
						else if (direction.equals("bear")) {
							tradePrice = CalcUtils.roundToHalfPip(continuousContractName, currentBid);
						}
						BacktestQueryManager.backtestRecordClose("Open", openOrderID, tradePrice, "Closeout", filledAmount, direction, BackTester.getCurrentPeriodEnd());
						BacktestQueryManager.backtestNoteCloseout("Open", openOrderID);
						BacktestQueryManager.backtestUpdateCommission(openOrderID, calculateCommission(filledAmount, tradePrice));
						Double proceeds = BacktestQueryManager.backtestGetTradeProceeds(openOrderID);
						if (optionBacktest && proceeds != null) {
							bankRoll += proceeds;
						}
						
						System.out.println("Closeout: " + direction + " " + filledAmount + " at " + tradePrice);
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
				filled = filled * 1000;
				double avgFillPrice = (double)orderStatusDataHash.get("avgFillPrice");
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
				double suggestedStopPrice = ((BigDecimal)fieldHash.get("suggestedstopprice")).doubleValue();
				Timestamp expirationTS = (Timestamp)fieldHash.get("expiration");
				Calendar expiration = Calendar.getInstance();
				expiration.setTimeInMillis(expirationTS.getTime());

				if (status.equals("Filled")) {
					// Open Filled.  Needs Close & Stop orders made.  This query only checks against OpenOrderIDs so I don't have to worry about it being for a different order type.
					if (orderType.equals("Open")) {
						// Update the trade in the DB
						IBQueryManager.updateOpen(orderId, status, filled, avgFillPrice, parentId, null);
						
						boolean needsStop = IBQueryManager.checkIfNeedsStopOrder(orderId);
						if (needsStop) {
							// Get a One-Cancels-All group ID
							int ibOCAGroup = IBQueryManager.getIBOCAGroup();
							
							// Get the stop price (either the bid or ask), to use to trigger the stop
							double stopTrigger = suggestedStopPrice - (IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
							if (direction.equals("bull")) {
								stopTrigger = suggestedStopPrice + (IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
							}
							stopTrigger = CalcUtils.roundToHalfPip(continuousContractName, stopTrigger);
							
							// Make the stop trade
							int stopOrderID = IBQueryManager.recordStopTradeRequest(orderId);		
							ibWorker.placeOrder(stopOrderID, ibOCAGroup, OrderType.STP, closeAction, filled / 1000, stopTrigger, suggestedStopPrice, false, expiration);
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
				else if (status.equals("PreSubmitted")) {
					
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
						if ((expired || (fridayCloseout && optionFridayCloseout)) && remainingAmountNeededToClose > 0) {
							// Make a new Limit Close & Stop Limit Stop in the same OCA group tight against the current price and don't worry about these expiring because they won't last long.
							
							// Get a One-Cancels-All group ID
							int ibOCAGroup = IBQueryManager.getIBOCAGroup();
							
							// If this order was cancelled due to friday closeout, note it
							if (fridayCloseout) {
								IBQueryManager.noteCloseout("Close", orderId);
							}
							
							// Get prices a couple pips on each side of the bid/ask spread
							double ask = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_ASK_PRICE);
							double askPlus2Pips = ask + (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
							double bid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
							double bidMinus2Pips = bid - (PIP_SPREAD_ON_EXPIRATION * IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName));
							ask = CalcUtils.roundToHalfPip(continuousContractName, ask);
							bid = CalcUtils.roundToHalfPip(continuousContractName, bid);
							askPlus2Pips = CalcUtils.roundToHalfPip(continuousContractName, askPlus2Pips);
							bidMinus2Pips = CalcUtils.roundToHalfPip(continuousContractName, bidMinus2Pips);
							double askPlus1p5Pips = askPlus2Pips -(IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName) / 2d);
							askPlus1p5Pips = CalcUtils.roundToHalfPip(continuousContractName, askPlus1p5Pips);
							double bidMinus1p5Pips = bidMinus2Pips +(IBConstants.TICKER_PIP_SIZE_HASH.get(continuousContractName) / 2d);
							bidMinus1p5Pips = CalcUtils.roundToHalfPip(continuousContractName, bidMinus1p5Pips);
							
							// Make a good-till-date far in the future
							Calendar gtd = Calendar.getInstance();
							gtd.add(Calendar.DATE, 100);
							
							Calendar statusTime = null;
							if (optionBacktest) {
								statusTime = BackTester.getCurrentPeriodEnd();
							}
							
							if (direction.equals("bull")) {
								// Make the new close trade
								int newCloseOrderID = IBQueryManager.updateCloseTradeRequestWithCloseOrderID(orderId, ibOCAGroup, statusTime);
								ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, askPlus2Pips, false, gtd);
								
								// Make the new stop trade
								int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
								ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, bidMinus1p5Pips, bidMinus2Pips, false, gtd);
						
								System.out.println("Bull Position Expired.  Making new tight exits.  " + newCloseOrderID + " in place of " + orderId + ", " + askPlus2Pips);
								System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
							}
							else {
								// Make the new close trade
								int newCloseOrderID = IBQueryManager.updateCloseTradeRequestWithCloseOrderID(orderId, ibOCAGroup, statusTime);
								ibWorker.placeOrder(newCloseOrderID, ibOCAGroup, OrderType.LMT, closeAction, remainingAmountNeededToClose, null, bidMinus2Pips, false, gtd);
								
								// Make the new stop trade						
								int newStopOrderID = IBQueryManager.updateStopTradeRequest(newCloseOrderID, statusTime);
								ibWorker.placeOrder(newStopOrderID, ibOCAGroup, OrderType.STP, closeAction, remainingAmountNeededToClose, askPlus1p5Pips, askPlus2Pips, false, gtd);
	
								System.out.println("Bear Position Expired.  Making new tight exits  " + newCloseOrderID + " in place of " + orderId + ", " + bidMinus2Pips);
								System.out.println(ibOCAGroup + ", " + closeAction + ", " + remainingAmountNeededToClose + ", " + bidMinus2Pips + ", " + askPlus2Pips + ", " + gtd.getTime().toString());
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
		 * These events only come from IBEngine2
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
				
				if (errorCode == 200) { // 200 = No security definition has been found for the request
					IBQueryManager.recordRejection(orderType, orderID, null);
				}
				else if (errorCode == 201) { // 201 = Order rejected
					IBQueryManager.recordRejection(orderType, orderID, null);
				}
				else {
					System.err.println("Previously unencountered error code: " + errorCode + " for order #" + orderID);
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
						double currentAsk = BackTester.getCurrentClose(continuousContractName);
						bpPositionSizeALT = (int)(buyingPower / currentAsk);
						basePositionSizeALT = (int)(BASE_TRADE_SIZE / currentAsk);
						minPositionSizeALT = (int)(MIN_TRADE_SIZE / currentAsk);
						maxPositionSizeALT = (int)(MAX_TRADE_SIZE / currentAsk);
					}
					if (action.equals("sell")) {
						double currentBid = BackTester.getCurrentClose(continuousContractName);
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
							double currentAsk = (rawCurrentAsk != null ? Double.parseDouble(Formatting.df6.format(rawCurrentAsk)) : 0);
							bpPositionSizeALT = (int)(buyingPower / currentAsk);
							basePositionSizeALT = (int)(BASE_TRADE_SIZE / currentAsk);
							minPositionSizeALT = (int)(MIN_TRADE_SIZE / currentAsk);
							maxPositionSizeALT = (int)(MAX_TRADE_SIZE / currentAsk);
						}
						if (action.equals("sell")) {
							Double rawCurrentBid = ibs.getTickerFieldValue(ibWorker.getBarKey(), IBConstants.TICK_FIELD_BID_PRICE);
							double currentBid = (rawCurrentBid != null ? Double.parseDouble(Formatting.df6.format(rawCurrentBid)) : 0);
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
				
				// Backtest position sizes are 1000x bigger - I think IB puts ZN orders in lots of 1000 automatically.
				if (!optionBacktest) {
					basePositionSizeALT /= 1000;
				}
				
				return basePositionSizeALT;
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		private double calculateCommission(int positionSize, double price) {
			double commission = positionSize * .001 * 1.61 * 2;
			return new Double(Formatting.df2.format(commission)).doubleValue();
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
					int cutoffMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF;
					if (minutesIntoDay < cutoffMinute) {
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
					int cutoffMinute = (16 * 60) - MIN_BEFORE_FRIDAY_CLOSE_TRADE_CUTOFF;
					if (minutesIntoDay < cutoffMinute) {
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
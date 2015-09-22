package threads;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.Gson;

import data.Bar;
import data.MetricKey;
import data.Model;
import dbio.QueryManager;
import ml.ARFF;
import ml.Modelling;
import singletons.StatusSingleton;
import trading.Commission;
import trading.PositionSizing;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class TradingThread extends Thread {

	private boolean running = false;
	private StatusSingleton ss = null;
	private ArrayList<Model> models = new ArrayList<Model>();
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = new HashMap<MetricKey, ArrayList<Float>>();
	private String modelsPath = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public TradingThread() {
		ss = StatusSingleton.getInstance();
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
			for (Model model : models) {
				try {
					HashMap<String, String> openMessages = monitorOpen(model);
					HashMap<String, String> closeMessages = monitorClose(model);
					String jsonMessages = packageMessages(openMessages, closeMessages);
					ss.addMessageToTradingMessageQueue(jsonMessages);	
					
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
	
	private HashMap<String, String> monitorOpen(Model model) {
		HashMap<String, String> messages = new HashMap<String, String>();
		try {
			Calendar c = Calendar.getInstance();
			Calendar periodStart = CalendarUtils.getBarStart(c, model.getBk().duration);
			Calendar periodEnd = CalendarUtils.getBarEnd(c, model.getBk().duration);
			
			long barLengthMS = periodEnd.getTimeInMillis() - periodStart.getTimeInMillis();
			long barSoFarMS = c.getTimeInMillis() - periodStart.getTimeInMillis();
			long barRemainingMS = periodEnd.getTimeInMillis() - c.getTimeInMillis();
			double barPercentComplete = barSoFarMS / (double)barLengthMS;
			int secsUntilBarEnd = (int)barRemainingMS / 1000;
			int secsUntilNextSignal = secsUntilBarEnd - 5;
			
			String actionMessage = "Waiting";
			String timeMessage = sdf.format(c.getTime());
			String modelMessage = model.getModelFile();

			Bar mostRecentBar = QueryManager.getMostRecentBar(model.getBk(), Calendar.getInstance());
			String priceString = new Double((double)Math.round(mostRecentBar.close * 100) / 100).toString();
			
			Calendar lastBarUpdate = ss.getLastDownload(model.getBk());
			String priceDelay = "";
			if (lastBarUpdate != null) {
				long timeSinceLastBarUpdate = c.getTimeInMillis() - lastBarUpdate.getTimeInMillis();
				priceDelay = new Double((double)Math.round((timeSinceLastBarUpdate / 1000d) * 100) / 100).toString();
			}
			
			// If we're within 5 seconds of the end of the bar
			if (barRemainingMS < 5000) {
				ArrayList<ArrayList<Object>> unlabeledList = ARFF.createUnlabeledWekaArffData(periodStart, periodEnd, model.getBk(), model.getMetrics(), metricDiscreteValueHash);
				Instances instances = Modelling.loadData(model.getMetrics(), unlabeledList);
				Classifier classifier = Modelling.loadModel(model.getModelFile(), modelsPath);

				if (instances != null && instances.firstInstance() != null) {
					double label = classifier.classifyInstance(instances.firstInstance());
					
					String action = "None";
					if (model.type.equals("bull")) {
						if (label == 1) {
							action = "Buy";
							double targetClose = (double)mostRecentBar.close * (1d + ((double)model.sellMetricValue / 100d));
							String targetCloseString = new Double((double)Math.round(targetClose * 100) / 100).toString();
							double targetStop = (double)mostRecentBar.close * (1d - ((double)model.stopMetricValue / 100d));
							String stopCloseString = new Double((double)Math.round(targetStop * 100) / 100).toString();
							
							model.lastActionPrice = priceString;
							model.lastAction = action;
							model.lastActionTime = c;
							model.lastTargetClose = targetCloseString;
							model.lastStopClose = stopCloseString;
						}
					}
					if (model.type.equals("bear")) {
						if (label == 1) {
							action = "Sell";
							double targetClose = (double)mostRecentBar.close * (1d - ((double)model.sellMetricValue / 100d));
							String targetCloseString = new Double((double)Math.round(targetClose * 100) / 100).toString();
							double targetStop = (double)mostRecentBar.close * (1d + ((double)model.stopMetricValue / 100d));
							String stopCloseString = new Double((double)Math.round(targetStop * 100) / 100).toString();
							
							model.lastActionPrice = priceString;
							model.lastAction = action;
							model.lastActionTime = c;
							model.lastTargetClose = targetCloseString;
							model.lastStopClose = stopCloseString;
						}
					}
					
					// Testing 
					if (label == 1) {
						QueryManager.insertTestTrade(model.modelFile, model.lastActionTime, Double.parseDouble(model.lastActionPrice), Double.parseDouble(model.lastTargetClose), Double.parseDouble(model.lastStopClose), model.numBars);
					
						// Calculate position size
						float tradePrice = Float.parseFloat(priceString);
						float cash = QueryManager.getTradingAccountCash();
						int numShares = PositionSizing.getPositionSize(model.bk.symbol, tradePrice);
						float commission = Commission.getOKCoinEstimatedCommission();
						float tradeCost = (numShares * Float.parseFloat(priceString)) + commission;
						
						// Send trade signal
						System.out.println("Opening LONG position on " + model.bk.symbol);
						int tradeID = QueryManager.makeTrade("long", model.bk.symbol, tradePrice, numShares, commission, model);
						QueryManager.updateTradingAccountCash(cash - tradeCost);
					}
					
					actionMessage = action;
				}
			}
			else {
				actionMessage = "Waiting";
			}
			
			messages.put("Action", actionMessage);
			messages.put("Time", timeMessage);
			messages.put("SecondsRemaining", new Integer(secsUntilNextSignal).toString());
			messages.put("Model", modelMessage);
			messages.put("TestWinPercentage", new Double((double)Math.round(model.getTestWinPercent() * 1000) / 10).toString());
			messages.put("TestEstimatedAverageReturn", new Double((double)Math.round(model.getTestEstimatedAverageReturn() * 1000) / 1000).toString());
			messages.put("Type", model.type);
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
	
	private HashMap<String, String> monitorClose(Model model) {
		HashMap<String, String> messages = new HashMap<String, String>();
		try {
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
}
package threads;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;

import data.MetricKey;
import data.Model;
import ml.ARFF;
import ml.Modelling;
import singletons.StatusSingleton;
import utils.CalendarUtils;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class TradingThread extends Thread {

	private boolean running = false;
	private StatusSingleton ss = null;
	private ArrayList<Model> models = new ArrayList<Model>();
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = new HashMap<MetricKey, ArrayList<Float>>();
	private String modelsPath = null;

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
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			
			while (running) {
				for (Model model : models) {
					Calendar c = Calendar.getInstance();
					Calendar periodStart = CalendarUtils.getBarStart(c, model.getBk().duration);
					Calendar periodEnd = CalendarUtils.getBarEnd(c, model.getBk().duration);
					
					long barLengthMS = periodEnd.getTimeInMillis() - periodStart.getTimeInMillis();
					long barSoFarMS = c.getTimeInMillis() - periodStart.getTimeInMillis();
					long barRemainingMS = periodEnd.getTimeInMillis() - c.getTimeInMillis();
					double barPercentComplete = barSoFarMS / (double)barLengthMS;
					int secsUntilBarEnd = (int)barRemainingMS / 1000;
					int secsUntilNextSignal = secsUntilBarEnd - 5;
					
					HashMap<String, String> messages = new HashMap<String, String>();
					
					String actionMessage = "Waiting";
					String timeMessage = sdf.format(c.getTime());
					String modelMessage = model.getModelFile();

					// If we're within 5 seconds of the end of the bar
					if (barRemainingMS < 5000) {
						Classifier classifier = Modelling.loadModel(model.getModelFile(), modelsPath);
						
						ArrayList<ArrayList<Object>> unlabeledList = ARFF.createUnlabeledWekaArffData(periodStart, periodEnd, model.getBk(), model.getMetrics(), metricDiscreteValueHash);
						Instances instances = Modelling.loadData(model.getMetrics(), unlabeledList);
						if (instances != null && instances.firstInstance() != null) {
							double label = classifier.classifyInstance(instances.firstInstance());
							
							String action = "None";
							if (model.type.equals("bull")) {
								if (label == 1) {
									action = "Buy";
								}
							}
							if (model.type.equals("bear")) {
								if (label == 1) {
									action = "Sell";
								}
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
					Gson gson = new Gson();
					String json = gson.toJson(messages);
					ss.addMessageToTradingMessageQueue(json);
					
//					ss.addMessageToTradingMessageQueue(actionMessage);
//					ss.addMessageToTradingMessageQueue(timeMessage);
				}
				
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
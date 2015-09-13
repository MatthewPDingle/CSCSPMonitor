package threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants.BAR_SIZE;
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
			while (running) {
				for (Model model : models) {
					Calendar c = Calendar.getInstance();
					Calendar periodStart = CalendarUtils.getBarStart(c, model.getBk().duration);
					Calendar periodEnd = CalendarUtils.getBarEnd(c, model.getBk().duration);
					
					Classifier classifier = Modelling.loadModel(model.getModelFile(), modelsPath);
					
					ArrayList<ArrayList<Object>> unlabeledList = ARFF.createUnlabeledWekaArffData(periodStart, periodEnd, model.getBk(), model.getMetrics(), metricDiscreteValueHash);
					Instances instances = Modelling.loadData(model.getMetrics(), unlabeledList);
					if (instances != null && instances.firstInstance() != null) {
						double label = classifier.classifyInstance(instances.firstInstance());
						ss.addMessageToTradingMessageQueue(model.getModelFile() + " - Indicating " + label);
					}
				}
				
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
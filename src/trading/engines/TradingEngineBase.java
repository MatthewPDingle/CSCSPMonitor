package trading.engines;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

import data.MetricKey;
import data.Model;
import status.StatusSingleton;

public abstract class TradingEngineBase extends Thread {

	protected final int TRADING_WINDOW_MS = 5000; // How many milliseconds before the end of a bar trading is evaluated for real
	protected final int TRADING_TIMEOUT = 30000; // How many milliseconds have to pass after a specific model has traded before it is allowed to trade again
	
	protected boolean running = false;
	protected StatusSingleton ss = null;
	protected ArrayList<Model> models = new ArrayList<Model>();
	protected HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = new HashMap<MetricKey, ArrayList<Float>>();
	protected String modelsPath = null;
	protected SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public TradingEngineBase() {
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

	public String packageMessages(HashMap<String, String> openMessages, HashMap<String, String> closeMessages) {
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
	
	public abstract void run();
	
	public abstract HashMap<String, String> monitorOpen(Model model);
	
	public abstract HashMap<String, String> monitorClose(Model model);
}

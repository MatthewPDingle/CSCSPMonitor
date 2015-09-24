package singletons;

import java.util.ArrayList;
import java.util.HashMap;

import data.MetricKey;
import data.Model;
import dbio.QueryManager;
import trading.TradingThread;

public class TradingSingleton {

	private static TradingSingleton instance = null;
	
	private TradingThread tt = new TradingThread();
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash;
	private ArrayList<Model> tradingModels;
	private String modelsPath = "";
	
	protected TradingSingleton() {
		metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
		tradingModels = new ArrayList<Model>();
	}
	
	public static TradingSingleton getInstance() {
		if (instance == null) {
			instance = new TradingSingleton();
		}
		return instance;
	}

	public void setRunning(boolean running) {
		try {
			if (running) {
				if (!tt.isRunning()) {
					tt = new TradingThread();
					tt.setModels(tradingModels);
					tt.setMetricDiscreteValueHash(metricDiscreteValueHash);
					tt.setModelsPath(modelsPath);
					tt.setRunning(true);
					tt.start();
				}
			}
			else {
				tt.setRunning(false);
				tt.join();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<MetricKey, ArrayList<Float>> getMetricDiscreteValueHash() {
		return metricDiscreteValueHash;
	}

	public ArrayList<Model> getTradingModels() {
		return tradingModels;
	}

	public void setTradingModels(ArrayList<Model> tradingModels) {
		this.tradingModels = tradingModels;
		tt.setModels(tradingModels);
	}

	public String getModelsPath() {
		return modelsPath;
	}

	public void setModelsPath(String modelsPath) {
		this.modelsPath = modelsPath;
		tt.setModelsPath(modelsPath);
	}
}
package servlets.trading;

import java.util.ArrayList;
import java.util.HashMap;

import data.MetricKey;
import data.Model;
import dbio.QueryManager;
import ml.Modelling;
import trading.engines.OKCoinPaperTradingEngine;
import weka.classifiers.Classifier;

public class TradingSingleton {

	private static TradingSingleton instance = null;
	
	private OKCoinPaperTradingEngine tt = new OKCoinPaperTradingEngine();
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash;
	private ArrayList<Model> tradingModels;
	private HashMap<String, Classifier> wekaClassifierHash;
	private String modelsPath = "";
	
	protected TradingSingleton() {
		metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
		tradingModels = new ArrayList<Model>();
		wekaClassifierHash = new HashMap<String, Classifier>();
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
					tt = new OKCoinPaperTradingEngine();
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

	public void addClassifierToHash(String modelFile, Classifier c) {
		wekaClassifierHash.put(modelFile, c);
	}
	
	public HashMap<String, Classifier> getWekaClassifierHash() {
		return wekaClassifierHash;
	}

	public void setWekaClassifierHash(HashMap<String, Classifier> wekaClassifierHash) {
		this.wekaClassifierHash = wekaClassifierHash;
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
		
		for (Model model : tradingModels) {
			if (wekaClassifierHash.get(model.modelFile) == null) {
				Classifier classifier = Modelling.loadZippedModel(model.modelFile, modelsPath);
				wekaClassifierHash.put(model.modelFile, classifier);
			}
		}
	}

	public String getModelsPath() {
		return modelsPath;
	}

	public void setModelsPath(String modelsPath) {
		this.modelsPath = modelsPath;
		tt.setModelsPath(modelsPath);
	}
}
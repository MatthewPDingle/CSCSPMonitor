package trading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import data.Model;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
import dbio.QueryManager;
import ml.Modelling;
import trading.engines.IBTestEngine;
import trading.engines.TradingEngineBase;
import weka.classifiers.Classifier;

public class TradingSingleton {

//	private static String ENGINE = "OKCoinLiveStrict";
//	private static String ENGINE = "OKCoinPaperStrict";
//	private static String ENGINE = "OKCoinPaperRESTLoose";
		
	private static TradingSingleton instance = null;
	
	private TradingEngineBase tradingEngine;
	
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash;
	private HashMap<BarKey, ArrayList<Model>> bkModelHash; // Each BarKey has a list of Models that it uses for trading.  Each BarKey also gets it's own IBWorker for API interaction.
	private HashMap<BarKey, TradingEngineBase> bkEngineHash; // Each BarKey also has it's own trading engine.
	private HashMap<String, Classifier> wekaClassifierHash;
	private String modelsPath = "";
	
	protected TradingSingleton() {

		metricDiscreteValueHash = QueryManager.loadMetricDisccreteValueHash();
		bkModelHash = new HashMap<BarKey, ArrayList<Model>>();
		bkEngineHash = new HashMap<BarKey, TradingEngineBase>();
		wekaClassifierHash = new HashMap<String, Classifier>();
		
		// TODO: Put something in the ModelManagement page to select which engines to run against BarKeys
		IBWorker ibWorker = IBSingleton.getInstance().requestWorker(new BarKey("EUR.USD", BAR_SIZE.BAR_1M));
		bkEngineHash.put(new BarKey("EUR.USD", BAR_SIZE.BAR_1M), new IBTestEngine(ibWorker));
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
				for (Entry<BarKey, TradingEngineBase> entry : bkEngineHash.entrySet()) {
					TradingEngineBase engine = entry.getValue();
					if (!engine.isRunning()) {
						ArrayList<Model> models = bkModelHash.get(entry.getKey());
						engine.setModels(models);
						engine.setMetricDiscreteValueHash(metricDiscreteValueHash);
						engine.setModelsPath(modelsPath);
						engine.setRunning(true);
						engine.start();
					}
				}
			}
			else {
				for (TradingEngineBase engine : bkEngineHash.values()) {
					engine.setRunning(false);
					engine.join();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setBkEngineHash(HashMap<BarKey, TradingEngineBase> bkEngineHash) {
		this.bkEngineHash = bkEngineHash;
	}

	public void addClassifierToHash(String modelFile, Classifier c) {
		wekaClassifierHash.put(modelFile, c);
	}
	
	public HashMap<String, Classifier> getWekaClassifierHash() {
		return wekaClassifierHash;
	}

	public void setModelsPath(String modelsPath) {
		this.modelsPath = modelsPath;
		tradingEngine.setModelsPath(modelsPath);
	}
	
	public void addModel(Model model) {
		// Associate this model with it's BarKey
		ArrayList<Model> bkModels = bkModelHash.get(model.getBk());
		if (bkModels == null) {
			bkModels = new ArrayList<Model>();
		}
		if (!bkModels.contains(model)) {
			bkModels.add(model);
		}
		bkModelHash.put(model.getBk(), bkModels);
		
		// Add the model to the hash of classifiers by name
		if (wekaClassifierHash.get(model.modelFile) == null) {
			Classifier classifier = Modelling.loadZippedModel(model.modelFile, modelsPath);
			wekaClassifierHash.put(model.modelFile, classifier);
		}
	}
	
	public void clearBKModelHash() {
		bkModelHash = new HashMap<BarKey, ArrayList<Model>>();
	}
}
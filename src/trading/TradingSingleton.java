package trading;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.BarWithMetricData;
import data.MetricKey;
import data.Model;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
import dbio.QueryManager;
import ml.Modelling;
import trading.engines.IBForexEngine2;
import trading.engines.IBFutureZNEngine2;
import trading.engines.TradingEngineBase;
import weka.classifiers.Classifier;

public class TradingSingleton {

//	private static String ENGINE = "OKCoinLiveStrict";
//	private static String ENGINE = "OKCoinPaperStrict";
//	private static String ENGINE = "OKCoinPaperRESTLoose";
		
	private static TradingSingleton instance = null;
	
	private HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash;
	private HashMap<BarKey, ArrayList<Model>> bkModelHash; // Each BarKey has a list of Models that it uses for trading.  Each BarKey also gets it's own IBWorker for API interaction.
	private HashMap<BarKey, TradingEngineBase> bkEngineHash; // Each BarKey also has it's own trading engine.
	private HashMap<String, Classifier> wekaClassifierHash;
	private String modelsPath = "";
	
	protected TradingSingleton() {

		metricDiscreteValueHash = QueryManager.loadMetricDiscreteValueHash("Percentiles Set 10");
		bkModelHash = new HashMap<BarKey, ArrayList<Model>>();
		bkEngineHash = new HashMap<BarKey, TradingEngineBase>();
		wekaClassifierHash = new HashMap<String, Classifier>();
		
		// TODO: Put something in the ModelManagement page to select which engines to run against BarKeys
		IBWorker ibWorkerEURUSD1H = IBSingleton.getInstance().requestWorker(new BarKey("EUR.USD", BAR_SIZE.BAR_1H));
		IBWorker ibWorkerEURGBP1H = IBSingleton.getInstance().requestWorker(new BarKey("EUR.GBP", BAR_SIZE.BAR_1H));
		IBWorker ibWorkerGBPUSD1H = IBSingleton.getInstance().requestWorker(new BarKey("GBP.USD", BAR_SIZE.BAR_1H));
		IBWorker ibWorkerEURUSD2H = IBSingleton.getInstance().requestWorker(new BarKey("EUR.USD", BAR_SIZE.BAR_2H));
		IBWorker ibWorkerEURGBP2H = IBSingleton.getInstance().requestWorker(new BarKey("EUR.GBP", BAR_SIZE.BAR_2H));
		IBWorker ibWorkerGBPUSD2H = IBSingleton.getInstance().requestWorker(new BarKey("GBP.USD", BAR_SIZE.BAR_2H));
		IBWorker ibWorkerES1H = IBSingleton.getInstance().requestWorker(new BarKey("ES", BAR_SIZE.BAR_1H));
		IBWorker ibWorkerZN1H = IBSingleton.getInstance().requestWorker(new BarKey("ZN", BAR_SIZE.BAR_1H));
		IBWorker ibWorkerZN2H = IBSingleton.getInstance().requestWorker(new BarKey("ZN", BAR_SIZE.BAR_2H));
//		IBWorker ibWorkerEURUSD5M = IBSingleton.getInstance().requestWorker(new BarKey("EUR.USD", BAR_SIZE.BAR_5M));
		bkEngineHash.put(new BarKey("EUR.USD", BAR_SIZE.BAR_1H), new IBForexEngine2(ibWorkerEURUSD1H));
		bkEngineHash.put(new BarKey("EUR.GBP", BAR_SIZE.BAR_1H), new IBForexEngine2(ibWorkerEURGBP1H));
		bkEngineHash.put(new BarKey("GBP.USD", BAR_SIZE.BAR_1H), new IBForexEngine2(ibWorkerGBPUSD1H));
		bkEngineHash.put(new BarKey("EUR.USD", BAR_SIZE.BAR_2H), new IBForexEngine2(ibWorkerEURUSD2H));
		bkEngineHash.put(new BarKey("EUR.GBP", BAR_SIZE.BAR_2H), new IBForexEngine2(ibWorkerEURGBP2H));
		bkEngineHash.put(new BarKey("GBP.USD", BAR_SIZE.BAR_2H), new IBForexEngine2(ibWorkerGBPUSD2H));
		bkEngineHash.put(new BarKey("ES", BAR_SIZE.BAR_1H), new IBForexEngine2(ibWorkerES1H));
		bkEngineHash.put(new BarKey("ZN", BAR_SIZE.BAR_1H), new IBFutureZNEngine2(ibWorkerZN1H));
		bkEngineHash.put(new BarKey("ZN", BAR_SIZE.BAR_2H), new IBFutureZNEngine2(ibWorkerZN2H));
//		bkEngineHash.put(new BarKey("EUR.USD", BAR_SIZE.BAR_5M), new IBEngine2(ibWorkerEURUSD5M));
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
						if (models != null) {
							engine.setModels(models);
							engine.setMetricDiscreteValueHash(metricDiscreteValueHash);
							engine.setModelsPath(modelsPath);
							engine.setRunning(true);
							if (engine.getState() == State.NEW) {
								engine.start();
							}
						}
					}
				}
			}
			else {
				for (TradingEngineBase engine : bkEngineHash.values()) {
					engine.setRunning(false);
//					engine.join();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getEngineToString(BarKey bk) {
		return bkEngineHash.get(bk).toString();
	}
	
	public void refreshEngineModels() {
		try {
			for (Entry<BarKey, TradingEngineBase> entry : bkEngineHash.entrySet()) {
				TradingEngineBase engine = entry.getValue();
				if (engine.isRunning()) {
					engine.setModels(bkModelHash.get(entry.getKey()));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRunning() {
		boolean isRunning = false;
		for (TradingEngineBase engine : bkEngineHash.values()) {
			if (engine.isRunning()) {
				isRunning = true;
			}
		}
		return isRunning;
	}
	
	public void kill() {
		try {
			for (TradingEngineBase engine : bkEngineHash.values()) {
				engine.setRunning(false);
				engine.interrupt();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setBacktestBarWMDListForBacktest(BarKey bk, ArrayList<BarWithMetricData> barWMDList) {
		TradingEngineBase te = bkEngineHash.get(bk);
		if (te != null) {
			if (te instanceof IBForexEngine2) {
				((IBForexEngine2)te).setOptionBacktest(true);
				te.setBacktestBarWMDList(barWMDList);
			}
			if (te instanceof IBFutureZNEngine2) {
				((IBFutureZNEngine2)te).setOptionBacktest(true);
				te.setBacktestBarWMDList(barWMDList);
			}
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
		for (Entry<BarKey, TradingEngineBase> entry : bkEngineHash.entrySet()) {
			entry.getValue().setModelsPath(modelsPath);
		}
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
	
	public void clearWekaClassifierHash() {
		wekaClassifierHash = new HashMap<String, Classifier>();
	}
}
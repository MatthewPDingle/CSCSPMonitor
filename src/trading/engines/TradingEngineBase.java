package trading.engines;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

import data.BarWithMetricData;
import data.MetricKey;
import data.Model;
import data.downloaders.okcoin.OKCoinConstants;
import status.StatusSingleton;

public abstract class TradingEngineBase extends Thread {

	protected final int TRADING_WINDOW_MS = 15000; // How many milliseconds before the end of a bar trading is evaluated for real
	protected final int TRADING_TIMEOUT = 300000; // How many milliseconds have to pass after a specific model has traded before it is allowed to trade again
	
	protected boolean running = false;
	protected StatusSingleton ss = null;
	protected ArrayList<Model> models = new ArrayList<Model>();
	protected HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = new HashMap<MetricKey, ArrayList<Float>>();
	protected String modelsPath = null;
	protected ArrayList<BarWithMetricData> backtestBarWMDList = new ArrayList<BarWithMetricData>();
	
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
		if (models != null) {
			this.models.clear();
			this.models.addAll(models);
		}
	}

	public void setMetricDiscreteValueHash(HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash) {
		this.metricDiscreteValueHash = metricDiscreteValueHash;
	}

	public void setModelsPath(String modelsPath) {
		this.modelsPath = modelsPath;
	}

	public ArrayList<BarWithMetricData> getBacktestBarWMDList() {
		return backtestBarWMDList;
	}

	public void setBacktestBarWMDList(ArrayList<BarWithMetricData> backtestBarWMDList) {
		this.backtestBarWMDList = backtestBarWMDList;
	}
	
	public HashMap<MetricKey, ArrayList<Float>> getMetricDiscreteValueHash() {
		return metricDiscreteValueHash;
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
	
	/**
	 * Finds the best price to place a limit order at.  If the best price in the order book would be better than the model 
	 * price, then use the best price in the order book +/- 1 pip.  Otherwise use the model price.
	 * 
	 * @param orderBook
	 * @param orderBookType
	 * @param modelPrice
	 * @return
	 */
	public double findBestOrderBookPrice(ArrayList<ArrayList<Double>> orderBook, String orderBookType, double modelPrice) {
		
//		System.out.println("OrderBookType: " + orderBookType);
//		for (ArrayList<Double> obl : orderBook) {
//			System.out.print(obl.get(0) + ", ");
//		}
//		System.out.println("");
		
		if (orderBookType.equals("bid")) {
			double bestBid = orderBook.get(0).get(0);
			double bestOBPrice = bestBid + OKCoinConstants.PIP_SIZE;
			if (bestOBPrice < modelPrice) {
				return bestOBPrice;
			}
			else {
				return modelPrice;
			}
		}
		else if (orderBookType.equals("ask")) {
			double bestAsk = orderBook.get(orderBook.size() - 1).get(0);
			double bestOBPrice = bestAsk - OKCoinConstants.PIP_SIZE;
			if (bestOBPrice > modelPrice) {
				return bestOBPrice;
			}
			else {
				return modelPrice;
			}
		}
		return modelPrice;
	}
	
	public double estimateMarketOrderVWAP(ArrayList<ArrayList<Double>> orderBook, String orderBookType, double orderSize) {
		// Build the vwap pieces
		ArrayList<double[]> vwapPieces = new ArrayList<double[]>();
		if (orderBookType.equals("bid")) {
			for (ArrayList<Double> bid : orderBook) {
				if (bid.get(1) > orderSize) {
					double[] piece = new double[2];
					piece[0] = bid.get(0);
					piece[1] = orderSize;
					vwapPieces.add(piece);
					break;
				}
				else {
					orderSize -= bid.get(1);
					double[] piece = new double[2];
					piece[0] = bid.get(0);
					piece[1] = bid.get(1);
					vwapPieces.add(piece);
				}
			}
		}
		else if (orderBookType.equals("ask")) {
			for (int a = orderBook.size() - 1; a >= 0; a--) {
				ArrayList<Double> ask = orderBook.get(a);
				if (ask.get(1) > orderSize) {
					double[] piece = new double[2];
					piece[0] = ask.get(0);
					piece[1] = orderSize;
					vwapPieces.add(piece);
					break;
				}
				else {
					orderSize -= ask.get(1);
					double[] piece = new double[2];
					piece[0] = ask.get(0);
					piece[1] = ask.get(1);
					vwapPieces.add(piece);
				}
			}
		}
		
		// Now calculate the vwap
		double sumVolumes = 0;
		double sumProducts = 0;
		for (double[] piece : vwapPieces) {
			sumVolumes += piece[1];
			sumProducts += (piece[0] * piece[1]);
		}
		double vwap = sumProducts / sumVolumes;
		
		return vwap;
	}
}
package metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants;
import data.Bar;
import data.BarKey;
import data.Metric;
import data.MetricKey;
import data.MetricTimeCache;
import dbio.QueryManager;
import status.StatusSingleton;
import utils.CalendarUtils;

public class MetricSingleton {

	private static MetricSingleton instance = null;
	
	private static final int NUM_THREADS = 10;
	private MetricsUpdaterThread[] muts = new MetricsUpdaterThread[NUM_THREADS];
	private boolean threadsRunning = false;

	// List of metrics I need updated
	ArrayList<String> neededMetrics = null;
	
	// List of BarKeys that will be getting metrics
	ArrayList<BarKey> barKeys = new ArrayList<BarKey>();

	// ArrayList<Metric> = chronological list of 100 metrics.  HashMap<MetricKey,... stores all of these chronological lists for each MetricKey
	private HashMap<MetricKey, ArrayList<Metric>> metricSequenceHash = new HashMap<MetricKey, ArrayList<Metric>>();
	private HashMap<MetricKey, MetricTimeCache> metricTimeCache = new HashMap<MetricKey, MetricTimeCache>();
	private ArrayList<MetricKey> metricSequenceKeyList = new ArrayList<MetricKey>(); // Used to help iterate through the metricSequenceHash
	private int metricSequenceIndex = 0;
	
	protected MetricSingleton() {
		for (int a = 0; a < NUM_THREADS; a++) {
			muts[a] = new MetricsUpdaterThread();
		}
	}
	
	public static MetricSingleton getInstance() {
		if (instance == null) {
			instance = new MetricSingleton();
		}
		return instance;
	}
	
	public void stopThreads() {
		try {
			for (MetricsUpdaterThread mut : muts) {
				if (mut != null && mut.isRunning()) {
					mut.setRunning(false);
					mut.join();
				}
			}
			threadsRunning = false;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startThreads() {
		try {
			for (int a = 0; a < muts.length; a++) {
				MetricsUpdaterThread mut = muts[a];

				if (!mut.isRunning()) {
					mut = new MetricsUpdaterThread();
					muts[a] = mut;
					mut.setRunning(true);
					threadsRunning = true;
					mut.start();
				}
			}
			// Wait for it to finish
			for (MetricsUpdaterThread mut : muts) {
				if (mut != null && mut.isRunning()) {
					mut.join();
					mut.setRunning(false);
				}
			}
			threadsRunning = false;
			metricSequenceIndex = 0;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean areThreadsRunning() {
		return threadsRunning;
	}
	
	public MetricTimeCache getMetricTimeCache(MetricKey mk) {
		return metricTimeCache.get(mk);
	}
	
	public void updateMetricTimeCache(MetricKey mk, Calendar start) {
		MetricTimeCache mtc = metricTimeCache.get(mk);
		// If this MetricTimeCache doesn't exist yet because we don't have any metrics in the system
		if (mtc == null) {
			mtc = new MetricTimeCache(start, start);
			metricTimeCache.put(mk, mtc);
		}
		
		if (start != null && start.before(mtc.minStart)) {
			mtc.minStart = start;
		}
		if (start != null && start.after(mtc.maxStart)) {
			mtc.maxStart = start;
		}
	}

	public void init(ArrayList<BarKey> barKeys, ArrayList<String> neededMetrics) {
		this.neededMetrics = neededMetrics;
		metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys, neededMetrics);
		metricTimeCache = QueryManager.loadMetricTimeCache(barKeys);
		metricSequenceKeyList.clear();
		metricSequenceKeyList.addAll(metricSequenceHash.keySet());
		StatusSingleton.getInstance().addMessageToDataMessageQueue("MetricSingleton init(...) finished for " + metricSequenceKeyList.size() + " metrics");
	}
	
	public void init() {
		metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys, neededMetrics);
		metricTimeCache = QueryManager.loadMetricTimeCache(barKeys);
		metricSequenceKeyList.clear();
		metricSequenceKeyList.addAll(metricSequenceHash.keySet());
		StatusSingleton.getInstance().addMessageToDataMessageQueue("MetricSingleton init(...) finished for " + metricSequenceKeyList.size() + " metrics");
	}
	
	public synchronized void updateMetricSequenceHash(Bar bar) {
		BarKey bk = new BarKey(bar.symbol, bar.duration);
		
		if (neededMetrics != null) {
			for (String metricName : neededMetrics) {
				MetricKey mk = new MetricKey(metricName, bar.symbol, bar.duration);
				ArrayList<Metric> ms = metricSequenceHash.get(mk);
				
				if (ms != null) {
					Metric lastMetricInMetricSequence = ms.get(ms.size() - 1);
					// If the Bar data we got corresponds to the last Metric in the MetricSequence
					if (CalendarUtils.areSame(lastMetricInMetricSequence.start, bar.periodStart)) {
						Metric updatedMetric = new Metric(metricName, bk.symbol, bar.periodStart, bar.periodEnd, bk.duration, bar.volume, bar.open, bar.close, bar.high, bar.low, bar.gap, bar.change, bar.close, bar.change);
						while (ms.size() > Constants.METRIC_NEEDED_BARS.get(metricName)) {
							ms.remove(0);
						}
						ms.set(ms.size() - 1, updatedMetric);
	//					System.out.print("," + ms.size());
					}
					// Otherwise if the Bar data corresponds to the next bar slot after the last Metric in the MetricSequence
					else if (CalendarUtils.areSame(lastMetricInMetricSequence.end, bar.periodStart)){
						Metric newMetric = new Metric(metricName, bk.symbol, bar.periodStart, bar.periodEnd, bk.duration, bar.volume, bar.open, bar.close, bar.high, bar.low, bar.gap, bar.change, bar.close, bar.change);
						while (ms.size() >= Constants.METRIC_NEEDED_BARS.get(metricName)) {
							ms.remove(0);
						}
						ms.add(newMetric);
	//					System.out.print("." + ms.size());
					}
					else {
	//					System.out.println("Probably just from 4 of the first 5 bars in OKCoin's kline bar WebSocket API");
					}
				}
				else {
					System.err.println("MetricSingleton updateMetricSequenceHash(...)  tried getting " + mk.toString() + " but it didn't have a metricSequenceHash");
				}
			}
		}
	}
	
	public synchronized ArrayList<Metric> getNextMetricSequence() {
		if (metricSequenceKeyList.size() - 1 >= metricSequenceIndex && threadsRunning) {
			MetricKey mk = metricSequenceKeyList.get(metricSequenceIndex);
//			System.out.println("Got " + metricSequenceIndex);
			metricSequenceIndex++;
			return metricSequenceHash.get(mk); // Some threads might keep grabbing sequences after they've all been done because they're already in the loop
		}
		else {
			metricSequenceIndex = 0;
			for (MetricsUpdaterThread mut : muts) {
				mut.setRunning(false);
			}
//			System.out.println("Stopping MUTs");
			return null;
		}
	}

	public void setNeededMetrics(ArrayList<String> neededMetrics) {
		this.neededMetrics = neededMetrics;
	}

	public void setBarKeys(ArrayList<BarKey> barKeys) {
		this.barKeys = barKeys;
	}
	
	public void addBarKey(BarKey bk) {
		this.barKeys.add(bk);
	}

	public ArrayList<String> getNeededMetrics() {
		return neededMetrics;
	}
}
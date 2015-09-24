package metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import data.BarKey;
import data.Metric;
import data.MetricKey;
import dbio.QueryManager;

public class MetricSingleton {

	private static MetricSingleton instance = null;
	
	private static final int NUM_THREADS = 10;
	private MetricsUpdaterThread[] muts = new MetricsUpdaterThread[NUM_THREADS];
	private boolean threadsRunning = false;
	
	// For holding all the BarKeys
	private ArrayList<BarKey> barKeys = null;
	
	// List of metrics I need updated
	ArrayList<String> neededMetrics = null;

	// For holding all the Metric Sequences
	private HashMap<MetricKey, ArrayList<Metric>> metricSequenceHash = new HashMap<MetricKey, ArrayList<Metric>>();
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
				mut.setRunning(false);
				mut.join();
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
				mut.join();
				mut.setRunning(false);
				threadsRunning = false;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean areThreadsRunning() {
		return threadsRunning;
	}
	
	public ArrayList<BarKey> getBarKeys() {
		return barKeys;
	}

	public void init(ArrayList<BarKey> barKeys, ArrayList<String> neededMetrics) {
		this.barKeys = barKeys;
		this.neededMetrics = neededMetrics;
		this.metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys, neededMetrics);
	}
	
	public void refreshMetricSequenceHash() {
		this.metricSequenceHash = QueryManager.loadMetricSequenceHash(barKeys, neededMetrics);
	}

	public HashMap<MetricKey, ArrayList<Metric>> getMetricSequenceHash() {
		return metricSequenceHash;
	}
	
	public synchronized Map.Entry<MetricKey, ArrayList<Metric>> popSingleMetricSequence() {
		for (Iterator<Map.Entry<MetricKey, ArrayList<Metric>>> it = metricSequenceHash.entrySet().iterator(); it.hasNext();) {
			Map.Entry<MetricKey, ArrayList<Metric>> entry = it.next();
			it.remove();
			return entry;
		}
		return null;
	}
	
	public synchronized Map.Entry<MetricKey, ArrayList<Metric>> getNextMetricSequence() {
		return null;
	}
}
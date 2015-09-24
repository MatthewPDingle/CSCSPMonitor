package singletons;

import java.util.ArrayList;

import data.BarKey;
import metrics.MetricCacheSingleton;
import metrics.MetricsUpdaterThread;

public class MetricSingleton {

	private static MetricSingleton instance = null;
	
	private static final int NUM_THREADS = 10;
	private MetricsUpdaterThread[] muts = new MetricsUpdaterThread[NUM_THREADS];

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
	
	public void setRunning(boolean running) {
		try {
			if (running) {
				for (int a = 0; a < muts.length; a++) {
					MetricsUpdaterThread mut = muts[a];

					if (!mut.isRunning()) {
						mut = new MetricsUpdaterThread();
						muts[a] = mut;
						mut.setRunning(true);
						mut.start();
					}
				}
				// Wait for it to finish
				for (MetricsUpdaterThread mut : muts) {
					mut.join();
					mut.setRunning(false);
				}
			}
			else {
				for (MetricsUpdaterThread mut : muts) {
					mut.setRunning(false);
					mut.join();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(ArrayList<BarKey> barKeys, ArrayList<String> metrics) {
		MetricCacheSingleton.getInstance().init(barKeys, metrics);
	}
	
}
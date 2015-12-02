package data.downloaders.interactivebrokers;

import java.util.HashMap;

import data.BarKey;

public class IBSingleton {

	private static IBSingleton instance = null;
	
	private HashMap<BarKey, IBWorker> ibWorkerHash;
	
	protected IBSingleton() {
		ibWorkerHash = new HashMap<BarKey, IBWorker>();
	}
	
	public static IBSingleton getInstance() {
		if (instance == null) {
			instance = new IBSingleton();
		}
		return instance;
	}

	public HashMap<BarKey, IBWorker> getIbWorkerHash() {
		return ibWorkerHash;
	}

	public IBWorker requestWorker(BarKey bk) {
		IBWorker ibWorker = ibWorkerHash.get(bk);
		if (ibWorker == null) {
			ibWorker = new IBWorker(bk);
		}
		ibWorkerHash.put(bk, ibWorker);
		
		return ibWorker;
	}
}
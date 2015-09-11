package singletons;

import threads.TradingThread;

public class TradingSingleton {

	private static TradingSingleton instance = null;
	
	private TradingThread tt = new TradingThread();

	protected TradingSingleton() {
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
}
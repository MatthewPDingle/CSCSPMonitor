package threads;

import singletons.StatusSingleton;

public class TradingThread extends Thread {

	private boolean running = false;
	private StatusSingleton ss = null;

	public TradingThread() {
		ss = StatusSingleton.getInstance();
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public void run() {
		try {
			while (running) {
				ss.addMessageToTradingMessageQueue("Trading");
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
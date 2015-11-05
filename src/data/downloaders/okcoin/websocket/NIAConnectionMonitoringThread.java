package data.downloaders.okcoin.websocket;

import java.util.Calendar;

public class NIAConnectionMonitoringThread extends Thread {

	private final int TIMEOUT_SEC = 100;
	
	NIAStatusSingleton niass = null;
	
	public NIAConnectionMonitoringThread() {
		niass = NIAStatusSingleton.getInstance();
	}
	
	@Override
	public void run() {
		try {
			while (niass.isKeepAlive()) {
				
				long lastActivity = niass.getLastActivityTime().getTimeInMillis();
				long now = Calendar.getInstance().getTimeInMillis();
				
				if (now - lastActivity > TIMEOUT_SEC * 1000) {
					System.err.println("NIAConnectionMonitoringThread has detected inactivity from OKCoin's WebSocket API.  Will disconnect and attempt a reconnect...");
					niass.noteActivity();
					
					// Reconnect
					niass.reinitClient();
				}
				
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
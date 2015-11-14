package data.downloaders.okcoin.websocket;

import java.util.Calendar;

import dbio.QueryManager;

public class NIAConnectionMonitoringThread extends Thread {

	private final int TIMEOUT_SEC = 90;
	
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
				
				if (!niass.isStartup()) {
					if (now - lastActivity > TIMEOUT_SEC * 1000 || !niass.isNiaClientHandlerConnected()) { 
						System.err.println("NIAConnectionMonitoringThread has detected inactivity or failulre from OKCoin's WebSocket API.  Will disconnect and attempt a reconnect...");
						niass.noteActivity();
						niass.recordDisconnect();
						System.err.println("NIAConnectionMonitoringThread has recorded " + niass.getDisconnectCount() + " disconnects.  This one was at least " + (now - lastActivity) + "ms");
						
						int numRowsDeleted = QueryManager.deleteAllRequestedOrders();
						System.err.println("Unfortunately we had to delete " + numRowsDeleted + " trades with requests in the DB.");
						
						// Reconnect
						niass.reinitClient();
					}
				}
				Thread.sleep(1000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
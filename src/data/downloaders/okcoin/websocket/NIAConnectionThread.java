package data.downloaders.okcoin.websocket;

import java.util.HashMap;
import java.util.Map.Entry;

public class NIAConnectionThread extends Thread {

	private boolean running = false;
	private NIAListener listener = null;
	private NIAClient client = null;
	private HashMap<String, Boolean> channels = new HashMap<String, Boolean>();
	
	public NIAConnectionThread() {
		listener = new NIAListener();
		client = new NIAClient();
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public synchronized void addChannel(String channel) {
		channels.put(channel, false);
	}
	
	public synchronized void removeChannel(String channel) {
		client.removeChannel(channel);
		channels.remove(channel);
	}
	
	public synchronized void removeAllChannels() {
		client.removeAllChannels();
	}
	
	public void spotTrade(String symbol, String price, String amount, String type) {
		if (listener == null || client == null) {
			System.err.println(("NIAConnectionThread client and/or service has a problem.  Cannot execute spotTrade(...)"));
//			NIAStatusSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.spotTrade(symbol, price, amount, type);
		}
	}
	
	public void cancelOrder(String symbol, Long orderId) {
		if (listener == null || client == null) {
			System.err.println(("NIAConnectionThread client and/or service has a problem.  Cannot execute cancelOrder(...)"));
//			NIAStatusSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.cancelOrder(symbol, orderId);
		}
	}
	
	public void getOrderInfo(String okCoinSymbol, long orderID) {
		if (listener == null || client == null) {
			System.err.println(("NIAConnectionThread client and/or service has a problem.  Cannot execute getOrderInfo(...)"));
//			NIAStatusSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.getOrderInfo(okCoinSymbol, orderID);
		}
	}
	
	public void getUserInfo() {
		if (listener == null || client == null) {
			System.err.println(("NIAConnectionThread client and/or service has a problem.  Cannot execute getUserInfo(...)"));
//			NIAStatusSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.getUserInfo();
		}
	}
	
	public void getRealTrades() {
		if (listener == null || client == null) {
			System.err.println(("NIAConnectionThread client and/or service has a problem.  Cannot execute getRealTrades(...)"));
//			NIAStatusSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.getRealTrades();
		}
	}

	@Override
	public void run () {
//		try {
//			if (running) {
//				System.out.println("NIAConnectionThread starting");
//				client.connect();
//				
//				// Wait until we get connected
//				while (!NIAStatusSingleton.getInstance().isNiaClientHandlerConnected()) {
//					Thread.sleep(100);
//				}
//				NIAStatusSingleton.getInstance().setDisconnected(false);
//			}
//			while (running) {
//				try {
//					Thread.sleep(5000);
//					
//					if (listener == null || client == null) {
//						System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//						NIAStatusSingleton.getInstance().setDisconnected(true);
//					}
//
//					if (NIAStatusSingleton.getInstance().isDisconnected()) {
//						System.out.println("Reconnecting");
//						if (client != null) {
//							client.removeAllChannels();
//						}
//						listener = null;
//						client = null;
//						listener = new NIAListener();
//						client = new NIAClient();
//						client.connect();
//						// Wait until we get connected
//						while (!NIAStatusSingleton.getInstance().isNiaClientHandlerConnected()) {
//							Thread.sleep(100);
//						}
//						NIAStatusSingleton.getInstance().setDisconnected(false);
//						for (Entry<String, Boolean> entry : channels.entrySet()) {
//							client.addChannel(entry.getKey());
//							entry.setValue(true);
//						}
//						
//					}
//					for (Entry<String, Boolean> entry : channels.entrySet()) {
//						if (!entry.getValue()) {
//							System.out.println("Adding " + entry.getKey() + "to client");
//							client.addChannel(entry.getKey());
//							entry.setValue(true);
//						}
//					}
//					client.sendPing();
//					System.out.println("{'event':'ping'}");
//				}
//				catch (Exception e) {
//					System.err.println("NIAConnectionThread error");
//					e.printStackTrace();
//				}
//			}
//			
//			// If we're about to exit the thread, explicitly remove all channels
//			if (client != null) {
//				client.removeAllChannels();
//			}
//			channels.clear();
//			System.out.println("NIAConnectionThread IS EXITING.  DAME DAME DAME!");
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			System.err.println("CATASTROPHIC FAILURE IN NIAConnectionThread");
//		}
	}	
}
package data.downloaders.okcoin.websocket;

import java.util.HashMap;
import java.util.Map.Entry;

import data.downloaders.okcoin.OKCoinConstants;

public class OKCoinWebSocketThread extends Thread {

	private boolean running = false;
	private OKCoinWebSocketService service = null;
	private OKCoinWebSocketClient client = null;
	private HashMap<String, Boolean> channels = new HashMap<String, Boolean>();
	
	public OKCoinWebSocketThread() {
		service = new OKCoinWebSocketListener();
		client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
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
		if (service == null || client == null || client.isNettyChannelNull() || !client.isNettyChannelOpen() || !client.isNettyChannelActive()) {
			System.err.println(("OKCoinWebSocketThread's client and/or service has a problem.  Cannot execute spotTrade(...)"));
			OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.spotTrade(symbol, price, amount, type);
		}
	}
	
	public void cancelOrder(String symbol, Long orderId) {
		if (service == null || client == null || client.isNettyChannelNull() || !client.isNettyChannelOpen() || !client.isNettyChannelActive()) {
			System.err.println(("OKCoinWebSocketThread's client and/or service has a problem.  Cannot execute cancelOrder(...)"));
			OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.cancelOrder(symbol, orderId);
		}
	}
	
	public void getOrderInfo(String okCoinSymbol, long orderID) {
		if (service == null || client == null || client.isNettyChannelNull() || !client.isNettyChannelOpen() || !client.isNettyChannelActive()) {
			System.err.println(("OKCoinWebSocketThread's client and/or service has a problem.  Cannot execute getOrderInfo(...)"));
			OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.getOrderInfo(okCoinSymbol, orderID);
		}
	}
	
	public void getUserInfo() {
		if (service == null || client == null || client.isNettyChannelNull() || !client.isNettyChannelOpen() || !client.isNettyChannelActive()) {
			System.err.println(("OKCoinWebSocketThread's client and/or service has a problem.  Cannot execute getUserInfo(...)"));
			OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.getUserInfo();
		}
	}
	
	public void getRealTrades() {
		if (service == null || client == null || client.isNettyChannelNull() || !client.isNettyChannelOpen() || !client.isNettyChannelActive()) {
			System.err.println(("OKCoinWebSocketThread's client and/or service has a problem.  Cannot execute getRealTrades(...)"));
			OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
		}
		else {
			client.realTrades();
		}
	}

	@Override
	public void run () {
		try {
			if (running) {
				System.out.println("OKCoinWebSocketThread starting");
				boolean success = client.start();
				if (success) {
					System.out.println("OKCoinWebSocketThread initial connect successful");
					OKCoinWebSocketSingleton.getInstance().setDisconnected(false);
				}
				else {
					System.out.println("OKCoinWebSocketThread initial connect failed");
					OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
				}
			}
			while (running) {
				try {
					Thread.sleep(5000);
					
					if (service == null || client == null || client.isNettyChannelNull() || !client.isNettyChannelOpen() || !client.isNettyChannelActive()) {
						System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
						OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
					}
					System.out.print("0");
					if (OKCoinWebSocketSingleton.getInstance().isDisconnected()) {
						System.out.println("Reconnecting");
						if (client != null) {
							System.out.print("1");
							client.removeAllChannels();
							System.out.print("2");
						}
						service = new OKCoinWebSocketListener();
						System.out.print("3");
						client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
						System.out.print("4");
						boolean success = client.start();
						System.out.print("5");
						if (success) {
							System.out.print("6");
							OKCoinWebSocketSingleton.getInstance().setDisconnected(false);
							System.out.print("7");
							for (Entry<String, Boolean> entry : channels.entrySet()) {
								System.out.print("8");
								client.addChannel(entry.getKey());
								entry.setValue(true);
							}
						}
						else {
							System.out.print("9");
							OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
							System.out.print("0");
						}
					}
					System.out.print("A");
					for (Entry<String, Boolean> entry : channels.entrySet()) {
						if (!entry.getValue()) {
							System.out.print("B");
							client.addChannel(entry.getKey());
							entry.setValue(true);
							System.out.print("C");
						}
					}
					System.out.print("D");
					System.out.println("sendPing");
					client.sendPing();
					System.out.print("E");
				}
				catch (Exception e) {
					System.err.println("OKCoinWebSocketThread error");
					e.printStackTrace();
				}
			}
			
			// If we're about to exit the thread, explicitly remove all channels
			if (client != null) {
				client.removeAllChannels();
			}
			channels.clear();
			System.out.println("OKCoinWebSocketThread IS EXITING.  DAME DAME DAME!");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("CATASTROPHIC FAILURE IN OKCoinWebSocketThread");
		}
	}	
}
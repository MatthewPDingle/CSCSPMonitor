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

	@Override
	public void run () {
		if (running) {
			System.out.println("OKCoinWebSocketThread starting");
			boolean success = client.start();
			if (success) {
				OKCoinWebSocketSingleton.getInstance().setDisconnected(false);
			}
		}
		while (running) {
			try {
				Thread.sleep(5000);
				
				if (service == null || client == null || client.isNettyChannelNull()) {
					System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
					OKCoinWebSocketSingleton.getInstance().setDisconnected(true);
				}
				
				if (OKCoinWebSocketSingleton.getInstance().isDisconnected()) {
					System.out.println("Reconnecting");
					if (client != null) {
						client.removeAllChannels();
					}
					service = new OKCoinWebSocketListener();
					client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
					boolean success = client.start();
					if (success) {
						OKCoinWebSocketSingleton.getInstance().setDisconnected(false);
					}
					for (Entry<String, Boolean> entry : channels.entrySet()) {
						client.addChannel(entry.getKey());
						entry.setValue(true);
					}
				}
				
				for (Entry<String, Boolean> entry : channels.entrySet()) {
					if (!entry.getValue()) {
						client.addChannel(entry.getKey());
						entry.setValue(true);
					}
				}
			
				System.out.println("sentPing");
				client.sentPing();
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
	}	
}
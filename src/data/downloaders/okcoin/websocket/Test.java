package data.downloaders.okcoin.websocket;

import data.downloaders.okcoin.OKCoinConstants;

public class Test {

	public static void main(String[] args) {

		OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
		okss.setRunning(true);
//		okss.addChannel(OKCoinConstants.WEBSOCKET_TICKER_BTCCNY);
		okss.addChannel(OKCoinConstants.WEBSOCKET_BAR_BTCCNY + OKCoinConstants.BAR_DURATION_1M);
		
//
//		OKCoinWebSocketService service = new OKCoinBusinessWebSocketServiceImpl();
//		OKCoinWebSocketClient client = new OKCoinWebSocketClient(OKCoinConstants.WEBSOCKET_URL_CHINA, service);
//	
//		client.start();
//		client.addChannel("ok_btccny_ticker");

	}
}

package data.downloaders.okcoin.websocket;

import constants.Constants.BAR_SIZE;
import data.downloaders.okcoin.OKCoinConstants;

public class NIATest {

	public static void main(String[] args) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			NIAClient niaClient = new NIAClient();
			niaClient.connect();
			
			// Wait until we get connected
			while (!niass.isNiaClientHandlerConnected()) {
				Thread.sleep(100);
			}
			
			String websocketPrefix = OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get("okcoinBTCCNY");
			String okCoinBarDuration = OKCoinConstants.OKCOIN_BAR_SIZE_TO_BAR_DURATION_HASH.get(BAR_SIZE.BAR_1M);
			
			niaClient.addChannel(websocketPrefix + "kline_" + okCoinBarDuration); // Bars

			niaClient.addChannel(OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_SYMBOL_HASH.get("okcoinBTCCNY")); // Ticks
			niaClient.addChannel(OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get("okcoinBTCCNY") + "depth"); // Order Book
			

			while (true) {
				
				String dataMsg = "{'event':'ping'}";
				System.out.println(dataMsg);
				niaClient.sendMessage(dataMsg);
				
				Thread.sleep(5000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package data.downloaders.okcoin.websocket;

import constants.Constants.BAR_SIZE;
import data.downloaders.okcoin.OKCoinConstants;

public class NIATest {

	public static void main(String[] args) {
		try {
			NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
			

			niass.getNiaClient().connect();
			
			// Wait until we get connected
			while (!niass.isNiaClientHandlerConnected()) {
				Thread.sleep(100);
			}
			
			String websocketPrefix = OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get("okcoinBTCCNY");
			String okCoinBarDuration = OKCoinConstants.OKCOIN_BAR_SIZE_TO_BAR_DURATION_HASH.get(BAR_SIZE.BAR_1M);
			
			niass.getNiaClient().addChannel(websocketPrefix + "kline_" + okCoinBarDuration); // Bars

			niass.getNiaClient().addChannel(OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_SYMBOL_HASH.get("okcoinBTCCNY")); // Ticks
			niass.getNiaClient().addChannel(OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get("okcoinBTCCNY") + "depth"); // Order Book
			
			int a = 0;
			while (a < 3600) {
				
				niass.getUserInfo();
				niass.getRealTrades();
				niass.spotTrade("btc_cny", 2700, 1.015, "buy");
				
				Thread.sleep(1000);
				a++;
			}
			niass.getNiaClient().disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
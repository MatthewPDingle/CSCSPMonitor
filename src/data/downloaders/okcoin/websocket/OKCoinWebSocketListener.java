package data.downloaders.okcoin.websocket;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.downloaders.okcoin.OKCoinConstants;
import dbio.QueryManager;
import utils.CalendarUtils;

public class OKCoinWebSocketListener implements OKCoinWebSocketService {

	@Override
	public void onReceive(String msg) {
		try {
			System.out.println(msg);
			
			Gson gson = new Gson();
			Object messageObject = gson.fromJson(msg, Object.class);
			if (messageObject instanceof ArrayList<?>) {
				ArrayList<LinkedTreeMap<String, Object>> messageList = gson.fromJson(msg, ArrayList.class);

				OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
				
				for (LinkedTreeMap<String, Object> message : messageList) {
					String channel = message.get("channel").toString();
					
					// Ticker WebSocket
					if (channel.contains("ticker")) {
						String symbol = OKCoinConstants.WEBSOCKET_SYMBOL_TO_TICK_SYMBOL_HASH.get(channel);
						LinkedTreeMap<String, String> data = (LinkedTreeMap<String, String>)message.get("data");
						HashMap tickerDataHash = new HashMap<String, String>();
						
						tickerDataHash.putAll(data);
						okss.putSymbolTickerDataHash(symbol, tickerDataHash);
					}
					
					// Bar WebSocket
					if (channel.contains("kline")) {
						ArrayList<Object> data = (ArrayList<Object>)message.get("data");
						
						String channelMinusDuration = channel.substring(0, channel.lastIndexOf("_"));
						String prefix = channelMinusDuration.substring(0, channelMinusDuration.lastIndexOf("_") + 1);
						String symbol = OKCoinConstants.WEBSOCKET_PREFIX_TO_TICK_SYMBOL_HASH.get(prefix);

						String channelDuration = channel.substring(channel.lastIndexOf("_") + 1);
						BAR_SIZE duration = OKCoinConstants.OKCOIN_BAR_DURATION_TO_BAR_SIZE_HASH.get(channelDuration);
						
						ArrayList<Bar> bars = new ArrayList<Bar>();
						
						// When the data is the bar itself, not a list of bars
						if (data.get(0) != null && !(data.get(0) instanceof ArrayList<?>)) {
							ArrayList<Object> tempBar = new ArrayList<Object>();
							tempBar.addAll(data);
							data.clear();
							data.add(tempBar);
						}
						
						// These go oldest to newest
						for (int a = 0; a < data.size(); a++) {
							if (data.get(a) instanceof ArrayList<?>) {
								ArrayList<Object> barJSON = (ArrayList<Object>)data.get(a);
								
								double timeMS = (double)barJSON.get(0);
								double open = (double)barJSON.get(1);
								double high = (double)barJSON.get(2);
								double low = (double)barJSON.get(3);
								double close = (double)barJSON.get(4);
								double volume = (double)barJSON.get(5);
								double vwap = (open + high + low + close) / 4d;
								
								Calendar c = Calendar.getInstance();
								c.setTimeInMillis((long)timeMS);
								Calendar barStart = CalendarUtils.getBarStart(c, duration);
								Calendar barEnd = CalendarUtils.getBarEnd(c, duration);
								
								Bar mostRecentBarInDB = QueryManager.getMostRecentBar(new BarKey(symbol, duration), barStart);
								Float change = null;
								Float gap = null;
								if (mostRecentBarInDB != null) {
									change = (float)close - mostRecentBarInDB.close;
									gap = (float)open - mostRecentBarInDB.close;
								}
								
								boolean partial = false;
								if (data.size() == 1) {
									partial = true;
								}
								if (a == data.size() - 1 && data.size() > 1) {
									partial = true;
								}
	
								Bar bar = new Bar(symbol, (float)open, (float)close, (float)high, (float)low, (float)vwap, (float)volume, null, change, gap, barStart, barEnd, duration, partial);
								//QueryManager.insertOrUpdateIntoBar(bar);
								bars.add(bar);
							}
						}

						okss.addLatestBars(bars);
					}
				}
			}
			else {
				// {'event':'pong'} probably
				
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
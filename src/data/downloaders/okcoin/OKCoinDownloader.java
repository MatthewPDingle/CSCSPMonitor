package data.downloaders.okcoin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.Converter;
import data.Tick;
import dbio.QueryManager;
import metrics.MetricSingleton;
import utils.CalendarUtils;
import utils.StringUtils;


public class OKCoinDownloader {

	/**
	 * Parameters have to come in sets of 3.
	 * First is okcoin symbol like btc_cny
	 * Second is bar duration
	 * Third is number of bars requested
	 * @param args
	 */
	public static void main(String[] args) {
		// Get params
		ArrayList<String[]> params = new ArrayList<String[]>();
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		if (args != null) {
			for (int a = 0; a < args.length; a += 3) {
				String symbol = args[a];
				String duration = args[a + 1];
				String numBars = args[a + 2];
				String[] param = new String[3];
				param[0] = symbol;
				param[1] = duration;
				param[2] = numBars;
				params.add(param);
				BarKey barKey = new BarKey(OKCoinConstants.OKCOIN_SYMBOL_TO_TICK_SYMBOL_HASH.get(symbol), BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
		}
		
		// Experimental
		ArrayList<String> metricNames = new ArrayList<String>();
		metricNames.addAll(Constants.METRICS);
		
		BarKey bk = new BarKey("okcoinBTCCNY", BAR_SIZE.BAR_15M);
		
		// Loop.  First pass get 1000 bars.  All other passes, get the number specified by parameters.
		if (params.size() > 0) {
			MetricSingleton metricSingleton = MetricSingleton.getInstance();
			metricSingleton.init(barKeys, metricNames);
			
			boolean firstPass = true;
			int numBars = 1000;
			while (true) {	
				try {
					System.out.println(Calendar.getInstance().getTime().toString());
					for (String[] param : params) {
						if (!firstPass) {
							numBars = Integer.parseInt(param[2]);
						}
						downloadBarsAndUpdate(param[0], BAR_SIZE.valueOf(param[1]), numBars);
						System.out.println(param[0] + " " + param[1] + " done.");
					}
					firstPass = false;
					System.out.println(Calendar.getInstance().getTime().toString() + " - Bar Downloads & Inserts Done");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static ArrayList<Bar> downloadLatestBar(String okcoinSymbol, BAR_SIZE barSize) {
		ArrayList<Bar> bars = getMostRecentBarsFromBarHistory(okcoinSymbol, barSize, 1, null);
		if (bars != null) {
			return bars;
		}
		else {
			return new ArrayList<Bar>();
		}
	}
	
	public static int downloadBarsAndUpdate(String okcoinSymbol, BAR_SIZE barSize, Integer numBars) {
		int n = 0;
		if (numBars != null) {
			n = numBars;
		}
		ArrayList<Bar> bars = getMostRecentBarsFromBarHistory(okcoinSymbol, barSize, n, null); // Use n = 2000 if you want a lot
		for (Bar bar : bars) {
			QueryManager.insertOrUpdateIntoBar(bar);
		}
		if (bars != null && bars.size() > 0) {
			return bars.size();
		}
		return 0;
	}
	
	public static void downloadTicksAndUpdate(String okcoinSymbol, BAR_SIZE barSize) {
		ArrayList<Bar> bars2 = getMostRecentBarsFromTickHistory(okcoinSymbol, barSize, "5000");
		for (Bar bar : bars2) {
			QueryManager.insertOrUpdateIntoBar(bar);
		}
	}
	
	/**
	 * Returns the most recent bars in order of oldest to newest
	 * The newest bar may be "partial" if the full bar duration has not passed yet.
	 * 
	 * @param okCoinSymbol
	 * @param barSize
	 * @param sinceID
	 * @return
	 */
	public static ArrayList<Bar> getMostRecentBarsFromTickHistory(String okCoinSymbol, Constants.BAR_SIZE barSize, String sinceID) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			String tickSymbol = "okcoin";
			if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCUSD)) {
				tickSymbol = "okcoinBTCUSD";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCCNY)) {
				tickSymbol = "okcoinBTCCNY";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCUSD)) {
				tickSymbol = "okcoinLTCUSD";
			}
			else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCCNY)) {
				tickSymbol = "okcoinLTCCNY";
			}
			
			String json = getTickHistoryJSON(okCoinSymbol, sinceID);
			List<Map> list = new Gson().fromJson(json, List.class);
			
			// From oldest to newest
			ArrayList<Tick> ticks = new ArrayList<Tick>();
			for (Map map : list) {
				float volume = Float.parseFloat(map.get("amount").toString());
				String timeMS = map.get("date").toString();
				timeMS = timeMS.replace(".", "");
				if (timeMS.contains("E")) {
					timeMS = timeMS.substring(0, timeMS.indexOf("E"));
				}
				while (timeMS.length() < 10) {
					timeMS = timeMS + "0";
				}
				long ms = Long.parseLong(timeMS) * 1000;
				Calendar timestamp = Calendar.getInstance();
				timestamp.setTimeInMillis(ms);
				float price = Float.parseFloat(map.get("price").toString());
				Tick tick = new Tick(tickSymbol, price, volume, timestamp);
				ticks.add(tick);
			}
			
			if (ticks != null) {
				Tick firstTick = ticks.get(0);
				Calendar barStart = CalendarUtils.getBarStart(firstTick.timestamp, barSize);
				Calendar barEnd = CalendarUtils.getBarEnd(firstTick.timestamp, barSize);
				
				ArrayList<Tick> barTicks = new ArrayList<Tick>();
				float previousClose = 0f;
				if (ticks.size() > 0) {
					previousClose = ticks.get(0).price;
				}
				boolean firstBar = true;
				// Oldest to newest
				for (Tick tick : ticks) {
					if ((tick.timestamp.after(barStart) || CalendarUtils.areSame(tick.timestamp, barStart) == true) && tick.timestamp.before(barEnd)) {
						// We're still in the same bar so add this tick to the collection.
						barTicks.add(tick);
					}
					else {
						// Turn the collection of bar ticks into a proper bar.  Don't do the first bar because it will probably be partial and will also have no previousClose info.
						if (!firstBar) {
							Bar bar = Converter.ticksToBar(barTicks, barStart, barEnd, barSize, previousClose, firstBar);
							bars.add(bar);
							if (barTicks.size() > 0) {
								previousClose = barTicks.get(barTicks.size() - 1).price;
							}
						}
						firstBar = false;
						
						// It's a new bar so reset the bar start & end times.
						barStart = CalendarUtils.getBarStart(tick.timestamp, barSize);
						barEnd = CalendarUtils.getBarEnd(tick.timestamp, barSize);
						
						// Clear the collection of bar ticks so it can be used again for the next bar.
						barTicks.clear();
						barTicks.add(tick);
					}
				}
				// Create a final bar.  This one is partial
				Bar bar = Converter.ticksToBar(barTicks, barStart, barEnd, barSize, previousClose, true);
				bars.add(bar);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return bars;
	}
	
	/**
	 * Note: 2M, 10M, 8H bars are not supported on OKCoin's API
	 * It appears that the max barCount = 1682
	 * 
	 * @param barSize
	 * @return
	 */
	public static ArrayList<Bar> getMostRecentBarsFromBarHistory(String okCoinSymbol, Constants.BAR_SIZE barSize, int barCount, Calendar since) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			for (int attempt = 0; attempt < 3; attempt++) {
				String barSymbol = "okcoin";
				if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCUSD)) {
					barSymbol = "okcoinBTCUSD";
				}
				else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_BTCCNY)) {
					barSymbol = "okcoinBTCCNY";
				}
				else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCUSD)) {
					barSymbol = "okcoinLTCUSD";
				}
				else if (okCoinSymbol.equals(OKCoinConstants.SYMBOL_LTCCNY)) {
					barSymbol = "okcoinLTCCNY";
				}
				
				String okBarDuration = OKCoinConstants.BAR_DURATION_15M; 
				int barMinutes = 0;
				switch (barSize) {
					case BAR_1M:
						okBarDuration = OKCoinConstants.BAR_DURATION_1M;
						barMinutes = 1;
						break;
					case BAR_3M:
						okBarDuration = OKCoinConstants.BAR_DURATION_3M;
						barMinutes = 3;
						break;
					case BAR_5M:
						okBarDuration = OKCoinConstants.BAR_DURATION_5M;
						barMinutes = 5;
						break;
					case BAR_15M:
						okBarDuration = OKCoinConstants.BAR_DURATION_15M;
						barMinutes = 15;
						break;
					case BAR_30M:
						okBarDuration = OKCoinConstants.BAR_DURATION_30M;
						barMinutes = 30;
						break;
					case BAR_1H:
						okBarDuration = OKCoinConstants.BAR_DURATION_1H;
						barMinutes = 60;
						break;
					case BAR_2H:
						okBarDuration = OKCoinConstants.BAR_DURATION_2H;
						barMinutes = 120;
						break;
					case BAR_4H:
						okBarDuration = OKCoinConstants.BAR_DURATION_4H;
						barMinutes = 240;
						break;
					case BAR_6H:
						okBarDuration = OKCoinConstants.BAR_DURATION_6H;
						barMinutes = 360;
						break;
					case BAR_12H:
						okBarDuration = OKCoinConstants.BAR_DURATION_12H;
						barMinutes = 720;
						break;
					case BAR_1D:
						okBarDuration = OKCoinConstants.BAR_DURATION_1D;
						barMinutes = 1440;
						break;
					default:
						break;
				}
				String json = getBarHistoryJSON(okCoinSymbol, okBarDuration, new Integer(barCount + 1).toString(), since);
				if (json != null && json.length() > 0) {
					Object jsonObject = null;
					try {
						jsonObject = new Gson().fromJson(json, Object.class);
					}
					catch (Exception e) {
						System.err.println(e.getMessage());
					}
					List<List> list = null;
					if (jsonObject != null && jsonObject instanceof List<?>) {
						list = (List<List>)jsonObject;
					}
					else {
						System.err.println("OKCoin REST API returned garbage for " + okCoinSymbol + " - " + barSize.toString() + " on attempt " + (attempt + 1) +".  Waiting a second and trying again");
						Thread.sleep(1000);
						continue;
					}
	
					Double previousClose = null;
				
					// From oldest to newest
					if (list != null) {
						for (List jsonBar : list) {
							String timeMS = jsonBar.get(0).toString();
							timeMS = timeMS.replace(".", "");
							if (timeMS.contains("E")) {
								timeMS = timeMS.substring(0, timeMS.indexOf("E"));
							}
							while (timeMS.length() < 10) {
								timeMS = timeMS + "0";
							}
							long ms = Long.parseLong(timeMS) * 1000;
							Calendar periodStart = Calendar.getInstance();
							periodStart.setTimeInMillis(ms);
							Calendar periodEnd = Calendar.getInstance();
							periodEnd.setTime(periodStart.getTime());
							periodEnd.add(Calendar.MINUTE, barMinutes);
							double open = Float.parseFloat(jsonBar.get(1).toString());
							double high = Float.parseFloat(jsonBar.get(2).toString());
							double low = Float.parseFloat(jsonBar.get(3).toString());
							double close = Float.parseFloat(jsonBar.get(4).toString());
							double volume = Float.parseFloat(jsonBar.get(5).toString());
							double vwapEstimate = (open + close + high + low) / 4d;
							Double change = null;
							Double gap = null;
							if (previousClose != null) {
								change = close - previousClose; 
								gap = open - previousClose;
							}
						
							Bar bar = new Bar(barSymbol, open, close, high, low, vwapEstimate, volume, null, change, gap, periodStart, periodEnd, barSize, false);
							bars.add(bar);
							
							previousClose = close;
						}
					}
				}
				
				// Set the most recent one to partial and toss the oldest one (we got one more bar than we needed)
				if (bars.size() > 0) {
					bars.get(bars.size() - 1).partial = true;
					bars.remove(0);
				}
				break; // Made it here ok - no need to try again
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return bars;
	}
	
	/**
	 * Gets JSON for individual ticks.  Supposedly it returns the 600 most recent ticks
	 * starting from the "since" parameter.  Since is an OKCoin transaction id (tid).
	 * "Since" does not seem to work, but you have to supply a number like 5000 if you want to get 600 results.
	 * If you don't provide a "Since" parameter, you get a lot fewer results.
	 * 
	 * @param symbol
	 * @param since DOES NOT SEEM TO WORK
	 * @return
	 */
	private static String getTickHistoryJSON(String symbol, String since) {
		String result = "";
		try {
			OKCoinRestAPI okCoin = OKCoinRestAPI.getInstance();
			String param = "";
			if (!StringUtils.isEmpty(symbol)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "symbol=" + symbol;
			}
			if (!StringUtils.isEmpty(since)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "since=" + since;
			}
			String url = OKCoinConstants.URL_USA;
			if (symbol != null && symbol.endsWith("cny")) {
				url = OKCoinConstants.URL_CHINA;
			}
			result = okCoin.requestHttpGet(url, "/trades.do", param);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Gets JSON for numBars following the since Calendar
	 * If no since is provided, it gets the most recent numBars
	 * 
	 * @param symbol
	 * @param type
	 * @param numBars
	 * @param since DOES NOT SEEM TO WORK
	 * @return
	 */
	private static String getBarHistoryJSON(String symbol, String type, String numBars, Calendar since) {
		String result = "";
		try {
			OKCoinRestAPI okCoin = OKCoinRestAPI.getInstance();
			String param = "";
			if (!StringUtils.isEmpty(symbol)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "symbol=" + symbol;
			}
			if (!StringUtils.isEmpty(type)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "type=" + type;
			}
			if (!StringUtils.isEmpty(numBars)) {
				if (!param.equals("")) {
					param += "&";
				}
				param += "size=" + numBars;
			}
			if (since != null) {
				if (!param.equals("")) {
					param += "&";
				}
				long sinceMS = since.getTimeInMillis();
				param += "since=" + sinceMS;
			}
			String url = OKCoinConstants.URL_USA;
			if (symbol != null && symbol.endsWith("cny")) {
				url = OKCoinConstants.URL_CHINA;
			}
			result = okCoin.requestHttpGet(url, "/kline.do", param);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}

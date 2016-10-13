package data.downloaders.poloniex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.downloaders.okcoin.OKCoinConstants;
import data.downloaders.okcoin.OKCoinRestAPI;
import dbio.QueryManager;
import metrics.MetricSingleton;
import utils.StringUtils;

public class PoloniexDownloader {
	
	/**
	 * Parameters have to come in sets of 3.
	 * First is poloniex symbol like BTC_LTC
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
				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
		}
		
		// Experimental
		ArrayList<String> metricNames = new ArrayList<String>();
		metricNames.addAll(Constants.METRICS);
				
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
	
	public static int downloadBarsAndUpdate(String okcoinSymbol, BAR_SIZE barSize, Integer numBars) {
		int n = 0;
//		if (numBars != null) {
//			n = numBars;
//		}
//		ArrayList<Bar> bars = getMostRecentBarsFromBarHistory(okcoinSymbol, barSize, n, null); // Use n = 2000 if you want a lot
//		for (Bar bar : bars) {
//			QueryManager.insertOrUpdateIntoBar(bar);
//		}
//		if (bars != null && bars.size() > 0) {
//			return bars.size();
//		}
		return 0;
	}
	
	/**
	 * Note: 2M, 10M, 8H bars are not supported on OKCoin's API
	 * It appears that the max barCount = 1682
	 * 
	 * @param barSize
	 * @return
	 */
	public static ArrayList<Bar> getMostRecentBarsFromBarHistory(String pxSymbol, Constants.BAR_SIZE barSize, int barCount, Calendar startC, Calendar endC) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			for (int attempt = 0; attempt < 3; attempt++) {
				String barSymbol = "px" + pxSymbol;
				
				String pxBarDuration = PoloniexConstants.BAR_DURATION_5M; 
				int barMinutes = 0;
				switch (barSize) {
					case BAR_5M:
						pxBarDuration = PoloniexConstants.BAR_DURATION_5M;
						barMinutes = 5;
						break;
					case BAR_15M:
						pxBarDuration = PoloniexConstants.BAR_DURATION_15M;
						barMinutes = 15;
						break;
					case BAR_30M:
						pxBarDuration = PoloniexConstants.BAR_DURATION_30M;
						barMinutes = 30;
						break;
					case BAR_2H:
						pxBarDuration = PoloniexConstants.BAR_DURATION_2H;
						barMinutes = 120;
						break;
					case BAR_4H:
						pxBarDuration = PoloniexConstants.BAR_DURATION_4H;
						barMinutes = 240;
						break;
					case BAR_1D:
						pxBarDuration = PoloniexConstants.BAR_DURATION_1D;
						barMinutes = 1440;
						break;
					default:
						break;
				}
				int barSeconds = barMinutes * 60;
				
				ArrayList<LinkedTreeMap<String, Object>> chartData = returnChartData("BTC_CURE", startC, endC, barSeconds);
				for (LinkedTreeMap<String, Object> chartBar : chartData) {
					double start = (double)chartBar.get("date");
					double open = (double)chartBar.get("open");
					double close = (double)chartBar.get("close");
					double high = (double)chartBar.get("high");
					double low = (double)chartBar.get("low");
					double weightedAverage = (double)chartBar.get("weightedAverage");
					double volume = (double)chartBar.get("volume");
					double quoteVolume = (double)chartBar.get("quoteVolume");
					
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis((long)start * 1000);
//					System.out.println(sdf.format(c.getTime()) + " - " + close);
				}
				
				
//				String json = getBarHistoryJSON(pxSymbol, pxBarDuration, new Integer(barCount + 1).toString(), since);
//				if (json != null && json.length() > 0) {
//					Object jsonObject = null;
//					try {
//						jsonObject = new Gson().fromJson(json, Object.class);
//					}
//					catch (Exception e) {
//						System.err.println(e.getMessage());
//					}
//					List<List> list = null;
//					if (jsonObject != null && jsonObject instanceof List<?>) {
//						list = (List<List>)jsonObject;
//					}
//					else {
//						System.err.println("Poloniex REST API returned garbage for " + pxSymbol + " - " + barSize.toString() + " on attempt " + (attempt + 1) +".  Waiting a second and trying again");
//						Thread.sleep(1000);
//						continue;
//					}
//	
//					Float previousClose = null;
//				
//					// From oldest to newest
//					if (list != null) {
//						for (List jsonBar : list) {
//							String timeMS = jsonBar.get(0).toString();
//							timeMS = timeMS.replace(".", "");
//							if (timeMS.contains("E")) {
//								timeMS = timeMS.substring(0, timeMS.indexOf("E"));
//							}
//							while (timeMS.length() < 10) {
//								timeMS = timeMS + "0";
//							}
//							long ms = Long.parseLong(timeMS) * 1000;
//							Calendar periodStart = Calendar.getInstance();
//							periodStart.setTimeInMillis(ms);
//							Calendar periodEnd = Calendar.getInstance();
//							periodEnd.setTime(periodStart.getTime());
//							periodEnd.add(Calendar.MINUTE, barMinutes);
//							float open = Float.parseFloat(jsonBar.get(1).toString());
//							float high = Float.parseFloat(jsonBar.get(2).toString());
//							float low = Float.parseFloat(jsonBar.get(3).toString());
//							float close = Float.parseFloat(jsonBar.get(4).toString());
//							float volume = Float.parseFloat(jsonBar.get(5).toString());
//							float vwapEstimate = (open + close + high + low) / 4f;
//							Float change = null;
//							Float gap = null;
//							if (previousClose != null) {
//								change = close - previousClose; 
//								gap = open - previousClose;
//							}
//						
//							Bar bar = new Bar(barSymbol, open, close, high, low, vwapEstimate, volume, null, change, gap, periodStart, periodEnd, barSize, false);
//							bars.add(bar);
//							
//							previousClose = close;
//						}
//					}
//				}
//				
//				// Set the most recent one to partial and toss the oldest one (we got one more bar than we needed)
//				if (bars.size() > 0) {
//					bars.get(bars.size() - 1).partial = true;
//					bars.remove(0);
//				}
				break; // Made it here ok - no need to try again
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return bars;
	}
	
	public static ArrayList<LinkedTreeMap<String, Object>> returnChartData(String ticker, Calendar start, Calendar end, int period) {
		try {
			long startMS = start.getTimeInMillis() / 1000;
			long endMS = end.getTimeInMillis() / 1000;

			String urlString = PoloniexConstants.BASE_URL + PoloniexConstants.COMMAND_RETURNCHARTDATA + PoloniexConstants.PARAM_TICKER + ticker
					+ PoloniexConstants.PARAM_START + startMS + PoloniexConstants.PARAM_END + endMS + PoloniexConstants.PARAM_PERIOD + period;

			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String json = response.toString();
			ArrayList<LinkedTreeMap<String, Object>> jsonList = new Gson().fromJson(json, ArrayList.class);
			return jsonList;
		} 
		catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}
}
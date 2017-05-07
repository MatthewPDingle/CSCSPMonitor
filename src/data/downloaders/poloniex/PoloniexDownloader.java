package data.downloaders.poloniex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import utils.CalendarUtils;
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
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS zzz");
			String sStart = "01/01/2016 00:00:00.000 EST";
			String sEnd = 	"05/07/2017 00:00:00.000 EST";
			Calendar start = Calendar.getInstance();
			start.setTime(sdf.parse(sStart));
			Calendar end = Calendar.getInstance();
			end.setTime(sdf.parse(sEnd));
			
			getMostRecentBarsFromBarHistory("BTC_ETH", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_LTC", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_XRP", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_XMR", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_DASH", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_FCT", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_DOGE", BAR_SIZE.BAR_5M, start, end);
			getMostRecentBarsFromBarHistory("BTC_XEM", BAR_SIZE.BAR_5M, start, end);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		// Get params
//		ArrayList<String[]> params = new ArrayList<String[]>();
//		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
//		if (args != null) {
//			for (int a = 0; a < args.length; a += 3) {
//				String symbol = args[a];
//				String duration = args[a + 1];
//				String numBars = args[a + 2];
//				String[] param = new String[3];
//				param[0] = symbol;
//				param[1] = duration;
//				param[2] = numBars;
//				params.add(param);
//				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
//				barKeys.add(barKey);
//			}
//		}
//		
//		// Experimental
//		ArrayList<String> metricNames = new ArrayList<String>();
//		metricNames.addAll(Constants.METRICS);
//				
//		// Loop.  First pass get 1000 bars.  All other passes, get the number specified by parameters.
//		if (params.size() > 0) {
//			MetricSingleton metricSingleton = MetricSingleton.getInstance();
//			metricSingleton.init(barKeys, metricNames);
//			
//			boolean firstPass = true;
//			int numBars = 1000;
//			while (true) {	
//				try {
//					System.out.println(Calendar.getInstance().getTime().toString());
//					for (String[] param : params) {
//						if (!firstPass) {
//							numBars = Integer.parseInt(param[2]);
//						}
//						downloadBarsAndUpdate(param[0], BAR_SIZE.valueOf(param[1]), numBars);
//						System.out.println(param[0] + " " + param[1] + " done.");
//					}
//					firstPass = false;
//					System.out.println(Calendar.getInstance().getTime().toString() + " - Bar Downloads & Inserts Done");
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	/**
	 * Note: 2M, 10M, 8H bars are not supported on Poloniex's API
	 * 
	 * @param barSize
	 * @return
	 */
	public static ArrayList<Bar> getMostRecentBarsFromBarHistory(String pxSymbol, Constants.BAR_SIZE barSize, Calendar startC, Calendar endC) {
		ArrayList<Bar> bars = new ArrayList<Bar>();
		try {
			int barMinutes = 0;
			switch (barSize) {
				case BAR_5M:
					barMinutes = 5;
					break;
				case BAR_15M:
					barMinutes = 15;
					break;
				case BAR_30M:
					barMinutes = 30;
					break;
				case BAR_2H:
					barMinutes = 120;
					break;
				case BAR_4H:
					barMinutes = 240;
					break;
				case BAR_1D:
					barMinutes = 1440;
					break;
				default:
					break;
			}
			int barSeconds = barMinutes * 60;
			
			ArrayList<LinkedTreeMap<String, Object>> chartData = returnChartData(pxSymbol, startC, endC, barSeconds);
			for (LinkedTreeMap<String, Object> chartBar : chartData) {
				double start = (double)chartBar.get("date");
				double open = (double)chartBar.get("open");
				double close = (double)chartBar.get("close");
				double high = (double)chartBar.get("high");
				double low = (double)chartBar.get("low");
				double vwap = (double)chartBar.get("weightedAverage");
				double volume = (double)chartBar.get("volume");
				double quoteVolume = (double)chartBar.get("quoteVolume");
				
				Calendar barStart = Calendar.getInstance();
				barStart.setTimeInMillis((long)start * 1000);
				
				Calendar barEnd = Calendar.getInstance();
				barEnd = CalendarUtils.addBars(barStart, barSize, 1);

				Bar bar = new Bar(pxSymbol, open, close, high, low, vwap, volume, null, null, null, barStart, barEnd, barSize, false);
				System.out.println(bar.toString());
				QueryManager.insertOrUpdateIntoBar(bar);
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
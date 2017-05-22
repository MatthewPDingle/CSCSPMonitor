package servlets.downloaders;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.downloaders.interactivebrokers.IBConstants;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
import data.downloaders.okcoin.OKCoinConstants;
import data.downloaders.okcoin.OKCoinDownloader;
import data.downloaders.okcoin.websocket.NIAConnectionMonitoringThread;
import data.downloaders.okcoin.websocket.NIAStatusSingleton;
import dbio.QueryManager;
import metrics.MetricSingleton;
import status.StatusSingleton;
import utils.CalendarUtils;

/**
 * Servlet implementation class RealtimeDownloaderServlet
 */
@WebServlet("/RealtimeDownloaderServlet")
public class RealtimeDownloaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RealtimeDownloaderServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get params
		String[] symbols = request.getParameterValues("symbols[]");
		String[] durations = request.getParameterValues("durations[]");
		String[] metrics = request.getParameterValues("metrics[]");
		boolean includeMetrics = Boolean.parseBoolean(request.getParameter("includeMetrics")); 
		boolean run = Boolean.parseBoolean(request.getParameter("run")); 
		
		MetricSingleton ms = MetricSingleton.getInstance();
		StatusSingleton ss = StatusSingleton.getInstance();
		IBSingleton ibs = IBSingleton.getInstance();
		HashMap<String, String> out = new HashMap<String, String>();
		
		// Tell the StatusSingleton if we're running
		ss.setRealtimeDownloaderRunning(run);
		
		// Setup BarKeys & Metrics
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		ArrayList<String> metricList = new ArrayList<String>();
		if (ss.isRealtimeDownloaderRunning()) {
			ss.addMessageToDataMessageQueue("Preparing to run Realtime Downloader");
				
			// Build BarKeys
			if (symbols != null) {
				for (int a = 0; a < symbols.length; a++) {
					String symbol = symbols[a];
					String duration = durations[a];
		
					BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
					barKeys.add(barKey);
				}
			}
			
			// What metrics do we want
			if (metrics != null) {
				metricList.addAll(Arrays.asList(metrics));
			}
		}
		
		// Tell the NIASS to stop if that's the signal
		NIAStatusSingleton niass = NIAStatusSingleton.getInstance();
		if (!ss.isRealtimeDownloaderRunning()) {
			niass.stopClient();
			niass.setKeepAlive(false);
			out.put("exitReason", "cancelled");
			
			ibs.cancelWorkers();
			ss.addMessageToDataMessageQueue("RealtimeDownloaderServlet requesting cancel realtime bars.");
		}
		else {
			HashMap<BarKey, Calendar> lastDownloadHash = ss.getLastDownloadHash();

			boolean restOK = true;
			boolean okCoin = false;
			
			for (BarKey bk : barKeys) {
				// Figure out how many bars to download
				int numBarsNeeded = 1200;
				Calendar cNow = Calendar.getInstance();
				Bar mostRecentDBBar = QueryManager.getMostRecentBar(bk, Calendar.getInstance());
				if (mostRecentDBBar != null) {
					numBarsNeeded = CalendarUtils.getNumBars(mostRecentDBBar.periodStart, cNow, bk.duration) + 1;
				}
				if (lastDownloadHash.get(bk) != null) {
					Calendar lastDownloadForThisBK = lastDownloadHash.get(bk);
					numBarsNeeded = CalendarUtils.getNumBars(lastDownloadForThisBK, cNow, bk.duration);
				}
		
				String equityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(bk.symbol);
				if (equityType == null) {
					equityType = "";
				}
				
				// OKCOIN
				if (bk.symbol.contains("okcoin")) {
					// Run the REST API bulk bar downloader
					okCoin = true;
					int numDownloadedBars = OKCoinDownloader.downloadBarsAndUpdate(OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol), bk.duration, numBarsNeeded);
					if (numDownloadedBars > 0) {
						ss.addMessageToDataMessageQueue("OKCoin REST API downloaded " + numDownloadedBars + " of " + numBarsNeeded + " bars of " + bk.duration + " " + bk.symbol);
						ss.recordLastDownload(bk, Calendar.getInstance());

						if (includeMetrics) {
							ms.init(barKeys, metricList); // Need to init here after the bars are updated so the metric sequences extend forward until now.
							ms.startThreads();
							ss.addMessageToDataMessageQueue("Calculated " + metricList.size() + " metrics for " + bk.duration + " for " + OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol));
						}
						else {
							ms.stopThreads();
						}	
					}
					else if (numBarsNeeded > 0) {
						restOK = false;
						ss.setRealtimeDownloaderRunning(false);
						niass.stopClient();
						niass.setKeepAlive(false);
						ss.addMessageToDataMessageQueue("OKCoin REST API failed to download " + bk.duration + " " + bk.symbol);
						out.put("exitReason", "failed");
					}
				}
				// INTERACTIVE BROKERS
				else if (equityType.equals("CASH") || equityType.equals("FUT")) {
					// Have to tell the MetricSingleton about the metrics & BKs ahead of time because the IBWorker will need to do an initial calculation after getting the historical data
					if (includeMetrics) {
						ms.setNeededMetrics(metricList);
						ms.setBarKeys(barKeys);
					}
					else {
						ms.stopThreads();
					}	
					
					// IBWorker will handle both historical data to catch up and realtime bars.
					
					// For futures, I may want to be downloading multiple specific contracts simultaneously.
					if (equityType.equals("FUT")) {
						if (bk.symbol.equals("ZN")) {
							Calendar cInOneWeek = Calendar.getInstance();
							cInOneWeek.add(Calendar.WEEK_OF_YEAR, 1);
							Calendar cOneWeekAgo = Calendar.getInstance();
							cOneWeekAgo.add(Calendar.WEEK_OF_YEAR, -1);
							
							String contractSuffix1 = CalendarUtils.getFuturesContractBasedOnRolloverDate(bk.symbol, cInOneWeek);
							String contractSuffix2 = CalendarUtils.getFuturesContractBasedOnRolloverDate(bk.symbol, cOneWeekAgo);
							
							HashSet<String> contractSuffixes = new HashSet<String>();
							contractSuffixes.add(contractSuffix1);
							contractSuffixes.add(contractSuffix2);
							
							for (String contractSuffix : contractSuffixes) {
								String fullContract = bk.symbol + " " + contractSuffix;
								BarKey bkSpecific = new BarKey(fullContract, bk.duration);
								
								IBWorker ibWorker = ibs.requestWorker(bkSpecific);
								ibWorker.downloadRealtimeBars();
								ibWorker.requestTickSubscription();
							}
						}
					}
					else {
						IBWorker ibWorker = ibs.requestWorker(bk);
						ibWorker.downloadRealtimeBars();
						ibWorker.requestTickSubscription();
					}	
				}
			} // Go to next BarKey
			
			if (restOK && okCoin) {
				System.out.println("NOW WE CAN START THE WEBSOCKET");
				niass.startClient();
				ArrayList<String> symbolsSubscribedTo = new ArrayList<String>();
				for (BarKey bk : barKeys) {
					if (bk.symbol.contains("okcoin")) {
						String websocketPrefix = OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get(bk.symbol);
						String okCoinBarDuration = OKCoinConstants.OKCOIN_BAR_SIZE_TO_BAR_DURATION_HASH.get(bk.duration);
						// Subscribe to the Bar data and the Tick data and the OrderBook data
						niass.addChannel(websocketPrefix + "kline_" + okCoinBarDuration); // Bars
						// Subscribe to Ticks & Order Book for symbol, but make sure we don't try subscribing more than once
						if (!symbolsSubscribedTo.contains(bk.symbol)) {
							niass.addChannel(OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_SYMBOL_HASH.get(bk.symbol)); // Ticks
							niass.addChannel(OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get(bk.symbol) + "depth"); // Order Book
							symbolsSubscribedTo.add(bk.symbol);
						}
					}
				}
				NIAConnectionMonitoringThread monitoringThread = new NIAConnectionMonitoringThread();
				monitoringThread.start();
			}
		}
		
		out.put("exitReason", "complete");
			
		Gson gson = new Gson();
		String json = gson.toJson(out);
		
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();
		pw.println(json);
		pw.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
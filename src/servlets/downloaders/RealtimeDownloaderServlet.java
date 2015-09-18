package servlets.downloaders;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.downloaders.okcoin.OKCoinConstants;
import data.downloaders.okcoin.OKCoinDownloader;
import data.downloaders.okcoin.websocket.OKCoinWebSocketSingleton;
import singletons.StatusSingleton;

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
		
		singletons.MetricSingleton ms = singletons.MetricSingleton.getInstance();
		StatusSingleton ss = StatusSingleton.getInstance();
		HashMap<String, String> out = new HashMap<String, String>();
		
		// Tell the StatusSingleton if we're running
		ss.setRealtimeDownloaderRunning(run);
		
		if (symbols == null || symbols.length == 0) {
			return;
		}
		
		// Setup BarKeys & Metrics
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		ArrayList<String> metricList = new ArrayList<String>();
		if (ss.isRealtimeDownloaderRunning()) {
			ss.addMessageToDataMessageQueue("Preparing to run Realtime Downloader");
				
			// Build BarKeys
			for (int a = 0; a < symbols.length; a++) {
				String symbol = symbols[a];
				String duration = durations[a];
	
				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
			
			// What metrics do we want
			if (metrics != null) {
				metricList.addAll(Arrays.asList(metrics));
			}
			
			out.put("exitReason", "cancelled");
		}
		
		OKCoinWebSocketSingleton okss = OKCoinWebSocketSingleton.getInstance();
		if (ss.isRealtimeDownloaderRunning()) {
			if (barKeys.size() > 0) {
				okss.setRunning(true);
				for (BarKey bk : barKeys) {
					if (bk.symbol.contains("okcoin")) {
						String websocketPrefix = OKCoinConstants.TICK_SYMBOL_TO_WEBSOCKET_PREFIX_HASH.get(bk.symbol);
						String okCoinBarDuration = OKCoinConstants.OKCOIN_BAR_SIZE_TO_BAR_DURATION_HASH.get(bk.duration);
						okss.addChannel(websocketPrefix + "kline_" + okCoinBarDuration);
					}
				}
			}
		}
		else {
		
			okss.setRunning(false);
		}
		
		while (ss.isRealtimeDownloaderRunning()) {
			try {
				HashMap<BarKey, Calendar> lastDownloadHash = ss.getLastDownloadHash();
				
				if (includeMetrics) {
					ms.init(barKeys, metricList);
				}
				
				for (BarKey bk : barKeys) {
					// Figure out how many bars to download
					boolean firstGo = true;
					if (lastDownloadHash.get(bk) != null) {
						firstGo = false;
					}

					if (bk.symbol.contains("okcoin")) {
						// On the first go, use the REST API to download the latest 2000 bars
						if (firstGo) {
							int numBars = 2000;
							OKCoinDownloader.downloadBarsAndUpdate(OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol), bk.duration, numBars);
							String message = "OKCoin REST API downloaded " + numBars + " bars of " + bk.duration + " " + OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol);
							ss.addMessageToDataMessageQueue(message);
							ss.recordLastDownload(bk, Calendar.getInstance());
						}
						else {
							boolean anyInsertsMade = okss.insertLatestBarsIntoDB();
							if (anyInsertsMade) {
								StatusSingleton.getInstance().recordLastDownload(bk, Calendar.getInstance());
							}
							ss.addMessageToDataMessageQueue("OKCoin WebSocket API streaming " + OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol));
						}
					}
					
					if (includeMetrics) {
						ms.setRunning(true);
						ss.addMessageToDataMessageQueue("Calculating " + metricList.size() + " metrics for " + bk.duration + " for " + OKCoinConstants.TICK_SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol));
					}
					else {
						ms.setRunning(false);
					}
				}
				
				Thread.sleep(1000);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Realtime download ending, so clear out last download times
		ss.setLastDownloadHash(new HashMap<BarKey, Calendar>());

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
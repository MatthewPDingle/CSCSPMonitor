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
import gui.singletons.MetricSingleton;
import metrics.MetricsUpdaterThread;
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
		
		while (ss.isRealtimeDownloaderRunning()) {
			try {
				Calendar lastRealtimeDownload = ss.getLastRealtimeDownload();
				int numBars = 1000;
				if (lastRealtimeDownload != null) {
					numBars = 5;
				}
				
				if (includeMetrics) {
					ss.addMessageToDataMessageQueue("Initializing MetricSingleton");
					ms.init(barKeys, metricList);
				}
				
				for (BarKey bk : barKeys) {
					if (bk.symbol.contains("okcoin")) {
						OKCoinDownloader.downloadBarsAndUpdate(OKCoinConstants.SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol), bk.duration, numBars);
						String message = "Downloaded " + numBars + " bars of " + bk.duration + " for " + OKCoinConstants.SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol);
						ss.addMessageToDataMessageQueue(message);
						ss.setLastRealtimeDownload(Calendar.getInstance());
					}
					
					if (includeMetrics) {
						ss.addMessageToDataMessageQueue("Calculating " + metricList.size() + " metrics for " + bk.duration + " for " + OKCoinConstants.SYMBOL_TO_OKCOIN_SYMBOL_HASH.get(bk.symbol));
						ms.setRunning(true);
						ss.addMessageToDataMessageQueue("Finished calculating metrics");
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
		ss.setLastRealtimeDownload(null);

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
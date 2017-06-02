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
import data.BarKey;
import data.downloaders.interactivebrokers.IBConstants;
import data.downloaders.interactivebrokers.IBSingleton;
import data.downloaders.interactivebrokers.IBWorker;
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
		
		if (!ss.isRealtimeDownloaderRunning()) {
			out.put("exitReason", "cancelled");
			
			ibs.cancelWorkers();
			ss.addMessageToDataMessageQueue("RealtimeDownloaderServlet requesting cancel realtime bars.");
		}
		else {
			// Have to tell the MetricSingleton about the metrics & BKs ahead of time because the IBWorker will need to do an initial calculation after getting the historical data
			if (includeMetrics) {
				ms.setNeededMetrics(metricList);
			}
			else {
				ms.stopThreads();
			}	
			
			for (BarKey bk : barKeys) {
				String equityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(bk.symbol);
				if (equityType == null) {
					equityType = "";
				}

				// INTERACTIVE BROKERS - IBWorker will handle both historical data to catch up and realtime bars.
				if (equityType.equals("CASH")) {
					ms.addBarKey(bk);
					IBWorker ibWorker = ibs.requestWorker(bk);
					ibWorker.downloadRealtimeBars();
				}
				else if (equityType.equals("FUT")) {
					// For futures, I need to grab the correct dated contract
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
							ms.addBarKey(bk); // Want to calculate metrics for the continuous contract, not the dated ones.
							ss.addMessageToDataMessageQueue("Futures contract for " + bk.symbol + ": " + fullContract);
							IBWorker ibWorker = ibs.requestWorker(bkSpecific);
							ibWorker.downloadRealtimeBars();
						}
					}
				}
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
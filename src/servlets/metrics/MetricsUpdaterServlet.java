package servlets.metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import metrics.MetricsUpdaterThread;
import singletons.MetricSingleton;
import singletons.StatusSingleton;

/**
 * Servlet implementation class MetricsUpdaterServlet
 */
@WebServlet("/MetricsUpdaterServlet")
public class MetricsUpdaterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MetricsUpdaterServlet() {
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
		
		// Build BarKeys
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		if (symbols != null) {
			for (int a = 0; a < symbols.length; a++) {
				String symbol = symbols[a];
				String duration = durations[a];
	
				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
		} 
		
		// What metrics do we want
		ArrayList<String> metricList = new ArrayList<String>();
		if (metrics == null || metrics.length == 0) {
			metricList = Constants.METRICS;
		}
		else {
			metricList.addAll(Arrays.asList(metrics));
		}
		
		StatusSingleton ss = StatusSingleton.getInstance();
		ss.addMessageToDataMessageQueue("Initializing MetricSingleton");
		MetricSingleton ms = MetricSingleton.getInstance();
		ms.init(barKeys, metricList);
		
		if (barKeys != null && barKeys.size() > 0) {
			ss.addMessageToDataMessageQueue("Metric calculations starting");
			ms.setRunning(true);
			ss.addMessageToDataMessageQueue("Metric calculations finished");
		}
		else {
			ss.addMessageToDataMessageQueue("Metric calculations stopping");
			ms.setRunning(false);
			ss.addMessageToDataMessageQueue("Metric calculations stopped");
		}
		
		ArrayList<String> out = new ArrayList<String>();
		
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
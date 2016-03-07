package servlets.trading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.Model;
import dbio.QueryManager;
import status.StatusSingleton;
import trading.TradingSingleton;

/**
 * Servlet implementation class TradingServlet
 */
@WebServlet("/TradingServlet")
public class TradingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TradingServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] symbols = request.getParameterValues("symbols[]");
		String[] durations = request.getParameterValues("durations[]");
		String[] modelFiles = request.getParameterValues("modelfiles[]");
		
		String modelsPath = getServletContext().getRealPath("/WEB-INF/weka/models");
		
		StatusSingleton ss = StatusSingleton.getInstance();
		TradingSingleton ts = TradingSingleton.getInstance();
		
		// Build BarKeys
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		if (symbols != null && symbols.length > 0) {
			for (int a = 0; a < symbols.length; a++) {
				String symbol = symbols[a];
				String duration = durations[a];
	
				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
		}
		
		// Load the models that are going to be used for trading
		String whereClause = "";
		if (modelFiles != null && modelFiles.length > 0) {
			whereClause = "WHERE modelfile IN (";
			for (String modelFile : modelFiles) {
				whereClause += "'" + modelFile + "', ";
			}
			whereClause = whereClause.substring(0, whereClause.length() - 2);
			whereClause += ")";
		}
		
		ts.setModelsPath(modelsPath);

		// Load & Cache the trading models & classifiers
		ArrayList<Model> tradingModels = new ArrayList<Model>();
		if (modelFiles != null && modelFiles.length > 0) {
			tradingModels = QueryManager.getModels(whereClause);
			ss.addStatusMessageToTradingMessageQueue("Loading " + modelFiles.length + " models into memory for " + barKeys.size() + " BarKeys.");
			for (Model model : tradingModels) {
				ts.addModel(model);
			}
			
			ss.addStatusMessageToTradingMessageQueue(ts.getWekaClassifierHash().size() + " models now cached in memory.");
			ss.addStatusMessageToTradingMessageQueue("Trading engine running.");
		}
		else {
			ss.addStatusMessageToTradingMessageQueue(ts.getWekaClassifierHash().size() + " models cached in memory.  Trading engine inactive.");
			ts.clearBKModelHash();
		}
		

		// Start/Stop the trading engine 
		if (barKeys.size() > 0) {
			ts.setRunning(true);
		}
		else {
			ts.setRunning(false);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
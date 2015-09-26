package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import data.Model;
import dbio.QueryManager;

/**
 * Servlet implementation class ModelServlet
 */
@WebServlet("/ModelServlet")
public class ModelServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModelServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String whereClause = request.getParameter("whereClause");
		
		ArrayList<Model> models = QueryManager.getModels(whereClause);
		ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>> out = new ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>>();
		
		HashMap<String, ArrayList<HashMap>> columnHash = new HashMap<String, ArrayList<HashMap>>();

		// Explicitly define columns so we can have ordering
		ArrayList<String> columnList = new ArrayList<String>();
		columnList.add("id");
		columnList.add("type");
		columnList.add("modelFile");
		columnList.add("algo");
		columnList.add("symbol");
		columnList.add("duration");
		columnList.add("metrics");
		columnList.add("interBarData");
//		columnList.add("trainStart");
//		columnList.add("trainEnd");
//		columnList.add("testStart");
//		columnList.add("testEnd");
		columnList.add("sellMetric");
		columnList.add("sellMetricValue");
		columnList.add("stopMetric");
		columnList.add("stopMetricValue");
		columnList.add("multiplier");
		columnList.add("numBars");
		columnList.add("trainDatasetSize");
//		columnList.add("trainTrueNegatives");
//		columnList.add("trainFalseNegatives");
//		columnList.add("trainTruePositives");
//		columnList.add("trainFalsePositives");
//		columnList.add("trainTruePositiveRate");
//		columnList.add("trainFalsePositiveRate");
//		columnList.add("trainWinPercent");
//		columnList.add("trainROCArea");
		columnList.add("testDatasetSize");
//		columnList.add("testTrueNegatives");
//		columnList.add("testFalseNegatives");
//		columnList.add("testTruePositives");
//		columnList.add("testFalsePositives");
		columnList.add("testTruePositiveRate");
		columnList.add("testFalsePositiveRate");
		columnList.add("testWinPercent");
		columnList.add("testEstimatedAverageReturn");
		columnList.add("testNumOpportunities");
		columnList.add("testROCArea");
		
		HashMap<String, ArrayList<HashMap<String, Object>>> rowPart = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		ArrayList<HashMap<String, Object>> modelHashList = (ArrayList<HashMap<String, Object>>)Model.convertCollection(models);
		
		// Optionally auto-generate all columns, but the ordering will be lost
//		ArrayList<String> columnList = new ArrayList<String>();
//		HashMap<String, Object> modelHash = modelHashList.get(0);
//		if (modelHash != null) {	
//			Iterator i = modelHash.entrySet().iterator();
//			while (i.hasNext()) {
//				Map.Entry pair = (Map.Entry)i.next();
//				String field = pair.getKey().toString();
//				columnList.add(field);
//			}
//		}
		// Schema
		ArrayList<HashMap<String, Object>> schema = new ArrayList<HashMap<String, Object>>();
		
		ArrayList<String> hiddenColumns = new ArrayList<String>();
		hiddenColumns.add("params");
		hiddenColumns.add("metrics");
		hiddenColumns.add("trainCorrectRate");
		hiddenColumns.add("trainKappa");
		hiddenColumns.add("trainMeanAbsolueError");
		hiddenColumns.add("trainRootMeanSquaredError");
		hiddenColumns.add("trainRelativeAbsoluteError");
		hiddenColumns.add("trainRootRelativeSquaredError");
		hiddenColumns.add("testCorrectRate");
		hiddenColumns.add("testKappa");
		hiddenColumns.add("testMeanAbsolueError");
		hiddenColumns.add("testRootMeanSquaredError");
		hiddenColumns.add("testRelativeAbsoluteError");
		hiddenColumns.add("testRootRelativeSquaredError");
		
		for (String column : columnList) {
			HashMap<String, Object> colPropertyHash = new HashMap<String, Object>();
			
			colPropertyHash.put("text", column);
			colPropertyHash.put("datafield", column);
			colPropertyHash.put("type", "string");
			colPropertyHash.put("align", "left");
			colPropertyHash.put("cellsalign", "left");
			
			// Hidden Columns
			if (hiddenColumns.contains(column)) {
				colPropertyHash.put("hidden", true);
			}
			else {
				colPropertyHash.put("hidden", false);
			}
			
			// Column Widths
			if (column.contains("Start") || column.contains("End")) {
				colPropertyHash.put("width", 82);
			}
			else if (column.equals("symbol")) {
				colPropertyHash.put("width", 120);
			}
			else if (column.equals("duration")) {
				colPropertyHash.put("width", 70);
			}
			else if (column.equals("modelFile")) {
				colPropertyHash.put("width", 140);
			}
			else if (column.equals("interBarData")) {
				colPropertyHash.put("text", "IBD");
				colPropertyHash.put("width", 40);
			}
			else if (column.equals("id")) {
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 30);
			}
			else if (column.equals("type")) {
				colPropertyHash.put("width", 38);
			}
			else if (column.equals("numBars")) {
				colPropertyHash.put("text", "Bars");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 38);
			}
			else if (column.equals("sellMetric")) {
				colPropertyHash.put("text", "Sell");
				colPropertyHash.put("width", 66);
			}
			else if (column.equals("stopMetric")) {
				colPropertyHash.put("text", "Stop");
				colPropertyHash.put("width", 66);
			}
			else if (column.equals("sellMetricValue")) {
				colPropertyHash.put("text", "SellV");
				colPropertyHash.put("width", 38);
			}
			else if (column.equals("stopMetricValue")) {
				colPropertyHash.put("text", "StopV");
				colPropertyHash.put("width", 38);
			}
			else if (column.equals("multiplier")) {
				colPropertyHash.put("text", "Mult");
				colPropertyHash.put("width", 38);
			}
			else if (column.equals("trainDatasetSize")) {
				colPropertyHash.put("text", "Tr.Size");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainTrueNegatives")) {
				colPropertyHash.put("text", "Tr.TN");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainFalseNegatives")) {
				colPropertyHash.put("text", "Tr.FN");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainTruePositives")) {
				colPropertyHash.put("text", "Tr.TP");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainFalsePositives")) {
				colPropertyHash.put("text", "Tr.FP");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainTruePositiveRate")) {
				colPropertyHash.put("text", "Tr.TPR");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainFalsePositiveRate")) {
				colPropertyHash.put("text", "Tr.FPR");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainWinPercent")) {
				colPropertyHash.put("text", "Tr.WP");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("trainROCArea")) {
				colPropertyHash.put("text", "Tr.ROC");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testDatasetSize")) {
				colPropertyHash.put("text", "Te.Size");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testTrueNegatives")) {
				colPropertyHash.put("text", "Te.TN");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testFalseNegatives")) {
				colPropertyHash.put("text", "Te.FN");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testTruePositives")) {
				colPropertyHash.put("text", "Te.TP");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testFalsePositives")) {
				colPropertyHash.put("text", "Te.FP");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testTruePositiveRate")) {
				colPropertyHash.put("text", "Te.TPR");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testFalsePositiveRate")) {
				colPropertyHash.put("text", "Te.FPR");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testWinPercent")) {
				colPropertyHash.put("text", "Te.WP");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testEstimatedAverageReturn")) {
				colPropertyHash.put("text", "Te.EAR");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testNumOpportunities")) {
				colPropertyHash.put("text", "Te.#Op");
				colPropertyHash.put("type", "number");
				colPropertyHash.put("width", 58);
			}
			else if (column.equals("testROCArea")) {
				colPropertyHash.put("text", "Te.ROC");
				colPropertyHash.put("type", "float");
				colPropertyHash.put("width", 58);
			}
			else {
				colPropertyHash.put("width", 100);
			}
			
			
			
			
			schema.add(colPropertyHash);
		}
		HashMap<String, ArrayList<HashMap<String, Object>>> schemaPart = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		
		schemaPart.put("columns", schema);
		rowPart.put("rows", modelHashList);
		
		out.add(schemaPart);
		out.add(rowPart);
		
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
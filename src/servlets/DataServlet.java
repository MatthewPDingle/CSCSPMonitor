package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dbio.QueryManager;

/**
 * Servlet implementation class DataServlet
 */
@WebServlet("/DataServlet")
public class DataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ArrayList<HashMap<String, Object>> barAndMetricInfo = QueryManager.getBarAndMetricInfo();
		ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>> out = new ArrayList<HashMap<String, ArrayList<HashMap<String, Object>>>>();
		
		// Explicitly define columns so we can have ordering
		ArrayList<String> columnList = new ArrayList<String>();
		columnList.add("symbol");
		columnList.add("duration");
		columnList.add("barmin");
		columnList.add("barmax");
		columnList.add("barage");
		columnList.add("barcount");
		columnList.add("metricmin");
		columnList.add("metricmax");
		columnList.add("metricage");

		HashMap<String, ArrayList<HashMap<String, Object>>> rowPart = new HashMap<String, ArrayList<HashMap<String, Object>>>();

		// Schema
		ArrayList<HashMap<String, Object>> schema = new ArrayList<HashMap<String, Object>>();

		for (String column : columnList) {
			HashMap<String, Object> colPropertyHash = new HashMap<String, Object>();
			
			colPropertyHash.put("text", column);
			colPropertyHash.put("datafield", column);
			colPropertyHash.put("type", "string");
			colPropertyHash.put("align", "left");
			colPropertyHash.put("cellsalign", "left");
			colPropertyHash.put("width", 150);

			if (column.contains("age")) {
				colPropertyHash.put("width", 170);
			}
			else if (column.equals("symbol")) {
				colPropertyHash.put("width", 120);
			}
			else if (column.equals("duration")) {
				colPropertyHash.put("width", 80);
			}
			else if (column.equals("barcount")) {
				colPropertyHash.put("width", 80);
			}
			
			schema.add(colPropertyHash);
		}
		HashMap<String, ArrayList<HashMap<String, Object>>> schemaPart = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		
		
		schemaPart.put("columns", schema);
		rowPart.put("rows", barAndMetricInfo);
		
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
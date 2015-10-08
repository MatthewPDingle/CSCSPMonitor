package servlets.models;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import dbio.QueryManager;

/**
 * Servlet implementation class ModelUpdateServlet
 */
@WebServlet("/ModelUpdateServlet")
public class ModelUpdateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModelUpdateServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String modelID = request.getParameter("modelID");
		String column = request.getParameter("column");
		String checked = request.getParameter("checked");

		if (column.equals("favorite")) {
			QueryManager.updateModelFavorite(Integer.parseInt(modelID), Boolean.parseBoolean(checked));
		}
		else if (column.equals("tradeOffPrimary")) {
			QueryManager.updateModelTradeOffPrimary(Integer.parseInt(modelID), Boolean.parseBoolean(checked));
		}
		else if (column.equals("tradeOffOpposite")) {
			QueryManager.updateModelTradeOffOpposite(Integer.parseInt(modelID), Boolean.parseBoolean(checked));
		}
		
		ArrayList<String> messages = new ArrayList<String>();
		Gson gson = new Gson();
		String json = gson.toJson(messages);
		
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
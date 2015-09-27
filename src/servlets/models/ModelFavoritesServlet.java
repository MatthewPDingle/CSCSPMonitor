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
 * Servlet implementation class ModelFavoritesServlet
 */
@WebServlet("/ModelFavoritesServlet")
public class ModelFavoritesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ModelFavoritesServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String modelID = request.getParameter("modelID");
		String checked = request.getParameter("checked");

		QueryManager.updateModelFavorite(Integer.parseInt(modelID), Boolean.parseBoolean(checked));
		
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
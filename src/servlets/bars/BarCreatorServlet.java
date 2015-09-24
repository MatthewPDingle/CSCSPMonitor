package servlets.bars;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import constants.Constants.BAR_SIZE;
import data.Converter;
import status.StatusSingleton;

/**
 * Servlet implementation class BarCreatorServlet
 */
@WebServlet("/BarCreatorServlet")
public class BarCreatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BarCreatorServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get params
		String[] symbols = request.getParameterValues("symbols[]");
		String[] durations = request.getParameterValues("durations[]");
		
		StatusSingleton ss = StatusSingleton.getInstance();
		
		if (symbols != null && durations != null) {
			for (String symbol : symbols) {
				for (String duration : durations) {
					ss.addMessageToDataMessageQueue("Creating bars of " + duration + " for " + symbol);
					Converter.processTickDataIntoBars(symbol, BAR_SIZE.valueOf(duration));
				}
			}
			ss.addMessageToDataMessageQueue("Done making bars");
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
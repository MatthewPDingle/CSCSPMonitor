package servlets.downloaders.bitcoincharts;

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
import data.downloaders.bitcoincharts.BitcoinChartsConstants;
import data.downloaders.bitcoincharts.BitcoinChartsDownloader;
import singletons.StatusSingleton;

/**
 * Servlet implementation class BitcoinChartsServlet
 */
@WebServlet("/BitcoinChartsServlet")
public class BitcoinChartsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BitcoinChartsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] archiveSymbols = request.getParameterValues("archiveSymbols[]");
		String[] archiveDurations = request.getParameterValues("archiveDurations[]");

		String dataPath = getServletContext().getRealPath("/WEB-INF/data");
		
		try {
			ArrayList<String> archiveSymbolsNoDups = new ArrayList<String>();
			for (int a = 0; a < archiveSymbols.length; a++) {
				if (!archiveSymbolsNoDups.contains(archiveSymbols[a])) {
					archiveSymbolsNoDups.add(archiveSymbols[a]);
				}
			}
			
			StatusSingleton ss = StatusSingleton.getInstance();
			
			// Download archive files
			for (String archiveSymbol : archiveSymbolsNoDups) {
				String filename = BitcoinChartsConstants.TICKNAME_FILENAME_HASH.get(archiveSymbol);
				ss.addMessageToDataMessageQueue("Downloading archive " + filename);
				BitcoinChartsDownloader.downloadArchive(filename, dataPath);
				ss.addMessageToDataMessageQueue("Processing archive " + filename + " into ticks");
				Converter.processArchiveFileIntoTicks(filename, dataPath);
			}
			
			// Process into ticks & bars
			for (int a = 0; a < archiveDurations.length; a++) {
				String filename = BitcoinChartsConstants.TICKNAME_FILENAME_HASH.get(archiveSymbols[a]);
				String tickname = BitcoinChartsConstants.FILENAME_TICKNAME_HASH.get(filename);
				String barSize = archiveDurations[a];
				
				ss.addMessageToDataMessageQueue("Processing ticks for " + tickname + " into " + barSize);
				Converter.processTickDataIntoBars(tickname, BAR_SIZE.valueOf(barSize));
			}
			
			ss.addMessageToDataMessageQueue("BitcoinCharts Downloader complete");
	
			ArrayList<String> out = new ArrayList<String>();
			
			Gson gson = new Gson();
			String json = gson.toJson(out);
			
			response.setContentType("application/json");
			PrintWriter pw = response.getWriter();
			pw.println(json);
			pw.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		
	}
}
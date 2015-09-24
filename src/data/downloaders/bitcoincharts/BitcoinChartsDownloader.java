package data.downloaders.bitcoincharts;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import constants.Constants.BAR_SIZE;
import data.Converter;

public class BitcoinChartsDownloader {

	/**
	 * args must come in pairs.  
	 * First is the filename you want to download from BitcoinCharts
	 * Second is the bar size i.e. BAR_15M
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		for (int a = 0; a < args.length; a += 2) {
			String filename = args[a];
			String barSize = args[a + 1];
			String tickname = BitcoinChartsConstants.FILENAME_TICKNAME_HASH.get(filename);

			if (tickname != null) {
				if (BAR_SIZE.valueOf(barSize) != null) {
					System.out.println("Downloading: " + filename);
					downloadArchive(filename, null);
					System.out.println("Inserting ticks from file into DB");
					Converter.processArchiveFileIntoTicks(filename, null);
					System.out.println("Converting ticks into bars and inserting into DB");
					Converter.processTickDataIntoBars(tickname, BAR_SIZE.valueOf(barSize));
					System.out.println("Finished: " + filename);
				}
				else {
					System.out.println("Bad BAR_SIZE: " + barSize);
				}
			}
			else {
				System.out.println("No support for " + filename);
			}
		}
	}

	public static void downloadArchive(String fileName, String dataPath) {
		try {
			if (dataPath == null) {
				dataPath = "data";
			}
			
			File dir = new File(dataPath);
			if (!dir.exists()) {
				dir.mkdir();
			}
			
			FileUtils.copyURLToFile(new URL(BitcoinChartsConstants.URL + fileName), new File(dataPath + "/" + fileName));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
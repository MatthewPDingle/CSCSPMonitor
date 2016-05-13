package test.backtest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.BarWithMetricData;
import dbio.QueryManager;

public class BackTester {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	
	public static void main(String[] args) {
		try {
			String start = "01/01/2016 00:00:00";
			String end = "05/12/2016 16:00:00";
			
			Calendar startC = Calendar.getInstance();
			Calendar endC = Calendar.getInstance();
			
			startC.setTimeInMillis(sdf.parse(start).getTime());
			endC.setTimeInMillis(sdf.parse(end).getTime());
			
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			barKeys.add(new BarKey("EUR.USD", BAR_SIZE.BAR_5M));
			
			ArrayList<BarWithMetricData> barWMDList = QueryManager.loadMetricSequenceHashForBackTests(barKeys, startC, endC);
			
			for (BarWithMetricData barWMD : barWMDList) {
				System.out.println(barWMD.periodStart.getTime().toString() + "\t\t" + barWMD.getMetricData().size());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadBarAndMetricData(BarKey bk) {
		try {
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package data.downloaders.poloniex;

public class PoloniexConstants {

	public static String BASE_URL = "https://poloniex.com/public";
	
	public static String COMMAND_RETURNCHARTDATA = "?command=returnChartData";
	public static String COMMAND_RETURNTICKER = "?command=returnTicker";
	
	public static String PARAM_TICKER = "&currencyPair=";
	public static String PARAM_START = "&start=";
	public static String PARAM_END = "&end=";
	public static String PARAM_PERIOD = "&period=";
	
	public static String BAR_DURATION_5M = "5min";
	public static String BAR_DURATION_15M = "15min";
	public static String BAR_DURATION_30M = "30min";
	public static String BAR_DURATION_2H = "2hour";
	public static String BAR_DURATION_4H = "4hour";
	public static String BAR_DURATION_1D = "1day";
}
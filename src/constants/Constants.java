package constants;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {

	public static String URL = "jdbc:postgresql://localhost:5432/stocks";
	public static String USERNAME = "postgres";
	public static String PASSWORD = "graham23";
	
	public static enum BAR_SIZE {BAR_1M, BAR_3M, BAR_5M, BAR_10M, BAR_15M, BAR_30M, BAR_1H, BAR_2H, BAR_4H, BAR_6H, BAR_8H, BAR_12H, BAR_1D};
	
	public static String BAR_TABLE = "bar";			
	public static String METRICS_TABLE = "metrics";	
	public static String INDEXLIST_TABLE = "indexlist"; // NYSE, Nasdaq, ETF, Index, Bitcoin
	public static String SECTORANDINDUSTRY_TABLE = "sectorandindustry";
	public static String REALTIMESYMBOLS_TABLE = "realtimesymbols";

	public static int NUM_BARS_NEEDED_FOR_REALTIME_DOWNLOAD_METRIC_CALC = 101;
	
	// Datasource URLs.  These occasionally break and need to be fixed or replaced.
	public static String YAHOO_NYSE_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5ENYA&c="; // "c" parameter = a page number (0+) // Broken as of 7/2/2015
	public static String NYSE_SYMBOL_URL = "http://www1.nyse.com/indexes/nyaindex.csv";
	public static String YAHOO_NASDAQ_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5EIXIC&c=";
	public static String YAHOO_DJIA_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5EDJI&c=";
	public static String YAHOO_ETF_SYMBOL_URL = "http://finance.yahoo.com/etf/lists/?mod_id=mediaquotesetf&tab=tab3&scol=volint&stype=desc&rcnt=100"; 
	public static String WIKI_ETF_SYMBOL_URL = "http://en.wikipedia.org/wiki/List_of_American_exchange-traded_funds";
	public static String YAHOO_REALTIME_QUOTE_URL = "http://download.finance.yahoo.com/d/quotes.csv?f=sk1d1c6ohgv&e=.csv&s=";
	public static String WIKI_SP500_SYMBOL_URL = "http://en.wikipedia.org/wiki/List_of_S%26P_500_companies"; // Broken as of 7/2/2015
	public static String OKFN_SP500_SYMBOL_URL = "http://data.okfn.org/data/core/s-and-p-500-companies/r/constituents.csv";
	public static String BITCOINCHARTS_CSV_URL = "http://api.bitcoincharts.com/v1/csv/";
	
	public static String OTHER_SELL_METRIC_NUM_BARS_LATER = "# Bars";
	public static String OTHER_SELL_METRIC_PERCENT_UP = "% Up";
	public static String OTHER_SELL_METRIC_PERCENT_DOWN = "% Down";
	
	public static String STOP_METRIC_NONE = "None";
	public static String STOP_METRIC_PERCENT_DOWN = "% Down";
	public static String STOP_METRIC_PERCENT_UP = "% Up";
	public static String STOP_METRIC_NUM_BARS = "# Bars";

	public static ArrayList<String> METRICS = new ArrayList<String>();
	public static ArrayList<String> OTHER_SELL_METRICS = new ArrayList<String>();
	public static ArrayList<String> STOP_METRICS = new ArrayList<String>();
	public static HashMap<String, Float> METRIC_MIN_MAX_VALUE = new HashMap<String, Float>();
	public static HashMap<String, Integer> METRIC_NEEDED_BARS = new HashMap<String, Integer>();
	
	static {
		// METRICS
		METRICS.add("rsi2");
		METRICS.add("rsi5"); // OUT	
		METRICS.add("rsi14"); // OUT
//		METRICS.add("rsi10"); // IN
//		METRICS.add("rsi40"); // IN
		METRICS.add("mfi8");
		METRICS.add("mfi12"); // OUT
		METRICS.add("mfi16");
		METRICS.add("consecutiveups");
		METRICS.add("consecutivedowns");
		METRICS.add("cps");
		METRICS.add("pricebolls20");
		METRICS.add("pricebolls50");
		METRICS.add("pricebolls100");
//		METRICS.add("pricebolls200");
//		METRICS.add("gapboll10");
//		METRICS.add("gapboll20");
//		METRICS.add("gapboll50");
//		METRICS.add("intradayboll10");
//		METRICS.add("intradayboll20"); // IN
//		METRICS.add("intradayboll50");
		METRICS.add("volumebolls20");
		METRICS.add("volumebolls50");
		METRICS.add("volumebolls100");
//		METRICS.add("volumebolls200");
//		METRICS.add("dvol10ema"); // IN
//		METRICS.add("dvol25ema");
//		METRICS.add("dvol50ema");
//		METRICS.add("dvol75ema");
//		METRICS.add("breakout20"); // IN
//		METRICS.add("breakout50"); // IN
//		METRICS.add("breakout100"); // IN
//		METRICS.add("breakout200");
		METRICS.add("williamsr10"); // OUT
		METRICS.add("williamsr20"); 
		METRICS.add("williamsr50"); // OUT
		METRICS.add("macd12_26_9");
		METRICS.add("macd20_40_9"); // OUT
		METRICS.add("macdsignal12_26_9");
		METRICS.add("macdsignal20_40_9"); // OUT
		METRICS.add("macdhistory12_26_9");
		METRICS.add("macdhistory20_40_9"); // OUT
		METRICS.add("tsf10");
		METRICS.add("tsf20"); // OUT
//		METRICS.add("tsf30"); // IN
		METRICS.add("tsf40"); //OUT
//		METRICS.add("tsf60"); // IN
		METRICS.add("psar");
		METRICS.add("ultimateoscillator4_10_25");
		METRICS.add("ultimateoscillator8_20_50");
		METRICS.add("aroonoscillator10"); // OUT
		METRICS.add("aroonoscillator25");
		METRICS.add("aroonoscillator50"); // OUT
		METRICS.add("stochasticdrsi14_3_3");
		METRICS.add("stochasticdrsi20_5_5");
		METRICS.add("stochastick14_3_3");
		METRICS.add("stochasticd14_3_3"); // OUT
		METRICS.add("stochastick20_5_5");
		METRICS.add("stochasticd20_5_5"); // OUT
		METRICS.add("cci10");
		METRICS.add("cci20"); // OUT
		METRICS.add("cci40"); // OUT
//		METRICS.add("cci60"); // IN
		METRICS.add("atr10"); 
		METRICS.add("atr20"); // OUT
		METRICS.add("atr40"); // OUT
//		METRICS.add("atr60"); // IN
//		METRICS.add("mvol100"); // IN
//		METRICS.add("cdlhammer");
//		METRICS.add("cdldoji");
//		METRICS.add("cdlmorningstar");
		
		// METRIC_NEEDED_BARS
		METRIC_NEEDED_BARS.put("rsi2", 10);
		METRIC_NEEDED_BARS.put("rsi5", 10);
		METRIC_NEEDED_BARS.put("rsi10", 20);
		METRIC_NEEDED_BARS.put("rsi14", 20);
		METRIC_NEEDED_BARS.put("rsi40", 50);
		METRIC_NEEDED_BARS.put("mfi8", 10);
		METRIC_NEEDED_BARS.put("mfi12", 20);
		METRIC_NEEDED_BARS.put("mfi16", 20);
		METRIC_NEEDED_BARS.put("consecutiveups", 20);
		METRIC_NEEDED_BARS.put("consecutivedowns", 20);
		METRIC_NEEDED_BARS.put("cps", 20);
		METRIC_NEEDED_BARS.put("pricebolls20", 30);
		METRIC_NEEDED_BARS.put("pricebolls50", 60);
		METRIC_NEEDED_BARS.put("pricebolls100", 110);
		METRIC_NEEDED_BARS.put("pricebolls200", 210);
		METRIC_NEEDED_BARS.put("gapboll10", 20);
		METRIC_NEEDED_BARS.put("gapboll20", 30);
		METRIC_NEEDED_BARS.put("gapboll50", 60);
		METRIC_NEEDED_BARS.put("intradayboll10", 20);
		METRIC_NEEDED_BARS.put("intradayboll20", 30);
		METRIC_NEEDED_BARS.put("intradayboll50", 60);
		METRIC_NEEDED_BARS.put("volumebolls20", 30);
		METRIC_NEEDED_BARS.put("volumebolls50", 60);
		METRIC_NEEDED_BARS.put("volumebolls100", 110);
		METRIC_NEEDED_BARS.put("volumebolls200", 210);
		METRIC_NEEDED_BARS.put("dvol10ema", 30);
		METRIC_NEEDED_BARS.put("dvol25ema", 20);
		METRIC_NEEDED_BARS.put("dvol50ema", 10);
		METRIC_NEEDED_BARS.put("dvol75ema", 10);
		METRIC_NEEDED_BARS.put("breakout20", 30);
		METRIC_NEEDED_BARS.put("breakout50", 60);
		METRIC_NEEDED_BARS.put("breakout100", 110);
		METRIC_NEEDED_BARS.put("breakout200", 210);
		METRIC_NEEDED_BARS.put("williamsr10", 20);
		METRIC_NEEDED_BARS.put("williamsr20", 30);
		METRIC_NEEDED_BARS.put("williamsr50", 60);
		METRIC_NEEDED_BARS.put("macd12_26_9", 40);
		METRIC_NEEDED_BARS.put("macd20_40_9", 50);
		METRIC_NEEDED_BARS.put("macdsignal12_26_9", 40);
		METRIC_NEEDED_BARS.put("macdsignal20_40_9", 50);
		METRIC_NEEDED_BARS.put("macdhistory12_26_9", 40);
		METRIC_NEEDED_BARS.put("macdhistory20_40_9", 50);
		METRIC_NEEDED_BARS.put("tsf10", 20);
		METRIC_NEEDED_BARS.put("tsf20", 30);
		METRIC_NEEDED_BARS.put("tsf30", 40);
		METRIC_NEEDED_BARS.put("tsf40", 50);
		METRIC_NEEDED_BARS.put("tsf60", 70);
		METRIC_NEEDED_BARS.put("psar", 20);
		METRIC_NEEDED_BARS.put("ultimateoscillator4_10_25", 30);
		METRIC_NEEDED_BARS.put("ultimateoscillator8_20_50", 60);
		METRIC_NEEDED_BARS.put("aroonoscillator10", 20);
		METRIC_NEEDED_BARS.put("aroonoscillator25", 40);
		METRIC_NEEDED_BARS.put("aroonoscillator50", 60);
		METRIC_NEEDED_BARS.put("stochasticdrsi14_3_3", 30);
		METRIC_NEEDED_BARS.put("stochasticdrsi20_5_5", 30);
		METRIC_NEEDED_BARS.put("stochastick14_3_3", 30);
		METRIC_NEEDED_BARS.put("stochasticd14_3_3", 30);
		METRIC_NEEDED_BARS.put("stochastick20_5_5", 30);
		METRIC_NEEDED_BARS.put("stochasticd20_5_5", 30);
		METRIC_NEEDED_BARS.put("cci10", 20);
		METRIC_NEEDED_BARS.put("cci20", 30);
		METRIC_NEEDED_BARS.put("cci40", 50);
		METRIC_NEEDED_BARS.put("cci60", 70);
		METRIC_NEEDED_BARS.put("atr10", 20);
		METRIC_NEEDED_BARS.put("atr20", 30);
		METRIC_NEEDED_BARS.put("atr40", 50);
		METRIC_NEEDED_BARS.put("atr60", 70);
		METRIC_NEEDED_BARS.put("mvol100", 110);
//		METRIC_NEEDED_BARS.put("cdlhammer", 10);
//		METRIC_NEEDED_BARS.put("cdldoji", 10);
//		METRIC_NEEDED_BARS.put("cdlmorningstar", 10);
		
		// METRIC_MIN_MAX_VALUE
		METRIC_MIN_MAX_VALUE.put("min_consecutiveups", 0f);
		METRIC_MIN_MAX_VALUE.put("max_consecutiveups", 10f);
		METRIC_MIN_MAX_VALUE.put("min_consecutivedowns", 0f);
		METRIC_MIN_MAX_VALUE.put("max_consecutivedowns", 10f);
		METRIC_MIN_MAX_VALUE.put("min_cps", -10f);
		METRIC_MIN_MAX_VALUE.put("max_cps", 10f);
		METRIC_MIN_MAX_VALUE.put("min_rsi40", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi40", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi14", 0f); // RSI and MFI are 0 to 100 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_rsi14", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi10", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi5", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi5", 100f);
		METRIC_MIN_MAX_VALUE.put("min_rsi2", 0f);
		METRIC_MIN_MAX_VALUE.put("max_rsi2", 100f);
		METRIC_MIN_MAX_VALUE.put("min_mfi8", 0f);
		METRIC_MIN_MAX_VALUE.put("max_mfi8", 100f);
		METRIC_MIN_MAX_VALUE.put("min_mfi12", 0f);
		METRIC_MIN_MAX_VALUE.put("max_mfi12", 100f);
		METRIC_MIN_MAX_VALUE.put("min_mfi16", 0f);
		METRIC_MIN_MAX_VALUE.put("max_mfi16", 100f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd20", 0f); // Don't normalized standard deviations?  Default values are 0 - 6
		METRIC_MIN_MAX_VALUE.put("max_pricesd20", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd50", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd100", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd100", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricesd200", 0f);
		METRIC_MIN_MAX_VALUE.put("max_pricesd200", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd20", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd50", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd100", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd100", 6f);
		METRIC_MIN_MAX_VALUE.put("min_volumesd200", 0f);
		METRIC_MIN_MAX_VALUE.put("max_volumesd200", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricebolls20", -6f); // Bolls are all -4 to 4 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_pricebolls20", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricebolls50", -6f);
		METRIC_MIN_MAX_VALUE.put("max_pricebolls50", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricebolls100", -6f);
		METRIC_MIN_MAX_VALUE.put("max_pricebolls100", 6f);
		METRIC_MIN_MAX_VALUE.put("min_pricebolls200", -6f);
		METRIC_MIN_MAX_VALUE.put("max_pricebolls200", 6f);
		METRIC_MIN_MAX_VALUE.put("min_gapboll10", 0f);
		METRIC_MIN_MAX_VALUE.put("max_gapboll10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_gapboll20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_gapboll20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_gapboll50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_gapboll50", 100f);
		METRIC_MIN_MAX_VALUE.put("min_intradayboll10", 0f);
		METRIC_MIN_MAX_VALUE.put("max_intradayboll10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_intradayboll20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_intradayboll20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_intradayboll50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_intradayboll50", 100f);
		METRIC_MIN_MAX_VALUE.put("min_volumebolls20", -3f);
		METRIC_MIN_MAX_VALUE.put("max_volumebolls20", 7f);
		METRIC_MIN_MAX_VALUE.put("min_volumebolls50", -3f);
		METRIC_MIN_MAX_VALUE.put("max_volumebolls50", 7f);
		METRIC_MIN_MAX_VALUE.put("min_volumebolls100", -2f);
		METRIC_MIN_MAX_VALUE.put("max_volumebolls100", 13f);
		METRIC_MIN_MAX_VALUE.put("min_volumebolls200", -2f);
		METRIC_MIN_MAX_VALUE.put("max_volumebolls200", 13f);
		METRIC_MIN_MAX_VALUE.put("min_dvol10ema", 0f); // Dvol is 0 - 8 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_dvol10ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_dvol25ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol25ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_dvol50ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol50ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_dvol75ema", 0f);
		METRIC_MIN_MAX_VALUE.put("max_dvol75ema", 100f);
		METRIC_MIN_MAX_VALUE.put("min_breakout20", 0f); // Breakout is -1 - 1 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_breakout20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_breakout50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_breakout50", 100f);
		METRIC_MIN_MAX_VALUE.put("min_breakout100", 0f);
		METRIC_MIN_MAX_VALUE.put("max_breakout100", 100f);
		METRIC_MIN_MAX_VALUE.put("min_breakout200", 0f);
		METRIC_MIN_MAX_VALUE.put("max_breakout200", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsr10", 0f); // WilliamsR is 0 - 100 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_williamsr10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsr20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsr20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_williamsr50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_williamsr50", 100f);
		METRIC_MIN_MAX_VALUE.put("min_tsf10", -3f); // TSF is ? - ? when not normalized
		METRIC_MIN_MAX_VALUE.put("max_tsf10", 3f);
		METRIC_MIN_MAX_VALUE.put("min_tsf20", -4f);
		METRIC_MIN_MAX_VALUE.put("max_tsf20", 4f);
		METRIC_MIN_MAX_VALUE.put("min_tsf30", -4f);
		METRIC_MIN_MAX_VALUE.put("max_tsf30", 4f);
		METRIC_MIN_MAX_VALUE.put("min_tsf40", -5f);
		METRIC_MIN_MAX_VALUE.put("max_tsf40", 5f);
		METRIC_MIN_MAX_VALUE.put("min_tsf60", -5f);
		METRIC_MIN_MAX_VALUE.put("max_tsf60", 5f);
		METRIC_MIN_MAX_VALUE.put("min_macd12_26_9", -4f); // MACD is -10 to 10 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_macd12_26_9", 4f);
		METRIC_MIN_MAX_VALUE.put("min_macd20_40_9", -4f);
		METRIC_MIN_MAX_VALUE.put("max_macd20_40_9", 4f);
		METRIC_MIN_MAX_VALUE.put("min_macdsignal12_26_9", -4f); // MACD Signal is -2.5 to 2.5 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_macdsignal12_26_9", 4f);
		METRIC_MIN_MAX_VALUE.put("min_macdsignal20_40_9", -4f);
		METRIC_MIN_MAX_VALUE.put("max_macdsignal20_40_9", 4f);	
		METRIC_MIN_MAX_VALUE.put("min_macdhistory12_26_9", -2f); // MACD History is -? to ? when not normalized
		METRIC_MIN_MAX_VALUE.put("max_macdhistory12_26_9", 2f);
		METRIC_MIN_MAX_VALUE.put("min_macdhistory20_40_9", -2f);
		METRIC_MIN_MAX_VALUE.put("max_macdhistory20_40_9", 2f);
		METRIC_MIN_MAX_VALUE.put("min_psar", -4f); // PSAR is -10 to 10 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_psar", 4f);
		METRIC_MIN_MAX_VALUE.put("min_ultimateoscillator4_10_25", 0f); // Ultimate & Aroon oscillators are 0 to 100 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_ultimateoscillator4_10_25", 100f);
		METRIC_MIN_MAX_VALUE.put("min_ultimateoscillator8_20_50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_ultimateoscillator8_20_50", 100f);
		METRIC_MIN_MAX_VALUE.put("min_aroonoscillator10", 0f);
		METRIC_MIN_MAX_VALUE.put("max_aroonoscillator10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_aroonoscillator25", 0f);
		METRIC_MIN_MAX_VALUE.put("max_aroonoscillator25", 100f);
		METRIC_MIN_MAX_VALUE.put("min_aroonoscillator50", 0f);
		METRIC_MIN_MAX_VALUE.put("max_aroonoscillator50", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci10", -100f); // CCI is -100 to 100 when not normalized
		METRIC_MIN_MAX_VALUE.put("max_cci10", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci20", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci20", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci30", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci30", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci40", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci40", 100f);
		METRIC_MIN_MAX_VALUE.put("min_cci60", -100f);
		METRIC_MIN_MAX_VALUE.put("max_cci60", 100f);
		METRIC_MIN_MAX_VALUE.put("min_stochasticdrsi14_3_3", 0f); // Stochastic RSI is ? to ?
		METRIC_MIN_MAX_VALUE.put("max_stochasticdrsi14_3_3", 100f);
		METRIC_MIN_MAX_VALUE.put("min_stochasticdrsi20_5_5", 0f);
		METRIC_MIN_MAX_VALUE.put("max_stochasticdrsi20_5_5", 100f);
		METRIC_MIN_MAX_VALUE.put("min_stochastick14_3_3", 0f); // Stochastic is ? to ?
		METRIC_MIN_MAX_VALUE.put("max_stochastick14_3_3", 100f);
		METRIC_MIN_MAX_VALUE.put("min_stochastick20_5_5", 0f);
		METRIC_MIN_MAX_VALUE.put("max_stochastick20_5_5", 100f);
		METRIC_MIN_MAX_VALUE.put("min_stochasticd14_3_3", 0f);
		METRIC_MIN_MAX_VALUE.put("max_stochasticd14_3_3", 100f);
		METRIC_MIN_MAX_VALUE.put("min_stochasticd20_5_5", 0f);
		METRIC_MIN_MAX_VALUE.put("max_stochasticd20_5_5", 100f);
		METRIC_MIN_MAX_VALUE.put("min_atr10", 0f); // ATR is ? - ? when not normalized
		METRIC_MIN_MAX_VALUE.put("max_atr10", 20f);
		METRIC_MIN_MAX_VALUE.put("min_atr20", 0f);
		METRIC_MIN_MAX_VALUE.put("max_atr20", 20f);
		METRIC_MIN_MAX_VALUE.put("min_atr30", 0f);
		METRIC_MIN_MAX_VALUE.put("max_atr30", 20f);
		METRIC_MIN_MAX_VALUE.put("min_atr40", 0f);
		METRIC_MIN_MAX_VALUE.put("max_atr40", 20f);
		METRIC_MIN_MAX_VALUE.put("min_atr60", 0f);
		METRIC_MIN_MAX_VALUE.put("max_atr60", 20f);
		METRIC_MIN_MAX_VALUE.put("min_mvol100", 0f); // Need to check what the min and max are for this one
		METRIC_MIN_MAX_VALUE.put("max_mvol100", 20f);
		METRIC_MIN_MAX_VALUE.put("min_cdlhammer", 0f); // ATR is ? - ? when not normalized
		METRIC_MIN_MAX_VALUE.put("max_cdlhammer", 1f);
		METRIC_MIN_MAX_VALUE.put("min_cdldoji", 0f);
		METRIC_MIN_MAX_VALUE.put("max_cdldoji", 1f);
		METRIC_MIN_MAX_VALUE.put("min_cdlmorningstar", 0f);
		METRIC_MIN_MAX_VALUE.put("max_cdlmorningstar", 1f);
		
		// OTHER_SELL_METRICS
		OTHER_SELL_METRICS.add(OTHER_SELL_METRIC_NUM_BARS_LATER);
		OTHER_SELL_METRICS.add(OTHER_SELL_METRIC_PERCENT_UP);
		OTHER_SELL_METRICS.add(OTHER_SELL_METRIC_PERCENT_DOWN);
		
		// STOP_METRICS
		STOP_METRICS.add(STOP_METRIC_NONE);
		STOP_METRICS.add(STOP_METRIC_PERCENT_DOWN);
		STOP_METRICS.add(STOP_METRIC_PERCENT_UP);
		STOP_METRICS.add(STOP_METRIC_NUM_BARS);
	}
}
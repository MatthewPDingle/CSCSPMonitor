package constants;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {

	public static String URL = "jdbc:postgresql://localhost:5432/stocks";
	public static String USERNAME = "postgres";
	public static String PASSWORD = "graham23";

	public static enum BAR_SIZE {
		BAR_15S, BAR_30S, BAR_1M, BAR_3M, BAR_5M, BAR_10M, BAR_15M, BAR_30M, BAR_1H, BAR_2H, BAR_4H, BAR_6H, BAR_8H, BAR_12H, BAR_1D
	};

	public static String BAR_TABLE = "bar";
	public static String METRICS_TABLE = "metrics";
	public static String INDEXLIST_TABLE = "indexlist"; // NYSE, Nasdaq, ETF, Index, Bitcoin
	public static String SECTORANDINDUSTRY_TABLE = "sectorandindustry";
	public static String REALTIMESYMBOLS_TABLE = "realtimesymbols";

	public static int NUM_BARS_NEEDED_FOR_REALTIME_DOWNLOAD_METRIC_CALC = 101;

	// Datasource URLs. These occasionally break and need to be fixed or replaced
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
	public static ArrayList<String> METRICS_BEST = new ArrayList<String>();
	public static ArrayList<String> OTHER_SELL_METRICS = new ArrayList<String>();
	public static ArrayList<String> STOP_METRICS = new ArrayList<String>();
	public static HashMap<String, Integer> METRIC_NEEDED_BARS = new HashMap<String, Integer>();

	static {
		// METRICS
		// METRICS.add("ado3_10");
		// METRICS.add("ado10_30");
		// METRICS.add("ado30_100");
		// METRICS.add("ado100_300");
		// METRICS.add("adodydx10_30");
		// METRICS.add("adodydx30_100");
		// METRICS.add("adodydx100_300");
		METRICS.add("adx3");
		METRICS.add("adx10");
		METRICS.add("adx30");
		METRICS.add("adx100");
		METRICS.add("adx300");
		METRICS.add("adx1000");
		METRICS.add("adxdydx30");
		METRICS.add("adxdydx100");
		METRICS.add("adxdydx300");
		METRICS.add("adxr3");
		METRICS.add("adxr10");
		METRICS.add("adxr30");
		METRICS.add("adxr100");
		METRICS.add("adxr300");
		METRICS.add("adxr1000");
		METRICS.add("adxrdydx30");
		METRICS.add("adxrdydx100");
		METRICS.add("adxrdydx300");
		METRICS.add("aroonoscillator3");
		METRICS.add("aroonoscillator10");
		METRICS.add("aroonoscillator30");
		METRICS.add("aroonoscillator100");
		METRICS.add("aroonoscillator300");
		METRICS.add("atr3");
		METRICS.add("atr10");
		METRICS.add("atr30");
		METRICS.add("atr100");
		METRICS.add("atr300");
		METRICS.add("atr1000");
		METRICS.add("atrdydx30");
		METRICS.add("atrdydx100");
		METRICS.add("atrdydx300");
		METRICS.add("breakout30");
		METRICS.add("breakout100");
		METRICS.add("breakout300");
		METRICS.add("cci3");
		METRICS.add("cci10");
		METRICS.add("cci30");
		METRICS.add("cci100");
		METRICS.add("cci300");
		METRICS.add("cmo3");
		METRICS.add("cmo10");
		METRICS.add("cmo30");
		METRICS.add("cmo100");
		METRICS.add("cmo300");
		// METRICS.add("consecutiveups");
		// METRICS.add("consecutivedowns");
		// METRICS.add("cps");
		METRICS.add("dvol1ema");
		METRICS.add("dvol2ema");
		METRICS.add("dvol3ema");
		METRICS.add("dvol5ema");
		METRICS.add("dvol10ema");
		METRICS.add("dvol25ema");
		METRICS.add("dvol50ema");
		METRICS.add("dvol75ema");
		// METRICS.add("dvoldydx2ema");
		// METRICS.add("dvoldydx3ema");
		// METRICS.add("dvoldydx5ema");
		// METRICS.add("gapboll3");
		// METRICS.add("gapboll10");
		// METRICS.add("gapboll30");
		// METRICS.add("gapboll100");
		// METRICS.add("gapboll300");
		METRICS.add("intradayboll3");
		METRICS.add("intradayboll10");
		METRICS.add("intradayboll30");
		METRICS.add("intradayboll100");
		METRICS.add("intradayboll300");
		METRICS.add("macd10_30_8");
		METRICS.add("macd30_100_24");
		METRICS.add("macd100_300_80");
		METRICS.add("macd300_1000_240");
		METRICS.add("macds10_30_8");
		METRICS.add("macds30_100_24");
		METRICS.add("macds100_300_80");
		METRICS.add("macds300_1000_240");
		METRICS.add("macdh10_30_8");
		METRICS.add("macdh30_100_24");
		METRICS.add("macdh100_300_80");
		METRICS.add("macdh300_1000_240");
		// METRICS.add("mfi3");
		// METRICS.add("mfi10");
		// METRICS.add("mfi30");
		// METRICS.add("mfi100");
		// METRICS.add("mfi300");
		METRICS.add("mvol3");
		METRICS.add("mvol10");
		METRICS.add("mvol30");
		METRICS.add("mvol100");
		METRICS.add("mvol300");
		METRICS.add("mvol1000");
		METRICS.add("mvol3000");
		// METRICS.add("mvoldydx300");
		// METRICS.add("mvoldydx1000");
		METRICS.add("ppo3_10");
		METRICS.add("ppo10_30");
		METRICS.add("ppo30_100");
		METRICS.add("ppo100_300");
		METRICS.add("ppo300_1000");
		METRICS.add("ppodydx10_30");
		METRICS.add("ppodydx30_100");
		METRICS.add("ppodydx100_300");
		METRICS.add("pricebolls3");
		METRICS.add("pricebolls10");
		METRICS.add("pricebolls30");
		METRICS.add("pricebolls100");
		METRICS.add("pricebolls300");
		METRICS.add("psar");
		METRICS.add("rangepressure30");
		METRICS.add("rangepressure100");
		METRICS.add("rangepressure300");
		METRICS.add("rangepressure1000");
		METRICS.add("rangepressure3000");
		METRICS.add("rangerank30");
		METRICS.add("rangerank100");
		METRICS.add("rangerank300");
		METRICS.add("rangerank1000");
		METRICS.add("rangerank3000");
		METRICS.add("rsi3");
		METRICS.add("rsi10");
		METRICS.add("rsi30");
		METRICS.add("rsi100");
		METRICS.add("rsi300");
		METRICS.add("stodrsi10_3_3");
		METRICS.add("stodrsi30_10_10");
		METRICS.add("stodrsi100_30_30");
		METRICS.add("stodrsi300_100_100");
		METRICS.add("stokrsi10_3_3");
		METRICS.add("stokrsi30_10_10");
		METRICS.add("stokrsi100_30_30");
		METRICS.add("stokrsi300_100_100");
		METRICS.add("stod10_3_3");
		METRICS.add("stod30_10_10");
		METRICS.add("stod100_30_30");
		METRICS.add("stod300_100_100");
		METRICS.add("stok10_3_3");
		METRICS.add("stok30_10_10");
		METRICS.add("stok100_30_30");
		METRICS.add("stok300_100_100");
		METRICS.add("timerange2");
		METRICS.add("timerange5");
		METRICS.add("timerange8");
		METRICS.add("timerange13");
		METRICS.add("timerange20");
		METRICS.add("timerange30");
//		METRICS.add("timerange40");
		METRICS.add("timerangealpha2");
		METRICS.add("timerangealpha5");
		METRICS.add("timerangealpha8");
		METRICS.add("timerangealpha13");
		METRICS.add("timerangealpha20");
		METRICS.add("timerangealpha30");
//		METRICS.add("timerangealpha40");
		METRICS.add("tsf3");
		METRICS.add("tsf10");
		METRICS.add("tsf30");
		METRICS.add("tsf100");
		METRICS.add("tsf300");
		METRICS.add("tsfdydx30");
		METRICS.add("tsfdydx100");
		METRICS.add("tsfdydx300");
		METRICS.add("uo3_10_30");
		METRICS.add("uo10_30_100");
		METRICS.add("uo30_100_300");
		// METRICS.add("volumebolls3");
		// METRICS.add("volumebolls10");
		// METRICS.add("volumebolls30");
		// METRICS.add("volumebolls100");
		// METRICS.add("volumebolls300");
		METRICS.add("williamsr3");
		METRICS.add("williamsr10");
		METRICS.add("williamsr30");
		METRICS.add("williamsr100");
		METRICS.add("williamsr300");
		// METRICS.add("cdlhammer");
		// METRICS.add("cdldoji");
		// METRICS.add("cdlmorningstar");

		METRICS_BEST.add("adx100");
		METRICS_BEST.add("adx300");
		METRICS_BEST.add("adxr100");
		METRICS_BEST.add("adxr300");
		METRICS_BEST.add("adxrdydx30");
		METRICS_BEST.add("adxrdydx300");
		METRICS_BEST.add("aroonoscillator300");
		METRICS_BEST.add("atr3");
		METRICS_BEST.add("atr10");
		METRICS_BEST.add("atr30");
		METRICS_BEST.add("atr100");
		METRICS_BEST.add("atr300");
		METRICS_BEST.add("cci300");
		METRICS_BEST.add("cmo10");
		METRICS_BEST.add("cmo300");
		METRICS_BEST.add("dvol2ema");
		METRICS_BEST.add("dvol3ema");
		METRICS_BEST.add("dvol5ema");
		METRICS_BEST.add("dvol10ema");
		METRICS_BEST.add("dvol25ema");
		METRICS_BEST.add("dvol50ema");
		METRICS_BEST.add("dvol75ema");
		METRICS_BEST.add("intradayboll10");
		METRICS_BEST.add("intradayboll30");
		METRICS_BEST.add("macd30_100_24");
		METRICS_BEST.add("macd100_300_80");
		METRICS_BEST.add("macdh10_30_8");
		METRICS_BEST.add("macdh30_100_24");
		METRICS_BEST.add("macdh100_300_80");
		METRICS_BEST.add("macds10_30_8");
		METRICS_BEST.add("macds30_100_24");
		METRICS_BEST.add("macds100_300_80");
		METRICS_BEST.add("mvol10");
		METRICS_BEST.add("mvol100");
		METRICS_BEST.add("mvol300");
		METRICS_BEST.add("mvol1000");
		METRICS_BEST.add("ppo3_10");
		METRICS_BEST.add("ppo30_100");
		METRICS_BEST.add("ppo100_300");
		METRICS_BEST.add("pricebolls10");
		METRICS_BEST.add("pricebolls30");
		METRICS_BEST.add("pricebolls300");
		METRICS_BEST.add("rangepressure3000");
		METRICS_BEST.add("rsi10");
		METRICS_BEST.add("rsi300");
		METRICS_BEST.add("stod10_3_3");
		METRICS_BEST.add("stod30_10_10");
		METRICS_BEST.add("stod300_100_100");
		METRICS_BEST.add("stok100_30_30");
		METRICS_BEST.add("stok300_100_100");
		METRICS_BEST.add("timerangealpha2");
		METRICS_BEST.add("timerangealpha5");
		METRICS_BEST.add("timerangealpha8");
		METRICS_BEST.add("timerangealpha13");
		METRICS_BEST.add("timerangealpha20");
		METRICS_BEST.add("timerangealpha30");
		METRICS_BEST.add("timerange2");
		METRICS_BEST.add("timerange5");
		METRICS_BEST.add("timerange8");
		METRICS_BEST.add("timerange13");
		METRICS_BEST.add("timerange20");
		METRICS_BEST.add("timerange30");
		METRICS_BEST.add("tsf30");
		METRICS_BEST.add("williamsr300");
		
//		METRICS.clear();
//		METRICS.addAll(METRICS_BEST);
		
		// METRIC_NEEDED_BARS
		METRIC_NEEDED_BARS.put("ado3_10", 21);				// TA-Lib
		METRIC_NEEDED_BARS.put("ado10_30", 61);				// TA-Lib
		METRIC_NEEDED_BARS.put("ado30_100", 201);			// TA-Lib
		METRIC_NEEDED_BARS.put("ado100_300", 601);			// TA-Lib
		METRIC_NEEDED_BARS.put("adodydx10_30", 62);			// TA-Lib
		METRIC_NEEDED_BARS.put("adodydx30_100", 202);		// TA-Lib
		METRIC_NEEDED_BARS.put("adodydx100_300", 602);		// TA-Lib
		METRIC_NEEDED_BARS.put("adx3", 7);					// TA-Lib
		METRIC_NEEDED_BARS.put("adx10", 21);				// TA-Lib
		METRIC_NEEDED_BARS.put("adx30", 61);				// TA-Lib
		METRIC_NEEDED_BARS.put("adx100", 201);				// TA-Lib 
		METRIC_NEEDED_BARS.put("adx300", 601); 				// TA-Lib 
		METRIC_NEEDED_BARS.put("adx1000", 2001); 			// TA-Lib 
		METRIC_NEEDED_BARS.put("adxdydx30", 62);			// TA-Lib 
		METRIC_NEEDED_BARS.put("adxdydx100", 202);			// TA-Lib 
		METRIC_NEEDED_BARS.put("adxdydx300", 602);			// TA-Lib 
		METRIC_NEEDED_BARS.put("adxr3", 10);				// TA-Lib 
		METRIC_NEEDED_BARS.put("adxr10", 31);				// TA-Lib 
		METRIC_NEEDED_BARS.put("adxr30", 91);				// TA-Lib 	
		METRIC_NEEDED_BARS.put("adxr100", 301); 			// TA-Lib
		METRIC_NEEDED_BARS.put("adxr300", 901); 			// TA-Lib
		METRIC_NEEDED_BARS.put("adxr1000", 3001); 			// TA-Lib
		METRIC_NEEDED_BARS.put("adxrdydx30", 92);			// TA-Lib 
		METRIC_NEEDED_BARS.put("adxrdydx100", 302);			// TA-Lib 
		METRIC_NEEDED_BARS.put("adxrdydx300", 902);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonoscillator3", 7);		// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonoscillator10", 21);	// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonoscillator30", 61);	// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonoscillator100", 201);	// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonoscillator300", 601);	// TA-Lib 
		METRIC_NEEDED_BARS.put("atr3", 7);					// TA-Lib 
		METRIC_NEEDED_BARS.put("atr10", 21);				// TA-Lib 
		METRIC_NEEDED_BARS.put("atr30", 61);				// TA-Lib 
		METRIC_NEEDED_BARS.put("atr100", 201);				// TA-Lib 
		METRIC_NEEDED_BARS.put("atr300", 601);				// TA-Lib 
		METRIC_NEEDED_BARS.put("atr1000", 2001);			// TA-Lib 
		METRIC_NEEDED_BARS.put("atrdydx30", 62);			// TA-Lib 
		METRIC_NEEDED_BARS.put("atrdydx100", 202);			// TA-Lib 
		METRIC_NEEDED_BARS.put("atrdydx300", 602);			// TA-Lib 
		METRIC_NEEDED_BARS.put("breakout30", 40);
		METRIC_NEEDED_BARS.put("breakout100", 110);
		METRIC_NEEDED_BARS.put("breakout300", 310);
		METRIC_NEEDED_BARS.put("cci3", 7);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cci10", 21);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cci30", 61);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cci100", 201);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cci300", 601);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo3", 7);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo10", 21);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo30", 61);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo100", 201);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo300", 601);				// TA-Lib 
		METRIC_NEEDED_BARS.put("consecutiveups", 20);
		METRIC_NEEDED_BARS.put("consecutivedowns", 20);
		METRIC_NEEDED_BARS.put("cps", 20);
		METRIC_NEEDED_BARS.put("dvol1ema", 600);
		METRIC_NEEDED_BARS.put("dvol2ema", 300);
		METRIC_NEEDED_BARS.put("dvol3ema", 100);
		METRIC_NEEDED_BARS.put("dvol5ema", 80);
		METRIC_NEEDED_BARS.put("dvol10ema", 30);
		METRIC_NEEDED_BARS.put("dvol25ema", 20);
		METRIC_NEEDED_BARS.put("dvol50ema", 10);
		METRIC_NEEDED_BARS.put("dvol75ema", 10);
		METRIC_NEEDED_BARS.put("dvoldydx2ema", 300);
		METRIC_NEEDED_BARS.put("dvoldydx3ema", 100);
		METRIC_NEEDED_BARS.put("dvoldydx5ema", 50);
		METRIC_NEEDED_BARS.put("gapboll3", 20);
		METRIC_NEEDED_BARS.put("gapboll10", 20);
		METRIC_NEEDED_BARS.put("gapboll30", 40);
		METRIC_NEEDED_BARS.put("gapboll100", 110);
		METRIC_NEEDED_BARS.put("gapboll300", 310);
		METRIC_NEEDED_BARS.put("intradayboll3", 20);
		METRIC_NEEDED_BARS.put("intradayboll10", 20);
		METRIC_NEEDED_BARS.put("intradayboll30", 40);
		METRIC_NEEDED_BARS.put("intradayboll100", 110);
		METRIC_NEEDED_BARS.put("intradayboll300", 310);
		METRIC_NEEDED_BARS.put("macd10_30_8", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("macd30_100_24", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macd100_300_80", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macd300_1000_240", 1801);	// TA-Lib 
		METRIC_NEEDED_BARS.put("macds10_30_8", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("macds30_100_24", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macds100_300_80", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macds300_1000_240", 1801);	// TA-Lib
		METRIC_NEEDED_BARS.put("macdh10_30_8", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("macdh30_100_24", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macdh100_300_80", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macdh300_1000_240", 1801);	// TA-Lib 
		METRIC_NEEDED_BARS.put("mfi3", 7);					// TA-Lib 
		METRIC_NEEDED_BARS.put("mfi10", 21);				// TA-Lib 
		METRIC_NEEDED_BARS.put("mfi30", 61);				// TA-Lib 
		METRIC_NEEDED_BARS.put("mfi100", 201);				// TA-Lib 
		METRIC_NEEDED_BARS.put("mfi300", 601);				// TA-Lib 
		METRIC_NEEDED_BARS.put("mvol3", 20);
		METRIC_NEEDED_BARS.put("mvol10", 20);
		METRIC_NEEDED_BARS.put("mvol30", 40);
		METRIC_NEEDED_BARS.put("mvol100", 110);
		METRIC_NEEDED_BARS.put("mvol300", 310);
		METRIC_NEEDED_BARS.put("mvol1000", 1010);
		METRIC_NEEDED_BARS.put("mvol3000", 3010);
		METRIC_NEEDED_BARS.put("mvoldydx300", 310);
		METRIC_NEEDED_BARS.put("mvoldydx1000", 1010);
		METRIC_NEEDED_BARS.put("ppo3_10", 21);				// TA-Lib 
		METRIC_NEEDED_BARS.put("ppo10_30", 61);				// TA-Lib 
		METRIC_NEEDED_BARS.put("ppo30_100", 201);			// TA-Lib 
		METRIC_NEEDED_BARS.put("ppo100_300", 601);			// TA-Lib 
		METRIC_NEEDED_BARS.put("ppo300_1000", 601);			// TA-Lib 
		METRIC_NEEDED_BARS.put("ppodydx10_30", 62);			// TA-Lib 
		METRIC_NEEDED_BARS.put("ppodydx30_100", 202);		// TA-Lib 
		METRIC_NEEDED_BARS.put("ppodydx100_300", 602);		// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls3", 7);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls10", 21);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls30", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls100", 201);		// TA-Lib 	
		METRIC_NEEDED_BARS.put("pricebolls300", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("psar", 61);					// TA-Lib 
		METRIC_NEEDED_BARS.put("rangepressure30", 35);
		METRIC_NEEDED_BARS.put("rangepressure100", 110);
		METRIC_NEEDED_BARS.put("rangepressure300", 310);
		METRIC_NEEDED_BARS.put("rangepressure1000", 1010);
		METRIC_NEEDED_BARS.put("rangepressure3000", 3010);
		METRIC_NEEDED_BARS.put("rangerank30", 35);
		METRIC_NEEDED_BARS.put("rangerank100", 110);
		METRIC_NEEDED_BARS.put("rangerank300", 310);
		METRIC_NEEDED_BARS.put("rangerank1000", 1010);
		METRIC_NEEDED_BARS.put("rangerank3000", 3010);
		METRIC_NEEDED_BARS.put("rsi3", 7);					// TA-Lib
		METRIC_NEEDED_BARS.put("rsi10", 21);				// TA-Lib
		METRIC_NEEDED_BARS.put("rsi30", 61);				// TA-Lib
		METRIC_NEEDED_BARS.put("rsi100", 201);				// TA-Lib
		METRIC_NEEDED_BARS.put("rsi300", 601);				// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi10_3_3", 21);		// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi30_10_10", 61);		// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi100_30_30", 201);	// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi300_100_100", 601);	// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi10_3_3", 21);		// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi30_10_10", 61);		// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi100_30_30", 201);	// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi300_100_100", 601);	// TA-Lib
		METRIC_NEEDED_BARS.put("stod10_3_3", 21);			// TA-Lib
		METRIC_NEEDED_BARS.put("stod30_10_10", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("stod100_30_30", 201);		// TA-Lib
		METRIC_NEEDED_BARS.put("stod300_100_100", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("stok10_3_3", 21);			// TA-Lib
		METRIC_NEEDED_BARS.put("stok30_10_10", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("stok100_30_30", 201);		// TA-Lib
		METRIC_NEEDED_BARS.put("stok300_100_100", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("timerange2", 300);
		METRIC_NEEDED_BARS.put("timerange5", 750);
		METRIC_NEEDED_BARS.put("timerange8", 1200);
		METRIC_NEEDED_BARS.put("timerange13", 1950);
		METRIC_NEEDED_BARS.put("timerange20", 3000);
		METRIC_NEEDED_BARS.put("timerange30", 4500);
		METRIC_NEEDED_BARS.put("timerange40", 6000);
		METRIC_NEEDED_BARS.put("timerangealpha2", 300);
		METRIC_NEEDED_BARS.put("timerangealpha5", 750);
		METRIC_NEEDED_BARS.put("timerangealpha8", 1200);
		METRIC_NEEDED_BARS.put("timerangealpha13", 1950);
		METRIC_NEEDED_BARS.put("timerangealpha20", 3000);
		METRIC_NEEDED_BARS.put("timerangealpha30", 4500);
		METRIC_NEEDED_BARS.put("timerangealpha40", 6000);
		METRIC_NEEDED_BARS.put("tsf3", 7);					// TA-Lib
		METRIC_NEEDED_BARS.put("tsf10", 21);				// TA-Lib
		METRIC_NEEDED_BARS.put("tsf30", 61);				// TA-Lib
		METRIC_NEEDED_BARS.put("tsf100", 201);				// TA-Lib
		METRIC_NEEDED_BARS.put("tsf300", 601);				// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx30", 62);			// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx100", 202);			// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx300", 602);			// TA-Lib
		METRIC_NEEDED_BARS.put("uo3_10_30", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("uo10_30_100", 201);			// TA-Lib
		METRIC_NEEDED_BARS.put("uo30_100_300", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls3", 7);			// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls10", 21);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls30", 61);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls100", 201);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls300", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr3", 7);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr10", 21);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr30", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr100", 201);		// TA-Lib	
		METRIC_NEEDED_BARS.put("williamsr300", 601);		// TA-Lib	
		// METRIC_NEEDED_BARS.put("cdlhammer", 10);			
		// METRIC_NEEDED_BARS.put("cdldoji", 10);
		// METRIC_NEEDED_BARS.put("cdlmorningstar", 10);

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
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
	public static HashMap<String, ArrayList<String>> METRIC_SETS = new HashMap<String, ArrayList<String>>();
	public static ArrayList<String> OTHER_SELL_METRICS = new ArrayList<String>();
	public static ArrayList<String> STOP_METRICS = new ArrayList<String>();
	public static HashMap<String, Integer> METRIC_NEEDED_BARS = new HashMap<String, Integer>();

	static {
		// METRICS
		METRICS.add("ado3_10");			// Volume
		METRICS.add("ado10_30");		// Volume
		METRICS.add("ado30_100");		// Volume
		METRICS.add("ado100_300");		// Volume
		METRICS.add("adodydx10_30");	// Volume
		METRICS.add("adodydx30_100");	// Volume
		METRICS.add("adodydx100_300");	// Volume
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
		METRICS.add("aroonup3");
		METRICS.add("aroonup10");
		METRICS.add("aroonup30");
		METRICS.add("aroonup100");
		METRICS.add("aroonup300");
		METRICS.add("aroondown3");
		METRICS.add("aroondown10");
		METRICS.add("aroondown30");
		METRICS.add("aroondown100");
		METRICS.add("aroondown300");
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
		METRICS.add("cci2");
		METRICS.add("cci3");
		METRICS.add("cci5");
		METRICS.add("cci10");
		METRICS.add("cci30");
		METRICS.add("cci100");
		METRICS.add("cci300");
		METRICS.add("cmo2");
		METRICS.add("cmo3");
		METRICS.add("cmo5");
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
		METRICS.add("intradayboll1000");
		METRICS.add("intradayboll3000");
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
		METRICS.add("mfi3");				// Volume
		METRICS.add("mfi10");				// Volume
		METRICS.add("mfi30");				// Volume
		METRICS.add("mfi100");				// Volume
		METRICS.add("mfi300");				// Volume
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
		METRICS.add("pricebolls2");
		METRICS.add("pricebolls3");
		METRICS.add("pricebolls5");
		METRICS.add("pricebolls10");
		METRICS.add("pricebolls30");
		METRICS.add("pricebolls100");
		METRICS.add("pricebolls300");
		METRICS.add("psar");
		METRICS.add("rangepressure5");
		METRICS.add("rangepressure10");
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
		METRICS.add("rsi2");
		METRICS.add("rsi3");
		METRICS.add("rsi5");
		METRICS.add("rsi10");
		METRICS.add("rsi30");
		METRICS.add("rsi100");
		METRICS.add("rsi300");
		METRICS.add("stodrsi7_2_2");
		METRICS.add("stodrsi10_3_3");
		METRICS.add("stodrsi30_10_10");
		METRICS.add("stodrsi100_30_30");
		METRICS.add("stodrsi300_100_100");
		METRICS.add("stokrsi7_2_2");
		METRICS.add("stokrsi10_3_3");
		METRICS.add("stokrsi30_10_10");
		METRICS.add("stokrsi100_30_30");
		METRICS.add("stokrsi300_100_100");
		METRICS.add("stod7_2_2");
		METRICS.add("stod10_3_3");
		METRICS.add("stod30_10_10");
		METRICS.add("stod100_30_30");
		METRICS.add("stod300_100_100");
		METRICS.add("stok7_2_2");
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
		// METRICS.add("timerange40");
		METRICS.add("timerangealpha2");
		METRICS.add("timerangealpha5");
		METRICS.add("timerangealpha8");
		METRICS.add("timerangealpha13");
		METRICS.add("timerangealpha20");
		METRICS.add("timerangealpha30");
		// METRICS.add("timerangealpha40");
		METRICS.add("tsf3");
		METRICS.add("tsf10");
		METRICS.add("tsf30");
		METRICS.add("tsf100");
		METRICS.add("tsf300");
		METRICS.add("tsf1000");
		METRICS.add("tsfdydx30");
		METRICS.add("tsfdydx100");
		METRICS.add("tsfdydx300");
		METRICS.add("tsfdydx1000");
		METRICS.add("uo1_4_10");
		METRICS.add("uo2_7_20");
		METRICS.add("uo3_10_30");
		METRICS.add("uo10_30_100");
		METRICS.add("uo30_100_300");
		METRICS.add("volumebolls3");			// Volume
		METRICS.add("volumebolls10");			// Volume
		METRICS.add("volumebolls30");			// Volume
		METRICS.add("volumebolls100");			// Volume
		METRICS.add("volumebolls300");			// Volume	
		METRICS.add("williamsr2");
		METRICS.add("williamsr3");
		METRICS.add("williamsr5");
		METRICS.add("williamsr10");
		METRICS.add("williamsr30");
		METRICS.add("williamsr100");
		METRICS.add("williamsr300");
////		METRICS.add("cdl2crows");			// 0
////		METRICS.add("cdl3blackcrows");		// 0
////		METRICS.add("cdl3inside");			// 2
////		METRICS.add("cdl3linestrike");		// 3
////		METRICS.add("cdl3outside");			// 9
////		METRICS.add("cdl3starsinsouth");	// 0
////		METRICS.add("cdl3whitesoldiers");	// 19
////		METRICS.add("cdlabandonedbaby");	// 0
////		METRICS.add("cdladvanceblock");		// 0
//		METRICS.add("cdlbelthold");			// 3563
////		METRICS.add("cdlbreakaway");		// 0
//		METRICS.add("cdlclosingmarubozu");	// 2222
////		METRICS.add("cdlconcealbabyswall");	// 0
////		METRICS.add("cdlcounterattack");	// 0
////		METRICS.add("cdldarkcloudcover");	// 0
//		METRICS.add("cdldoji");				// 8164
//		METRICS.add("cdldragonflydoji");	// 1342
////		METRICS.add("cdlengulfing");		// 23
////		METRICS.add("cdleveningdojistar");	// 0
////		METRICS.add("cdleveningstar");		// 0
////		METRICS.add("cdlgapsidesidewhite");	// 9
//		METRICS.add("cdlgravestonedoji");	// 1491
//		METRICS.add("cdlhammer");			// 1499
////		METRICS.add("cdlhangingman");		// 0
////		METRICS.add("cdlharami");			// 48
////		METRICS.add("cdlharamicross");		// 27
//		METRICS.add("cdlhignwave");			// 3404
//		METRICS.add("cdlhikkake");			// 2786
////		METRICS.add("cdlhikkakemod");		// 76
////		METRICS.add("cdlhomingpigeon");		// 13
////		METRICS.add("cdlidentical3crows");	// 0
////		METRICS.add("cdlinneck");			// 0
////		METRICS.add("cdlinvertedhammer");	// 102
////		METRICS.add("cdlkicking");			// 0
////		METRICS.add("cdlkickingbylength");	// 0
////		METRICS.add("cdlladderbottom");		// 6
//		METRICS.add("cdllongleggeddoji");	// 8051
//		METRICS.add("cdllongline");			// 3373
////		METRICS.add("cdlmarubozu");			// 0
//		METRICS.add("cdlmatchinglow");		// 878
////		METRICS.add("cdlmathold");			// 0
////		METRICS.add("cdlmorningstar");		// 1
////		METRICS.add("cdlmorningdojistar");	// 0
////		METRICS.add("cdlonneck");			// 0
////		METRICS.add("cdlpiercing");			// 0
//		METRICS.add("cdlrickshawman");		// 5532
////		METRICS.add("cdlrisefall3methods");	// 0
////		METRICS.add("cdlseperatinglines");	// 157
////		METRICS.add("cdlshootingstar");		// 0
//		METRICS.add("cdlshortline");		// 5517
//		METRICS.add("cdlspinningtop");		// 5599
////		METRICS.add("cdlstalledpattern");	// 0
////		METRICS.add("cdlsticksandwich");	// 11
//		METRICS.add("cdltakuri");			// 1260
////		METRICS.add("cdltasukigap");		// 0
////		METRICS.add("cdlthursting");		// 0
////		METRICS.add("cdltristar");			// 13
////		METRICS.add("cdlunique3river");		// 1
////		METRICS.add("cdlupsidegap2crows");	// 0
////		METRICS.add("cdlxsidegap3methods"); // 0

		ArrayList<String> METRIC_SET_23_12752 = new ArrayList<String>();
		METRIC_SET_23_12752.add("adx1000");
		METRIC_SET_23_12752.add("adxrdydx30");
		METRIC_SET_23_12752.add("aroonup300");
		METRIC_SET_23_12752.add("macdh300_1000_240");	
		METRIC_SET_23_12752.add("stod10_3_3");
		METRIC_SET_23_12752.add("stodrsi300_100_100");
		METRIC_SET_23_12752.add("tsf1000");	
		METRIC_SET_23_12752.add("tsfdydx100");
		METRIC_SET_23_12752.add("tsfdydx1000");
		METRIC_SET_23_12752.add("uo1_4_10");		
		METRIC_SET_23_12752.add("uo3_10_30");	
		METRIC_SET_23_12752.add("williamsr10");
		
		ArrayList<String> METRIC_SET_28_2990 = new ArrayList<String>();
		METRIC_SET_28_2990.add("adxdydx100");
		METRIC_SET_28_2990.add("aroonoscillator10");
		METRIC_SET_28_2990.add("aroonup3");
		METRIC_SET_28_2990.add("aroonup30");	
		METRIC_SET_28_2990.add("pricebolls3");
		METRIC_SET_28_2990.add("rangerank30");
		METRIC_SET_28_2990.add("stodrsi30_10_10");	
		METRIC_SET_28_2990.add("stodrsi100_30_30");
		METRIC_SET_28_2990.add("stokrsi300_100_100");
		METRIC_SET_28_2990.add("tsfdydx100");
		METRIC_SET_28_2990.add("uo1_4_10");		
		METRIC_SET_28_2990.add("uo2_7_20");	
	
		ArrayList<String> METRIC_SET_29_4190 = new ArrayList<String>(); // 2/18/2017
		METRIC_SET_29_4190.add("aroonoscillator10");
		METRIC_SET_29_4190.add("cdlclosingmarubozu");
		METRIC_SET_29_4190.add("cmo10");
		METRIC_SET_29_4190.add("ppo10_30");	
		METRIC_SET_29_4190.add("stod30_10_10");
		METRIC_SET_29_4190.add("stokrsi100_30_30");
		METRIC_SET_29_4190.add("timerangealpha30");	
		METRIC_SET_29_4190.add("timerange5");
		METRIC_SET_29_4190.add("tsfdydx1000");
		METRIC_SET_29_4190.add("uo1_4_10");		
		METRIC_SET_29_4190.add("uo30_100_300");
		METRIC_SET_29_4190.add("williamsr5");	
		
		ArrayList<String> METRIC_SET_30_3394 = new ArrayList<String>(); // 2/25/2017
		METRIC_SET_30_3394.add("adxrdydx30");
		METRIC_SET_30_3394.add("aroondown100");
		METRIC_SET_30_3394.add("cdldragonflydoji");
		METRIC_SET_30_3394.add("cmo10");
		METRIC_SET_30_3394.add("macd10_30_8");
		METRIC_SET_30_3394.add("pricebolls2");
		METRIC_SET_30_3394.add("stokrsi100_30_30");
		METRIC_SET_30_3394.add("uo1_4_10");
		METRIC_SET_30_3394.add("uo10_30_100");
		METRIC_SET_30_3394.add("uo30_100_300");
		METRIC_SET_30_3394.add("williamsr2");
		METRIC_SET_30_3394.add("williamsr30");
		
		ArrayList<String> METRIC_SET_31_5828 = new ArrayList<String>(); // 2013 Base Dates for 2H Bars
		METRIC_SET_31_5828.add("adxrdydx30");
		METRIC_SET_31_5828.add("aroonup10");
		METRIC_SET_31_5828.add("atr3");
		METRIC_SET_31_5828.add("cci3");
		METRIC_SET_31_5828.add("dvol1ema");
		METRIC_SET_31_5828.add("dvol75ema");
		METRIC_SET_31_5828.add("macdh300_1000_240");
		METRIC_SET_31_5828.add("ppo10_30");
		METRIC_SET_31_5828.add("stokrsi30_10_10");
		METRIC_SET_31_5828.add("timerangealpha5");
		METRIC_SET_31_5828.add("tsf300");
		METRIC_SET_31_5828.add("tsf1000");
		
		ArrayList<String> METRIC_SET_32_110504 = new ArrayList<String>(); // ES C 1H Test 32
		METRIC_SET_32_110504.add("ado30_100");
		METRIC_SET_32_110504.add("adx300");
		METRIC_SET_32_110504.add("adxrdydx300");
		METRIC_SET_32_110504.add("cmo2");
		METRIC_SET_32_110504.add("dvol50ema");
		METRIC_SET_32_110504.add("intradayboll300");
		METRIC_SET_32_110504.add("mfi10");
		METRIC_SET_32_110504.add("ppodydx100_300");
		METRIC_SET_32_110504.add("timerange2");
		METRIC_SET_32_110504.add("timerangealpha2");
		METRIC_SET_32_110504.add("tsf3");
		METRIC_SET_32_110504.add("uo30_100_300");
		
		ArrayList<String> METRIC_SET_33_45060 = new ArrayList<String>(); // ZN C 1H Test 33
		METRIC_SET_33_45060.add("adxr10");
		METRIC_SET_33_45060.add("adxr30");
		METRIC_SET_33_45060.add("adxdydx30");
		METRIC_SET_33_45060.add("stokrsi7_2_2");
		METRIC_SET_33_45060.add("cci30");
		METRIC_SET_33_45060.add("macds10_30_8");
		METRIC_SET_33_45060.add("mvol10");
		METRIC_SET_33_45060.add("pricebolls300");
		METRIC_SET_33_45060.add("timerangealpha2");
		METRIC_SET_33_45060.add("tsf300");
		METRIC_SET_33_45060.add("uo1_4_10");
		METRIC_SET_33_45060.add("volumebolls3");
		
		ArrayList<String> METRIC_SET_34_22962 = new ArrayList<String>(); // CL C 1H Test 34
		METRIC_SET_34_22962.add("adx3");
		METRIC_SET_34_22962.add("adxdydx100");
		METRIC_SET_34_22962.add("adxrdydx30");
		METRIC_SET_34_22962.add("aroonup100");
		METRIC_SET_34_22962.add("mfi30");
		METRIC_SET_34_22962.add("pricebolls2");
		METRIC_SET_34_22962.add("stod7_2_2");
		METRIC_SET_34_22962.add("stodrsi7_2_2");
		METRIC_SET_34_22962.add("timerange20");
		METRIC_SET_34_22962.add("timerangealpha8");
		METRIC_SET_34_22962.add("tsfdydx30");
		METRIC_SET_34_22962.add("uo1_4_10");
		
		ArrayList<String> METRIC_SET_35_2299 = new ArrayList<String>(); // ZN C 2H Test 35
		METRIC_SET_35_2299.add("adxr10");
		METRIC_SET_35_2299.add("aroondown10");
		METRIC_SET_35_2299.add("aroonup30");
		METRIC_SET_35_2299.add("atr10");
		METRIC_SET_35_2299.add("cci300");
		METRIC_SET_35_2299.add("cmo300");
		METRIC_SET_35_2299.add("mfi30");
		METRIC_SET_35_2299.add("ppodydx30_100");
		METRIC_SET_35_2299.add("rsi2");
		METRIC_SET_35_2299.add("uo2_7_20");
		METRIC_SET_35_2299.add("volumebolls3");
		METRIC_SET_35_2299.add("williamsr30");
		
		ArrayList<String> METRIC_SET_35_10718 = new ArrayList<String>(); // ZN C 2H Test 35
		METRIC_SET_35_10718.add("adxr10");
		METRIC_SET_35_10718.add("aroondown10");
		METRIC_SET_35_10718.add("atr30");
		METRIC_SET_35_10718.add("cci5");
		METRIC_SET_35_10718.add("cci300");
		METRIC_SET_35_10718.add("dvol10ema");
		METRIC_SET_35_10718.add("macdh300_1000_240");
		METRIC_SET_35_10718.add("mfi30");
		METRIC_SET_35_10718.add("ppodydx30_100");
		METRIC_SET_35_10718.add("rsi2");
		METRIC_SET_35_10718.add("tsf100");
		METRIC_SET_35_10718.add("volumebolls3");
		
		ArrayList<String> METRIC_SET_36_2586 = new ArrayList<String>(); // BTC_ETH 1H Test 36
		METRIC_SET_36_2586.add("adxr30");
		METRIC_SET_36_2586.add("atrdydx100");
		METRIC_SET_36_2586.add("cci5");
		METRIC_SET_36_2586.add("intradayboll10");
		METRIC_SET_36_2586.add("rangepressure5");
		METRIC_SET_36_2586.add("rangerank3000");
		METRIC_SET_36_2586.add("rsi5");
		METRIC_SET_36_2586.add("rsi300");
		METRIC_SET_36_2586.add("tsfdydx100");
		METRIC_SET_36_2586.add("uo1_4_10");
		METRIC_SET_36_2586.add("uo2_7_20");
		METRIC_SET_36_2586.add("volumebolls10");
		
		ArrayList<String> METRIC_SET_37_1651 = new ArrayList<String>(); // BTC_ETH 15M Test 37
		METRIC_SET_37_1651.add("aroonoscillator100");
		METRIC_SET_37_1651.add("cmo3");
		METRIC_SET_37_1651.add("cmo10");
		METRIC_SET_37_1651.add("intradayboll30");
		METRIC_SET_37_1651.add("macdh10_30_8");
		METRIC_SET_37_1651.add("rangerank3000");
		METRIC_SET_37_1651.add("rsi300");
		METRIC_SET_37_1651.add("tsf300");
		METRIC_SET_37_1651.add("uo1_4_10");
		METRIC_SET_37_1651.add("uo3_10_30");
		METRIC_SET_37_1651.add("williamsr2");
		METRIC_SET_37_1651.add("williamsr100");
		
		ArrayList<String> METRIC_SET_38_2005 = new ArrayList<String>(); // BTC_XMR 1H Test 38
		METRIC_SET_38_2005.add("aroondown30");
		METRIC_SET_38_2005.add("cci30");
		METRIC_SET_38_2005.add("intradayboll30");
		METRIC_SET_38_2005.add("intradayboll1000");
		METRIC_SET_38_2005.add("ppo10_30");
		METRIC_SET_38_2005.add("rangerank300");
		METRIC_SET_38_2005.add("stod10_3_3");
		METRIC_SET_38_2005.add("stokrsi100_30_30");
		METRIC_SET_38_2005.add("uo1_4_10");
		METRIC_SET_38_2005.add("uo2_7_20");
		METRIC_SET_38_2005.add("williamsr3");
		METRIC_SET_38_2005.add("volumebolls30");
		
		ArrayList<String> METRIC_SET_39_2963 = new ArrayList<String>(); // BTC_ETH 15M Test 39 ExtremeBar x4
		METRIC_SET_39_2963.add("dvol3ema");
		METRIC_SET_39_2963.add("mfi300");
		METRIC_SET_39_2963.add("ppo30_100");
		METRIC_SET_39_2963.add("ppodydx30_100");
		METRIC_SET_39_2963.add("rangerank3000");
		METRIC_SET_39_2963.add("rsi10");
		METRIC_SET_39_2963.add("tsfdydx1000");
		METRIC_SET_39_2963.add("uo1_4_10");
		METRIC_SET_39_2963.add("williamsr2");
		METRIC_SET_39_2963.add("williamsr3");
		METRIC_SET_39_2963.add("williamsr5");
		METRIC_SET_39_2963.add("williamsr10");
		
		ArrayList<String> METRIC_SET_40_4508 = new ArrayList<String>(); // EUR.USD 2H Test 40 ExtremeBar x4
		METRIC_SET_40_4508.add("adx1000");
		METRIC_SET_40_4508.add("aroonoscillator300");
		METRIC_SET_40_4508.add("atr3");
		METRIC_SET_40_4508.add("macdh30_100_24");
		METRIC_SET_40_4508.add("macdh300_1000_240");
		METRIC_SET_40_4508.add("ppo3_10");
		METRIC_SET_40_4508.add("ppo10_30");
		METRIC_SET_40_4508.add("uo1_4_10");
		METRIC_SET_40_4508.add("rsi10");
		METRIC_SET_40_4508.add("stodrsi100_30_30");
		METRIC_SET_40_4508.add("stodrsi300_100_100");
		METRIC_SET_40_4508.add("timerange30");
		
		METRIC_SETS.put("Test 23.12752", METRIC_SET_23_12752);
		METRIC_SETS.put("Test 28.2990", METRIC_SET_28_2990);
		METRIC_SETS.put("Test 29.4190", METRIC_SET_29_4190);
		METRIC_SETS.put("Test 30.3394", METRIC_SET_30_3394);
		METRIC_SETS.put("Test 31.5828", METRIC_SET_31_5828);
		METRIC_SETS.put("Test 32.110504", METRIC_SET_32_110504);
		METRIC_SETS.put("Test 33.45060", METRIC_SET_33_45060);
		METRIC_SETS.put("Test 34.22962", METRIC_SET_34_22962);
		METRIC_SETS.put("Test 35.2299", METRIC_SET_35_2299);
		METRIC_SETS.put("Test 35.10718", METRIC_SET_35_10718);
		METRIC_SETS.put("Test 36.2586", METRIC_SET_36_2586);
		METRIC_SETS.put("Test 37.1651", METRIC_SET_37_1651);
		METRIC_SETS.put("Test 38.2005", METRIC_SET_38_2005);
		METRIC_SETS.put("Test 39.2963", METRIC_SET_39_2963);
		METRIC_SETS.put("Test 40.4508", METRIC_SET_40_4508);
		
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
		METRIC_NEEDED_BARS.put("aroonup3", 7);				// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonup10", 21);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonup30", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonup100", 201);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroonup300", 601);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroondown3", 7);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroondown10", 21);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroondown30", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("aroondown100", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("aroondown300", 601);		// TA-Lib 
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
		METRIC_NEEDED_BARS.put("cci2", 57);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cci3", 7);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cci5", 11);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cci10", 21);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cci30", 61);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cci100", 201);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cci300", 601);				// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo2", 5);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo3", 7);					// TA-Lib 
		METRIC_NEEDED_BARS.put("cmo5", 11);					// TA-Lib 
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
		METRIC_NEEDED_BARS.put("intradayboll1000", 1010);
		METRIC_NEEDED_BARS.put("intradayboll3000", 3010);
		METRIC_NEEDED_BARS.put("macd10_30_8", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("macd30_100_24", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macd100_300_80", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macd300_1000_240", 2001);	// TA-Lib 
		METRIC_NEEDED_BARS.put("macds10_30_8", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("macds30_100_24", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macds100_300_80", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macds300_1000_240", 2001);	// TA-Lib
		METRIC_NEEDED_BARS.put("macdh10_30_8", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("macdh30_100_24", 201);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macdh100_300_80", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("macdh300_1000_240", 2001);	// TA-Lib 
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
		METRIC_NEEDED_BARS.put("pricebolls2", 5);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls3", 7);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls5", 11);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls10", 21);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls30", 61);			// TA-Lib 
		METRIC_NEEDED_BARS.put("pricebolls100", 201);		// TA-Lib 	
		METRIC_NEEDED_BARS.put("pricebolls300", 601);		// TA-Lib 
		METRIC_NEEDED_BARS.put("psar", 61);					// TA-Lib 
		METRIC_NEEDED_BARS.put("rangepressure5", 10);
		METRIC_NEEDED_BARS.put("rangepressure10", 15);
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
		METRIC_NEEDED_BARS.put("rsi2", 5);					// TA-Lib
		METRIC_NEEDED_BARS.put("rsi3", 7);					// TA-Lib
		METRIC_NEEDED_BARS.put("rsi5", 11);					// TA-Lib
		METRIC_NEEDED_BARS.put("rsi10", 21);				// TA-Lib
		METRIC_NEEDED_BARS.put("rsi30", 61);				// TA-Lib
		METRIC_NEEDED_BARS.put("rsi100", 201);				// TA-Lib
		METRIC_NEEDED_BARS.put("rsi300", 601);				// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi7_2_2", 15);			// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi10_3_3", 21);		// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi30_10_10", 61);		// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi100_30_30", 201);	// TA-Lib
		METRIC_NEEDED_BARS.put("stodrsi300_100_100", 601);	// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi7_2_2", 15);			// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi10_3_3", 21);		// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi30_10_10", 61);		// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi100_30_30", 201);	// TA-Lib
		METRIC_NEEDED_BARS.put("stokrsi300_100_100", 601);	// TA-Lib
		METRIC_NEEDED_BARS.put("stod7_2_2", 15);			// TA-Lib
		METRIC_NEEDED_BARS.put("stod10_3_3", 21);			// TA-Lib
		METRIC_NEEDED_BARS.put("stod30_10_10", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("stod100_30_30", 201);		// TA-Lib
		METRIC_NEEDED_BARS.put("stod300_100_100", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("stok7_2_2", 15);			// TA-Lib
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
		METRIC_NEEDED_BARS.put("tsf1000", 2001);			// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx30", 62);			// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx100", 202);			// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx300", 602);			// TA-Lib
		METRIC_NEEDED_BARS.put("tsfdydx1000", 2002);		// TA-Lib
		METRIC_NEEDED_BARS.put("uo1_4_10", 21);				// TA-Lib
		METRIC_NEEDED_BARS.put("uo2_7_20", 41);				// TA-Lib
		METRIC_NEEDED_BARS.put("uo3_10_30", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("uo10_30_100", 201);			// TA-Lib
		METRIC_NEEDED_BARS.put("uo30_100_300", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls3", 7);			// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls10", 21);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls30", 61);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls100", 201);		// TA-Lib
		METRIC_NEEDED_BARS.put("volumebolls300", 601);		// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr2", 5);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr3", 7);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr5", 11);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr10", 21);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr30", 61);			// TA-Lib
		METRIC_NEEDED_BARS.put("williamsr100", 201);		// TA-Lib	
		METRIC_NEEDED_BARS.put("williamsr300", 601);		// TA-Lib	
		METRIC_NEEDED_BARS.put("cdl2crows", 10);
		METRIC_NEEDED_BARS.put("cdl3blackcrows", 10);
		METRIC_NEEDED_BARS.put("cdl3inside", 10);
		METRIC_NEEDED_BARS.put("cdl3linestrike", 10);
		METRIC_NEEDED_BARS.put("cdl3outside", 10);
		METRIC_NEEDED_BARS.put("cdl3starsinsouth", 10);
		METRIC_NEEDED_BARS.put("cdl3whitesoldiers", 10);
		METRIC_NEEDED_BARS.put("cdlabandonedbaby", 10);
		METRIC_NEEDED_BARS.put("cdladvanceblock", 10);
		METRIC_NEEDED_BARS.put("cdlbelthold", 10);
		METRIC_NEEDED_BARS.put("cdlbreakaway", 10);
		METRIC_NEEDED_BARS.put("cdlclosingmarubozu", 20);
		METRIC_NEEDED_BARS.put("cdlconcealbabyswall", 10);
		METRIC_NEEDED_BARS.put("cdlcounterattack", 10);
		METRIC_NEEDED_BARS.put("cdldarkcloudcover", 10);
		METRIC_NEEDED_BARS.put("cdldoji", 10);
		METRIC_NEEDED_BARS.put("cdldragonflydoji", 10);
		METRIC_NEEDED_BARS.put("cdlengulfing", 10);
		METRIC_NEEDED_BARS.put("cdleveningdojistar", 10);
		METRIC_NEEDED_BARS.put("cdleveningstar", 10);
		METRIC_NEEDED_BARS.put("cdlgapsidesidewhite", 10);
		METRIC_NEEDED_BARS.put("cdlgravestonedoji", 10);
		METRIC_NEEDED_BARS.put("cdlhammer", 10);
		METRIC_NEEDED_BARS.put("cdlhangingman", 10);
		METRIC_NEEDED_BARS.put("cdlharami", 10);
		METRIC_NEEDED_BARS.put("cdlharamicross", 10);
		METRIC_NEEDED_BARS.put("cdlhignwave", 10);
		METRIC_NEEDED_BARS.put("cdlhikkake", 10);
		METRIC_NEEDED_BARS.put("cdlhikkakemod", 10);
		METRIC_NEEDED_BARS.put("cdlhomingpigeon", 10);
		METRIC_NEEDED_BARS.put("cdlidentical3crows", 10);
		METRIC_NEEDED_BARS.put("cdlinneck", 10);
		METRIC_NEEDED_BARS.put("cdlinvertedhammer", 10);
		METRIC_NEEDED_BARS.put("cdlkicking", 10);
		METRIC_NEEDED_BARS.put("cdlkickingbylength", 10);
		METRIC_NEEDED_BARS.put("cdlladderbottom", 10);
		METRIC_NEEDED_BARS.put("cdllongleggeddoji", 10);
		METRIC_NEEDED_BARS.put("cdllongline", 10);
		METRIC_NEEDED_BARS.put("cdlmarubozu", 10);
		METRIC_NEEDED_BARS.put("cdlmatchinglow", 10);
		METRIC_NEEDED_BARS.put("cdlmathold", 10);
		METRIC_NEEDED_BARS.put("cdlmorningstar", 10);
		METRIC_NEEDED_BARS.put("cdlmorningdojistar", 10);
		METRIC_NEEDED_BARS.put("cdlonneck", 10);
		METRIC_NEEDED_BARS.put("cdlpiercing", 10);
		METRIC_NEEDED_BARS.put("cdlrickshawman", 10);
		METRIC_NEEDED_BARS.put("cdlrisefall3methods", 10);
		METRIC_NEEDED_BARS.put("cdlseperatinglines", 10);
		METRIC_NEEDED_BARS.put("cdlshootingstar", 10);
		METRIC_NEEDED_BARS.put("cdlshortline", 10);
		METRIC_NEEDED_BARS.put("cdlspinningtop", 10);
		METRIC_NEEDED_BARS.put("cdlstalledpattern", 10);
		METRIC_NEEDED_BARS.put("cdlsticksandwich", 10);
		METRIC_NEEDED_BARS.put("cdltakuri", 10);
		METRIC_NEEDED_BARS.put("cdltasukigap", 10);
		METRIC_NEEDED_BARS.put("cdlthursting", 10);
		METRIC_NEEDED_BARS.put("cdltristar", 10);
		METRIC_NEEDED_BARS.put("cdlunique3river", 10);
		METRIC_NEEDED_BARS.put("cdlupsidegap2crows", 10);
		METRIC_NEEDED_BARS.put("cdlxsidegap3methods", 10);
		
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
	
	public static void setMetricSet(String metricSet) {
		METRICS.clear();
		METRICS.addAll(METRIC_SETS.get(metricSet));
	}
}
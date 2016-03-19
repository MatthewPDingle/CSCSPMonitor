package constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Constants {

	public static String URL = "jdbc:postgresql://localhost:5432/stocks";
	public static String USERNAME = "postgres";
	public static String PASSWORD = "graham23";

	public static enum BAR_SIZE {
		BAR_15S, BAR_30S, BAR_1M, BAR_3M, BAR_5M, BAR_10M, BAR_15M, BAR_30M, BAR_1H, BAR_2H, BAR_4H, BAR_6H, BAR_8H, BAR_12H, BAR_1D
	};

	public static String BAR_TABLE = "bar";
	public static String METRICS_TABLE = "metrics";
	public static String INDEXLIST_TABLE = "indexlist"; // NYSE, Nasdaq, ETF,
														// Index, Bitcoin
	public static String SECTORANDINDUSTRY_TABLE = "sectorandindustry";
	public static String REALTIMESYMBOLS_TABLE = "realtimesymbols";

	public static int NUM_BARS_NEEDED_FOR_REALTIME_DOWNLOAD_METRIC_CALC = 101;

	// Datasource URLs. These occasionally break and need to be fixed or
	// replaced.
	public static String YAHOO_NYSE_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5ENYA&c="; // "c"
																								// parameter
																								// =
																								// a
																								// page
																								// number
																								// (0+)
																								// //
																								// Broken
																								// as
																								// of
																								// 7/2/2015
	public static String NYSE_SYMBOL_URL = "http://www1.nyse.com/indexes/nyaindex.csv";
	public static String YAHOO_NASDAQ_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5EIXIC&c=";
	public static String YAHOO_DJIA_SYMBOL_URL = "http://finance.yahoo.com/q/cp?s=%5EDJI&c=";
	public static String YAHOO_ETF_SYMBOL_URL = "http://finance.yahoo.com/etf/lists/?mod_id=mediaquotesetf&tab=tab3&scol=volint&stype=desc&rcnt=100";
	public static String WIKI_ETF_SYMBOL_URL = "http://en.wikipedia.org/wiki/List_of_American_exchange-traded_funds";
	public static String YAHOO_REALTIME_QUOTE_URL = "http://download.finance.yahoo.com/d/quotes.csv?f=sk1d1c6ohgv&e=.csv&s=";
	public static String WIKI_SP500_SYMBOL_URL = "http://en.wikipedia.org/wiki/List_of_S%26P_500_companies"; // Broken
																												// as
																												// of
																												// 7/2/2015
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
	public static ArrayList<String> METRICS_NB = new ArrayList<String>(); // These
																			// tend
																			// to
																			// be
																			// low
																			// info
																			// gain
																			// ones
																			// in
																			// attribute
																			// selection
	public static ArrayList<String> METRICS_RF = new ArrayList<String>(); // These
																			// tend
																			// to
																			// be
																			// the
																			// high
																			// info
																			// gain
																			// ones
																			// in
																			// attribute
																			// selection
	public static ArrayList<String> METRICS_B2 = new ArrayList<String>(); // GainRatioAttributeEval
																			// <=
																			// .001
	public static ArrayList<String> METRICS_B3 = new ArrayList<String>(); // GainRatioAttributeEval
																			// <=
																			// .000
	public static ArrayList<String> METRICS_B4 = new ArrayList<String>(); // GainRatioAttributeEval
																			// >=
																			// .002
	public static ArrayList<String> METRICS_B5 = new ArrayList<String>(); // GainRatioAttributeEval
																			// >=
																			// .020
	public static ArrayList<String> METRICS_B6 = new ArrayList<String>(); // GainRatioAttributeEval
																			// >=
																			// .030
	public static ArrayList<String> METRICS_B7 = new ArrayList<String>(); // GainRatioAttributeEval
																			// >=
																			// .040
	public static ArrayList<String> METRICS_B8 = new ArrayList<String>(); // GainRatioAttributeEval
																			// >=
																			// .050
	public static ArrayList<String> METRICS_B9 = new ArrayList<String>(); // PrincipalComponents
																			// Top
																			// 6
	public static ArrayList<String> METRICS_B10 = new ArrayList<String>(); // PrincipalComponents
																			// Bottom
																			// 6
	public static ArrayList<String> METRICS_B11 = new ArrayList<String>(); // For
																			// RandomForest
																			// 10587
																			// Testing
																			// on
																			// EUR.USD
	public static ArrayList<String> METRICS_B12 = new ArrayList<String>(); // For
																			// 5M
																			// EUR.USD
																			// Testing.
																			// Highest
																			// 24
																			// InfoGain
	public static ArrayList<String> METRICS_B13 = new ArrayList<String>(); // For
																			// 5M
																			// EUR.USD
																			// Testing.
																			// Highest
																			// 30
																			// InfoGain
	public static ArrayList<String> METRICS_B14 = new ArrayList<String>(); // For
																			// 30M
																			// EUR.USD,
																			// EUR.GBP,
																			// GBP.USD
																			// Testing.
																			// Highest
																			// 28
																			// InfoGain
																			// +
																			// Time
																			// &
																			// Hour
	public static ArrayList<String> METRICS_B15 = new ArrayList<String>(); // For
																			// 5M
																			// EUR.USD,
																			// EUR.GBP,
																			// GBP.USD.
																			// .4/.4
																			// Unbouonded.
																			// Highest
																			// 30
																			// InfoGain
																			// +
																			// Time,
																			// Hour,
																			// Symbol
	public static ArrayList<String> METRICS_B16 = new ArrayList<String>(); // For
																			// 5M
																			// EUR.USD,
																			// EUR.GBP,
																			// GBP.USD.
																			// .1/.1
																			// Unbounded.
																			// Highest
																			// 30
																			// InfoGain
																			// +
																			// Time,
																			// Hour,
																			// Symbol
	public static ArrayList<String> OTHER_SELL_METRICS = new ArrayList<String>();
	public static ArrayList<String> STOP_METRICS = new ArrayList<String>();
	public static HashMap<String, Integer> METRIC_NEEDED_BARS = new HashMap<String, Integer>();

	static {
		// METRICS
		// METRICS.add("ado3_10");
		// METRICS.add("ado10_30");
		// METRICS.add("ado30_100");
		// METRICS.add("adodydx10_30");
		// METRICS.add("adodydx30_100");
		METRICS.add("adx3");
		METRICS.add("adx10");
		METRICS.add("adx30");
		METRICS.add("adx100");
		METRICS.add("adx300");
		METRICS.add("adxdydx30");
		METRICS.add("adxdydx100");
		METRICS.add("adxdydx300");
		METRICS.add("adxr3");
		METRICS.add("adxr10");
		METRICS.add("adxr30");
		METRICS.add("adxr100");
		METRICS.add("adxr300");
		METRICS.add("adxrdydx30");
		METRICS.add("adxrdydx100");
		METRICS.add("adxrdydx300");
		METRICS.add("aroonoscillator10");
		METRICS.add("aroonoscillator25");
		METRICS.add("aroonoscillator50");
		METRICS.add("atr10");
		METRICS.add("atr20");
		METRICS.add("atr40");
		METRICS.add("atr60");
		METRICS.add("atr120");
		METRICS.add("atrdydx40");
		METRICS.add("atrdydx60");
		METRICS.add("breakout20");
		METRICS.add("breakout50");
		METRICS.add("breakout100");
		METRICS.add("breakout200");
		METRICS.add("cci5");
		METRICS.add("cci10");
		METRICS.add("cci20");
		METRICS.add("cci40");
		METRICS.add("cci60");
		METRICS.add("cmo3");
		METRICS.add("cmo10");
		METRICS.add("cmo30");
		METRICS.add("cmo100");
		METRICS.add("cmo300");
		// METRICS.add("consecutiveups");
		// METRICS.add("consecutivedowns");
		// METRICS.add("cps");
		METRICS.add("dvol3ema");
		METRICS.add("dvol5ema");
		METRICS.add("dvol10ema");
		METRICS.add("dvol25ema");
		METRICS.add("dvol50ema");
		METRICS.add("dvol75ema");
		// METRICS.add("dvoldydx5ema");
		// METRICS.add("dvoldydx10ema");
		// METRICS.add("gapboll10");
		// METRICS.add("gapboll20");
		// METRICS.add("gapboll50");
		METRICS.add("intradayboll10");
		METRICS.add("intradayboll20");
		METRICS.add("intradayboll50");
		METRICS.add("macd6_13_5");
		METRICS.add("macd12_26_9");
		METRICS.add("macd20_40_9");
		METRICS.add("macdsignal6_13_5");
		METRICS.add("macdsignal12_26_9");
		METRICS.add("macdsignal20_40_9");
		METRICS.add("macdhistory6_13_5");
		METRICS.add("macdhistory12_26_9");
		METRICS.add("macdhistory20_40_9");
		// METRICS.add("mfi4");
		// METRICS.add("mfi8");
		// METRICS.add("mfi12");
		// METRICS.add("mfi16");
		// METRICS.add("mfi30");
		// METRICS.add("mfi60");
		METRICS.add("mvol10");
		METRICS.add("mvol20");
		METRICS.add("mvol50");
		METRICS.add("mvol100");
		METRICS.add("mvol200");
		METRICS.add("mvol500");
		// METRICS.add("mvoldydx100");
		// METRICS.add("mvoldydx200");
		METRICS.add("ppo3_10");
		METRICS.add("ppo10_30");
		METRICS.add("ppo30_100");
		METRICS.add("ppo100_300");
		METRICS.add("ppodydx10_30");
		METRICS.add("ppodydx30_100");
		METRICS.add("ppodydx100_300");
		METRICS.add("pricebolls10");
		METRICS.add("pricebolls20");
		METRICS.add("pricebolls50");
		METRICS.add("pricebolls100");
		METRICS.add("pricebolls200");
		METRICS.add("psar");
		METRICS.add("rangepressure50");
		METRICS.add("rangepressure100");
		METRICS.add("rangepressure200");
		METRICS.add("rangepressure500");
		METRICS.add("rsi3");
		METRICS.add("rsi10");
		METRICS.add("rsi30");
		METRICS.add("rsi100");
		METRICS.add("rsi300");
		METRICS.add("stochasticdrsi9_2_2");
		METRICS.add("stochasticdrsi14_3_3");
		METRICS.add("stochasticdrsi20_5_5");
		METRICS.add("stochastick9_2_2");
		METRICS.add("stochasticd9_2_2");
		METRICS.add("stochastick14_3_3");
		METRICS.add("stochasticd14_3_3");
		METRICS.add("stochastick20_5_5");
		METRICS.add("stochasticd20_5_5");
		METRICS.add("timerange2");
		METRICS.add("timerange5");
		METRICS.add("timerange8");
		METRICS.add("timerange13");
		METRICS.add("timerange20");
		METRICS.add("timerange30");
		METRICS.add("timerangealpha2");
		METRICS.add("timerangealpha5");
		METRICS.add("timerangealpha8");
		METRICS.add("timerangealpha13");
		METRICS.add("timerangealpha20");
		METRICS.add("timerangealpha30");
		METRICS.add("tsf10");
		METRICS.add("tsf20");
		METRICS.add("tsf30");
		METRICS.add("tsf40");
		METRICS.add("tsf60");
		METRICS.add("tsfdydx40");
		METRICS.add("tsfdydx60");
		METRICS.add("ultimateoscillator4_10_25");
		METRICS.add("ultimateoscillator8_20_50");
		METRICS.add("ultimateoscillator16_40_100");
		// METRICS.add("volumebolls10");
		// METRICS.add("volumebolls20");
		// METRICS.add("volumebolls50");
		// METRICS.add("volumebolls100");
		// METRICS.add("volumebolls200");
		METRICS.add("williamsr10");
		METRICS.add("williamsr20");
		METRICS.add("williamsr50");
		// METRICS.add("cdlhammer");
		// METRICS.add("cdldoji");
		// METRICS.add("cdlmorningstar");

		// METRICS_RF uses close but not hour - These seem to be high info gain
		METRICS_RF.add("rsi10");
		METRICS_RF.add("rsi14");
		METRICS_RF.add("rsi40");
		METRICS_RF.add("cps");
		METRICS_RF.add("pricebolls50");
		METRICS_RF.add("pricebolls100");
		METRICS_RF.add("pricebolls200");
		METRICS_RF.add("volumebolls50");
		METRICS_RF.add("volumebolls100");
		METRICS_RF.add("volumebolls200");
		METRICS_RF.add("dvol10ema");
		METRICS_RF.add("breakout20");
		METRICS_RF.add("breakout50");
		METRICS_RF.add("breakout100");
		METRICS_RF.add("williamsr50");
		METRICS_RF.add("macd12_26_9");
		METRICS_RF.add("macd20_40_9");
		METRICS_RF.add("macdsignal12_26_9");
		METRICS_RF.add("macdsignal20_40_9");
		METRICS_RF.add("macdhistory12_26_9");
		METRICS_RF.add("macdhistory20_40_9");
		METRICS_RF.add("tsf10");
		METRICS_RF.add("tsf20");
		METRICS_RF.add("tsf30");
		METRICS_RF.add("tsf40");
		METRICS_RF.add("tsf60");
		METRICS_RF.add("psar");
		METRICS_RF.add("cci40");
		METRICS_RF.add("cci60");
		METRICS_RF.add("atr10");
		METRICS_RF.add("atr20");
		METRICS_RF.add("atr40");
		METRICS_RF.add("atr60");
		METRICS_RF.add("mvol10");
		METRICS_RF.add("mvol20");
		METRICS_RF.add("mvol50");
		METRICS_RF.add("mvol100");
		METRICS_RF.add("mvol200");

		// METRICS_NB uses hour but not close - These seem to be low info gain
		METRICS_NB.add("rsi2");
		METRICS_NB.add("rsi5");
		METRICS_NB.add("mfi4");
		METRICS_NB.add("mfi8");
		METRICS_NB.add("mfi12");
		METRICS_NB.add("mfi16");
		METRICS_NB.add("consecutiveups");
		METRICS_NB.add("consecutivedowns");
		METRICS_NB.add("pricebolls10");
		METRICS_NB.add("pricebolls20");
		METRICS_NB.add("intradayboll10");
		METRICS_NB.add("intradayboll20");
		METRICS_NB.add("intradayboll50");
		METRICS_NB.add("volumebolls10");
		METRICS_NB.add("volumebolls20");
		METRICS_NB.add("breakout200");
		METRICS_NB.add("williamsr10");
		METRICS_NB.add("williamsr20");
		METRICS_NB.add("ultimateoscillator4_10_25");
		METRICS_NB.add("ultimateoscillator8_20_50");
		METRICS_NB.add("aroonoscillator10");
		METRICS_NB.add("aroonoscillator25");
		METRICS_NB.add("aroonoscillator50");
		METRICS_NB.add("stochasticdrsi9_2_2");
		METRICS_NB.add("stochasticdrsi14_3_3");
		METRICS_NB.add("stochasticdrsi20_5_5");
		METRICS_NB.add("stochastick9_2_2");
		METRICS_NB.add("stochasticd9_2_2");
		METRICS_NB.add("stochastick14_3_3");
		METRICS_NB.add("stochasticd14_3_3");
		METRICS_NB.add("stochastick20_5_5");
		METRICS_NB.add("stochasticd20_5_5");
		METRICS_NB.add("cci5");
		METRICS_NB.add("cci10");
		METRICS_NB.add("cci20");

		METRICS_B2.add("cci60");
		METRICS_B2.add("pricebolls50");
		METRICS_B2.add("volumebolls50");
		METRICS_B2.add("adxdydx30");
		METRICS_B2.add("cci40");
		METRICS_B2.add("rsi10");
		METRICS_B2.add("cmo10");
		METRICS_B2.add("adxr100");
		METRICS_B2.add("rangepressure100");
		METRICS_B2.add("adxrdydx30");
		METRICS_B2.add("volumebolls20");
		METRICS_B2.add("adx30");
		METRICS_B2.add("mfi60");
		METRICS_B2.add("rsi5");
		METRICS_B2.add("adxr30");
		METRICS_B2.add("williamsr50");
		METRICS_B2.add("rangepressure50");
		METRICS_B2.add("pricebolls20");
		METRICS_B2.add("aroonoscillator50");
		METRICS_B2.add("cci20");
		METRICS_B2.add("ultimateoscillator16_40_100");
		METRICS_B2.add("cmo3");
		METRICS_B2.add("intradayboll50");
		METRICS_B2.add("volumebolls10");
		METRICS_B2.add("aroonoscillator10");
		METRICS_B2.add("ultimateoscillator8_20_50");
		METRICS_B2.add("rsi2");
		METRICS_B2.add("adx10");
		METRICS_B2.add("williamsr20");
		METRICS_B2.add("adxr10");
		METRICS_B2.add("mfi30");
		METRICS_B2.add("pricebolls10");
		METRICS_B2.add("ultimateoscillator4_10_25");
		METRICS_B2.add("williamsr10");
		METRICS_B2.add("cci10");
		METRICS_B2.add("stochastick9_2_2");
		METRICS_B2.add("intradayboll20");
		METRICS_B2.add("stochastick20_5_5");
		METRICS_B2.add("stochastick14_3_3");
		METRICS_B2.add("stochasticd9_2_2");
		METRICS_B2.add("aroonoscillator25");
		METRICS_B2.add("stochasticd20_5_5");
		METRICS_B2.add("consecutiveups");
		METRICS_B2.add("consecutivedowns");
		METRICS_B2.add("mfi8");
		METRICS_B2.add("cci5");
		METRICS_B2.add("stochasticd14_3_3");
		METRICS_B2.add("mfi16");
		METRICS_B2.add("stochasticdrsi9_2_2");
		METRICS_B2.add("adxr3");
		METRICS_B2.add("mfi12");
		METRICS_B2.add("intradayboll10");
		METRICS_B2.add("adx3");
		METRICS_B2.add("stochasticdrsi14_3_3");
		METRICS_B2.add("stochasticdrsi20_5_5");

		METRICS_B3.add("intradayboll50");
		METRICS_B3.add("volumebolls10");
		METRICS_B3.add("aroonoscillator10");
		METRICS_B3.add("ultimateoscillator8_20_50");
		METRICS_B3.add("rsi2");
		METRICS_B3.add("adx10");
		METRICS_B3.add("williamsr20");
		METRICS_B3.add("adxr10");
		METRICS_B3.add("mfi30");
		METRICS_B3.add("pricebolls10");
		METRICS_B3.add("ultimateoscillator4_10_25");
		METRICS_B3.add("williamsr10");
		METRICS_B3.add("cci10");
		METRICS_B3.add("stochastick9_2_2");
		METRICS_B3.add("intradayboll20");
		METRICS_B3.add("stochastick20_5_5");
		METRICS_B3.add("stochastick14_3_3");
		METRICS_B3.add("stochasticd9_2_2");
		METRICS_B3.add("aroonoscillator25");
		METRICS_B3.add("stochasticd20_5_5");
		METRICS_B3.add("consecutiveups");
		METRICS_B3.add("consecutivedowns");
		METRICS_B3.add("mfi8");
		METRICS_B3.add("cci5");
		METRICS_B3.add("stochasticd14_3_3");
		METRICS_B3.add("mfi16");
		METRICS_B3.add("stochasticdrsi9_2_2");
		METRICS_B3.add("adxr3");
		METRICS_B3.add("mfi12");
		METRICS_B3.add("intradayboll10");
		METRICS_B3.add("adx3");
		METRICS_B3.add("stochasticdrsi14_3_3");
		METRICS_B3.add("stochasticdrsi20_5_5");

		METRICS_B4.add("dvol5ema");
		METRICS_B4.add("atr60");
		METRICS_B4.add("atr40");
		METRICS_B4.add("atr20");
		METRICS_B4.add("dvol10ema");
		METRICS_B4.add("atr10");
		METRICS_B4.add("dvol25ema");
		METRICS_B4.add("dvol50ema");
		METRICS_B4.add("mvol20");
		METRICS_B4.add("mvol100");
		METRICS_B4.add("dvol75ema");
		METRICS_B4.add("mvol10");
		METRICS_B4.add("mvol50");
		METRICS_B4.add("mvol200");
		METRICS_B4.add("timerange20");
		METRICS_B4.add("timerange13");
		METRICS_B4.add("psar");
		METRICS_B4.add("timerange8");
		METRICS_B4.add("atrdydx60");
		METRICS_B4.add("ppodydx30_100");
		METRICS_B4.add("timerange5");
		METRICS_B4.add("timerange2");
		METRICS_B4.add("atrdydx40");
		METRICS_B4.add("dvoldydx10ema");
		METRICS_B4.add("ppodydx10_30");
		METRICS_B4.add("mvoldydx100");
		METRICS_B4.add("macd20_40_9");
		METRICS_B4.add("ppo30_100");
		METRICS_B4.add("macdsignal20_40_9");
		METRICS_B4.add("macd12_26_9");
		METRICS_B4.add("tsf20");
		METRICS_B4.add("tsf30");
		METRICS_B4.add("tsf40");
		METRICS_B4.add("ppo10_30");
		METRICS_B4.add("macdsignal12_26_9");
		METRICS_B4.add("tsf60");
		METRICS_B4.add("macd6_13_5");
		METRICS_B4.add("ppo3_10");
		METRICS_B4.add("macdsignal6_13_5");
		METRICS_B4.add("macdhistory20_40_9");
		METRICS_B4.add("macdhistory6_13_5");
		METRICS_B4.add("macdhistory12_26_9");
		METRICS_B4.add("tsfdydx40");
		METRICS_B4.add("tsfdydx60");
		METRICS_B4.add("tsf10");
		METRICS_B4.add("mvoldydx200");
		METRICS_B4.add("cps");
		METRICS_B4.add("breakout200");
		METRICS_B4.add("dvoldydx5ema");
		METRICS_B4.add("adodydx30_100");
		METRICS_B4.add("adodydx10_30");
		METRICS_B4.add("ado10_30");
		METRICS_B4.add("ado3_10");
		METRICS_B4.add("ado30_100");
		METRICS_B4.add("breakout100");
		METRICS_B4.add("breakout20");
		METRICS_B4.add("breakout50");
		METRICS_B4.add("cmo100");
		METRICS_B4.add("rangepressure500");
		METRICS_B4.add("rsi40");
		METRICS_B4.add("pricebolls200");
		METRICS_B4.add("cmo30");
		METRICS_B4.add("volumebolls200");
		METRICS_B4.add("adxdydx100");
		METRICS_B4.add("rsi25");
		METRICS_B4.add("mfi4");
		METRICS_B4.add("pricebolls100");
		METRICS_B4.add("volumebolls100");
		METRICS_B4.add("rangepressure200");
		METRICS_B4.add("adx100");
		METRICS_B4.add("adxrdydx100");
		METRICS_B4.add("rsi14");

		METRICS_B5.add("dvol5ema");
		METRICS_B5.add("atr60");
		METRICS_B5.add("atr40");
		METRICS_B5.add("atr20");
		METRICS_B5.add("dvol10ema");
		METRICS_B5.add("atr10");
		METRICS_B5.add("dvol25ema");
		METRICS_B5.add("dvol50ema");
		METRICS_B5.add("mvol20");
		METRICS_B5.add("mvol100");
		METRICS_B5.add("dvol75ema");
		METRICS_B5.add("mvol10");
		METRICS_B5.add("mvol50");
		METRICS_B5.add("mvol200");
		METRICS_B5.add("timerange20");
		METRICS_B5.add("timerange13");
		METRICS_B5.add("psar");
		METRICS_B5.add("timerange8");
		METRICS_B5.add("atrdydx60");
		METRICS_B5.add("ppodydx30_100");
		METRICS_B5.add("timerange5");
		METRICS_B5.add("timerange2");
		METRICS_B5.add("atrdydx40");
		METRICS_B5.add("dvoldydx10ema");
		METRICS_B5.add("ppodydx10_30");
		METRICS_B5.add("mvoldydx100");
		METRICS_B5.add("macd20_40_9");
		METRICS_B5.add("ppo30_100");
		METRICS_B5.add("macdsignal20_40_9");
		METRICS_B5.add("macd12_26_9");
		METRICS_B5.add("tsf20");
		METRICS_B5.add("tsf30");
		METRICS_B5.add("tsf40");
		METRICS_B5.add("ppo10_30");
		METRICS_B5.add("macdsignal12_26_9");
		METRICS_B5.add("tsf60");
		METRICS_B5.add("macd6_13_5");
		METRICS_B5.add("ppo3_10");
		METRICS_B5.add("macdsignal6_13_5");
		METRICS_B5.add("macdhistory20_40_9");
		METRICS_B5.add("macdhistory6_13_5");
		METRICS_B5.add("macdhistory12_26_9");
		METRICS_B5.add("tsfdydx40");
		METRICS_B5.add("tsfdydx60");
		METRICS_B5.add("tsf10");
		METRICS_B5.add("mvoldydx200");
		METRICS_B5.add("cps");
		METRICS_B5.add("breakout200");
		METRICS_B5.add("dvoldydx5ema");

		METRICS_B6.add("dvol5ema");
		METRICS_B6.add("atr60");
		METRICS_B6.add("atr40");
		METRICS_B6.add("atr20");
		METRICS_B6.add("dvol10ema");
		METRICS_B6.add("atr10");
		METRICS_B6.add("dvol25ema");
		METRICS_B6.add("dvol50ema");
		METRICS_B6.add("mvol20");
		METRICS_B6.add("mvol100");
		METRICS_B6.add("dvol75ema");
		METRICS_B6.add("mvol10");
		METRICS_B6.add("mvol50");
		METRICS_B6.add("mvol200");
		METRICS_B6.add("timerange20");
		METRICS_B6.add("timerange13");
		METRICS_B6.add("psar");
		METRICS_B6.add("timerange8");
		METRICS_B6.add("atrdydx60");
		METRICS_B6.add("ppodydx30_100");
		METRICS_B6.add("timerange5");
		METRICS_B6.add("timerange2");
		METRICS_B6.add("atrdydx40");
		METRICS_B6.add("dvoldydx10ema");
		METRICS_B6.add("ppodydx10_30");
		METRICS_B6.add("mvoldydx100");
		METRICS_B6.add("macd20_40_9");
		METRICS_B6.add("ppo30_100");
		METRICS_B6.add("macdsignal20_40_9");
		METRICS_B6.add("macd12_26_9");

		METRICS_B7.add("dvol5ema");
		METRICS_B7.add("atr60");
		METRICS_B7.add("atr40");
		METRICS_B7.add("atr20");
		METRICS_B7.add("dvol10ema");
		METRICS_B7.add("atr10");
		METRICS_B7.add("dvol25ema");
		METRICS_B7.add("dvol50ema");
		METRICS_B7.add("mvol20");
		METRICS_B7.add("mvol100");
		METRICS_B7.add("dvol75ema");
		METRICS_B7.add("mvol10");
		METRICS_B7.add("mvol50");
		METRICS_B7.add("mvol200");
		METRICS_B7.add("timerange20");
		METRICS_B7.add("timerange13");
		METRICS_B7.add("psar");
		METRICS_B7.add("timerange8");
		METRICS_B7.add("atrdydx60");
		METRICS_B7.add("ppodydx30_100");
		METRICS_B7.add("timerange5");

		METRICS_B8.add("dvol5ema");
		METRICS_B8.add("atr60");
		METRICS_B8.add("atr40");
		METRICS_B8.add("atr20");
		METRICS_B8.add("dvol10ema");
		METRICS_B8.add("atr10");
		METRICS_B8.add("dvol25ema");
		METRICS_B8.add("dvol50ema");
		METRICS_B8.add("mvol20");
		METRICS_B8.add("mvol100");
		METRICS_B8.add("dvol75ema");
		METRICS_B8.add("mvol10");
		METRICS_B8.add("mvol50");
		METRICS_B8.add("mvol200");

		METRICS_B9.add("timerange2");
		METRICS_B9.add("rangepressure50");
		METRICS_B9.add("rangepressure100");
		METRICS_B9.add("timerange5");
		METRICS_B9.add("breakout100");
		METRICS_B9.add("breakout20");
		METRICS_B9.add("rangepressure200");
		METRICS_B9.add("breakout200");
		METRICS_B9.add("breakout50");
		METRICS_B9.add("rsi25");
		METRICS_B9.add("mfi4");
		METRICS_B9.add("rsi14");
		METRICS_B9.add("rsi10");
		METRICS_B9.add("cmo10");
		METRICS_B9.add("ppodydx30_100");
		METRICS_B9.add("dvoldydx5ema");
		METRICS_B9.add("dvol75ema");
		METRICS_B9.add("dvol25ema");
		METRICS_B9.add("atr10");
		METRICS_B9.add("dvol10ema");
		METRICS_B9.add("dvol50ema");
		METRICS_B9.add("mvol20");
		METRICS_B9.add("mvoldydx200");
		METRICS_B9.add("mvol10");
		METRICS_B9.add("mvoldydx100");
		METRICS_B9.add("consecutiveups");
		METRICS_B9.add("consecutivedowns");
		METRICS_B9.add("cci40");
		METRICS_B9.add("pricebolls50");
		METRICS_B9.add("cci60");
		METRICS_B9.add("pricebolls100");
		METRICS_B9.add("rsi40");
		METRICS_B9.add("cmo30");

		METRICS_B10.add("tsfdydx40");
		METRICS_B10.add("breakout200");
		METRICS_B10.add("tsfdydx60");
		METRICS_B10.add("psar");
		METRICS_B10.add("rangepressure500");
		METRICS_B10.add("breakout20");
		METRICS_B10.add("adodydx10_30");
		METRICS_B10.add("stochasticdrsi9_2_2");
		METRICS_B10.add("macdsignal12_26_9");
		METRICS_B10.add("stochastick14_3_3");
		METRICS_B10.add("stochasticd14_3_3");
		METRICS_B10.add("williamsr20");
		METRICS_B10.add("timerange5");
		METRICS_B10.add("breakout50");
		METRICS_B10.add("williamsr50");
		METRICS_B10.add("timerange20");
		METRICS_B10.add("volumebolls200");
		METRICS_B10.add("macdhistory6_13_5");
		METRICS_B10.add("breakout100");
		METRICS_B10.add("rangepressure200");
		METRICS_B10.add("volumebolls20");
		METRICS_B10.add("dvol5ema");
		METRICS_B10.add("volumebolls10");
		METRICS_B10.add("volumebolls50");
		METRICS_B10.add("atr20");
		METRICS_B10.add("macd20_40_9");
		METRICS_B10.add("timerange8");
		METRICS_B10.add("mfi16");
		METRICS_B10.add("adxdydx100");
		METRICS_B10.add("tsf30");

		METRICS_B11.add("atr10");
		METRICS_B11.add("atr20");
		METRICS_B11.add("atr40");
		METRICS_B11.add("atr60");
		METRICS_B11.add("dvol5ema");
		METRICS_B11.add("dvol10ema");
		METRICS_B11.add("dvol25ema");
		METRICS_B11.add("dvol50ema");
		METRICS_B11.add("dvol75ema");
		METRICS_B11.add("mvol10");
		METRICS_B11.add("mvol20");
		METRICS_B11.add("mvol50");
		METRICS_B11.add("mvol100");
		METRICS_B11.add("mvol200");
		METRICS_B11.add("ppo3_10");
		METRICS_B11.add("pricebolls10");
		METRICS_B11.add("psar");
		METRICS_B11.add("timerange2");
		METRICS_B11.add("timerange5");
		METRICS_B11.add("tsf10");
		METRICS_B11.add("tsf20");
		METRICS_B11.add("tsfdydx40");
		METRICS_B11.add("tsfdydx60");

		METRICS_B12.add("atr10");
		METRICS_B12.add("atr20");
		METRICS_B12.add("atr40");
		METRICS_B12.add("atrdydx40");
		METRICS_B12.add("atrdydx60");
		METRICS_B12.add("dvol5ema");
		METRICS_B12.add("dvol10ema");
		METRICS_B12.add("dvol25ema");
		METRICS_B12.add("dvol50ema");
		METRICS_B12.add("dvol75ema");
		METRICS_B12.add("macd6_13_5");
		METRICS_B12.add("macd12_26_9");
		METRICS_B12.add("macdsignal6_13_5");
		METRICS_B12.add("macdsignal12_26_9");
		METRICS_B12.add("mvol10");
		METRICS_B12.add("mvol20");
		METRICS_B12.add("mvol50");
		METRICS_B12.add("psar");
		METRICS_B12.add("timerange2");
		METRICS_B12.add("timerange5");
		METRICS_B12.add("timerange8");
		METRICS_B12.add("timerange13");
		METRICS_B12.add("timerange20");

		METRICS_B13.add("adx30");
		METRICS_B13.add("adx100");
		METRICS_B13.add("adxdydx100");
		METRICS_B13.add("adxr30");
		METRICS_B13.add("adxr100");
		METRICS_B13.add("adxrdydx100");
		METRICS_B13.add("atr20");
		METRICS_B13.add("atr40");
		METRICS_B13.add("atr60");
		METRICS_B13.add("cmo30");
		METRICS_B13.add("cmo100");
		METRICS_B13.add("dvol5ema");
		METRICS_B13.add("dvol10ema");
		METRICS_B13.add("mvol20");
		METRICS_B13.add("mvol50");
		METRICS_B13.add("mvol100");
		METRICS_B13.add("mvol200");
		METRICS_B13.add("ppo10_30");
		METRICS_B13.add("ppo30_100");
		METRICS_B13.add("pricebolls100");
		METRICS_B13.add("pricebolls200");
		METRICS_B13.add("rangepressure100");
		METRICS_B13.add("rangepressure200");
		METRICS_B13.add("rangepressure500");
		METRICS_B13.add("rsi25");
		METRICS_B13.add("rsi40");
		METRICS_B13.add("timerange5");
		METRICS_B13.add("timerange8");
		METRICS_B13.add("timerange13");
		METRICS_B13.add("timerange20");

		METRICS_B14.add("adx100");
		METRICS_B14.add("atr10");
		METRICS_B14.add("atr20");
		METRICS_B14.add("atr40");
		METRICS_B14.add("atr60");
		METRICS_B14.add("atrdydx40");
		METRICS_B14.add("atrdydx60");
		METRICS_B14.add("dvol5ema");
		METRICS_B14.add("dvol10ema");
		METRICS_B14.add("dvol25ema");
		METRICS_B14.add("dvol50ema");
		METRICS_B14.add("dvol75ema");
		METRICS_B14.add("mvol10");
		METRICS_B14.add("mvol20");
		METRICS_B14.add("mvol50");
		METRICS_B14.add("mvol100");
		METRICS_B14.add("mvol200");
		METRICS_B14.add("ppo30_100");
		METRICS_B14.add("ppodydx30_100");
		METRICS_B14.add("ppodydx10_30");
		METRICS_B14.add("psar");
		METRICS_B14.add("timerange2");
		METRICS_B14.add("timerange5");
		METRICS_B14.add("timerange8");
		METRICS_B14.add("timerange13");
		METRICS_B14.add("timerange20");
		METRICS_B14.add("tsf40");
		METRICS_B14.add("tsfdydx60");

		METRICS_B15.add("adx300");
		METRICS_B15.add("adx100");
		METRICS_B15.add("adx30");
		METRICS_B15.add("adxr300");
		METRICS_B15.add("adxr30");
		METRICS_B15.add("atr120");
		METRICS_B15.add("atr60");
		METRICS_B15.add("atr40");
		METRICS_B15.add("atr20");
		METRICS_B15.add("atr10");
		METRICS_B15.add("cmo300");
		METRICS_B15.add("cmo100");
		METRICS_B15.add("dvol3ema");
		METRICS_B15.add("dvol5ema");
		METRICS_B15.add("dvol10ema");
		METRICS_B15.add("dvol25ema");
		METRICS_B15.add("mvol500");
		METRICS_B15.add("mvol200");
		METRICS_B15.add("mvol100");
		METRICS_B15.add("ppo100_300");
		METRICS_B15.add("rsi100");
		METRICS_B15.add("timerange30");
		METRICS_B15.add("timerange20");
		METRICS_B15.add("timerange13");
		METRICS_B15.add("timerange5");
		METRICS_B15.add("timerangealpha30");
		METRICS_B15.add("timerangealpha20");
		METRICS_B15.add("timerangealpha13");
		METRICS_B15.add("timerangealpha8");
		METRICS_B15.add("timerangealpha5");

		METRICS_B16.add("cci10");
		METRICS_B16.add("cci20");
		METRICS_B16.add("cci40");
		METRICS_B16.add("cci60");
		METRICS_B16.add("cmo3");
		METRICS_B16.add("cmo10");
		METRICS_B16.add("cmo30");
		METRICS_B16.add("cmo100");
		METRICS_B16.add("rsi5");
		METRICS_B16.add("rsi10");
		METRICS_B16.add("rsi14");
		METRICS_B16.add("rsi25");
		METRICS_B16.add("rsi40");
		METRICS_B16.add("rsi100");
		METRICS_B16.add("pricebolls10");
		METRICS_B16.add("pricebolls20");
		METRICS_B16.add("pricebolls50");
		METRICS_B16.add("rangepressure50");
		METRICS_B16.add("rangepressure200");
		METRICS_B16.add("stochasticd14_3_3");
		METRICS_B16.add("stochastick9_2_2");
		METRICS_B16.add("stochastick14_3_3");
		METRICS_B16.add("stochastick20_5_5");
		METRICS_B16.add("timerange13");
		METRICS_B16.add("timerangealpha20");
		METRICS_B16.add("ultimateoscillator8_20_50");
		METRICS_B16.add("ultimateoscillator16_40_100");
		METRICS_B16.add("williamsr10");
		METRICS_B16.add("williamsr20");
		METRICS_B16.add("williamsr50");

		// METRICS.clear();
		//
		// Set<String> set = new HashSet<String>();
		// set.addAll(METRICS_B16);
		//
		// METRICS.addAll(set);

		// METRIC_NEEDED_BARS
		METRIC_NEEDED_BARS.put("ado3_10", 20);
		METRIC_NEEDED_BARS.put("ado10_30", 40);
		METRIC_NEEDED_BARS.put("ado30_100", 110);
		METRIC_NEEDED_BARS.put("adodydx10_30", 40);
		METRIC_NEEDED_BARS.put("adodydx30_100", 110);
		METRIC_NEEDED_BARS.put("adx3", 6);				// TA-Lib 5
		METRIC_NEEDED_BARS.put("adx10", 20);			// TA-Lib 19	((period * 2) - 1), Start Index = period * 3
		METRIC_NEEDED_BARS.put("adx30", 60);			// TA-Lib 59
		METRIC_NEEDED_BARS.put("adx100", 200);			// TA-Lib 199
		METRIC_NEEDED_BARS.put("adx300", 600); 			// TA-Lib 599
		METRIC_NEEDED_BARS.put("adxdydx30", 40);
		METRIC_NEEDED_BARS.put("adxdydx100", 110);
		METRIC_NEEDED_BARS.put("adxdydx300", 310);
		METRIC_NEEDED_BARS.put("adxr3", 10);
		METRIC_NEEDED_BARS.put("adxr10", 20);
		METRIC_NEEDED_BARS.put("adxr30", 40);
		METRIC_NEEDED_BARS.put("adxr100", 299); // TA-Lib starts at 298
		METRIC_NEEDED_BARS.put("adxr300", 899); // TA-Lib starts at 898
		METRIC_NEEDED_BARS.put("adxrdydx30", 40);
		METRIC_NEEDED_BARS.put("adxrdydx100", 110);
		METRIC_NEEDED_BARS.put("adxrdydx300", 310);
		METRIC_NEEDED_BARS.put("aroonoscillator10", 20);
		METRIC_NEEDED_BARS.put("aroonoscillator25", 35);
		METRIC_NEEDED_BARS.put("aroonoscillator50", 60);
		METRIC_NEEDED_BARS.put("atr10", 20);
		METRIC_NEEDED_BARS.put("atr20", 30);
		METRIC_NEEDED_BARS.put("atr40", 50);
		METRIC_NEEDED_BARS.put("atr60", 70);
		METRIC_NEEDED_BARS.put("atr120", 130);
		METRIC_NEEDED_BARS.put("atrdydx40", 50);
		METRIC_NEEDED_BARS.put("atrdydx60", 70);
		METRIC_NEEDED_BARS.put("breakout20", 30);
		METRIC_NEEDED_BARS.put("breakout50", 60);
		METRIC_NEEDED_BARS.put("breakout100", 110);
		METRIC_NEEDED_BARS.put("breakout200", 210);
		METRIC_NEEDED_BARS.put("cci5", 15);
		METRIC_NEEDED_BARS.put("cci10", 20);
		METRIC_NEEDED_BARS.put("cci20", 30);
		METRIC_NEEDED_BARS.put("cci40", 50);
		METRIC_NEEDED_BARS.put("cci60", 70);
		METRIC_NEEDED_BARS.put("cmo3", 10);
		METRIC_NEEDED_BARS.put("cmo10", 20);
		METRIC_NEEDED_BARS.put("cmo30", 40);
		METRIC_NEEDED_BARS.put("cmo100", 110);
		METRIC_NEEDED_BARS.put("cmo300", 310);
		METRIC_NEEDED_BARS.put("consecutiveups", 20);
		METRIC_NEEDED_BARS.put("consecutivedowns", 20);
		METRIC_NEEDED_BARS.put("cps", 20);
		METRIC_NEEDED_BARS.put("dvol3ema", 100);
		METRIC_NEEDED_BARS.put("dvol5ema", 50);
		METRIC_NEEDED_BARS.put("dvol10ema", 30);
		METRIC_NEEDED_BARS.put("dvol25ema", 20);
		METRIC_NEEDED_BARS.put("dvol50ema", 10);
		METRIC_NEEDED_BARS.put("dvol75ema", 10);
		METRIC_NEEDED_BARS.put("dvoldydx5ema", 50);
		METRIC_NEEDED_BARS.put("dvoldydx10ema", 30);
		METRIC_NEEDED_BARS.put("gapboll10", 20);
		METRIC_NEEDED_BARS.put("gapboll20", 30);
		METRIC_NEEDED_BARS.put("gapboll50", 60);
		METRIC_NEEDED_BARS.put("intradayboll10", 20);
		METRIC_NEEDED_BARS.put("intradayboll20", 30);
		METRIC_NEEDED_BARS.put("intradayboll50", 60);
		METRIC_NEEDED_BARS.put("macd6_13_5", 23);
		METRIC_NEEDED_BARS.put("macd12_26_9", 36);
		METRIC_NEEDED_BARS.put("macd20_40_9", 50);
		METRIC_NEEDED_BARS.put("macdsignal6_13_5", 23);
		METRIC_NEEDED_BARS.put("macdsignal12_26_9", 36);
		METRIC_NEEDED_BARS.put("macdsignal20_40_9", 50);
		METRIC_NEEDED_BARS.put("macdhistory6_13_5", 23);
		METRIC_NEEDED_BARS.put("macdhistory12_26_9", 36);
		METRIC_NEEDED_BARS.put("macdhistory20_40_9", 50);
		METRIC_NEEDED_BARS.put("mfi4", 14);
		METRIC_NEEDED_BARS.put("mfi8", 18);
		METRIC_NEEDED_BARS.put("mfi12", 22);
		METRIC_NEEDED_BARS.put("mfi16", 26);
		METRIC_NEEDED_BARS.put("mfi30", 40);
		METRIC_NEEDED_BARS.put("mfi60", 70);
		METRIC_NEEDED_BARS.put("mvol10", 20);
		METRIC_NEEDED_BARS.put("mvol20", 30);
		METRIC_NEEDED_BARS.put("mvol50", 60);
		METRIC_NEEDED_BARS.put("mvol100", 110);
		METRIC_NEEDED_BARS.put("mvol200", 210);
		METRIC_NEEDED_BARS.put("mvol500", 510);
		METRIC_NEEDED_BARS.put("mvoldydx100", 110);
		METRIC_NEEDED_BARS.put("mvoldydx200", 210);
		METRIC_NEEDED_BARS.put("ppo3_10", 20);
		METRIC_NEEDED_BARS.put("ppo10_30", 40);
		METRIC_NEEDED_BARS.put("ppo30_100", 110);
		METRIC_NEEDED_BARS.put("ppo100_300", 310);
		METRIC_NEEDED_BARS.put("ppodydx10_30", 40);
		METRIC_NEEDED_BARS.put("ppodydx30_100", 110);
		METRIC_NEEDED_BARS.put("ppodydx100_300", 310);
		METRIC_NEEDED_BARS.put("pricebolls10", 20);
		METRIC_NEEDED_BARS.put("pricebolls20", 30);
		METRIC_NEEDED_BARS.put("pricebolls50", 60);
		METRIC_NEEDED_BARS.put("pricebolls100", 110);
		METRIC_NEEDED_BARS.put("pricebolls200", 210);
		METRIC_NEEDED_BARS.put("psar", 20);
		METRIC_NEEDED_BARS.put("rangepressure50", 55);
		METRIC_NEEDED_BARS.put("rangepressure100", 110);
		METRIC_NEEDED_BARS.put("rangepressure200", 210);
		METRIC_NEEDED_BARS.put("rangepressure500", 510);
		METRIC_NEEDED_BARS.put("rsi3", 6);
		METRIC_NEEDED_BARS.put("rsi10", 20);
		METRIC_NEEDED_BARS.put("rsi30", 60);
		METRIC_NEEDED_BARS.put("rsi100", 200);
		METRIC_NEEDED_BARS.put("rsi300", 600);
		METRIC_NEEDED_BARS.put("stochasticdrsi9_2_2", 20);
		METRIC_NEEDED_BARS.put("stochasticdrsi14_3_3", 25);
		METRIC_NEEDED_BARS.put("stochasticdrsi20_5_5", 30);
		METRIC_NEEDED_BARS.put("stochastick9_2_2", 20);
		METRIC_NEEDED_BARS.put("stochasticd9_2_2", 20);
		METRIC_NEEDED_BARS.put("stochastick14_3_3", 25);
		METRIC_NEEDED_BARS.put("stochasticd14_3_3", 25);
		METRIC_NEEDED_BARS.put("stochastick20_5_5", 30);
		METRIC_NEEDED_BARS.put("stochasticd20_5_5", 30);
		METRIC_NEEDED_BARS.put("timerange2", 300);
		METRIC_NEEDED_BARS.put("timerange5", 350);
		METRIC_NEEDED_BARS.put("timerange8", 400);
		METRIC_NEEDED_BARS.put("timerange13", 500);
		METRIC_NEEDED_BARS.put("timerange20", 700);
		METRIC_NEEDED_BARS.put("timerange30", 1000);
		METRIC_NEEDED_BARS.put("timerangealpha2", 300);
		METRIC_NEEDED_BARS.put("timerangealpha5", 350);
		METRIC_NEEDED_BARS.put("timerangealpha8", 400);
		METRIC_NEEDED_BARS.put("timerangealpha13", 500);
		METRIC_NEEDED_BARS.put("timerangealpha20", 700);
		METRIC_NEEDED_BARS.put("timerangealpha30", 1000);
		METRIC_NEEDED_BARS.put("tsf10", 20);
		METRIC_NEEDED_BARS.put("tsf20", 30);
		METRIC_NEEDED_BARS.put("tsf30", 40);
		METRIC_NEEDED_BARS.put("tsf40", 50);
		METRIC_NEEDED_BARS.put("tsf60", 70);
		METRIC_NEEDED_BARS.put("tsfdydx40", 50);
		METRIC_NEEDED_BARS.put("tsfdydx60", 70);
		METRIC_NEEDED_BARS.put("ultimateoscillator4_10_25", 35);
		METRIC_NEEDED_BARS.put("ultimateoscillator8_20_50", 60);
		METRIC_NEEDED_BARS.put("ultimateoscillator16_40_100", 110);
		METRIC_NEEDED_BARS.put("volumebolls10", 20);
		METRIC_NEEDED_BARS.put("volumebolls20", 30);
		METRIC_NEEDED_BARS.put("volumebolls50", 60);
		METRIC_NEEDED_BARS.put("volumebolls100", 110);
		METRIC_NEEDED_BARS.put("volumebolls200", 210);
		METRIC_NEEDED_BARS.put("williamsr10", 20);
		METRIC_NEEDED_BARS.put("williamsr20", 30);
		METRIC_NEEDED_BARS.put("williamsr50", 60);
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
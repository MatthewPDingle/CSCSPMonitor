package data;

import java.util.ArrayList;

public class TickConstants {

	public static String TICK_NAME_BITFINEX_BTC_USD = "bitfinexBTCUSD";
	public static String TICK_NAME_BITSTAMP_BTC_USD = "bitstampBTCUSD";
	
	public static String TICK_NAME_BTCE_BTC_USD = "btceBTCUSD";
	
	public static String TICK_NAME_BTCN_BTC_CNY = "btcnBTCCNY";
	
	public static String TICK_NAME_KRAKEN_BTC_USD = "krakenBTCUSD";
	public static String TICK_NAME_KRAKEN_BTC_EUR = "krakenBTCEUR";
	
	public static String TICK_NAME_OKCOIN_BTC_CNY = "okcoinBTCCNY";
	public static String TICK_NAME_OKCOIN_BTC_USD = "okcoinBTCUSD";
	public static String TICK_NAME_OKCOIN_LTC_CNY = "okcoinLTCCNY";
	public static String TICK_NAME_OKCOIN_LTC_USD = "okcoinLTCUSD";
	
	public static String TICK_NAME_FOREX_AUD_JPY = "AUD.JPY";
	public static String TICK_NAME_FOREX_AUD_USD = "AUD.USD";
	public static String TICK_NAME_FOREX_EUR_CHF = "EUR.CHF";
	public static String TICK_NAME_FOREX_EUR_GBP = "EUR.GBP";
	public static String TICK_NAME_FOREX_EUR_JPY = "EUR.JPY";
	public static String TICK_NAME_FOREX_EUR_USD = "EUR.USD";
	public static String TICK_NAME_FOREX_GBP_JPY = "GBP.JPY";
	public static String TICK_NAME_FOREX_GBP_USD = "GBP.USD";
	public static String TICK_NAME_FOREX_NZD_USD = "NZD.USD";
	public static String TICK_NAME_FOREX_USD_CAD = "USD.CAD";
	public static String TICK_NAME_FOREX_USD_CHF = "USD.CHF";
	public static String TICK_NAME_FOREX_USD_JPY = "USD.JPY";
	
	public static ArrayList<String> BITCOIN_TICK_NAMES = new ArrayList<String>();
	
	static {
		BITCOIN_TICK_NAMES.add(TICK_NAME_BITFINEX_BTC_USD);
		BITCOIN_TICK_NAMES.add(TICK_NAME_BITSTAMP_BTC_USD);
		BITCOIN_TICK_NAMES.add(TICK_NAME_BTCE_BTC_USD);
		BITCOIN_TICK_NAMES.add(TICK_NAME_BTCN_BTC_CNY);
		BITCOIN_TICK_NAMES.add(TICK_NAME_KRAKEN_BTC_USD);
		BITCOIN_TICK_NAMES.add(TICK_NAME_KRAKEN_BTC_EUR);
		BITCOIN_TICK_NAMES.add(TICK_NAME_OKCOIN_BTC_CNY);
		BITCOIN_TICK_NAMES.add(TICK_NAME_OKCOIN_BTC_USD);
		BITCOIN_TICK_NAMES.add(TICK_NAME_OKCOIN_LTC_CNY);
		BITCOIN_TICK_NAMES.add(TICK_NAME_OKCOIN_LTC_USD);
	}
}
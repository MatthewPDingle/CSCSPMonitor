package data.downloaders.interactivebrokers;

import java.util.HashMap;

import constants.Constants.BAR_SIZE;
import data.BarKey;

public class IBConstants {

	public static final int IB_API_PORT = 7497;
	
	public static final int CONNECT_TIMEOUT_MS = 60000;
	
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
	
	public static enum ORDER_ACTION { BUY, SELL, SSHORT };
	
	public static final String TICK_FIELD_BID_PRICE = "bidPrice";
	public static final String TICK_FIELD_ASK_PRICE = "askPrice";
	public static final String TICK_FIELD_BID_SIZE = "bidSize";
	public static final String TICK_FIELD_ASK_SIZE = "askSize";
	public static final String TICK_FIELD_HIGH = "high";
	public static final String TICK_FIELD_LOW = "low";
	public static final String TICK_FIELD_CLOSE = "close";
	public static final String TICK_FIELD_LAST = "last"; // TODO: confirm this is correct
	public static final String TICK_FIELD_MIDPOINT = "midpoint"; // Not in the API
	
	public static final HashMap<String, String> SECURITY_TYPE_EXCHANGE_HASH = new HashMap<String, String>();
	public static final HashMap<BAR_SIZE, String> BAR_DURATION_IB_BAR_SIZE = new HashMap<BAR_SIZE, String>();
	public static final HashMap<BarKey, Integer> BARKEY_TICKER_ID_HASH = new HashMap<BarKey, Integer>();
	public static final HashMap<String, String> TICKER_SECURITY_TYPE_HASH = new HashMap<String, String>();
	
	static {
		SECURITY_TYPE_EXCHANGE_HASH.put("CASH", "IDEALPRO");
		SECURITY_TYPE_EXCHANGE_HASH.put("STK", "SMART");
		
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_30S, "30 secs");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_1M, "1 min");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_3M, "3 min");	
		
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_JPY, BAR_SIZE.BAR_30S), 1);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_USD, BAR_SIZE.BAR_30S), 2);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_CHF, BAR_SIZE.BAR_30S), 3);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_GBP, BAR_SIZE.BAR_30S), 4);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_JPY, BAR_SIZE.BAR_30S), 5);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_USD, BAR_SIZE.BAR_30S), 6);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_JPY, BAR_SIZE.BAR_30S), 7);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_USD, BAR_SIZE.BAR_30S), 8);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_NZD_USD, BAR_SIZE.BAR_30S), 9);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CAD, BAR_SIZE.BAR_30S), 10);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CHF, BAR_SIZE.BAR_30S), 11);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_JPY, BAR_SIZE.BAR_30S), 12);
		
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_JPY, BAR_SIZE.BAR_1M), 13);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_USD, BAR_SIZE.BAR_1M), 14);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_CHF, BAR_SIZE.BAR_1M), 15);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_GBP, BAR_SIZE.BAR_1M), 16);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_JPY, BAR_SIZE.BAR_1M), 17);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_USD, BAR_SIZE.BAR_1M), 18);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_JPY, BAR_SIZE.BAR_1M), 19);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_USD, BAR_SIZE.BAR_1M), 20);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_NZD_USD, BAR_SIZE.BAR_1M), 21);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CAD, BAR_SIZE.BAR_1M), 22);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CHF, BAR_SIZE.BAR_1M), 23);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_JPY, BAR_SIZE.BAR_1M), 24);
		
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_JPY, BAR_SIZE.BAR_3M), 25);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_USD, BAR_SIZE.BAR_3M), 26);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_CHF, BAR_SIZE.BAR_3M), 27);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_GBP, BAR_SIZE.BAR_3M), 28);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_JPY, BAR_SIZE.BAR_3M), 29);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_USD, BAR_SIZE.BAR_3M), 30);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_JPY, BAR_SIZE.BAR_3M), 31);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_USD, BAR_SIZE.BAR_3M), 32);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_NZD_USD, BAR_SIZE.BAR_3M), 33);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CAD, BAR_SIZE.BAR_3M), 34);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CHF, BAR_SIZE.BAR_3M), 35);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_JPY, BAR_SIZE.BAR_3M), 36);
		
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_AUD_JPY, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_AUD_USD, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_EUR_CHF, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_EUR_GBP, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_EUR_JPY, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_EUR_USD, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_GBP_JPY, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_GBP_USD, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_NZD_USD, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_USD_CAD, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_USD_CHF, "CASH");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_FOREX_USD_JPY, "CASH");
	}
	
	public static String getIBSymbolFromForexSymbol(String forexSymbol) {
		String ibSymbol = "";
		try {
			ibSymbol = forexSymbol.substring(0, 3);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ibSymbol;
	}
	
	public static String getIBCurrencyFromForexSymbol(String forexSymbol) {
		String ibCurrency = "";
		try {
			ibCurrency = forexSymbol.substring(4, 7);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ibCurrency;
	}
}
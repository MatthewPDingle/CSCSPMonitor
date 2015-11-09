package data.downloaders.interactivebrokers;

import java.util.HashMap;

import constants.Constants.BAR_SIZE;

public class IBConstants {

	public static final int IB_API_PORT = 7497;
	
	public static final HashMap<String, String> SECURITY_TYPE_EXCHANGE_HASH = new HashMap<String, String>();
	public static final HashMap<BAR_SIZE, String> BAR_DURATION_IB_BAR_SIZE = new HashMap<BAR_SIZE, String>();
	
	static {
		SECURITY_TYPE_EXCHANGE_HASH.put("CASH", "IDEALPRO");
		SECURITY_TYPE_EXCHANGE_HASH.put("STK", "SMART");
		
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_30S, "30 secs");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_1M, "1 min");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_3M, "3 min");
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
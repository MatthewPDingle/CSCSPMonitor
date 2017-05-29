package data.downloaders.interactivebrokers;

import java.util.ArrayList;
import java.util.HashMap;

import constants.Constants.BAR_SIZE;
import data.BarKey;

public class IBConstants {

	public static final int IB_API_PORT = 7497; // or 7496
	
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
	public static String TICK_NAME_CME_GLOBEX_FUTURES_SPX = "SPX";	// SP 500
	public static String TICK_NAME_CME_GLOBEX_FUTURES_ES = 	"ES";	// SP 500 e-mini
	public static String TICK_NAME_CME_GLOBEX_FUTURES_NQ = 	"NQ";	// Nasdaq 100 e-mini
	public static String TICK_NAME_CME_GLOBEX_FUTURES_GBP = "GBP";	// British Pound
	public static String TICK_NAME_CME_GLOBEX_FUTURES_JPY = "JPY";	// Japanese Yen
	public static String TICK_NAME_CME_GLOBEX_FUTURES_CAD = "CAD";	// Canadian Dollar
	public static String TICK_NAME_CME_GLOBEX_FUTURES_RMB = "RMB";	// Chinese Renminbi
	public static String TICK_NAME_CME_GLOBEX_FUTURES_CHF = "CHF";	// Swiss Franc
	public static String TICK_NAME_CME_GLOBEX_FUTURES_AUD = "AUD";	// Australian Dollar
	public static String TICK_NAME_CME_GLOBEX_FUTURES_EUR = "EUR";	// Euro
	public static String TICK_NAME_CME_GLOBEX_FUTURES_RUR = "RUR";	// Russian Ruble
	public static String TICK_NAME_CME_GLOBEX_FUTURES_BRE = "BRE";	// Brazilian Real
	public static String TICK_NAME_CME_GLOBEX_FUTURES_GE = 	"GE";	// Euro-Dollar
	public static String TICK_NAME_CME_GLOBEX_FUTURES_LE = 	"LE";	// Live Cattle
	public static String TICK_NAME_CME_ECBOT_FUTURES_ZB =	"ZB";	// 30 Year T-Note
	public static String TICK_NAME_CME_ECBOT_FUTURES_ZN =	"ZN";	// 10 Year T-Note
	public static String TICK_NAME_CME_ECBOT_FUTURES_ZF =	"ZF";	// 5 Year T-Note
	public static String TICK_NAME_CME_ECBOT_FUTURES_ZT =	"ZT";	// 2 Year T-Note
	public static String TICK_NAME_CME_NYMEX_FUTURES_CL = 	"CL";	// Crude Oil
	public static String TICK_NAME_CME_NYMEX_FUTURES_SI = 	"SI";	// Silver
	public static String TICK_NAME_CME_NYMEX_FUTURES_GC = 	"GC";	// Gold

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
	
	public static final String ACCOUNT_CODE = "AccountCode";
	public static final String ACCOUNT_OR_GROUP = "AccountOrGroup";
	public static final String ACCOUNT_READY = "AccountReady";
	public static final String ACCOUNT_TYPE = "AccountType";
	public static final String ACCOUNT_TIME = "AccountTime";
	public static final String ACCOUNT_ACCRUED_CASH = "AccruedCash";
	public static final String ACCOUNT_ACCRUED_CASH_C = "AccruedCash-C";
	public static final String ACCOUNT_ACCRUED_CASH_S = "AccruedCash-S";
	public static final String ACCOUNT_ACCRUED_DIVIDEND = "AccruedDividend";
	public static final String ACCOUNT_ACCRUED_DIVIDEND_C = "AccruedDividend-C";
	public static final String ACCOUNT_ACCRUED_DIVIDEND_S = "AccruedDividend-S";
	public static final String ACCOUNT_AVAILABLE_FUNDS = "AvailableFunds"; // Useful
	public static final String ACCOUNT_AVAILABLE_FUNDS_C = "AvailableFunds-C";
	public static final String ACCOUNT_AVAILABLE_FUNDS_S = "AvailableFunds-S";
	public static final String ACCOUNT_BILLABLE = "Billable";
	public static final String ACCOUNT_BILLABLE_C = "Billable-C";
	public static final String ACCOUNT_BILLABLE_S = "Billable-S";
	public static final String ACCOUNT_BUYING_POWER = "BuyingPower"; // Useful
	public static final String ACCOUNT_CASH_BALANCE = "CashBalance";
	public static final String ACCOUNT_CORPORATE_BOND_VALUE = "CorporateBondValue";
	public static final String ACCOUNT_CURRENCY = "Currency";
	public static final String ACCOUNT_CUSHION = "Cushion";
	public static final String ACCOUNT_DAY_TRADES_REMAINING = "DayTradesRemaining";
	public static final String ACCOUNT_DAY_TRADES_REMAINING_T1 = "DayTradesRemainingT+1";
	public static final String ACCOUNT_DAY_TRADES_REMAINING_T2 = "DayTradesRemainingT+2";
	public static final String ACCOUNT_DAY_TRADES_REMAINING_T3 = "DayTradesRemainingT+3";
	public static final String ACCOUNT_DAY_TRADES_REMAINING_T4 = "DayTradesRemainingT+4";
	public static final String ACCOUNT_EQUITY_WITH_LOAN_VALUE = "EquityWithLoanValue";
	public static final String ACCOUNT_EQUITY_WITH_LOAN_VALUE_C = "EquityWithLoanValue-C";
	public static final String ACCOUNT_EQUITY_WITH_LOAN_VALUE_S = "EquityWithLoanValue-S";
	public static final String ACCOUNT_EXCESS_LIQUIDITY = "ExcessLiquidty";
	public static final String ACCOUNT_EXCESS_LIQUIDITY_C = "ExcessLiquidty-C";
	public static final String ACCOUNT_EXCESS_LIQUIDITY_S = "ExcessLiquidty-S";
	public static final String ACCOUNT_EXCHANGE_RATE = "ExchangeRate";
	public static final String ACCOUNT_FULL_AVAILABLE_FUNDS = "FullAvailableFunds";
	public static final String ACCOUNT_FULL_AVAILABLE_FUNDS_C = "FullAvailableFunds-C";
	public static final String ACCOUNT_FULL_AVAILABLE_FUNDS_S = "FullAvailableFunds-S";
	public static final String ACCOUNT_FULL_EXCESS_LIQUIDITY = "FullExcessLiquidity";
	public static final String ACCOUNT_FULL_EXCESS_LIQUIDITY_C = "FullExcessLiquidity-C";
	public static final String ACCOUNT_FULL_EXCESS_LIQUIDITY_S = "FullExcessLiquidity-S";
	public static final String ACCOUNT_FULL_INIT_MARGIN_REQ = "FullInitMarginReq";
	public static final String ACCOUNT_FULL_INIT_MARGIN_REQ_C = "FullInitMarginReq-C";
	public static final String ACCOUNT_FULL_INIT_MARGIN_REQ_S = "FullInitMarginReq-S";
	public static final String ACCOUNT_FULL_MAINT_MARGIN_REQ = "FullMaintMarginReq";
	public static final String ACCOUNT_FULL_MAINT_MARGIN_REQ_C = "FullMaintMarginReq-C";
	public static final String ACCOUNT_FULL_MAINT_MARGIN_REQ_S = "FullMaintMarginReq-S";
	public static final String ACCOUNT_FUND_VALUE = "FundValue";
	public static final String ACCOUNT_FUTURE_OPTION_VALUE = "FutureOptionValue";
	public static final String ACCOUNT_FUTURES_PNL = "FuturesPNL";
	public static final String ACCOUNT_FX_CASH_BALANCE = "FxCashBalance";
	public static final String ACCOUNT_GROSS_POSITION_VALUE = "GrossPositionValue";
	public static final String ACCOUNT_GROSS_POSITION_VALUE_S = "GrossPositionValue-S";
	public static final String ACCOUNT_INDIAN_STOCK_HAIRCUT = "IndianStockHaircut";
	public static final String ACCOUNT_INDIAN_STOCK_HAIRCUT_C = "IndianStockHaircut-C";
	public static final String ACCOUNT_INDIAN_STOCK_HAIRCUT_S = "IndianStockHaircut-S";
	public static final String ACCOUNT_INIT_MARGIN_REQ = "InitMarginReq";
	public static final String ACCOUNT_INIT_MARGIN_REQ_C = "InitMarginReq-C";
	public static final String ACCOUNT_INIT_MARGIN_REQ_S = "InitMarginReq-S";
	public static final String ACCOUNT_ISSUER_OPTION_VALUE = "IssuerOptionValue";
	public static final String ACCOUNT_LEVERAGE_S = "Leverage-S";
	public static final String ACCOUNT_LOOK_AHEAD_AVAILABLE_FUNDS = "LookAheadAvailableFunds";
	public static final String ACCOUNT_LOOK_AHEAD_AVAILABLE_FUNDS_C = "LookAheadAvailableFunds-C";
	public static final String ACCOUNT_LOOK_AHEAD_AVAILABLE_FUNDS_S = "LookAheadAvailableFunds-S";
	public static final String ACCOUNT_LOOK_AHEAD_EXCESS_LIQUIDITY = "LookAheadExcessLiquidity";
	public static final String ACCOUNT_LOOK_AHEAD_EXCESS_LIQUIDITY_C = "LookAheadExcessLiquidity-C";
	public static final String ACCOUNT_LOOK_AHEAD_EXCESS_LIQUIDITY_S = "LookAheadExcessLiquidity-S";
	public static final String ACCOUNT_LOOK_AHEAD_INIT_MARGIN_REQ = "LookAheadInitMarginReq";
	public static final String ACCOUNT_LOOK_AHEAD_INIT_MARGIN_REQ_C = "LookAheadInitMarginReq-C";
	public static final String ACCOUNT_LOOK_AHEAD_INIT_MARGIN_REQ_S = "LookAheadInitMarginReq-S";
	public static final String ACCOUNT_LOOK_AHEAD_MAINT_MARGIN_REQ = "LookAheadMaintMarginReq";
	public static final String ACCOUNT_LOOK_AHEAD_MAINT_MARGIN_REQ_C = "LookAheadMaintMarginReq-C";
	public static final String ACCOUNT_LOOK_AHEAD_MAINT_MARGIN_REQ_S = "LookAheadMaintMarginReq-S";
	public static final String ACCOUNT_LOOK_AHEAD_NEXT_CHANGE = "LookAheadNextChange";
	public static final String ACCOUNT_MAINT_MARGIN_REQ = "MaintMarginReq";
	public static final String ACCOUNT_MAINT_MARGIN_REQ_C = "MaintMarginReq-C";
	public static final String ACCOUNT_MAINT_MARGIN_REQ_S = "MaintMarginReq-S";
	public static final String ACCOUNT_MONEY_MARKET_FUND_VALUE = "MoneyMarketFundValue";
	public static final String ACCOUNT_MUTUAL_FUND_VALUE = "MutualFundValue";
	public static final String ACCOUNT_NET_DIVIDEND = "NetDividend";
	public static final String ACCOUNT_NET_LIQUIDATION = "NetLiquidation";
	public static final String ACCOUNT_NET_LIQUIDATION_C = "NetLiquidation-C";
	public static final String ACCOUNT_NET_LIQUIDATION_S = "NetLiquidation-S";
	public static final String ACCOUNT_NET_LIQUIDATION_BY_CURRENCY = "NetLiquidationByCurrency";
	public static final String ACCOUNT_OPTION_MARKET_VALUE = "OptionMarketValue";
	public static final String ACCOUNT_PA_SHARES_VALUE = "PASharesValue";
	public static final String ACCOUNT_PA_SHARES_VALUE_C = "PASharesValue-C";
	public static final String ACCOUNT_PA_SHARES_VALUE_S = "PASharesValue-S";
	public static final String ACCOUNT_POST_EXPIRATION_EXCESS = "PostExpirationExcess";
	public static final String ACCOUNT_POST_EXPIRATION_EXCESS_C = "PostExpirationExcess-C";
	public static final String ACCOUNT_POST_EXPIRATION_EXCESS_S = "PostExpirationExcess-S";
	public static final String ACCOUNT_POST_EXPIRATION_MARGIN = "PostExpirationMargin";
	public static final String ACCOUNT_POST_EXPIRATION_MARGIN_C = "PostExpirationMargin-C";
	public static final String ACCOUNT_POST_EXPIRATION_MARGIN_S = "PostExpirationMargin-S";
	public static final String ACCOUNT_PREVIOUS_DAY_EQUITY_WITH_LOAN_VALUE = "PreviousDayEquityWithLoanValue";
	public static final String ACCOUNT_PREVIOUS_DAY_EQUITY_WITH_LOAN_VALUE_S = "PreviousDayEquityWithLoanValue-S";
	public static final String ACCOUNT_REAL_CURRENCY = "RealCurrency";
	public static final String ACCOUNT_REALIZED_PNL = "RealizedPnL";
	public static final String ACCOUNT_REG_T_EQUITY = "RegTEquity";
	public static final String ACCOUNT_REG_T_EQUITY_S = "RegTEquity-S";
	public static final String ACCOUNT_REG_T_MARGIN = "RegTMargin";
	public static final String ACCOUNT_REG_T_MARGIN_S = "RegTMargin-S";
	public static final String ACCOUNT_SMA = "SMA";
	public static final String ACCOUNT_SMA_S = "SMA-S";
	public static final String ACCOUNT_SEGMENT_TITLE_C = "SegmentTitle-C";
	public static final String ACCOUNT_SEGMENT_TITLE_S = "SegmentTitle-S";
	public static final String ACCOUNT_STOCK_MARKET_VALUE = "StockMarketValue";
	public static final String ACCOUNT_TBILL_VALUE = "TBillValue";
	public static final String ACCOUNT_TBOND_VALUE = "TBondValue";
	public static final String ACCOUNT_TOTAL_CASH_BALANCE = "TotalCashBalance";
	public static final String ACCOUNT_TOTAL_CASH_VALUE = "TotalCashValue";
	public static final String ACCOUNT_TOTAL_CASH_VALUE_C = "TotalCashValue-C";
	public static final String ACCOUNT_TOTAL_CASH_VALUE_S = "TotalCashValue-S";
	public static final String ACCOUNT_TRADING_TYPE_S = "TradingType-S";
	public static final String ACCOUNT_UNREALIZED_PNL = "UnrealizedPnL";
	public static final String ACCOUNT_WARRANT_VALUE = "WarrantValue";
	public static final String ACCOUNT_WHAT_IF_PM_ENABLED = "WhatIfPMEnabled";
	
	public static final HashMap<String, String> SECURITY_TYPE_EXCHANGE_HASH = new HashMap<String, String>();
	public static final HashMap<BAR_SIZE, String> BAR_DURATION_IB_BAR_SIZE = new HashMap<BAR_SIZE, String>();
	public static final HashMap<BarKey, Integer> BARKEY_TICKER_ID_HASH = new HashMap<BarKey, Integer>();
	public static final HashMap<String, String> TICKER_SECURITY_TYPE_HASH = new HashMap<String, String>();
	public static final HashMap<String, String> TICKER_EXCHANGE_HASH = new HashMap<String, String>();
	public static final HashMap<String, Double> TICKER_PIP_SIZE_HASH = new HashMap<String, Double>();
	public static final HashMap<String, String> FUTURE_SYMBOL_MULTIPLIER_HASH = new HashMap<String, String>();
	public static final ArrayList<String> TICK_NAMES = new ArrayList<String>();
	
	static {
//		SECURITY_TYPE_EXCHANGE_HASH.put("CASH", "FXCONV");		// Non-Leveraged Forex
		SECURITY_TYPE_EXCHANGE_HASH.put("CASH", "IDEALPRO");	// Leveraged Forex
		SECURITY_TYPE_EXCHANGE_HASH.put("STK", "SMART");
		SECURITY_TYPE_EXCHANGE_HASH.put("FUT", "GLOBEX");
		
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_30S, "30 secs");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_1M, "1 min");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_3M, "3 mins");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_5M, "5 mins");	
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_15M, "15 mins");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_30M, "30 mins");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_1H, "1 hour");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_2H, "2 hours");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_4H, "4 hours");
		
		TICK_NAMES.add(TICK_NAME_CME_ECBOT_FUTURES_ZB);
		TICK_NAMES.add(TICK_NAME_CME_ECBOT_FUTURES_ZF);
		TICK_NAMES.add(TICK_NAME_CME_ECBOT_FUTURES_ZN);
		TICK_NAMES.add(TICK_NAME_CME_ECBOT_FUTURES_ZT);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_AUD);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_BRE);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_CAD);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_CHF);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_ES);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_EUR);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_GBP);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_GE);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_JPY);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_LE);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_NQ);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_RMB);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_RUR);
		TICK_NAMES.add(TICK_NAME_CME_GLOBEX_FUTURES_SPX);
		TICK_NAMES.add(TICK_NAME_CME_NYMEX_FUTURES_CL);
		TICK_NAMES.add(TICK_NAME_CME_NYMEX_FUTURES_GC);
		TICK_NAMES.add(TICK_NAME_CME_NYMEX_FUTURES_SI);
		TICK_NAMES.add(TICK_NAME_FOREX_AUD_JPY);
		TICK_NAMES.add(TICK_NAME_FOREX_AUD_USD);
		TICK_NAMES.add(TICK_NAME_FOREX_EUR_CHF);
		TICK_NAMES.add(TICK_NAME_FOREX_EUR_GBP);
		TICK_NAMES.add(TICK_NAME_FOREX_EUR_JPY);
		TICK_NAMES.add(TICK_NAME_FOREX_EUR_USD);
		TICK_NAMES.add(TICK_NAME_FOREX_GBP_JPY);
		TICK_NAMES.add(TICK_NAME_FOREX_GBP_USD);
		TICK_NAMES.add(TICK_NAME_FOREX_NZD_USD);
		TICK_NAMES.add(TICK_NAME_FOREX_USD_CAD);
		TICK_NAMES.add(TICK_NAME_FOREX_USD_CHF);
		TICK_NAMES.add(TICK_NAME_FOREX_USD_JPY);
		
		for (String tickName : TICK_NAMES) {
			for (BAR_SIZE duration : BAR_SIZE.values()) {
				BarKey bk = new BarKey(tickName, duration);
				BARKEY_TICKER_ID_HASH.put(bk, bk.toString().hashCode());
			}
		}
	
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
		
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_SPX, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_ES, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_NQ, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GBP, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_JPY, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CAD, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RMB, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CHF, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_AUD, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_EUR, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RUR, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_BRE, "FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GE, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_LE, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZB, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZN, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZF, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZT, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_CL, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_SI, 	"FUT");
		TICKER_SECURITY_TYPE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_GC, 	"FUT");
		
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_SPX, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_ES, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_NQ, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GBP, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_JPY, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CAD, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RMB, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CHF, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_AUD, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_EUR, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RUR, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_BRE, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GE, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_LE, 	"GLOBEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZB, 	"ECBOT");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZN, 	"ECBOT");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZF, 	"ECBOT");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZT, 	"ECBOT");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_CL, 	"NYMEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_SI, 	"NYMEX");
		TICKER_EXCHANGE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_GC, 	"NYMEX");
		
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_AUD_JPY, .01);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_AUD_USD, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_EUR_CHF, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_EUR_GBP, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_EUR_JPY, .01);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_EUR_USD, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_GBP_JPY, .01);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_GBP_USD, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_NZD_USD, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_USD_CAD, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_USD_CHF, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_FOREX_USD_JPY, .01);
		
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_SPX, .1);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_ES,  .25);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_NQ,  .25);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GBP, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_JPY, .0000005);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CAD, .00005);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RMB, .00001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CHF, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_AUD, .0001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_EUR, .00005);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RUR, .000005);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_BRE, .00005);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GE,  .005);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_LE,  .00025);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZB,   .015625);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZN,   .015625);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZF,   .0078125);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZT,   .0078125);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_CL,   .01);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_SI,   .001);
		TICKER_PIP_SIZE_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_GC,   .1);
		
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_SPX, "250");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_ES,  "50");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_NQ,  "20");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GBP, "62500");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_JPY, "12500000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CAD, "100000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RMB, "1000000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_CHF, "125000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_AUD, "100000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_EUR, "125000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_RUR, "2500000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_BRE, "100000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_GE,  "2500");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_GLOBEX_FUTURES_LE,  "40000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZB,   "1000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZN,   "1000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZF,   "1000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_ECBOT_FUTURES_ZT,   "2000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_CL, 	"1000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_SI, 	"5000");
		FUTURE_SYMBOL_MULTIPLIER_HASH.put(TICK_NAME_CME_NYMEX_FUTURES_GC, 	"100");
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
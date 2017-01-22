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
	public static final HashMap<String, Double> TICKER_PIP_SIZE_HASH = new HashMap<String, Double>();
	
	static {
		SECURITY_TYPE_EXCHANGE_HASH.put("CASH", "IDEALPRO");
		SECURITY_TYPE_EXCHANGE_HASH.put("STK", "SMART");
		
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_30S, "30 secs");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_1M, "1 min");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_3M, "3 mins");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_5M, "5 mins");	
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_15M, "15 mins");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_30M, "30 mins");
		BAR_DURATION_IB_BAR_SIZE.put(BAR_SIZE.BAR_1H, "1 hour");
		
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
		
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_JPY, BAR_SIZE.BAR_5M), 37);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_USD, BAR_SIZE.BAR_5M), 38);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_CHF, BAR_SIZE.BAR_5M), 39);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_GBP, BAR_SIZE.BAR_5M), 40);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_JPY, BAR_SIZE.BAR_5M), 41);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_USD, BAR_SIZE.BAR_5M), 42);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_JPY, BAR_SIZE.BAR_5M), 43);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_USD, BAR_SIZE.BAR_5M), 44);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_NZD_USD, BAR_SIZE.BAR_5M), 45);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CAD, BAR_SIZE.BAR_5M), 46);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CHF, BAR_SIZE.BAR_5M), 47);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_JPY, BAR_SIZE.BAR_5M), 48);
		
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_JPY, BAR_SIZE.BAR_15M), 49);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_USD, BAR_SIZE.BAR_15M), 50);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_CHF, BAR_SIZE.BAR_15M), 51);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_GBP, BAR_SIZE.BAR_15M), 52);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_JPY, BAR_SIZE.BAR_15M), 53);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_USD, BAR_SIZE.BAR_15M), 54);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_JPY, BAR_SIZE.BAR_15M), 55);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_USD, BAR_SIZE.BAR_15M), 56);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_NZD_USD, BAR_SIZE.BAR_15M), 57);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CAD, BAR_SIZE.BAR_15M), 58);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CHF, BAR_SIZE.BAR_15M), 59);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_JPY, BAR_SIZE.BAR_15M), 60);
		
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_JPY, BAR_SIZE.BAR_30M), 61);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_AUD_USD, BAR_SIZE.BAR_30M), 62);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_CHF, BAR_SIZE.BAR_30M), 63);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_GBP, BAR_SIZE.BAR_30M), 64);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_JPY, BAR_SIZE.BAR_30M), 65);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_EUR_USD, BAR_SIZE.BAR_30M), 66);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_JPY, BAR_SIZE.BAR_30M), 67);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_GBP_USD, BAR_SIZE.BAR_30M), 68);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_NZD_USD, BAR_SIZE.BAR_30M), 69);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CAD, BAR_SIZE.BAR_30M), 70);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_CHF, BAR_SIZE.BAR_30M), 71);
		BARKEY_TICKER_ID_HASH.put(new BarKey(TICK_NAME_FOREX_USD_JPY, BAR_SIZE.BAR_30M), 72);
		
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
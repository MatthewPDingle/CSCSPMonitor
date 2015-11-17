package data.downloaders.interactivebrokers;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TagValue;
import com.ib.client.UnderComp;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.TickConstants;
import dbio.QueryManager;
import utils.CalendarUtils;

public class IBDataDownloader implements EWrapper {

	EClientSocket client = new EClientSocket(this);
	
	private ArrayList<Bar> bars = new ArrayList<Bar>(); // Should come in oldest to newest
	private String symbol;
	private BAR_SIZE barSize;
	private int barSeconds;
	private Calendar realtimeBarStart = null;
	private float realtimeBarOpen;
	private float realtimeBarClose;
	private float realtimeBarHigh;
	private float realtimeBarLow;
	private float realtimeBarVolume;
	private int lastProcessedRequestID;
	
	public static void main(String[] args) {
		try {
			IBDataDownloader ibdd = new IBDataDownloader();
			
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS zzz");
			String sStart = "02/01/2015 00:00:00.000 EST";
			String sEnd = "03/02/2015 00:00:00.000 EST";
			Calendar start = Calendar.getInstance();
			start.setTime(sdf.parse(sStart));
			Calendar end = Calendar.getInstance();
			end.setTime(sdf.parse(sEnd));
			
			ibdd.connect();
//			ArrayList<Bar> bars = ibdd.downloadHistoricalBars(IBConstants.TICK_NAME_FOREX_EUR_USD, Constants.BAR_SIZE.BAR_1M, start, end, "CASH", false);
//			ibdd.disconnect();
//			for (Bar bar : bars) {
//				QueryManager.insertOrUpdateIntoBar(bar);
//			}
			
			ibdd.downloadRealtimeBars(IBConstants.TICK_NAME_FOREX_EUR_USD, Constants.BAR_SIZE.BAR_1M, "CASH", false);
			
			Thread.sleep(240 * 1000);
			
			ibdd.cancelRealtimeBars(new BarKey(IBConstants.TICK_NAME_FOREX_EUR_USD, Constants.BAR_SIZE.BAR_1M));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void connect() {
		if (!client.isConnected()) {
			client.eConnect("localhost", IBConstants.IB_API_PORT, 1);
			while (!client.isConnected()) {
			}
		}
	}
	
	public void disconnect() {
		if (client.isConnected()) {
			client.eDisconnect();
		}
	}
	
	public void downloadRealtimeBars(String forexSymbol, Constants.BAR_SIZE barSize, String securityType, boolean regularTradingHoursOnly) {
		try {
			if (client.isConnected()) {
				// Record what this IBDataDownloader will be processing once it starts getting data ba
				this.symbol = forexSymbol;
				this.barSize = barSize;
				
				// Build contract 
				Contract contract = new Contract();
				contract.m_conId = 0;
				if (securityType.equals("CASH")) {
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(forexSymbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(forexSymbol);
				}
				contract.m_secType = securityType;
				contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				
				// Need to make this unique per ticker so I setup this hash
				int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(new BarKey(forexSymbol, barSize));
				
				Vector<TagValue> chartOptions = new Vector<TagValue>();
				
				// Only 5 sec real time bars are supported so I'll have to do post-processing to make my own size bars with beer and hookers.
				client.reqRealTimeBars(tickerID, contract, 5, "MIDPOINT", regularTradingHoursOnly, chartOptions);
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void cancelRealtimeBars(BarKey bk) {
		try {
			if (client.isConnected()) {
				// Need to make this unique per ticker so I setup this hash
				int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(bk);
				client.cancelRealTimeBars(tickerID);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Bar> downloadHistoricalBars(String forexSymbol, Constants.BAR_SIZE barSize, Calendar startDateTime, Calendar endDateTime, String securityType, boolean regularTradingHoursOnly) {
		try {
			if (client.isConnected()) {
				// Record what this IBDataDownloader will be processing once it starts getting data ba
				this.symbol = forexSymbol;
				this.barSize = barSize;
				
				// Build contract 
				Contract contract = new Contract();
				contract.m_conId = 0;
				if (securityType.equals("CASH")) {
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(forexSymbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(forexSymbol);
				}
				contract.m_secType = securityType;
				contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				
				Vector<TagValue> chartOptions = new Vector<TagValue>();
				
				String whatToShow = "TRADES";
				if (securityType.equals("CASH")) {
					whatToShow = "BID_ASK";
				}
				
				switch (barSize) {
					case BAR_30S:
						this.barSeconds = 30;
						break;	
					case BAR_1M:
						this.barSeconds = 60;
						break;
					case BAR_3M:
						this.barSeconds = 180;
						break;
					default:
						break;
				}
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss zzz");
				long periodMS = endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis();
				long periodS = periodMS / 1000;
				
				int requestCounter = 0;
				if (periodS >= 60 * 60 * 24) { // At least a day of data.  Will have to make multiple requests to cover more than one day.
					while (startDateTime.getTimeInMillis() < endDateTime.getTimeInMillis()) {
						String durationString = "1 D";
						int durationMS = 1000 * 60 * 60 * 24;
						
						Calendar thisEndDateTime = Calendar.getInstance();
						thisEndDateTime.setTimeInMillis(startDateTime.getTimeInMillis());
						thisEndDateTime.add(Calendar.MILLISECOND, durationMS);
						String endDateTimeString = sdf.format(thisEndDateTime.getTime());
						
						System.out.println(startDateTime.getTime().toString());
						client.reqHistoricalData(requestCounter++, contract, endDateTimeString, durationString, IBConstants.BAR_DURATION_IB_BAR_SIZE.get(barSize), whatToShow, (regularTradingHoursOnly ? 1 : 0), 1, chartOptions);
						
						// Wait half a sec to avoid pacing violations and set the timeframe forward "one duration".
						Thread.sleep(1000);
						startDateTime.add(Calendar.MILLISECOND, durationMS);
					}
				}
				
//				while (requestCounter != lastProcessedRequestID) {
					Thread.sleep(5000);
//				}
				// We've downloaded all the data.  Add in the change & gap values and return it
				Float previousClose = null;
				DecimalFormat df = new DecimalFormat("#.#####");
				df.setRoundingMode(RoundingMode.HALF_UP);
				for (Bar bar : this.bars) { // Oldest to newest
					Float change = null;
					Float gap = null;
					if (previousClose != null) {
						change = bar.close - previousClose; 
						gap = bar.open - previousClose;
					}
					if (change != null) {
						bar.change = Float.parseFloat(df.format(change));
					}
					if (gap != null) {
						bar.gap = Float.parseFloat(df.format(gap));
					}
					previousClose = bar.close;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bars;
	}
	
	@Override
	public void error(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void error(String str) {
		System.out.println("Error " + str);
		
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		System.out.println("Error " + id + ", " + errorCode + ", " + errorMsg);
	}

	@Override
	public void connectionClosed() {
		System.out.println("connectionClosed()");
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		System.out.println("tickPrice(...)");
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		System.out.println("tickSize(...)");
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {
		System.out.println("tickOptionComputation(...)");
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		System.out.println("tickGeneric(...)");
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		System.out.println("tickString(...)");
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		System.out.println("tickEFP(...)");
	}

	@Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId,int parentId, double lastFillPrice, int clientId, String whyHeld) {
		System.out.println("orderStatus(...)");
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		System.out.println("openOrder(...)");
	}

	@Override
	public void openOrderEnd() {
		System.out.println("openOrderEnd()");
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		System.out.println("updateAccountValue(...)");
	}

	@Override
	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		System.out.println("updatePortfolio(...)");
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		System.out.println("updateAccountTime(...)");
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		System.out.println("accountDownloadEnd(...)");
	}

	@Override
	public void nextValidId(int orderId) {
		System.out.println("nextValidId(...) - " + orderId);
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println("contractDetails(...)");
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println("bondContractDetails(...)");
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		System.out.println("contractDetailsEnd(...)");
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		System.out.println("execDetails(...)");
	}

	@Override
	public void execDetailsEnd(int reqId) {
		System.out.println("execDetailsEnd(...)");
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		System.out.println("updateMktDepth(...)");
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
		System.out.println("updateMktDepthL2(...)");
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		System.out.println("updateNewsBulletin(...)");
	}

	@Override
	public void managedAccounts(String accountsList) {
		System.out.println("managedAccounts(...)");
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		System.out.println("receiveFA(...)");
	}

	@Override
	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) {
		try {
			if (date.contains("finished")) {
				lastProcessedRequestID++;
				System.out.println("Batch " + lastProcessedRequestID + " done");
				return;
			}
			
			lastProcessedRequestID = reqId;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
			Calendar periodStart = Calendar.getInstance();
			periodStart.setTimeInMillis(sdf.parse(date).getTime());
			
			Calendar periodEnd = Calendar.getInstance();
			periodEnd.setTime(periodStart.getTime());
			periodEnd.add(Calendar.SECOND, barSeconds);
			
			// We'll fill in the change & gap later;
			Bar bar = new Bar(symbol, (float)open, (float)close, (float)high, (float)low, null, -1f, null, null, null, periodStart, periodEnd, barSize, false);
			this.bars.add(bar);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scannerParameters(String xml) {
		System.out.println("scannerParameters(...)");
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
		System.out.println("scannerData(...)");
	}

	@Override
	public void scannerDataEnd(int reqId) {
		System.out.println("scannerDataEnd(...)");
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
		System.out.println("realtimeBar(...)");
		try {
		
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time * 1000);
			
			if (realtimeBarStart == null) {
				realtimeBarStart = CalendarUtils.getBarStart(c, barSize);
				return;
			}
			
			if (realtimeBarStart.getTimeInMillis() == CalendarUtils.getBarStart(c, barSize).getTimeInMillis()) {
				// Same bar
				if (high > realtimeBarHigh) {
					realtimeBarHigh = (float)high;
				}
				if (low < realtimeBarLow) {
					realtimeBarLow = (float)low;
				}
				
				Calendar realtimeBarEnd = Calendar.getInstance();
				realtimeBarEnd.setTimeInMillis(realtimeBarStart.getTimeInMillis());
				realtimeBarEnd.add(Calendar.SECOND, 5);
				if (realtimeBarStart.getTimeInMillis() == CalendarUtils.getBarStart(realtimeBarEnd, barSize).getTimeInMillis()) {
					// Last sub-bar in the bar
					realtimeBarClose = (float)close;
				}
			}
			else {
				// New bar
				Calendar barEnd = Calendar.getInstance();
				barEnd.setTimeInMillis(realtimeBarStart.getTimeInMillis());
				Calendar barStart = Calendar.getInstance();
				barStart = CalendarUtils.addBars(barEnd, barSize, -1);
				
				Bar bar = new Bar(symbol, realtimeBarOpen, realtimeBarClose, realtimeBarHigh, realtimeBarLow, null, realtimeBarVolume, null, null, null, barStart, barEnd, barSize, false);
				System.out.println(bar.toString());
				
				realtimeBarOpen = (float)open;
				realtimeBarClose = (float)close;
				realtimeBarHigh = (float)high;
				realtimeBarLow = (float)low;
				realtimeBarVolume = (float)volume;
			}
			
			System.out.println(c.getTime().toString());
			System.out.println(open + ", " + close + ", " + high + ", " + low);
			System.out.println(reqId + ", " + volume + ", " + wap + ", " + count);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void currentTime(long time) {
		System.out.println("currentTime(...)");
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		System.out.println("fundamentalData(...)");
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		System.out.println("deltaNeutralValidation(...)");
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		System.out.println("tickSnapshotEnd(...)");
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		System.out.println("marketDataType(...)");
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		System.out.println("commissionReport(...)");
	}

	@Override
	public void position(String account, Contract contract, int pos, double avgCost) {
		System.out.println("position(...)");
	}

	@Override
	public void positionEnd() {
		System.out.println("positionEnd(...)");
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		System.out.println("accountSummary(...)");
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		System.out.println("accountSummaryEnd(...)");
	}

	@Override
	public void verifyMessageAPI(String apiData) {
		System.out.println("verifyMessageAPI(...)");
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		System.out.println("verifyCompleted(...)");
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		System.out.println("displayGroupList(...)");
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		System.out.println("displayGroupUpdated(...)");
	}
}
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
import data.Bar;
import data.BarKey;
import dbio.QueryManager;
import status.StatusSingleton;
import utils.CalendarUtils;

public class IBWorker implements EWrapper {

	EClientSocket client = new EClientSocket(this);
	
	private DecimalFormat df = null;
	private ArrayList<Bar> historicalBars = new ArrayList<Bar>(); // Should come in oldest to newest
	private BarKey barKey;
	private int barSeconds;
	private Calendar fullBarStart = null;
	private Calendar fullBarEnd = null;
	private float realtimeBarOpen;
	private float realtimeBarClose;
	private float realtimeBarHigh;
	private float realtimeBarLow;
	private float realtimeBarVolume;
	private int realtimeBarSubBarCounter;
	private int realtimeBarNumSubBarsInFullBar;
	private float realtimeBarLastBarOpen;
	private float realtimeBarLastBarClose;
	private int lastProcessedRequestID;
	private StatusSingleton ss;
	
	public static void main(String[] args) {
		try {
			IBWorker ibdd = new IBWorker(new BarKey(IBConstants.TICK_NAME_FOREX_EUR_USD, Constants.BAR_SIZE.BAR_1M));
			
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS zzz");
			String sStart = "11/28/2015 00:00:00.000 EST";
			String sEnd = "11/28/2015 00:00:00.000 EST";
			Calendar start = Calendar.getInstance();
			start.setTime(sdf.parse(sStart));
			Calendar end = Calendar.getInstance();
			end.setTime(sdf.parse(sEnd));
				
			// Figure out when to start the historical data download, and make the end equal to right now.
			Bar mostRecentDBBar = QueryManager.getMostRecentBar(ibdd.barKey, Calendar.getInstance());
			if (mostRecentDBBar != null) {
				start.setTimeInMillis(mostRecentDBBar.periodStart.getTimeInMillis());
				start = CalendarUtils.addBars(start, ibdd.barKey.duration, -2); // Go back 2 additional bars so we cover partial bars & get the 2nd to last one's open & close.
			}
			end.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
			end.set(Calendar.MILLISECOND, 0);
			end.set(Calendar.SECOND, 0);
			end.set(Calendar.MINUTE, 0);
			end.set(Calendar.HOUR, 0);
			end.add(Calendar.DATE, 1);
			end.add(Calendar.HOUR, -1); // -1 for CST

			System.out.println("Start: " + start.getTime().toString());
			System.out.println("End: " + end.getTime().toString());
			
			ibdd.connect();
			ArrayList<Bar> bars = ibdd.downloadHistoricalBars(start, end, false);
			ibdd.disconnect();
			for (Bar bar : bars) {
				QueryManager.insertOrUpdateIntoBar(bar);
			}
			
//			ibdd.preloadRealtimeBarWithLastHistoricalBar();
			
//			ibdd.connect();
//			ibdd.downloadRealtimeBars();
//			Thread.sleep(200 * 1000);	
//			ibdd.cancelRealtimeBars(ibdd.barKey);
//			ibdd.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IBWorker(BarKey bk) {
		super();
		
		ss = StatusSingleton.getInstance();
		
		this.df = new DecimalFormat("#.######");
		this.df.setRoundingMode(RoundingMode.HALF_UP);
		
		this.barKey = bk;
		this.realtimeBarOpen = 0;
		this.realtimeBarClose = 0;
		this.realtimeBarHigh = 0;
		this.realtimeBarLow = 1000000;
		this.realtimeBarVolume = -1;
		this.realtimeBarSubBarCounter = 0;
		this.realtimeBarNumSubBarsInFullBar = 1;
		this.realtimeBarLastBarOpen = 0;
		this.realtimeBarLastBarClose = 0;
	}
	
	private void preloadRealtimeBarWithLastHistoricalBar() {
		if (historicalBars.size() > 1) {
			Bar newestHistoricalBar = historicalBars.get(historicalBars.size() - 1);
			realtimeBarOpen = newestHistoricalBar.open;
			realtimeBarClose = newestHistoricalBar.close;
			realtimeBarHigh = newestHistoricalBar.high;
			realtimeBarLow = newestHistoricalBar.low;
			realtimeBarVolume = newestHistoricalBar.volume;
			
			Bar secondNewestHistoricalBar = historicalBars.get(historicalBars.size() - 2);
			realtimeBarLastBarOpen = secondNewestHistoricalBar.open;
			realtimeBarLastBarClose = secondNewestHistoricalBar.close;
		}
	}

	public boolean connect() {
		try {
			if (!client.isConnected()) {
				client.eConnect("localhost", IBConstants.IB_API_PORT, 1);
				int waitTimeMS = 0;
				while (!client.isConnected()) {
					Thread.sleep(100);
					waitTimeMS += 100;
					if (waitTimeMS >= IBConstants.CONNECT_TIMEOUT_MS) {
						return false;
					}
				}
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void disconnect() {
		if (client.isConnected()) {
			client.eDisconnect();
		}
	}
	
	public void downloadRealtimeBars() {
		try {
			if (!client.isConnected()) {
				connect();
			}
			if (client.isConnected()) {
				// Figure out when to start the historical data download, and make the end equal to right now.
				Calendar start = Calendar.getInstance();
				Calendar end = Calendar.getInstance();
				Bar mostRecentDBBar = QueryManager.getMostRecentBar(barKey, Calendar.getInstance());
				if (mostRecentDBBar == null) {
					start.set(Calendar.YEAR, 2015);
					start.set(Calendar.MONTH, 0);
					start.set(Calendar.DAY_OF_MONTH, 1);
					start.set(Calendar.HOUR, 0);
					start.set(Calendar.MINUTE, 0);
					start.set(Calendar.SECOND, 0);
					start.set(Calendar.MILLISECOND, 0);
				}
				else {
					start.setTimeInMillis(mostRecentDBBar.periodStart.getTimeInMillis());
					start = CalendarUtils.addBars(start, barKey.duration, -2); // Go back 2 additional bars so we cover partial bars & get the 2nd to last one's open & close.
				}
				end.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
				end.set(Calendar.MILLISECOND, 0);
				end.set(Calendar.SECOND, 0);
				end.set(Calendar.MINUTE, 0);
				end.set(Calendar.HOUR, 0);
				end.add(Calendar.DATE, 1);
				end.add(Calendar.HOUR, -1); // -1 for CST
				
				// Download any needed historical data first so we're caught up and ready for realtime bars
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") downloading historical data to catch up to realtime bars...");
				ArrayList<Bar> bars = downloadHistoricalBars(start, end, false);
				if (bars != null) {
					ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") downloaded " + bars.size() + " historical bars.");
				}
				for (Bar bar : bars) {
					QueryManager.insertOrUpdateIntoBar(bar);
				}
				preloadRealtimeBarWithLastHistoricalBar();
				
				// Now prepare for realtime bars
				switch(barKey.duration) {
					case BAR_30S: 
						realtimeBarNumSubBarsInFullBar = 6;
						break;
					case BAR_1M:
						realtimeBarNumSubBarsInFullBar = 12;
						break;
					case BAR_3M:
						realtimeBarNumSubBarsInFullBar = 36;
						break;
					default:
						throw new Exception("Bar size not supported");
				}
				
				// Build contract 
				Contract contract = new Contract();
				contract.m_conId = 0;
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(barKey.symbol);
				if (securityType.equals("CASH")) {
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(barKey.symbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(barKey.symbol);
				}
				contract.m_secType = securityType;
				contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				
				// Need to make this unique per ticker so I setup this hash
				int tickerID = IBConstants.BARKEY_TICKER_ID_HASH.get(barKey);
				
				Vector<TagValue> chartOptions = new Vector<TagValue>();
				
				// Only 5 sec real time bars are supported so I'll have to do post-processing to make my own size bars with beer and hookers.
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") now starting realtime bars.");
				client.reqRealTimeBars(tickerID, contract, 5, "MIDPOINT", false, chartOptions);
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
	
	public ArrayList<Bar> downloadHistoricalBars(Calendar startDateTime, Calendar endDateTime, boolean regularTradingHoursOnly) {
		try {
			if (!client.isConnected()) {
				connect();
			}
			if (client.isConnected()) {
				// Build contract 
				Contract contract = new Contract();
				contract.m_conId = 0;
				String securityType = IBConstants.TICKER_SECURITY_TYPE_HASH.get(barKey.symbol);
				if (securityType.equals("CASH")) {
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(barKey.symbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(barKey.symbol);
				}
				contract.m_secType = securityType;
				contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				
				Vector<TagValue> chartOptions = new Vector<TagValue>();
				
				String whatToShow = "MIDPOINT";
//				if (securityType.equals("CASH")) {
//					whatToShow = "BID_ASK";
//				}
				
				switch (barKey.duration) {
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
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
				long periodMS = endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis();
				long periodS = periodMS / 1000;
				
				int requestCounter = 0;
				if (periodS >= 60 * 60 * 24) { // At least a day of data.  Will have to make multiple requests to cover more than one day.
					while (startDateTime.getTimeInMillis() < endDateTime.getTimeInMillis()) {
						String durationString = "86400 S";
						int durationMS = 1000 * 60 * 60 * 24;
						
						Calendar thisEndDateTime = Calendar.getInstance();
						thisEndDateTime.setTimeInMillis(startDateTime.getTimeInMillis());
						thisEndDateTime.add(Calendar.MILLISECOND, durationMS);
						String endDateTimeString = sdf.format(thisEndDateTime.getTime());

						System.out.println(startDateTime.getTime().toString());
						client.reqHistoricalData(requestCounter++, contract, endDateTimeString, durationString, IBConstants.BAR_DURATION_IB_BAR_SIZE.get(barKey.duration), whatToShow, (regularTradingHoursOnly ? 1 : 0), 1, chartOptions);
						
						// Wait half a sec to avoid pacing violations and set the timeframe forward "one duration".
						Thread.sleep(1000);
						startDateTime.add(Calendar.MILLISECOND, durationMS);
					}
				}
				else { // Less than a day of data.  Can do everything in one request
					int durationMS = (int)(endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis());
					String durationString = "" + (durationMS / 1000) + " S";
					
					Calendar thisEndDateTime = Calendar.getInstance();
					thisEndDateTime.setTimeInMillis(startDateTime.getTimeInMillis());
					thisEndDateTime.add(Calendar.MILLISECOND, durationMS);
					String endDateTimeString = sdf.format(thisEndDateTime.getTime());
					
					System.out.println(startDateTime.getTime().toString());
					client.reqHistoricalData(requestCounter++, contract, endDateTimeString, durationString, IBConstants.BAR_DURATION_IB_BAR_SIZE.get(barKey.duration), whatToShow, (regularTradingHoursOnly ? 1 : 0), 1, chartOptions);
				}
				
				Thread.sleep(2000);

				// We've downloaded all the data.  Add in the change & gap values and return it
				Float previousClose = null;
				
				for (Bar bar : this.historicalBars) { // Oldest to newest
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
					// If this is the first historical bar, see if we can find the previous bar in the DB so we can calculate change & gap
					if (change == null || gap == null) {
						Bar previousBar = QueryManager.getMostRecentBar(barKey, bar.periodStart);
						if (previousBar != null) {
							if (change == null) {
								bar.change = new Float(df.format(bar.close - previousBar.close));
							}
							if (gap == null) {
								bar.gap = new Float(df.format(bar.open - previousBar.close));
							}
						}
					}
					
					if (this.historicalBars.indexOf(bar) == this.historicalBars.size() - 1) {
						bar.partial = true;
					}
					previousClose = bar.close;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return historicalBars;
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
			Bar bar = new Bar(barKey.symbol, new Float(df.format(open)), new Float(df.format(close)), new Float(df.format(high)), new Float(df.format(low)), null, -1f, null, null, null, periodStart, periodEnd, barKey.duration, false);
			this.historicalBars.add(bar);
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
			
			Calendar subBarStart = CalendarUtils.getBarStart(c, barKey.duration);
			
			
			if (fullBarStart == null) {
				fullBarStart = CalendarUtils.getBarStart(c, barKey.duration);
				fullBarEnd = CalendarUtils.getBarEnd(c, barKey.duration);
				return;
			}
			
			if (fullBarStart.getTimeInMillis() == subBarStart.getTimeInMillis()) {
				// Same bar
				if (high > realtimeBarHigh) {
					realtimeBarHigh = (float)high;
				}
				if (low < realtimeBarLow) {
					realtimeBarLow = (float)low;
				}
				
				realtimeBarSubBarCounter++;
				
				Calendar subBarEnd = Calendar.getInstance();
				subBarEnd.setTimeInMillis(fullBarStart.getTimeInMillis());
				subBarEnd.add(Calendar.SECOND, 5);
				if (fullBarStart.getTimeInMillis() == CalendarUtils.getBarStart(subBarEnd, barKey.duration).getTimeInMillis()) {
					// Last sub-bar in the bar
					realtimeBarClose = (float)close;
				}
				
				// Interim partial bar for the DB
				float gap = new Float(df.format(realtimeBarOpen - realtimeBarLastBarClose));
				float change = new Float(df.format(realtimeBarClose - realtimeBarLastBarClose));
				Bar bar = new Bar(barKey.symbol, realtimeBarOpen, realtimeBarClose, realtimeBarHigh, realtimeBarLow, null, realtimeBarVolume, null, change, gap, fullBarStart, fullBarEnd, barKey.duration, true);
				QueryManager.insertOrUpdateIntoBar(bar);
				ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") received and processed realtime bar data.");
			}
			else {
				// New bar
				Calendar lastBarStart = Calendar.getInstance();
				lastBarStart.setTimeInMillis(fullBarStart.getTimeInMillis());
				
				fullBarStart.setTimeInMillis(subBarStart.getTimeInMillis());
				fullBarEnd = CalendarUtils.getBarEnd(fullBarStart, barKey.duration);
				
				Calendar lastBarEnd = Calendar.getInstance();
				lastBarEnd.setTimeInMillis(fullBarStart.getTimeInMillis());
				
				float gap = new Float(df.format(realtimeBarOpen - realtimeBarLastBarClose));
				float change = new Float(df.format(realtimeBarClose - realtimeBarLastBarClose));
				
				System.out.println("-------START-------");
				if (realtimeBarSubBarCounter == realtimeBarNumSubBarsInFullBar) {
					Bar bar = new Bar(barKey.symbol, realtimeBarOpen, realtimeBarClose, realtimeBarHigh, realtimeBarLow, null, realtimeBarVolume, null, change, gap, lastBarStart, lastBarEnd, barKey.duration, false);
					QueryManager.insertOrUpdateIntoBar(bar);
					System.out.println(bar.toString());
					ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") received and processed realtime bar data. " + barKey.duration + " bar complete.");
				}
				else {
					System.out.println("First bar was partially based off historical bar.");
					Bar bar = new Bar(barKey.symbol, realtimeBarOpen, realtimeBarClose, realtimeBarHigh, realtimeBarLow, null, realtimeBarVolume, null, change, gap, lastBarStart, lastBarEnd, barKey.duration, false);
					QueryManager.insertOrUpdateIntoBar(bar);
					System.out.println(bar.toString());
					ss.addMessageToDataMessageQueue("IBWorker (" + barKey.toString() + ") received and processed realtime bar data. " + barKey.duration + " bar complete.");
				}
				System.out.println("--------END--------");
	
				realtimeBarLastBarOpen = realtimeBarOpen;
				realtimeBarLastBarClose = realtimeBarClose;
				realtimeBarOpen = (float)open;
				realtimeBarClose = (float)close;
				realtimeBarHigh = (float)high;
				realtimeBarLow = (float)low;
				realtimeBarVolume = (float)volume;
				realtimeBarSubBarCounter = 1;
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
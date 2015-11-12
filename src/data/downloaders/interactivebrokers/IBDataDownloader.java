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
import data.TickConstants;
import dbio.QueryManager;

public class IBDataDownloader implements EWrapper {

	EClientSocket client = new EClientSocket(this);
	
	private ArrayList<Bar> bars = new ArrayList<Bar>(); // Should come in oldest to newest
	private String symbol;
	private BAR_SIZE barSize;
	private int barSeconds;
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
			ArrayList<Bar> bars = ibdd.downloadHistoricalBars(TickConstants.TICK_NAME_FOREX_EUR_USD, Constants.BAR_SIZE.BAR_1M, start, end, "CASH", false);
			ibdd.disconnect();
			for (Bar bar : bars) {
				QueryManager.insertOrUpdateIntoBar(bar);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void connect() {
		if (!client.isConnected()) {
			client.eConnect("localhost", IBConstants.IB_API_PORT, 0);
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
				// Build contract 
				Contract contract = new Contract();
				contract.m_conId = 0;
				if (securityType.equals("CASH")) {
					contract.m_symbol = IBConstants.getIBSymbolFromForexSymbol(forexSymbol);
					contract.m_currency = IBConstants.getIBCurrencyFromForexSymbol(forexSymbol);
				}
				contract.m_secType = securityType;
				contract.m_exchange = IBConstants.SECURITY_TYPE_EXCHANGE_HASH.get(securityType);
				
				// Need to make this unique per ticker
				int tickerID = 1;
				
				Vector<TagValue> chartOptions = new Vector<TagValue>();
				
				// Only 5 sec real time bars are supported so I'll have to do post-processing to make my own size bars with beer and hookers.
				client.reqRealTimeBars(tickerID, contract, 5, "TRADES", regularTradingHoursOnly, chartOptions);
				
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
			double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId,
			int parentId, double lastFillPrice, int clientId, String whyHeld) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue,
			double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nextValidId(int orderId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
			int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managedAccounts(String accountsList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerDataEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume,
			double wap, int count) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void currentTime(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void position(String account, Contract contract, int pos, double avgCost) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void positionEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountSummary(int reqId, String account, String tag, String value, String currency) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountSummaryEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyMessageAPI(String apiData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyCompleted(boolean isSuccessful, String errorText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayGroupList(int reqId, String groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayGroupUpdated(int reqId, String contractInfo) {
		// TODO Auto-generated method stub
		
	}

}
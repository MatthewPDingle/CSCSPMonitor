package dbio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import data.BarKey;
import data.Model;
import utils.ConnectionSingleton;

public class BacktestQueryManager {
	
	public static DecimalFormat df2 = new DecimalFormat("#.##");
	public static DecimalFormat df5 = new DecimalFormat("#.#####");
	
	public static ArrayList<HashMap<String, Object>> backtestGetOpenRequestedOrders() {
		ArrayList<HashMap<String, Object>> orderHashList = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM backtesttrades WHERE status = 'Open Requested'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int openOrderID = rs.getInt("ibopenorderid");
				int stopOrderID = rs.getInt("ibstoporderid");
				int ocaGroup = rs.getInt("ibocagroup");
				String direction = rs.getString("direction");
				int requestedAmount = rs.getBigDecimal("requestedamount").intValue();
				double suggestedEntryPrice = rs.getBigDecimal("suggestedentryprice").doubleValue();
				double suggestedExitPrice = rs.getBigDecimal("suggestedexitprice").doubleValue();
				double suggestedStopPrice = rs.getBigDecimal("suggestedstopprice").doubleValue();
				
				HashMap<String, Object> orderHash = new HashMap<String, Object>();
				orderHash.put("ibstoporderid", stopOrderID);
				orderHash.put("ibopenorderid", openOrderID);
				orderHash.put("ibocagroup", ocaGroup);
				orderHash.put("direction", direction);
				orderHash.put("suggestedentryprice", suggestedEntryPrice);
				orderHash.put("suggestedexitprice", suggestedExitPrice);
				orderHash.put("suggestedstopprice", suggestedStopPrice);
				orderHash.put("requestedamount", requestedAmount);
				orderHashList.add(orderHash);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return orderHashList;
	}
	
	public static ArrayList<HashMap<String, Object>> backtestGetFilledOrders(Calendar openTime) {
		ArrayList<HashMap<String, Object>> orderHashList = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT b.*, m.sellmetricvalue, m.stopmetricvalue FROM backtesttrades b INNER JOIN models m ON b.model = m.modelfile WHERE b.status = 'Filled' AND b.opentime < ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setTimestamp(1, new Timestamp(openTime.getTimeInMillis()));
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int openOrderID = rs.getInt("ibopenorderid");
				int stopOrderID = rs.getInt("ibstoporderid");
				int ocaGroup = rs.getInt("ibocagroup");
				String direction = rs.getString("direction");
				Timestamp expiration = rs.getTimestamp("expiration");
				Calendar expirationC = Calendar.getInstance();
				expirationC.setTimeInMillis(expiration.getTime());
				int requestedAmount = rs.getBigDecimal("requestedamount").intValue();
				int filledAmount = rs.getBigDecimal("filledamount").intValue();
				double suggestedEntryPrice = rs.getBigDecimal("suggestedentryprice").doubleValue();
				double suggestedExitPrice = rs.getBigDecimal("suggestedexitprice").doubleValue();
				double suggestedStopPrice = rs.getBigDecimal("suggestedstopprice").doubleValue();
				double sellMetricValue = rs.getBigDecimal("sellmetricvalue").doubleValue();
				double stopMetricValue = rs.getBigDecimal("stopmetricvalue").doubleValue();
				
				HashMap<String, Object> orderHash = new HashMap<String, Object>();
				orderHash.put("ibstoporderid", stopOrderID);
				orderHash.put("ibopenorderid", openOrderID);
				orderHash.put("ibocagroup", ocaGroup);
				orderHash.put("direction", direction);
				orderHash.put("suggestedentryprice", suggestedEntryPrice);
				orderHash.put("suggestedexitprice", suggestedExitPrice);
				orderHash.put("suggestedstopprice", suggestedStopPrice);
				orderHash.put("requestedamount", requestedAmount);
				orderHash.put("filledamount", filledAmount);
				orderHash.put("expiration", expirationC);
				orderHash.put("sellmetricvalue", sellMetricValue);
				orderHash.put("stopmetricvalue", stopMetricValue);
				orderHashList.add(orderHash);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return orderHashList;
	}
	
	public static void backtestUpdateStop(int orderID, double newStop) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE backtesttrades SET suggestedstopprice = ? WHERE ibopenorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setDouble(1, newStop);
			s.setInt(2, orderID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Double backtestGetTradeProceeds(int openOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();

			String q = 	"SELECT (filledamount * actualexitprice) - (filledamount * actualentryprice) AS grossprofit, " + 
						"(filledamount * actualentryprice) AS capital, " +
						"direction, commission, netprofit FROM backtesttrades WHERE ibopenorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, openOrderID);
			
			Double netProfit = null;
			Double proceeds = null;
			ResultSet rs = s.executeQuery();
			if (rs.next()) {
				Double grossProfit = rs.getDouble("grossprofit");
				String direction = rs.getString("direction");
				Double commission = rs.getDouble("commission");
				Double netProfitCheck = rs.getDouble("netprofit");
				Double capital = rs.getDouble("capital");
				
				if (direction.equals("bull")) {
					netProfit = grossProfit - commission;
					proceeds = capital + netProfit;
				}
				else if (direction.equals("bear")) {
					netProfit = -grossProfit - commission;
					proceeds = capital + netProfit;
				}
				
				if (!df2.format(netProfit).equals(df2.format(netProfitCheck))) {
					throw new Exception ("Net Profit Calculation Is Off");
				}
			}
			
			rs.close();
			s.close();
			c.close();
			
			return proceeds;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void backtestUpdateOpen(int openOrderID, String status, int filled, double avgFillPrice, int parentOrderID, Calendar statusTime) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE backtesttrades SET status = ?, statustime = now(), opentime = now(), filledamount = ?, actualentryprice = ?, bestprice = ? WHERE ibopenorderid = ?";
			if (statusTime != null) {
				q = "UPDATE backtesttrades SET status = ?, statustime = ?, opentime = ?, filledamount = ?, actualentryprice = ?, bestprice = ? WHERE ibopenorderid = ?";
			}
			
			PreparedStatement s = c.prepareStatement(q);
			
			int i = 1;
			s.setString(i++, status);
			if (statusTime != null) {
				s.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime())); // StatusTime
				s.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime())); // OpenTime
			}
			s.setInt(i++, filled);
			s.setBigDecimal(i++, new BigDecimal(df5.format(avgFillPrice)).setScale(5));
			s.setBigDecimal(i++, new BigDecimal(df5.format(avgFillPrice)).setScale(5));
			s.setInt(i++, openOrderID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void backtestCancelOpenOrder(int openOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "UPDATE backtesttrades SET status = 'Cancelled' WHERE ibopenorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, openOrderID);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void backtestDeleteStatusFilledRecords() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "DELETE FROM backtesttrades WHERE status = 'Filled'";
			PreparedStatement s = c.prepareStatement(q);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void backtestRecordClose(String orderType, int orderID, double actualExitPrice, String exitReason, int closeFilledAmount, String direction, Calendar statusTime) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String idcolumn = "";
			if (orderType.equals("Close")) {
				idcolumn = "ibcloseorderid";
			}
			else if (orderType.equals("Stop")) {
				idcolumn = "ibstoporderid";
			}
			else if (orderType.equals("Open")) {
				idcolumn = "ibopenorderid"; // This only happens in the BackTester - it only has this id to identify the record with.
			}
			else {
				System.err.println("recordClose(...)");
			}
			
			String grossProfitClause = "? - actualentryprice";
			if (direction.equals("bear")) {
				grossProfitClause = "actualentryprice - ?";
			}
			
			String statusTimeClause = "now()";
			if (statusTime != null) {
				statusTimeClause = "?";
			}
			String q = "UPDATE backtesttrades SET status = 'Closed', statustime = " + statusTimeClause + ", actualexitprice = ?, exitreason = COALESCE(note, ?), "
					+ "closefilledamount = ?, grossprofit = round((" + grossProfitClause + ") * filledamount, 2) WHERE " + idcolumn + " = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			int i = 1;
			if (statusTime != null) {
				s.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s.setBigDecimal(i++, new BigDecimal(df5.format(actualExitPrice)).setScale(5));
			s.setString(i++, exitReason);
			s.setBigDecimal(i++, new BigDecimal(closeFilledAmount));
			s.setBigDecimal(i++, new BigDecimal(df5.format(actualExitPrice)).setScale(5));
			s.setInt(i++, orderID);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static void backtestUpdateCommission(int openOrderID, double commission) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();

			String q = "UPDATE backtesttrades SET commission = (COALESCE(commission, 0) + ?), "
					+ "netprofit = grossprofit - (COALESCE(commission, 0) + ?) "
					+ "WHERE ibopenorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setBigDecimal(1, new BigDecimal(df2.format(commission)).setScale(2));
			s.setBigDecimal(2, new BigDecimal(df2.format(commission)).setScale(2));
			s.setInt(3, openOrderID);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void backtestNoteCloseout(String orderType, int orderID) {
		try {
			String idcolumn = "";
			if (orderType.equals("Close")) {
				idcolumn = "ibcloseorderid";
			}
			else if (orderType.equals("Stop")) {
				idcolumn = "ibstoporderid";
			}
			else if (orderType.equals("Open")) {
				idcolumn = "ibopenorderid"; // This only happens in the BackTester - it only has this id to identify the record with.
			}
			else {
				System.err.println("recordClose(...)");
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE backtesttrades SET note = 'Closeout' WHERE " + idcolumn + " = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, orderID);
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Calendar backtestGetMostRecentFilledTime() {
		Calendar cal = null;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(statustime) FROM backtesttrades WHERE status = 'Filled'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				Timestamp ts = rs.getTimestamp(1);
				if (ts != null) {
					cal = Calendar.getInstance();
					cal.setTimeInMillis(ts.getTime());
				}
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return cal;
	}
	
	public static int backtestSelectCountOpenOrders() {
		int count = 0;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT COUNT(*) AS c FROM backtesttrades WHERE status = 'Filled'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				count = rs.getInt(1);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	
	public static HashMap<String, Object> backtestFindOppositeOpenOrderToCancel(Model model, String direction) {
		HashMap<String, Object> orderInfo = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String directionWanted = "bear";
			if (direction.equals("bear")) {
				directionWanted = "bull";
			}
			
			String q = "SELECT * FROM backtesttrades WHERE status = 'Filled' AND direction = ? ORDER BY ibopenorderid LIMIT 1";
			if (model != null) {
				q = "SELECT * FROM backtesttrades WHERE status = 'Filled' AND direction = ? AND model = ? ORDER BY ibopenorderid LIMIT 1";
			}
			
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, directionWanted);
			if (model != null) {
				s.setString(2, model.modelFile);
			}
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				orderInfo.put("ibopenorderid", rs.getInt("ibopenorderid"));
				orderInfo.put("ibcloseorderid", rs.getInt("ibcloseorderid"));
				orderInfo.put("ibstoporderid", rs.getInt("ibstoporderid"));
				orderInfo.put("filledamount", rs.getBigDecimal("filledamount").intValue());
				orderInfo.put("closefilledamount", rs.getBigDecimal("closefilledamount"));
				orderInfo.put("iborderaction", rs.getString("iborderaction"));
				orderInfo.put("direction", rs.getString("direction"));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return orderInfo;
	}
	
	public static int backtestRecordTradeRequest(String orderType, String orderAction, String status, Calendar statusTime, String direction, BarKey bk,
			Double suggestedEntryPrice, Double suggestedExitPrice, Double suggestedStopPrice, 
			int requestedAmount, String modelFile, Double awp, Double modelWP, Calendar expiration, String runName) {
		int ibOpenOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String statusTimePart = "now()";
			if (statusTime != null) {
				statusTimePart = "?";
			}
			String q = "INSERT INTO backtesttrades( "
					+ "ibordertype, iborderaction, status, statustime, direction, symbol, duration, "
					+ "requestedamount, suggestedentryprice, suggestedexitprice, suggestedstopprice, "
					+ "model, awp, modelwp, expiration, runname, rundate) "
					+ "VALUES (?, ?, ?, " + statusTimePart + ", ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, now())";
			
			PreparedStatement s = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			
			int z = 1;
			s.setString(z++, orderType);
			s.setString(z++, orderAction);
			s.setString(z++, status);
			if (statusTime != null) {
				s.setTimestamp(z++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s.setString(z++, direction);
			s.setString(z++, bk.symbol);
			s.setString(z++, bk.duration.toString());

			s.setInt(z++, requestedAmount); 
			s.setBigDecimal(z++, new BigDecimal(df5.format(suggestedEntryPrice)).setScale(5));
			s.setBigDecimal(z++, new BigDecimal(df5.format(suggestedExitPrice)).setScale(5)); 
			s.setBigDecimal(z++, new BigDecimal(df5.format(suggestedStopPrice)).setScale(5)); 
			
			s.setString(z++, modelFile);
			s.setBigDecimal(z++, new BigDecimal(df5.format(awp)).setScale(5));
			s.setBigDecimal(z++, new BigDecimal(df5.format(modelWP)).setScale(5));
			s.setTimestamp(z++, new java.sql.Timestamp(expiration.getTime().getTime())); 
			s.setString(z++, runName);
			
			s.executeUpdate();
			ResultSet rs = s.getGeneratedKeys();
			
			if (rs.next()) {
				ibOpenOrderID = rs.getInt(1);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ibOpenOrderID;
	}
	
	public static void backtestUpdateOrderNote(int openOrderID, String note) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "UPDATE backtesttrades SET note = ? WHERE ibopenorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, note);
			s.setInt(2, openOrderID);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int backtestGetIBOCAGroup() {
		int ocaGroup = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT nextval('ib_ocagroup_seq')";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				ocaGroup = rs.getInt("nextval");
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ocaGroup;
	}
	
	public static ArrayList<HashMap<String, Object>> backtestGetCloseOrderIDsNeedingCloseout() {
		ArrayList<HashMap<String, Object>> orderInfoList = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM backtesttrades WHERE status = 'Filled' AND COALESCE(note, '') != 'Closeout'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> orderInfo = new HashMap<String, Object>();
				orderInfo.put("ibopenorderid", rs.getInt("ibopenorderid"));
				orderInfo.put("ibcloseorderid", rs.getInt("ibcloseorderid"));
				orderInfo.put("ibstoporderid", rs.getInt("ibstoporderid"));
				orderInfo.put("filledamount", rs.getBigDecimal("filledamount").intValue());
				orderInfo.put("closefilledamount", rs.getBigDecimal("closefilledamount"));
				orderInfo.put("iborderaction", rs.getString("iborderaction"));
				orderInfo.put("direction", rs.getString("direction"));
				orderInfoList.add(orderInfo);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return orderInfoList;
	}
}

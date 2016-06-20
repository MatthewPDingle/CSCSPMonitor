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
import data.downloaders.interactivebrokers.IBConstants;
import utils.CalcUtils;
import utils.ConnectionSingleton;

public class IBQueryManager {

	public static DecimalFormat df5 = new DecimalFormat("#.#####");
	public static DecimalFormat df2 = new DecimalFormat("#.##");
	
	public static int recordTradeRequest(String orderType, String orderAction, String status, Calendar statusTime, String direction, BarKey bk,
			Double suggestedEntryPrice, Double suggestedExitPrice, Double suggestedStopPrice, 
			int requestedAmount, String modelFile, Double awp, Calendar expiration) {
		int ibOpenOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String statusTimePart = "now()";
			if (statusTime != null) {
				statusTimePart = "?";
			}
			String q = "INSERT INTO ibtrades( "
					+ "ibordertype, iborderaction, status, statustime, direction, symbol, duration, "
					+ "requestedamount, suggestedentryprice, suggestedexitprice, suggestedstopprice, "
					+ "model, awp, expiration) "
					+ "VALUES (?, ?, ?, " + statusTimePart + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			
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
			s.setTimestamp(z++, new java.sql.Timestamp(expiration.getTime().getTime())); 
			
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
	
	public static void updateOpen(int openOrderID, String status, int filled, double avgFillPrice, int parentOrderID, Calendar statusTime) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET status = ?, statustime = now(), filledamount = ?, actualentryprice = ?, bestprice = ? WHERE ibopenorderid = ?";
			if (statusTime != null) {
				q = "UPDATE ibtrades SET status = ?, statustime = ?, filledamount = ?, actualentryprice = ?, bestprice = ? WHERE ibopenorderid = ?";
			}
			
			PreparedStatement s = c.prepareStatement(q);
			
			int i = 1;
			s.setString(i++, status);
			if (statusTime != null) {
				s.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime()));
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
	
	public static void updateClose(int closeOrderID, int filled, double avgFillPrice, int parentOrderID, Calendar statusTime) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET statustime = now(), closefilledamount = ?, actualexitprice = ? WHERE ibcloseorderid = ?";
			if (statusTime != null) {
				q = "UPDATE ibtrades SET statustime = ?, closefilledamount = ?, actualexitprice = ? WHERE ibcloseorderid = ?";
			}

			PreparedStatement s = c.prepareStatement(q);
			
			int z = 1;
			if (statusTime != null) {
				s.setTimestamp(z++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s.setInt(z++, filled);
			s.setBigDecimal(z++, new BigDecimal(df5.format(avgFillPrice)).setScale(5));
			s.setInt(z++, closeOrderID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateStop(int stopOrderID, int filled, double avgFillPrice, int parentOrderID, Calendar statusTime) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET statustime = now(), closefilledamount = ?, actualexitprice = ? WHERE ibstoporderid = ?";
			if (statusTime != null) {
				q = "UPDATE ibtrades SET statustime = ?, closefilledamount = ?, actualexitprice = ? WHERE ibstoporderid = ?";
			}
			
			PreparedStatement s = c.prepareStatement(q);
			
			int z = 1;
			if (statusTime != null) {
				s.setTimestamp(z++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s.setInt(z++, filled);
			s.setBigDecimal(z++, new BigDecimal(df5.format(avgFillPrice)).setScale(5));
			s.setInt(z++, stopOrderID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getOrderIDType(int orderID) {
		String type = "Unknown";
		boolean found = false;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Try OpenOrderID
			if (!found) {
				String q1 = "SELECT ibopenorderid FROM ibtrades WHERE ibopenorderid = ?";
				PreparedStatement s1 = c.prepareStatement(q1);
				
				s1.setInt(1, orderID);
				
				ResultSet rs1 = s1.executeQuery();
				while (rs1.next()) {
					type = "Open";
					found = true;
				}
				rs1.close();
				s1.close();
			}
			
			// Try CloseOrderID
			if (!found) {
				String q2 = "SELECT ibcloseorderid FROM ibtrades WHERE ibcloseorderid = ?";
				PreparedStatement s2 = c.prepareStatement(q2);
				
				s2.setInt(1, orderID);
				
				ResultSet rs2 = s2.executeQuery();
				while (rs2.next()) {
					type = "Close";
					found = true;
				}
				rs2.close();
				s2.close();
			}
			
			// Try StopOrderID
			if (!found) {
				String q3 = "SELECT ibstoporderid FROM ibtrades WHERE ibstoporderid = ?";
				PreparedStatement s3 = c.prepareStatement(q3);
				
				s3.setInt(1, orderID);
				
				ResultSet rs3 = s3.executeQuery();
				while (rs3.next()) {
					type = "Stop";
					found = true;
				}
				rs3.close();
				s3.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return type;
	}
	
	public static String getExecIDType(String execID) {
		String type = "Unknown";
		boolean found = false;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Try OpenOrderID
			if (!found) {
				String q1 = "SELECT ibopenorderid FROM ibtrades WHERE ibopenexecid = ?";
				PreparedStatement s1 = c.prepareStatement(q1);
				
				s1.setString(1, execID);
				
				ResultSet rs1 = s1.executeQuery();
				while (rs1.next()) {
					type = "Open";
					found = true;
				}
				rs1.close();
				s1.close();
			}
			
			// Try CloseOrderID
			if (!found) {
				String q2 = "SELECT ibcloseorderid FROM ibtrades WHERE ibcloseexecid = ?";
				PreparedStatement s2 = c.prepareStatement(q2);
				
				s2.setString(1, execID);
				
				ResultSet rs2 = s2.executeQuery();
				while (rs2.next()) {
					type = "Close";
					found = true;
				}
				rs2.close();
				s2.close();
			}
			
			// Try StopOrderID
			if (!found) {
				String q3 = "SELECT ibstoporderid FROM ibtrades WHERE ibstopexecid = ?";
				PreparedStatement s3 = c.prepareStatement(q3);
				
				s3.setString(1, execID);
				
				ResultSet rs3 = s3.executeQuery();
				while (rs3.next()) {
					type = "Stop";
					found = true;
				}
				rs3.close();
				s3.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return type;
	}
	
	public static HashMap<String, Object> getOrderInfo(String orderIDType, int orderID) {
		HashMap<String, Object> fieldHash = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "";
			if (orderIDType.equals("Open")) {
				q = "SELECT * FROM ibtrades WHERE ibopenorderid = ?";
			}
			else if (orderIDType.equals("Close")) {
				q = "SELECT * FROM ibtrades WHERE ibcloseorderid = ?";
			}
			else if (orderIDType.equals("Stop")) {
				q = "SELECT * FROM ibtrades WHERE ibstoporderid = ?";
			}
			
			else {
				return fieldHash;
			}
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, orderID);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				fieldHash.put("iborderaction", rs.getString("iborderaction"));
				fieldHash.put("direction", rs.getString("direction"));
				fieldHash.put("requestedamount", rs.getBigDecimal("requestedamount"));
				fieldHash.put("filledamount", rs.getBigDecimal("filledamount"));
				fieldHash.put("suggestedexitprice", rs.getBigDecimal("suggestedexitprice"));
				fieldHash.put("suggestedstopprice", rs.getBigDecimal("suggestedstopprice"));
				fieldHash.put("expiration", rs.getTimestamp("expiration"));
				fieldHash.put("closefilledamount", rs.getBigDecimal("closefilledamount"));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fieldHash;
	}
	
	public static int recordCloseTradeRequest(int openOrderID, int ocaGroup) {
		int closeOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Query 1 - Get the nextval
			String q1 = "SELECT nextval('ibtrades_orderid_seq')";
			PreparedStatement s1 = c.prepareStatement(q1);
			
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				closeOrderID = rs1.getInt("nextval");
			}
			rs1.close();
			s1.close();
			
			// Query 2 - Update the ibcloseorderId with nextval
			String q2 = "UPDATE ibtrades SET ibcloseorderid = ?, ibocagroup = ? WHERE ibopenorderid = ?";
			PreparedStatement s2 = c.prepareStatement(q2);
			s2.setInt(1, closeOrderID);
			s2.setInt(2, ocaGroup);
			s2.setInt(3, openOrderID);
			
			s2.executeUpdate();
			s2.close();
			
			c.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return closeOrderID;
	}
	
	public static int recordStopTradeRequest(int openOrderID) {
		int stopOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Query 1 - Get the nextval
			String q1 = "SELECT nextval('ibtrades_orderid_seq')";
			PreparedStatement s1 = c.prepareStatement(q1);
			
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				stopOrderID = rs1.getInt("nextval");
			}
			rs1.close();
			s1.close();
			
			// Query 2 - Update the ibstoporderId with nextval
			String q2 = "UPDATE ibtrades SET ibstoporderid = ? WHERE ibopenorderid = ?";
			PreparedStatement s2 = c.prepareStatement(q2);
			s2.setInt(1, stopOrderID);
			s2.setInt(2, openOrderID);
			
			s2.executeUpdate();
			s2.close();
			
			c.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stopOrderID;
	}
	
	public static int getIBOCAGroup() {
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
	
	public static boolean checkIfNeedsCloseAndStopOrders(int openOrderID) {
		boolean answer = false;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT ibcloseorderid, ibstoporderid, status FROM ibtrades WHERE ibopenorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, openOrderID);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				String status = rs.getString("status");
				int ibCloseOrderID = rs.getInt("ibcloseorderid");
				int ibStopOrderID = rs.getInt("ibstoporderid");
				
				if (status.equals("Filled") && ibCloseOrderID == 0 && ibStopOrderID == 0) {
					answer = true;
				}
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}
	
	public static boolean checkIfCloseOrderExpired(int closeOrderID) {
		boolean answer = false;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			// Apparently the timing is really fast and I often get signals from IB slightly before my computer's time so I need to add 10 sec to be safe.
			String q = "SELECT * FROM ibtrades WHERE ibcloseorderid = ? AND (now() + INTERVAL '10 seconds') > expiration";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, closeOrderID);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				answer = true;
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}
	
	public static boolean checkIfStopOrderExpired(int stopOrderID) {
		boolean answer = false;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM ibtrades WHERE ibstoporderid = ? AND now() > expiration";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, stopOrderID);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				answer = true;
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}
	
	public static int updateCloseTradeRequest(int closeOrderID, int ocaGroup, Calendar statusTime) {
		int newCloseOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Query 1 - Get the nextval
			String q1 = "SELECT nextval('ibtrades_orderid_seq')";
			PreparedStatement s1 = c.prepareStatement(q1);
			
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				newCloseOrderID = rs1.getInt("nextval");
			}
			rs1.close();
			s1.close();
			
			// Query 2 - Update the ibcloseorderId with nextval
			String q2 = "UPDATE ibtrades SET ibcloseorderid = ?, ibocagroup = ?, statustime = now() WHERE ibcloseorderid = ?";
			if (statusTime != null) {
				q2 = "UPDATE ibtrades SET ibcloseorderid = ?, ibocagroup = ?, statustime = ? WHERE ibcloseorderid = ?";
			}
			
			PreparedStatement s2 = c.prepareStatement(q2);
			int i = 1;
			s2.setInt(i++, newCloseOrderID);
			s2.setInt(i++, ocaGroup);
			if (statusTime != null) {
				s2.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s2.setInt(i++, closeOrderID);
			
			s2.executeUpdate();
			s2.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return newCloseOrderID;
	}
	
	public static int updateStopTradeRequest(int closeOrderID, Calendar statusTime) {
		int newStopOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Query 1 - Get the nextval
			String q1 = "SELECT nextval('ibtrades_orderid_seq')";
			PreparedStatement s1 = c.prepareStatement(q1);
			
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				newStopOrderID = rs1.getInt("nextval");
			}
			rs1.close();
			s1.close();
			
			// Query 2 - Update the ibcloseorderId with nextval
			String q2 = "UPDATE ibtrades SET ibstoporderid = ?, statustime = now() WHERE ibcloseorderid = ?";
			if (statusTime != null) {
				q2 = "UPDATE ibtrades SET ibstoporderid = ?, statustime = ? WHERE ibcloseorderid = ?";
			}
			PreparedStatement s2 = c.prepareStatement(q2);
			int i = 1;
			s2.setInt(i++, newStopOrderID);
			if (statusTime != null) {
				s2.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s2.setInt(i++, closeOrderID);
			
			s2.executeUpdate();
			s2.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return newStopOrderID;
	}
	
	public static void recordClose(String orderType, int orderID, double actualExitPrice, String exitReason, int closeFilledAmount, String direction, Calendar statusTime) {
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
			String q = "UPDATE ibtrades SET status = 'Closed', statustime = " + statusTimeClause + ", actualexitprice = ?, exitreason = COALESCE(note, ?), "
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
	
	public static void recordRejection(String orderType, int orderID, Calendar statusTime) {
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
				idcolumn = "ibopenorderid";
			}
			else {
				System.err.println("recordCancellation(...)");
			}
			
			String q = "UPDATE ibtrades SET status = 'Rejected', statustime = now() WHERE " + idcolumn + " = ?";
			if (statusTime != null) {
				q = "UPDATE ibtrades SET status = 'Rejected', statustime = ? WHERE " + idcolumn + " = ?";
			}
			PreparedStatement s = c.prepareStatement(q);
			
			int i = 1;
			if (statusTime != null) {
				s.setTimestamp(i++, new java.sql.Timestamp(statusTime.getTime().getTime()));
			}
			s.setInt(i++, orderID);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelOpenOrder(int openOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "UPDATE ibtrades SET status = 'Cancelled' WHERE ibopenorderid = ?";
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
	
	public static void updateCommission(String orderType, String execID, double commission) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String idField = "";
			if (orderType.equals("Open")) {
				idField = "ibopenexecid";
			}
			else if (orderType.equals("Close")) {
				idField = "ibcloseexecid";
			}
			else if (orderType.equals("Stop")) {
				idField = "ibstopexecid";
			}
			String q = "UPDATE ibtrades SET commission = (COALESCE(commission, 0) + ?), "
					+ "netprofit = grossprofit - (COALESCE(commission, 0) + ?) "
					+ "WHERE " + idField + " = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setBigDecimal(1, new BigDecimal(df2.format(commission)).setScale(2));
			s.setBigDecimal(2, new BigDecimal(df2.format(commission)).setScale(2));
			s.setString(3, execID);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateExecID(String orderType, int orderID, String execID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			if (orderType.equals("Open")) {
				String q = "UPDATE ibtrades SET ibopenexecid = ? WHERE ibopenorderid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setString(1, execID);
				s.setInt(2, orderID);
				
				s.executeUpdate();
				s.close();
				c.close();
			}
			if (orderType.equals("Close")) {
				String q = "UPDATE ibtrades SET ibcloseexecid = ? WHERE ibcloseorderid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setString(1, execID);
				s.setInt(2, orderID);
				
				s.executeUpdate();
				s.close();
				c.close();
			}
			if (orderType.equals("Stop")) {
				String q = "UPDATE ibtrades SET ibstopexecid = ? WHERE ibstoporderid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setString(1, execID);
				s.setInt(2, orderID);
				
				s.executeUpdate();
				s.close();
				c.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, Object> findOppositeOpenOrderToCancel(Model model, String direction) {
		HashMap<String, Object> orderInfo = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String directionWanted = "bear";
			if (direction.equals("bear")) {
				directionWanted = "bull";
			}
			
			String q = "SELECT * FROM ibtrades WHERE status = 'Filled' AND direction = ? AND model = ? ORDER BY ibopenorderid LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, directionWanted);
			s.setString(2, model.modelFile);
			
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
	
	public static void updateOrderNote(int openOrderID, String note) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "UPDATE ibtrades SET note = ? WHERE ibopenorderid = ?";
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
	
	public static ArrayList<HashMap<String, Object>> updateStopsAndBestPricesForOpenOrders(double bid, double ask) {
		ArrayList<HashMap<String, Object>> stopHashList = new ArrayList<HashMap<String, Object>>(); // StopOrderID, NewStopPrice
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// First get info about the open orders
			String q1 = "SELECT * FROM ibtrades WHERE status = 'Filled'";
			PreparedStatement s1 = c.prepareStatement(q1);
			
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				int openOrderID = rs1.getInt("ibopenorderid");
				int stopOrderID = rs1.getInt("ibstoporderid");
				int ocaGroup = rs1.getInt("ibocagroup");
				String symbol = rs1.getString("symbol");
				String direction = rs1.getString("direction");
				Timestamp expiration = rs1.getTimestamp("expiration");
				int filledAmount = rs1.getBigDecimal("filledamount").intValue();
				int closeFilledAmount = 0;
				BigDecimal bdCloseFilledAmount = rs1.getBigDecimal("closefilledamount");
				if (bdCloseFilledAmount != null) {
					closeFilledAmount = bdCloseFilledAmount.intValue();
				}
				int remainingAmount = filledAmount - closeFilledAmount;
				double actualEntryPrice = rs1.getBigDecimal("actualentryprice").doubleValue();
				double suggestedExitPrice = rs1.getBigDecimal("suggestedexitprice").doubleValue();
				double suggestedStopPrice = rs1.getBigDecimal("suggestedstopprice").doubleValue();
				BigDecimal bdBestPrice = rs1.getBigDecimal("bestprice");
				double bestPrice = actualEntryPrice;
				if (bdBestPrice != null) {
					bestPrice = bdBestPrice.doubleValue();
				}
				
				double modelRatio = Math.abs(suggestedExitPrice - actualEntryPrice) / Math.abs(suggestedStopPrice - actualEntryPrice);
				
				if (direction.equals("bull")) {
					if (bid > bestPrice) {
						String q2 = "UPDATE ibtrades SET bestprice = ? WHERE ibopenorderid = ?";
						PreparedStatement s2 = c.prepareStatement(q2);
						
						s2.setBigDecimal(1, new BigDecimal(df5.format(bid)).setScale(5)); 
						s2.setInt(2, openOrderID);
						
						s2.executeUpdate();
						s2.close();
						
						// Calculate new stop
						double distanceToClose = suggestedExitPrice - bid;
						double newStop = bid - (distanceToClose / modelRatio);
						if (newStop > actualEntryPrice) {
//							newStop = actualEntryPrice + IBConstants.TICKER_PIP_SIZE_HASH.get(symbol); // Old way stopping around the entry
							double halfwayToExit = (actualEntryPrice + suggestedExitPrice) / 2d;
							if (bid > halfwayToExit) {
								double changeFromEntry = bid - halfwayToExit;
								newStop = actualEntryPrice + changeFromEntry;
							}
						}
						newStop = CalcUtils.roundTo5DigitHalfPip(newStop);
						
						HashMap<String, Object> stopHash = new HashMap<String, Object>();
						stopHash.put("ibstoporderid", stopOrderID);
						stopHash.put("ibocagroup", ocaGroup);
						stopHash.put("direction", direction);
						stopHash.put("remainingamount", remainingAmount);
						stopHash.put("newstop", newStop);
						stopHash.put("expiration", expiration);
						stopHashList.add(stopHash);
					}
				}
				else if (direction.equals("bear")) {
					if (ask < bestPrice) {
						String q3 = "UPDATE ibtrades SET bestprice = ? WHERE ibopenorderid = ?";
						PreparedStatement s3 = c.prepareStatement(q3);
						
						s3.setBigDecimal(1, new BigDecimal(df5.format(ask)).setScale(5)); 
						s3.setInt(2, openOrderID);
						
						s3.executeUpdate();
						s3.close();
						
						// Calculate the new stop
						double distanceToClose = ask - suggestedExitPrice;
						double newStop = ask + (distanceToClose / modelRatio);
						if (newStop < actualEntryPrice) {
//							newStop = actualEntryPrice - IBConstants.TICKER_PIP_SIZE_HASH.get(symbol); // Old way stopping around the entry
							newStop = ask + (distanceToClose * 2);
							double halfwayToExit = (actualEntryPrice + suggestedExitPrice) / 2d;
							if (ask < halfwayToExit) {
								double changeFromEntry = halfwayToExit - ask;
								newStop = actualEntryPrice - changeFromEntry;
							}
						}
						newStop = CalcUtils.roundTo5DigitHalfPip(newStop);
						
						HashMap<String, Object> stopHash = new HashMap<String, Object>();
						stopHash.put("ibstoporderid", stopOrderID);
						stopHash.put("ibocagroup", ocaGroup);
						stopHash.put("direction", direction);
						stopHash.put("remainingamount", remainingAmount);
						stopHash.put("newstop", newStop);
						stopHash.put("expiration", expiration);
						stopHashList.add(stopHash);
					}
				}
			}
			
			rs1.close();
			s1.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stopHashList;
	}
	
	public static int selectCountOpenOrders() {
		int count = 0;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT COUNT(*) AS c FROM ibtrades WHERE status = 'Filled'";
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
	
	public static ArrayList<Integer> getCloseOrderIDsNeedingCloseout() {
		ArrayList<Integer> closeOrderIDs = new ArrayList<Integer>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT ibcloseorderid FROM ibtrades WHERE status = 'Filled' AND COALESCE(note, '') != 'Closeout'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				closeOrderIDs.add(rs.getInt("ibcloseorderid"));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return closeOrderIDs;
	}
	
	public static void noteCloseout(int closeOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET note = 'Closeout' WHERE ibcloseorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, closeOrderID);
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Calendar getMostRecentFilledTime() {
		Calendar cal = null;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(statustime) FROM ibtrades WHERE status = 'Filled'";
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
	
	public static ArrayList<HashMap<String, Object>> backtestGetOpenRequestedOrders() {
		ArrayList<HashMap<String, Object>> orderHashList = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM ibtrades WHERE status = 'Open Requested'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int openOrderID = rs.getInt("ibopenorderid");
				int stopOrderID = rs.getInt("ibstoporderid");
				int ocaGroup = rs.getInt("ibocagroup");
				String symbol = rs.getString("symbol");
				String direction = rs.getString("direction");
				Timestamp expiration = rs.getTimestamp("expiration");
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
	
	public static ArrayList<HashMap<String, Object>> backtestGetFilledOrders() {
		ArrayList<HashMap<String, Object>> orderHashList = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM ibtrades WHERE status = 'Filled'";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int openOrderID = rs.getInt("ibopenorderid");
				int stopOrderID = rs.getInt("ibstoporderid");
				int ocaGroup = rs.getInt("ibocagroup");
				String symbol = rs.getString("symbol");
				String direction = rs.getString("direction");
				Timestamp expiration = rs.getTimestamp("expiration");
				Calendar expirationC = Calendar.getInstance();
				expirationC.setTimeInMillis(expiration.getTime());
				int requestedAmount = rs.getBigDecimal("requestedamount").intValue();
				int filledAmount = rs.getBigDecimal("filledamount").intValue();
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
				orderHash.put("filledamount", filledAmount);
				orderHash.put("expiration", expirationC);
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
}
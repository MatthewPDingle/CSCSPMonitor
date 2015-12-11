package dbio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import data.BarKey;
import utils.ConnectionSingleton;

public class IBQueryManager {

	private static DecimalFormat df5 = new DecimalFormat("#.#####");
	
	public static int recordTradeRequest(String orderType, String orderAction, String status, String direction, BarKey bk,
			Double suggestedEntryPrice, Double suggestedExitPrice, Double suggestedStopPrice, 
			int requestedAmount, String modelFile, Calendar expiration) {
		int ibOpenOrderID = -1;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO ibtrades( "
					+ "ibordertype, iborderaction, status, statustime, direction, symbol, duration, "
					+ "requestedamount, suggestedentryprice, suggestedexitprice, suggestedstopprice, "
					+ "model, expiration) "
					+ "VALUES (?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			
			int z = 1;
			s.setString(z++, orderType);
			s.setString(z++, orderAction);
			s.setString(z++, status);
			s.setString(z++, direction);
			s.setString(z++, bk.symbol);
			s.setString(z++, bk.duration.toString());

			s.setInt(z++, requestedAmount); 
			s.setBigDecimal(z++, new BigDecimal(df5.format(suggestedEntryPrice)).setScale(5));
			s.setBigDecimal(z++, new BigDecimal(df5.format(suggestedExitPrice)).setScale(5)); 
			s.setBigDecimal(z++, new BigDecimal(df5.format(suggestedStopPrice)).setScale(5)); 
			
			s.setString(z++, modelFile);
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
	
	public static void updateOpen(int openOrderID, String status, int filled, double avgFillPrice, int parentOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET status = ?, statustime = now(), filledamount = ?, actualentryprice = ? WHERE ibopenorderid = ?";

			PreparedStatement s = c.prepareStatement(q);
			
			int z = 1;
			s.setString(z++, status);
			s.setInt(z++, filled);
			s.setBigDecimal(z++, new BigDecimal(df5.format(avgFillPrice)).setScale(5));
			s.setInt(z++, openOrderID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateClose(int closeOrderID, int filled, double avgFillPrice, int parentOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET statustime = now(), closefilledamount = ?, actualexitprice = ? WHERE ibcloseorderid = ?";

			PreparedStatement s = c.prepareStatement(q);
			
			int z = 1;
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
	
	public static void updateStop(int stopOrderID, int filled, double avgFillPrice, int parentOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET statustime = now(), closefilledamount = ?, actualexitprice = ? WHERE ibstoporderid = ?";

			PreparedStatement s = c.prepareStatement(q);
			
			int z = 1;
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
			
			// Try ExpirationorderID
			if (!found) {
				String q4 = "SELECT ibexpirationorderid FROM ibtrades WHERE ibexpirationorderid = ?";
				PreparedStatement s4 = c.prepareStatement(q4);
				
				s4.setInt(1, orderID);
				
				ResultSet rs4 = s4.executeQuery();
				while (rs4.next()) {
					type = "Expiration";
					found = true;
				}
				rs4.close();
				s4.close();
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
	
	public static int recordCloseTradeRequest(int openOrderID) {
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
			String q2 = "UPDATE ibtrades SET ibcloseorderid = ? WHERE ibopenorderid = ?";
			PreparedStatement s2 = c.prepareStatement(q2);
			s2.setInt(1, closeOrderID);
			s2.setInt(2, openOrderID);
			
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
			String q = "SELECT * FROM ibtrades WHERE ibcloseorderid = ? AND now() > expiration";
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
	
	public static int updateCloseTradeRequest(int closeOrderID) {
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
			String q2 = "UPDATE ibtrades SET ibcloseorderid = ? WHERE ibcloseorderid = ?";
			PreparedStatement s2 = c.prepareStatement(q2);
			s2.setInt(1, newCloseOrderID);
			s2.setInt(2, closeOrderID);
			
			s2.executeUpdate();
			s2.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return newCloseOrderID;
	}
	
	public static int updateStopTradeRequest(int closeOrderID) {
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
			String q2 = "UPDATE ibtrades SET ibstoporderid = ? WHERE ibcloseorderid = ?";
			PreparedStatement s2 = c.prepareStatement(q2);
			s2.setInt(1, newStopOrderID);
			s2.setInt(2, closeOrderID);
			
			s2.executeUpdate();
			s2.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return newStopOrderID;
	}
	
	public static void recordClose(int closeOrderID, double actualExitPrice, String exitReason, int closeFilledAmount, double commission, double netProfit, double grossProfit) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "UPDATE ibtrades SET status = 'Closed', statustime = now(), actualexitprice = ?, exitreason = ?, "
					+ "closefilledamount = ?, commission = ?, netprofit = ?, grossprofit = ? WHERE ibcloseorderid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setBigDecimal(1, new BigDecimal(actualExitPrice));
			s.setString(2, exitReason);
			s.setBigDecimal(3, new BigDecimal(closeFilledAmount));
			s.setBigDecimal(4, new BigDecimal(commission));
			s.setBigDecimal(5, new BigDecimal(netProfit));
			s.setBigDecimal(6, new BigDecimal(grossProfit));
			s.setInt(6, closeOrderID);
			
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
}
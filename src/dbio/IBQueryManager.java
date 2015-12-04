package dbio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import data.BarKey;
import utils.ConnectionSingleton;

public class IBQueryManager {

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
			s.setBigDecimal(z++, new BigDecimal(suggestedEntryPrice));
			s.setBigDecimal(z++, new BigDecimal(suggestedExitPrice)); 
			s.setBigDecimal(z++, new BigDecimal(suggestedStopPrice)); 
			
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
	
	public static void updateTrade(int orderID, String status, int filled, double avgFillPrice, int parentOrderID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE ibtrades SET status = ?, statustime = now(), filledamount = ?, actualentryprice = ? WHERE ibopenorderid = ?";

			PreparedStatement s = c.prepareStatement(q);
			
			int z = 1;
			s.setString(z++, status);
			s.setInt(z++, filled);
			s.setBigDecimal(z++, new BigDecimal(avgFillPrice));
			s.setInt(z++, orderID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package data;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import constants.Constants;
import utils.ConnectionSingleton;
import utils.Formatting;

public class Fixer {

	public static void main(String[] args) {
		fixGapAndChange(new BarKey("BTC_ETH", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_LTC", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_DASH", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_DOGE", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_FCT", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_XEM", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_XMR", Constants.BAR_SIZE.BAR_4H));
		fixGapAndChange(new BarKey("BTC_XRP", Constants.BAR_SIZE.BAR_4H));
		
	}

	private static void fixGapAndChange(BarKey bk) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q1 = "SELECT * FROM bar WHERE symbol = ? AND duration = ? ORDER BY start";
			PreparedStatement s1 = c.prepareStatement(q1);
			
			s1.setString(1, bk.symbol);
			s1.setString(2, bk.duration.toString());
			
			Double yesterdayClose = null;
			Double yesterdayOpen = null;
			
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				double close = rs1.getBigDecimal("close").doubleValue();
				double open = rs1.getBigDecimal("open").doubleValue();
				Timestamp start = rs1.getTimestamp("start");
				
				if (yesterdayClose != null) {
					double todayGap = open - yesterdayClose;
					double todayChange = close - yesterdayClose;
					
					String q2 = "UPDATE bar SET gap = ?, change = ? WHERE symbol = ? AND duration = ? AND start = ?";
					PreparedStatement s2 = c.prepareStatement(q2);
					
					s2.setBigDecimal(1, new BigDecimal(Formatting.df8.format(todayGap)).setScale(8));
					s2.setBigDecimal(2, new BigDecimal(Formatting.df8.format(todayChange)).setScale(8));
					s2.setString(3, bk.symbol);
					s2.setString(4, bk.duration.toString());
					s2.setTimestamp(5, start);
					
					s2.executeUpdate();
					s2.close();
				}
				
				yesterdayClose = close;
				yesterdayOpen = open;
			}
			rs1.close();
			s1.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package trading.engines.paper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedList;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.downloaders.interactivebrokers.IBConstants;
import data.downloaders.interactivebrokers.IBSingleton;
import utils.ConnectionSingleton;

public class IBAdaptiveTest {

	private IBSingleton ibs = IBSingleton.getInstance();
	
	private LinkedList<Double> last600AWPs = new LinkedList<Double>();
	private int positionSize = 0;

	private boolean level42 = false;
	private boolean level43 = false;
	private boolean level44 = false;
	private boolean level45 = false;
	private boolean level46 = false;
	private boolean level47 = false;
	private boolean level48 = false;
	private boolean level52 = false;
	private boolean level53 = false;
	private boolean level54 = false;
	private boolean level55 = false;
	private boolean level56 = false;
	private boolean level57 = false;
	private boolean level58 = false;
	
	private boolean level50Rising = false;
	private boolean level50Falling = false;
	
	public void runChecks(LinkedList<Double> last600AWPs) {
		this.last600AWPs.clear();
		this.last600AWPs.addAll(last600AWPs);
		
		double newerAWP = averageMostRecent300AWPs();
		double olderAWP = averageOldest300AWPs();
		
		// Going Up
		if (newerAWP > olderAWP) {
			if (newerAWP > .50 && !level50Rising) {
				level50Rising = true;
				level50Falling = false;
				level42 = false;
				level43 = false;
				level44 = false;
				level45 = false;
				level46 = false;
				level47 = false;
				level48 = false;

				while (positionSize < 0) {
					recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L50 Rising. Closing Shorts");
					positionSize += 20000;
				}
			}
			
			if (newerAWP > .52 && !level52) {
				level52 = true;
				positionSize = 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L52 Rising. Buying 20000");
			}
			if (newerAWP > .53 && !level53) {
				level53 = true;
				positionSize += 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L53 Rising. Buying 20000");
			}
			if (newerAWP > .54 && !level54) {
				level54 = true;
				positionSize += 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L54 Rising. Buying 20000");
			}
			if (newerAWP > .55 && !level55) {
				level55 = true;
				positionSize += 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L55 Rising. Buying 20000");
			}
			if (newerAWP > .56 && !level56) {
				level56 = true;
				positionSize += 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L56 Rising. Buying 20000");
			}
			if (newerAWP > .57 && !level57) {
				level57 = true;
				positionSize += 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L57 Rising. Buying 20000");
			}
			if (newerAWP > .58 && !level58) {
				level58 = true;
				positionSize += 20000;
				recordPaperTrade("Buy", 20000, getPrice("Buy"), newerAWP, "L58 Rising. Buying 20000");
			}
		}
		
		// Going Down
		if (newerAWP < olderAWP) {
			if (newerAWP < .50 && !level50Falling) {
				level50Falling = true;
				level50Rising = false;
				level52 = false;
				level53 = false;
				level54 = false;
				level55 = false;
				level56 = false;
				level57 = false;
				level58 = false;
				
				while (positionSize > 0) {
					recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L50 Falling. Closing Longs");
					positionSize -= 20000;
				}
			}
			
			if (newerAWP < .48 && !level48) {
				level48 = true;
				positionSize = -20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L48 Falling. Selling 20000");
			}
			if (newerAWP < .47 && !level47) {
				level47 = true;
				positionSize -= 20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L47 Falling. Selling 20000");
			}
			if (newerAWP < .46 && !level46) {
				level46 = true;
				positionSize -= 20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L46 Falling. Selling 20000");
			}
			if (newerAWP < .45 && !level45) {
				level45 = true;
				positionSize -= 20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L45 Falling. Selling 20000");
			}
			if (newerAWP < .44 && !level44) {
				level44 = true;
				positionSize -= 20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L44 Falling. Selling 20000");
			}
			if (newerAWP < .43 && !level43) {
				level43 = true;
				positionSize -= 20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L43 Falling. Selling 20000");
			}
			if (newerAWP < .42 && !level42) {
				level42 = true;
				positionSize -= 20000;
				recordPaperTrade("Sell", -20000, getPrice("Sell"), newerAWP, "L42 Falling. Selling 20000");
			}
		}
	}
	
	private double getPrice(String buyOrSell) {
		BarKey bk = new BarKey("EUR.USD", BAR_SIZE.BAR_5M);
		if (buyOrSell.equals("Buy")) {
			if (ibs.getTickerFieldValue(bk, IBConstants.TICK_FIELD_ASK_PRICE) != null) {
				return ibs.getTickerFieldValue(bk, IBConstants.TICK_FIELD_ASK_PRICE);
			}
		}
		if (buyOrSell.equals("Sell")) {
			if (ibs.getTickerFieldValue(bk, IBConstants.TICK_FIELD_BID_PRICE) != null) {
				return ibs.getTickerFieldValue(bk, IBConstants.TICK_FIELD_BID_PRICE);
			}
		}
		return 0;
	}
	
	private double averageMostRecent300AWPs() {
		try {
			if (last600AWPs == null || last600AWPs.size() < 600) {
				return .5;
			}
			
			double sumAWPs = 0;
			for (int a = 0; a < 300; a++) {
				sumAWPs += last600AWPs.get(a);
			}
			return sumAWPs / (double)300;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0d;
		}
	}
	
	private double averageOldest300AWPs() {
		try {
			if (last600AWPs == null || last600AWPs.size() < 600) {
				return .5;
			}
			
			double sumAWPs = 0;
			for (int a = 300; a < 600; a++) {
				sumAWPs += last600AWPs.get(a);
			}
			return sumAWPs / (double)300;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0d;
		}
	}
	
	public static void recordPaperTrade(String action, int amount, double price, double awp, String notes) {
		try {
			DecimalFormat df5 = new DecimalFormat("#.#####");
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO ibpapertrades( "
					+ "action, amount, price, tradetime, awp, notes) "
					+ "VALUES (?, ?, ?, now(), ?, ?)";
			PreparedStatement s = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			
			s.setString(1, action);
			s.setInt(2, amount);
			s.setBigDecimal(3, new BigDecimal(df5.format(price)).setScale(5));
			s.setBigDecimal(4, new BigDecimal(df5.format(awp)).setScale(5));
			s.setString(5, notes);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadStateFromDB() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT SUM(amount) FROM ibpapertrades WHERE tradetime > (SELECT MAX(tradetime) FROM ibpapertrades WHERE notes LIKE '%Closing%')";
			PreparedStatement s = c.prepareStatement(q);
		
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				positionSize = rs.getInt(1);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
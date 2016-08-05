package dbio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Array;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
import data.BarWithMetricData;
import data.Metric;
import data.MetricKey;
import data.MetricTimeCache;
import data.Model;
import metrics.MetricSingleton;
import utils.CalendarUtils;
import utils.ConnectionSingleton;

public class QueryManager {
	
	public static Calendar getMaxDateFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(start) AS d FROM bar";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);

			Date d = null;
			while (rs.next()) {
				d = rs.getDate("d");
			}

			rs.close();
			s.close();
			c.close();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			return cal;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void insertIntoMetricDiscreteValues(String metricName, BarKey bk, Calendar start, Calendar end, ArrayList<Float> percentiles, ArrayList<Float> values) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"INSERT INTO metricdiscretevalues(name, symbol, start, \"end\", duration, percentiles, \"values\") " +
						"VALUES (?, ?, ?, ?, ?, ?, ?)";

			Array percentilesArray = c.createArrayOf("float", percentiles.toArray());
			Array valuesArray = c.createArrayOf("float", values.toArray());
			
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, metricName);
			ps.setString(2, bk.symbol);
			ps.setTimestamp(3, new java.sql.Timestamp(start.getTime().getTime()));
			ps.setTimestamp(4, new java.sql.Timestamp(end.getTime().getTime()));
			ps.setString(5, bk.duration.toString());
			ps.setArray(6, percentilesArray);
			ps.setArray(7, valuesArray);
			
			ps.executeUpdate();
			
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static HashMap<MetricKey, ArrayList<Float>> loadMetricDiscreteValueHash() {
		HashMap<MetricKey, ArrayList<Float>> metricDiscreteValueHash = new HashMap<MetricKey, ArrayList<Float>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"SELECT * FROM metricdiscretevalues";
			
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				String metricName = rs.getString(1);
				String symbol = rs.getString(2);
				String duration = rs.getString(5);
				Array valuesArray = rs.getArray(7);
				Float[] values = (Float[])valuesArray.getArray();
				ArrayList<Float> valuesList = new ArrayList<Float>(Arrays.asList((Float[])values));
				
				MetricKey mk = new MetricKey(metricName, symbol, BAR_SIZE.valueOf(duration));
				metricDiscreteValueHash.put(mk, valuesList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metricDiscreteValueHash;
	}
	
	public static ArrayList<String> getUniqueListOfSymbols(ArrayList<String> indices) {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT DISTINCT symbol FROM "	+ Constants.INDEXLIST_TABLE;
			String whereClause = "";
			if (indices != null && indices.size() > 0) {
				whereClause = " WHERE (";
			}
			for (String index : indices) {
				whereClause += "index = '" + index + "' OR ";
			}
			if (whereClause.endsWith("OR ")) {
				whereClause = whereClause.substring(0, whereClause.length() - 3);
				whereClause += ")";
			}
			q += whereClause;
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				symbols.add(rs.getString("symbol"));
			}
			
			rs.close();
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return symbols;
	}
	
	public static ArrayList<BarKey> getUniqueBarKeys() {
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT symbol, duration FROM bar GROUP BY symbol, duration";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				barKeys.add(new BarKey(symbol, duration));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return barKeys;
	}
	
	public static ArrayList<BarKey> getUniqueBarKeysWithMetrics() {
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT symbol, duration FROM metrics GROUP BY symbol, duration";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				barKeys.add(new BarKey(symbol, duration));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return barKeys;
	}
	
	public static HashMap<String, Calendar> getMinMaxMetricStarts(BarKey bk) {
		HashMap<String, Calendar> metricTimes = new HashMap<String, Calendar>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MIN(start) AS minstart, MAX(start) AS maxstart FROM metrics WHERE symbol = ? AND duration = ?";
			PreparedStatement ps = c.prepareStatement(q);
			
			ps.setString(1, bk.symbol);
			ps.setString(2, bk.duration.toString());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Timestamp tsMinStart = rs.getTimestamp("minstart");
				Calendar minStart = Calendar.getInstance();
				minStart.setTimeInMillis(tsMinStart.getTime());
				
				Timestamp tsMaxStart = rs.getTimestamp("maxstart");
				Calendar maxStart = Calendar.getInstance();
				maxStart.setTimeInMillis(tsMaxStart.getTime());
				
				metricTimes.put("min", minStart);
				metricTimes.put("max", maxStart);
			}
			
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metricTimes;
	}
	
	/**
	 * Loads metric sequences (oldest to newest) starting from the bar of the last metric available, or as early as 2010-01-01 
	 * if the bar data goes back that far.
	 * 
	 * @param barKeys
	 * @return
	 */
	public static HashMap<MetricKey, ArrayList<Metric>> loadMetricSequenceHash(ArrayList<BarKey> barKeys, ArrayList<String> neededMetrics) {
		HashMap<MetricKey, ArrayList<Metric>> metricSequenceHash = new HashMap<MetricKey, ArrayList<Metric>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			for (BarKey bk : barKeys) {
				for (String metricName : neededMetrics) {
					
					MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
					ArrayList<Metric> ms = metricSequenceHash.get(mk);
					if (ms == null) {
						ms = new ArrayList<Metric>();
					}
					
					// Get the base date
					int neededBars = Constants.METRIC_NEEDED_BARS.get(metricName);
//					String q0 = "SELECT COALESCE((SELECT MAX(start) FROM metrics WHERE symbol = ? AND duration = ? AND name = ?), '2010-01-01 00:00:00')";
					String q0 = "SELECT COALESCE((SELECT start FROM (SELECT start FROM metrics WHERE symbol = ? AND duration = ? AND name = ? ORDER BY start DESC LIMIT ?) t ORDER BY start LIMIT 1), '2010-01-01 00:00:00')";
					PreparedStatement s0 = c.prepareStatement(q0);
					s0.setString(1, bk.symbol);
					s0.setString(2, bk.duration.toString());
					s0.setString(3, metricName);
					s0.setInt(4, neededBars);
					ResultSet rs0 = s0.executeQuery();
					Calendar startCal = Calendar.getInstance();
					while (rs0.next()) {
						Timestamp tsStart = rs0.getTimestamp(1);
						startCal.setTimeInMillis(tsStart.getTime());
						break;
					}
					
//					startCal = CalendarUtils.addBars(startCal, bk.duration, -neededBars);
					rs0.close();
					s0.close();
					
//					String alphaComparison = "SPY"; // TODO: probably change this.  Seems weird to compare bitcoin or forex to SPY.
					String q = "SELECT b.* " +
//							",(SELECT close FROM bar WHERE symbol = ? AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphaclose, " +
//							"(SELECT change FROM bar WHERE symbol = ? AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphachange " +
							"FROM bar b " +
							"WHERE b.start >= ? " +
							"AND b.symbol = ? " +
							"AND b.duration = ? " +
							"ORDER BY b.start";
					
					PreparedStatement s = c.prepareStatement(q);
//					s.setString(1, alphaComparison);
//					s.setString(2, alphaComparison);
					s.setTimestamp(1, new Timestamp(startCal.getTimeInMillis()));
					s.setString(2, bk.symbol);
					s.setString(3, bk.duration.toString());
					ResultSet rs = s.executeQuery();
					
					int counter = 0;
					
					while (rs.next()) {
						Timestamp tsStart = rs.getTimestamp("start");
						Calendar start = Calendar.getInstance();
						start.setTimeInMillis(tsStart.getTime());
						Timestamp tsEnd = rs.getTimestamp("end");
						Calendar end = Calendar.getInstance();
						end.setTimeInMillis(tsEnd.getTime());
						end.set(Calendar.SECOND, 0);
						long volume = rs.getLong("volume");
						float adjOpen = rs.getFloat("open");
						float adjClose = rs.getFloat("close");
						float adjHigh = rs.getFloat("high");
						float adjLow = rs.getFloat("low");
//						float alphaClose = rs.getFloat("alphaclose");
//						float alphaChange = rs.getFloat("alphachange");
						float gap = rs.getFloat("gap");
						float change = rs.getFloat("change");

						// Create a Metric
						Metric m = new Metric(metricName, bk.symbol, start, end, bk.duration, volume, adjOpen, adjClose, adjHigh, adjLow, gap, change, 0, 0);
						ms.add(m);
						counter++;
					}
					
					metricSequenceHash.put(mk, ms);
//					System.out.println("Adding " + counter + " metrics to MetricSequence for " + mk.toString());
					
					rs.close();
					s.close();
				}
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metricSequenceHash;
	}

	public static ArrayList<LinkedList<Metric>> loadMetricSequencesForRealtimeUpdates(ArrayList<BarKey> barKeys) {
		ArrayList<LinkedList<Metric>> metricSequences = new ArrayList<LinkedList<Metric>>();
		try {
			// Put the list of symbols together into a string for the query
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (BarKey bk : barKeys) {
				sb.append("'").append(bk.symbol).append("'").append(",");
			}
			String symbolsString = sb.toString();
			if (barKeys.size() > 0) {
				symbolsString = symbolsString.substring(0, symbolsString.length() - 1) + ")";
			}
			else {
				symbolsString = "()";
			}
			
			// Query
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q1 = "SELECT DISTINCT symbol, " +
						"(SELECT MIN(start) FROM (SELECT symbol, start FROM bar WHERE symbol IN " + symbolsString + " ORDER BY start DESC LIMIT 300) t) AS baseDate " +
						"FROM bar WHERE symbol IN " + symbolsString;
			Statement s1 = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs1 = s1.executeQuery(q1);

			while (rs1.next()) {
				String symbol = rs1.getString("symbol");
				String baseDate = rs1.getString("baseDate"); // 300 bars ago

				// Fill a "metricSequence" with the price data for the last X
				// days + however many days I need stats for
				String alphaComparison = "SPY"; // TODO: probably change this
				String q2 = "SELECT r.*, " +
						"(SELECT close FROM bar WHERE symbol = '" + alphaComparison + "' AND start <= r.start ORDER BY start DESC LIMIT 1) AS alphaclose, " +
						"(SELECT change FROM bar WHERE symbol = '" + alphaComparison + "' AND start <= r.start ORDER BY start DESC LIMIT 1) AS alphachange " +
						"FROM bar r " +
						"WHERE r.symbol = '" + symbol + "' " +
						"AND r.start >= '" + baseDate + "' " +
						"ORDER BY start ASC";
				
				LinkedList<Metric> metricSequence = new LinkedList<Metric>();
				
				Statement s2 = c.createStatement();
				ResultSet rs2 = s2.executeQuery(q2);
				while (rs2.next()) {
					Timestamp tsStart = rs2.getTimestamp("start");
					Calendar start = Calendar.getInstance();
					start.setTimeInMillis(tsStart.getTime());
					Timestamp tsEnd = rs2.getTimestamp("end");
					Calendar end = Calendar.getInstance();
					end.setTimeInMillis(tsEnd.getTime());
					end.set(Calendar.SECOND, 0);
					String duration = rs2.getString("duration");
					long volume = rs2.getLong("volume");
					float adjOpen = rs2.getFloat("open");
					float adjClose = rs2.getFloat("close");
					float adjHigh = rs2.getFloat("high");
					float adjLow = rs2.getFloat("low");
					float alphaClose = rs2.getFloat("alphaclose");
					float alphaChange = rs2.getFloat("alphachange");
					float gap = rs2.getFloat("gap");
					float change = rs2.getFloat("change");

					Metric day = new Metric(symbol, start, end, BAR_SIZE.valueOf(duration), volume, adjOpen, adjClose, adjHigh, adjLow, gap, change, alphaClose, alphaChange);
					metricSequence.add(day);
				}
				metricSequences.add(metricSequence);
				rs2.close();
				s2.close();
			}
			rs1.close();
			s1.close();
			c.close();
			
			return metricSequences;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return metricSequences;
		}
	}
	
	/**
	 * Loads metric sequences (oldest to newest) starting from a specified datetime
	 * 
	 * @param barKeys
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static ArrayList<BarWithMetricData> loadMetricSequenceHashForBackTests(ArrayList<BarKey> barKeys,  
			Calendar startDate, Calendar endDate) {
		
		ArrayList<BarWithMetricData> barWMDList = new ArrayList<BarWithMetricData>();
		try {
			Connection c1 = ConnectionSingleton.getInstance().getConnection();
			
			for (BarKey bk : barKeys) {
				String q1 = "SELECT * FROM bar WHERE start >= ? AND \"end\" <= ? AND symbol = ? AND duration = ? ORDER BY start";

				PreparedStatement s1 = c1.prepareStatement(q1);
				s1.setTimestamp(1, new Timestamp(startDate.getTimeInMillis()));
				s1.setTimestamp(2, new Timestamp(endDate.getTimeInMillis()));
				s1.setString(3, bk.symbol);
				s1.setString(4, bk.duration.toString());
				ResultSet rs1 = s1.executeQuery();
				
				while (rs1.next()) {
					Timestamp tsStart = rs1.getTimestamp("start");
					Calendar start = Calendar.getInstance();
					start.setTimeInMillis(tsStart.getTime());
					Timestamp tsEnd = rs1.getTimestamp("end");
					Calendar end = Calendar.getInstance();
					end.setTimeInMillis(tsEnd.getTime());
					end.set(Calendar.SECOND, 0);
					long volume = rs1.getLong("volume");
					float open = rs1.getFloat("open");
					float close = rs1.getFloat("close");
					float high = rs1.getFloat("high");
					float low = rs1.getFloat("low");
					float gap = rs1.getFloat("gap");
					float change = rs1.getFloat("change");
					boolean partial = rs1.getBoolean("partial");

					BarWithMetricData barWMD = new BarWithMetricData(bk.symbol, open, close, high, low, null, volume, null, change, gap, start, end, bk.duration, partial);
					
					// Now query for the metric data
					Connection c2 = ConnectionSingleton.getInstance().getConnection();
					String q2 = "SELECT name, value FROM metrics WHERE symbol = ? AND duration = ? AND start = ?";
					PreparedStatement s2 = c2.prepareStatement(q2);
					s2.setString(1, bk.symbol);
					s2.setString(2, bk.duration.toString());
					s2.setTimestamp(3, new Timestamp(start.getTimeInMillis()));
					ResultSet rs2 = s2.executeQuery();
					
					HashMap<String, Double> metricData = new HashMap<String, Double>();
					
					while (rs2.next()) {
						String name = rs2.getString("name");
						Double value = rs2.getDouble("value");
						metricData.put(name, value);
					}
					
					rs2.close();
					s2.close();
					c2.close();
					
					if (metricData.size() == 0) {
						throw new Exception("This bar doesn't have metric data! \n\n " + barWMD.toString());
					}
					else {
						barWMD.setMetricData(metricData);
					}
					barWMDList.add(barWMD);
				}

				rs1.close();
				s1.close();
				
			}
			c1.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return barWMDList;
	}

	public static void deleteStocksFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "DELETE FROM bar " +
						"WHERE symbol IN (SELECT symbol FROM indexlist WHERE index = 'NYSE' OR index = 'Nasdaq' OR index = 'DJIA' OR index = 'SP500' OR index = 'ETF' OR index = 'Stock Index') ";
			Statement s = c.createStatement();
			s.executeUpdate(q);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void dropMetricTable() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			String q = "DROP TABLE metrics";
			s.executeUpdate(q);
			s.close();
			c.close();
		}
		catch (Exception e) {}
	}
	
	public static void createMetricTable() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
	
			String q = "CREATE TABLE metrics " +
						"( " +
						"	name character varying(32) NOT NULL, " +
						"	symbol character varying(16) NOT NULL, " +
						"   start timestamp without time zone NOT NULL, " +
						"   \"end\" timestamp without time zone NOT NULL, " +
						"   duration character varying(16) NOT NULL, " +
						"   value real, " +
						"   CONSTRAINT metrics_pk PRIMARY KEY (name, symbol, start, \"end\") " +
						") " +
						"WITH (OIDS=FALSE);";
			s.execute(q);
			
			s.close();
			c.close();
		}
		catch (Exception e) {
		}
	}

	public static void insertMetrics(ArrayList<String> records) {
		try {
			String insertQuery = "INSERT INTO metrics(name, symbol, start, \"end\", duration, value) VALUES ";

			StringBuilder sb = new StringBuilder();
			for (String record : records) {
				System.out.println(record);
				sb.append(record + ", ");
			}
			String valuesPart = sb.toString();
			valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
			insertQuery = insertQuery + valuesPart;

			Connection c = ConnectionSingleton.getInstance().getConnection();
			Statement s = c.createStatement();
			s.executeUpdate(insertQuery);
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertIntoMetrics(Metric metric) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// First see if this bar exists in the DB
			String q = "SELECT * FROM metrics WHERE name = ? AND symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, metric.name);
			s.setString(2, metric.symbol);
			s.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
			s.setString(4, metric.duration.toString());
			
			ResultSet rs = s.executeQuery();
			boolean exists = false;
			while (rs.next()) {
				exists = true;
				break;
			}
			s.close();
			
			// If it doesn't exist, insert it
			if (!exists) {
				String q2 = "INSERT INTO metrics(name, symbol, start, \"end\", duration, value) " + 
							"VALUES (?, ?, ?, ?, ?, ?)";
				PreparedStatement s2 = c.prepareStatement(q2);
				s2.setString(1, metric.name);
				s2.setString(2, metric.symbol);
				s2.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
				s2.setTimestamp(4, new java.sql.Timestamp(metric.end.getTime().getTime()));
				s2.setString(5, metric.duration.toString());
				if (metric.value == null) {
					s2.setNull(6, java.sql.Types.FLOAT);
				}
				else {
					s2.setFloat(6, metric.value);
				}
				
				s2.executeUpdate();
				s2.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param metricName
	 * @param bk - optional
	 * @param type - "min" or "max"
	 * @param percentile - 0-100
	 * @return
	 */
	public static float getMetricValueAtPercentile(String metricName, BarKey bk, String type, float percentile) {
		float result = 0f;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Create query parameters & clauses
			String bkClause = "";
			if (bk != null) {
				bkClause = "AND duration = '" + bk.duration.toString() + "' AND symbol = '" + bk.symbol + "' ";
			}
	
			String sort1 = "ASC";
			String sort2 = "DESC";
			if (type.equals("max")) {
				sort1 = "DESC";
				sort2 = "ASC";
			}
			
			float divisor = 100f / percentile;
			
			String q = "SELECT * FROM (SELECT value FROM metrics WHERE name = ? " + bkClause + " ORDER BY value " + sort1 + " LIMIT (SELECT COUNT(*) / ? FROM metrics WHERE name = ? " + bkClause + " )) t ORDER BY value " + sort2 + " LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, metricName);
			s.setFloat(2, divisor);
			s.setString(3, metricName);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				result = rs.getFloat(1);
				break;
			}
			rs.close();
			s.close();
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static HashMap<MetricKey, MetricTimeCache> loadMetricTimeCache(ArrayList<BarKey> barKeys) {
		HashMap<MetricKey, MetricTimeCache> metricTimeCache = new HashMap<MetricKey, MetricTimeCache>();
		try {
			for (BarKey bk : barKeys) {
				for (String metricName : Constants.METRICS) {
					Connection c = ConnectionSingleton.getInstance().getConnection();
					String q = 	"SELECT MIN(start) as minstart, MAX(start) AS maxstart FROM metrics WHERE name = ? AND symbol = ? AND duration = ?";
					PreparedStatement ps = c.prepareStatement(q);
					ps.setString(1, metricName);
					ps.setString(2, bk.symbol);
					ps.setString(3, bk.duration.toString());
					
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						java.sql.Timestamp minStartTS = rs.getTimestamp("minstart");
						Calendar minStart = null;
						if (minStartTS != null) {
							minStart = Calendar.getInstance();
							minStart.setTimeInMillis(minStartTS.getTime());
						}
						
						java.sql.Timestamp maxStartTS = rs.getTimestamp("maxstart");
						Calendar maxStart = null;
						if (maxStartTS != null) {
							maxStart = Calendar.getInstance();
							maxStart.setTimeInMillis(maxStartTS.getTime());
						}
						
						if (minStart != null && maxStart != null) {
							MetricKey mk = new MetricKey(metricName, bk.symbol, bk.duration);
							MetricTimeCache mtc = new MetricTimeCache(minStart, maxStart);
							metricTimeCache.put(mk, mtc);
						}
					}
					rs.close();
					ps.close();
					c.close();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return metricTimeCache;
	}
	
	public static synchronized void insertOrUpdateIntoMetrics(ArrayList<Metric> metricSequence) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q2 = "INSERT INTO metrics(name, symbol, start, \"end\", duration, value) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement s2 = c.prepareStatement(q2);

			String q3 = "UPDATE metrics SET value = ? WHERE name = ? AND symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s3 = c.prepareStatement(q3);

			int numInserts = 0;
			int numUpdates = 0;
			
//			// Cache the bars we already have for this metric sequence - Using "starts" ArrayList is more sure, but a lot slower
////			ArrayList<String> starts = new ArrayList<String>();
//			Calendar minCal = Calendar.getInstance();
//			Calendar maxCal = Calendar.getInstance();
//			if (metricSequence != null && metricSequence.size() > 0) {
////				String q0 = "SELECT start FROM metrics WHERE name = ? AND symbol = ? AND duration = ?";
//				String q0 = "SELECT MIN(start) AS minstart, MAX(start) AS maxstart FROM metrics WHERE name = ? AND symbol = ? AND duration = ?";
//				PreparedStatement s0 = c.prepareStatement(q0);
//				s0.setString(1, metricSequence.get(0).name);
//				s0.setString(2, metricSequence.get(0).symbol);
//				s0.setString(3, metricSequence.get(0).duration.toString());
//				
//				ResultSet rs0 = s0.executeQuery();
//				while (rs0.next()) {
////					Timestamp ts = rs0.getTimestamp("start");
////					Calendar cal = Calendar.getInstance();
////					cal.setTimeInMillis(ts.getTime());
////					starts.add(cal.getTime().toString());
//					
//					Timestamp minTS = rs0.getTimestamp("minstart");
//					if (minTS == null) {
//						Calendar min = Calendar.getInstance();
//						min.set(Calendar.YEAR, 2014);
//						min.set(Calendar.MONTH, 0);
//						min.set(Calendar.DATE, 1);
//						min.set(Calendar.HOUR, 0);
//						min.set(Calendar.MINUTE, 0);
//						min.set(Calendar.SECOND, 0);
//						min.set(Calendar.MILLISECOND, 0);
//						minTS = new Timestamp(min.getTimeInMillis());
//					}
//					minCal.setTimeInMillis(minTS.getTime());
//					Timestamp maxTS = rs0.getTimestamp("maxstart");
//					if (maxTS == null) {
//						Calendar max = Calendar.getInstance();
//						max.set(Calendar.YEAR, 2014);
//						max.set(Calendar.MONTH, 0);
//						max.set(Calendar.DATE, 1);
//						max.set(Calendar.HOUR, 0);
//						max.set(Calendar.MINUTE, 0);
//						max.set(Calendar.SECOND, 0);
//						max.set(Calendar.MILLISECOND, 0);
//						maxTS = new Timestamp(max.getTimeInMillis());
//					}
//					maxCal.setTimeInMillis(maxTS.getTime());
//				}
//				rs0.close();
//				s0.close();
//			}
			
			MetricKey mk = new MetricKey(metricSequence.get(0).name, metricSequence.get(0).symbol, metricSequence.get(0).duration);
			MetricTimeCache mtc = MetricSingleton.getInstance().getMetricTimeCache(mk);
	
			for (Metric metric : metricSequence) {
				if (metric.value != null) {
					// First see if this metric exists	
					boolean exists = false;
					if (mtc != null && (CalendarUtils.areSame(metric.start, mtc.minStart) || CalendarUtils.areSame(metric.start, mtc.maxStart))) {
						exists = true;
					}
					if (mtc != null && metric.start.after(mtc.minStart) && metric.start.before(mtc.maxStart)) {
						exists = true;
					}
					
					// If it doesn't exist, insert it
					if (!exists) {
						MetricSingleton.getInstance().updateMetricTimeCache(mk, metric.start); // There's a check in there to make sure it's only updating if it's the latest start
						s2.setString(1, metric.name);
						s2.setString(2, metric.symbol);
						s2.setTimestamp(3, new java.sql.Timestamp(metric.start.getTime().getTime()));
						s2.setTimestamp(4, new java.sql.Timestamp(metric.end.getTime().getTime()));
						s2.setString(5, metric.duration.toString());
						if (metric.value == null) {
							s2.setNull(6, java.sql.Types.FLOAT);
						}
						else {
							s2.setFloat(6, metric.value);
						}
						s2.addBatch();
						numInserts++;
					}
					else { // Otherwise it does exist, so update it
						if (metric.value == null) {
							s3.setNull(1, java.sql.Types.FLOAT);
						}
						else {
							s3.setFloat(1, metric.value);
						}
						s3.setString(2, metric.name);
						s3.setString(3, metric.symbol);
						s3.setTimestamp(4, new java.sql.Timestamp(metric.start.getTime().getTime()));
						s3.setString(5, metric.duration.toString());
						s3.addBatch();
						numUpdates++;
					}
				}
			}
			
			if (numInserts > 0) {
				s2.executeBatch();
//				System.out.println("# Inserts: " + numInserts);
			}
			if (numUpdates > 0) {
//				System.out.println("# Updates: " + numUpdates);
				s3.executeBatch();
			}
			s2.close();
			s3.close();
			
			c.close();
		}
		catch (BatchUpdateException e) {
			e.printStackTrace();
			e.getNextException().printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public static ArrayList<String> getDistinctSymbolDurations(ArrayList<String> indexes) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			if (indexes == null || indexes.size() == 0) {
				return list;
			}
			
			String indexClause = "WHERE i.index IN (";
			for (String index : indexes) {
				indexClause += "'" + index + "',";
			}
			indexClause = indexClause.substring(0, indexClause.length() - 1) + ")";
		
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT b.symbol, b.duration, COUNT(b.*) AS barcount " +
						"FROM bar b " +
						"INNER JOIN indexlist i ON b.symbol = i.symbol " +
						indexClause +
						"GROUP BY b.symbol, b.duration " + 
						"ORDER BY b.symbol, b.duration";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				list.add(rs.getString("duration") + " - " + rs.getString("symbol") + " (" + rs.getInt("barcount") + ")");
			}
			rs.close();
			s.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(list);
		return list;
	}

	public static void deleteMostRecentBar(String symbol, String duration) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM bar WHERE (symbol, duration, start) IN ( " +
						"SELECT symbol, duration, MAX(start) FROM bar " +
						"WHERE symbol = ? AND duration = ? " +
						"GROUP BY symbol, duration)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, duration);
			ps.executeUpdate();
			ps.close();
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMostRecentMetrics(String symbol, String duration) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM metrics WHERE (symbol, duration, start) IN ( " +
						"SELECT symbol, duration, MAX(start) AS start FROM metrics " +
						"WHERE symbol = ? AND duration = ? " +
						"GROUP BY symbol, duration)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, duration);
			ps.executeUpdate();
			ps.close();
			
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteMostRecentMetrics(String symbol, String duration, ArrayList<String> metrics) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM metrics WHERE (symbol, duration, start) IN ( " +
						"SELECT symbol, duration, MAX(start) AS start FROM metrics " +
						"WHERE symbol = ? AND duration = ? " +
						"GROUP BY symbol, duration) ";
			String metricsTerm = "";
			if (metrics != null && metrics.size() > 0) {
				metricsTerm = "AND name IN (";
				
				for (String metric : metrics) {
					metricsTerm += "'" + metric + "',";
				}
				
				metricsTerm = metricsTerm.substring(0, metricsTerm.length() - 1) + ")";
			}
			q += metricsTerm;
			
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, symbol);
			ps.setString(2, duration);
			ps.executeUpdate();
			ps.close();
			
			c.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteHolidaysForStocksFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"DELETE FROM bar " +
						"WHERE symbol IN (SELECT symbol FROM indexlist WHERE index = 'NYSE' OR index = 'Nasdaq' OR index = 'DJIA' OR index = 'SP500' OR index = 'ETF' OR index = 'Stock Index') " +
						"AND start IN (SELECT * FROM (SELECT * FROM generate_series('2012-01-01'::date, now()::date, '1 day') AS alldates) t WHERE alldates NOT IN (SELECT * FROM tradingdays))";
		
			PreparedStatement ps = c.prepareStatement(q);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	public static Calendar getMaxStartFromBar() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT MAX(start) AS d FROM bar";
			PreparedStatement ps = c.prepareStatement(q);

			ResultSet rs = ps.executeQuery();
			java.sql.Timestamp ts = null;
			Calendar cal = Calendar.getInstance();
			while (rs.next()) {
				cal.setTimeInMillis(rs.getTimestamp(1).getTime());
				break;
			}

			rs.close();
			ps.close();
			c.close();
			
			return cal;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void insertIntoTick(ArrayList<String> records) {
		try {
			if (records != null && records.size() > 0) {
				Connection c = ConnectionSingleton.getInstance().getConnection();
				String q = "INSERT INTO tick(symbol, price, volume, \"timestamp\") VALUES ";
				StringBuilder sb = new StringBuilder();
				for (String record : records) {
					sb.append("(" + record + "), ");
				}
				String valuesPart = sb.toString();
				valuesPart = valuesPart.substring(0, valuesPart.length() - 2);
				q = q + valuesPart;
	
				Statement s = c.createStatement();
				s.executeUpdate(q);
				s.close();
				c.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Calendar getTickEarliestTick(String symbol) {
		Calendar earliestTick = Calendar.getInstance();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT timestamp FROM tick WHERE symbol = ? ORDER BY \"timestamp\" LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				earliestTick.setTimeInMillis(rs.getTimestamp(1).getTime());
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return earliestTick;
	}
	
	public static Calendar getTickLatestTick(String symbol) {
		Calendar latestTick = Calendar.getInstance();
		latestTick.set(2000, 0, 1); // Just make it old in case there isn't any tick data
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT timestamp FROM tick WHERE symbol = ? ORDER BY \"timestamp\" DESC LIMIT 1";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				latestTick.setTimeInMillis(rs.getTimestamp(1).getTime());
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return latestTick;
	}
	
	public static void setMostRecentBarsComplete(BarKey bk) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE bar SET partial = false WHERE symbol = ? AND duration = ? AND start IN (SELECT start FROM bar WHERE symbol = ? AND duration = ? ORDER BY start DESC LIMIT 2)";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, bk.symbol);
			s.setString(2, bk.duration.toString());
			s.setString(3, bk.symbol);
			s.setString(4, bk.duration.toString());
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts if the bar does not exist. Updates if it's marked as partial or if the numTrades column doesn't have data (i.e. the record didn't have tick data when it was made)
	 * 
	 * @param bar
	 */
	public static void insertOrUpdateIntoBar(Bar bar) {
		// First see if this bar exists in the DB
		boolean exists = false;
		boolean partial = false;
		Object numTrades = null;
		
		Connection c = ConnectionSingleton.getInstance().getConnection();
		try {
			String q = "SELECT partial, numtrades FROM bar WHERE symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, bar.symbol);
			s.setTimestamp(2, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
			s.setString(3, bar.duration.toString());
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				exists = true;
				partial = rs.getBoolean("partial");
				numTrades = rs.getObject("numtrades");
				break;
			}
			rs.close();
			s.close();
			c.close();	
		}
		catch (Exception e1) {
			System.err.println("Fear not 1 - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
			e1.printStackTrace();
			try {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		finally {
			// If there are no trades for this existing bar, say its partial so it can be updated with bar data that contains this (if coming from tick data)
			if (numTrades == null) {
				partial = true;
			}
		}
	
		// If it doesn't exist, insert it
		if (!exists) {
			Connection c2 = ConnectionSingleton.getInstance().getConnection();
			try {
				String q2 = "INSERT INTO bar(symbol, open, close, high, low, vwap, volume, numtrades, change, gap, start, \"end\", duration, partial) " + 
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement s2 = c2.prepareStatement(q2);
				s2.setString(1, bar.symbol);
				s2.setBigDecimal(2, new BigDecimal(bar.open).setScale(6, BigDecimal.ROUND_HALF_UP));
				s2.setBigDecimal(3, new BigDecimal(bar.close).setScale(6, BigDecimal.ROUND_HALF_UP));
				s2.setBigDecimal(4, new BigDecimal(bar.high).setScale(6, BigDecimal.ROUND_HALF_UP));
				s2.setBigDecimal(5, new BigDecimal(bar.low).setScale(6, BigDecimal.ROUND_HALF_UP));
				if (bar.vwap == null) {
					s2.setNull(6, Types.DECIMAL);
				}
				else {
					s2.setFloat(6, bar.vwap);
				}
				s2.setBigDecimal(7, new BigDecimal(bar.volume).setScale(6, BigDecimal.ROUND_HALF_UP));
				if (bar.numTrades == null) {
					s2.setNull(8, Types.INTEGER);
				}
				else {
					s2.setInt(8, bar.numTrades);
				}
				if (bar.change == null) {
					s2.setNull(9, Types.DECIMAL);
				}
				else {
					s2.setFloat(9, bar.change);
				}
				if (bar.gap == null) {
					s2.setNull(10, Types.DECIMAL);
				}
				else {
					s2.setFloat(10, bar.gap);
				}
				s2.setTimestamp(11, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
				s2.setTimestamp(12, new java.sql.Timestamp(bar.periodEnd.getTime().getTime()));
				s2.setString(13, bar.duration.toString());
				s2.setBoolean(14, bar.partial);
				
				s2.executeUpdate();
				s2.close();
				c2.close();
			}
			catch (Exception e1) {
				System.err.println("Fear not 2 - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
				e1.printStackTrace();
				try {
					if (c2 != null && !c2.isClosed()) {
						c2.close();
					}
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		// It exists and it's partial, so we need to update it.
		else if (partial) {
			Connection c3 = ConnectionSingleton.getInstance().getConnection();
			try {
				String q3 = "UPDATE bar SET symbol = ?, open = ?, close = ?, high = ?, low = ?, vwap = ?, volume = ?, numtrades = ?, change = ?, gap = ?, start = ?, \"end\" = ?, duration = ?, partial = ? " +
							"WHERE symbol = ? AND start = ? AND duration = ?";
				PreparedStatement s3 = c3.prepareStatement(q3);
				s3.setString(1, bar.symbol);
				s3.setBigDecimal(2, new BigDecimal(bar.open).setScale(6, BigDecimal.ROUND_HALF_UP));
				s3.setBigDecimal(3, new BigDecimal(bar.close).setScale(6, BigDecimal.ROUND_HALF_UP));
				s3.setBigDecimal(4, new BigDecimal(bar.high).setScale(6, BigDecimal.ROUND_HALF_UP));
				s3.setBigDecimal(5, new BigDecimal(bar.low).setScale(6, BigDecimal.ROUND_HALF_UP));
				if (bar.vwap == null) {
					s3.setNull(6, Types.DECIMAL);
				}
				else {
					s3.setFloat(6, bar.vwap);
				}
				s3.setBigDecimal(7, new BigDecimal(bar.volume).setScale(6, BigDecimal.ROUND_HALF_UP));
				if (bar.numTrades == null) {
					s3.setNull(8, Types.INTEGER);
				}
				else {
					s3.setInt(8, bar.numTrades);
				}
				if (bar.change == null) {
					s3.setNull(9, Types.DECIMAL);
				}
				else {
					s3.setFloat(9, bar.change);
				}
				if (bar.gap == null) {
					s3.setNull(10, Types.DECIMAL);
				}
				else {
					s3.setFloat(10, bar.gap);
				}
				s3.setTimestamp(11, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
				s3.setTimestamp(12, new java.sql.Timestamp(bar.periodEnd.getTime().getTime()));
				s3.setString(13, bar.duration.toString());
				s3.setBoolean(14, bar.partial);
				s3.setString(15, bar.symbol);
				s3.setTimestamp(16, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
				s3.setString(17, bar.duration.toString());
				
				s3.executeUpdate();
				s3.close();
				c3.close();
			}
			catch (Exception e1) {
				System.err.println("Fear not 3 - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
				e1.printStackTrace();
				try {
					if (c3 != null && !c3.isClosed()) {
						c3.close();
					}
				}
				catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	
	public static void insertOrUpdateIntoMetricCalcEssentials(MetricKey mk, HashMap<String, Object> mce) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// First check to see if this MCE exists in the metriccalcessentials table
			String q = "SELECT * FROM metriccalcessentials WHERE name = ? AND symbol = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, mk.name);
			s.setString(2, mk.symbol);
			s.setString(3, mk.duration.toString());
			
			ResultSet rs = s.executeQuery();
			boolean exists = false;
			while (rs.next()) {
				exists = true;
				break;
			}
			s.close();
			
			// If it doesn't exist, insert it
			if (!exists) {
				Iterator i = mce.entrySet().iterator();
				while (i.hasNext()) {
					// Get the VarName and VarValue out of the MCE hash
					Map.Entry pair = (Map.Entry)i.next();
					String varName = pair.getKey().toString();
					Array varValue = null;
					Object o = pair.getValue();
					if (!(o instanceof Calendar)) {
						if (o instanceof Float || o instanceof Integer || o instanceof Double) {
							varValue = c.createArrayOf("float", new Float[] {Float.parseFloat(o.toString())});
						}
						else if (o instanceof List) {
							List l = (List)o;
							varValue = c.createArrayOf("float", l.toArray());
						}
						
						String q1 = "INSERT INTO metriccalcessentials(name, symbol, duration, start, varname, varvalue) VALUES (?, ?, ?, ?, ?, ?)";
						PreparedStatement s1 = c.prepareStatement(q1);
						s1.setString(1, mk.name);
						s1.setString(2, mk.symbol);
						s1.setString(3, mk.duration.toString());
						Calendar start = (Calendar)mce.get("start");
						s1.setTimestamp(4, new java.sql.Timestamp(start.getTime().getTime()));
						s1.setString(5, varName);
						s1.setArray(6, varValue);
						
						s1.executeUpdate();
						s1.close();
					}
				}
			}
			else { // It does exist, update it
				Iterator i = mce.entrySet().iterator();
				while (i.hasNext()) {
					// Get the VarName and VarValue out of the MCE hash
					Map.Entry pair = (Map.Entry)i.next();
					String varName = pair.getKey().toString();
					Array varValue = null;
					Object o = pair.getValue();
					if (!(o instanceof Calendar)) {
						if (o instanceof Float || o instanceof Integer || o instanceof Double) {
							varValue = c.createArrayOf("float", new Float[] {Float.parseFloat(o.toString())});
						}
						else if (o instanceof List) {
							List l = (List)o;
							varValue = c.createArrayOf("float", l.toArray());
						}
						
						String q1 = "UPDATE metriccalcessentials SET start = ?, varname = ?, varvalue = ? WHERE name = ? AND symbol = ? AND duration = ? AND varname = ?";
						PreparedStatement s1 = c.prepareStatement(q1);
						Calendar start = (Calendar)mce.get("start");
						s1.setTimestamp(1, new java.sql.Timestamp(start.getTime().getTime()));
						s1.setString(2, varName);
						s1.setArray(3, varValue);
						s1.setString(4, mk.name);
						s1.setString(5, mk.symbol);
						s1.setString(6, mk.duration.toString());
						s1.setString(7, varName);
						
						s1.executeUpdate();
						s1.close();
					}
				}
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param bk
	 * @param start
	 * @param end
	 * @param metricNames
	 * @param subsetModulo - Use null if you want all data.  Use a multiple of the bar duration if you want a subset.  2x multiple = 1/2 data, 3x multiple = 1/3 data, etc.
	 * @return
	 */
	public static ArrayList<HashMap<String, Object>> getTrainingSet(BarKey bk, Calendar start, Calendar end, ArrayList<String> metricNames, Integer subsetModulo) {
		ArrayList<HashMap<String, Object>> trainingSet = new ArrayList<HashMap<String, Object>>();
		try {
			// Create metric clauses
			String metricColumnClause = "";
			for (int a = 0; a < metricNames.size(); a++) {
				metricColumnClause += ", m" + a + ".value AS m" + a + " ";
			}
			
			String metricJoinClause = "";
			for (int a = 0; a < metricNames.size(); a++) {
				String metricName = metricNames.get(a);
				metricJoinClause += "LEFT OUTER JOIN metrics m" + a + " ON b.symbol = m" + a + ".symbol AND b.duration = m" + a + ".duration AND b.start = m" + a + ".start AND m" + a + ".name = '" + metricName + "' ";
			}
			
			int numBars = CalendarUtils.getNumBars(start, end, bk.duration);
			String startOp = ">=";
			String endOp = "<=";
			if (numBars == 1) {
				startOp = "=";
				endOp = "=";
			}
			
			Connection c1 = ConnectionSingleton.getInstance().getConnection();

			String q1 = 	"SELECT b.*, date_part('hour', b.start) AS hour " + metricColumnClause + 
						"FROM bar b " + metricJoinClause +
						"WHERE b.symbol = ? AND b.duration = ? AND b.start " + startOp + " ? AND b.\"end\" " + endOp + " ? ";
			if (subsetModulo != null) {
				q1 += "AND ((EXTRACT(HOUR FROM b.start))::integer * 60 + (EXTRACT(MINUTE FROM b.start))::integer) % " + subsetModulo + " = 0";
			}
			q1 += " ORDER BY b.start DESC";
			PreparedStatement s1 = c1.prepareStatement(q1);
			s1.setString(1, bk.symbol);
			s1.setString(2, bk.duration.toString());
			s1.setTimestamp(3, new Timestamp(start.getTimeInMillis()));
			s1.setTimestamp(4, new Timestamp(end.getTimeInMillis()));
			
			ResultSet rs = s1.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> record = new HashMap<String, Object>();
				float open = rs.getFloat("open");
				float close = rs.getFloat("close");
				float high = rs.getFloat("high");
				float low = rs.getFloat("low");
				int hour = rs.getInt("hour");
				Timestamp startTS = rs.getTimestamp("start");
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				record.put("open", open);
				record.put("close", close);
				record.put("high", high);
				record.put("low", low);
				record.put("hour", hour);
				record.put("start", startTS);
				record.put("symbol", symbol);
				record.put("duration", duration);
				for (int a = 0; a < metricNames.size(); a++) {
					String metricName = metricNames.get(a);
					if (!metricName.equals("hour") && !metricName.equals("symbol") && !metricName.equals("close")) {
						float metricValue = rs.getFloat("m" + a);
						record.put(metricName, metricValue);
					}
				}
				
				trainingSet.add(record);
			}
			
			rs.close();
			s1.close();
			c1.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return trainingSet;
	}
	
	public static ArrayList<HashMap<String, Object>> getBarAndMetricInfo() {
		ArrayList<HashMap<String, Object>> barAndMetricInfo = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = 	"SELECT b.*, m.metricmin, m.metricmax, m.metricage FROM ( " +
						"SELECT symbol, duration, MIN(start) AS barmin, MAX(start) AS barmax, AGE(now(), MAX(start)) AS barage, COUNT(*) AS barcount " + 
						"FROM bar GROUP BY symbol, duration ORDER BY symbol, duration) b " +
						"LEFT OUTER JOIN (SELECT symbol, duration, MIN(start) AS metricmin, MAX(start) AS metricmax, AGE(now(), MAX(start)) AS metricage FROM metrics GROUP BY symbol, duration) m " +
						"ON b.symbol = m.symbol AND b.duration = m.duration";
			PreparedStatement ps = c.prepareStatement(q);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> record = new HashMap<String, Object>();
				
				String symbol = rs.getString("symbol");
				String duration = rs.getString("duration");
				Timestamp barminTS = rs.getTimestamp("barmin");
				Calendar barmin = Calendar.getInstance();
				barmin.setTimeInMillis(barminTS.getTime());
				Timestamp barmaxTS = rs.getTimestamp("barmax");
				Calendar barmax = Calendar.getInstance();
				barmax.setTimeInMillis(barmaxTS.getTime());
				String barage = rs.getString("barage");
				int barcount = rs.getInt("barcount");
				
				Timestamp metricminTS = rs.getTimestamp("metricmin");
				Calendar metricmin = Calendar.getInstance();
				if (metricminTS != null) {
					metricmin.setTimeInMillis(metricminTS.getTime());
				}
				else {
					metricmin = null;
				}
				
				Timestamp metricmaxTS = rs.getTimestamp("metricmax");
				Calendar metricmax = Calendar.getInstance();
				if (metricmaxTS != null) {
					metricmax.setTimeInMillis(metricmaxTS.getTime());
				}
				else {
					metricmax = null;
				}
				
				String metricage = rs.getString("metricage");
				if (metricage == null) {
					metricage = "";
				}
				
				record.put("symbol", symbol);
				record.put("duration", duration);
				record.put("barmin", sdf.format(barmin.getTime()));
				record.put("barmax", sdf.format(barmax.getTime()));
				record.put("barage", barage);
				record.put("barcount", barcount);
				if (metricmin != null) {
					record.put("metricmin", sdf.format(metricmin.getTime()));
				}
				else {
					record.put("metricmin", "");
				}
				if (metricmax != null) {
					record.put("metricmax", sdf.format(metricmax.getTime()));
				}
				else {
					record.put("metricmax", "");
				}
				record.put("metricage", metricage);
				barAndMetricInfo.add(record);
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return barAndMetricInfo;
	}
	
	public static ArrayList<Model> getModels(String whereClause) {
		ArrayList<Model> models = new ArrayList<Model>();
		try {
			if (whereClause == null || whereClause.equals("")) {
				whereClause = " LIMIT 20";
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "SELECT * FROM models " + whereClause;
			PreparedStatement ps = c.prepareStatement(q);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String type = rs.getString("type");
				String modelFile = rs.getString("modelfile");
				String algo = rs.getString("algo");
				String params = rs.getString("params");
				String symbol = rs.getString("symbol");
				BAR_SIZE duration = BAR_SIZE.valueOf(rs.getString("duration"));
				boolean interbarData = rs.getBoolean("interbardata");
				Array metricArray = rs.getArray("metrics");
				String[] metrics = (String[])metricArray.getArray();
				ArrayList<String> metricList = new ArrayList<String>(Arrays.asList(metrics));
				Timestamp trainStartTS = rs.getTimestamp("trainstart");
				Calendar trainStart = Calendar.getInstance();
				trainStart.setTimeInMillis(trainStartTS.getTime());
				Timestamp trainEndTS = rs.getTimestamp("trainend");
				Calendar trainEnd = Calendar.getInstance();
				trainEnd.setTimeInMillis(trainEndTS.getTime());
				Timestamp testStartTS = rs.getTimestamp("teststart");
				Calendar testStart = Calendar.getInstance();
				testStart.setTimeInMillis(testStartTS.getTime());
				Timestamp testEndTS = rs.getTimestamp("testend");
				Calendar testEnd = Calendar.getInstance();
				testEnd.setTimeInMillis(testEndTS.getTime());
				String sellMetric = rs.getString("sellmetric");
				float sellMetricValue = rs.getBigDecimal("sellmetricvalue").floatValue();
				String stopMetric = rs.getString("stopmetric");
				float stopMetricValue = rs.getBigDecimal("stopmetricvalue").floatValue();
				int numBars = rs.getInt("numbars");
				int numClasses = rs.getInt("numclasses");
				int trainDatasetSize = rs.getInt("traindatasetsize");
				int trainTrueNegatives = rs.getInt("traintruenegatives");
				int trainFalseNegatives = rs.getInt("trainfalsenegatives");
				int trainFalsePositives = rs.getInt("trainfalsepositives");
				int trainTruePositives = rs.getInt("traintruepositives");
				double trainTruePositiveRate = rs.getDouble("traintruepositiverate");
				double trainFalsePositiveRate = rs.getDouble("trainfalsepositiverate");
				double trainCorrectRate = rs.getDouble("traincorrectrate");
				double trainKappa = rs.getDouble("trainKappa");
				double trainMeanAbsoluteError = rs.getDouble("trainmeanabsoluteerror");
				double trainRootMeanSquaredError = rs.getDouble("trainrootmeansquarederror");
				double trainRelativeAbsoluteError = rs.getDouble("trainrelativeabsoluteerror");
				double trainRootRelativeSquaredError = rs.getDouble("trainrootrelativesquarederror");
				double trainROCArea = rs.getDouble("trainrocarea");
				int testDatasetSize = rs.getInt("testdatasetsize");
				int testTrueNegatives = rs.getInt("testtruenegatives");
				int testFalseNegatives = rs.getInt("testfalsenegatives");
				int testFalsePositives = rs.getInt("testfalsepositives");
				int testTruePositives = rs.getInt("testtruepositives");
				double testTruePositiveRate = rs.getDouble("testtruepositiverate");
				double testFalsePositiveRate = rs.getDouble("testfalsepositiverate");
				double testCorrectRate = rs.getDouble("testcorrectrate");
				double testKappa = rs.getDouble("testKappa");
				double testMeanAbsoluteError = rs.getDouble("testmeanabsoluteerror");
				double testRootMeanSquaredError = rs.getDouble("testrootmeansquarederror");
				double testRelativeAbsoluteError = rs.getDouble("testrelativeabsoluteerror");
				double testRootRelativeSquaredError = rs.getDouble("testrootrelativesquarederror");
				double testROCArea = rs.getDouble("testrocarea");
				double[] testBucketPercentCorrect = new double[5];
				Array testBucketPercentCorrectArray = rs.getArray("testbucketpercentcorrect");
				if (testBucketPercentCorrectArray != null) {
					BigDecimal[] testBucketPercentCorrectBD = (BigDecimal[])testBucketPercentCorrectArray.getArray();
					for (int a = 0; a < testBucketPercentCorrect.length; a++) {
						testBucketPercentCorrect[a] = testBucketPercentCorrectBD[a].doubleValue();
					}
				}
				double[] testBucketDistribution = new double[5];
				Array testBucketDistributionArray = rs.getArray("testbucketdistribution"); 
				if (testBucketDistributionArray != null) {
					BigDecimal[] testBucketDistributionBD = (BigDecimal[])testBucketDistributionArray.getArray();
					for (int a = 0; a < testBucketDistribution.length; a++) {
						testBucketDistribution[a] = testBucketDistributionBD[a].doubleValue();
					}
				}
				double[] testBucketPValues = new double[5];
				Array testBucketPValuesArray = rs.getArray("testbucketpvalues"); 
				if (testBucketPValuesArray != null) {
					BigDecimal[] testBucketPValuesBD = (BigDecimal[])testBucketPValuesArray.getArray();
					for (int a = 0; a < testBucketPValues.length; a++) {
						testBucketPValues[a] = testBucketPValuesBD[a].doubleValue();
					}
				}
				String notes = rs.getString("notes");
				boolean favorite = rs.getBoolean("favorite");
				boolean tradeOffPrimary = rs.getBoolean("tradeoffprimary");
				boolean tradeOffOpposite = rs.getBoolean("tradeoffopposite");
				boolean useInBackTests = rs.getBoolean("useinbacktests");
				Timestamp baseDateTS = rs.getTimestamp("basedate");
				Calendar baseDate = Calendar.getInstance();
				if (baseDateTS != null) {
					baseDate.setTimeInMillis(baseDateTS.getTime());
				}
				
				Model model = new Model(type, modelFile, algo, params, new BarKey(symbol, duration), interbarData, metricList,
						trainStart, trainEnd, testStart, testEnd, sellMetric,
						sellMetricValue, stopMetric, stopMetricValue, numBars, numClasses, trainDatasetSize,
						trainTrueNegatives, trainFalseNegatives, trainFalsePositives, trainTruePositives,
						trainTruePositiveRate, trainFalsePositiveRate, trainCorrectRate, trainKappa,
						trainMeanAbsoluteError, trainRootMeanSquaredError, trainRelativeAbsoluteError,
						trainRootRelativeSquaredError, trainROCArea, testDatasetSize, testTrueNegatives,
						testFalseNegatives, testFalsePositives, testTruePositives, testTruePositiveRate,
						testFalsePositiveRate, testCorrectRate, testKappa, testMeanAbsoluteError,
						testRootMeanSquaredError, testRelativeAbsoluteError, testRootRelativeSquaredError,
						testROCArea, testBucketPercentCorrect, testBucketDistribution, testBucketPValues, 
						notes, favorite, tradeOffPrimary, tradeOffOpposite, useInBackTests, baseDate);
				model.id = id;
				
				models.add(model);
			}
			
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return models;
	}
	
	public static int insertModel(Model m) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "INSERT INTO models( " +
			            "type, modelfile, algo, params, symbol, duration, interbardata, metrics, trainstart,  " +
			            "trainend, teststart, testend, sellmetric, sellmetricvalue, stopmetric,  " +
			            "stopmetricvalue, numbars, numclasses, traindatasetsize, traintruenegatives,  " +
			            "trainfalsenegatives, trainfalsepositives, traintruepositives,  " +
			            "traintruepositiverate, trainfalsepositiverate, traincorrectrate,  " +
			            "trainkappa, trainmeanabsoluteerror, trainrootmeansquarederror,  " +
			            "trainrelativeabsoluteerror, trainrootrelativesquarederror, trainrocarea,  " +
			            "testdatasetsize, testtruenegatives, testfalsenegatives, testfalsepositives,  " +
			            "testtruepositives, testtruepositiverate, testfalsepositiverate,  " +
			            "testcorrectrate, testkappa, testmeanabsoluteerror, testrootmeansquarederror,  " +
			            "testrelativeabsoluteerror, testrootrelativesquarederror, testrocarea, " +
			            "testbucketpercentcorrect, testbucketdistribution, testbucketpvalues, " +
			            "notes, favorite, tradeoffprimary, tradeoffopposite, basedate) " +
			            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			
			Array testBucketPercentCorrectArray = c.createArrayOf("numeric", ArrayUtils.toObject(m.testBucketPercentCorrect));
			Array testBucketDistribution = c.createArrayOf("numeric", ArrayUtils.toObject(m.testBucketDistribution));
			Array testBucketPValues = c.createArrayOf("numeric", ArrayUtils.toObject(m.testBucketPValues));
			
			ps.setString(1, m.type);
			if (m.modelFile == null) {
				ps.setNull(2, Types.VARCHAR);
			}
			else {
				ps.setString(2, m.modelFile);
			}
			ps.setString(3, m.algo);
			if (m.params == null) {
				ps.setNull(4, Types.VARCHAR);
			}
			else {
				ps.setString(4, m.params);
			}
			ps.setString(5, m.bk.symbol);
			ps.setString(6, m.bk.duration.toString());
			ps.setBoolean(7, m.interBarData);
			ps.setArray(8, c.createArrayOf("text", m.metrics.toArray()));
			ps.setTimestamp(9, new Timestamp(m.trainStart.getTime().getTime()));
			ps.setTimestamp(10, new Timestamp(m.trainEnd.getTime().getTime()));
			ps.setTimestamp(11, new Timestamp(m.testStart.getTime().getTime()));
			ps.setTimestamp(12, new Timestamp(m.testEnd.getTime().getTime()));
			ps.setString(13, m.sellMetric);
			ps.setBigDecimal(14, new BigDecimal(m.sellMetricValue).setScale(2, RoundingMode.HALF_UP));
			ps.setString(15, m.stopMetric);
			ps.setBigDecimal(16, new BigDecimal(m.stopMetricValue).setScale(2, RoundingMode.HALF_UP));
			ps.setInt(17, m.numBars);
			ps.setInt(18, m.numClasses);
			ps.setInt(19, m.trainDatasetSize);
			ps.setInt(20, m.trainTrueNegatives);
			ps.setInt(21, m.trainFalseNegatives);
			ps.setInt(22, m.trainFalsePositives);
			ps.setInt(23, m.trainTruePositives);
			ps.setDouble(24, m.trainTruePositiveRate);
			ps.setDouble(25, m.trainFalsePositiveRate);
			ps.setDouble(26, m.trainCorrectRate);
			ps.setDouble(27, m.trainKappa);
			ps.setDouble(28, m.trainMeanAbsoluteError);
			ps.setDouble(29, m.trainRootMeanSquaredError);
			ps.setDouble(30, m.trainRelativeAbsoluteError);
			ps.setDouble(31, m.trainRootRelativeSquaredError);
			ps.setDouble(32, m.trainROCArea);
			ps.setInt(33, m.testDatasetSize);
			ps.setInt(34, m.testTrueNegatives);
			ps.setInt(35, m.testFalseNegatives);
			ps.setInt(36, m.testFalsePositives);
			ps.setInt(37, m.testTruePositives);
			ps.setDouble(38, m.testTruePositiveRate);
			ps.setDouble(39, m.testFalsePositiveRate);
			ps.setDouble(40, m.testCorrectRate);
			ps.setDouble(41, m.testKappa);
			ps.setDouble(42, m.testMeanAbsoluteError);
			ps.setDouble(43, m.testRootMeanSquaredError);
			ps.setDouble(44, m.testRelativeAbsoluteError);
			ps.setDouble(45, m.testRootRelativeSquaredError);
			ps.setDouble(46, m.testROCArea);
			ps.setArray(47, testBucketPercentCorrectArray);
			ps.setArray(48, testBucketDistribution);
			ps.setArray(49, testBucketPValues);
			ps.setString(50, m.notes);
			ps.setBoolean(51, m.favorite);
			ps.setBoolean(52, m.tradeOffPrimary);
			ps.setBoolean(53, m.tradeOffOpposite);
			ps.setTimestamp(54, new Timestamp(m.baseDate.getTime().getTime()));
			
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int id = -1;
			if (rs.next()) {
				id = rs.getInt(1);
				m.id = id;
				m.modelFile = id + ".model";
			}
			rs.close();
			ps.close();
			c.close();
			
			return id;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static void updateModelFileByID(int modelID, String modelFile) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE models SET modelfile = ? WHERE id = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, modelFile);
			ps.setInt(2, modelID);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateModelFavorite(int modelID, boolean favorite) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE models SET favorite = ? WHERE id = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setBoolean(1, favorite);
			ps.setInt(2, modelID);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateModelTradeOffPrimary(int modelID, boolean tradeOffPrimary) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE models SET tradeoffprimary = ? WHERE id = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setBoolean(1, tradeOffPrimary);
			ps.setInt(2, modelID);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateModelTradeOffOpposite(int modelID, boolean tradeOffOpposite) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE models SET tradeoffopposite = ? WHERE id = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setBoolean(1, tradeOffOpposite);
			ps.setInt(2, modelID);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static int getNextModelID() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT last_value FROM models2_id_seq";
			PreparedStatement ps = c.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			int id = -1;
			if (rs.next()) {
				id =  rs.getInt(1) + 1;
			}
			rs.close();
			ps.close();
			c.close();
			return id;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static Bar getMostRecentBar(BarKey bk, Calendar cBefore) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM bar WHERE symbol = ? AND duration = ? AND start < ? ORDER BY start DESC LIMIT 1";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setString(1, bk.symbol);
			ps.setString(2, bk.duration.toString());
			ps.setTimestamp(3, new Timestamp(cBefore.getTimeInMillis()));
			ResultSet rs = ps.executeQuery();
			Bar b = null;
			if (rs.next()) {
				String symbol = rs.getString("symbol");
				float open = rs.getFloat("open");
				float close = rs.getFloat("close");
				float high = rs.getFloat("high");
				float low = rs.getFloat("low");
				float vwap = rs.getFloat("vwap");
				float volume = rs.getFloat("volume");
				int numTrades = rs.getInt("numtrades");
				float change = rs.getFloat("change");
				float gap = rs.getFloat("gap");
				Timestamp tsStart = rs.getTimestamp("start");
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(tsStart.getTime());
				Timestamp tsEnd = rs.getTimestamp("end");
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(tsEnd.getTime());
				String duration = rs.getString("duration");
				b  = new Bar(symbol, open, close, high, low, vwap, volume, numTrades, change, gap, start, end, BAR_SIZE.valueOf(duration), true);
			}
			rs.close();
			ps.close();
			c.close();
			
			return b;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static HashMap<String, Object> getMetricCalcEssentials(MetricKey mk) {
		HashMap<String, Object> mce = new HashMap<String, Object>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();

			String q = "SELECT * FROM metriccalcessentials WHERE name = ? AND symbol = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, mk.name);
			s.setString(2, mk.symbol);
			s.setString(3, mk.duration.toString());
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				String varName = rs.getString("varname");
				Array varValue = rs.getArray("varvalue");
				Timestamp startTS = rs.getTimestamp("start");
				Calendar start = Calendar.getInstance();
				start.setTimeInMillis(startTS.getTime());
				
				Float[] values = (Float[])varValue.getArray();
				if (values.length == 1) {
					mce.put(varName, values[0]);
				}
				else {
					mce.put(varName, values);
				}
				mce.put("start", start);
			}
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (mce.size() == 0) {
			return null;
		}
		else {
			return mce;
		}
	}
	
	public static ArrayList<HashMap<String, Object>> getTickData(String symbol, Calendar periodStart, BAR_SIZE barSize) {
		ArrayList<HashMap<String, Object>> listOfRecords = new ArrayList<HashMap<String, Object>>();
		try {
			Calendar periodEnd = Calendar.getInstance();
			periodEnd.setTime(periodStart.getTime());
			switch (barSize) {
				case BAR_1M:
					periodEnd.add(Calendar.MINUTE, 1);
					break;
				case BAR_3M:
					periodEnd.add(Calendar.MINUTE, 3);
					break;
				case BAR_5M:
					periodEnd.add(Calendar.MINUTE, 5);
					break;
				case BAR_10M:
					periodEnd.add(Calendar.MINUTE, 10);
					break;
				case BAR_15M:
					periodEnd.add(Calendar.MINUTE, 15);
					break;
				case BAR_30M:
					periodEnd.add(Calendar.MINUTE, 30);
					break;
				case BAR_1H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case BAR_2H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 2);
					break;
				case BAR_4H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 4);
					break;
				case BAR_6H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 6);
					break;
				case BAR_8H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 8);
					break;
				case BAR_12H:
					periodEnd.add(Calendar.HOUR_OF_DAY, 12);
					break;
				case BAR_1D:
					periodEnd.add(Calendar.DATE, 1);
					break;
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM tick " + 
						"WHERE symbol = ? AND \"timestamp\" >= '" + CalendarUtils.getSqlDateTimeString(periodStart) + "' AND \"timestamp\" < '" + CalendarUtils.getSqlDateTimeString(periodEnd) + "' ORDER BY \"timestamp\" ";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> record = new HashMap<String, Object>();
				record.put("price", rs.getFloat("price"));
				record.put("volume", rs.getFloat("volume"));
				record.put("start", periodStart);
				record.put("end", periodEnd);
				listOfRecords.add(record);
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return listOfRecords;
	}
	
	public static float getTradingAccountCash() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT cash FROM tradingaccount";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			float cash = 0f;
			while (rs.next()) {
				cash = rs.getFloat("cash");
			}
			rs.close();
			s.close();
			c.close();
			return cash;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static float getTradingAccountBTC() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT bitcoin FROM tradingaccount";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			float bitcoin = 0f;
			while (rs.next()) {
				bitcoin = rs.getFloat("bitcoin");
			}
			rs.close();
			s.close();
			c.close();
			return bitcoin;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static void insertRecordIntoPaperLoose(float price) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO paperloose VALUES ((SELECT cash + (bitcoin * ?) FROM tradingaccount LIMIT 1), (SELECT bitcoin + (cash / ?) FROM tradingaccount LIMIT 1), ?, now())";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, price);
			ps.setFloat(2, price);
			ps.setFloat(3, price);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateTradingAccount(float changeInCash, float changeInBTC) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE tradingaccount SET cash = cash + ?, bitcoin = bitcoin + ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, changeInCash);
			ps.setFloat(2, changeInBTC);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method should get the volatility relative to an index of some sort like SPY'
	 * 
	 * @param symbol
	 * @return
	 */
	public static float getSymbolRelativeVolatility(String symbol) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q =  "SELECT 1 AS relvol ";
			PreparedStatement s = c.prepareStatement(q);
			ResultSet rs = s.executeQuery();
			float relvol = 1f; 
			while (rs.next()) {
				relvol = rs.getFloat("relvol");
			}
			rs.close();
			s.close();
			c.close();
			
			// If the stock hasn't been listed for 100 days, use 1/3 standard position size
			if (relvol <= 0) { 
				relvol = 3;
			}
			
			return relvol;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 1f;
		}
	}
	
	public static float getTradingAccountValue() {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = 	"SELECT " +
						"COALESCE((SELECT SUM(b.close * t.filledamount) " +
						"FROM trades t " +
						"INNER JOIN bar b ON t.symbol = b.symbol AND b.start = (SELECT MAX(start) FROM bar WHERE symbol = t.symbol AND duration = t.duration) " +
						"WHERE t.type = 'long' AND (t.status = 'Open Pending' OR t.status = 'Open Partially Filled' OR t.status = 'Open Filled')), 0) " +
						"+  " +
						"COALESCE((SELECT SUM(b.close * t.filledamount) " +
						"FROM trades t " +
						"INNER JOIN bar b ON t.symbol = b.symbol AND b.start = (SELECT MAX(start) FROM bar WHERE symbol = t.symbol AND duration = t.duration) " +
						"WHERE t.type = 'short' AND (t.status = 'Open Pending' OR t.status = 'Open Partially Filled' OR t.status = 'Open Filled')), 0) " +
						"+ " + 
						"(SELECT cash FROM tradingaccount) AS value";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			float value = 0f;
			while (rs.next()) {
				value = rs.getFloat("value");
			}
			rs.close();
			s.close();
			c.close();
			return value;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static void saveAccountHistoryValue(float accountValue) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO tradingaccounthistory(date, \"value\") VALUES (now(), ?)";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, accountValue);
			ps.executeUpdate();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static float updateTradingAccountCash(float cash) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE tradingaccount SET cash = ?";
			PreparedStatement ps = c.prepareStatement(q);
			ps.setFloat(1, cash);
			ps.executeUpdate();
			ps.close();
			c.close();
			return cash;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0f;
		}
	}
	
	public static ArrayList<HashMap<String, Object>> getOpenPositionsPossiblyNeedingCloseMonitoring(String table) {
		ArrayList<HashMap<String, Object>> openPositions = new ArrayList<HashMap<String, Object>>(); 
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT tempid, exchangeopentradeid, exchangeclosetradeid, status, stopstatus, expirationstatus, type, opentradetime, symbol, duration, model, filledamount, closefilledamount, suggestedentryprice, actualentryprice, suggestedexitprice, suggestedstopprice, commission, expiration FROM " + table + " "
					+ "WHERE (status = 'Open Filled' OR status = 'Close Requested' OR status = 'Close Partially Filled' OR status = 'Close Pending' OR (stopstatus = 'Stop Needed' AND status != 'Abandoned') OR (expirationstatus = 'Expiration Needed' AND status != 'Abandoned')) ";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				HashMap<String, Object> openPosition = new HashMap<String, Object>();
				openPosition.put("tempid", rs.getInt("tempid"));
				openPosition.put("exchangeopentradeid", rs.getLong("exchangeopentradeid"));
				openPosition.put("exchangeclosetradeid", rs.getLong("exchangeclosetradeid"));
				openPosition.put("status", rs.getString("status"));
				openPosition.put("stopstatus", rs.getString("stopstatus"));
				openPosition.put("expirationstatus", rs.getString("expirationstatus"));
				openPosition.put("type", rs.getString("type"));
				openPosition.put("opentradetime", rs.getTimestamp("opentradetime"));
				openPosition.put("symbol", rs.getString("symbol"));
				openPosition.put("duration", rs.getString("duration"));
				openPosition.put("model", rs.getString("model"));
				openPosition.put("filledamount", rs.getFloat("filledamount"));
				openPosition.put("closefilledamount", rs.getFloat("closefilledamount"));
				openPosition.put("suggestedentryprice", rs.getFloat("suggestedentryprice"));
				openPosition.put("actualentryprice", rs.getFloat("actualentryprice"));
				openPosition.put("suggestedexitprice", rs.getFloat("suggestedexitprice"));
				openPosition.put("suggestedstopprice", rs.getFloat("suggestedstopprice"));
				openPosition.put("commission", rs.getFloat("commission"));
				openPosition.put("expiration", rs.getTimestamp("expiration"));
				
				openPositions.add(openPosition);
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return openPositions;
	}
	
	public static void closePosition(String table, int tempID, String exitReason, float exitPrice, float totalCommission, float netProfit, float grossProfit) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE " + table + " " +
						"SET status = 'Closed', statustime = now(), closetradetime = now(), actualexitprice = ?, exitreason = ?, commission = ?, netprofit = ?, grossprofit = ? " +
						"WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setFloat(1, exitPrice);
			s.setString(2, exitReason);
			s.setFloat(3, totalCommission);
			s.setFloat(4, netProfit);
			s.setFloat(5, grossProfit);
			s.setInt(6, tempID);
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void abandonTooSmallPosition(int tempID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = 'Abandoned' WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setInt(1, tempID);
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * @param suggestedEntry
	 * @param requestedAmount
	 * @param commission
	 * @param model
	 * @return
	 */
	public static void makeTradeRequest(String table, String status, String direction, Float suggestedEntry, Float actualEntry, Float suggestedExitPrice, Float suggestedStopPrice, float requestedAmount, float commission, String symbol, String duration, String modelFile, Calendar expiration) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO " + table + "(exchangeopentradeid, exchangeclosetradeid, exchangestoptradeid, exchangeexpirationtradeid, status, statustime, "
					+ "opentradetime, closetradetime, stoptradetime, expirationtradetime, "
					+ "\"type\", symbol, duration, requestedamount, filledamount, suggestedentryprice, actualentryprice, suggestedexitprice, suggestedstopprice, actualexitprice, "
					+ "exitreason, closefilledamount, commission, netprofit, grossprofit, model, expiration) " +
						"VALUES (?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setNull(1, java.sql.Types.INTEGER);
			s.setNull(2, java.sql.Types.INTEGER);
			s.setNull(3, java.sql.Types.INTEGER);
			s.setNull(4, java.sql.Types.INTEGER);
			
			s.setString(5, status);

			s.setTimestamp(6, null); // opentradetime
			s.setTimestamp(7, null); // closetradetime
			s.setTimestamp(8, null); // stoptradetime
			s.setTimestamp(9, null); // expirationtradetime

			s.setString(10, direction);
			s.setString(11, symbol);
			s.setString(12, duration);
			
			// Requested Amount
			s.setFloat(13, requestedAmount); 
			
			// Filled Amount
			if (status.equals("Close Requested")) {
				s.setFloat(14, requestedAmount); // For Paper Trading make the Filled Amount equal to the Requested Amount
			}
			else {
				s.setNull(14, java.sql.Types.FLOAT); 
			}
			
			// Suggested Entry
			if (suggestedEntry == null) {
				s.setNull(15, java.sql.Types.FLOAT);
			}
			else {
				s.setFloat(15, suggestedEntry);
			}
			
			// Actual Entry
			if (actualEntry == null) {
				s.setNull(16, java.sql.Types.FLOAT);
			}
			else {
				s.setFloat(16, actualEntry);
			}
			
			// Suggested Exit
			if (suggestedExitPrice == null) {
				s.setNull(17, java.sql.Types.FLOAT);
			}
			else {
				s.setFloat(17, suggestedExitPrice); 
			}
			
			// Suggested Stop
			if (suggestedStopPrice == null) {
				s.setNull(18, java.sql.Types.FLOAT);
			}
			else {
				s.setFloat(18, suggestedStopPrice); 
			}
			
			s.setNull(19, java.sql.Types.FLOAT); // actualexitprice
			s.setString(20, null); // exitreason
			s.setNull(21, java.sql.Types.FLOAT); // closefilledamount
			s.setFloat(22, commission); // commission
			s.setNull(23, java.sql.Types.FLOAT); // netprofit
			s.setNull(24, java.sql.Types.FLOAT); // grossprofit
			s.setString(25, modelFile); // model
			s.setTimestamp(26, new java.sql.Timestamp(expiration.getTime().getTime())); // expiration
			
			s.executeUpdate();

			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void makeCloseTradeRequest(int tempid) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = 'Close Requested', statustime = now() WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempid);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void makeStopTradeRequest(long exchangeOpenTradeID, String requestType) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET stopstatus = ?, stopstatustime = now() WHERE exchangeOpenTradeID = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, requestType);
			s.setLong(2, exchangeOpenTradeID);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void makeExpirationTradeRequest(long exchangeOpenTradeID, String requestType) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET expirationstatus = ?, expirationstatustime = now() WHERE exchangeOpenTradeID = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, requestType);
			s.setLong(2, exchangeOpenTradeID);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelOpenTradeRequest(int tempid) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = 'Cancelled', statustime = now() WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempid);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelCloseTradeRequest(int tempid) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = 'Open Filled', statustime = now() WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempid);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelStopTradeRequest(int tempid) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET stopstatus = null WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempid);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelExpirationTradeRequest(int tempid) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET expirationstatus = null WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempid);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object[] getNextRequestedTrade() {
		Object[] data = new Object[2];
		data[0] = null;
		data[1] = null;
		try {
			String q1 = "SELECT tempid, status, stopstatus, expirationstatus, statustime, stopstatustime, expirationstatustime FROM trades " +
						"WHERE status LIKE '%Requested' " +
						"ORDER BY statustime LIMIT 1";
			
			String q2 = "SELECT tempid, status, stopstatus, expirationstatus, statustime, stopstatustime, expirationstatustime FROM trades " +
						"WHERE stopstatus LIKE '%Requested' AND status != 'Closed' AND status != 'Cancelled' AND status != 'Abandoned' " +
						"ORDER BY stopstatustime LIMIT 1";
			
			String q3 = "SELECT tempid, status, stopstatus, expirationstatus, statustime, stopstatustime, expirationstatustime FROM trades " +
						"WHERE expirationstatus LIKE '%Requested' AND status != 'Closed' AND status != 'Cancelled' AND status != 'Abandoned' " +
						"ORDER BY expirationstatustime LIMIT 1";
			
			Timestamp firstStatusTime = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
			
			Connection c1 = ConnectionSingleton.getInstance().getConnection();
			PreparedStatement s1 = c1.prepareStatement(q1);
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				int tempID = rs1.getInt("tempid");
				String status = rs1.getString("status");
				Timestamp statusTime = rs1.getTimestamp("statustime");

				if (statusTime.getTime() < firstStatusTime.getTime()) {
					firstStatusTime.setTime(statusTime.getTime());
					data[0] = tempID;
					data[1] = status;
				}
			}
			rs1.close();
			s1.close();
			c1.close();
			
			Connection c2 = ConnectionSingleton.getInstance().getConnection();
			PreparedStatement s2 = c2.prepareStatement(q2);
			ResultSet rs2 = s2.executeQuery();
			while (rs2.next()) {
				int tempID = rs2.getInt("tempid");
				String stopStatus = rs2.getString("stopstatus");
				Timestamp stopStatusTime = rs2.getTimestamp("stopstatustime");

				if (stopStatusTime.getTime() < firstStatusTime.getTime()) {
					firstStatusTime.setTime(stopStatusTime.getTime());
					data[0] = tempID;
					data[1] = stopStatus;
				}
			}
			rs2.close();
			s2.close();
			c2.close();
			
			Connection c3 = ConnectionSingleton.getInstance().getConnection();
			PreparedStatement s3 = c3.prepareStatement(q3);
			ResultSet rs3 = s3.executeQuery();
			while (rs3.next()) {
				int tempID = rs3.getInt("tempid");
				String expirationStatus = rs3.getString("expirationstatus");
				Timestamp expirationStatusTime = rs3.getTimestamp("expirationstatustime");

				if (expirationStatusTime.getTime() < firstStatusTime.getTime()) {
					firstStatusTime.setTime(expirationStatusTime.getTime());
					data[0] = tempID;
					data[1] = expirationStatus;
				}
			}
			rs3.close();
			s3.close();
			c3.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static int deleteAllRequestedOrders() {
		int numRowsAffected = 0;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "DELETE FROM trades WHERE status LIKE '%Requested' OR stopstatus LIKE '%Requested' OR expirationstatus LIKE '%Requested'";
			PreparedStatement s = c.prepareStatement(q);
			
			numRowsAffected = s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return numRowsAffected;
	}
	
	public static void updateMostRecentOpenTradeWithExchangeData(int tempID, long exchangeOpenTradeID, long timestamp, double price, double filledAmount, String status) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			if (filledAmount > 0) {
				String q = "UPDATE TRADES SET exchangeopentradeid = ?, opentradetime = ?, actualentryprice = ?, filledamount = ?, status = ?, statustime = now() WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeOpenTradeID);
				s.setTimestamp(2, new Timestamp(timestamp));
				s.setDouble(3, price);
				s.setDouble(4, filledAmount);
				s.setString(5, status);
				s.setInt(6, tempID);
				
				s.executeUpdate();
				s.close();
			}
			else {
				String q = "UPDATE TRADES SET exchangeopentradeid = ?, status = ?, statustime = now() WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeOpenTradeID);
				s.setString(2, status);
				s.setInt(3, tempID);
				
				s.executeUpdate();
				s.close();
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateMostRecentCloseTradeWithExchangeData(int tempID, long exchangeCloseTradeID, long timestamp, double price, double closeFilledAmount, String status) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			if (closeFilledAmount > 0) { // Close Partially Filled or Close Filled
				String q = "UPDATE TRADES SET exchangeclosetradeid = ?, closetradetime = ?, " +
						"actualexitprice = (COALESCE(actualexitprice, 0) * (COALESCE(closefilledamount, 0) / ?)) + (? * ((? - COALESCE(closefilledamount, 0)) / ?)), " +
						"closefilledamount = ?, status = ?, statustime = now(), exitreason = 'Target Hit' WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeCloseTradeID);
				s.setTimestamp(2, new Timestamp(timestamp));	
				s.setDouble(3, closeFilledAmount);
				s.setDouble(4, price);
				s.setDouble(5, closeFilledAmount);
				s.setDouble(6, closeFilledAmount);
				s.setDouble(7, closeFilledAmount);
				s.setString(8, status);
				s.setInt(9, tempID);
				
				s.executeUpdate();
				s.close();
			}
			else { // Close Pending
				String q = "UPDATE TRADES SET exchangeclosetradeid = ?, status = ?, statustime = now() WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeCloseTradeID);
				s.setString(2, status);
				s.setInt(3, tempID);
				
				s.executeUpdate();
				s.close();
			}
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void recordTradeProfit(int tempID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "UPDATE trades " + 
						"SET grossprofit = CASE " + 
						"WHEN type = 'bull' THEN ((actualexitprice - actualentryprice) * closefilledamount) " +
						"WHEN type = 'bear' THEN ((actualentryprice - actualexitprice) * closefilledamount) " +
						"END, " +
						"netprofit = CASE " +
						"WHEN type = 'bull' THEN ((actualexitprice - actualentryprice) * closefilledamount) " +
						"WHEN type = 'bear' THEN ((actualentryprice - actualexitprice) * closefilledamount) " +
						"END, " +
						"commission = 0 " +
						"WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setInt(1, tempID);
			
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateMostRecentStopTradeWithExchangeData(int tempID, long exchangeStopTradeID, long timestamp, double price, double closeFilledAmount, String status, String stopStatus) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			if (stopStatus.equals("Closed")) { // Stop Filled
				String q = "UPDATE TRADES SET exchangestoptradeid = ?, stoptradetime = ?, " +
						"actualexitprice = (COALESCE(actualexitprice, 0) * (COALESCE(closefilledamount, 0) / ?)) + (? * ((? - COALESCE(closefilledamount, 0)) / ?)), " +
						"closefilledamount = ?, status = ?, statustime = now(), stopstatus = ?, stopstatustime = now(), exitreason = 'Stop Hit' WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeStopTradeID);
				s.setTimestamp(2, new Timestamp(timestamp));
				s.setDouble(3, closeFilledAmount);
				s.setDouble(4, price);
				s.setDouble(5, closeFilledAmount);
				s.setDouble(6, closeFilledAmount);
				s.setDouble(7, closeFilledAmount);
				s.setString(8, status);
				s.setString(9, stopStatus);
				s.setInt(10, tempID);
				
				s.executeUpdate();
				s.close();
			}
			else if (stopStatus.equals("Stop Partially Filled")) { // Stop Partially Filled
				String q = "UPDATE TRADES SET exchangestoptradeid = ?, stoptradetime = ?, " +
						"actualexitprice = (COALESCE(actualexitprice, 0) * (COALESCE(closefilledamount, 0) / ?)) + (? * ((? - COALESCE(closefilledamount, 0)) / ?)), " +
						"closefilledamount = ?, stopstatus = ?, stopstatustime = now(), exitreason = 'Stop Hit' WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeStopTradeID);
				s.setTimestamp(2, new Timestamp(timestamp));
				s.setDouble(3, closeFilledAmount);
				s.setDouble(4, price);
				s.setDouble(5, closeFilledAmount);
				s.setDouble(6, closeFilledAmount);
				s.setDouble(7, closeFilledAmount);
				s.setString(8, stopStatus);
				s.setInt(9, tempID);
				
				s.executeUpdate();
				s.close();
			}
			else if (stopStatus.equals("Stop Pending")) { // Stop Pending
				String q = "UPDATE TRADES SET exchangestoptradeid = ?, stopstatus = ?, stopstatustime = now() WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeStopTradeID);
				s.setString(2, stopStatus);
				s.setInt(3, tempID);
				
				s.executeUpdate();
				s.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void updateMostRecentExpirationTradeWithExchangeData(int tempID, long exchangeExpirationTradeID, long timestamp, double price, double closeFilledAmount, String status, String expirationStatus) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			if (expirationStatus.equals("Closed")) { // Expiration Filled
				String q = "UPDATE TRADES SET exchangeexpirationtradeid = ?, expirationtradetime = ?, " +
						"actualexitprice = (COALESCE(actualexitprice, 0) * (COALESCE(closefilledamount, 0) / ?)) + (? * ((? - COALESCE(closefilledamount, 0)) / ?)), " +
						"closefilledamount = ?, status = ?, statustime = now(), expirationstatus = ?, expirationstatustime = now(), exitreason = 'Expiration' WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeExpirationTradeID);
				s.setTimestamp(2, new Timestamp(timestamp));
				s.setDouble(3, closeFilledAmount);
				s.setDouble(4, price);
				s.setDouble(5, closeFilledAmount);
				s.setDouble(6, closeFilledAmount);
				s.setDouble(7, closeFilledAmount);
				s.setString(8, status);
				s.setString(9, expirationStatus);
				s.setInt(10, tempID);
				
				s.executeUpdate();
				s.close();
			}
			else if (expirationStatus.equals("Expiration Partially Filled")) { // Expiration Partially Filled
				String q = "UPDATE TRADES SET exchangeexpirationtradeid = ?, expirationtradetime = ?, " +
						"actualexitprice = (COALESCE(actualexitprice, 0) * (COALESCE(closefilledamount, 0) / ?)) + (? * ((? - COALESCE(closefilledamount, 0)) / ?)), " +
						"closefilledamount = ?, expirationstatus = ?, expirationstatustime = now(), exitreason = 'Expiration' WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeExpirationTradeID);
				s.setTimestamp(2, new Timestamp(timestamp));
				s.setDouble(3, closeFilledAmount);
				s.setDouble(4, price);
				s.setDouble(5, closeFilledAmount);
				s.setDouble(6, closeFilledAmount);
				s.setDouble(7, closeFilledAmount);
				s.setString(8, expirationStatus);
				s.setInt(9, tempID);
				
				s.executeUpdate();
				s.close();
			}
			else if (expirationStatus.equals("Expiration Pending")) { // Expiration Pending
				String q = "UPDATE TRADES SET exchangeexpirationtradeid = ?, expirationstatus = ?, expirationstatustime = now() WHERE tempid = ?";
				PreparedStatement s = c.prepareStatement(q);
				
				s.setLong(1, exchangeExpirationTradeID);
				s.setString(2, expirationStatus);
				s.setInt(3, tempID);
				
				s.executeUpdate();
				s.close();
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Long> getPendingOrPartiallyFilledStaleOpenOrderExchangeOpenTradeIDs(int staleTradeSec) {
		ArrayList<Long> partiallyFilledExchangeIDs = new ArrayList<Long>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT exchangeopentradeid FROM trades WHERE (status = 'Open Partially Filled' OR status = 'Open Pending') AND AGE(now(), statustime) > '00:00:" + staleTradeSec + "' ORDER BY statustime";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				partiallyFilledExchangeIDs.add(rs.getLong("exchangeopentradeid"));
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return partiallyFilledExchangeIDs;
	}
	
	public static ArrayList<Long> getClosePartiallyFilledOrderExchangeCloseTradeIDs(int staleTradeSec) {
		ArrayList<Long> partiallyClosedExchangeIDs = new ArrayList<Long>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT exchangeclosetradeid FROM trades WHERE status = 'Close Partially Filled' AND AGE(now(), statustime) > '00:00:" + staleTradeSec + "' ORDER BY statustime";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				partiallyClosedExchangeIDs.add(rs.getLong("exchangeclosetradeid"));
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return partiallyClosedExchangeIDs;
	}
	
	public static ArrayList<Long> getStaleStopOrders(int staleTradeSec) {
		ArrayList<Long> staleStopExchangeIDs = new ArrayList<Long>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT exchangestoptradeid FROM trades WHERE (stopstatus = 'Stop Partially Filled' OR stopstatus = 'Stop Pending') AND AGE(now(), stopstatustime) > '00:00:" + staleTradeSec + "' AND status != 'Abandoned' AND status != 'Closed' AND status != 'Cancelled' ORDER BY stopstatustime";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				staleStopExchangeIDs.add(rs.getLong("exchangestoptradeid"));
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return staleStopExchangeIDs;
	}
	
	public static ArrayList<Long> getStaleExpirationOrders(int staleTradeSec) {
		ArrayList<Long> staleExpirationExchangeIDs = new ArrayList<Long>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT exchangeexpirationtradeid FROM trades WHERE (expirationstatus = 'Expiration Partially Filled' OR expirationstatus = 'Expiration Pending') AND AGE(now(), expirationstatustime) > '00:00:" + staleTradeSec + "' AND status != 'Abandoned' AND status != 'Closed' AND status != 'Cancelled' ORDER BY expirationstatustime";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				staleExpirationExchangeIDs.add(rs.getLong("exchangeexpirationtradeid"));
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return staleExpirationExchangeIDs;
	}
	
	public static ArrayList<HashMap<String, Object>> getFilledTradesThatNeedCloseOrdersPlaced() {
		ArrayList<HashMap<String, Object>> tradesNeedingExitOrders = new ArrayList<HashMap<String, Object>>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT * FROM trades WHERE exchangeclosetradeid IS NULL AND status = 'Open Filled' AND stopstatus IS NULL AND stopstatustime IS NULL AND expirationstatus IS NULL AND expirationstatustime IS NULL ORDER BY tempid";
			PreparedStatement s = c.prepareStatement(q);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> tradeHash = new HashMap<String, Object>();
				tradeHash.put("tempid", rs.getInt("tempid"));
				tradeHash.put("exchangeopentradeid", rs.getLong("exchangeopentradeid"));
				tradeHash.put("type", rs.getString("type"));
				tradeHash.put("suggestedexitprice", (rs.getDouble("suggestedexitprice")));
				tradeHash.put("filledamount", (rs.getDouble("filledamount")));
				tradeHash.put("closefilledamount", rs.getDouble("closefilledamount"));
				tradeHash.put("model", rs.getString("model"));
				tradeHash.put("symbol", rs.getString("symbol"));
				tradeHash.put("duration", rs.getString("duration"));
				tradesNeedingExitOrders.add(tradeHash);
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tradesNeedingExitOrders;
	}
	
	public static HashMap<String, Object> figureOutExchangeIdTradeType(long exchangeID) {
		HashMap<String, Object> results = new HashMap<String, Object>();
		String exchangeIDTradeType = "Unknown";
		Integer tempID = null;
		try {
			// Check for Open
			if (exchangeIDTradeType.equals("Unknown")) {
				Connection c = ConnectionSingleton.getInstance().getConnection();
				String q = "SELECT tempid FROM trades WHERE exchangeopentradeid = ?";
				PreparedStatement s = c.prepareStatement(q);
				s.setLong(1, exchangeID);
				
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					tempID = rs.getInt("tempid");
					exchangeIDTradeType = "Open";
				}
				
				rs.close();
				s.close();
				c.close();
			}
			
			// Check for Close
			if (exchangeIDTradeType.equals("Unknown")) {
				Connection c = ConnectionSingleton.getInstance().getConnection();
				String q = "SELECT tempid FROM trades WHERE exchangeclosetradeid = ?";
				PreparedStatement s = c.prepareStatement(q);
				s.setLong(1, exchangeID);
				
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					tempID = rs.getInt("tempid");
					exchangeIDTradeType = "Close";
				}
				
				rs.close();
				s.close();
				c.close();
			}
			
			// Check for Stop
			if (exchangeIDTradeType.equals("Unknown")) {
				Connection c = ConnectionSingleton.getInstance().getConnection();
				String q = "SELECT tempid FROM trades WHERE exchangestoptradeid = ?";
				PreparedStatement s = c.prepareStatement(q);
				s.setLong(1, exchangeID);
				
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					tempID = rs.getInt("tempid");
					exchangeIDTradeType = "Stop";
				}
				
				rs.close();
				s.close();
				c.close();
			}
			
			// Check for Expiration
			if (exchangeIDTradeType.equals("Unknown")) {
				Connection c = ConnectionSingleton.getInstance().getConnection();
				String q = "SELECT tempid FROM trades WHERE exchangeexpirationtradeid = ?";
				PreparedStatement s = c.prepareStatement(q);
				s.setLong(1, exchangeID);
				
				ResultSet rs = s.executeQuery();
				while (rs.next()) {
					tempID = rs.getInt("tempid");
					exchangeIDTradeType = "Expiration";
				}
				
				rs.close();
				s.close();
				c.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (!exchangeIDTradeType.equals("Unknown")) {
			results.put("type", exchangeIDTradeType);
			results.put("tempid", tempID);
		}
		return results;
	}
	
	public static void cancelOpenOrder(int tempID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = CASE WHEN filledamount > .01 THEN 'Open Filled' WHEN filledamount <= .01 AND filledamount > 0 THEN 'Abandoned' ELSE 'Cancelled' END, statustime = now() WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempID);	
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelCloseOrder(int tempID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = CASE WHEN COALESCE(closefilledamount, 0) < filledamount THEN 'Open Filled' ELSE 'Cancelled' END, statustime = now(), "
					+ "exchangeclosetradeid = CASE WHEN COALESCE(closefilledamount, 0) < filledamount THEN null ELSE exchangeclosetradeid END WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempID);
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelStopOrder(int tempID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET stopstatus = CASE WHEN COALESCE(closefilledamount, 0) < filledamount THEN null ELSE 'Cancelled' END, stopstatustime = now(), "
					+ "exchangestoptradeid = CASE WHEN COALESCE(closefilledamount, 0) < filledamount THEN null ELSE exchangestoptradeid END WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempID);
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cancelExpirationOrder(int tempID) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET expirationstatus = CASE WHEN COALESCE(closefilledamount, 0) < filledamount THEN null ELSE 'Cancelled' END, expirationstatustime = now(), "
					+ "exchangeexpirationtradeid = CASE WHEN COALESCE(closefilledamount, 0) < filledamount THEN null ELSE exchangeexpirationtradeid END WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setInt(1, tempID);
			s.executeUpdate();
			
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Long> getExchangeOrders(long exchangeid, int tempid) {
		ArrayList<Long> exchangeIDs = new ArrayList<Long>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			// Only need to get the close, stop, & expiration orders because this is only called from an order that has been fullly closed normally.
			String q = "SELECT exchangeclosetradeid, exchangestoptradeid, exchangeexpirationtradeid FROM trades WHERE tempid = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setInt(1, tempid);
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				HashMap<String, Object> tradeHash = new HashMap<String, Object>();
//				long exchangeOpenTradeID = rs.getLong("exchangeopentradeid");
				long exchangeCloseTradeID = rs.getLong("exchangeclosetradeid");
				long exchangeStopTradeID = rs.getLong("exchangestoptradeid");
				long exchangeExpirationTradeID = rs.getLong("exchangeexpirationtradeid");

//				if (exchangeOpenTradeID != null) {
//					exchangeIDs.add(exchangeOpenTradeID);
//				}
				if (exchangeCloseTradeID != exchangeid && exchangeCloseTradeID != 0) {
					exchangeIDs.add(exchangeCloseTradeID);
				}
				if (exchangeStopTradeID != exchangeid && exchangeStopTradeID != 0) {
					exchangeIDs.add(exchangeStopTradeID);
				}
				if (exchangeExpirationTradeID != exchangeid && exchangeExpirationTradeID != 0) {
					exchangeIDs.add(exchangeExpirationTradeID);
				}
			}
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return exchangeIDs;
	}
	
	public static ArrayList<Integer> cancelStuckOpenRequestedTempIDs(int staleTradeSec) {
		ArrayList<Integer> tempIDs = new ArrayList<Integer>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades SET status = 'Cancelled', statustime = now() WHERE status = 'Open Requested' AND AGE(now(), opentradetime) > '00:00:" + staleTradeSec + "'";
			PreparedStatement s = c.prepareStatement(q);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tempIDs;
	}
	
	public static void makeCancelRequest(ArrayList<Long> pendingOrPartiallyFilledStuckOrderExchangeIDs) {
		try {
			if (pendingOrPartiallyFilledStuckOrderExchangeIDs.size() == 0) {
				return;
			}
			
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String whereStatement = " WHERE exchangeopentradeid IN (";
			for (long id : pendingOrPartiallyFilledStuckOrderExchangeIDs) {
				whereStatement += (id + ", ");
			}
			whereStatement = whereStatement.substring(0, whereStatement.length() - 2);
			whereStatement += ")";
			
			String q = "UPDATE trades SET status = 'Cancel Requested', statustime = now() " + whereStatement;
			PreparedStatement s = c.prepareStatement(q);

			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void insertModelInstances(int modelID, ArrayList<Double> predictionScores, ArrayList<Boolean> predictionResults) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = "INSERT INTO modelinstances(modelid, score, correct) VALUES (?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q);

			for (int a = 0; a < predictionScores.size(); a++) {
				s.setInt(1, modelID);
				s.setBigDecimal(2, new BigDecimal(IBQueryManager.df5.format(predictionScores.get(a))).setScale(5)); 
				s.setBoolean(3, predictionResults.get(a));
				s.addBatch();
			}
			
			s.executeBatch();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, Object> getModelDataFromScore(int modelID, double modelScore) {
		HashMap<String, Object> modelData = new HashMap<String, Object>();
		modelData.put("PercentCorrect", 0d);
		modelData.put("InstanceCount", 0);
		try {
			// First check to see how much +/- we can look around the score.  This is the margin.
			Connection c1 = ConnectionSingleton.getInstance().getConnection();
			String q1 = "SELECT MAX(score) - MIN(score) FROM modelinstances WHERE modelid = ?";
			PreparedStatement s1 = c1.prepareStatement(q1);
			s1.setInt(1, modelID);
			
			double range = .5d;
			ResultSet rs1 = s1.executeQuery();
			while (rs1.next()) {
				range = rs1.getDouble(1);
			}
			double margin = range / 15d;
			
			rs1.close();
			s1.close();
			c1.close();
			
			// Second, get the data for within that margin
			Connection c2 = ConnectionSingleton.getInstance().getConnection();
			
			double lowerBounds = .5d;
			double upperBounds = .5d;
			double distanceFromMid = Math.abs(modelScore - .5);
			if (distanceFromMid >= margin) {
				lowerBounds = modelScore - margin;
				upperBounds = modelScore + margin;
			}
			else {
				lowerBounds = modelScore - distanceFromMid; // Making one of these .5
				upperBounds = modelScore + distanceFromMid;
				
				if (lowerBounds == .5d) {
					if (upperBounds < .53d) {
						upperBounds = .53d;
					}
				}
				else if (upperBounds == .5d) {
					if (lowerBounds < .47d) {
						lowerBounds = .47d;
					}
				}
			}
			
			String q2 = "SELECT correct, COUNT(*) AS c FROM modelinstances WHERE modelid = ? AND score >= ? AND score <= ? GROUP BY correct";
			PreparedStatement s2 = c2.prepareStatement(q2);
			s2.setInt(1, modelID);
			s2.setBigDecimal(2, new BigDecimal(lowerBounds));
			s2.setBigDecimal(3, new BigDecimal(upperBounds));

			int numCorrect = 0;
			int numIncorrect = 0;
			
			ResultSet rs2 = s2.executeQuery();
			while (rs2.next()) {
				boolean correct = rs2.getBoolean("correct");
				int num = rs2.getInt("c");
				if (correct) {
					numCorrect = num;
				}
				else {
					numIncorrect = num;
				}
			}
			
			double percentCorrect = numCorrect / (double)(numCorrect + numIncorrect);
			if (Double.isNaN(percentCorrect)) {
				percentCorrect = 0;
			}
			modelData.put("PercentCorrect", percentCorrect);
			modelData.put("InstanceCount", numCorrect + numIncorrect);
			
			rs2.close();
			s2.close();
			c2.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return modelData;
	}
	
	public static HashSet<Integer> selectTopModels(Calendar baseDate, Double minSellMetricValue, Double maxSellMetricValue, double minimumAlpha, int limit) {
		HashSet<Integer> modelIds = new HashSet<Integer>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			String q = 
				"SELECT *  FROM ( " +
				"	SELECT t.*, m.sellmetricvalue / m.stopmetricvalue AS ratio, " +
				"	CASE WHEN m.sellmetricvalue / m.stopmetricvalue = .5 THEN t.bullwinpercent - .66666 " +
				"		WHEN m.sellmetricvalue / m.stopmetricvalue = 2 THEN t.bullwinpercent - .33333 " +
				"		WHEN m.sellmetricvalue = m.stopmetricvalue THEN t.bullwinpercent - .5 " +
				"		ELSE NULL " +
				"		END AS bullalpha, " +
				"	CASE WHEN m.sellmetricvalue / m.stopmetricvalue = .5 THEN t.bearwinpercent - .33333 " +
				"		WHEN m.sellmetricvalue / m.stopmetricvalue = 2 THEN t.bearwinpercent - .66666 " +
				"		WHEN m.sellmetricvalue = m.stopmetricvalue THEN t.bearwinpercent - .5 " +
				"		ELSE NULL " +
				"		END AS bearalpha " +
				"	FROM ( " +
				"		SELECT m.id, " +
				"		( " +
				"			CASE WHEN 	 " +
				"				(SELECT COUNT(*) AS c FROM modelinstances " +
				"				WHERE score >= .5 " +
				"				AND modelid = m.id)::numeric > 0 " +
				"			THEN " +
				"				(SELECT COUNT(*) AS c FROM modelinstances " +
				"				WHERE score >= .5 " +
				"				AND modelid = m.id " +
				"				AND correct = true)::numeric " +
				"				/ " +
				"				(SELECT COUNT(*) AS c FROM modelinstances " +
				"				WHERE score >= .5 " +
				"				AND modelid = m.id)::numeric " +
				"			ELSE 0 " +
				"			END " +
				"		) AS bullWinPercent, " +
				"		( " +
				"			CASE WHEN " +
				"				(SELECT COUNT(*) AS c FROM modelinstances " +
				"				WHERE score < .5 " +
				"				AND modelid = m.id)::numeric > 0 " +
				"			THEN  " +
				"				(SELECT COUNT(*) AS c FROM modelinstances " +
				"				WHERE score < .5 " +
				"				AND modelid = m.id " +
				"				AND correct = true)::numeric " +
				"				/ " +
				"				(SELECT COUNT(*) AS c FROM modelinstances " +
				"				WHERE score < .5 " +
				"				AND modelid = m.id)::numeric " +
				"			ELSE 0 " +
				"			END " +
				"		) AS bearWinPercent " +
				"		FROM models m " +
				"		WHERE m.basedate >= ? AND m.basedate <= ? " +
				"	) t " +
				"	INNER JOIN models m ON t.id = m.id " +
				"	AND m.sellmetricvalue = m.stopmetricvalue ";// +
//				"   AND m.algo != 'MultilayerPerceptron' ";
			if (minSellMetricValue != null && maxSellMetricValue != null) {
				q += "	AND m.sellmetricvalue >= ? ";
			}
			if (maxSellMetricValue != null) {
				q += "  AND m.sellmetricvalue <= ? ";
			}
			q +=
				"	) t2 " +
				"WHERE bullalpha > ? AND bearalpha > ? " +
				"ORDER BY bullalpha + bearalpha DESC LIMIT ?";
					
			PreparedStatement s = c.prepareStatement(q);

			Calendar ca = Calendar.getInstance();
			ca.setTimeInMillis(baseDate.getTimeInMillis());
//			ca.add(Calendar.WEEK_OF_YEAR, -2);
			
			s.setTimestamp(1, new java.sql.Timestamp(ca.getTimeInMillis()));
			s.setTimestamp(2, new java.sql.Timestamp(baseDate.getTimeInMillis()));
			if (minSellMetricValue != null && maxSellMetricValue != null) {
				s.setDouble(3, minSellMetricValue);
				s.setDouble(4, maxSellMetricValue);
				s.setDouble(5, minimumAlpha);
				s.setDouble(6, minimumAlpha);
				s.setInt(7, limit);
			}
			else {
				s.setDouble(3, minimumAlpha);
				s.setDouble(4, minimumAlpha);
				s.setInt(5, limit);
			}
			
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				modelIds.add(rs.getInt("id"));
			}
			
			rs.close();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return modelIds;
	}
	
	public static void setModelsToUseInBacktest(HashSet<Integer> modelIDs) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			
			// Query 1 to set all useinbacktests to false
			String q1 = "UPDATE models SET useinbacktests = false";
			PreparedStatement s1 = c.prepareStatement(q1);
			s1.executeUpdate();
			s1.close();
			
			// Query 2 to set the selected top models useinbacktests to true
			String inClause = "(0)";
			if (modelIDs != null && modelIDs.size() > 0) {
				inClause = "(";
				for (int id : modelIDs) {
					inClause += id + ", ";
				}
				inClause = inClause.substring(0, inClause.length() - 2);
				inClause += ")";
			}
			
			String q2 = "UPDATE models SET useinbacktests = true WHERE id IN " + inClause;
			PreparedStatement s2 = c.prepareStatement(q2);

			s2.executeUpdate();
			
			s2.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
package dbio;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.Bar;
import data.BarKey;
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

	public static HashMap<MetricKey, ArrayList<Float>> loadMetricDisccreteValueHash() {
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
					String q0 = "SELECT COALESCE((SELECT MAX(start) FROM metrics WHERE symbol = ? AND duration = ? AND name = ?), '2010-01-01 00:00:00')";
					PreparedStatement s0 = c.prepareStatement(q0);
					s0.setString(1, bk.symbol);
					s0.setString(2, bk.duration.toString());
					s0.setString(3, metricName);
					ResultSet rs0 = s0.executeQuery();
					Calendar startCal = Calendar.getInstance();
					while (rs0.next()) {
						Timestamp tsStart = rs0.getTimestamp(1);
						startCal.setTimeInMillis(tsStart.getTime());
						break;
					}
					int neededBars = Constants.METRIC_NEEDED_BARS.get(metricName);
					startCal = CalendarUtils.addBars(startCal, bk.duration, -neededBars);
					rs0.close();
					s0.close();
					
					String alphaComparison = "SPY"; // TODO: probably change this.  Seems weird to compare bitcoin or forex to SPY.
					String q = "SELECT b.*, " +
							"(SELECT close FROM bar WHERE symbol = ? AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphaclose, " +
							"(SELECT change FROM bar WHERE symbol = ? AND start <= b.start ORDER BY start DESC LIMIT 1) AS alphachange " +
							"FROM bar b " +
							"WHERE b.start >= ? " +
							"AND b.symbol = ? " +
							"AND b.duration = ? " +
							"ORDER BY b.start";
					
					PreparedStatement s = c.prepareStatement(q);
					s.setString(1, alphaComparison);
					s.setString(2, alphaComparison);
					s.setTimestamp(3, new Timestamp(startCal.getTimeInMillis()));
					s.setString(4, bk.symbol);
					s.setString(5, bk.duration.toString());
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
						float alphaClose = rs.getFloat("alphaclose");
						float alphaChange = rs.getFloat("alphachange");
						float gap = rs.getFloat("gap");
						float change = rs.getFloat("change");

						// Create a Metric
						Metric m = new Metric(metricName, bk.symbol, start, end, bk.duration, volume, adjOpen, adjClose, adjHigh, adjLow, gap, change, alphaClose, alphaChange);
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
	
	public static void insertIntoBar(String symbol, float open, float close, float high, float low, float vwap, float volume, int numTrades, float change, float gap, Calendar start, Calendar end, BAR_SIZE barSize, boolean partial) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO bar(symbol, open, close, high, low, vwap, volume, numtrades, change, gap, start, \"end\", duration, partial) " + 
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, symbol);
			s.setFloat(2, open);
			s.setFloat(3, close);
			s.setFloat(4, high);
			s.setFloat(5, low);
			s.setFloat(6, vwap);
			s.setFloat(7, volume);
			s.setInt(8, numTrades);
			s.setFloat(9, change);
			s.setFloat(10, gap);
			s.setTimestamp(11, new java.sql.Timestamp(start.getTime().getTime()));
			s.setTimestamp(12, new java.sql.Timestamp(end.getTime().getTime()));
			s.setString(13, barSize.toString());
			s.setBoolean(14, partial);
			
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			System.err.println("Fear not - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts if the bar does not exist. Updates if it's marked as partial or if the numTrades column doesn't have data (i.e. the record didn't have tick data when it was made)
	 * 
	 * @param bar
	 */
	public static void insertOrUpdateIntoBar(Bar bar) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
		
			// First see if this bar exists in the DB
			String q = "SELECT partial, numtrades FROM bar WHERE symbol = ? AND start = ? AND duration = ?";
			PreparedStatement s = c.prepareStatement(q);
			s.setString(1, bar.symbol);
			s.setTimestamp(2, new java.sql.Timestamp(bar.periodStart.getTime().getTime()));
			s.setString(3, bar.duration.toString());
			
			ResultSet rs = s.executeQuery();
			boolean exists = false;
			boolean partial = false;
			Object numTrades = null;
			while (rs.next()) {
				exists = true;
				partial = rs.getBoolean("partial");
				numTrades = rs.getObject("numtrades");
				break;
			}
			rs.close();
			s.close();
			c.close();
			
			// If there are no trades for this existing bar, say its partial so it can be updated with bar data that contains this (if coming from tick data)
			if (numTrades == null) {
				partial = true;
			}
		
			// If it doesn't exist, insert it
			if (!exists) {
				Connection c2 = ConnectionSingleton.getInstance().getConnection();
				String q2 = "INSERT INTO bar(symbol, open, close, high, low, vwap, volume, numtrades, change, gap, start, \"end\", duration, partial) " + 
							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement s2 = c2.prepareStatement(q2);
				s2.setString(1, bar.symbol);
				s2.setFloat(2, bar.open);
				s2.setFloat(3, bar.close);
				s2.setFloat(4, bar.high);
				s2.setFloat(5, bar.low);
				s2.setFloat(6, bar.vwap);
				s2.setFloat(7, bar.volume);
				if (bar.numTrades == null) {
					s2.setNull(8, Types.INTEGER);
				}
				else {
					s2.setInt(8, bar.numTrades);
				}
				if (bar.change == null) {
					s2.setNull(9, Types.FLOAT);
				}
				else {
					s2.setFloat(9, bar.change);
				}
				if (bar.gap == null) {
					s2.setNull(10, Types.FLOAT);
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
			// It exists and it's partial, so we need to update it.
			else if (partial) {
				Connection c3 = ConnectionSingleton.getInstance().getConnection();
				String q3 = "UPDATE bar SET symbol = ?, open = ?, close = ?, high = ?, low = ?, vwap = ?, volume = ?, numtrades = ?, change = ?, gap = ?, start = ?, \"end\" = ?, duration = ?, partial = ? " +
							"WHERE symbol = ? AND start = ? AND duration = ?";
				PreparedStatement s3 = c3.prepareStatement(q3);
				s3.setString(1, bar.symbol);
				s3.setFloat(2, bar.open);
				s3.setFloat(3, bar.close);
				s3.setFloat(4, bar.high);
				s3.setFloat(5, bar.low);
				s3.setFloat(6, bar.vwap);
				s3.setFloat(7, bar.volume);
				if (bar.numTrades == null) {
					s3.setNull(8, Types.INTEGER);
				}
				else {
					s3.setInt(8, bar.numTrades);
				}
				if (bar.change == null) {
					s3.setNull(9, Types.FLOAT);
				}
				else {
					s3.setFloat(9, bar.change);
				}
				if (bar.gap == null) {
					s3.setNull(10, Types.FLOAT);
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
		}
		catch (Exception e) {
			System.err.println("Fear not - it's probably just duplicate times causing a PK violation because of FUCKING daylight savings");
			e.printStackTrace();
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

	public static ArrayList<HashMap<String, Object>> getTrainingSet(BarKey bk, Calendar start, Calendar end, ArrayList<String> metricNames) {
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
						"WHERE b.symbol = ? AND b.duration = ? AND b.start " + startOp + " ? AND b.\"end\" " + endOp + " ? ORDER BY b.start DESC";
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
				record.put("open", open);
				record.put("close", close);
				record.put("high", high);
				record.put("low", low);
				record.put("hour", hour);
				record.put("start", startTS);
				for (int a = 0; a < metricNames.size(); a++) {
					String metricName = metricNames.get(a);
					float metricValue = rs.getFloat("m" + a);
					record.put(metricName, metricValue);
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
					record.put("metricmin", null);
				}
				if (metricmax != null) {
					record.put("metricmax", sdf.format(metricmax.getTime()));
				}
				else {
					record.put("metricmax", null);
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
			if (whereClause == null) {
				whereClause = "";
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
				float sellMetricValue = rs.getFloat("sellmetricvalue");
				String stopMetric = rs.getString("stopmetric");
				float stopMetricValue = rs.getFloat("stopmetricvalue");
				int numBars = rs.getInt("numbars");
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
				boolean favorite = rs.getBoolean("favorite");
				boolean tradeOffPrimary = rs.getBoolean("tradeoffprimary");
				boolean tradeOffOpposite = rs.getBoolean("tradeoffopposite");
				
				Model model = new Model(type, modelFile, algo, params, new BarKey(symbol, duration), interbarData, metricList,
						trainStart, trainEnd, testStart, testEnd, sellMetric,
						sellMetricValue, stopMetric, stopMetricValue, numBars, trainDatasetSize,
						trainTrueNegatives, trainFalseNegatives, trainFalsePositives, trainTruePositives,
						trainTruePositiveRate, trainFalsePositiveRate, trainCorrectRate, trainKappa,
						trainMeanAbsoluteError, trainRootMeanSquaredError, trainRelativeAbsoluteError,
						trainRootRelativeSquaredError, trainROCArea, testDatasetSize, testTrueNegatives,
						testFalseNegatives, testFalsePositives, testTruePositives, testTruePositiveRate,
						testFalsePositiveRate, testCorrectRate, testKappa, testMeanAbsoluteError,
						testRootMeanSquaredError, testRelativeAbsoluteError, testRootRelativeSquaredError,
						testROCArea, favorite, tradeOffPrimary, tradeOffOpposite);
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
			            "stopmetricvalue, numbars, traindatasetsize, traintruenegatives,  " +
			            "trainfalsenegatives, trainfalsepositives, traintruepositives,  " +
			            "traintruepositiverate, trainfalsepositiverate, traincorrectrate,  " +
			            "trainkappa, trainmeanabsoluteerror, trainrootmeansquarederror,  " +
			            "trainrelativeabsoluteerror, trainrootrelativesquarederror, trainrocarea,  " +
			            "testdatasetsize, testtruenegatives, testfalsenegatives, testfalsepositives,  " +
			            "testtruepositives, testtruepositiverate, testfalsepositiverate,  " +
			            "testcorrectrate, testkappa, testmeanabsoluteerror, testrootmeansquarederror,  " +
			            "testrelativeabsoluteerror, testrootrelativesquarederror, testrocarea, favorite, tradeoffprimary, tradeoffopposite) " +
			            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			
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
			ps.setFloat(14, m.sellMetricValue);
			ps.setString(15, m.stopMetric);
			ps.setFloat(16, m.stopMetricValue);
			ps.setInt(17, m.numBars);
			ps.setInt(18, m.trainDatasetSize);
			ps.setInt(19, m.trainTrueNegatives);
			ps.setInt(20, m.trainFalseNegatives);
			ps.setInt(21, m.trainFalsePositives);
			ps.setInt(22, m.trainTruePositives);
			ps.setDouble(23, m.trainTruePositiveRate);
			ps.setDouble(24, m.trainFalsePositiveRate);
			ps.setDouble(25, m.trainCorrectRate);
			ps.setDouble(26, m.trainKappa);
			ps.setDouble(27, m.trainMeanAbsoluteError);
			ps.setDouble(28, m.trainRootMeanSquaredError);
			ps.setDouble(29, m.trainRelativeAbsoluteError);
			ps.setDouble(30, m.trainRootRelativeSquaredError);
			ps.setDouble(31, m.trainROCArea);
			ps.setInt(32, m.testDatasetSize);
			ps.setInt(33, m.testTrueNegatives);
			ps.setInt(34, m.testFalseNegatives);
			ps.setInt(35, m.testFalsePositives);
			ps.setInt(36, m.testTruePositives);
			ps.setDouble(37, m.testTruePositiveRate);
			ps.setDouble(38, m.testFalsePositiveRate);
			ps.setDouble(39, m.testCorrectRate);
			ps.setDouble(40, m.testKappa);
			ps.setDouble(41, m.testMeanAbsoluteError);
			ps.setDouble(42, m.testRootMeanSquaredError);
			ps.setDouble(43, m.testRelativeAbsoluteError);
			ps.setDouble(44, m.testRootRelativeSquaredError);
			ps.setDouble(45, m.testROCArea);
			ps.setBoolean(46, m.favorite);
			ps.setBoolean(47, m.tradeOffPrimary);
			ps.setBoolean(48, m.tradeOffOpposite);
			
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
	
	public static void insertTestTrade(String modelFile, Calendar time, double entry, double close, double stop, int numBars) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO testtrades(modelfile, \"time\", entry, close, stop, numbars) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = c.prepareStatement(q);
			
			ps.setString(1, modelFile);
			ps.setTimestamp(2,  new Timestamp(time.getTime().getTime()));
			ps.setDouble(3, entry);
			ps.setDouble(4, close);
			ps.setDouble(5, stop);
			ps.setInt(6, numBars);
			
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
						"COALESCE((SELECT SUM(b.close * t.shares) " +
						"FROM trades t " +
						"INNER JOIN bar b ON t.symbol = b.symbol AND b.start = (SELECT MAX(start) FROM bar WHERE symbol = t.symbol AND duration = t.duration) " +
						"WHERE t.type = 'long' AND t.status = 'open'), 0) " +
						"+  " +
						"COALESCE((SELECT SUM(b.close * t.shares) " +
						"FROM trades t " +
						"INNER JOIN bar b ON t.symbol = b.symbol AND b.start = (SELECT MAX(start) FROM bar WHERE symbol = t.symbol AND duration = t.duration) " +
						"WHERE t.type = 'short' AND t.status = 'open'), 0) " +
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
	
	public static float getCurrentValueOfLongOpenTrades() {
		float value = 0f;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT SUM(b.adjclose * t.shares) AS value " +
					"FROM trades t " +
					"INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol) " +
					"WHERE t.type = 'long' AND status = 'open'";
			PreparedStatement ps = c.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getFloat("value");
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static float getCurrentValueOfShortOpenTrades() {
		float value = 0f;
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT SUM(b.adjclose * t.shares) AS value " +
					"FROM trades t " +
					"INNER JOIN basicr b ON t.symbol = b.symbol AND b.date = (SELECT MAX(date) FROM basicr WHERE symbol = t.symbol) " +
					"WHERE t.type = 'short' AND status = 'open'";
			PreparedStatement ps = c.prepareStatement(q);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getFloat("value");
			}
			rs.close();
			ps.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static ArrayList<String> getTradingPositionSymbols() {
		ArrayList<String> symbols = new ArrayList<String>();
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT DISTINCT symbol FROM trades WHERE status = 'open'";
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
	
	public static ArrayList<HashMap<String, Object>> getOpenPositions() {
		ArrayList<HashMap<String, Object>> openPositions = new ArrayList<HashMap<String, Object>>(); 
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "SELECT type, entry, symbol, duration, shares, suggestedentryprice, actualentryprice, suggestedexitprice, suggestedstopprice, commission, expiration FROM trades WHERE status = 'open'";
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery(q);
			while (rs.next()) {
				HashMap<String, Object> openPosition = new HashMap<String, Object>();
				openPosition.put("type", rs.getString("type"));
				openPosition.put("entry", rs.getTimestamp("entry"));
				openPosition.put("symbol", rs.getString("symbol"));
				openPosition.put("duration", rs.getString("duration"));
				openPosition.put("shares", rs.getFloat("shares"));
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
	
	public static void closePosition(String symbol, String duration, java.sql.Timestamp entry, String exitReason, float exitPrice, float totalCommission, float netProfit, float grossProfit) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "UPDATE trades " +
						"SET status = 'closed', exit = now(), actualexitprice = ?, exitreason = ?, commission = ?, netprofit = ?, grossprofit = ? " +
						"WHERE symbol = ? AND duration = ? AND AGE(entry, ?) = '00:00:00' AND status = 'open'";
			PreparedStatement s = c.prepareStatement(q);
			s.setFloat(1, exitPrice);
			s.setString(2, exitReason);
			s.setFloat(3, totalCommission);
			s.setFloat(4, netProfit);
			s.setFloat(5, grossProfit);
			s.setString(6, symbol);
			s.setString(7, duration);
			s.setTimestamp(8, entry);
			s.executeUpdate();
			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Newer version used by CSCSP Monitor
	 * 
	 * @param suggestedEntry
	 * @param numShares
	 * @param commission
	 * @param model
	 * @return
	 */
	public static void makeTrade(float suggestedEntry, Float actualEntry, float suggestedExitPrice, float suggestedStopPrice, float numShares, float commission, Model model, Calendar expiration) {
		try {
			Connection c = ConnectionSingleton.getInstance().getConnection();
			String q = "INSERT INTO trades(status, entry, exit, \"type\", symbol, duration, shares, suggestedentryprice, actualentryprice, suggestedexitprice, suggestedstopprice, actualexitprice, exitreason, commission, netprofit, grossprofit, model, expiration) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement s = c.prepareStatement(q);
			
			s.setString(1, "open");
			s.setTimestamp(2, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
			s.setDate(3, null);
			s.setString(4, model.type);
			s.setString(5, model.bk.symbol);
			s.setString(6, model.bk.duration.toString());
			s.setFloat(7, numShares);
			s.setFloat(8, suggestedEntry);
			if (actualEntry == null) {
				s.setNull(9, java.sql.Types.FLOAT);
			}
			else {
				s.setFloat(9, actualEntry);
			}
			s.setFloat(10, suggestedExitPrice); // Suggested Exit
			s.setFloat(11, suggestedStopPrice); // Suggested Stop
			s.setNull(12, java.sql.Types.FLOAT); // Actual Exit
			s.setString(13, null);
			s.setFloat(14, commission);
			s.setNull(15, java.sql.Types.FLOAT);
			s.setNull(16, java.sql.Types.FLOAT);
			s.setString(17, model.modelFile);
			s.setTimestamp(18, new java.sql.Timestamp(expiration.getTime().getTime()));
			
			s.executeUpdate();

			s.close();
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
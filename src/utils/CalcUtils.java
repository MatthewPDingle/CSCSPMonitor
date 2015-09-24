package utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class CalcUtils {

	public static float getMean(ArrayList<?> list) {
		try {
			float sum = 0f;
			for (Object i:list) {
				if (i instanceof Float)
					sum += (float)i;
				else if (i instanceof Integer)
					sum += (int)i;
			}
			float mean = sum / (float)list.size();
			return mean;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
	public static float getStandardDeviation(ArrayList<?> list) {
		try {
			if (list.size() <= 0) {
				return -1f;
			}
			float sum = 0f;
			for (Object i:list) {
				if (i instanceof Float) {
					sum += (float)i;
				}
				if (i instanceof Integer) {
					sum +=(int)i;
				}
			}
			float mean = sum / list.size();
			
			// Deviations
			ArrayList<Float> deviations = new ArrayList<Float>();
			for (Object i:list) {
				if (i instanceof Float) {
					deviations.add((float)i - mean);
				}
				if (i instanceof Integer) {
					deviations.add((int)i - mean);
				}
			}
			
			// Deviation Squares
			ArrayList<Float> deviationSquares = new ArrayList<Float>();
			for (Float f:deviations) {
				float square = f * f;
				deviationSquares.add(square);
			}
			
			// Sum of Deviation Squares
			float sumds = 0f;
			for (Float f:deviationSquares) {
				sumds += f;
			}
			
			float d = sumds / (float)(deviationSquares.size() - 1);
			float stdev = (float)Math.sqrt(d);
			
			return stdev;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
	public static ArrayList<Float> removePositives(ArrayList<?> list) {
		ArrayList<Float> rlist = new ArrayList<Float>();
		try {
			for (Object i:list) {
				float n = (float)i;
				if (n <= 0)
					rlist.add(n);
				else {
					rlist.add(0f);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return rlist;
	}
	
	public static float getMaxDrawdownPercent(ArrayList<?> list) {
		try {
			float samplePortfolioValue = 1000f;
			float drawdownBase = samplePortfolioValue;
			float maxDrawdown = 0f;
			float lowestValue = samplePortfolioValue;
			float highestValue = samplePortfolioValue;
			for (Object i:list) {
				samplePortfolioValue *= (1f + ((float)i / 100f));
				if (samplePortfolioValue < drawdownBase) {
					lowestValue = samplePortfolioValue;
					float thisDrawdown = 1f - (lowestValue / drawdownBase);
					if (thisDrawdown > maxDrawdown) {
						maxDrawdown = thisDrawdown;
					}
				}
				if (samplePortfolioValue > highestValue) {
					highestValue = samplePortfolioValue;
					drawdownBase = highestValue;
				}
			}
			return -maxDrawdown * 100f;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
	public static float getMedian(ArrayList<?> list) {
		try {
			ArrayList<Float> flist = new ArrayList<Float>();
			ArrayList<Integer> ilist = new ArrayList<Integer>();
			for (Object i:list) {
				if (i instanceof Float) {
					flist.add((float)i);
				}
				else if (i instanceof Integer) {
					ilist.add((int)i);
				}
			}
			if (flist.size() > 0) {
				Collections.sort(flist);
				if (flist.size() % 2 == 1) {
					return flist.get((flist.size() + 1) / 2 - 1);
				}
				else {
					float lower = flist.get(flist.size() / 2 - 1);
					float upper = flist.get(flist.size() / 2);
					return (lower + upper) / 2f;
				}
			}
			else if (ilist.size() > 0) {
				Collections.sort(ilist);
				if (ilist.size() % 2 == 1) {
					return ilist.get((ilist.size() + 1) / 2 - 1);
				}
				else {
					float lower = ilist.get(ilist.size() / 2 - 1);
					float upper = ilist.get(ilist.size() / 2);
					return (lower + upper) / 2f;
				}
			}
			else {
				return -1f;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
	/**
	 * Note: Taking the geomean of percents vs. their multiplier equivalents is not the same.
	 * Example: (5, 7, -4) != (1.05, 1.07, .96)
	 * 
	 * But it's the best you can do when negative numbers are in the mix.  Just note it.
	 *
	 * @param list
	 * @return
	 */
	public static float getGeoMean(ArrayList<?> list) {
		try {
			float logSum = 0f;
			for (Object i:list) {
				if (i instanceof Float) {
					float n = (float)i;
					n = 1f + (n / 100f);
					
					double log = Math.log10(n);
					logSum += log;
				}
				else if (i instanceof Integer) {
					float n = (int)i;
					n = 1f + (n / 100f);
					
					double log = Math.log10(n);
					logSum += log;
				}
			}
			float meanLog = logSum / (float)list.size();
			float geoMean = (float)Math.pow(10, meanLog);
			float answer = (geoMean - 1f) * 100f;
			return answer;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
	public static float getWinPercent(ArrayList<?> list) {
		try {
			int numWinners = 0;
			for (Object i:list) {
				if (i instanceof Float)
					if ((float)i > 0)
						numWinners++;
				else if (i instanceof Integer)
					if ((int)i > 0)
						numWinners++;
			}
			float winPercent = numWinners / (float)list.size() * 100;
			return winPercent;
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
	public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN);
        return bd.floatValue();
    }
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } 
	    catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	public static boolean isFloat(String s) {
	    try { 
	        Float.parseFloat(s); 
	    } 
	    catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	public static void main (String[] args) {
		ArrayList<Float> l = new ArrayList<Float>();

		l.add(-1f);
		l.add(-1f);
		l.add(12.0f);
		System.out.println(removePositives(l));
	}
}
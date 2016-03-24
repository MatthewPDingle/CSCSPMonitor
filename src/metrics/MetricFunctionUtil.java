package metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.FuncUnstId;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import data.Metric;

public class MetricFunctionUtil {

	private static final int NUM_THREADS = 5;
	
	public static void main (String[] args) {
		try {

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Normalizes the metric values so that they range from 0 to 100.
	 * 
	 * This initial implementation is going to be inefficient as fuck.
	 * 
	 * @param metricSequence
	 */
	public static void normalizeMetricValues(LinkedList<Metric> metricSequence) {
		// Get the min and max denormalized values first
		float minValue = 1000000000f;
		float maxValue = -1000000000f;
		ArrayList<Float> values = new ArrayList<Float>(); 
		for (Metric metric:metricSequence) {
			Float value = metric.value;
			if (value != null) {
				if (value < minValue) {
					minValue = value;
				}
				if (value > maxValue) {
					maxValue = value;
				}
			}
			if (value != null) 
				values.add(value);
		}
		// For lists greater than 1000, toss out the 10 most extreme on each end.
		if (values.size() > 1000) {
			Collections.sort(values);
			minValue = values.get(10);
			maxValue = values.get(values.size() - 10);
		}
		
		// Normalize based on the range
		float denormalizedRange = maxValue - minValue;
		float scaleFactor = 1f;
		if (denormalizedRange != 0) {
			scaleFactor = 100f / denormalizedRange;
		}
		
		for (Metric metric:metricSequence) {
			// Shift unscaled values so the min becomes zero, then apply scale
			Float value = metric.value;
			if (value != null) {
				float zeroBasedValue = value - minValue;
				float normalizedValue = zeroBasedValue * scaleFactor;
				metric.value = normalizedValue;
			}
		}
	}

	/**********************************************************************************************
	 * TA-LIB METRICS
	 **********************************************************************************************/
	
	/**
	 * Accumulation/Distribution Oscillator
	 * 
	 * @param ms
	 * @param fastPeriod - 3 Default
	 * @param slowPeriod - 10 Default
	 */
	public static void fillInADO(ArrayList<Metric> ms, int fastPeriod, int slowPeriod) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = slowPeriod * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dCloses = new double[slowPeriod * multiplier + 1];
			double [] dHighs = new double[slowPeriod * multiplier + 1];
			double [] dLows = new double[slowPeriod * multiplier + 1];
			double [] dVolumes = new double[slowPeriod * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (slowPeriod * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				dVolumes[ii] = ms.get(i).getVolume();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.adOsc(slowPeriod * multiplier, slowPeriod * multiplier, dHighs, dLows, dCloses, dVolumes, fastPeriod, slowPeriod, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "ado" + fastPeriod + "_" + slowPeriod;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Accumulation/Distribution Oscillator First Derivative
	 * 
	 * @param ms
	 * @param fastPeriod - 3 Default
	 * @param slowPeriod - 10 Default
	 */
	public static void fillInADOdydx(ArrayList<Metric> ms, int fastPeriod, int slowPeriod) {
		Core core = new Core();
		
		int multiplier = 2;
		
		Float lastValue = null;
		// 7, 8
		for (int bi = slowPeriod * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dCloses = new double[slowPeriod * multiplier + 1];
			double [] dHighs = new double[slowPeriod * multiplier + 1];
			double [] dLows = new double[slowPeriod * multiplier + 1];
			double [] dVolumes = new double[slowPeriod * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			// 0 - 6, 1 - 7
			for (int i = bi - (slowPeriod * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				dVolumes[ii] = ms.get(i).getVolume();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.adOsc(slowPeriod * multiplier, slowPeriod * multiplier, dHighs, dLows, dCloses, dVolumes, fastPeriod, slowPeriod, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "adodydx" + fastPeriod + "_" + slowPeriod;
				float rawValue = (float)outReal[0];
				if (lastValue == null) { 
					lastValue = rawValue;
				}
				else {
					m.value = rawValue - lastValue;
					lastValue = rawValue;
				}
			}
		}
	}
	
	/**
	 * Average Index ADX
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInADX(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();	
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.adx(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "adx" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Average Index ADX First Derivative
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInADXdydx(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		Float lastValue = null;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.adx(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "adxdydx" + period;
				float rawValue = (float)outReal[0];
				if (lastValue == null) {
					lastValue = rawValue;
				}
				else {
					m.value = rawValue - lastValue;
					lastValue = rawValue;
				}
			}
		}
	}
	
	/**
	 * Average Index Rating ADXR
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInADXR(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 3;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.adxr(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "adxr" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Average Index Rating ADXR First Derivative
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInADXRdydx(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 3;
		
		Float lastValue = null;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.adxr(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "adxrdydx" + period;
				float rawValue = (float)outReal[0];
				if (lastValue == null) {
					lastValue = rawValue;
				}
				else {
					m.value = rawValue - lastValue;
					lastValue = rawValue;
				}
			}
		}
	}
	
	/**
	 * Aroon Oscillator
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInAroonOscillator(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
		
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.aroonOsc(period * multiplier, period * multiplier, dHighs, dLows, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "aroonoscillator" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Average True Range
	 * Normal values are close to the close, but I convert them to percent of the close and multiply by 10
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInATR(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.atr(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "atr" + period;
				float rawValue = (float)outReal[0];
				float adjClose = m.getAdjClose();
				float adjValue = rawValue / adjClose * 100f * 10f;
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * Average True Range - First Derivative
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInATRdydx(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		Float lastValue = null;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.atr(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "atrdydx" + period;
				float rawValue = (float)outReal[0];
				float adjClose = m.getAdjClose();
				float adjValue = rawValue / adjClose * 100f * 10f;
				if (lastValue == null) {
					lastValue = adjValue;
				}
				else {
					m.value = adjValue - lastValue;
					lastValue = adjValue;
				}
			}
		}
	}
	
	/**
	 * Beta - Compares volatility to an alpha index like SP500
	 * 
	 * @param ms
	 * @param period
	 * @return
	 */
	public static void fillInBeta(ArrayList<Metric> ms, int period) {
		Core core = new Core();

		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dAlphaCloses = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dAlphaCloses[ii] = ms.get(i).getAlphaAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.beta(period * multiplier, period * multiplier, dAlphaCloses, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "beta" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Commodity Channel Index
	 * Normal values are with -100 to 100 but can go several times that in either direction.
	 * I just divide the value by 5 to keep it smaller.
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInCCI(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.cci(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "cci" + period;
				float rawValue = (float)outReal[0];
				float adjValue = rawValue / 5f;
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * Chande Momentum Oscillator CMO - Like a modified RSI
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInCMO(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.cmo(period * multiplier, period * multiplier, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "cmo" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * MACD
	 * I adjust the value x10
	 * 
	 * @param ms
	 * @param period1 Fast
	 * @param period2 Slow
	 * @param period3 Signal
	 */
	public static void fillInMACD(ArrayList<Metric> ms, int period1, int period2, int period3) {
		Core core = new Core();
	
		int multiplier = 2;
		
		for (int bi = period2 * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period2 * multiplier + 1];
			double [] outMACD = new double[1];
			double [] outMACDSignal = new double[1];
			double [] outMACDHist = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period2 * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.macd(period2 * multiplier, period2 * multiplier, dCloses, period1, period2, period3, outBeginIndex, outLength, outMACD, outMACDSignal, outMACDHist);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "macd" + period1 + "_" + period2 + "_" + period3;
				float rawValue = (float)outMACD[0];
				float adjValue = rawValue * 10f;
				m.value = adjValue;
			}
		}
	}
		
	/**
	 * MACD Signal
	 * I adjust the value x10
	 * 
	 * @param ms
	 * @param period1 Fast
	 * @param period2 Slow
	 * @param period3 Signal
	 */
	public static void fillInMACDSignal(ArrayList<Metric> ms, int period1, int period2, int period3) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period2 * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period2 * multiplier + 1];
			double [] outMACD = new double[1];
			double [] outMACDSignal = new double[1];
			double [] outMACDHist = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period2 * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.macd(period2 * multiplier, period2 * multiplier, dCloses, period1, period2, period3, outBeginIndex, outLength, outMACD, outMACDSignal, outMACDHist);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "macds" + period1 + "_" + period2 + "_" + period3;
				float rawValue = (float)outMACDSignal[0];
				float adjValue = rawValue * 10f;
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * MACD History
	 * I adjust the value x10
	 * 
	 * @param ms
	 * @param period1 Fast
	 * @param period2 Slow
	 * @param period3 Signal
	 */
	public static void fillInMACDHistory(ArrayList<Metric> ms, int period1, int period2, int period3) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period2 * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period2 * multiplier + 1];
			double [] outMACD = new double[1];
			double [] outMACDSignal = new double[1];
			double [] outMACDHist = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period2 * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.macd(period2 * multiplier, period2 * multiplier, dCloses, period1, period2, period3, outBeginIndex, outLength, outMACD, outMACDSignal, outMACDHist);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "macdh" + period1 + "_" + period2 + "_" + period3;
				float rawValue = (float)outMACDHist[0];
				float adjValue = rawValue * 10f;
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * Money Flow Index
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInMFI(ArrayList<Metric> ms, int period) {
		Core core = new Core();

		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double[] dVolumes = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				dVolumes[ii] = ms.get(i).getVolume();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.mfi(period * multiplier, period * multiplier, dHighs, dCloses, dLows, dVolumes, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "mfi" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Candlestick Pattern Detection - The output ends up being 0 for no, 1 for yes.
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInPattern(ArrayList<Metric> ms, String patternName) {
		Core core = new Core();

		int multiplier = 2;
		int period = 10;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dOpens = new double[period * multiplier + 1];
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			int [] out = new int[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dOpens[ii] = ms.get(i).getAdjOpen();
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = null;
			switch (patternName) {
				case "hammer":
					retCode = core.cdlHammer(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "doji":
					retCode = core.cdlDoji(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "morningstar":
					double optInPenetration = 0;
					retCode = core.cdlMorningStar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, optInPenetration, outBeginIndex, outLength, out);
					break;
			}
			if (retCode != null && retCode == RetCode.Success) { 
				Metric m = ms.get(bi - 1);
				m.name = patternName;
				float rawValue = (float)out[0];
				float adjValue = 0f;
				if (rawValue == 100) {
					adjValue = 1f;
				}
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * Percentage Price Oscillator PPO
	 * 
	 * @param ms
	 * @param fastPeriod
	 * @param slowPeriod 
	 */
	public static void fillInPPO(ArrayList<Metric> ms, int fastPeriod, int slowPeriod) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = slowPeriod * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[slowPeriod * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (slowPeriod * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.ppo(slowPeriod * multiplier, slowPeriod * multiplier, dCloses, fastPeriod, slowPeriod, MAType.Sma, outBeginIndex, outLength, outReal);	
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "ppo" + fastPeriod + "_" + slowPeriod;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Percentage Price Oscillator PPO First Derivative
	 * 
	 * @param ms
	 * @param fastPeriod 
	 * @param slowPeriod 
	 */
	public static void fillInPPOdydx(ArrayList<Metric> ms, int fastPeriod, int slowPeriod) {
		Core core = new Core();
		
		int multiplier = 2;
		
		Float lastValue = null;
		
		for (int bi = slowPeriod * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[slowPeriod * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (slowPeriod * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.ppo(slowPeriod * multiplier, slowPeriod * multiplier, dCloses, fastPeriod, slowPeriod, MAType.Sma, outBeginIndex, outLength, outReal);	
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "ppodydx" + fastPeriod + "_" + slowPeriod;
				float rawValue = (float)outReal[0];
				if (lastValue == null) {
					lastValue = rawValue;
				}
				else {
					m.value = rawValue - lastValue;
					lastValue = rawValue;
				}
			}
		}
	}
	
	/**
	 * An interpretation of price Bollinger Bands.  This measures the number of standard deviations away from the simple moving average the price is
	 * @param ms
	 * @param period
	 */
	public static void fillInPriceBolls(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double[] outSMA = new double[1];
			double[] outSTDDEV = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex1 = new MInteger();
			MInteger outLength1 = new MInteger();
			MInteger outBeginIndex2 = new MInteger();
			MInteger outLength2 = new MInteger();
			double optInNbDev = 1; // Multiplier for band?
			
			RetCode smaRetCode = core.sma(period * multiplier, period * multiplier, dCloses, period, outBeginIndex1, outLength1, outSMA);
			RetCode stddevRetCode = core.stdDev(period * multiplier, period * multiplier, dCloses, period, optInNbDev, outBeginIndex2, outLength2, outSTDDEV);
			if (smaRetCode == RetCode.Success && stddevRetCode == RetCode.Success) { 
				Metric m = ms.get(bi - 1);
				m.name = "pricebolls" + period;
				float sma = (float)outSMA[0];
				float stddev = (float)outSTDDEV[0];
				float adjClose = m.getAdjClose();
				float boll = 0;
				if (stddev != 0) {
					boll = (adjClose - sma) / stddev;
				}
				float rawValue = boll;
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Parabolic SAR 
	 * Normal values are similar to the closes, but I changed it to be percentage away from the close.
	 * Experimenting with a period with optInAcceleration and optInMaximum didn't result in different values.
	 * 
	 * @param ms
	 * @return
	 */
	public static void fillInPSAR(ArrayList<Metric> ms) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = 30 * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dHighs = new double[30 * multiplier + 1];
			double [] dLows = new double[30 * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (30 * multiplier + 1); i < bi; i++) {
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			double optInAcceleration = 30 / 1000d;
			double optInMaximum = 30 / 100d;
			
			RetCode retCode = core.sar(30 * multiplier, 30 * multiplier, dHighs, dLows, optInAcceleration, optInMaximum, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "psar";
				float rawValue = (float)outReal[0];
				float adjValue = (rawValue - m.getAdjClose()) / m.getAdjClose() * 100f;
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * Relative Strength Index
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInRSI(ArrayList<Metric> ms, int period) {
		Core core = new Core();

		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dCloses = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.rsi(period * multiplier, period * multiplier, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "rsi" + period;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Stochastic (Slow D) using Exponential Moving Average
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInStochasticD(ArrayList<Metric> ms, int periodFastK, int periodSlowK, int periodSlowD) {
		Core core = new Core();

		int multiplier = 2;
		
		for (int bi = periodFastK * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[periodFastK * multiplier + 1];
			double [] dHighs = new double[periodFastK * multiplier + 1];
			double [] dLows = new double[periodFastK * multiplier + 1];
			double [] outSlowK = new double[1];
			double [] outSlowD = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (periodFastK * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.stoch(periodFastK * multiplier, periodFastK * multiplier, dHighs, dLows, dCloses, periodFastK, periodSlowK, MAType.Ema, periodSlowD, MAType.Ema, outBeginIndex, outLength, outSlowK, outSlowD);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "stod" + periodFastK + "_" + periodSlowK + "_" + periodSlowD;
				float rawValue = (float)outSlowD[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Stochastic (Slow K) using Exponential Moving Average
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInStochasticK(ArrayList<Metric> ms, int periodFastK, int periodSlowK, int periodSlowD) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = periodFastK * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[periodFastK * multiplier + 1];
			double [] dHighs = new double[periodFastK * multiplier + 1];
			double [] dLows = new double[periodFastK * multiplier + 1];
			double [] outSlowK = new double[1];
			double [] outSlowD = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (periodFastK * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.stoch(periodFastK * multiplier, periodFastK * multiplier, dHighs, dLows, dCloses, periodFastK, periodSlowK, MAType.Ema, periodSlowD, MAType.Ema, outBeginIndex, outLength, outSlowK, outSlowD);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "stok" + periodFastK + "_" + periodSlowK + "_" + periodSlowD;
				float rawValue = (float)outSlowK[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Stochastic RSI (Fast D) using Exponential Moving Average
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInStochasticDRSI(ArrayList<Metric> ms, int period, int periodFastK, int periodFastD) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] outFastK = new double[1];
			double [] outFastD = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.stochRsi(period * multiplier, period * multiplier, dCloses, period, periodFastK, periodFastD, MAType.Ema, outBeginIndex, outLength, outFastK, outFastD);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "stodrsi" + period + "_" + periodFastK + "_" + periodFastD;
				float rawValue = (float)outFastD[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Stochastic RSI (Fast K) using Exponential Moving Average
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInStochasticKRSI(ArrayList<Metric> ms, int period, int periodFastK, int periodFastD) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] outFastK = new double[1];
			double [] outFastD = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.stochRsi(period * multiplier, period * multiplier, dCloses, period, periodFastK, periodFastD, MAType.Ema, outBeginIndex, outLength, outFastK, outFastD);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "stokrsi" + period + "_" + periodFastK + "_" + periodFastD;
				float rawValue = (float)outFastK[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Time Series Forecast
	 * Normal values are close to the closes, but I normalize them to % away from close & multiply x10
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInTSF(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.tsf(period * multiplier, period * multiplier, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "tsf" + period;
				float rawValue = (float)outReal[0];
				float adjClose = m.getAdjClose();
				float adjValue = (rawValue - adjClose) / adjClose * 100f * 10f;
				m.value = adjValue;
			}
		}
	}
	
	/**
	 * Time Series Forecast First Derivative
	 * Normal values are close to the closes, but I normalize them to % away from close & multiply x10
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInTSFdydx(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		Float lastValue = null;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.tsf(period * multiplier, period * multiplier, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "tsfdydx" + period;
				float rawValue = (float)outReal[0];
				float adjClose = m.getAdjClose();
				float adjValue = (rawValue - adjClose) / adjClose * 100f * 10f;
				if (lastValue == null) {
					lastValue = adjValue;
				}
				else {
					m.value = adjValue - lastValue;
					lastValue = adjValue;
				}
			}
		}
	}
	
	/**
	 * Ultimate Oscillator
	 * 
	 * @param ms
	 */
	public static void fillInUltimateOscillator(ArrayList<Metric> ms, int period1, int period2, int period3) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period3 * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period3 * multiplier + 1];
			double [] dHighs = new double[period3 * multiplier + 1];
			double [] dLows = new double[period3 * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period3 * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.ultOsc(period3 * multiplier, period3 * multiplier, dHighs, dLows, dCloses, period1, period2, period3, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "uo" + period1 + "_" + period2 + "_" + period3;
				float rawValue = (float)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * An interpretation of volume Bollinger Bands.  This measures the number of standard deviations away from the simple moving average the volume is
	 *
	 * @param ms
	 * @param period
	 */
	public static void fillInVolumeBolls(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dVolumes = new double[period * multiplier + 1];
			double [] outSMA = new double[1];
			double [] outSTDDEV = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dVolumes[ii] = ms.get(i).getAdjClose();
				ii++;
			}
			
			MInteger outBeginIndex1 = new MInteger();
			MInteger outLength1 = new MInteger();
			MInteger outBeginIndex2 = new MInteger();
			MInteger outLength2 = new MInteger();
			double optInNbDev = 1; // Multiplier for band?
			
			RetCode smaRetCode = core.sma(period * multiplier, period * multiplier, dVolumes, period, outBeginIndex1, outLength1, outSMA);
			RetCode stddevRetCode = core.stdDev(period * multiplier, period * multiplier, dVolumes, period, optInNbDev, outBeginIndex2, outLength2, outSTDDEV);
			if (smaRetCode == RetCode.Success && stddevRetCode == RetCode.Success) { 
				Metric m = ms.get(bi - 1);
				m.name = "volumebolls" + period;
				float sma = (float)outSMA[0];
				float stddev = (float)outSTDDEV[0];
				float volume = (float)m.getVolume();
				float boll = 0;
				if (stddev != 0) {
					boll = (volume - sma) / stddev;
				}
				float rawValue = boll;
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Williams %R
	 * Normally values are -100 to 0 but I add 100 to make it 0 to 100
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInWilliamsR(ArrayList<Metric> ms, int period) {
		Core core = new Core();

		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) { // bi = Base Index - Need to get the last multiplier period Metrics and calculate the last one
			double [] dCloses = new double[period * multiplier + 1];
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outReal = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dCloses[ii] = ms.get(i).getAdjClose();
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
			
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.willR(period * multiplier, period * multiplier, dHighs, dLows, dCloses, period, outBeginIndex, outLength, outReal);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "williamsr" + period;
				float rawValue = (float)outReal[0];
				float adjValue = rawValue + 100;
				m.value = adjValue;
			}
		}
	}

	/**********************************************************************************************
	 * HOMEBREW METRICS
	 **********************************************************************************************/
	
	/**
	 * Number of consecutive up bars
	 * 
	 * @param ms
	 */
	public static void fillInConsecutiveUps(ArrayList<Metric> ms) {
		float lastAdjClose = -1f;
		int consecutiveUpMetrics = 0;
		
		for (Metric metric:ms) {
			float adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics++;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics = 0;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics = 0;
		  	}
			
			metric.value = (float)consecutiveUpMetrics;
			metric.name = "consecutiveups";
			
		  	lastAdjClose = adjClose;
		}
	}
	
	/**
	 * Number of consecutive down bars
	 * 
	 * @param ms
	 */
	public static void fillInConsecutiveDowns(ArrayList<Metric> ms) {
		float lastAdjClose = -1f;
	    int consecutiveDownMetrics = 0;
		
		for (Metric metric:ms) {
			float adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics = 0;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics++;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics = 0;
		  	}

			metric.value = (float)consecutiveDownMetrics;
			metric.name = "consecutivedowns";
			
		  	lastAdjClose = adjClose;
		}
	}
	
	/**
	 * For consecutive bars, the total movement % up or down
	 * 
	 * @param ms
	 */
	public static void fillInCPS(ArrayList<Metric> ms) {
		float lastAdjClose = -1f;
		float startingClose = 0f;
		int consecutiveCount = 0;
		String consecutiveType = "";
		
		for (Metric metric:ms) {
			float adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
				if (!consecutiveType.equals("up")) {
					consecutiveCount = 0;
					startingClose = lastAdjClose;
					consecutiveType = "up";
				}
		  		consecutiveCount++;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		if (!consecutiveType.equals("down")) {
		  			consecutiveCount = 0;
		  			startingClose = lastAdjClose;
		  			consecutiveType = "down";
		  		}
		  		consecutiveCount++;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveType = "";
		  		consecutiveCount = 0;
		  	}
			
			float perchange = 0f;
			if (consecutiveCount > 0) {
				perchange = (adjClose - startingClose) / startingClose * 100f;
			}
			
			metric.value = (float)perchange;
			metric.name = "cps";
			
		  	lastAdjClose = adjClose;
		}
	}
	
	public static void fillInDVOLdydx(ArrayList<Metric> metricSequence, int weight) { 
		// Initialize Variables
		float yesterdaysDVol = 0f;
	  	int c = 1;
	  	
	  	Float lastValue = null;
	  	for (Metric metric:metricSequence) {
	  		float adjClose = metric.getAdjClose();
	  		float adjOpen = metric.getAdjOpen();
	  		float adjHigh = metric.getAdjHigh();
	  		float adjLow = metric.getAdjLow();
	  		
	  		float todaysAvg = (adjClose + adjOpen + adjHigh + adjLow) / 4f;
	  		float todaysRange = adjHigh - adjLow;
	  		float todaysDVol = todaysRange / todaysAvg * 100f;
	  	
		  	if (c > 1) {
		  		todaysDVol = ((todaysDVol * weight / 100f) + (yesterdaysDVol * (1 - (weight / 100f))));
		  	}

		  	// Set this day's DVOL value and add it to the new sequence
		  	if (c >= 10) {
		  		if (lastValue == null) lastValue = todaysDVol;
			  	metric.value = todaysDVol - lastValue;
		  	}
		  	else {
		  		metric.value = null;
		  	}
		  	metric.name = "dvoldydx" + weight + "ema";
		  	
		  	lastValue = todaysDVol;
		  	yesterdaysDVol = todaysDVol;
		  	c++;
	  	}
	}
	
	/**
	 * Standard Deviation as a percent of DMA, then the derivative is taken.
	 * 
	 * @param metricSequence
	 * @param period
	 * @return
	 */
	public static void fillInMVOLdydx(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		Float lastValue = null;
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "mvol" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float periodsAdjClosesSum = 0;
		  		for (Float p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		float averagePrice = periodsAdjClosesSum / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		float sdapodma = sd / averagePrice * 100;
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		if (lastValue == null) lastValue = sdapodma;
		  		metric.value = sdapodma - lastValue;
		  		metric.name = "mvoldydx" + period;
		  		
		  		lastValue = sdapodma;
		  		periodsAdjCloses.remove();
		  	}
		}
	}
	
	/**
	 * Describes the pressure the price is to breaking out of the range defined by the period.  
	 * A rangepressure value of 0 would be extremely close to breaking downward out of the range.
	 * A rangepressure value of .5 would be right in the middle of the range.
	 * A rangepressure value of 1 would be extremely close to breaking upward out of a range.
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInRangePressure(ArrayList<Metric> ms, int period) {
		LinkedList<Float> closes = new LinkedList<Float>();

	  	for (Metric metric : ms) {
	  		float adjClose = metric.getAdjClose();
	  		if (closes.size() < period) {
	  			closes.add(adjClose);
	  			metric.value = null;
	  		}

	  		else if (closes.size() == period) {
	  			float highestClose = closes.getFirst();
	  			float lowestClose = closes.getFirst();
	  			for (Float close : closes) {
	  				if (close > highestClose) {
	  					highestClose = close;
	  				}
	  				if (close < lowestClose) {
	  					lowestClose = close;
	  				}
	  			}
	  			
	  			float periodRange = highestClose - lowestClose;
	  			float adjCloseFromHighestClose = highestClose - adjClose;
	  			float adjCloseFromLowestClose = adjClose - lowestClose;
	  			float rangePressure = .5f;
	  			if (adjCloseFromHighestClose < adjCloseFromLowestClose) {
	  				// We're nearer to the top of the range
	  				if (periodRange != 0) {
	  					rangePressure = 1 - (adjCloseFromHighestClose / periodRange);
	  				}
	  			}
	  			else {
	  				// We're nearer to the bottom of the range
	  				if (periodRange != 0) {
	  					rangePressure = (adjCloseFromLowestClose / periodRange);
	  				}
	  			}
	  			
	  			// If rangePressure > or < 0 it's broken out.
	  			if (rangePressure > 1) {
	  				rangePressure = 1;
	  			}
	  			if (rangePressure < 0) {
	  				rangePressure = 0;
	  			}

	  			if (rangePressure == Float.NEGATIVE_INFINITY || rangePressure == Float.POSITIVE_INFINITY) {
	  				rangePressure = .5f;
	  			}

	  			metric.value = rangePressure;
	  			
	  			// Toss the oldest, add the latest
	  			closes.remove();
	  			closes.add(adjClose);
	  		}

	  		metric.name = "rangepressure" + period;
	  	}
	}
	
	/**
	 * The number of bars that the open & close stay within a range, specified as a fraction of price. 
	 * The range resets to zero each time it is broken.
	 * Range comes in as thousandths
	 * A range of 10 (translating to .01) would allow 1% total price movement before resetting.
	 * The value is the number of bars spent in that range.  A double square root of the value is taken to keep values relatively low.
	 * 
	 * @param ms
	 * @param range
	 */
	public static void fillInTimeRange(ArrayList<Metric> ms, int range) {
		float rangeStartingPrice = -1;
		float rangeHigh = 0;
		float rangeLow = 0;
		int numBarsInRange = 1;
		int metricCounter = 0;
		float rangeFraction = range / 1000f;
		final int IGNORE_THE_FIRST_X_BARS = range / 2;
		
		for (Metric metric : ms) {
			metricCounter++;
			if (rangeStartingPrice == -1) {
				rangeStartingPrice = metric.getAdjClose();
				rangeHigh = rangeStartingPrice;
				rangeLow = rangeStartingPrice;
				continue;
			}
			
			float open = metric.getAdjOpen();
			float close = metric.getAdjClose();
			numBarsInRange++;
			
			if (open > rangeHigh) {
				rangeHigh = open;
			}
			if (close > rangeHigh) {
				rangeHigh = close;
			}
			if (open < rangeLow) {
				rangeLow = open;
			}
			if (close < rangeLow) {
				rangeLow = close;
			}
			
			float currentRange = rangeHigh - rangeLow;
			float currentRangeP = currentRange / rangeStartingPrice;
			if (currentRangeP > rangeFraction) {
				numBarsInRange = 1;
				rangeStartingPrice = close;
				rangeHigh = rangeStartingPrice;
				rangeLow = rangeStartingPrice;
			}
			
			float adjValue = (float)Math.sqrt(Math.sqrt(numBarsInRange)) - 1;
			
			if (metricCounter > IGNORE_THE_FIRST_X_BARS) {
				metric.value = adjValue;
				metric.name = "timerange" + range;
			}
			else {
				metric.value = null;
				metric.name = "timerange" + range;
			}
		}
	}
	
	/**
	 * The number of bars that the close stays within a range, specified as a fraction of price.
	 * The range never resets to zero.
	 * Range comes in as thousandths.
	 * A range of 10 (translating to .01) would allow 1% total price movement before resetting.
	 * The value is the number of bars (average of last 25) spent in that range.
	 * 
	 * @param ms
	 * @param range
	 */
	public static void fillInTimeRangeAlpha(ArrayList<Metric> ms, int range) {
		final int IGNORE_THE_FIRST_X_BARS = (range * 150) / 2;
		final int AVERAGE_OVER = (range * 150) / 10;
		
		LinkedList<Float> lastX = new LinkedList<Float>();

		for (int a = 0; a < ms.size(); a++) {
			Metric metric = ms.get(a);
			
			if (a > IGNORE_THE_FIRST_X_BARS) {
				float close = metric.getAdjClose();
				
				boolean inRange = true;
				int rangeCount = 1;
				while (inRange) {
					if (a - rangeCount < 0) {
						break;
					}
					Metric pMetric = ms.get(a - rangeCount);
					float pMetricClose = pMetric.getAdjClose();

					float pRange = Math.abs(close - pMetricClose);
					float pRangePercent = pRange / close;
					if (pRangePercent >= (range / 1000f)) {
						break;
					}
					rangeCount++;
				}
				
				if (lastX.size() == AVERAGE_OVER) {
					lastX.removeFirst();
				}
				lastX.addLast((float)rangeCount);
				
				float rangeTotal = 0;
				for (float r : lastX) {
					rangeTotal += r;
				}
				float averageOfLastX = rangeTotal / lastX.size();
				
				metric.name = "timerangealpha" + range;
				metric.value = (float)averageOfLastX;
			}
			else {
				metric.name = "timerangealpha" + range;
				metric.value = null;
			}
		}
	}

	/**********************************************************************************************
	 * OLD METRICS
	 **********************************************************************************************/
	
	public static void fillInPriceDMAs(ArrayList<Metric> ms, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:ms) {
			float adjClose = metric.getAdjClose();

			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "pricedma" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float priceSum = 0;
		  		for (Float price:periodsAdjCloses) {
		  			priceSum += price;
		  		}
		  		float dma = priceSum / (float)period;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.value = dma;
		  		metric.name = "pricedma" + period;
		  		
		  		periodsAdjCloses.remove();
		  	}
		}
	}
	
	public static void fillInGapBoll(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodGPCs = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float gap = metric.getGap();
			float adjOpen = metric.getAdjOpen();
			float gpc = gap / (adjOpen - gap) * 100f;

			if (periodGPCs.size() < (period - 1)) {
		  		periodGPCs.add(gpc);
		  		metric.value = null;
		  		metric.name = "gapboll" + period;
		  	}
		  	else {
		  		// DMA
		  		periodGPCs.add(gpc);
		  		float gpcSum = 0;
		  		for (Float thisGPC:periodGPCs) {
		  			gpcSum += thisGPC;
		  		}
		  		float dma = gpcSum / (float)period;
		  		
		  		// SD
		  		float gpcSum2 = 0;
		  		for (Float thisGPC:periodGPCs) {
		  			gpcSum2 += thisGPC;
		  		}
		  		float averageGPC = gpcSum2 / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float thisGPC:periodGPCs) {
		  			sumOfDifferenceFromAverageSquares += ((thisGPC - averageGPC) * (thisGPC - averageGPC));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		float boll = 0;
		  		if (sd != 0) {
		  			boll = (gpc - dma) / sd;
		  		}
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.value = boll;
		  		metric.name = "gapboll" + period;
		  		
		  		periodGPCs.remove();
		  	}
		}
	}
	
	public static void fillInIntradayBoll(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodIDPCs = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float gap = metric.getGap();
			float change = metric.getChange();
			float adjOpen = metric.getAdjOpen();
			float idpc = (change - gap) / (adjOpen - gap) * 100f;

			if (periodIDPCs.size() < (period - 1)) {
		  		periodIDPCs.add(idpc);
		  		metric.value = null;
		  		metric.name = "intradayboll" + period;
		  	}
		  	else {
		  		// DMA
		  		periodIDPCs.add(idpc);
		  		float idpcSum = 0;
		  		for (Float thisIDPC:periodIDPCs) {
		  			idpcSum += thisIDPC;
		  		}
		  		float dma = idpcSum / (float)period;
		  		
		  		// SD
		  		float idpcSum2 = 0;
		  		for (Float thisIDPC:periodIDPCs) {
		  			idpcSum2 += thisIDPC;
		  		}
		  		float averageIDPC = idpcSum2 / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float thisIDPC:periodIDPCs) {
		  			sumOfDifferenceFromAverageSquares += ((thisIDPC - averageIDPC) * (thisIDPC - averageIDPC));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// 
		  		float boll = 0f;
		  		if (sd != 0) {
		  			boll = (idpc - dma) / sd;
		  		}
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.value = boll;
		  		metric.name = "intradayboll" + period;
		  		
		  		periodIDPCs.remove();
		  	}
		}
	}
	
	public static void fillInVolumeDMAs(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Double> periodsVolumes = new LinkedList<Double>();
		
		for (Metric metric:metricSequence) {
			double volume = metric.getVolume();

			if (periodsVolumes.size() < (period - 1)) {
		  		periodsVolumes.add(volume);
		  		metric.value = null;
		  		metric.name = "volumedma" + period;
		  	}
		  	else {
		  		periodsVolumes.add(volume);
		  		double volumeSum = 0;
		  		for (Double pv : periodsVolumes) {
		  			volumeSum += pv;
		  		}
		  		double dma = volumeSum / period;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.value = (float)dma;
		  		metric.name = "volumedma" + period;
		  		
		  		periodsVolumes.remove();
		  	}
		}
	}
	
	public static void fillInPriceSDs(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "pricesd" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float periodsAdjClosesSum = 0;
		  		for (Float p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		float averagePrice = periodsAdjClosesSum / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.value = sd;
		  		metric.name = "pricesd" + period;
		
		  		periodsAdjCloses.remove();
		  	}
		}
	}
	
	/**
	 * Standard Deviation as a percent of DMA.  I might use this for position sizing.
	 * 
	 * @param metricSequence
	 * @param period
	 * @return
	 */
	public static void fillInMVOL(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Float> periodsAdjCloses = new LinkedList<Float>();
		
		for (Metric metric:metricSequence) {
			float adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "mvol" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		float periodsAdjClosesSum = 0;
		  		for (Float p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		float averagePrice = periodsAdjClosesSum / (float)period;
		  		float sumOfDifferenceFromAverageSquares = 0;
		  		for (Float p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		float sdapodma = sd / averagePrice * 100;
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.value = sdapodma;
		  		metric.name = "mvol" + period;
		
		  		periodsAdjCloses.remove();
		  	}
		}
	}
	
	public static void fillInVolumeSDs(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Double> periodsVolumes = new LinkedList<Double>();
		
		for (Metric metric:metricSequence) {
			double volume = metric.getVolume();
			
			if (periodsVolumes.size() < (period - 1)) {
		  		periodsVolumes.add(volume);
		  		metric.value = null;
		  		metric.name = "volumesd" + period;
		  	}
		  	else {
		  		periodsVolumes.add(volume);
		  		double periodsVolumesSum = 0;
		  		for (Double v : periodsVolumes) {
		  			periodsVolumesSum += v;
		  		}
		  		double averageVolume = periodsVolumesSum / period;
		  		double sumOfDifferenceFromAverageSquares = 0;
		  		for (Double v : periodsVolumes) {
		  			sumOfDifferenceFromAverageSquares += ((v - averageVolume) * (v - averageVolume));
		  		}
		  		float sd = (float)Math.sqrt(sumOfDifferenceFromAverageSquares / (float)period);
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.value = sd;
		  		metric.name = "volumesd" + period;
		  		
		  		periodsVolumes.remove();
		  	}
		}
	}
	
	public static void fillInWeightedDVol(ArrayList<Metric> metricSequence, int weight) { 
		// Initialize Variables
		float yesterdaysDVol = 0f;
	  	int c = 1;
	  	
	  	for (Metric metric:metricSequence) {
	  		float adjClose = metric.getAdjClose();
	  		float adjOpen = metric.getAdjOpen();
	  		float adjHigh = metric.getAdjHigh();
	  		float adjLow = metric.getAdjLow();
	  		
	  		float todaysAvg = (adjClose + adjOpen + adjHigh + adjLow) / 4f;
	  		float todaysRange = adjHigh - adjLow;
	  		float todaysDVol = todaysRange / todaysAvg * 100f;
	  	
		  	if (c > 1) {
		  		todaysDVol = ((todaysDVol * weight / 100f) + (yesterdaysDVol * (1 - (weight / 100f))));
		  	}

		  	// Set this day's DVOL value and add it to the new sequence
		  	if (c >= (weight / 2)) {
			  	metric.value = todaysDVol;
			  	
		  	}
		  	else {
		  		metric.value = null;
		  	}
		  	metric.name = "dvol" + weight + "ema";
		  	
		  	yesterdaysDVol = todaysDVol;
		  	c++;
	  	}
	}
	
	public static void fillInBreakouts(ArrayList<Metric> ms, int period) { 
		// Initialize Variables
	  	LinkedList<Float> closes = new LinkedList<Float>();

	  	LinkedList<Float> lastX = new LinkedList<Float>();
	  	
	  	for (Metric metric : ms) {
	  		float adjClose = metric.getAdjClose();
	  		if (closes.size() < period) {
	  			closes.add(adjClose);
	  			metric.value = 0f;
	  		}

	  		else if (closes.size() == period) {
	  			float highestClose = closes.getFirst();
	  			float lowestClose = closes.getFirst();
	  			int numDaysSinceToday = 0;
	  			int highNumDaysSincePeriodStart = 0;
	  			int lowNumDaysSincePeriodStart = 0;
	  			for (Float close:closes) {
	  				if (close > highestClose) {
	  					highNumDaysSincePeriodStart = numDaysSinceToday;
	  					highestClose = close;
	  				}
	  				if (close < lowestClose) {
	  					lowNumDaysSincePeriodStart = numDaysSinceToday;
	  					lowestClose = close;
	  				}
	  				numDaysSinceToday++;
	  			}
	  			
	  			float breakout = 0f;
	  			if (adjClose >= highestClose) {
	  				breakout = ((adjClose - highestClose) / highestClose * 100f) * (1 + ((period - highNumDaysSincePeriodStart) / 3f));
	  			}
	  			else if (adjClose <= lowestClose) {
	  				breakout = ((adjClose - lowestClose) / lowestClose * 100f) * (1 + ((period - lowNumDaysSincePeriodStart) / 3f));
	  			}
	  			// Normalize the results a bit to bunch them mostly in a -1 to 1 range
	  			float breakoutABS = Math.abs(breakout);
	  			float breakoutABSp1 = breakoutABS + 1;
	  			float sign = Math.signum(breakout);
	  			float log = (float)Math.log10(breakoutABSp1);
	  			if (log > 1) log = 1;
	  			float adjustedBreakout = log * sign;
	  		
	  			// Make it an average of the last *period*
	  			if (lastX.size() == period) {
					lastX.removeFirst();
				}
				lastX.addLast((float)adjustedBreakout);
				
				float adjustedBreakoutTotal = 0;
				for (float r : lastX) {
					adjustedBreakoutTotal += r;
				}
				float averageOfLastX = adjustedBreakoutTotal / lastX.size();
	  			
	  			metric.value = averageOfLastX;
	  			
	  			// Toss the oldest, add the latest
	  			closes.remove();
	  			closes.add(adjClose);
	  		}

	  		metric.name = "breakout" + period;
	  	}
	}
}
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
import weka.gui.SysErrLog;

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
		double minValue = 1000000000f;
		double maxValue = -1000000000f;
		ArrayList<Double> values = new ArrayList<Double>(); 
		for (Metric metric:metricSequence) {
			Double value = metric.value;
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
		double denormalizedRange = maxValue - minValue;
		double scaleFactor = 1f;
		if (denormalizedRange != 0) {
			scaleFactor = 100f / denormalizedRange;
		}
		
		for (Metric metric:metricSequence) {
			// Shift unscaled values so the min becomes zero, then apply scale
			Double value = metric.value;
			if (value != null) {
				double zeroBasedValue = value - minValue;
				double normalizedValue = zeroBasedValue * scaleFactor;
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
				double rawValue = (double)outReal[0];
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
		
		Double lastValue = null;
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
				m.name = "adodydx" + fastPeriod + "_" + slowPeriod;
				double rawValue = (double)outReal[0];
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
				double rawValue = (double)outReal[0];
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
		
		Double lastValue = null;
		
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
				double rawValue = (double)outReal[0];
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
				double rawValue = (double)outReal[0];
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
		
		Double lastValue = null;
		
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
				double rawValue = (double)outReal[0];
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
				double rawValue = (double)outReal[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Aroon Up
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInAroonUp(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outUp = new double[1];
			double [] outDown = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
		
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.aroon(period * multiplier, period * multiplier, dHighs, dLows, period, outBeginIndex, outLength, outDown, outUp);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "aroonup" + period;
				double rawValue = (double)outUp[0];
				m.value = rawValue;
			}
		}
	}
	
	/**
	 * Aroon Down
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInAroonDown(ArrayList<Metric> ms, int period) {
		Core core = new Core();
		
		int multiplier = 2;
		
		for (int bi = period * multiplier + 1; bi <= ms.size(); bi++) {
			double [] dHighs = new double[period * multiplier + 1];
			double [] dLows = new double[period * multiplier + 1];
			double [] outUp = new double[1];
			double [] outDown = new double[1];
			int ii = 0; // Input index for the data needed in this TA-Lib function
			for (int i = bi - (period * multiplier + 1); i < bi; i++) {
				dHighs[ii] = ms.get(i).getAdjHigh();
				dLows[ii] = ms.get(i).getAdjLow();
				ii++;
			}
		
			MInteger outBeginIndex = new MInteger();
			MInteger outLength = new MInteger();
			
			RetCode retCode = core.aroon(period * multiplier, period * multiplier, dHighs, dLows, period, outBeginIndex, outLength, outDown, outUp);
			if (retCode == RetCode.Success) {
				Metric m = ms.get(bi - 1);
				m.name = "aroondown" + period;
				double rawValue = (double)outDown[0];
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
				double rawValue = (double)outReal[0];
				double adjClose = m.getAdjClose();
				double adjValue = rawValue / adjClose * 100d * 10d;
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
		
		Double lastValue = null;
		
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
				double rawValue = (double)outReal[0];
				double adjClose = m.getAdjClose();
				double adjValue = rawValue / adjClose * 100f * 10f;
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
				double rawValue = (double)outReal[0];
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
				double rawValue = (double)outReal[0];
				double adjValue = rawValue / 5f;
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
				double rawValue = (double)outReal[0];
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
				double rawValue = (double)outMACD[0];
				double adjValue = rawValue * 10f;
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
				double rawValue = (double)outMACDSignal[0];
				double adjValue = rawValue * 10f;
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
				double rawValue = (double)outMACDHist[0];
				double adjValue = rawValue * 10f;
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
				double rawValue = (double)outReal[0];
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
				case "cdl3blackcrows":
					retCode = core.cdl3BlackCrows(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdl3starsinsouth":
					retCode = core.cdl3StarsInSouth(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdl3whitesoldiers":
					retCode = core.cdl3WhiteSoldiers(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlabandonedbaby":
					retCode = core.cdlAbandonedBaby(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;
				case "cdldarkcloudcover":
					retCode = core.cdlDarkCloudCover(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;	
				case "cdldoji":
					retCode = core.cdlDoji(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdldragonflydoji":
					retCode = core.cdlDragonflyDoji(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlengulfing":
					retCode = core.cdlEngulfing(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdleveningdojistar":
					retCode = core.cdlEveningDojiStar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;	
				case "cdleveningstar":
					retCode = core.cdlEveningStar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;
				case "cdlgravestonedoji":
					retCode = core.cdlGravestoneDoji(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlhammer":
					retCode = core.cdlHammer(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlhangingman":
					retCode = core.cdlHangingMan(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlharami":
					retCode = core.cdlHarami(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlharamicross":
					retCode = core.cdlHaramiCross(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlinvertedhammer":
					retCode = core.cdlInvertedHammer(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdllongleggeddoji":
					retCode = core.cdlLongLeggedDoji(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlcdlmarubozu":
					retCode = core.cdlMarubozu(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlmorningstar":
					retCode = core.cdlMorningStar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;
				case "cdlmorningdojistar":
					retCode = core.cdlMorningDojiStar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;
				case "cdlpiercing":
					retCode = core.cdlPiercing(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlrisefall3methods":
					retCode = core.cdlRiseFall3Methods(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlshootingstar":
					retCode = core.cdlShootingStar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlshortline":
					retCode = core.cdlShortLine(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlspinningtop":
					retCode = core.cdlSpinningTop(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlsticksandwich":
					retCode = core.cdlStickSandwhich(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdltasukigap":
					retCode = core.cdlTasukiGap(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlupsidegap2crows":
					retCode = core.cdlUpsideGap2Crows(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdl2crows":
					retCode = core.cdl2Crows(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdl3inside":
					retCode = core.cdl3Inside(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdl3linestrike":
					retCode = core.cdl3LineStrike(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdl3outside":
					retCode = core.cdl3Outside(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;	
				case "cdladvanceblock":
					retCode = core.cdlAdvanceBlock(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlbelthold":
					retCode = core.cdlBeltHold(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlbreakaway":
					retCode = core.cdlBreakaway(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlclosingmarubozu":
					retCode = core.cdlClosingMarubozu(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlconcealbabyswall":
					retCode = core.cdlConcealBabysWall(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlcounterattack":
					retCode = core.cdlCounterAttack(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlgapsidesidewhite":
					retCode = core.cdlGapSideSideWhite(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlhignwave":
					retCode = core.cdlHignWave(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlhikkake":
					retCode = core.cdlHikkake(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlhikkakemod":
					retCode = core.cdlHikkakeMod(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlhomingpigeon":
					retCode = core.cdlHomingPigeon(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlidentical3crows":
					retCode = core.cdlIdentical3Crows(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlinneck":
					retCode = core.cdlInNeck(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlkicking":
					retCode = core.cdlKicking(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlkickingbylength":
					retCode = core.cdlKickingByLength(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlladderbottom":
					retCode = core.cdlLadderBottom(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdllongline":
					retCode = core.cdlLongLine(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlmatchinglow":
					retCode = core.cdlMatchingLow(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlmathold":
					retCode = core.cdlMatHold(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, 0, outBeginIndex, outLength, out);
					break;
				case "cdlonneck":
					retCode = core.cdlOnNeck(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlrickshawman":
					retCode = core.cdlRickshawMan(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlseperatinglines":
					retCode = core.cdlSeperatingLines(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlstalledpattern":
					retCode = core.cdlStalledPattern(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdltakuri":
					retCode = core.cdlTakuri(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlthursting":
					retCode = core.cdlThrusting(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdltristar":
					retCode = core.cdlTristar(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlunique3river":
					retCode = core.cdlUnique3River(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
				case "cdlxsidegap3methods":
					retCode = core.cdlXSideGap3Methods(period * multiplier, period * multiplier, dOpens, dHighs, dLows, dCloses, outBeginIndex, outLength, out);
					break;
			}
			if (retCode != null && retCode == RetCode.Success) { 
				Metric m = ms.get(bi - 1);
				m.name = patternName;
				double rawValue = (double)out[0];
				double adjValue = 0f;
				if (rawValue == 100) {
					adjValue = 1f;
				}
				m.value = adjValue;
			}
			else if (retCode != RetCode.Success) {
				System.err.println("Something wrong with " + patternName + " " + retCode);
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
				double rawValue = (double)outReal[0];
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
		
		Double lastValue = null;
		
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
				double rawValue = (double)outReal[0];
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
				double sma = (double)outSMA[0];
				double stddev = (double)outSTDDEV[0];
				double adjClose = m.getAdjClose();
				double boll = 0;
				if (stddev != 0) {
					boll = (adjClose - sma) / stddev;
				}
				double rawValue = boll;
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
				double rawValue = (double)outReal[0];
				double adjValue = (rawValue - m.getAdjClose()) / m.getAdjClose() * 100f;
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
				double rawValue = (double)outReal[0];
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
				double rawValue = (double)outSlowD[0];
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
				double rawValue = (double)outSlowK[0];
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
				double rawValue = (double)outFastD[0];
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
				double rawValue = (double)outFastK[0];
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
				double rawValue = (double)outReal[0];
				double adjClose = m.getAdjClose();
				double adjValue = (rawValue - adjClose) / adjClose * 100f * 10f;
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
		
		Double lastValue = null;
		
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
				double rawValue = (double)outReal[0];
				double adjClose = m.getAdjClose();
				double adjValue = (rawValue - adjClose) / adjClose * 100f * 10f;
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
				double rawValue = (double)outReal[0];
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
				double sma = (double)outSMA[0];
				double stddev = (double)outSTDDEV[0];
				double volume = (double)m.getVolume();
				double boll = 0;
				if (stddev != 0) {
					boll = (volume - sma) / stddev;
				}
				double rawValue = boll;
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
				double rawValue = (double)outReal[0];
				double adjValue = rawValue + 100;
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
		double lastAdjClose = -1f;
		int consecutiveUpMetrics = 0;
		
		for (Metric metric:ms) {
			double adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics++;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics = 0;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveUpMetrics = 0;
		  	}
			
			metric.value = (double)consecutiveUpMetrics;
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
		double lastAdjClose = -1f;
	    int consecutiveDownMetrics = 0;
		
		for (Metric metric:ms) {
			double adjClose = metric.getAdjClose();
			
			if (adjClose > lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics = 0;
		  	}
		  	else if (adjClose < lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics++;
		  	}
		  	else if (adjClose == lastAdjClose && lastAdjClose >= 0f) {
		  		consecutiveDownMetrics = 0;
		  	}

			metric.value = (double)consecutiveDownMetrics;
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
		double lastAdjClose = -1f;
		double startingClose = 0f;
		int consecutiveCount = 0;
		String consecutiveType = "";
		
		for (Metric metric:ms) {
			double adjClose = metric.getAdjClose();
			
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
			
			double perchange = 0f;
			if (consecutiveCount > 0) {
				perchange = (adjClose - startingClose) / startingClose * 100f;
			}
			
			metric.value = (double)perchange;
			metric.name = "cps";
			
		  	lastAdjClose = adjClose;
		}
	}
	
	public static void fillInDVOLdydx(ArrayList<Metric> metricSequence, int weight) { 
		// Initialize Variables
		double yesterdaysDVol = 0f;
	  	int c = 1;
	  	
	  	Double lastValue = null;
	  	for (Metric metric:metricSequence) {
	  		double adjClose = metric.getAdjClose();
	  		double adjOpen = metric.getAdjOpen();
	  		double adjHigh = metric.getAdjHigh();
	  		double adjLow = metric.getAdjLow();
	  		
	  		double todaysAvg = (adjClose + adjOpen + adjHigh + adjLow) / 4f;
	  		double todaysRange = adjHigh - adjLow;
	  		double todaysDVol = todaysRange / todaysAvg * 100f;
	  	
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
		LinkedList<Double> periodsAdjCloses = new LinkedList<Double>();
		
		Double lastValue = null;
		for (Metric metric:metricSequence) {
			double adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "mvol" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		double periodsAdjClosesSum = 0;
		  		for (Double p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		double averagePrice = periodsAdjClosesSum / (double)period;
		  		double sumOfDifferenceFromAverageSquares = 0;
		  		for (Double p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		double sd = (double)Math.sqrt(sumOfDifferenceFromAverageSquares / (double)period);
		  		double sdapodma = sd / averagePrice * 100;
		  		
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
		LinkedList<Double> closes = new LinkedList<Double>();

	  	for (Metric metric : ms) {
	  		double adjClose = metric.getAdjClose();
	  		if (closes.size() < period) {
	  			closes.add(adjClose);
	  			metric.value = null;
	  		}

	  		else if (closes.size() == period) {
	  			double highestClose = closes.getFirst();
	  			double lowestClose = closes.getFirst();
	  			for (Double close : closes) {
	  				if (close > highestClose) {
	  					highestClose = close;
	  				}
	  				if (close < lowestClose) {
	  					lowestClose = close;
	  				}
	  			}
	  			
	  			double periodRange = highestClose - lowestClose;
	  			double adjCloseFromHighestClose = highestClose - adjClose;
	  			double adjCloseFromLowestClose = adjClose - lowestClose;
	  			double rangePressure = .5f;
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

	  			if (rangePressure == Double.NEGATIVE_INFINITY || rangePressure == Double.POSITIVE_INFINITY) {
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
	 * Ranks the close as a percentile out of the last x periods 
	 * 
	 * @param ms
	 * @param period
	 */
	public static void fillInRangeRank(ArrayList<Metric> ms, int period) {
		LinkedList<Double> closes = new LinkedList<Double>();

	  	for (Metric metric : ms) {
	  		double adjClose = metric.getAdjClose();
	  		if (closes.size() < period) {
	  			closes.add(adjClose);
	  			metric.value = null;
	  		}

	  		else if (closes.size() == period) {
	  			int numAbove = 0;
	  			for (double close : closes) {
	  				if (adjClose > close) {
	  					numAbove++;
	  				}
	  			}
	  			double rank = 1 - (numAbove / (double)closes.size());

	  			metric.value = rank;
	  			
	  			// Toss the oldest, add the latest
	  			closes.remove();
	  			closes.add(adjClose);
	  		}

	  		metric.name = "rangerank" + period;
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
		double rangeStartingPrice = -1;
		double rangeHigh = 0;
		double rangeLow = 0;
		int numBarsInRange = 1;
		int metricCounter = 0;
		final int IGNORE_THE_FIRST_X_BARS = range * 75;
		
		for (Metric metric : ms) {
			metric.name = "timerange" + range;
			
			metricCounter++;
			if (rangeStartingPrice == -1) {
				rangeStartingPrice = metric.getAdjClose();
				rangeHigh = rangeStartingPrice;
				rangeLow = rangeStartingPrice;
				continue;
			}
			
			double open = metric.getAdjOpen();
			double close = metric.getAdjClose();
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
			
			double currentRange = rangeHigh - rangeLow;
			double currentRangeP = currentRange / rangeStartingPrice;
			if (currentRangeP > range / 1000f) {
				numBarsInRange = 1;
				rangeStartingPrice = close;
				rangeHigh = rangeStartingPrice;
				rangeLow = rangeStartingPrice;
			}
			
			double adjValue = (double)Math.sqrt(Math.sqrt(numBarsInRange)) - 1;
			
			if (metricCounter > IGNORE_THE_FIRST_X_BARS) {
				metric.value = adjValue;
			}
		}
	}
	
	/**
	 * The number of past bars that the closes have stayed within a range compared to the current close.
	 * Range comes in as thousandths.
	 * A range of 10 (translating to .01) would allow 1% total price movement.
	 * The value is the number of bars (average of last 15 * range) spent in that range.
	 * 
	 * @param ms
	 * @param range
	 */
	public static void fillInTimeRangeAlpha(ArrayList<Metric> ms, int range) {
		final int IGNORE_THE_FIRST_X_BARS = range * 75;
		final int AVERAGE_OVER = range * 15;
		
		LinkedList<Double> lastX = new LinkedList<Double>();

		for (int a = 0; a < ms.size(); a++) {
			Metric metric = ms.get(a);
			metric.name = "timerangealpha" + range;
			
			if (a > IGNORE_THE_FIRST_X_BARS) {
				double close = metric.getAdjClose();
				
				boolean inRange = true;
				int rangeCount = 1;
				while (inRange) { // Go back through the bars and count how many in a row the old closes were in range.
					if (a - rangeCount < 0) {
						break;
					}
					Metric pMetric = ms.get(a - rangeCount);
					double pMetricClose = pMetric.getAdjClose();

					double pRange = Math.abs(close - pMetricClose);
					double pRangePercent = pRange / close;
					if (pRangePercent >= (range / 1000f)) {
						break;
					}
					rangeCount++;
				}
				
				// Find the average number of bars
				if (lastX.size() >= AVERAGE_OVER) {
					lastX.removeFirst();
				}
				lastX.addLast((double)rangeCount);
				
				if (lastX.size() == AVERAGE_OVER) {
					double rangeTotal = 0;
					for (double r : lastX) {
						rangeTotal += r;
					}
					double averageOfLastX = rangeTotal / lastX.size();
					
					metric.value = averageOfLastX;
				}
			}
		}
	}

	/**********************************************************************************************
	 * OLD METRICS
	 **********************************************************************************************/
	
	public static void fillInPriceDMAs(ArrayList<Metric> ms, int period) {
		// Initialize Variables
		LinkedList<Double> periodsAdjCloses = new LinkedList<Double>();
		
		for (Metric metric:ms) {
			double adjClose = metric.getAdjClose();

			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "pricedma" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		double priceSum = 0;
		  		for (Double price:periodsAdjCloses) {
		  			priceSum += price;
		  		}
		  		double dma = priceSum / (double)period;
		  		
		  		// Set this day's DMA value and add it to the new sequence
		  		metric.value = dma;
		  		metric.name = "pricedma" + period;
		  		
		  		periodsAdjCloses.remove();
		  	}
		}
	}
	
	public static void fillInGapBoll(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Double> periodGPCs = new LinkedList<Double>();
		
		for (Metric metric:metricSequence) {
			double gap = metric.getGap();
			double adjOpen = metric.getAdjOpen();
			double gpc = gap / (adjOpen - gap) * 100f;

			if (periodGPCs.size() < (period - 1)) {
		  		periodGPCs.add(gpc);
		  		metric.value = null;
		  		metric.name = "gapboll" + period;
		  	}
		  	else {
		  		// DMA
		  		periodGPCs.add(gpc);
		  		double gpcSum = 0;
		  		for (Double thisGPC:periodGPCs) {
		  			gpcSum += thisGPC;
		  		}
		  		double dma = gpcSum / (double)period;
		  		
		  		// SD
		  		double gpcSum2 = 0;
		  		for (Double thisGPC:periodGPCs) {
		  			gpcSum2 += thisGPC;
		  		}
		  		double averageGPC = gpcSum2 / (double)period;
		  		double sumOfDifferenceFromAverageSquares = 0;
		  		for (Double thisGPC:periodGPCs) {
		  			sumOfDifferenceFromAverageSquares += ((thisGPC - averageGPC) * (thisGPC - averageGPC));
		  		}
		  		double sd = (double)Math.sqrt(sumOfDifferenceFromAverageSquares / (double)period);
		  		
		  		double boll = 0;
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
		LinkedList<Double> periodIDPCs = new LinkedList<Double>();
		
		for (Metric metric:metricSequence) {
			double gap = metric.getGap();
			double change = metric.getChange();
			double adjOpen = metric.getAdjOpen();
			double idpc = (change - gap) / (adjOpen - gap) * 100f;

			if (periodIDPCs.size() < (period - 1)) {
		  		periodIDPCs.add(idpc);
		  		metric.value = null;
		  		metric.name = "intradayboll" + period;
		  	}
		  	else {
		  		// DMA
		  		periodIDPCs.add(idpc);
		  		double idpcSum = 0;
		  		for (Double thisIDPC:periodIDPCs) {
		  			idpcSum += thisIDPC;
		  		}
		  		double dma = idpcSum / (double)period;
		  		
		  		// SD
		  		double idpcSum2 = 0;
		  		for (Double thisIDPC:periodIDPCs) {
		  			idpcSum2 += thisIDPC;
		  		}
		  		double averageIDPC = idpcSum2 / (double)period;
		  		double sumOfDifferenceFromAverageSquares = 0;
		  		for (Double thisIDPC:periodIDPCs) {
		  			sumOfDifferenceFromAverageSquares += ((thisIDPC - averageIDPC) * (thisIDPC - averageIDPC));
		  		}
		  		double sd = (double)Math.sqrt(sumOfDifferenceFromAverageSquares / (double)period);
		  		
		  		// 
		  		double boll = 0f;
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
		  		metric.value = (double)dma;
		  		metric.name = "volumedma" + period;
		  		
		  		periodsVolumes.remove();
		  	}
		}
	}
	
	public static void fillInPriceSDs(ArrayList<Metric> metricSequence, int period) {
		// Initialize Variables
		LinkedList<Double> periodsAdjCloses = new LinkedList<Double>();
		
		for (Metric metric:metricSequence) {
			double adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "pricesd" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		double periodsAdjClosesSum = 0;
		  		for (Double p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		double averagePrice = periodsAdjClosesSum / (double)period;
		  		double sumOfDifferenceFromAverageSquares = 0;
		  		for (Double p:periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		double sd = (double)Math.sqrt(sumOfDifferenceFromAverageSquares / (double)period);
		  		
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
	 * @param ms
	 * @param period
	 * @return
	 */
	public static void fillInMVOL(ArrayList<Metric> ms, int period) {
		// Initialize Variables
		LinkedList<Double> periodsAdjCloses = new LinkedList<Double>();
		
		for (Metric metric : ms) {
			double adjClose = metric.getAdjClose();
			
			if (periodsAdjCloses.size() < (period - 1)) {
		  		periodsAdjCloses.add(adjClose);
		  		metric.value = null;
		  		metric.name = "mvol" + period;
		  	}
		  	else {
		  		periodsAdjCloses.add(adjClose);
		  		double periodsAdjClosesSum = 0;
		  		for (Double p:periodsAdjCloses) {
		  			periodsAdjClosesSum += p;
		  		}
		  		double averagePrice = periodsAdjClosesSum / (double)period;
		  		double sumOfDifferenceFromAverageSquares = 0;
		  		for (Double p : periodsAdjCloses) {
		  			sumOfDifferenceFromAverageSquares += ((p - averagePrice) * (p - averagePrice));
		  		}
		  		double sd = (double)Math.sqrt(sumOfDifferenceFromAverageSquares / (double)period);
		  		double sdapodma = sd / averagePrice * 100;
		  		
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
		  		double sd = (double)Math.sqrt(sumOfDifferenceFromAverageSquares / (double)period);
		  		
		  		// Set this day's SD value and add it to the new sequence
		  		metric.value = sd;
		  		metric.name = "volumesd" + period;
		  		
		  		periodsVolumes.remove();
		  	}
		}
	}
	
	public static void fillInWeightedDVol(ArrayList<Metric> ms, int weight) { 
		// Initialize Variables
		double yesterdaysDVol = 0f;
	  	int c = 1;
	  	
	  	for (Metric metric : ms) {
	  		double adjClose = metric.getAdjClose();
	  		double adjOpen = metric.getAdjOpen();
	  		double adjHigh = metric.getAdjHigh();
	  		double adjLow = metric.getAdjLow();
	  		
	  		double todaysAvg = (adjClose + adjOpen + adjHigh + adjLow) / 4f;
	  		double todaysRange = adjHigh - adjLow;
	  		double todaysDVol = todaysRange / todaysAvg * 100f;
	  	
		  	if (c > 1) {
		  		todaysDVol = ((todaysDVol * weight / 100f) + (yesterdaysDVol * (1 - (weight / 100f))));
		  	}

		  	// Set this day's DVOL value and add it to the new sequence
		  	if (c >= (100 / (double)weight * 3)) {
			  	metric.value = todaysDVol;
		  	}
		  	metric.name = "dvol" + weight + "ema";
		  	
		  	yesterdaysDVol = todaysDVol;
		  	c++;
	  	}
	}
	
	public static void fillInBreakouts(ArrayList<Metric> ms, int period) { 
		// Initialize Variables
	  	LinkedList<Double> closes = new LinkedList<Double>();

	  	LinkedList<Double> lastX = new LinkedList<Double>();
	  	
	  	for (Metric metric : ms) {
	  		double adjClose = metric.getAdjClose();
	  		if (closes.size() < period) {
	  			closes.add(adjClose);
	  			metric.value = 0d;
	  		}

	  		else if (closes.size() == period) {
	  			double highestClose = closes.getFirst();
	  			double lowestClose = closes.getFirst();
	  			int numDaysSinceToday = 0;
	  			int highNumDaysSincePeriodStart = 0;
	  			int lowNumDaysSincePeriodStart = 0;
	  			for (Double close:closes) {
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
	  			
	  			double breakout = 0f;
	  			if (adjClose >= highestClose) {
	  				breakout = ((adjClose - highestClose) / highestClose * 100f) * (1 + ((period - highNumDaysSincePeriodStart) / 3f));
	  			}
	  			else if (adjClose <= lowestClose) {
	  				breakout = ((adjClose - lowestClose) / lowestClose * 100f) * (1 + ((period - lowNumDaysSincePeriodStart) / 3f));
	  			}
	  			// Normalize the results a bit to bunch them mostly in a -1 to 1 range
	  			double breakoutABS = Math.abs(breakout);
	  			double breakoutABSp1 = breakoutABS + 1;
	  			double sign = Math.signum(breakout);
	  			double log = (double)Math.log10(breakoutABSp1);
	  			if (log > 1) log = 1;
	  			double adjustedBreakout = log * sign;
	  		
	  			// Make it an average of the last *period*
	  			if (lastX.size() == period) {
					lastX.removeFirst();
				}
				lastX.addLast((double)adjustedBreakout);
				
				double adjustedBreakoutTotal = 0;
				for (double r : lastX) {
					adjustedBreakoutTotal += r;
				}
				double averageOfLastX = adjustedBreakoutTotal / lastX.size();
	  			
	  			metric.value = averageOfLastX;
	  			
	  			// Toss the oldest, add the latest
	  			closes.remove();
	  			closes.add(adjClose);
	  		}

	  		metric.name = "breakout" + period;
	  	}
	}
}
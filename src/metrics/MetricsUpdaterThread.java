package metrics;

import java.util.ArrayList;

import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.Metric;
import dbio.QueryManager;

public class MetricsUpdaterThread extends Thread {
	
	private boolean running = false;
	
	/**
	 * Parameters have to come in sets of 2.
	 * First is symbol
	 * Second is bar duration
	 * @param args
	 */
	public static void main (String[] args) {
		// Get params
		ArrayList<String[]> params = new ArrayList<String[]>();
		ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
		if (args != null) {
			for (int a = 0; a < args.length; a += 2) {
				String symbol = args[a];
				String duration = args[a + 1];

				String[] param = new String[2];
				param[0] = symbol;
				param[1] = duration;
				params.add(param);
				BarKey barKey = new BarKey(symbol, BAR_SIZE.valueOf(duration));
				barKeys.add(barKey);
			}
		}
		
		// What metrics do we want
		ArrayList<String> metrics = new ArrayList<String>(); // Constants.METRICS
		metrics.add("atr10");
		metrics.add("atr20");
		metrics.add("atr40");
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		try {
			int c = 1;
			ArrayList<Metric> ms = MetricSingleton.getInstance().getNextMetricSequence();
			while (ms != null && running) {
				String threadName = this.getName() + this.getId();
//				System.out.println("MetricsUpdateThread " + threadName + " working on " + c + " - " + ms.get(0).name);
				c++;
				switch (ms.get(0).name) {
					// ADO
					case "ado3_10":
						MetricFunctionUtil.fillInADO(ms, 3, 10);
						break;
					case "ado10_30":
						MetricFunctionUtil.fillInADO(ms, 10, 30);					
						break;
					case "ado30_100":
						MetricFunctionUtil.fillInADO(ms, 30, 100);					
						break;
						
					// ADO dydx
					case "adodydx10_30":
						MetricFunctionUtil.fillInADOdydx(ms, 10, 30);					
						break;
					case "adodydx30_100":
						MetricFunctionUtil.fillInADOdydx(ms, 30, 100);					
						break;
				
					// ADX
					case "adx3":
						MetricFunctionUtil.fillInADX(ms, 3);
						break;
					case "adx10":
						MetricFunctionUtil.fillInADX(ms, 10);					
						break;
					case "adx30":
						MetricFunctionUtil.fillInADX(ms, 30);					
						break;
					case "adx100":
						MetricFunctionUtil.fillInADX(ms, 100);					
						break;
						
					// ADX dydx
					case "adxdydx30":
						MetricFunctionUtil.fillInADXdydx(ms, 30);					
						break;
					case "adxdydx100":
						MetricFunctionUtil.fillInADXdydx(ms, 100);					
						break;
						
					// ADXR
					case "adxr3":
						MetricFunctionUtil.fillInADXR(ms, 3);
						break;
					case "adxr10":
						MetricFunctionUtil.fillInADXR(ms, 10);					
						break;
					case "adxr30":
						MetricFunctionUtil.fillInADXR(ms, 30);					
						break;
					case "adxr100":
						MetricFunctionUtil.fillInADXR(ms, 100);					
						break;
						
					// ADXR dydx
					case "adxrdydx30":
						MetricFunctionUtil.fillInADXRdydx(ms, 30);					
						break;
					case "adxrdydx100":
						MetricFunctionUtil.fillInADXRdydx(ms, 100);					
						break;

					// CMO
					case "cmo3":
						MetricFunctionUtil.fillInCMO(ms, 3);
						break;
					case "cmo10":
						MetricFunctionUtil.fillInCMO(ms, 10);					
						break;
					case "cmo30":
						MetricFunctionUtil.fillInCMO(ms, 30);					
						break;
					case "cmo100":
						MetricFunctionUtil.fillInCMO(ms, 100);					
						break;
						
					// PPO
					case "ppo3_10":
						MetricFunctionUtil.fillInPPO(ms, 3, 10);
						break;
					case "ppo10_30":
						MetricFunctionUtil.fillInPPO(ms, 10, 30);					
						break;
					case "ppo30_100":
						MetricFunctionUtil.fillInPPO(ms, 30, 100);					
						break;
					
					// PPO dydx
					case "ppodydx10_30":
						MetricFunctionUtil.fillInPPOdydx(ms, 10, 30);					
						break;
					case "ppodydx30_100":
						MetricFunctionUtil.fillInPPOdydx(ms, 30, 100);					
						break;
						
					// RSI
					case "rsi2":
						MetricFunctionUtil.fillInRSI(ms, 2);
						break;
					case "rsi5":
						MetricFunctionUtil.fillInRSI(ms, 5);					
						break;
					case "rsi10":
						MetricFunctionUtil.fillInRSI(ms, 10);					
						break;
					case "rsi14":
						MetricFunctionUtil.fillInRSI(ms, 14);
						break;
					case "rsi25":
						MetricFunctionUtil.fillInRSI(ms, 25);
						break;
					case "rsi40":
						MetricFunctionUtil.fillInRSI(ms, 40);					
						break;
					
					// MFI
					case "mfi4":
						MetricFunctionUtil.fillInMFI(ms, 4);
						break;
					case "mfi8":
						MetricFunctionUtil.fillInMFI(ms, 8);
						break;
					case "mfi12":
						MetricFunctionUtil.fillInMFI(ms, 12);					
						break;
					case "mfi16":
						MetricFunctionUtil.fillInMFI(ms, 16);
						break;
					case "mfi30":
						MetricFunctionUtil.fillInMFI(ms, 30);
						break;
					case "mfi60":
						MetricFunctionUtil.fillInMFI(ms, 60);
						break;
						
					// Consecutive Bars
					case "consecutiveups":
						MetricFunctionUtil.fillInConsecutiveUps(ms);
						break;
					case "consecutivedowns":
						MetricFunctionUtil.fillInConsecutiveDowns(ms);
						break;
						
					// Consecutive Percents
					case "cps":
						MetricFunctionUtil.fillInCPS(ms);
						break;
						
					// Intraday Boll
					case "intradayboll10":
						MetricFunctionUtil.fillInIntradayBoll(ms, 10);
						break;
					case "intradayboll20":
						MetricFunctionUtil.fillInIntradayBoll(ms, 20);
						break;
					case "intradayboll50":
						MetricFunctionUtil.fillInIntradayBoll(ms, 50);
						break;
						
					// CCI
					case "cci5":
						MetricFunctionUtil.fillInCCI(ms, 5);
						break;
					case "cci10":
						MetricFunctionUtil.fillInCCI(ms, 10);
						break;
					case "cci20":
						MetricFunctionUtil.fillInCCI(ms, 20);					
						break;
					case "cci40":	
						MetricFunctionUtil.fillInCCI(ms, 40);
						break;
					case "cci60":	
						MetricFunctionUtil.fillInCCI(ms, 60);
						break;
					
					// DVOL
					case "dvol5ema":
						MetricFunctionUtil.fillInWeightedDVol(ms, 5);
						break;
					case "dvol10ema":
						MetricFunctionUtil.fillInWeightedDVol(ms, 10);
						break;
					case "dvol25ema":
						MetricFunctionUtil.fillInWeightedDVol(ms, 25);
						break;
					case "dvol50ema":
						MetricFunctionUtil.fillInWeightedDVol(ms, 50);
						break;
					case "dvol75ema":
						MetricFunctionUtil.fillInWeightedDVol(ms, 75);
						break;
					
					// DVOL dydx
					case "dvoldydx5ema":
						MetricFunctionUtil.fillInWeightedDVoldydx(ms, 5);
						break;
					case "dvoldydx10ema":
						MetricFunctionUtil.fillInWeightedDVoldydx(ms, 10);
						break;
						
					// Breakout
					case "breakout10":
						MetricFunctionUtil.fillInBreakouts(ms, 10);
						break;
					case "breakout20":
						MetricFunctionUtil.fillInBreakouts(ms, 20);
						break;
					case "breakout50":
						MetricFunctionUtil.fillInBreakouts(ms, 50);
						break;
					case "breakout100":
						MetricFunctionUtil.fillInBreakouts(ms, 100);
						break;
					case "breakout200":
						MetricFunctionUtil.fillInBreakouts(ms, 200);
						break;
						
					// Williams R
					case "williamsr10":
						MetricFunctionUtil.fillInWilliamsR(ms, 10);
						break;
					case "williamsr20":
						MetricFunctionUtil.fillInWilliamsR(ms, 20);					
						break;
					case "williamsr50":
						MetricFunctionUtil.fillInWilliamsR(ms, 50);
						break;	
						
					// PSAR
					case "psar":
						MetricFunctionUtil.fillInPSAR(ms);
						break;
						
					// Ultimate
					case "ultimateoscillator4_10_25":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 4, 10, 25);
						break;
					case "ultimateoscillator8_20_50":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 8, 20, 50);
						break;
					case "ultimateoscillator16_40_100":
						MetricFunctionUtil.fillInUltimateOscillator(ms, 16, 40, 100);
						break;
					
					// Aroon
					case "aroonoscillator10":
						MetricFunctionUtil.fillInAroonOscillator(ms, 10);
						break;
					case "aroonoscillator25":
						MetricFunctionUtil.fillInAroonOscillator(ms, 25);
						break;
					case "aroonoscillator50":
						MetricFunctionUtil.fillInAroonOscillator(ms, 50);
						break;
					
					// Price Boll using SMA
					case "pricebolls10":
						MetricFunctionUtil.fillInPriceBollS(ms, 10);
						break;
					case "pricebolls20":
						MetricFunctionUtil.fillInPriceBollS(ms, 20);
						break;
					case "pricebolls50":
						MetricFunctionUtil.fillInPriceBollS(ms, 50);					
						break;
					case "pricebolls100":
						MetricFunctionUtil.fillInPriceBollS(ms, 100);
						break;	
					case "pricebolls200":
						MetricFunctionUtil.fillInPriceBollS(ms, 200);
						break;	
						
					// Volume Boll using SMA
					case "volumebolls10":
						MetricFunctionUtil.fillInVolumeBollS(ms, 10);
						break;
					case "volumebolls20":
						MetricFunctionUtil.fillInVolumeBollS(ms, 20);
						break;
					case "volumebolls50":
						MetricFunctionUtil.fillInVolumeBollS(ms, 50);					
						break;
					case "volumebolls100":
						MetricFunctionUtil.fillInVolumeBollS(ms, 100);
						break;
					case "volumebolls200":
						MetricFunctionUtil.fillInVolumeBollS(ms, 200);
						break;
	
					// MACD
					case "macd6_13_5":
						MetricFunctionUtil.fillInMACD(ms, 6, 13, 5);
						break;
					case "macd12_26_9":
						MetricFunctionUtil.fillInMACD(ms, 12, 26, 9);
						break;
					case "macd20_40_9":
						MetricFunctionUtil.fillInMACD(ms, 20, 40, 9);					
						break;
	
					// MACD Signal
					case "macdsignal6_13_5":
						MetricFunctionUtil.fillInMACDSignal(ms, 6, 13, 5);
						break;
					case "macdsignal12_26_9":
						MetricFunctionUtil.fillInMACDSignal(ms, 12, 26, 9);
						break;
					case "macdsignal20_40_9":
						MetricFunctionUtil.fillInMACDSignal(ms, 20, 40, 9);					
						break;
	
					// MACD History
					case "macdhistory6_13_5":
						MetricFunctionUtil.fillInMACDHistory(ms, 6, 13, 5);
						break;
					case "macdhistory12_26_9":
						MetricFunctionUtil.fillInMACDHistory(ms, 12, 26, 9);
						break;
					case "macdhistory20_40_9":
						MetricFunctionUtil.fillInMACDHistory(ms, 20, 40, 9);							
						break;
						
					// Time Series Forecast
					case "tsf10":
						MetricFunctionUtil.fillInTSF(ms, 10);
						break;
					case "tsf20":
						MetricFunctionUtil.fillInTSF(ms, 20);
						break;
					case "tsf30":
						MetricFunctionUtil.fillInTSF(ms, 30);
						break;	
					case "tsf40":
						MetricFunctionUtil.fillInTSF(ms, 40);
						break;
					case "tsf60":
						MetricFunctionUtil.fillInTSF(ms, 60);
						break;
						
					// Time Series Forecast dydx
					case "tsfdydx40":
						MetricFunctionUtil.fillInTSFdydx(ms, 40);
						break;
					case "tsfdydx60":
						MetricFunctionUtil.fillInTSFdydx(ms, 60);
						break;
						
					// Stochastic RSI
					case "stochasticdrsi9_2_2":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 9, 2, 2);
						break;
					case "stochasticdrsi14_3_3":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 14, 3, 3);
						break;
					case "stochasticdrsi20_5_5":
						MetricFunctionUtil.fillInStochasticDRSI(ms, 20, 5, 5);
						break;
						
					// Stochastic
					case "stochasticd9_2_2":
						MetricFunctionUtil.fillInStochasticD(ms, 9, 2, 2);
						break;
					case "stochastick9_2_2":
						MetricFunctionUtil.fillInStochasticK(ms, 9, 2, 2);
						break;	
					case "stochasticd14_3_3":
						MetricFunctionUtil.fillInStochasticD(ms, 14, 3, 3);
						break;
					case "stochastick14_3_3":
						MetricFunctionUtil.fillInStochasticK(ms, 14, 3, 3);
						break;	
					case "stochasticd20_5_5":
						MetricFunctionUtil.fillInStochasticD(ms, 20, 5, 5);
						break;
					case "stochastick20_5_5":
						MetricFunctionUtil.fillInStochasticK(ms, 20, 5, 5);
						break;	
						
					// Average True Range
					case "atr10":
						MetricFunctionUtil.fillInATR(ms, 10);
						break;
					case "atr20":
						MetricFunctionUtil.fillInATR(ms, 20);
						break;		
					case "atr40":
						MetricFunctionUtil.fillInATR(ms, 40);
						break;	
					case "atr60":
						MetricFunctionUtil.fillInATR(ms, 60);
						break;	
						
					// Average True Range DYDX
					case "atrdydx40":
						MetricFunctionUtil.fillInATRdydx(ms, 40);
						break;	
					case "atrdydx60":
						MetricFunctionUtil.fillInATRdydx(ms, 60);
						break;	
						
					// MVOL
					case "mvol10":
						MetricFunctionUtil.fillInMVOL(ms, 10);
						break;
					case "mvol20":
						MetricFunctionUtil.fillInMVOL(ms, 20);
						break;
					case "mvol50":
						MetricFunctionUtil.fillInMVOL(ms, 50);
						break;
					case "mvol100":
						MetricFunctionUtil.fillInMVOL(ms, 100);
						break;
					case "mvol200":
						MetricFunctionUtil.fillInMVOL(ms, 200);
						break;
						
					// MVOL dydx
					case "mvoldydx100":
						MetricFunctionUtil.fillInMVOLdydx(ms, 100);
						break;
					case "mvoldydx200":
						MetricFunctionUtil.fillInMVOLdydx(ms, 200);
						break;
						
					// Candlestick Patterns
					case "cdlhammer":
						MetricFunctionUtil.fillInPattern(ms, "cdlhammer");
						break;
					case "cdldoji":
						MetricFunctionUtil.fillInPattern(ms, "cdldoji");
						break;
					case "cdlmorningstar":
						MetricFunctionUtil.fillInPattern(ms, "cdlmorningstar");
						break;
						
					// Time Range
					case "timerange2":
						MetricFunctionUtil.fillInTimeRange(ms, 2);
						break;
					case "timerange5":
						MetricFunctionUtil.fillInTimeRange(ms, 5);
						break;
					case "timerange8":
						MetricFunctionUtil.fillInTimeRange(ms, 8);
						break;
					case "timerange13":
						MetricFunctionUtil.fillInTimeRange(ms, 13);
						break;
					case "timerange20":
						MetricFunctionUtil.fillInTimeRange(ms, 20);
						break;
						
					// Time Range Alpha
					case "timerangealpha2":
						MetricFunctionUtil.fillInTimeRangeAlpha(ms, 2);
						break;
					case "timerangealpha5":
						MetricFunctionUtil.fillInTimeRangeAlpha(ms, 5);
						break;
					case "timerangealpha8":
						MetricFunctionUtil.fillInTimeRangeAlpha(ms, 8);
						break;
					case "timerangealpha13":
						MetricFunctionUtil.fillInTimeRangeAlpha(ms, 13);
						break;
					case "timerangealpha20":
						MetricFunctionUtil.fillInTimeRangeAlpha(ms, 20);
						break;
						
					// Range Pressure
					case "rangepressure50":
						MetricFunctionUtil.fillInRangePressure(ms, 50);
						break;		
					case "rangepressure100":
						MetricFunctionUtil.fillInRangePressure(ms, 100);
						break;	
					case "rangepressure200":
						MetricFunctionUtil.fillInRangePressure(ms, 200);
						break;	
					case "rangepressure500":
						MetricFunctionUtil.fillInRangePressure(ms, 500);
						break;	
				}
				
				QueryManager.insertOrUpdateIntoMetrics(ms);
				
				if (running) {
					ms = MetricSingleton.getInstance().getNextMetricSequence();
				}
			}
//			System.out.println("This thread did " + c + " metrics");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
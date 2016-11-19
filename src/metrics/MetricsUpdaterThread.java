package metrics;

import java.util.ArrayList;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.Metric;
import dbio.QueryManager;

public class MetricsUpdaterThread extends Thread {

	private boolean running = false;

	/**
	 * Parameters have to come in sets of 2. First is symbol Second is bar
	 * duration
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
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
//			int c = 1;
			ArrayList<Metric> ms = MetricSingleton.getInstance().getNextMetricSequence();
			while (ms != null && running) {
//				boolean chronologicalCheck = metricSequenceChronologicalCheck(ms);
//				if (chronologicalCheck == false) {
//					System.err.println("METRIC SEQUENCE IN MetricsUpdaterThread IS NOT IN ORDER!!!");
//				}
//				String threadName = this.getName() + this.getId();
//				System.out.println("MetricsUpdateThread " + threadName + " working on " + c + " - " + ms.get(0).name);
//				c++;
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
				case "ado100_300":
					MetricFunctionUtil.fillInADO(ms, 100, 300);
					break;

				// ADO dydx
				case "adodydx10_30":
					MetricFunctionUtil.fillInADOdydx(ms, 10, 30);
					break;
				case "adodydx30_100":
					MetricFunctionUtil.fillInADOdydx(ms, 30, 100);
					break;
				case "adodydx100_300":
					MetricFunctionUtil.fillInADOdydx(ms, 100, 300);
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
				case "adx300":
					MetricFunctionUtil.fillInADX(ms, 300);
					break;
				case "adx1000":
					MetricFunctionUtil.fillInADX(ms, 1000);
					break;


				// ADX dydx
				case "adxdydx30":
					MetricFunctionUtil.fillInADXdydx(ms, 30);
					break;
				case "adxdydx100":
					MetricFunctionUtil.fillInADXdydx(ms, 100);
					break;
				case "adxdydx300":
					MetricFunctionUtil.fillInADXdydx(ms, 300);
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
				case "adxr300":
					MetricFunctionUtil.fillInADXR(ms, 300);
					break;
				case "adxr1000":
					MetricFunctionUtil.fillInADXR(ms, 1000);
					break;	

				// ADXR dydx
				case "adxrdydx30":
					MetricFunctionUtil.fillInADXRdydx(ms, 30);
					break;
				case "adxrdydx100":
					MetricFunctionUtil.fillInADXRdydx(ms, 100);
					break;
				case "adxrdydx300":
					MetricFunctionUtil.fillInADXRdydx(ms, 300);
					break;

				// CMO
				case "cmo2":
					MetricFunctionUtil.fillInCMO(ms, 2);
					break;
				case "cmo3":
					MetricFunctionUtil.fillInCMO(ms, 3);
					break;
				case "cmo5":
					MetricFunctionUtil.fillInCMO(ms, 5);
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
				case "cmo300":
					MetricFunctionUtil.fillInCMO(ms, 300);
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
				case "ppo100_300":
					MetricFunctionUtil.fillInPPO(ms, 100, 300);
					break;
				case "ppo300_1000":
					MetricFunctionUtil.fillInPPO(ms, 300, 1000);
					break;

				// PPO dydx
				case "ppodydx10_30":
					MetricFunctionUtil.fillInPPOdydx(ms, 10, 30);
					break;
				case "ppodydx30_100":
					MetricFunctionUtil.fillInPPOdydx(ms, 30, 100);
					break;
				case "ppodydx100_300":
					MetricFunctionUtil.fillInPPOdydx(ms, 100, 300);
					break;

				// RSI
				case "rsi2":
					MetricFunctionUtil.fillInRSI(ms, 2);
					break;
				case "rsi3":
					MetricFunctionUtil.fillInRSI(ms, 3);
					break;
				case "rsi5":
					MetricFunctionUtil.fillInRSI(ms, 5);
					break;
				case "rsi10":
					MetricFunctionUtil.fillInRSI(ms, 10);
					break;
				case "rsi30":
					MetricFunctionUtil.fillInRSI(ms, 30);
					break;
				case "rsi100":
					MetricFunctionUtil.fillInRSI(ms, 100);
					break;
				case "rsi300":
					MetricFunctionUtil.fillInRSI(ms, 300);
					break;

				// MFI
				case "mfi3":
					MetricFunctionUtil.fillInMFI(ms, 3);
					break;
				case "mfi10":
					MetricFunctionUtil.fillInMFI(ms, 10);
					break;
				case "mfi30":
					MetricFunctionUtil.fillInMFI(ms, 30);
					break;
				case "mfi100":
					MetricFunctionUtil.fillInMFI(ms, 100);
					break;
				case "mfi300":
					MetricFunctionUtil.fillInMFI(ms, 300);
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
				case "intradayboll3":
					MetricFunctionUtil.fillInIntradayBoll(ms, 3);
					break;
				case "intradayboll10":
					MetricFunctionUtil.fillInIntradayBoll(ms, 10);
					break;
				case "intradayboll30":
					MetricFunctionUtil.fillInIntradayBoll(ms, 30);
					break;
				case "intradayboll100":
					MetricFunctionUtil.fillInIntradayBoll(ms, 100);
					break;
				case "intradayboll300":
					MetricFunctionUtil.fillInIntradayBoll(ms, 300);
					break;
				case "intradayboll1000":
					MetricFunctionUtil.fillInIntradayBoll(ms, 1000);
					break;
				case "intradayboll3000":
					MetricFunctionUtil.fillInIntradayBoll(ms, 3000);
					break;

				// CCI
				case "cci2":
					MetricFunctionUtil.fillInCCI(ms, 2);
					break;
				case "cci3":
					MetricFunctionUtil.fillInCCI(ms, 3);
					break;
				case "cci5":
					MetricFunctionUtil.fillInCCI(ms, 5);
					break;
				case "cci10":
					MetricFunctionUtil.fillInCCI(ms, 10);
					break;
				case "cci30":
					MetricFunctionUtil.fillInCCI(ms, 30);
					break;
				case "cci100":
					MetricFunctionUtil.fillInCCI(ms, 100);
					break;
				case "cci300":
					MetricFunctionUtil.fillInCCI(ms, 300);
					break;

				// DVOL
				case "dvol1ema":
					MetricFunctionUtil.fillInWeightedDVol(ms, 1);
					break;		
				case "dvol2ema":
					MetricFunctionUtil.fillInWeightedDVol(ms, 2);
					break;	
				case "dvol3ema":
					MetricFunctionUtil.fillInWeightedDVol(ms, 3);
					break;
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
				case "dvoldydx2ema":
					MetricFunctionUtil.fillInDVOLdydx(ms, 2);
					break;
				case "dvoldydx3ema":
					MetricFunctionUtil.fillInDVOLdydx(ms, 3);
					break;
				case "dvoldydx5ema":
					MetricFunctionUtil.fillInDVOLdydx(ms, 5);
					break;
				
				// Breakout
				case "breakout30":
					MetricFunctionUtil.fillInBreakouts(ms, 30);
					break;
				case "breakout100":
					MetricFunctionUtil.fillInBreakouts(ms, 100);
					break;
				case "breakout300":
					MetricFunctionUtil.fillInBreakouts(ms, 300);
					break;

				// Williams R
				case "williamsr2":
					MetricFunctionUtil.fillInWilliamsR(ms, 2);
					break;
				case "williamsr3":
					MetricFunctionUtil.fillInWilliamsR(ms, 3);
					break;
				case "williamsr5":
					MetricFunctionUtil.fillInWilliamsR(ms, 5);
					break;
				case "williamsr10":
					MetricFunctionUtil.fillInWilliamsR(ms, 10);
					break;
				case "williamsr30":
					MetricFunctionUtil.fillInWilliamsR(ms, 30);
					break;
				case "williamsr100":
					MetricFunctionUtil.fillInWilliamsR(ms, 100);
					break;
				case "williamsr300":
					MetricFunctionUtil.fillInWilliamsR(ms, 300);
					break;

				// PSAR
				case "psar":
					MetricFunctionUtil.fillInPSAR(ms);
					break;

				// Ultimate
				case "uo1_4_10":
					MetricFunctionUtil.fillInUltimateOscillator(ms, 1, 4, 10);
					break;
				case "uo2_7_20":
					MetricFunctionUtil.fillInUltimateOscillator(ms, 2, 7, 20);
					break;
				case "uo3_10_30":
					MetricFunctionUtil.fillInUltimateOscillator(ms, 3, 10, 30);
					break;
				case "uo10_30_100":
					MetricFunctionUtil.fillInUltimateOscillator(ms, 10, 30, 100);
					break;
				case "uo30_100_300":
					MetricFunctionUtil.fillInUltimateOscillator(ms, 30, 100, 300);
					break;

				// Aroon Oscillattor
				case "aroonoscillator3":
					MetricFunctionUtil.fillInAroonOscillator(ms, 3);
					break;
				case "aroonoscillator10":
					MetricFunctionUtil.fillInAroonOscillator(ms, 10);
					break;
				case "aroonoscillator30":
					MetricFunctionUtil.fillInAroonOscillator(ms, 30);
					break;
				case "aroonoscillator100":
					MetricFunctionUtil.fillInAroonOscillator(ms, 100);
					break;
				case "aroonoscillator300":
					MetricFunctionUtil.fillInAroonOscillator(ms, 300);
					break;
					
				// Aroon Up
				case "aroonup3":
					MetricFunctionUtil.fillInAroonUp(ms, 3);
					break;
				case "aroonup10":
					MetricFunctionUtil.fillInAroonUp(ms, 10);
					break;
				case "aroonup30":
					MetricFunctionUtil.fillInAroonUp(ms, 30);
					break;
				case "aroonup100":
					MetricFunctionUtil.fillInAroonUp(ms, 100);
					break;
				case "aroonup300":
					MetricFunctionUtil.fillInAroonUp(ms, 300);
					break;
					
				// Aroon Down
				case "aroondown3":
					MetricFunctionUtil.fillInAroonDown(ms, 3);
					break;
				case "aroondown10":
					MetricFunctionUtil.fillInAroonDown(ms, 10);
					break;
				case "aroondown30":
					MetricFunctionUtil.fillInAroonDown(ms, 30);
					break;
				case "aroondown100":
					MetricFunctionUtil.fillInAroonDown(ms, 100);
					break;
				case "aroondown300":
					MetricFunctionUtil.fillInAroonDown(ms, 300);
					break;

				// Price Boll using SMA
				case "pricebolls2":
					MetricFunctionUtil.fillInPriceBolls(ms, 2);
					break;
				case "pricebolls3":
					MetricFunctionUtil.fillInPriceBolls(ms, 3);
					break;
				case "pricebolls5":
					MetricFunctionUtil.fillInPriceBolls(ms, 5);
					break;
				case "pricebolls10":
					MetricFunctionUtil.fillInPriceBolls(ms, 10);
					break;
				case "pricebolls30":
					MetricFunctionUtil.fillInPriceBolls(ms, 30);
					break;
				case "pricebolls100":
					MetricFunctionUtil.fillInPriceBolls(ms, 100);
					break;
				case "pricebolls300":
					MetricFunctionUtil.fillInPriceBolls(ms, 300);
					break;

				// Volume Boll using SMA
				case "volumebolls3":
					MetricFunctionUtil.fillInVolumeBolls(ms, 3);
					break;
				case "volumebolls10":
					MetricFunctionUtil.fillInVolumeBolls(ms, 10);
					break;
				case "volumebolls30":
					MetricFunctionUtil.fillInVolumeBolls(ms, 30);
					break;
				case "volumebolls100":
					MetricFunctionUtil.fillInVolumeBolls(ms, 100);
					break;
				case "volumebolls300":
					MetricFunctionUtil.fillInVolumeBolls(ms, 300);
					break;

				// MACD
				case "macd10_30_8":
					MetricFunctionUtil.fillInMACD(ms, 10, 30, 8);
					break;
				case "macd30_100_24":
					MetricFunctionUtil.fillInMACD(ms, 30, 100, 24);
					break;
				case "macd100_300_80":
					MetricFunctionUtil.fillInMACD(ms, 100, 300, 80);
					break;
				case "macd300_1000_240":
					MetricFunctionUtil.fillInMACD(ms, 300, 1000, 240);
					break;

				// MACD Signal
				case "macds10_30_8":
					MetricFunctionUtil.fillInMACDSignal(ms, 10, 30, 8);
					break;
				case "macds30_100_24":
					MetricFunctionUtil.fillInMACDSignal(ms, 30, 100, 24);
					break;
				case "macds100_300_80":
					MetricFunctionUtil.fillInMACDSignal(ms, 100, 300, 80);
					break;
				case "macds300_1000_240":
					MetricFunctionUtil.fillInMACDSignal(ms, 300, 1000, 240);
					break;

				// MACD History
				case "macdh10_30_8":
					MetricFunctionUtil.fillInMACDHistory(ms, 10, 30, 8);
					break;
				case "macdh30_100_24":
					MetricFunctionUtil.fillInMACDHistory(ms, 30, 100, 24);
					break;
				case "macdh100_300_80":
					MetricFunctionUtil.fillInMACDHistory(ms, 100, 300, 80);
					break;
				case "macdh300_1000_240":
					MetricFunctionUtil.fillInMACDHistory(ms, 300, 1000, 240);
					break;	

				// Time Series Forecast
				case "tsf3":
					MetricFunctionUtil.fillInTSF(ms, 3);
					break;
				case "tsf10":
					MetricFunctionUtil.fillInTSF(ms, 10);
					break;
				case "tsf30":
					MetricFunctionUtil.fillInTSF(ms, 30);
					break;
				case "tsf100":
					MetricFunctionUtil.fillInTSF(ms, 100);
					break;
				case "tsf300":
					MetricFunctionUtil.fillInTSF(ms, 300);
					break;
				case "tsf1000":
					MetricFunctionUtil.fillInTSF(ms, 1000);
					break;

				// Time Series Forecast dydx
				case "tsfdydx30":
					MetricFunctionUtil.fillInTSFdydx(ms, 30);
					break;
				case "tsfdydx100":
					MetricFunctionUtil.fillInTSFdydx(ms, 100);
					break;
				case "tsfdydx300":
					MetricFunctionUtil.fillInTSFdydx(ms, 300);
					break;
				case "tsfdydx1000":
					MetricFunctionUtil.fillInTSFdydx(ms, 1000);
					break;

				// Stochastic D RSI
				case "stodrsi7_2_2":
					MetricFunctionUtil.fillInStochasticDRSI(ms, 7, 2, 2);
					break;
				case "stodrsi10_3_3":
					MetricFunctionUtil.fillInStochasticDRSI(ms, 10, 3, 3);
					break;
				case "stodrsi30_10_10":
					MetricFunctionUtil.fillInStochasticDRSI(ms, 30, 10, 10);
					break;
				case "stodrsi100_30_30":
					MetricFunctionUtil.fillInStochasticDRSI(ms, 100, 30, 30);
					break;
				case "stodrsi300_100_100":
					MetricFunctionUtil.fillInStochasticDRSI(ms, 300, 100, 100);
					break;
					
				// Stochastic K RSI
				case "stokrsi7_2_2":
					MetricFunctionUtil.fillInStochasticKRSI(ms, 7, 2, 2);
					break;
				case "stokrsi10_3_3":
					MetricFunctionUtil.fillInStochasticKRSI(ms, 10, 3, 3);
					break;
				case "stokrsi30_10_10":
					MetricFunctionUtil.fillInStochasticKRSI(ms, 30, 10, 10);
					break;
				case "stokrsi100_30_30":
					MetricFunctionUtil.fillInStochasticKRSI(ms, 100, 30, 30);
					break;
				case "stokrsi300_100_100":
					MetricFunctionUtil.fillInStochasticKRSI(ms, 300, 100, 100);
					break;

				// Stochastic D
				case "stod7_2_2":
					MetricFunctionUtil.fillInStochasticD(ms, 7, 2, 2);
					break;
				case "stod10_3_3":
					MetricFunctionUtil.fillInStochasticD(ms, 10, 3, 3);
					break;
				case "stod30_10_10":
					MetricFunctionUtil.fillInStochasticD(ms, 30, 10, 10);
					break;
				case "stod100_30_30":
					MetricFunctionUtil.fillInStochasticD(ms, 100, 30, 30);
					break;
				case "stod300_100_100":
					MetricFunctionUtil.fillInStochasticD(ms, 300, 100, 100);
					break;
				
				// Stochastic K
				case "stok7_2_2":
					MetricFunctionUtil.fillInStochasticK(ms, 7, 2, 2);
					break;
				case "stok10_3_3":
					MetricFunctionUtil.fillInStochasticK(ms, 10, 3, 3);
					break;
				case "stok30_10_10":
					MetricFunctionUtil.fillInStochasticK(ms, 30, 10, 10);
					break;
				case "stok100_30_30":
					MetricFunctionUtil.fillInStochasticK(ms, 100, 30, 30);
					break;
				case "stok300_100_100":
					MetricFunctionUtil.fillInStochasticK(ms, 300, 100, 100);
					break;
					
				// Average True Range
				case "atr3":
					MetricFunctionUtil.fillInATR(ms, 3);
					break;
				case "atr10":
					MetricFunctionUtil.fillInATR(ms, 10);
					break;
				case "atr30":
					MetricFunctionUtil.fillInATR(ms, 30);
					break;
				case "atr100":
					MetricFunctionUtil.fillInATR(ms, 100);
					break;
				case "atr300":
					MetricFunctionUtil.fillInATR(ms, 300);
					break;
				case "atr1000":
					MetricFunctionUtil.fillInATR(ms, 1000);
					break;

				// Average True Range DYDX
				case "atrdydx30":
					MetricFunctionUtil.fillInATRdydx(ms, 30);
					break;	
				case "atrdydx100":
					MetricFunctionUtil.fillInATRdydx(ms, 100);
					break;
				case "atrdydx300":
					MetricFunctionUtil.fillInATRdydx(ms, 300);
					break;

				// MVOL
				case "mvol3":
					MetricFunctionUtil.fillInMVOL(ms, 3);
					break;
				case "mvol10":
					MetricFunctionUtil.fillInMVOL(ms, 10);
					break;
				case "mvol30":
					MetricFunctionUtil.fillInMVOL(ms, 30);
					break;
				case "mvol100":
					MetricFunctionUtil.fillInMVOL(ms, 100);
					break;
				case "mvol300":
					MetricFunctionUtil.fillInMVOL(ms, 300);
					break;
				case "mvol1000":
					MetricFunctionUtil.fillInMVOL(ms, 1000);
					break;
				case "mvol3000":
					MetricFunctionUtil.fillInMVOL(ms, 3000);
					break;

				// MVOL dydx
				case "mvoldydx300":
					MetricFunctionUtil.fillInMVOLdydx(ms, 300);
					break;
				case "mvoldydx1000":
					MetricFunctionUtil.fillInMVOLdydx(ms, 1000);
					break;

				// Candlestick Patterns
				case "cdl2crows":
					MetricFunctionUtil.fillInPattern(ms, "cdl2crows");
					break;
				case "cdl3blackcrows":
					MetricFunctionUtil.fillInPattern(ms, "cdl3blackcrows");
					break;
				case "cdl3inside":
					MetricFunctionUtil.fillInPattern(ms, "cdl3inside");
					break;
				case "cdl3linestrike":
					MetricFunctionUtil.fillInPattern(ms, "cdl3linestrike");
					break;
				case "cdl3outside":
					MetricFunctionUtil.fillInPattern(ms, "cdl3outside");
					break;
				case "cdl3starsinsouth":
					MetricFunctionUtil.fillInPattern(ms, "cdl3starsinsouth");
					break;	
				case "cdl3whitesoldiers":
					MetricFunctionUtil.fillInPattern(ms, "cdl3whitesoldiers");
					break;	
				case "cdlabandonedbaby":
					MetricFunctionUtil.fillInPattern(ms, "cdlabandonedbaby");
					break;		
				case "cdladvanceblock":
					MetricFunctionUtil.fillInPattern(ms, "cdladvanceblock");
					break;
				case "cdlbelthold":
					MetricFunctionUtil.fillInPattern(ms, "cdlbelthold");
					break;
				case "cdlbreakaway":
					MetricFunctionUtil.fillInPattern(ms, "cdlbreakaway");
					break;
				case "cdlclosingmarubozu":
					MetricFunctionUtil.fillInPattern(ms, "cdlclosingmarubozu");
					break;
				case "cdlconcealbabyswall":
					MetricFunctionUtil.fillInPattern(ms, "cdlconcealbabyswall");
					break;
				case "cdlcounterattack":
					MetricFunctionUtil.fillInPattern(ms, "cdlcounterattack");
					break;
				case "cdldarkcloudcover":
					MetricFunctionUtil.fillInPattern(ms, "cdldarkcloudcover");
					break;	
				case "cdldoji":
					MetricFunctionUtil.fillInPattern(ms, "cdldoji");
					break;
				case "cdldragonflydoji":
					MetricFunctionUtil.fillInPattern(ms, "cdldragonflydoji");
					break;
				case "cdlengulfing":
					MetricFunctionUtil.fillInPattern(ms, "cdlengulfing");
					break;
				case "cdleveningdojistar":
					MetricFunctionUtil.fillInPattern(ms, "cdleveningdojistar");
					break;
				case "cdleveningstar":
					MetricFunctionUtil.fillInPattern(ms, "cdleveningstar");
					break;
				case "cdlgapsidesidewhite":
					MetricFunctionUtil.fillInPattern(ms, "cdlgapsidesidewhite");
					break;
				case "cdlgravestonedoji":
					MetricFunctionUtil.fillInPattern(ms, "cdlgravestonedoji");
					break;	
				case "cdlhammer":
					MetricFunctionUtil.fillInPattern(ms, "cdlhammer");
					break;
				case "cdlhangingman":
					MetricFunctionUtil.fillInPattern(ms, "cdlhangingman");
					break;
				case "cdlharami":
					MetricFunctionUtil.fillInPattern(ms, "cdlharami");
					break;
				case "cdlharamicross":
					MetricFunctionUtil.fillInPattern(ms, "cdlharamicross");
					break;
				case "cdlhignwave":
					MetricFunctionUtil.fillInPattern(ms, "cdlhignwave");
					break;
				case "cdlhikkake":
					MetricFunctionUtil.fillInPattern(ms, "cdlhikkake");
					break;
				case "cdlhikkakemod":
					MetricFunctionUtil.fillInPattern(ms, "cdlhikkakemod");
					break;
				case "cdlhomingpigeon":
					MetricFunctionUtil.fillInPattern(ms, "cdlhomingpigeon");
					break;
				case "cdlidentical3crows":
					MetricFunctionUtil.fillInPattern(ms, "cdlidentical3crows");
					break;
				case "cdlinneck":
					MetricFunctionUtil.fillInPattern(ms, "cdlinneck");
					break;
				case "cdlinvertedhammer":
					MetricFunctionUtil.fillInPattern(ms, "cdlinvertedhammer");
					break;
				case "cdlkicking":
					MetricFunctionUtil.fillInPattern(ms, "cdlkicking");
					break;
				case "cdlkickingbylength":
					MetricFunctionUtil.fillInPattern(ms, "cdlkickingbylength");
					break;
				case "cdlladderbottom":
					MetricFunctionUtil.fillInPattern(ms, "cdlladderbottom");
					break;
				case "cdllongleggeddoji":
					MetricFunctionUtil.fillInPattern(ms, "cdllongleggeddoji");
					break;
				case "cdllongline":
					MetricFunctionUtil.fillInPattern(ms, "cdllongline");
					break;
				case "cdlmarubozu":
					MetricFunctionUtil.fillInPattern(ms, "cdlmarubozu");
					break;
				case "cdlmatchinglow":
					MetricFunctionUtil.fillInPattern(ms, "cdlmatchinglow");
					break;
				case "cdlmathold":
					MetricFunctionUtil.fillInPattern(ms, "cdlmathold");
					break;
				case "cdlmorningstar":
					MetricFunctionUtil.fillInPattern(ms, "cdlmorningstar");
					break;
				case "cdlmorningdojistar":
					MetricFunctionUtil.fillInPattern(ms, "cdlmorningdojistar");
					break;
				case "cdlonneck":
					MetricFunctionUtil.fillInPattern(ms, "cdlonneck");
					break;
				case "cdlpiercing":
					MetricFunctionUtil.fillInPattern(ms, "cdlpiercing");
					break;
				case "cdlrickshawman":
					MetricFunctionUtil.fillInPattern(ms, "cdlrickshawman");
					break;
				case "cdlrisefall3methods":
					MetricFunctionUtil.fillInPattern(ms, "cdlrisefall3methods");
					break;
				case "cdlseperatinglines":
					MetricFunctionUtil.fillInPattern(ms, "cdlseperatinglines");
					break;
				case "cdlshootingstar":
					MetricFunctionUtil.fillInPattern(ms, "cdlshootingstar");
					break;
				case "cdlshortline":
					MetricFunctionUtil.fillInPattern(ms, "cdlshortline");
					break;
				case "cdlspinningtop":
					MetricFunctionUtil.fillInPattern(ms, "cdlspinningtop");
					break;
				case "cdlstalledpattern":
					MetricFunctionUtil.fillInPattern(ms, "cdlstalledpattern");
					break;
				case "cdlsticksandwich":
					MetricFunctionUtil.fillInPattern(ms, "cdlsticksandwich");
					break;
				case "cdltakuri":
					MetricFunctionUtil.fillInPattern(ms, "cdltakuri");
					break;
				case "cdltasukigap":
					MetricFunctionUtil.fillInPattern(ms, "cdltasukigap");
					break;
				case "cdlthursting":
					MetricFunctionUtil.fillInPattern(ms, "cdlthursting");
					break;
				case "cdltristar":
					MetricFunctionUtil.fillInPattern(ms, "cdltristar");
					break;
				case "cdlunique3river":
					MetricFunctionUtil.fillInPattern(ms, "cdlunique3river");
					break;
				case "cdlupsidegap2crows":
					MetricFunctionUtil.fillInPattern(ms, "cdlupsidegap2crows");
					break;
				case "cdlxsidegap3methods":
					MetricFunctionUtil.fillInPattern(ms, "cdlxsidegap3methods");
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
				case "timerange30":
					MetricFunctionUtil.fillInTimeRange(ms, 30);
					break;
				case "timerange40":
					MetricFunctionUtil.fillInTimeRange(ms, 40);
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
				case "timerangealpha30":
					MetricFunctionUtil.fillInTimeRangeAlpha(ms, 30);
					break;
				case "timerangealpha40":
					MetricFunctionUtil.fillInTimeRangeAlpha(ms, 40);
					break;

				// Range Pressure
				case "rangepressure5":
					MetricFunctionUtil.fillInRangePressure(ms, 5);
					break;
				case "rangepressure10":
					MetricFunctionUtil.fillInRangePressure(ms, 10);
					break;
				case "rangepressure30":
					MetricFunctionUtil.fillInRangePressure(ms, 30);
					break;
				case "rangepressure100":
					MetricFunctionUtil.fillInRangePressure(ms, 100);
					break;
				case "rangepressure300":
					MetricFunctionUtil.fillInRangePressure(ms, 300);
					break;
				case "rangepressure1000":
					MetricFunctionUtil.fillInRangePressure(ms, 1000);
					break;
				case "rangepressure3000":
					MetricFunctionUtil.fillInRangePressure(ms, 3000);
					break;
				
				// Range Rank
				case "rangerank30":
					MetricFunctionUtil.fillInRangeRank(ms, 30);
					break;
				case "rangerank100":
					MetricFunctionUtil.fillInRangeRank(ms, 100);
					break;
				case "rangerank300":
					MetricFunctionUtil.fillInRangeRank(ms, 300);
					break;
				case "rangerank1000":
					MetricFunctionUtil.fillInRangeRank(ms, 1000);
					break;
				case "rangerank3000":
					MetricFunctionUtil.fillInRangeRank(ms, 3000);
					break;
				
				}
				
				QueryManager.insertOrUpdateIntoMetrics(ms);

				if (running) {
					ms = MetricSingleton.getInstance().getNextMetricSequence();
				}
			}
			// System.out.println("This thread did " + c + " metrics");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks to see if a metric sequence contains any out of order elements. It
	 * should always be oldest to newest.
	 * 
	 * @param ms
	 * @return true if OK, false if something is out of order.
	 */
	private boolean metricSequenceChronologicalCheck(ArrayList<Metric> ms) {
		try {
			if (ms != null) {
				long ts0 = 0;
				for (Metric m : ms) {
					long ts1 = m.start.getTimeInMillis();
					if (ts0 > ts1) {
						return false;
					}
					ts0 = ts1;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
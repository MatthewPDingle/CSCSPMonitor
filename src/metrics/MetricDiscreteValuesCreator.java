package metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import data.BarKey;
import data.MetricKey;
import dbio.QueryManager;
import utils.CalcUtils;

public class MetricDiscreteValuesCreator {

	public static void main(String[] args) {
		runPercentiles();
//		runBobsBuckets();
	}
	
	public static void runBobsBuckets() {
		try {
			HashMap<String, ArrayList<Float>> metricCutoffHash = new HashMap<String, ArrayList<Float>>();
			
			metricCutoffHash.put("adx3", 					new ArrayList<Float>(Arrays.asList(new Float[] {10f, 20f, 50f, 90f, 95f})));
			metricCutoffHash.put("adx10", 					new ArrayList<Float>(Arrays.asList(new Float[] {5f, 10f, 50f, 85f, 95f})));
			metricCutoffHash.put("adx30", 					new ArrayList<Float>(Arrays.asList(new Float[] {5f, 10f, 45f, 55f, 60f, 65f, 75f})));
			metricCutoffHash.put("adx100", 					new ArrayList<Float>(Arrays.asList(new Float[] {18f, 26f, 32f, 38f})));
			metricCutoffHash.put("adx300", 					new ArrayList<Float>(Arrays.asList(new Float[] {4f, 10f, 16f, 20f})));
			metricCutoffHash.put("adx1000", 				new ArrayList<Float>(Arrays.asList(new Float[] {7f})));
			
			metricCutoffHash.put("adxdydx30", 				new ArrayList<Float>(Arrays.asList(new Float[] {-10f, 0f, 15f})));
			metricCutoffHash.put("adxdydx100", 				new ArrayList<Float>(Arrays.asList(new Float[] {-4f, 2f, 5f})));
			metricCutoffHash.put("adxdydx300", 				new ArrayList<Float>(Arrays.asList(new Float[] {-.5f, .5f})));
			
			metricCutoffHash.put("adxr3", 					new ArrayList<Float>(Arrays.asList(new Float[] {20f, 25f, 85f})));
			metricCutoffHash.put("adxr10", 					new ArrayList<Float>(Arrays.asList(new Float[] {5f, 50f, 75f})));
			metricCutoffHash.put("adxr30", 					new ArrayList<Float>(Arrays.asList(new Float[] {14f, 28f, 42f, 50f})));
			metricCutoffHash.put("adxr100", 				new ArrayList<Float>(Arrays.asList(new Float[] {22f, 30f})));
			metricCutoffHash.put("adxr300", 				new ArrayList<Float>(Arrays.asList(new Float[] {4f, 13f})));
			metricCutoffHash.put("adxr1000", 				new ArrayList<Float>(Arrays.asList(new Float[] {3f, 6f})));
			
			metricCutoffHash.put("adxrdydx30", 				new ArrayList<Float>(Arrays.asList(new Float[] {-5f, 5f})));
			metricCutoffHash.put("adxrdydx100",				new ArrayList<Float>(Arrays.asList(new Float[] {-2f, 2f})));
			metricCutoffHash.put("adxrdydx300",				new ArrayList<Float>(Arrays.asList(new Float[] {-.5f, .5f})));
			
			metricCutoffHash.put("aroonoscillator3", 		new ArrayList<Float>(Arrays.asList(new Float[] {0f}))); // Worthless
			metricCutoffHash.put("aroonoscillator10", 		new ArrayList<Float>(Arrays.asList(new Float[] {-80f, 0f, 20f, 80f})));
			metricCutoffHash.put("aroonoscillator30", 		new ArrayList<Float>(Arrays.asList(new Float[] {-80f, -40f, 40f, 80f})));
			metricCutoffHash.put("aroonoscillator100", 		new ArrayList<Float>(Arrays.asList(new Float[] {-50f})));
			metricCutoffHash.put("aroonoscillator300", 		new ArrayList<Float>(Arrays.asList(new Float[] {0f})));
			
			metricCutoffHash.put("atr3", 					new ArrayList<Float>(Arrays.asList(new Float[] {3f, 5f})));
			metricCutoffHash.put("atr10", 					new ArrayList<Float>(Arrays.asList(new Float[] {3f})));
			metricCutoffHash.put("atr30", 					new ArrayList<Float>(Arrays.asList(new Float[] {3f})));
			metricCutoffHash.put("atr100", 					new ArrayList<Float>(Arrays.asList(new Float[] {.5f, 3f})));
			metricCutoffHash.put("atr300",	 				new ArrayList<Float>(Arrays.asList(new Float[] {2.4f})));
			metricCutoffHash.put("atr1000", 				new ArrayList<Float>(Arrays.asList(new Float[] {2.4f})));
			
			metricCutoffHash.put("atrdydx30", 				new ArrayList<Float>(Arrays.asList(new Float[] {-.1f, .1f})));
			metricCutoffHash.put("atrdydx100", 				new ArrayList<Float>(Arrays.asList(new Float[] {-.05f, .05f})));
			metricCutoffHash.put("atrdydx300", 				new ArrayList<Float>(Arrays.asList(new Float[] {-.05f, .05f})));
			
			metricCutoffHash.put("breakout30", 				new ArrayList<Float>(Arrays.asList(new Float[] {-.02f, .04f})));
			metricCutoffHash.put("breakout100", 			new ArrayList<Float>(Arrays.asList(new Float[] {.01f})));
			metricCutoffHash.put("breakout300", 			new ArrayList<Float>(Arrays.asList(new Float[] {.01f})));
			
			metricCutoffHash.put("cci3", 					new ArrayList<Float>(Arrays.asList(new Float[] {-19f, 19f})));
			metricCutoffHash.put("cci10", 					new ArrayList<Float>(Arrays.asList(new Float[] {-40f, 10f})));
			metricCutoffHash.put("cci30", 					new ArrayList<Float>(Arrays.asList(new Float[] {-60f, -40f, 20f})));
			metricCutoffHash.put("cci100", 					new ArrayList<Float>(Arrays.asList(new Float[] {0f})));
			metricCutoffHash.put("cci300",	 				new ArrayList<Float>(Arrays.asList(new Float[] {0f})));
			
			metricCutoffHash.put("cmo3", 					new ArrayList<Float>(Arrays.asList(new Float[] {-20f, 40f, 80f})));
			metricCutoffHash.put("cmo10", 					new ArrayList<Float>(Arrays.asList(new Float[] {-50f, 0f, 50f})));
			metricCutoffHash.put("cmo30", 					new ArrayList<Float>(Arrays.asList(new Float[] {-50f, 0f})));
			metricCutoffHash.put("cmo100", 					new ArrayList<Float>(Arrays.asList(new Float[] {-50f, 0f, 30f})));
			metricCutoffHash.put("cmo300",	 				new ArrayList<Float>(Arrays.asList(new Float[] {-20f, -10f, 0f, 10f, 20f})));
			
			metricCutoffHash.put("dvol1ema", 				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .3f})));
			metricCutoffHash.put("dvol2ema", 				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .25f})));
			metricCutoffHash.put("dvol3ema", 				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .27f, .3f})));
			metricCutoffHash.put("dvol5ema",  				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .22f, .28f, .32f})));
			metricCutoffHash.put("dvol10ema",				new ArrayList<Float>(Arrays.asList(new Float[] {.2f, .3f, .35f})));
			metricCutoffHash.put("dvol25ema",				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .37f})));
			metricCutoffHash.put("dvol50ema",				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .34f})));
			metricCutoffHash.put("dvol75ema",				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .6f})));
			
			metricCutoffHash.put("intradayboll3",			new ArrayList<Float>(Arrays.asList(new Float[] {0f}))); // Worthless
			metricCutoffHash.put("intradayboll10",			new ArrayList<Float>(Arrays.asList(new Float[] {-2.5f, -1.5f, 1f, 2f})));
			metricCutoffHash.put("intradayboll30",			new ArrayList<Float>(Arrays.asList(new Float[] {-3f, -2f, 2f})));
			metricCutoffHash.put("intradayboll100",			new ArrayList<Float>(Arrays.asList(new Float[] {-3f, 4f})));
			metricCutoffHash.put("intradayboll300",			new ArrayList<Float>(Arrays.asList(new Float[] {-4f, -3f, 2f})));
			
			metricCutoffHash.put("macd10_30_8",				new ArrayList<Float>(Arrays.asList(new Float[] {-.06f, .06f})));
			metricCutoffHash.put("macd30_100_24",			new ArrayList<Float>(Arrays.asList(new Float[] {-.1f, 0f, .1f})));
			metricCutoffHash.put("macd100_300_80",			new ArrayList<Float>(Arrays.asList(new Float[] {-.08f, .16f})));
			metricCutoffHash.put("macd300_1000_240",		new ArrayList<Float>(Arrays.asList(new Float[] {-.32f, -.08f, .24f, .44f})));
			
			metricCutoffHash.put("macdh10_30_8",			new ArrayList<Float>(Arrays.asList(new Float[] {-.02f, .01f, .02f})));
			metricCutoffHash.put("macdh30_100_24",			new ArrayList<Float>(Arrays.asList(new Float[] {-.07f, -.04f, .03f, .05f, .07f})));
			metricCutoffHash.put("macdh100_300_80",			new ArrayList<Float>(Arrays.asList(new Float[] {-.05f, .05f})));
			metricCutoffHash.put("macdh300_1000_240",		new ArrayList<Float>(Arrays.asList(new Float[] {-.05f, .15f})));
			
			metricCutoffHash.put("macds10_30_8",			new ArrayList<Float>(Arrays.asList(new Float[] {-.12f, .08f})));
			metricCutoffHash.put("macds30_100_24",			new ArrayList<Float>(Arrays.asList(new Float[] {-.21f, -.10f, .13f})));
			metricCutoffHash.put("macds100_300_80",			new ArrayList<Float>(Arrays.asList(new Float[] {-.19f, -.08f, .13f, .19f})));
			metricCutoffHash.put("macds300_1000_240",		new ArrayList<Float>(Arrays.asList(new Float[] {-.4f, -.3f, .3f, .35f, .45f, .5f})));
			
			metricCutoffHash.put("mvol3", 					new ArrayList<Float>(Arrays.asList(new Float[] {.06f, .15f})));
			metricCutoffHash.put("mvol10", 					new ArrayList<Float>(Arrays.asList(new Float[] {.4f})));
			metricCutoffHash.put("mvol30", 					new ArrayList<Float>(Arrays.asList(new Float[] {.07f, .25f, .35f})));
			metricCutoffHash.put("mvol100", 				new ArrayList<Float>(Arrays.asList(new Float[] {.2f, .85f, .95f})));
			metricCutoffHash.put("mvol300",	 				new ArrayList<Float>(Arrays.asList(new Float[] {1.1f, 1.3f, 1.7f})));
			metricCutoffHash.put("mvol1000", 				new ArrayList<Float>(Arrays.asList(new Float[] {.6f, 2.6f})));
			metricCutoffHash.put("mvol3000",	 			new ArrayList<Float>(Arrays.asList(new Float[] {1.2f, 3f})));
			
			metricCutoffHash.put("ppo3_10",	 				new ArrayList<Float>(Arrays.asList(new Float[] {.1f})));
			metricCutoffHash.put("ppo10_30",	 			new ArrayList<Float>(Arrays.asList(new Float[] {-.9f, -.6f, .6f})));
			metricCutoffHash.put("ppo30_100",	 			new ArrayList<Float>(Arrays.asList(new Float[] {-1f, .9f})));
			metricCutoffHash.put("ppo100_300",	 			new ArrayList<Float>(Arrays.asList(new Float[] {-.9f, 1.7f})));
			metricCutoffHash.put("ppo300_1000",	 			new ArrayList<Float>(Arrays.asList(new Float[] {-3.1f, -.7f, .5f, 3.1f, 3.5f, 4.4f})));
			
			metricCutoffHash.put("ppodydx10_30",	 		new ArrayList<Float>(Arrays.asList(new Float[] {-.07f})));
			metricCutoffHash.put("ppodydx30_100",	 		new ArrayList<Float>(Arrays.asList(new Float[] {-.06f, -.04f, .04f, .06f})));
			metricCutoffHash.put("ppodydx100_300",			new ArrayList<Float>(Arrays.asList(new Float[] {-.018f, .021f})));
			
			metricCutoffHash.put("pricebolls3",				new ArrayList<Float>(Arrays.asList(new Float[] {-.5f, .3f})));
			metricCutoffHash.put("pricebolls10",			new ArrayList<Float>(Arrays.asList(new Float[] {-2.7f, -1.3f, -.9f, .6f})));
			metricCutoffHash.put("pricebolls30",			new ArrayList<Float>(Arrays.asList(new Float[] {-1.2f, 1.4f, 3f})));
			metricCutoffHash.put("pricebolls100",			new ArrayList<Float>(Arrays.asList(new Float[] {-2f, 2.8f, 4f})));
			metricCutoffHash.put("pricebolls300",			new ArrayList<Float>(Arrays.asList(new Float[] {-.9f, 2.7f})));
			
			metricCutoffHash.put("psar",					new ArrayList<Float>(Arrays.asList(new Float[] {-.3f})));
			
			metricCutoffHash.put("rangepressure30",			new ArrayList<Float>(Arrays.asList(new Float[] {.09f, .2f, .88f, .95f})));
			metricCutoffHash.put("rangepressure100",		new ArrayList<Float>(Arrays.asList(new Float[] {.01f, .24f, .42f, .94f, .99f})));
			metricCutoffHash.put("rangepressure300",		new ArrayList<Float>(Arrays.asList(new Float[] {.19f, .99f})));
			metricCutoffHash.put("rangepressure1000",		new ArrayList<Float>(Arrays.asList(new Float[] {.14f, .99f})));
			metricCutoffHash.put("rangepressure3000",		new ArrayList<Float>(Arrays.asList(new Float[] {.07f, .99f})));
			
			metricCutoffHash.put("rangerank30",				new ArrayList<Float>(Arrays.asList(new Float[] {.15f, .55f, .7f, .8f})));
			metricCutoffHash.put("rangerank100",	 		new ArrayList<Float>(Arrays.asList(new Float[] {.04f, .3f, .7f, .98f})));
			metricCutoffHash.put("rangerank300",			new ArrayList<Float>(Arrays.asList(new Float[] {.16f, .76f})));
			metricCutoffHash.put("rangerank1000",			new ArrayList<Float>(Arrays.asList(new Float[] {.04f, .26f, .32f, .9f})));
			metricCutoffHash.put("rangerank3000",			new ArrayList<Float>(Arrays.asList(new Float[] {.04f, .97f})));
			
			metricCutoffHash.put("rsi3",					new ArrayList<Float>(Arrays.asList(new Float[] {1f, 40f, 70f, 94f})));
			metricCutoffHash.put("rsi10",					new ArrayList<Float>(Arrays.asList(new Float[] {2f, 50f, 90f, 99f})));
			metricCutoffHash.put("rsi30",					new ArrayList<Float>(Arrays.asList(new Float[] {20f, 44f, 68f})));
			metricCutoffHash.put("rsi100",					new ArrayList<Float>(Arrays.asList(new Float[] {32f, 35f, 42f, 66f})));
			metricCutoffHash.put("rsi300",					new ArrayList<Float>(Arrays.asList(new Float[] {50f, 55f})));
			
			metricCutoffHash.put("stod10_3_3",				new ArrayList<Float>(Arrays.asList(new Float[] {36f, 80f, 90f})));
			metricCutoffHash.put("stod30_10_10",			new ArrayList<Float>(Arrays.asList(new Float[] {30f, 60f, 84f})));
			metricCutoffHash.put("stod100_30_30",			new ArrayList<Float>(Arrays.asList(new Float[] {22f, 90f})));
			metricCutoffHash.put("stod300_100_100",			new ArrayList<Float>(Arrays.asList(new Float[] {22f})));
			
			metricCutoffHash.put("stodrsi10_3_3",			new ArrayList<Float>(Arrays.asList(new Float[] {3f, 93f, 96f})));
			metricCutoffHash.put("stodrsi30_10_10",			new ArrayList<Float>(Arrays.asList(new Float[] {6f, 92f, 97f})));
			metricCutoffHash.put("stodrsi100_30_30",		new ArrayList<Float>(Arrays.asList(new Float[] {9f, 91f})));
			metricCutoffHash.put("stodrsi300_100_100",		new ArrayList<Float>(Arrays.asList(new Float[] {50f}))); // Worthless
			
			metricCutoffHash.put("stok10_3_3",				new ArrayList<Float>(Arrays.asList(new Float[] {8f, 70f, 83f})));
			metricCutoffHash.put("stok30_10_10",			new ArrayList<Float>(Arrays.asList(new Float[] {9f, 85f})));
			metricCutoffHash.put("stok100_30_30",			new ArrayList<Float>(Arrays.asList(new Float[] {10f, 40f, 90f})));
			metricCutoffHash.put("stok300_100_100",			new ArrayList<Float>(Arrays.asList(new Float[] {12f, 88f})));
			
			metricCutoffHash.put("stokrsi10_3_3",			new ArrayList<Float>(Arrays.asList(new Float[] {1f, 31f, 69f, 99f})));
			metricCutoffHash.put("stokrsi30_10_10",			new ArrayList<Float>(Arrays.asList(new Float[] {2f, 74f, 99f})));
			metricCutoffHash.put("stokrsi100_30_30",		new ArrayList<Float>(Arrays.asList(new Float[] {1f, 80f, 99f})));
			metricCutoffHash.put("stokrsi300_100_100",		new ArrayList<Float>(Arrays.asList(new Float[] {1f, 53f, 81f, 99f}))); 
			
			metricCutoffHash.put("timerange2",				new ArrayList<Float>(Arrays.asList(new Float[] {.85f, 1.05f}))); 
			metricCutoffHash.put("timerange5",				new ArrayList<Float>(Arrays.asList(new Float[] {1.8f}))); 
			metricCutoffHash.put("timerange8",				new ArrayList<Float>(Arrays.asList(new Float[] {.9f, 1.8f}))); 
			metricCutoffHash.put("timerange13",				new ArrayList<Float>(Arrays.asList(new Float[] {.4f, .8f, 1.9f}))); 
			metricCutoffHash.put("timerange20",				new ArrayList<Float>(Arrays.asList(new Float[] {.2f, .6f, 2.4f, 3f}))); 
			metricCutoffHash.put("timerange30",				new ArrayList<Float>(Arrays.asList(new Float[] {.1f, .55f, 1.1f, 1.5f, 2.4f, 3.1f, 3.8f})));
			
			metricCutoffHash.put("timerangealpha2",			new ArrayList<Float>(Arrays.asList(new Float[] {5f, 33f}))); 
			metricCutoffHash.put("timerangealpha5",			new ArrayList<Float>(Arrays.asList(new Float[] {40f, 70f, 90f}))); 
			metricCutoffHash.put("timerangealpha8",			new ArrayList<Float>(Arrays.asList(new Float[] {20f, 170f}))); 
			metricCutoffHash.put("timerangealpha13",		new ArrayList<Float>(Arrays.asList(new Float[] {250f}))); 
			metricCutoffHash.put("timerangealpha20",		new ArrayList<Float>(Arrays.asList(new Float[] {100f, 510f}))); 
			metricCutoffHash.put("timerangealpha30",		new ArrayList<Float>(Arrays.asList(new Float[] {850f})));
			
			metricCutoffHash.put("tsf3",					new ArrayList<Float>(Arrays.asList(new Float[] {1f, 2f}))); // Worthless
			metricCutoffHash.put("tsf10",					new ArrayList<Float>(Arrays.asList(new Float[] {0f}))); // Worthless
			metricCutoffHash.put("tsf30",					new ArrayList<Float>(Arrays.asList(new Float[] {-1f, 5f})));
			metricCutoffHash.put("tsf100",					new ArrayList<Float>(Arrays.asList(new Float[] {-8f, 10f})));
			metricCutoffHash.put("tsf300",					new ArrayList<Float>(Arrays.asList(new Float[] {-9f, 13f})));
			
			metricCutoffHash.put("tsfdydx30",				new ArrayList<Float>(Arrays.asList(new Float[] {-4f, 1f})));
			metricCutoffHash.put("tsfdydx100",				new ArrayList<Float>(Arrays.asList(new Float[] {-4.5f, 2f})));
			metricCutoffHash.put("tsfdydx300",				new ArrayList<Float>(Arrays.asList(new Float[] {-4.5f, 3.5f})));
			
			metricCutoffHash.put("uo3_10_30",				new ArrayList<Float>(Arrays.asList(new Float[] {28f, 59f})));
			metricCutoffHash.put("uo10_30_100",				new ArrayList<Float>(Arrays.asList(new Float[] {36f, 62f})));
			metricCutoffHash.put("uo30_100_300",			new ArrayList<Float>(Arrays.asList(new Float[] {43f, 57f})));
			
			metricCutoffHash.put("williamsr3",				new ArrayList<Float>(Arrays.asList(new Float[] {4f })));
			metricCutoffHash.put("williamsr10",				new ArrayList<Float>(Arrays.asList(new Float[] {5f, 20f, 81f})));
			metricCutoffHash.put("williamsr30",				new ArrayList<Float>(Arrays.asList(new Float[] {5f, 9f, 50f, 90f})));
			metricCutoffHash.put("williamsr100",			new ArrayList<Float>(Arrays.asList(new Float[] {4f, 9f, 29f, 46f, 89f})));
			metricCutoffHash.put("williamsr300",			new ArrayList<Float>(Arrays.asList(new Float[] {2f, 22f, 71f})));
			
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bk2 = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			BarKey bk3 = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			barKeys.add(bk1);
			barKeys.add(bk2);
			barKeys.add(bk3);
			
			ArrayList<String> newMetrics = Constants.METRICS;
	
			for (String metric : newMetrics) {
				if (!metric.startsWith("cdl")) {
					for (BarKey bk : barKeys) {
						HashMap<String, Calendar> metricTimes = QueryManager.getMinMaxMetricStarts(bk);
						
						ArrayList<Float> cutoffs = metricCutoffHash.get(metric);
						
						System.out.println(metric + ", " + bk.toString());
						QueryManager.insertIntoMetricDiscreteValues(metric, bk, metricTimes.get("min"), metricTimes.get("max"), cutoffs, cutoffs, "Bobs Buckets");
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void runPercentiles() {
		try {
			ArrayList<Float> percentiles = new ArrayList<Float>();
			// Set 1
//			percentiles.add(.5f);
//			percentiles.add(1f);
//			percentiles.add(2f);
//			percentiles.add(5f);
//			percentiles.add(10f);
//			percentiles.add(50f);
//			percentiles.add(90f);
//			percentiles.add(95f);
//			percentiles.add(98f);
//			percentiles.add(99f);
//			percentiles.add(99.5f);
			
			// Set 2
//			percentiles.add(1f);
//			percentiles.add(3f);
//			percentiles.add(10f);
//			percentiles.add(50f);
//			percentiles.add(90f);
//			percentiles.add(97f);
//			percentiles.add(99f);
			
			// Set 3
//			percentiles.add(1f);
//			percentiles.add(4f);
//			percentiles.add(50f);
//			percentiles.add(96f);
//			percentiles.add(99f);
			
			// Set 4
//			percentiles.add(5f);
//			percentiles.add(50f);
//			percentiles.add(95f);
			
			// Set 5
//			percentiles.add(3f);
//			percentiles.add(50f);
//			percentiles.add(97f);

			// Set 6
//			percentiles.add(8f);
//			percentiles.add(50f);
//			percentiles.add(92f);
			
			// Set 7
//			percentiles.add(5f);
//			percentiles.add(15f);
//			percentiles.add(35f);
//			percentiles.add(50f);
//			percentiles.add(65f);
//			percentiles.add(75f);
//			percentiles.add(85f);
//			percentiles.add(95f);
			
			// Set 8
//			percentiles.add(20f);
//			percentiles.add(50f);
//			percentiles.add(80f);
			
			// Set 9
//			percentiles.add(10f);
//			percentiles.add(20f);
//			percentiles.add(30f);
//			percentiles.add(40f);
//			percentiles.add(50f);
//			percentiles.add(60f);
//			percentiles.add(70f);
//			percentiles.add(80f);
//			percentiles.add(90f);

			// Set 10
			percentiles.add(1f);
			percentiles.add(5f);
			percentiles.add(50f);
			percentiles.add(95f);
			percentiles.add(99f);	
			
			BarKey bk1 = new BarKey("EUR.USD", BAR_SIZE.BAR_1H);
			BarKey bk2 = new BarKey("EUR.GBP", BAR_SIZE.BAR_1H);
			BarKey bk3 = new BarKey("GBP.USD", BAR_SIZE.BAR_1H);
			ArrayList<BarKey> barKeys = new ArrayList<BarKey>();
			barKeys.add(bk1);
			barKeys.add(bk2);
			barKeys.add(bk3);
			
			ArrayList<String> newMetrics = Constants.METRICS;
//			ArrayList<String> newMetrics = new ArrayList<String>();
			
			ArrayList<Float> values = new ArrayList<Float>();
			for (String metric : newMetrics) {
//				if (!metric.startsWith("cdl")) {
					for (BarKey bk : barKeys) {
						HashMap<String, Calendar> metricTimes = QueryManager.getMinMaxMetricStarts(bk);
						
						for (float percentile : percentiles) {
							float maxValue = QueryManager.getMetricValueAtPercentile(metric, bk, "max", percentile);	
							maxValue = CalcUtils.round(maxValue, 2);
							if (!values.contains(maxValue)) {
								values.add(maxValue);
							}
						}
						MetricKey mk = new MetricKey(metric, bk.symbol, bk.duration);
						System.out.println(metric + ", " + bk.toString());
						QueryManager.insertIntoMetricDiscreteValues(metric, bk, metricTimes.get("min"), metricTimes.get("max"), percentiles, values, "Percentiles Set 10");
						values = new ArrayList<Float>();
					}
//				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
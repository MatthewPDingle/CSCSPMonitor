package data;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import utils.Formatting;

public class Model {

	public int id = -1;
	public String type;
	public String modelFile;
	public String algo;
	public String params;
	public BarKey bk;
	public ArrayList<BarKey> barKeys;
	public boolean interBarData;
	public ArrayList<String> metrics;
	public Calendar trainStart;
	public Calendar trainEnd;
	public Calendar testStart;
	public Calendar testEnd;
	public String sellMetric;
	public float sellMetricValue;
	public String stopMetric;
	public float stopMetricValue;
	public int numBars;
	public int numClasses;
	
	public int trainDatasetSize;
	public int trainTrueNegatives;
	public int trainFalseNegatives;
	public int trainFalsePositives;
	public int trainTruePositives;
	public double trainTruePositiveRate;
	public double trainFalsePositiveRate;
	public double trainCorrectRate;
	public double trainKappa;
	public double trainMeanAbsoluteError;
	public double trainRootMeanSquaredError;
	public double trainRelativeAbsoluteError;
	public double trainRootRelativeSquaredError;
	public double trainROCArea;
	
	public int testDatasetSize;
	public int testTrueNegatives;
	public int testFalseNegatives;
	public int testFalsePositives;
	public int testTruePositives;
	public double testTruePositiveRate;
	public double testFalsePositiveRate;
	public double testCorrectRate;
	public double testKappa;
	public double testMeanAbsoluteError;
	public double testRootMeanSquaredError;
	public double testRelativeAbsoluteError;
	public double testRootRelativeSquaredError;
	public double testROCArea;
	
	public double[] testBucketPercentCorrect;
	public double[] testBucketDistribution;
	public double[] testBucketPValues;
	
	public boolean favorite;
	public boolean tradeOffPrimary;
	public boolean tradeOffOpposite;
	public boolean useInBackTests;

	public Calendar baseDate;
	public String notes;
	
	public double predictionDistributionPercentage;
	
	public String lastActionPrice = "";
	public String lastAction = "";
	public Calendar lastActionTime = null;
	public String lastTargetClose = "";
	public String lastStopClose = "";

	public Model(String type, String modelFile, String algo, String params, BarKey bk, boolean interBarData, ArrayList<String> metrics,
			Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, String sellMetric,
			float sellMetricValue, String stopMetric, float stopMetricValue, int numBars, int numClasses, int trainDatasetSize,
			int trainTrueNegatives, int trainFalseNegatives, int trainFalsePositives, int trainTruePositives,
			double trainTruePositiveRate, double trainFalsePositiveRate, double trainCorrectRate, double trainKappa,
			double trainMeanAbsoluteError, double trainRootMeanSquaredError, double trainRelativeAbsoluteError,
			double trainRootRelativeSquaredError, double trainROCArea, int testDatasetSize, int testTrueNegatives,
			int testFalseNegatives, int testFalsePositives, int testTruePositives, double testTruePositiveRate,
			double testFalsePositiveRate, double testCorrectRate, double testKappa, double testMeanAbsoluteError,
			double testRootMeanSquaredError, double testRelativeAbsoluteError, double testRootRelativeSquaredError,
			double testROCArea, double[] testBucketPercentCorrect, double[] testBucketDistribution, double[] testBucketPValues, 
			String notes, boolean favorite, boolean tradeOffPrimary, boolean tradeOffOpposite, boolean useInBackTests, Calendar baseDate) {
		super();
		this.type = type;
		this.modelFile = modelFile;
		this.algo = algo;
		this.params = params;
//		this.barKeys = new ArrayList<BarKey>();
//		this.barKeys.addAll(barKeys);
		this.bk = bk;
		this.interBarData = interBarData;
		this.metrics = metrics;
		this.trainStart = trainStart;
		this.trainEnd = trainEnd;
		this.testStart = testStart;
		this.testEnd = testEnd;
		this.sellMetric = sellMetric;
		this.sellMetricValue = Float.parseFloat(Formatting.df2.format(sellMetricValue));
		this.stopMetric = stopMetric;
		this.stopMetricValue = Float.parseFloat(Formatting.df2.format(stopMetricValue));
		this.numBars = numBars;
		this.numClasses = numClasses;
		this.trainDatasetSize = trainDatasetSize;
		this.trainTrueNegatives = trainTrueNegatives;
		this.trainFalseNegatives = trainFalseNegatives;
		this.trainFalsePositives = trainFalsePositives;
		this.trainTruePositives = trainTruePositives;
		this.trainTruePositiveRate = trainTruePositiveRate;
		this.trainFalsePositiveRate = trainFalsePositiveRate;
		this.trainCorrectRate = trainCorrectRate;
		this.trainKappa = trainKappa;
		this.trainMeanAbsoluteError = trainMeanAbsoluteError;
		this.trainRootMeanSquaredError = trainRootMeanSquaredError;
		this.trainRelativeAbsoluteError = trainRelativeAbsoluteError;
		this.trainRootRelativeSquaredError = trainRootRelativeSquaredError;
		this.trainROCArea = trainROCArea;
		this.testDatasetSize = testDatasetSize;
		this.testTrueNegatives = testTrueNegatives;
		this.testFalseNegatives = testFalseNegatives;
		this.testFalsePositives = testFalsePositives;
		this.testTruePositives = testTruePositives;
		this.testTruePositiveRate = testTruePositiveRate;
		this.testFalsePositiveRate = testFalsePositiveRate;
		this.testCorrectRate = testCorrectRate;
		this.testKappa = testKappa;
		this.testMeanAbsoluteError = testMeanAbsoluteError;
		this.testRootMeanSquaredError = testRootMeanSquaredError;
		this.testRelativeAbsoluteError = testRelativeAbsoluteError;
		this.testRootRelativeSquaredError = testRootRelativeSquaredError;
		this.testROCArea = testROCArea;
		this.testBucketPercentCorrect = testBucketPercentCorrect;
		this.testBucketDistribution = testBucketDistribution;
		this.testBucketPValues = testBucketPValues;
		this.notes = notes;
		this.favorite = favorite;
		this.tradeOffPrimary = tradeOffPrimary;
		this.tradeOffOpposite = tradeOffOpposite;
		this.useInBackTests = useInBackTests;
		this.predictionDistributionPercentage = 0;
		this.baseDate = Calendar.getInstance();
		this.baseDate.setTimeInMillis(baseDate.getTimeInMillis());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof Model) {
			Model m = (Model)o;
			if (this.modelFile.equals(m.modelFile)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(modelFile).toHashCode();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getModelFile() {
		return modelFile;
	}

	public void setModelFile(String modelFile) {
		this.modelFile = modelFile;
	}

	public String getAlgo() {
		return algo;
	}

	public void setAlgo(String algo) {
		this.algo = algo;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public BarKey getBk() {
		return bk;
	}

	public void setBk(BarKey bk) {
		this.bk = bk;
	}

	public boolean isInterBarData() {
		return interBarData;
	}

	public void setInterBarData(boolean interBarData) {
		this.interBarData = interBarData;
	}

	public ArrayList<String> getMetrics() {
		return metrics;
	}

	public void setMetrics(ArrayList<String> metrics) {
		this.metrics = metrics;
	}

	public Calendar getTrainStart() {
		return trainStart;
	}

	public void setTrainStart(Calendar trainStart) {
		this.trainStart = trainStart;
	}

	public Calendar getTrainEnd() {
		return trainEnd;
	}

	public void setTrainEnd(Calendar trainEnd) {
		this.trainEnd = trainEnd;
	}

	public Calendar getTestStart() {
		return testStart;
	}

	public void setTestStart(Calendar testStart) {
		this.testStart = testStart;
	}

	public Calendar getTestEnd() {
		return testEnd;
	}

	public void setTestEnd(Calendar testEnd) {
		this.testEnd = testEnd;
	}

	public String getSellMetric() {
		return sellMetric;
	}

	public void setSellMetric(String sellMetric) {
		this.sellMetric = sellMetric;
	}

	public float getSellMetricValue() {
		return sellMetricValue;
	}

	public void setSellMetricValue(float sellMetricValue) {
		this.sellMetricValue = sellMetricValue;
	}

	public String getStopMetric() {
		return stopMetric;
	}

	public void setStopMetric(String stopMetric) {
		this.stopMetric = stopMetric;
	}

	public float getStopMetricValue() {
		return stopMetricValue;
	}

	public void setStopMetricValue(float stopMetricValue) {
		this.stopMetricValue = stopMetricValue;
	}

	public int getNumBars() {
		return numBars;
	}

	public void setNumBars(int numBars) {
		this.numBars = numBars;
	}

	public int getTrainDatasetSize() {
		return trainDatasetSize;
	}

	public void setTrainDatasetSize(int trainDatasetSize) {
		this.trainDatasetSize = trainDatasetSize;
	}

	public int getTrainTrueNegatives() {
		return trainTrueNegatives;
	}

	public void setTrainTrueNegatives(int trainTrueNegatives) {
		this.trainTrueNegatives = trainTrueNegatives;
	}

	public int getTrainFalseNegatives() {
		return trainFalseNegatives;
	}

	public void setTrainFalseNegatives(int trainFalseNegatives) {
		this.trainFalseNegatives = trainFalseNegatives;
	}

	public int getTrainFalsePositives() {
		return trainFalsePositives;
	}

	public void setTrainFalsePositives(int trainFalsePositives) {
		this.trainFalsePositives = trainFalsePositives;
	}

	public int getTrainTruePositives() {
		return trainTruePositives;
	}

	public void setTrainTruePositives(int trainTruePositives) {
		this.trainTruePositives = trainTruePositives;
	}

	public double getTrainTruePositiveRate() {
		return trainTruePositiveRate;
	}

	public void setTrainTruePositiveRate(double trainTruePostitiveRate) {
		this.trainTruePositiveRate = trainTruePostitiveRate;
	}

	public double getTrainFalsePositiveRate() {
		return trainFalsePositiveRate;
	}

	public void setTrainFalsePositiveRate(double trainFalsePositiveRate) {
		this.trainFalsePositiveRate = trainFalsePositiveRate;
	}

	public double getTrainCorrectRate() {
		return trainCorrectRate;
	}

	public void setTrainCorrectRate(double trainCorrectRate) {
		this.trainCorrectRate = trainCorrectRate;
	}

	public double getTrainKappa() {
		return trainKappa;
	}

	public void setTrainKappa(double trainKappa) {
		this.trainKappa = trainKappa;
	}

	public double getTrainMeanAbsoluteError() {
		return trainMeanAbsoluteError;
	}

	public void setTrainMeanAbsoluteError(double trainMeanAbsoluteError) {
		this.trainMeanAbsoluteError = trainMeanAbsoluteError;
	}

	public double getTrainRootMeanSquaredError() {
		return trainRootMeanSquaredError;
	}

	public void setTrainRootMeanSquaredError(double trainRootMeanSquaredError) {
		this.trainRootMeanSquaredError = trainRootMeanSquaredError;
	}

	public double getTrainRelativeAbsoluteError() {
		return trainRelativeAbsoluteError;
	}

	public void setTrainRelativeAbsoluteError(double trainRelativeAbsoluteError) {
		this.trainRelativeAbsoluteError = trainRelativeAbsoluteError;
	}

	public double getTrainRootRelativeSquaredError() {
		return trainRootRelativeSquaredError;
	}

	public void setTrainRootRelativeSquaredError(double trainRootRelativeSquaredError) {
		this.trainRootRelativeSquaredError = trainRootRelativeSquaredError;
	}

	public double getTrainROCArea() {
		return trainROCArea;
	}

	public void setTrainROCArea(double trainROCArea) {
		this.trainROCArea = trainROCArea;
	}

	public int getTestDatasetSize() {
		return testDatasetSize;
	}

	public void setTestDatasetSize(int testDatasetSize) {
		this.testDatasetSize = testDatasetSize;
	}

	public int getTestTrueNegatives() {
		return testTrueNegatives;
	}

	public void setTestTrueNegatives(int testTrueNegatives) {
		this.testTrueNegatives = testTrueNegatives;
	}

	public int getTestFalseNegatives() {
		return testFalseNegatives;
	}

	public void setTestFalseNegatives(int testFalseNegatives) {
		this.testFalseNegatives = testFalseNegatives;
	}

	public int getTestFalsePositives() {
		return testFalsePositives;
	}

	public void setTestFalsePositives(int testFalsePositives) {
		this.testFalsePositives = testFalsePositives;
	}

	public int getTestTruePositives() {
		return testTruePositives;
	}

	public void setTestTruePositives(int testTruePositives) {
		this.testTruePositives = testTruePositives;
	}

	public double getTestTruePositiveRate() {
		return testTruePositiveRate;
	}

	public void setTestTruePositiveRate(double testTruePositiveRate) {
		this.testTruePositiveRate = testTruePositiveRate;
	}

	public double getTestFalsePositiveRate() {
		return testFalsePositiveRate;
	}

	public void setTestFalsePositiveRate(double testFalsePositiveRate) {
		this.testFalsePositiveRate = testFalsePositiveRate;
	}

	public double getTestCorrectRate() {
		return testCorrectRate;
	}

	public void setTestCorrectRate(double testCorrectRate) {
		this.testCorrectRate = testCorrectRate;
	}

	public double getTestKappa() {
		return testKappa;
	}

	public void setTestKappa(double testKappa) {
		this.testKappa = testKappa;
	}

	public double getTestMeanAbsoluteError() {
		return testMeanAbsoluteError;
	}

	public void setTestMeanAbsoluteError(double testMeanAbsoluteError) {
		this.testMeanAbsoluteError = testMeanAbsoluteError;
	}

	public double getTestRootMeanSquaredError() {
		return testRootMeanSquaredError;
	}

	public void setTestRootMeanSquaredError(double testRootMeanSquaredError) {
		this.testRootMeanSquaredError = testRootMeanSquaredError;
	}

	public double getTestRelativeAbsoluteError() {
		return testRelativeAbsoluteError;
	}

	public void setTestRelativeAbsoluteError(double testRelativeAbsoluteError) {
		this.testRelativeAbsoluteError = testRelativeAbsoluteError;
	}

	public double getTestRootRelativeSquaredError() {
		return testRootRelativeSquaredError;
	}

	public void setTestRootRelativeSquaredError(double testRootRelativeSquaredError) {
		this.testRootRelativeSquaredError = testRootRelativeSquaredError;
	}

	public double getTestROCArea() {
		return testROCArea;
	}

	public void setTestROCArea(double testROCArea) {
		this.testROCArea = testROCArea;
	}

	public double[] getTestBucketPercentCorrect() {
		return testBucketPercentCorrect;
	}

	public void setTestBucketPercentCorrect(double[] testBucketPercentCorrect) {
		this.testBucketPercentCorrect = testBucketPercentCorrect;
	}
	
	public double[] getTestBucketPValues() {
		return testBucketPValues;
	}

	public void setTestBucketPValues(double[] testBucketPValues) {
		this.testBucketPValues = testBucketPValues;
	}

	public String getTestBucketPercentCorrectJSON() {
		if (testBucketPercentCorrect.length != 5) {
			return "[]";
		}
		
		String json = "{\"TestBucketPercentCorrect\": [";
		
		for (int a = 4; a >= 0; a--) {
			json += Formatting.df5.format(testBucketPercentCorrect[a]) + ", ";
		}
		for (int a = 0; a <= 4; a++) {
			json += Formatting.df5.format(testBucketPercentCorrect[a]) + ", ";
		}
		json = json.substring(0, json.length() - 2);
		json += "] }";
		return json;
	}
	
	public String getTestBucketDistributionJSON() {
		if (testBucketDistribution.length != 5) {
			return "[]";
		}
		
		String json = "{\"TestBucketDistribution\": [";
		
		for (int a = 4; a >= 0; a--) {
			json += Formatting.df5.format(testBucketDistribution[a]) + ", ";
		}
		for (int a = 0; a <= 4; a++) {
			json += Formatting.df5.format(testBucketDistribution[a]) + ", ";
		}
		json = json.substring(0, json.length() - 2);
		json += "] }";
		return json;
	}
	
	public String getTestBucketPValuesJSON() {
		if (testBucketPValues.length != 5) {
			return "[]";
		}
		
		String json = "{\"TestBucketPValues\": [";
		
		for (int a = 4; a >= 0; a--) {
			json += Formatting.df5.format(testBucketPValues[a]) + ", ";
		}
		for (int a = 0; a <= 4; a++) {
			json += Formatting.df5.format(testBucketPValues[a]) + ", ";
		}
		json = json.substring(0, json.length() - 2);
		json += "] }";
		return json;
	}

	public double[] getTestBucketDistribution() {
		return testBucketDistribution;
	}

	public void setTestBucketDistribution(double[] testBucketDistribution) {
		this.testBucketDistribution = testBucketDistribution;
	}

	public int getNumClasses() {
		return numClasses;
	}

	public void setNumClasses(int numClasses) {
		this.numClasses = numClasses;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public boolean isTradeOffPrimary() {
		return tradeOffPrimary;
	}

	public void setTradeOffPrimary(boolean tradeOffPrimary) {
		this.tradeOffPrimary = tradeOffPrimary;
	}

	public boolean isTradeOffOpposite() {
		return tradeOffOpposite;
	}

	public void setTradeOffOpposite(boolean tradeOffOpposite) {
		this.tradeOffOpposite = tradeOffOpposite;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public ArrayList<BarKey> getBarKeys() {
		return barKeys;
	}

	public void setBarKeys(ArrayList<BarKey> barKeys) {
		this.barKeys = barKeys;
	}

	public double getPredictionDistributionPercentage() {
		return predictionDistributionPercentage;
	}

	public void setPredictionDistributionPercentage(double predictionDistributionPercentage) {
		this.predictionDistributionPercentage = predictionDistributionPercentage;
	}

	public double getTrainWinPercent() {
		if (trainTruePositiveRate + trainFalsePositiveRate == 0) {
			return 0;
		}
		return trainTruePositives / (double)(trainTruePositives + trainFalsePositives);
	}

	public double getTestWinPercent() {
		return (((getTestBullWinPercent() * getTestNumBullPredictions()) + (getTestBearWinPercent() * getTestNumBearPredictions())) 
				/ (float)(getTestNumBullOpportunities() + getTestNumBearOpportunities()));
	}
	
	public double getTestBullWinPercent() {
		if (testTruePositiveRate + testFalsePositiveRate == 0) {
			return 0;
		}
		return testTruePositives / (double)(testTruePositives + testFalsePositives);
	}
	
	public double getTestBearWinPercent() {
		if (testTrueNegatives + testFalseNegatives == 0) {
			return 0;
		}
		return testTrueNegatives / (double)(testTrueNegatives + testFalseNegatives);
	}
	
	public int getTestNumBullOpportunities() {
		return testTruePositives + testFalseNegatives;
	}
	
	public int getTestNumBearOpportunities() {
		return testTrueNegatives + testFalsePositives;
	}
	
	public int getTestNumBullPredictions() {
		return testTruePositives + testFalsePositives;
	}
	
	public int getTestNumBearPredictions() {
		return testTrueNegatives + testFalseNegatives;
	}
	
	public double getTestEstimatedAverageReturn() {
		return (sellMetricValue * getTestWinPercent()) - (stopMetricValue * (1 - getTestWinPercent()));
	}
	
	public double getTestBearEstimatedAverageReturn() {
		return (sellMetricValue * getTestBearWinPercent()) - (stopMetricValue * (1 - getTestBearWinPercent()));
	}
	
	public double getMultiplier() {
		return sellMetricValue / stopMetricValue;
	}
	
	public double getTestBullReturnPower() {
		return getTestNumBullOpportunities() * testTruePositiveRate * getTestEstimatedAverageReturn();
	}
	
	public double getTestBearReturnPower() {
		double testTrueNegativeRate = testTrueNegatives / (double)(testTrueNegatives + testFalsePositives);
		return getTestNumBearOpportunities() * testTrueNegativeRate * getTestBearEstimatedAverageReturn();
	}
	
	public double getTestOppPercent() {
		return (getTestNumBullOpportunities() + getTestNumBearOpportunities()) / (double)testDatasetSize;
	}
	
	public String getTestBucketPercentCorrectString() {
		String s = "[";
		for (double pc : testBucketPercentCorrect) {
			String t = Formatting.df2.format(pc);
			if (t.length() == 1) {
				t += ".";
			}
			while (t.length() < 4) {
				t += "0";
			}
			s += t + ", ";
		}
		s = s.substring(0, s.length() - 2);
		s = s + "]";
		return s;
	}
	
	public String getTestBucketDistributionString() {
		String s = "[";
		for (double pc : testBucketDistribution) {
			String t = Formatting.df2.format(pc);
			if (t.length() == 1) {
				t += ".";
			}
			while (t.length() < 4) {
				t += "0";
			}
			s += t + ", ";
		}
		s = s.substring(0, s.length() - 2);
		s = s + "]";
		return s;
	}
	
	/** 
	 * This is the implied winning percentage that is found if you only consider trading off buckets that meet the tradable criteria (60% WP + .1% Distribution)
	 * @return
	 */
	public double getTradeWinPercent() {
		double percentCorrectThreshold = .6;
		double distributionThreshold = .001;
		
		double s = 0;
		double d = 0;
		for (int a = 0; a < testBucketPercentCorrect.length; a++) {
			if (testBucketPercentCorrect[a] >= percentCorrectThreshold && testBucketDistribution[a] >= distributionThreshold) {
				s += testBucketPercentCorrect[a] * testBucketDistribution[a];
				d += testBucketDistribution[a];
			}
		}
		
		return s / d;
	}
	
	/** 
	 * Finds the percentage of trades that should be tradable, given the bucket distributions on the test set.
	 * @return
	 */
	public double getTradePercent() {
		double percentCorrectThreshold = .6;
		double distributionThreshold = .001;
		
		double d = 0;
		for (int a = 0; a < testBucketDistribution.length; a++) {
			if (testBucketPercentCorrect[a] >= percentCorrectThreshold && testBucketDistribution[a] >= distributionThreshold) {
				d += testBucketDistribution[a];
			}
		}
		
		return d;
	}

	public static ArrayList<HashMap<String, Object>> convertCollection(Collection collection) {
	    ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	    for (Object element : collection) {
	        list.add(getValues(element));
	    }	
	    return list;
	}

	public static HashMap<String, Object> getValues(Object o)  {
	    HashMap<String, Object> values = new HashMap<String, Object>();
	    try {
		    BeanInfo info = Introspector.getBeanInfo(o.getClass());
		    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
		        // This will access public properties through getters
		        Method getter = pd.getReadMethod();
		        if (getter != null) {
		        	if (!pd.getName().equals("class")) {
		        		String field = pd.getName();
		        		Object oValue = getter.invoke(o);
		        		String className = pd.getPropertyType().toString();
		        		if (oValue != null) {
		        			if (className.equals("class java.util.Calendar")) {
		        				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		        				values.put(field, sdf.format(((Calendar)oValue).getTime()));
		        			}
		        			else if (className.equals("class data.BarKey")) {
		        				values.put("symbol", ((BarKey)oValue).symbol);
		        				values.put("duration", ((BarKey)oValue).duration.toString());
		        			}
		        			else if (className.equals("double")) {
		        				if (!Double.isInfinite((Double)oValue) && !Double.isNaN((Double)oValue)) {
		        					values.put(field, oValue);
		        				}
		        				else {
		        					values.put(field, oValue.toString());
		        				}
		        			}
		        			else if (className.equals("float")) {
		        				if (!Float.isInfinite((Float)oValue) && !Float.isNaN((Float)oValue)) {
		        					values.put(field, oValue);
		        				}
		        				else {
		        					values.put(field, oValue.toString());
		        				}
		        			}
		        			else if (className.equals("int")) {
		        				values.put(field, oValue);
		        			}
		        			else if (className.equals("boolean")) {
		        				values.put(field, oValue);
		        			}
		        			else if (className.equals("class java.lang.String")) {
		        				values.put(field, oValue.toString());
		        			}
		        			else if (className.equals("class java.util.ArrayList")) {
		        				values.put(field, oValue.toString());
		        			}
		        			else {
		        				values.put(field, oValue.toString());
		        			}
		        		}
		        		else {
		        			values.put(field, null);
		        		}
		        	}
		        }
		            
		    }
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return values;
	}
}
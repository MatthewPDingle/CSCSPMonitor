package data;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

public class Model {

	public int id = -1;
	public String type;
	public String modelFile;
	public String algo;
	public String params;
	public BarKey bk;
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
	
	public boolean favorite;
	
	public String lastActionPrice = "";
	public String lastAction = "";
	public Calendar lastActionTime = null;
	public String lastTargetClose = "";
	public String lastStopClose = "";

	public Model(String type, String modelFile, String algo, String params, BarKey bk, boolean interBarData, ArrayList<String> metrics,
			Calendar trainStart, Calendar trainEnd, Calendar testStart, Calendar testEnd, String sellMetric,
			float sellMetricValue, String stopMetric, float stopMetricValue, int numBars, int trainDatasetSize,
			int trainTrueNegatives, int trainFalseNegatives, int trainFalsePositives, int trainTruePositives,
			double trainTruePositiveRate, double trainFalsePositiveRate, double trainCorrectRate, double trainKappa,
			double trainMeanAbsoluteError, double trainRootMeanSquaredError, double trainRelativeAbsoluteError,
			double trainRootRelativeSquaredError, double trainROCArea, int testDatasetSize, int testTrueNegatives,
			int testFalseNegatives, int testFalsePositives, int testTruePositives, double testTruePositiveRate,
			double testFalsePositiveRate, double testCorrectRate, double testKappa, double testMeanAbsoluteError,
			double testRootMeanSquaredError, double testRelativeAbsoluteError, double testRootRelativeSquaredError,
			double testROCArea, boolean favorite) {
		super();
		this.type = type;
		this.modelFile = modelFile;
		this.algo = algo;
		this.params = params;
		this.bk = bk;
		this.interBarData = interBarData;
		this.metrics = metrics;
		this.trainStart = trainStart;
		this.trainEnd = trainEnd;
		this.testStart = testStart;
		this.testEnd = testEnd;
		this.sellMetric = sellMetric;
		this.sellMetricValue = sellMetricValue;
		this.stopMetric = stopMetric;
		this.stopMetricValue = stopMetricValue;
		this.numBars = numBars;
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
		this.favorite = favorite;
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

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public double getTrainWinPercent() {
		if (trainTruePositiveRate + trainFalsePositiveRate == 0) {
			return 0;
		}
		return trainTruePositives / (double)(trainTruePositives + trainFalsePositives);
	}

	public double getTestWinPercent() {
		if (testTruePositiveRate + testFalsePositiveRate == 0) {
			return 0;
		}
		return testTruePositives / (double)(testTruePositives + testFalsePositives);
	}
	
	public int getTestNumOpportunities() {
		return testTruePositives + testFalseNegatives;
	}
	
	public double getTestEstimatedAverageReturn() {
		return (sellMetricValue * getTestWinPercent()) - (stopMetricValue * (1 - getTestWinPercent()));
	}
	
	public double getMultiplier() {
		return sellMetricValue / stopMetricValue;
	}
	
	public double getTestReturnPower() {
		return getTestNumOpportunities() * testTruePositiveRate * getTestEstimatedAverageReturn();
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
		        				if (Double.isFinite((Double)oValue)) {
		        					values.put(field, oValue);
		        				}
		        				else {
		        					values.put(field, oValue.toString());
		        				}
		        			}
		        			else if (className.equals("float")) {
		        				if (Float.isFinite((Float)oValue)) {
		        					values.put(field, oValue);
		        				}
		        				else {
		        					values.put(field, oValue.toString());
		        				}
		        			}
		        			else if (className.equals("int")) {
		        				values.put(field, oValue);
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
package data;

import java.util.Calendar;

import constants.Constants.BAR_SIZE;

public class Metric {
	
	// Essentials
	public String name;
	public String symbol;
	public Calendar start;
	public Calendar end;
	public BAR_SIZE duration;
	public Float value;
	
	public boolean calculated = false;
	
	// Auxiliary
	private double volume = 0;
	private float adjOpen = 0f;
	private float adjClose = 0f;
	private float adjHigh = 0f;
	private float adjLow = 0f;
	private float gap = 0f;
	private float change = 0f;
	private float alphaClose = 0f;
	private float alphaChange = 0f;

	public Metric(String symbol, Calendar start, Calendar end, BAR_SIZE duration, double volume, float adjOpen, float adjClose,
			float adjHigh, float adjLow, float gap, float change, float alphaClose, float alphaChange) {
		super();
		this.symbol = symbol;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.volume = volume;
		this.adjOpen = adjOpen;
		this.adjClose = adjClose;
		this.adjHigh = adjHigh;
		this.adjLow = adjLow;
		this.gap = gap;
		this.change = change;
		this.alphaClose = alphaClose;
		this.alphaChange = alphaChange;
	}
	
	public Metric(String name, String symbol, Calendar start, Calendar end, BAR_SIZE duration, double volume, float adjOpen, float adjClose,
			float adjHigh, float adjLow, float gap, float change, float alphaClose, float alphaChange) {
		super();
		this.name = name;
		this.symbol = symbol;
		this.start = start;
		this.end = end;
		this.duration = duration;
		this.volume = volume;
		this.adjOpen = adjOpen;
		this.adjClose = adjClose;
		this.adjHigh = adjHigh;
		this.adjLow = adjLow;
		this.gap = gap;
		this.change = change;
		this.alphaClose = alphaClose;
		this.alphaChange = alphaChange;
	}
	
	@Override
	public String toString() {
		String s = "*** METRIC *** \n";
		s += "Name: " + name + "\n";
		s += "Symbol: " + symbol + "\n";
		s += "Start: " + start.getTime().toString() + "\n";
		s += "End: " + end.getTime().toString() + "\n";
		s += "Duration: " + duration.toString() + "\n";
		s += "Value: " + value + "\n";
		return s;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public float getAdjOpen() {
		return adjOpen;
	}

	public void setAdjOpen(float adjOpen) {
		this.adjOpen = adjOpen;
	}

	public float getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(float adjClose) {
		this.adjClose = adjClose;
	}

	public float getAdjHigh() {
		return adjHigh;
	}

	public void setAdjHigh(float adjHigh) {
		this.adjHigh = adjHigh;
	}

	public float getAdjLow() {
		return adjLow;
	}

	public void setAdjLow(float adjLow) {
		this.adjLow = adjLow;
	}

	public float getAlphaAdjClose() {
		return alphaClose;
	}

	public void setAlphaAdjClose(float spyAdjClose) {
		this.alphaClose = spyAdjClose;
	}

	public float getGap() {
		return gap;
	}

	public void setGap(float gap) {
		this.gap = gap;
	}

	public float getChange() {
		return change;
	}

	public void setChange(float change) {
		this.change = change;
	}

	public float getAlphaChange() {
		return alphaChange;
	}

	public void setAlphaChange(float spyChange) {
		this.alphaChange = spyChange;
	}
}
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
	public Double value;
	
	public boolean calculated = false;
	
	// Auxiliary
	private double volume = 0;
	private double adjOpen = 0f;
	private double adjClose = 0f;
	private double adjHigh = 0f;
	private double adjLow = 0f;
	private double gap = 0f;
	private double change = 0f;
	private double alphaClose = 0f;
	private double alphaChange = 0f;

	public Metric(String symbol, Calendar start, Calendar end, BAR_SIZE duration, double volume, double adjOpen, double adjClose,
			double adjHigh, double adjLow, double gap, double change, double alphaClose, double alphaChange) {
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
	
	public Metric(String name, String symbol, Calendar start, Calendar end, BAR_SIZE duration, double volume, double adjOpen, double adjClose,
			double adjHigh, double adjLow, double gap, double change, double alphaClose, double alphaChange) {
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

	public double getAdjOpen() {
		return adjOpen;
	}

	public void setAdjOpen(double adjOpen) {
		this.adjOpen = adjOpen;
	}

	public double getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(double adjClose) {
		this.adjClose = adjClose;
	}

	public double getAdjHigh() {
		return adjHigh;
	}

	public void setAdjHigh(double adjHigh) {
		this.adjHigh = adjHigh;
	}

	public double getAdjLow() {
		return adjLow;
	}

	public void setAdjLow(double adjLow) {
		this.adjLow = adjLow;
	}

	public double getAlphaAdjClose() {
		return alphaClose;
	}

	public void setAlphaAdjClose(double spyAdjClose) {
		this.alphaClose = spyAdjClose;
	}

	public double getGap() {
		return gap;
	}

	public void setGap(double gap) {
		this.gap = gap;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public double getAlphaChange() {
		return alphaChange;
	}

	public void setAlphaChange(double spyChange) {
		this.alphaChange = spyChange;
	}
}
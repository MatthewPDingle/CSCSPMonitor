package data;

import java.util.Calendar;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import utils.CalendarUtils;

public class Bar {

	public String symbol;
	public double open;
	public double close;
	public double high;
	public double low;
	public Double vwap;
	public double volume;
	public Integer numTrades;
	public Double change;
	public Double gap;
	public Calendar periodStart;
	public Calendar periodEnd;
	public Constants.BAR_SIZE duration;
	public boolean partial;
	
	public double changeAtTarget;

	public Bar(String symbol, double open, double close, double high, double low,
			Double vwap, double volume, Integer numTrades, Double change, Double gap,
			Calendar periodStart, Calendar periodEnd, BAR_SIZE duration,
			boolean partial) {
		super();
		this.symbol = symbol;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.vwap = vwap;
		this.volume = volume;
		this.numTrades = numTrades;
		this.change = change;
		this.gap = gap;
		this.periodStart = Calendar.getInstance();
		if (periodStart != null) {
			this.periodStart.setTimeInMillis(periodStart.getTimeInMillis());
		}
		this.periodEnd = Calendar.getInstance();
		if (periodEnd != null) {
			this.periodEnd.setTimeInMillis(periodEnd.getTimeInMillis());
		}
		this.duration = duration;
		this.partial = partial;
	}
	
	public Bar(Bar b) {
		super();
		if (b != null) {
			this.symbol = b.symbol;
			this.open = b.open;
			this.close = b.close;
			this.high = b.high;
			this.low = b.low;
			this.vwap = b.vwap;
			this.volume = b.volume;
			this.numTrades = b.numTrades;
			this.change = b.change;
			this.gap = b.gap;
			this.periodStart = Calendar.getInstance();
			if (b.periodStart != null) {
				this.periodStart.setTimeInMillis(b.periodStart.getTimeInMillis());
			}
			this.periodEnd = Calendar.getInstance();
			if (b.periodEnd != null) {
				this.periodEnd.setTimeInMillis(b.periodEnd.getTimeInMillis());
			}
			this.duration = b.duration;
			this.partial = b.partial;
		}
	}

	@Override
	public String toString() {
		String s = "*** BAR *** \n";
		s += "Symbol: " + symbol + "\n";
		s += "Open: " + open + "\n";
		s += "Close: " + close + "\n";
		s += "High: " + high + "\n";
		s += "Low: " + low + "\n";
		s += "VWAP: " + vwap + "\n";
		s += "Volume: " + volume + "\n";
		s += "#Trades: " + numTrades + "\n";
		s += "Change: " + String.format("%.6f", change) + "\n";
		s += "Gap: " + String.format("%.6f", gap) + "\n";
		s += "Start: " + periodStart.getTime().toString() + "\n";
		s += "End: " + periodEnd.getTime().toString() + "\n";
		s += "Duration: " + duration.toString() + "\n";
		s += "Partial: " + partial;
		return s;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof Bar) {
			Bar b = (Bar)o;
			if (this.symbol.equals(b.symbol) && this.duration == b.duration && CalendarUtils.areSame(this.periodStart, b.periodStart)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(symbol).append(duration).append(periodStart).toHashCode();
	}
}
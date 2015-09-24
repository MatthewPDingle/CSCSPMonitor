package data;

import java.util.Calendar;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import constants.Constants;
import constants.Constants.BAR_SIZE;
import utils.CalendarUtils;

public class Bar {

	public String symbol;
	public float open;
	public float close;
	public float high;
	public float low;
	public Float vwap;
	public float volume;
	public Integer numTrades;
	public Float change;
	public Float gap;
	public Calendar periodStart;
	public Calendar periodEnd;
	public Constants.BAR_SIZE duration;
	public boolean partial;

	public Bar(String symbol, float open, float close, float high, float low,
			Float vwap, float volume, Integer numTrades, Float change, Float gap,
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
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.duration = duration;
		this.partial = partial;
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
		s += "Change: " + change + "\n";
		s += "Gap: " + gap + "\n";
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
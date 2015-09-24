package data;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import constants.Constants.BAR_SIZE;

/**
 * Not a Primary Key for the Metrics table.  Just a unique name, symbol, duration combo
 * 
 */
public final class MetricKey {

	public final String name;
	public final String symbol;
	public final BAR_SIZE duration;
	
	public MetricKey(String name, String symbol, BAR_SIZE duration) {
		super();
		this.name = name;
		this.symbol = symbol;
		this.duration = duration;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof MetricKey) {
			MetricKey m = (MetricKey)o;
			if (this.name.equals(m.name) && this.symbol.equals(m.symbol) && this.duration == m.duration) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(name).append(symbol).append(duration).toHashCode();
	}

	@Override
	public String toString() {
		return symbol + " - " + name + " - " + duration.toString();
	}
}
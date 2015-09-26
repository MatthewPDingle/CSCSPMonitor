package data;

import java.util.Calendar;

public class MetricTimeCache {

	public Calendar minStart;
	public Calendar maxStart;
	
	public MetricTimeCache(Calendar minStart, Calendar maxStart) {
		super();
		this.minStart = minStart;
		this.maxStart = maxStart;
	}
		
}
package data;

import java.util.Comparator;

public class BarComparator implements Comparator<Bar> {

	@Override
	public int compare(Bar bar1, Bar bar2) {
		int value = 0;
		try {
			if (bar1.periodStart.getTimeInMillis() > bar2.periodStart.getTimeInMillis()) {
				value = 1;
			}
			else if (bar1.periodStart.getTimeInMillis() < bar2.periodStart.getTimeInMillis()) {
				value = -1;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}	
}
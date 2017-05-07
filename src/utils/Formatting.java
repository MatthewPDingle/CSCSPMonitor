package utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Formatting {

	public static SimpleDateFormat sdfMMDDYYYY_HHMMSS = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	public static SimpleDateFormat sdfYYYYMMDD_HHMMSS = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	public static SimpleDateFormat sdfYYYYMMDD__HHMMSS = new SimpleDateFormat("yyyyMMdd  HH:mm:ss");
	public static SimpleDateFormat sdfMMDDYYYY = new SimpleDateFormat("MM/dd/yyyy");
	public static SimpleDateFormat sdfHHMMSS = new SimpleDateFormat("HH:mm:ss");
	
	public static DecimalFormat df8 = new DecimalFormat("#.########");
	public static DecimalFormat df6 = new DecimalFormat("#.######");
	public static DecimalFormat df5 = new DecimalFormat("#.#####");
	public static DecimalFormat df4 = new DecimalFormat("#.####");
	public static DecimalFormat df3 = new DecimalFormat("#.###");
	public static DecimalFormat df2 = new DecimalFormat("#.##");
	public static DecimalFormat df1 = new DecimalFormat("#.#");
}
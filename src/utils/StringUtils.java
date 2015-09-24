package utils;

public class StringUtils {

	/**
	 * Checks to see if the string is null, empty, or written out as "null"
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null) {
			return true; 
		}
		String tempStr = str.trim(); 
		if (tempStr.length() == 0) {
			return true; 
		}
		if (tempStr.toLowerCase().equals("null")) {
			return true;
		}
		return false; 
	}
}
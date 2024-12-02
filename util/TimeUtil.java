package uk.co.dlineradio.nhr.blackberryapp.util;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.global.Formatter;

import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;

import net.rim.device.api.system.EventLogger;

/**
 * Time utility methods.
 * @author Marc Steele
 */

public class TimeUtil {
	
	private static final int YEAR_OFFSET = 0;
	private static final int YEAR_LENGTH = 4;
	private static final int MONTH_OFFSET = 5;
	private static final int MONTH_LENGTH = 2;
	private static final int DAY_OFFSET = 8;
	private static final int DAY_LENGTH = 2;
	private static final int HOUR_OFFSET = 11;
	private static final int HOUR_LENGTH = 2;
	private static final int MINUTE_OFFSET = 14;
	private static final int MINUTE_LENGTH = 2;
	
	/**
	 * Derives a time object from a JSON string.
	 * @param json_string The JSON string to convert.
	 * @return The converted date if successful. Otherwise NULL.
	 */
	
	public static Date dateFromJSONString(String json_string) {
		
		// Sanity check
		
		if (json_string == null || json_string.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to convert a NULL JSON date. Aborting.".getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Perform the conversion
		
		try {
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Integer.parseInt(json_string.substring(YEAR_OFFSET, YEAR_OFFSET + YEAR_LENGTH)));
			cal.set(Calendar.MONTH, Integer.parseInt(json_string.substring(MONTH_OFFSET, MONTH_OFFSET + MONTH_LENGTH)));
			cal.set(Calendar.DATE, Integer.parseInt(json_string.substring(DAY_OFFSET, DAY_OFFSET + DAY_LENGTH)));
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(json_string.substring(HOUR_OFFSET, HOUR_OFFSET + HOUR_LENGTH)));
			cal.set(Calendar.MINUTE, Integer.parseInt(json_string.substring(MINUTE_OFFSET, MINUTE_OFFSET + MINUTE_LENGTH)));
			cal.set(Calendar.SECOND, 0);
			return cal.getTime();
			
		} catch (Exception ex) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an error trying to convert a date from a JSON string. Reason: {0}", new String[] { ex.getMessage() }).getBytes(), EventLogger.ERROR);
			return null;
		}
		
	}

}

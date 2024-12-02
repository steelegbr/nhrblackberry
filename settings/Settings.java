package uk.co.dlineradio.nhr.blackberryapp.settings;

import javax.microedition.global.Formatter;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.rim.device.api.system.EventLogger;
import uk.co.dlineradio.nhr.blackberryapp.util.HTTPUtil;

/**
 * Holds the application wide settings.
 * @author Marc Steele
 */

public class Settings {
	
	private static Settings settings = null;
	private static String SETTINGS_URL = "http://phoneapps.nhradio.org.uk/settings/blackberry.json";
	
	private static final long GUID = 0x6fc7ed46;
	private static final String APP_NAME = "NottsNHR";
	
	private String stream_url = null;
	private String photo_path_base = null;
	private String now_playing_url = null;
	private String on_air_url = null;
	private String song_rating_url = null;
	private int song_update_rate = 60000;
	private String studio_number = null;
	private String request_url = null;
	private String facebook_url = null;
	
	/**
	 * Follows singleton pattern.
	 */
	
	private Settings() {}
	
	/**
	 * Obtains the current application settings.
	 * @return The current settings. Well, assuming we downloaded them OK.
	 */
	
	public static Settings get() {
		
		if (settings == null) {
			
			// Load from the website
			
			String raw_json = HTTPUtil.retrieveDataFromURL(SETTINGS_URL);
			if (raw_json != null) {
				
				// Success
				
				settings = Settings.inflateSettings(raw_json);
				
			}
			
		}
		
		return settings;
		
	}
	
	/**
	 * Obtains the URL the stream is on.
	 * @return The URL the stream is on.
	 */

	public String getStream_url() {
		return stream_url;
	}

	/**
	 * Sets the URL the stream is on.
	 * @param stream_url The url the stream is on.
	 */
	
	public void setStream_url(String stream_url) {
		this.stream_url = stream_url;
	}
	
	/**
	 * Obtains the base URL for photos.
	 * @return The base URL for photos.
	 */

	public String getPhoto_path_base() {
		return photo_path_base;
	}
	
	/**
	 * Sets the base URL for photos.
	 * @param photo_path_base The base URL for photos.
	 */

	public void setPhoto_path_base(String photo_path_base) {
		this.photo_path_base = photo_path_base;
	}
	
	/**
	 * Obtains the now playing URL.
	 * @return The now playing URL.
	 */

	public String getNow_playing_url() {
		return now_playing_url;
	}
	
	/**
	 * Sets the now playing URL.
	 * @param now_playing_url The now playing URL.
	 */

	public void setNow_playing_url(String now_playing_url) {
		this.now_playing_url = now_playing_url;
	}
	
	/**
	 * Obtains the who's on air URL.
	 * @return The who's on air URL.
	 */

	public String getOn_air_url() {
		return on_air_url;
	}
	
	/**
	 * Sets the who's on air URL.
	 * @param on_air_url The who's on air URL.
	 */

	public void setOn_air_url(String on_air_url) {
		this.on_air_url = on_air_url;
	}
	
	/**
	 * Obtains the song rating URL.
	 * @return The song rating URL.
	 */

	public String getSong_rating_url() {
		return song_rating_url;
	}
	
	/**
	 * Sets the song rating URL.
	 * @param song_rating_url The song rating URL.
	 */

	public void setSong_rating_url(String song_rating_url) {
		this.song_rating_url = song_rating_url;
	}
	
	/**
	 * Retrives the time to wait between now playing updates.
	 * @return The time to wait in ms.
	 */

	public int getSong_update_rate() {
		return song_update_rate;
	}
	
	/**
	 * Sets the time to wait between now playing updates.
	 * @param song_update_rate The time to wait in ms.
	 */

	public void setSong_update_rate(int song_update_rate) {
		this.song_update_rate = song_update_rate;
	}
	
	/**
	 * Obtains the studio phone number.
	 * @return The studio phone number.
	 */

	public String getStudio_number() {
		return studio_number;
	}
	
	/**
	 * Sets the studio phone number.
	 * @param studio_number The studio phone number.
	 */

	public void setStudio_number(String studio_number) {
		this.studio_number = studio_number;
	}
	
	/**
	 * Obtains the URL to post requests to.
	 * @return The URL.
	 */

	public String getRequest_url() {
		return request_url;
	}
	
	/**
	 * Sets the URL to post requests to.
	 * @param request_url The URL.
	 */

	public void setRequest_url(String request_url) {
		this.request_url = request_url;
	}
	
	/**
	 * Retrieves the station Facebook URL.
	 * @return The station facebook URL.
	 */

	public String getFacebook_url() {
		return facebook_url;
	}
	
	/**
	 * Sets the station Facebook URL.
	 * @param facebook_url The station Facebook URL.
	 */

	public void setFacebook_url(String facebook_url) {
		this.facebook_url = facebook_url;
	}
	

	
	/**
	 * Obtains the GUID for this application.
	 * @return The GUID.
	 */

	public static long getGUID() {
		return GUID;
	}
	
	/**
	 * Obtains the app name.
	 * @return The app name.
	 */
	
	public static String getAppName() {
		return APP_NAME;
	}
	
	/**
	 * Inflates a settings object from raw JSON.
	 * @param json The raw JSON.
	 * @return The application settings if the process was successful.
	 */

	private static Settings inflateSettings(String json) {

		// Sanity check
		
		if (json == null || json.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to inflate the application settings from NULL JSON.".getBytes(), EventLogger.ERROR);
			return null;
		}
		
		try {
			
			JSONObject json_obj = new JSONObject(json);
			Settings settings = new Settings();
			
			// Fill in the fields
			
			settings.setStream_url(json_obj.getString("stream_url"));
			settings.setPhoto_path_base(json_obj.getString("photo_base_path"));
			settings.setNow_playing_url(json_obj.getString("now_playing_url"));
			settings.setOn_air_url(json_obj.getString("on_air_url"));
			settings.setSong_rating_url(json_obj.getString("song_rating_url"));
			settings.setStudio_number(json_obj.getString("studio_number"));
			settings.setRequest_url(json_obj.getString("request_url"));
			settings.setFacebook_url(json_obj.getString("facebook_url"));
						
			return settings;
			
		} catch (JSONException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an issue trying to inflate the settings from JSON. Reason: Reason: {0}", new String[] {e.getMessage()}).getBytes(), EventLogger.ERROR);
			return null;
		}
		

	}
	
}

package uk.co.dlineradio.nhr.blackberryapp.station;

import javax.microedition.global.Formatter;

import net.rim.device.api.system.EventLogger;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;
import uk.co.dlineradio.nhr.blackberryapp.util.HTTPUtil;

/**
 * Represents a song that plays on the station.
 * @author Marc Steele
 */

public class Song {

	private int id = -1;
	private String artist = null;
	private String title = null;
	private boolean rated = false;
	
	/**
	 * Obtains the unique ID of the song.
	 * @return The unique ID of the song.
	 */
	
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the unique ID of the song.
	 * @param id The unique ID of the song.
	 */
	
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Obtains the artist name.
	 * @return The artist name.
	 */
	
	public String getArtist() {
		return artist;
	}
	
	/**
	 * Sets the artist name.
	 * @param artist The artist name.
	 */
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	/**
	 * Obtains the song title.
	 * @return The song title.
	 */
	
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the song title.
	 * @param title The song title.
	 */
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Indicates if the user has already rated the song.
	 * @return TRUE if the have. Otherwise FALSE.
	 */
	
	public boolean isRated() {
		return rated;
	}
	
	/**
	 * Sets the flag indicating if the user has already rated the song.
	 * @param rated TRUE if they have. Otherwise FALSE.
	 */
	
	public void setRated(boolean rated) {
		this.rated = rated;
	}
	
	/**
	 * Retrieves the currently playing song.
	 * @return The currently playing song.
	 */
	
	public static Song getCurrentSong() {
		
		// Retrieve the raw JSON from the server
		
		Settings settings = Settings.get();
		if (settings == null) {
			EventLogger.logEvent(Settings.getGUID(), "Failed to retrieve the currently playing song as we got a NULL settings object.".getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Retrieve the data
		
		String raw_json = HTTPUtil.retrieveDataFromURL(Settings.get().getNow_playing_url());
		if (raw_json == null || raw_json.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to retrieve the currently playing song as we got nothing from {0}", new String[] { settings.getNow_playing_url() }).getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Now inflate the song
		
		Song current_song = null;
		
		try {
			
			JSONObject json = new JSONObject(raw_json);
			JSONObject song_json = json.getJSONObject("song");
			
			current_song = new Song();
			current_song.setId(song_json.getInt("id"));
			current_song.setArtist(song_json.getString("given_artist"));
			current_song.setTitle(song_json.getString("name"));
			current_song.setRated(false);
			
		} catch (JSONException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into a JSON error trying to retrieve the current song from {0}. Reason: Reason: {1}", new String[] { settings.getNow_playing_url(), e.getMessage()}).getBytes(), EventLogger.ERROR);
			return null;
		}
		
		return current_song;
		
	}
	
}

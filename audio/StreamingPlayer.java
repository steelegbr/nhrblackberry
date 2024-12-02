package uk.co.dlineradio.nhr.blackberryapp.audio;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.global.Formatter;
import javax.microedition.io.HttpConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.EventLogger;
import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;

/**
 * Streaming audio player.
 * @author Marc Steele
 */

public class StreamingPlayer implements PlayerListener {

	private static StreamingPlayer current_player = null;
	
	// Player states
	
	public static int STATE_PLAYING = 0;
	public static int STATE_STOPPED = 1;
	public static int STATE_ERROR = 2;
	public static int STATE_LOADING = 3;
	
	// Private properties
	
	Player player = null;
	private int state = STATE_STOPPED;
	private boolean resume_playback = false;
	
	/**
	 * Follows singleton pattern.
	 */
	
	private StreamingPlayer() { }
	
	/**
	 * Retreaves the current streaming audio player.
	 * @return The current player.
	 */
	
	public static StreamingPlayer get() {
		
		if (current_player == null) {
			current_player = new StreamingPlayer();
		}
		
		return current_player;
		
	}
	
	/**
	 * Obtains the current player state.
	 * @return The current player state.
	 */
	
	public int getState() {
		
		if (this.state == StreamingPlayer.STATE_PLAYING && this.player.getState() != Player.STARTED) {
			return StreamingPlayer.STATE_ERROR;
		}
		
		return this.state;
		
	}
	
	/**
	 * Plays the player.
	 */
	
	public void play() {
		
		if (this.state != StreamingPlayer.STATE_PLAYING) {
			
			// Load the URL from settings
			
			Settings settings = Settings.get();
			if (settings == null) {
				EventLogger.logEvent(Settings.getGUID(), "Failed to read in the settings for launching the audio player.".getBytes(), EventLogger.ERROR);
				this.state = StreamingPlayer.STATE_ERROR;
				return;
			}
			
			try {
				
				this.state = StreamingPlayer.STATE_LOADING;
				
				// Generate a data source
				
				ConnectionFactory conn_fact = new ConnectionFactory();
				ConnectionDescriptor conn_desc = conn_fact.getConnection(settings.getStream_url());
				if (conn_desc == null) {
					EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to get a connnection to {0} from the factory.", new String[] { settings.getStream_url() }).getBytes(), EventLogger.ERROR);
					return;
				}
				
				HttpConnection http_conn = (HttpConnection) conn_desc.getConnection();
				InputStream audio_data_stream = http_conn.openInputStream();
				
				// Load the player
				
				this.player = Manager.createPlayer(audio_data_stream, "audio/mp3");
				this.player.start();
				this.player.addPlayerListener(this);
				
				if (this.player.getState() == Player.STARTED) {
					this.state = StreamingPlayer.STATE_PLAYING;
				} else {
					this.state = StreamingPlayer.STATE_ERROR;
				}
				
			} catch (IOException e) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an IO exception trying to start the audio player. Reason: {0}", new String[] {e.getMessage()}).getBytes(), EventLogger.ERROR);
				this.state = StreamingPlayer.STATE_ERROR;
			} catch (MediaException e) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an media issue trying to start the audio player. Reason: {0}", new String[] {e.getMessage()}).getBytes(), EventLogger.ERROR);
				this.state = StreamingPlayer.STATE_ERROR;
			}
			
		}
		
	}
	
	/**
	 * Stops the player.
	 */
	
	public void stop() {
		
		if (this.state == StreamingPlayer.STATE_PLAYING) {
			
			try {
				this.player.stop();
			} catch (MediaException e) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into a wee issue stopping the player. Typical. However, the resong was Reason: {0}", new String[] {e.getMessage()}).getBytes(), EventLogger.ERROR);
			}
			
			this.player.close();
			this.player = null;
			
		}
		
		this.state = StreamingPlayer.STATE_STOPPED;
		
	}
	
	/**
	 * Clears the error state.
	 */
	
	public void clearError() {
		
		if (this.state == StreamingPlayer.STATE_ERROR) {
			this.state = StreamingPlayer.STATE_STOPPED;
		}		
		
	}

	public void playerUpdate(Player player, String event, Object eventData) {
		
		if (event == PlayerListener.DEVICE_UNAVAILABLE) {
			
			// Stop the player as necessary
			
			if (this.state == StreamingPlayer.STATE_PLAYING) {
				
				this.resume_playback = true;
				
				try {
					this.player.stop();
				} catch (MediaException e) {
					EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an error temporarily stopping the player. Reason: {0}", new String[] { e.getMessage() }).getBytes(), EventLogger.ERROR);
				}
				
			} else {
				this.resume_playback = false;
			}
			
		} else if (event == PlayerListener.DEVICE_AVAILABLE) {
			
			// Resume as required
			
			if (this.resume_playback) {
				
				try {
					this.player.start();
				} catch (MediaException e) {
					EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an error resuming the player. Reason: {0}", new String[] { e.getMessage() }).getBytes(), EventLogger.ERROR);
				}
				
				this.resume_playback = false;
				
			}
			
		}
	}
	
}

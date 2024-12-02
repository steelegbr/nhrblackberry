package uk.co.dlineradio.nhr.blackberryapp.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;

import javax.microedition.global.Formatter;

import uk.co.dlineradio.nhr.blackberryapp.audio.StreamingPlayer;
import uk.co.dlineradio.nhr.blackberryapp.network.WifiNetworkHandler;
import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;
import uk.co.dlineradio.nhr.blackberryapp.station.Show;
import uk.co.dlineradio.nhr.blackberryapp.station.Song;
import uk.co.dlineradio.nhr.blackberryapp.util.HTTPUtil;
import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.RadioException;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * A class extending the MainScreen class, which provides default standard
 * behavior for BlackBerry GUI applications.
 */
public final class MainTabScreen extends MainScreen implements Runnable, FocusChangeListener, FieldChangeListener
{
	
	private static final int TAB_COUNT = 3;
	private static final String[] TAB_NAMES = {"On Air", "Make a Request", "Facebook"};
	private static final String SPACER_TEXT = " | ";
	private static final int ON_AIR_TAB = 0;
	private static final int MAKE_REQUEST_TAB = 1;
	private static final int FACEBOOK_TAB = 2;
	private static final int THREAD_SLEEP_TIME = 1000;
	
	private boolean run_thread = false;
	private Show current_show = null;
	private Show next_show = null;
	private Song current_song = null;
	private long last_song_update = -1;
	private WifiNetworkHandler wifilistener = new WifiNetworkHandler();
	
	// Font Sizes
	
	private static final int FONT_SIZE_ON_AIR_LABEL = 18;
	private static final int FONT_SIZE_NOW_PLAYING_LABEL = 18;
	private static final int FONT_SIZE_CALL_STUDIO_LABEL = 18;
	private static final int FONT_SIZE_REQUEST_ONLINE = 18;
	
	// Tab controls
	
	private Thread updater_thread = null;
	private LabelField[] tabs = new LabelField[TAB_COUNT];
	private LabelField[] spaces = new LabelField[TAB_COUNT - 1];
	private VerticalFieldManager[] manager = new VerticalFieldManager[TAB_COUNT];
	private VerticalFieldManager current_tab = null;
	
	// On Air Controls
	
	BitmapField show_photo_field = null;
	RichTextField show_on_air = null;
	ButtonField listen_live_button = null;
	RichTextField now_playing = null;
	ButtonField rate_positive = null;
	ButtonField rate_negative = null;
	ButtonField rate_why = null;
	
	// Make a request controls
	
	ButtonField call_studio_button = null;
	BasicEditField request_name = null;
	BasicEditField request_request = null;
	BasicEditField request_message = null;
	ButtonField request_submit = null;
	
	// Facebook controls
	
	ButtonField facebook_launch = null;
	
    /**
     * Creates a new MainTabScreen object
     */
	
    public MainTabScreen() {      
    	
    	// GUI basics
    	
        this.setTitle("NHR");
        
        // Setup the tabs
        
        this.generateTabBar();
        this.generateOnAirTab();
        this.generateRequestTab();
        this.generateFacebookTab();
        
        // Select the default tab
        
        this.selectTab(ON_AIR_TAB);    
        
        // Launch the thread
        
        this.run_thread = true;
        this.updater_thread = new Thread(this);
        this.updater_thread.start(); 
        
    }
    
    /**
     * Generate the Facebook tab.
     */

	private void generateFacebookTab() {
		
		// Load the Facebook button
		
		this.manager[FACEBOOK_TAB] = new VerticalFieldManager();
		this.facebook_launch = new ButtonField(Strings.DEFAULT_FACEBOOK_BUTTON, ButtonField.CONSUME_CLICK) {
        	public int getPreferredWidth() {
        		return Display.getWidth();
        	}
        };
        this.facebook_launch.setChangeListener(this);
		this.manager[FACEBOOK_TAB].add(this.facebook_launch);
		
	}
	
	/**
	 * Generates the request tab.
	 */

	private void generateRequestTab() {
		
		// Setup the controls
        
        this.manager[MAKE_REQUEST_TAB] = new VerticalFieldManager();
        
        RichTextField call_studio_label = new RichTextField(Strings.DEFAULT_CALL_STUDIO_LABEL);
        this.call_studio_button = new ButtonField(Strings.DEFAULT_CALL_STUDIO_BUTTON, ButtonField.CONSUME_CLICK) {
        	public int getPreferredWidth() {
        		return Display.getWidth();
        	}
        };
        
        RichTextField online_request_label = new RichTextField(Strings.DEFAULT_INTERNET_LABEL);
        this.request_name = new BasicEditField(Strings.DEFAULT_NAME_LABEL, "");
        this.request_request = new BasicEditField(Strings.DEFAULT_REQUEST_LABEL, "");
        this.request_message = new BasicEditField(Strings.DEFAULT_MESSAGE_LABEL, "");
        this.request_submit = new ButtonField(Strings.DEFAULT_REQUEST_SUBMIT_BUTTON, ButtonField.CONSUME_CLICK);
        
        // Change any values
        
        this.call_studio_button.setChangeListener(this);
        call_studio_label.setFont(this.getFont(FONT_SIZE_CALL_STUDIO_LABEL, Font.BOLD));
        online_request_label.setFont(this.getFont(FONT_SIZE_REQUEST_ONLINE, Font.BOLD));
        this.request_submit.setChangeListener(this);
        
        // Construct the tab
        
        this.manager[MAKE_REQUEST_TAB].add(call_studio_label);
        this.manager[MAKE_REQUEST_TAB].add(this.call_studio_button);
        this.manager[MAKE_REQUEST_TAB].add(online_request_label);
        this.manager[MAKE_REQUEST_TAB].add(this.request_name);
        this.manager[MAKE_REQUEST_TAB].add(this.request_request);
        this.manager[MAKE_REQUEST_TAB].add(this.request_message);
        this.manager[MAKE_REQUEST_TAB].add(this.request_submit);
	}
	
	/**
	 * Generates the on air tab.
	 */

	private void generateOnAirTab() {
		
		// Generate the controls
        
        this.manager[ON_AIR_TAB] = new VerticalFieldManager();
        
        RichTextField on_air_label = new RichTextField(Strings.DEFAULT_ON_AIR_LABEL);
        HorizontalFieldManager on_air_area = new HorizontalFieldManager();
        this.show_photo_field = new BitmapField();
        this.show_on_air = new RichTextField(Strings.DEFAULT_NOW_ON_AIR);
        
        this.listen_live_button = new ButtonField(Strings.TEXT_NOT_LISTENING, ButtonField.CONSUME_CLICK) {
        	public int getPreferredWidth() {
        		return Display.getWidth();
        	}
        };
        
        RichTextField now_playing_label  = new RichTextField(Strings.DEFAULT_NOW_PLAYING_LABEL);
        this.now_playing = new RichTextField(Strings.DEFAULT_NOW_PLAYING);
        
        HorizontalFieldManager rating_layout = new HorizontalFieldManager();
        this.rate_positive = new ButtonField(Strings.DEFAULT_RATE_POSITIVE, ButtonField.CONSUME_CLICK);
        this.rate_negative = new ButtonField(Strings.DEFAULT_RATE_NEGATIVE, ButtonField.CONSUME_CLICK);
        this.rate_why = new ButtonField(Strings.DEFAULT_RATE_WHY, ButtonField.CONSUME_CLICK);
        
        // Change any values
        
        on_air_label.setFont(this.getFont(FONT_SIZE_ON_AIR_LABEL, Font.BOLD));
        
        Bitmap default_logo = Bitmap.getBitmapResource(Strings.DEFAULT_IMAGE);
        this.show_photo_field.setBitmap(default_logo);
        
        this.listen_live_button.setChangeListener(this);
        
        now_playing_label.setFont(this.getFont(FONT_SIZE_NOW_PLAYING_LABEL, Font.BOLD));
        
        this.rate_positive.setChangeListener(this);
        this.rate_negative.setChangeListener(this);
        this.rate_why.setChangeListener(this);
        
        // Construct the tab
        
        this.manager[ON_AIR_TAB].add(on_air_label);
        on_air_area.add(this.show_photo_field);
        on_air_area.add(this.show_on_air);
        this.manager[ON_AIR_TAB].add(on_air_area);
        this.manager[ON_AIR_TAB].add(this.listen_live_button);
        this.manager[ON_AIR_TAB].add(now_playing_label);
        this.manager[ON_AIR_TAB].add(this.now_playing);
        rating_layout.add(this.rate_positive);
        rating_layout.add(this.rate_negative);
        rating_layout.add(this.rate_why);
        this.manager[ON_AIR_TAB].add(rating_layout);
	}
	
	/**
	 * Generates the actual tab bar.
	 */

	private void generateTabBar() {
		HorizontalFieldManager tab_button_layout = new HorizontalFieldManager();
        
        for (int i = 0; i < TAB_COUNT; i++) {
        	
        	this.tabs[i] = new LabelField(TAB_NAMES[i], LabelField.FOCUSABLE | LabelField.HIGHLIGHT_SELECT);
        	this.tabs[i].setFocusListener(this);
        	tab_button_layout.add(this.tabs[i]);
        	
        	if (i < TAB_COUNT - 1) {
        		this.spaces[i] = new LabelField(SPACER_TEXT, LabelField.NON_FOCUSABLE);
        		tab_button_layout.add(this.spaces[i]);
        	}
        	
        }
        
        this.add(tab_button_layout);
        this.add(new SeparatorField());
	}

	public void run() {
		
		while (this.run_thread) {
			
			// Either if we've got no shows or the current one has expired
			// go and get some!
			
			if (this.current_show == null || this.current_show.getEnd().getTime() <= System.currentTimeMillis()) {
				
				// Retrieve the shows from the server
				
				Show[] shows = Show.getCurrentShows();
				if (shows != null && shows.length >= 2) {
					
					this.current_show = shows[0];
					this.next_show = shows[1];
					
				} else {
					
					this.current_show = null;
					this.next_show = null;
					
				}
				
			}
			
			// Current song
			
			if (!(this.current_show == null || this.current_show.isLive())) {
				
				// Only refresh if we've timed out or have no data
				
				if (this.current_song == null || this.last_song_update < 0 || System.currentTimeMillis() - this.last_song_update >= Settings.get().getSong_update_rate()) {
					
					// Retrive the next song
					
					Song new_song = Song.getCurrentSong();
					this.last_song_update = System.currentTimeMillis();
					
					if (new_song != null && (this.current_song == null || this.current_song.getId() != new_song.getId())) {
						
						this.current_song = new_song;
						
					}
					
				}
				
			} else {
				
				this.current_song = null;
				
			}
			
			// Refresh the display
			
			this.updateOnAirContent();
			
			// Sleep it off
			
			try {
				Thread.sleep(THREAD_SLEEP_TIME);
			} catch (InterruptedException e) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to sleep in updater thread. Reason: {0}", new String[]{e.getMessage()}).getBytes(), EventLogger.INFORMATION);
			}
			
		}
		
	}
	
	/**
	 * Updates the onscreen display.
	 */
	
	private void updateOnAirContent() {
		
		synchronized(UiApplication.getEventLock()) {
		
			// Now on air text
		
			try {
		
				ByteArrayOutputStream onair_baos = new ByteArrayOutputStream();
		
				if (this.current_show == null) {
			
					onair_baos.write(Strings.DEFAULT_NOW_ON_AIR.getBytes());
			
				} else {
			
					// Show name
			
					onair_baos.write("Now - ".getBytes());
					onair_baos.write(this.current_show.getName().getBytes());
				
					// Presenters
				
					if (this.current_show.isLive()) {
					
						onair_baos.write(" with ".getBytes());
						onair_baos.write(this.current_show.getPresenterString().getBytes());
					
					}
				
					// Description
				
					onair_baos.write(". ".getBytes());
					onair_baos.write(this.current_show.getDescription().getBytes());
					onair_baos.write("\n\n".getBytes());
			
				}
			
				if (this.next_show != null) {
				
					// Show start time
				
					Calendar next_show_starts = Calendar.getInstance();
					next_show_starts.setTime(this.next_show.getStart());
					int hour = next_show_starts.get(Calendar.HOUR) % 12;
					if (hour == 0) {
						hour = 12;
					}
				
					onair_baos.write("From ".getBytes());
					onair_baos.write(Integer.toString(hour).getBytes());
					if (next_show_starts.get(Calendar.MINUTE) > 0) {
					
						onair_baos.write(":".getBytes());
						onair_baos.write(Integer.toBinaryString(next_show_starts.get(Calendar.MINUTE)).getBytes());
					
					}
				
					// Show name
				
					onair_baos.write(" - ".getBytes());
					onair_baos.write(this.next_show.getName().getBytes());
				
				}
			
				// Finally, write it to the GUI
			
				this.show_on_air.setText(onair_baos.toString());
			
			} catch (IOException ex) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to generate the now on air data. Reason: {0}", new String[] {ex.getMessage()}).getBytes(), EventLogger.ERROR);
			}
			
			// Player button
			
			if (StreamingPlayer.get().getState() == StreamingPlayer.STATE_PLAYING) {
				
				this.listen_live_button.setLabel(Strings.TEXT_LISTENING);
				
			} else if (StreamingPlayer.get().getState() == StreamingPlayer.STATE_STOPPED) {
				
				this.listen_live_button.setLabel(Strings.TEXT_NOT_LISTENING);
				
			} else if (StreamingPlayer.get().getState() == StreamingPlayer.STATE_ERROR) {
				
				// Clear the error and pop up a message
				
				StreamingPlayer.get().clearError();
				Dialog.alert(Strings.MESSAGE_PLAYER_ERROR);
				
			}
			
			// Current song
			
			if (this.current_song == null) {
				
				this.now_playing.setText(Strings.DEFAULT_NOW_PLAYING);
				
			} else {
				
				try {
					
					ByteArrayOutputStream now_playing_baos = new ByteArrayOutputStream();
					now_playing_baos.write(this.current_song.getArtist().getBytes());
					now_playing_baos.write(" - ".getBytes());
					now_playing_baos.write(this.current_song.getTitle().getBytes());
					this.now_playing.setText(now_playing_baos.toString());
					
				} catch (IOException ex) {
					EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an issue trying to say what the current song is! Reason: {0}", new String[] {ex.getMessage()}).getBytes(), EventLogger.ERROR);
				}
				
			}
			
			// Show picture
			
			if (this.current_show == null || this.current_show.getPictureData() == null) {
				this.show_photo_field.setBitmap(Bitmap.getBitmapResource("icon.png"));
			} else {
				this.show_photo_field.setImage(this.current_show.getPictureData());
			}
			
		}
			
	}
	
	/**
	 * Selects a tab on the screen.
	 * @param tab_number The tab you want to select.
	 */
	
	private void selectTab(int tab_number) {
		
		if (this.current_tab != null) {
			this.delete(current_tab);
		}
		
		this.add(this.manager[tab_number]);
		this.current_tab = this.manager[tab_number];
		
	}

	public void focusChanged(Field field, int eventType) {

		if (eventType == FOCUS_GAINED) {
			
			for (int i = 0; i < TAB_COUNT; i++) {
				if (field == this.tabs[i]) {
					this.selectTab(i);
				}
			}
			
		}
		
	}
	
	/**
	 * Obtains a font.
	 * @param size The size of the font.
	 * @param style The font style (e.g. bold, italic).
	 * @return The font.
	 */
	
	private Font getFont(int size, int style) {
		
		FontFamily family = this.getFont().getFontFamily();
		return family.getFont(style, size);
		
	}

	public void fieldChanged(Field field, int context) {

		if (field == this.listen_live_button) {
			
			// Toggle the listen live state
			
			new Thread(new Runnable() {
				public void run() {
					toggleListenLive();
				}
			}).start();
			
		} else if (field == this.rate_why) {
			
			// Show the user why
			
			Dialog.alert(Strings.MESSAGE_RATE_WHY);
			
		} else if (field == this.rate_negative) {
			
			// Negative song rating
			
			new Thread(new Runnable() {
				public void run() {
					rateSong(false);
				}
			}).start();
			
		} else if (field == this.rate_positive) {
			
			// Positive song rating
			
			new Thread(new Runnable() {
				public void run() {
					rateSong(true);
				}
			}).start();
			
		} else if (field == this.call_studio_button) {
			
			// Call the studio
			
			this.callStudio();
			
		} else if (field == this.request_submit) {
			
			// Send an online request
			
			this.makeRequest();
			
		} else if (field == this.facebook_launch) {
			
			// Go to Facebook
			
			this.launchFacebook();
			
		}
		
	}
	
	/**
	 * Toggles if we're listening to the stream or not.
	 */
	
	private void toggleListenLive() {
		
		// Check the player state
		
		if (StreamingPlayer.get().getState() != StreamingPlayer.STATE_PLAYING) {
			
			// Check with the listener if we're not on wifi
			
			if (!this.wifilistener.isOnWifi()) {
				
				if (Dialog.ask(Dialog.D_YES_NO, Strings.MESSAGE_STREAM_CONFIRM) != Dialog.YES) {
					return;
				}
				
			}
			
			// Let the user know what we're doing
			
			synchronized(UiApplication.getEventLock()) {
				this.listen_live_button.setLabel(Strings.TEXT_CONNECTING);
			}
			
			// Let's start the player
			// The GUI will update in time based on what the player is currently doing
			
			StreamingPlayer.get().play();
			
		} else {
			
			// Stop the player
			
			StreamingPlayer.get().stop();
			
		}
		
	}
	
	/**
	 * Allows a listener to rate a song.
	 * @param response Whether their response was positive or negative.
	 */
	
	private void rateSong(boolean response) {
		
		// Check we have a song
		
		if (this.current_song == null) {
			this.safelyDisplayAlert(Strings.MESSAGE_RATE_NOSONG);
			return;
		}
		
		// Check we've not already rated it
		
		if (this.current_song.isRated()) {
			this.safelyDisplayAlert(Strings.MESSAGE_RATE_ALREADY);
			return;
		}
		
		// Generate the URL to supply the rating to
		
		Settings settings = Settings.get();
		if (settings == null) {
			this.safelyDisplayAlert(Strings.MESSAGE_RATE_ERROR);
			return;
		}
		
		String url = Formatter.formatMessage(settings.getSong_rating_url(), new String[] { Integer.toString(this.current_song.getId()), response ? Strings.TRUE : Strings.FALSE });
		if (HTTPUtil.httpGet(url)) {
			this.current_song.setRated(true);
			this.safelyDisplayAlert(Strings.MESSAGE_RATE_SUCCESS);
		} else {
			this.safelyDisplayAlert(Strings.MESSAGE_RATE_ERROR);
		}
		
	}
	
	/**
	 * Displays a message on the screen in a thread safe manor.
	 * @param message The message to display.
	 */
	
	private void safelyDisplayAlert(final String message) {
		
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(message);
			}
		});
		
	}
	
	/**
	 * Makes a phone call to the studio.
	 */
	
	private void callStudio() {
		
		// Try to retrieve the number
		
		Settings settings = Settings.get();
		if (settings == null) {
			Dialog.alert(Strings.MESSAGE_CALL_ERROR);
			return;
		}
		
		// Make the call
		
		try {
			Phone.initiateCall(Phone.getLineIds()[0], settings.getStudio_number());
		} catch (RadioException e) {
			Dialog.alert(Strings.MESSAGE_CALL_FAIL);
		}
		
	}
	
	/**
	 * Attempts to make a request.
	 * This is run async in a background thread.
	 */
	
	private void makeRequest() {
		
		// Read in the details from the listener
		
		String name = this.request_name.getText();
		String request = this.request_request.getText();
		String message = this.request_message.getText();
		
		// Check 'em
		
		if (name == null || name.length() == 0 || request == null || request.length() == 0) {
			this.safelyDisplayAlert(Strings.MESSAGE_REQUEST_MISSING);
			return;
		}
		
		// Check the URL
		
		Settings settings = Settings.get();
		if (settings == null) {
			this.safelyDisplayAlert(Strings.MESSAGE_REQUEST_ERROR);
			return;
		}
		
		// Let's make a request
		// Then shove it on down the line
		
		Hashtable post_variables = new Hashtable();
		post_variables.put("name", name);
		post_variables.put("request", request);
		post_variables.put("message", message);
		
		if (HTTPUtil.httpPost(settings.getRequest_url(), post_variables)) {
			
			// Clear the form and thank the listener
			// We do this in the UI thread
			
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					// Clear the form
					
					request_name.setText("");
					request_request.setText("");
					request_message.setText("");
					
					// Let the user know
					
					Dialog.alert(Strings.MESSAGE_REQUEST_SUCCESS);
					
				}
			});
			
		} else {
			this.safelyDisplayAlert(Strings.MESSAGE_REQUEST_ERROR);
		}
		
	}

	protected void onExposed() {
		
		super.onExposed();
		
		// Fire up the thread
		
		this.run_thread = true;
        this.updater_thread = new Thread(this);
        this.updater_thread.start(); 
		
	}

	protected void onObscured() {

		super.onObscured();
		
		// Stop the thread
		
		this.run_thread = false;
		this.updater_thread.interrupt();
		
	}
	
	/**
	 * Launches Facebook in a web broweser on the NHR page.
	 */
	
	private void launchFacebook() {

		// Switch!
		
		Ui.getUiEngine().pushScreen(new FacebookBrowser());
		
	}
	
}

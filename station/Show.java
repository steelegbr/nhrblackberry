package uk.co.dlineradio.nhr.blackberryapp.station;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.microedition.global.Formatter;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;
import uk.co.dlineradio.nhr.blackberryapp.util.HTTPUtil;
import uk.co.dlineradio.nhr.blackberryapp.util.TimeUtil;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.EventLogger;

/**
 * Represents a show that airs on NHR.
 * @author Marc Steele
 */

public class Show {
	
	private int id = 0;
	private String name = null;
	private Date start = null;
	private Date end = null;
	private String[] presenters = null;
	private String photo = null;
	private EncodedImage photo_data = null;
	private String description = null;
	private boolean downloading_data = false;
	
	/**
	 * Obtains the unique ID of the show.
	 * @return The unqiue ID of the show.
	 */
	
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the unique ID of the show.
	 * @param id The unique ID.
	 */
	
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Obtains the name of the show.
	 * @return The name of the show.
	 */
	
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the show.
	 * @param name The name of the show.
	 */
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Obtains the start time of the show.
	 * @return The start time.
	 */
	
	public Date getStart() {
		return start;
	}
	
	/**
	 * Sets the start time of the show.
	 * @param start The start time.
	 */
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	/**
	 * Obtains the end time of the show.
	 * @return The end time.
	 */
	
	public Date getEnd() {
		return end;
	}
	
	/**
	 * Sets the end time of the show.
	 * @param end The end time of the show.
	 */
	
	public void setEnd(Date end) {
		this.end = end;
	}
	
	/**
	 * Obtains the presenters in the show.
	 * @return The presenters in the show.
	 */
	
	public String[] getPresenters() {
		return presenters;
	}
	
	/**
	 * Sets the presenters in the show.
	 * @param presenters The presenters in the show.
	 */
	
	public void setPresenters(String[] presenters) {
		this.presenters = presenters;
	}
	
	/**
	 * Indicates if a show is live or not.
	 * @return TRUE if the show is live. Otherwise FALSE.
	 */
	
	public boolean isLive() {
		
		if (this.presenters == null) {
			return false;
		}
		
		return this.presenters.length > 0;
		
	}
	
	/**
	 * Obtains the path to the show photo.
	 * @return The path to the show photo.
	 */
	
	public String getPhoto() {
		return photo;
	}
	
	/**
	 * Sets the path to the show photo.
	 * @param photo The path to the show photo.
	 */

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	/**
	 * Retrieves the next few shows from the website.
	 * @return The next few shows (assuming the process was successful).
	 */
	
	public static Show[] getCurrentShows() {
	
		// Read in the URL from settings
		
		Settings settings = Settings.get();
		if (settings == null) {
			EventLogger.logEvent(Settings.getGUID(), "Failed to retrieve the next few shows as we got a null settings object.".getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Grab the JSON
		
		String raw_json = HTTPUtil.retrieveDataFromURL(settings.getOn_air_url());
		if (raw_json == null || raw_json.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to retieve the show data from {0}. We got back null data!", new String[] {settings.getOn_air_url()}).getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Convert to some shows
		
		Show[] shows = null;
		
		try {
			
			JSONArray json = new JSONArray(raw_json);
			int show_count = json.length();
			if (show_count > 0) {
				shows = new Show[show_count];
			}
			
			for (int i = 0; i < show_count; i++) {
				
				// Basic properties
				
				JSONObject current_obj = json.getJSONObject(i);
				Show current_show = new Show();
				current_show.setId(current_obj.getInt("id"));
				current_show.setName(current_obj.getString("name"));
				current_show.setStart(TimeUtil.dateFromJSONString(current_obj.getString("start")));
				current_show.setEnd(TimeUtil.dateFromJSONString(current_obj.getString("end")));
				current_show.setDescription(current_obj.getString("description"));
				current_show.setPhoto(settings.getPhoto_path_base().concat(current_obj.getString("photo_path")));
				
				// Presenters
				
				JSONArray presenter_array = current_obj.getJSONArray("presenters");
				int presenter_count = presenter_array.length();
				
				if (presenter_count > 0) {
					
					String[] presenters = new String[presenter_count];
					
					for (int j = 0; j < presenter_array.length(); j++) {
						presenters[j] = presenter_array.getString(j);
					}
					
					current_show.presenters = presenters;
					
				}
				
				shows[i] = current_show;
				
			}
			
		} catch (JSONException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into a JSON error trying to update the show data from {0}. Reason: {1}", new String[] { settings.getOn_air_url(), e.getMessage() }).getBytes(), EventLogger.ERROR);
			return null;
		} 
		
		return shows;
		
	}
	
	/**
	 * Generates a user friendly list of presenters.
	 * @return A user friendly list of presenters.
	 */
	
	public String getPresenterString() {
	
		// Sanity check
		
		if (this.presenters == null || this.presenters.length == 0) {
			return null;
		}
		
		// Build up the string
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
		
			if (this.presenters != null) {
				
				for (int i = 0; i < this.presenters.length; i++) {
			
					baos.write(this.presenters[i].getBytes());
			
					if (i < this.presenters.length - 2) {
				
						// Slap an 'and' on the end
				
						baos.write(", ".getBytes());
				
					} else if (i == this.presenters.length - 2) {
				
						// Stick a , on the end
				
						baos.write(" and ".getBytes());
				
					}
			
				}
			
			}
		
		} catch (IOException ex) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an IO issue trying to figure out who the presenters are. Reason: Reason: {0}", new String[] {ex.getMessage()}).getBytes(), EventLogger.ERROR);
			return null;
		}
		
		return new String(baos.toByteArray());
		
	}
	
	/**
	 * Get the actual show picture data. This will go through the process of downloading it
	 * if we don't already have it.
	 * @return The photo data.
	 */
	
	public EncodedImage getPictureData() {
		
		if (this.photo_data == null && !this.downloading_data) {
						
			// Retrive the photo async
			// We do this as the GUI thread will pick it up quickly enough
			
			new Thread(new Runnable() {

				public void run() {
					
					// Download the photo data
					
					downloading_data = true;
					
					byte[] data = HTTPUtil.retriveRawDataFromURL(getPhoto());
					if (data != null) {
						photo_data = EncodedImage.createEncodedImage(data, 0, data.length);						
					}
					
					downloading_data = false;
					
				}
				
			}).start();
			
			
		}
		
		return this.photo_data;
		
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

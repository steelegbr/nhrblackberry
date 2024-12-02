package uk.co.dlineradio.nhr.blackberryapp.gui;

import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.ui.container.MainScreen;

/**
 * Provides a simple Facebook broswer window.
 * @author Marc Steele
 */

public class FacebookBrowser extends MainScreen {

	public FacebookBrowser() {
		
		// Setup the UI
		
		BrowserFieldConfig browser_config = new BrowserFieldConfig();    
        browser_config.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
        BrowserField facebook_browser = new BrowserField(browser_config);
        this.add(facebook_browser);
        
        // Work out which url to use and load it
        
        Settings settings = Settings.get();
        
        if (settings == null) {
        	facebook_browser.requestContent(Strings.FALLBACK_FACEBOOK_URL);
        } else {
        	facebook_browser.requestContent(settings.getFacebook_url());
        }
		
	}

}

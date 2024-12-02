package uk.co.dlineradio.nhr.blackberryapp;

import uk.co.dlineradio.nhr.blackberryapp.gui.MainTabScreen;
import uk.co.dlineradio.nhr.blackberryapp.network.WifiNetworkHandler;
import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.UiApplication;

/**
 * This class extends the UiApplication class, providing a
 * graphical user interface.
 */
public class NHRadioApp extends UiApplication
{
    /**
     * Entry point for application
     * @param args Command line arguments (not used)
     */ 
    public static void main(String[] args)
    {
    	
    	// Logging
    	
    	EventLogger.register(Settings.getGUID(), Settings.getAppName(), EventLogger.VIEWER_STRING);
    	
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        NHRadioApp theApp = new NHRadioApp();       
        theApp.enterEventDispatcher();
        
    }
    

    /**
     * Creates a new NHRadioApp object
     */
    public NHRadioApp()
    {        
        // Push a screen onto the UI stack for rendering.
        this.pushScreen(new MainTabScreen());
    }    
}

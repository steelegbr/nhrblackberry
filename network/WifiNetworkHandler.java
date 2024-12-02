package uk.co.dlineradio.nhr.blackberryapp.network;

import uk.co.dlineradio.nhr.blackberryapp.audio.StreamingPlayer;
import uk.co.dlineradio.nhr.blackberryapp.gui.Strings;
import net.rim.device.api.system.WLANConnectionListener;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

/**
 * Handles Wifi status updates for the application.
 * @author Marc Steele
 */

public class WifiNetworkHandler implements WLANConnectionListener {
	
	/**
	 * Creates a new instance of the listener and attaches it.
	 */
	
	public WifiNetworkHandler() {
		WLANInfo.addListener(this);
	}
	
	/**
	 * Indicates if we are on wifi.
	 * @return TRUE if we are on WiFi. Otherwise FALSE.
	 */
	
	public boolean isOnWifi() {
		
		int network_status = WLANInfo.getWLANState();
		return (network_status & WLANInfo.WLAN_STATE_CONNECTED) != 0;
		
	}

	public void networkConnected() {}

	public void networkDisconnected(int reason) {
		
		// Drop the stream and alert the listener
		
		StreamingPlayer.get().stop();
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(Strings.MESSAGE_DROPPED_CONNECTION);
			}
		});
		
	}

}

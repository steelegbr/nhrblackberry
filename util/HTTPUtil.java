package uk.co.dlineradio.nhr.blackberryapp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.global.Formatter;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import uk.co.dlineradio.nhr.blackberryapp.settings.Settings;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.EventLogger;

/**
 * HTTP utility methods.
 * @author Marc Steele
 */

public class HTTPUtil {
	
	private static final String HTTP_SUFFIX = ";deviceside=true";
	private static final int BUFFER_SIZE = 1024;
	private static final int HTTP_SUCCESS = 200;
	
	/**
	 * Retrieves raw data from the web.
	 * @param url The URL of the raw data.
	 * @return The data if we were successful. Otherwise NULL.
	 */
	
	public static byte[] retriveRawDataFromURL(String url) {
		
		// Sanity check
		
		if (url == null || url.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to retrieve raw data from a NULL URL. That's just stupid.".getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Append the Blackberry stuff on the URL so we favor WiFi
		
		String request_url = url.concat(HTTP_SUFFIX);
		
		try {
			
			// Connect
			
			ConnectionFactory conn_fact = new ConnectionFactory();
			ConnectionDescriptor conn_desc = conn_fact.getConnection(url);
			if (conn_desc == null) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to get a connnection to {0} from the factory.", new String[] { url }).getBytes(), EventLogger.ERROR);
				return null;
			}
			
			HttpConnection conn = (HttpConnection) conn_desc.getConnection();
			InputStream response = conn.openInputStream();
			
			// Now pull all the bytes in
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			
			while ((length = response.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			
			// Cleanup
			
			response.close();
			conn.close();
			return baos.toByteArray();
			
		} catch (IOException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into a general IO error trying to download raw data from {0}. Reason {1}", new String[] { request_url, e.getMessage() }).getBytes(), EventLogger.ERROR);
			return null;
		}
		
	}
	
	/**
	 * Retrieves the string data from a specified URL. 
	 * @return The string data if the process was successul. Otherwise NULL.
	 */
	
	public static String retrieveDataFromURL(String url) {
		
		// Sanity check
		
		if (url == null || url.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to retrieve data from a null URL. Aborting.".getBytes(), EventLogger.ERROR);
			return null;
		}
		
		// Append the blackberry specific stuff on the back of the URL
		
		String data = null;
		String request_url = url.concat(HTTP_SUFFIX);
		
		try {
			
			// Connect
			
			ConnectionFactory conn_fact = new ConnectionFactory();
			ConnectionDescriptor conn_desc = conn_fact.getConnection(url);
			if (conn_desc == null) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to get a connnection to {0} from the factory.", new String[] { url }).getBytes(), EventLogger.ERROR);
				return null;
			}
			
			HttpConnection conn = (HttpConnection) conn_desc.getConnection();
			InputStream response = conn.openInputStream();
			
			// Read in the data
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			
			while ((length = response.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			
			// Cleanup
			
			response.close();
			conn.close();
			data = new String(baos.toByteArray());
			
		} catch (IOException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into a general IO error trying to download string data from {0}. Reason {1}", new String[] { request_url, e.getMessage() }).getBytes(), EventLogger.ERROR);
			return null;
		}
		
		return data;
		
	}
	
	/**
	 * Performs an HTTP get.
	 * @param url The URL to perform the GET on.
	 * @return TRUE if the process was successful. Otherwise FALSE.
	 */
	
	public static boolean httpGet(String url) {
		
		// Sanity check
		
		if (url == null || url.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to do an HTTP GET on a NULL URL. That's stupid so we won't bother.".getBytes(), EventLogger.ERROR);
			return false;
		}
		
		// Construct the full url
		
		String request_url = url.concat(HTTP_SUFFIX);
		
		// Perform the get
		
		try {
			
			ConnectionFactory conn_fact = new ConnectionFactory();
			ConnectionDescriptor conn_desc = conn_fact.getConnection(url);
			if (conn_desc == null) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to get a connnection to {0} from the factory.", new String[] { url }).getBytes(), EventLogger.ERROR);
				return false;
			}
			
			HttpConnection conn = (HttpConnection) conn_desc.getConnection();
			return conn.getResponseCode() == HTTP_SUCCESS;
			
		} catch (IOException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an IO issue trying to do an HTTP GET on {0}. Reason: {1}", new String[] { request_url, e.getMessage() }).getBytes(), EventLogger.ERROR);
			return false;
		}
		
	}
	
	/**
	 * Performs an HTTP post.
	 * @param url The URL to post to.
	 * @param params The parameters to transmit.
	 * @return TRUE if successful. Otherwise FALSE.
	 */
	
	public static boolean httpPost(String url, Hashtable params) {
		
		// Sanity check the url
		
		if (url == null || url.length() == 0) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to do an HTTP POST on a NULL URL. That's stupid so we won't bother.".getBytes(), EventLogger.ERROR);
			return false;
		}
		
		if (params == null) {
			EventLogger.logEvent(Settings.getGUID(), "Asked to do an HTTP POST with no parameters. That's stupid so we won't bother.".getBytes(), EventLogger.ERROR);
			return false;
		}
		
		// Construct the full url
		
		String request_url = url.concat(HTTP_SUFFIX);
		
		// Perform the POST
		
		try {
			
			// Connect
			
			ConnectionFactory conn_fact = new ConnectionFactory();
			ConnectionDescriptor conn_desc = conn_fact.getConnection(url);
			if (conn_desc == null) {
				EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Failed to get a connnection to {0} from the factory.", new String[] { url }).getBytes(), EventLogger.ERROR);
				return false;
			}
			
			HttpConnection conn = (HttpConnection) conn_desc.getConnection();
			conn.setRequestMethod(HttpConnection.POST);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			// Generate the form data
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Enumeration keys = params.keys();
			
			while (keys.hasMoreElements()) {
				
				String current_key = (String) keys.nextElement();
				String current_value = (String) params.get(current_key);
				
				baos.write(urlEncode(current_key).getBytes());
				baos.write("=".getBytes());
				baos.write(urlEncode(current_value).getBytes());
				baos.write("&".getBytes());
				
			}
			
			byte[] data = baos.toByteArray();
			
			// Perform the post
			
			conn.setRequestProperty("Content-length", Integer.toString(data.length));
			OutputStream out = conn.openOutputStream();
			out.write(data);
			out.flush();
			
			// Cleanup
			
			out.close();
			return conn.getResponseCode() == HTTP_SUCCESS;
			
		} catch (IOException e) {
			EventLogger.logEvent(Settings.getGUID(), Formatter.formatMessage("Ran into an IO issue trying to do an HTTP POST to {0}. Reason: {1}", new String[] { request_url, e.getMessage() }).getBytes(), EventLogger.ERROR);
			return false;
		}
		
		
	}
	
	/**
	 * Roll your own URL encoder. Taken from http://www.coderanch.com/t/229071/JME/Mobile/URL-Encode-ME
	 * @param sUrl The URL to encode.
	 * @return The encoded string.
	 */
	
	public static String urlEncode(String sUrl)   
	   {  
	        StringBuffer urlOK = new StringBuffer();  
	        for(int i=0; i<sUrl.length(); i++)   
	        {  
	            char ch=sUrl.charAt(i);  
	            switch(ch)  
	            {  
	                case '<': urlOK.append("%3C"); break;  
	                case '>': urlOK.append("%3E"); break;  
	                case '/': urlOK.append("%2F"); break;  
	                case ' ': urlOK.append("%20"); break;  
	                case ':': urlOK.append("%3A"); break;  
	                case '-': urlOK.append("%2D"); break;  
	                default: urlOK.append(ch); break;  
	            }   
	        }  
	        return urlOK.toString();  
	    }  

}

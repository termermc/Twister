package net.termer.twister;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.termer.twister.utils.Config;

/**
 * Utility class the read values from twister.ini
 * @author termer
 * @since 0.2
 */
public class Settings {
	// Default values for configurations
	private static HashMap<String,String> _DEFAULTS_ = new HashMap<String,String>();
	
	// Settings file
	private static File file = new File("twister.ini");
	
	/**
	 * (Re)loads the settings file
	 * @throws IOException if reading twister.ini fails
	 * @since 0.2
	 */
	public static void reload() throws IOException {
		_DEFAULTS_.put("ip", "127.0.0.1");
		_DEFAULTS_.put("port", "2003");
		_DEFAULTS_.put("keystore", "keystore.jks");
		_DEFAULTS_.put("keystore-password", "drowssap");
		_DEFAULTS_.put("logging", "true");
		_DEFAULTS_.put("static", "globalstatic/");
		_DEFAULTS_.put("default-domain", "localhost");
		_DEFAULTS_.put("caching", "true");
		_DEFAULTS_.put("caching-interval", "600");
		
		if(file.exists()) {
			Twister.settings = Config.parseConfig(file, ":", "#");
		}
	}
	
	/**
	 * Returns the value of the specified field in the settings, or the default value if the field is not present
	 * @param field the field requested
	 * @return the value of the field, or the default value if no value is present
	 * @since 0.2
	 */
	public static String get(String field) {
		String r = null;
		
		if(Twister.settings.containsKey(field)) {
			r = Twister.settings.get(field);
		} else if(_DEFAULTS_.containsKey(field)) {
			r = _DEFAULTS_.get(field);
		}
		
		return r;
	}
	
	/**
	 * Returns the default value for the specified field
	 * @param field the field
	 * @return the default value for the field
	 * @since 0.2
	 */
	public static String getDefault(String field) {
		String r = null;
		
		if(_DEFAULTS_.containsKey(field)) {
			r = _DEFAULTS_.get(field);
		}
		
		return r;
	}
}
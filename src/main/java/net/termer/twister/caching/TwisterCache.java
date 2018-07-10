package net.termer.twister.caching;

import java.util.HashMap;

/**
 * Class that stores caches files
 * @author termer
 * @since 0.2
 */
public class TwisterCache {
	/**
	 * Cached domain tops.
	 * Key: Domain
	 * Value: Cached top
	 * @since 0.2
	 */
	public static HashMap<String,String> _TOPS_ = new HashMap<String,String>();
	
	/**
	 * Cached domain bottoms.
	 * Key: Domain
	 * Value: Cached bottom
	 * @since 0.2
	 */
	public static HashMap<String,String> _BOTTOMS_ = new HashMap<String,String>();
	
	/**
	 * Cached 404 message.
	 * @since 0.2
	 */
	public static String _404_ = "";
}

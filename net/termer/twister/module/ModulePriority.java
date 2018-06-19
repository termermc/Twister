package net.termer.twister.module;

/**
 * Utility class for TwisterModule loading priorities
 * 
 * @author termer
 * @since 0.1
 */
public class ModulePriority {
	/**
	 * Low priority
	 * Gets loaded last and gets unloaded first
	 * @since 0.1
	 */
	public static int LOW = 0;
	/**
	 * Medium priority
	 * Gets loaded and unloaded in between HIGH and LOW priority modules
	 * @since 0.1
	 */
	public static int MEDIUM = 1;
	/**
	 * High priority
	 * Gets loaded first and get unloaded last
	 * @since 0.1
	 */
	public static int HIGH = 2;
}

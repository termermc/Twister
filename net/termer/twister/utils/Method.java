package net.termer.twister.utils;

/**
 * Convenience class for easy selection of METHOD by name
 * @author termer
 * @since 0.1
 */
public class Method {
	/**
	 * GET Method
	 * Used to handle GET requests, such as visiting a page
	 * @since 0.1
	 */
	public static int GET = 0;
	/**
	 * POST Method
	 * Used to handle POST requests, such as when someone submits a form
	 * @since 0.1
	 */
	public static int POST = 1;
	/**
	 * DELETE Method
	 * Used to handle DELETE requests
	 * @since 0.1
	 */
	public static int DELETE = 2;
	/**
	 * PUT Method
	 * Used to handle PUT requests
	 * @since 0.2
	 */
	public static int PUT = 3;
}
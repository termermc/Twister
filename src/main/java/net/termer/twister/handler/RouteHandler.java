package net.termer.twister.handler;

/**
 * The interface for all route handlers
 * @author termer
 * @since 0.3
 */
public interface RouteHandler {
	/**
	 * The method to return content for a route
	 * @param req the Request object for the request
	 * @param res the Response object for the request
	 * @return content for the response
	 * @since 0.3
	 */
	public String handle(String[] wildcards);
}

package net.termer.twister.handler;

/**
 * The interface for all route handlers
 * @author termer
 * @since 1.0
 */
public interface RouteHandler {
	/**
	 * The method to return content for a route
	 * @param wildcards the text filled in for the wildcards in the route this is handling
	 * @return content for the response
	 * @since 1.0
	 */
	public String handle(String[] wildcards);
}

package net.termer.twister.handler;

import spark.Request;
import spark.Response;

/**
 * The interface for all route handlers
 * @author termer
 * @since 1.0
 */
public interface RouteHandler {
	/**
	 * The method to return content for a route
	 * @param req the Request object for the request
	 * @param res the Response object for the request
	 * @param wildcards the text filled in for the wildcards in the route this is handling
	 * @return content for the response
	 * @since 1.0
	 */
	public String handle(Request req, Response res, String[] wildcards);
}

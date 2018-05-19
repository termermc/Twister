package net.termer.twister.handler;

/**
 * The interface for all request handlers
 * @author termer
 *
 */
public interface RequestHandler {
	/**
	 * The method to return content for a request
	 * @param req the Request object for the request
	 * @param res the Response object for the request
	 * @return content for the response
	 */
	public String handle(spark.Request req, spark.Response res);
}
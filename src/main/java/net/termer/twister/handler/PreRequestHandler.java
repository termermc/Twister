package net.termer.twister.handler;

/**
 * Interface to be implemented by pre-request handlers
 * @author termer
 * @since 1.0
 */
public interface PreRequestHandler {
	/**
	 * This method is called before each request and is given a PreRequestOptions object to manipulate the request
	 * @param options the options to manipulate the request
	 * @since 1.0
	 */
	public void handle(PreRequestOptions options);
}
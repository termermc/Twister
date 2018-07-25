package net.termer.twister.handler;

/**
 * Interface to be implemented by pre-request handlers
 * @author termer
 * @since 0.3
 */
public interface PreRequestHandler {
	/**
	 * This method is called before each request and is given a PreRequestOptions object to manipulate the request
	 * @param options the options to manipulate the request
	 * @since 0.3
	 */
	public void handle(PreRequestOptions options);
}
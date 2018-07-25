package net.termer.twister.handler;

import spark.Request;
import spark.Response;

/**
 * Utility class to handle PreRequestHandler options
 * @author termer
 * @since 0.3
 */
public class PreRequestOptions {
	private Request _REQUEST_ = null;
	private Response _RESPONSE_ = null;
	private String _DOMAIN_ = null;
	private boolean _CANCELLED_ = false;
	private String _CANCEL_TEXT_ = "";
	private String _PATH_ = null;
	
	/**
	 * Creates a new PreRequestOptions object for the specified request, response, and domain.
	 * For internal use.
	 * @param req the request object
	 * @param res the response object
	 * @param domain the domain
	 * @since 0.3
	 */
	public PreRequestOptions(Request req, Response res, String domain) {
		_REQUEST_ = req;
		_RESPONSE_ = res;
		_DOMAIN_ = domain;
		_PATH_ = req.pathInfo();
	}
	
	/**
	 * Returns the request object for this request
	 * @return the request object
	 * @since 0.3
	 */
	public Request getRequest() {
		return _REQUEST_;
	}
	
	/**
	 * Returns the response object for this request
	 * @return the response object
	 * @since 0.3
	 */
	public Response getResponse() {
		return _RESPONSE_;
	}
	
	/**
	 * Returns the domain this request originated from
	 * @return the domain this request originated from
	 * @since 0.3
	 */
	public String getDomain() {
		return _DOMAIN_;
	}
	
	/**
	 * Returns the path requested by the client
	 * @return the path requested by the client
	 * @since 0.3
	 */
	public String getPath() {
		return _PATH_;
	}
	
	/**
	 * Returns whether this request has been marked as cancelled
	 * @return whether this request has been marked as cancelled
	 * @since 0.3
	 */
	public boolean isCancelled() {
		return _CANCELLED_;
	}
	
	/**
	 * Returns the content to rendered instead of the requested content if the request is marked as cancelled
	 * @return the content to render if request is cancelled
	 * @since 0.3
	 */
	public String getCancelText() {
		return _CANCEL_TEXT_;
	}
	
	
	// Set methods
	
	/**
	 * Sets the domain this requested appears to have originated from
	 * @param domain the domain
	 * @since 0.3
	 */
	public void setDomain(String domain) {
		_DOMAIN_ = domain;
	}
	
	/**
	 * Sets the path this client appears to have requested
	 * @param path the path
	 * @since 0.3
	 */
	public void setPath(String path) {
		_PATH_ = path;
	}
	
	/**
	 * Sets whether this request is marked as cancelled
	 * @param cancelled whether it should be cancelled
	 * @since 0.3
	 */
	public void setCancelled(boolean cancelled) {
		_CANCELLED_ = cancelled;
	}
	
	/**
	 * Sets the text to be rendered instead of the requested content if the request is marked as cancelled
	 * @param text the text
	 * @since 0.3
	 */
	public void setCancelText(String text) {
		_CANCEL_TEXT_ = text;
	}
	
	
	// Action methods
	
	/**
	 * Marks this request as cancelled and sets the cancel text to the specified value
	 * @param text the cancel text
	 * @since 0.3
	 */
	public void cancel(String text) {
		setCancelled(true);
		setCancelText(text);
	}
	
	/**
	 * Marks the request cancelled and causes it to redirect to the specified URL
	 * @param url the URL to redirect to
	 * @since 0.3
	 */
	public void redirect(String url) {
		cancel("");
		_RESPONSE_.redirect(url);
	}
}
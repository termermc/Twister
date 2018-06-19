package net.termer.twister.document;

/**
 * The interface that is implemented by all document response objects passed to event handlers.
 * @author termer
 * @since 0.2
 */
public interface TwisterDocumentResponse {
	/**
	 * Returns the path of the document
	 * @return the path of the document
	 * @since 0.2
	 */
	public String getPath();
	/**
	 * Returns the domain of the document
	 * @return the domain of the document
	 * @since 0.2
	 */
	public String getDomain();
	/**
	 * Returns the actual filesystem path of the document
	 * @return the actual filesystem path of the document
	 * @since 0.2
	 */
	public String getFilePath();
}

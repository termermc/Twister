package net.termer.twister.document;

/**
 * The TwisterDocument implementation for HTML documents
 * @author termer
 * @since 0.2
 */
public class HTMLDocumentResponse implements TwisterDocumentResponse {
	private String _PATH_ = null;
	private String _DOMAIN_ = null;
	private String _FILE_PATH_ = null;
	private String _TEXT_ = null;
	
	/**
	 * Stores values for new HTMLDocumentResponse class
	 * @param path the path to document
	 * @param domain the domain the document is in
	 * @param filePath the filesystem path to the document file
	 * @param text the text contents of the document
	 * @since 0.2
	 */
	public HTMLDocumentResponse(String path, String domain, String filePath, String text) {
		_PATH_ = path;
		_DOMAIN_ = domain;
		_FILE_PATH_ = filePath;
		_TEXT_ = text;
	}
	
	/**
	 * Returns the path of the document
	 * @return the path of the document
	 * @since 0.2
	 */
	public String getPath() {
		return _PATH_;
	}
	
	/**
	 * Returns the domain the document is in
	 * @return the domain the document is in
	 * @since 0.2
	 */
	public String getDomain() {
		return _DOMAIN_;
	}
	
	/**
	 * Returns the filesystem path of the document file
	 * @return the filesystem path of the document file
	 * @since 0.2
	 */
	public String getFilePath() {
		return _FILE_PATH_;
	}
	
	/**
	 * Returns the text of the HTML document
	 * @return the text of the HTML document
	 * @since 0.2
	 */
	public String getText() {
		return _TEXT_;
	}
	
	/**
	 * Sets the text of the document
	 * @param text the text to assign to the document
	 * @since 0.2
	 */
	public void setText(String text) {
		_TEXT_ = text;
	}
	
	/**
	 * Replaces all instances of <i>str</i> in the document's text with <i>replacement</i>.
	 * @param str the String to replace
	 * @param replacement the String to replace the instances with
	 * @since 0.2
	 */
	public void replace(String str, String replacement) {
		setText(getText().replace(str, replacement));
	}
}
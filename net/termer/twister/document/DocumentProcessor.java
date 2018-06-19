package net.termer.twister.document;

import spark.Request;
import spark.Response;

/**
 * The interface for all document processors
 * @author termer
 * @since 0.2
 */
public interface DocumentProcessor {
	/**
	 * Method called to process a document
	 * @param doc the HTMLDocumentResponse to manipulate
	 * @param req the Request
	 * @param res the Response
	 * @since 0.2
	 */
	public void process(HTMLDocumentResponse doc, Request req, Response res);
}
package net.termer.twister.exception;

import net.lingala.zip4j.exception.ZipException;

/**
 * Exception class for JarLoader
 * @author termer
 * @since 0.1
 */
public class JarLoaderException extends ZipException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates the exception
	 * @param msg the exception's message
	 * @since 0.1
	 */
	public JarLoaderException(String msg) {
		super(msg);
	}
}

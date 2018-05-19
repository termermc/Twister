package net.termer.twister.exception;

import net.lingala.zip4j.exception.ZipException;
/**
 * Exception class for JarLoader
 * @author termer
 *
 */
public class JarLoaderException extends ZipException {
	private static final long serialVersionUID = 1L;

	public JarLoaderException(String msg) {
		super(msg);
	}
}

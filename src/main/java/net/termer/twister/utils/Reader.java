package net.termer.twister.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Utility class to read files and HTTP content
 * @author termer
 * @since 0.3
 */
public class Reader {
	/**
	 * Returns the contents of the specified file path
	 * @param path the path to the file
	 * @return the contents of the file as a String
	 * @throws IOException if the file does not exist, or reading it fails
	 * @since 0.3
	 */
	public static String readFile(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		FileInputStream fin = new FileInputStream(path);
		while(fin.available()>0) {
			sb.append((char)fin.read());
		}
		fin.close();
		return sb.toString();
	}
	
	/**
	 * Opens a new URL connection and returns the value as a String
	 * @param url the URL
	 * @return the URL response as a String
	 * @throws IOException if reading the stream fails
	 * @since 0.3
	 */
	public static String readHTTP(String url) throws IOException {
		StringBuilder sb = new StringBuilder();
		URL loc = new URL(url);
		InputStream is = loc.openStream();
		while(is.available()>0) {
			sb.append((char)is.read());
		}
		is.close();
		return sb.toString();
	}
}

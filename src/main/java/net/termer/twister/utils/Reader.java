package net.termer.twister.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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
		StringBuilder fileData = new StringBuilder(1000);//Constructs a string buffer with no characters in it and the specified initial capacity
		BufferedReader reader = new BufferedReader(new FileReader(path));
		
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		
		reader.close();
 
		return fileData.toString();
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

package net.termer.twister.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File writing utility method
 * @author termer
 * @since 0.1
 */
public class Writer {
	/**
	 * Writes a String to a File
	 * NOTE: Does not append to the file
	 * @param str - the String to write to the File
	 * @param file - the File to write
	 * @throws IOException if writing to the file fails
	 * @since 0.1
	 */
	public static void print(String str, File file) throws IOException {
		FileOutputStream fout = new FileOutputStream(file);
		for(int i = 0; i < str.length(); i++) {
			fout.write((int)str.charAt(i));
		}
		fout.close();
	}
	
	/**
	 * Writes a String to a File with a newline on the end
	 * NOTE: Does not append to the file
	 * @param str - the String to write to the File
	 * @param file - the File to write
	 * @throws IOException if writing to the file fails
	 * @since 0.1
	 */
	public static void println(String str, File file) throws IOException {
		print(str+'\n',file);
	}
}

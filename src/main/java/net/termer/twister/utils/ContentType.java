package net.termer.twister.utils;

import java.util.HashMap;

/**
 * Utility class to get and set content types for various file extensions
 * @author termer
 * @since 1.1
 */
public class ContentType {
	private static HashMap<String,String> TYPES = new HashMap<String,String>();
	
	/**
	 * Gets the content type for the specified file extension, application/octet-stream
	 * if the file extension is not present in the map.
	 * @param extension the file extension
	 * @return the correct content type for the file extension
	 * @since 1.1
	 */
	public static String getForExtension(String extension) {
		String type = TYPES.get(extension);
		if(type==null) {
			type = "application/octet-stream";
		}
		return type;
	}
	
	/**
	 * Sets the content type associated with the specified file extension
	 * @param extension the file extension
	 * @param type the content type
	 * @since 1.1
	 */
	public static void setForExtension(String extension, String type) {
		TYPES.put(extension, type);
	}
	
	/**
	 * Clears the content type entry for the specified file extension
	 * @param extension the file extension
	 * @since 1.1
	 */
	public static void clearForExtension(String extension) {
		TYPES.put(extension, null);
	}
	
	/**
	 * Sets the default file extension/content type values
	 * @since 1.1
	 */
	public static void applyBasicTypes() {
		TYPES.put("html", "text/html");
		TYPES.put("php", "text/html");
		TYPES.put("txt", "text/plain");
		TYPES.put("js", "application/javascript");
		TYPES.put("aac", "audio/aac");
		TYPES.put("avi", "video/x-msvideo");
		TYPES.put("bmp", "image/bmp");
		TYPES.put("bz", "application/x-bzip");
		TYPES.put("bz2", "application/x-bzip2");
		TYPES.put("css", "text/css");
		TYPES.put("doc", "application/msword");
		TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		TYPES.put("eot", "application/vnd.ms-fontobject");
		TYPES.put("epub", "application/epub+zip");
		TYPES.put("htm", "text/html");
		TYPES.put("gif", "image/gif");
		TYPES.put("ico", "image/x-icon");
		TYPES.put("jar", "application/java-archive");
		TYPES.put("jpeg", "image/jpeg");
		TYPES.put("jpg", "image/jpeg");
		TYPES.put("json", "application/json");
		TYPES.put("mid", "audio/midi");
		TYPES.put("midi", "audio/midi");
		TYPES.put("mpeg", "audio/mpeg");
		TYPES.put("odp", "application/vnd.oasis.opendocument.presentation");
		TYPES.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		TYPES.put("odt", "application/vnd.oasis.opendocument.text");
		TYPES.put("oga", "audio/ogg");
		TYPES.put("ogv", "video/ogg");
		TYPES.put("ogx", "application/ogg");
		TYPES.put("ogg", "audio/ogg");
		TYPES.put("png", "image/png");
		TYPES.put("pdf", "application/pdf");
		TYPES.put("application/vnd.ms-powerpoint", "ppt");
		TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		TYPES.put("rar", "application/x-rar-compressed");
		TYPES.put("rtf", "application/rtf");
		TYPES.put("sh", "application/x-sh");
		TYPES.put("svg", "image/svg+xml");
		TYPES.put("swf", "application/x-shockwave-flash");
		TYPES.put("tar", "application/x-tar");
		TYPES.put("wav", "audio/wave");
		TYPES.put("wave", "audio/wave");
		TYPES.put("weba", "audio/webm");
		TYPES.put("webm", "video/webm");
		TYPES.put("webp", "image/webm");
		TYPES.put("woff2", "font/woff2");
		TYPES.put("woff", "font/woff");
		TYPES.put("xhtml", "application/xhtml+xml");
		TYPES.put("xls", "application/vnd.ms-excel");
		TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		TYPES.put("xml", "application/xml");
		TYPES.put("zip", "application/zip");
		TYPES.put("7z", "application/x-7z-compressed");
	}
}
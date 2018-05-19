package net.termer.twister.utils;

import java.io.File;

/**
 * Utility class for getting data about domains
 * @author termer
 *
 */
public class Domain {
	// Values
	private String _NAME_ = null;
	private boolean hasTop = false;
	private boolean hasBottom = false;
	private String topPath = null;
	private String bottomPath = null;
	
	/**
	 * Define values for quick withdrawal
	 * @param name the name of the domain
	 */
	// Define values for quick withdrawal
	public Domain(String name) {
		_NAME_ = name;
		File topFile = new File("domains/"+name+"/top.html");
		File bottomFile = new File("domains/"+name+"/bottom.html");
		hasTop = topFile.exists();
		hasBottom = bottomFile.exists();
		if(hasTop) {
			topPath = topFile.getPath();
		}
		if(hasBottom) {
			bottomPath = bottomFile.getPath();
		}
	}
	
	/**
	 * Returns the domain name (or address if IP address)
	 * @return the domain name (or address if IP address)
	 */
	// Return the values
	public String getName() {
		return _NAME_;
	}
	
	/**
	 * Returns whether the domain has a "top.html" file
	 * @return whether the domain has a "top.html" file
	 */
	// Returns whether the domain has a "top.html" file
	public boolean hasTop() {
		return hasTop;
	}
	
	/**
	 * Returns whether the domain has a "bottom.html" file
	 * @return whether the domain has a "bottom.html" file
	 */
	// Returns whether the domain has a "bottom.html" file
	public boolean hasBottom() {
		return hasBottom;
	}
	
	/**
	 * Returns the file system path to this domain's "top.html" file, if any
	 * @return the file system path to this domain's "top.html" file, if any
	 */
	// Returns the file system path to this domain's "top.html" file, if any
	public String getTopPath() {
		return topPath;
	}
	
	/**
	 * Returns the file system path to this domain's "bottom.html" file, if any
	 * @return the file system path to this domain's "bottom.html" file, if any
	 */
	// Returns the file system path to this domain's "bottom.html" file, if any
	public String getBottomPath() {
		return bottomPath;
	}
}

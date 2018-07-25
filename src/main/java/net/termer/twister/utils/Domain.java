package net.termer.twister.utils;

import java.io.File;
import java.io.IOException;

import net.termer.twister.Settings;
import net.termer.twister.caching.TwisterCache;
import net.termer.twister.document.DocumentBuilder;
import spark.Request;
import spark.Response;

/**
 * Utility class for getting data about domains
 * @author termer
 * @since 0.1
 */
public class Domain {
	// Values
	private String _NAME_ = null;
	
	/**
	 * Instantiates the new class and stores the domain name
	 * @param name the name of the domain
	 * @since 0.1
	 */
	public Domain(String name) {
		_NAME_ = name;
	}
	
	/**
	 * Returns the domain name (or address if IP address)
	 * @return the domain name (or address if IP address)
	 * @since 0.1
	 */
	public String getName() {
		return _NAME_;
	}
	
	/**
	 * Returns whether the domain has a "top.html" file, or a cached version
	 * @return whether the domain has a "top.html" file, or a cached version
	 * @since 0.1
	 */
	public boolean hasTop() {
		boolean has = false;
		
		if(Boolean.parseBoolean(Settings.get("caching"))) {
			has = TwisterCache._TOPS_.containsKey(_NAME_);
		} else {
			has = new File("domains/"+_NAME_+"/top.html").exists();
		}
		
		return has;
	}
	
	/**
	 * Returns whether the domain has a "bottom.html" file, or a cached version
	 * @return whether the domain has a "bottom.html" file, or a cached version
	 * @since 0.1
	 */
	public boolean hasBottom() {
		boolean has = false;
		
		if(Boolean.parseBoolean(Settings.get("caching"))) {
			has = TwisterCache._BOTTOMS_.containsKey(_NAME_);
		} else {
			has = new File("domains/"+_NAME_+"/bottom.html").exists();
		}
		
		return has;
	}
	
	/**
	 * Returns whether the domain has a "404.html" file, or a cached version
	 * @return whether the domain has a "404.html" file, or a cached version
	 * @since 0.3
	 */
	public boolean has404() {
		boolean has = false;
		
		if(Boolean.parseBoolean(Settings.get("caching"))) {
			has = TwisterCache._404S_.containsKey(_NAME_);
		} else {
			has = new File("domains/"+_NAME_+"/404.html").exists();
		}
		
		return has;
	}
	
	/**
	 * Returns the file system path to this domain's "top.html" file, if any
	 * @return the file system path to this domain's "top.html" file, if any
	 * @since 0.1
	 */
	public String getTopPath() {
		String path = null;
		
		if(hasTop()) {
			path = "domains/"+_NAME_+"/top.html";
		}
		
		return path;
	}
	
	/**
	 * Returns the file system path to this domain's "bottom.html" file, if any
	 * @return the file system path to this domain's "bottom.html" file, if any
	 * @since 0.1
	 */
	public String getBottomPath() {
		String path = null;
		
		if(hasBottom()) {
			path = "domains/"+_NAME_+"/bottom.html";
		}
		
		return path;
	}
	
	/**
	 * Returns the top for domain, if any
	 * @return the top for domain
	 * @throws IOException if reading the domain's top fails
	 * @since 0.2
	 */
	public String getTop() throws IOException {
		String r = null;
		
		if(hasTop()) {
			if(Boolean.parseBoolean(Settings.get("caching"))) {
				r = TwisterCache._TOPS_.get(_NAME_);
			} else {
				r = DocumentBuilder.readFile("domains/"+_NAME_+"/top.html");
			}
		}
		
		return r;
	}
	
	/**
	 * Returns the bottom for domain, if any
	 * @return the bottom for domain
	 * @throws IOException if reading the domain's bottom fails
	 * @since 0.2
	 */
	public String getBottom() throws IOException {
		String r = null;
		
		if(hasTop()) {
			if(Boolean.parseBoolean(Settings.get("caching"))) {
				r = TwisterCache._BOTTOMS_.get(_NAME_);
			} else {
				r = DocumentBuilder.readFile("domains/"+_NAME_+"/bottom.html");
			}
		}
		
		return r;
	}
	
	/**
	 * Returns the 404 page for domain, if any
	 * @return the 404 page for domain
	 * @throws IOException if reading the domain's 404 page fails
	 * @since 0.3
	 */
	public String get404() throws IOException {
		String r= null;
		
		if(has404()) {
			if(Boolean.parseBoolean(Settings.get("caching"))) {
				r = TwisterCache._404S_.get(_NAME_);
			} else {
				r = DocumentBuilder.readFile("domains/"+_NAME_+"/404.html");
			}
		}
		
		return r;
	}
	
	/**
	 * Returns the processed version of the domain's top, if any
	 * @param req the request
	 * @param res the response
	 * @return the processed top
	 * @throws IOException if reading the top document fails
	 * @since 0.2
	 */
	public String getProcessedTop(Request req, Response res) throws IOException {
		String r = getTop();
		
		if(r!=null) {
			r = DocumentBuilder.processTopDocument("top.html", r, _NAME_, req, res);
		}
		
		return r;
	}
	
	/**
	 * Returns the processed version of the domain's bottom, if any
	 * @param req the request
	 * @param res the response
	 * @return the processed top
	 * @throws IOException if reading the bottom document fails
	 * @since 0.2
	 */
	public String getProcessedBottom(Request req, Response res) throws IOException {
		String r = getBottom();
		
		if(r!=null) {
			r = DocumentBuilder.processBottomDocument("bottom.html", r, _NAME_, req, res);
		}
		
		return r;
	}
	
	/**
	 * Returns the processed version of the domain's 404 page, if any
	 * @param req the request
	 * @param res the response
	 * @return the processed 404 page
	 * @throws IOException if reading the 404 document fails
	 * @since 0.3
	 */
	public String getProcessed404(Request req, Response res) throws IOException {
		String r = get404();
		
		if(r!=null) {
			r = DocumentBuilder.processDocument("404.html", r, _NAME_, req, res);
		}
		
		return r;
	}
	
	/**
	 * Returns whether the requested domain exists
	 * @return whether the requested domain exists
	 * @since 0.2
	 */
	public boolean exists() {
		return new File("domains/"+_NAME_+"/").exists();
	}
}

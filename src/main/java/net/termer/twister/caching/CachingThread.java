package net.termer.twister.caching;

import java.io.File;
import java.io.IOException;

import net.termer.twister.Settings;
import net.termer.twister.Twister;
import net.termer.twister.document.DocumentBuilder;
import net.termer.twister.utils.StringFilter;

/**
 * Thread used by Twister that caches various files
 * @author termer
 * @since 0.2
 */
public class CachingThread extends Thread {
	public void run() {
		setName("Caching Thread");
		
		// Initially cache tops and bottoms
		if(Boolean.parseBoolean(Settings.get("caching"))) {
			cacheTopsAndBottoms();
			cache404();
		}
		
		// Run forever
		while(true) {
			
			// Check if top and bottom caching is enabled
			if(Boolean.parseBoolean(Settings.get("caching"))) {
				try {
					// Sleep the thread for the configured amount of seconds
					sleep(Math.round(Double.parseDouble(Settings.get("caching-interval"))*1000));
					
					// Cache domain tops and bottoms
					cacheTopsAndBottoms();
					
					// Cache 404 message
					cache404();
					
					// Cycle complete
					
				} catch (NumberFormatException | InterruptedException e) {
					Twister.current().logError("Caching thread failed to sleep for the");
					Twister.current().logError("cache-topbottom-interval time, not caching this cycle");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Caches all 404 page
	 * @since 0.2
	 */
	public static void cache404() {
		try {
			TwisterCache._404_ = DocumentBuilder.readFile("404.html");
		} catch (IOException e) {
			Twister.current().logError("Failed to cache 404.html");
			e.printStackTrace();
		}
		
		// Clear cached 404 pages
		TwisterCache._404S_.clear();
		
		File domainsDir = new File("domains/");
		for(File dir : domainsDir.listFiles()) {
			if(dir.isDirectory()) {
				String domain = dir.getName();
				
				// Cycle through domain's files
				for(File domainFile : dir.listFiles()) {
					if(domainFile.isFile()) {
						if(StringFilter.same(domainFile.getName(),"404.html")) {
							try {
								TwisterCache._404S_.put(domain, DocumentBuilder.readFile(domainFile.getAbsolutePath()));
							} catch (IOException e) {
								Twister.current().logError("Failed to cache 404.html for domain "+domain);
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Caches all domain tops and bottoms
	 * @since 0.2
	 */
	public static void cacheTopsAndBottoms() {
		// Clear cache
		TwisterCache._TOPS_.clear();
		TwisterCache._BOTTOMS_.clear();
		
		File domainsDir = new File("domains/");
		for(File dir : domainsDir.listFiles()) {
			if(dir.isDirectory()) {
				String domain = dir.getName();
				
				// Cycle through domain's files
				for(File domainFile : dir.listFiles()) {
					if(domainFile.isFile()) {
						if(StringFilter.same(domainFile.getName(),"top.html")) {
							try {
								TwisterCache._TOPS_.put(domain, DocumentBuilder.readFile(domainFile.getAbsolutePath()));
							} catch (IOException e) {
								Twister.current().logError("Failed to cache top.html for domain "+domain);
								e.printStackTrace();
							}
						} else if(StringFilter.same(domainFile.getName(),"bottom.html")) {
							try {
								TwisterCache._BOTTOMS_.put(domain, DocumentBuilder.readFile(domainFile.getAbsolutePath()));
							} catch (IOException e) {
								Twister.current().logError("Failed to cache bottom.html for domain "+domain);
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
}

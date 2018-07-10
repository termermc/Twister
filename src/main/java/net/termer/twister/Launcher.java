package net.termer.twister;

import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;
import net.termer.twister.module.ModuleManager;

/**
 * Launcher class for Twister
 * @author termer
 * @since 0.1
 */
public class Launcher {
	
	/**
	 * Launch Twiser
	 * @param args - the launch arguments
	 * @since 0.1
	 */
	// Launch Twiser
	public static void main(String[] args) {
		
		// Check if there is already an instance of Twister running
		if(Twister.current() == null) {
			System.out.println("[Launcher] Starting Twister...");
			Twister.twister = new Twister();
			// Load all modules
			try {
				ModuleManager.loadModules();
			} catch (ZipException | IOException e) {
				System.err.println("[Launcher] Failed to load modules:");
				e.printStackTrace();
			}
			System.out.println("[Launcher] Twister started.");
		} else {
			System.out.println("[Launcher] There is already an instance of Twiser runnning!");
		}
	}
}

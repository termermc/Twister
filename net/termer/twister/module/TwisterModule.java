package net.termer.twister.module;

import net.termer.twister.Twister;

/**
 * Twister Module Interface
 * This is the interface that modules implement to be handled
 * @author termer
 *
 */
public interface TwisterModule {
	/**
	 * Used by Modules to return their name
	 * @return the module's name
	 */
	public String moduleName();
	/**
	 * Used by Modules to return the version of Twister
	 * they were designed to work with
	 * @return the version of Twister the module was made for
	 */
	public double twiserVersion();
	
	/**
	 * Used by Modules to return their loading priority
	 * To get values, it is best to refer to the ModulePriority class
	 * @return the module's loading priority
	 */
	public int modulePriority();
	
	/**
	 * The method that is called when the Module is loaded
	 */
	public void initializeModule(Twister instance);
	/**
	 * The method that is called when the Module is unloaded
	 */
	public void shutdownModule();
}

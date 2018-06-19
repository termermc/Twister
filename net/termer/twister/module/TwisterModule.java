package net.termer.twister.module;

import net.termer.twister.Twister;

/**
 * Twister Module Interface
 * This is the interface that modules implement to be handled
 * @author termer
 * @since 0.1
 */
public interface TwisterModule {
	/**
	 * Used by Modules to return their name
	 * @return the module's name
	 * @since 0.1
	 */
	public String moduleName();
	/**
	 * Used by Modules to return the version of Twister
	 * they were designed to work with
	 * @return the version of Twister the module was made for
	 * @since 0.1
	 */
	public double twiserVersion();
	
	/**
	 * Used by Modules to return their loading priority
	 * To get values, it is best to refer to the ModulePriority class
	 * @return the module's loading priority
	 * @since 0.1
	 */
	public int modulePriority();
	
	/**
	 * The method that is called when the Module is loaded
	 * @since 0.1
	 */
	public void initializeModule(Twister instance);
	/**
	 * The method that is called when the Module is shutdown or unloaded
	 * @since 0.1
	 */
	public void shutdownModule();
}

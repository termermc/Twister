package net.termer.twister.module;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.termer.twister.Twister;
import net.termer.twister.exception.JarLoaderException;
import net.termer.twister.utils.StringFilter;

/**
 * Module Manager class
 * Handles the loading, reloading, and shutting down of modules.
 * @author termer
 *
 */
public class ModuleManager {
	// Current Twister modules
	public static Collection<TwisterModule> _MODULES_ = new ArrayList<TwisterModule>();
	
	// LOW priority TwisterModules
	public static ArrayList<TwisterModule> _LOW_ = new ArrayList<TwisterModule>();
	
	// MEDIUM priority TwisterModules
	public static ArrayList<TwisterModule> _MEDIUM_ = new ArrayList<TwisterModule>();
	
	// HIGH priority TwisterModules
	public static ArrayList<TwisterModule> _HIGH_ = new ArrayList<TwisterModule>();
	
	/**
	 * Loads all dependencies, then
	 * loads and starts all TwisterModules
	 * @throws ZipException if reading a module fails
	 * @throws IOException if closing the ClassLoader fails
	 */
	// Load and start all TwisterModules
	@SuppressWarnings("rawtypes")
	public static void loadModules() throws ZipException, IOException {
		Twister.current().logInfo("Loading modules...");
		
		ArrayList<URL> urls = new ArrayList<URL>();
		ArrayList<String> classes = new ArrayList<String>();
		
		for(File jar : new File("dependencies/").listFiles()) {
			if(jar.getName().toLowerCase().endsWith(".jar")) {
				ZipFile zf = new ZipFile(jar.getAbsolutePath());
				if(zf.isValidZipFile()) {
					urls.add(new URL("file:"+jar.getAbsolutePath()));
					JarFile jf = new JarFile(jar.getAbsolutePath());
					Enumeration<JarEntry> ent = jf.entries();
					while(ent.hasMoreElements()) {
						String name = ent.nextElement().getName();
						if(name.toLowerCase().endsWith(".class")) {
							classes.add(name.replaceAll("/", ".").replaceAll(".class", ""));
						}
					}
					jf.close();
				} else {
					throw new JarLoaderException("File is not a valid jarfile");
				}
			}
		}
		
		ArrayList<String> launchClasses = new ArrayList<String>();
		
		for(File jar : new File("modules/").listFiles()) {
			if(jar.getName().toLowerCase().endsWith(".jar")) {
				ZipFile zf = new ZipFile(jar.getAbsolutePath());
				if(zf.isValidZipFile()) {
					urls.add(new URL("file:"+jar.getAbsolutePath()));
					JarFile jf = new JarFile(jar.getAbsolutePath());
					Enumeration<JarEntry> ent = jf.entries();
					while(ent.hasMoreElements()) {
						String name = ent.nextElement().getName();
						if(name.toLowerCase().endsWith(".class")) {
							String clazz = name.replaceAll("/", ".").replaceAll(".class", "");
							if(clazz.endsWith("Module")) {
								launchClasses.add(clazz);
							}
							classes.add(clazz);
						}
					}
					jf.close();
				} else {
					throw new JarLoaderException("File is not a valid jarfile");
				}
			}
		}
		
		URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[0]));
		for(String clazz : classes) {
			try {
				Class cls = ucl.loadClass(clazz);
				
				for(String launchClass : launchClasses) {
					if(StringFilter.same(launchClass, clazz)) {
						for(Class inter : cls.getInterfaces()) {
							if(StringFilter.same(inter.getTypeName(),"net.termer.twister.module.TwisterModule")) {
								_MODULES_.add((TwisterModule) cls.newInstance());
								break;
							}
						}
					}
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		ucl.close();
		
		for(TwisterModule module : _MODULES_) {
			if(module.modulePriority()==0) {
				_LOW_.add(module);
			} else if(module.modulePriority()==2) {
				_HIGH_.add(module);
			} else {
				_MEDIUM_.add(module);
			}
		}
		for(TwisterModule module : _HIGH_) {
			Twister.current().logInfo("Loading module \""+module.moduleName()+"\"...");
			if(module.twiserVersion() > Twister._VERSION_) {
				Twister.current().logError("Module \""+module.moduleName()+"\" was written for a newer version of Twiser.");
				Twister.current().logError("The module will not be loaded.");
			} else {
				try {
					module.initializeModule(Twister.current());
				} catch(AbstractMethodError e) {
					Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a initilization method.");
					Twister.current().logError("The module will not be loaded.");
				}
			}
		}
		for(TwisterModule module : _MEDIUM_) {
			Twister.current().logInfo("Loading module \""+module.moduleName()+"\"...");
			if(module.twiserVersion() > Twister._VERSION_) {
				Twister.current().logError("Module \""+module.moduleName()+"\" was written for a newer version of Twiser.");
				Twister.current().logError("The module will not be loaded.");
			} else {
				try {
					module.initializeModule(Twister.current());
				} catch(AbstractMethodError e) {
					Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a initilization method.");
					Twister.current().logError("The module will not be loaded.");
				}
			}
		}
		for(TwisterModule module : _LOW_) {
			Twister.current().logInfo("Loading module \""+module.moduleName()+"\"...");
			if(module.twiserVersion() > Twister._VERSION_) {
				Twister.current().logError("Module \""+module.moduleName()+"\" was written for a newer version of Twiser.");
				Twister.current().logError("The module will not be loaded.");
			} else {
				try {
					module.initializeModule(Twister.current());
				} catch(AbstractMethodError e) {
					Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a initilization method.");
					Twister.current().logError("The module will not be loaded.");
				}
			}
		}
		Twister.current().logInfo("Modules loaded.");
	}
	
	/**
	 * Unloads and shuts down all TwisterModules
	 */
	// Unload and shutdown all TwisterModules
	public static void unloadModules() {
		Twister.current().logInfo("Unloading modules...");
		
		for(TwisterModule module : _LOW_) {
			Twister.current().logInfo("Unloading module \""+module.moduleName()+"\"...");
			try {
				module.shutdownModule();
			} catch(AbstractMethodError e) {
				Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a shutdown method.");
			}
		}
		for(TwisterModule module : _MEDIUM_) {
			Twister.current().logInfo("Unloading module \""+module.moduleName()+"\"...");
			try {
				module.shutdownModule();
			} catch(AbstractMethodError e) {
				Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a shutdown method.");
			}
		}
		for(TwisterModule module : _HIGH_) {
			Twister.current().logInfo("Unloading module \""+module.moduleName()+"\"...");
			try {
				module.shutdownModule();
			} catch(AbstractMethodError e) {
				Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a shutdown method.");
			}
		}
		_LOW_.clear();
		_MEDIUM_.clear();
		_HIGH_.clear();
		_MODULES_.clear();
		
		Twister.current().logInfo("Modules unloaded.");
	}
	
	/**
	 * Unloads and loads all modules
	 * Does not actually unload the module classes,
	 * should not be used to load in new modules
	 * or load new versions of existing ones.
	 * @throws IOException if loading modules fails
	 * @throws ZipException if reading the modules fails
	 */
	// Unloads and loads all modules
	public static void reloadModules() throws ZipException, IOException {
		unloadModules();
		loadModules();
	}
	
	/**
	 * Returns all currently loaded HIGH priority TwisterModules
	 * @return all currently loaded HIGH priority TwisterModules
	 */
	// Returns all currently loaded HIGH priority TwisterModules
	public static TwisterModule[] getHighPriorityModules() {
		return _HIGH_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Returns all currently loaded MEDIUM priority TwisterModules
	 * @return all currently loaded MEDIUM priority TwisterModules
	 */
	// Returns all currently loaded MEDIUM priority TwisterModules
	public static TwisterModule[] getMediumPriorityModules() {
		return _MEDIUM_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Returns all currently loaded LOW priority TwisterModules
	 * @return all currently loaded LOW priority TwisterModules
	 */
	// Returns all currently loaded LOW priority TwisterModules
	public static TwisterModule[] getLowPriorityModules() {
		return _LOW_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Returns all currently loaded TwisterModules
	 * @return all currently loaded TwisterModules
	 */
	// Returns all currently loaded TwisterModules
	public static TwisterModule[] getModules() {
		return _MODULES_.toArray(new TwisterModule[0]);
	}
}

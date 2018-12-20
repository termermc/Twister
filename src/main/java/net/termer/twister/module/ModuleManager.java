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
 * @since 0.1
 */
public class ModuleManager {
	
	/**
	 * Twister versions that are not compatible with this version
	 * @since 0.2
	 */
	public static double[] _INCOMPATIBLE_VERSIONS_ = {};
	
	/**
	 * Current Twister modules
	 * @since 0.1
	 */
	public static Collection<TwisterModule> _MODULES_ = new ArrayList<TwisterModule>();
	
	/**
	 * LOW priority TwisterModule
	 * @since 0.1
	 */
	public static ArrayList<TwisterModule> _LOW_ = new ArrayList<TwisterModule>();
	
	/**
	 * MEDIUM priority TwisterModules
	 * @since 0.1
	 */
	public static ArrayList<TwisterModule> _MEDIUM_ = new ArrayList<TwisterModule>();
	
	/**
	 * HIGH priority TwisterModules
	 * @since 0.1
	 */
	public static ArrayList<TwisterModule> _HIGH_ = new ArrayList<TwisterModule>();
	
	private static ArrayList<URLClassLoader> _DependencyClasses = new ArrayList<URLClassLoader>();
	
	/**
	 * Loads all dependencies, then
	 * loads and starts all TwisterModules
	 * @throws ZipException if reading a module fails
	 * @throws IOException if closing the ClassLoader fails
	 * @since 0.1
	 */
	@SuppressWarnings("deprecation")
	public static void loadModules() throws ZipException, IOException {
		Twister.current().logInfo("Loading modules...");
		
		ArrayList<URL> urls = new ArrayList<URL>();
		ArrayList<String> classes = new ArrayList<String>();
		
		for(File jar : new File("dependencies/").listFiles()) {
			_DependencyClasses.add(new URLClassLoader(new URL[] {jar.toURL()}));
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
							String clazz = name.replace("/", ".").replace(".class", "");
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
				Class<?> cls = ucl.loadClass(clazz);
				
				for(String launchClass : launchClasses) {
					if(StringFilter.same(launchClass, clazz)) {
						for(Class<?> inter : cls.getInterfaces()) {
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
			if(isCompatibleVersion(module.twiserVersion())) {
				if(module.modulePriority()==0) {
					_LOW_.add(module);
				} else if(module.modulePriority()==2) {
					_HIGH_.add(module);
				} else {
					_MEDIUM_.add(module);
				}
			} else {
				Twister.current().logError("Module \""+module.moduleName()+"\" is written for Twister version "+Double.toString(module.twiserVersion())+"\" which is incompatible with version "+Double.toString(Twister._VERSION_)+".");
				Twister.current().logError("The module will not be loaded.");
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
				} catch(Exception e) {
					Twister.current().logError("Error occurred while initializing module \""+module.moduleName()+"\":");
					e.printStackTrace();
					Twister.current().logWarning("The module will be removed from the modules stack, but can still be referenced by other modules.");
					_MODULES_.remove(module);
					_HIGH_.remove(module);
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
				} catch(Exception e) {
					Twister.current().logError("Error occurred while initializing module \""+module.moduleName()+"\":");
					Twister.current().logError(e.getClass().getName()+": "+e.getMessage()+"");
					for(StackTraceElement ste : e.getStackTrace()) {
						Twister.current().logError(ste.getClassName()+"("+ste.getFileName()+":"+Integer.toString(ste.getLineNumber())+")");
					}
					Twister.current().logWarning("The module will be removed from the modules stack, but can still be referenced by other modules.");
					_MODULES_.remove(module);
					_MEDIUM_.remove(module);
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
				} catch(Exception e) {
					Twister.current().logError("Error occurred while initializing module \""+module.moduleName()+"\":");
					Twister.current().logError(e.getClass().getName()+": "+e.getMessage()+"");
					for(StackTraceElement ste : e.getStackTrace()) {
						Twister.current().logError(ste.getClassName()+"("+ste.getFileName()+":"+Integer.toString(ste.getLineNumber())+")");
					}
					Twister.current().logWarning("The module will be removed from the modules stack, but can still be referenced by other modules.");
					_MODULES_.remove(module);
					_LOW_.remove(module);
				}
			}
		}
		Twister.current().logInfo("Modules loaded.");
	}
	
	/**
	 * Shuts down all TwisterModules
	 * @deprecated This method's name leads the user to
	 * believe that the modules are actually being
	 * unloaded, when in fact they are simply being
	 * shutdown. Use shutdownModules() instead.
	 * @since 0.1
	 */
	@Deprecated
	public static void unloadModules() {
		shutdownModules();
	}
	
	/**
	 * Shuts down all TwisterModules
	 * @since 0.2
	 */
	public static void shutdownModules() {
		Twister.current().logInfo("Shutting down modules...");
		
		for(TwisterModule module : _LOW_) {
			Twister.current().logInfo("Shutting down module \""+module.moduleName()+"\"...");
			try {
				module.shutdownModule();
			} catch(AbstractMethodError e) {
				Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a shutdown method.");
			}
		}
		for(TwisterModule module : _MEDIUM_) {
			Twister.current().logInfo("Shutting down module \""+module.moduleName()+"\"...");
			try {
				module.shutdownModule();
			} catch(AbstractMethodError e) {
				Twister.current().logError("Module \""+module.moduleName()+"\" does not contain a shutdown method.");
			}
		}
		for(TwisterModule module : _HIGH_) {
			Twister.current().logInfo("Shutting down module \""+module.moduleName()+"\"...");
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
		
		Twister.current().logInfo("Modules shut down.");
	}
	
	/**
	 * Shuts down and loads all modules
	 * Does not actually unload the module classes,
	 * should not be used to load in new modules
	 * or load new versions of existing ones.
	 * @throws IOException if loading modules fails
	 * @throws ZipException if reading the modules fails
	 * @since 0.1
	 */
	public static void reloadModules() throws ZipException, IOException {
		shutdownModules();
		loadModules();
	}
	
	/**
	 * Returns all currently loaded HIGH priority TwisterModules
	 * @return all currently loaded HIGH priority TwisterModules
	 * @since 0.1
	 */
	public static TwisterModule[] getHighPriorityModules() {
		return _HIGH_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Returns all currently loaded MEDIUM priority TwisterModules
	 * @return all currently loaded MEDIUM priority TwisterModules
	 * @since 0.1
	 */
	public static TwisterModule[] getMediumPriorityModules() {
		return _MEDIUM_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Returns all currently loaded LOW priority TwisterModules
	 * @return all currently loaded LOW priority TwisterModules
	 * @since 0.1
	 */
	public static TwisterModule[] getLowPriorityModules() {
		return _LOW_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Returns all currently loaded TwisterModules
	 * @return all currently loaded TwisterModules
	 * @since 0.1
	 */
	public static TwisterModule[] getModules() {
		return _MODULES_.toArray(new TwisterModule[0]);
	}
	
	/**
	 * Checks whether the specified Twister version is compatible this one
	 * @param version the version to check
	 * @return whether the specified version is compatible
	 * @since 0.2
	 */
	public static boolean isCompatibleVersion(double version) {
		boolean compatible = true;
		for(double ver : _INCOMPATIBLE_VERSIONS_) {
			if(version==ver) {
				compatible = false;
				break;
			}
		}
		return compatible;
	}
}
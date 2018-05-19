package net.termer.twister.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.termer.twister.exception.JarLoaderException;

/**
 * Utility class for loading classes from a jarfile
 * @author termer
 *
 */
public class JarLoader {
	private static ArrayList<ClassLoader> cl = new ArrayList<ClassLoader>();
	
	/**
	 * Returns classes from a jarfile
	 * @param jar the File containing the jarfile
	 * @return the classes contained in the jarfile
	 * @throws ZipException if reading the jarfile fails
	 * @throws IOException if loading the jarfile fails
	 * @throws ClassNotFoundException if loading a class from the jarfile fails
	 */
	@SuppressWarnings("rawtypes")
	public static Class[] loadJar(File jar) throws ZipException, IOException, ClassNotFoundException {
		ArrayList<Class> clazzes = new ArrayList<Class>();
		URL url = null;
		ArrayList<String> classes = new ArrayList<String>();
		
		if(jar.getName().toLowerCase().endsWith(".jar")) {
			ZipFile zf = new ZipFile(jar.getAbsolutePath());
			if(zf.isValidZipFile()) {
				url = new URL("file:"+jar.getAbsolutePath());
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
		URLClassLoader ucl = new URLClassLoader(new URL[] {url});
		for(String clazz : classes) {
			clazzes.add(ucl.loadClass(clazz));
		}
		cl.add(ucl);
		ucl.close();
		
		return clazzes.toArray(new Class[0]);
	}
	
	/**
	 * Returns classes from all jarfiles in a directory
	 * @param dir the File containing the directory
	 * @return the classes found in the jarfiles
	 * @throws ClassNotFoundException if loading a class from a jarfile fails
	 * @throws ZipException if reading a jarfile fails
	 * @throws IOException if loading a jarfile fails
	 */
	@SuppressWarnings("rawtypes")
	public static Class[] loadJars(File dir) throws ClassNotFoundException, ZipException, IOException {
		ArrayList<Class> clazzes = new ArrayList<Class>();
		
		if(dir.isDirectory()) {
			for(File jar : dir.listFiles()) {
				if(jar.getName().endsWith(".jar")) {
					for(Class clazz : loadJar(jar)) {
						clazzes.add(clazz);
					}
				}
			}
		} else {
			throw new JarLoaderException("File is not a directory");
		}
		
		return clazzes.toArray(new Class[0]);
	}
}

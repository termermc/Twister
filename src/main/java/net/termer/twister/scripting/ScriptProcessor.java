package net.termer.twister.scripting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import bsh.EvalError;
import bsh.Interpreter;
import net.termer.twister.Settings;
import net.termer.twister.Twister;
import net.termer.twister.document.DocumentBuilder;
import net.termer.twister.document.DocumentProcessor;
import net.termer.twister.document.HTMLDocumentResponse;
import net.termer.twister.utils.Reader;
import spark.Request;
import spark.Response;

/**
 * Utility class and DocumentProcessor to parse and run embedded scripting
 * in documents.
 * @author termer
 * @since 1.1
 */
public class ScriptProcessor implements DocumentProcessor {
	// The list of VariableProviders to be executed before a document is processed for scripting
	private static ArrayList<VariableProvider> _VarProviders = new ArrayList<VariableProvider>();
	
	/**
	 * Registers a new VariableProvider for providing data to scripts
	 * @param provider the VariableProvider to register
	 * @since 1.1
	 */
	public static void addVariableProvider(VariableProvider provider) {
		_VarProviders.add(provider);
	}
	
	/**
	 * Removes the specified VariableProvider from the pool of registered providers
	 * @param provider the VariableProvider to remove
	 * @since 1.1
	 */
	public static void removeVariableProvider(VariableProvider provider) {
		_VarProviders.remove(provider);
	}
	
	/**
	 * Method to process a document and its scripts
	 * @param path the path to the document to process
	 * @param variables the variables to make available to the scripts in the document
	 * @return the processed document
	 * @since 1.1
	 */
	public static String processFile(String path, HashMap<String, Object> variables, String domain) throws IOException {
		String document = Reader.readFile(path);
		
		return processDocument(document, variables, domain);
	}
	
	/**
	 * Method to process a document and its scripts
	 * @param document the document to process
	 * @param variables the variables to make available to the scripts in the document
	 * @return the processed document
	 * @since 1.1
	 */
	public static String processDocument(String document, HashMap<String, Object> variables, String domain) {
		String tmp = document;
		
		if(tmp.contains("<?java") && tmp.contains("?>")) {
			int index = 0;
			boolean proceed = true;
			Interpreter inter = new Interpreter();
			
			// Add variables
			String[] keys = variables.keySet().toArray(new String[0]);
			Object[] values = variables.values().toArray(new Object[0]);
			
			for(int i = 0; i < keys.length; i++) {
				try {
					inter.set(keys[i], values[i]);
				} catch (EvalError ex) {
					ex.printStackTrace();
				}
			}
			
			while(proceed) {
				int opening = tmp.indexOf("<?java", index)+6;
				int closing = tmp.indexOf("?>", index);
				if(opening > 6 && closing > -1) {
					String script = tmp.substring(opening, closing);
					
					Out result = new Out(domain);
					try {
						inter.set("out", result);
						inter.eval(script.trim());
					} catch (Exception e) {
						e.printStackTrace();
						// Append error if enabled
						if(Settings.get("append-scripting-exceptions").equalsIgnoreCase("true")) {
							result.append(e.getMessage());
						}
					}
					tmp = tmp.replace("<?java"+script+"?>", result.toString());
					index++;
				} else {
					proceed = false;
				}
			}
		}
		
		return tmp;
	}
	
	// Method to process a document from an HTMLDocumentResponse object,
	// as required by the DocumentProcessor interface.
	public void process(HTMLDocumentResponse doc, Request req, Response res) {
		if(doc.getText().startsWith("<!--TES-->")) {
			String tmp = doc.getText().substring(10).trim();
			if(tmp.contains("<?java") && tmp.contains("?>")) {
				// Set variables for the script
				HashMap<String, Object> vars = new HashMap<String, Object>();
				vars.put("request", req);
				vars.put("response", res);
				vars.put("twister", Twister.current());
				vars.put("domain", doc.getDomain());
				vars.put("method", req.requestMethod());
				
				// Process the variables map using registered VariableProviders
				for(VariableProvider provider : _VarProviders) {
					provider.provide(doc.getDomain(), vars);
				}
				
				// Run document through processor
				doc.setText(processDocument(tmp, vars, doc.getDomain()));
			}
		}
	}
	
	// Class to be used by scripts to output content
	public static class Out {
		private StringBuilder result = new StringBuilder();
		private String dom = null;
		
		public Out(String domain) {
			dom = domain;
		}
		
		public void append(Object content) {
			result.append(content.toString());
		}
		
		public void include(String path) {
			include(path, false);
		}
		
		private String processInclude(String content, boolean escapeScripts) {
			String tmp = content;
			if(tmp.startsWith("<!--TES-->")) {
				tmp = tmp.substring(10).trim();
			}
			if(escapeScripts) {
				tmp = tmp
						.replace("<?", "&lt?")
						.replace("?>", "?&gt");
			}
			return tmp;
		}
		
		// Appends a file or a directory of files.
		// path - the path of the file or directory
		// escapeScripts - whether openers and closers should be escaped
		public void include(String path, boolean escapeScripts) {
			try {
				File inc = new File("domains/"+dom+"/"+path);
				if(inc.isFile()) {
					String content = DocumentBuilder.readFile(inc.getPath());
					
					// Process content before appending it
					content = processInclude(content, escapeScripts);
					
					// Append content
					result.append(content);
				} else if(inc.isDirectory()) {
					for(File file : inc.listFiles()) {
						String content = DocumentBuilder.readFile(file.getPath());
						
						// Process content before appending it
						content = processInclude(content, escapeScripts);
						
						// Append content
						result.append(content);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public String toString() {
			return result.toString();
		}
	}
	
	public static ScriptProcessor current = null;
}

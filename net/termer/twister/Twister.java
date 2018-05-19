package net.termer.twister;

import static spark.Spark.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.termer.twister.handler.RequestHandler;
import net.termer.twister.module.ModuleManager;
import net.termer.twister.document.DocumentBuilder;
import net.termer.twister.utils.Config;
import net.termer.twister.utils.StringFilter;
import net.termer.twister.utils.Writer;

/**
 * The Twister main class.
 * Contains most main Twister methods.
 * @author termer
 *
 */
public class Twister {
	
	// Twister version
	public static double _VERSION_ = 0.1;
	
	/**
	 * Shared variables for using across modules
	 */
	public HashMap<String,Object> sharedVariables = new HashMap<String,Object>();
	
	// Various config maps
	
	/**
	 * Twister settings map
	 */
	// Twister settings map
	public static HashMap<String,String> settings = null;
	/**
	 * Twister linked domains map
	 */
	// Twister linked domains map
	public static HashMap<String,String> linkedDomains = null;
	
	/**
	 * Forbidden paths list
	 */
	// Forbidden directories list
	public static ArrayList<String> forbiddenPaths = new ArrayList<String>();
	
	// The current instance of Twister
	protected static Twister twister = null;
	
	// Request handlers
	private ArrayList<HashMap<String,HashMap<String,RequestHandler>>> requestHandlers = new ArrayList<HashMap<String,HashMap<String,RequestHandler>>>();
	
	// Handler to be executed before all requests
	private RequestHandler beforeHandler = null;
	
	// Default domain
	private String defaultDomain = null;
	
	// Configuration files
	private File domainsDir = new File("domains/");
	private File notfoundFile = new File("404.html");
	private File settingsFile = new File("twister.ini");
	private File linkedFile = new File("linkeddomains.ini");
	private File globalstaticFile = null;
	private File modulesDirectory = new File("modules/");
	private File forbiddenPathsFile = new File("forbiddenpaths.ini");
	private File dependenciesDirectory = new File("dependencies/");
	
	/**
	 * Returns the current Twister instance
	 * @return the current Twister instance
	 */
	public static Twister current() {
		return twister;
	}
	
	/**
	 * Instantiates Twister
	 */
	protected Twister() {
		
		// Setup request handler maps
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>());
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>());
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>());
		
		// Setup files and directories
		reloadConfigurations();
		
		// Set global static files location
		staticFiles.externalLocation(globalstaticFile.getAbsolutePath());
		
		// Set port to run on
		if(settings.containsKey("port")) {
			port(Integer.parseInt(settings.get("port")));
		} else {
			port(2003);
		}
		// Set IP address to bind to
		if(settings.containsKey("ip")) {
			ipAddress(settings.get("ip"));
		} else {
			ipAddress("127.0.0.1");
		}
		// Enable HTTPS if keystore is available
		if(settings.containsKey("keystore") && settings.containsKey("keystore-password")) {
			File ks = new File(settings.get("keystore"));
			if(ks.exists()) {
				secure(settings.get("keystore"),settings.get("keystore-password"),null,null);
			}
		}
		
		// Before requests get handed off to handlers, log if enabled
		// as well as disallow requests to forbidden directories
		before("*", (req, res) -> {
			// If logging enabled, log access to console
			if(settings.containsKey("logging")) {
				if(settings.get("logging").toLowerCase().startsWith("t")) {
					String ln = new java.util.Date().toString()+": GET "+req.pathInfo()+" ("+req.ip()+" "+req.userAgent()+")";
					System.out.println(ln);
				}
			}
			
			// Check for a before handler and execute if present
			if(beforeHandler != null) {
				beforeHandler.handle(req, res);
			}
			
			boolean bad = false;
			for(String path : forbiddenPaths) {
				if(!path.startsWith("/")) path="/"+path;
				if(!path.endsWith("/")) path+="/";
				
				if(StringFilter.same(req.pathInfo(), path)) {
					bad = true;
					break;
				}
			}
			if(bad) {
				halt(403,"Forbidden");
			}
		});
		
		// Handle GET requests
		get("*", (req, res) -> {
			// Result
			String r = "";
			
			// Determine domain
			String domain = req.url();
			if(domain.toLowerCase().startsWith("http://")) {
				domain=domain.substring(7);
			} else if(domain.toLowerCase().startsWith("https://")) {
				domain=domain.substring(8);
			}
			domain=domain.replaceAll(req.pathInfo(),"");
			if(domain.contains(":")) {
				domain=domain.split(":")[0];
			}
			domain=domain.toLowerCase();
			
			// Determine what to send
			
			// Determine URL with "/" added to the end of the path
			String redirectURL = req.pathInfo()+"/";
			if(req.queryMap().toMap().keySet().size()>0) {
				redirectURL+="?";
				for(String key : req.queryMap().toMap().keySet()) {
					if(!redirectURL.endsWith("?")) {
						redirectURL+="&";
					}
					redirectURL+=key+"="+StringFilter.encodeURIComponent(req.queryParams(key));
				}
			}
			
			// Determine if there is a request handler available for domain and path
			boolean handlerAvailable = false;
			if(requestHandlers.get(0).containsKey(domain.toLowerCase())) {
				String path = req.pathInfo();
				if(!path.endsWith("/")) path=path+"/";
				if(requestHandlers.get(0).get(domain.toLowerCase()).containsKey(path.toLowerCase())) {
					handlerAvailable = true;
				}
			}
			if(new File("domains/"+domain+"/"+req.pathInfo()).isDirectory()) {
				if(!req.pathInfo().endsWith("/")) {
					res.redirect(redirectURL);
				} else {
					
					// If handler available, use it instead of loading static
					if(handlerAvailable) {
						r = requestHandlers.get(0).get(domain.toLowerCase()).get(req.pathInfo().toLowerCase()).handle(req, res);
					} else {
						r = DocumentBuilder.loadDocument(domain, req.pathInfo(), req, res);
					}
				}
			} else {
				// If handler available, use it instead of loading static
				if(handlerAvailable) {
					if(!req.pathInfo().endsWith("/")) {
						res.redirect(redirectURL);
					} else {
						r = requestHandlers.get(0).get(domain.toLowerCase()).get(req.pathInfo().toLowerCase()).handle(req, res);
					}
				} else {
					r = DocumentBuilder.loadDocument(domain, req.pathInfo(), req, res);
				}
			}
			return r;
		});
		
		// Handle POST requests
		post("*", (req, res) -> {
			// Result
			String r = "";
			
			// Determine domain
			String domain = req.url();
			if(domain.toLowerCase().startsWith("http://")) {
				domain=domain.substring(7);
			} else if(domain.toLowerCase().startsWith("https://")) {
				domain=domain.substring(8);
			}
			domain=domain.replaceAll(req.pathInfo(),"");
			if(domain.contains(":")) {
				domain=domain.split(":")[0];
			}
			domain=domain.toLowerCase();
			
			// Determine what to send
			
			// Determine URL with "/" added to the end of the path
			String redirectURL = req.pathInfo()+"/";
			if(req.queryMap().toMap().keySet().size()>0) {
				redirectURL+="?";
				for(String key : req.queryMap().toMap().keySet()) {
					if(!redirectURL.endsWith("?")) {
						redirectURL+="&";
					}
					redirectURL+=key+"="+StringFilter.encodeURIComponent(req.queryParams(key));
				}
			}
			
			// Determine if there is a request handler available for domain and path
			boolean handlerAvailable = false;
			if(requestHandlers.get(1).containsKey(domain.toLowerCase())) {
				String path = req.pathInfo();
				if(!path.endsWith("/")) path=path+"/";
				if(requestHandlers.get(1).get(domain.toLowerCase()).containsKey(path.toLowerCase())) {
					handlerAvailable = true;
				}
			}
			if(new File("domains/"+domain+"/"+req.pathInfo()).isDirectory()) {
				if(!req.pathInfo().endsWith("/")) {
					res.redirect(redirectURL);
				} else {
					
					// If handler available, use it instead of loading static
					if(handlerAvailable) {
						r = requestHandlers.get(1).get(domain.toLowerCase()).get(req.pathInfo().toLowerCase()).handle(req, res);
					} else {
						r = DocumentBuilder.loadDocument(domain, req.pathInfo(), req, res);
					}
				}
			} else {
				// If handler available, use it instead of loading static
				if(handlerAvailable) {
					if(!req.pathInfo().endsWith("/")) {
						res.redirect(redirectURL);
					} else {
						r = requestHandlers.get(1).get(domain.toLowerCase()).get(req.pathInfo().toLowerCase()).handle(req, res);
					}
				} else {
					r = DocumentBuilder.loadDocument(domain, req.pathInfo(), req, res);
				}
			}
			return r;
		});
		
		// Handle DELETE requests
		delete("*", (req, res) -> {
			// Result
			String r = "";
			
			// Determine domain
			String domain = req.url();
			if(domain.toLowerCase().startsWith("http://")) {
				domain=domain.substring(7);
			} else if(domain.toLowerCase().startsWith("https://")) {
				domain=domain.substring(8);
			}
			domain=domain.replaceAll(req.pathInfo(),"");
			if(domain.contains(":")) {
				domain=domain.split(":")[0];
			}
			domain=domain.toLowerCase();
			
			// Determine what to send
			
			// Determine URL with "/" added to the end of the path
			String redirectURL = req.pathInfo()+"/";
			if(req.queryMap().toMap().keySet().size()>0) {
				redirectURL+="?";
				for(String key : req.queryMap().toMap().keySet()) {
					if(!redirectURL.endsWith("?")) {
						redirectURL+="&";
					}
					redirectURL+=key+"="+StringFilter.encodeURIComponent(req.queryParams(key));
				}
			}
			
			// Determine if there is a request handler available for domain and path
			boolean handlerAvailable = false;
			if(requestHandlers.get(2).containsKey(domain.toLowerCase())) {
				String path = req.pathInfo();
				if(!path.endsWith("/")) path=path+"/";
				if(requestHandlers.get(2).get(domain.toLowerCase()).containsKey(path.toLowerCase())) {
					handlerAvailable = true;
				}
			}
			if(new File("domains/"+domain+"/"+req.pathInfo()).isDirectory()) {
				if(!req.pathInfo().endsWith("/")) {
					res.redirect(redirectURL);
				} else {
					
					// If handler available, use it instead of loading static
					if(handlerAvailable) {
						r = requestHandlers.get(2).get(domain.toLowerCase()).get(req.pathInfo().toLowerCase()).handle(req, res);
					} else {
						r = DocumentBuilder.loadDocument(domain, req.pathInfo(), req, res);
					}
				}
			} else {
				// If handler available, use it instead of loading static
				if(handlerAvailable) {
					if(!req.pathInfo().endsWith("/")) {
						res.redirect(redirectURL);
					} else {
						r = requestHandlers.get(2).get(domain.toLowerCase()).get(req.pathInfo().toLowerCase()).handle(req, res);
					}
				} else {
					r = DocumentBuilder.loadDocument(domain, req.pathInfo(), req, res);
				}
			}
			return r;
		});
		
	}
	
	/**
	 * (Re)loads all configuration files
	 */
	// Loads all configuration files
	public void reloadConfigurations() {
		// Domains directory
		if(!domainsDir.exists()) {
			domainsDir.mkdirs();
		}
		
		// Dependencies directory
		if(!dependenciesDirectory.exists()) {
			dependenciesDirectory.mkdirs();
		}
		
		// 404 Error Page
		if(!notfoundFile.exists()) {
			try {
				notfoundFile.createNewFile();
				Writer.print("<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p>File could not be located on this server.</p></body></html>", notfoundFile);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
			
		// Settings file
		if(!settingsFile.exists()) {
			try {
				settingsFile.createNewFile();
				Writer.print("# IP address to bind to\n"+
						"ip: 127.0.0.1\n\n"+
						"# Port to run server on\n"+
						"port: 2003\n\n"+
						"# Java Keystore path to use to enable HTTPS\n"+
						"# Leave commented out to disable HTTPS\n"+
						"#keystore: keystore.jks\n\n"+
						"# Keystore password\n"+
						"#keystore-password: drowssap\n\n"+
						"# Enable/Disable logging\n"+
						"logging: true\n\n"+
						"# Global static directory location\n"+
						"static: globalstatic/\n\n"+
						"# Default domain for modules to access\n"+
						"default-domain: localhost", settingsFile);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		// Parse settings
		try {
			settings = Config.parseConfig(settingsFile, ":", "#");
		} catch(IOException e) {
			System.err.println("=========================\n"+
							   "Failed to parse settings\n"+
							   "=========================");
			e.printStackTrace();
		}
		
		// Set default domain
		if(settings.containsKey("default-domain")) {
			defaultDomain = settings.get("default-domain");
		} else {
			defaultDomain = "localhost";
		}
		
		// If static directory is set in the settings file, apply it
		if(settings.containsKey("static")) {
			File dir = new File(settings.get("static"));
			if(!dir.exists()) {
				dir.mkdirs();
			}
			globalstaticFile = dir;
			staticFiles.externalLocation(globalstaticFile.getAbsolutePath());
		}
		
		// Linked Domains file
		if(!linkedFile.exists()) {
			try {
				linkedFile.createNewFile();
				Writer.print("# You can specify domains that will\n"+
							 "# use another domains's file system\n"+
							 "# by using a variant of the below:\n"+
							 "127.0.0.1 > localhost\n"+
							 "# The above specified that when\n"+
							 "# users connect to 127.0.0.1 they\n"+
							 "# will be served with the same\n"+
							 "# filesystem system as they would\n"+
							 "# if they connected to localhost", linkedFile);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		// Parse linked domains
		try {
			linkedDomains = Config.parseConfig(linkedFile, ">", "#");
		} catch(IOException e) {
			System.err.println("===============================\n"+
							   "Failed to parse linked domains\n"+
							   "===============================");
			e.printStackTrace();
		}
		
		// Global Static files directory
		if(!globalstaticFile.exists()) {
			globalstaticFile.mkdirs();
		}
		
		// Twister modules directory
		if(!modulesDirectory.exists()) {
			modulesDirectory.mkdirs();
		}
		
		// Forbidden paths file
		if(!forbiddenPathsFile.exists()) {
			try {
				forbiddenPathsFile.createNewFile();
				Writer.print("# Add forbidden paths.\n"+
							 "# All paths are separated\n"+
							 "# by newlines.\n", forbiddenPathsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			forbiddenPaths = Config.getLines(forbiddenPathsFile, "#");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to register request handlers
	 * @param domain - the domain to register the handler for
	 * @param path - the path to register the handler for
	 * @param handler - the handler
	 * @param method - the HTTP method (Method.GET/POST/DELETE)
	 */
	// Method to register request handlers
	public void addRequestHandler(String domain, String path, RequestHandler handler, int method) {
		// Determine correct request handler map
		
		// If path does not end with "/", add it
		if(!path.endsWith("/")) path+="/";
		
		// If path does not start with "/", add it
		if(!path.startsWith("/")) path="/"+path;
		
		// If no domain exists in the handler map, create one
		if(!requestHandlers.get(method).containsKey(domain.toLowerCase())) {
			requestHandlers.get(method).put(domain.toLowerCase(), new HashMap<String,RequestHandler>());
		}
		
		// If handler for domain and path already exist, remove it
		if(requestHandlers.get(method).get(domain.toLowerCase()).containsKey(path.toLowerCase())) {
			requestHandlers.get(method).get(domain.toLowerCase()).remove(path.toLowerCase());
		}
		
		// Add the handler
		requestHandlers.get(method).get(domain.toLowerCase()).put(path.toLowerCase(), handler);
	}
	
	/**
	 * Method to unregister a request handler
	 * @param domain - the domain to unregister the handler from
	 * @param path - the path to unregister the handler from
	 * @param method - the HTTP method (Method.GET/POST/DELETE) to unregister the handler from
	 */
	// Method to unregister a request handler
	public void removeRequestHandler(String domain, String path, int method) {
		if(requestHandlers.get(method).containsKey(domain.toLowerCase())) {
			requestHandlers.get(method).get(domain.toLowerCase()).remove(path.toLowerCase());
		} else {
			logError("No RequestHandlers for domain \""+domain.toLowerCase()+"\"");
		}
	}
	
	/**
	 * Returns whether a before handler is present
	 * @return whether a before handler is present
	 */
	// Returns whether a before handler is present
	public boolean isBeforeHandlerPresent() {
		return beforeHandler!=null;
	}
	
	/**
	 * Returns the handler that gets executed before all requests
	 * @return the handler that gets executed before all requests
	 */
	// Returns the handler that gets executed before all requests
	public RequestHandler getBeforeRequestHandler() {
		return beforeHandler;
	}
	
	/**
	 * Sets the handler to be executed before all requests
	 * Setting to null will clear the handler
	 * @param handler the handler to execute before all requests
	 */
	// Sets the handler to be executed before all requests
	public void setBeforeRequestHandler(RequestHandler handler) {
		beforeHandler = handler;
	}
	
	/**
	 * Clears the before request handler
	 */
	// Clears the before request handler
	public void clearBeforeRequestHandler() {
		beforeHandler = null;
	}
	
	/**
	 * Returns the default domain to be used by modules
	 * @return the default domain to be used by modules
	 */
	// Returns the default domain to be used by modules
	public String getDefaultDomain() {
		return defaultDomain;
	}
	
	/**
	 * Returns a list of handlers assigned to the specified domain and method
	 * @param domain - the domain
	 * @param method - the method
	 * @return a list of handlers assigned to the specified domain and method
	 */
	// Returns a list of handlers assigned to the specified domain and method
	public RequestHandler[] getRequestHandlers(String domain, int method) {
		ArrayList<RequestHandler> handlers = new ArrayList<RequestHandler>();
		if(requestHandlers.size()>method) {
			if(requestHandlers.get(method).containsKey(domain.toLowerCase())) {
				for(RequestHandler handler : requestHandlers.get(method).get(domain.toLowerCase()).values()) {
					handlers.add(handler);
				}
			}
		}
		return handlers.toArray(new RequestHandler[0]);
	}
	
	/**
	 * Returns whether the specified domain, method, and path has a RequestHandler assigned to it
	 * @param domain - the domain
	 * @param method - the method
	 * @param path - the path
	 * @return whether the specified domain, method, and path has a RequestHandler assigned to it
	 */
	// Returns whether the specified domain, method, and path has a RequestHandler assigned to it
	public boolean hasRequestHandler(String domain, int method, String path) {
		boolean has = false;
		
		if(!path.startsWith("/")) path="/"+path;
		if(!path.endsWith("/")) path+="/";
		
		if(requestHandlers.size()>method) {
			if(requestHandlers.get(method).containsKey(domain.toLowerCase())) {
				has = requestHandlers.get(method).get(domain.toLowerCase()).containsKey(path.toLowerCase());
			}
		}
		
		return has;
	}
	
	/**
	 * Returns the RequestHandler for the specified domain, method, and path
	 * Returns null if no handler is registered for the domain, method, and path
	 * @param domain - the domain
	 * @param method - the method
	 * @param path - the path
	 * @return the RequestHandler for the specified domain, method, and path
	 */
	// Returns the RequestHandler for the specified domain, method, and path
	public RequestHandler getRequestHandler(String domain, int method, String path) {
		RequestHandler handler = null;
		
		if(!path.startsWith("/")) path="/"+path;
		if(!path.endsWith("/")) path+="/";
		
		if(requestHandlers.size()>method) {
			if(requestHandlers.get(method).containsKey(domain.toLowerCase())) {
				if(requestHandlers.get(method).get(domain.toLowerCase()).containsKey(path.toLowerCase())) {
					handler = requestHandlers.get(method).get(domain.toLowerCase()).get(path.toLowerCase());
				}
			}
		}
		
		return handler;
	}
	
	/**
	 * Log info message to the console
	 * @param msg - the message to log
	 */
	// Log info message to the console
	public void logInfo(String msg) {
		String prefix = new Date().toString()+" INFO ";
		if(msg.contains("\n")) {
			System.out.println(prefix+msg);
		} else {
			for(String str : msg.split("\n")) {
				System.out.println(prefix+str);
			}
		}
	}
	
	/**
	 * Log error message to the console
	 * @param msg - the message to log
	 */
	// Log error message to the console
	public void logError(String msg) {
		String prefix = new Date().toString()+" ERROR ";
		if(msg.contains("\n")) {
			System.err.println(prefix+msg);
		} else {
			for(String str : msg.split("\n")) {
				System.err.println(prefix+str);
			}
		}
	}
	
	/**
	 * Safely shutdown Twister and all of its modules
	 */
	// Safely shutdown Twister and all of its modules
	public void shutdown() {
		logInfo("Shutting down Twister...");
		ModuleManager.unloadModules();
		System.exit(0);
	}
}
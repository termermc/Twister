package net.termer.twister;

import static spark.Spark.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.termer.twister.handler.PreRequestHandler;
import net.termer.twister.handler.PreRequestOptions;
import net.termer.twister.handler.RequestHandler;
import net.termer.twister.handler.RouteHandler;
import net.termer.twister.module.ModuleManager;
import net.termer.twister.caching.CachingThread;
import net.termer.twister.document.DocumentBuilder;
import net.termer.twister.document.DocumentProcessor;
import net.termer.twister.utils.Config;
import net.termer.twister.utils.StringFilter;
import net.termer.twister.utils.Writer;
import spark.Request;
import spark.Response;

/**
 * The Twister main class.
 * Contains most main Twister methods.
 * @author termer
 * @since 0.1
 */
public class Twister {
	
	/**
	 * Twister version
	 * @since 0.1
	 */
	public static double _VERSION_ = 1.1;
	
	/**
	 * Shared variables for using across modules
	 * @since 0.1
	 */
	public HashMap<String,Object> sharedVariables = new HashMap<String,Object>();
	
	// Various config maps
	
	/**
	 * Twister settings map
	 * @since 0.1
	 */
	public static HashMap<String,String> settings = new HashMap<String,String>();
	
	/**
	 * Twister redirect paths map
	 * @since 0.2
	 */
	public static HashMap<String,HashMap<String,String>> domainRedirects = new HashMap<String,HashMap<String,String>>();
	
	/**
	 * Twister linked domains map
	 * @since 0.1
	 */
	public static HashMap<String,String> linkedDomains = null;
	
	/**
	 * Forbidden paths list
	 * @since 0.1
	 */
	public static ArrayList<String> forbiddenPaths = new ArrayList<String>();
	
	protected static Twister twister = null;
	
	private ArrayList<HashMap<String,HashMap<String,RequestHandler>>> requestHandlers = new ArrayList<HashMap<String,HashMap<String,RequestHandler>>>();
	private ArrayList<PreRequestHandler> preRequestHandlers = new ArrayList<PreRequestHandler>();
	private ArrayList<HashMap<String,HashMap<String,RouteHandler>>> routeHandlers = new ArrayList<HashMap<String,HashMap<String,RouteHandler>>>();
	
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
	
	private CachingThread cachingThread = null;
	
	/**
	 * Returns the current Twister instance
	 * @return the current Twister instance
	 * @since 0.1
	 */
	public static Twister current() {
		return twister;
	}
	
	/**
	 * Instantiates Twister
	 * @since 0.1
	 */
	protected Twister() {
		
		// Setup request handler maps
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>()); // GET
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>()); // POST
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>()); // DELETE
		requestHandlers.add(new HashMap<String,HashMap<String,RequestHandler>>()); // PUT
		
		// Setup route handler maps
		routeHandlers.add(new HashMap<String,HashMap<String,RouteHandler>>()); // GET
		routeHandlers.add(new HashMap<String,HashMap<String,RouteHandler>>()); // POST
		routeHandlers.add(new HashMap<String,HashMap<String,RouteHandler>>()); // DELETE
		routeHandlers.add(new HashMap<String,HashMap<String,RouteHandler>>()); // PUT
		
		// Extract README
		try {
			InputStream readmeInput = getClass().getResource("/resources/README").openStream();
			FileOutputStream readmeOut = new FileOutputStream(new File("README"));
			while(readmeInput.available()>0) {
				readmeOut.write(readmeInput.read());
			}
			readmeInput.close();
			readmeOut.close();
		} catch (IOException e) {
			logError("Failed to extract README file from jar");
			e.printStackTrace();
		}
		
		// Instantiate caching thread
		cachingThread = new CachingThread();
		
		// Setup files and directories
		reloadConfigurations();
		
		staticFiles.externalLocation(globalstaticFile.getAbsolutePath());
		
		port(Integer.parseInt(Settings.get("port")));
		
		ipAddress(Settings.get("ip"));
		
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
			if(Settings.get("logging").toLowerCase().startsWith("t")) {
				String ln = new java.util.Date().toString()+": GET "+req.pathInfo()+" ("+req.ip()+" "+req.userAgent()+")";
				System.out.println(ln);
			}
			
			boolean bad = false;
			for(String path : forbiddenPaths) {
				if(!path.startsWith("/")) path="/"+path;
				if(!path.endsWith("/")) path+="/";
				
				if(StringFilter.same(path, path)) {
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
			return handleRequest(req, res, 0);
		});
		
		// Handle POST requests
		post("*", (req, res) -> {
			return handleRequest(req, res, 1);
		});
		
		// Handle DELETE requests
		delete("*", (req, res) -> {
			return handleRequest(req, res, 2);
		});
		
		// Handle PUT requests
		put("*", (req, res) -> {
			return handleRequest(req, res, 3);
		});
		
		logInfo("Starting caching thread...");
		if(!cachingThread.isAlive()) {
			try {
				cachingThread.start();
			} catch(Exception e) {
				logError("Error starting caching thread:");
				e.printStackTrace();
			}
		}
		logInfo("Started.");
	}
	
	
	// Processes a request
	private String handleRequest(Request req, Response res, int method) throws IOException {
		// Result
		String r = "";
		
		// Store path
		String path = req.pathInfo();
		
		// Determine domain
		String domain = req.url();
		if(domain.toLowerCase().startsWith("http://")) {
			domain=domain.substring(7);
		} else if(domain.toLowerCase().startsWith("https://")) {
			domain=domain.substring(8);
		}
		domain=domain.replace(path,"");
		if(domain.contains(":")) {
			domain=domain.split(":")[0];
		}
		domain=domain.toLowerCase();
		
		// Execute pre-request handlers
		PreRequestOptions preOptions = new PreRequestOptions(req, res, domain);
		
		for(PreRequestHandler handler : preRequestHandlers) {
			handler.handle(preOptions);
		}
		
		// Set path and domain to values provided by the pre-request handlers
		path = preOptions.getPath();
		domain = preOptions.getDomain();
		
		// If the request was cancelled by a pre-request handler...
		if(preOptions.isCancelled()) {
			r = preOptions.getCancelText();
		} else {
			// Check if there is a redirect assigned for the requested path
			boolean noRedirect = true;
			if(domainRedirects.containsKey(domain)) {
				HashMap<String,String> redirects = domainRedirects.get(domain);
				if(redirects.containsKey("*")) {
					noRedirect=false;
					res.redirect(redirects.get("*"));
				} else {
					if(redirects.containsKey(path)) {
						noRedirect=false;
						res.redirect(redirects.get(path));
					} else if(redirects.containsKey(path+'/')) {
						noRedirect=false;
						res.redirect(redirects.get(path+'/'));
					} else if(redirects.containsKey(path.substring(1))) {
						noRedirect=false;
						res.redirect(redirects.get(path.substring(1)));
					}
				}
			}
			
			if(noRedirect) {
				// Determine what to send
				
				// Determine URL with "/" added to the end of the path
				String redirectURL = path+"/";
				if(req.queryMap().toMap().keySet().size()>0) {
					redirectURL+="?";
					for(String key : req.queryMap().toMap().keySet()) {
						if(!redirectURL.endsWith("?")) {
							redirectURL+="&";
						}
						redirectURL+=key+"="+StringFilter.encodeURIComponent(req.queryParams(key));
					}
				}
				
				// Determine if there is a route handler available for the domain
				RouteHandler routeHandler = null;
				String route = null;
				
				if(routeHandlers.get(method).containsKey(domain.toLowerCase())) {
					String[] routes = routeHandlers.get(method).get(domain.toLowerCase()).keySet().toArray(new String[0]);
					RouteHandler[] handlers = routeHandlers.get(method).get(domain.toLowerCase()).values().toArray(new RouteHandler[0]);
					for(int i = 0; i < routes.length; i++) {
						if(StringFilter.matchesRoute(routes[i], path)) {
							routeHandler = handlers[i];
							route = routes[i];
						}
					}
				}
				
					
				if(routeHandler == null) {
					// Determine if there is a request handler available for domain and path
					boolean handlerAvailable = false;
					if(requestHandlers.get(method).containsKey(domain.toLowerCase())) {
						if(requestHandlers.get(method).get(domain.toLowerCase()).containsKey(path.toLowerCase())) {
							handlerAvailable = true;
						} else if(requestHandlers.get(method).get(domain.toLowerCase()).containsKey(path.toLowerCase()+"/")) {
							handlerAvailable = true;
						}
					}
					if(new File("domains/"+domain+"/"+path).isDirectory()) {
						if(!path.endsWith("/")) {
							res.redirect(redirectURL);
						} else {
							// If handler available, use it instead of loading static
							if(handlerAvailable) {
								r = requestHandlers.get(method).get(domain.toLowerCase()).get(path.toLowerCase()).handle(req, res);
							} else {
								r = DocumentBuilder.loadDocument(domain, path, req, res);
							}
						}
					} else {
						// If handler available, use it instead of loading static
						if(handlerAvailable) {
							if(!path.endsWith("/")) {
								res.redirect(redirectURL);
							} else {
								r = requestHandlers.get(method).get(domain.toLowerCase()).get(path.toLowerCase()).handle(req, res);
							}
						} else {
							r = DocumentBuilder.loadDocument(domain, path, req, res);
						}
					}
				} else {
					// Get wildcards for route, then execute the route handler
					r = routeHandler.handle(req, res, StringFilter.processRoute(route, path));
				}
			}
		}
		return r;
	}
	
	/**
	 * Returns the current caching thread
	 * @since 0.2
	 */
	public CachingThread getCachingThread() {
		return cachingThread;
	}
	
	/**
	 * (Re)loads all configuration files
	 * @since 0.1
	 */
	public void reloadConfigurations() {
		// Domains directory
		if(!domainsDir.exists()) {
			domainsDir.mkdirs();
		}
		
		// Enumerate domain redirect paths
		domainRedirects.clear();
		for(File dir : domainsDir.listFiles()) {
			if(dir.isDirectory()) {
				String tmp = dir.getPath();
				if(!tmp.endsWith("/")) tmp+='/';
				File redirects = new File(tmp+"redirects.ini");
				if(redirects.exists()) {
					try {
						domainRedirects.put(dir.getName(), Config.parseConfig(redirects, ">", "#"));
					} catch (IOException e) {
						logError("Failed to load redirects file for domain "+dir.getName());
						e.printStackTrace();
					}
				}
			}
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
		try {
			Settings.reload();
			if(!settingsFile.exists()) {
				settingsFile.createNewFile();
				String stngs = "# IP address to bind to\n"+
						"ip: "+Settings.getDefault("ip")+"\n\n"+
						"# Port to the run server on\n"+
						"port: "+Settings.getDefault("port")+"\n\n"+
						"# Java Keystore path to use to enable HTTPS\n"+
						"# Leave commented out to disable HTTPS\n"+
						"#keystore: "+Settings.getDefault("keystore")+"\n\n"+
						"# Keystore password\n"+
						"#keystore-password: "+Settings.getDefault("keystore-password")+"\n\n"+
						"# Enable/Disable logging\n"+
						"logging: "+Settings.getDefault("logging")+"\n\n"+
						"# Global static directory location\n"+
						"static: "+Settings.getDefault("static")+"\n\n"+
						"# Default domain for modules to access\n"+
						"default-domain: "+Settings.getDefault("default-domain")+"\n\n"+
						"# Whether Twister should cache files such as\n"+
						"# domain tops and bottoms and 404 messages in\n"+
						"# RAM, instead of serving them from disk\n"+
						"caching: "+Settings.getDefault("caching")+"\n\n"+
						"# The interval in seconds when Twister should\n"+
						"# update the cached files in RAM\n"+
						"caching-interval: "+Settings.getDefault("caching-interval")+"\n\n"+
						"# Enable/Disable embedded scripting\n"+
						"scripting: "+Settings.getDefault("scripting");
				Writer.print(stngs, settingsFile);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Parse settings
		try {
			Settings.reload();
		} catch(IOException e) {
			System.err.println("=========================\n"+
							   "Failed to parse settings\n"+
							   "=========================");
			e.printStackTrace();
		}
		
		// Set default domain
		defaultDomain = Settings.get("default-domain");
		
		// If static directory is set in the settings file, apply it
		File dir = new File(Settings.get("static"));
		if(!dir.exists()) {
			dir.mkdirs();
		}
		globalstaticFile = dir;
		
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
		
		// Re-cache files
		CachingThread.cache404();
		CachingThread.cacheTopsAndBottoms();
	}
	
	/**
	 * Method to register request handlers
	 * @param domain - the domain to register the handler for
	 * @param path - the path to register the handler for
	 * @param handler - the handler
	 * @param method - the HTTP method (Method.GET/POST/DELETE/PUT)
	 * @since 0.1
	 */
	public void addRequestHandler(String domain, String path, RequestHandler handler, int method) {
		if(handler != null && domain != null && path != null) {
			// Determine correct request handler map
			
			// If path does not end with "/", add it
			if(!path.endsWith("/")) path+="/";
			
			// If path does not start with "/", add it
			if(!path.startsWith("/")) path="/"+path;
			
			// If no domain exists in the handler map, create one
			if(!requestHandlers.get(method).containsKey(domain.toLowerCase())) {
				requestHandlers.get(method).put(domain.toLowerCase(), new HashMap<String,RequestHandler>());
			}
			
			// Add the handler
			requestHandlers.get(method).get(domain.toLowerCase()).put(path.toLowerCase(), handler);
		}
	}
	
	/**
	 * Method to unregister a request handler
	 * @param domain - the domain to unregister the handler from
	 * @param path - the path to unregister the handler from
	 * @param method - the HTTP method (Method.GET/POST/DELETE) to unregister the handler from
	 * @since 0.1
	 */
	public void removeRequestHandler(String domain, String path, int method) {
		if(requestHandlers.get(method).containsKey(domain.toLowerCase())) {
			requestHandlers.get(method).get(domain.toLowerCase()).remove(path.toLowerCase());
		} else {
			logError("No RequestHandlers for domain \""+domain.toLowerCase()+"\"");
		}
	}
	
	/**
	 * Method to register route handlers
	 * @param domain - the domain to register the handler for
	 * @param route - the route to register the handler for
	 * @param handler - the handler
	 * @param method - the HTTP method (Method.GET/POST/DELETE/PUT)
	 * @since 1.0
	 */
	public void addRouteHandler(String domain, String route, RouteHandler handler, int method) {
		if(handler != null && domain != null && route != null) {
			// Determine correct request handler map
			
			// If path does not start with "/", add it
			if(!route.startsWith("/")) route="/"+route;
			
			// If no domain exists in the handler map, create one
			if(!routeHandlers.get(method).containsKey(domain.toLowerCase())) {
				routeHandlers.get(method).put(domain.toLowerCase(), new HashMap<String,RouteHandler>());
			}
			
			// Add the handler
			routeHandlers.get(method).get(domain.toLowerCase()).put(route.toLowerCase(), handler);
		}
	}
	
	/**
	 * Method to unregister a route handler
	 * @param domain - the domain to unregister the handler from
	 * @param route - the route to unregister the handler from
	 * @param method - the HTTP method (Method.GET/POST/DELETE) to unregister the handler from
	 * @since 1.0
	 */
	public void removeRouteHandler(String domain, String route, int method) {
		if(routeHandlers.get(method).containsKey(domain.toLowerCase())) {
			routeHandlers.get(method).get(domain.toLowerCase()).remove(route.toLowerCase());
		} else {
			logError("No RouteHandlers for domain \""+domain.toLowerCase()+"\"");
		}
	}
	
	/**
	 * Method to register a DocumentProcessor for domain
	 * @param domain the domain
	 * @param processor the DocumentProcessor
	 * @since 0.2
	 */
	public void addDocumentProcessor(String domain, DocumentProcessor processor) {
		if(processor != null && domain != null) {
			if(!DocumentBuilder._DOCUMENT_PROCESSORS_.containsKey(domain)) {
				DocumentBuilder._DOCUMENT_PROCESSORS_.put(domain, new ArrayList<DocumentProcessor>());
			}
			DocumentBuilder._DOCUMENT_PROCESSORS_.get(domain).add(processor);
		}
	}
	
	/**
	 * Method to unregister a DocumentProcessor for domain
	 * @param domain the domain
	 * @param processor the processor
	 * @since 0.2
	 */
	public void removeDocumentProcessor(String domain, DocumentProcessor processor) {
		if(DocumentBuilder._DOCUMENT_PROCESSORS_.containsKey(domain)) {
			DocumentBuilder._DOCUMENT_PROCESSORS_.get(domain).remove(processor);
			if(DocumentBuilder._DOCUMENT_PROCESSORS_.size()<1) {
				DocumentBuilder._DOCUMENT_PROCESSORS_.remove(domain);
			}
		}
	}
	
	/**
	 * Method to register a DocumentProcessor for domain top
	 * @param domain the domain
	 * @param processor the DocumentProcessor
	 * @since 0.2
	 */
	public void addTopDocumentProcessor(String domain, DocumentProcessor processor) {
		if(processor != null && domain != null) {
			if(!DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.containsKey(domain)) {
				DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.put(domain, new ArrayList<DocumentProcessor>());
			}
			DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.get(domain).add(processor);
		}
	}
	
	/**
	 * Method to unregister a DocumentProcessor for domain top
	 * @param domain the domain
	 * @param processor the processor
	 * @since 0.2
	 */
	public void removeTopDocumentProcessor(String domain, DocumentProcessor processor) {
		if(DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.containsKey(domain)) {
			DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.get(domain).remove(processor);
			if(DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.size()<1) {
				DocumentBuilder._DOCUMENT_TOP_PROCESSORS_.remove(domain);
			}
		}
	}
	
	/**
	 * Method to register a DocumentProcessor for domain bottom
	 * @param domain the domain
	 * @param processor the DocumentProcessor
	 * @since 0.2
	 */
	public void addBottomDocumentProcessor(String domain, DocumentProcessor processor) {
		if(processor != null && domain != null) {
			if(!DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.containsKey(domain)) {
				DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.put(domain, new ArrayList<DocumentProcessor>());
			}
			DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.get(domain).add(processor);
		}
	}
	
	/**
	 * Method to unregister a DocumentProcessor for domain bottom
	 * @param domain the domain
	 * @param processor the processor
	 * @since 0.2
	 */
	public void removeBottomDocumentProcessor(String domain, DocumentProcessor processor) {
		if(DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.containsKey(domain)) {
			DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.get(domain).remove(processor);
			if(DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.size()<1) {
				DocumentBuilder._DOCUMENT_BOTTOM_PROCESSORS_.remove(domain);
			}
		}
	}
	
	/**
	 * Method to register a PreRequestHandler
	 * @param handler the handler to register
	 * @since 1.0
	 */
	public void addPreRequestHandler(PreRequestHandler handler) {
		if(handler != null) {
			preRequestHandlers.add(handler);
		}
	}
	
	/**
	 * Method to unregister a PreRequestHandler
	 * @param handler the handler to unregister
	 * @since 1.0
	 */
	public void removePreRequestHandler(PreRequestHandler handler) {
		preRequestHandlers.remove(handler);
	}
	
	/**
	 * Returns all currently registered PreRequestHandlers
	 * @return all currently registered PreRequestHandlers
	 * @since 1.0
	 */
	public PreRequestHandler[] getPreRequestHandlers() {
		return preRequestHandlers.toArray(new PreRequestHandler[0]);
	}
	
	/**
	 * Returns whether a before handler is present
	 * @return whether a before handler is present
	 * @since 0.1
	 * @deprecated As of 1.0, the before request handler has been succeeded by pre-request handlers. This method now returns false.
	 */
	@Deprecated
	public boolean isBeforeHandlerPresent() {
		return false;
	}
	
	/**
	 * Returns the handler that gets executed before all requests
	 * @return the handler that gets executed before all requests
	 * @since 0.1
	 * @deprecated As of 1.0, the before request handler has been succeeded by pre-request handlers. This method now returns a request handler that returns an empty String.
	 */
	@Deprecated
	public RequestHandler getBeforeRequestHandler() {
		return (req, res) -> {
			return "";
		};
	}
	
	/**
	 * Sets the handler to be executed before all requests
	 * Setting to null will clear the handler
	 * @param handler the handler to execute before all requests
	 * @since 0.1
	 * @deprecated As of 1.0 the before request handler has been succeeded by pre-request handlers. This method now does nothing.
	 */
	@Deprecated
	public void setBeforeRequestHandler(RequestHandler handler) {}
	
	/**
	 * Clears the before request handler
	 * @since 0.1
	 * @deprecated As of 1.0 the before request handler has been succeeded by pre-request handlers. This method now does nothing.
	 */
	@Deprecated
	public void clearBeforeRequestHandler() {}
	
	/**
	 * Returns the default domain to be used by modules
	 * @return the default domain to be used by modules
	 * @since 0.1
	 */
	public String getDefaultDomain() {
		return defaultDomain;
	}
	
	/**
	 * Returns a list of handlers assigned to the specified domain and method
	 * @param domain - the domain
	 * @param method - the method
	 * @return a list of handlers assigned to the specified domain and method
	 * @since 0.1
	 */
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
	 * @since 0.1
	 */
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
	 * @since 0.1
	 */
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
	 * @since 0.1
	 */
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
	 * Log warning message to the console
	 * @param msg - the message to log
	 * @since 0.2
	 */
	public void logWarning(String msg) {
		String prefix = new Date().toString()+" WARNING ";
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
	 * @since 0.1
	 */
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
	 * @since 0.1
	 */
	public void shutdown() {
		logInfo("Shutting down Twister...");
		ModuleManager.shutdownModules();
		System.exit(0);
	}
}
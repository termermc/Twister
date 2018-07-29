package net.termer.twister.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import spark.Request;
import spark.Response;
import net.termer.twister.Settings;
import net.termer.twister.Twister;
import net.termer.twister.caching.TwisterCache;
import net.termer.twister.utils.Domain;
import net.termer.twister.utils.StringFilter;

/**
 * Utility class to render webpages from file paths
 * @author termer
 * @since 0.1
 */
public class DocumentBuilder {
	
	/**
	 * DocumentProcessors for domains
	 * Key: Domain
	 * Value: ArrayList of DocumentProcessors
	 * @since 0.2
	 */
	public static HashMap<String,ArrayList<DocumentProcessor>> _DOCUMENT_PROCESSORS_ = new HashMap<String,ArrayList<DocumentProcessor>>();
	
	/**
	 * DocumentProcessors for domain tops
	 * Key: Domain
	 * Value: ArrayList of DocumentProcessors
	 * @since 0.2
	 */
	public static HashMap<String,ArrayList<DocumentProcessor>> _DOCUMENT_TOP_PROCESSORS_ = new HashMap<String,ArrayList<DocumentProcessor>>();
	
	/**
	 * DocumentProcessors for domain bottoms
	 * Key: Domain
	 * Value: ArrayList of DocumentProcessors
	 * @since 0.2
	 */
	public static HashMap<String,ArrayList<DocumentProcessor>> _DOCUMENT_BOTTOM_PROCESSORS_ = new HashMap<String,ArrayList<DocumentProcessor>>();
	
	
	/**
	 * Renders a webpage using the provided domain, path, request, and response.
	 * For instance, to render the webpage "hello.html" located in the domain
	 * "example.com" you would type loadDocument("example.com", "hello.html", req, res);
	 * @param domain the domain the webpage is located at
	 * @param path the path inside of the domain that the page is located at
	 * @param req the Request
	 * @param res the Response
	 * @return the rendered document
	 * @throws IOException if reading the webpage fails
	 * @since 0.1
	 */
	public static String loadDocument(String domain, String path, Request req, Response res) throws IOException {
		String r = "";
		try {
			if(Twister.linkedDomains.containsKey(domain)) {
				domain = Twister.linkedDomains.get(domain);
			}
			Domain dom = new Domain(domain);
			if(dom.exists()) {
				if(path.startsWith("/")) {
					path = path.substring(1);
				}
				if(path.length()<1) {
					path="index.html";
				}
				if(path.endsWith("/")) {
					path+="index.html";
				}
				
				File document = new File("domains/"+domain+"/"+path);
				if(document.isDirectory()) {
					if(!path.endsWith("/")) {
						path+='/';
					}
					path+="index.html";
				}
				if(document.exists()) {
					String[] parts = document.getName().split("\\.");
					String ending = parts[parts.length-1];
					String filetype = "application/octet-stream";
					if(document.getName().contains(".")) {
						
						if(ending.equalsIgnoreCase("html")) {
							filetype = "text/html";
						} else if(ending.equalsIgnoreCase("txt")) {
							filetype = "text/plain";
						} else if(ending.equalsIgnoreCase("js")) {
							filetype = "application/javascript";
						} else if(ending.equalsIgnoreCase("aac")) {
							filetype = "audio/aac";
						} else if(ending.equalsIgnoreCase("avi")) {
							filetype = "video/x-msvideo";
						} else if(ending.equalsIgnoreCase("bmp")) {
							filetype = "image/bmp";
						} else if(ending.equalsIgnoreCase("bz")) {
							filetype = "application/x-bzip";
						} else if(ending.equalsIgnoreCase("bz2")) {
							filetype = "application/x-bzip2";
						} else if(ending.equalsIgnoreCase("")) {
							filetype = "";
						} else if(ending.equalsIgnoreCase("css")) {
							filetype = "text/css";
						} else if(ending.equalsIgnoreCase("doc")) {
							filetype = "msword";
						} else if(ending.equalsIgnoreCase("docx")) {
							filetype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
						} else if(ending.equalsIgnoreCase("eot")) {
							filetype = "application/vnd.ms-fontobject";
						} else if(ending.equalsIgnoreCase("epub")) {
							filetype = "application/epub+zip";
						} else if(ending.equalsIgnoreCase("htm")) {
							filetype = "text/html";
						} else if(ending.equalsIgnoreCase("gif")) {
							filetype = "image/gif";
						} else if(ending.equalsIgnoreCase("ico")) {
							filetype = "image/x-icon";
						} else if(ending.equalsIgnoreCase("jar")) {
							filetype = "application/java-archive";
						} else if(ending.equalsIgnoreCase("jpg")) {
							filetype = "image/jpeg";
						} else if(ending.equalsIgnoreCase("jpeg")) {
							filetype = "image/jpeg";
						} else if(ending.equalsIgnoreCase("json")) {
							filetype = "application/json";
						} else if(ending.equalsIgnoreCase("mid")) {
							filetype = "audio/midi";
						} else if(ending.equalsIgnoreCase("midi")) {
							filetype = "audio/midi";
						} else if(ending.equalsIgnoreCase("mpeg")) {
							filetype = "audio/mpeg";
						} else if(ending.equalsIgnoreCase("odp")) {
							filetype = "application/vnd.oasis.opendocument.presentation";
						} else if(ending.equalsIgnoreCase("ods")) {
							filetype = "application/vnd.oasis.opendocument.spreadsheet";
						} else if(ending.equalsIgnoreCase("odt")) {
							filetype = "application/vnd.oasis.opendocument.text";
						} else if(ending.equalsIgnoreCase("oga")) {
							filetype = "audio/ogg";
						} else if(ending.equalsIgnoreCase("ogv")) {
							filetype = "video/ogg";
						} else if(ending.equalsIgnoreCase("ogx")) {
							filetype = "application/ogg";
						} else if(ending.equalsIgnoreCase("ogg")) {
							filetype = "audio/ogg";
						} else if(ending.equalsIgnoreCase("png")) {
							filetype = "image/png";
						} else if(ending.equalsIgnoreCase("pdf")) {
							filetype = "application/pdf";
						} else if(ending.equalsIgnoreCase("ppt")) {
							filetype = "application/vnd.ms-powerpoint";
						} else if(ending.equalsIgnoreCase("pptx")) {
							filetype = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
						} else if(ending.equalsIgnoreCase("rar")) {
							filetype = "application/x-rar-compressed";
						} else if(ending.equalsIgnoreCase("rtf")) {
							filetype = "application/rtf";
						} else if(ending.equalsIgnoreCase("sh")) {
							filetype = "application/x-sh";
						} else if(ending.equalsIgnoreCase("svg")) {
							filetype = "image/svg+xml";
						} else if(ending.equalsIgnoreCase("swf")) {
							filetype = "application/x-shockwave-flash";
						} else if(ending.equalsIgnoreCase("tar")) {
							filetype = "application/x-tar";
						} else if(ending.equalsIgnoreCase("wav")) {
							filetype = "audio/wave";
						} else if(ending.equalsIgnoreCase("weba")) {
							filetype = "audio/webm";
						} else if(ending.equalsIgnoreCase("webm")) {
							filetype = "video/webm";
						} else if(ending.equalsIgnoreCase("webp")) {
							filetype = "image/webp";
						} else if(ending.equalsIgnoreCase("woff")) {
							filetype = "font/woff";
						} else if(ending.equalsIgnoreCase("woff2")) {
							filetype = "font/woff2";
						} else if(ending.equalsIgnoreCase("xhtml")) {
							filetype = "application/xhtml+xml";
						} else if(ending.equalsIgnoreCase("xls")) {
							filetype = "application/vnd.ms-excel";
						} else if(ending.equalsIgnoreCase("xlsx")) {
							filetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
						} else if(ending.equalsIgnoreCase("xml")) {
							filetype = "application/xml";
						} else if(ending.equalsIgnoreCase("zip")) {
							filetype = "application/zip";
						} else if(ending.equalsIgnoreCase("7z")) {
							filetype = "application/x-7z-compressed";
						}
					}
					res.type(filetype);
					
					if(StringFilter.same(filetype, "text/html")) {
						// Load as document
						if(dom.hasTop()) {
							r+=dom.getProcessedTop(req, res);
						}
						
						r+=processDocument(path, readFile(document.getPath()), domain, req, res);
						
						if(dom.hasBottom()) {
							r+=dom.getProcessedBottom(req, res);
						}
					} else {
						// Load as file
						res.raw().getOutputStream().write(Files.readAllBytes(Paths.get(document.getPath())));
					}
				} else {
					if(dom.has404()) {
						if(dom.hasTop()) {
							r+=dom.getProcessedTop(req, res);
						}
						
						r+=dom.getProcessed404(req, res);
						
						if(dom.hasBottom()) {
							r+=dom.getProcessedBottom(req, res);
						}
					} else {
						if(Boolean.parseBoolean(Settings.get("caching"))) {
							r = TwisterCache._404_;
						} else {
							r = readFile("404.html");
						}
					}
					res.status(404);
				}
			} else {
				if(Boolean.parseBoolean(Settings.get("caching"))) {
					r = TwisterCache._404_;
				} else {
					r = readFile("404.html");
				}
				res.status(404);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return r;
	}
	
	/**
	 * Gets the content of a file as a String
	 * @param path the absolute path of the file
	 * @return the content of the file as a String
	 * @throws IOException if reading the file fails, or file does not exist
	 * @since 0.1
	 */
	public static String readFile(String path) throws IOException {
		StringBuilder sb = new StringBuilder();
		FileInputStream fin = new FileInputStream(new File(path));
		while(fin.available()>0) {
			sb.append((char)fin.read());
		}
		fin.close();
		return sb.toString();
	}
	
	
	/**
	 * Process a document using registered DocumentProcessors
	 * @param path the path of the document (not file path)
	 * @param text the text of the document
	 * @param domain the domain
	 * @param req the request
	 * @param res the response
	 * @return the processed document
	 * @since 0.2
	 */
	public static String processDocument(String path, String text, String domain, Request req, Response res) {
		String r = text;
		
		if(_DOCUMENT_PROCESSORS_.containsKey(domain)) {
			HTMLDocumentResponse docResp = new HTMLDocumentResponse(path, domain, domain+path, text);
			for(DocumentProcessor dp : _DOCUMENT_PROCESSORS_.get(domain)) {
				dp.process(docResp, req, res);
			}
			r = docResp.getText();
		}
		
		return r;
	}
	
	/**
	 * Process a domain top using registered DocumentProcessors
	 * @param path the path of the document (not file path)
	 * @param text the text of the document
	 * @param domain the domain
	 * @param req the request
	 * @param res the response
	 * @return the processed document
	 * @since 0.2
	 */
	public static String processTopDocument(String path, String text, String domain, Request req, Response res) {
		String r = text;
		
		if(_DOCUMENT_TOP_PROCESSORS_.containsKey(domain)) {
			HTMLDocumentResponse docResp = new HTMLDocumentResponse(path, domain, domain+path, text);
			for(DocumentProcessor dp : _DOCUMENT_TOP_PROCESSORS_.get(domain)) {
				dp.process(docResp, req, res);
			}
			r = docResp.getText();
		}
		
		return r;
	}
	
	/**
	 * Process a domain bottom using registered DocumentProcessors
	 * @param path the path of the document (not file path)
	 * @param text the text of the document
	 * @param domain the domain
	 * @param req the request
	 * @param res the response
	 * @return the processed document
	 * @since 0.2
	 */
	public static String processBottomDocument(String path, String text, String domain, Request req, Response res) {
		String r = text;
		
		if(_DOCUMENT_BOTTOM_PROCESSORS_.containsKey(domain)) {
			HTMLDocumentResponse docResp = new HTMLDocumentResponse(path, domain, domain+path, text);
			for(DocumentProcessor dp : _DOCUMENT_BOTTOM_PROCESSORS_.get(domain)) {
				dp.process(docResp, req, res);
			}
			r = docResp.getText();
		}
		
		return r;
	}
}

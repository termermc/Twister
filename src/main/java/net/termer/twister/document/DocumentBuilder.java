package net.termer.twister.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import spark.Request;
import spark.Response;
import net.termer.twister.Settings;
import net.termer.twister.Twister;
import net.termer.twister.caching.TwisterCache;
import net.termer.twister.utils.Domain;

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
			if(new File("domains/"+domain+"/"+path).isDirectory()) {
				if(!path.endsWith("/")) {
					path+='/';
				}
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
				if(dom.hasTop()) {
					r+=dom.getProcessedTop(req, res);
				}
				
				r+=processDocument(path, readFile(document.getPath()), domain, req, res);
				
				if(dom.hasBottom()) {
					r+=dom.getProcessedBottom(req, res);
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
		String r = "";
		FileInputStream fin = new FileInputStream(new File(path));
		while(fin.available()>0) {
			r+=(char)fin.read();
		}
		fin.close();
		return r;
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

package net.termer.twister.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import spark.Request;
import spark.Response;
import net.termer.twister.Twister;

/**
 * Utility class to render webpages from file paths
 * @author termer
 *
 */
public class DocumentBuilder {
	
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
	 */
	public static String loadDocument(String domain, String path, Request req, Response res) throws IOException {
		String r = "";
		try {
		if(Twister.linkedDomains.containsKey(domain)) {
			domain = Twister.linkedDomains.get(domain);
		}
		if(new File("domains/"+domain+"/").exists()) {
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
				File top = new File("domains/"+domain+"/top.html");
				File bottom = new File("domains/"+domain+"/bottom.html");
				if(top.exists()) {
					r=readFile(top.getPath());
				}
				r+=readFile(document.getPath());
				if(bottom.exists()) {
					r+=readFile(bottom.getPath());
				}
			} else {
				r = readFile("404.html");
				res.status(404);
			}
		} else {
			r = readFile("404.html");
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
}

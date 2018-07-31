package net.termer.twister.utils;

import java.io.File;
import java.io.IOException;

import bsh.EvalError;
import bsh.Interpreter;
import net.termer.twister.Twister;
import net.termer.twister.document.DocumentBuilder;
import net.termer.twister.document.DocumentProcessor;
import net.termer.twister.document.HTMLDocumentResponse;
import spark.Request;
import spark.Response;

public class ScriptProcessor implements DocumentProcessor {
	public void process(HTMLDocumentResponse doc, Request req, Response res) {
		// Check if scripting is enabled
		if(doc.getText().startsWith("<!--TES-->")) {
			String tmp = doc.getText().substring(10);
			if(tmp.contains("<?java") && tmp.contains("?>")) {
				int index = 0;
				boolean proceed = true;
				Interpreter inter = new Interpreter();
				try {
					inter.set("request", req);
					inter.set("response", res);
					inter.set("twister", Twister.current());
					inter.set("domain", doc.getDomain());
				} catch (EvalError ex) {
					ex.printStackTrace();
				}
				while(proceed) {
					int opening = tmp.indexOf("<?java", index)+6;
					int closing = tmp.indexOf("?>", index);
					if(opening > 6 && closing > -1) {
						String script = tmp.substring(opening, closing);
						
						Out result = new Out(doc.getDomain());
						try {
							inter.set("out", result);
							inter.eval(script.trim());
						} catch (Exception e) {
							e.printStackTrace();
						}
						tmp = tmp.replace("<?java"+script+"?>", result.toString());
						index++;
					} else {
						proceed = false;
					}
				}
				doc.setText(tmp);
			}
		}
	}
	
	public static class Out {
		private StringBuilder result = new StringBuilder();
		private String dom = null;
		
		public Out(String domain) {
			dom = domain;
		}
		
		public void append(String content) {
			result.append(content);
		}
		
		public void include(String path) {
			try {
				File inc = new File("domains/"+dom+"/"+path);
				if(inc.isFile()) {
					result.append(DocumentBuilder.readFile(inc.getPath()).replace("<!--TES-->", ""));
				} else if(inc.isDirectory()) {
					for(File file : inc.listFiles()) {
						result.append(DocumentBuilder.readFile(file.getPath()).replace("<!--TES-->", ""));
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

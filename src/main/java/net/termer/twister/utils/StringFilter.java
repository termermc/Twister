package net.termer.twister.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

/**
 * String utility class
 * @author termer
 * @since 0.1
 */
public class StringFilter {
	/**
	 * A list of acceptable chars
	 * @since 0.1
	 */
	public static char[] acceptableChars = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','3','4','5','6','7','8','9','_'};
	
	/**
	 * Remove all unacceptable chars from a String
	 * @param str - the String to filter
	 * @return the filtered String
	 * @since 0.1
	 */
	public static String filter(String str) {
		String result = "";
		for(char ch : str.toLowerCase().toCharArray()) {
			for(char acceptableChar : acceptableChars) {
				boolean ok = true;
				if(ch!=acceptableChar) {
					ok = false;
					break;
				}
				if(ok) result+=ch;
			}
		}
		return result;
	}
	
	/**
	 * Check if String only contains acceptable characters
	 * @param str - the String to check
	 * @return whether the String only contains acceptable characters
	 * @since 0.1
	 */
	public static boolean acceptableString(String str) {
		boolean ok = false;
		for(char ch : str.toLowerCase().toCharArray()) {
			for(char acceptableChar : acceptableChars) {
				if(ch==acceptableChar) {
					ok = true;
					break;
				}
			}
		}
		return ok;
	}
	
	/**
	 * Generate a String of the desired length using only acceptable characters
	 * @param length - the desired String length
	 * @return the generated String
	 * @since 0.1
	 */
	public static String generateString(int length) {
		String str = "";
		for(int i = 0; i < length; i++) {
			Random rand = new Random();
			str+=acceptableChars[rand.nextInt(acceptableChars.length-1)];
		}
		return str;
	}
	
	/**
	 * Check if all the chars in two Strings are the same
	 * @param str1 - the first String
	 * @param str2 - the second String
	 * @return whether the Strings are equivelent
	 * @since 0.1
	 */
	public static boolean same(String str1, String str2) {
		boolean same = true;
		
		if(str1.length()==str2.length()) {
			for(int i = 0; i < str1.length(); i++) {
				if(str1.charAt(i)!=str2.charAt(i)) {
					same = false;
					break;
				}
			}
		} else {
			same = false;
		}
		
		return same;
	}
	
	/**
	 * Encode a String to be acceptable in a URI (including URLs)
	 * @param s - the String to encode
	 * @return the encoded String
	 * @since 0.1
	 */
	public static String encodeURIComponent(String s) {
	    String result;

	    try {
	        result = URLEncoder.encode(s, "UTF-8")
	                .replaceAll("\\+", "%20")
	                .replaceAll("\\%21", "!")
	                .replaceAll("\\%27", "'")
	                .replaceAll("\\%28", "(")
	                .replaceAll("\\%29", ")")
	                .replaceAll("\\%7E", "~");
	    } catch (UnsupportedEncodingException e) {
	        result = s;
	    }

	    return result;
	}
	
	/**
	 * Replaces all regular expression characters with their escaped versions
	 * @param regexString the String to escape
	 * @return the String with the escaped regex characters
	 * @since 0.3
	 */
	public static String escapeRegexCharacters(String regexString) {
		return regexString
				.replace("<", "\\<")
				.replace("(", "\\(")
				.replace("[", "\\[")
				.replace("{", "\\{")
				.replace("\\", "\\\\")
				.replace("^", "\\^")
				.replace("-", "\\-")
				.replace("=", "\\=")
				.replace("$", "\\$")
				.replace("!", "\\!")
				.replace("|", "\\|")
				.replace("]", "\\]")
				.replace("}", "\\}")
				.replace(")", "\\)")
				.replace("?", "\\?")
				.replace("*", "\\*")
				.replace("+", "\\+")
				.replace(".", "\\.")
				.replace(">", "\\>");
	}
	
	/**
	 * Removes all regular expression characters from the provided String
	 * @param regexString the String to process
	 * @return the String minus all regex characters
	 * @since 0.3
	 */
	public static String removeRegexCharacters(String regexString) {
		return regexString
				.replace("<", "")
				.replace("(", "")
				.replace("[", "")
				.replace("{", "")
				.replace("\\", "")
				.replace("^", "")
				.replace("-", "")
				.replace("=", "")
				.replace("$", "")
				.replace("!", "")
				.replace("|", "")
				.replace("]", "")
				.replace("}", "")
				.replace(")", "")
				.replace("?", "")
				.replace("*", "")
				.replace("+", "")
				.replace(".", "")
				.replace(">", "");
	}
	
	/**
	 * Returns whether the specified path matches the specified route, using * as wildcards
	 * @param route the route
	 * @param path the path to check
	 * @return whether the path matches the route
	 * @since 0.3
	 */
	public static boolean matchesRoute(String route, String path) {
		String escRoute = removeRegexCharacters(route.replace("*", "\n")).replace("\n", "\\w*");
		String escPath = removeRegexCharacters(path);
		return escPath.matches(escRoute);
	}
	
	/**
	 * Takes in the provided path and gets all of its filled in wildcard spaces from the route.
	 * Example:
	 * If the route is "/hello/[asterisk]/"
	 * and your path is "/hello/world/"
	 * then the method will return an array with "world".
	 * @param route the route to check the path against
	 * @param path the path to be checked
	 * @return all the filled in asterisk spaces
	 * @since 0.3
	 */
	public static String[] processRoute(String route, String path) {
		ArrayList<String> wildcards = new ArrayList<String>();
		
		String[] parts = route.replace("*", "\n").split("\n");
		for(int i = 0; i < parts.length; i++) {
			if(!same(parts[i], path.charAt(path.length()-1)+"")) {
				String tmp = path.substring(path.indexOf(parts[i])+parts[i].length());
				if(parts.length >= i+2) {
					if(same(parts[i+1], path.charAt(path.length()-1)+"")) {
						tmp = tmp.substring(0, tmp.length()-1);
					} else {
						tmp = tmp.substring(0, path.indexOf(parts[i+1])-parts[i].length());
					}
				}
				wildcards.add(tmp);
			}
;		}
		
		return wildcards.toArray(new String[0]);
	}
}
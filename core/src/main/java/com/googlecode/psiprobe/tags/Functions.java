package com.googlecode.psiprobe.tags;

/**
 *
 * @author <a href="mailto:mlewis@itos.uga.edu">Mark Lewis</a>
 */
public class Functions {

	public static String safeCookieName(String cookieName) {
		return cookieName.replaceAll("\"", "");
	}
    
}

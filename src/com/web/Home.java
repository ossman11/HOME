/**
 * 
 */
package com.web;

import network.HTTP.HTTPHandler;

import com.Page;

/**
 * @author laptop
 *
 */
public class Home extends Page {
	// Public Values
	public static final String url = "/";
	public static final String title = "Home";
	
	// Private Values
	
	
	// Public Functions
	public static String doGet(HTTPHandler Req) {
		return "<html><head></head><body><h1>Welcome Home</h1></body></html>";
	}
	
	public static String doPost(HTTPHandler Req) {
		return null;
	}
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
}

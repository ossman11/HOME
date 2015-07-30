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
public class Devices extends Page {
	// Public Values
	public static final String url = "/Devices";
	public static final String title = "Devices";
	
	// Private Values
	
	
	// Public Functions
	public static String doGet(HTTPHandler Req) {
		
		return	"<h1>Devices</h1>";
	}
	
	public static String doPost(HTTPHandler Req) {
		return "<html><head></head><body><h1>Welcome Home</h1>" + Req.Content + "</body></html>";
	}
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
}

/**
 * 
 */
package com;

import entrance.HOME;
import network.HTTP.HTTPHandler;

/**
 * @author Bob den Os
 *
 */
public class Page {
	// Public Values
	public String url = "";
	public String title = "";

	// Private Values
	public HOME H;
	
	
	// Public Functions
	public void init(HOME h){
		H = h;
	}

	public void close(){}

	protected String doGet(HTTPHandler Req) {
		return null;
	}

	protected String doPost(HTTPHandler Req) {
		return null;
	}
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
	public Page(){}
}

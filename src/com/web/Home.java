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
		
		return "<html><head></head><body>" + 
				"<h1>Welcome Home</h1>" + 
				Req.Home.database.Auth.toHTML() + 
				"<form action='/' method='post'>" +
				"<input name='User' type='text' />" +
				"<input name='Pass' type='password' />" +
				"<input name='Auth' type='text' />" +
				"<input value='Submit' type='submit' />" +
				"</form>" +
				"</body></html>";
	}
	
	public static String doPost(HTTPHandler Req) {
		String[] vals = Req.Content.split("&");
		
		String Pass = "";
		String User = "";
		String Auth = "";
		String Salt = System.nanoTime()+"";
		
		for(int i = 0; i < vals.length; i++) {
			String[] cur = vals[i].split("=");
			if(cur.length == 1){cur = new String[]{cur[0],""};}

			switch(cur[0]) {
				case "User": User = cur[1];
					break;
				case "Pass": Pass = cur[1];
					break;
				case "Auth": Auth = cur[1];
					break;
				default: continue;
			}
		}
		
		Pass = String.valueOf( (Pass + Salt).hashCode() );
		
		Req.Home.database.Auth.Add(new String[]{User,Pass,Salt,Auth});
		
		Req.Home.database.Auth.Save();
		
		return "<html><head></head><body><h1>Welcome Home</h1>" + Req.Content + "</body></html>";
	}
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
}

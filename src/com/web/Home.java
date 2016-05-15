/**
 * 
 */
package com.web;

import network.HTTP.HTTPHandler;

import java.util.Date;

import com.Page;

import closset.DataBase.SubTable;
import closset.DataBase.Table;

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
		String ret = "<h1>Welcome Home</h1>" + 
				"<form action='/' method='post'>" +
				"<input name='Name' type='text' />" +
				"<input value='Submit' type='submit' />" +
				"</form>";
		/*
		long start = new Date().getTime();
		String dbtable = Req.Home.database.tables.get(0).toHTML();
		long e = new Date().getTime();
		*/
		ret += "<div><h1>" + Req.Home.database.tables.get(0).Size() + " lines</h1></div>";
		// ret += dbtable;
		
		return	ret;
				
	}
	
	public static String doPost(HTTPHandler Req) {
		String[] vals = Req.Content.split("&");
		
		String Name = "";
		
		for(int i = 0; i < vals.length; i++) {
			String[] cur = vals[i].split("=");
			if(cur.length == 1){cur = new String[]{cur[0],""};}

			switch(cur[0]) {
				case "Name": Name = cur[1];
					break;
				default: continue;
			}
		}
		long s = new Date().getTime();
		SubTable result = Req.Home.database.tables.get(0).Find("First", Name);
		long e = new Date().getTime();
		
		return "<html><head></head><body><h1>Welcome Home</h1>" + 
			Req.Content + "<h1>" + (e-s) + "</h1>" + result.toHTML() + "</body></html>";
	}
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
}

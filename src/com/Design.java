/**
 * 
 */
package com;

import java.util.ArrayList;

/**
 * @author laptop
 *
 */
public class Design {
	// Public Values
	
	// Private Values
	private static final String Head = "<html>" + System.lineSeparator() +
			"<head>" + System.lineSeparator() +
			// css
			"<link rel='stylesheet' type='text/css' href='/CSS/main.css' />" + System.lineSeparator() +
			// js
			"<script src='/JS/jquery.js'></script>" + System.lineSeparator() +
			"</head>" + System.lineSeparator() +
			// page content
			"<body>" + System.lineSeparator();
	
	private static final String Foot = "</body>" + System.lineSeparator() + 
			"</html>" + System.lineSeparator();
	
	// Public Functions
	public static String Paginate(String Content, int Cur, ArrayList<String> URI, ArrayList<String> TITLES) {
		StringBuffer SB = new StringBuffer();
		
		SB.append(Head);
		SB.append(Menu(Cur, URI, TITLES));
		SB.append(Content);
		SB.append(Foot);
		
		return SB.toString();
	}
	
	// Private Functions
	private static String Menu(int Cur, ArrayList<String> URI, ArrayList<String> TITLES ) {
		StringBuffer SB = new StringBuffer();
		// Menu Holders
		SB.append("<div class='MenuHolder'><div class='MenuCenter'>");
		// Menu Content
		SB.append("<div class='Logo'>");
		// Menu Items
		for(int i = 0; i < URI.size(); i++) {
			SB.append("<a class='MenuItem");
			if( i == Cur) {
				SB.append(" Cur");
			}
			SB.append("' href='" + URI.get(i) + "'><div>" + TITLES.get(i) + "</div></a>");
		}
		// Menu Ending
		SB.append("</div></div>");
		
		return SB.toString();
	}
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
}

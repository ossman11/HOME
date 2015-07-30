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
			"<title>HOME</title>" +
			// css
			"<link rel='stylesheet' type='text/css' href='/CSS/main.css' />" + System.lineSeparator() +
			// js
			"<script src='/JS/jquery.js'></script>" + System.lineSeparator() +
			"<script src='/JS/main.js'></script>" + System.lineSeparator() +
			"</head>" + System.lineSeparator() +
			// page content
			"<body>" + System.lineSeparator();
	
	private static final String Foot = "</div></div><div class='MastFoot'></div></body>" + System.lineSeparator() + 
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
		// Top Layer
		SB.append("<div class='TopLayer'>");
			// Head
			SB.append("<div class='MastHead'>");
				// Loading Bar
				SB.append("<div id='LBar' class='LoadingBar'><div></div><div></div><div></div><div></div></div>");
				// Menu Toggle Button
				SB.append("<div id='TMenu' class='ToggleMenu'><div></div><div></div><div></div><div class='Title'>HOME</div></div>");
				// Notifications Toggle
				SB.append("<div id='TNoti' class='ToggleNoti'><div class='Point'></div><div class='Num'>0</div></div>");
			// Head End
			SB.append("</div>");
			// Menu Master
			SB.append("<div id='MMenu' class='MastMenu'>");
				// Menu Items
				for(int i = 0; i < URI.size(); i++) {
					SB.append("<a class='MenuItem");
					if( i == Cur) {
						SB.append(" Cur");
					}
					SB.append("' href='" + URI.get(i) + "'><div class='title'>" + TITLES.get(i) + "</div><div class='back'>" + TITLES.get(i) + "</div></a>");
				}
			// Menu End
			SB.append("</div>");
			// Notifications Master
			SB.append("<div id='MNoti' class='MastNoti'>");
			
			SB.append("</div>");
		SB.append("</div>");	
		
		SB.append("<div id='MCont' class='MastCont'><div class='Center'>");
		
		return SB.toString();
	}
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
}

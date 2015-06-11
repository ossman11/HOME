/**
 * 
 */
package com;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

import entrance.HOME;
import network.HTTP.HTTPHandler;


/**
 * @author Bob den Os
 *
 */
public class Portal { 
	
	// Public Values
	
	// Private Values
	private HOME H = null;
	
	private ArrayList<String> URI = null;
	private ArrayList<String> TITLES = null;
	private ArrayList<Class<?>> PClass = null;
	
	// Public Functions
	public String GetPage(HTTPHandler Req) {
		// Lookup Page
		int PNR = URI.indexOf(Req.Request);
		// Page not found
		if(PNR < 0){return null;}
		// Retrieve page class
		Class<?> PC = PClass.get(PNR);
		String ret = null;
		try {
			// Execute Page Method
			switch(Req.Method.Nr){
				case 1: 
					ret = PC.getMethod("doGet", new Class<?>[] {Req.getClass()}).invoke(this,Req).toString();
					break;
				case 3:
					ret = PC.getMethod("doPost", new Class<?>[] {Req.getClass()}).invoke(this,Req).toString();
					break;
				default:return null;
			}
			if(ret == null) { return null; }
			return Design.Paginate(ret, PNR, URI, TITLES);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Private Functions
	private void LoadAllPages() {
		// setup Classloader
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		// Prepare Index Lists
		URI = new ArrayList<String>();
		TITLES = new ArrayList<String>();
		PClass = new ArrayList<Class<?>>();
		// look up all classes
		String dir = "com/web";
		Iterator<Path> Pages = H.folder.GetDirFiles(dir);
		while(Pages.hasNext()) {
			Path tmpPath = Pages.next();
			try{
				String tmpString = tmpPath.toString();
				if(!tmpString.endsWith(".class")) { continue; }
				// Cut out class name
				String tmpName = "com.web." + 
						tmpString.substring( ( H.folder.LocalDir + dir ).length()+1, tmpString.length()-6 );
				
				Class<?> tmpClass = classLoader.loadClass( tmpName );
				String tmpUrl = "";
				String tmpTitle = "";
				tmpUrl += tmpClass.getDeclaredField("url").get(null);
				tmpTitle += tmpClass.getDeclaredField("title").get(null);
				// Save All Pages that have an url
				if(tmpUrl != "") {
					URI.add(tmpUrl);
					TITLES.add(tmpTitle);
					PClass.add(tmpClass);
				}
			} catch (ClassNotFoundException | IllegalArgumentException 
					| IllegalAccessException | NoSuchFieldException 
					| SecurityException e) {
				// When failed to read any part of the Page
				System.out.println(e.getMessage());
			} 				
		}
	}
	
	// Public Classes
	
	// Private Classes
	
	// Portal Constructors
	public Portal(HOME h){
		H = h;
		LoadAllPages();
	}
}

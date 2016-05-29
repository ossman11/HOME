/**
 * 
 */
package com;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;

import closset.Folders;
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
	private boolean loading = true;
	private int busy = 0;
	
	private ArrayList<String> URI = null;
	private ArrayList<String> TITLES = null;
	private ArrayList<Page> PClass = null;

	private UpdateThread pageUpdateThread = null;
	
	// Public Functions
	public String GetPage(HTTPHandler Req) {
		while(loading){}
		busy++;
		String ret = ProcessRequest(Req);
		busy--;
		return ret;
	}

	public void Reload(){
		if(loading){return;}
		while(busy > 0){}
		loading = true;
		// close all the old instances of the pages
		UnloadAllPages();
		// Start reloading all the pages
		LoadAllPages();
		loading = false;
	}

	public void stop(){
		UnloadAllPages();
	}

	// Private Functions
	private String ProcessRequest(HTTPHandler Req){
		boolean plain = Req.Request.startsWith("/PLAIN/");
		if(plain){
			Req.Request = Req.Request.substring(6);
		}
		// Lookup Page
		int PageNr = URI.indexOf(Req.Request);
		// Page not found
		if(PageNr < 0){return null;}
		// Retrieve page class
		Page PC = PClass.get(PageNr);
		String ret = null;
		try {
			// Execute Page Method
			switch(Req.Method.Nr){
				case 1:
					ret = PC.doGet(Req);
					break;
				case 3:
					ret = PC.doPost(Req);
					break;
				default:return null;
			}
			if(ret == null) { return null; }
			if(plain){
				return ret;
			}
			return Design.Paginate(ret, PageNr, URI, TITLES);
		} catch ( SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void LoadAllPages() {
		// look up all classes
		PageLoader PL = new PageLoader("dynamic/",H);
		URI = PL.URI;
		TITLES = PL.TITLES;
		PClass = PL.PClass;
	}

	private void UnloadAllPages(){
		if(PClass != null){
			PClass.forEach(Page::close);
		}
	}
	// Public Classes
	
	// Private Classes
	private class UpdateThread extends Thread{

		WatchKey key;
		WatchService watcher;
		Path dir;
		Portal par;
		long lastReload = 0l;

		public boolean watching = false;

		public UpdateThread(String target,Portal p){
			par = p;
			try {
				dir = Paths.get(target);
				watcher =  FileSystems.getDefault().newWatchService();
				key = dir.register(watcher,
						StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run(){
			watching = true;
			while(watching){
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					return;
				}

				for (WatchEvent<?> event: key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}
					long target = dir.resolve(((WatchEvent<Path>)event).context()).toFile().lastModified();
					if(target > lastReload + 10000) {
						lastReload = target;
						par.Reload();
					}
					break;
				}


				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}
		}
	}
	
	// Portal Constructors
	public Portal(HOME h){
		H = h;
		LoadAllPages();
		loading = false;
		pageUpdateThread = new UpdateThread("dynamic",this);
		pageUpdateThread.start();
	}
}

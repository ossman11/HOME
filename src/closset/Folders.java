/**
 * 
 */
package closset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;

/**
 * @author Bob den Os
 * The Folders class is the backend solution to centralize all storage related function
 * The Folders class indexes all registered storage solutions on the HOME system
 * Allowing all datastorage to be outsourced to other dedicated storage systems
 * With a fall back to the HOME system for temp files or buffer when a storage system is down
 * Which will be automatically be syncronized when the system comes back online
 */
public class Folders {
	// Public Values
	public String LocalDir;
	
	// Private Values
	private ClassLoader CLoader;
	
	// Public Functions
	public static String CleanUrl(String Url) {
		// Replace Codes
		Url.replace("%20", " ");
		// Remove unwanted slashes
		int S = Url.indexOf('/');
		int C = Url.lastIndexOf(':');
		while(S < C && S > -1 && C > 0)
		{
			Url = Url.substring(S+1);
			C -= S;
			S = Url.indexOf('/');
		}
		return Url;
	}
	
	public byte[] LoadLocalFile(String Url) {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		try {
			Path path = Paths.get( LocalDir + Url );
			if(path == null) { return null; }
			return Files.readAllBytes( path );
		} catch (IOException e) { /* File Could not be found */}
		return null;
	}
	
	public Iterator<String> LoadLocalFileLines(String Url) {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		try {
			Path path = Paths.get( LocalDir + Url );
			if(path == null) { return null; }
			return Files.lines(path).iterator();
		} catch (IOException e) { /* File Could not be found */}
		return null;
	}
	
	public Boolean SaveLocalFile(String Url, byte[] Data) {
		try {
			Url = CleanUrl( LocalDir + Url );
			Path path = Paths.get(Url);
			if(path != null && !Files.exists(path)) {
				Files.createFile(path, new FileAttribute<?>[]{});
			}
			Files.write(path, Data, new OpenOption[]{});
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Iterator<Path> GetDirFiles(String Url) {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		try {
			return Files.list( Paths.get( LocalDir + Url ) ).iterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Private Functions
	private void GetLocalDir() {
		CLoader = Thread.currentThread().getContextClassLoader();
		String ThisClass = "closset/Folders.class";
		String tmpDir = CLoader.getResource(ThisClass).getPath();
		LocalDir = CleanUrl( tmpDir.substring( 0, tmpDir.indexOf(ThisClass) ) );
	}
	
	// Public Classes
	
	// Private Classes
	
	// Folders Constructors
	public Folders() {
		GetLocalDir();
	}
}

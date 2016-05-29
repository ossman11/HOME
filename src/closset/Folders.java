/**
 * 
 */
package closset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	
	// Public Functions
	public static String CleanUrl(String Url) {
		// Replace Codes
		Url = Url.replace("%20", " ");
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

	public static String GetLocalUrl(Object o){
		String ThisClass = o.getClass().getName().replace('.','/')+".class";
		String tmpDir = o.getClass().getClassLoader().getResource(ThisClass).getPath();
		return CleanUrl( tmpDir.substring( 0, tmpDir.indexOf(ThisClass) ) );
	}

	public byte[] LoadLocalFile(String Url) {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		try {
			Path path = Paths.get( LocalDir + Url );
			if(path == null || !Files.exists(path)) { return null; }
			return Files.readAllBytes( path );
		} catch (IOException e) { /* File Could not be found */}
		return null;
	}
	
	public InputStream LoadLocalFileStream(String Url) {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		Path path = Paths.get( LocalDir + Url );
		if(path == null || !Files.exists(path)) { return null; }
		try {
			InputStream r = Channels.newInputStream(FileChannel.open(path));
			return r;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public BufferedReader LoadLocalFileLines(String Url) throws IOException {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		Path path = Paths.get( LocalDir + Url );
		if(path == null || !Files.exists(path)) { return null; }
		CharsetDecoder dec=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.IGNORE);
		
		try {
			Reader r = Channels.newReader(FileChannel.open(path), dec, -1); 
			return new BufferedReader(r);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<String> LoadLocalFileList(String Url) {
		if(Url.charAt(0) == '/') { Url = Url.substring(1); }
		try {
			Path path = Paths.get( LocalDir + Url );
			if(path == null || !Files.exists(path)) { return null; }
			List<String> ret = Files.readAllLines(path);
			return ret;
		} catch (IOException e) { e.printStackTrace();/* File Could not be found */}
		return null;
	}
	
	public Boolean SaveLocalFile(String Url, byte[] Data) {
		try {
			Url = CleanUrl( LocalDir + Url );
			Path path = Paths.get(Url);
			if(path != null) {
				if(!Files.exists(path))
				{
					Files.createFile(path, new FileAttribute<?>[]{});
				}
				Files.write(path, Data, new OpenOption[]{});
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Writer SaveLocalFile(String Url) {
		return SaveLocalFile( Url, true );
	}
	
	public Writer SaveLocalFile(String Url, boolean OverWrite) {
		try {
			Url = CleanUrl( LocalDir + Url );
			Path path = Paths.get(Url);
			if(path != null) {
				Path dir = path.getParent();
				if(!Files.exists(dir)){
					Files.createDirectories(dir, new FileAttribute<?>[]{});
				}
				if(!Files.exists(path))
				{
					Files.createFile(path, new FileAttribute<?>[]{});
				}
				return new FileWriter( new File( path.toString() ), !OverWrite );
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public FileOutputStream SaveLocalFileBinary(String Url) {
		return SaveLocalFileBinary(Url,true);
	}
	
	public FileOutputStream SaveLocalFileBinary(String Url, boolean OverWrite) {
		try {
			Url = CleanUrl( LocalDir + Url );
			Path path = Paths.get(Url);
			if(path != null) {
				Path dir = path.getParent();
				if(!Files.exists(dir)){
					Files.createDirectories(dir, new FileAttribute<?>[]{});
				}
				if(!Files.exists(path))
				{
					Files.createFile(path, new FileAttribute<?>[]{});
				}
				return new FileOutputStream( new File( path.toString() ), !OverWrite );
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
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
	
	public void GetDirFiles(String directoryName, ArrayList<File> files) {
	    File directory = new File(directoryName);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    for (File file : fList) {
	        if (file.isFile()) {
	            files.add(file);
	        } else if (file.isDirectory()) {
	        	GetDirFiles(file.getAbsolutePath(), files);
	        }
	    }
	}
	
	// Private Functions
	private void GetLocalDir() {
		LocalDir = GetLocalUrl(this);
	}
	
	// Public Classes
	
	// Private Classes
	
	// Folders Constructors
	public Folders() {
		GetLocalDir();
	}
}

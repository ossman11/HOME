/**
 * 
 */
package entrance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import closset.DataBase;
import closset.DataBase.BOOL;
import closset.DataBase.INT;
import closset.DataBase.SubTable;
import closset.DataBase.TEXT;
import closset.DataBase.Table;
import closset.Folders;
import network.DB;
import network.FTP;
import network.HTTP;

/**
 * @author Bob den Os
 * HOME is the core of the House Open Management Environment.
 * HOME runs multiple small servers internally for devices to register.
 * HOME uses meta data from the registered devices to determine the location of house hold members.
 * The location data is used to determine where notification need to be send and
 * allows HOME to keep everything accessible only where the user actually has access to it.
 * HOME will be the only possible connection to the in/outside world.
 * If all household members with pre-defined permission are not present the notifications
 * will be send to their representative device by HOME.
 * This allows the head of the house to grand temporary access to other members of the household or 
 * Answer requests while on the go.
 * External connections can be allowed to access locally stored files or data sets.
 * HOME is not required to be used as datastorage. Local systems can extend the HOME server with storage.
 */
public class HOME {

	// Public Values
	public Folders folder = null;
	public DataBase database = null;
	
	public HTTP http = null;
	public FTP ftp = null;
	public DB db = null;
	
	public int Threads = 1;
	
	public ArrayList<WeakReference<char[]>> chunks;
	public ReferenceQueue<char[]> chunkqueue;
	
	// Private Values
	
	// Public Functions
	public String getServerTime() {
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
	
	// Private Functions
	private void CommandListener(){
		System.out.println("Listening to commands:");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String[] in = null;
			while((in = br.readLine().split(":")) != null){
				switch(in[0].toLowerCase()){
					case "reload": http.Reload();
						System.out.println("HTTP Cache updated");
						break;
					case "find": 
						long s = new Date().getTime();
						SubTable result = database.tables.get(0).Find("First", in[1]);
						long e = new Date().getTime();
						
						System.out.println(result.toString());
						
						System.out.println(in[1]+ " was found: " + result.Size() + " times.");
						System.out.println("the search took: " + (e-s) + "ms");
						System.out.println(database.tables.get(0).Size()/(e-s) + " lines per ms");
						break;
					default: System.out.println("unknown command: " + in[0]);
						break;
				}
			}
		} catch (IOException ioe) {
		   System.out.println("End of input stream.");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// HTTP Constructors
	
	public HOME() {
		Threads = Runtime.getRuntime().availableProcessors();
		// Initiation Backend
		folder = new Folders();
		database = new DataBase(this);
		// construct a test table
		Table names = database.CreateTable("Names", 
				new String[]{ "First", "Last", "Age", "Sex" },
				new Class<?>[]{TEXT.class, TEXT.class, INT.class, BOOL.class});	
		
		database.tables.add(names);
		
		System.out.println(names.Size());
		System.out.println("Loaded Database");

		// Starts Servers
		System.out.println("Starting up Servers");
		http = new HTTP(this);
		
		List<String> FirstNames = folder.LoadLocalFileList("test-data/NAMES/first-names.txt");
		int TN = FirstNames.size();
		
		// used to write directly to disk
		// Writer namesWriter = database.OpenTable("Names");
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		while(names.Size() < 100000000){
		
			names.Add(new Object[]{
					FirstNames.get( random.nextInt(TN) ).toCharArray(),
					FirstNames.get( random.nextInt(TN) ).toCharArray(),
					(random.nextInt(100)),
					random.nextBoolean()});
		}
		// disabled to prevent a giant database to be saved to disk
		// names.Save();
	
		// type find:(Some name) to search the database for rows with this name
		// personal results would be between 60k and 90k lines being processed per millisecond
		CommandListener();
	}
	
	/**
	 * @param args
	 * Starts HOME server
	 */
	public static void main(String[] args) {
		new HOME();
	}
}

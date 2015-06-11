/**
 * 
 */
package entrance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import closset.DataBase;
import closset.Folders;
import network.DB;
import network.FTP;
import network.HTTP;

/**
 * @author Bob den Os
 * HOME is the core of the House Open Management Enviroment.
 * HOME runs multiple small servers internally for devices to reqister.
 * HOME uses meta data from the registered devices to determine the location of house hold members.
 * The location data is used to determine where notification need to be send and
 * allows HOME to keep everything accessible only where the user actually has acces to it.
 * HOME will be the only possible connection to the in/outside world.
 * If all household members with pre-defined permission are not present the notifications
 * will be send to thier representative device by HOME.
 * This allows the head of the house to grand temporary acces to other members of the household or 
 * Answer requests while on the go.
 * External connections can be allowed to acces locally stored files or data sets.
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
	
	// HTTP Constructors
	
	public HOME() {
		System.out.println( System.getProperty("sun.arch.data.model") );
		Threads = Runtime.getRuntime().availableProcessors();
		// Initiation Backend
		folder = new Folders();
		database = new DataBase(this);
		System.out.println("Loaded Database");

		// Starts Servers
		System.out.println("Starting up Servers");
		http = new HTTP(this);
	}
	
	/**
	 * @param args
	 * Starts HOME server
	 */
	public static void main(String[] args) {
		new HOME();
	}
}

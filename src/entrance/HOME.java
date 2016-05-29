/**
 * 
 */
package entrance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import closset.DataBase;
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

	public FTP ftp = null;
	public DB db = null;

	// Private Values
	private static final ZoneId GMT = ZoneId.of("GMT");

	private HTTP http = null;

	// Public Functions
	public String getServerTime() {
	    return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(GMT));
	}
	
	// Private Functions
	private void runCommand(String in){
		String[] command = in.split(":");
		switch(command[0].toLowerCase()){
			case "reload":
				http.Reload();
				System.out.println("HTTP Cache updated");

				break;
			case "exit":
				http.stop();
				http = null;
				System.out.println("HTTP Server shutdown");

				database.Auth.Save();
				database.Auth.Dispose();
				for(DataBase.Table t : database.tables){
					t.Save();
					t.Dispose();
				}
				database = null;
				System.out.println("Saved database");

				folder = null;

				try {
					System.in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
				break;
			default: System.out.println("unknown command: " + command[0]);
				break;
		}
	}

	private void CommandListener(String[] args){
		System.out.println("Listening to commands:");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String in = null;
			while((in = br.readLine()) != null){
				runCommand(in);
			}
		} catch (IOException ioe) {
		   System.out.println("End of input stream.");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// HTTP Constructors
	
	public HOME(String[] args) {
		// Init Backend
		folder = new Folders();
		database = new DataBase(this);
		// Init HTTP server
		http = new HTTP(this);
		// Listen for server sided commands
		CommandListener(args);
	}
	
	/**
	 * @param args
	 * Starts HOME server
	 */
	public static void main(String[] args) {
		new HOME(args);
	}
}

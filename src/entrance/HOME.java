/**
 * 
 */
package entrance;

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

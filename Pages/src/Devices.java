/**
 * 
 */

import com.Page;
import network.HTTP.HTTPHandler;

/**
 * @author laptop
 *
 */
public class Devices extends Page {
	// Public Values

	
	// Private Values
	private static final String URL = "/Devices";
	private static final String TITLE = "Devices";
	
	// Public Functions
	protected String doGet(HTTPHandler Req) {
		return	"<h1>Devices</h1>";
	}

	protected String doPost(HTTPHandler Req) {
		return "<html><head></head><body><h1>Welcome Home</h1>" + Req.Content + "</body></html>";
	}
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// Page Constructors
	public Devices(){
		url = URL;
		title = TITLE;
	}
}

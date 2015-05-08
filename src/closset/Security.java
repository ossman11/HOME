/**
 * 
 */
package closset;

/**
 * @author Bob den Os
 * The Security class makes sure that all connection types are secure.
 * All registered authorized devices and connection are checked upon connection.
 * If the request is an registration request the connection is processed and access will
 * need to be granted by an authorized HOME network member.
 * All other request without authorization will be dropped.
 * FTP transfers can only be preformed by registrated devices.
 */
public class Security {

}

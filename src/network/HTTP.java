/**
 * 
 */
package network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entrance.HOME;
import closset.Folders;

import com.Portal;

/**
 * @author Bob den Os
 * HTTP based server for handeling http request
 * Provides acces to the HOME web portal.
 * Allows devices to register to the HOME network
 */
public class HTTP {
	
	// Public Values
	
	// Private Values
	private HOME H = null;
	private Portal P = null;
	private SocketThread OST = null;
	private SocketThread SST = null;
	
	// Public Functions
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// HTTP protocol classes
	// HTTP Request Methods
	public class Method
    {
        // public values
        public int Nr;
        public String Val;

        // private values
        private final List<String> Methods = Arrays.asList(
        	new String[]{
                "OPTIONS", // connection test (basic responce 200 + Allow)
                "GET", // request of url (conditional GET include if-* header)
                "HEAD", // = GET - data
                "POST", // handle embeded data in uri
                "PUT", // handle Content-* or rsponce 501 (Not Implemented)
                "DELETE", // remove content (ignore)
                "TRACE", // 200 (OK) + Content-Type: message/http
                "CONNECT", // proxy method (ignore)
                "ERROR"
            });
        
        // Constructors
        public Method(String method)
        {
            int s = method.indexOf(' ');
            if(s>-1){method = method.split(" ")[0];}
            Val = method.toUpperCase();
            Nr = GetMethodNr(Val);
        }

        // Handlers
        private int GetMethodNr(String method)
        {
            return Methods.indexOf(method);
        }
    }
	// HTTP Request Status
    public class Status
    {
        // public Values
        public String val;
        public int Code;
        public int nr;

        // Static values
        private final int[] Codes = new int[] {
            100,101,
            200,201,202,203,204,205,206,
            300,301,302,303,304,305,306,307,
            400,401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,
            500,501,502,503,504,505
        };

        private final String[] Stats = new String[]
        {
            "100 Continue","101 Switching Protocols",
            "200 OK","201 Created","202 Accepted","203 Non-Authoritative Information","204 No Content","205 Reset Content","206 Partial Content",
            "300 Multiple Choices","301 Moved Permanently","302 Found","303 See Other","304 Not Modified","305 Use Proxy","306 (Unused)","307 Temporary Redirect",
            "400 Bad Request","401 Unauthorized","402 Payment Required","403 Forbidden","404 Not Found",
            "405 Method Not Allowed","406 Not Acceptable","407 Proxy Authentication Required","408 Request Timeout","409 Conflict",
            "410 Gone","411 Length Required","412 Precondition Failed","413 Request Entity Too Large","414 Request-URI Too Long",
            "415 Unsupported Media Type","416 Requested Range Not Satisfiable","417 Expectation Failed",
            "500 Internal Server Error","501 Not Implemented","502 Bad Gateway","503 Service Unavailable","504 Gateway Timeout","505 HTTP Version Not Supported"
        };

        // Constructors
        public Status(int code)
        {
            IntToStatus(code);
        }

        public Status(String code) {
        	this(Integer.parseInt(code.split(" ")[0]));
        }

        // Handlers
        public String toString()
        {
            return Stats[nr];
        }

        private void IntToStatus(int code)
        {
            Code = code;
            nr = FindCode(Code);
            val = Stats[nr];
        }

        private int FindCode(int code)
        {
            for(int i = 0; i < Codes.length; i++)
            {
                if (Codes[i] == code) { return i; }
            }
            return 35;
        }
    }
    // HTTP Headers
    public enum HeaderId
    {
        Accept, AcceptCharset, AcceptEncoding, AcceptLanguage, AcceptRanges,
        Age, Allow, Authorization, CacheControl, Connection,
        ContentEncoding, ContentLanguage, ContentLength, ContentLocation, ContentMD5, ContentRange, ContentType, Date, ETag, Expect, Expires, From, Host, Origin,
        IfMatch, IfModifiedSince, IfNoneMatch, IfRange, IfUnmodifiedSince, LastModified,
        Location, MaxForwards, Pragma, ProxyAuthenticate, ProxyAuthorization,
        Range, Referer, RetryAfter, Server, TE, Trailer, TransferEncoding, Upgrade, UserAgent, Vary, Via, Warning, WWWAuthenticate;
    }
	// HTTP Request and Responce Headers
    public class Header
    {
        // public Values
        public String Head;
        public int Nr;
        public String Val;

        // Static values
        private final String[] Headers = new String[]
        {
            "Accept","Accept-Charset","Accept-Encoding","Accept-Language","Accept-Ranges", // client headers
            "Age","Allow","Authorization","Cache-Control","Connection", // 
            "Content-Encoding","Content-Language","Content-Length","Content-Location","Content-MD5","Content-Range","Content-Type","Date","ETag","Expect","Expires","From","Host", "Origin", // Server headers
            "If-Match","If-Modified-Since","If-None-Match","If-Range","If-Unmodified-Since","Last-Modified", // conditional headers
            "Location","Max-Forwards","Pragma","Proxy-Authenticate","Proxy-Authorization", // Proxy headers
            "Range","Referer","Retry-After","Server","TE","Trailer","Transfer-Encoding","Upgrade","User-Agent","Vary","Via","Warning","WWW-Authenticate" // Rare headers
        };

        // Constructors
        public Header(String head)
        {
            int s = head.indexOf(':');
            if (s < 1)
            {
                Head = head;
                Nr = GetNr(Head);
                Val = "";
            }
            else
            {
                Head = head.substring(0, s);
                Nr = GetNr(Head);
                Val = head.substring(s + 2);
            }
        }

        public Header(int nr, String val)
        {
            Nr = nr;
            Head = Headers[Nr];
            Val = val;
        }

        public Header(HeaderId hid, String val) {
        	this(hid.ordinal(), val);
        }

        // Handlers
        private int GetNr(String head)
        {
            for(int i = 0; i < Headers.length; i++)
            {
                if (Headers[i].equals(head) ) { return i; }
            }
            return -1;
        }

        public String toString()
        {
            return Head + ": " + Val;
        }
    }
    
    // HTTP Handler creates responces to request headers
	public class HTTPHandler {
		// Public Values
		public String Content = null;
		public String Request = null;
		public String Protocol = null;
		public Method Method = null;
		public Header[] Headers = null;
		public Boolean Secure = false;
		
		public Boolean NotFound = false;		
		// Private Values
		
		// Public Functions
		public byte[] ByteResponce(){			
			StringBuilder SB = new StringBuilder();
			byte[] ret;

			SB.append(Protocol + " 200 OK" + System.lineSeparator());
			SB.append("Date: " + H.getServerTime() + System.lineSeparator());
			if(Request.contains(".")) {
				// Check correct methods
				if(Method.Nr != 1 && Method.Nr != 2){return RetNotFound();}
				// Load file
				Request = Request.replace("/favicon.ico", "/favicon.png");
				byte[] file = H.folder.LoadLocalFile( "web/" + Request );
				// Check if File exists
				if(file == null){ return RetNotFound(); }
				// Add Content Headers
				SB.append("Content-Length: " + file.length + System.lineSeparator());
				SB.append("Content-Type: image/png" + System.lineSeparator());
				SB.append(System.lineSeparator());
				// Bind bytes
				byte[] head = SB.toString().getBytes(Charset.forName("UTF-8"));
				if(Method.Nr == 1)
				{
					ret = new byte[head.length + file.length];
					System.arraycopy(head,0,ret,0,head.length);
					System.arraycopy(file,0,ret,head.length,file.length);
				} else {
					ret = head;
				}
				// Return final responce
				return ret;
			} else {
				ret = H.folder.LoadLocalFile( "cache/" + Request.hashCode() + ".tmp" );
				if(ret != null) {
					return ret;
				}
				
				String Content = P.GetPage(this);
				if(Content == null) { return RetNotFound(); }
				// Send standard responce
				SB.append("Content-Length: " + Content.length() + System.lineSeparator());
				SB.append("Content-Type:text/html;" + System.lineSeparator());
				SB.append(System.lineSeparator());
				if(Method.Nr != 2) {
					SB.append(Content.toString());
				}
				// Convert to bytes and cache
				ret = SB.toString().getBytes(Charset.forName("UTF-8"));
				H.folder.SaveLocalFile( "cache/" + Request.hashCode() + ".tmp", ret);
				return ret;
			}
		}
				
		// Private Functions
		private byte[] RetNotFound(){
			NotFound = true;
			
			String NFStr = Protocol + " 404 Not Found" + System.lineSeparator();
			NFStr += new Header(HeaderId.Connection, "close").toString() + System.lineSeparator();
			NFStr += new Header(HeaderId.ContentType, "text/html").toString() + System.lineSeparator();
			NFStr += new Header(HeaderId.Date, H.getServerTime()).toString() + System.lineSeparator();
			NFStr += System.lineSeparator();
			return NFStr.getBytes(Charset.forName("UTF-8"));
		}
		
		// HTTPHandler Constructors
		public HTTPHandler(ArrayList<String> request, Boolean secure) {
			System.out.print(request.get(0));
			// Setup
			Secure = secure;
			String[] tmpReq = request.get(0).split(" ");
			Request = tmpReq[1];
			Protocol = tmpReq[2].substring(0,tmpReq[2].length()-2);
			
			Method = new Method(tmpReq[0]);
			Headers = new Header[HeaderId.values().length];
			for(int i = 1; i < request.size()-2; i++) {
				if(request.get(i).indexOf(':') < 1){continue;}
				Header tmp = new Header(request.get(i));
				Headers[tmp.Nr] = tmp;
			}
			
			Content = request.get(request.size()-1);
		}
	}
	
	// Private Threads
	private class SocketThread extends Thread {
		
		// Public Values
		public ServerSocket ServerSocket = null;
		public int Port = 0;
		public Boolean Secure = false;
		public Boolean Running = false;
		
		// Private Values
		
		public SocketThread(int port) {
			if(port == 443) { Secure = true; }
			Port = port;
        }

        public void run() {
        	try {
        		// Create ServerSocket
				ServerSocket = new ServerSocket(Port);
				Running = true;
				System.out.println("Server");
        		while(Running) {
					// Thread waits for a request
					Socket RS = ServerSocket.accept();
					RequestThread RT = new RequestThread( RS, Secure);
					RT.run();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	private class RequestThread extends Thread {
		
		// Public Values
		public Socket CSocket = null;
		public Boolean Secure = false;
		
		// Private Values
		private OutputStream Writer = null;
		private InputStreamReader Reader = null;
		
		private HTTPHandler request = null;
		
		public RequestThread(Socket socket, Boolean CType) {
			// Create ServerSocket
			Secure = CType;
			CSocket = socket;
        }

        public void run() {
        	try {
        		// Connection
        		Writer = CSocket.getOutputStream();
				Reader = new InputStreamReader(CSocket.getInputStream());
				Boolean R = true;
				// Content Length
				int CL = 0;
				// Recieved DataStream
        		char[] in = new char[1];
        		String buff = "";
        		// Buffered Data
        		ArrayList<String> RA = new ArrayList<String>();
        		
				while (Reader.read(in) > -1 && R) {
					buff+=in[0];
					//System.out.println(((byte)in[0]));
					if(in[0] == (char)10) {
						RA.add(buff);
						if(buff.startsWith("Content-Length: ")){ CL = Integer.parseInt(buff.substring(16, buff.length()-2)) ;}
						if((byte)buff.charAt(0) == 13)
						{
							buff = "";
							while(CL > 0){
								Reader.read(in);
								buff+=in[0];
								CL--;
							}
							RA.add(buff);
							
							// Converts Read lines to HTTPHandler
							request = new HTTPHandler( RA, Secure);
							RA = new ArrayList<String>();
							// Send Responce back
							Writer.write(request.ByteResponce());
							System.out.println("Finished request.");
							// Check wether responce was a Not Found error and closes connection
							if(request.NotFound) {				
								break;
							}
						}
						buff = "";
					}
				}
				Writer.close();
				Reader.close();
				CSocket.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

	// HTTP Constructors
	public HTTP(HOME h) {
		// Saves parent HOME
		H = h;
		P = new Portal(h);
		
		// Creates HTTP server threads
		OST = new SocketThread(80);
		SST = new SocketThread(443);
		
		// Start Servers
		OST.run();
		SST.run();
	}
}

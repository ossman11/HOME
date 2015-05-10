/**
 * 
 */
package network;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import entrance.HOME;

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
	private SocketThread OST = null;
	private SocketThread SST = null;
	
	// Public Functions
	
	// Private Functions
	
	// Public Classes
	
	// Private Classes
	
	// HTTP protocol classes
    private class Method
    {
        // public values
        public int Nr;
        public String Val;

        // private values
        private final String[] Methods = new String[]
        {
            "OPTIONS", // connection test (basic responce 200 + Allow)
            "GET", // request of url (conditional GET include if-* header)
            "HEAD", // = GET - data
            "POST", // handle embeded data in uri
            "PUT", // handle Content-* or rsponce 501 (Not Implemented)
            "DELETE", // remove content (ignore)
            "TRACE", // 200 (OK) + Content-Type: message/http
            "CONNECT", // proxy method (ignore)
            "ERROR"
        };

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
            for (int i = 0; i < Methods.length; i++)
            {
                if (Methods[i] == method) { return i; }
            }
            return Methods.length;
        }
    }

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

    public enum HeaderId
    {
        Accept, AcceptCharset, AcceptEncoding, AcceptLanguage, AcceptRanges,
        Age, Allow, Authorization, CacheControl, Connection,
        ContentEncoding, ContentLanguage, ContentLength, ContentLocation, ContentMD5, ContentRange, ContentType, Date, ETag, Expect, Expires, From, Host,
        IfMatch, IfModifiedSince, IfNoneMatch, IfRange, IfUnmodifiedSince, LastModified,
        Location, MaxForwards, Pragma, ProxyAuthenticate, ProxyAuthorization,
        Range, Referer, RetryAfter, Server, TE, Trailer, TransferEncoding, Upgrade, UserAgent, Vary, Via, Warning, WWWAuthenticate
    }
	
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
            "Content-Encoding","Content-Language","Content-Length","Content-Location","Content-MD5","Content-Range","Content-Type","Date","ETag","Expect","Expires","From","Host", // Server headers
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
	private class HTTPHandler {
		// Public Values
		public String Request = null;
		public String Protocol = null;
		public Method Method = null;
		public Header[] Headers = null;
		public Boolean Secure = false;
		
		// Private Values
		
		// Public Functions
		public byte[] ByteResponce(){
			StringBuilder SB = new StringBuilder();
			StringBuilder CB = new StringBuilder();
			
			CB.append("<html><head></head><body><h1>You got me :D</h1></body></html>");			

			SB.append(Protocol + " 200 OK" + System.lineSeparator());
			SB.append("Date: " + H.getServerTime() + System.lineSeparator());
			if(Request.contains("favicon.ico")) {
				// Send Custom Favicon				
				byte[] fav;
				try {
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					String favURL = classLoader.getResource("favicon.png").getPath();
					favURL = favURL.replace("%20", " ");
					while(favURL.charAt(0) == '/')
					{
						favURL = favURL.substring(1,favURL.length());
					}
					fav = Files.readAllBytes( Paths.get( favURL ) );
					
					SB.append("Content-Length: " + fav.length + System.lineSeparator());
					SB.append("Content-Type: image/jpeg" + System.lineSeparator());
					SB.append(System.lineSeparator());
					byte[] head = SB.toString().getBytes(Charset.forName("UTF-8"));
					byte[] ret = new byte[head.length + fav.length];
					
					System.arraycopy(head,0,ret,0,head.length);
					System.arraycopy(fav,0,ret,head.length,fav.length);
					
					return ret;
				} catch (IOException e) {
					e.printStackTrace();
					return "HTTP/1.1 404 NOT FOUND".getBytes(Charset.forName("UTF-8"));
				}
			} else {
				// Send standard responce
				SB.append("Content-Length: " + CB.toString().length() + System.lineSeparator());
				SB.append("Content-Type:text/html;" + System.lineSeparator());
				SB.append(System.lineSeparator());
				SB.append(CB.toString());
				
				return SB.toString().getBytes();
			}
		}
		
		// Private Functions
		
		// HTTPHandler Constructors
		public HTTPHandler(ArrayList<String> request, Boolean secure) {
			Secure = secure;
			String[] tmpReq = request.get(0).split(" ");
			Request = tmpReq[1];
			Protocol = tmpReq[2];
			Method = new Method(tmpReq[0]);
			Headers = new Header[HeaderId.values().length];
			for(int i = 1; i < request.size()-1; i++) {
				if(request.get(i).indexOf(':') < 1){continue;}
				Header tmp = new Header(request.get(i));
				Headers[tmp.Nr] = tmp;
			}
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
		private BufferedReader Reader = null;
		
		private HTTPHandler request = null;
		
		public RequestThread(Socket socket, Boolean CType) {
			// Create ServerSocket
			Secure = CType;
			CSocket = socket;
        }

        public void run() {
        	try {
        		Writer = CSocket.getOutputStream();
				Reader = new BufferedReader( new InputStreamReader(CSocket.getInputStream()));
				
        		String in;
        		ArrayList<String> RA = new ArrayList<String>();
        		
				while ((in = Reader.readLine()) != null) {
					RA.add(in);
					System.out.println(in);
					if(in.isEmpty()) {
						System.out.println("Header Ended.");
						// Converts Read lines to HTTPHandler
						request = new HTTPHandler( RA, Secure);
						RA = new ArrayList<String>();
						// Send Responce back
						Writer.write(request.ByteResponce());
						System.out.println("Finished request.");
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

	// HTTP Constructors
	public HTTP(HOME h) {
		// Saves parent HOME
		H = h;
		
		// Creates HTTP server threads
		OST = new SocketThread(80);
		SST = new SocketThread(443);
		
		// Start Servers
		OST.run();
		SST.run();
	}
}

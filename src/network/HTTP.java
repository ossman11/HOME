/**
 * 
 */
package network;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import entrance.HOME;

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
	private ContentManager M = null;
	private SocketThread OST = null;
	private SocketThread SST = null;
	
	// Public Functions
	public void Reload(){
		M.Reload();
	}
	
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
            return Head + ": " + Val + System.lineSeparator();
        }
        
        public byte[] toBytes(){
        	return this.toString().getBytes(Charset.forName("UTF-8"));
        }
    }
    
    // HTTP Handler creates responces to request headers
	public class HTTPHandler {
		// Public Values
		public HOME Home = null;
		public String Content = null;
		public String Request = null;
		public String Protocol = null;
		public Method Method = null;
		public Header[] Headers = null;
		public Boolean Secure = false;
		
		public Boolean NotFound = false;		
		// Private Values
		private String GetContentType(String url)
        {
            String end = url.substring(url.lastIndexOf('.')+1);
            switch (end.toLowerCase())
            {
                // text
                case "html":
                    return "text/html";
                case "js":
                    return "text/javascript";
                case "css":
                    return "text/css";
                case "xml":
                    return "text/xml";
                // Image
                case "jpg":
                    return "image/jpeg";
                case "jpeg":
                    return "image/jpeg";
                case "gif":
                    return "image/gif";
                case "png":
                    return "image/png";
                // default
                default:
                    return "text/plain";
            }
        }
		
		// Public Functions
		public void write( OutputStream out ) throws IOException{	
			if(Request.contains(".")) {
				RetFile(out);
			} else {
				RetPage(out);
			}
		}
				
		// Private Functions
		private void RetNotFound( OutputStream out ) throws IOException{
			NotFound = true;
			
			out.write((Protocol + " 404 Not Found" + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));
			out.write( new Header(HeaderId.Connection, "close").toBytes() );
			out.write( new Header(HeaderId.ContentType, "text/html").toBytes() );
			out.write( new Header(HeaderId.Date, Home.getServerTime()).toBytes() );
			out.write( System.lineSeparator().getBytes(Charset.forName("UTF-8")) );
			return;
		}
		
		private void RetFile( OutputStream out ) throws IOException{
			// Check correct methods
			if(Method.Nr != 1 && Method.Nr != 2){ RetNotFound(out); return; }
			// Load file
			Request = Request.replace("/favicon.ico", "/favicon.png");
			byte[] file = M.Get(Request);
			// Check if File exists
			if(file == null){ RetNotFound(out); return; }
			// Check Request headers
			if(Headers[HeaderId.IfModifiedSince.ordinal()] != null && Headers[HeaderId.IfModifiedSince.ordinal()].Val.contains(M.GetTime())) {
				out.write((Protocol + " 304 Not Modified" + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));
				out.write(new Header(HeaderId.CacheControl,"max-age=315360000").toBytes());
				out.write(new Header(HeaderId.Date,Home.getServerTime()).toBytes());
				out.write(new Header(HeaderId.Expires,"Thu, 31 Dec 2037 23:55:55 GMT").toBytes());
				if(Headers[9] != null && Headers[9].Val.contains("keep-alive")) {
					out.write(new Header(HeaderId.Connection, "Keep-Alive").toBytes());
				}	
				out.write(System.lineSeparator().getBytes(Charset.forName("UTF-8")));
				return;
			}
			// Respond
			out.write((Protocol + " 200 OK" + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));
			out.write(new Header(HeaderId.Date, Home.getServerTime()).toBytes());
			out.write(new Header(HeaderId.ContentLength, file.length+"").toBytes());
			out.write(new Header(HeaderId.ContentType, GetContentType(Request)).toBytes());
			out.write(new Header(HeaderId.LastModified, M.GetTime()).toBytes());
			
			if(Headers[9] != null && Headers[9].Val.contains("keep-alive")) {
				out.write(new Header(HeaderId.Connection, "Keep-Alive").toBytes());
			}	
			out.write(System.lineSeparator().getBytes(Charset.forName("UTF-8")));
			// Bind bytes
			out.write(file);
			return;
		}
		
		private void RetPage( OutputStream out ) throws IOException{
			String Content = P.GetPage(this);
			if(Content == null) { RetNotFound(out);return; }
			// Send standard responce
			out.write((Protocol + " 200 OK" + System.lineSeparator()).getBytes(Charset.forName("UTF-8")));
			out.write(new Header(HeaderId.Date, Home.getServerTime()).toBytes());
			out.write(new Header(HeaderId.ContentLength, Content.length()+"").toBytes());
			out.write(new Header(HeaderId.ContentType, "text/html").toBytes());

			if(Headers[9] != null && Headers[9].Val.contains("keep-alive")) {
				out.write(new Header(HeaderId.Connection, "Keep-Alive").toBytes());
			}
			
			out.write(System.lineSeparator().getBytes(Charset.forName("UTF-8")));
			
			if(Method.Nr != 2) {
				out.write(Content.getBytes(Charset.forName("UTF-8")));
			}
			return;
		}
		
		// HTTPHandler Constructors
		public HTTPHandler(ArrayList<String> request, Boolean secure) {
			System.out.print(request.get(0));
			// Setup
			Secure = secure;
			String[] tmpReq = request.get(0).split(" ");
			Request = tmpReq[1];
			Protocol = tmpReq[2].substring(0,tmpReq[2].length()-2);
			
			Home = H;
			
			Method = new Method(tmpReq[0]);
			Headers = new Header[HeaderId.values().length];
			for(int i = 1; i < request.size()-2; i++) {
				if(request.get(i).indexOf(':') < 1){continue;}
				Header tmp = new Header(request.get(i));
				if(tmp.Nr > -1){
					Headers[tmp.Nr] = tmp;
				}
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
					RT.start();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	private class RequestThread extends Thread {
		
		// Public Values
		public ArrayList<ArrayList<String>> queue = null;
		public OutputStream Writer = null;
		
		public Socket CSocket = null;
		public Boolean Secure = false;
		
		// Private Values
		private HandlerThread Child = null;
		private InputStreamReader Reader = null;
		
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
				// Content Length
				int CL = 0;
				// Recieved DataStream
        		char[] in = new char[1];
        		String buff = "";
        		// Buffered Data
        		queue = new ArrayList<ArrayList<String>>();
        		Child = new HandlerThread(this);
        		Child.start();
        		ArrayList<String> RA = new ArrayList<String>();
        		
				while (Reader.read(in) > -1) {
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
							queue.add(new ArrayList<String>(RA));
							RA = new ArrayList<String>();
						}
						buff = "";
					}
				}
				System.out.println("Quiting Request Thread");
				Child.Quit();
			} catch (IOException e) {
				// it is almost supposed to happen.
				// Without the message you don't even notice that using exceptions to kill threads is a bad idea.
				// e.printStackTrace();
			} finally {
				Child = null;
				try {
					if(Writer != null) { Writer.close(); }
					if(Reader != null) { Reader.close(); }
					if(CSocket != null) { CSocket.close(); }
				} catch (IOException e) {}
			}
        	return;
        }
    }
	
	private class HandlerThread extends Thread {
		// Public Values
		public Boolean Stopping = false;
		
		// Private Values
		private RequestThread Par = null;
		private OutputStream out = null;
		
		public HandlerThread(RequestThread RT) {
			Par = RT;
			out = Par.Writer;
        }
		
		public void Quit()
		{
			Stopping = true;
			while(Stopping){};
			return;
		}

        public void run() {
        	HTTPHandler tmp = null;
        	while(out != null && Par.queue != null)
        	{
        		if(Stopping || !Par.isAlive() || Par == null || out == null){return;}
        		if(Par.queue.size() > 0 && Par != null && out != null) {
        			try {
        				ArrayList<String> tmpRA = Par.queue.get(0);
        				if(tmpRA != null)
        				{
        					tmp = new HTTPHandler(tmpRA, Par.Secure);
            				Par.queue.remove(0);
            				if(tmp != null) {
            					tmp.write(out);
            					if(tmp.NotFound) {
            						Par.CSocket.close();
            						break;
            					}
            				}
        				}
    				} catch (IOException e) {
    					break;
    				}
        		}
        	}
        	Stopping = false;
        }
	}
	
	private class ContentManager{
		// Public Values
		
		// Private Values
		private List<String> WEBFiles = null;
		private byte[][] WEBCache = null;
		private long[] WEBDate = null;
		
		private String Time = null;
		
		// constructors
		public ContentManager(){
			Load();
		}
		
		// public functions
		public String GetTime(){
			return Time;
		}
		
		public byte[] Get(String url){
			url = "web" + url;
			int i = WEBFiles.indexOf( url );
			if(i > -1 && i < WEBCache.length ){return WEBCache[i];}
			System.out.println("Page not Found: " + url + " - " + i);
			return null;			
		}
		
		public void Reload(){	
			ArrayList<File> FileList = new ArrayList<File>();
			
			H.folder.GetDirFiles(H.folder.LocalDir+"web/", FileList);
			if(FileList.size() != WEBFiles.size()){Load(); return;}
			
			Iterator<File> FileIt = FileList.iterator();
			
			long[] DateTMP = new long[FileList.size()];
			String[] UrlTMP = new String[FileList.size()];
			
			int i = 0;
			while(FileIt.hasNext()) {
				File cur = FileIt.next();
				if(WEBFiles.get(i) != GetFileName(cur)){ Load(); }
				if(WEBDate[i] < cur.lastModified())
				{
					DateTMP[i] = cur.lastModified();
					UrlTMP[i] = cur.getAbsolutePath().replace("\\", "/");
					UrlTMP[i] = UrlTMP[i].substring(UrlTMP[i].indexOf("bin/")+4);
					WEBCache[i] = H.folder.LoadLocalFile(UrlTMP[i]);
				} else {
					DateTMP[i] = WEBDate[i];
					UrlTMP[i] = WEBFiles.get(i);
				}

				i++;
			}
			WEBFiles = Arrays.asList( UrlTMP );
			WEBDate = DateTMP;
			
			UrlTMP = null;
			Time = H.getServerTime();
		}
		
		// private functions
		private void Load(){
			ArrayList<File> FileList = new ArrayList<File>();
			
			H.folder.GetDirFiles(H.folder.LocalDir+"web/", FileList);
			Iterator<File> FileIt = FileList.iterator();
			
			long[] DateTMP = new long[FileList.size()];
			String[] UrlTMP = new String[FileList.size()];
			WEBCache = new byte[FileList.size()][0];
			
			int i = 0;
			while(FileIt.hasNext()) {
				File cur = FileIt.next();
				
				DateTMP[i] = cur.lastModified();
				UrlTMP[i] = cur.getAbsolutePath().replace("\\", "/");
				UrlTMP[i] = UrlTMP[i].substring(UrlTMP[i].indexOf("bin/")+4);
				//System.out.println(UrlTMP[i]);
				WEBCache[i] = H.folder.LoadLocalFile(UrlTMP[i]);
				
				i++;
			}
			WEBFiles = Arrays.asList( UrlTMP );
			WEBDate = DateTMP;
			
			UrlTMP = null;
			Time = H.getServerTime();
		}
	
		private String GetFileName(File c){
			String file = c.getAbsolutePath().replace("\\", "/");
			return file.substring(file.indexOf("bin/")+4);
		}
	}
	
	// HTTP Constructors
	public HTTP(HOME h) {
		// Saves parent HOME
		H = h;
		P = new Portal(h);
		M = new ContentManager();
		M.Reload();
		
		// Creates HTTP server threads
		OST = new SocketThread(80);
		SST = new SocketThread(443);
		
		// Start Servers
		OST.start();
		SST.start();
	}
}

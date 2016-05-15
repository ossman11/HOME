package closset;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entrance.HOME;

/**
 * @author Bob den Os
 * HOME DataBase used by HOME devices to access and store datasets
 * The HOME DataBases are stored on the local storage solutions (default: HOME system internal storage)
 */
public class DataBase {
	// Public Values
	public Table Auth = null;
	public ArrayList<Table> tables = null;
	
	// Private Values
	private HOME H = null;
	
	// Public Functions
	public Table CreateTable(String name, String[] fields, Class<?>[] types){
		Table ret = new Table(name,fields,types);
		if(tables != null){
			tables.add(ret);
		}
		return ret;
	}
	/*
	public Table Find( String table, String Field, String Val) {
		return Find(table,new String[]{Field}, new String[]{Val});
	}
	*/
	/*
	public Table Find( String table, String[] Field, String[] val) {
		Table ret = new Table(table);
		BufferedReader d = null;
		try {
			d = H.folder.LoadLocalFileLines("db/" + table + ".db");
			if(d == null) {return null;}
			
			int F = 0;
			Object[] Val = new Object[val.length];
			Class<?>[] FC = new Class<?>[Field.length];
			int[] field = new int[Field.length];
			
			String S = "";
			
			while(d.ready()){
				S = d.readLine();
				if(S == null ) {break;}
				if(S.startsWith("Table:")) {
					continue;
				}
				if(S.equals("Meta:")) {
					// Read Meta
					ArrayList<String> tmpField = new ArrayList<String>();
					ArrayList<Class<?>> tmpClass = new ArrayList<Class<?>>();
					while(d.ready()) {
						S = d.readLine();
						if(S.equals("Row:")) { break; }
						String[] split = S.split(":");
						tmpField.add(split[0]);
						try {
							tmpClass.add(ClassLoader.getSystemClassLoader().loadClass(split[1].replace("class ", "")));
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							return null;
						}
					}
					// Create Meta
					String[] conField = new String[F];
					Class<?>[] conType = new Class<?>[F];
					conField = tmpField.toArray(conField);
					conType = tmpClass.toArray(conType);
					ret.Meta = new Meta(conField,conType);
					// Pre Process Meta for Check
					F = ret.Meta.Field.size();
					for(int i = 0; i < Field.length; i++) {
						field[i] = ret.Meta.Field.indexOf(Field[i]);
						FC[i] = ret.Meta.Type[field[i]];
					}
					// convert all values into database objects
					for(int i = 0; i < val.length; i++) {
						if( FC[i] == INT.class) {
							Val[i] = INT.fromString(val[i]);
		    			} else if( FC[i] == TEXT.class ) {
		    				Val[i] = TEXT.fromString(val[i]);
		    			} else if( FC[i] == BOOL.class ) {
		    				Val[i] = BOOL.fromString(val[i]);
		    			}
					}
					
					System.out.println(ret.Meta.toString());
				}
				
				if(S.equals("Row:")) {
					// Read Row
					ArrayList<Object> tmpRow = new ArrayList<Object>();
					for(int i = 0;(d.ready() && i < F); i++) {
						S = d.readLine();
						if( ret.Meta.Type[i] == INT.class) {
							tmpRow.add(INT.fromString(S));
						} else if( ret.Meta.Type[i] == TEXT.class ) {
							tmpRow.add(TEXT.fromString(S));
						}
					}
					Object[] conRow = new Object[F];
					conRow = tmpRow.toArray(conRow);
					// Check Row
					boolean curc = true;
	        		for( int i = 0; i < field.length; i++ ) {
		    			if( FC[i] == INT.class) {
		    				//System.out.println(conRow[field[i]]);
		    				if( !INT.equals(conRow[field[i]], Val[i]) ) {
		                		curc = false;
		                		break;
		                	}
		    			} else if( FC[i] == TEXT.class ) {
		    				if( !TEXT.equals(conRow[field[i]], Val[i]) ) {
		    					curc = false;
		                		break;
							}
		    			} else if( FC[i] == BOOL.class ) {
		    				// System.out.println("Bool");
		    				if( !BOOL.equals(conRow[field[i]], Val[i]) ) {
		    					curc = false;
		                		break;
		                	}
		    			}
	        		}
	        		
	        		// Add if Match
	        		if(curc) {
	        			ret.Data.add(new Row(ret, ret.Data.size(), conRow));
	        		}
				}
			}
			return ret;
		} catch ( IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	*/
	public boolean Add( String table, Row row ) {
		Writer out = H.folder.SaveLocalFile( "db/" + table + ".db" , false);
		if(out == null) { System.out.println("Failed to get writer");return false; }
		try {
		    row.write(out);
		    out.flush();
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public boolean Add( Writer out, Row row ) {
		if(out == null) { return false; }
		try {
		    row.write(out);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	public Writer OpenTable(String table){
		return H.folder.SaveLocalFile( "db/" + table + ".db" , false);
	}
	
	// Private Functions
	
	// Public Classes
	public class Table {
		public String Name = null;
		public Meta Meta = null;
		public ArrayList<char[]> Data = null;
		private int Size = 0;
		
		public static final int chunkSize = 100000;
		
		// storage
		public Boolean Save(){
			if(Meta == null || Data == null || Name == null) { return false; }
			Writer out = H.folder.SaveLocalFile( "db/" + Name + ".db" );
			try {
				// header
				out.write( "Table:" + Name + System.lineSeparator() );
				Meta.write( out );
				// Data
				out.write( "Data:" + System.lineSeparator() );
				
				out.flush();
				for(int i=0;i<Data.size()-1;i++){
					out.write(Data.get(i));
					out.flush();
				}
				int left = (Size%chunkSize)*Meta.Size;
				if(left == 0 && (Size/chunkSize) == Data.size()){
					out.write(Data.get(Data.size()-1));
				} else {
					out.write(Data.get(Data.size()-1),0,left);
				}				
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		public Boolean Load(String name) {
			BufferedReader d = null;
			try {
				
				d = H.folder.LoadLocalFileLines("db/" + name + ".db");
				if(d == null) {return false;}
				int F = 0;
				String S = "";
				
				Data = new ArrayList<char[]>(); 
				
				while(d.ready()){
					S = d.readLine();
					if(S == null ) {break;}
					if(S.startsWith("Table:")) {
						Name = S.substring(6);
					}
					if(S.equals("Meta:")) {
						ArrayList<String> tmpField = new ArrayList<String>();
						ArrayList<Class<?>> tmpClass = new ArrayList<Class<?>>();
						while(d.ready()) {
							S = d.readLine();
							if(S == null || S.startsWith("Data:")) { break; }
							String[] split = S.split(":");
							tmpField.add(split[0]);
							try {
								tmpClass.add(ClassLoader.getSystemClassLoader().loadClass(split[1].replace("class ", "")));
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								return false;
							}
						}
						String[] conField = new String[F];
						Class<?>[] conType = new Class<?>[F];
						conField = tmpField.toArray(conField);
						conType = tmpClass.toArray(conType);
						Meta = new Meta(conField,conType);
						F = Meta.Field.size();
						if(S == null){break;}
					}
					if(S != null && S.equals("Data:")) {
						char[] tmp = new char[Meta.Size];
						while(d.read(tmp) == Meta.Size){
							Add(tmp);
						}
					}
				}

				return true;
			} catch( IOException e) {
				e.printStackTrace();
			} finally {
				if(d!=null) {
					try{d.close();}catch(IOException e){e.printStackTrace();}
				}
			}
			return false;
		}
		
		// Display
		public String toString() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			SB.append( "Table:" + Name + SP );
			SB.append( Meta.toString() );
			for(int i=0;i<Size;i++){
				SB.append( new Row(this,i).toString() );
			}
			return SB.toString();
		}
		
		public String toHTML() {
			return toHTML(0,this.Size());
		}
		
		public String toHTML(int start, int end) {
			if(end > Size()){end = Size();}
			if(start < 0){start = 0;}
			if(start > end){end = start;}
			
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			SB.append( "<table>" + SP );
			SB.append( Meta.toHTML() );
			Row tmp = null;
			for( int i = start; i < end; i++) {
				// TODO needs to work out rows.
				tmp = new Row(this,i);
				SB.append(tmp.toHTML());
			}
			SB.append("</table>");
			return SB.toString();
		}
		
		// edit
		public void Add(String[] value) {
			
			if(value.length == Meta.Type.length) {
				Object[] tmp = new Object[Meta.Type.length];				
				for(int i = 0; i < value.length; i++ ) {
					if( Meta.Type[i] == INT.class) {
						tmp[i] = INT.fromString(value[i]);
					} else if( Meta.Type[i] == TEXT.class ) {
						tmp[i] = TEXT.fromString(value[i]);
					} else if( Meta.Type[i] == BOOL.class ) {
						tmp[i] = BOOL.fromString(value[i]);
					}
				}
				this.Add(tmp);
			}
		}
		
		public void Add(Object[] value) {
			if(value.length == Meta.Type.length) {
				if((Size+1) > Data.size() * chunkSize){
					AddChunk();
				}
				Row newRow = new Row(this,Size);
				for(int i = 0; i < value.length; i++ ) {
					newRow.Set(i, value[i]);
				}
				Size++;
			}
		}
		
		public void Add(char[] value) {
			if(value.length == Meta.Size) {
				if((Size+1) > Data.size() * chunkSize){
					AddChunk();
				}				
				int chunk = (int) (Size/chunkSize);
				int localpos = (int) (Size % chunkSize);				
				System.arraycopy(value, 0, Data.get(chunk), localpos*Meta.Size, value.length);
				Size++;
			}
		}
		
		private void AddChunk(){
			Data.add(new char[Meta.Size*chunkSize]);
		}
		
		public Row Get(int id) {
			if(id > -1 && id < this.Size()) {
				return new Row(this,id);
			}
			return null;
		}
		
		public int Size(){
			return Size;
		}
		
		// dump
		public void Dispose() {
			Name = null;
			Meta = null;
			Data = null;
			System.gc();
		}
		
		// process
		public SubTable Find(String Field, String value ) {
			// Preperation
			int[] field = new int[]{ Meta.Field(Field) };
			if(field[0]<0){return null;}
			Class<?>[] FC = new Class<?>[]{ Meta.Type[field[0]] };
			Object[] obj = new Object[1];
			if( FC[0] == INT.class) {
				obj[0] = INT.fromString(value);
			} else if( FC[0] == TEXT.class ) {
				obj[0] = TEXT.fromString(value);
			} else if( FC[0] == BOOL.class ) {
				obj[0] = BOOL.fromString(value);
			}
			
			// Threading
			System.out.println("Started multi threading a table.");
			Handler han = new Handler(Query.Select, field, obj);
			for(int i = 0; i < Data.size(); i++) {
				new RowThread( this, han ).run();
			}
			while(han.Counter < this.Data.size()){};
			System.out.println("Finished multi threading a table.");
			return new SubTable(this, Field + "=" + value,han.Ret);
		}
		
		// table constructors
		public Table(String name, String[] fields, Class<?>[] types) {
			if(Load(name)) { return; }
			Name = name;
			Meta = new Meta(fields, types);
			Data = new ArrayList<char[]>();
			AddChunk();
			Size = 0;
			Save();
		}
		
		public Table(String name, String[] fields, Class<?>[] types, ArrayList<char[]> data){
			this(name,new Meta(fields, types),data);
		}
		
		public Table(String name, Meta meta, ArrayList<char[]> data){
			Name = name;
			Meta = meta;
			Data = data;
		}
	}
	
	public class SubTable{
		public String Name = "";
		public Table Parent = null;
		public String Mod = "";
		
		public ArrayList<Row> Rows = null;
		
		public String toString(){
			StringBuilder ret = new StringBuilder();
			for(Row r : Rows){
				ret.append(r.toString());
			}
			return ret.toString();
		}
		
		public String toHTML() {
			return toHTML(0,this.Size());
		}
		
		public String toHTML(int start, int end) {
			
			if(end > Rows.size()){end = Rows.size();}
			if(start < 0){start = 0;}
			if(start > end){end = start;}
			
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			SB.append( "<table>" + SP );
			SB.append( Parent.Meta.toHTML() );
			for( int i = start; i < end; i++) {
				// TODO needs to work out rows.
				SB.append(Rows.get(i).toHTML());
			}
			SB.append("</table>");
			return SB.toString();
		}
		
		public int Size(){
			return Rows.size();
		}
		
		public SubTable(Table par, String mod, ArrayList<Row> data){
			Name = par.Name + "-" + mod;
			Parent = par;
			Mod = mod;
			
			Rows = data;
		}
	}
	
	public class Row{
		// public values
		public int Id = -1;
		public int Hash = -1;
		
		private Table Par = null;
		private long start = 0;
		
		// public functions	
		public String toString() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "Row:" + Id + SP );
			for(int i = 0; i < Par.Meta.Type.length; i++ ) {
				String cur = GetString(i);
				SB.append( cur + SP );
			}
			return SB.toString();
		}
		
		public String toHTML() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "<tr>" + SP );
			SB.append("<td>" + Id + "</td>" + SP );
			for(int i = 0; i < Par.Meta.Type.length; i++ ) {				
				String cur = GetString(i);
				SB.append("<td>" + cur + "</td>" + SP );
			}
			SB.append( "</tr>" + SP );
			return SB.toString();
		}
		
		public void write(Writer out) throws IOException {
			out.write( "Row:" + System.lineSeparator() );
			for(int i = 0; i < Par.Meta.Type.length; i++ ) {
				out.write(GetString(i));
				out.write( System.lineSeparator() );
			}
		}
		
		public Boolean Set(String field, Object value) {
			return Set(Par.Meta.Field.indexOf(field),value);
		}
		
		public Boolean Set(int field, Object value) {
			if(field > -1 && field < Par.Meta.Type.length){
				Class<?> CC = Par.Meta.Type[field];
				if(CC == INT.class) {
					INT.toData(Par.Data, start + Par.Meta.Offset[field],(int)value);
				} else if(CC == TEXT.class) {
					TEXT.toData(Par.Data, start + Par.Meta.Offset[field],(char[])value);
				} else if(CC == BOOL.class) {
					BOOL.toData(Par.Data, start + Par.Meta.Offset[field],(boolean)value);
				}
			}
			return true;
		}
		// TODO
		public Boolean Set(char[] value) {
			/*
			if(Par.Meta.Size != value.length) { return false; }
			System.arraycopy(value, 0, Par.Data, start, value.length);
			*/
			return false;
		}
		
		public Boolean Set(String[] field, Object[] value) {
			int end = 0;
			if(field.length > value.length) { end = field.length; } else { end = value.length; }
			for(int i = 0; i < end; i++ ) {
				if(!Set(field[i],value[i])){ return false; }
			}
			return true;
		}
		
		public Object Get(String field) {
			return Get(Par.Meta.Field(field));
		}
		
		public Object Get(int field) {
			if(field > -1 && field < Par.Meta.Type.length){
				Class<?> CC = Par.Meta.Type[field];
				if(CC == INT.class) {
					return INT.fromData(Par.Data, start + Par.Meta.Offset[field]);
				} else if(CC == TEXT.class) {
					return TEXT.fromData(Par.Data, start + Par.Meta.Offset[field]);
				} else if(CC == BOOL.class) {
					return BOOL.fromData(Par.Data, start + Par.Meta.Offset[field]);
				}
			}
			return null;
		}
		
		public String GetString(int field){
			if(field > -1 && field < Par.Meta.Type.length){
				Class<?> CC = Par.Meta.Type[field];
				if(CC == INT.class) {
					return INT.toString(INT.fromData(Par.Data, start + Par.Meta.Offset[field]));
				} else if(CC == TEXT.class) {
					return TEXT.toString(TEXT.fromData(Par.Data, start + Par.Meta.Offset[field]));
				} else if(CC == BOOL.class) {
					return BOOL.toString(BOOL.fromData(Par.Data, start + Par.Meta.Offset[field]));
				}
			}
			return "";
		}
		
		public Object[] Get() {
			Object[] ret = new Object[ Par.Meta.Type.length ];
			for(int i=0;i<ret.length;i++){
				ret[i] = Get(i);
			}
			return ret;
		}
		
		// Row COnstructors
		public Row(Table par, int id) {
			Par = par;
			Id = id;
			start = ((long)id) * Par.Meta.Size;
		}
	}
	
	public class Meta{
		
		public List<String> Field = null;
		public Class<?>[] Type = null;
		public int[] Offset = null;
		public int Size = 0;
		
		public String toString() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "Meta:" + SP );
			for(int i = 0; i < Field.size(); i++ ) {
				SB.append( Field.get(i) + ":" + Type[i].toString() + SP );
			}
			
			return SB.toString();
		}
		
		public String toHTML() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "<tr>" + SP );
			SB.append( "<th>" + "ID" + "</th>" + SP );
			for(int i = 0; i < Field.size(); i++ ) {
				SB.append( "<th>" + Field.get(i) + "</th>" + SP );
			}
			SB.append( "</tr>" + SP );
			return SB.toString();
		}
		
		public void write(Writer out) throws IOException {
			out.write( "Meta:" + System.lineSeparator() );
			
			for(int i = 0; i < Field.size(); i++ ) {
				out.write( Field.get(i) + ":" + Type[i].toString() + System.lineSeparator() );
			}
		}
		
		public int Field(String field) {
			return Field.indexOf(field);
		}
		
		public void getSize(){
			int ret = 0;
			Offset = new int[ Type.length ];
			for(int i=0;i<Type.length;i++){
				Class<?> CC = Type[i];
				Offset[i] = ret;
				if(CC == INT.class) {
					ret += INT.s;
				} else if(CC == TEXT.class) {
					ret += TEXT.s;
				} else if(CC == BOOL.class) {
					ret += BOOL.s;
				}
			}
			Size = ret;
		}
		
		// Meta constructors
		public Meta(String[] field, Class<?>[] type) {
			Field = Arrays.asList(field);
			Type = type;
			getSize();
		}
	}

	// DataBase Types
	static public class BOOL {
		public static final Class<?> Type = boolean.class;
		public static final int s = 1;
		
		public static boolean fromString(String S) {
			if(S.startsWith("1")) {
				return true;
			}
			return false;
		}
		
		public static boolean fromChunk(char[] Data, int start){
			if(Data[start] == '1') {
				return true;
			}
			return false;
		}
		
		public static boolean fromData(ArrayList<char[]> Data, long start) {
			int chunksize = Data.get(0).length;
			int chunk = (int) (start/chunksize);
			int localpos = (int) (start % chunksize);
			
			if(Data.get(chunk)[localpos] == '1') {
				return true;
			}
			return false;
		}
		
		public static char[] toData( boolean V) {
			if(V){
				return new char[]{'1'};
			} else {
				return new char[]{'0'};
			}
		}
		
		public static void toData(ArrayList<char[]> Data, long start, boolean V) {
			int chunksize = Data.get(0).length;
			int chunk = (int) (start/chunksize);
			int localpos = (int) (start % chunksize);
			
			if(V){
				Data.get(chunk)[localpos] = '1';
			} else {
				Data.get(chunk)[localpos] = '0';
			}
		}
		
		public static String toString(boolean V) {
			if(V) {
				return "1";
			}
			return "0";
		}
		
		public static boolean equals(Object a, Object b) {
			if( a.getClass() == Type && b.getClass() == Type ) {
				return ( a == b );
			}
			return false;
		}
	}
	
	static public class INT {
		public static final Class<?> Type = int.class;
		public static final int s = 2;
		
		public static int fromString(String S) {
			int ret = 0;
			try {
				ret = Integer.parseInt(S);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		public static int fromChunk(char[] Data, int start){
			int ret = ((int)Data[start] << 16) | ((int)Data[start+1]);
			return ret;
		}
		
		public static int fromData(ArrayList<char[]> Data, long start) {
			int chunksize = Data.get(0).length;
			int chunk = (int) (start/chunksize);
			int localpos = (int) (start % chunksize);
			
			int ret = ((int)Data.get(chunk)[localpos] << 16) | ((int)Data.get(chunk)[localpos+1]);
			return ret;
		}
		
		public static char[] toData(int V) {
			char[] ret = new char[]{(char)(V >>> 16),(char)(V)};
			return ret;
		}
		
		public static void toData(ArrayList<char[]> Data,long start, int V) {
			int chunksize = Data.get(0).length;
			int chunk = (int) (start/chunksize);
			int localpos = (int) (start % chunksize);
			
			Data.get(chunk)[localpos] = (char)(V >>> 16);
			Data.get(chunk)[localpos+1] = (char)(V);
		}
		
		public static String toString(int V) {
			return V+"";
		}
		
		public static boolean equals(Object a, Object b) {
			if( a.getClass() == Type && b.getClass() == Type ) {
				return ((int)a) == ((int)b);
			}
			return false;
		}
	}
	
	static public class TEXT {
		public static final Class<?> Type = char[].class;
		public static final int s = 15; // temporary max size for the current tests
		
		private static final char NULL = (char)0;
		
		public static char[] fromString(String S) {
			char[] ret = new char[s];
			if(S.length() == 0){return ret;}
			System.arraycopy(S.toCharArray(), 0, ret, 0, S.length());
			return ret;
		}
		
		public static String toString(char[] V) {
			if(V == null || V.length == 0) { return "";}
			StringBuilder ret = new StringBuilder();
			for(char c : V){
				if(c == NULL){
					break;
				}
				ret.append(c);
			}
			return ret.toString();
		}
		
		public static char[] fromChunk(char[] Data,int start){
			return Arrays.copyOfRange(Data, start, start+s);
		}
		
		public static boolean compareChunk(char[] Data,int start, Object b){
			if(b.getClass() == Type){
				char[] bb = (char[])b;
				for(int i=0; i<s;i++){
					if(bb[i] != Data[start+i]){return false;}
				}
				return true;
			}
			return false;
		}
		
		public static char[] fromData(ArrayList<char[]> Data, long start) {
			int chunksize = Data.get(0).length;
			int chunk = (int) (start/chunksize);
			int localpos = (int) (start % chunksize);
			
			return Arrays.copyOfRange(Data.get(chunk), localpos, localpos+s);
		}
		
		public static char[] toData(String V) {
			return fromString(V);
		}
		
		public static void toData(ArrayList<char[]> Data, long start, String V) {
					
			char[] A = V.toCharArray();
			toData(Data,start,A);
		}
		
		public static void toData(ArrayList<char[]> Data, long start, char[] V) {
			int chunksize = Data.get(0).length;
			int chunk = (int) (start/chunksize);
			int localpos = (int) (start % chunksize);
			try{
				System.arraycopy(V, 0, Data.get(chunk), localpos, V.length);
			} catch(Exception e){
				System.out.println(chunk + " => " + localpos);
				e.printStackTrace();
			}
		}
		
		public static boolean equals(Object a, Object b) {
			if( a.getClass() == Type && b.getClass() == Type ) {
				char[] aa = (char[])a;
				char[] bb = (char[])b;
				for(int i = 0; i < aa.length; i++) {
					if(aa[i] != bb[i]){return false;}
				}
				return true;
			}
			return false;
		}
	}
	
	public enum Query {
		Select,
		Sort
	}
	
	// Queuery Thread handeling
	private class Handler {
		public Query Action;
		private int[] Field;
		private Object[] Val;
		
		public int Counter;
		public ArrayList<Row> Ret;
		
		public Handler( Query a, int[] field, Object[] value ) {
			Action = a;
			Field = field;
			Val = value;
			
			Counter = 0;
			Ret = new ArrayList<Row>();
		}
	}
	// Queuery Threads
	private class RowThread extends Thread {
		
		// Public Values
		
		// Private Values
		private Table Table;
		
		private int[] Field;
		private Object[] Val;
		
		private Handler Han;
		
		
		public RowThread( Table table, Handler counter) {
			Table = table;		
			Han = counter;
			
			Field = Han.Field;
			Val = Han.Val;
        }

        public void run() {
        	if(Han.Action == Query.Select) {
        		Select();
        	} else if( Han.Action == Query.Sort) {
        		Sort();
        	}
        	
        	this.interrupt();
        }
        
        private void Select() {
        	int rowSize = Table.Meta.Size;
        	int cur = Han.Counter;
        	boolean curc = true;
        	Class<?>[] FC = new Class<?>[Field.length];
        	for( int i = 0; i < Field.length; i++ ) {
        		FC[i] = Table.Meta.Type[Field[i]];
        	}
        	
        	while(Han.Counter < Table.Data.size())
        	{
        		char[] chunk = Table.Data.get(cur);
        		for(int x=0;x<chunk.length;x+=rowSize){
        			curc = true;
        			for( int i = 0; i < Field.length; i++ ) {
    	    			if( FC[i] == INT.class) {
    	    				int curval = INT.fromChunk(chunk, x + Table.Meta.Offset[i]);
    	    				if( !INT.equals(curval, Val[i]) ) {
    	                		curc = false;
    	                		break;
    	                	}
    	    			} else if( FC[i] == TEXT.class ) {
    	    				if( !TEXT.compareChunk(chunk, x + Table.Meta.Offset[i], Val[i]) ) {
    	    					curc = false;
    	                		break;
    						}
    	    			} else if( FC[i] == BOOL.class ) {
    	    				boolean curval = BOOL.fromChunk(chunk, x + Table.Meta.Offset[i]);
    	    				if( !BOOL.equals(curval, Val[i]) ) {
    	    					curc = false;
    	                		break;
    	                	}
    	    			}
            		}
        			if(curc) {
            			Han.Ret.add(Table.Get((cur*Table.chunkSize) + (x/rowSize)));
            		}
        		}
        		
        		cur = Han.Counter;
        		if(cur == Table.Data.size()){break;}
            	Han.Counter++;
        	}
        }
        
        private void Sort() {
        	// TODO
        }
        
        protected void finalize() throws Throwable {
        	Table = null;
        	Val = null;
        	Han = null;
        	super.finalize();
        }
    }

	// Page Constructors
	public DataBase(HOME Home) {
		H = Home;
		
		Auth = new Table(
				"Users", 
				new String[]{ "User", "Pass", "Salt", "Auth" },
				new Class<?>[]{TEXT.class, TEXT.class, TEXT.class, TEXT.class}
		);
		tables = new ArrayList<Table>();
	}
}

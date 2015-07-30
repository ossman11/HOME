package closset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entrance.HOME;

/**
 * @author Bob den Os
 * HOME DataBase used by HOME devices to acces and store datasets
 * The HOME DataBases are stored on the local storage solutions (default: HOME system internal storage)
 */
public class DataBase {
	// Public Values
	public Table Auth = null;
	
	// Private Values
	private HOME H = null;
	
	// Public Functions
	public Table Find( String table, String Field, String Val) {
		return Find(table,new String[]{Field}, new String[]{Val});
	}
	
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
		    				// System.out.println("Int");
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
	
	// Private Functions
	
	// Public Classes
	public class Table {
		public String Name = null;
		public Meta Meta = null;
		public ArrayList<Row> Data = null;
		
		// storage
		public Boolean Save(){
			if(Meta == null || Data == null || Name == null) { return false; }
			Writer out = H.folder.SaveLocalFile( "db/" + Name + ".db" );
			try {
				// header
				out.write( "Table:" + Name + System.lineSeparator() );
				Meta.write( out );
				// Data
				for( int i = 0; i < Data.size(); i++) {
					Data.get(i).write( out );
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
				
				Data = new ArrayList<Row>(); 
				
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
							if(S == null || S.equals("Row:")) { break; }
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
					if(S != null && S.equals("Row:")) {
						ArrayList<Object> tmpRow = new ArrayList<Object>();
						for(int i = 0;(d.ready() && i < F); i++) {
							S = d.readLine();
							if( Meta.Type[i] == INT.class) {
								tmpRow.add(INT.fromString(S));
							} else if( Meta.Type[i] == TEXT.class ) {
								tmpRow.add(TEXT.fromString(S));
							}
						}
						Object[] conRow = new Object[F];
						conRow = tmpRow.toArray(conRow);
						Data.add(new Row(this, Data.size(), conRow));
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
			for( int i = 0; i < Data.size(); i++) {
				SB.append( Data.get(i).toString() );
			}
			return SB.toString();
		}
		
		public String toHTML() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			SB.append( "<table>" + SP );
			SB.append( Meta.toHTML() );
			for( int i = 0; i < Data.size(); i++) {
				SB.append( Data.get(i).toHTML() );
			}
			SB.append("</table>");
			return SB.toString();
		}
		
		// edit
		public void Add(Object[] value) {
			Data.add( new Row( this, Data.size(), value ) );
		}
		
		public void Add(String[] value) {
			if(value.length == Meta.Field.size()) {
				Object[] tmp = new Object[value.length];
				
				for(int i = 0; i < value.length; i++ ) {
					if( Meta.Type[i] == INT.class) {
						tmp[i] = INT.fromString(value[i]);
					} else if( Meta.Type[i] == TEXT.class ) {
						tmp[i] = TEXT.fromString(value[i]);
					} else if( Meta.Type[i] == BOOL.class ) {
						tmp[i] = BOOL.fromString(value[i]);
					}
				}
				Row newRow = new Row( this, Data.size(), tmp );
				Data.add( newRow );
				Writer out = H.folder.SaveLocalFile( "db/" + Name + ".db" , false);
				if(out != null) {
					try {
						newRow.write(out);
					    out.flush();
					    out.close();
					} catch (IOException e) {
						System.out.println("table " + Name + " Not Saved.");
					}
				} else {
					System.out.println("table " + Name + " Not Saved.");
				}
			}
			
		}
		
		public Row Get(int id) {
			if(id > -1 && id < Data.size()) {
				return Data.get(id);
			}
			return null;
		}
		
		public Boolean Get(Row row) {
			if(Data.indexOf(row) > -1){ return true; }
			return false;
		}
		
		// dump
		public void Dispose() {
			Name = null;
			Meta = null;
			Data.clear();
			Data = null;
			System.gc();
		}
		
		// process
		public List<Row> Find(String Field, String value ) {
			// Preperation
			int[] field = new int[]{ Meta.Field(Field) };
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
			Handler han = new Handler(Query.Select, field, obj);
			for(int i = 0; i < H.Threads; i++) {
				new RowThread( this, han ).run();
			}
			while(han.Counter < Data.size()){};
			return han.Ret;
		}
		
		// table constructors
		public Table(String name, String[] fields, Class<?>[] types) {
			if(Load(name)) { return; }
			Name = name;
			Meta = new Meta(fields, types);
			Data = new ArrayList<Row>();
		}
		
		public Table(String name) {
			Name = name;
			Data = new ArrayList<Row>();
		}
	}
	
	public class Row{
		// public values
		public int Id = -1;
		public int Hash = -1;
		
		// public Table Parent = null;
		// private values
		private Object[] Data = null;
		private boolean Lock = false;
		private Meta Meta = null;
		
		// public functions
		public String toString() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "Row:" + SP );
			for(int i = 0; i < Data.length; i++ ) {
				Class<?> CC = Meta.Type[i];
				String cur = "";
				if(CC == INT.class) {
					cur = INT.toString( (int)Data[i] );
				} else if(CC == TEXT.class) {
					cur = TEXT.toString( (char[])Data[i] );
				} else if(CC == BOOL.class) {
					cur = BOOL.toString( (boolean)Data[i] );
				}
				SB.append( cur + SP );
			}
			return SB.toString();
		}
		
		public String toHTML() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "<tr>" + SP );
			for(int i = 0; i < Data.length; i++ ) {
				Class<?> CC = Meta.Type[i];
				String cur = "";
				if(CC == INT.class) {
					cur = INT.toString( (int)Data[i] );
				} else if(CC == TEXT.class) {
					cur = TEXT.toString( (char[])Data[i] );
				} else if(CC == BOOL.class) {
					cur = BOOL.toString( (boolean)Data[i] );
				}
				SB.append("<td>" + cur + "</td>" + SP );
			}
			SB.append( "</tr>" + SP );
			return SB.toString();
		}
		
		public void write(Writer out) throws IOException {
			out.write( "Row:" + System.lineSeparator() );
			for(int i = 0; i < Data.length; i++ ) {
				Class<?> CC = Meta.Type[i];
				if(CC == INT.class) {
					out.write( (int)Data[i] + "" );
				} else if(CC == TEXT.class) {
					out.write( (char[])Data[i] );
				} else if(CC == BOOL.class) {
					out.write( (char[])Data[i] );
				}
				out.write( System.lineSeparator() );
			}
		}
		
		public Boolean Set(String field, Object value) {
			if(Lock) { return false; }
			Lock();
			Data[Meta.Field(field)] = value;
			UnLock();
			Hash();
			return true;
		}
		
		public Boolean Set(Object[] value) {
			if(Lock) { return false; }
			Lock();
			if(Meta.Type.length != value.length) { return false; }
			for(int i = 0; i < value.length; i++) {
				if(Meta.Type[i] != value[i].getClass()) { return false; }
			}
			Data = value;
			Hash();
			UnLock();
			return true;
		}
		
		public Boolean Set(String[] field, Object[] value) {
			if(Lock) { return false; }
			int end = 0;
			if(field.length > value.length) { end = field.length; } else { end = value.length; }
			for(int i = 0; i < end; i++ ) {
				while(Lock) { };
				if(!Set(field[i],value[i])){ return false; }
			}
			return true;
		}
		
		public Object Get(String field) {
			while(Lock) { };
			return Data[Meta.Field(field)];
		}
		
		public Object Get(int field) {
			while(Lock) { };
			if(field < Data.length){
				return Data[field];
			}
			return null;
		}
		
		public Object[] Get() {
			while(Lock) { };
			return Data;
		}
		
		// private functions
		private void Hash(){
			Hash = Data.hashCode();
		}
		
		private void Lock() {
			Lock = true;
		}
		
		private void UnLock() {
			Lock = false;
		}
		
		// Row COnstructors
		public Row(Table par, int id, Object[] dat) {
			Meta = par.Meta;
			Id = id;
			Data = dat;
			Hash();
		}
		
		// private functions
		private void writeObject(ObjectOutputStream s) throws IOException {
			s.writeObject(Data);
		}
	}
	
	public class Meta{
		
		public List<String> Field = null;
		public Class<?>[] Type = null;
		
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
		
		// Meta constructors
		public Meta(String[] field, Class<?>[] type) {
			Field = Arrays.asList(field);
			Type = type;
		}
	}

	// DataBase Types
	static public class BOOL {
		public static final Class<?> Type = boolean.class;
		
		public static boolean fromString(String S) {
			if(S.startsWith("1")) {
				return true;
			}
			return false;
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
		
		public static int fromString(String S) {
			int ret = 0;
			try {
				ret = Integer.parseInt(S);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			return ret;
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
		
		public static char[] fromString(String S) {
			if(S.length() == 0){return new char[0];}
			char[] ret = new char[S.length()];
			S.getChars(0, S.length(), ret, 0);
			return ret;
		}
		
		public static String toString(char[] V) {
			if(V == null || V.length == 0) { return "";}
			return new String(V);
		}
		
		public static boolean equals(Object a, Object b) {
			if( a.getClass() == Type && b.getClass() == Type ) {
				char[] aa = (char[])a;
				char[] bb = (char[])b;
				if(aa.length == bb.length) {
					for(int i = 0; i < aa.length; i++) {
						if(aa[i] != bb[i]){return false;}
					}
					return true;
				}
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
        	int cur = Han.Counter;
        	boolean curc = true;
        	Class<?>[] FC = new Class<?>[Field.length];
        	for( int i = 0; i < Field.length; i++ ) {
        		FC[i] = Table.Meta.Type[Field[i]];
        	}
        	
        	while(Han.Counter < Table.Data.size())
        	{
        		curc = true;
        		for( int i = 0; i < Field.length; i++ ) {
	    			if( FC[i] == INT.class) {
	    				if( !INT.equals(Table.Get(cur).Get(Field[i]), Val[i]) ) {
	                		curc = false;
	                		break;
	                	}
	    			} else if( FC[i] == TEXT.class ) {
	    				if( !TEXT.equals(Table.Get(cur).Get(Field[i]), Val[i]) ) {
	    					curc = false;
	                		break;
						}
	    			} else if( FC[i] == BOOL.class ) {
	    				if( !BOOL.equals(Table.Get(cur).Get(Field[i]), Val[i]) ) {
	    					curc = false;
	                		break;
	                	}
	    			}
        		}
        		
        		if(curc) {
        			Han.Ret.add(Table.Get(cur));
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
				new String[]{ "User", "Pass", "Salt", "Auth", },
				new Class<?>[]{TEXT.class, TEXT.class, TEXT.class, TEXT.class}
		);
		Auth.Save();
	}
}

package closset;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import entrance.HOME;

/**
 * @author Bob den Os
 * HOME DataBase used by HOME devices to acces and store datasets
 * The HOME DataBases are stored on the local storage solutions (default: HOME system internal storage)
 */
public class DataBase {
	// Public Values
	
	// Private Values
	private HOME H = null;
	
	// Public Functions

	// Private Functions
	
	// Public Classes
	public class Table {
		public String Name = null;
		
		public Meta Meta = null;
		
		public ArrayList<Row> Data = null;
		private ArrayList<Boolean> Lock = null;
		
		// converter to String
		public Boolean Save(){
			if(Meta == null || Data == null || Name == null) { return false; }
			H.folder.SaveLocalFile(
					"db/" + Name + ".db",
					toString().getBytes( Charset.forName( "UTF-8" ) )
					);
			return true;
		}
		
		public Boolean Load(String name) {
			Iterator<String> d = H.folder.LoadLocalFileLines("db/" + name + ".db");
			if(d == null) {return false;}
			int F = 0;
			String S = "";
			
			Data = new ArrayList<Row>(); 
			
			while(d.hasNext()){
				S = d.next();
				if(S.startsWith("Table:")) {
					Name = S.substring(6);
				}
				if(S.equals("Meta:")) {
					ArrayList<String> tmpField = new ArrayList<String>();
					ArrayList<Class<?>> tmpClass = new ArrayList<Class<?>>();
					while(d.hasNext()) {
						S = d.next();
						if(S.equals("Row:")) { break; }
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
				}
				if(S.equals("Row:")) {
					ArrayList<Object> tmpRow = new ArrayList<Object>();
					for(int i = 0;(d.hasNext() && i < F); i++) {
						S = d.next();
						if( Meta.Type[i] == Integer.class) {
							tmpRow.add(Integer.parseInt(S));
						} else if( Meta.Type[i] == String.class ) {
							tmpRow.add(S);
						}
					}
					Object[] conRow = new Object[F];
					conRow = tmpRow.toArray(conRow);
					Data.add(new Row(this, Data.size(), conRow));
				}
			}
			return true;
		}
		
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
		
		// locking functions only for child rows
		public Boolean isLocked(Row child) {
			return Lock.get(child.Id);
		}
		
		public void Lock(Row child){
			if(child.Parent == this) {
				Lock.set(child.Id, true);
			}
		}
		
		public void UnLock(Row child){
			if(child.Parent == this) {
				Lock.set(child.Id, false);
			}
		}
		
		public void Add(Object[] value) {
			Lock.add( false );
			Data.add( new Row( this, Data.size(), value ) );
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
		
		// table constructors
		public Table(String name, String[] fields, Class<?>[] types) {
			if(Load(name)) { return; }
			Name = name;
			Lock = new ArrayList<Boolean>();
			Meta = new Meta(fields, types);
			Data = new ArrayList<Row>();
		}
		
		public Table(String name) {
			Load(name);
		}
	}
	
	public class Row{
		// public values
		public int Id = -1;
		public int Hash = -1;
		public Table Parent = null;
		// private values
		private Object[] Data = null;
		
		// public functions
		public String toString() {
			String SP = System.lineSeparator();
			StringBuffer SB = new StringBuffer();
			
			SB.append( "Row:" + SP );
			for(int i = 0; i < Data.length; i++ ) {
				SB.append( Data[i].toString() + SP );
			}
			return SB.toString();
		}
		
		public Boolean Set(String field, Object value) {
			if(Parent.isLocked(this)) { return false; }
			Parent.Lock(this);
			Data[Parent.Meta.Field(field)] = value;
			Parent.UnLock(this);
			Hash();
			return true;
		}
		
		public Boolean Set(Object[] value) {
			if(Parent.isLocked(this)) { return false; }
			Parent.Lock(this);
			if(Parent.Meta.Type.length != value.length) { return false; }
			for(int i = 0; i < value.length; i++) {
				if(Parent.Meta.Type[i] != value[i].getClass()) { return false; }
			}
			Data = value;
			Hash();
			Parent.UnLock(this);
			return true;
		}
		
		public Boolean Set(String[] field, Object[] value) {
			if(Parent.isLocked(this)) { return false; }
			int end = 0;
			if(field.length > value.length) { end = field.length; } else { end = value.length; }
			for(int i = 0; i < end; i++ ) {
				while(Parent.isLocked(this)) { };
				if(!Set(field[i],value[i])){ return false; }
			}
			return true;
		}
		
		public Object Get(String field) {
			while(Parent.isLocked(this)) { };
			return Data[Parent.Meta.Field(field)];
		}
		
		public Object Get(int field) {
			while(Parent.isLocked(this)) { };
			if(field < Data.length){
				return Data[field];
			}
			return null;
		}
		
		public Object[] Get() {
			while(Parent.isLocked(this)) { };
			return Data;
		}
		
		// private functions
		private void Hash(){
			Hash = Data.hashCode();
		}
		
		// Row COnstructors
		public Row(Table par, int id, Object[] dat) {
			Parent = par;
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
		
		public int Field(String field) {
			return Field.indexOf(field);
		}
		
		// Meta constructors
		public Meta(String[] field, Class<?>[] type) {
			Field = Arrays.asList(field);
			Type = type;
		}
	}
	
	public enum DropOptions {
		
	}
	
	public enum Index {
		
	}
	
	// Private Classes
	
	// Page Constructors
	public DataBase(HOME Home) {
		H = Home;
	}
}

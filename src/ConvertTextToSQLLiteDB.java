import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//references:
//http://www.sqlitetutorial.net/sqlite-java/create-database/
//http://www.sqlitetutorial.net/sqlite-java/

public class ConvertTextToSQLLiteDB {
//CREATE TABLE `Masechet` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, `name` TEXT NOT NULL UNIQUE )
//CREATE TABLE "daf" ( `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `masechetId` INTEGER NOT NULL, `dafNumber` INTEGER NOT NULL, `side` INTEGER NOT NULL, `text` TEXT NOT NULL, FOREIGN KEY(`masechetId`) REFERENCES Masechet(id) )
	
	private static String mUrl = "jdbc:sqlite:";
	private static String mFileName="";
	/**
	 *
	 * @author sqlitetutorial.net
	 */
	
	    /**
	     * Connect to a sample database
	     *
	     * @param fileName the database file name
	     */
	    public static void createNewDatabase(String fileName) {
	 
	    	mFileName=fileName;
	        mUrl += fileName;
	 
	        try (Connection conn = DriverManager.getConnection(mUrl)) {
	            if (conn != null) {
	                DatabaseMetaData meta = conn.getMetaData();
	                System.out.println("The driver name is " + meta.getDriverName());
	                System.out.println("A new database has been created.");
	            }
	 
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }
	    }
	    
	    public static void createNewTable() {
	        // SQL statement for creating new tables
	        String sqlTableMasechet = "CREATE TABLE IF NOT EXISTS Masechet ("
	                + "	id integer PRIMARY KEY AUTOINCREMENT UNIQUE,"
	                + "	name text NOT NULL UNIQUE"
	                + ");";
	        //CREATE TABLE IF NOT EXISTS tabel1(id INT PRIMARY KEY, text TEXT);
	      
	        
	        String sqlTableDaf = "CREATE TABLE IF NOT EXISTS Daf("
	                + "	id integer PRIMARY KEY AUTOINCREMENT UNIQUE,"
	                + "	masechetId INTEGER NOT NULL,"
	                +" dafNumber INTEGER NOT NULL,"
	                +" side INTEGER NOT NULL,"
	                +" text TEXT NOT NULL,"
	                +" FOREIGN KEY(masechetId) REFERENCES Masechet(id)"
	                + ");";
	        
	        
	        try (Connection conn = DriverManager.getConnection(mUrl);
	        		
	                Statement stmt = conn.createStatement()) {
	            // create a new table
	        	stmt.execute(sqlTableMasechet);
	            stmt.execute(sqlTableDaf);
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	            System.out.println("mURL= "+mUrl);
	        }
	    }
	 
	    /**
	     * @param args the command line arguments
	     */
	    public static void main(String[] args) {
	        createNewDatabase("test.db");
	        createNewTable();
	    }
	}



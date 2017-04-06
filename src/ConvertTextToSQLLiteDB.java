import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

//references:
//http://www.sqlitetutorial.net/sqlite-java/create-database/
//http://www.sqlitetutorial.net/sqlite-java/

public class ConvertTextToSQLLiteDB {
	// CREATE TABLE `Masechet` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
	// `name` TEXT NOT NULL UNIQUE )
	// CREATE TABLE "daf" ( `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT
	// UNIQUE, `masechetId` INTEGER NOT NULL, `dafNumber` INTEGER NOT NULL,
	// `side` INTEGER NOT NULL, `text` TEXT NOT NULL, FOREIGN KEY(`masechetId`)
	// REFERENCES Masechet(id) )

	private static String mUrl = "jdbc:sqlite:";
	private static String mFileName = "";
	private static Set<String> mMasechtoth = new TreeSet<>();

	private static final String DBFILENAME = "test.db";
	// queries for new tables
	private static final String SQLTABLEMASECHET = "CREATE TABLE IF NOT EXISTS Masechet ("
			+ "	id integer PRIMARY KEY AUTOINCREMENT UNIQUE," + "	name text NOT NULL UNIQUE" + ");";

	private static final String SQLTABLEDAF = "CREATE TABLE IF NOT EXISTS Daf("
			+ "	id integer PRIMARY KEY AUTOINCREMENT UNIQUE," + "	masechetId INTEGER NOT NULL,"
			+ " dafNumber INTEGER NOT NULL," + " side INTEGER NOT NULL," + " text TEXT NOT NULL,"
			+ " FOREIGN KEY(masechetId) REFERENCES Masechet(id)" + ");";

	/**
	 *
	 * @author Gershon Lehrer
	 */

	/**
	 * Connect to a sample database
	 *
	 * @param fileName
	 *            the database file name
	 */
	public static void createNewDatabase(String fileName) {

		mFileName = fileName;
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

	public static ResultSet runQuery(String query) {
		ResultSet rs = null;
		try (Connection conn = DriverManager.getConnection(mUrl);

				Statement stmt = conn.createStatement()) {

			rs = stmt.executeQuery(query);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("mURL= " + mUrl);
			System.out.println("query= " + query);
		}
		return rs;
	}

	public static void fillContentsInDB() {
		for (String s : mMasechtoth) {
			String addMasecheth = "INSERT INTO Masechet (name) VALUES ('" + s + "');";
			runQuery(addMasecheth);
		}
	}

	public static void getMasechtothFromExternalFile(String file) {
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String regel;

			boolean addMasechet = true;
			boolean firstLine = true;
			String content = "";
			String currentMasechet = "";
			String currentDaf = "";
			String currentAmmud = "";
			while ((regel = r.readLine()) != null) {
				if (!regel.isEmpty()) {
					if (firstLine) {
						String parts[] = regel.split(" ");
						currentMasechet = parts[parts.length - 3];
						currentDaf = parts[parts.length - 2];
						currentAmmud = parts[parts.length - 1];
						firstLine = false;
					} else if (!firstLine) {
						content += regel;
					}

				} else {
					// we finished one ammud, now reset firstLine to true
					// for the next ammud
					firstLine = true;
					// we initialize the rest also
					content = "";
					currentMasechet = "";
					currentDaf = "";
					currentAmmud = "";
				}

				// TODO: check how there should be a better way. I am doing this
				// check because
				// else I may find sometimes empty daf, ammud, content, etc.
				if (content.length() > 0) {
					System.out.println("currentMasechet: " + currentMasechet);
					System.out.println("content: " + content);
					System.out.println("Daf: " + currentDaf);
					System.out.println("Ammud: " + currentAmmud);
					fillContentsInDB();
					String getMasechetIdSQL = "SELECT Masechet.id from Masechet where Masechet.name='" + currentMasechet + "';";
					ResultSet rs = runQuery(getMasechetIdSQL);
					int currentMasechetId=0;
					while (rs.next()) {
						currentMasechetId=rs.getInt("id");
					}
					System.out.println(currentMasechetId);
					String addAmmud = "INSERT INTO Daf (masechetId, dafNumber, side,text) VALUES " + "('"
							+ currentMasechetId + "," + currentDaf + "," + currentAmmud + "," + content + "');";
					// runQuery(addAmmud);
				}
			}

		} catch (IOException|SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} 
	}

	private static void deleteFile(String file) {
		try {
			Files.delete(Paths.get(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		createNewDatabase(DBFILENAME);
		runQuery(SQLTABLEMASECHET);
		runQuery(SQLTABLEDAF);
		getMasechtothFromExternalFile("Export.txt");
		
		//deleteFile(DBFILENAME);
	}

}

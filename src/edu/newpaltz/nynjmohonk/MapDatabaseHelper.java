package edu.newpaltz.nynjmohonk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A simple SQLite helper class that copies, connects to and reads from our database of maps.
 *
 */
public class MapDatabaseHelper extends SQLiteOpenHelper {
	private static String DB_PATH = ""; 
	private static final String DB_NAME = "nynj.sqlite";
	private final Context myContext;
	private SQLiteDatabase myDatabase;
	
	private static MapDatabaseHelper myDBConnection;
	
	/**
	 * Generate a MapDatabaseHelper instance within the context of this application. Also sets the path
	 * to the SQLite database file based on the application context
	 * @param context The current application context
	 */
	public MapDatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		DB_PATH = "/data/data/" + context.getApplicationContext().getPackageName() + "/databases/";
	}

	/**
	 * Gets an instance of MapDatabaseHelper which will be connected to our SQLite database
	 * @param context The current application context
	 * @return A connection to the database
	 */
	public static synchronized MapDatabaseHelper getDBInstance(Context context) {
		if (myDBConnection == null) {
			myDBConnection = new MapDatabaseHelper(context);
		}
		return myDBConnection;
	}
	
	/**
	 * Copies the database to the phone, if needed. If there are any errors in copying the database, such as writing
	 * to the phone, an IOException is thrown.
	 * @throws IOException
	 */
	public void createDatabase() throws IOException {
		boolean dbExist = databaseExists();
		if (dbExist) {
			// database already exists - do nothing
		} else {
			this.getReadableDatabase();
			try {
				copyDatabase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}
	
	/**
	 * Checks if the database has already been copied to the phone's local filesystem
	 * @return True if the database is already copied and false otherwise
	 */
	public boolean databaseExists() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does not yet exist
		}
		
		if(checkDB != null) {
			checkDB.close();
		}
		
		return checkDB != null;
	}
	
	/**
	 * Does the actual database copy, from reading the file in our assets to writing each by to our
	 * new file that is stored on the phone. If there are any errors reading/writing, an IOException
	 * is thrown
	 * @throws IOException
	 */
	private void copyDatabase() throws IOException {
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		String outFilename = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFilename);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	/**
	 * Reads the database from the phone
	 * @throws IOException
	 */
	public void openDatabase() throws IOException {
		String myPath = DB_PATH + DB_NAME;
		myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	}
	
	/**
	 * Closes the connection to the SQLite database
	 */
	@Override
	public synchronized void close() {
		if (myDatabase != null) {
			myDatabase.close();
		}
		super.close();
	}

	/**
	 * Method required because we are extending SQLiteOpenHelper
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {}

	/**
	 * Method required because we are extending SQLiteOpenHelper
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	
	/**
	 * Returns an ArrayList of Map objects that are returned by the SQL SELECT query. This method assumes
	 * that the select query is accessing the Map table 
	 * @param query The SQL SELECT query on the Map table
	 * @param selectionArgs A list of selection arguments, if applicable
	 * @return An arraylist of map objects corresponding to our SELECT query
	 */
	// Select query function 
	public ArrayList<Map> selectFromDatabase(String query, String[] selectionArgs) {
		ArrayList<Map> results = new ArrayList<Map>();
		Cursor c = myDatabase.rawQuery(query, selectionArgs);
		if(c.moveToFirst()) {
			do {
				Map m = new Map(myContext);
				for(int i = 0; i < c.getColumnCount(); i++) {
					if(c.getString(i) != null) {
						if(i >= 5 && i <= 8) {
							// For longitude values we want to use getDouble() to preserve accuracy
							m.setVal(i, c.getDouble(i));
						} else {
							m.setVal(i, c.getString(i));
						}
					}
				}
				results.add(m);
			} while(c.moveToNext());
		}
		c.deactivate();
		return results;
	}

}

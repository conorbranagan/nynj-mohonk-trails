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

public class MapDatabaseHelper extends SQLiteOpenHelper {
	private static String DB_PATH = ""; 
	private static final String DB_NAME = "nynj.sqlite";
	private final Context myContext;
	private SQLiteDatabase myDatabase;
	
	private static MapDatabaseHelper myDBConnection;
	
	public MapDatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		DB_PATH = "/data/data/" + context.getApplicationContext().getPackageName() + "/databases/";
	}

	public static synchronized MapDatabaseHelper getDBInstance(Context context) {
		if (myDBConnection == null) {
			myDBConnection = new MapDatabaseHelper(context);
		}
		return myDBConnection;
	}
	
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
	
	public void copyDatabase() throws IOException {
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
	
	public void openDatabase() throws IOException {
		String myPath = DB_PATH + DB_NAME;
		myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	}
	
	@Override
	public synchronized void close() {
		if (myDatabase != null) {
			myDatabase.close();
		}
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	
	// Select query function 
	public ArrayList<Map> selectFromDatabase(String query, String[] selectionArgs) {
		ArrayList<Map> results = new ArrayList<Map>();
		Cursor c = myDatabase.rawQuery(query, selectionArgs);
		if(c.moveToFirst()) {
			do {
				Map m = new Map();
				for(int i = 0; i < c.getColumnCount(); i++) {
					if(c.getString(i) != null) {
						m.setVal(i, c.getString(i));
					}
				}
				results.add(m);
			} while(c.moveToNext());
		}
		return results;
	}

}

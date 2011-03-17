package edu.newpaltz.nynjmohonk;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends Activity {
	Button openMap, closeApp;
	MapDatabaseHelper mdb;
	AlertDialog mapChoice;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Open (and copy, if needed) the database
        mdb = MapDatabaseHelper.getDBInstance(this);
        try {
        	mdb.createDatabase();
        } catch (IOException e) {
        	Log.d("DEBUG", "Error creating database..."); // CHANGEME
        }
        
        try {
        	mdb.openDatabase();
        } catch (IOException e) {
        	Log.d("DEBUG", "Error opening database..."); // CHANGEME
        }
        
        if(mapChoice == null) {
	        String query = "SELECT * FROM map";
	        final ArrayList<Map> results = mdb.selectFromDatabase(query, null);
	        
	        final CharSequence [] names = new CharSequence[results.size()];
	        for(int i = 0; i < results.size(); i++) {
	        	names[i] = results.get(i).getName();
	        }
	        
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Select A Map");
	        builder.setItems(names, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int item) {
					Intent i = new Intent(MainMenu.this, MapViewActivity.class);
					i.putExtra("myMap", results.get(item));
					startActivity(i);
				}
			});
	        mapChoice = builder.create();
        }
        
        
        openMap = (Button) findViewById(R.id.launchMap);
        openMap.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mapChoice.show();
			}
        });
        closeApp = (Button) findViewById(R.id.exit);
        closeApp.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
	}
}
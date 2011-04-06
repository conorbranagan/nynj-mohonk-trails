package edu.newpaltz.nynjmohonk;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
	ProgressDialog d = null;
	Map currentMap = null;
	
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
					// Show dialog symbolizing loading of image (or downloading of image)
					mapChoice.dismiss();
					currentMap = results.get(item);
					d = ProgressDialog.show(MainMenu.this, "", "Loading map...");
					// Start thread which checks on image download state
					Thread t1 = new Thread(waitImageLoaded);
					t1.start();
					// Start actual image download
					Thread t2 = new Thread(loadImage);
					t2.start();
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
	
    private Runnable waitImageLoaded = new Runnable() {
    	public void run() {
			while(currentMap.getImageLoadState() == 0);
			if(currentMap.getImageLoadState() == 3) {
				// Image is downloading..alert the user in some way.
				MainMenu.this.runOnUiThread(new Runnable() {
					public void run() {
						d.setMessage("Image does not exist on phone. Dowloading now...");
					}
				});
				while(currentMap.getImageLoadState() == 3);
			}
			switch(currentMap.getImageLoadState()) {
				case 1:
					// Image loaded successfully (either downloaded or was already downloaded)
					MainMenu.this.runOnUiThread(new Runnable() {
						public void run() {
							d.dismiss();
							Intent i = new Intent(MainMenu.this, MapViewActivity.class);
							i.putExtra("myMap", currentMap);
							MainMenu.this.startActivity(i); // start activity on main thread								
						}
					});
					break;
				case 2:
					// There was some error in downloading the image
					MainMenu.this.runOnUiThread(new Runnable() {
						public void run() {
							d.dismiss();
							AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
							builder.setMessage("There was an error in loading the map. Please try another map or try again later.")
								.setNeutralButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										// TODO
									}
								});
							AlertDialog a = builder.create();
							a.show();							
						}
					});
					break;
			}
    	}
    };
    
    private Runnable loadImage = new Runnable() {
    	public void run() {
    		currentMap.loadImage();
    	}
    };
}
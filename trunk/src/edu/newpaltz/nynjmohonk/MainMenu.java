package edu.newpaltz.nynjmohonk;

import java.io.File;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * The activity which displays the main menu for our app. It includes a few buttons to
 * view a map, exit, or (eventually) change settings.
 */
public class MainMenu extends Activity {
	Button openMap, closeApp, downloadMaps;
	MapDatabaseHelper mdb;
	AlertDialog mapChoice;
	ProgressDialog d = null;
	Map currentMap = null;
	
	/**
	 * Create an instance of the main menu, including copying the database (if needed) and reading the maps from the
	 * map table. Create the onClickListener for the Select Map button.
	 */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        // Open map button shows the select dialog for a map
        openMap = (Button) findViewById(R.id.launchMap);
        openMap.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		        final ArrayList<Map> results = Map.getDownloadedMaps(MainMenu.this);
		        if(results.size() > 0) {
			        final CharSequence [] names = new CharSequence[results.size()];
			        for(int i = 0; i < results.size(); i++) {
			        	names[i] = results.get(i).getName();
			        }
			        
			        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
			        builder.setTitle("Select A Map");
			        builder.setItems(names, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int item) {
							// Show dialog symbolizing loading of image (or downloading of image)
							mapChoice.dismiss();
							currentMap = results.get(item);
							Intent i = new Intent(MainMenu.this, MapViewActivity.class);
							i.putExtra("myMap", currentMap);
							MainMenu.this.startActivity(i); // start activity on main thread	
						}
					});
			        mapChoice = builder.create();			
					mapChoice.show();
		        } else {
		        	AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
					builder.setMessage("No maps downloaded.\nClick \"Download Maps\" first.")
						.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
							}
						});
					AlertDialog a = builder.create();
					a.show();		
		        }
			}
        });
        
        // Close app button exits the application
        closeApp = (Button) findViewById(R.id.exit);
        closeApp.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
        
        // Clear data button clears out any downloaded maps and re-copies the database
        downloadMaps = (Button)findViewById(R.id.download_maps);
        downloadMaps.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
    			MainMenu.this.startActivity(new Intent(MainMenu.this, DownloadMapsActivity.class)); // start activity on main thread	
        	}
        });
	}
	
	/**
     * Create the options menu at the main screen
     * @param menu The menu we want to create
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if(d != null) {
    		d.dismiss();
    	}
    }
    
    /**
     * Setup the event responders for the options selected. 
     * @param item The item that has been selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.clear_cache:
    		d = ProgressDialog.show(this, "", "Clearing data");
    		Thread t1 = new Thread(clearCache);
    		t1.start();
    		break;
    	case R.id.support:
    		//TODO: create a reference people can go to for help
    		break;
    	case R.id.info:
    		//TODO: create a "credits page" giving information about the app
    		break;
    	}
    	return true;
    }	
    
    // Thread to delete downloaded maps and clear out and re-copy the database
    private Runnable clearCache = new Runnable() {
    	public void run() {
    		// Delete map files
    		File f = MainMenu.this.getFilesDir();
    		File[] allFiles = f.listFiles(); 
    		for(int i = 0; i < allFiles.length; i++) {
    			allFiles[i].delete();
    		}
    		
    		
    		MainMenu.this.deleteDatabase("nynj.sqlite");
    		
    		
    		MapDatabaseHelper mdb = MapDatabaseHelper.getDBInstance(MainMenu.this); // Open database connection
    		// Copy database again
    		try {
    			mdb.createDatabase();
    			mdb.openDatabase();
    		} catch (Exception e) {
    			// Shouldn't happen
    		}
    		
    		// Close progress dialog on main UI thread
    		MainMenu.this.runOnUiThread(new Runnable() {
				public void run() {
					d.dismiss();						
				}
			});
    	}
    };
}
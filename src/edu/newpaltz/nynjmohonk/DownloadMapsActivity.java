package edu.newpaltz.nynjmohonk;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 *
 */
public class DownloadMapsActivity extends ListActivity {
	ProgressDialog d = null;
	AlertDialog alert = null;
	Map currentMap = null;
	LinearLayout currentView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			final ArrayList<Map> allMaps = Map.getAllMaps(this);
			this.setTitle("Download Maps");
			
			// Populate a HashMap with all the map names
			ArrayList<HashMap<String, String>> mapInfoForList = new ArrayList<HashMap<String, String>>();
			for(int i = 0; i < allMaps.size(); i++) {
				Map m = allMaps.get(i);
				HashMap<String, String> thisMap = new HashMap<String, String>();
				thisMap.put("name", m.getName());
				if(m.isDownloaded()) {
					thisMap.put("image", R.drawable.list_checkmark + "");
				} else {
					thisMap.put("image", android.R.drawable.ic_input_add + "");
				}
				thisMap.put("description", m.getDescription());
				mapInfoForList.add(thisMap);
			}
			
			SimpleAdapter mapAdapter = new SimpleAdapter(
					getApplicationContext(), 
					mapInfoForList, 
					R.layout.map_list_item, 
					new String[] {"name", "image", "description"},
					new int[] {R.id.list_title, R.id.list_image, R.id.list_description}
			);
			
			this.setListAdapter(mapAdapter);
					
			// Load all un-downloaded maps
			ListView lv = getListView();
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
					currentMap = allMaps.get(position);
					if(currentMap.isDownloaded()) {
		        		// Map is already downloaded - do nothing
					} else {
						d = ProgressDialog.show(DownloadMapsActivity.this, "", "Downloading map...");
						// Start thread which checks on image download state
						Thread t1 = new Thread(waitImageLoaded);
						t1.start();
						Thread t2 = new Thread(loadImage);
						t2.start();
						currentView = (LinearLayout)view;					
					}
				}
			});
		} catch(Exception e) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(DownloadMapsActivity.this);
			builder.setMessage("Your map database is corrupted. Please download a new database on the main menu.")
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
			    		finish();
					}
				});
			AlertDialog a = builder.create();
			a.show();			
		}

	}
	
	// Start a background thread that downloads the image (if needed) and loads the map and shows
	// our MapViewActivity
    private Runnable waitImageLoaded = new Runnable() {
    	public void run() {
			while(currentMap.getImageLoadState() == 0); // wait while the image is in an unknown state
			while(currentMap.getImageLoadState() == 3); // wait while image actually downloads...
			switch(currentMap.getImageLoadState()) {
				case 1:
					// Image loaded successfully (either downloaded or was already downloaded)
					DownloadMapsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							ImageView im = (ImageView)currentView.findViewById(R.id.list_image);
							im.setImageDrawable(DownloadMapsActivity.this.getResources().getDrawable(R.drawable.list_checkmark));
							d.dismiss();	
						}
					});
					break;
				case 2:
					// There was some error in downloading the image
					DownloadMapsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							d.dismiss();
							// Show an alert dialog that shows an error in downloading the map
							AlertDialog.Builder builder = new AlertDialog.Builder(DownloadMapsActivity.this);
							builder.setMessage("There was an error in downloading the map. Check your network settings and try again later.")
								.setNeutralButton("OK", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										
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
    
    // Short thread to load the image in the background
    private Runnable loadImage = new Runnable() {
    	public void run() {
    		currentMap.loadImage();
    	}
    };   
    
}

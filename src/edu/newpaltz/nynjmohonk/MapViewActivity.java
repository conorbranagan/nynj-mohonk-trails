package edu.newpaltz.nynjmohonk;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class MapViewActivity extends Activity {
	private ProgressDialog d = null;
	private Map myMap;
	private MapView myMapView;
	private Handler mHandler = new Handler();
	private double maxLatitude, minLatitude, maxLongitude, minLongitude, latPerPixel, lonPerPixel;
	private LocationListener locationListener;
	private AlertDialog outOfRangeAlert;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        // Show the map view
        setContentView(R.layout.map_layout);
        
        // Get the Map object and a reference to the MapView
        Intent i = getIntent();
        myMap = (Map)i.getParcelableExtra("myMap");
        myMapView = (MapView)findViewById(R.id.map_image);
        
        // Set the map image based on our map object
        myMapView.setImageResource(R.drawable.mohonk_map); // TODO
        // Max/min values are relative to the image and NOT to the numbers themselves
        maxLongitude = myMap.getTrlon(); 
        minLongitude = myMap.getBllon();
        maxLatitude = myMap.getTrlat(); 
        minLatitude = myMap.getTrlat(); 
        latPerPixel = (Math.abs(maxLatitude - minLatitude)) / myMapView.getWidth();
        lonPerPixel = (Math.abs(maxLongitude - minLongitude)) / myMapView.getHeight();
        
        // Show progress dialog until GPS location is found
        d = ProgressDialog.show(this, "", "Waiting for GPS...");
        
        // Turn on the location updating
        turnOnLocation();
        
        // Start the timer for looking for a GPS
        mHandler.postDelayed(mRemoveGPSWaiting, 60000); // 60 seconds: higher or lower?
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.map_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.exit_map:
    		// Exit the MapViewActivity
    		finish();
    		break;
    	case R.id.current_location:
    		myMapView.showCurrentLocation();
    		break;
    	}
    	return true;
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	mHandler.removeCallbacks(mRemoveGPSWaiting);
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        d.dismiss();
        outOfRangeAlert.dismiss();
        finish();
    }
    
    private void turnOnLocation() {
        // Turn on the LocationManager to figure out current location
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        
        // Define a location listener and the events that go with it    
        locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        		if(d.isShowing()) {
        			d.hide();
        		}
        		double longitude = location.getLongitude();
        		double latitude = location.getLatitude();
        		// In this method we want to update our BitMap to reflect the change in location
        		Log.d("DEBUG", "LOCATION CHANGED TO: " + longitude + ", " + latitude);
        		updateMapLocation(longitude, latitude);
        	}
        	
        	public void onStatusChanged(String provider, int status, Bundle extras) {}
        	
        	public void onProviderEnabled(String provider) {}
        	
        	public void onProviderDisabled(String provider) {}
        };
        
        // Link the location listener to the location manager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    private void updateMapLocation(double lon, double lat) {
    	if(inRange(lat, lon)) {
    		// Calculate pixel point
    		double numLatitudeIn = Math.abs(lat - minLatitude);
    		double numLongitudeIn = Math.abs(lon - minLongitude);
    		double cx = numLatitudeIn / latPerPixel;
    		double cy = numLongitudeIn / lonPerPixel;
    		myMapView.updateLocation((float)cx, (float)cy);
    	} else {
    		if(outOfRangeAlert == null) {
        		// Out of range. Display a message.
    			AlertDialog.Builder builder = new AlertDialog.Builder(MapViewActivity.this);
    			builder.setMessage("You are out of range of this map.")
    				.setNeutralButton("Go Back", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						MapViewActivity.this.finish();
    					}
    				});
    			outOfRangeAlert = builder.create();
    		}
    		if(!outOfRangeAlert.isShowing()) {
    			outOfRangeAlert.show();   		
    		}
    	}
    }
    
    private boolean inRange(double lat, double lon) {
    	if((maxLatitude > minLatitude && lat >= minLatitude && lat <= maxLatitude) || (maxLatitude < minLatitude && lat <= minLatitude && lat >= maxLatitude)) {
        	if((maxLongitude > minLongitude && lat >= minLongitude && lat <= maxLongitude) || (maxLongitude < minLongitude && lat <= minLongitude && lat >= maxLongitude)) {
        		return true;
        	}
        }
    	return false;
    }
    
    private Runnable mRemoveGPSWaiting = new Runnable() {
    	public void run() {
			if(d.isShowing()) {
				d.hide();
				// If still waiting for GPS, we assume device has no signal currently. 
				AlertDialog.Builder builder = new AlertDialog.Builder(MapViewActivity.this);
				builder.setMessage("No GPS Signal could be found or GPS is inactive on phone.")
					.setNeutralButton("Go Back", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							MapViewActivity.this.finish();
						}
					});
				AlertDialog a = builder.create();
				a.show();
			}
    	}
    };
    
}
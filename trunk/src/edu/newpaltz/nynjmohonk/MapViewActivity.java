package edu.newpaltz.nynjmohonk;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class MapViewActivity extends Activity {
	float lastX = -1, lastY = -1;
	ProgressDialog d = null;
	MapView m;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // Show progress dialog until GPS location is found
      //  d = ProgressDialog.show(this, "", "Waiting for GPS...");
        // Turn on the location updating
        turnOnLocation();
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
    	m = (MapView)findViewById(R.id.map_image);
    	switch(item.getItemId()) {
    	case R.id.exit_map:
    		
    		break;
    	case R.id.current_location:
    		if(m == null) {

    		} else {
    			m.showCurrentLocation();
    		}
    		break;
    	}
    	return true;
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	finish();
    }
    
    private void turnOnLocation() {
        // Turn on the LocationManager to figure out current location
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        
        // Define a location listener and the events that go with it    
        LocationListener locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        	/*	if(d.isShowing()) {
        			d.hide();
        		}*/
            	m = (MapView)findViewById(R.id.map_image);
        		double longitude = location.getLongitude();
        		double latitude = location.getLatitude();
        		// In this method we want to update our BitMap to reflect the change in location
        		Log.d("DEBUG", "LOCATION CHANGED TO: " + longitude + ", " + latitude);
        		m.updateLocation(longitude, latitude);
        	}
        	
        	public void onStatusChanged(String provider, int status, Bundle extras) {}
        	
        	public void onProviderEnabled(String provider) {}
        	
        	public void onProviderDisabled(String provider) {}
        };
        
        // Link the location listener to the location manager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    private void updateMapLocation(double longitude, double latitude) {
    	/*
    	 *  Uses longitude and latitude parameters to update our Mohonk map bitmap
    	 *  
    	 *  If we have 2 opposite corner points: bottom-left (bl), top-right (tr)
    	 *  calculate:
    	 *  	latPerPixel = (tr.latitude - bl.latitude) / (num pixels width)
    	 *  	lonPerPixel = (tr.longitude - bl.longitude) / (num pixels height)
    	 * 	
    	 * For that, we can give the following pseudocode for this method:
    	 * 		
    	 * 		if(latitude >= bl.latitude && latitude <= tr.latitude && longitude >= bl.longitude && longitude <= bl.longitude) {
    	 * 			// In range of our map
    	 * 			numLatitudeIn = myPixel.latitude - bl.latitude
    	 * 			numLongitudeIn = myPixel.longitude - bl.longitude
    	 * 			myPixel.x = round(numLatitudeIn / latitudePerPixel) // Round to nearest whole
    	 * 			myPixel.y = round(numLongitudeIn / longitudePerPixel) // Round to nearest whole
    	 * 			// Plot this point and a set number of surrounding pixels
    	 * 		} else {
    	 * 			// Out of range.
    	 * 		}
    	 * 
    	 * 
    	 */
    }
    
    
}
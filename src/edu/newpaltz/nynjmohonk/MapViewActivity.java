package edu.newpaltz.nynjmohonk;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class MapViewActivity extends Activity {
	float lastX = -1, lastY = -1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // Turn on the location updating
        //turnOnLocation();
    }
    
    private void turnOnLocation() {
        // Turn on the LocationManager to figure out current location
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        
        // Define a location listener and the events that go with it    
        LocationListener locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        		double longitude = location.getLongitude();
        		double latitude = location.getLatitude();
        		// In this method we want to update our BitMap to reflect the change in location
        		Log.d("NYNJ Trails", "LOCATION CHANGED TO: " + longitude + ", " + latitude);
        		updateMapLocation(longitude, latitude);
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
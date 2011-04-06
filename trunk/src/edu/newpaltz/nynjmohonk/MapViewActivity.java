package edu.newpaltz.nynjmohonk;


import java.io.BufferedInputStream;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
	private double curLatitude = 0, curLongitude = 0;
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
        try {
        	// Will eventually have to decrypt file before opening
        	BufferedInputStream buf = new BufferedInputStream(this.openFileInput(myMap.getFilename()));
        	Bitmap b = BitmapFactory.decodeStream(buf);
        	myMapView.setImageBitmap(b);
        } catch (IOException e) {
        	
        }
                
        // Max/min values are relative to the image and NOT to the numbers themselves
        minLongitude = myMap.getMinLongitude();
        maxLatitude = myMap.getMaxLatitude();
        latPerPixel = myMap.getLatPerPixel();
        lonPerPixel = myMap.getLonPerPixel();
                
        Log.d("DEBUG", "Minimum Longitude: " + minLongitude);
        Log.d("DEBUG", "Maximum Latitude: " + maxLatitude);
        Log.d("DEBUG", "Lon Per Pixel: " + latPerPixel);
        Log.d("DEBUG", "Lat Per Pixel: " + lonPerPixel);

        // Using world file, maxLongitude and minLatitude are calculated
        maxLongitude = minLongitude + (myMapView.getDrawable().getMinimumWidth() * lonPerPixel);
        minLatitude = maxLatitude + (myMapView.getDrawable().getMinimumHeight() * -latPerPixel);
      

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
    		myMapView.showCurrentLocation(curLatitude, curLongitude, this);
    		break;
    	}
    	return true;
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	mHandler.removeCallbacks(mRemoveGPSWaiting);
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if(locationListener != null) locationManager.removeUpdates(locationListener);
        if(d != null) d.dismiss();
        if(outOfRangeAlert != null) {
        	outOfRangeAlert.dismiss();
        }
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
        			d.dismiss();
        		}
        		double longitude = location.getLongitude();
        		double latitude = location.getLatitude();
        		// In this method we want to update our BitMap to reflect the change in location
        		curLatitude = latitude;
        		curLongitude = longitude;
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
    		double cy = numLatitudeIn / latPerPixel;
    		double cx = numLongitudeIn / lonPerPixel;
    		myMapView.updateLocation((float)cx, myMapView.getDrawable().getMinimumHeight() - (float)cy + 8);
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
    	// Lon: -74.1185445
    	// Lat: 41.7612376
    	
    	// Max Lat: 41.827491
    	// Min Lat: 41.705781
    	
    	if((maxLatitude > minLatitude && lat >= minLatitude && lat <= maxLatitude) || (maxLatitude < minLatitude && lat <= minLatitude && lat >= maxLatitude)) {
    		if((maxLongitude > minLongitude && lon >= minLongitude && lon <= maxLongitude) || (maxLongitude < minLongitude && lon <= minLongitude && lon >= maxLongitude)) {
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
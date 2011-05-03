package edu.newpaltz.nynjmohonk;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * This activity is called from our Main Menu activity and it displays the map, updating the 
 * user's location from GPS and generally responding to any events that deal with the map *
 */
public class MapViewActivity extends Activity {
	private ProgressDialog d = null;
	private Map myMap;
	private MapView myMapView;
	private Handler mHandler = new Handler();
	private double maxLatitude, minLatitude, maxLongitude, minLongitude, latPerPixel, lonPerPixel;
	private double curLatitude = 0, curLongitude = 0;
	private LocationListener locationListener;
	private AlertDialog outOfRangeAlert;
	private CompassListener cl;
	private Bitmap mapBitmap;
	private boolean willCenter = false;
	
	/**
	 * Sets up the content view to be the map layout. Turns on the compass and links it to the map. Pulls the map
	 * data from the Parcable and uses that information to decrypt the file and load it into the buffer. The necessary
	 * longitude and latitude points are calculated using the information from the world file and the GPS location listener
	 * is turned on.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show the map view
        setContentView(R.layout.map_layout);
          
        // Get the Map object and a reference to the MapView
        Intent i = getIntent();
        myMap = (Map)i.getParcelableExtra("myMap");
        myMapView = (MapView)findViewById(R.id.map_image);
  
        // Set the title of this view to the name of the map
        setTitle(myMap.getName());
        
        // Initialize and turn on the compass
        cl = new CompassListener(this, myMapView);
        myMapView.setCompass(cl);
        
        
    	byte [] decryptedFile = myMap.getDecryptedImage(this);
    	Log.d("DEBUG", "Decrypted file length: " + decryptedFile.length);
    	mapBitmap = BitmapFactory.decodeByteArray(decryptedFile, 0, decryptedFile.length);
    	myMapView.setImageBitmap(mapBitmap);
            	
        // Max/min values are relative to the image and NOT to the numbers themselves
        minLongitude = myMap.getMinLongitude();
        maxLatitude = myMap.getMaxLatitude();
        latPerPixel = myMap.getLatPerPixel();
        lonPerPixel = myMap.getLonPerPixel();

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
    
    /**
     * When the activity resumes, turn the compass listener back on
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	cl.registerListener();
    }
    
    /**
     * When the activity is in a paused state, we want to turn of the compass sensor as it uses a lot of battery
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	cl.unregisterListener();
    }
    

    /**
     * Create the options menu
     * @param menu The menu we want to create
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.map_menu, menu);
    	return true;
    }
    
    /**
     * Setup the event responders for the options selected. For exit map, simply exit the activity. For current
     * location, we want to call our MapView to show the current location and center it on the screen.
     * @param item The item that has been selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.exit_map:
    		// Exit the MapViewActivity
    		finish();
    		break;
    	case R.id.current_location:
    		myMapView.showCurrentLocation(curLatitude, curLongitude);
    		break;
    	}
    	return true;
    }
    
    /**
     * If the activity is stopped, turn off the GPS location listener and dismiss any lingering
     * alerts that may still be showing
     */
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
        // General cleanup to save memory on the VM
        myMapView.closeDown();
        myMapView = null;
        myMap = null;
        mapBitmap.recycle();
        mapBitmap = null;
        finish();
    }
    
    /**
     * This turns on the location listener which will listen to events from the GPS and respond to them by
     * updating the location indicator on the map
     */
    private void turnOnLocation() {
        // Turn on the LocationManager to figure out current location
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        
        // Define a location listener and the events that go with it    
        locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        		if(d != null && d.isShowing()) {
        			d.hide();
        			d.dismiss();
        			// Center on the first point
        			willCenter = true;
        		}
        		double longitude = location.getLongitude();
        		double latitude = location.getLatitude();
        		// In this method we want to update our image to reflect the change in location
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
    
    /**
     * Calculate the pixel point based on the given longitude and latitude. Uses information from the world
     * file given by ArcGIS and from other values calculated at the start of the activity
     * @param lon The current longitude read from the GPS
     * @param lat The current latitude read from the GPS
     */
    private void updateMapLocation(double lon, double lat) {
    	lon = -74.14879;
    	lat = 41.79921;
		double numLatitudeIn = Math.abs(lat - minLatitude);
		double numLongitudeIn = Math.abs(lon - minLongitude);
		double cy = numLatitudeIn / latPerPixel;
		double cx = numLongitudeIn / lonPerPixel;
		Log.d("DEBUG", "checking in range for: " + cx + ", " + cy);
		if(willCenter) { 
			myMapView.centerOnPoint((float)cx, (float)cy, true); 
			willCenter = false;
		}
    	if(inRange(lat, lon) && inPolygonRange(cx, cy)) {
    		// Calculate pixel point
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
    
    /**
     * Determines if the given longitude and latitude is in range of this image
     * @param lat The current latitude read from the GPS
     * @param lon The current longitude read from the GPS
     * @return True if the point is in range, false otherwise.
     */
    private boolean inRange(double lat, double lon) {    	
    	if((maxLatitude > minLatitude && lat >= minLatitude && lat <= maxLatitude) || (maxLatitude < minLatitude && lat <= minLatitude && lat >= maxLatitude)) {
    		if((maxLongitude > minLongitude && lon >= minLongitude && lon <= maxLongitude) || (maxLongitude < minLongitude && lon <= minLongitude && lon >= maxLongitude)) {
    			return true;
        	}
        }
    	return false;
    }
    
    /**
     * Determines if the point is within the range of the map image shape using a Polygon and Point
     * object which are derived from the data in the SQLite database
     * @param x Calculated x pixel
     * @param y Calculated y pixel
     */
    private boolean inPolygonRange(double x, double y) {
    	int realY = myMapView.getDrawable().getMinimumHeight() - (int)y;
    	return myMap.getPolygon().contains((int)x, (int)realY); // may lose value on cast
    }
    
    /**
     * This is an inner class thread that is executed after a given time (currently 60 seconds) and checks if the
     * GPS is active. If not, an alert is shown telling the user that their GPS is either inactive or they are in
     * a location without GPS service
     */
    private Runnable mRemoveGPSWaiting = new Runnable() {
    	public void run() {
			if(d != null && d.isShowing()) {
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
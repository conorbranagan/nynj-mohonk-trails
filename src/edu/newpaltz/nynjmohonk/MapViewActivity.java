package edu.newpaltz.nynjmohonk;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class MapViewActivity extends Activity {
	private static LinearLayout mLinearLayout;
	private static ImageView i;
	private static Bitmap mohonkBitmap;
	float lastX = -1, lastY = -1;
	//MapView view;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        mLinearLayout = new LinearLayout(this);
        i = new ImageView(this);
        Bitmap bit = ((BitmapDrawable)getResources().getDrawable(R.drawable.mohonk_map)).getBitmap();
        mohonkBitmap = bit.copy(Bitmap.Config.RGB_565, true); // Copy bitmap (so it will be mutable)
        i.setImageBitmap(mohonkBitmap); // Put the image on the screen
        mLinearLayout.addView(i);
        setContentView(mLinearLayout);
        // Turn on the location updating
        turnOnLocation();
    }
    public boolean onTouchEvent(MotionEvent e) {
    	int p = e.getPointerCount();
    	if(lastX == -1 && lastY == -1) {
    		lastX = e.getX();
    		lastY = e.getY();
    		return true;
    	}
    	
    	float dx = e.getX() - lastX;
    	float dy = e.getY() - lastY;

    	switch(e.getAction()) {
    		case MotionEvent.ACTION_MOVE:
    			if(p == 1) {
	    	        Matrix m = i.getImageMatrix();
	    	        m.postTranslate(dx, dy);
	    	        i.setImageMatrix(m);
	    	        i.setScaleType(ScaleType.MATRIX);
	    	        i.invalidate();
    			} else {
    				// Multi-touch
	    	        Matrix m = i.getImageMatrix();
	    	        m.postScale((float).9, (float).9);
	    	        i.setImageMatrix(m);
	    	        i.setScaleType(ScaleType.MATRIX);
	    	        i.invalidate();
	    	    }
    	       break;
	    	}
    	lastX = e.getX();
    	lastY = e.getY();

        return true;
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
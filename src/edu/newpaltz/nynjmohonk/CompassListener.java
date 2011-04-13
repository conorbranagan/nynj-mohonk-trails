package edu.newpaltz.nynjmohonk;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * A Compass Listener listens to the orientation sensor of the phone and detects what position the phone
 * is facing.
 */
public class CompassListener implements SensorEventListener {
	private float[] vals = new float[3];
	private SensorManager mSensorManager;
	private Context mContext;
	private MapView m;
	
	/**
	 * Create a compass listener object
	 * @param c Current Android application context
	 */
	public CompassListener(Context c, MapView mapView) {
		mContext = c;
		mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
		m = mapView;
		registerListener();
	}
	
	/**
	 * Get the direction that your phone is facing from the sensor
	 * @return The direction (in degrees) that the top of the phone is facing
	 */
	public float getForward() { return vals[0]; }
	
	/**
	 * Unregister the sensor listener so that sensor data is not being taken
	 */
	public void unregisterListener() {
		mSensorManager.unregisterListener(this);
	}
	
	/**
	 * Turn on the sensor and start taking data from the phone's compass
	 */
	public void registerListener() {
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
	}
	
	/**
	 * Place holder for implemented method for when accuracy changes
	 * @param sensor The sensor that changes accuracy
	 * @param accuracy The new accuracy 
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	}

	/**
	 * Read the sensor values from the event when it  changes
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		vals = event.values;
		m.invalidate(); // Redraw the map
	}
	
	
	
}

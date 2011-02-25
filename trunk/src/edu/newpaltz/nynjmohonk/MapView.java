package edu.newpaltz.nynjmohonk;
/*Pieces of Code for Multi-Touch and Zoom features were used from the following sources: 
 * http://code.google.com/p/krvarma-android-samples/ - MultiTouch example
 * http://code.google.com/p/android-pinch/ - PinchZoom example
 * 
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MapView extends ImageView {
	public static final float MIN_SCALE = 1f;
	public static final float MAX_SCALE = 4f;
	public static final int GROW = 0;
	public static final int SHRINK = 1;
	
	public boolean isMultiTouch = false;
	private Matrix m;
	private Bitmap myBitmap;
	public float prevX = -1, prevY = -1, curX, curY, nextX, nextY, dx, dy;
	public float distPre = -1, distCur, distMeasure; 
	public float zoomIn = 1.01f, zoomOut = 0.01f, scale;
	int mTouchSlop;
	
	public MapView(Context context, Bitmap b){
		super(context);
		myBitmap = b;
		this.setImageBitmap(myBitmap);
		this.setScaleType(ScaleType.MATRIX);
		initialize();
	}
	
	private void initialize(){
		mTouchSlop = ViewConfiguration.getTouchSlop();
		setScaleType(ScaleType.MATRIX);
	}
	
    public float getScale(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MSCALE_X];
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	int action = event.getAction() & MotionEvent.ACTION_MASK;
		    	
		if(prevX == -1 && prevY == -1) {
    		prevX = event.getX(0);
    		prevY = event.getY(0);
    		return true;
		}
		
		switch(action)
		{
			case MotionEvent.ACTION_DOWN:
			{
				invalidate();
				break;
			}
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				isMultiTouch = true;
				invalidate();		
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
			{
				isMultiTouch = false;
				break;
			}
			case MotionEvent.ACTION_MOVE:
			{
				// First Finger's coordinates
	    		curX = event.getX(0);
	    		curY = event.getY(0);
				if(!isMultiTouch) {
					//translation if only one finger is detected
					dx = curX - prevX;
					dy = curY - prevY;
					Log.d("DEBUG", "Moving x: " + dx + " and y: " + dy);
					m = getImageMatrix();
	    	        m.preTranslate(dx, dy);
	    	        setScaleType(ScaleType.MATRIX);
	    	        setImageMatrix(m);
	    	        invalidate();
				} else {
					// Zoom if two fingers are detected
					Log.d("DEBUG", "Zoom Touch Event");
					// second Finger's coordinates
		    		nextX = event.getX(1);
		    		nextY = event.getY(1);
		    		
		    		// figure out the distance in order to find which direction the fingers are moving
		    		distCur = (float) Math.sqrt(Math.pow(nextX - curX, 2) + Math.pow(nextY - curY, 2));
		    		distMeasure = distPre > -1 ? distCur - distPre : 0;
		    		
		    		m = getImageMatrix();
		    		scale = getScale(m);
			    	if (Math.abs(distMeasure) > mTouchSlop) {
		    			int mode = distMeasure > 0 ? GROW : (distCur == distPre ? 2 : SHRINK);
			    		switch (mode) {
				    		case GROW: // detect fingers reverse pinching
				    			zoom_In(scale);
				    		break;
				    		case SHRINK: // detect fingers pinching
				    			zoom_Out(scale);
				    		break;
			    		}
			    	}
			    		
					distPre = distCur;
			    }	
				break;
			}
		}
    	prevX = curX;
	   	prevY = curY;
	   	return true;
	}
    	
	public void zoom_In(float scale) {
		if(scale > MAX_SCALE) return; //keeps from zooming in too much
		
		m = this.getImageMatrix();
		m.postScale(zoomIn, zoomIn, getWidth()/2f, getHeight()/2f);
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
	
	public void zoom_Out(float scale) {
		if(scale < MIN_SCALE) return; //keeps from zooming out too much
		
		m = this.getImageMatrix();
		m.postScale(zoomOut, zoomOut, getWidth()/2f, getHeight()/2f);
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
}
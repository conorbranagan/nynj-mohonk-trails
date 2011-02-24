package edu.newpaltz.nynjmohonk;
/*Pieces of Code for Multi-Touch and Zoom features were used from the following sources: 
 * http://code.google.com/p/krvarma-android-samples/ - MultiTouch example
 * http://code.google.com/p/android-pinch/ - PinchZoom example
 * 
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MapView extends View {
	public static final float MIN_SCALE = 1f;
	public static final float MAX_SCALE = 4f;
	public static final int GROW = 0;
	public static final int SHRINK = 1;
	
	public boolean isMultiTouch = false;
	Matrix m = new Matrix();
	private ImageView i = null;
	public float prevX = -1, prevY = -1, curX, curY, nextX, nextY, dx, dy;
	public float distPre = -1, distCur, distMeasure; 
	public float zoomIn = 1.01f, zoomOut = 0.01f, scale;
	int mTouchSlop;
	
	public MapView(Context context) {
		super(context);
		
		initialize();
	}

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize();
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize();
	}
	public MapView(ImageView i){
		super(i.getContext());
		initialize();
		this.i = i;
		this.i.setScaleType(ScaleType.MATRIX);
	}
	private void initialize(){
		m = i.getImageMatrix();
		i.setImageMatrix(m);
		mTouchSlop = ViewConfiguration.getTouchSlop();
		i.setScaleType(ScaleType.MATRIX);
	}
    public float getScale(Matrix m) {
		float[] values = new float[9];
		 m.getValues(values);
		return values[Matrix.MSCALE_X];
    }
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		
		if(prevX == -1 && prevY == -1) {
    		prevX = event.getX();
    		prevY = event.getY();
    		return true;
		}
		//translation points
		float dx = event.getX() - prevX;
    	float dy = event.getY() - prevY;
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
				if(isMultiTouch = false) {
					//translation if only one finger is detected
					m = i.getImageMatrix();
	    	        m.postTranslate(dx, dy);
	    	        i.setImageMatrix(m);
	    	        i.setScaleType(ScaleType.MATRIX);
	    	        i.invalidate();
				}
				// Move to the else if two fingers are detected
				else {
					// second Finger's coordinates
		    		nextX = event.getX(1);
		    		nextY = event.getY(1);
		    		
		    		// figure out the distance in order to find which direction the fingers are moving
		    		distCur = (float) Math.sqrt(Math.pow(nextX - curX, 2) + Math.pow(nextY - curY, 2));
		    		distMeasure = distPre > -1 ? distCur - distPre : 0;
		    		
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
			    		
			    	prevX = curX;
				   	prevY = curY;
					distPre = distCur;
			    	return true;
			    	}	
				break;
				}
			}
		}
	return false;
	}
	public void zoom_In(float scale) {
		if(scale > MAX_SCALE) return; //keeps from zooming in too much
		
		m.postScale(zoomIn, zoomIn, getWidth()/2f, getHeight()/2f);
		i.setImageMatrix(m);
        i.setScaleType(ScaleType.MATRIX);
        i.invalidate();
		return;
	}
	public void zoom_Out(float scale) {
		if(scale < MIN_SCALE) return; //keeps from zooming out too much
		
		m.postScale(zoomOut, zoomOut, getWidth()/2f, getHeight()/2f);
		i.setImageMatrix(m);
        i.setScaleType(ScaleType.MATRIX);
        i.invalidate();
		return;
	}
}
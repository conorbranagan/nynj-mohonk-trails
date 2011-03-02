package edu.newpaltz.nynjmohonk;
/*Pieces of Code for Multi-Touch and Zoom features were used from the following sources: 
 * http://code.google.com/p/krvarma-android-samples/ - MultiTouch example
 * http://code.google.com/p/android-pinch/ - PinchZoom example
 * 
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MapView extends ImageView {
	public static final float MIN_SCALE = .2f;
	public static final float MAX_SCALE = .9f;
	public static final int GROW = 0;
	public static final int SHRINK = 1;
	
	public boolean isMultiTouch = false, doMove = true;
	private Matrix m;
	private Bitmap myBitmap;
	public float prevX = -1, prevY = -1, curX, curY, nextX, nextY, dx, dy;
	public float distPre = -1, distCur, distMeasure; 
	public float zoomIn = 1.01f, zoomOut = 0.99f, scale;
	
	public MapView(Context c, AttributeSet a) {
		super(c, a);
        initialize();
	}
	
	private void initialize(){
		Bitmap bit = ((BitmapDrawable)getResources().getDrawable(R.drawable.mohonk_map_smaller)).getBitmap();
        myBitmap = bit.copy(Bitmap.Config.RGB_565, true); // Copy bitmap (so it will be mutable)
        this.setImageBitmap(myBitmap);
        m = getImageMatrix();
        m.postScale(1, 1);
	}
	
    public float getScale(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MSCALE_X];
    }
    
    @Override
    public void onDraw(Canvas c) {
    	super.onDraw(c);
    	Paint p = new Paint();
    	p.setColor(Color.RED);
    	c.drawCircle(10, 10, 10, p);
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
				curX = event.getX(0);
				curY = event.getY(0);
				//invalidate();
				doMove = true;
				break;
			}
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				isMultiTouch = true;
				//invalidate();		
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
			{
				isMultiTouch = false;
				doMove = false;
				break;
			}
			case MotionEvent.ACTION_MOVE:
			{
				
				// First Finger's coordinates
	    		curX = event.getX(0);
	    		curY = event.getY(0);
				if(!isMultiTouch && doMove) {
					
					//translation if only one finger is detected
					dx = curX - prevX;
					dy = curY - prevY;
	    	        
					m = getImageMatrix();
	    	        m.postTranslate(dx, dy);
	    	        
	    	        
	    	        setImageMatrix(m);
	    	        setScaleType(ScaleType.MATRIX);
	    	        invalidate();
				} else {
					// Zoom if two fingers are detected

					// second Finger's coordinates
		    		nextX = event.getX(1);
		    		nextY = event.getY(1);
		    		// figure out the distance in order to find which direction the fingers are moving
		    		distCur = (float) Math.sqrt(Math.pow(nextX - curX, 2) + Math.pow(nextY - curY, 2));
		    		distMeasure = distPre > -1 ? distCur - distPre : 0;
		    		
		    		m = getImageMatrix();
		    		scale = getScale(m);
		    		Log.d("DEBUG", "Distance is: " + distCur);
	    			int mode = distMeasure > 0 ? GROW : (distCur == distPre ? 2 : SHRINK);
		    		switch (mode) {
			    		case GROW: // detect fingers reverse pinching
			    			zoomIn(scale, distCur);
			    		break;
			    		case SHRINK: // detect fingers pinching
			    			zoomOut(scale, distCur);
			    		break;
		    		}
					distPre = distCur;
			    }	
				break;
			}
		}
    	prevX = curX;
	   	prevY = curY;
	   	super.onTouchEvent(event);
	   	return true;
	}
    	
	public void zoomIn(float scale, float dist) {
		if(scale > MAX_SCALE) return; //keeps from zooming in too much
		
		m = getImageMatrix();
		m.postScale(zoomIn, zoomIn, getWidth()/2f, getHeight()/2f);
				
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
	
	public void zoomOut(float scale, float dist) {
		if(scale < MIN_SCALE) return; //keeps from zooming out too much
		m = getImageMatrix();
		m.postScale(zoomOut, zoomOut, getWidth()/2f, getHeight()/2f);
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
}
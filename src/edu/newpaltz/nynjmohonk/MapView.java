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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MapView extends ImageView {
	public static final float MIN_SCALE = .5f;
	public static final float MAX_SCALE = 2f;
	public static final int GROW = 0;
	public static final int SHRINK = 1;
	
	public boolean isMultiTouch = false, doMove = true;
	private Matrix m;
	private Bitmap myBitmap;
	private Paint p;
	public float prevX = -1, prevY = -1, curX, curY, nextX, nextY, dx, dy;
	public float distPre = -1, distCur, distMeasure; 
	public float zoomIn = 1.05f, zoomOut = 0.95f, scale;
	public float circleX = 300, circleY = 300, circleRadius = 10;
	
	public MapView(Context c, AttributeSet a) {
		super(c, a);
		m = getImageMatrix();
		m.setScale(1f, 1f);
		setImageMatrix(m);
		setScaleType(ScaleType.MATRIX);
    	p = new Paint();
    	p.setColor(Color.BLUE);
	}
		
    private float getScale(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MSCALE_X];
    }
    
    private float getTransX(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MTRANS_X];
    }    
  
    private float getTransY(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MTRANS_Y];
    }    
    
    @Override
    protected void onDraw(Canvas c) {
    	super.onDraw(c);    	
    	if(myBitmap == null) {
        	myBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
    	} else {
    		myBitmap.eraseColor(Color.TRANSPARENT); // Erase the bitmap
    	}
    	Canvas bitmapCanvas = new Canvas(myBitmap);
    	bitmapCanvas.drawCircle(circleX, circleY, circleRadius, p);
    	c.drawBitmap(myBitmap, getImageMatrix(), p);
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
				if(!isMultiTouch) {			
					if(doMove) {
						//translation if only one finger is detected
						dx = curX - prevX;
						dy = curY - prevY;
		    	        
						m = getImageMatrix();
		    	        m.postTranslate(dx, dy);
		    	        
		    	        setImageMatrix(m);
		    	        setScaleType(ScaleType.MATRIX);
		    	        invalidate();
					}
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
    
    public void updateLocation(float cx, float cy) {
    	circleX = cx;
    	circleY = cy;
    	invalidate();
    }
    
	// Translates imageview so that the current location (circle) is centered 
    public void showCurrentLocation() {
    	m = getImageMatrix();
    	float xoffset = 230;
    	float yoffset = 400;
    	final float dx = -getTransX(m) - circleX * getScale(m) + xoffset;
    	final float dy = -getTransY(m) - circleY * getScale(m) + yoffset;
    	m.postTranslate(dx, dy);
    	invalidate();
    }
    	
	private void zoomIn(float scale, float dist) {
		if(scale > MAX_SCALE) return; //keeps from zooming in too much
		
		m = getImageMatrix();
		m.postScale(zoomIn, zoomIn, getWidth()/2f, getHeight()/2f);
		
		circleRadius -= .05 * getScale(m);
		
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
	
	private void zoomOut(float scale, float dist) {
		if(scale < MIN_SCALE) return; //keeps from zooming out too much
		
		m = getImageMatrix();
		m.postScale(zoomOut, zoomOut, getWidth()/2f, getHeight()/2f);
		
		circleRadius += .05 * getScale(m);
		
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
	
}
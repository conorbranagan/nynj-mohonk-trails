package edu.newpaltz.nynjmohonk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * A view class, extending Android's ImageView, that is used for displaying a Map to the user.
 * This view includes many modifications to the original ImageView, including adjustments to 
 * the drawing where a dot is shown of the user's location on every update and having the ability
 * to zoom in and out and move the image.
 *
 */
public class MapView extends ImageView {
	public static final float MIN_SCALE = .5f;
	public static final float MAX_SCALE = 1.7f;
	public static final int GROW = 0;
	public static final int SHRINK = 1;
	
	public boolean isMultiTouch = false, doMove = true;
	private Matrix m, pm;
	private Bitmap myBitmap, pointBitmap, mapBitmap;
	private Paint p;
	public float prevX = -1, prevY = -1, curX, curY, nextX, nextY, dx, dy;
	public float distPre = -1, distCur, distMeasure, curRotation = 0;
	public float zoomIn = 1.05f, zoomOut = 0.95f, scale;
	public float circleX = -1, circleY = -1;
	private CompassListener cl = null;
	private Canvas bitmapCanvas;	
	private float bearing = 0.0f;
	private Context mContext;
	private Map myMap;
	private boolean firstDraw = true;
	
	private static native void renderBitmap(Bitmap bitmap, long time_ms, String filename, int oldCircleX, int oldCircleY, boolean firstDraw);

	
	/**
	 * Create a map view with the given context and attribute set. The image is set to an initial
	 * zoom level and to an initial rotation. The paint object is initialized to the color blue.
	 * @param c The current application context
	 * @param a An attribute set for this MapView
	 */
	public MapView(Context c, AttributeSet a) {
		super(c, a);
		pm = new Matrix();
		m = getImageMatrix();
		m.setScale(.8f, .8f);
		m.setRotate(40);
		centerOnPoint(1500, 1000, false);
		setImageMatrix(m);
		setScaleType(ScaleType.MATRIX);
		p = new Paint();
		p.setColor(Color.BLUE);
		mContext = c;
		invalidate();
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		mapBitmap = bm;
	}
	
	public void setMapObj(Map m) {
		myMap = m;
	}
	
	/**
	 * Sets the compass listener for this MapView which will be used in the onDraw method to show which direction
	 * the phone is pointing
	 * @param listener
	 */
	public void setCompass(CompassListener listener) { cl = listener; }
		
	/**
	 * @param m The ImageMatrix of the Drawable in our MapView
	 * @return The current scale of the image
	 */
    private float getScale(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MSCALE_X];
    }
    
    /**
     * @param m The ImageMatrix of the image
     * @return The current X translation of the image
     */
    private float getTransX(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MTRANS_X];
    }    
  
    /**
     * @param m The ImageMatrix of the image
     * @return The current Y translation of the image
     */
    private float getTransY(Matrix m) {
		float[] values = new float[9];
		this.getImageMatrix().getValues(values);
		return values[Matrix.MTRANS_Y];
    }    
    
    /**
     * Along with the basic draw which redraws the map, we use the user's current location to draw a circle on the
     * map showing their position. Also the map is rotated to reflect the direction that the phone is currently pointing
     * @param c The canvas of this image
     */
    @Override
    protected void onDraw(Canvas c) {    
    	super.onDraw(c);
    	renderBitmap(mapBitmap, 0, mContext.getFilesDir() + "/" + myMap.getFilename(), (int)circleX, (int)circleY, firstDraw);
    	firstDraw = false;
    	
    	if(pointBitmap == null) {
    		pointBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_point);
    	}
    	
    	Matrix l = new Matrix(getImageMatrix());
    	l.setTranslate(circleX - (pointBitmap.getWidth() / 2), circleY - (pointBitmap.getHeight() / 2));
    	l.preRotate(curRotation, pointBitmap.getWidth() / 2, pointBitmap.getHeight() / 2);
    	l.preScale(0.85f, 0.85f, pointBitmap.getWidth() / 2, pointBitmap.getHeight() / 2);
    	
    	if(cl != null) {
    		float forward = cl.getForward() - 90;
    		float offset = (Math.abs(forward) - Math.abs(curRotation));
    		
			
			if(offset >  5 || offset < -5) {
				l.preRotate(offset, pointBitmap.getWidth() / 2, pointBitmap.getHeight() / 2);
				curRotation += offset;
			}

    	}
    	
    	Canvas bitmapCanvas = new Canvas(mapBitmap);
    	bitmapCanvas.drawBitmap(pointBitmap, l, p);
    }
    
    /**
     * Depending on the type of touch for the event, the image will change in multiple ways. If it is a multi-touch
     * event, then the image will either be zoomed in or out, depending on the movement of the fingers. If it's a single
     * touch event, then the image will be translated in the X or Y direction, depending on the movement of the fingers
     * @param event The current motion event that caused this method to be called
     */
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
				doMove = true;
				break;
			}
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				isMultiTouch = true;
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
		    	        pm.postTranslate(dx, dy);
		    	        
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
			    			zoomIn(scale);
			    		break;
			    		case SHRINK: // detect fingers pinching
			    			zoomOut(scale);
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
    
    /**
     * Updates the location of the circle on the map and redraws the image. This is called from our MapViewActivity
     * when the location of the phone changes.
     * @param cx The X position of the circle
     * @param cy The Y position of the circle
     */
    public void updateLocation(float cx, float cy) {
    	circleX = cx;
    	circleY = cy;
    	invalidate();
    }
    
    /**
     * Centers the view window on the current location of the user
     * @param lat The current latitude location
     * @param lon The current longitude location
     */
    public void showCurrentLocation(double lat, double lon) {
    	centerOnPoint(circleX, circleY, true);
    }
    
    public void setBearing(float b) { this.bearing = b; }
    
    /**
     * Centers to the given coordinate
     * @param x The x coordinate to center on
     * @param y The y coordinate to center on
     * @param post Tells whether this is after the map has been drawn or not
     */
    public void centerOnPoint(float x, float y, boolean post) {
    	m = getImageMatrix();
       	float xoffset = 150, yoffset = 500; // these values should probably not be hardcoded
    	final float dx = -getTransX(m) - x * getScale(m) + xoffset;
    	final float dy = -getTransY(m) - y * getScale(m) + yoffset;
    	    	
    	if(post) {
    		m.postTranslate(dx, dy);   
	        pm.postTranslate(dx, dy);
        	invalidate();
    	}
    	else {
    		m.setTranslate(dx, dy);
	        pm.setTranslate(dx, dy);
    	}
    }
    
    /**
     * Zooms the image in to the given scale
     * @param scale The scale to zoom the image to
     */
	private void zoomIn(float scale) {
		if(scale > MAX_SCALE) return; //keeps from zooming in too much
		
		m = getImageMatrix();
		m.postScale(zoomIn, zoomIn, getWidth()/2f, getHeight()/2f);
		
		//circleRadius -= .05 * getScale(m);
		
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
	
	/**
	 * Zooms the image out to the given scale
	 * @param scale 
	 */
	private void zoomOut(float scale) {
		if(scale < MIN_SCALE) return; //keeps from zooming out too much

		m = getImageMatrix();
		m.postScale(zoomOut, zoomOut, getWidth()/2f, getHeight()/2f);
		
		//circleRadius += .05 * getScale(m);
		
		setImageMatrix(m);
        setScaleType(ScaleType.MATRIX);
        invalidate();
		return;
	}
	
	/**
	 * Forces a recycle of the bitmap so that when the activity is run again it doesn't run out of space
	 */
	public void closeDown() {
		if(myBitmap != null) myBitmap.recycle();
		if(pointBitmap != null) pointBitmap.recycle();
		myBitmap = null;
		pointBitmap = null;
		System.gc();
	}
	
}
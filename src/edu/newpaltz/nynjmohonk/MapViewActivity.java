package edu.newpaltz.nynjmohonk;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;


public class MapViewActivity extends Activity {
	LinearLayout mLinearLayout;
	ImageView i;
	float lastX = -1, lastY = -1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mLinearLayout = new LinearLayout(this);
        i = new ImageView(this);
        i.setImageResource(R.drawable.mohonk_map);
        
        mLinearLayout.addView(i);
        setContentView(mLinearLayout);
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
    	Log.d("NYNJ Trails", "Moving " + dx + ", " + dy);
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
}
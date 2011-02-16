package edu.newpaltz.nynjmohonk;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;


public class MapViewActivity extends Activity {
	LinearLayout mLinearLayout;
	ImageView i;
	
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
        Matrix m = i.getImageMatrix();
        m.postTranslate(3, 3);
        i.setImageMatrix(m);
        i.setScaleType(ScaleType.MATRIX);
        //i.invalidate();
        return true;
    }
}
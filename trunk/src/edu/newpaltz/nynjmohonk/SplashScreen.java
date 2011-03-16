package edu.newpaltz.nynjmohonk;	

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;


public class SplashScreen extends Activity {	
	protected boolean _active = true, continueOn = true;
	protected int _splashTime = 5000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);
	 
	
	    Thread splashTread = new Thread() {
	        @Override
	        public void run() {
	            try {
	                int waited = 0;
	                while(_active && (waited < _splashTime)) {
	                    sleep(100);
	                    if(_active) {
	                        waited += 100;
	                    }
	                }
	            } catch(InterruptedException e) {
	                // do nothing
	            } finally {
	            	if(continueOn) {
	            		startActivity(new Intent("edu.newpaltz.nynjmohonk.MainMenu"));
	            	}
	            	finish();
	                stop();
	            }
	        }
	    };
	    splashTread.start();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  // TODO Auto-generated method stub
	  if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_HOME){
	    continueOn = false; _active =false;
	  }
	 
	  return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        _active = false;
	    }
	    return true;
	}
}
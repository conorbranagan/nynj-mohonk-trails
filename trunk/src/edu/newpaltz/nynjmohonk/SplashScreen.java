package edu.newpaltz.nynjmohonk;	

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashScreen extends Activity {	
	//protected boolean _active = true, continueOn = true;
	protected int _splashTime = 5000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);

	    new Handler().postDelayed(new Runnable(){
	    	@Override
	    	public void run() {
	    		finish();
	    		startActivity(new Intent("edu.newpaltz.nynjmohonk.MainMenu"));
	    	}
	    }, _splashTime);
	}
}




package edu.newpaltz.nynjmohonk;	

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


public class SplashScreen extends Activity {	
	//protected boolean _active = true, continueOn = true;
	protected int _splashTime = 5000;
	Toast t;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);

	    new Handler().postDelayed(new Runnable(){
	    	@Override
	    	public void run() {
	    		finish();
	    		startActivity(new Intent("edu.newpaltz.nynjmohonk.MainMenu"));
	    		System.exit(0);
	    	}
	    }, _splashTime);
	    
	    ImageView myImg= (ImageView) findViewById(R.id.npcslogo);
	    myImg.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View view) {
	                	t = Toast.makeText(SplashScreen.this, "Created by Conor Branagan and Jacquelyn Sagaas of the SUNY New Paltz Computer Science Department", Toast.LENGTH_LONG);
						t.show();
	                }
	            });
	}
	
}




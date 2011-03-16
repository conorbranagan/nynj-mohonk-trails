package edu.newpaltz.nynjmohonk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends Activity {
	Button openMap, closeApp;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        openMap = (Button) findViewById(R.id.launchMap);
        openMap.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this, MapViewActivity.class));
			}
        });
        closeApp = (Button) findViewById(R.id.exit);
        closeApp.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });
	}
}
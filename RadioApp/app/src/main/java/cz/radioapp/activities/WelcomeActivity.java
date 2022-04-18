package cz.radioapp.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import cz.radioapp.AppStorage;
import cz.radioapp.R;

public class WelcomeActivity extends AppCompatActivity {
		
	private AppStorage appStorage;
	
	
	@RequiresApi(api = Build.VERSION_CODES.O)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_welcome);
		
		// Nastavení orientace na výšku
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		// Nastavení režimu celé obrazovky
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Skrýt panel akcí
		getSupportActionBar().hide();
		
		appStorage = new AppStorage(this);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				if (appStorage.getSelectedStationIndex() != -1) {
					
					// Přesměrování na hlavní aktivitu
					startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
					
				// Přesměrování na výběr stanice, při prvotním spuštění
				} else startActivity(new Intent(WelcomeActivity.this, StationSelectionActivity.class));
			}
			
		}, 1500);
	}
		
}
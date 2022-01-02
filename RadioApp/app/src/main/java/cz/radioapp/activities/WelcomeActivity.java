package cz.radioapp.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import cz.radioapp.R;

public class WelcomeActivity extends AppCompatActivity {
		
		/**
		 * Vytvoření aktivity
		 */
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
				
				new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
								
								// Přesměrování na hlavní aktivitu
								startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
						}
						
				}, 1500);
		}
		
}
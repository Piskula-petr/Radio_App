package cz.radioapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import cz.radioapp.AppStorage;
import cz.radioapp.R;

public class SettingsActivity extends AppCompatActivity {
	
	private TextView textViewQuality, textViewQualityDescription, textViewAutoPlayDescription, textViewVolumeDescription, textViewInitialLevel;
	private Switch switchQuality, switchAutoPlay, switchVolume;
	private SeekBar seekBarInitialVolume;
	private ConstraintLayout constraintLayoutVolume;
	private AudioManager audioManager;
	
	private AppStorage appStorage;
	
	private boolean initHighQualityState;
	private boolean isQualityStateChanged = false;
	private boolean initAutoPlayState;
	private boolean isAutoPlayStateChanged = false;
	private boolean initialVolumeState;
	private boolean isInitialVolumeChanged = false;
	
	
	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		// Nastavení orientace na výšku
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		// Nastavení panelu akcí
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.settings));
		
		appStorage = new AppStorage(SettingsActivity.this);
		initHighQualityState = appStorage.getHighQualityState();
		initAutoPlayState = appStorage.getAutoPlayState();
		initialVolumeState = appStorage.getInitialVolumeState();
		
		// Kvalita přehrávání
		textViewQuality = findViewById(R.id.textViewQuality);
		textViewQuality.setText((initHighQualityState ? getString(R.string.high_quality) : getString(R.string.low_quality)));
		
		// Popis kvality přehrávání
		textViewQualityDescription = findViewById(R.id.textViewQualityDescription);
		textViewQualityDescription.setText((initHighQualityState ? getString(R.string.change_to_low_quality) : getString(R.string.change_to_high_quality)));
		
		// Výběr kvality přehrávání
		switchQuality = findViewById(R.id.switchQuality);
		switchQuality.setChecked(initHighQualityState);
		switchQuality.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				isQualityStateChanged = (isChecked != initHighQualityState ? true : false);
				
				appStorage.saveHighQualityState(isChecked);
				
				textViewQuality.setText((appStorage.getHighQualityState() ? getString(R.string.high_quality) : getString(R.string.low_quality)));
				textViewQualityDescription.setText((appStorage.getHighQualityState() ? getString(R.string.change_to_low_quality) : getString(R.string.change_to_high_quality)));
			}
		});
		
		// Popis automatického přehrávání
		textViewAutoPlayDescription = findViewById(R.id.textViewAutoPlayDescription);
		textViewAutoPlayDescription.setText((initAutoPlayState ? getString(R.string.change_to_disable_auto_play) : getString(R.string.change_to_enable_auto_play)));
		
		// Výběr automatického přehrávání
		switchAutoPlay = findViewById(R.id.switchAutoPlay);
		switchAutoPlay.setChecked(appStorage.getAutoPlayState());
		switchAutoPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				isAutoPlayStateChanged = (isChecked != initAutoPlayState ? true : false);
				
				appStorage.saveAutoPlayState(isChecked);
				
				textViewAutoPlayDescription.setText((appStorage.getAutoPlayState() ? getString(R.string.change_to_disable_auto_play) : getString(R.string.change_to_enable_auto_play)));
			}
		});

		// Popis počáteční hlasitosti
		textViewVolumeDescription = findViewById(R.id.textViewVolumeDescription);
		textViewVolumeDescription.setText((initialVolumeState ? getString(R.string.change_to_system_volume) : getString(R.string.change_to_custom_volume)));
		
		// Výběr typu počáteční hlasitosti
		switchVolume = findViewById(R.id.switchVolume);
		switchVolume.setChecked(appStorage.getInitialVolumeState());
		switchVolume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				isInitialVolumeChanged = (isChecked != initialVolumeState ? true : false);

				appStorage.saveInitialVolumeState(isChecked);

				textViewVolumeDescription.setText((appStorage.getInitialVolumeState() ? getString(R.string.change_to_system_volume) : getString(R.string.change_to_custom_volume)));
				constraintLayoutVolume.setVisibility((appStorage.getInitialVolumeState() ? View.VISIBLE : View.INVISIBLE));
			}
		});
		
		constraintLayoutVolume = findViewById(R.id.constraintLayoutCustomVolume);
		constraintLayoutVolume.setVisibility((appStorage.getInitialVolumeState() ? View.VISIBLE : View.INVISIBLE));
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		// Posuvník počáteční hlasitosti
		seekBarInitialVolume = findViewById(R.id.seekBarInitialVolume);
		seekBarInitialVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		seekBarInitialVolume.setProgress(appStorage.getCustomVolume());
		seekBarInitialVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
				appStorage.saveInitialVolumeValue(progress);
				
				textViewInitialLevel.setText(appStorage.getCustomVolume() + " / " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		// Úrověň počáteční hlasitosti
		textViewInitialLevel = findViewById(R.id.textViewInitialLevel);
		textViewInitialLevel.setText(appStorage.getCustomVolume() + " / " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
	}
	
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		
		boolean anyChanges = (isQualityStateChanged || isAutoPlayStateChanged || isInitialVolumeChanged ? true : false);
		
		// Ukončení aktivity, při stisku návratového tlačítka
		if (item.getItemId() == android.R.id.home && !anyChanges) {

			finish();

			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
		
}
package cz.radioapp.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import cz.radioapp.AppStorage;
import cz.radioapp.R;

public class SettingsActivity extends AppCompatActivity {
		
	private TextView textViewQuality;
	private TextView textViewQualityDetail, textViewAutoPlayDetail;
	private Switch switchQuality, switchAutoPlay;
	
	private AppStorage appStorage;
	
	private boolean initHighQualityState;
	private boolean isQualityStateChanged = false;
	private boolean initAutoPlayState;
	private boolean isAutoPlayStateChanged = false;
	
	
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
		
		// Kvalita přehrávání
		textViewQuality = findViewById(R.id.textViewQuality);
		textViewQuality.setText((initHighQualityState ? getString(R.string.high_quality) : getString(R.string.low_quality)));
		
		// Detail pro změnu kvality
		textViewQualityDetail = findViewById(R.id.textViewQualityDetail);
		textViewQualityDetail.setText((initHighQualityState ? getString(R.string.change_to_low_quality) : getString(R.string.change_to_high_quality)));
		
		// Výběr kvality přehrávání
		switchQuality = findViewById(R.id.switchQuality);
		switchQuality.setChecked(initHighQualityState);
		switchQuality.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				isQualityStateChanged = (isChecked != initHighQualityState ? true : false);
				
				appStorage.saveHighQualityState(isChecked);
				
				textViewQuality.setText((appStorage.getHighQualityState() ? getString(R.string.high_quality) : getString(R.string.low_quality)));
				textViewQualityDetail.setText((appStorage.getHighQualityState() ? getString(R.string.change_to_low_quality) : getString(R.string.change_to_high_quality)));
			}
		});
		
		// Detail automatického přehrávání
		textViewAutoPlayDetail = findViewById(R.id.textViewAutoPlayDetail);
		textViewAutoPlayDetail.setText((initAutoPlayState ? getString(R.string.change_to_disable_auto_play) : getString(R.string.change_to_enable_auto_play)));
		
		// Výběr automatického přehrávání
		switchAutoPlay = findViewById(R.id.switchAutoPlay);
		switchAutoPlay.setChecked(appStorage.getAutoPlayState());
		switchAutoPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				isAutoPlayStateChanged = (isChecked != initAutoPlayState ? true : false);
				
				appStorage.saveAutoPlayState(isChecked);
				
				textViewAutoPlayDetail.setText((appStorage.getAutoPlayState() ? getString(R.string.change_to_disable_auto_play) : getString(R.string.change_to_enable_auto_play)));
			}
		});
	}
	
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		
		boolean anyChanges = (isQualityStateChanged || isAutoPlayStateChanged ? true : false);
		
		// Ukončení aktivity, při stisku návratového tlačítka
		if (item.getItemId() == android.R.id.home && !anyChanges) {

			finish();

			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
		
}
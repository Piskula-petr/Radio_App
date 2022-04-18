package cz.radioapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cz.radioapp.AppStorage;
import cz.radioapp.R;
import cz.radioapp.recycler_view.OnItemClickListener;
import cz.radioapp.recycler_view.StationNamesRecyclerViewAdapter;

public class StationSelectionActivity extends AppCompatActivity {
		
	private RecyclerView recyclerViewStationNames;
	private StationNamesRecyclerViewAdapter stationNamesRecyclerViewAdapter;
	
	private AppStorage appStorage;
	
	
	@SuppressLint("RestrictedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_station_selection);
		
		// Nastavení orientace na výšku
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		appStorage = new AppStorage(this);
		
		boolean isBackButtonVisible = (appStorage.getSelectedStationIndex() != -1) ? true : false;
		
		// Nastavení panelu akcí
		getSupportActionBar().setDisplayHomeAsUpEnabled(isBackButtonVisible);
		getSupportActionBar().setTitle(getString(R.string.station_selection));
		
		stationNamesRecyclerViewAdapter = new StationNamesRecyclerViewAdapter(this);
		stationNamesRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(int position) {
				
				// Uložení indexu vybrané stanice
				appStorage.saveSelectedStationIndex(position);
				
				sendBroadcast(new Intent(getString(R.string.media_player_stop_start_playing)));
				
				// Přesměrování na hlavní aktivitu
				startActivity(new Intent(StationSelectionActivity.this, MainActivity.class));
			}
		});
		
		recyclerViewStationNames = findViewById(R.id.recyclerViewStationNames);
		recyclerViewStationNames.setAdapter(stationNamesRecyclerViewAdapter);
		recyclerViewStationNames.setLayoutManager(new LinearLayoutManager(this));
	}
	
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		
		// Ukončení aktivity, při stisku návratového tlačítka
		if (item.getItemId() == android.R.id.home) {
			
			finish();
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
		
}
package cz.radioapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cz.radioapp.AppStorage;
import cz.radioapp.R;
import cz.radioapp.recycler_view.OnItemClickListener;
import cz.radioapp.recycler_view.StationNamesRecyclerViewAdapter;

public class StationSelectionActivity extends AppCompatActivity {
		
		private AppStorage appStorage;
		
		private RecyclerView recyclerViewStationNames;
		private StationNamesRecyclerViewAdapter stationNamesRecyclerViewAdapter;
		
		
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
				getSupportActionBar().setTitle("Výběr stanice");
				
				stationNamesRecyclerViewAdapter = new StationNamesRecyclerViewAdapter(this);
				stationNamesRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
						
						@Override
						public void onItemClick(int position) {
								
								// Uložení indexu vybrané stanice
								appStorage.saveSelectedStationIndex(position);
								
								sendBroadcast(new Intent(getString(R.string.image_button_play_pause_click)));
								
								// Přesměrování na hlavní aktivitu
								startActivity(new Intent(StationSelectionActivity.this, MainActivity.class));
						}
				});
				
				recyclerViewStationNames = findViewById(R.id.recyclerViewStationNames);
				recyclerViewStationNames.setAdapter(stationNamesRecyclerViewAdapter);
				recyclerViewStationNames.setLayoutManager(new LinearLayoutManager(this));
		}
}
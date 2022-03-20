package cz.radioapp.recycler_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import cz.radioapp.R;

public class StationNamesRecyclerViewAdapter extends RecyclerView.Adapter<StationNamesRecyclerViewHolder> {
		
		private List<String> stationNames;
		private LayoutInflater layoutInflater;
		private OnItemClickListener onItemClickListener;
		
		
		/**
		 * Konstruktor
		 *
		 * @param context
		 */
		public StationNamesRecyclerViewAdapter(Context context) {
				stationNames = Arrays.asList(context.getResources().getStringArray(R.array.station_names));
				layoutInflater = LayoutInflater.from(context);
		}
		
		
		@NonNull
		@Override
		public StationNamesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				
				View view = layoutInflater.inflate(R.layout.recycler_view_station_names_item, parent, false);
				StationNamesRecyclerViewHolder namesRecyclerViewHolder = new StationNamesRecyclerViewHolder(view, onItemClickListener);
				
				return namesRecyclerViewHolder;
		}
		
		
		@Override
		public void onBindViewHolder(@NonNull StationNamesRecyclerViewHolder viewHolder, int i) {
		
				viewHolder.textViewStationRowName.setText(stationNames.get(i));
		}
		
		
		@Override
		public int getItemCount() {
				return stationNames.size();
		}
		
		
		public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
				this.onItemClickListener = onItemClickListener;
		}
		
}

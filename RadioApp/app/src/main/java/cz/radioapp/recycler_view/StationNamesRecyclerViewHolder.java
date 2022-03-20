package cz.radioapp.recycler_view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cz.radioapp.R;

public class StationNamesRecyclerViewHolder extends RecyclerView.ViewHolder {
		
		protected TextView textViewStationRowName;
		
		
		/**
		 * Konstruktor
		 *
		 * @param itemView
		 */
		public StationNamesRecyclerViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
				super(itemView);
				
				textViewStationRowName = itemView.findViewById(R.id.textViewStationRowName);
				itemView.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
								
								if (onItemClickListener != null) onItemClickListener.onItemClick(getAdapterPosition());
						}
				});
		}
}

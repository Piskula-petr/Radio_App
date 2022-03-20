package cz.radioapp;

import android.content.Context;
import android.content.SharedPreferences;

public class AppStorage {

		private Context context;
		
		
		/**
		 * Konstruktor
		 *
		 * @param context
		 */
		public AppStorage(Context context) {
				this.context = context;
		}
		
		
		/**
		 * Uložení indexu vybrané stanice
		 *
		 * @param index
		 */
		public void saveSelectedStationIndex(int index) {
				
				SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
				editor.putInt("selectedStation", index);
				editor.apply();
		}
		
		
		/**
		 * Získání indexu vybrané stanice
		 *
		 * @return - vrací index vybrané stanice [výchozí hondota -1]
		 */
		public int getSelectedStationIndex() {
				
				SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
				int selectedStation = sharedPreferences.getInt("selectedStation", -1);
				
				return selectedStation;
		}
		
}

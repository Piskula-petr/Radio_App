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
		
		
		/**
		 * Uložení stavu o vysoké kvalitě přehrávání
		 *
		 * @param isHighQualityEnable
		 */
		public void saveHighQualityState(boolean isHighQualityEnable) {
				
				SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
				editor.putBoolean("quality", isHighQualityEnable);
				editor.apply();
		}
		
		
		/**
		 * Získání stavu o vysoké kvalitě přehrávání
		 *
		 * @return - vrací boolean hodnotu true - výsoká kvalita / false - nízká kvalita [výchozí hodnota true]
		 */
		public boolean getHighQualityState() {
				
				SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
				boolean isHighQualityEnable = sharedPreferences.getBoolean("quality", true);
				
				return isHighQualityEnable;
		}
		
		
		/**
		 * Uložení stavu automatického přehrávání
		 *
		 * @param isEnableAutoPlay
		 */
		public void saveAutoPlayState(boolean isEnableAutoPlay) {
				
				SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
				editor.putBoolean("autoPlay", isEnableAutoPlay);
				editor.apply();
		}
		
		
		/**
		 * Získání stavu o automatickém přehrávání
		 *
		 * @return - vrací boolean hodnotu true - automatické přehrávání povoleno / false - automatické přehrávání zakázáno
		 */
		public boolean getAutoPlayState() {
				
				SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
				boolean isEnableAutoPlay = sharedPreferences.getBoolean("autoPlay", false);
				
				return isEnableAutoPlay;
		}
		
}

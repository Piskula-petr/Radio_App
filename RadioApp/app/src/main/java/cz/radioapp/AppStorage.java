package cz.radioapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

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
	 * @return - vrací true - výsoká kvalita / false - nízká kvalita [výchozí hodnota true]
	 */
	public boolean getHighQualityState() {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		boolean isHighQualityEnable = sharedPreferences.getBoolean("quality", true);
		
		return isHighQualityEnable;
	}
	
	
	/**
	 * Uložení stavu automatického přehrávání
	 *
	 * @param isAutoPlayEnable
	 */
	public void saveAutoPlayState(boolean isAutoPlayEnable) {
		
		SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
		editor.putBoolean("autoPlay", isAutoPlayEnable);
		editor.apply();
	}
	
	
	/**
	 * Získání stavu o automatickém přehrávání
	 *
	 * @return - vrací true - automatické přehrávání povoleno / false - automatické přehrávání zakázáno
	 */
	public boolean getAutoPlayState() {
		
		SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		boolean isAutoPlayEnable = sharedPreferences.getBoolean("autoPlay", false);
		
		return isAutoPlayEnable;
	}


	/**
	 * Uložení stavu počáteční hlasitosti
	 *
	 * @param isInitialVolumeEnable
	 */
	public void saveInitialVolumeState(boolean isInitialVolumeEnable) {

		SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
		editor.putBoolean("initialVolume", isInitialVolumeEnable);
		editor.apply();
	}


	/**
	 * Získání stavu počáteční hlasitosti
	 *
	 * @return - vrací false - počáteční hlasitosti podle systému / true - počáteční hlasitosti podle nastavené hodnoty
	 */
	public boolean getInitialVolumeState() {

		SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		boolean isInitialVolumeEnable = sharedPreferences.getBoolean("initialVolume", false);

		return isInitialVolumeEnable;
	}
	
	
	/**
	 * Uložení počáteční úrovně hlasitosti
	 *
	 * @param volumeLevel
	 */
	public void saveInitialVolumeValue(int volumeLevel) {
		
		SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
		editor.putInt("initialVolumeValue", volumeLevel);
		editor.apply();
	}
	
	
	/**
	 * Získání počáteční úrovně hlasitosti
	 *
	 * @return - vrací počáteční úrověň hlasitosti / default - polovina hlasitosti
	 */
	public int getCustomVolume() {
		
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		int initialVolume = sharedPreferences.getInt("initialVolumeValue", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2);
		
		return initialVolume;
	}
	
}

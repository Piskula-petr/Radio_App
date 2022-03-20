package cz.radioapp.activities;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import cz.radioapp.R;

public class StreamService extends Service {
		
		private String stationName;
		private String stationDataSource;
		private boolean mediaPlayerPlaying = false;
		
		private MediaPlayer mediaPlayer;
		
		private BroadcastReceiver imageButtonPlayPauseClickReceiver = new BroadcastReceiver() {
				
				@RequiresApi(api = Build.VERSION_CODES.M)
				@Override
				public void onReceive(Context context, Intent intent) {
						
						// Zastavení přehrávání
						if (mediaPlayerPlaying) stopPlaying();
						
						// Spuštění přehrávání
						else startPlaying();
				}
		};
		
		
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
				
				stationName = intent.getStringExtra("stationName");
				stationDataSource = intent.getStringExtra("stationDataSource");
				
				// Vytvoření notifikace
				Notification notification = new NotificationCompat.Builder(this, getString(R.string.stream_service_channel_id))
						.setContentTitle(stationName)
						.setSmallIcon(R.drawable.icon)
						.build();
				
				startForeground(1, notification);
				
				// Registrace receivru
				registerReceiver(imageButtonPlayPauseClickReceiver, new IntentFilter(getString(R.string.image_button_play_pause_click)));
				
				// Přehrávač
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setLooping(true);
				mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						
						@Override
						public void onPrepared(MediaPlayer mp) {
								
								sendBroadcast(new Intent(getString(R.string.media_player_prepared)));
								
								mediaPlayer.start();
						}
				});
				
				return START_NOT_STICKY;
		}
		
		
		@Override
		public void onDestroy() {
				super.onDestroy();
				
				if (mediaPlayer != null) mediaPlayer.release();
				
				unregisterReceiver(imageButtonPlayPauseClickReceiver);
		}
		
		
		@Nullable
		@Override
		public IBinder onBind(Intent intent) {
				return null;
		}
		
		
		/**
		 * Spuštění přehrávání
		 */
		@RequiresApi(api = Build.VERSION_CODES.M)
		private void startPlaying() {
				
				try {
						
						mediaPlayer.setDataSource(stationDataSource);
						mediaPlayer.prepareAsync();
						
				} catch (IOException e) {
						e.printStackTrace();
				}
				
				mediaPlayerPlaying = true;
				
				sendBroadcast(new Intent(getString(R.string.media_player_start_playing)));
		}
		
		
		/**
		 * Zastavení přehrávání
		 */
		@RequiresApi(api = Build.VERSION_CODES.M)
		private void stopPlaying() {
				
				mediaPlayer.reset();
				
				mediaPlayerPlaying = false;
				
				sendBroadcast(new Intent(getString(R.string.media_player_stop_playing)));
		}
		
}

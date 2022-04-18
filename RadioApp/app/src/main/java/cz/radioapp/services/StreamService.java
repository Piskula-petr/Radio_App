package cz.radioapp.services;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import cz.radioapp.AppStorage;
import cz.radioapp.R;

public class StreamService extends Service {

    private MediaPlayer mediaPlayer;

    private AppStorage appStorage;

    private String stationName;
    private String stationDataSource;
    private boolean mediaPlayerPlaying = false;

    private BroadcastReceiver mediaPlayerStopStartPlayingReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
		
		boolean isAudioDeviceDisconnected = intent.getBooleanExtra("isBluetoothDeviceDisconnected", false);
		
        // Zastavení přehrávání
        if (mediaPlayerPlaying || isAudioDeviceDisconnected)
        	stopPlaying();

        // Spuštění přehrávání
        else startPlaying(true);
        }
    };
    
    private BroadcastReceiver mediaPlayerRestartPlayingReceiver = new BroadcastReceiver() {
    
		@RequiresApi(api = Build.VERSION_CODES.M)
		@Override
		public void onReceive(Context context, Intent intent) {
		
		boolean isConnectedViaWifi = intent.getBooleanExtra("isConnectedViaWifi", false);
		
		stopPlaying();
		
		// Vteřinový delay, při wifi připojení
		if (isConnectedViaWifi) {
			
			try {
				
				Thread.sleep(1_000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		startPlaying(false);
		}
	};


    @RequiresApi(api = Build.VERSION_CODES.M)
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

        // Registrace receivrů
        registerReceiver(mediaPlayerStopStartPlayingReceiver, new IntentFilter(getString(R.string.media_player_stop_start_playing)));
        registerReceiver(mediaPlayerRestartPlayingReceiver, new IntentFilter(getString(R.string.media_player_restart_playing)));

        // Přehrávač
        mediaPlayer = new MediaPlayer();
        //mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                sendBroadcast(new Intent(getString(R.string.media_player_prepared)));

                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {

                Intent intent = new Intent(getString(R.string.media_player_buffering));
                
                switch (what) {

                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    	
                        intent.putExtra("isBuffering", true);
                        sendBroadcast(intent);
                        break;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    	
                        intent.putExtra("isBuffering", false);
                        sendBroadcast(intent);
                        break;
                }

                return false;
            }
        });

        appStorage = new AppStorage(this);

        // Automatické spuštění přehrávání
        if (appStorage.getAutoPlayState()) startPlaying(true);

        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) mediaPlayer.release();

        // Zrušení receiverů
        unregisterReceiver(mediaPlayerStopStartPlayingReceiver);
        unregisterReceiver(mediaPlayerRestartPlayingReceiver);
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
    private void startPlaying(boolean displayQualityNotification) {

        AppStorage appStorage = new AppStorage(this);
	
		// Zobrazení aktuální kvality přehrávání
        if (displayQualityNotification)
			Toast.makeText(this, (appStorage.getHighQualityState()
				? getString(R.string.high_quality)
					: getString(R.string.low_quality)), Toast.LENGTH_LONG).show();

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

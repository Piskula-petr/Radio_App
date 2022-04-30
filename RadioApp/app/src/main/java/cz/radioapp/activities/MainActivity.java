package cz.radioapp.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import cz.radioapp.AppStorage;
import cz.radioapp.R;
import cz.radioapp.enums.ConnectionType;
import cz.radioapp.services.StreamService;
import wseemann.media.FFmpegMediaMetadataRetriever;
import wseemann.media.FFmpegMediaMetadataRetriever.Metadata;

public class MainActivity extends AppCompatActivity {

    private ImageButton imageButtonPlayPause;
    private TextView textViewStationName, textViewSongName;
    private ImageView imageViewStationNames, imageViewSettings, imageViewVolume, imageViewConnectivity;
    private SeekBar seekBarVolume;
    private ProgressBar progressBar;
    private AudioManager audioManager;
    private Animation animation;
    
    private UpdateSongName updateSongName;
    private AppStorage appStorage;

    private String stationName;
    private String stationDataSource;
    private ConnectionType connectionType = ConnectionType.NONE;

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {

			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			Network network = connectivityManager.getActiveNetwork();
			
			if (network != null) {
				
				NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
	
				// Připojení přes wifi
				if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
	
					imageViewConnectivity.setImageResource(R.drawable.wifi);
	
					// Resetování přehrávače
					if (connectionType == ConnectionType.NETWORK || connectionType == ConnectionType.NONE)
						sendBroadcast(new Intent(getString(R.string.media_player_restart_playing)));
					
					connectionType = ConnectionType.WIFI;
	
				// Připojení přes mobilní síť
				} else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
	
					imageViewConnectivity.setImageResource(R.drawable.network);
	
					// Resetování přehrávače
					if (connectionType == ConnectionType.NONE)
						sendBroadcast(new Intent(getString(R.string.media_player_restart_playing)));
		
					connectionType = ConnectionType.NETWORK;
				}
	
			// Žádné připojení
			} else {
	
				imageViewConnectivity.setImageResource(R.drawable.no_connectivity);
	
				sendBroadcast(new Intent(getString(R.string.media_player_stop_start_playing)));
				
				connectionType = ConnectionType.NONE;
			}
        }
    };
    
    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
	
			Intent streamServiceIntent = new Intent();
			
			// Odpojení audio zařízení
			if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
				
				int plugState = intent.getIntExtra("state", -1);
				
				if (plugState == 0) {
					
					streamServiceIntent.setAction(getString(R.string.media_player_stop_start_playing));
					streamServiceIntent.putExtra("isAudioDeviceDisconnected", true);
					sendBroadcast(streamServiceIntent);
					
					Toast.makeText(MainActivity.this, getString(R.string.audio_device_disconnected), Toast.LENGTH_LONG).show();
				}
			
        	// Odpojení bluetooth zařízení
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
	
				streamServiceIntent.setAction(getString(R.string.media_player_stop_start_playing));
				streamServiceIntent.putExtra("isAudioDeviceDisconnected", true);
				sendBroadcast(streamServiceIntent);
				
                Toast.makeText(MainActivity.this, getString(R.string.bluetooth_device_disconnected), Toast.LENGTH_LONG).show();
            
            // Připojení bluetooth zažízení
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
	
				streamServiceIntent.setAction(getString(R.string.media_player_restart_playing));
				streamServiceIntent.putExtra("isConnectedViaWifi", true);
				sendBroadcast(streamServiceIntent);
            	
            	Toast.makeText(MainActivity.this, getString(R.string.bluetooth_device_connected), Toast.LENGTH_LONG).show();
			}
        }
    };

    private BroadcastReceiver mediaPlayerPreparedReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
        
			textViewSongName.setVisibility(View.VISIBLE);
			imageButtonPlayPause.setEnabled(true);
        }
    };
    
    private BroadcastReceiver mediaPlayerBuffering = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            
			boolean isBuffering = intent.getBooleanExtra("isBuffering", false);
			
			progressBar.setVisibility((isBuffering ? View.VISIBLE : View.INVISIBLE));
			textViewSongName.setVisibility((isBuffering ? View.INVISIBLE : View.VISIBLE));
        }
    };
    
    private BroadcastReceiver mediaPlayerStartPlayingReceiver = new BroadcastReceiver() {
        
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
    
			imageButtonPlayPause.setForeground(getDrawable(R.drawable.pause));
			textViewSongName.setVisibility(View.VISIBLE);
			
			updateSongName.start();
        }
    };
    
    private BroadcastReceiver mediaPlayerStopPlayingReceiver = new BroadcastReceiver() {
        
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
    
			imageButtonPlayPause.setForeground(getDrawable(R.drawable.play));
			textViewSongName.setVisibility(View.INVISIBLE);
			
			updateSongName.reset();
        }
    };
    
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        // Nastavení orientace na výšku
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        
        // Skrytí panelu akcí
        getSupportActionBar().hide();
    
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        
        // Registrace receiverů
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(audioReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(audioReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(audioReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(mediaPlayerPreparedReceiver, new IntentFilter(getString(R.string.media_player_prepared)));
        registerReceiver(mediaPlayerStartPlayingReceiver, new IntentFilter(getString(R.string.media_player_start_playing)));
        registerReceiver(mediaPlayerStopPlayingReceiver, new IntentFilter(getString(R.string.media_player_stop_playing)));
        registerReceiver(mediaPlayerBuffering, new IntentFilter(getString(R.string.media_player_buffering)));
        
        appStorage = new AppStorage(this);
        int selectedStationIndex = appStorage.getSelectedStationIndex();
        
        // Výběr vysoké / nízké kvality přehrávání
        String[] stationsDataSource = getResources().getStringArray((appStorage.getHighQualityState()
            ? R.array.station_high_quality_data_sources
                : R.array.station_low_quality_data_sources));
        
        stationName = getResources().getStringArray(R.array.station_names)[selectedStationIndex];
        stationDataSource = stationsDataSource[selectedStationIndex];
        
        // Spuštění služby na pozadí
        Intent streamService = new Intent(this, StreamService.class);
        streamService.putExtra("stationName", stationName);
        streamService.putExtra("stationDataSource", stationDataSource);
        
        startService(streamService);
        
        // Ikona konektivity
        imageViewConnectivity = findViewById(R.id.imageViewConnectivity);
        
        // Název rádia
        textViewStationName = findViewById(R.id.textViewStationName);
        textViewStationName.setSelected(true);
        textViewStationName.setSingleLine(true);
        textViewStationName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textViewStationName.setText(stationName);
        
        // Název skladby
        textViewSongName = findViewById(R.id.textViewSongName);
        textViewSongName.setSelected(true);
        textViewSongName.setSingleLine(true);
        textViewSongName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
    
        progressBar = findViewById(R.id.progressBar);
        
        // Výběr názvu stanice
        imageViewStationNames = findViewById(R.id.imageViewStationNames);
        imageViewStationNames.setOnClickListener(new View.OnClickListener() {
        
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                
                imageViewStationNames.startAnimation(animation);
                
                // Přesměrování na výběr stanice
                startActivity(new Intent(MainActivity.this, StationSelectionActivity.class));
            }
        });
    
        // Nastavení
        imageViewSettings = findViewById(R.id.imageViewSettings);
        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                imageViewSettings.startAnimation(animation);
                
                // Přesměrování do nastavení
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        
        // Ikona hlasitosti
        imageViewVolume = findViewById(R.id.imageViewVolume);
    
        // Aktualizace názvu skladby
        updateSongName = new UpdateSongName(stationDataSource, 0);
        
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        int volumeLevel = appStorage.getInitialVolumeState()
			? appStorage.getCustomVolume()
				: audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0);
        
        // Posuvník hlasitosti
        seekBarVolume = findViewById(R.id.seekBarVolume);
        seekBarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setProgress(volumeLevel);
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    
                // Změna ikony hlasitosti
                setVolumeImage(progress, seekBarVolume.getMax());
                
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
    
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
    
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Změna ikony hlasitosti
        setVolumeImage(seekBarVolume.getProgress(), seekBarVolume.getMax());
        
        // Tlačítko přehrání / zastavení
        imageButtonPlayPause = findViewById(R.id.imageButtonPlayPause);
        imageButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                
                // Offline
                if (connectionType == ConnectionType.NONE) {
                    
                    Toast.makeText(getApplicationContext(), getString(R.string.check_connectivity), Toast.LENGTH_LONG).show();
                    
                // Online
                } else sendBroadcast(new Intent(getString(R.string.media_player_stop_start_playing)));
                
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Zrušení receiverů
        unregisterReceiver(connectivityReceiver);
        unregisterReceiver(audioReceiver);
        unregisterReceiver(mediaPlayerPreparedReceiver);
        unregisterReceiver(mediaPlayerStartPlayingReceiver);
        unregisterReceiver(mediaPlayerStopPlayingReceiver);
        unregisterReceiver(mediaPlayerBuffering);
        
        stopService(new Intent(this, StreamService.class));
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        switch (keyCode) {
    
            // Zvýšení hlasitosti
            case KeyEvent.KEYCODE_VOLUME_UP:
                seekBarVolume.setProgress(seekBarVolume.getProgress() + 1); break;
    
            // Snížení hlasitosti
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                seekBarVolume.setProgress(seekBarVolume.getProgress() - 1); break;
        }
        
        // Změna ikony hlasitosti
        setVolumeImage(seekBarVolume.getProgress(), seekBarVolume.getMax());
        
        return super.onKeyDown(keyCode, event);
    }
    
    
    /**
     * // Změna ikony hlasitosti
     *
     * @param currentVolume - aktuální hlasitost
     * @param maxVolume - maximální hlasitost
     */
    private void setVolumeImage(int currentVolume, int maxVolume) {
        
        int segmentSize = maxVolume / 3;
        
        // Ztlumění
        if (currentVolume == 0) {
            
            imageViewVolume.setImageResource(R.drawable.volume_mute);
            
        // Nízká hlasitost
        } else if (currentVolume > 0 && currentVolume < segmentSize) {
            
            imageViewVolume.setImageResource(R.drawable.volume_min);
            
        // Střední hlasitost
        } else if (currentVolume >= segmentSize && currentVolume < (segmentSize * 2)) {
            
            imageViewVolume.setImageResource(R.drawable.volume_medium);
         
        // Vysoká hlasitost
        } else if (currentVolume >= (segmentSize * 2)) {
            
            imageViewVolume.setImageResource(R.drawable.volume_max);
        }
    }
    
    
    /**
     * Aktualizace názvu skladby
     */
    private class UpdateSongName extends Thread {
    
        private FFmpegMediaMetadataRetriever metadataRetriever;
        
        private String url;
        private boolean isStop = false;
        private long initialDelay;
    
        
        /**
         * Konstruktor
         *
         * @param url
         */
        public UpdateSongName(String url, long initialDelay) {
            this.url = url;
            this.initialDelay = initialDelay;
            this.metadataRetriever = new FFmpegMediaMetadataRetriever();
        }
    
        
        /**
         * Start
         */
        @Override
        public void run() {
            
            isStop = false;
            long delay = initialDelay;
            
            // Kontrola názvu skladby, každých 20 sec
            while (!isStop) {
                
                try {
    
                    Thread.sleep(delay);
                    
                    metadataRetriever.setDataSource(url);
                    
                    Metadata metadata = metadataRetriever.getMetadata();
                    
                    if (metadata != null) {
                        
                        String newSongName = (metadata.has("StreamTitle") ? metadata.getString("StreamTitle") : "");
                        
                        textViewSongName.post(new Runnable() {
        
                            @Override
                            public void run() {
            
                                String oldSongName = (String) textViewSongName.getText();
            
                                // Změna názvu nové skladby
                                if (!oldSongName.equals(newSongName)) textViewSongName.setText(newSongName);
                            }
                        });
                    }
                    
                    delay = 20_000;
                    
                } catch (InterruptedException | IllegalArgumentException e) {
                    e.printStackTrace();
                    delay = 20_000;
                }
            }
        }
    
        
        /**
         * Přerušení
         */
        public void reset() {

            isStop = true;
        }
    }
    
}
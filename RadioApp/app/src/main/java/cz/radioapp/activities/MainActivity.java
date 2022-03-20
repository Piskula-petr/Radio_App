package cz.radioapp.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import cz.radioapp.AppStorage;
import cz.radioapp.R;
import wseemann.media.FFmpegMediaMetadataRetriever;
import wseemann.media.FFmpegMediaMetadataRetriever.Metadata;

public class MainActivity extends AppCompatActivity {

    private ImageButton imageButtonPlayPause;
    private TextView textViewStationName, textViewSongName;
    private ImageView imageViewStationNames, imageViewVolume;
    
    private SeekBar seekBarVolume;
    private AudioManager audioManager;
    private UpdateSongName updateSongName;
    
    private AppStorage appStorage;
    private String stationName;
    private String stationDataSource;
    
    private boolean mediaPlayerPlaying = false;
    private boolean noConnectivity = false;
    private boolean mediaPlayerStopBuffering = false;
    
    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            
            noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
    
            // Zastavení přehrávání
            if (noConnectivity) {
                
                imageButtonPlayPause.setForeground(getDrawable(R.drawable.play));
                textViewSongName.setVisibility(View.INVISIBLE);
            }
        }
    };
    
    private BroadcastReceiver mediaPlayerPreparedReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
    
            textViewSongName.setVisibility(View.VISIBLE);
            imageButtonPlayPause.setEnabled(true);
            
            mediaPlayerPlaying = true;
        }
    };
    
    private BroadcastReceiver mediaPlayerStartPlayingReceiver = new BroadcastReceiver() {
        
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
    
            imageButtonPlayPause.setForeground(getDrawable(R.drawable.pause));
            
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
        
        // Registrace receiverů
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mediaPlayerPreparedReceiver, new IntentFilter(getString(R.string.media_player_prepared)));
        registerReceiver(mediaPlayerStartPlayingReceiver, new IntentFilter(getString(R.string.media_player_start_playing)));
        registerReceiver(mediaPlayerStopPlayingReceiver, new IntentFilter(getString(R.string.media_player_stop_playing)));
        
        appStorage = new AppStorage(this);
        int selectedStationIndex = appStorage.getSelectedStationIndex();
        
        stationName = getResources().getStringArray(R.array.station_names)[selectedStationIndex];
        stationDataSource = getResources().getStringArray(R.array.station_high_quality_data_sources)[selectedStationIndex];
        
        // Spuštění služby na pozadí
        Intent streamService = new Intent(this, StreamService.class);
        streamService.putExtra("stationName", stationName);
        streamService.putExtra("stationDataSource", stationDataSource);
        
        startService(streamService);
        
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
    
        // Výběr názvu stanice
        imageViewStationNames = findViewById(R.id.imageViewStationNames);
        imageViewStationNames.setOnClickListener(new View.OnClickListener() {
        
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                
                // Přesměrování na výběr stanice
                startActivity(new Intent(MainActivity.this, StationSelectionActivity.class));
            }
        });
    
        // Ikona hlasitosti
        imageViewVolume = findViewById(R.id.imageViewVolume);
    
        // Aktualizace názvu skladby
        updateSongName = new UpdateSongName(stationDataSource, 0);
        
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        // Posuvník hlasitosti
        seekBarVolume = findViewById(R.id.seekBarVolume);
        seekBarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
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
                if (noConnectivity) {
                    
                    Toast.makeText(getApplicationContext(), "Zkontrolujte své připojení k internetu.", Toast.LENGTH_LONG).show();
                    
                // Online
                } else sendBroadcast(new Intent(getString(R.string.image_button_play_pause_click)));
            }
        });
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        unregisterReceiver(connectivityReceiver);
        unregisterReceiver(mediaPlayerPreparedReceiver);
        unregisterReceiver(mediaPlayerStartPlayingReceiver);
        unregisterReceiver(mediaPlayerStopPlayingReceiver);
        
        stopService(new Intent(this, StreamService.class));
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        // Zvýšení hlasitosti
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            
            seekBarVolume.setProgress(seekBarVolume.getProgress() + 1);
            
        // Snížení hlasitosti
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            seekBarVolume.setProgress(seekBarVolume.getProgress() - 1);
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
    
                        System.out.println("Název skladby: " + newSongName);
                        
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
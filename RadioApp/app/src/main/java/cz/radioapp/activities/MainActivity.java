package cz.radioapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import cz.radioapp.R;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity {

    private ImageButton imageButtonPlayPause;
    private TextView textViewStation;
    private TextView textViewSongName;
    private ImageView imageViewVolume;
    
    private MediaPlayer mediaPlayer;
    private FFmpegMediaMetadataRetriever metadataRetriever;
    private SeekBar seekBarVolume;
    private AudioManager audioManager;
    
    // Aktualizace názvu skladby
    private UpdateSongName updateSongName;
    
    private boolean mediaPlayerPrepared = false;
    
    
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
        
        // Název rádia
        textViewStation = findViewById(R.id.textViewStation);
        textViewStation.setSelected(true);
        textViewStation.setSingleLine(true);
        textViewStation.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textViewStation.setText("Radio Čas Rock");
        
        // Název skladby
        textViewSongName = findViewById(R.id.textViewSongName);
        textViewSongName.setSelected(true);
        textViewSongName.setSingleLine(true);
        textViewSongName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
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
    
        imageViewVolume = findViewById(R.id.imageViewVolume);
        
        // Změna ikony hlasitosti
        setVolumeImage(seekBarVolume.getProgress(), seekBarVolume.getMax());
        
        metadataRetriever = new FFmpegMediaMetadataRetriever();
        updateSongName = new UpdateSongName("http://icecast6.play.cz/casrock128.mp3");
        
        imageButtonPlayPause = findViewById(R.id.imageButtonPlayPause);
        imageButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
    
                imageButtonPlayPause.setForeground(getDrawable(R.drawable.pause));
                
                // Údovní nastavení přehrávače
                if (!mediaPlayerPrepared) {
    
                    imageButtonPlayPause.setEnabled(false);
                    
                    try {
                        
                        mediaPlayer.setDataSource("http://icecast6.play.cz/casrock128.mp3");
                        mediaPlayer.prepareAsync();
                        
                        // Aktualizace názvu skladby
                        updateSongName.start();
        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
    
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        
                        @Override
                        public void onPrepared(MediaPlayer mp) {
            
                            mediaPlayer.start();
                            
                            textViewSongName.setVisibility(View.VISIBLE);
                            imageButtonPlayPause.setEnabled(true);
                            
                            mediaPlayerPrepared = true;
                        }
                    });
                    
                } else {
                    
                    // Zastavení přehrávání
                    if (mediaPlayer.isPlaying()) {
                        
                        mediaPlayer.pause();
                        imageButtonPlayPause.setForeground(getDrawable(R.drawable.play));
                        updateSongName.cancel();
                        
                    // Souštění přehrávání
                    } else  {
                        
                        mediaPlayer.start();
                        imageButtonPlayPause.setForeground(getDrawable(R.drawable.pause));
                        updateSongName.start();
                    }
                }
            }
        });
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        // Přidání hlasitosti
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
         
        // Maximální hlasitost
        } else if (currentVolume >= (segmentSize * 2)) {
            
            imageViewVolume.setImageResource(R.drawable.volume_max);
        }
    }
    
    
    /**
     * Aktualizace názvu skladby
     */
    private class UpdateSongName extends Thread {
    
        private String url;
        private boolean isStop = false;
    
        
        /**
         * Konstruktor
         *
         * @param url
         */
        public UpdateSongName(String url) {
            this.url = url;
        }
    
        
        /**
         * Start
         */
        @Override
        public void run() {
            
            isStop = false;
            
            while (!isStop) {
                
                try {
                    
                    metadataRetriever.setDataSource(url);
                    String newSongName = metadataRetriever.getMetadata().getString("StreamTitle");
    
                    textViewSongName.post(new Runnable() {
        
                        @Override
                        public void run() {
            
                            String oldSongName = (String) textViewSongName.getText();
            
                            if (!oldSongName.equals(newSongName)) textViewSongName.setText(newSongName);
                        }
                    });
                    
                    Thread.sleep(20_000);
        
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    
        
        /**
         * Přerušení
         */
        public void cancel() {
            isStop = true;
        }
   
    }
    
}
package com.example.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.example.android.spotifystreamer.models.Track;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener {

    public static final String ACTION_PLAY = "com.example.action.PLAY";
    MediaPlayer mediaPlayer = null;
    ArrayList<Track> tracks = null;


    public MusicPlayerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            int position = intent.getIntExtra(getString(R.string.track_position), -1);
            String url = "";
            if (position > 0) {
                tracks = intent.getParcelableArrayListExtra(getString(R.string.tracklist_key));
                if (null != tracks && tracks.size() > 0 && null != tracks.get(position)) {
                    // "http://d318706lgtcm8e.cloudfront.net/mp3-preview/f454c8224828e21fa146af84916fd22cb89cedc6";
                    url = tracks.get(position).getPreviewURL();
                }
                mediaPlayer = new MediaPlayer(); // initialize it here
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    if (!url.isEmpty())
                        mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync(); // prepare async to not block main thread
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onDestroy() {

        if (null != mediaPlayer)
            mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
    }
}

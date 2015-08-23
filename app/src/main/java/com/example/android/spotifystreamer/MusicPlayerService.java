package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.spotifystreamer.models.Track;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyService;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final String ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE";
    public static final String ACTION_PREV = "com.example.action.PREVIOUS";
    public static final String ACTION_NEXT = "com.example.action.NEXT";
    public static final String ACTION_SEEKBAR_CHANGED = "com.example.action.SEEK";
    public static final String ACTION_DURATION = "com.example.action.DURATION";
    public static final String ACTION_REQUEST_TIME_POSITION = "com.example.action.REQUEST_CURRENT_POSITION";
    public static final String ACTION_FRAGMENT_RESUMED = "com.example.action.RESUMED";
    private static final int NOTIFICATION_ID = 146;
    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();
    private static final int IMAGE_SIZE = 40;
    private static OnNotificationEventListener listener;
    MediaPlayer mediaPlayer = null;
    ArrayList<Track> tracks = null;
    String url = "";
    String trackName = "";
    private int position;
    private Notification notification;
    private int notificationLockScreenVisibility;
    private Track currentTrack;
    private NotificationCompat.Builder builder;
    private RemoteViews remoteView;
    private NotificationManager nManager;
    private boolean isPaused = false;
    private int seekbarPosition;

    public MusicPlayerService() {

        tracks = new ArrayList<>();
    }

    public static void registerOnNotificationEventListener(OnNotificationEventListener onNotificationEventListener) {
        listener = onNotificationEventListener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null && intent.getAction() != null) {

            // create all intents
            Intent notificationIntent = new Intent(this, MusicPlayAcitvity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent previousIntent = new Intent(this, MusicPlayerService.class);
            previousIntent.setAction(ACTION_PREV);

            PendingIntent pendingPreviousIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0);

            Intent playIntent = new Intent(this, MusicPlayerService.class);
            playIntent.setAction(ACTION_PLAY_PAUSE);

            PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

            Intent nextIntent = new Intent(this, MusicPlayerService.class);
            nextIntent.setAction(ACTION_NEXT);

            PendingIntent pendingNextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0);

            Intent seekIntent = new Intent(this, MusicPlayerService.class);
            seekIntent.setAction(ACTION_NEXT);

            PendingIntent pendingSeekIntent = PendingIntent.getService(this, 0,
                    seekIntent, 0);


            // check intent action
            if (null != intent && null != intent.getAction() && intent.getAction().equals(ACTION_PLAY)) {
                // TODO check if a different song was playing
                position = intent.getIntExtra(getString(R.string.track_position), -1);

                if (position > -1) {

                    tracks = intent.getParcelableArrayListExtra(getString(R.string.tracklist_key));

                    if (null != tracks && tracks.size() > 0 && null != tracks.get(position)) {

                        currentTrack = tracks.get(position);

                        url = tracks.get(position).getPreviewURL();
                        trackName = tracks.get(position).getTrackName();

                    }
                    mediaPlayer = new MediaPlayer(); // initialize it here
                    // get media player ready
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        if (!url.isEmpty()) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();

                            }
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(url);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    seekbarPosition = intent.getIntExtra(getString(R.string.seekbar_progress_position), 0);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.prepareAsync();// prepare async to not block main thread
                    playMedia(url);

                    // build notification
                    remoteView = new RemoteViews(getPackageName(), R.layout.service_music_player_notification);

                    // TODO check visibility from shared preference;
                    notificationLockScreenVisibility = Notification.VISIBILITY_PUBLIC;
                    nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    builder = new NotificationCompat.Builder(this);
                    builder.setTicker(SpotifyService.class.getSimpleName())
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentIntent(pendingIntent)
                            .setContent(remoteView)
                            .setOngoing(true)
                            .setVisibility(notificationLockScreenVisibility);
                    notification = builder.build();
                    startForeground(NOTIFICATION_ID,
                            notification);

                    remoteView.setOnClickPendingIntent(R.id.prev_imagebutton, pendingPreviousIntent);
                    remoteView.setOnClickPendingIntent(R.id.play_pause_imagebutton, pendingPlayIntent);
                    remoteView.setOnClickPendingIntent(R.id.next_imagebutton, pendingNextIntent);

                }
            } else if (null != intent.getAction() && intent.getAction().equals(ACTION_PREV)) {

                Log.i(LOG_TAG, "Clicked Previous");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_play);
                    nManager.notify(NOTIFICATION_ID, notification);

                }

                // update current track and play it
                if (null != tracks && tracks.size() > 0 && position-- > -1 && null != tracks.get(position)) {
                    currentTrack = tracks.get(position);
                    String url = currentTrack.getPreviewURL();
                    playMedia(url);
                    if (null != listener) {
                        listener.previousClicked();
                        listener.getTrackNumber(position);
                    }
                }

            } else if (null != intent.getAction() && intent.getAction().equals(ACTION_PLAY_PAUSE)) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPaused = true;
                    if (null != listener)
                        listener.onMusicPaused();
                    remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_play);
                    nManager.notify(NOTIFICATION_ID, notification);

                } else {

                    mediaPlayer.start();
                    isPaused = false;
                    if (null != listener)
                        listener.onMusicResumed();
                    remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_pause);
                    nManager.notify(NOTIFICATION_ID, notification);
                }


                Log.i(LOG_TAG, "Clicked Play");
            } else if (null != intent.getAction() && intent.getAction().equals(ACTION_NEXT)) {
                Log.i(LOG_TAG, "Clicked Next");

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_play);
                    nManager.notify(NOTIFICATION_ID, notification);
                }
                if (null != tracks && tracks.size() > 0 && position++ < tracks.size() && null != tracks.get(position)) {

                    currentTrack = tracks.get(position);
                    String url = currentTrack.getPreviewURL();
                    playMedia(url);
                    if (null != listener) {
                        listener.nextClicked();
                        listener.getTrackNumber(position);
                    }

                }

            } else if (null != intent.getAction() && intent.getAction().equals(ACTION_SEEKBAR_CHANGED)) {
                int seekBarProgress = intent.getIntExtra(getString(R.string.seekbar_progress_position), -1);
                if (null != mediaPlayer) {
                    mediaPlayer.seekTo(seekBarProgress);
                    mediaPlayer.start();
                }
            } else if (null != intent.getAction() && intent.getAction().equals(ACTION_REQUEST_TIME_POSITION)) {
                if (null != listener && null != mediaPlayer)
                    listener.setCurrentTrackTimePosition(mediaPlayer.getCurrentPosition());
            } else if (null != intent.getAction() && intent.getAction().equals(ACTION_FRAGMENT_RESUMED)) {
                if (null != listener && null != mediaPlayer) {
                    listener.getCurrentState(tracks, position, mediaPlayer.getCurrentPosition(), mediaPlayer != null, isPaused, mediaPlayer.getDuration());
                }


            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void playMedia(String url) {
        mediaPlayer.reset();
        try {
            if (!url.isEmpty())
                mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();

    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mediaPlayer.seekTo(seekbarPosition);
        mediaPlayer.start();

        if (null != listener) {
            listener.getDuration(mediaPlayer.getDuration());
            listener.onMusicStarted();
        }
        remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_pause);
        remoteView.setTextViewText(R.id.track_name_textview, currentTrack.getTrackName());
        //nManager.notify(NOTIFICATION_ID, notification);
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Picasso
                        .with(MusicPlayerService.this)
                        .load(currentTrack.getAlbumThumbnailLink())
                        .into(remoteView, R.id.album_thumbnail_imageview, NOTIFICATION_ID, notification);
            }
        });


        // TODO add error listener . to stop seekbar from advancing
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != listener)
            listener.onTrackCompleted();
        if (null != mediaPlayer)
            mediaPlayer.release();
        mediaPlayer = null;
        stopForeground(true);


    }

    @Override
    public void onDestroy() {

        if (null != listener)
            listener = null;
        super.onDestroy();
    }

    public interface OnNotificationEventListener {
        void getDuration(int duration);

        void nextClicked();

        void previousClicked();

        void onMusicPaused();

        void onMusicStarted();

        void onMusicResumed();

        void setCurrentTrackTimePosition(int position);

        void onTrackCompleted();

        void getTrackNumber(int trackNumber);

        void getCurrentState(ArrayList<Track> trackList, int currentTrackNumber, int currentTimeTrackPosition, boolean isMediaPlayerON, boolean isPaused, int duration);


    }
}

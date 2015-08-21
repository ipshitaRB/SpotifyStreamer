package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyService;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final String ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE";
    public static final String ACTION_PREV = "com.example.action.PREVIOUS";
    public static final String ACTION_NEXT = "com.example.action.NEXT";
    private static final int NOTIFICATION_ID = 146;
    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();
    private static final int IMAGE_SIZE = 40;
    MediaPlayer mediaPlayer = null;
    ArrayList<Track> tracks = null;
    String url = "";
    String trackName = "";
    private int position;
    private Notification notification;
    private int notificationLockScreenVisibility;
    private Track currentTrack;
    private Target target;
    private NotificationCompat.Builder builder;
    private NotificationCompat.Action pausePlayAction;
    private NotificationCompat.Action nextAction;
    private NotificationCompat.Action previousAction;
    private RemoteViews remoteView;
    private NotificationManager nManager;

    public MusicPlayerService() {
        mediaPlayer = new MediaPlayer(); // initialize it here
        tracks = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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

        if (intent.getAction().equals(ACTION_PLAY)) {
            // TODO check if a different song was playing
            position = intent.getIntExtra(getString(R.string.track_position), -1);

            if (position > -1) {

                tracks = intent.getParcelableArrayListExtra(getString(R.string.tracklist_key));

                if (null != tracks && tracks.size() > 0 && null != tracks.get(position)) {
                    currentTrack = tracks.get(position);
                    url = tracks.get(position).getPreviewURL();
                    trackName = tracks.get(position).getTrackName();

                }

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    if (!url.isEmpty())
                        mediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.prepareAsync();// prepare async to not block main thread
                playMedia(url);


                // TODO seekbar intent

                // build notification
                Bitmap icon = BitmapFactory.decodeResource(getResources(),
                        android.R.drawable.ic_btn_speak_now);
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
                remoteView.setTextViewText(R.id.track_name_textview, trackName);
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
                remoteView.setOnClickPendingIntent(R.id.prev_imagebutton, pendingPreviousIntent);
                remoteView.setOnClickPendingIntent(R.id.play_pause_imagebutton, pendingPlayIntent);
                remoteView.setOnClickPendingIntent(R.id.next_imagebutton, pendingNextIntent);






                /*pausePlayAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "",
                        pendingPlayIntent);
                nextAction = new NotificationCompat.Action(android.R.drawable.ic_media_next, "",
                        pendingNextIntent);
                previousAction = new NotificationCompat.Action(android.R.drawable.ic_media_previous,
                        "", pendingPreviousIntent);
                builder = new NotificationCompat.Builder(this);
                builder.setContentTitle(trackName)
                        .setTicker(SpotifyService.class.getSimpleName())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(
                                Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setVisibility(notificationLockScreenVisibility)
                        .addAction(previousAction)
                        .addAction(pausePlayAction)
                        .addAction(nextAction);//.build();
                startForeground(NOTIFICATION_ID,
                        builder.build());
                // mTarget should an instance variable of your class so it doesn't get GC'ed
                target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        builder.setLargeIcon(bitmap)
                                .setDefaults(Notification.DEFAULT_ALL);
                        // send the notification again to update it w/ the right image
                        ((NotificationManager) (getSystemService(NOTIFICATION_SERVICE)))
                                .notify(NOTIFICATION_ID, builder.build());
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                };

                Picasso.with(this).load(currentTrack.getAlbumThumbnailLink()).into(target);
*/
            }
        } else if (intent.getAction().equals(ACTION_PREV)) {

            Log.i(LOG_TAG, "Clicked Previous");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            // update current track and play it
            if (null != tracks && tracks.size() > 0 && position-- > -1 && null != tracks.get(position)) {
                currentTrack = tracks.get(position);
                String url = currentTrack.getPreviewURL();
                playMedia(url);
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

                remoteView.setTextViewText(R.id.track_name_textview, currentTrack.getTrackName());
            }

        } else if (intent.getAction().equals(ACTION_PLAY_PAUSE)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_play);
                nManager.notify(NOTIFICATION_ID, notification);

            } else {
                mediaPlayer.start();
                remoteView.setImageViewResource(R.id.play_pause_imagebutton, android.R.drawable.ic_media_pause);
                nManager.notify(NOTIFICATION_ID, notification);
            }
            //TODO change play pause and track details only after music plays

            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(ACTION_NEXT)) {
            Log.i(LOG_TAG, "Clicked Next");

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            if (null != tracks && tracks.size() > 0 && position++ < tracks.size() && null != tracks.get(position)) {

                currentTrack = tracks.get(position);
                String url = currentTrack.getPreviewURL();
                playMedia(url);
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

                remoteView.setTextViewText(R.id.track_name_textview, currentTrack.getTrackName());

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
        // TODO: Return the communication channel to the service.
        return null;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {

        mediaPlayer.start();


        // TODO send message to fragment that music has started playing
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (null != mediaPlayer)
            mediaPlayer.release();
        mediaPlayer = null;
        stopForeground(true);
    }


}

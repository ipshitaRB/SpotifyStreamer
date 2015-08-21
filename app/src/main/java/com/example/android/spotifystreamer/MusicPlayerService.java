package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

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
    private static final int NOTIFICATION_ID = 146;
    private static final String ACTION_NEXT = "com.example.action.NEXT";
    private static final String LOG_TAG = MusicPlayerService.class.getSimpleName();
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

            if (position > 0) {

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


                // TODO check visibility from shared preference;
                notificationLockScreenVisibility = Notification.VISIBILITY_SECRET;


                pausePlayAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "",
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

                // update track name
                builder.setContentTitle(currentTrack.getTrackName());
                // update album image
                Picasso.with(this).load(currentTrack.getAlbumThumbnailLink()).into(target);
            }

        } else if (intent.getAction().equals(ACTION_PLAY_PAUSE)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                builder.mActions.clear();
                builder.addAction(previousAction);
                pausePlayAction.icon = android.R.drawable.ic_media_play;
                builder.addAction(pausePlayAction);
                builder.addAction(nextAction);

            } else {
                mediaPlayer.start();
                builder.addAction(android.R.drawable.ic_media_play, "",
                        pendingPlayIntent);
            }
            //TODO pause play image switch

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


                // update track name
                builder.setContentTitle(currentTrack.getTrackName());
                // update album image
                Picasso.with(this).load(currentTrack.getAlbumThumbnailLink()).into(target);
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

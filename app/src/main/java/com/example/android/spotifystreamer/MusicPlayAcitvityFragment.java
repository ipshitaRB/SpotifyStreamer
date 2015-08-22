package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.spotifystreamer.models.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayAcitvityFragment extends Fragment implements MusicPlayerService.OnNotificationEventListener {

    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 800;
    private static final long SEEK_BAR_UPDATE_INTERVAL = 200;
    private static final int MILISECONDS_IN_ONE_SECOND = 1000;
    private static final int SECONDS_IN_ONE_MINUTE = 60;
    public List<Track> trackList;
    public int currentTrackPosition;
    TextView artistNameTextView;
    TextView albumNameTextView;
    TextView trackNameTextView;
    ImageView albumThumbnailImageView;
    private boolean isPlaying = false;
    private ImageButton playPauseButton, previousButton, nextButton;
    private SeekBar seekBar;
    private Track currentTrack;
    private TextView totalDurationTextView, elapsedTimeTextView;
    private int trackTimePosition;
    private int trackDuration = 0;

    public MusicPlayAcitvityFragment() {


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(getString(R.string.tracklist_parcel_key), (ArrayList<? extends Parcelable>) trackList);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicPlayerService.registerOnNotificationEventListener(this);
        if (savedInstanceState == null || !savedInstanceState.containsKey(getString(R.string.tracklist_parcel_key))) {
            // get artist name from previous intent
            Intent intent = getActivity().getIntent();
            trackList = intent.getParcelableArrayListExtra(getString(R.string.tracklist_key));
            currentTrackPosition = intent.getIntExtra(getString(R.string.track_position), -1);
// start music player Service
            Intent startServiceIntent = new Intent(getActivity(), MusicPlayerService.class);
            // pass the entire top track list and position
            startServiceIntent.putParcelableArrayListExtra(getString(R.string.tracklist_key), (ArrayList<? extends Parcelable>) trackList);
            startServiceIntent.putExtra(getString(R.string.track_position), currentTrackPosition);
            // set action play
            startServiceIntent.setAction(MusicPlayerService.ACTION_PLAY);

            getActivity().startService(startServiceIntent);
        } else {
            trackList = savedInstanceState.getParcelableArrayList(getString(R.string.tracklist_parcel_key));

        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_music_play_acitvity, container, false);
        if (null != trackList && trackList.size() > 0)
            currentTrack = trackList.get(currentTrackPosition);

        artistNameTextView = (TextView) rootView.findViewById(R.id.artist_name_textview);

        albumNameTextView = (TextView) rootView.findViewById(R.id.album_name_textview);

        trackNameTextView = (TextView) rootView.findViewById(R.id.track_name_textview);

        albumThumbnailImageView = (ImageView) rootView.findViewById(R.id.album_thumbnail_imageview);
        artistNameTextView.setText(currentTrack.getArtistName());
        albumNameTextView.setText(currentTrack.getAlbumName());
        trackNameTextView.setText(currentTrack.getTrackName());
        if (null != currentTrack.getAlbumThumbnailLink() && !currentTrack.getAlbumThumbnailLink().isEmpty())
            Picasso.with(getActivity()).load(currentTrack.getAlbumThumbnailLink()).resize(IMAGE_WIDTH, IMAGE_HEIGHT).centerCrop().into(albumThumbnailImageView);

        // TODO orientation change checks


        elapsedTimeTextView = (TextView) rootView.findViewById(R.id.elapsed_time_textview);
        totalDurationTextView = (TextView) rootView.findViewById(R.id.total_duration_textview);
        playPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    playPauseButton.setImageResource(android.R.drawable.ic_media_play);


                } else {
                    playPauseButton.setImageResource(android.R.drawable.ic_media_pause);

                }
                // start music player Service
                Intent startServiceIntent = new Intent(getActivity(), MusicPlayerService.class);
                // set action play
                startServiceIntent.setAction(MusicPlayerService.ACTION_PLAY_PAUSE);
                getActivity().startService(startServiceIntent);
            }
        });

        previousButton = (ImageButton) rootView.findViewById(R.id.prev_button);

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start music player Service
                Intent startServiceIntent = new Intent(getActivity(), MusicPlayerService.class);
                // set action play
                startServiceIntent.setAction(MusicPlayerService.ACTION_PREV);
                getActivity().startService(startServiceIntent);
            }
        });

        nextButton = (ImageButton) rootView.findViewById(R.id.next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // start music player Service
                Intent startServiceIntent = new Intent(getActivity(), MusicPlayerService.class);
                // set action play
                startServiceIntent.setAction(MusicPlayerService.ACTION_NEXT);
                getActivity().startService(startServiceIntent);
            }
        });

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // start music player Service
                Intent startServiceIntent = new Intent(getActivity(), MusicPlayerService.class);
                startServiceIntent.putExtra(getString(R.string.seekbar_progress_position), seekBar.getProgress());
                // set action play
                startServiceIntent.setAction(MusicPlayerService.ACTION_SEEKBAR_CHANGED);
                getActivity().startService(startServiceIntent);
            }
        });


        // initialize album name textview
        // initialize trackname
        // initialize image view

        return rootView;

    }


    @Override
    public void getDuration(int duration) {
        // set durationt
        trackDuration = duration;
        seekBar.setMax(duration);
        totalDurationTextView.setText(getDurationInMinutes(duration));
        Log.i(MusicPlayAcitvityFragment.class.getSimpleName(), String.valueOf(duration));

    }


    private String getDurationInMinutes(int duration) {

        int durationInSeconds = duration / MILISECONDS_IN_ONE_SECOND;
        int minutes = durationInSeconds / SECONDS_IN_ONE_MINUTE;
        int seconds = durationInSeconds % SECONDS_IN_ONE_MINUTE;
        String time = String.valueOf(minutes) + getString(R.string.colon) + String.valueOf(seconds);
        return time;
    }

    @Override
    public void nextClicked() {

        updateUI();
    }

    @Override
    public void previousClicked() {
        updateUI();
    }

    private void updateUI() {
        currentTrack = trackList.get(currentTrackPosition);
        artistNameTextView.setText(currentTrack.getArtistName());
        albumNameTextView.setText(currentTrack.getAlbumName());
        trackNameTextView.setText(currentTrack.getTrackName());
        if (null != currentTrack.getAlbumThumbnailLink() && !currentTrack.getAlbumThumbnailLink().isEmpty())
            Picasso.with(getActivity()).load(currentTrack.getAlbumThumbnailLink()).resize(IMAGE_WIDTH, IMAGE_HEIGHT).centerCrop().into(albumThumbnailImageView);
    }

    @Override
    public void onMusicPaused() {
        isPlaying = false;

    }


    @Override
    public void onMusicStarted() {
        isPlaying = true;
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        new UpdateSeekBarTask().execute();

    }

    @Override
    public void onMusicResumed() {
        isPlaying = true;
    }




    @Override
    public void setCurrentTrackTimePosition(int position) {
        trackTimePosition = position;
        seekBar.setProgress(trackTimePosition);
        elapsedTimeTextView.setText(getDurationInMinutes(trackTimePosition));
        updateUI();

    }

    @Override
    public void onTrackCompleted() {
        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    @Override
    public void getTrackNumber(int trackNumber) {
        currentTrackPosition = trackNumber;
    }

    public class UpdateSeekBarTask extends AsyncTask<Void, Void, Void> {

        // start music player Service
        Intent startServiceIntent = new Intent(getActivity(), MusicPlayerService.class);


        @Override
        protected Void doInBackground(Void... arg0) {


            while (trackTimePosition < trackDuration) {
                try {
                    Thread.sleep(SEEK_BAR_UPDATE_INTERVAL);
                    // set action play
                    if (null != startServiceIntent) {
                        startServiceIntent.setAction(MusicPlayerService.ACTION_REQUEST_TIME_POSITION);
                        if (null != getActivity())
                            getActivity().startService(startServiceIntent);
                    }
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }


            }
            return null;
        }
    }

}

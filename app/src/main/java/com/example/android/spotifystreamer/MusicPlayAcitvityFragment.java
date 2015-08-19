package com.example.android.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.spotifystreamer.R;
import com.example.android.spotifystreamer.models.Track;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MusicPlayAcitvityFragment extends Fragment {

    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 800;
    public List<Track> trackList;
    public int currentTrackPosition;
    public MusicPlayAcitvityFragment() {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get artist name from previous intent
        Intent intent = getActivity().getIntent();
        trackList = intent.getParcelableArrayListExtra(getString(R.string.tracklist_key));
        currentTrackPosition = intent.getIntExtra(getString(R.string.track_position),-1);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_music_play_acitvity, container, false);
        Track currentTrack = trackList.get(currentTrackPosition);
        // artisName textview init
        TextView artistNameTextView = (TextView) rootView.findViewById(R.id.artist_name_textview);
        TextView albumNameTextView = (TextView) rootView.findViewById(R.id.album_name_textview);
        TextView trackNameTextView = (TextView) rootView.findViewById(R.id.track_name_textview);
        ImageView albumThumbnailImageView = (ImageView) rootView.findViewById(R.id.album_thumbnail_imageview);
        artistNameTextView.setText(currentTrack.getArtistName());
        albumNameTextView.setText(currentTrack.getAlbumName());
        trackNameTextView.setText(currentTrack.getTrackName());
        if (null != currentTrack.getAlbumThumbnailLink() && !currentTrack.getAlbumThumbnailLink().isEmpty())
            Picasso.with(getActivity()).load(currentTrack.getAlbumThumbnailLink()).resize(IMAGE_WIDTH, IMAGE_HEIGHT).centerCrop().into(albumThumbnailImageView);

        // initialize album name textview
        // initialize trackname
        // initialize image view

        return rootView;

    }
}

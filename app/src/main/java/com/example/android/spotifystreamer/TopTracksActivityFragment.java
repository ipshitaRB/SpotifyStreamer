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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.spotifystreamer.adapters.TopTracksAdapter;
import com.example.android.spotifystreamer.models.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {
    // list of top tracks
    public List<Track> trackList;
    // declare custom adapter for the tracks listview
    public TopTracksAdapter topTracksAdapter;

    public String artistSpotifyId;
    public String artistName;

    public SpotifyApi spotifyApi;
    public SpotifyService spotifyService;



    public TopTracksActivityFragment() {
        //  initialize tracklist
        trackList = new ArrayList<Track>();
        spotifyApi = new SpotifyApi();
        spotifyService = spotifyApi.getService();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get artist name from previous intent
        Intent intent = getActivity().getIntent();
        artistSpotifyId = intent.getStringExtra(Intent.EXTRA_TEXT);
        artistName = intent.getStringExtra(getString(R.string.artist_name_key));



        //get top tracks of the artist in an asynctask using spotify api
        new FetchTopTracksTask().execute(artistSpotifyId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        trackList.clear();
        // initialize tracks adapter
        topTracksAdapter = new TopTracksAdapter(getActivity(), trackList);
        // initialize top tracks list view
        ListView topTracksListView = (ListView) rootView.findViewById(R.id.top_tracks_listview);


        //set adapter to list view
        topTracksListView.setAdapter(topTracksAdapter);

        //set on item click listener
        topTracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get list of track name , album name, artist name, track image url, preview_url

                Intent musicPlayIntent = new Intent(getActivity(), MusicPlayAcitvity.class);

                musicPlayIntent.putParcelableArrayListExtra(getString(R.string.tracklist_key), (ArrayList<? extends Parcelable>) trackList);
                musicPlayIntent.putExtra(getString(R.string.track_position),position);
                startActivity(musicPlayIntent);

            }
        });

        return rootView;
    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {

        private static final int MAX_TOP_TRACKS_LIMIT = 10;
        private final String LOG_TAG = FetchTopTracksTask.class.getSimpleName();
        private Tracks spotifyTopTrackList;

        private Map<String, Object> options;
        private List<Track> newTrackList = new ArrayList<Track>();

        @Override
        protected List<Track> doInBackground(String... params) {

            if (null != params[0]) {

                options = new HashMap<>();
                options.put(getString(R.string.spotify_country_param), getString(R.string.spotify_country_value));
                try {
                    spotifyTopTrackList = spotifyService.getArtistTopTrack(params[0], options);
                    if (null != spotifyTopTrackList.tracks && !spotifyTopTrackList.tracks.isEmpty()) {
                        int size = spotifyTopTrackList.tracks.size() > MAX_TOP_TRACKS_LIMIT ? MAX_TOP_TRACKS_LIMIT : spotifyTopTrackList.tracks.size();
                        String albumName;
                        String albumThumbnailLink;
                        String trackName;
                        String artistName;
                        String preview_url;

                        kaaes.spotify.webapi.android.models.Track currentTrack;
                        trackList.clear();
                        for (int i = 0; i < size; i++) {
                            currentTrack = spotifyTopTrackList.tracks.get(i);
                            if (null != currentTrack && null != currentTrack.album) {
                                albumName = currentTrack.album.name;
                                if (null != currentTrack.album.images && !currentTrack.album.images.isEmpty()) {
                                    albumThumbnailLink = currentTrack.album.images.get(0).url;
                                    trackName = currentTrack.name;
                                    artistName = currentTrack.artists.get(0).name;
                                    preview_url = currentTrack.preview_url;

                                    newTrackList.add(new Track.Builder().trackName(trackName).albumName(albumName).albumThumbnailLink(albumThumbnailLink).artistName(artistName).previewURL(preview_url).build());
                                }


                            }
                        }
                    }
                } catch (RetrofitError e) {
                    Log.d(LOG_TAG, "error kind : " + e.getKind().name());

                }

            }

            return newTrackList;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (null != tracks) {

                // if no artist was found then display message
                if (tracks.isEmpty())
                    Toast.makeText(getActivity(), getString(R.string.no_top_tracks), Toast.LENGTH_LONG).show();
                else
                    topTracksAdapter.addAll(tracks);
            }
        }
    }
}

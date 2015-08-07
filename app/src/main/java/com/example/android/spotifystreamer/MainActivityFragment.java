package com.example.android.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.spotifystreamer.Util.NetworkUtil;
import com.example.android.spotifystreamer.adapters.ArtistAdapter;
import com.example.android.spotifystreamer.models.Artist;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    // Declare List of artists
    public ArrayList<Artist> artistList;

    // Declare custom Adapter for Artist results
    public ArtistAdapter artistAdapter;
    String searchKeyword;
    // Declare SpotifyApi object
    private SpotifyApi spotifyApi;
    // Declare SpotifyService object
    private SpotifyService spotifyService;

    public MainActivityFragment() {

        // initialize list of artists
        artistList = new ArrayList<Artist>();
        // initialize spotify api object
        spotifyApi = new SpotifyApi();
        // initialize spotify service object
        spotifyService = spotifyApi.getService();


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey(getString(R.string.artist_parcel_key))) {
            artistList.clear();
        } else {
            artistList = savedInstanceState.getParcelableArrayList(getString(R.string.artist_parcel_key));
        }


    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //declare and initialize rootview
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final SearchView searchText = (SearchView) rootView.findViewById(R.id.searchText);

        searchText.setIconifiedByDefault(false);
        searchText.setQueryHint(getResources().getString(R.string.search_artist_hint));
        searchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchKeyword = searchText.getQuery().toString();
                if (NetworkUtil.isNetworkAvailable(getActivity())) {
                    FetchArtistTask task = new FetchArtistTask();
                    task.execute(searchKeyword);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }

                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        // initialize custom adapter with the list of artists

        artistAdapter = new ArtistAdapter(getActivity(), artistList);

        // initialize artist list view
        ListView artistListView = (ListView) rootView.findViewById(R.id.artist_listview);
        // set custom adapter to the list view
        artistListView.setAdapter(artistAdapter);

        // add onclick action for listview item
        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //start top ten track activity and pass artist id (to query spotify service for top tracks )and artist name(to show in the action bar)
                String artistId = artistAdapter.getItem(position).getArtistId();
                String artistName = artistAdapter.getItem(position).getArtistName();
                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
                topTracksIntent.putExtra(Intent.EXTRA_TEXT, artistId);
                topTracksIntent.putExtra(getString(R.string.artist_name_key), artistName);
                startActivity(topTracksIntent);

            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(getString(R.string.artist_parcel_key), artistList);
        super.onSaveInstanceState(outState);
    }

    /**
     * Created by Ipshita on 09-07-2015.
     */
    public class FetchArtistTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();


        // Create a list of Artists
        List<Artist> artists = new ArrayList<Artist>();

        String artistToSearch;
        // find total number of search results
        int numberOfResults = 0;

        String artistName;
        String artistThumbnailLink;
        String artistId;

        List<kaaes.spotify.webapi.android.models.Artist> artistResultList;

        kaaes.spotify.webapi.android.models.Artist currentArtist;

        List<kaaes.spotify.webapi.android.models.Image> currentArtistImageList;

        ArtistsPager results;

        @Override
        protected List<Artist> doInBackground(String... params) {
            artistToSearch = params[0];

            try {
                results = spotifyService.searchArtists(artistToSearch);
                // clear artist list
                artists.clear();

                artistResultList = results.artists.items;


                if (null != artistResultList && !artistResultList.isEmpty()) {
                    numberOfResults = artistResultList.size();
                    for (int i = 0; i < numberOfResults; i++) {

                        if (null != artistResultList.get(i)) {

                            currentArtist = artistResultList.get(i);
                            artistName = currentArtist.name;
                            currentArtistImageList = currentArtist.images;
                            artistId = currentArtist.id;

                            if (null != currentArtistImageList && !currentArtistImageList.isEmpty() && null != currentArtistImageList.get(0)) {
                                artistThumbnailLink = currentArtistImageList.get(0).url;
                            }

                            artists.add(new Artist.Builder().artistName(artistName).artistThumbnailLink(artistThumbnailLink).artistId(artistId).build());

                        }

                    }
                }

            } catch (RetrofitError e) {
                Log.d(LOG_TAG, "error kind : " + e.getKind().name());

            }


            return artists;
        }

        @Override
        protected void onPostExecute(List<Artist> artistResult) {
            if (null != artistResult) {
                artistAdapter.clear();
                // if no artist was found then display message
                if (artistResult.isEmpty())
                    Toast.makeText(getActivity(), getString(R.string.no_result_found) + "\"" + artistToSearch + "\". " + getString(R.string.no_result_found_suggestion), (Toast.LENGTH_LONG)).show();
                else
                    artistAdapter.addAll(artistResult);
            }
        }
    }
}



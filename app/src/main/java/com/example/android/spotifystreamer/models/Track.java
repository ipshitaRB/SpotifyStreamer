package com.example.android.spotifystreamer.models;

/**
 * Created by Ipshita on 09-07-2015.
 */
public class Track {
    // create String instance variable for album name
    private final String albumName;
    // create string instance variable for album cover image
    private final String albumThumbnailLink;
    // create string instance variable for the track's name
    private final String trackName;


    // create private constructor using fields
    private Track(Builder builder) {
        this.albumName = builder.albumName;
        this.albumThumbnailLink = builder.albumThumbnailLink;
        this.trackName = builder.trackName;

    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumThumbnailLink() {
        return albumThumbnailLink;
    }

    public String getTrackName() {
        return trackName;
    }


    // creating Builder Pattern
    public static class Builder {
        private String albumName;
        private String albumThumbnailLink;
        private String trackName;


        // builder methods for setting property
        public Builder albumName(String albumName) {
            this.albumName = albumName;
            return this;
        }

        public Builder albumThumbnailLink(String albumThumbnailLink) {
            this.albumThumbnailLink = albumThumbnailLink;
            return this;
        }

        public Builder trackName(String trackName) {
            this.trackName = trackName;
            return this;
        }

        public Track build() {
            return new Track(this);
        }

    }
}

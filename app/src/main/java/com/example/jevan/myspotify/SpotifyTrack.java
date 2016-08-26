package com.example.jevan.myspotify;

/**
 * SpotifyTrack
 *
 * Created by jack on 8/25/16.
 */
public class SpotifyTrack {
    private String title, artist;

    public SpotifyTrack(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle() {return title;}

    public String getArtist() {return artist;}
}

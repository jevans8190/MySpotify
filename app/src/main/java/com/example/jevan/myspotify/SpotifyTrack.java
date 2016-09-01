package com.example.jevan.myspotify;

import android.nfc.Tag;
import android.util.Log;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * SpotifyTrack
 *
 * Created by jack on 8/25/16.
 */
public class SpotifyTrack implements Serializable {
    private String id, title, artist, uri;
    private URL imageURL;
    private int popularity;

    private static final String TAG = SpotifyTrack.class.getSimpleName();

    public SpotifyTrack() {}

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getArtist() {return artist;}

    public void setArtist(String artist) {this.artist = artist;}

    public String getUri() {return uri;}

    public void setUri(String uri) {this.uri = uri;}

    public URL getImageURL() {return imageURL;}

    public void setImageURL(String url) {
        try {
            this.imageURL = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid album image url");
        }
    }

    public int getPopularity() {return popularity;}

    public void setPopularity(int popularity) {this.popularity = popularity;}

}

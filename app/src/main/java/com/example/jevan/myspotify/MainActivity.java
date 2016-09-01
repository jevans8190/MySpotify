package com.example.jevan.myspotify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jakewharton.rxbinding.view.RxView;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements
        PlayerNotificationCallback,
        ConnectionStateCallback {

    // Recycler
    private RecyclerView trackRecyclerView;
    private TrackAdapter mAdapter;

    // Spotify
    private Player mPlayer;
    private String mAccessToken;

    // Spotify constants
    private static final String CLIENT_ID = "4cc42aa0dceb42c99e24cb940131f3a0";
    private static final String REDIRECT_URI = "jevans-myspotify-login://callback";
    private static final String MY_TRACKS_URL = "https://api.spotify.com/v1/me/tracks";

    // Request code for activity result
    private static final int REQUEST_CODE = 80085;

    // Log tag
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recycler
        trackRecyclerView = (RecyclerView) findViewById(R.id.track_recycler_view);
        trackRecyclerView.setHasFixedSize(true);
        trackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RxView.clicks(trackRecyclerView).subscribe(event -> {

        });

        // Start spotify auth intent
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming", "user-library-read"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                mAccessToken = response.getAccessToken();
                Config config = new Config(this, mAccessToken, CLIENT_ID);
                Spotify.getPlayer(config, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }

        // Fetch tracks from Spotify API
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject params = new JSONObject();
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + mAccessToken);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                MY_TRACKS_URL+"?limit=50",
                params,
                json -> {
                    List<SpotifyTrack> tracks = new ArrayList<>();
                    int numSongs = 0;
                    try {
                        JSONArray songs = json.getJSONArray("items");
                        numSongs = songs.length();
                        for (int i = 0; i < numSongs; i++) {
                            // Get the json for this song object
                            JSONObject song = songs
                                    .getJSONObject(i)
                                    .getJSONObject("track");
                            // Add the SpotifyTrack object
                            tracks.add(json2Track(song));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON ERROR");
                    } finally {
                        // Set the adapter for the recycler view
                        // using the list of songs in the result
                        mAdapter = new TrackAdapter(
                                this,
                                mPlayer,
                                tracks.toArray(new SpotifyTrack[numSongs]));
                        trackRecyclerView.setAdapter(mAdapter);
                    }

                },
                error -> Log.e(TAG, "" + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return header;
            }
        };
        requestQueue.add(request);
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d(TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(PlayerNotificationCallback.ErrorType errorType, String errorDetails) {
        Log.d(TAG, "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    private static SpotifyTrack json2Track(JSONObject song) {
        SpotifyTrack result = new SpotifyTrack();
        try {
            String id = song
                    .getString("id");
            String title = song
                    .getString("name");
            JSONObject artistObj = song
                    .getJSONArray("artists")
                    .getJSONObject(0);
            String artist = artistObj
                    .getString("name");
            String uri = song
                    .getString("uri");
            String imageUrl = song
                    .getJSONObject("album")
                    .getJSONArray("images")
                    .getJSONObject(0)
                    .getString("url");
            int popularity = song
                    .getInt("popularity");
            // Add all the attributes to the SpotifyTrack
            result.setId(id);
            result.setTitle(title);
            result.setArtist(artist);
            result.setUri(uri);
            result.setImageURL(imageUrl);
            result.setPopularity(popularity);
            return result;
        } catch (JSONException e) {
            Log.e(TAG, "Error loading song\n" + e.getMessage());
            return null;
        }
    }

    public String getAccessToken() { return mAccessToken; }

    public String getClientId() { return CLIENT_ID; }
}

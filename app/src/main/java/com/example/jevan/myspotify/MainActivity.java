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
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.PlayConfig;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // Recycler
    private RecyclerView trackRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TrackAdapter mAdapter;

    // Spotify
    private Player mPlayer;
    private PlayConfig mPlayConfig;
    private String mAccessToken;

    // Spotify constants
    private static final String CLIENT_ID = "4cc42aa0dceb42c99e24cb940131f3a0";
    private static final String REDIRECT_URI = "jevans-myspotify-login://callback";
    private static final String MY_TRACKS_URL = "https://api.spotify.com/v1/me/tracks";
    private static final String MY_TRACKS_PARAMS = "limit=10";

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
        mLayoutManager = new LinearLayoutManager(this);
        trackRecyclerView.setLayoutManager(mLayoutManager);

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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject params = new JSONObject();
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + mAccessToken);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                MY_TRACKS_URL,// + "?" + MY_TRACKS_PARAMS, // url
                params,
                json -> {
                    List<SpotifyTrack> tracks = new ArrayList<>();
                    int numSongs = 0;
                    try {
                        JSONArray songs = json.getJSONArray("items");
                        numSongs = songs.length();
                        // Get the relevant info for each song
                        for (int i = 0; i < numSongs; i++) {
                            JSONObject song = songs
                                    .getJSONObject(i)
                                    .getJSONObject("track");
                            String title = song
                                    .getString("name");
                            String artist = song
                                    .getJSONArray("artists")
                                    .getJSONObject(0)
                                    .getString("name");
                            tracks.add(new SpotifyTrack(title, artist));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON ERROR");
                    } finally {
                        // Set the adapter for the recycler view
                        // using the list of songs in the result
                        mAdapter = new TrackAdapter(
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
}

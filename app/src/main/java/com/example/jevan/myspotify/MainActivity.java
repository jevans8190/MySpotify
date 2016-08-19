package com.example.jevan.myspotify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;
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

public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {
    // UI Elements
    Button playButton, pauseButton;
    // Spotify
    private Player mPlayer;
    private PlayConfig mPlayConfig;

    // Client ID
    private static final String CLIENT_ID = "4cc42aa0dceb42c99e24cb940131f3a0";
    // Redirect URI
    private static final String REDIRECT_URI = "jevans-myspotify-login://callback";
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 80085;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = (Button) findViewById(R.id.button_main_play);
        pauseButton = (Button) findViewById(R.id.button_main_pause);

        // Define button click observables
        RxView.clicks(pauseButton).subscribe(o -> {
            Log.d(TAG, "Pause pressed");
            if (mPlayer != null) {
                mPlayer.pause();
            }
        });
        RxView.clicks(playButton).subscribe(o -> {
            Log.d(TAG, "Play pressed");
            if (mPlayer != null) {
                mPlayer.resume();
            }
        });


        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
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
                Config config = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(config, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        mPlayConfig = PlayConfig.createFor("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
                        mPlayer.play(mPlayConfig);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
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

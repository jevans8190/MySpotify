package com.example.jevan.myspotify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;

public class TrackDetailActivity extends Activity implements
        PlayerNotificationCallback {
    // Track UI
    private TextView titleTV, artistTV, popularityTV;
    private ImageView albumIV;
    // Action UI
    private Button playPauseButton;
    // Spotify data
    private SpotifyTrack mTrack;
    private Player mPlayer;
    private boolean trackPlaying;
    // log tag
    private static final String TAG = TrackDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);
        Bundle extras = getIntent().getExtras();
        // get the track
        mTrack = (SpotifyTrack) extras.getSerializable("track");
        // create the player
        String accessToken = extras.getString("access_token");
        String clientId = extras.getString("client_id", "default");
        Config config = new Config(this, accessToken, clientId);
        Spotify.getPlayer(config, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer = player;
                mPlayer.addPlayerNotificationCallback(TrackDetailActivity.this);
                Log.d(TAG, "Player initialized");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Error creating player");
            }
        });
        // Track UI
        titleTV = (TextView) findViewById(R.id.track_title_text);
        artistTV = (TextView) findViewById(R.id.track_artist_text);
        popularityTV = (TextView) findViewById(R.id.track_popularity_text);
        albumIV = (ImageView) findViewById(R.id.track_album_image);
        setTrackUI();
        //Action UI
        playPauseButton = (Button) findViewById(R.id.play_pause_button);
        // Spotify data
        mTrack = (SpotifyTrack) getIntent().getSerializableExtra("track");
        Log.d(TAG, "Getting track from spotify");
        trackPlaying = true;
        // Play/Pause callback
        RxView.clicks(playPauseButton).subscribe(v -> {
            if (trackPlaying) {
                mPlayer.pause();
                playPauseButton.setText(R.string.track_play);
            } else {
                mPlayer.resume();
                playPauseButton.setText(R.string.track_pause);
            }
            trackPlaying = !trackPlaying;
        });
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

    private void setTrackUI() {
        titleTV.setText(mTrack.getTitle());
        artistTV.setText(mTrack.getArtist());
//        popularityTV.setText(mTrack.getPopularity());
        try {
            Picasso.with(this)
                    .load(mTrack.getImageURL().toURI().toString())
                    .into(albumIV);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Error loading album image");
        }
    }
}

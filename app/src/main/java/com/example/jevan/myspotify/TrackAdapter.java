package com.example.jevan.myspotify;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.spotify.sdk.android.player.Player;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;

/**
 * Track adapter
 *
 * Created by jack on 8/25/16.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {
    // context
    private MainActivity mContext;
    private SpotifyTrack[] tracks;
    private Player mPlayer;
    // intent
    private Intent detailIntent;

    private static final String TAG = TrackAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTV, artistTV, popularityTV;
        private ImageView albumIV;

        public ViewHolder(View v) {
            super(v);
            titleTV = (TextView) v.findViewById(R.id.track_title_text);
            artistTV = (TextView) v.findViewById(R.id.track_artist_text);
            albumIV = (ImageView) v.findViewById(R.id.track_album_image);
            popularityTV = (TextView) v.findViewById(R.id.track_popularity_text);
        }
    }

    public TrackAdapter(MainActivity activity, Player player, SpotifyTrack[]tracks) {
        this.mPlayer = player;
        this.mContext = activity;
        this.tracks = tracks;
        // intent
        detailIntent = new Intent(mContext, TrackDetailActivity.class);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the track object
        SpotifyTrack track = tracks[position];
        // title
        holder.titleTV.setText(track.getTitle());
        // artist
        holder.artistTV.setText(track.getArtist());
        // popularity
        String popularityStr = "Popularity: " + track.getPopularity();
        holder.popularityTV.setText(popularityStr);
        // album image
        try {
            Picasso.with(mContext)
                    .load(Uri.parse(track.getImageURL().toURI().toString()))
                    .into(holder.albumIV);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Error with album image url");
        }
        // on click listener
        RxView.clicks(holder.itemView).subscribe(v -> {
            // play the song
            mPlayer.play(track.getUri());
            // start the detail activity
            detailIntent.putExtra("track", track);
            detailIntent.putExtra("access_token", mContext.getAccessToken());
            detailIntent.putExtra("client_id", mContext.getClientId());
            mContext.startActivity(detailIntent);
        });
    }

    @Override
    public int getItemCount() {
        return tracks.length;
    }
}

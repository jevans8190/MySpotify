package com.example.jevan.myspotify;

import android.app.Activity;
import android.app.admin.SystemUpdatePolicy;
import android.content.Context;
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

import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Track adapter
 *
 * Created by jack on 8/25/16.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {
    private Context mContext;
    private SpotifyTrack[] tracks;
    private PlayerCallBack mPlayerCallBack;

    private static final String TAG = TrackAdapter.class.getSimpleName();

    public interface PlayerCallBack {

        void playSong(String uri);

        void pauseSong();
    }

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

    public TrackAdapter(Activity activity, SpotifyTrack[]tracks) {
        this.mPlayerCallBack = (PlayerCallBack) activity;
        this.mContext = activity;
        this.tracks = tracks;
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
        RxView.clicks(holder.itemView).subscribe(
                v -> mPlayerCallBack.playSong(track.getUri())
        );
    }

    @Override
    public int getItemCount() {
        return tracks.length;
    }
}

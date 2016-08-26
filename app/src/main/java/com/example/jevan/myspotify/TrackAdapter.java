package com.example.jevan.myspotify;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Track adapter
 *
 * Created by jack on 8/25/16.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {
    // Dataset
    private SpotifyTrack[] tracks;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTV, artistTV;

        public ViewHolder(View v) {
            super(v);
            titleTV = (TextView) v.findViewById(R.id.track_title_text);
            artistTV = (TextView) v.findViewById(R.id.track_artist_text);
        }
    }

    public TrackAdapter(SpotifyTrack[] tracks) {
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
        SpotifyTrack track = tracks[position];
        // Update the UI properly
        holder.titleTV.setText(track.getTitle());
        holder.artistTV.setText(track.getArtist());
    }

    @Override
    public int getItemCount() {
        return tracks.length;
    }
}

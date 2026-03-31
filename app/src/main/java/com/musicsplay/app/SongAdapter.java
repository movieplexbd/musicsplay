package com.musicsplay.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView adapter that binds the list of Song objects to list item views.
 * Each row shows the song title and fires a click callback so MainActivity
 * can start playback for the selected track.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    /** Callback interface — implemented by MainActivity to handle song selection. */
    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    private final List<Song> songs;
    private final OnSongClickListener listener;
    private int selectedPosition = -1; // Track currently highlighted song

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the individual list-item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.titleText.setText(song.getTitle());

        // Highlight the currently playing track
        holder.itemView.setSelected(position == selectedPosition);

        // Notify MainActivity when user taps a song row
        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onSongClick(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * Called from MainActivity when the song changes (e.g., via Next/Prev buttons)
     * so the highlight follows the active track.
     */
    public void setSelectedPosition(int position) {
        int prev = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(prev);
        notifyItemChanged(selectedPosition);
    }

    /** ViewHolder caches the TextView reference to avoid repeated findViewById calls. */
    static class SongViewHolder extends RecyclerView.ViewHolder {
        final TextView titleText;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tvSongTitle);
        }
    }
}

package com.musicsplay.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity — the single screen of the Music Player.
 *
 * Responsibilities:
 *  1. Request runtime storage permission (READ_EXTERNAL_STORAGE / READ_MEDIA_AUDIO).
 *  2. Query the device MediaStore for all audio files.
 *  3. Show songs in a RecyclerView.
 *  4. Control playback (Play/Pause, Next, Previous) via Android's MediaPlayer API.
 *  5. Keep the "Now Playing" label in sync with the active track.
 */
public class MainActivity extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    // -------------------------------------------------------------------------
    // Permission request codes
    // -------------------------------------------------------------------------
    private static final int REQUEST_PERMISSION_CODE = 101;

    // -------------------------------------------------------------------------
    // UI references
    // -------------------------------------------------------------------------
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private TextView tvNowPlaying;    // Shows title of currently playing song
    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    private ImageButton btnPrev;

    // -------------------------------------------------------------------------
    // Playback state
    // -------------------------------------------------------------------------
    private MediaPlayer mediaPlayer;  // Android's built-in audio engine
    private List<Song> songList = new ArrayList<>();
    private int currentIndex = 0;     // Index into songList

    // -------------------------------------------------------------------------
    // Activity lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire up views
        recyclerView  = findViewById(R.id.recyclerView);
        tvNowPlaying  = findViewById(R.id.tvNowPlaying);
        btnPlayPause  = findViewById(R.id.btnPlayPause);
        btnNext       = findViewById(R.id.btnNext);
        btnPrev       = findViewById(R.id.btnPrev);

        // Set up RecyclerView with a vertical linear layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(songList, this);
        recyclerView.setAdapter(songAdapter);

        // Hook up player control buttons
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNext());
        btnPrev.setOnClickListener(v -> playPrev());

        // Request storage permission before loading songs
        checkAndRequestPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources when activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // -------------------------------------------------------------------------
    // Runtime Permission Handling
    // -------------------------------------------------------------------------

    /**
     * On Android 13+ (API 33) use READ_MEDIA_AUDIO; on older versions use
     * READ_EXTERNAL_STORAGE.
     */
    private void checkAndRequestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted — load songs immediately
            loadSongs();
        } else {
            // Ask the user for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted permission — now load the song list
                loadSongs();
            } else {
                Toast.makeText(this,
                        "Storage permission is required to load music files.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // -------------------------------------------------------------------------
    // MediaStore query — load all audio tracks from device storage
    // -------------------------------------------------------------------------

    /**
     * Queries the Android MediaStore for all audio files on the device.
     * Populates songList and refreshes the RecyclerView adapter.
     */
    private void loadSongs() {
        songList.clear();

        // Columns we want from MediaStore
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA   // Absolute file path
        };

        // Only fetch files that MediaStore considers music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        try (Cursor cursor = getContentResolver().query(
                uri, projection, selection, null,
                MediaStore.Audio.Media.TITLE + " ASC")) { // Sort A–Z by title

            if (cursor != null && cursor.moveToFirst()) {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int pathIndex  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                do {
                    String title = cursor.getString(titleIndex);
                    String path  = cursor.getString(pathIndex);
                    songList.add(new Song(title, path));
                } while (cursor.moveToNext());
            }
        }

        if (songList.isEmpty()) {
            Toast.makeText(this, "No music found on device.", Toast.LENGTH_SHORT).show();
            tvNowPlaying.setText("No songs found");
        } else {
            // Notify adapter that the data set changed
            songAdapter.notifyDataSetChanged();
            // Show the first song title as a hint
            tvNowPlaying.setText(songList.get(0).getTitle());
        }
    }

    // -------------------------------------------------------------------------
    // SongAdapter.OnSongClickListener — called when user taps a song in the list
    // -------------------------------------------------------------------------

    @Override
    public void onSongClick(int position) {
        currentIndex = position;
        playSong(currentIndex);
    }

    // -------------------------------------------------------------------------
    // Playback helpers
    // -------------------------------------------------------------------------

    /**
     * Starts or resumes/pauses playback depending on current state.
     */
    private void togglePlayPause() {
        if (songList.isEmpty()) return;

        if (mediaPlayer == null) {
            // Nothing loaded yet — play the current index
            playSong(currentIndex);
        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    /** Skips to the next song, wrapping around to the first if at the end. */
    private void playNext() {
        if (songList.isEmpty()) return;
        currentIndex = (currentIndex + 1) % songList.size();
        playSong(currentIndex);
    }

    /** Goes back to the previous song, wrapping around to the last if at the start. */
    private void playPrev() {
        if (songList.isEmpty()) return;
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        playSong(currentIndex);
    }

    /**
     * Core playback method.
     * Releases any existing MediaPlayer instance, creates a fresh one for the
     * given song index, and starts it.
     *
     * @param index Position in songList to play
     */
    private void playSong(int index) {
        if (index < 0 || index >= songList.size()) return;

        Song song = songList.get(index);

        // Release the previous player before creating a new one (important!)
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(song.getPath()); // Set audio file path
            mediaPlayer.prepare();                     // Synchronous prepare (small files OK)
            mediaPlayer.start();                       // Begin playback

            // Update the Now Playing label
            tvNowPlaying.setText(song.getTitle());

            // Flip the button icon to "pause"
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

            // Highlight the active row in the RecyclerView
            songAdapter.setSelectedPosition(index);

            // Auto-advance to the next song when current one finishes
            mediaPlayer.setOnCompletionListener(mp -> playNext());

        } catch (IOException e) {
            Toast.makeText(this,
                    "Unable to play: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }
}

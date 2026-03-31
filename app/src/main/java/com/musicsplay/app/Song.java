package com.musicsplay.app;

/**
 * Simple data model representing a single audio track.
 * Holds the song title and the URI path to the audio file on device storage.
 */
public class Song {

    private final String title;  // Display name shown in the list
    private final String path;   // Absolute file path or content URI

    public Song(String title, String path) {
        this.title = title;
        this.path = path;
    }

    /** Returns the human-readable song title. */
    public String getTitle() {
        return title;
    }

    /** Returns the full file path / URI string for MediaPlayer. */
    public String getPath() {
        return path;
    }
}

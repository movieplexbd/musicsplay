# MusicSplay — Android Music Player

A simple Android music player written in Java. Loads audio files from device storage, shows a song list, and provides playback controls.

## Features

- Loads all audio tracks from device storage via MediaStore
- Scrollable song list (RecyclerView) sorted A–Z
- Play / Pause / Next / Previous controls
- Auto-advances to the next song when one finishes
- Highlights the currently playing song in the list
- Runtime storage permission request (supports Android 5 – 14+)
- Clean dark-themed UI

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java |
| Min SDK | 21 (Android 5.0) |
| Audio playback | Android MediaPlayer API |
| Song list | RecyclerView + custom adapter |
| Permissions | Runtime permission (READ_EXTERNAL_STORAGE / READ_MEDIA_AUDIO) |

## Project Structure

```
musicsplay/
├── app/
│   └── src/main/
│       ├── java/com/musicsplay/app/
│       │   ├── MainActivity.java   ← Main screen + playback logic
│       │   ├── Song.java           ← Data model (title + path)
│       │   └── SongAdapter.java    ← RecyclerView adapter
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml   ← Main screen layout
│       │   │   └── item_song.xml       ← Song list row layout
│       │   ├── drawable/               ← Button backgrounds, selectors
│       │   └── values/                 ← Colors, strings, themes
│       └── AndroidManifest.xml
├── .github/
│   └── workflows/
│       └── build.yml   ← GitHub Actions CI (auto-builds APK on push)
├── build.gradle
├── settings.gradle
└── gradlew
```

## How to Build Locally

1. Clone the repo and open it in **Android Studio**.
2. Let Gradle sync complete.
3. Click **Run ▶** or run from the terminal:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

The debug APK is output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions — Automatic APK Build

Every push to the repository triggers the **Build APK** workflow:

1. Go to your repository on GitHub.
2. Click the **Actions** tab.
3. Select the latest **Build APK** workflow run.
4. Scroll down to the **Artifacts** section at the bottom.
5. Click **musicsplay-apk** to download a ZIP containing the APK.

> The APK is a debug build — it can be sideloaded onto any Android device with *Unknown sources / Install unknown apps* enabled.

## GitHub Setup

```bash
# Inside the musicsplay/ directory:
git init
git add .
git commit -m "Initial commit: MusicSplay Android app"
git branch -M main
git remote add origin https://github.com/<your-username>/musicsplay.git
git push -u origin main
```

Once pushed, the Actions workflow will run automatically.

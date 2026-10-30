# AuraBeat

A premium native Android music player built as a portfolio piece. Kotlin, Jetpack Compose, Media3, Hilt, Room, and a Spotify bridge for real-world integration proof.

Not a Spotify clone, not a WebView, not React Native — every screen is Compose, every playback path is Media3.

---

## Features

### Core playback
- Local audio playback via **AndroidX Media3 / ExoPlayer**
- `MediaSessionService` for real background playback — audio continues when the app is closed or the phone is locked
- Media notification with play / pause / next / previous / seek
- Lock-screen widget with the track's actual embedded artwork
- Bluetooth and wired headset controls
- Audio focus and "becoming noisy" handled

### Music sources — three, all working in the same UI
- **Bundled tracks** — MP3/M4A files dropped into `app/src/main/res/raw/`
- **Device library scan** — reads every music file MediaStore knows about (Music folder, Downloads, etc.). One-time runtime permission
- **SAF file & folder picker** — user picks specific files or a whole folder outside standard music locations; URIs persist across app restarts

### Spotify integration
- **OAuth PKCE sign-in** — no client secret ever leaves the device
- **Auto-refreshing tokens** stored in DataStore
- **Profile display** — name, country, subscription tier
- **Catalog search** via Spotify Web API (when Spotify's dashboard has enabled it for the app)
- **Playback bridge** via Spotify's App Remote SDK — tap a Spotify result in AuraBeat, Spotify itself plays it in the background, AuraBeat shows a live "Playing on Spotify" bar
- **Graceful fallback** — if the Web API 403s (rate limit / regional / outage), the app shows a clear message and a green button that hands off search to the Spotify app

### App-native features
- 10 distinct screens, each matching the wireframe deck in `design/`:
  Splash · Home · Search · Now Playing · Playlist · Album · Artist · Library · Queue · Settings · Lyrics · Favorites · Device Music
- Custom **Compose Canvas waveform** that breathes only while playing
- **Cinematic dark theme** with the AuraBeat purple + orange palette, centralized as tokens
- Room-backed **favorites**, **recently played**, and **user-created playlists**
- **Add to playlist** bottom sheet on Now Playing
- **Drag-reorder queue** (up/down controls)
- **DataStore preferences** — shuffle/repeat/audio quality/notifications/Wi-Fi-only downloads all survive app kill
- **Haptic feedback** on transport controls
- Splash → Home **crossfade + subtle scale** transition
- Buffering indicator ring around the play button
- 14 unit tests around `PlayerState` and the catalog

---

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.0.20 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM · single-source-of-truth `StateFlow` |
| DI | Hilt |
| Persistence | Room (SQLite) · DataStore Preferences |
| Playback engine | AndroidX Media3 (ExoPlayer + MediaSessionService) |
| Image loading | Coil |
| Networking | Retrofit + OkHttp + KotlinX Serialization |
| Spotify | Spotify Android App Remote SDK (local `.aar`) + Web API via Retrofit |
| Build | Gradle Kotlin DSL · version catalog (`libs.versions.toml`) |
| Min SDK | 24 · Target SDK 34 |

---

## Getting started

### Prerequisites
- **Android Studio Koala (2024.1.1)** or newer
- **JDK 17** (Studio bundles one)
- Android SDK 34
- A real Android device with **USB debugging enabled** *(recommended over emulator — Spotify and background playback behave more like production)*

### Clone and open
```bash
git clone <repo-url>
cd AuraBeat
```

Open the project in Android Studio. On first open Studio will offer to generate the Gradle wrapper — accept. Wait for Gradle sync to finish.

### Add the Spotify App Remote SDK (optional)
Spotify does not publish this SDK to Maven Central. If you want the Spotify playback bridge to compile:

1. Download the SDK zip from https://github.com/spotify/android-sdk/releases
2. Extract, find `spotify-app-remote-release-0.8.0.aar`
3. Copy it to `app/libs/` (create the folder if needed)

Without this file the app still builds if you comment out the `implementation(files("libs/..."))` line in `app/build.gradle.kts` and stub the Spotify calls. The rest of the app works fully without Spotify.

### Set your own Spotify Client ID
`app/build.gradle.kts` currently ships with a demo Client ID. To use your own:

1. Register an app at https://developer.spotify.com/dashboard
2. Add `com.aurabeat.app://spotify-callback` as a redirect URI
3. Copy the Client ID into the `buildConfigField` line in `app/build.gradle.kts`
4. On the dashboard's **User Management** tab, add the Spotify account email you'll test with

### Run it
Plug in a phone (USB debugging on), pick it from Studio's device dropdown, hit ▶ Run. First install is ~30 seconds.

---

## Project layout

```
app/src/main/java/com/aurabeat/app/
├── data/
│   ├── local/          Room entities, DAOs, database
│   ├── model/          Song, Artist, Album, Playlist, Genre
│   ├── preferences/    DataStore-backed AuraPrefs
│   └── repository/     MusicRepository, DeviceMusicRepository
├── di/                 Hilt modules
├── navigation/         Screen sealed class, NavHost setup
├── player/             AuraBeatPlayer (StateFlow SoT), PlayerService, Artwork extraction
├── presentation/       Each screen has its own package + ViewModel
│   ├── album/  artist/  device/  favorites/  home/  library/
│   ├── lyrics/  nowplaying/  playlist/  queue/  search/
│   ├── settings/  splash/  spotify/
├── spotify/            OAuth PKCE, Web API, App Remote controller
├── ui/
│   ├── components/     Reusable Compose primitives (SongRow, MiniPlayer, Waveform...)
│   └── theme/          Color, Type, Shape, Dimensions tokens
├── MainActivity.kt
└── AuraBeatApplication.kt
```

Design deck lives in `design/wireframes.html` — open in any browser to see the ten screens as high-fidelity mobile mockups with tokens and design notes.

---

## Adding your own music

Three paths, no code changes needed:

1. **Copy files** into `app/src/main/res/raw/` (filenames must be lowercase + underscores). Then edit `data/MockCatalog.kt` and point one of the `mediaUri` fields at `android.resource://com.aurabeat.app/raw/<filename_without_extension>`.
2. **Drop music** in your phone's Music folder — Library → Device Music picks it up.
3. **Use the SAF importer** — Library → Device Music → Add files / Add folder.

---

## Building a release APK

```
Build → Generate Signed Bundle / APK → APK
```

Create a keystore the first time (Studio walks you through). Output lands in `app/release/app-release.apk`. See `app/build.gradle.kts` — a `release` signing config placeholder is in place; edit it with your keystore path and passwords before building.

---

## Roadmap

- Full 5-band equalizer
- Downloadable-for-offline mode with encrypted storage
- Home-screen widget
- Wear OS companion
- Supabase-backed cloud playlists and cross-device sync

---

## Contact

Feedback / bug reports: **emmaboat631@gmail.com**

---

*AuraBeat is a portfolio project. Bundled tracks are the property of their respective rights holders. Spotify metadata and playback control © Spotify AB.*

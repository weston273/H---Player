# System Architecture

## 1) Text Diagram

```text
React Native UI Layer
  |- Navigation (Home, Library, Search)
  |- Mini Player + Full Player Overlay
  |- Zustand State Stores
  |- Native Bridges (PlaybackBridge, ScannerBridge, AnalyticsBridge)

Native Bridge Layer (Kotlin RN Modules)
  |- PlaybackModule (Media3 control + state events)
  |- MediaStoreModule (incremental scan + paginated track fetch)
  |- AnalyticsModule (home insights + recommendations)
  |- AudioFocusModule (explicit focus request/release hooks)

Android Core Services
  |- PlayerMediaSessionService (MediaSessionService)
  |- PlaybackEngine (ExoPlayer + MediaSession + queue restore)
  |- LibraryRepository (MediaStore indexing + Room ingestion)
  |- AnalyticsRepository + RecommendationEngine
  |- LyricsRepository (confidence-based matching + cache)
  |- ArtworkCacheManager (memory+disk LRU)

Persistence Layer (Room / SQLite)
  |- tracks, artists, albums, genres
  |- playlists, playlist_tracks
  |- play_events
  |- lyrics_cache
  |- artwork_cache
  |- scan_state

Android Storage APIs
  |- MediaStore Audio collections across all volumes
  |- ContentResolver + content:// URIs
  |- openFileDescriptor() for metadata/artwork fallback reads
  |- Optional SAF tree permission persistence for advanced folder mode
```

## 2) Storage & MediaStore Strategy

- Discovery uses MediaStore audio collections only.
- No raw path crawling.
- Stable keys: `track_id = volume:media_id`, persisted with `media_id` and `content_uri`.
- Multi-volume support via `MediaStore.getExternalVolumeNames(context)`.
- Scoped Storage compliant (Android 10+): no file path synthesis from `RELATIVE_PATH`.
- Reads open via `ContentResolver` and `openFileDescriptor()` where deeper parsing is required.
- Optional advanced mode: persist SAF tree URI permission using `takePersistableUriPermission`.

### Indexing Pipeline
1. Read `scan_state.scan_watermark_sec`.
2. Query MediaStore delta (`DATE_MODIFIED > watermark`) with `IS_MUSIC = 1`.
3. Normalize metadata (`TagNormalizer`).
4. Fallback parse missing tags with `MediaMetadataRetriever` using file descriptors.
5. Upsert artists/albums/genres/tracks in one DB transaction.
6. Update watermark to max modified time seen.
7. Emit scan progress events to RN (`querying -> normalizing -> persisting -> done`).

Notes:
- Delta scans are upsert-only to avoid accidental deletion.
- Optional maintenance job can perform full reconciliation during idle + charging.

## 3) Playback Service Design (Media3)

- Playback is owned by `PlayerMediaSessionService`.
- `PlaybackEngine` hosts ExoPlayer + MediaSession.
- Audio focus is configured through Media3 audio attributes (`setAudioAttributes(..., true)`).
- Lock screen and notification controls flow through MediaSessionService.
- Background playback continues after RN UI closes.

### Queue Persistence & Recovery
- Queue track IDs + index + position are persisted to `SharedPreferences`.
- On service startup, queue is restored by resolving track IDs back to content URIs from Room.
- RN pulls initial state via `getPlaybackState()` and then listens for `playbackStateChanged` events.

## 4) Database Schema (Room / SQLite)

### Core tables
- `tracks`
- `artists`
- `albums`
- `genres`
- `playlists`
- `playlist_tracks`
- `play_events`
- `lyrics_cache`
- `artwork_cache`
- `scan_state`

### Required columns (key examples)
- `tracks`: `track_id`, `media_id`, `content_uri`, `artist_id`, `album_id`, `play_count`, `last_played`
- `play_events`: `track_id`, `played_at`, `duration_listened_ms`, `completion_percentage`, `is_qualified_play`
- `lyrics_cache`: `track_id`, `query_hash`, `status`, `confidence`, `lyrics_text`
- `artwork_cache`: `artwork_key`, `track_id`, `disk_path`, `last_accessed_at`

### Indexes
- `tracks(artist_id)`
- `tracks(album_id)`
- `tracks(play_count)`
- `tracks(last_played)`
- Plus event and cache indexes for query speed.

## 5) Play Definition + Analytics

A play is qualified when:
- `duration_listened >= 30s` OR
- `completion_percentage >= 50%`

Generated lists:
- Most Played Weekly: qualified plays in last 7 days
- Most Played Monthly: qualified plays in last 30 days
- Most Played All-Time
- Most Listened Artist (aggregate qualified plays)
- Favorite Albums (play count normalized by album track count)

## 6) Offline Recommendation Formula (Top 10)

Scoring runs locally from DB-backed aggregates.

For each candidate track:

```text
frequency = play_count / max_play_count
recency = 1 / (1 + age_in_days_since_last_play)
genre_affinity = normalized qualified plays for that genre
artist_affinity = normalized qualified plays for that artist
album_completion = played_distinct_tracks_in_album / total_tracks_in_album

score = 0.35*frequency
      + 0.25*recency
      + 0.15*genre_affinity
      + 0.15*artist_affinity
      + 0.10*album_completion
```

Behavior:
- Returns top 10 tracks by score.
- Incremental update optimization: cached results reused until new play events arrive or TTL expires.

## 7) Metadata + Artwork Strategy

- Primary metadata source: MediaStore.
- Fallback parser: `MediaMetadataRetriever` for missing tags.
- Artwork extraction done off main thread from embedded art bytes.
- Caching:
  - Memory LRU (`LruCache<String, Bitmap>`)
  - Disk cache table + file persistence
  - LRU eviction for disk entries
- Large decode avoided on main thread.

## 8) Lyrics Matching Logic

Pipeline:
1. Normalize title/artist/album (remove parentheses, feat., remaster tokens, special chars).
2. Build query: `title + artist + album?`.
3. Fetch candidates from configured lyrics sources with timeout + retry.
4. Score each candidate using string similarity:
   - Title weight 0.50
   - Artist weight 0.40
   - Album weight 0.10
5. Accept only if score >= 0.84.
6. Cache success/failure in `lyrics_cache`.
7. Skip repeated failed matches via status + query hash.

Playback isolation:
- Lyrics retrieval is async and never blocks playback.

## 9) Native Module Design

- `PlaybackModule`
  - methods: `getPlaybackState`, `togglePlayPause`, `seekTo`, `skipNext`, `skipPrevious`, `playTrack`
  - events: `playbackStateChanged`
- `MediaStoreModule`
  - methods: `runIncrementalScan`, `getTracks(offset,limit)`, `persistDocumentTree`
  - events: `scanProgress`
- `AnalyticsModule`
  - methods: `getHomeInsights`
- `AudioFocusModule`
  - methods: `requestFocus`, `abandonFocus`

## 10) React Native UI Architecture

- Tabs: Home, Library, Search
- Home sections:
  - Recently Played
  - Most Played (Weekly/Monthly/All-Time)
  - Recommended For You (Top 10)
  - Favorite Artists
  - Favorite Albums
- Library modes:
  - Songs, Artists, Albums, Genres
- Player:
  - Persistent mini-player
  - Animated expand/collapse full-player sheet
  - Queue display + basic playback controls

## 11) Folder Structure

```text
src/
  app/
  navigation/
  features/
    home/
    library/
    search/
    player/
    lyrics/
    recommendations/
  components/
    player/
    artwork/
  services/native/
  store/slices/
  theme/
android/app/src/main/java/com/hplayer/
  MainApplication.kt
  player/
    bridge/
    service/
    playback/
    scan/
    repository/
    db/
      dao/
      entities/
      views/
    analytics/
    reco/
    lyrics/
    artwork/
    util/
```

## 12) Scalability Notes (10k+ tracks)

- All scanning and DB operations on IO threads.
- RN lists use paginated loading + FlatList virtualization.
- Track fetching is paginated by offset/limit.
- Caches reduce repeated metadata/artwork work.
- Corrupted files can be flagged (`tracks.is_corrupted`) and excluded from UI/playback queries.

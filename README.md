# H Player - Offline Android Music Player (React Native + Native Android)

This project is an Android-first, offline-first local music player architecture.

It is explicitly **not** a streaming app.

## Highlights
- MediaStore-first indexing (Scoped Storage compatible)
- Content URI playback (no raw path dependency)
- Media3 + MediaSessionService background playback stack
- Room-based local DB for 10,000+ track libraries
- Incremental scanner pipeline running off main thread
- Local analytics + Top 10 recommendation engine
- Lyrics matching pipeline with confidence scoring and failure caching
- React Native UI layer (tab shell, mini-player, full-player transition)

## Docs
- [Architecture](./docs/ARCHITECTURE.md)
- [Performance Checklist](./docs/PERFORMANCE_CHECKLIST.md)
- [Lyrics Legal Notes](./docs/LYRICS_LEGAL.md)

## Tooling Added
- Gradle wrapper files are present in `android/gradle/wrapper` and `android/gradlew*`.
- JavaScript dependency lockfile: `package-lock.json`.
- Expo Dev Client + EAS CLI tooling installed.
- Android test scaffolding:
  - Unit: scanner mapping, recommendation scoring, playback state store.
  - Instrumentation: playback state persistence.

## Local Requirements
- Node.js + npm
- Java 17 (`JAVA_HOME` set) for Gradle tasks
- Android SDK for build/instrumented tests

## Useful Commands
- `npm run typecheck`
- `npm run android`
- `npm run expo:start`
- `npm run expo:android`
- `npm run eas:build:dev`
- `npm run eas:build:preview`
- `npm run eas:build:production`
- `npm run android:lock`
- `npm run android:test:unit`
- `npm run android:test:instrumented`

## Notes for Expo Dev Client
- This project cannot run in Expo Go because it includes custom native modules.
- You must build and install a Dev Client app first (`eas build -p android --profile development`).
- `eas build:configure` and cloud builds require logging into your Expo account (`eas login`).

import {create} from 'zustand';
import {PlaybackBridge, playbackEmitterEvents} from '../../services/native/PlaybackBridge';
import {PlaybackStateSnapshot} from '../../services/native/types';

interface PlaybackStoreState extends PlaybackStateSnapshot {
  hydrated: boolean;
  setSnapshot: (snapshot: Partial<PlaybackStateSnapshot>) => void;
  hydrate: () => Promise<void>;
  togglePlayPause: () => Promise<void>;
  seekTo: (positionMs: number) => Promise<void>;
  skipNext: () => Promise<void>;
  skipPrevious: () => Promise<void>;
  playTrack: (trackId: string) => Promise<void>;
}

const initialState: PlaybackStateSnapshot = {
  isPlaying: false,
  currentTrackId: undefined,
  queue: [],
  positionMs: 0,
  bufferedMs: 0,
  durationMs: 0,
  shuffleEnabled: false,
  repeatMode: 'off',
};

const sleep = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));

export const usePlaybackStore = create<PlaybackStoreState>((set) => ({
  ...initialState,
  hydrated: false,
  setSnapshot: (snapshot) => set((state) => ({...state, ...snapshot})),
  hydrate: async () => {
    let state: PlaybackStateSnapshot | null = null;

    for (let attempt = 0; attempt < 4; attempt += 1) {
      try {
        state = await PlaybackBridge.getState();
        break;
      } catch {
        if (attempt === 3) break;
        await sleep(150 * (attempt + 1));
      }
    }

    if (state) {
      set({...state, hydrated: true});
    } else {
      set({hydrated: true});
    }

    playbackEmitterEvents.subscribe((snapshot) => {
      set((prev) => ({...prev, ...snapshot}));
    });
  },
  togglePlayPause: async () => {
    await PlaybackBridge.togglePlayPause();
  },
  seekTo: async (positionMs) => {
    await PlaybackBridge.seekTo(positionMs);
  },
  skipNext: async () => {
    await PlaybackBridge.skipNext();
  },
  skipPrevious: async () => {
    await PlaybackBridge.skipPrevious();
  },
  playTrack: async (trackId) => {
    await PlaybackBridge.playTrack(trackId);
  },
}));

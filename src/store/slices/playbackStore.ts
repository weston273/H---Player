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

export const usePlaybackStore = create<PlaybackStoreState>((set) => ({
  ...initialState,
  hydrated: false,
  setSnapshot: (snapshot) => set((state) => ({...state, ...snapshot})),
  hydrate: async () => {
    const state = await PlaybackBridge.getState();
    set({...state, hydrated: true});
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

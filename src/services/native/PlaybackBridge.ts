import {NativeEventEmitter, NativeModules} from 'react-native';
import {PlaybackStateSnapshot} from './types';

type PlaybackNativeModule = {
  getPlaybackState: () => Promise<PlaybackStateSnapshot>;
  togglePlayPause: () => Promise<void>;
  seekTo: (positionMs: number) => Promise<void>;
  skipNext: () => Promise<void>;
  skipPrevious: () => Promise<void>;
  playTrack: (trackId: string) => Promise<void>;
};

const native = NativeModules.PlaybackModule as PlaybackNativeModule;
const emitter = new NativeEventEmitter(NativeModules.PlaybackModule);

export const playbackEmitterEvents = {
  subscribe: (callback: (snapshot: Partial<PlaybackStateSnapshot>) => void) => {
    const sub = emitter.addListener('playbackStateChanged', callback);
    return () => sub.remove();
  },
};

export const PlaybackBridge = {
  getState: () => native.getPlaybackState(),
  togglePlayPause: () => native.togglePlayPause(),
  seekTo: (positionMs: number) => native.seekTo(positionMs),
  skipNext: () => native.skipNext(),
  skipPrevious: () => native.skipPrevious(),
  playTrack: (trackId: string) => native.playTrack(trackId),
};

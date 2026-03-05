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

const native = NativeModules.PlaybackModule as PlaybackNativeModule | undefined;
const emitter = native ? new NativeEventEmitter(NativeModules.PlaybackModule) : null;

function missingNativeModuleError() {
  return Promise.reject(new Error('PlaybackModule is not available on this build.'));
}

export const playbackEmitterEvents = {
  subscribe: (callback: (snapshot: Partial<PlaybackStateSnapshot>) => void) => {
    if (!emitter) {
      return () => undefined;
    }
    const sub = emitter.addListener('playbackStateChanged', callback);
    return () => sub.remove();
  },
};

export const PlaybackBridge = {
  getState: () => native?.getPlaybackState() ?? missingNativeModuleError(),
  togglePlayPause: () => native?.togglePlayPause() ?? missingNativeModuleError(),
  seekTo: (positionMs: number) => native?.seekTo(positionMs) ?? missingNativeModuleError(),
  skipNext: () => native?.skipNext() ?? missingNativeModuleError(),
  skipPrevious: () => native?.skipPrevious() ?? missingNativeModuleError(),
  playTrack: (trackId: string) => native?.playTrack(trackId) ?? missingNativeModuleError(),
};

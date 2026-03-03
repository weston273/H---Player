import {NativeEventEmitter, NativeModules} from 'react-native';
import {Track, ScanProgress} from './types';

type ScannerNativeModule = {
  runIncrementalScan: () => Promise<void>;
  getTracks: (offset: number, limit: number) => Promise<Track[]>;
};

const native = NativeModules.MediaStoreModule as ScannerNativeModule;
const emitter = new NativeEventEmitter(NativeModules.MediaStoreModule);

export const ScannerBridge = {
  runIncrementalScan: () => native.runIncrementalScan(),
  getTracks: (offset = 0, limit = 120) => native.getTracks(offset, limit),
  subscribeScanProgress: (callback: (progress: ScanProgress) => void) => {
    const sub = emitter.addListener('scanProgress', callback);
    return () => sub.remove();
  },
};

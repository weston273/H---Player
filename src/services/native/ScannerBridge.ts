import {NativeEventEmitter, NativeModules} from 'react-native';
import {Track, ScanProgress} from './types';

type ScannerNativeModule = {
  runIncrementalScan: () => Promise<void>;
  getTracks: (offset: number, limit: number) => Promise<Track[]>;
};

const native = NativeModules.MediaStoreModule as ScannerNativeModule | undefined;
const emitter = native ? new NativeEventEmitter(NativeModules.MediaStoreModule) : null;

function missingNativeModuleError() {
  return Promise.reject(new Error('MediaStoreModule is not available on this build.'));
}

export const ScannerBridge = {
  runIncrementalScan: () => native?.runIncrementalScan() ?? missingNativeModuleError(),
  getTracks: (offset = 0, limit = 120) => native?.getTracks(offset, limit) ?? missingNativeModuleError(),
  subscribeScanProgress: (callback: (progress: ScanProgress) => void) => {
    if (!emitter) {
      return () => undefined;
    }
    const sub = emitter.addListener('scanProgress', callback);
    return () => sub.remove();
  },
};

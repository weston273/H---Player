import {create} from 'zustand';
import {Track, ScanProgress} from '../../services/native/types';
import {ScannerBridge} from '../../services/native/ScannerBridge';

interface LibraryStoreState {
  tracks: Track[];
  scanProgress: ScanProgress;
  refreshing: boolean;
  loadLibrary: (offset?: number, limit?: number) => Promise<void>;
  incrementalScan: () => Promise<void>;
  setScanProgress: (value: Partial<ScanProgress>) => void;
}

export const useLibraryStore = create<LibraryStoreState>((set) => ({
  tracks: [],
  refreshing: false,
  scanProgress: {
    stage: 'idle',
    processed: 0,
    total: 0,
  },
  setScanProgress: (value) =>
    set((state) => ({
      scanProgress: {
        ...state.scanProgress,
        ...value,
      },
    })),
  loadLibrary: async (offset = 0, limit = 120) => {
    const tracks = await ScannerBridge.getTracks(offset, limit);
    set((state) => ({
      tracks: offset === 0 ? tracks : [...state.tracks, ...tracks],
    }));
  },
  incrementalScan: async () => {
    set({refreshing: true});
    await ScannerBridge.runIncrementalScan();
    const tracks = await ScannerBridge.getTracks(0, 120);
    set({tracks, refreshing: false});
  },
}));

import {useEffect} from 'react';
import {usePlaybackStore} from '../store/slices/playbackStore';
import {useLibraryStore} from '../store/slices/libraryStore';
import {ScannerBridge} from '../services/native/ScannerBridge';

export function usePlaybackBootstrap() {
  const hydratePlayback = usePlaybackStore((state) => state.hydrate);
  const setScanProgress = useLibraryStore((state) => state.setScanProgress);
  const loadLibrary = useLibraryStore((state) => state.loadLibrary);

  useEffect(() => {
    hydratePlayback();
    loadLibrary(0, 120);

    const unsubscribe = ScannerBridge.subscribeScanProgress((progress) => {
      setScanProgress(progress);
    });

    return () => {
      unsubscribe();
    };
  }, [hydratePlayback, loadLibrary, setScanProgress]);
}

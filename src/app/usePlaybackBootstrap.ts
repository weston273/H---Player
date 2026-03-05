import {useEffect} from 'react';
import {PermissionsAndroid, Platform} from 'react-native';
import {usePlaybackStore} from '../store/slices/playbackStore';
import {useLibraryStore} from '../store/slices/libraryStore';
import {ScannerBridge} from '../services/native/ScannerBridge';

async function ensureAudioPermission(): Promise<boolean> {
  if (Platform.OS !== 'android') return true;

  const permission =
    Platform.Version >= 33
      ? PermissionsAndroid.PERMISSIONS.READ_MEDIA_AUDIO
      : PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE;

  const granted = await PermissionsAndroid.check(permission);
  if (granted) return true;

  const result = await PermissionsAndroid.request(permission);
  return result === PermissionsAndroid.RESULTS.GRANTED;
}

export function usePlaybackBootstrap() {
  const hydratePlayback = usePlaybackStore((state) => state.hydrate);
  const setScanProgress = useLibraryStore((state) => state.setScanProgress);
  const loadLibrary = useLibraryStore((state) => state.loadLibrary);

  useEffect(() => {
    let active = true;

    const bootstrap = async () => {
      await hydratePlayback().catch(() => undefined);

      const canReadAudio = await ensureAudioPermission().catch(() => false);
      if (!active || !canReadAudio) return;

      await loadLibrary(0, 120).catch(() => undefined);
    };

    bootstrap();

    const unsubscribe = ScannerBridge.subscribeScanProgress((progress) => {
      setScanProgress(progress);
    });

    return () => {
      active = false;
      unsubscribe();
    };
  }, [hydratePlayback, loadLibrary, setScanProgress]);
}

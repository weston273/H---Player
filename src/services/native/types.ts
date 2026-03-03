export type RepeatMode = 'off' | 'all' | 'one';

export interface Track {
  id: string;
  mediaStoreId: number;
  contentUri: string;
  title: string;
  artistName: string;
  albumName: string;
  albumId?: number;
  durationMs: number;
  genreName?: string;
  trackNumber?: number;
  artworkKey?: string;
}

export interface QueueItem {
  trackId: string;
  index: number;
}

export interface PlaybackStateSnapshot {
  isPlaying: boolean;
  currentTrackId?: string;
  queue: QueueItem[];
  positionMs: number;
  bufferedMs: number;
  durationMs: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
}

export interface ScanProgress {
  stage: 'idle' | 'querying' | 'normalizing' | 'persisting' | 'done' | 'error';
  processed: number;
  total: number;
  message?: string;
}

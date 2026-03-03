import React, {useMemo, useState} from 'react';
import {FlatList, Pressable, StyleSheet, Text, View} from 'react-native';
import {useLibraryStore} from '../../store/slices/libraryStore';
import {usePlaybackStore} from '../../store/slices/playbackStore';
import {appTheme} from '../../theme/theme';
import {ArtworkImage} from '../../components/artwork/ArtworkImage';
import {Track} from '../../services/native/types';

type LibraryMode = 'songs' | 'artists' | 'albums' | 'genres';

const MODES: LibraryMode[] = ['songs', 'artists', 'albums', 'genres'];

function LibraryHeader({mode, onMode}: {mode: LibraryMode; onMode: (next: LibraryMode) => void}) {
  return (
    <View style={styles.modeRow}>
      {MODES.map((item) => {
        const active = item === mode;
        return (
          <Pressable key={item} style={[styles.modeChip, active && styles.modeChipActive]} onPress={() => onMode(item)}>
            <Text style={[styles.modeText, active && styles.modeTextActive]}>{item.toUpperCase()}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

export function LibraryScreen() {
  const [mode, setMode] = useState<LibraryMode>('songs');
  const tracks = useLibraryStore((state) => state.tracks);
  const loadLibrary = useLibraryStore((state) => state.loadLibrary);
  const incrementalScan = useLibraryStore((state) => state.incrementalScan);
  const scanProgress = useLibraryStore((state) => state.scanProgress);
  const playTrack = usePlaybackStore((state) => state.playTrack);

  const grouped = useMemo(() => {
    const byArtist = new Map<string, Track[]>();
    const byAlbum = new Map<string, Track[]>();
    const byGenre = new Map<string, Track[]>();

    for (const t of tracks) {
      const artistKey = t.artistName || 'Unknown Artist';
      const albumKey = t.albumName || 'Unknown Album';
      const genreKey = t.genreName || 'Unknown Genre';

      byArtist.set(artistKey, [...(byArtist.get(artistKey) ?? []), t]);
      byAlbum.set(albumKey, [...(byAlbum.get(albumKey) ?? []), t]);
      byGenre.set(genreKey, [...(byGenre.get(genreKey) ?? []), t]);
    }

    return {byArtist, byAlbum, byGenre};
  }, [tracks]);

  return (
    <View style={styles.root}>
      <Text style={styles.title}>Library</Text>
      <LibraryHeader mode={mode} onMode={setMode} />

      <View style={styles.scanRow}>
        <Pressable style={styles.scanButton} onPress={incrementalScan}>
          <Text style={styles.scanText}>Incremental Scan</Text>
        </Pressable>
        <Text style={styles.scanInfo}>
          {scanProgress.stage} {scanProgress.processed}/{scanProgress.total}
        </Text>
      </View>

      {mode === 'songs' ? (
        <FlatList
          data={tracks}
          windowSize={12}
          initialNumToRender={18}
          maxToRenderPerBatch={24}
          keyExtractor={(item) => item.id}
          contentContainerStyle={{paddingBottom: 140}}
          onEndReached={() => loadLibrary(tracks.length, 120)}
          onEndReachedThreshold={0.6}
          renderItem={({item}) => (
            <Pressable style={styles.trackRow} onPress={() => playTrack(item.id)}>
              <ArtworkImage artworkKey={item.artworkKey} size={48} radius={10} />
              <View style={{flex: 1}}>
                <Text style={styles.trackTitle} numberOfLines={1}>
                  {item.title}
                </Text>
                <Text style={styles.trackSubtitle} numberOfLines={1}>
                  {item.artistName} | {item.albumName}
                </Text>
              </View>
            </Pressable>
          )}
        />
      ) : null}

      {mode === 'artists' ? (
        <FlatList
          data={[...grouped.byArtist.entries()]}
          keyExtractor={(item) => item[0]}
          contentContainerStyle={{paddingBottom: 140}}
          renderItem={({item}) => (
            <View style={styles.groupRow}>
              <Text style={styles.groupTitle}>{item[0]}</Text>
              <Text style={styles.groupMeta}>{item[1].length} tracks</Text>
            </View>
          )}
        />
      ) : null}

      {mode === 'albums' ? (
        <FlatList
          data={[...grouped.byAlbum.entries()]}
          keyExtractor={(item) => item[0]}
          contentContainerStyle={{paddingBottom: 140}}
          renderItem={({item}) => (
            <View style={styles.groupRow}>
              <Text style={styles.groupTitle}>{item[0]}</Text>
              <Text style={styles.groupMeta}>{item[1].length} tracks</Text>
            </View>
          )}
        />
      ) : null}

      {mode === 'genres' ? (
        <FlatList
          data={[...grouped.byGenre.entries()]}
          keyExtractor={(item) => item[0]}
          contentContainerStyle={{paddingBottom: 140}}
          renderItem={({item}) => (
            <View style={styles.groupRow}>
              <Text style={styles.groupTitle}>{item[0]}</Text>
              <Text style={styles.groupMeta}>{item[1].length} tracks</Text>
            </View>
          )}
        />
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: appTheme.colors.background,
  },
  title: {
    marginHorizontal: 16,
    marginTop: 20,
    color: appTheme.colors.textPrimary,
    fontSize: 36,
    fontWeight: '900',
    fontFamily: appTheme.typography.hero,
  },
  modeRow: {
    marginTop: 10,
    marginHorizontal: 16,
    flexDirection: 'row',
    gap: 8,
  },
  modeChip: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: appTheme.colors.divider,
    paddingVertical: 6,
    paddingHorizontal: 12,
  },
  modeChipActive: {
    borderColor: appTheme.colors.accent,
    backgroundColor: '#252D3D',
  },
  modeText: {
    color: appTheme.colors.textSecondary,
    fontSize: 11,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
  },
  modeTextActive: {
    color: appTheme.colors.textPrimary,
  },
  scanRow: {
    marginTop: 14,
    marginHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  scanButton: {
    backgroundColor: appTheme.colors.card,
    borderColor: appTheme.colors.divider,
    borderWidth: 1,
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 10,
  },
  scanText: {
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
  },
  scanInfo: {
    color: appTheme.colors.textSecondary,
    fontSize: 12,
    fontFamily: appTheme.typography.body,
  },
  trackRow: {
    marginHorizontal: 16,
    marginTop: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  trackTitle: {
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
    fontSize: 14,
  },
  trackSubtitle: {
    color: appTheme.colors.textSecondary,
    marginTop: 2,
    fontSize: 12,
    fontFamily: appTheme.typography.body,
  },
  groupRow: {
    marginHorizontal: 16,
    marginTop: 8,
    marginBottom: 4,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: appTheme.colors.divider,
    paddingVertical: 10,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  groupTitle: {
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
  },
  groupMeta: {
    color: appTheme.colors.textSecondary,
  },
});

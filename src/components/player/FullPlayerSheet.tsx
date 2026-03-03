import React from 'react';
import {Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {appTheme} from '../../theme/theme';
import {usePlaybackStore} from '../../store/slices/playbackStore';
import {useLibraryStore} from '../../store/slices/libraryStore';
import {ArtworkImage} from '../artwork/ArtworkImage';

interface FullPlayerSheetProps {
  onClose: () => void;
}

export function FullPlayerSheet({onClose}: FullPlayerSheetProps) {
  const currentTrackId = usePlaybackStore((state) => state.currentTrackId);
  const isPlaying = usePlaybackStore((state) => state.isPlaying);
  const togglePlayPause = usePlaybackStore((state) => state.togglePlayPause);
  const skipNext = usePlaybackStore((state) => state.skipNext);
  const skipPrevious = usePlaybackStore((state) => state.skipPrevious);
  const queue = usePlaybackStore((state) => state.queue);
  const tracks = useLibraryStore((state) => state.tracks);

  const track = tracks.find((item) => item.id === currentTrackId);
  const queueTracks = queue
    .map((q) => tracks.find((t) => t.id === q.trackId))
    .filter((t): t is NonNullable<typeof t> => Boolean(t));

  if (!track) {
    return null;
  }

  return (
    <View style={styles.sheet}>
      <Pressable style={styles.closeHandle} onPress={onClose}>
        <View style={styles.handleBar} />
      </Pressable>
      <ScrollView contentContainerStyle={styles.content}>
        <ArtworkImage artworkKey={track.artworkKey} size={320} radius={24} />
        <Text style={styles.title}>{track.title}</Text>
        <Text style={styles.subtitle}>{track.artistName}</Text>

        <View style={styles.controlsRow}>
          <Pressable onPress={skipPrevious} style={styles.ctrlButton}>
            <Text style={styles.ctrlLabel}>Prev</Text>
          </Pressable>
          <Pressable onPress={togglePlayPause} style={styles.ctrlPrimary}>
            <Text style={styles.ctrlLabel}>{isPlaying ? 'Pause' : 'Play'}</Text>
          </Pressable>
          <Pressable onPress={skipNext} style={styles.ctrlButton}>
            <Text style={styles.ctrlLabel}>Next</Text>
          </Pressable>
        </View>

        <Text style={styles.sectionHeader}>Up Next</Text>
        {queueTracks.map((item) => (
          <View key={item.id} style={styles.queueRow}>
            <ArtworkImage artworkKey={item.artworkKey} size={38} radius={8} />
            <View style={{flex: 1}}>
              <Text style={styles.queueTitle} numberOfLines={1}>
                {item.title}
              </Text>
              <Text style={styles.queueSubtitle} numberOfLines={1}>
                {item.artistName}
              </Text>
            </View>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  sheet: {
    marginTop: 40,
    flex: 1,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    backgroundColor: '#0B0F17',
    overflow: 'hidden',
  },
  closeHandle: {
    alignItems: 'center',
    paddingVertical: 12,
  },
  handleBar: {
    width: 40,
    height: 5,
    borderRadius: 3,
    backgroundColor: '#A8B2C560',
  },
  content: {
    alignItems: 'center',
    paddingBottom: 60,
    paddingHorizontal: 20,
  },
  title: {
    marginTop: 20,
    color: appTheme.colors.textPrimary,
    fontSize: 28,
    fontWeight: '800',
    textAlign: 'center',
  },
  subtitle: {
    marginTop: 6,
    color: appTheme.colors.textSecondary,
    fontSize: 16,
  },
  controlsRow: {
    marginTop: 24,
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-evenly',
  },
  ctrlButton: {
    paddingVertical: 12,
    paddingHorizontal: 18,
  },
  ctrlPrimary: {
    backgroundColor: appTheme.colors.accent,
    paddingVertical: 12,
    paddingHorizontal: 26,
    borderRadius: 20,
  },
  ctrlLabel: {
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
  },
  sectionHeader: {
    marginTop: 30,
    marginBottom: 12,
    width: '100%',
    color: appTheme.colors.textPrimary,
    fontWeight: '800',
    fontSize: 18,
  },
  queueRow: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 12,
  },
  queueTitle: {
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
    fontSize: 14,
  },
  queueSubtitle: {
    color: appTheme.colors.textSecondary,
    fontSize: 12,
  },
});

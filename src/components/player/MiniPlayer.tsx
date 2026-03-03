import React from 'react';
import {Pressable, StyleSheet, Text, View} from 'react-native';
import {usePlaybackStore} from '../../store/slices/playbackStore';
import {useLibraryStore} from '../../store/slices/libraryStore';
import {ArtworkImage} from '../artwork/ArtworkImage';
import {appTheme} from '../../theme/theme';

interface MiniPlayerProps {
  onPress: () => void;
}

export function MiniPlayer({onPress}: MiniPlayerProps) {
  const currentTrackId = usePlaybackStore((state) => state.currentTrackId);
  const isPlaying = usePlaybackStore((state) => state.isPlaying);
  const togglePlayPause = usePlaybackStore((state) => state.togglePlayPause);
  const tracks = useLibraryStore((state) => state.tracks);

  const track = tracks.find((item) => item.id === currentTrackId);
  if (!track) {
    return null;
  }

  return (
    <View style={styles.root}>
      <Pressable style={styles.touchArea} onPress={onPress}>
        <ArtworkImage artworkKey={track.artworkKey} size={42} />
        <View style={styles.textWrap}>
          <Text numberOfLines={1} style={styles.title}>
            {track.title}
          </Text>
          <Text numberOfLines={1} style={styles.subtitle}>
            {track.artistName}
          </Text>
        </View>
      </Pressable>
      <Pressable onPress={togglePlayPause} style={styles.actionButton}>
        <Text style={styles.actionText}>{isPlaying ? 'Pause' : 'Play'}</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    position: 'absolute',
    left: 10,
    right: 10,
    bottom: 78,
    height: 64,
    borderRadius: 18,
    backgroundColor: appTheme.colors.card,
    borderWidth: 1,
    borderColor: appTheme.colors.divider,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
  },
  touchArea: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  textWrap: {
    flex: 1,
  },
  title: {
    color: appTheme.colors.textPrimary,
    fontSize: 14,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
  },
  subtitle: {
    marginTop: 2,
    color: appTheme.colors.textSecondary,
    fontSize: 12,
    fontFamily: appTheme.typography.body,
  },
  actionButton: {
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  actionText: {
    color: appTheme.colors.accentSoft,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
  },
});

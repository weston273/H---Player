import React, {useMemo, useState} from 'react';
import {FlatList, Pressable, StyleSheet, Text, TextInput, View} from 'react-native';
import {useLibraryStore} from '../../store/slices/libraryStore';
import {usePlaybackStore} from '../../store/slices/playbackStore';
import {appTheme} from '../../theme/theme';

export function SearchScreen() {
  const [query, setQuery] = useState('');
  const tracks = useLibraryStore((state) => state.tracks);
  const playTrack = usePlaybackStore((state) => state.playTrack);

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) {
      return tracks.slice(0, 50);
    }

    return tracks
      .filter(
        (t) =>
          t.title.toLowerCase().includes(q) ||
          t.artistName.toLowerCase().includes(q) ||
          t.albumName.toLowerCase().includes(q),
      )
      .slice(0, 150);
  }, [query, tracks]);

  return (
    <View style={styles.root}>
      <Text style={styles.heading}>Search</Text>
      <TextInput
        value={query}
        onChangeText={setQuery}
        placeholder="Songs, artists, albums"
        placeholderTextColor={appTheme.colors.textSecondary}
        style={styles.input}
      />

      <FlatList
        data={results}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{paddingBottom: 130}}
        windowSize={10}
        initialNumToRender={20}
        renderItem={({item}) => (
          <Pressable style={styles.row} onPress={() => playTrack(item.id)}>
            <View style={styles.dot} />
            <View style={{flex: 1}}>
              <Text style={styles.title}>{item.title}</Text>
              <Text style={styles.subtitle} numberOfLines={1}>
                {item.artistName} | {item.albumName}
              </Text>
            </View>
          </Pressable>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: appTheme.colors.background,
  },
  heading: {
    marginHorizontal: 16,
    marginTop: 20,
    color: appTheme.colors.textPrimary,
    fontSize: 36,
    fontWeight: '900',
    fontFamily: appTheme.typography.hero,
  },
  input: {
    marginTop: 14,
    marginHorizontal: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: appTheme.colors.divider,
    backgroundColor: appTheme.colors.surface,
    color: appTheme.colors.textPrimary,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  row: {
    marginHorizontal: 16,
    marginTop: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: appTheme.colors.accent,
  },
  title: {
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
    fontFamily: appTheme.typography.title,
    fontSize: 14,
  },
  subtitle: {
    marginTop: 2,
    color: appTheme.colors.textSecondary,
    fontSize: 12,
    fontFamily: appTheme.typography.body,
  },
});

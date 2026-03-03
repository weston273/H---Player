import React, {useEffect, useMemo, useState} from 'react';
import {FlatList, Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {useLibraryStore} from '../../store/slices/libraryStore';
import {usePlaybackStore} from '../../store/slices/playbackStore';
import {ArtworkImage} from '../../components/artwork/ArtworkImage';
import {appTheme} from '../../theme/theme';
import {AnalyticsBridge, HomeInsights} from '../../services/native/AnalyticsBridge';
import {Track} from '../../services/native/types';

function TrackStrip({title, tracks, onPlay}: {title: string; tracks: Track[]; onPlay: (id: string) => void}) {
  if (!tracks.length) {
    return null;
  }

  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title}</Text>
      <FlatList
        horizontal
        data={tracks}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{gap: 10}}
        showsHorizontalScrollIndicator={false}
        renderItem={({item}) => (
          <Pressable style={styles.albumCard} onPress={() => onPlay(item.id)}>
            <ArtworkImage artworkKey={item.artworkKey} size={136} radius={14} />
            <Text style={styles.cardTitle} numberOfLines={1}>
              {item.title}
            </Text>
            <Text style={styles.cardSubtitle} numberOfLines={1}>
              {item.artistName}
            </Text>
          </Pressable>
        )}
      />
    </View>
  );
}

export function HomeScreen() {
  const playTrack = usePlaybackStore((state) => state.playTrack);
  const tracks = useLibraryStore((state) => state.tracks);
  const [insights, setInsights] = useState<HomeInsights | null>(null);

  useEffect(() => {
    AnalyticsBridge.getHomeInsights().then(setInsights).catch(() => {
      const fallback = tracks.slice(0, 10);
      setInsights({
        recentlyPlayed: fallback,
        mostPlayedWeekly: fallback,
        mostPlayedMonthly: fallback,
        mostPlayedAllTime: fallback,
        recommendedTop10: fallback,
        favoriteArtists: [],
        favoriteAlbums: [],
      });
    });
  }, [tracks]);

  const heroTracks = useMemo(() => insights?.recommendedTop10 ?? tracks.slice(0, 10), [insights, tracks]);

  return (
    <ScrollView style={styles.root} contentContainerStyle={styles.content}>
      <Text style={styles.heroHeading}>Listen Now</Text>
      <Text style={styles.heroSubheading}>Offline library, instant playback, zero network dependency.</Text>

      <TrackStrip title="Recommended For You" tracks={heroTracks} onPlay={playTrack} />
      <TrackStrip title="Recently Played" tracks={insights?.recentlyPlayed ?? []} onPlay={playTrack} />
      <TrackStrip title="Most Played (Weekly)" tracks={insights?.mostPlayedWeekly ?? []} onPlay={playTrack} />
      <TrackStrip title="Most Played (Monthly)" tracks={insights?.mostPlayedMonthly ?? []} onPlay={playTrack} />
      <TrackStrip title="Most Played (All-Time)" tracks={insights?.mostPlayedAllTime ?? []} onPlay={playTrack} />

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Favorite Artists</Text>
        {(insights?.favoriteArtists ?? []).map((artist) => (
          <View key={artist.artistId} style={styles.inlineRow}>
            <Text style={styles.inlineLabel}>{artist.name}</Text>
            <Text style={styles.inlineScore}>{artist.score.toFixed(2)}</Text>
          </View>
        ))}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Favorite Albums</Text>
        {(insights?.favoriteAlbums ?? []).map((album) => (
          <View key={album.albumId} style={styles.inlineRow}>
            <Text style={styles.inlineLabel}>{album.name}</Text>
            <Text style={styles.inlineScore}>{album.score.toFixed(2)}</Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: appTheme.colors.background,
  },
  content: {
    paddingBottom: 150,
  },
  heroHeading: {
    marginTop: 22,
    marginHorizontal: 16,
    color: appTheme.colors.textPrimary,
    fontSize: 40,
    fontWeight: '900',
    letterSpacing: 0.3,
    fontFamily: appTheme.typography.hero,
  },
  heroSubheading: {
    marginTop: 6,
    marginHorizontal: 16,
    color: appTheme.colors.textSecondary,
    fontSize: 14,
    fontFamily: appTheme.typography.body,
  },
  section: {
    marginTop: 22,
  },
  sectionTitle: {
    marginHorizontal: 16,
    marginBottom: 10,
    color: appTheme.colors.textPrimary,
    fontSize: 21,
    fontWeight: '800',
    fontFamily: appTheme.typography.title,
  },
  albumCard: {
    width: 136,
  },
  cardTitle: {
    marginTop: 8,
    color: appTheme.colors.textPrimary,
    fontWeight: '700',
    fontSize: 13,
    fontFamily: appTheme.typography.title,
  },
  cardSubtitle: {
    marginTop: 2,
    color: appTheme.colors.textSecondary,
    fontSize: 12,
    fontFamily: appTheme.typography.body,
  },
  inlineRow: {
    marginHorizontal: 16,
    marginBottom: 8,
    borderBottomColor: appTheme.colors.divider,
    borderBottomWidth: StyleSheet.hairlineWidth,
    paddingVertical: 8,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  inlineLabel: {
    color: appTheme.colors.textPrimary,
    fontWeight: '600',
    fontFamily: appTheme.typography.body,
  },
  inlineScore: {
    color: appTheme.colors.textSecondary,
    fontFamily: appTheme.typography.body,
  },
});

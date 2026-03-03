import {NativeModules} from 'react-native';
import {Track} from './types';

export interface HomeInsights {
  recentlyPlayed: Track[];
  mostPlayedWeekly: Track[];
  mostPlayedMonthly: Track[];
  mostPlayedAllTime: Track[];
  recommendedTop10: Track[];
  favoriteArtists: Array<{artistId: string; name: string; score: number}>;
  favoriteAlbums: Array<{albumId: string; name: string; artistName: string; score: number}>;
}

type AnalyticsNativeModule = {
  getHomeInsights: () => Promise<HomeInsights>;
};

const native = NativeModules.AnalyticsModule as AnalyticsNativeModule;

export const AnalyticsBridge = {
  getHomeInsights: () => native.getHomeInsights(),
};

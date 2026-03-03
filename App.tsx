import React from 'react';
import {GestureHandlerRootView} from 'react-native-gesture-handler';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {NavigationContainer, DefaultTheme} from '@react-navigation/native';
import {StatusBar} from 'react-native';
import {RootTabs} from './src/navigation/RootTabs';
import {NowPlayingOverlay} from './src/components/player/NowPlayingOverlay';
import {usePlaybackBootstrap} from './src/app/usePlaybackBootstrap';
import {appTheme} from './src/theme/theme';

const navTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: appTheme.colors.background,
    card: appTheme.colors.surface,
    text: appTheme.colors.textPrimary,
    border: appTheme.colors.divider,
    primary: appTheme.colors.accent,
  },
};

export default function App() {
  usePlaybackBootstrap();

  return (
    <GestureHandlerRootView style={{flex: 1}}>
      <SafeAreaProvider>
        <StatusBar barStyle="light-content" />
        <NavigationContainer theme={navTheme}>
          <RootTabs />
          <NowPlayingOverlay />
        </NavigationContainer>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

import React, {useMemo, useState} from 'react';
import {
  Animated,
  Dimensions,
  PanResponder,
  Pressable,
  StyleSheet,
  View,
} from 'react-native';
import {MiniPlayer} from './MiniPlayer';
import {FullPlayerSheet} from './FullPlayerSheet';
import {usePlaybackStore} from '../../store/slices/playbackStore';

const SCREEN_HEIGHT = Dimensions.get('window').height;

export function NowPlayingOverlay() {
  const [expanded, setExpanded] = useState(false);
  const currentTrackId = usePlaybackStore((state) => state.currentTrackId);
  const progress = useMemo(() => new Animated.Value(0), []);

  const open = () => {
    setExpanded(true);
    Animated.spring(progress, {
      toValue: 1,
      useNativeDriver: true,
      bounciness: 6,
      speed: 16,
    }).start();
  };

  const close = () => {
    Animated.timing(progress, {
      toValue: 0,
      duration: 220,
      useNativeDriver: true,
    }).start(() => setExpanded(false));
  };

  const panResponder = PanResponder.create({
    onMoveShouldSetPanResponder: (_, gesture) => expanded && gesture.dy > 6,
    onPanResponderMove: (_, gesture) => {
      if (gesture.dy > 0) {
        const next = 1 - Math.min(gesture.dy / (SCREEN_HEIGHT * 0.6), 1);
        progress.setValue(next);
      }
    },
    onPanResponderRelease: (_, gesture) => {
      if (gesture.dy > 120 || gesture.vy > 1.2) {
        close();
      } else {
        open();
      }
    },
  });

  if (!currentTrackId) {
    return null;
  }

  const sheetTranslateY = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [SCREEN_HEIGHT, 0],
  });

  return (
    <View pointerEvents="box-none" style={StyleSheet.absoluteFill}>
      {!expanded ? <MiniPlayer onPress={open} /> : null}
      <Animated.View
        pointerEvents={expanded ? 'auto' : 'none'}
        style={[styles.sheetContainer, {transform: [{translateY: sheetTranslateY}]}]}
        {...panResponder.panHandlers}>
        <Pressable style={styles.scrim} onPress={close} />
        <FullPlayerSheet onClose={close} />
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  sheetContainer: {
    ...StyleSheet.absoluteFillObject,
  },
  scrim: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(2, 4, 7, 0.5)',
  },
});

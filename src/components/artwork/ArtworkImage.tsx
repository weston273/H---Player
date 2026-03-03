import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {appTheme} from '../../theme/theme';

interface ArtworkImageProps {
  artworkKey?: string;
  size: number;
  radius?: number;
}

export function ArtworkImage({artworkKey, size, radius = 10}: ArtworkImageProps) {
  return (
    <View
      style={[
        styles.frame,
        {
          width: size,
          height: size,
          borderRadius: radius,
        },
      ]}>
      <Text style={styles.placeholder} numberOfLines={1}>
        {artworkKey ? artworkKey.slice(0, 2).toUpperCase() : 'ART'}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  frame: {
    backgroundColor: '#253043',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: appTheme.colors.divider,
  },
  placeholder: {
    color: appTheme.colors.textSecondary,
    fontWeight: '700',
  },
});

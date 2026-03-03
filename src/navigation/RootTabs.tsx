import React from 'react';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import {HomeScreen} from '../features/home/HomeScreen';
import {LibraryScreen} from '../features/library/LibraryScreen';
import {SearchScreen} from '../features/search/SearchScreen';
import {appTheme} from '../theme/theme';

export type RootTabParamList = {
  Home: undefined;
  Library: undefined;
  Search: undefined;
};

const Tab = createBottomTabNavigator<RootTabParamList>();

export function RootTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: appTheme.colors.textPrimary,
        tabBarInactiveTintColor: appTheme.colors.textSecondary,
        tabBarStyle: {
          backgroundColor: appTheme.colors.surface,
          borderTopColor: appTheme.colors.divider,
        },
      }}>
      <Tab.Screen name="Home" component={HomeScreen} />
      <Tab.Screen name="Library" component={LibraryScreen} />
      <Tab.Screen name="Search" component={SearchScreen} />
    </Tab.Navigator>
  );
}

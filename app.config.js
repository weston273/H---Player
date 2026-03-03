module.exports = {
  expo: {
    name: 'H Player',
    slug: 'h-player',
    scheme: 'hplayer',
    version: '1.0.0',
    orientation: 'portrait',
    userInterfaceStyle: 'dark',
    splash: {
      backgroundColor: '#0E1118'
    },
    android: {
      package: 'com.hplayer'
    },
    plugins: ['expo-dev-client'],
    extra: {
      eas: {
        projectId: '7528f685-cef1-4250-bccc-e037ffe5f9b6'
      }
    }
  }
};

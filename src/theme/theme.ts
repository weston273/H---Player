export const appTheme = {
  colors: {
    background: '#0E1118',
    surface: '#161B25',
    card: '#1D2430',
    accent: '#E2465E',
    accentSoft: '#F47C8E',
    textPrimary: '#F4F6FB',
    textSecondary: '#A8B2C5',
    divider: '#2A3445',
    success: '#49C58E',
  },
  typography: {
    hero: 'Fraunces72pt-Black',
    title: 'Manrope-Bold',
    body: 'Manrope-Regular',
  },
  spacing: {
    xxs: 4,
    xs: 8,
    sm: 12,
    md: 16,
    lg: 20,
    xl: 24,
    xxl: 32,
  },
  radius: {
    sm: 10,
    md: 14,
    lg: 20,
  },
};

export type AppTheme = typeof appTheme;

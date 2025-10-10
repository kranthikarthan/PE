// Color Palette System for Bank Branding
// Professional, modern color schemes for different bank types

export interface ColorPalette {
  name: string;
  type: 'corporate' | 'modern' | 'classic' | 'tech' | 'luxury';
  description: string;
  colors: {
    primary: {
      main: string;
      light: string;
      dark: string;
      contrastText: string;
    };
    secondary: {
      main: string;
      light: string;
      dark: string;
      contrastText: string;
    };
    accent: {
      main: string;
      light: string;
      dark: string;
    };
    background: {
      default: string;
      paper: string;
      elevated: string;
    };
    text: {
      primary: string;
      secondary: string;
      disabled: string;
    };
    success: string;
    warning: string;
    error: string;
    info: string;
    gradient: {
      primary: string;
      secondary: string;
    };
  };
}

export const colorPalettes: ColorPalette[] = [
  {
    name: 'Corporate Blue',
    type: 'corporate',
    description: 'Professional corporate banking theme',
    colors: {
      primary: {
        main: '#1e3a8a',
        light: '#3b82f6',
        dark: '#1e40af',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#64748b',
        light: '#94a3b8',
        dark: '#475569',
        contrastText: '#ffffff',
      },
      accent: {
        main: '#0ea5e9',
        light: '#38bdf8',
        dark: '#0284c7',
      },
      background: {
        default: '#f8fafc',
        paper: '#ffffff',
        elevated: '#f1f5f9',
      },
      text: {
        primary: '#0f172a',
        secondary: '#64748b',
        disabled: '#94a3b8',
      },
      success: '#10b981',
      warning: '#f59e0b',
      error: '#ef4444',
      info: '#3b82f6',
      gradient: {
        primary: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
        secondary: 'linear-gradient(135deg, #64748b 0%, #94a3b8 100%)',
      },
    },
  },
  {
    name: 'Modern Green',
    type: 'modern',
    description: 'Modern fintech-inspired green theme',
    colors: {
      primary: {
        main: '#059669',
        light: '#10b981',
        dark: '#047857',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#6b7280',
        light: '#9ca3af',
        dark: '#4b5563',
        contrastText: '#ffffff',
      },
      accent: {
        main: '#06b6d4',
        light: '#22d3ee',
        dark: '#0891b2',
      },
      background: {
        default: '#f9fafb',
        paper: '#ffffff',
        elevated: '#f3f4f6',
      },
      text: {
        primary: '#111827',
        secondary: '#6b7280',
        disabled: '#9ca3af',
      },
      success: '#10b981',
      warning: '#f59e0b',
      error: '#ef4444',
      info: '#06b6d4',
      gradient: {
        primary: 'linear-gradient(135deg, #059669 0%, #10b981 100%)',
        secondary: 'linear-gradient(135deg, #6b7280 0%, #9ca3af 100%)',
      },
    },
  },
  {
    name: 'Classic Navy',
    type: 'classic',
    description: 'Traditional banking navy theme',
    colors: {
      primary: {
        main: '#1e293b',
        light: '#334155',
        dark: '#0f172a',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#dc2626',
        light: '#ef4444',
        dark: '#b91c1c',
        contrastText: '#ffffff',
      },
      accent: {
        main: '#f59e0b',
        light: '#fbbf24',
        dark: '#d97706',
      },
      background: {
        default: '#f8fafc',
        paper: '#ffffff',
        elevated: '#f1f5f9',
      },
      text: {
        primary: '#0f172a',
        secondary: '#475569',
        disabled: '#94a3b8',
      },
      success: '#059669',
      warning: '#d97706',
      error: '#dc2626',
      info: '#0284c7',
      gradient: {
        primary: 'linear-gradient(135deg, #1e293b 0%, #334155 100%)',
        secondary: 'linear-gradient(135deg, #dc2626 0%, #ef4444 100%)',
      },
    },
  },
  {
    name: 'Tech Purple',
    type: 'tech',
    description: 'Modern tech startup purple theme',
    colors: {
      primary: {
        main: '#7c3aed',
        light: '#8b5cf6',
        dark: '#6d28d9',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#06b6d4',
        light: '#22d3ee',
        dark: '#0891b2',
        contrastText: '#ffffff',
      },
      accent: {
        main: '#f59e0b',
        light: '#fbbf24',
        dark: '#d97706',
      },
      background: {
        default: '#fafafa',
        paper: '#ffffff',
        elevated: '#f5f5f5',
      },
      text: {
        primary: '#111827',
        secondary: '#6b7280',
        disabled: '#9ca3af',
      },
      success: '#10b981',
      warning: '#f59e0b',
      error: '#ef4444',
      info: '#06b6d4',
      gradient: {
        primary: 'linear-gradient(135deg, #7c3aed 0%, #8b5cf6 100%)',
        secondary: 'linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%)',
      },
    },
  },
  {
    name: 'Luxury Gold',
    type: 'luxury',
    description: 'Premium luxury banking gold theme',
    colors: {
      primary: {
        main: '#b45309',
        light: '#d97706',
        dark: '#92400e',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#1f2937',
        light: '#374151',
        dark: '#111827',
        contrastText: '#ffffff',
      },
      accent: {
        main: '#dc2626',
        light: '#ef4444',
        dark: '#b91c1c',
      },
      background: {
        default: '#fefefe',
        paper: '#ffffff',
        elevated: '#f9fafb',
      },
      text: {
        primary: '#111827',
        secondary: '#4b5563',
        disabled: '#9ca3af',
      },
      success: '#059669',
      warning: '#d97706',
      error: '#dc2626',
      info: '#0284c7',
      gradient: {
        primary: 'linear-gradient(135deg, #b45309 0%, #d97706 100%)',
        secondary: 'linear-gradient(135deg, #1f2937 0%, #374151 100%)',
      },
    },
  },
];

export const getDefaultPalette = (): ColorPalette => colorPalettes[0];

export const getPaletteByName = (name: string): ColorPalette | undefined => {
  return colorPalettes.find(palette => palette.name === name);
};

export const getPalettesByType = (type: ColorPalette['type']): ColorPalette[] => {
  return colorPalettes.filter(palette => palette.type === type);
};
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { createTheme, ThemeProvider as MuiThemeProvider, Theme } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { colorPalettes, ColorPalette, getDefaultPalette } from './colorPalettes';

interface ThemeContextType {
  currentPalette: ColorPalette;
  setPalette: (palette: ColorPalette) => void;
  setPaletteByName: (name: string) => void;
  availablePalettes: ColorPalette[];
  isDarkMode: boolean;
  toggleDarkMode: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

interface ThemeProviderProps {
  children: ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
  const [currentPalette, setCurrentPalette] = useState<ColorPalette>(getDefaultPalette());
  const [isDarkMode, setIsDarkMode] = useState(false);

  // Load saved theme preferences
  useEffect(() => {
    const savedPalette = localStorage.getItem('bank-theme-palette');
    const savedDarkMode = localStorage.getItem('bank-theme-dark-mode') === 'true';
    
    if (savedPalette) {
      const palette = colorPalettes.find(p => p.name === savedPalette);
      if (palette) {
        setCurrentPalette(palette);
      }
    }
    
    setIsDarkMode(savedDarkMode);
  }, []);

  // Save theme preferences
  useEffect(() => {
    localStorage.setItem('bank-theme-palette', currentPalette.name);
  }, [currentPalette]);

  useEffect(() => {
    localStorage.setItem('bank-theme-dark-mode', isDarkMode.toString());
  }, [isDarkMode]);

  const setPalette = (palette: ColorPalette) => {
    setCurrentPalette(palette);
  };

  const setPaletteByName = (name: string) => {
    const palette = colorPalettes.find(p => p.name === name);
    if (palette) {
      setCurrentPalette(palette);
    }
  };

  const toggleDarkMode = () => {
    setIsDarkMode(!isDarkMode);
  };

  const createCustomTheme = (palette: ColorPalette, darkMode: boolean): Theme => {
    return createTheme({
      palette: {
        mode: darkMode ? 'dark' : 'light',
        primary: {
          main: palette.colors.primary.main,
          light: palette.colors.primary.light,
          dark: palette.colors.primary.dark,
          contrastText: palette.colors.primary.contrastText,
        },
        secondary: {
          main: palette.colors.secondary.main,
          light: palette.colors.secondary.light,
          dark: palette.colors.secondary.dark,
          contrastText: palette.colors.secondary.contrastText,
        },
        background: {
          default: darkMode ? '#121212' : palette.colors.background.default,
          paper: darkMode ? '#1e1e1e' : palette.colors.background.paper,
        },
        text: {
          primary: darkMode ? '#ffffff' : palette.colors.text.primary,
          secondary: darkMode ? '#b0b0b0' : palette.colors.text.secondary,
          disabled: darkMode ? '#666666' : palette.colors.text.disabled,
        },
        success: {
          main: palette.colors.success,
        },
        warning: {
          main: palette.colors.warning,
        },
        error: {
          main: palette.colors.error,
        },
        info: {
          main: palette.colors.info,
        },
      },
      typography: {
        fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
        h1: {
          fontWeight: 700,
          fontSize: '2.5rem',
          lineHeight: 1.2,
        },
        h2: {
          fontWeight: 600,
          fontSize: '2rem',
          lineHeight: 1.3,
        },
        h3: {
          fontWeight: 600,
          fontSize: '1.75rem',
          lineHeight: 1.3,
        },
        h4: {
          fontWeight: 600,
          fontSize: '1.5rem',
          lineHeight: 1.4,
        },
        h5: {
          fontWeight: 600,
          fontSize: '1.25rem',
          lineHeight: 1.4,
        },
        h6: {
          fontWeight: 600,
          fontSize: '1.125rem',
          lineHeight: 1.4,
        },
        body1: {
          fontSize: '1rem',
          lineHeight: 1.6,
        },
        body2: {
          fontSize: '0.875rem',
          lineHeight: 1.6,
        },
        button: {
          fontWeight: 500,
          textTransform: 'none',
        },
      },
      shape: {
        borderRadius: 12,
      },
      components: {
        MuiButton: {
          styleOverrides: {
            root: {
              borderRadius: 8,
              padding: '10px 24px',
              fontWeight: 500,
              textTransform: 'none',
              boxShadow: 'none',
              '&:hover': {
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              },
            },
            contained: {
              background: palette.colors.gradient.primary,
              '&:hover': {
                background: palette.colors.primary.dark,
              },
            },
            outlined: {
              borderColor: palette.colors.primary.main,
              color: palette.colors.primary.main,
              '&:hover': {
                backgroundColor: `${palette.colors.primary.main}08`,
                borderColor: palette.colors.primary.dark,
              },
            },
          },
        },
        MuiCard: {
          styleOverrides: {
            root: {
              borderRadius: 16,
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
              border: `1px solid ${darkMode ? '#333' : '#e5e7eb'}`,
              '&:hover': {
                boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
              },
            },
          },
        },
        MuiPaper: {
          styleOverrides: {
            root: {
              borderRadius: 12,
            },
          },
        },
        MuiChip: {
          styleOverrides: {
            root: {
              borderRadius: 8,
              fontWeight: 500,
            },
          },
        },
        MuiTextField: {
          styleOverrides: {
            root: {
              '& .MuiOutlinedInput-root': {
                borderRadius: 8,
                '&:hover .MuiOutlinedInput-notchedOutline': {
                  borderColor: palette.colors.primary.main,
                },
                '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
                  borderColor: palette.colors.primary.main,
                  borderWidth: 2,
                },
              },
            },
          },
        },
        MuiTabs: {
          styleOverrides: {
            root: {
              '& .MuiTab-root': {
                textTransform: 'none',
                fontWeight: 500,
                minHeight: 48,
              },
              '& .Mui-selected': {
                color: palette.colors.primary.main,
              },
            },
            indicator: {
              backgroundColor: palette.colors.primary.main,
              height: 3,
              borderRadius: '3px 3px 0 0',
            },
          },
        },
        MuiAlert: {
          styleOverrides: {
            root: {
              borderRadius: 8,
            },
          },
        },
        MuiDialog: {
          styleOverrides: {
            paper: {
              borderRadius: 16,
              boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
            },
          },
        },
        MuiTooltip: {
          styleOverrides: {
            tooltip: {
              backgroundColor: darkMode ? '#2d2d2d' : '#1f2937',
              color: '#ffffff',
              fontSize: '0.875rem',
              borderRadius: 8,
              padding: '8px 12px',
            },
          },
        },
      },
    });
  };

  const theme = createCustomTheme(currentPalette, isDarkMode);

  const contextValue: ThemeContextType = {
    currentPalette,
    setPalette,
    setPaletteByName,
    availablePalettes: colorPalettes,
    isDarkMode,
    toggleDarkMode,
  };

  return (
    <ThemeContext.Provider value={contextValue}>
      <MuiThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </MuiThemeProvider>
    </ThemeContext.Provider>
  );
};
import React from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActionArea,
  Chip,
  Tooltip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  Palette as PaletteIcon,
  Brightness4 as DarkModeIcon,
  Brightness7 as LightModeIcon,
  Check as CheckIcon,
} from '@mui/icons-material';
import { useTheme } from '../../theme/themeProvider';
import { ColorPalette } from '../../theme/colorPalettes';

interface ThemeSelectorProps {
  open: boolean;
  onClose: () => void;
}

const ThemeSelector: React.FC<ThemeSelectorProps> = ({ open, onClose }) => {
  const { 
    currentPalette, 
    setPalette, 
    availablePalettes, 
    isDarkMode, 
    toggleDarkMode 
  } = useTheme();

  const handlePaletteSelect = (palette: ColorPalette) => {
    setPalette(palette);
  };

  const getTypeColor = (type: ColorPalette['type']) => {
    switch (type) {
      case 'corporate': return '#3b82f6';
      case 'modern': return '#10b981';
      case 'classic': return '#1e293b';
      case 'tech': return '#8b5cf6';
      case 'luxury': return '#d97706';
      default: return '#6b7280';
    }
  };

  return (
    <Dialog 
      open={open} 
      onClose={onClose} 
      maxWidth="md" 
      fullWidth
      PaperProps={{
        sx: { borderRadius: 3 }
      }}
    >
      <DialogTitle sx={{ pb: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <PaletteIcon color="primary" />
          <Typography variant="h6" fontWeight={600}>
            Theme Customization
          </Typography>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
            Color Palette
          </Typography>
          <Grid container spacing={2}>
            {availablePalettes.map((palette) => (
              <Grid item xs={12} sm={6} md={4} key={palette.name}>
                <Card
                  sx={{
                    position: 'relative',
                    cursor: 'pointer',
                    border: currentPalette.name === palette.name ? 2 : 1,
                    borderColor: currentPalette.name === palette.name 
                      ? 'primary.main' 
                      : 'divider',
                    transition: 'all 0.2s',
                    '&:hover': {
                      transform: 'translateY(-2px)',
                      boxShadow: 4,
                    },
                  }}
                >
                  <CardActionArea onClick={() => handlePaletteSelect(palette)}>
                    <CardContent sx={{ p: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <Box
                          sx={{
                            width: 20,
                            height: 20,
                            borderRadius: '50%',
                            background: palette.colors.gradient.primary,
                          }}
                        />
                        <Typography variant="subtitle2" fontWeight={600}>
                          {palette.name}
                        </Typography>
                        {currentPalette.name === palette.name && (
                          <CheckIcon color="primary" fontSize="small" />
                        )}
                      </Box>
                      
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {palette.description}
                      </Typography>
                      
                      <Box sx={{ display: 'flex', gap: 0.5, mb: 1 }}>
                        <Chip
                          label={palette.type}
                          size="small"
                          sx={{
                            backgroundColor: getTypeColor(palette.type),
                            color: 'white',
                            fontSize: '0.75rem',
                            height: 20,
                          }}
                        />
                      </Box>
                      
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        <Box
                          sx={{
                            width: 16,
                            height: 16,
                            borderRadius: 1,
                            backgroundColor: palette.colors.primary.main,
                          }}
                        />
                        <Box
                          sx={{
                            width: 16,
                            height: 16,
                            borderRadius: 1,
                            backgroundColor: palette.colors.secondary.main,
                          }}
                        />
                        <Box
                          sx={{
                            width: 16,
                            height: 16,
                            borderRadius: 1,
                            backgroundColor: palette.colors.accent.main,
                          }}
                        />
                        <Box
                          sx={{
                            width: 16,
                            height: 16,
                            borderRadius: 1,
                            backgroundColor: palette.colors.success,
                          }}
                        />
                      </Box>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>

        <Box sx={{ borderTop: 1, borderColor: 'divider', pt: 2 }}>
          <FormControlLabel
            control={
              <Switch
                checked={isDarkMode}
                onChange={toggleDarkMode}
                icon={<LightModeIcon />}
                checkedIcon={<DarkModeIcon />}
              />
            }
            label={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                {isDarkMode ? <DarkModeIcon /> : <LightModeIcon />}
                <Typography variant="body1">
                  {isDarkMode ? 'Dark Mode' : 'Light Mode'}
                </Typography>
              </Box>
            }
          />
        </Box>
      </DialogContent>
      
      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} variant="outlined">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ThemeSelector;
import React, { useState } from 'react';
import {
  Box,
  Drawer,
  Typography,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemButton,
  IconButton,
  TextField,
  InputAdornment,
  Divider,
  Chip,
  Card,
  CardContent,
  Link,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  Close as CloseIcon,
  Search as SearchIcon,
  Help as HelpIcon,
  Book as BookIcon,
  VideoLibrary as VideoIcon,
  Code as CodeIcon,
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Payment as PaymentIcon,
  Business as BusinessIcon,
  ExpandMore as ExpandMoreIcon,
  Launch as LaunchIcon,
} from '@mui/icons-material';

interface HelpArticle {
  id: string;
  title: string;
  category: string;
  content: string;
  tags: string[];
  type: 'guide' | 'tutorial' | 'reference' | 'troubleshooting';
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  estimatedTime: number; // in minutes
  lastUpdated: Date;
}

interface HelpSystemProps {
  open: boolean;
  onClose: () => void;
}

const HelpSystem: React.FC<HelpSystemProps> = ({ open, onClose }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedArticle, setSelectedArticle] = useState<HelpArticle | null>(null);

  const helpArticles: HelpArticle[] = [
    {
      id: 'tenant-setup-guide',
      title: 'Complete Tenant Setup Guide',
      category: 'setup',
      content: 'Step-by-step guide to setting up a new tenant from scratch...',
      tags: ['tenant', 'setup', 'configuration', 'beginner'],
      type: 'guide',
      difficulty: 'beginner',
      estimatedTime: 30,
      lastUpdated: new Date(),
    },
    {
      id: 'tenant-cloning-tutorial',
      title: 'How to Clone a Tenant',
      category: 'management',
      content: 'Learn how to clone an existing tenant configuration...',
      tags: ['tenant', 'cloning', 'migration', 'intermediate'],
      type: 'tutorial',
      difficulty: 'intermediate',
      estimatedTime: 15,
      lastUpdated: new Date(),
    },
    {
      id: 'security-configuration',
      title: 'Security Configuration Best Practices',
      category: 'security',
      content: 'Best practices for configuring tenant security settings...',
      tags: ['security', 'authentication', 'authorization', 'advanced'],
      type: 'guide',
      difficulty: 'advanced',
      estimatedTime: 45,
      lastUpdated: new Date(),
    },
    {
      id: 'payment-processing-setup',
      title: 'Payment Processing Configuration',
      category: 'payment',
      content: 'Configure payment processing settings for your tenant...',
      tags: ['payment', 'processing', 'configuration', 'intermediate'],
      type: 'tutorial',
      difficulty: 'intermediate',
      estimatedTime: 25,
      lastUpdated: new Date(),
    },
    {
      id: 'troubleshooting-common-issues',
      title: 'Troubleshooting Common Issues',
      category: 'troubleshooting',
      content: 'Common issues and their solutions...',
      tags: ['troubleshooting', 'issues', 'solutions', 'beginner'],
      type: 'troubleshooting',
      difficulty: 'beginner',
      estimatedTime: 20,
      lastUpdated: new Date(),
    },
  ];

  const categories = [
    { id: 'all', label: 'All Topics', icon: <HelpIcon /> },
    { id: 'setup', label: 'Setup & Configuration', icon: <SettingsIcon /> },
    { id: 'management', label: 'Tenant Management', icon: <BusinessIcon /> },
    { id: 'security', label: 'Security', icon: <SecurityIcon /> },
    { id: 'payment', label: 'Payment Processing', icon: <PaymentIcon /> },
    { id: 'troubleshooting', label: 'Troubleshooting', icon: <HelpIcon /> },
  ];

  const filteredArticles = helpArticles.filter(article => {
    const matchesSearch = article.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         article.content.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         article.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesCategory = selectedCategory === 'all' || article.category === selectedCategory;
    return matchesSearch && matchesCategory;
  });

  const getDifficultyColor = (difficulty: HelpArticle['difficulty']) => {
    switch (difficulty) {
      case 'beginner': return 'success';
      case 'intermediate': return 'warning';
      case 'advanced': return 'error';
      default: return 'default';
    }
  };

  const getTypeIcon = (type: HelpArticle['type']) => {
    switch (type) {
      case 'guide': return <BookIcon />;
      case 'tutorial': return <VideoIcon />;
      case 'reference': return <CodeIcon />;
      case 'troubleshooting': return <HelpIcon />;
      default: return <BookIcon />;
    }
  };

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDrawer-paper': {
          width: 400,
          maxWidth: '90vw',
        },
      }}
    >
      <Box sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h6" fontWeight={600}>
            Help & Documentation
          </Typography>
          <IconButton onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Box>

        {/* Search */}
        <TextField
          fullWidth
          placeholder="Search help articles..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
          sx={{ mb: 2 }}
        />

        {!selectedArticle ? (
          <>
            {/* Categories */}
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" fontWeight={600} sx={{ mb: 1 }}>
                Categories
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                {categories.map((category) => (
                  <Chip
                    key={category.id}
                    label={category.label}
                    icon={category.icon}
                    variant={selectedCategory === category.id ? 'filled' : 'outlined'}
                    onClick={() => setSelectedCategory(category.id)}
                    size="small"
                  />
                ))}
              </Box>
            </Box>

            <Divider sx={{ mb: 2 }} />

            {/* Articles List */}
            <Box sx={{ flex: 1, overflow: 'auto' }}>
              <Typography variant="subtitle2" fontWeight={600} sx={{ mb: 1 }}>
                Articles ({filteredArticles.length})
              </Typography>
              <List>
                {filteredArticles.map((article) => (
                  <ListItem key={article.id} disablePadding>
                    <ListItemButton onClick={() => setSelectedArticle(article)}>
                      <ListItemIcon>
                        {getTypeIcon(article.type)}
                      </ListItemIcon>
                      <ListItemText
                        primary={article.title}
                        secondary={
                          <Box>
                            <Typography variant="caption" color="text.secondary">
                              {article.content.substring(0, 100)}...
                            </Typography>
                            <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                              <Chip
                                label={article.difficulty}
                                size="small"
                                color={getDifficultyColor(article.difficulty)}
                                variant="outlined"
                              />
                              <Chip
                                label={`${article.estimatedTime}m`}
                                size="small"
                                variant="outlined"
                              />
                            </Box>
                          </Box>
                        }
                      />
                    </ListItemButton>
                  </ListItem>
                ))}
              </List>
            </Box>
          </>
        ) : (
          /* Article Detail */
          <Box sx={{ flex: 1, overflow: 'auto' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <IconButton onClick={() => setSelectedArticle(null)}>
                <CloseIcon />
              </IconButton>
              <Typography variant="h6" fontWeight={600}>
                {selectedArticle.title}
              </Typography>
            </Box>

            <Card sx={{ mb: 2 }}>
              <CardContent>
                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <Chip
                    icon={getTypeIcon(selectedArticle.type)}
                    label={selectedArticle.type}
                    size="small"
                    variant="outlined"
                  />
                  <Chip
                    label={selectedArticle.difficulty}
                    size="small"
                    color={getDifficultyColor(selectedArticle.difficulty)}
                    variant="outlined"
                  />
                  <Chip
                    label={`${selectedArticle.estimatedTime} min read`}
                    size="small"
                    variant="outlined"
                  />
                </Box>
                
                <Typography variant="body1" sx={{ mb: 2 }}>
                  {selectedArticle.content}
                </Typography>

                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" fontWeight={600} sx={{ mb: 1 }}>
                    Tags
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                    {selectedArticle.tags.map((tag) => (
                      <Chip key={tag} label={tag} size="small" variant="outlined" />
                    ))}
                  </Box>
                </Box>

                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Link href="#" variant="body2" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <LaunchIcon fontSize="small" />
                    View Full Documentation
                  </Link>
                </Box>
              </CardContent>
            </Card>

            {/* Related Articles */}
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="subtitle2" fontWeight={600}>
                  Related Articles
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <List>
                  {helpArticles
                    .filter(article => 
                      article.id !== selectedArticle.id && 
                      article.category === selectedArticle.category
                    )
                    .slice(0, 3)
                    .map((article) => (
                      <ListItem key={article.id} disablePadding>
                        <ListItemButton onClick={() => setSelectedArticle(article)}>
                          <ListItemText
                            primary={article.title}
                            secondary={`${article.estimatedTime} min read`}
                          />
                        </ListItemButton>
                      </ListItem>
                    ))}
                </List>
              </AccordionDetails>
            </Accordion>
          </Box>
        )}
      </Box>
    </Drawer>
  );
};

export default HelpSystem;
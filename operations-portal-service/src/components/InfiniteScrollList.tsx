/**
 * Infinite scroll list component for performance optimization
 */

import React, { useCallback, useMemo } from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';
import { useInfiniteScroll, useLazyLoading } from '../hooks/useVirtualization';

interface InfiniteScrollListProps<T> {
  items: T[];
  renderItem: (item: T, index: number) => React.ReactNode;
  batchSize?: number;
  threshold?: number;
  loadingText?: string;
  endText?: string;
  className?: string;
  style?: React.CSSProperties;
}

const InfiniteScrollList = <T,>({
  items,
  renderItem,
  batchSize = 20,
  threshold = 100,
  loadingText = 'Loading more items...',
  endText = 'No more items to load',
  className,
  style
}: InfiniteScrollListProps<T>) => {
  const {
    loadedItems,
    isLoading,
    hasMore,
    triggerRef
  } = useLazyLoading(items, batchSize, threshold);

  const handleLoadMore = useCallback(() => {
    // This is handled by the useLazyLoading hook
  }, []);

  const { triggerRef: infiniteScrollRef } = useInfiniteScroll(
    handleLoadMore,
    hasMore,
    threshold
  );

  const isEndReached = useMemo(() => 
    loadedItems.length >= items.length, 
    [loadedItems.length, items.length]
  );

  return (
    <Box className={className} style={style}>
      {loadedItems.map((item, index) => (
        <Box key={index}>
          {renderItem(item, index)}
        </Box>
      ))}
      
      {isLoading && (
        <Box 
          display="flex" 
          justifyContent="center" 
          alignItems="center" 
          py={2}
        >
          <CircularProgress size={24} />
          <Typography variant="body2" sx={{ ml: 2 }}>
            {loadingText}
          </Typography>
        </Box>
      )}
      
      {isEndReached && !isLoading && (
        <Box 
          display="flex" 
          justifyContent="center" 
          alignItems="center" 
          py={2}
        >
          <Typography variant="body2" color="text.secondary">
            {endText}
          </Typography>
        </Box>
      )}
      
      <Box ref={triggerRef} />
    </Box>
  );
};

export default InfiniteScrollList;

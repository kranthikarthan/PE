/**
 * Virtualized list component for performance optimization
 */

import React, { forwardRef, useImperativeHandle, useRef } from 'react';
import { Box, List, ListItem, ListItemProps } from '@mui/material';
import { useVirtualization } from '../hooks/useVirtualization';

interface VirtualizedListProps<T> {
  items: T[];
  itemHeight: number;
  containerHeight: number;
  renderItem: (item: T, index: number) => React.ReactNode;
  overscan?: number;
  onScroll?: (scrollTop: number) => void;
  className?: string;
  style?: React.CSSProperties;
}

export interface VirtualizedListRef {
  scrollToIndex: (index: number) => void;
  scrollToTop: () => void;
  scrollToBottom: () => void;
}

const VirtualizedList = forwardRef<VirtualizedListRef, VirtualizedListProps<any>>(
  ({ 
    items, 
    itemHeight, 
    containerHeight, 
    renderItem, 
    overscan = 5,
    onScroll,
    className,
    style 
  }, ref) => {
    const containerRef = useRef<HTMLDivElement>(null);
    
    const {
      visibleItems,
      startIndex,
      endIndex,
      totalHeight,
      offsetY
    } = useVirtualization(items, {
      itemHeight,
      containerHeight,
      overscan
    });

    useImperativeHandle(ref, () => ({
      scrollToIndex: (index: number) => {
        if (containerRef.current) {
          const scrollTop = index * itemHeight;
          containerRef.current.scrollTop = scrollTop;
        }
      },
      scrollToTop: () => {
        if (containerRef.current) {
          containerRef.current.scrollTop = 0;
        }
      },
      scrollToBottom: () => {
        if (containerRef.current) {
          containerRef.current.scrollTop = totalHeight;
        }
      }
    }));

    const handleScroll = (event: React.UIEvent<HTMLDivElement>) => {
      const scrollTop = event.currentTarget.scrollTop;
      onScroll?.(scrollTop);
    };

    return (
      <Box
        ref={containerRef}
        className={className}
        style={{
          height: containerHeight,
          overflow: 'auto',
          ...style
        }}
        onScroll={handleScroll}
      >
        <Box
          style={{
            height: totalHeight,
            position: 'relative'
          }}
        >
          <Box
            style={{
              transform: `translateY(${offsetY}px)`,
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0
            }}
          >
            {visibleItems.map((item, index) => (
              <Box
                key={startIndex + index}
                style={{
                  height: itemHeight,
                  display: 'flex',
                  alignItems: 'center'
                }}
              >
                {renderItem(item, startIndex + index)}
              </Box>
            ))}
          </Box>
        </Box>
      </Box>
    );
  }
);

VirtualizedList.displayName = 'VirtualizedList';

export default VirtualizedList;

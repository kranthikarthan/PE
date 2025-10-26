/**
 * Tests for InfiniteScrollList component
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import InfiniteScrollList from '../InfiniteScrollList';

describe('InfiniteScrollList', () => {
  const mockItems = Array.from({ length: 100 }, (_, i) => ({
    id: i,
    name: `Item ${i}`
  }));

  const mockRenderItem = (item: any, index: number) => (
    <div key={item.id} data-testid={`item-${index}`}>
      {item.name}
    </div>
  );

  it('should render initial batch of items', () => {
    render(
      <InfiniteScrollList
        items={mockItems}
        renderItem={mockRenderItem}
        batchSize={20}
      />
    );

    // Should render initial batch (20 items)
    expect(screen.getAllByTestId(/item-/)).toHaveLength(20);
  });

  it('should show loading state', async () => {
    render(
      <InfiniteScrollList
        items={mockItems}
        renderItem={mockRenderItem}
        batchSize={20}
        loadingText="Loading more items..."
      />
    );

    // Wait for loading state
    await waitFor(() => {
      expect(screen.getByText('Loading more items...')).toBeInTheDocument();
    });
  });

  it('should show end state when all items loaded', async () => {
    render(
      <InfiniteScrollList
        items={mockItems}
        renderItem={mockRenderItem}
        batchSize={20}
        endText="No more items to load"
      />
    );

    // Wait for end state
    await waitFor(() => {
      expect(screen.getByText('No more items to load')).toBeInTheDocument();
    });
  });

  it('should handle custom batch size', () => {
    render(
      <InfiniteScrollList
        items={mockItems}
        renderItem={mockRenderItem}
        batchSize={10}
      />
    );

    // Should render initial batch (10 items)
    expect(screen.getAllByTestId(/item-/)).toHaveLength(10);
  });

  it('should handle custom threshold', () => {
    render(
      <InfiniteScrollList
        items={mockItems}
        renderItem={mockRenderItem}
        batchSize={20}
        threshold={50}
      />
    );

    // Should render initial batch
    expect(screen.getAllByTestId(/item-/)).toHaveLength(20);
  });

  it('should apply custom className and style', () => {
    const { container } = render(
      <InfiniteScrollList
        items={mockItems}
        renderItem={mockRenderItem}
        className="custom-class"
        style={{ backgroundColor: 'red' }}
      />
    );

    const listContainer = container.firstChild as HTMLElement;
    expect(listContainer).toHaveClass('custom-class');
    expect(listContainer).toHaveStyle('background-color: red');
  });

  it('should handle empty items array', () => {
    render(
      <InfiniteScrollList
        items={[]}
        renderItem={mockRenderItem}
        batchSize={20}
      />
    );

    // Should not render any items
    expect(screen.queryAllByTestId(/item-/)).toHaveLength(0);
  });

  it('should handle single item', () => {
    const singleItem = [{ id: 1, name: 'Single Item' }];
    
    render(
      <InfiniteScrollList
        items={singleItem}
        renderItem={mockRenderItem}
        batchSize={20}
      />
    );

    // Should render single item
    expect(screen.getByTestId('item-0')).toBeInTheDocument();
    expect(screen.getByText('Single Item')).toBeInTheDocument();
  });
});

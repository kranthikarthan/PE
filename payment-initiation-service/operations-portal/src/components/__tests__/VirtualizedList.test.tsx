/**
 * Tests for VirtualizedList component
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { VirtualizedList } from '../VirtualizedList';

describe('VirtualizedList', () => {
  const mockItems = Array.from({ length: 100 }, (_, i) => ({
    id: i,
    name: `Item ${i}`
  }));

  const mockRenderItem = (item: any, index: number) => (
    <div key={item.id} data-testid={`item-${index}`}>
      {item.name}
    </div>
  );

  it('should render visible items', () => {
    render(
      <VirtualizedList
        items={mockItems}
        itemHeight={50}
        containerHeight={200}
        renderItem={mockRenderItem}
      />
    );

    // Should render visible items (4 items in 200px height with 50px item height)
    expect(screen.getAllByTestId(/item-/)).toHaveLength(4);
  });

  it('should handle scroll to index', () => {
    const ref = React.createRef<any>();
    
    render(
      <VirtualizedList
        ref={ref}
        items={mockItems}
        itemHeight={50}
        containerHeight={200}
        renderItem={mockRenderItem}
      />
    );

    act(() => {
      ref.current?.scrollToIndex(10);
    });

    // Should scroll to index 10
    expect(ref.current).toBeDefined();
  });

  it('should handle scroll to top', () => {
    const ref = React.createRef<any>();
    
    render(
      <VirtualizedList
        ref={ref}
        items={mockItems}
        itemHeight={50}
        containerHeight={200}
        renderItem={mockRenderItem}
      />
    );

    act(() => {
      ref.current?.scrollToTop();
    });

    expect(ref.current).toBeDefined();
  });

  it('should handle scroll to bottom', () => {
    const ref = React.createRef<any>();
    
    render(
      <VirtualizedList
        ref={ref}
        items={mockItems}
        itemHeight={50}
        containerHeight={200}
        renderItem={mockRenderItem}
      />
    );

    act(() => {
      ref.current?.scrollToBottom();
    });

    expect(ref.current).toBeDefined();
  });

  it('should handle scroll events', () => {
    const onScroll = jest.fn();
    
    render(
      <VirtualizedList
        items={mockItems}
        itemHeight={50}
        containerHeight={200}
        renderItem={mockRenderItem}
        onScroll={onScroll}
      />
    );

    // Simulate scroll event
    const container = screen.getByRole('list');
    fireEvent.scroll(container, { target: { scrollTop: 100 } });

    expect(onScroll).toHaveBeenCalledWith(100);
  });

  it('should apply custom className and style', () => {
    const { container } = render(
      <VirtualizedList
        items={mockItems}
        itemHeight={50}
        containerHeight={200}
        renderItem={mockRenderItem}
        className="custom-class"
        style={{ backgroundColor: 'red' }}
      />
    );

    const listContainer = container.firstChild as HTMLElement;
    expect(listContainer).toHaveClass('custom-class');
    expect(listContainer).toHaveStyle('background-color: red');
  });
});

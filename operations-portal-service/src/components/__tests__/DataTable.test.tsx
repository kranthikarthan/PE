/**
 * DataTable Component Tests
 * 
 * Component tests for the DataTable component.
 * Tests rendering, sorting, filtering, pagination, and user interactions.
 */

import React from 'react';
import { screen, fireEvent, waitFor } from '@testing-library/react';
import { render } from '../../test-utils';
import DataTable from '../DataTable';
import { fixtures } from '../../test-utils/fixtures';

describe('DataTable', () => {
  const mockColumns = [
    {
      id: 'name',
      label: 'Name',
      sortable: true,
    },
    {
      id: 'status',
      label: 'Status',
      sortable: true,
      render: (value: string) => <span data-testid={`status-${value}`}>{value}</span>,
    },
    {
      id: 'amount',
      label: 'Amount',
      sortable: true,
      align: 'right' as const,
    },
  ];

  const mockData = [
    { id: '1', name: 'Service 1', status: 'UP', amount: 1000 },
    { id: '2', name: 'Service 2', status: 'DOWN', amount: 2000 },
    { id: '3', name: 'Service 3', status: 'UP', amount: 1500 },
  ];

  const defaultProps = {
    columns: mockColumns,
    data: mockData,
  };

  describe('Basic Rendering', () => {
    it('should render table with data', () => {
      render(<DataTable {...defaultProps} />);

      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Status')).toBeInTheDocument();
      expect(screen.getByText('Amount')).toBeInTheDocument();
      expect(screen.getByText('Service 1')).toBeInTheDocument();
      expect(screen.getByText('Service 2')).toBeInTheDocument();
      expect(screen.getByText('Service 3')).toBeInTheDocument();
    });

    it('should render empty state when no data', () => {
      render(<DataTable {...defaultProps} data={[]} />);

      expect(screen.getByText('No data available')).toBeInTheDocument();
    });

    it('should render custom empty message', () => {
      render(<DataTable {...defaultProps} data={[]} emptyMessage="No services found" />);

      expect(screen.getByText('No services found')).toBeInTheDocument();
    });
  });

  describe('Loading State', () => {
    it('should show loading skeletons when loading', () => {
      render(<DataTable {...defaultProps} loading={true} />);

      expect(screen.getAllByTestId('skeleton')).toHaveLength(15); // 3 columns * 5 rows
    });
  });

  describe('Error State', () => {
    it('should show error message when error occurs', () => {
      render(<DataTable {...defaultProps} error="Failed to load data" />);

      expect(screen.getByText('Failed to load data')).toBeInTheDocument();
    });
  });

  describe('Sorting', () => {
    it('should render sortable column headers', () => {
      render(<DataTable {...defaultProps} />);

      const nameHeader = screen.getByText('Name');
      expect(nameHeader).toBeInTheDocument();
    });

    it('should call onSort when sortable header is clicked', () => {
      const mockOnSort = jest.fn();
      render(<DataTable {...defaultProps} onSort={mockOnSort} />);

      const nameHeader = screen.getByText('Name');
      fireEvent.click(nameHeader);

      expect(mockOnSort).toHaveBeenCalledWith('name', 'asc');
    });

    it('should toggle sort direction on subsequent clicks', () => {
      const mockOnSort = jest.fn();
      render(<DataTable {...defaultProps} onSort={mockOnSort} />);

      const nameHeader = screen.getByText('Name');
      fireEvent.click(nameHeader);
      fireEvent.click(nameHeader);

      expect(mockOnSort).toHaveBeenCalledWith('name', 'desc');
    });

    it('should not call onSort for non-sortable columns', () => {
      const mockOnSort = jest.fn();
      const nonSortableColumns = [
        { id: 'name', label: 'Name', sortable: false },
        { id: 'status', label: 'Status', sortable: true },
      ];

      render(<DataTable {...defaultProps} columns={nonSortableColumns} onSort={mockOnSort} />);

      const nameHeader = screen.getByText('Name');
      fireEvent.click(nameHeader);

      expect(mockOnSort).not.toHaveBeenCalled();
    });
  });

  describe('Selection', () => {
    it('should render checkboxes when selectable is true', () => {
      render(<DataTable {...defaultProps} selectable={true} />);

      const checkboxes = screen.getAllByRole('checkbox');
      expect(checkboxes).toHaveLength(4); // 1 header + 3 rows
    });

    it('should not render checkboxes when selectable is false', () => {
      render(<DataTable {...defaultProps} selectable={false} />);

      const checkboxes = screen.queryAllByRole('checkbox');
      expect(checkboxes).toHaveLength(0);
    });

    it('should select all rows when header checkbox is clicked', () => {
      const mockOnSelectionChange = jest.fn();
      render(
        <DataTable
          {...defaultProps}
          selectable={true}
          onSelectionChange={mockOnSelectionChange}
        />
      );

      const headerCheckbox = screen.getAllByRole('checkbox')[0];
      fireEvent.click(headerCheckbox);

      expect(mockOnSelectionChange).toHaveBeenCalledWith(mockData);
    });

    it('should deselect all rows when header checkbox is clicked again', () => {
      const mockOnSelectionChange = jest.fn();
      render(
        <DataTable
          {...defaultProps}
          selectable={true}
          onSelectionChange={mockOnSelectionChange}
        />
      );

      const headerCheckbox = screen.getAllByRole('checkbox')[0];
      fireEvent.click(headerCheckbox);
      fireEvent.click(headerCheckbox);

      expect(mockOnSelectionChange).toHaveBeenCalledWith([]);
    });

    it('should select individual row when row checkbox is clicked', () => {
      const mockOnSelectionChange = jest.fn();
      render(
        <DataTable
          {...defaultProps}
          selectable={true}
          onSelectionChange={mockOnSelectionChange}
        />
      );

      const rowCheckbox = screen.getAllByRole('checkbox')[1];
      fireEvent.click(rowCheckbox);

      expect(mockOnSelectionChange).toHaveBeenCalledWith([mockData[0]]);
    });
  });

  describe('Pagination', () => {
    it('should render pagination when pagination is true', () => {
      render(
        <DataTable
          {...defaultProps}
          pagination={true}
          totalCount={100}
          page={0}
          pageSize={10}
        />
      );

      expect(screen.getByText('Rows per page:')).toBeInTheDocument();
    });

    it('should not render pagination when pagination is false', () => {
      render(<DataTable {...defaultProps} pagination={false} />);

      expect(screen.queryByText('Rows per page:')).not.toBeInTheDocument();
    });

    it('should call onPageChange when page is changed', () => {
      const mockOnPageChange = jest.fn();
      render(
        <DataTable
          {...defaultProps}
          pagination={true}
          totalCount={100}
          page={0}
          pageSize={10}
          onPageChange={mockOnPageChange}
        />
      );

      const nextPageButton = screen.getByLabelText('Go to next page');
      fireEvent.click(nextPageButton);

      expect(mockOnPageChange).toHaveBeenCalledWith(1);
    });

    it('should call onPageSizeChange when page size is changed', () => {
      const mockOnPageSizeChange = jest.fn();
      render(
        <DataTable
          {...defaultProps}
          pagination={true}
          totalCount={100}
          page={0}
          pageSize={10}
          onPageSizeChange={mockOnPageSizeChange}
        />
      );

      const pageSizeSelect = screen.getByDisplayValue('10');
      fireEvent.change(pageSizeSelect, { target: { value: '25' } });

      expect(mockOnPageSizeChange).toHaveBeenCalledWith(25);
    });
  });

  describe('Search', () => {
    it('should render search input when searchable is true', () => {
      render(<DataTable {...defaultProps} searchable={true} />);

      expect(screen.getByPlaceholderText('Search...')).toBeInTheDocument();
    });

    it('should not render search input when searchable is false', () => {
      render(<DataTable {...defaultProps} searchable={false} />);

      expect(screen.queryByPlaceholderText('Search...')).not.toBeInTheDocument();
    });

    it('should call onSearch when search input changes', () => {
      const mockOnSearch = jest.fn();
      render(<DataTable {...defaultProps} searchable={true} onSearch={mockOnSearch} />);

      const searchInput = screen.getByPlaceholderText('Search...');
      fireEvent.change(searchInput, { target: { value: 'test' } });

      expect(mockOnSearch).toHaveBeenCalledWith('test');
    });
  });

  describe('Filters', () => {
    const mockFilters = [
      {
        field: 'status',
        label: 'Status',
        options: [
          { value: 'UP', label: 'Up' },
          { value: 'DOWN', label: 'Down' },
        ],
        value: '',
        onChange: jest.fn(),
      },
    ];

    it('should render filters when filterable is true', () => {
      render(<DataTable {...defaultProps} filterable={true} filters={mockFilters} />);

      expect(screen.getByText('Status')).toBeInTheDocument();
    });

    it('should not render filters when filterable is false', () => {
      render(<DataTable {...defaultProps} filterable={false} />);

      expect(screen.queryByText('Status')).not.toBeInTheDocument();
    });

    it('should call filter onChange when filter value changes', () => {
      render(<DataTable {...defaultProps} filterable={true} filters={mockFilters} />);

      const statusSelect = screen.getByDisplayValue('All');
      fireEvent.change(statusSelect, { target: { value: 'UP' } });

      expect(mockFilters[0].onChange).toHaveBeenCalledWith('UP');
    });
  });

  describe('Actions', () => {
    const mockActions = [
      {
        label: 'View',
        icon: <span>👁️</span>,
        onClick: jest.fn(),
      },
      {
        label: 'Edit',
        icon: <span>✏️</span>,
        onClick: jest.fn(),
      },
    ];

    it('should render action buttons when actions are provided', () => {
      render(<DataTable {...defaultProps} actions={mockActions} />);

      const actionButtons = screen.getAllByRole('button');
      expect(actionButtons.length).toBeGreaterThan(0);
    });

    it('should call action onClick when action is clicked', () => {
      render(<DataTable {...defaultProps} actions={mockActions} />);

      const viewAction = screen.getByText('View');
      fireEvent.click(viewAction);

      expect(mockActions[0].onClick).toHaveBeenCalledWith(mockData[0]);
    });

    it('should disable action when disabled function returns true', () => {
      const actionsWithDisabled = [
        {
          label: 'View',
          icon: <span>👁️</span>,
          onClick: jest.fn(),
          disabled: (row: any) => row.status === 'DOWN',
        },
      ];

      render(<DataTable {...defaultProps} actions={actionsWithDisabled} />);

      const viewAction = screen.getByText('View');
      expect(viewAction).toBeDisabled();
    });
  });

  describe('Bulk Actions', () => {
    const mockBulkActions = [
      {
        label: 'Delete Selected',
        icon: <span>🗑️</span>,
        onClick: jest.fn(),
      },
    ];

    it('should render bulk actions when rows are selected', () => {
      render(
        <DataTable
          {...defaultProps}
          selectable={true}
          bulkActions={mockBulkActions}
        />
      );

      // Select a row first
      const rowCheckbox = screen.getAllByRole('checkbox')[1];
      fireEvent.click(rowCheckbox);

      expect(screen.getByText('1 selected')).toBeInTheDocument();
      expect(screen.getByText('Delete Selected')).toBeInTheDocument();
    });

    it('should call bulk action onClick when bulk action is clicked', () => {
      render(
        <DataTable
          {...defaultProps}
          selectable={true}
          bulkActions={mockBulkActions}
        />
      );

      // Select a row first
      const rowCheckbox = screen.getAllByRole('checkbox')[1];
      fireEvent.click(rowCheckbox);

      const deleteAction = screen.getByText('Delete Selected');
      fireEvent.click(deleteAction);

      expect(mockBulkActions[0].onClick).toHaveBeenCalledWith([mockData[0]]);
    });
  });

  describe('Row Click', () => {
    it('should call onRowClick when row is clicked', () => {
      const mockOnRowClick = jest.fn();
      render(<DataTable {...defaultProps} onRowClick={mockOnRowClick} />);

      const firstRow = screen.getByText('Service 1');
      fireEvent.click(firstRow);

      expect(mockOnRowClick).toHaveBeenCalledWith(mockData[0]);
    });

    it('should not call onRowClick when row is not clickable', () => {
      const mockOnRowClick = jest.fn();
      render(<DataTable {...defaultProps} onRowClick={undefined} />);

      const firstRow = screen.getByText('Service 1');
      fireEvent.click(firstRow);

      expect(mockOnRowClick).not.toHaveBeenCalled();
    });
  });

  describe('Custom Rendering', () => {
    it('should render custom cell content using render function', () => {
      render(<DataTable {...defaultProps} />);

      expect(screen.getByTestId('status-UP')).toBeInTheDocument();
      expect(screen.getByTestId('status-DOWN')).toBeInTheDocument();
    });

    it('should align content correctly', () => {
      render(<DataTable {...defaultProps} />);

      const amountCells = screen.getAllByText('1000');
      expect(amountCells[0]).toHaveStyle('text-align: right');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', () => {
      render(<DataTable {...defaultProps} />);

      expect(screen.getByRole('table')).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'Name' })).toBeInTheDocument();
    });

    it('should have proper heading structure', () => {
      render(<DataTable {...defaultProps} />);

      expect(screen.getByRole('columnheader', { name: 'Name' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'Status' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'Amount' })).toBeInTheDocument();
    });
  });

  describe('Dense Mode', () => {
    it('should render in dense mode when dense is true', () => {
      render(<DataTable {...defaultProps} dense={true} />);

      const table = screen.getByRole('table');
      expect(table).toHaveClass('MuiTable-sizeSmall');
    });

    it('should render in normal mode when dense is false', () => {
      render(<DataTable {...defaultProps} dense={false} />);

      const table = screen.getByRole('table');
      expect(table).not.toHaveClass('MuiTable-sizeSmall');
    });
  });

  describe('Sticky Header', () => {
    it('should render sticky header when stickyHeader is true', () => {
      render(<DataTable {...defaultProps} stickyHeader={true} />);

      const tableContainer = screen.getByRole('table').closest('[class*="MuiTableContainer"]');
      expect(tableContainer).toHaveStyle('position: sticky');
    });

    it('should not render sticky header when stickyHeader is false', () => {
      render(<DataTable {...defaultProps} stickyHeader={false} />);

      const tableContainer = screen.getByRole('table').closest('[class*="MuiTableContainer"]');
      expect(tableContainer).toHaveStyle('position: static');
    });
  });
});

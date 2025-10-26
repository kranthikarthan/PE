import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders payments engine operations portal', () => {
  render(<App />);
  const titleElement = screen.getByText(/Payments Engine/i);
  expect(titleElement).toBeInTheDocument();
});

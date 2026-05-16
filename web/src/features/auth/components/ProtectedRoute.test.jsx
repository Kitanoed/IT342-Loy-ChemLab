import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import ProtectedRoute from './ProtectedRoute';

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({ user: { role: 'STUDENT' }, loading: false }),
}));

describe('ProtectedRoute', () => {
  it('renders children when authenticated', () => {
    render(
      <ProtectedRoute>
        <div>secure content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('secure content')).toBeInTheDocument();
  });
});
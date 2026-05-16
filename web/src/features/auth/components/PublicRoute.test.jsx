import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import PublicRoute from './PublicRoute';

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({ user: null, loading: false }),
}));

describe('PublicRoute', () => {
  it('renders public children when unauthenticated', () => {
    render(
      <PublicRoute>
        <div>public content</div>
      </PublicRoute>
    );

    expect(screen.getByText('public content')).toBeInTheDocument();
  });
});
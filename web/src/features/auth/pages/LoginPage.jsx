import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { GoogleLogin } from '@react-oauth/google';
import Orbs from '../../../components/Orbs';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    setError('');
    setLoading(true);
    try {
      await loginWithGoogle(credentialResponse.credential);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Google login failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleError = () => {
    setError('Google login failed. Please try again.');
  };

  return (
    <div className="page page-center">
      <Orbs />
      <div className="glass auth-card">
        <h2 className="auth-title">Sign In</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label className="form-label" htmlFor="email">Email</label>
            <input id="email" type="email" className="form-input" placeholder="you@example.com" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input id="password" type="password" className="form-input" placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>{loading ? <span className="spinner" /> : 'Sign In'}</button>
        </form>
        <div className="divider"><span>OR</span></div>
        <div className="google-login-container" style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.5rem' }}>
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={handleGoogleError}
            useOneTap
            shape="rectangular"
            theme="outline"
            text="signin_with"
            size="large"
          />
        </div>
        <p className="auth-footer">Don't have an account? <Link to="/register" className="auth-link">Register here</Link></p>
      </div>
    </div>
  );
};

export default LoginPage;
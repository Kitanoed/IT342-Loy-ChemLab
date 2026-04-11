import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Orbs from '../components/Orbs';

const Homepage = () => {
  const { user, logout } = useAuth();

  return (
    <div className="page page-center">
      <Orbs />

      <div className="glass home-card">
        <h1 className="home-logo">ChemLab</h1>
        <p className="home-tagline">Laboratory Inventory &amp; Request System</p>
        <p className="home-desc">
          Browse chemicals and equipment, submit item requests,
          and track your request statuses — all in one place.
        </p>

        {user ? (
          <>
            <div className="home-welcome">
              <h2>Welcome back, {user.username}!</h2>
              <p>You're signed in as {user.role}</p>
            </div>
            <div className="home-actions">
              <Link to="/dashboard" className="btn btn-primary btn-block">
                Dashboard
              </Link>
              <Link to="/inventory" className="btn btn-secondary btn-block">
                View Inventory
              </Link>
              <button onClick={logout} className="btn btn-secondary btn-block">
                Logout
              </button>
            </div>
          </>
        ) : (
          <div className="home-actions">
            <Link to="/register" className="btn btn-primary btn-block">
              Register
            </Link>
            <Link to="/login" className="btn btn-secondary btn-block">
              Login
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default Homepage;

import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import Orbs from '../components/Orbs';

const Dashboard = () => {
  const { user, logout } = useAuth();

  if (!user) return null;

  const role = user.role || 'STUDENT';
  const fullName =
    [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;

  const fmtDate = (iso) => {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div className="page dash-page">
      <Orbs />

      {/* ── Topbar ── */}
      <header className="topbar">
        <div className="topbar-left">
          <span className="topbar-logo">ChemLab</span>
          <nav className="topbar-nav">
            <span className="topbar-link active">Dashboard</span>
            <Link to="/inventory" className="topbar-link">Inventory</Link>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>
          Logout
        </button>
      </header>

      {/* ── Main content ── */}
      <main className="dash-main">
        <h1 className="dash-greeting">Welcome, {user.username}!</h1>

        {/* Profile Card */}
        <section className="glass dash-profile">
          <h3 className="dash-section-title">Profile Information</h3>

          <div className="profile-grid">
            <div className="profile-item">
              <span className="profile-label">Full Name</span>
              <span className="profile-value">{fullName}</span>
            </div>
            <div className="profile-item">
              <span className="profile-label">Username</span>
              <span className="profile-value">{user.username}</span>
            </div>
            <div className="profile-item">
              <span className="profile-label">Email</span>
              <span className="profile-value">{user.email}</span>
            </div>
            <div className="profile-item">
              <span className="profile-label">User ID</span>
              <span className="profile-value">#{user.id}</span>
            </div>
            <div className="profile-item">
              <span className="profile-label">Member Since</span>
              <span className="profile-value">{fmtDate(user.createdAt)}</span>
            </div>
            <div className="profile-item">
              <span className="profile-label">Last Updated</span>
              <span className="profile-value">{fmtDate(user.updatedAt)}</span>
            </div>
          </div>

          <span className={`badge badge-${role.toLowerCase()}`}>{role}</span>
        </section>
      </main>
    </div>
  );
};

export default Dashboard;

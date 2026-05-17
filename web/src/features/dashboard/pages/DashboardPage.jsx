import { Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../../auth/context/AuthContext';
import { requestsAPI } from '../../../services/api';
import Orbs from '../../../components/Orbs';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const [activeRequest, setActiveRequest] = useState(null);
  const [loadingRequest, setLoadingRequest] = useState(true);

  useEffect(() => {
    if (user && user.role === 'STUDENT') {
      requestsAPI.list({ size: 1, sort: 'createdAt,desc' })
        .then(res => {
          if (res.data && res.data.content && res.data.content.length > 0) {
            setActiveRequest(res.data.content[0]);
          }
        })
        .catch(err => console.error('Failed to fetch active request', err))
        .finally(() => setLoadingRequest(false));
    } else {
      setLoadingRequest(false);
    }
  }, [user]);

  if (!user) return null;

  const role = user.role || 'STUDENT';
  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;

  return (
    <div className="page dash-page">
      <Orbs />
      <header className="topbar">
        <div className="topbar-left">
          <span className="topbar-logo">ChemLab</span>
          <nav className="topbar-nav">
            <span className="topbar-link active">Dashboard</span>
            <Link to="/inventory" className="topbar-link">Inventory</Link>
            <Link to="/requests" className="topbar-link">Requests</Link>
          </nav>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div className={`identity-badge role-${role.toLowerCase()}`}>
            <span className="id-name">{fullName}</span>
            <span className="id-role">{role.replace('_', ' ')}</span>
          </div>
          <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
        </div>
      </header>
      <main className="dash-main">
        <h1 className="dash-greeting">Welcome back to the bench.</h1>
        
        {role === 'STUDENT' && (
          <div className="dash-widgets-grid">
            <div className="widget-glass" style={{ gridColumn: '1 / -1' }}>
              <h3 className="widget-title">
                <svg width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
                Active Flask Workflow
              </h3>
              {loadingRequest ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: '20px' }}>
                  <div className="flask-spinner" style={{ transform: 'scale(0.5)' }}></div>
                </div>
              ) : activeRequest ? (
                <div className="workflow-stepper">
                  {/* Step 1: Submitted */}
                  <div className="stepper-item completed">
                    <div className="step-icon">✓</div>
                    <span className="step-label">Submitted</span>
                  </div>

                  {/* Step 2: Pending Review */}
                  <div className={`stepper-item ${activeRequest.status === 'PENDING' ? 'active' : (activeRequest.status !== 'REJECTED' ? 'completed' : '')}`}>
                    <div className="step-icon">
                      {activeRequest.status === 'PENDING' ? <div className="flask-spinner" style={{ transform: 'scale(0.5)' }}></div> : '✓'}
                    </div>
                    <span className="step-label">Pending Review</span>
                  </div>

                  {/* Step 3: Approved */}
                  <div className={`stepper-item ${activeRequest.status === 'APPROVED' ? 'active' : (['RELEASED', 'COMPLETED'].includes(activeRequest.status) ? 'completed' : '')}`}>
                    <div className="step-icon">
                      {activeRequest.status === 'APPROVED' ? <div className="flask-spinner" style={{ transform: 'scale(0.5)' }}></div> : (['RELEASED', 'COMPLETED'].includes(activeRequest.status) ? '✓' : '3')}
                    </div>
                    <span className="step-label">Approved</span>
                  </div>

                  {/* Step 4: Released */}
                  <div className={`stepper-item ${['RELEASED', 'COMPLETED'].includes(activeRequest.status) ? 'completed' : ''}`}>
                    <div className="step-icon">
                      {['RELEASED', 'COMPLETED'].includes(activeRequest.status) ? '✓' : '4'}
                    </div>
                    <span className="step-label">Released</span>
                  </div>
                </div>
              ) : (
                <div className="empty-bench" style={{ padding: '20px 0' }}>
                  <p>No active workflow found.</p>
                </div>
              )}
            </div>

            <div className="widget-glass">
              <h3 className="widget-title">Quick Request Pad</h3>
              <div className="form-group">
                <input type="text" className="form-input" placeholder="Search Chemicals (e.g., HCl, Ethanol)..." />
                <button className="btn btn-primary" style={{ marginTop: '12px' }}>Add to Cart</button>
              </div>
            </div>

            <div className="widget-glass">
              <h3 className="widget-title">Lab Compliance Drawer</h3>
              <p style={{ fontSize: '0.85rem', color: '#94A3B8', marginBottom: '12px' }}>Download Safety Data Sheets (SDS) for active items.</p>
              <div className="empty-bench" style={{ padding: '20px 0' }}>
                <svg width="40" height="40" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24"><path d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path></svg>
                <p>No active items checked out.</p>
              </div>
            </div>
          </div>
        )}

        {role === 'LAB_TECHNICIAN' && (
          <div className="dash-widgets-grid">
            <div className="widget-glass" style={{ gridColumn: '1 / -1' }}>
              <h3 className="widget-title">Live Request Queue Snapshot</h3>
              <div className="empty-bench">
                <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
                <p>All quiet on the bench. No pending requests.</p>
              </div>
            </div>

            <div className="widget-glass">
              <h3 className="widget-title">Critical Inventory Gauges</h3>
              <div className="gauge-card danger">
                <span className="gauge-label">Low Stock Alerts</span>
                <span className="gauge-value">3</span>
              </div>
              <div className="gauge-card warning">
                <span className="gauge-label">Expiring Fuses (&lt; 30 days)</span>
                <span className="gauge-value">1</span>
              </div>
            </div>

            <div className="widget-glass" style={{ justifyContent: 'center' }}>
              <button className="btn btn-primary btn-block" style={{ padding: '20px', fontSize: '1.1rem' }}>
                <svg width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4"></path></svg>
                Quick Scan Scanner
              </button>
            </div>
          </div>
        )}

        {role === 'ADMINISTRATOR' && (
          <div className="dash-widgets-grid">
            <div className="widget-glass" style={{ gridColumn: '1 / -1' }}>
              <h3 className="widget-title">Lab Activity Metrics</h3>
              <div style={{ height: '150px', background: 'rgba(255,255,255,0.02)', border: '1px dashed rgba(255,255,255,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94A3B8', borderRadius: '8px' }}>
                [ Minimalist Line Chart Placeholder ]
              </div>
            </div>

            <div className="widget-glass">
              <h3 className="widget-title">System Audit Feed</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ fontSize: '0.85rem', color: '#94A3B8', padding: '12px', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', borderLeft: '3px solid #3B82F6' }}>Admin modified Sodium Chloride (+500g)</div>
                <div style={{ fontSize: '0.85rem', color: '#94A3B8', padding: '12px', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', borderLeft: '3px solid #10B981' }}>Technician approved Request #1024</div>
                <div style={{ fontSize: '0.85rem', color: '#94A3B8', padding: '12px', background: 'rgba(0,0,0,0.2)', borderRadius: '8px', borderLeft: '3px solid #EF4444' }}>System flagged Ethanol low stock</div>
              </div>
            </div>

            <div className="widget-glass">
              <h3 className="widget-title">User Grid Controller</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                  <div>
                    <div style={{ fontSize: '0.9rem', color: '#F0F4F8' }}>John Doe</div>
                    <div style={{ fontSize: '0.75rem', color: '#94A3B8' }}>STUDENT</div>
                  </div>
                  <button className="btn btn-secondary btn-sm">Manage</button>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                  <div>
                    <div style={{ fontSize: '0.9rem', color: '#F0F4F8' }}>Jane Smith</div>
                    <div style={{ fontSize: '0.75rem', color: '#94A3B8' }}>LAB_TECHNICIAN</div>
                  </div>
                  <button className="btn btn-secondary btn-sm">Manage</button>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default DashboardPage;
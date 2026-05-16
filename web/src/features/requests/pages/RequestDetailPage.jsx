import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { requestsAPI } from '../../../services/api';
import Orbs from '../../../components/Orbs';

const RequestDetailPage = () => {
  const { requestId } = useParams();
  const { user, logout } = useAuth();

  const [request, setRequest] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionRemarks, setActionRemarks] = useState('');
  const [success, setSuccess] = useState('');

  const role = user?.role || 'STUDENT';
  const canApproveReject = (role === 'TECHNICIAN' || role === 'ADMIN') && request?.status === 'PENDING';

  useEffect(() => {
    loadRequest();
  }, [requestId]);

  const loadRequest = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await requestsAPI.getById(requestId);
      setRequest(res.data);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load request.');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      const res = await requestsAPI.approve(requestId, { remarks: actionRemarks || null });
      setRequest(res.data);
      setSuccess('Request approved, inventory deducted, and marked as completed.');
      setActionRemarks('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to approve request.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async () => {
    setActionLoading(true);
    setError('');
    setSuccess('');
    try {
      const res = await requestsAPI.reject(requestId, { remarks: actionRemarks || null });
      setRequest(res.data);
      setSuccess('Request rejected.');
      setActionRemarks('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to reject request.');
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const s = (status || '').toLowerCase();
    return `badge badge-req-${s}`;
  };

  return (
    <div className="page dash-page">
      <Orbs />
      <header className="topbar">
        <div className="topbar-left">
          <span className="topbar-logo">ChemLab</span>
          <nav className="topbar-nav">
            <Link to="/dashboard" className="topbar-link">Dashboard</Link>
            <Link to="/inventory" className="topbar-link">Inventory</Link>
            <Link to="/requests" className="topbar-link">Requests</Link>
            <span className="topbar-link active">Request #{requestId}</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">Request #{requestId}</h1>
            <p className="inv-subtext">Request details and approval workflow.</p>
          </div>
          <Link to="/requests" className="btn btn-secondary">Back to Requests</Link>
        </section>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        {loading ? (
          <div className="glass inv-table-card"><div className="inv-loading">Loading request...</div></div>
        ) : request && (
          <>
            <section className="glass inv-table-card">
              <h3 className="dash-section-title">Request Information</h3>
              <div className="inv-details-grid">
                <div className="profile-item">
                  <span className="profile-label">Request ID</span>
                  <span className="profile-value">#{request.id}</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">Status</span>
                  <span className={getStatusBadge(request.status)}>{request.status}</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">Requester</span>
                  <span className="profile-value">{request.requesterUsername} ({request.requesterEmail})</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">Created At</span>
                  <span className="profile-value">{request.createdAt ? new Date(request.createdAt).toLocaleString() : '—'}</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">Last Updated</span>
                  <span className="profile-value">{request.updatedAt ? new Date(request.updatedAt).toLocaleString() : '—'}</span>
                </div>
              </div>
              {request.remarks && (
                <div style={{ marginTop: '16px' }}>
                  <span className="profile-label">Remarks</span>
                  <pre className="req-remarks">{request.remarks}</pre>
                </div>
              )}
            </section>

            <section className="glass inv-table-card">
              <h3 className="dash-section-title">Requested Items ({request.items?.length || 0})</h3>
              {(!request.items || request.items.length === 0) ? (
                <div className="inv-empty">No items in this request.</div>
              ) : (
                <div className="inv-table-wrap">
                  <table className="inv-table">
                    <thead>
                      <tr>
                        <th>Code</th>
                        <th>Item Name</th>
                        <th>Quantity</th>
                        <th>Unit</th>
                        <th>Expiration</th>
                      </tr>
                    </thead>
                    <tbody>
                      {request.items.map((item) => (
                        <tr key={item.id}>
                          <td>{item.itemCode}</td>
                          <td>{item.itemName}</td>
                          <td>{item.quantity}</td>
                          <td>{item.unitSnapshot || '—'}</td>
                          <td>{item.expirationSnapshot || '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>

            {canApproveReject && (
              <section className="glass inv-table-card">
                <h3 className="dash-section-title">Review Actions</h3>
                <div className="form-group">
                  <label className="form-label">Reviewer Remarks (optional)</label>
                  <textarea
                    className="form-input"
                    rows={3}
                    value={actionRemarks}
                    onChange={(e) => setActionRemarks(e.target.value)}
                    placeholder="Add notes about your decision..."
                    disabled={actionLoading}
                  />
                </div>
                <div className="req-action-btns">
                  <button className="btn btn-approve" onClick={handleApprove} disabled={actionLoading}>
                    {actionLoading ? 'Processing...' : '✓ Approve & Complete'}
                  </button>
                  <button className="btn btn-reject" onClick={handleReject} disabled={actionLoading}>
                    {actionLoading ? 'Processing...' : '✕ Reject'}
                  </button>
                </div>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default RequestDetailPage;

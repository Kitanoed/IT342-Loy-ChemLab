import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { requestsAPI } from '../../../services/api';
import Orbs from '../../../components/Orbs';

const statusOptions = ['', 'PENDING', 'APPROVED', 'REJECTED', 'RELEASED', 'COMPLETED'];

const RequestsPage = () => {
  const { user, logout } = useAuth();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const role = user?.role || 'STUDENT';

  const fetchRequests = async (nextPage = 0) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: nextPage, size: 15 };
      if (statusFilter) params.status = statusFilter;
      const res = await requestsAPI.list(params);
      setRequests(res.data.content || []);
      setPage(res.data.number ?? nextPage);
      setTotalPages(res.data.totalPages ?? 0);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load requests.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests(0);
  }, []);

  const onApplyFilter = (e) => {
    e.preventDefault();
    fetchRequests(0);
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
            <span className="topbar-link active">Requests</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">Requests</h1>
            <p className="inv-subtext">
              {role === 'STUDENT' ? 'Submit and track your item requests.' : 'Manage incoming item requests.'}
            </p>
          </div>
          <Link to="/requests/new" className="btn btn-primary">New Request</Link>
        </section>

        <section className="glass inv-filter-card">
          <form className="inv-filter-grid" onSubmit={onApplyFilter}>
            <div className="form-group">
              <label className="form-label">Status</label>
              <select className="form-input" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                {statusOptions.map((s) => (
                  <option key={s} value={s}>{s || 'All'}</option>
                ))}
              </select>
            </div>
            <div className="inv-filter-actions">
              <button type="submit" className="btn btn-primary">Apply</button>
              <button type="button" className="btn btn-secondary" onClick={() => { setStatusFilter(''); setTimeout(() => fetchRequests(0), 0); }}>Reset</button>
            </div>
          </form>
        </section>

        <section className="glass inv-table-card">
          {error && <div className="alert alert-error">{error}</div>}

          {loading ? (
            <div className="inv-loading">Loading requests...</div>
          ) : requests.length === 0 ? (
            <div className="inv-empty">No requests found.</div>
          ) : (
            <div className="inv-table-wrap">
              <table className="inv-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Requester</th>
                    <th>Items</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((req) => (
                    <tr key={req.id}>
                      <td>#{req.id}</td>
                      <td>{req.requesterUsername}</td>
                      <td>{req.items?.length || 0} item(s)</td>
                      <td><span className={getStatusBadge(req.status)}>{req.status}</span></td>
                      <td>{req.createdAt ? new Date(req.createdAt).toLocaleString() : '—'}</td>
                      <td>
                        <Link to={`/requests/${req.id}`} className="inv-view-link">View</Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="inv-pagination">
            <button className="btn btn-secondary btn-sm" disabled={page <= 0 || loading} onClick={() => fetchRequests(page - 1)}>Prev</button>
            <span className="inv-page-text">Page {page + 1} of {Math.max(totalPages, 1)}</span>
            <button className="btn btn-secondary btn-sm" disabled={loading || page + 1 >= totalPages} onClick={() => fetchRequests(page + 1)}>Next</button>
          </div>
        </section>
      </main>
    </div>
  );
};

export default RequestsPage;

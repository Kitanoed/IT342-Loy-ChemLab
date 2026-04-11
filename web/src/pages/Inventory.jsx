import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { inventoryAPI } from '../services/api';
import Orbs from '../components/Orbs';

const Inventory = () => {
  const { user, logout } = useAuth();

  const [search, setSearch] = useState('');
  const [type, setType] = useState('');
  const [status, setStatus] = useState('');
  const [labId, setLabId] = useState('');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);

  const role = user?.role || 'STUDENT';

  const fetchInventory = async (nextPage = 0) => {
    setLoading(true);
    setError('');

    try {
      const params = {
        page: nextPage,
        size,
        sort: 'updatedAt,desc',
      };

      if (search.trim()) params.search = search.trim();
      if (type) params.type = type;
      if (status) params.status = status;
      if (labId.trim()) params.labId = labId.trim();

      const res = await inventoryAPI.list(params);
      setItems(res.data.content || []);
      setPage(res.data.page ?? nextPage);
      setTotalPages(res.data.totalPages ?? 0);
    } catch (err) {
      const backendMessage = err?.response?.data?.message;
      const backendCode = err?.response?.data?.code;
      setError(backendCode ? `${backendCode}: ${backendMessage}` : (backendMessage || 'Failed to load inventory.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInventory(0);
  }, []);

  const onApplyFilters = (e) => {
    e.preventDefault();
    fetchInventory(0);
  };

  const onResetFilters = () => {
    setSearch('');
    setType('');
    setStatus('');
    setLabId('');
    setTimeout(() => fetchInventory(0), 0);
  };

  const columns = useMemo(
    () => [
      { key: 'itemCode', label: 'Code' },
      { key: 'itemName', label: 'Item Name' },
      { key: 'itemType', label: 'Type' },
      { key: 'quantity', label: 'Quantity' },
      { key: 'status', label: 'Status' },
      { key: 'storageLocation', label: 'Location' },
      { key: 'updatedAt', label: 'Last Updated' },
    ],
    []
  );

  return (
    <div className="page dash-page">
      <Orbs />

      <header className="topbar">
        <div className="topbar-left">
          <span className="topbar-logo">ChemLab</span>
          <nav className="topbar-nav">
            <Link to="/dashboard" className="topbar-link">Dashboard</Link>
            <span className="topbar-link active">Inventory</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">Inventory Viewing</h1>
            <p className="inv-subtext">Read-only inventory list for {role} role.</p>
          </div>
          {(role === 'TECHNICIAN' || role === 'ADMIN') && (
            <Link to="/inventory/new" className="btn btn-primary">Add Inventory</Link>
          )}
        </section>

        <section className="glass inv-filter-card">
          <form className="inv-filter-grid" onSubmit={onApplyFilters}>
            <div className="form-group">
              <label className="form-label">Search</label>
              <input
                className="form-input"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Name or code"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Type</label>
              <select className="form-input" value={type} onChange={(e) => setType(e.target.value)}>
                <option value="">All</option>
                <option value="CHEMICAL">Chemical</option>
                <option value="EQUIPMENT">Equipment</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Status</label>
              <select className="form-input" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="">All</option>
                <option value="AVAILABLE">Available</option>
                <option value="LOW_STOCK">Low Stock</option>
                <option value="OUT_OF_STOCK">Out of Stock</option>
                <option value="QUARANTINED">Quarantined</option>
                <option value="UNDER_MAINTENANCE">Under Maintenance</option>
                <option value="DISPOSED">Disposed</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Lab ID</label>
              <input
                className="form-input"
                value={labId}
                onChange={(e) => setLabId(e.target.value)}
                placeholder="e.g. 1"
              />
            </div>

            <div className="inv-filter-actions">
              <button type="submit" className="btn btn-primary">Apply</button>
              <button type="button" className="btn btn-secondary" onClick={onResetFilters}>Reset</button>
            </div>
          </form>
        </section>

        <section className="glass inv-table-card">
          {error && <div className="alert alert-error">{error}</div>}

          {loading ? (
            <div className="inv-loading">Loading inventory...</div>
          ) : items.length === 0 ? (
            <div className="inv-empty">No inventory items found.</div>
          ) : (
            <div className="inv-table-wrap">
              <table className="inv-table">
                <thead>
                  <tr>
                    {columns.map((col) => <th key={col.key}>{col.label}</th>)}
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((item) => (
                    <tr key={item.id}>
                      <td>{item.itemCode}</td>
                      <td>{item.itemName}</td>
                      <td>{item.itemType}</td>
                      <td>{item.quantity} {item.unit}</td>
                      <td>
                        <span className={`badge badge-${String(item.status || '').toLowerCase()}`}>
                          {item.status}
                        </span>
                      </td>
                      <td>{item.storageLocation || '—'}</td>
                      <td>{item.updatedAt ? new Date(item.updatedAt).toLocaleString() : '—'}</td>
                      <td>
                        <Link to={`/inventory/${item.id}`} className="inv-view-link">View</Link>
                        {(role === 'TECHNICIAN' || role === 'ADMIN') && (
                          <>
                            {' · '}
                            <Link to={`/inventory/${item.id}/edit`} className="inv-view-link">Edit</Link>
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="inv-pagination">
            <button
              className="btn btn-secondary btn-sm"
              disabled={page <= 0 || loading}
              onClick={() => fetchInventory(page - 1)}
            >
              Prev
            </button>
            <span className="inv-page-text">Page {page + 1} of {Math.max(totalPages, 1)}</span>
            <button
              className="btn btn-secondary btn-sm"
              disabled={loading || page + 1 >= totalPages}
              onClick={() => fetchInventory(page + 1)}
            >
              Next
            </button>
          </div>
        </section>
      </main>
    </div>
  );
};

export default Inventory;

import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { inventoryAPI } from '../services/api';
import Orbs from '../components/Orbs';

const InventoryItem = () => {
  const { itemId } = useParams();
  const { logout } = useAuth();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [item, setItem] = useState(null);
  const [audit, setAudit] = useState([]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');

      try {
        const [inventoryRes, auditRes] = await Promise.all([
          inventoryAPI.getById(itemId),
          inventoryAPI.getItemAuditLogs(itemId, { page: 0, size: 10 }),
        ]);

        setItem(inventoryRes.data);
        setAudit(auditRes.data.content || []);
      } catch (err) {
        setError(err?.response?.data?.message || err.message || 'Failed to load item details.');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [itemId]);

  const detailRows = useMemo(() => {
    if (!item) return [];

    return [
      ['Code', item.itemCode],
      ['Name', item.itemName],
      ['Type', item.itemType],
      ['Category', item.category || '—'],
      ['Quantity', `${item.quantity} ${item.unit}`],
      ['Status', item.status],
      ['Location', item.storageLocation || '—'],
      ['Lot Number', item.lotNumber || '—'],
      ['Expiry Date', item.expiryDate || '—'],
      ['Lab ID', item.labId],
      ['Version', item.version],
      ['Updated At', item.updatedAt ? new Date(item.updatedAt).toLocaleString() : '—'],
    ];
  }, [item]);

  return (
    <div className="page dash-page">
      <Orbs />

      <header className="topbar">
        <div className="topbar-left">
          <span className="topbar-logo">ChemLab</span>
          <nav className="topbar-nav">
            <Link to="/dashboard" className="topbar-link">Dashboard</Link>
            <Link to="/inventory" className="topbar-link">Inventory</Link>
            <span className="topbar-link active">Item Details</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">Inventory Item Details</h1>
            <p className="inv-subtext">Read-only details and latest audit logs.</p>
          </div>
          <Link to="/inventory" className="btn btn-secondary">Back to Inventory</Link>
        </section>

        <section className="glass inv-table-card">
          {error && <div className="alert alert-error">{error}</div>}

          {loading ? (
            <div className="inv-loading">Loading item details...</div>
          ) : (
            <div className="inv-details-grid">
              {detailRows.map(([label, value]) => (
                <div className="profile-item" key={label}>
                  <span className="profile-label">{label}</span>
                  <span className="profile-value">{value}</span>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="glass inv-table-card">
          <h3 className="dash-section-title">Recent Audit Logs</h3>
          {loading ? (
            <div className="inv-loading">Loading audit logs...</div>
          ) : audit.length === 0 ? (
            <div className="inv-empty">No audit logs available.</div>
          ) : (
            <div className="inv-table-wrap">
              <table className="inv-table">
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Actor</th>
                    <th>Role</th>
                    <th>Reason</th>
                    <th>Created At</th>
                  </tr>
                </thead>
                <tbody>
                  {audit.map((entry) => (
                    <tr key={entry.id}>
                      <td>{entry.action}</td>
                      <td>{entry.actorUserId}</td>
                      <td>{entry.actorRole}</td>
                      <td>{entry.reasonCode || '—'}</td>
                      <td>{entry.createdAt ? new Date(entry.createdAt).toLocaleString() : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  );
};

export default InventoryItem;

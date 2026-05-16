import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { inventoryAPI, requestsAPI } from '../../../services/api';
import Orbs from '../../../components/Orbs';

const RequestCreatePage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [inventory, setInventory] = useState([]);
  const [selectedItems, setSelectedItems] = useState([]);
  const [remarks, setRemarks] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadInventory();
  }, []);

  const loadInventory = async () => {
    setLoading(true);
    try {
      const res = await inventoryAPI.list({ page: 0, size: 100, sort: 'itemName,asc' });
      setInventory(res.data.content || []);
    } catch (err) {
      setError('Failed to load inventory items.');
    } finally {
      setLoading(false);
    }
  };

  const addItem = (item) => {
    if (selectedItems.find((si) => si.inventoryItemId === item.id)) return;
    setSelectedItems([...selectedItems, {
      inventoryItemId: item.id,
      itemName: item.itemName,
      itemCode: item.itemCode,
      unit: item.unit,
      availableQty: item.quantity,
      quantity: 1,
    }]);
  };

  const removeItem = (inventoryItemId) => {
    setSelectedItems(selectedItems.filter((si) => si.inventoryItemId !== inventoryItemId));
  };

  const updateQuantity = (inventoryItemId, qty) => {
    setSelectedItems(selectedItems.map((si) =>
      si.inventoryItemId === inventoryItemId ? { ...si, quantity: Math.max(1, parseInt(qty) || 1) } : si
    ));
  };

  const filteredInventory = inventory.filter((item) => {
    const term = searchTerm.toLowerCase();
    return (
      (item.itemName || '').toLowerCase().includes(term) ||
      (item.itemCode || '').toLowerCase().includes(term)
    );
  });

  const onSubmit = async (e) => {
    e.preventDefault();
    if (selectedItems.length === 0) {
      setError('Add at least one item to your request.');
      return;
    }

    setSubmitting(true);
    setError('');
    setSuccess('');

    try {
      await requestsAPI.create({
        items: selectedItems.map((si) => ({
          inventoryItemId: si.inventoryItemId,
          quantity: si.quantity,
        })),
        remarks: remarks || null,
      });
      setSuccess('Request submitted successfully!');
      setTimeout(() => navigate('/requests'), 800);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to submit request.');
    } finally {
      setSubmitting(false);
    }
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
            <span className="topbar-link active">New Request</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">Create New Request</h1>
            <p className="inv-subtext">Search and select items, set quantities, then submit.</p>
          </div>
          <Link to="/requests" className="btn btn-secondary">Back</Link>
        </section>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <section className="glass inv-table-card">
          <h3 className="dash-section-title">Available Inventory</h3>
          <input
            className="form-input"
            placeholder="Search items by name or code..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ marginBottom: '12px' }}
          />
          {loading ? (
            <div className="inv-loading">Loading inventory...</div>
          ) : (
            <div className="inv-table-wrap" style={{ maxHeight: '260px', overflowY: 'auto' }}>
              <table className="inv-table">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Available</th>
                    <th>Unit</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {filteredInventory.map((item) => {
                    const isAdded = selectedItems.find((si) => si.inventoryItemId === item.id);
                    return (
                      <tr key={item.id} style={{ opacity: isAdded ? 0.5 : 1 }}>
                        <td>{item.itemCode}</td>
                        <td>{item.itemName}</td>
                        <td>{item.quantity}</td>
                        <td>{item.unit}</td>
                        <td>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => addItem(item)}
                            disabled={!!isAdded}
                          >
                            {isAdded ? 'Added' : 'Add'}
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="glass inv-table-card">
          <h3 className="dash-section-title">Selected Items ({selectedItems.length})</h3>
          {selectedItems.length === 0 ? (
            <div className="inv-empty">No items selected yet. Add items from the inventory above.</div>
          ) : (
            <div className="inv-table-wrap">
              <table className="inv-table">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Quantity</th>
                    <th>Unit</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {selectedItems.map((si) => (
                    <tr key={si.inventoryItemId}>
                      <td>{si.itemCode}</td>
                      <td>{si.itemName}</td>
                      <td>
                        <input
                          type="number"
                          className="form-input"
                          style={{ width: '90px' }}
                          min="1"
                          max={si.availableQty}
                          value={si.quantity}
                          onChange={(e) => updateQuantity(si.inventoryItemId, e.target.value)}
                        />
                      </td>
                      <td>{si.unit}</td>
                      <td>
                        <button className="btn btn-danger btn-sm" onClick={() => removeItem(si.inventoryItemId)}>Remove</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="form-group" style={{ marginTop: '16px' }}>
            <label className="form-label">Remarks (optional)</label>
            <textarea
              className="form-input"
              rows={3}
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              placeholder="Additional notes for your request..."
            />
          </div>

          <div className="inv-form-actions" style={{ marginTop: '16px' }}>
            <button
              className="btn btn-primary"
              onClick={onSubmit}
              disabled={submitting || selectedItems.length === 0}
            >
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </div>
        </section>
      </main>
    </div>
  );
};

export default RequestCreatePage;

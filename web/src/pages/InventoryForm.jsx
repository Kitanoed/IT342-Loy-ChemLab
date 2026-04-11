import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { inventoryAPI, pubchemAPI } from '../services/api';
import Orbs from '../components/Orbs';

const defaultForm = {
  itemCode: '',
  itemName: '',
  itemType: 'CHEMICAL',
  category: '',
  quantity: '0',
  unit: 'g',
  minThreshold: '0',
  labId: '',
  storageLocation: '',
  description: '',
  safetyNotes: '',
  pubchemCid: null,
  molecularFormula: '',
  molecularWeight: '',
  iupacName: '',
};

const InventoryForm = () => {
  const { itemId } = useParams();
  const isEdit = Boolean(itemId);
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [form, setForm] = useState(defaultForm);
  const [chemicalName, setChemicalName] = useState('');
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [lookupLoading, setLookupLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!isEdit) return;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await inventoryAPI.getById(itemId);
        const data = res.data;
        setForm({
          itemCode: data.itemCode || '',
          itemName: data.itemName || '',
          itemType: data.itemType || 'CHEMICAL',
          category: data.category || '',
          quantity: String(data.quantity ?? 0),
          unit: data.unit || '',
          minThreshold: String(data.minThreshold ?? 0),
          labId: String(data.labId ?? ''),
          storageLocation: data.storageLocation || '',
          description: data.description || '',
          safetyNotes: data.safetyNotes || '',
          pubchemCid: data.pubchemCid ?? null,
          molecularFormula: data.molecularFormula || '',
          molecularWeight: data.molecularWeight || '',
          iupacName: data.iupacName || '',
        });
        setChemicalName(data.itemName || '');
      } catch (err) {
        setError(err?.response?.data?.message || 'Failed to load inventory item.');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [isEdit, itemId]);

  const canEdit = useMemo(() => {
    const role = user?.role || 'STUDENT';
    return role === 'TECHNICIAN' || role === 'ADMIN';
  }, [user]);

  const updateField = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const fetchChemical = async (name) => {
    const res = await pubchemAPI.lookup(name);
    return res.data;
  };

  const onFetchPubChem = async () => {
    setLookupLoading(true);
    setError('');
    setSuccess('');

    try {
      const query = chemicalName.trim() || form.itemName.trim();
      if (!query) {
        throw new Error('Please enter a chemical name first.');
      }

      const data = await fetchChemical(query);

      setPreview(data);
      setForm((prev) => ({
        ...prev,
        itemName: prev.itemName || query,
        pubchemCid: data.cid,
        molecularFormula: data.formula || '',
        molecularWeight: data.weight || '',
        iupacName: data.iupac || '',
        description: prev.description || `Chemical reference fetched from PubChem for ${query}.`,
        safetyNotes:
          prev.safetyNotes ||
          'Handle with proper PPE and follow your laboratory SDS protocol before use.',
      }));

      setSuccess(`Chemical data loaded (${data.source || 'pubchem'}).`);
    } catch (err) {
      setError(err.message || 'Failed to fetch PubChem data.');
      setPreview(null);
    } finally {
      setLookupLoading(false);
    }
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const quantity = Number(form.quantity);
      const minThreshold = Number(form.minThreshold || 0);

      if (Number.isNaN(quantity)) {
        throw new Error('Quantity must be a valid number.');
      }
      if (Number.isNaN(minThreshold)) {
        throw new Error('Min threshold must be a valid number.');
      }

      const payload = {
        ...form,
        quantity,
        minThreshold,
        pubchemCid: form.pubchemCid ? Number(form.pubchemCid) : null,
      };

      if (isEdit) {
        delete payload.labId;
      } else {
        const labId = Number(form.labId);
        if (Number.isNaN(labId) || labId <= 0) {
          throw new Error('Lab ID must be a valid positive number.');
        }
        payload.labId = labId;
      }

      if (isEdit) {
        await inventoryAPI.update(itemId, payload);
      } else {
        await inventoryAPI.create(payload);
      }

      setSuccess(`Inventory item ${isEdit ? 'updated' : 'created'} successfully.`);
      setTimeout(() => navigate('/inventory'), 700);
    } catch (err) {
      const backendMessage = err?.response?.data?.message;
      const backendCode = err?.response?.data?.code;
      setError(backendCode ? `${backendCode}: ${backendMessage}` : (backendMessage || err.message || 'Failed to save inventory item.'));
    } finally {
      setLoading(false);
    }
  };

  if (!canEdit) {
    return (
      <div className="page page-center">
        <Orbs />
        <div className="glass auth-card">
          <h2 className="auth-title">Access denied</h2>
          <p className="auth-subtitle">Only Technician or Admin can create/edit inventory.</p>
          <Link className="btn btn-secondary btn-block" to="/inventory">Back to Inventory</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page dash-page">
      <Orbs />

      <header className="topbar">
        <div className="topbar-left">
          <span className="topbar-logo">ChemLab</span>
          <nav className="topbar-nav">
            <Link to="/dashboard" className="topbar-link">Dashboard</Link>
            <Link to="/inventory" className="topbar-link">Inventory</Link>
            <span className="topbar-link active">{isEdit ? 'Edit Item' : 'Create Item'}</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">{isEdit ? 'Edit Inventory Item' : 'Create Inventory Item'}</h1>
            <p className="inv-subtext">Use PubChem lookup to auto-fill chemical fields.</p>
          </div>
          <Link to="/inventory" className="btn btn-secondary">Back</Link>
        </section>

        <section className="glass inv-table-card">
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}

          <div className="inv-pubchem-row">
            <input
              className="form-input"
              placeholder="Chemical Name"
              value={chemicalName}
              onChange={(e) => setChemicalName(e.target.value)}
              disabled={lookupLoading || loading}
            />
            <button
              type="button"
              className="btn btn-primary"
              onClick={onFetchPubChem}
              disabled={lookupLoading || loading}
            >
              {lookupLoading ? 'Fetching...' : 'Fetch from PubChem'}
            </button>
          </div>

          {preview && (
            <div className="inv-preview">
              <h3 className="dash-section-title">Preview from PubChem</h3>
              <div className="inv-preview-grid">
                <div><span className="profile-label">CID</span><span className="profile-value">{preview.cid}</span></div>
                <div><span className="profile-label">Formula</span><span className="profile-value">{preview.formula || '—'}</span></div>
                <div><span className="profile-label">Weight</span><span className="profile-value">{preview.weight || '—'}</span></div>
                <div><span className="profile-label">IUPAC</span><span className="profile-value">{preview.iupac || '—'}</span></div>
              </div>
            </div>
          )}

          <form className="inv-form-grid" onSubmit={onSubmit}>
            <div className="form-group">
              <label className="form-label">Item Code</label>
              <input className="form-input" value={form.itemCode} onChange={(e) => updateField('itemCode', e.target.value)} disabled={loading || isEdit} required={!isEdit} />
            </div>

            <div className="form-group">
              <label className="form-label">Name</label>
              <input className="form-input" value={form.itemName} onChange={(e) => updateField('itemName', e.target.value)} disabled={loading} required />
            </div>

            <div className="form-group">
              <label className="form-label">Type</label>
              <select className="form-input" value={form.itemType} onChange={(e) => updateField('itemType', e.target.value)} disabled={loading}>
                <option value="CHEMICAL">Chemical</option>
                <option value="EQUIPMENT">Equipment</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Category</label>
              <input className="form-input" value={form.category} onChange={(e) => updateField('category', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group">
              <label className="form-label">Quantity</label>
              <input className="form-input" type="number" step="0.001" value={form.quantity} onChange={(e) => updateField('quantity', e.target.value)} disabled={loading} required />
            </div>

            <div className="form-group">
              <label className="form-label">Unit</label>
              <input className="form-input" value={form.unit} onChange={(e) => updateField('unit', e.target.value)} disabled={loading} required />
            </div>

            <div className="form-group">
              <label className="form-label">Min Threshold</label>
              <input className="form-input" type="number" step="0.001" value={form.minThreshold} onChange={(e) => updateField('minThreshold', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group">
              <label className="form-label">Lab ID</label>
              <input className="form-input" type="number" value={form.labId} onChange={(e) => updateField('labId', e.target.value)} disabled={loading} required />
            </div>

            <div className="form-group inv-col-span-2">
              <label className="form-label">Storage Location</label>
              <input className="form-input" value={form.storageLocation} onChange={(e) => updateField('storageLocation', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group inv-col-span-2">
              <label className="form-label">Description</label>
              <textarea className="form-input" rows={3} value={form.description} onChange={(e) => updateField('description', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group inv-col-span-2">
              <label className="form-label">Safety Notes</label>
              <textarea className="form-input" rows={3} value={form.safetyNotes} onChange={(e) => updateField('safetyNotes', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group">
              <label className="form-label">PubChem CID</label>
              <input className="form-input" value={form.pubchemCid || ''} onChange={(e) => updateField('pubchemCid', e.target.value ? Number(e.target.value) : null)} disabled={loading} />
            </div>

            <div className="form-group">
              <label className="form-label">Molecular Formula</label>
              <input className="form-input" value={form.molecularFormula} onChange={(e) => updateField('molecularFormula', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group">
              <label className="form-label">Molecular Weight</label>
              <input className="form-input" value={form.molecularWeight} onChange={(e) => updateField('molecularWeight', e.target.value)} disabled={loading} />
            </div>

            <div className="form-group">
              <label className="form-label">IUPAC Name</label>
              <input className="form-input" value={form.iupacName} onChange={(e) => updateField('iupacName', e.target.value)} disabled={loading} />
            </div>

            <div className="inv-form-actions inv-col-span-2">
              <button className="btn btn-primary" type="submit" disabled={loading}>
                {loading ? 'Saving...' : isEdit ? 'Update Item' : 'Create Item'}
              </button>
            </div>
          </form>
        </section>
      </main>
    </div>
  );
};

export default InventoryForm;

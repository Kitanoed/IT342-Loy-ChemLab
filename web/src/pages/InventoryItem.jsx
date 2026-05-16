import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { inventoryAPI, filesAPI } from '../services/api';
import Orbs from '../components/Orbs';

const InventoryItem = () => {
  const { itemId } = useParams();
  const { user, logout } = useAuth();
  const role = user?.role || 'STUDENT';

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [item, setItem] = useState(null);
  const [audit, setAudit] = useState([]);

  // SDS Files
  const [files, setFiles] = useState([]);
  const [uploadFile, setUploadFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState('');

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
    loadFiles();
  }, [itemId]);

  const loadFiles = async () => {
    try {
      const res = await filesAPI.list(itemId);
      setFiles(res.data || []);
    } catch {
      // Silent fail — files section is supplementary
    }
  };

  const handleUpload = async () => {
    if (!uploadFile) return;

    setUploading(true);
    setUploadError('');
    setUploadSuccess('');

    try {
      await filesAPI.upload(uploadFile, itemId);
      setUploadSuccess('File uploaded successfully.');
      setUploadFile(null);
      // Reset file input
      const fileInput = document.getElementById('sds-file-input');
      if (fileInput) fileInput.value = '';
      loadFiles();
    } catch (err) {
      setUploadError(err?.response?.data?.message || 'Failed to upload file.');
    } finally {
      setUploading(false);
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  };

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
            <Link to="/requests" className="topbar-link">Requests</Link>
            <span className="topbar-link active">Item Details</span>
          </nav>
        </div>
        <button className="btn btn-danger btn-sm" onClick={logout}>Logout</button>
      </header>

      <main className="dash-main inv-main">
        <section className="inv-header-row">
          <div>
            <h1 className="dash-greeting">Inventory Item Details</h1>
            <p className="inv-subtext">Read-only details, audit logs, and SDS files.</p>
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

        {/* SDS Files Section */}
        <section className="glass inv-table-card">
          <h3 className="dash-section-title">Safety Data Sheets (SDS)</h3>

          {(role === 'TECHNICIAN' || role === 'ADMIN') && (
            <div className="sds-upload-row">
              <input
                id="sds-file-input"
                type="file"
                accept=".pdf,.jpg,.jpeg,.png"
                className="form-input"
                onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                disabled={uploading}
              />
              <button
                className="btn btn-primary btn-sm"
                onClick={handleUpload}
                disabled={uploading || !uploadFile}
              >
                {uploading ? 'Uploading...' : 'Upload SDS'}
              </button>
            </div>
          )}

          {uploadError && <div className="alert alert-error">{uploadError}</div>}
          {uploadSuccess && <div className="alert alert-success">{uploadSuccess}</div>}

          {files.length === 0 ? (
            <div className="inv-empty">No SDS files uploaded yet.</div>
          ) : (
            <div className="inv-table-wrap">
              <table className="inv-table">
                <thead>
                  <tr>
                    <th>File Name</th>
                    <th>Type</th>
                    <th>Size</th>
                    <th>Uploaded By</th>
                    <th>Date</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {files.map((f) => (
                    <tr key={f.id}>
                      <td>{f.fileName}</td>
                      <td>{f.fileType}</td>
                      <td>{formatFileSize(f.fileSize)}</td>
                      <td>{f.uploaderUsername}</td>
                      <td>{f.createdAt ? new Date(f.createdAt).toLocaleString() : '—'}</td>
                      <td>
                        <a href={f.downloadUrl} target="_blank" rel="noopener noreferrer" className="inv-view-link">
                          Download
                        </a>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
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

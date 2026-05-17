import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { inventoryAPI, filesAPI } from '../../../services/api';
import Orbs from '../../../components/Orbs';

const InventoryItemPage = () => {
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
            <div className="inv-loading" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '40px' }}>
              <div className="flask-spinner"></div>
              <p style={{ marginTop: '16px', color: '#94A3B8' }}>Loading compound blueprint...</p>
            </div>
          ) : (
            <div className="pubchem-inspector">
              <div className="blueprint-frame">
                <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <div className="chem-icon" style={{ width: '48px', height: '48px', fontSize: '1.8rem' }}>
                    {item?.itemType === 'CHEMICAL' ? '⚗️' : '🔬'}
                  </div>
                  <div>
                    <div style={{ fontSize: '1.2rem', color: '#F0F4F8', fontWeight: '800' }}>{item?.itemName}</div>
                    <div style={{ fontSize: '0.8rem', color: '#06B6D4', fontFamily: 'monospace' }}>CID: {item?.itemCode}</div>
                  </div>
                </div>

                {detailRows.map(([label, value]) => (
                  <div className="blueprint-row" key={label}>
                    <span className="blueprint-label">{label}</span>
                    <span className="blueprint-value">{value}</span>
                  </div>
                ))}
              </div>

              <div className="safety-shield">
                <div className="safety-title">
                  <svg width="24" height="24" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                  GHS Safety Shield
                </div>
                
                {item?.itemType === 'CHEMICAL' ? (
                  <>
                    <p className="safety-notes">
                      <strong>Handling Warning:</strong> Observe all institutional safety protocols before dispensing. This material may require specialized fume hood extraction. Review the attached Safety Data Sheets (SDS) below before handling.
                    </p>
                    <div className="hazard-symbols">
                      <div className="hazard-icon" title="Flammable"><span>🔥</span></div>
                      <div className="hazard-icon" title="Toxic"><span>☠️</span></div>
                      <div className="hazard-icon" title="Corrosive"><span>🧪</span></div>
                    </div>
                  </>
                ) : (
                  <p className="safety-notes" style={{ color: '#94A3B8' }}>
                    <strong>Equipment Status:</strong> Structural integrity checks are recommended before each use. Ensure power cords and physical housings are not compromised.
                  </p>
                )}

                <div style={{ marginTop: '24px', padding: '16px', background: 'rgba(0,0,0,0.3)', borderRadius: '8px', borderLeft: `3px solid ${item?.status === 'AVAILABLE' ? '#10B981' : '#F59E0B'}` }}>
                  <div style={{ fontSize: '0.75rem', color: '#94A3B8', textTransform: 'uppercase', marginBottom: '4px' }}>Real-time Status</div>
                  <div style={{ color: '#F0F4F8', fontWeight: '700', fontSize: '1.1rem' }}>{item?.status}</div>
                  <div style={{ fontSize: '0.85rem', color: '#94A3B8', marginTop: '4px' }}>Shelf Location: {item?.storageLocation || 'Unknown'}</div>
                </div>
              </div>
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
            <div className="empty-bench">
              <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
              <p>No safety documents found for this item.</p>
            </div>
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
            <div className="empty-bench">
              <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              <p>No recent activity on this item.</p>
            </div>
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

export default InventoryItemPage;
import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import './Devices.css';

function Devices() {
  const [devices, setDevices] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    max_consumption: '',
    ownerUsername: ''
  });
  const [editingId, setEditingId] = useState(null);
  
  const currentUsername = localStorage.getItem('username');
  const userRole = localStorage.getItem('role');
  const isAdmin = userRole === 'ADMIN';

  useEffect(() => {
    fetchDevices();
    if (isAdmin) {
      fetchUsers();
    }
  }, []);

  const fetchDevices = async () => {
    try {
      const response = await API.get('/devices');
      setDevices(response.data);
    } catch (err) {
      setError('Failed to fetch devices');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await API.get('/devices/users');
      setUsers(response.data);
    } catch (err) {
      console.error('Failed to fetch users:', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    // Validate max_consumption is a number
    const maxConsumption = parseFloat(formData.max_consumption);
    if (isNaN(maxConsumption) || maxConsumption <= 0) {
      setError('Max consumption must be a positive number');
      return;
    }
    
    try {
      const deviceData = {
        name: formData.name,
        max_consumption: maxConsumption,
        // For CLIENT users, always use their own username
        // For ADMIN users, use selected username from dropdown
        ownerUsername: isAdmin ? formData.ownerUsername : currentUsername
      };

      if (editingId) {
        await API.put(`/devices/${editingId}`, deviceData);
      } else {
        await API.post('/devices', deviceData);
      }
      fetchDevices();
      resetForm();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save device');
    }
  };

  const handleEdit = (device) => {
    setFormData({
      name: device.name,
      max_consumption: device.max_consumption.toString(),
      ownerUsername: device.ownerUsername
    });
    setEditingId(device.id);
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this device?')) return;
    
    try {
      await API.delete(`/devices/${id}`);
      setDevices(devices.filter(device => device.id !== id));
    } catch (err) {
      setError('Failed to delete device');
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      max_consumption: '',
      ownerUsername: isAdmin ? '' : currentUsername
    });
    setEditingId(null);
    setShowModal(false);
    setError('');
  };

  const openModal = () => {
    setFormData({
      name: '',
      max_consumption: '',
      ownerUsername: isAdmin ? '' : currentUsername
    });
    setShowModal(true);
  };

  if (loading) return (
    <div className="loading-container">
      <div className="spinner"></div>
      <p>Loading devices...</p>
    </div>
  );

  return (
    <div className="devices-container">
      <div className="page-header">
        <div>
          <h2>📱 {isAdmin ? 'All Devices' : 'My Devices'}</h2>
          <p className="page-subtitle">
            {isAdmin ? 'Manage devices for all users' : 'Manage your connected devices'}
          </p>
        </div>
        <button onClick={openModal} className="btn-primary">
          ➕ Add Device
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {devices.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📭</div>
          <h3>No devices yet</h3>
          <p>Start by adding your first device</p>
          <button onClick={openModal} className="btn-primary">
            Add Your First Device
          </button>
        </div>
      ) : (
        <div className="devices-grid">
          {devices.map(device => (
            <div key={device.id} className="device-card">
              <div className="device-header">
                <div className="device-icon">⚡</div>
                <h3>{device.name}</h3>
              </div>
              <div className="device-body">
                <div className="device-field">
                  <span className="field-label">Max Consumption:</span>
                  <span className="field-value consumption-badge">
                    {device.max_consumption} kWh
                  </span>
                </div>
                <div className="device-field">
                  <span className="field-label">Owner:</span>
                  <span className="field-value badge-owner">{device.ownerUsername}</span>
                </div>
              </div>
              <div className="device-actions">
                <button onClick={() => window.location.href = `/devices/${device.id}/consumption`} className="btn-view">
                  📊 View Consumption
                </button>     
                <button onClick={() => handleEdit(device)} className="btn-edit">
                  ✏️ Edit
                </button>
                <button onClick={() => handleDelete(device.id)} className="btn-delete">
                  🗑️ Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={resetForm}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingId ? '✏️ Edit Device' : '➕ Add New Device'}</h3>
              <button className="modal-close" onClick={resetForm}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Device Name *</label>
                <input
                  type="text"
                  placeholder="e.g., Air Conditioner, Refrigerator"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  required
                />
              </div>

              <div className="form-group">
                <label>Max Consumption (kWh) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  placeholder="e.g., 2.5"
                  value={formData.max_consumption}
                  onChange={(e) => setFormData({...formData, max_consumption: e.target.value})}
                  required
                />
                <small className="form-hint">Enter the maximum power consumption in kilowatt-hours</small>
              </div>

              {/* Only show owner selection for ADMIN users */}
              {isAdmin && (
                <div className="form-group">
                  <label>Device Owner *</label>
                  <select
                    value={formData.ownerUsername}
                    onChange={(e) => setFormData({...formData, ownerUsername: e.target.value})}
                    required
                    className="owner-select"
                  >
                    <option value="">Select a user...</option>
                    {users.map(user => (
                      <option key={user.id} value={user.username}>
                        {user.username} ({user.email})
                      </option>
                    ))}
                  </select>
                  <small className="form-hint">Select the user who will own this device</small>
                </div>
              )}

              {/* Show info for CLIENT users */}
              {!isAdmin && (
                <div className="form-group">
                  <label>Device Owner</label>
                  <div className="owner-display">
                    <span className="badge-owner">{currentUsername}</span>
                    <small className="form-hint">You are the owner of this device</small>
                  </div>
                </div>
              )}

              <div className="modal-actions">
                <button type="submit" className="btn-primary">
                  {editingId ? 'Update Device' : 'Add Device'}
                </button>
                <button type="button" onClick={resetForm} className="btn-secondary">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Devices;

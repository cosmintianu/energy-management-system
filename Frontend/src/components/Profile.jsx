import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import './Profile.css';

function Profile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    address: ''
  });

  const currentUsername = localStorage.getItem('username');
  const role = localStorage.getItem('role');

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      // Get all users and find current user
      const response = await API.get('/users');
    //   const role = await API.get(`/users/${currentUsername}/role`)
      const currentUser = response.data.find(u => u.username === currentUsername);
      
      if (currentUser) {
        setUser(currentUser);
        setFormData({
          username: currentUser.username,
          email: currentUser.email || '',
          address: currentUser.address || ''
        });
      } else {
        setError('Profile not found');
      }
    } catch (err) {
      setError('Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    try {
      await API.put(`/users/${user.id}`, formData);
      
      setUser({ ...user, ...formData });
      setSuccess('Profile updated successfully!');
      setEditing(false);
      
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    }
  };

  const handleCancel = () => {
    setFormData({
      username: user.username,
      email: user.email || '',
      address: user.address || ''
    });
    setEditing(false);
    setError('');
  };

  if (loading) return (
    <div className="loading-container">
      <div className="spinner"></div>
      <p>Loading profile...</p>
    </div>
  );

  if (!user) return (
    <div className="error-container">
      <div className="error-icon">⚠️</div>
      <p>Profile not found</p>
    </div>
  );

  return (
    <div className="profile-container">
      <div className="profile-header">
        <div>
          <h2>👤 My Profile</h2>
          <p className="page-subtitle">Manage your personal information</p>
        </div>
        {!editing && (
          <button onClick={() => setEditing(true)} className="btn-primary">
            ✏️ Edit Profile
          </button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <div className="profile-card">
        {!editing ? (
          // View Mode
          <div className="profile-view">
            <div className="profile-section">
              <div className="profile-item">
                <div className="profile-icon">👤</div>
                <div className="profile-details">
                  <label>Username</label>
                  <p>{user.username}</p>
                </div>
              </div>

              <div className="profile-item">
                <div className="profile-icon">📧</div>
                <div className="profile-details">
                  <label>Email</label>
                  <p>{user.email || <span className="text-muted">Not provided</span>}</p>
                </div>
              </div>

              <div className="profile-item">
                <div className="profile-icon">📍</div>
                <div className="profile-details">
                  <label>Address</label>
                  <p>{user.address || <span className="text-muted">Not provided</span>}</p>
                </div>
              </div>

              <div className="profile-item">
                <div className="profile-icon">🔐</div>
                <div className="profile-details">
                  <label>Role</label>
                  <p>
                    <span className={`role-badge role-${role.toLowerCase()}`}>
                      {role === 'ADMIN' ? '👑' : '👤'} {role}
                    </span>
                  </p>
                </div>
              </div>
            </div>
          </div>
        ) : (
          // Edit Mode
          <form onSubmit={handleSubmit} className="profile-form">
            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                value={formData.username}
                disabled
                className="input-disabled"
              />
              <small className="form-hint">Username cannot be changed</small>
            </div>

            <div className="form-group">
              <label>Email *</label>
              <input
                type="email"
                placeholder="your.email@example.com"
                value={formData.email}
                onChange={(e) => setFormData({...formData, email: e.target.value})}
                required
              />
            </div>

            <div className="form-group">
              <label>Address *</label>
              <input
                type="text"
                placeholder="Your address"
                value={formData.address}
                onChange={(e) => setFormData({...formData, address: e.target.value})}
                required
              />
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-primary">
                💾 Save Changes
              </button>
              <button type="button" onClick={handleCancel} className="btn-secondary">
                ❌ Cancel
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

export default Profile;

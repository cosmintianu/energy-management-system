import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import './Users.css';

function Users() {
  const [users, setUsers] = useState([]);
  const [authUsers, setAuthUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    address: ''
  });

  useEffect(() => {
    fetchUsers();
    fetchAuthUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await API.get('/users');
      setUsers(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  };

  const fetchAuthUsers = async () => {
    try {
      // This endpoint needs to exist in auth service
      const response = await API.get('/auth/users');
      setAuthUsers(response.data);
    } catch (err) {
      console.error('Failed to fetch auth users:', err);
    }
  };

  const getUserRole = (username) => {
    const authUser = authUsers.find(u => u.username === username);
    return authUser?.role || 'CLIENT';
  };

  const handlePromote = async (username) => {
    const currentRole = getUserRole(username);
    const newRole = currentRole === 'ADMIN' ? 'CLIENT' : 'ADMIN';
    const action = newRole === 'ADMIN' ? 'promote to ADMIN' : 'demote to CLIENT';
    const currentUsername = localStorage.getItem('username');
    
    // Special warning if demoting self
    if (username === currentUsername && newRole === 'CLIENT') {
      if (!window.confirm(
        `WARNING: You are about to demote yourself to CLIENT! ` +
        `You will lose admin privileges and be logged out. Continue?`
      )) return;
    } else {
      if (!window.confirm(`Are you sure you want to ${action} user "${username}"?`)) return;
    }
    
    try {
      await API.put(`/auth/users/${username}/role`, { role: newRole });
      
      // Update local state
      setAuthUsers(authUsers.map(user => 
        user.username === username 
          ? { ...user, role: newRole }
          : user
      ));
      
      // If user demoted themselves, update localStorage and force logout
      if (username === currentUsername) {
        localStorage.setItem('role', newRole);
        
        if (newRole === 'CLIENT') {
          alert('You have been demoted to CLIENT. You will now be logged out.');
          localStorage.clear();
          window.location.href = '/login';
        } else {
          alert('You have been promoted to ADMIN! Page will reload.');
          window.location.reload();
        }
      } else {
        // Show success message for other users
        alert(`User "${username}" has been ${newRole === 'ADMIN' ? 'promoted to ADMIN' : 'demoted to CLIENT'}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update role');
    }
  };


  const handleEdit = (user) => {
    setEditingUser(user);
    setFormData({
      username: user.username,
      email: user.email || '',
      address: user.address || ''
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      await API.put(`/users/${editingUser.id}`, formData);
      
      setUsers(users.map(user => 
        user.id === editingUser.id 
          ? { ...user, ...formData }
          : user
      ));
      
      resetForm();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update user');
    }
  };

  const handleDelete = async (id, username) => {
    if (!window.confirm(`Are you sure you want to delete user "${username}"?`)) return;
    
    try {
      // Delete from users service
      await API.delete(`/users/${id}`);
      
      // Also delete from auth service
      try {
        await API.delete(`/auth/users/${username}`);
      } catch (err) {
        console.error('Failed to delete from auth service:', err);
      }
      
      setUsers(users.filter(user => user.id !== id));
      setAuthUsers(authUsers.filter(user => user.username !== username));
    } catch (err) {
      setError('Failed to delete user');
    }
  };

  const resetForm = () => {
    setFormData({
      username: '',
      email: '',
      address: ''
    });
    setEditingUser(null);
    setShowModal(false);
    setError('');
  };

  if (loading) return (
    <div className="loading-container">
      <div className="spinner"></div>
      <p>Loading users...</p>
    </div>
  );

  if (error && !showModal) return (
    <div className="error-container">
      <div className="error-icon">⚠️</div>
      <p>{error}</p>
    </div>
  );

  return (
    <div className="users-container">
      <div className="page-header">
        <div>
          <h2>👥 All Users</h2>
          <p className="page-subtitle">Manage system users and roles</p>
        </div>
      </div>

      {error && showModal && <div className="error-message">{error}</div>}

      {users.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📭</div>
          <h3>No users found</h3>
          <p>There are no users in the system yet.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="users-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Address</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => {
                const role = getUserRole(user.username);
                return (
                  <tr key={user.id}>
                    <td><strong>{user.username}</strong></td>
                    <td>{user.email}</td>
                    <td>{user.address || <span className="text-muted">N/A</span>}</td>
                    <td>
                      <span className={`role-badge role-${role.toLowerCase()}`}>
                        {role === 'ADMIN' ? '👑' : '👤'} {role}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button 
                          onClick={() => handleEdit(user)} 
                          className="btn-edit-small"
                          title="Edit user"
                        >
                          ✏️ Edit
                        </button>
                        <button 
                          onClick={() => handlePromote(user.username)} 
                          className={`btn-role-small ${role === 'ADMIN' ? 'btn-demote' : 'btn-promote'}`}
                          title={role === 'ADMIN' ? 'Demote to Client' : 'Promote to Admin'}
                        >
                          {role === 'ADMIN' ? '👤 Demote' : '👑 Promote'}
                        </button>
                        <button 
                          onClick={() => handleDelete(user.id, user.username)} 
                          className="btn-delete-small"
                          title="Delete user"
                        >
                          🗑️ Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={resetForm}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>✏️ Edit User</h3>
              <button className="modal-close" onClick={resetForm}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
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
                  placeholder="user@example.com"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                  required
                />
              </div>

              <div className="form-group">
                <label>Address *</label>
                <input
                  type="text"
                  placeholder="User address"
                  value={formData.address}
                  onChange={(e) => setFormData({...formData, address: e.target.value})}
                  required
                />
              </div>

              <div className="modal-actions">
                <button type="submit" className="btn-primary">
                  Update User
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

export default Users;

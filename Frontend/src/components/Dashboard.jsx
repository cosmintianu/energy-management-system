import React from 'react';
import { Link } from 'react-router-dom';
import './Dashboard.css';

function Dashboard() {
  const username = localStorage.getItem('username');
  const role = localStorage.getItem('role');

  return (
    <div className="dashboard">
      <h1>Welcome back, {username}! 👋</h1>
      <p className="subtitle">Manage your microservices from here</p>
      
      <div className="dashboard-cards">
        {role === 'ADMIN' && (
          <Link to="/users" className="card">
            <div className="card-icon">👥</div>
            <h3>Users Management</h3>
            <p>View and manage all users in the system</p>
            <span className="card-badge">Admin Only</span>
          </Link>
        )}
        <Link to="/devices" className="card">
          <div className="card-icon">📱</div>
          <h3>My Devices</h3>
          <p>Manage and monitor your connected devices</p>
        </Link>
        <Link to="/profile" className="card">
          <div className="card-icon">👤</div>
          <h3>My Profile</h3>
          <p>View and update your personal information</p>
        </Link>
      </div>

      <div className="stats">
        <div className="stat-card">
          <div className="stat-icon">🔐</div>
          <div className="stat-info">
            <h4>Role</h4>
            <p>{role}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">⏱️</div>
          <div className="stat-info">
            <h4>Session</h4>
            <p>15 minutes</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;

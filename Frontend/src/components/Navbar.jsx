import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

function Navbar() {
  const username = localStorage.getItem('username');
  const role = localStorage.getItem('role');

  const handleLogout = () => {
    localStorage.clear();
    window.location.replace('/login');
  };

  return (
    <nav className="navbar">
      <div className="nav-brand">⚡ Energy Management System</div>
      <div className="nav-links">
        <Link to="/dashboard">Dashboard</Link>
        {role === 'ADMIN' && <Link to="/users">Users</Link>}
        <Link to="/devices">Devices</Link>
        <Link to="/chat">Chat</Link>
        <Link to="/support">Support</Link>
        <Link to="/notifications">Notifications</Link>
        <Link to="/profile">Profile</Link>
      </div>
      <div className="nav-user">
        <span className="user-info">
          <strong>{username}</strong> <span className="badge">{role}</span>
        </span>
        <button onClick={handleLogout} className="logout-btn">Logout</button>
      </div>
    </nav>
  );
}

export default Navbar;

import React, { useEffect, useState, useRef } from 'react';
import './Chat.css';

function Notifications() {
  const [items, setItems] = useState([]);
  const wsRef = useRef(null);

  useEffect(() => {
    const base = import.meta.env.VITE_WEBSOCKET_URL || '';
    const url = base || `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.hostname}:8000/ws/notifications`;
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => console.log('Notifications websocket open');
    ws.onmessage = (ev) => {
      try {
        const data = JSON.parse(ev.data);
        setItems((s) => [data, ...s].slice(0, 50));
      } catch (e) {
        setItems((s) => [{ payload: ev.data, timestamp: Date.now() }, ...s].slice(0,50));
      }
    };
    ws.onclose = () => console.log('Notifications websocket closed');

    return () => ws.close();
  }, []);

  return (
    <div style={{padding: '12px'}}>
      <h2>Notifications</h2>
      <div className="messages">
        {items.length === 0 && <div className="notification-item">No notifications yet</div>}
        {items.map((it, i) => (
          <div key={i} className="notification-item">
            <div><strong>{it.type || it.payload?.type || 'notification'}</strong></div>
            <div style={{fontSize: '0.9em'}}>{JSON.stringify(it.payload || it)}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Notifications;

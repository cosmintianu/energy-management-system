import React, { useEffect, useState, useRef } from 'react';
import './Chat.css';

function GlobalChat() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const wsRef = useRef(null);

  useEffect(() => {
    const base = import.meta.env.VITE_WEBSOCKET_URL || '';
    const url = base || `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.hostname}:8000/ws/global`;
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => console.log('Global chat websocket open');
    ws.onmessage = (ev) => {
      try {
        const data = JSON.parse(ev.data);
        setMessages((m) => [...m, data]);
      } catch (e) {
        setMessages((m) => [...m, { from: 'server', text: ev.data }]);
      }
    };
    ws.onclose = () => console.log('Global chat websocket closed');

    return () => ws.close();
  }, []);

  function send() {
    if (!input) return;
    const username = localStorage.getItem('username') || 'Anonymous';
    const payload = { type: 'chat', text: input, username };
    wsRef.current?.send(JSON.stringify(payload));
    setMessages((m) => [...m, { username, text: input }]);
    setInput('');
  }

  return (
    <div className="chat-container">
      <h2>Global Chat</h2>
      <div className="messages">
        {messages.map((m, i) => {
          const isMe = (m.username || localStorage.getItem('username')) === (localStorage.getItem('username') || 'Anonymous');
          return (
            <div key={i} className={`message ${isMe ? 'me' : 'other'}`}>
              <div className="meta">{m.username || localStorage.getItem('username') || 'Anonymous'}</div>
              <div className="body">{m.text}</div>
            </div>
          );
        })}
      </div>
      <div className="chat-input">
        <input value={input} onChange={(e) => setInput(e.target.value)} placeholder="Type message..." />
        <button onClick={send}>Send</button>
      </div>
    </div>
  );
}

export default GlobalChat;

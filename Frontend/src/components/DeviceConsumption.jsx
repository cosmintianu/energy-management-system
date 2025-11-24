import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import API from '../api/axios';
import './DeviceConsumption.css';

function DeviceConsumption() {
  const { deviceId } = useParams();
  const navigate = useNavigate();
  const [device, setDevice] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [chartData, setChartData] = useState([]);
  const [chartType, setChartType] = useState('line'); // 'line' or 'bar'
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDevice();
  }, [deviceId]);

  useEffect(() => {
    if (selectedDate) {
      fetchConsumption();
    }
  }, [selectedDate]);

  const fetchDevice = async () => {
    try {
      const response = await API.get(`/devices/${deviceId}`);
      setDevice(response.data);
    } catch (err) {
      setError('Failed to load device details');
    }
  };

  const fetchConsumption = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await API.get(`/monitoring/device/${deviceId}/daily`, {
        params: { date: selectedDate }
      });
      
      // Transform backend data: [{hour: 0, totalEnergy: 1.2}, ...] 
      // to chart format: [{hour: "00:00", energy: 1.2}, ...]
      const formatted = response.data.map(item => ({
        hour: `${String(item.hour).padStart(2, '0')}:00`,
        energy: parseFloat(item.totalEnergy.toFixed(3))
      }));
      
      setChartData(formatted);
    } catch (err) {
      setError('Failed to load consumption data');
    } finally {
      setLoading(false);
    }
  };

  if (!device) return <div className="loading-container"><div className="spinner"></div></div>;

  return (
    <div className="consumption-container">
      <div className="consumption-header">
        <button onClick={() => navigate('/devices')} className="btn-back">← Back to Devices</button>
        <h2>📊 Energy Consumption</h2>
        <div className="device-info-banner">
          <span className="device-name">⚡ {device.name}</span>
          <span className="device-owner">Owner: {device.ownerUsername}</span>
        </div>
      </div>

      <div className="controls-panel">
        <div className="date-picker-group">
          <label>Select Date:</label>
          <input
            type="date"
            value={selectedDate}
            max={new Date().toISOString().split('T')[0]}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="date-input"
          />
        </div>

        <div className="chart-type-toggle">
          <button
            className={chartType === 'line' ? 'active' : ''}
            onClick={() => setChartType('line')}
          >
            📈 Line Chart
          </button>
          <button
            className={chartType === 'bar' ? 'active' : ''}
            onClick={() => setChartType('bar')}
          >
            📊 Bar Chart
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner"></div></div>
      ) : (
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={400}>
            {chartType === 'line' ? (
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="hour" label={{ value: 'Hour of Day', position: 'insideBottom', offset: -5 }} />
                <YAxis label={{ value: 'Energy (kWh)', angle: -90, position: 'insideLeft' }} />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="energy" stroke="#8884d8" strokeWidth={2} name="Energy (kWh)" />
              </LineChart>
            ) : (
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="hour" label={{ value: 'Hour of Day', position: 'insideBottom', offset: -5 }} />
                <YAxis label={{ value: 'Energy (kWh)', angle: -90, position: 'insideLeft' }} />
                <Tooltip />
                <Legend />
                <Bar dataKey="energy" fill="#82ca9d" name="Energy (kWh)" />
              </BarChart>
            )}
          </ResponsiveContainer>

          <div className="summary-stats">
            <div className="stat-card">
              <span className="stat-label">Total Consumption</span>
              <span className="stat-value">
                {chartData.reduce((sum, d) => sum + d.energy, 0).toFixed(2)} kWh
              </span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Peak Hour</span>
              <span className="stat-value">
                {chartData.reduce((max, d) => d.energy > max.energy ? d : max, chartData[0])?.hour || 'N/A'}
              </span>
            </div>
            <div className="stat-card">
              <span className="stat-label">Average per Hour</span>
              <span className="stat-value">
                {(chartData.reduce((sum, d) => sum + d.energy, 0) / 24).toFixed(3)} kWh
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DeviceConsumption;

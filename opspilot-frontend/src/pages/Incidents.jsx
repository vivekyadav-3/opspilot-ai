import React, { useState, useEffect } from 'react';
import { Flame, Users, CheckCircle2, ShieldAlert } from 'lucide-react';
import { fetchIncidents, resolveIncident } from '../api';

export default function Incidents() {
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadIncidents();
  }, []);

  const loadIncidents = async () => {
    try {
      const data = await fetchIncidents();
      setIncidents(data);
    } catch (e) {
      console.error('Failed to load incidents', e);
    } finally {
      setLoading(false);
    }
  };

  const handleResolve = async (id) => {
    await resolveIncident(id);
    await loadIncidents();
  };

  return (
    <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '32px 24px' }}>
      <div style={{ marginBottom: '24px' }}>
        <h1 style={{ fontSize: '22px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Flame size={24} color="#ef4444" />
          Correlated Major Incidents & Outages
        </h1>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
          Autonomous clustering: Multiple related employee tickets aggregated into single high-impact incidents.
        </p>
      </div>

      {incidents.length === 0 ? (
        <div className="glass-panel" style={{ padding: '64px', textAlign: 'center', color: 'var(--text-muted)' }}>
          <ShieldAlert size={40} color="#10b981" style={{ margin: '0 auto 12px' }} />
          <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)' }}>All Systems Operational</h3>
          <p style={{ fontSize: '13px', marginTop: '4px' }}>
            No correlated outages detected. Single tickets are routed normally.
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px' }}>
          {incidents.map((inc) => (
            <div key={inc.id} className="glass-panel" style={{
              padding: '24px',
              borderLeft: inc.status === 'RESOLVED' ? '4px solid #10b981' : '4px solid #ef4444',
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <span className="badge badge-critical">{inc.category} OUTAGE</span>
                <span className={inc.status === 'RESOLVED' ? 'badge badge-success' : 'badge badge-critical'}>
                  {inc.status}
                </span>
              </div>

              <h2 style={{ fontSize: '16px', fontWeight: 700, marginTop: '12px', color: 'var(--text-primary)' }}>
                {inc.title}
              </h2>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '8px', lineHeight: 1.5 }}>
                {inc.description}
              </p>

              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '16px',
                marginTop: '16px',
                paddingTop: '16px',
                borderTop: '1px solid var(--border-color)',
                fontSize: '12px',
                color: 'var(--text-muted)',
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Users size={15} color="#38bdf8" />
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
                    {inc.affectedUsersCount || 1}
                  </span> affected users
                </div>
              </div>

              {inc.status !== 'RESOLVED' && (
                <button
                  onClick={() => handleResolve(inc.id)}
                  style={{
                    width: '100%',
                    marginTop: '16px',
                    padding: '9px',
                    borderRadius: '8px',
                    backgroundColor: 'rgba(16, 185, 129, 0.15)',
                    border: '1px solid rgba(16, 185, 129, 0.3)',
                    color: '#34d399',
                    fontWeight: 600,
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '6px',
                  }}
                >
                  <CheckCircle2 size={15} />
                  <span>Mark Outage Resolved</span>
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

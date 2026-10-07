import React, { useEffect, useState } from 'react';
import { X, Sparkles, Clock, AlertTriangle, CheckCircle2, ArrowUpRight, History } from 'lucide-react';
import { fetchTicketTimeline, updateTicketStatus, escalateTicket } from '../api';

export default function TicketDetailModal({ ticket, onClose, onRefresh }) {
  const [timeline, setTimeline] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (ticket?.id) {
      loadTimeline();
    }
  }, [ticket?.id]);

  const loadTimeline = async () => {
    try {
      const data = await fetchTicketTimeline(ticket.id);
      setTimeline(data);
    } catch (e) {
      console.error('Failed to load timeline', e);
    }
  };

  const handleStatusChange = async (newStatus) => {
    setLoading(true);
    try {
      await updateTicketStatus(ticket.id, newStatus);
      await loadTimeline();
      onRefresh();
    } finally {
      setLoading(false);
    }
  };

  const handleEscalate = async () => {
    setLoading(true);
    try {
      await escalateTicket(ticket.id, 'Escalated by operations dashboard engineer');
      await loadTimeline();
      onRefresh();
    } finally {
      setLoading(false);
    }
  };

  if (!ticket) return null;

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.75)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 50,
      padding: '20px',
    }}>
      <div className="glass-panel" style={{
        width: '100%',
        maxWidth: '820px',
        maxHeight: '90vh',
        overflowY: 'auto',
        padding: '30px',
        backgroundColor: 'var(--bg-secondary)',
      }}>
        {/* Top Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', fontSize: '14px' }}>
                #{ticket.id}
              </span>
              <span className={`badge badge-${ticket.priority?.toLowerCase() || 'medium'}`}>
                {ticket.priority || 'MEDIUM'}
              </span>
              <span className="badge badge-medium">{ticket.status}</span>
            </div>
            <h1 style={{ fontSize: '20px', fontWeight: 800, marginTop: '8px', color: 'var(--text-primary)' }}>
              {ticket.title}
            </h1>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
            <X size={22} />
          </button>
        </div>

        {/* Two-Column Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '24px' }}>
          {/* Left Column: Details & AI Decisions */}
          <div>
            {/* Description Card */}
            <div style={{
              backgroundColor: 'var(--bg-tertiary)',
              padding: '16px',
              borderRadius: '10px',
              border: '1px solid var(--border-color)',
              marginBottom: '20px',
            }}>
              <h3 style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px' }}>
                Problem Description
              </h3>
              <p style={{ fontSize: '13px', lineHeight: 1.6, color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
                {ticket.description}
              </p>
            </div>

            {/* Jev AI Classification Card */}
            <div style={{
              backgroundColor: 'rgba(139, 92, 246, 0.08)',
              border: '1px solid rgba(139, 92, 246, 0.3)',
              borderRadius: '10px',
              padding: '18px',
              marginBottom: '20px',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Sparkles size={17} color="#c084fc" />
                  <span style={{ fontSize: '13px', fontWeight: 700, color: '#c084fc' }}>
                    JEV AI STRUCTURED TRIAGE
                  </span>
                </div>
                {ticket.aiConfidence && (
                  <span style={{
                    fontSize: '11px',
                    backgroundColor: 'rgba(139, 92, 246, 0.2)',
                    color: '#c084fc',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontWeight: 700,
                  }}>
                    {Math.round(ticket.aiConfidence * 100)}% CONFIDENCE
                  </span>
                )}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '12px' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Classified Category:</span>
                  <p style={{ fontWeight: 600, marginTop: '2px' }}>{ticket.category || 'N/A'}</p>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Assigned Team:</span>
                  <p style={{ fontWeight: 600, marginTop: '2px' }}>{ticket.assignedTeamName || 'Auto-Routed'}</p>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Urgency Score:</span>
                  <p style={{ fontWeight: 600, marginTop: '2px', color: '#f59e0b' }}>
                    {ticket.urgencyScore ? `${ticket.urgencyScore} / 100` : 'Calibrating...'}
                  </p>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Escalation Flag:</span>
                  <p style={{ fontWeight: 600, marginTop: '2px', color: ticket.isEscalated ? '#ef4444' : '#10b981' }}>
                    {ticket.isEscalated ? 'ESCALATED' : 'STANDARD'}
                  </p>
                </div>
              </div>
            </div>

            {/* Engineer Action Bar */}
            <div style={{ display: 'flex', gap: '10px' }}>
              {ticket.status !== 'IN_PROGRESS' && ticket.status !== 'RESOLVED' && (
                <button
                  disabled={loading}
                  onClick={() => handleStatusChange('IN_PROGRESS')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '8px',
                    backgroundColor: 'var(--bg-tertiary)',
                    border: '1px solid var(--border-color)',
                    color: '#38bdf8',
                    fontWeight: 600,
                    cursor: 'pointer',
                  }}
                >
                  Start Working
                </button>
              )}

              {ticket.status !== 'RESOLVED' && (
                <button
                  disabled={loading}
                  onClick={() => handleStatusChange('RESOLVED')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '8px',
                    backgroundColor: 'rgba(16, 185, 129, 0.15)',
                    border: '1px solid rgba(16, 185, 129, 0.4)',
                    color: '#34d399',
                    fontWeight: 600,
                    cursor: 'pointer',
                  }}
                >
                  Resolve Incident
                </button>
              )}

              {!ticket.isEscalated && (
                <button
                  disabled={loading}
                  onClick={handleEscalate}
                  style={{
                    padding: '10px 14px',
                    borderRadius: '8px',
                    backgroundColor: 'rgba(239, 68, 68, 0.15)',
                    border: '1px solid rgba(239, 68, 68, 0.4)',
                    color: '#f87171',
                    fontWeight: 600,
                    cursor: 'pointer',
                  }}
                >
                  Escalate
                </button>
              )}
            </div>
          </div>

          {/* Right Column: Audit Timeline */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <History size={16} color="var(--text-muted)" />
              <h3 style={{ fontSize: '13px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
                Lifecycle Timeline
              </h3>
            </div>

            <div style={{
              backgroundColor: 'var(--bg-tertiary)',
              padding: '16px',
              borderRadius: '10px',
              border: '1px solid var(--border-color)',
              maxHeight: '380px',
              overflowY: 'auto',
            }}>
              {timeline.length === 0 ? (
                <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>No events recorded yet.</p>
              ) : (
                <div style={{ position: 'relative', paddingLeft: '16px' }}>
                  {/* Vertical rule line */}
                  <div style={{
                    position: 'absolute',
                    left: '5px',
                    top: '6px',
                    bottom: '6px',
                    width: '2px',
                    backgroundColor: 'var(--border-color)',
                  }} />

                  {timeline.map((evt) => (
                    <div key={evt.id} style={{ position: 'relative', marginBottom: '18px' }}>
                      {/* Timeline dot */}
                      <div style={{
                        position: 'absolute',
                        left: '-15px',
                        top: '4px',
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        backgroundColor: '#06b6d4',
                      }} />
                      <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)' }}>
                        {evt.eventType}
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                        {evt.description}
                      </div>
                      <div style={{ fontSize: '10px', color: 'var(--text-muted)', marginTop: '4px' }}>
                        {evt.performedBy} · {new Date(evt.createdAt).toLocaleTimeString()}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

import React from 'react';
import { Clock, ShieldAlert, Sparkles, ChevronRight, UserCheck } from 'lucide-react';

export default function TicketTable({ tickets, onSelectTicket }) {
  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'CRITICAL': return <span className="badge badge-critical">CRITICAL</span>;
      case 'HIGH': return <span className="badge badge-high">HIGH</span>;
      case 'MEDIUM': return <span className="badge badge-medium">MEDIUM</span>;
      default: return <span className="badge badge-low">{priority || 'LOW'}</span>;
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'RESOLVED':
      case 'CLOSED':
        return <span className="badge badge-success">{status}</span>;
      case 'AI_ANALYZING':
        return <span className="badge badge-ai">AI ANALYZING...</span>;
      case 'NEEDS_HUMAN_REVIEW':
        return <span className="badge badge-high">HUMAN REVIEW</span>;
      default:
        return <span className="badge badge-medium">{status}</span>;
    }
  };

  if (!tickets || tickets.length === 0) {
    return (
      <div className="glass-panel" style={{ padding: '48px', textAlign: 'center', color: 'var(--text-muted)' }}>
        <p style={{ fontSize: '15px' }}>No active tickets found. Click "Report Problem" to submit an issue.</p>
      </div>
    );
  }

  return (
    <div className="glass-panel" style={{ overflow: 'hidden' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)', backgroundColor: 'rgba(26, 32, 46, 0.4)' }}>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>ID</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>INCIDENT TITLE</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>CATEGORY</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>SEVERITY</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>STATUS</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>ASSIGNED TEAM</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>SLA REMAINING</th>
            <th style={{ padding: '12px 18px', fontWeight: 600 }}>AI TRIAGE</th>
            <th style={{ padding: '12px 18px', textAlign: 'right' }}>ACTION</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map((t) => {
            const isBreached = t.slaRemainingMinutes !== null && t.slaRemainingMinutes <= 0;
            const isAtRisk = t.slaRemainingMinutes !== null && t.slaRemainingMinutes > 0 && t.slaRemainingMinutes <= 30;

            return (
              <tr
                key={t.id}
                onClick={() => onSelectTicket(t)}
                style={{
                  borderBottom: '1px solid var(--border-color)',
                  cursor: 'pointer',
                  transition: 'background-color 0.15s ease',
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--bg-tertiary)'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              >
                <td style={{ padding: '14px 18px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                  #{t.id}
                </td>
                <td style={{ padding: '14px 18px' }}>
                  <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{t.title}</div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '2px' }}>
                    Reported by {t.createdByName || 'Employee'}
                  </div>
                </td>
                <td style={{ padding: '14px 18px', color: 'var(--text-secondary)' }}>
                  {t.category ? (
                    <span style={{ fontSize: '12px', fontWeight: 500 }}>{t.category}</span>
                  ) : (
                    <span style={{ color: 'var(--text-muted)' }}>Triage pending</span>
                  )}
                </td>
                <td style={{ padding: '14px 18px' }}>
                  {getPriorityBadge(t.priority)}
                </td>
                <td style={{ padding: '14px 18px' }}>
                  {getStatusBadge(t.status)}
                </td>
                <td style={{ padding: '14px 18px', color: 'var(--text-secondary)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <UserCheck size={14} color="var(--text-muted)" />
                    <span>{t.assignedTeamName || 'Unassigned'}</span>
                  </div>
                </td>
                <td style={{ padding: '14px 18px' }}>
                  {t.slaRemainingMinutes !== null ? (
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '5px',
                      color: isBreached ? '#ef4444' : isAtRisk ? '#f59e0b' : 'var(--text-secondary)',
                      fontWeight: (isBreached || isAtRisk) ? 700 : 500,
                    }}>
                      <Clock size={13} />
                      <span>
                        {isBreached ? 'BREACHED' : `${t.slaRemainingMinutes}m left`}
                      </span>
                    </div>
                  ) : (
                    <span style={{ color: 'var(--text-muted)' }}>-</span>
                  )}
                </td>
                <td style={{ padding: '14px 18px' }}>
                  {t.aiConfidence ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                      <Sparkles size={13} color="#c084fc" />
                      <span style={{ color: '#c084fc', fontWeight: 600 }}>
                        {Math.round(t.aiConfidence * 100)}%
                      </span>
                    </div>
                  ) : (
                    <span style={{ color: 'var(--text-muted)', fontSize: '11px' }}>Analyzing...</span>
                  )}
                </td>
                <td style={{ padding: '14px 18px', textAlign: 'right', color: 'var(--text-muted)' }}>
                  <ChevronRight size={16} />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ShieldAlert, PlusCircle, LayoutDashboard, Flame } from 'lucide-react';

export default function Navbar({ onOpenReportModal, onOpenNewTicket }) {
  const navigate = useNavigate();
  const location = useLocation();
  const isIncidents = location.pathname.includes('/incidents');
  const handleOpen = onOpenReportModal || onOpenNewTicket;

  return (
    <nav style={{
      borderBottom: '1px solid var(--border-color)',
      backgroundColor: 'rgba(10, 13, 20, 0.85)',
      backdropFilter: 'blur(16px)',
      position: 'sticky',
      top: 0,
      zIndex: 40,
      padding: '12px 24px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
    }}>
      {/* Brand */}
      <div 
        style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}
        onClick={() => navigate('/dashboard')}
      >
        <div style={{
          background: 'linear-gradient(135deg, #06b6d4 0%, #8b5cf6 100%)',
          width: '36px',
          height: '36px',
          borderRadius: '10px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 0 16px rgba(6, 182, 212, 0.35)',
        }}>
          <ShieldAlert size={20} color="#fff" />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontWeight: 800, fontSize: '18px', letterSpacing: '-0.5px' }}>OpsPilot</span>
            <span style={{
              fontSize: '10px',
              background: 'rgba(139, 92, 246, 0.2)',
              color: '#c084fc',
              border: '1px solid rgba(139, 92, 246, 0.4)',
              padding: '1px 6px',
              borderRadius: '4px',
              fontWeight: 700,
            }}>JEV AI</span>
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Autonomous IT Incident & Triage</p>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        backgroundColor: 'var(--bg-secondary)',
        padding: '4px',
        borderRadius: '10px',
        border: '1px solid var(--border-color)',
      }}>
        <button
          onClick={() => navigate('/dashboard')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '7px 14px',
            borderRadius: '7px',
            border: 'none',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            backgroundColor: !isIncidents ? 'var(--bg-tertiary)' : 'transparent',
            color: !isIncidents ? 'var(--text-primary)' : 'var(--text-secondary)',
            transition: 'all 0.15s ease',
          }}
        >
          <LayoutDashboard size={16} />
          Overview & Tickets
        </button>

        <button
          onClick={() => navigate('/incidents')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '7px 14px',
            borderRadius: '7px',
            border: 'none',
            fontSize: '13px',
            fontWeight: 600,
            cursor: 'pointer',
            backgroundColor: isIncidents ? 'rgba(239, 68, 68, 0.15)' : 'transparent',
            color: isIncidents ? '#f87171' : 'var(--text-secondary)',
            transition: 'all 0.15s ease',
          }}
        >
          <Flame size={16} />
          Correlated Outages
        </button>
      </div>

      {/* Action CTA */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <button className="btn btn-primary" onClick={handleOpen}>
          <PlusCircle size={17} />
          <span>Report Problem</span>
        </button>
      </div>
    </nav>
  );
}

import React, { useState, useEffect } from 'react';
import { AlertCircle, Clock, CheckCircle2, ShieldCheck, Search, Filter, RefreshCw } from 'lucide-react';
import { fetchDashboardSummary, fetchTickets, createTicket } from '../api';
import TicketTable from '../components/TicketTable';
import TicketFormModal from '../components/TicketFormModal';
import TicketDetailModal from '../components/TicketDetailModal';

export default function Dashboard({ isNewTicketOpen, setIsNewTicketOpen }) {
  const [summary, setSummary] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [loading, setLoading] = useState(true);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  useEffect(() => {
    loadData();
    const handleCreated = () => loadData();
    window.addEventListener('ticket-created', handleCreated);
    // Auto-poll every 5 seconds for real-time ticket state progression
    const interval = setInterval(loadData, 5000);
    return () => {
      clearInterval(interval);
      window.removeEventListener('ticket-created', handleCreated);
    };
  }, []);

  const loadData = async () => {
    try {
      const [sumData, tickData] = await Promise.all([
        fetchDashboardSummary(),
        fetchTickets(),
      ]);
      setSummary(sumData);
      setTickets(tickData);
    } catch (e) {
      console.error('Failed to load dashboard data', e);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTicket = async (ticketData) => {
    await createTicket(ticketData);
    // Reload right away to show ticket entering AI_ANALYZING state
    await loadData();
  };

  // Filtered tickets
  const filteredTickets = tickets.filter((t) => {
    const matchesSearch = t.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          t.id.toString().includes(searchTerm);
    const matchesPriority = priorityFilter === 'ALL' || t.priority === priorityFilter;
    const matchesCategory = categoryFilter === 'ALL' || t.category === categoryFilter;

    return matchesSearch && matchesPriority && matchesCategory;
  });

  return (
    <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '32px 24px' }}>
      {/* Top KPI Cards Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: '16px',
        marginBottom: '32px',
      }}>
        {/* Card 1: Open Incidents */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase' }}>Active Tickets</span>
            <AlertCircle size={18} color="#38bdf8" />
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, marginTop: '8px', color: 'var(--text-primary)' }}>
            {summary?.openTickets ?? 0}
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Currently unaddressed</p>
        </div>

        {/* Card 2: Critical */}
        <div className="glass-panel" style={{ padding: '20px', borderLeft: '3px solid #ef4444' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase' }}>Critical Severity</span>
            <AlertCircle size={18} color="#ef4444" />
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, marginTop: '8px', color: '#f87171' }}>
            {summary?.criticalIncidents ?? 0}
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Immediate escalation active</p>
        </div>

        {/* Card 3: SLA At Risk */}
        <div className="glass-panel" style={{ padding: '20px', borderLeft: '3px solid #f59e0b' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase' }}>SLA At Risk / Breached</span>
            <Clock size={18} color="#f59e0b" />
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, marginTop: '8px', color: '#fbbf24' }}>
            {summary?.slaAtRiskTickets ?? 0}
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>&lt; 30 minutes remaining</p>
        </div>

        {/* Card 4: Resolved */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase' }}>Resolved</span>
            <CheckCircle2 size={18} color="#10b981" />
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, marginTop: '8px', color: '#34d399' }}>
            {summary?.resolvedTickets ?? 0}
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Closed tickets</p>
        </div>
      </div>

      {/* Filter / Search Bar */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '16px',
        marginBottom: '20px',
      }}>
        {/* Search */}
        <div style={{
          position: 'relative',
          flex: '1',
          maxWidth: '360px',
        }}>
          <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '12px' }} />
          <input
            type="text"
            placeholder="Search tickets by title or ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: '9px 14px 9px 38px',
              borderRadius: '8px',
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              fontSize: '13px',
              outline: 'none',
            }}
          />
        </div>

        {/* Filter dropdowns */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            style={{
              padding: '9px 12px',
              borderRadius: '8px',
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              fontSize: '13px',
              outline: 'none',
            }}
          >
            <option value="ALL">All Severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            style={{
              padding: '9px 12px',
              borderRadius: '8px',
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              fontSize: '13px',
              outline: 'none',
            }}
          >
            <option value="ALL">All Categories</option>
            <option value="NETWORK">Network</option>
            <option value="DATABASE">Database</option>
            <option value="SOFTWARE">Software</option>
            <option value="SECURITY">Security</option>
            <option value="ACCOUNT_ACCESS">Account Access</option>
          </select>

          <button
            onClick={loadData}
            style={{
              padding: '9px 12px',
              borderRadius: '8px',
              backgroundColor: 'var(--bg-secondary)',
              border: '1px solid var(--border-color)',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <RefreshCw size={14} />
          </button>
        </div>
      </div>

      {/* Ticket Table */}
      <TicketTable tickets={filteredTickets} onSelectTicket={setSelectedTicket} />

      {/* Modals */}
      <TicketFormModal
        isOpen={isNewTicketOpen}
        onClose={() => setIsNewTicketOpen(false)}
        onSubmit={handleCreateTicket}
      />

      <TicketDetailModal
        ticket={selectedTicket}
        onClose={() => setSelectedTicket(null)}
        onRefresh={loadData}
      />
    </div>
  );
}

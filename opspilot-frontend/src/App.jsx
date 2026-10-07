import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Incidents from './pages/Incidents.jsx';
import TicketFormModal from './components/TicketFormModal.jsx';
import { createTicket } from './api';

export default function App() {
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);

  const handleGlobalCreateTicket = async (formData) => {
    try {
      await createTicket(formData);
      // Trigger a custom event to notify active pages to refresh immediately
      window.dispatchEvent(new CustomEvent('ticket-created'));
    } catch (err) {
      console.error('Failed to create ticket', err);
    }
  };

  return (
    <BrowserRouter>
      <div style={{ minHeight: '100vh', backgroundColor: 'var(--bg-primary)' }}>
        <Navbar onOpenReportModal={() => setIsReportModalOpen(true)} />
        <main style={{ paddingTop: '64px' }}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/incidents" element={<Incidents />} />
          </Routes>
        </main>
        <TicketFormModal
          isOpen={isReportModalOpen}
          onClose={() => setIsReportModalOpen(false)}
          onSubmit={handleGlobalCreateTicket}
        />
      </div>
    </BrowserRouter>
  );
}

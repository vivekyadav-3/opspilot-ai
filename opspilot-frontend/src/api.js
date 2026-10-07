import axios from 'axios';

const API_BASE = '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const fetchDashboardSummary = async () => {
  const res = await api.get('/dashboard/summary');
  return res.data;
};

export const fetchTickets = async () => {
  const res = await api.get('/tickets');
  return res.data;
};

export const fetchTicketById = async (id) => {
  const res = await api.get(`/tickets/${id}`);
  return res.data;
};

export const createTicket = async (ticketData, userId = 1) => {
  const res = await api.post(`/tickets?userId=${userId}`, ticketData);
  return res.data;
};

export const updateTicketStatus = async (id, status, performedBy = 'SUPPORT_AGENT') => {
  const res = await api.patch(`/tickets/${id}/status`, { status, performedBy });
  return res.data;
};

export const escalateTicket = async (id, reason = 'Urgent customer impact') => {
  const res = await api.post(`/tickets/${id}/escalate`, { reason });
  return res.data;
};

export const fetchTicketTimeline = async (id) => {
  const res = await api.get(`/tickets/${id}/timeline`);
  return res.data;
};

export const fetchIncidents = async () => {
  const res = await api.get('/incidents');
  return res.data;
};

export const resolveIncident = async (id) => {
  const res = await api.post(`/incidents/${id}/resolve`);
  return res.data;
};

export const login = async (email, password) => {
  const res = await api.post('/auth/login', { email, password });
  return res.data;
};

export default api;

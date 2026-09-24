import axios from 'axios';
import type { Event, CreateEventRequest, Ticket, ClaimTicketRequest, GateValidateRequest, GateValidateResponse, UserAccount } from '../types';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
});

const TOKEN_KEY = 'securetix_admin_session';
let authToken = sessionStorage.getItem(TOKEN_KEY) || '';
const setAuthToken = (token: string) => {
  authToken = token;
  sessionStorage.setItem(TOKEN_KEY, token);
};
export const clearAuthToken = () => {
  authToken = '';
  sessionStorage.removeItem(TOKEN_KEY);
};
apiClient.interceptors.request.use((config) => {
  if (authToken) config.headers.set('Authorization', `Bearer ${authToken}`);
  return config;
});
apiClient.interceptors.response.use((response) => response, (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 401 && authToken) {
    clearAuthToken();
    window.dispatchEvent(new Event('securetix-session-expired'));
  }
  return Promise.reject(error);
});

interface AuthResponse {
  token: string;
  roles: string[];
}

export const loginAdmin = async (username: string, password: string): Promise<void> => {
  clearAuthToken();
  const { data } = await apiClient.post<AuthResponse>('/auth/login', { username, password });
  if (!data.roles.includes('ADMIN')) {
    try {
      await apiClient.post('/auth/logout', {}, { headers: { Authorization: `Bearer ${data.token}` } });
    } catch { /* access remains denied even if revocation cannot reach the server */ }
    throw new Error('Tài khoản chưa được cấp quyền quản trị.');
  }
  setAuthToken(data.token);
};

export const restoreAdminSession = async (): Promise<boolean> => {
  if (!authToken) return false;
  try {
    const { data } = await apiClient.get<{ roles: string[] }>('/auth/me');
    if (data.roles.includes('ADMIN')) return true;
  } catch { /* expired or revoked session */ }
  clearAuthToken();
  return false;
};

export const logoutAdmin = async (): Promise<void> => {
  const token = authToken;
  clearAuthToken();
  try {
    if (token) await apiClient.post('/auth/logout', {}, { headers: { Authorization: `Bearer ${token}` } });
  } catch { /* the local session has already been removed */ }
};

export const getApiErrorMessage = (error: unknown, fallback = 'Đã có lỗi xảy ra'): string => {
  if (axios.isAxiosError(error)) {
    if (!error.response) return 'Không kết nối được Backend.';
    const data = error.response.data as { message?: string; detail?: string; error?: string } | undefined;
    return data?.message || data?.detail || data?.error || `Lỗi máy chủ (${error.response.status})`;
  }
  return error instanceof Error ? error.message : fallback;
};

export const fetchEvents = async (): Promise<Event[]> => {
  const { data } = await apiClient.get<Event[]>('/events');
  return data;
};

export const setEventCheckInEnabled = async (eventId: string, enabled: boolean): Promise<Event> => {
  const { data } = await apiClient.put<Event>(`/events/${encodeURIComponent(eventId)}/check-in`, { enabled });
  return data;
};

export const createEvent = async (eventData: CreateEventRequest): Promise<Event> => {
  const { data } = await apiClient.post<Event>('/events', eventData);
  return data;
};

interface BackendUserDto {
  id: string;
  username: string;
  email: string | null;
  displayName: string | null;
  avatarUrl: string | null;
  roles: string[];
}

export const fetchUsers = async (): Promise<UserAccount[]> => {
  const { data } = await apiClient.get<BackendUserDto[]>('/admin/users');
  return data.map((user) => ({
    id: user.id,
    username: user.username,
    name: user.displayName || user.username,
    email: user.email || '',
    role: user.roles.includes('ADMIN') ? 'ADMIN' as const : 'USER' as const,
    createdAt: '',
  }));
};

interface BackendTicketDto {
  ticketId: string;
  eventId: string;
  eventName: string;
  attendeeName: string;
  categoryName: string;
  seatNumber: string;
  status: string;
  startDateTime?: string;
}

const mapTicket = (ticket: BackendTicketDto, userId: string): Ticket => ({
  id: ticket.ticketId,
  eventId: ticket.eventId,
  eventName: ticket.eventName,
  userId,
  attendeeName: ticket.attendeeName,
  categoryName: ticket.categoryName,
  seatNumber: ticket.seatNumber,
  status: ticket.status,
  issuedAt: ticket.startDateTime,
});

export const claimTicket = async (eventId: string, userId: string, claimData: ClaimTicketRequest): Promise<Ticket> => {
  const { data } = await apiClient.post<BackendTicketDto>('/admin/tickets', { eventId, userId, ...claimData });
  return mapTicket(data, userId);
};

export const fetchUserTickets = async (userId: string): Promise<Ticket[]> => {
  const { data } = await apiClient.get<BackendTicketDto[]>(`/admin/tickets/users/${encodeURIComponent(userId)}`);
  return data.map((ticket) => mapTicket(ticket, userId));
};

export const validateGateQr = async (gateId: string, payload: GateValidateRequest): Promise<GateValidateResponse> => {
  const { data } = await apiClient.post<GateValidateResponse>(`/gates/${encodeURIComponent(gateId)}/validate`, payload);
  return data;
};

export const checkHealth = async (): Promise<boolean> => {
  try { await apiClient.get('/events', { timeout: 3000 }); return true; }
  catch { return false; }
};

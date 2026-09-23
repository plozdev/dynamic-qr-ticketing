import axios, { AxiosError } from 'axios';
import type { 
  Event, 
  CreateEventRequest, 
  Ticket, 
  ClaimTicketRequest, 
  GateValidateRequest, 
  GateValidateResponse, 
  UserAccount 
} from '../types';


export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Helper for extracting API error messages
export const getApiErrorMessage = (error: unknown, fallback: string = 'Đã có lỗi xảy ra'): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string; error?: string; detail?: string; reason?: string }>;
    if (axiosError.response?.data) {
      const data = axiosError.response.data;
      return data.message || data.error || data.reason || data.detail || `Lỗi máy chủ (${axiosError.response.status})`;
    }
    if (axiosError.code === 'ERR_NETWORK') {
      return 'Không thể kết nối đến máy chủ Backend (http://localhost:8080). Vui lòng kiểm tra Spring Boot!';
    }
    return axiosError.message || fallback;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return fallback;
};

// 1. Events APIs
export const fetchEvents = async (): Promise<Event[]> => {
  const response = await apiClient.get('/events');
  // Handle various Spring Boot response shapes (Page, List, Result wrapper)
  if (Array.isArray(response.data)) {
    return response.data;
  }
  if (response.data && Array.isArray(response.data.content)) {
    return response.data.content;
  }
  if (response.data && Array.isArray(response.data.data)) {
    return response.data.data;
  }
  return [];
};

export const createEvent = async (eventData: CreateEventRequest): Promise<Event> => {
  const response = await apiClient.post<Event>('/events', eventData);
  return response.data;
};

// 2. Ticket Assignment APIs
export const claimTicket = async (
  eventId: string, 
  userId: string, 
  claimData: ClaimTicketRequest
): Promise<Ticket> => {
  const response = await apiClient.post<Ticket>(
    `/events/${eventId}/claim`, 
    claimData, 
    { params: { userId } }
  );
  return response.data;
};

export const fetchUserTickets = async (userId: string): Promise<Ticket[]> => {
  const response = await apiClient.get('/tickets', { params: { userId } });
  if (Array.isArray(response.data)) {
    return response.data;
  }
  if (response.data && Array.isArray(response.data.content)) {
    return response.data.content;
  }
  if (response.data && Array.isArray(response.data.data)) {
    return response.data.data;
  }
  return [];
};

// 3. Gate Validator APIs
export const validateGateQr = async (
  gateId: string, 
  payload: GateValidateRequest
): Promise<GateValidateResponse> => {
  const response = await apiClient.post<GateValidateResponse>(
    `/gates/${gateId}/validate`, 
    payload
  );
  return response.data;
};

// 4. Health Check
export const checkHealth = async (): Promise<boolean> => {
  try {
    await apiClient.get('/events', { timeout: 3000 });
    return true;
  } catch (error) {
    // If we received an HTTP response (even 4xx/5xx or 401), the server is running
    if (axios.isAxiosError(error) && error.response) {
      return true;
    }
    return false;
  }
};

export interface BackendUserDto {
  id: string;
  firebaseUid?: string;
  email?: string;
  displayName?: string;
  avatarUrl?: string;
}

// 5. User Account APIs (Real Backend Integration)
export const fetchUsers = async (): Promise<UserAccount[]> => {
  const response = await apiClient.get<BackendUserDto[]>('/users');
  const backendList = Array.isArray(response.data) ? response.data : [];
  return backendList.map((u) => ({
    id: u.id,
    name: u.displayName || (u.email ? u.email.split('@')[0] : 'Khán Giả'),
    email: u.email || '',
    phone: '',
    role: 'USER',
    isDemoAppUser: u.id === '11111111-2222-3333-4444-555555555555',
    createdAt: new Date().toISOString(),
  }));
};

export const saveUser = async (user: UserAccount): Promise<UserAccount> => {
  const response = await apiClient.post<BackendUserDto>('/users', {
    id: user.id,
    email: user.email,
    displayName: user.name,
    avatarUrl: '',
  });
  const u = response.data;
  return {
    id: u.id,
    name: u.displayName || (u.email ? u.email.split('@')[0] : 'Khán Giả'),
    email: u.email || '',
    phone: user.phone || '',
    role: user.role || 'USER',
    isDemoAppUser: false,
    createdAt: new Date().toISOString(),
  };
};

export const deleteUser = async (userId: string): Promise<void> => {
  await apiClient.delete(`/users/${userId}`);
};


export interface Event {
  id: string;
  name: string;
  venueName: string;
  venueAddress?: string;
  venueGates: string[];
  description?: string;
  startDateTime: string;
  endDateTime: string;
  status?: string;
  totalCapacity?: number;
  availableTickets?: number;
  createdAt?: string;
}

export interface CreateEventRequest {
  name: string;
  venueName: string;
  venueAddress: string;
  venueGates: string[];
  description: string;
  startDateTime: string;
  endDateTime: string;
}

export interface Ticket {
  id: string;
  eventId: string;
  eventName?: string;
  userId: string;
  attendeeName: string;
  categoryName: string;
  seatNumber: string;
  status: 'READY_TO_CHECK_IN' | 'CHECKED_IN' | 'NOT_YET_CHECK_IN' | string;
  qrPayload?: string;
  issuedAt?: string;
  checkedInAt?: string;
  gateId?: string;
}

export interface ClaimTicketRequest {
  categoryName: string;
  seatNumber: string;
  attendeeName: string;
}

export interface GateValidateRequest {
  rawQrPayload: string;
}

export interface GateValidateResponse {
  valid?: boolean;
  success?: boolean;
  message?: string;
  ticketId?: string;
  eventId?: string;
  eventName?: string;
  attendeeName?: string;
  seatNumber?: string;
  categoryName?: string;
  checkInTime?: string;
  gateId?: string;
  reason?: string;
  errorCode?: string;
}

export interface UserAccount {
  id: string;
  name: string;
  email: string;
  phone?: string;
  role: 'USER' | 'ADMIN';
  isDemoAppUser?: boolean;
  createdAt: string;
}

export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'info' | 'warning';
  title: string;
  description?: string;
  duration?: number;
}

export interface GateScanHistoryItem {
  id: string;
  timestamp: string;
  gateId: string;
  rawPayload: string;
  success: boolean;
  ticketId?: string;
  message: string;
  reason?: string;
}

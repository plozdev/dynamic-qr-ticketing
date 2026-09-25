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
  checkInEnabled?: boolean;
  totalCapacity?: number;
  availableTickets?: number;
  totalTickets?: number;
  basePrice?: number;
  bannerUrl?: string | null;
  category?: string;
  isHotTrend?: boolean;
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
  publishNow?: boolean;
  basePrice?: number;
  bannerUrl?: string;
  totalTickets?: number;
}

export interface Ticket {
  id: string;
  eventId: string;
  eventName?: string;
  userId: string;
  attendeeName: string;
  categoryName: string;
  seatNumber: string;
  status: "READY_TO_CHECK_IN" | "CHECKED_IN" | "NOT_YET_CHECK_IN" | string;
  qrPayload?: string;
  issuedAt?: string;
  checkedInAt?: string;
  gateId?: string;
  gateInfo?: string;
  venueName?: string;
  endDateTime?: string;
  isCheckInOpen?: boolean;
  checkInNote?: string;
}

export interface AuthSession {
  userId: string;
  username: string;
  displayName: string;
  email: string;
  roles: string[];
}

export interface DynamicQr {
  ticketId: string;
  dynamicPayload: string;
  expiresAtEpochSeconds: number;
  refreshIntervalSeconds: number;
}

export interface GateDashboard {
  eventId: string;
  issued: number;
  checkedIn: number;
  replayAlerts: number;
  gates: {
    gateId: string;
    granted: number;
    scansPerMinute: number;
    denied: number;
  }[];
  activity: {
    ticketId: string;
    gateId: string;
    status: string;
    scannedAt: string;
  }[];
  refreshedAt: string;
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
  username?: string;
  name: string;
  email: string;
  phone?: string;
  role: "USER" | "ADMIN";
  createdAt: string;
}

export interface ToastMessage {
  id: string;
  type: "success" | "error" | "info" | "warning";
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

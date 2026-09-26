import React from 'react';
import type { Event, Ticket, UserAccount } from '../../types';
import { IssueTicketForm } from './IssueTicketForm';
import { UserTicketsList } from './UserTicketsList';
import { ShieldAlert } from 'lucide-react';


interface TicketsTabProps {
  events: Event[];
  selectedEventId: string;
  setSelectedEventId: (id: string) => void;
  userId: string;
  setUserId: (id: string) => void;
  userTickets: Ticket[];
  loadingTickets: boolean;
  onRefreshTickets: (targetUserId?: string) => void;
  availableUsers: UserAccount[];
  onTestAtGate?: (ticket: Ticket) => void;
}

export const TicketsTab: React.FC<TicketsTabProps> = ({
  events,
  selectedEventId,
  setSelectedEventId,
  userId,
  setUserId,
  userTickets,
  loadingTickets,
  onRefreshTickets,
  availableUsers,
  onTestAtGate,
}) => {
  return (
    <div className="space-y-6">
      {/* Notice Banner: Temporary Direct Assignment Flow */}
      <div className="rounded-2xl bg-gradient-to-r from-amber-500/10 via-[#121826] to-[#121826] border border-amber-500/30 p-4 shadow-lg flex items-start gap-3.5">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-500/20 text-amber-400 border border-amber-500/40 shrink-0 mt-0.5">
          <ShieldAlert className="h-5 w-5" />
        </div>
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <h3 className="text-sm font-bold text-amber-300">
              Cấp Vé Trực Tiếp Cho Tài Khoản Đã Đăng Ký
            </h3>
            <span className="rounded bg-amber-500/20 px-2 py-0.5 text-[10px] font-mono font-bold text-amber-300 border border-amber-500/30">
              TẠM THỜI
            </span>
          </div>
          <p className="text-xs text-slate-300 mt-1 leading-relaxed">
            Hiện tại hệ thống đang sử dụng cơ chế Admin gán vé trực tiếp cho tài khoản người dùng để phục vụ quá trình thử nghiệm ứng dụng di động Android (sinh mã Dynamic QR 30s) và cổng soát vé. Sau khi hoàn tất cổng thanh toán trực tuyến, người dùng sẽ tự chủ động chọn chỗ và mua vé trên ứng dụng.
          </p>
        </div>
      </div>

      {/* Main Grid: Left Form - Right Ticket List */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Issue Ticket Form (5 cols) */}
        <div className="lg:col-span-5">
          <IssueTicketForm
            events={events}
            selectedEventId={selectedEventId}
            setSelectedEventId={setSelectedEventId}
            userId={userId}
            setUserId={setUserId}
            availableUsers={availableUsers}
            onTicketIssued={(uId) => onRefreshTickets(uId)}
          />
        </div>

        {/* Right Column: User Tickets List (7 cols) */}
        <div className="lg:col-span-7">
          <UserTicketsList
            userId={userId}
            setUserId={setUserId}
            tickets={userTickets}
            events={events}
            loading={loadingTickets}
            onRefresh={onRefreshTickets}
            onTestAtGate={onTestAtGate}
            availableUsers={availableUsers}
          />

        </div>
      </div>
    </div>
  );
};

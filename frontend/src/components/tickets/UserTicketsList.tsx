import React, { useState } from 'react';
import type { Ticket, Event, UserAccount } from '../../types';
import { 
  Ticket as TicketIcon, 
  RefreshCw, 
  Copy, 
  Check, 
  ScanLine, 
  User, 
  Armchair, 
  CheckCircle2, 
  Clock, 
  Smartphone, 
  ChevronDown, 
  ChevronUp 
} from 'lucide-react';
import { useToast } from '../../context/ToastContext';

interface UserTicketsListProps {
  userId: string;
  setUserId: (id: string) => void;
  tickets: Ticket[];
  events: Event[];
  loading: boolean;
  onRefresh: (targetUserId?: string) => void;
  onTestAtGate?: (ticket: Ticket) => void;
  availableUsers?: UserAccount[];
}

export const UserTicketsList: React.FC<UserTicketsListProps> = ({
  userId,
  setUserId,
  tickets,
  events,
  loading,
  onRefresh,
  onTestAtGate,
  availableUsers = [],
}) => {
  const { success } = useToast();
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [showManualUuid, setShowManualUuid] = useState(false);

  // Find currently selected user object
  const selectedUser = availableUsers.find((u) => u.id === userId);

  const handleCopy = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(id);
    setCopiedId(id);
    success('Đã sao chép Ticket ID!', id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const getEventName = (ticket: Ticket) => {
    if (ticket.eventName) return ticket.eventName;
    const found = events.find((ev) => ev.id === ticket.eventId);
    return found ? found.name : `Sự kiện #${ticket.eventId.substring(0, 8)}...`;
  };

  const renderStatusBadge = (status: string) => {
    switch (status) {
      case 'READY_TO_CHECK_IN':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-[#00e599]/15 border border-[#00e599]/40 px-2.5 py-0.5 text-[10px] font-bold text-[#00e599] shadow-[0_0_10px_-2px_rgba(0,229,153,0.3)]">
            <span className="h-1.5 w-1.5 rounded-full bg-[#00e599] animate-pulse" />
            SẴN SÀNG SOÁT VÉ
          </span>
        );
      case 'CHECKED_IN':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-slate-800 border border-slate-700 px-2.5 py-0.5 text-[10px] font-bold text-slate-400">
            <CheckCircle2 className="h-3 w-3 text-slate-500" />
            ĐÃ QUA CỔNG
          </span>
        );
      case 'NOT_YET_CHECK_IN':
      default:
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-amber-500/15 border border-amber-500/40 px-2.5 py-0.5 text-[10px] font-bold text-amber-400">
            <Clock className="h-3 w-3 text-amber-400" />
            CHƯA MỞ CỔNG
          </span>
        );
    }
  };

  return (
    <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-6 shadow-xl flex flex-col h-full">
      {/* Header & User Selector */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4 pb-4 border-b border-[#1f293d]">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <TicketIcon className="h-5 w-5 text-[#00d2ff]" />
            <span>Danh Sách Vé Của Người Dùng</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Xem các vé đang nằm trong ví vé của khách hàng
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => onRefresh()}
            disabled={loading}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1f293d]/80 hover:bg-[#1f293d] text-slate-300 hover:text-white text-xs font-medium transition-colors"
            title="Tải lại danh sách vé"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin text-[#00d2ff]' : ''}`} />
            <span>Làm mới vé</span>
          </button>
        </div>
      </div>

      {/* User Selection Dropdown (No UUID typing needed) */}
      <div className="space-y-3 mb-4">
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            Chọn tài khoản để xem vé:
          </label>
          <select
            value={userId}
            onChange={(e) => {
              setUserId(e.target.value);
              onRefresh(e.target.value);
            }}
            className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] px-3.5 py-2.5 text-xs text-white"
          >
            {availableUsers.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name} {u.isDemoAppUser ? '(📱 Demo App)' : ''} — {u.email}
              </option>
            ))}
          </select>
        </div>

        {/* Selected User Summary Banner */}
        {selectedUser && (
          <div className="rounded-xl bg-[#090d16]/80 border border-[#1f293d] p-3 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-lg bg-[#00d2ff]/20 text-[#00d2ff] border border-[#00d2ff]/30 flex items-center justify-center font-bold text-xs">
                {selectedUser.name.charAt(0).toUpperCase()}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-white">{selectedUser.name}</span>
                  {selectedUser.isDemoAppUser && (
                    <span className="text-[10px] text-[#00e599] font-medium flex items-center gap-0.5">
                      <Smartphone className="h-3 w-3" /> App Demo
                    </span>
                  )}
                </div>
                <div className="text-[11px] text-slate-400">
                  {selectedUser.email} {selectedUser.phone ? `• ${selectedUser.phone}` : ''}
                </div>
              </div>
            </div>

            <div className="text-right">
              <span className="rounded-full bg-[#00e599]/10 border border-[#00e599]/30 px-2.5 py-1 text-[11px] font-bold text-[#00e599]">
                {tickets.length} vé trong ví
              </span>
            </div>
          </div>
        )}

        {/* Optional Collapsible Manual UUID (Hidden by default) */}
        <div>
          <button
            type="button"
            onClick={() => setShowManualUuid(!showManualUuid)}
            className="text-[10px] text-slate-500 hover:text-slate-300 flex items-center gap-1 transition-colors"
          >
            <span>Tùy chọn: Nhập mã UUID tra cứu khác</span>
            {showManualUuid ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
          </button>
          {showManualUuid && (
            <div className="mt-1.5 flex gap-2">
              <input
                type="text"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="Nhập UUID cần tra cứu..."
                className="flex-1 rounded-lg bg-[#090d16] border border-[#1f293d] px-3 py-1.5 text-[11px] font-mono text-slate-300"
              />
              <button
                type="button"
                onClick={() => onRefresh(userId)}
                className="px-3 py-1.5 rounded-lg bg-[#1f293d] hover:bg-slate-700 text-xs text-white"
              >
                Tra cứu
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Tickets List Container */}
      <div className="flex-1 overflow-y-auto space-y-3 min-h-[250px] max-h-[500px] pr-1">
        {loading ? (
          <div className="space-y-3 py-6">
            {[1, 2].map((n) => (
              <div
                key={n}
                className="h-28 rounded-xl bg-[#090d16] border border-[#1f293d] animate-pulse p-4"
              />
            ))}
          </div>
        ) : tickets.length > 0 ? (
          tickets.map((ticket) => (
            <div
              key={ticket.id}
              className="group rounded-xl bg-[#090d16] border border-[#1f293d] hover:border-[#00d2ff]/40 p-4 transition-all duration-200 flex flex-col justify-between gap-3 shadow-md"
            >
              {/* Event Name & Status */}
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h4 className="text-sm font-bold text-white group-hover:text-[#00d2ff] transition-colors leading-tight">
                    {getEventName(ticket)}
                  </h4>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="rounded bg-[#1f293d] px-2 py-0.5 text-[10px] font-semibold text-[#00e599] border border-slate-700">
                      {ticket.categoryName}
                    </span>
                    <span className="text-[11px] font-mono text-slate-400 flex items-center gap-1">
                      <Armchair className="h-3 w-3 text-slate-500" />
                      {ticket.seatNumber}
                    </span>
                  </div>
                </div>
                {renderStatusBadge(ticket.status)}
              </div>

              {/* Attendee Info */}
              <div className="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-[#1f293d]/60">
                <div className="flex items-center gap-1.5">
                  <User className="h-3.5 w-3.5 text-slate-500" />
                  <span className="text-slate-300 font-medium">{ticket.attendeeName}</span>
                </div>
                <div className="text-[11px] text-slate-500 font-mono">
                  {ticket.checkedInAt ? `Vào cổng: ${new Date(ticket.checkedInAt).toLocaleTimeString('vi-VN')}` : 'Chưa check-in'}
                </div>
              </div>

              {/* UUID & Action Buttons */}
              <div className="flex items-center justify-between gap-2 pt-1">
                <div className="flex items-center gap-1.5 text-[11px] font-mono text-slate-500 truncate max-w-[200px]">
                  <span>Mã vé:</span>
                  <span className="truncate" title={ticket.id}>
                    {ticket.id}
                  </span>
                </div>

                <div className="flex items-center gap-1.5 shrink-0">
                  <button
                    type="button"
                    onClick={(e) => handleCopy(ticket.id, e)}
                    className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#121826] hover:bg-[#1f293d] border border-[#1f293d] text-slate-300 hover:text-white text-[11px] font-medium transition-colors"
                    title="Sao chép mã Ticket ID"
                  >
                    {copiedId === ticket.id ? (
                      <>
                        <Check className="h-3 w-3 text-[#00e599]" />
                        <span className="text-[#00e599]">Đã chép</span>
                      </>
                    ) : (
                      <>
                        <Copy className="h-3 w-3 text-slate-400" />
                        <span>Chép mã</span>
                      </>
                    )}
                  </button>

                  {onTestAtGate && (
                    <button
                      type="button"
                      onClick={() => onTestAtGate(ticket)}
                      className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#00d2ff]/10 hover:bg-[#00d2ff]/20 border border-[#00d2ff]/30 text-[#00d2ff] hover:text-white text-[11px] font-medium transition-colors"
                      title="Chuyển sang Giả Lập Soát Vé để test vé này"
                    >
                      <ScanLine className="h-3 w-3" />
                      <span>Test tại cổng</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="h-48 rounded-xl border border-dashed border-[#1f293d] flex flex-col items-center justify-center p-6 text-center">
            <TicketIcon className="h-8 w-8 text-slate-600 mb-2" />
            <p className="text-xs font-semibold text-slate-400">
              {selectedUser ? `${selectedUser.name} chưa có vé nào` : 'Người dùng này chưa có vé nào'}
            </p>
            <p className="text-[11px] text-slate-500 mt-1 max-w-xs">
              Sử dụng form bên trái để cấp vé mới cho người dùng này và xem vé xuất hiện trên app Android.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

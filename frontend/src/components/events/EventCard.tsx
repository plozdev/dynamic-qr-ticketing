import React, { useState } from 'react';
import type { Event } from '../../types';
import { MapPin, Copy, Check, Ticket, DoorOpen, Clock } from 'lucide-react';

import { useToast } from '../../context/ToastContext';

interface EventCardProps {
  event: Event;
  onSelectForTicket?: (eventId: string) => void;
  onToggleCheckIn?: (event: Event) => void;
  checkInUpdating?: boolean;
}

export const EventCard: React.FC<EventCardProps> = ({ event, onSelectForTicket, onToggleCheckIn, checkInUpdating }) => {
  const { success } = useToast();
  const [copied, setCopied] = useState(false);

  const handleCopyId = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(event.id);
    setCopied(true);
    success('Đã sao chép Event ID!', event.id);
    setTimeout(() => setCopied(false), 2000);
  };

  const formatDate = (isoString?: string) => {
    if (!isoString) return 'Chưa xác định';
    try {
      const date = new Date(isoString);
      return new Intl.DateTimeFormat('vi-VN', {
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date);
    } catch {
      return isoString;
    }
  };

  return (
    <div className="group relative rounded-2xl bg-[#121826] border border-[#1f293d] hover:border-[#00e599]/40 transition-all duration-300 p-5 flex flex-col justify-between shadow-lg hover:shadow-[0_0_25px_-5px_rgba(0,229,153,0.15)] overflow-hidden">
      {/* Top Gradient Accent Line */}
      <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-[#00e599] via-[#00d2ff] to-transparent opacity-40 group-hover:opacity-100 transition-opacity" />

      {/* Main Content */}
      <div>
        {/* Header: Title & Status */}
        <div className="flex items-start justify-between gap-3 mb-3">
          <h3 className="text-base font-bold text-white group-hover:text-[#00e599] transition-colors leading-snug">
            {event.name}
          </h3>
          <span className="shrink-0 rounded-full bg-[#00e599]/10 border border-[#00e599]/30 px-2.5 py-0.5 text-[11px] font-semibold tracking-wider text-[#00e599]">
            {event.status || 'PUBLISHED'}
          </span>
        </div>

        {/* Description if available */}
        {event.description && (
          <p className="text-xs text-slate-400 mb-4 line-clamp-2 leading-relaxed">
            {event.description}
          </p>
        )}

        {/* Details: Venue & Timing */}
        <div className="space-y-2.5 mb-4 text-xs">
          {/* Venue */}
          <div className="flex items-start gap-2 text-slate-300">
            <MapPin className="h-4 w-4 text-[#00e599] shrink-0 mt-0.5" />
            <div>
              <div className="font-semibold text-white">{event.venueName}</div>
              {event.venueAddress && (
                <div className="text-[11px] text-slate-400 leading-tight">
                  {event.venueAddress}
                </div>
              )}
            </div>
          </div>

          {/* Time */}
          <div className="flex items-center gap-2 text-slate-300">
            <Clock className="h-4 w-4 text-[#00d2ff] shrink-0" />
            <div className="text-[11px]">
              <span className="text-slate-400">Bắt đầu: </span>
              <span className="font-medium text-slate-200">{formatDate(event.startDateTime)}</span>
            </div>
          </div>
          {event.endDateTime && (
            <div className="flex items-center gap-2 text-slate-400 pl-6 text-[11px]">
              <span>Kết thúc: </span>
              <span className="text-slate-300">{formatDate(event.endDateTime)}</span>
            </div>
          )}

          {/* Gates List */}
          {event.venueGates && event.venueGates.length > 0 && (
            <div className="pt-1">
              <div className="flex items-center gap-1.5 text-[11px] text-slate-400 mb-1.5">
                <DoorOpen className="h-3.5 w-3.5 text-[#00e599]" />
                <span>Cổng soát vé được phép:</span>
              </div>
              <div className="flex flex-wrap gap-1.5">
                {event.venueGates.map((gate, idx) => (
                  <span
                    key={idx}
                    className="rounded bg-[#090d16] border border-[#1f293d] px-2 py-0.5 text-[10px] font-mono text-slate-300"
                  >
                    {gate}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Footer: Event ID & Actions */}
      <div className="pt-4 border-t border-[#1f293d] flex flex-col gap-3">
        {/* Copyable Event UUID */}
        <div className="flex items-center justify-between gap-2 rounded-xl bg-[#090d16] border border-[#1f293d] px-2.5 py-1.5">
          <div className="flex items-center gap-1.5 min-w-0">
            <span className="text-[10px] font-mono uppercase text-slate-400 shrink-0">UUID:</span>
            <span className="text-[11px] font-mono text-slate-300 truncate" title={event.id}>
              {event.id}
            </span>
          </div>
          <button
            type="button"
            onClick={handleCopyId}
            className="flex items-center gap-1 text-[11px] font-medium text-slate-400 hover:text-[#00e599] transition-colors shrink-0"
            title="Sao chép Event ID"
          >
            {copied ? (
              <>
                <Check className="h-3.5 w-3.5 text-[#00e599]" />
                <span className="text-[#00e599]">Đã chép</span>
              </>
            ) : (
              <>
                <Copy className="h-3.5 w-3.5" />
                <span>Chép</span>
              </>
            )}
          </button>
        </div>

        {onToggleCheckIn && (
          <button
            type="button"
            disabled={checkInUpdating || event.status !== 'PUBLISHED'}
            onClick={() => onToggleCheckIn(event)}
            className="w-full rounded-xl border border-[#00e599]/30 bg-[#00e599]/10 px-3 py-2 text-xs font-semibold text-[#00e599] disabled:opacity-50"
          >
            {checkInUpdating ? 'Đang cập nhật...' : event.checkInEnabled ? 'Đóng check-in' : 'Mở check-in'}
          </button>
        )}
        {/* Action Button: Cấp vé ngay */}
        {onSelectForTicket && (
          <button
            type="button"
            onClick={() => onSelectForTicket(event.id)}
            className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-[#00e599]/15 to-[#00d2ff]/15 hover:from-[#00e599]/25 hover:to-[#00d2ff]/25 border border-[#00e599]/30 text-white hover:text-[#00e599] py-2 text-xs font-semibold transition-all group/btn"
          >
            <Ticket className="h-3.5 w-3.5 text-[#00e599] group-hover/btn:rotate-12 transition-transform" />
            <span>Cấp vé cho sự kiện này</span>
          </button>
        )}
      </div>
    </div>
  );
};

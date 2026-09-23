import React, { useState } from 'react';
import type { Event } from '../../types';

import { EventCard } from './EventCard';
import { EventFormModal } from './EventFormModal';
import { Plus, RefreshCw, Search, Calendar, Sparkles, AlertCircle } from 'lucide-react';

interface EventsTabProps {
  events: Event[];
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
  onSelectForTicket: (eventId: string) => void;
}

export const EventsTab: React.FC<EventsTabProps> = ({
  events,
  loading,
  error,
  onRefresh,
  onSelectForTicket,
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const filteredEvents = events.filter((ev) => {
    const q = searchTerm.toLowerCase();
    return (
      ev.name?.toLowerCase().includes(q) ||
      ev.venueName?.toLowerCase().includes(q) ||
      ev.id?.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Tab Header & Control Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 rounded-2xl bg-[#121826] border border-[#1f293d] p-5 shadow-xl">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#00e599]/20 text-[#00e599] border border-[#00e599]/30">
              <Calendar className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Quản Lý Sự Kiện</h2>
              <p className="text-xs text-slate-400">
                Tạo và công bố các sự kiện, thiết lập cổng vào và mã định danh
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onRefresh}
            disabled={loading}
            className="flex items-center gap-2 px-3.5 py-2.5 rounded-xl bg-[#1f293d]/80 hover:bg-[#1f293d] text-slate-300 hover:text-white border border-[#1f293d] text-xs font-medium transition-all"
            title="Làm mới danh sách sự kiện từ server"
          >
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin text-[#00e599]' : ''}`} />
            <span>Làm mới</span>
          </button>

          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-[#00e599] to-[#00d2ff] hover:opacity-95 text-slate-950 text-xs font-bold shadow-[0_0_20px_-3px_rgba(0,229,153,0.35)] transition-all"
          >
            <Plus className="h-4 w-4 stroke-[2.5]" />
            <span>Tạo Sự Kiện Mới</span>
          </button>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm kiếm sự kiện theo tên, địa điểm, hoặc Event UUID..."
            className="w-full rounded-xl bg-[#121826] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] pl-10 pr-4 py-2.5 text-xs text-white placeholder-slate-500 transition-colors"
          />
        </div>
        <div className="text-xs text-slate-400 px-2 font-mono">
          Tổng số: <strong className="text-[#00e599]">{filteredEvents.length}</strong> sự kiện
        </div>
      </div>

      {/* Error state */}
      {error && (
        <div className="rounded-2xl bg-rose-500/10 border border-rose-500/30 p-4 text-xs text-rose-300 flex items-start gap-3">
          <AlertCircle className="h-5 w-5 shrink-0 text-rose-400 mt-0.5" />
          <div className="flex-1">
            <h4 className="font-semibold text-rose-200">Không thể tải danh sách sự kiện</h4>
            <p className="mt-1 text-rose-300/80">{error}</p>
            <button
              type="button"
              onClick={onRefresh}
              className="mt-2 inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 text-xs font-medium"
            >
              <RefreshCw className="h-3.5 w-3.5" /> Thử lại
            </button>
          </div>
        </div>
      )}

      {/* Event Cards Grid */}
      {loading && events.length === 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {[1, 2, 3].map((n) => (
            <div
              key={n}
              className="h-64 rounded-2xl bg-[#121826]/60 border border-[#1f293d] animate-pulse p-5 flex flex-col justify-between"
            >
              <div className="space-y-3">
                <div className="h-5 bg-slate-800 rounded-md w-3/4" />
                <div className="h-3 bg-slate-800/60 rounded-md w-1/2" />
                <div className="h-3 bg-slate-800/60 rounded-md w-full" />
              </div>
              <div className="h-10 bg-slate-800/40 rounded-xl" />
            </div>
          ))}
        </div>
      ) : filteredEvents.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredEvents.map((event) => (
            <EventCard
              key={event.id}
              event={event}
              onSelectForTicket={onSelectForTicket}
            />
          ))}
        </div>
      ) : (
        <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-12 text-center flex flex-col items-center justify-center">
          <div className="h-16 w-16 rounded-2xl bg-[#00e599]/10 border border-[#00e599]/20 flex items-center justify-center text-[#00e599] mb-4">
            <Sparkles className="h-8 w-8" />
          </div>
          <h3 className="text-base font-bold text-white mb-1">
            {searchTerm ? 'Không tìm thấy sự kiện phù hợp' : 'Chưa có sự kiện nào được tạo'}
          </h3>
          <p className="text-xs text-slate-400 max-w-sm mb-6 leading-relaxed">
            {searchTerm
              ? 'Vui lòng thử lại với từ khóa khác hoặc xóa bộ lọc tìm kiếm.'
              : 'Hãy bắt đầu bằng cách tạo sự kiện đầu tiên để cấp vé và thử nghiệm hệ thống soát vé.'}
          </p>
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-[#00e599] to-[#00d2ff] hover:opacity-95 text-slate-950 text-xs font-bold shadow-[0_0_20px_-3px_rgba(0,229,153,0.35)] transition-all"
          >
            <Plus className="h-4 w-4 stroke-[2.5]" />
            <span>Tạo Sự Kiện Ngay</span>
          </button>
        </div>
      )}

      {/* Event Creation Modal */}
      <EventFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onEventCreated={onRefresh}
      />
    </div>
  );
};

import React, { useState } from 'react';
import { X, Calendar, MapPin, Tag, Plus, Loader2, Sparkles, Clock } from 'lucide-react';
import type { CreateEventRequest } from '../../types';

import { createEvent, getApiErrorMessage } from '../../services/api';
import { useToast } from '../../context/ToastContext';

interface EventFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onEventCreated: () => void;
}

export const EventFormModal: React.FC<EventFormModalProps> = ({
  isOpen,
  onClose,
  onEventCreated,
}) => {
  const { success, error: toastError } = useToast();
  const [submitting, setSubmitting] = useState(false);

  // Form State
  const [name, setName] = useState('');
  const [venueName, setVenueName] = useState('');
  const [venueAddress, setVenueAddress] = useState('');
  const [description, setDescription] = useState('');
  const [startDateTime, setStartDateTime] = useState('');
  const [endDateTime, setEndDateTime] = useState('');
  
  // Gates tag list
  const [gates, setGates] = useState<string[]>(['CỔNG CHÍNH', 'CỔNG A1', 'CỔNG B2']);
  const [newGateInput, setNewGateInput] = useState('');

  if (!isOpen) return null;

  const handleAddGate = () => {
    const trimmed = newGateInput.trim();
    if (trimmed && !gates.includes(trimmed)) {
      setGates([...gates, trimmed]);
      setNewGateInput('');
    }
  };

  const handleRemoveGate = (gateToRemove: string) => {
    setGates(gates.filter((g) => g !== gateToRemove));
  };

  // Helper to load sample mock data for super-fast testing
  const handleLoadSample = () => {
    const now = new Date();
    const startDate = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 days later
    startDate.setHours(19, 0, 0, 0);
    const endDate = new Date(startDate.getTime() + 4 * 60 * 60 * 1000); // 4 hours duration

    const formatToLocalISO = (d: Date) => {
      const pad = (n: number) => n.toString().padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    };

    setName('Đại Nhạc Hội EDM - SoundWave Festival 2026');
    setVenueName('Sân Vận Động Quốc Gia Mỹ Đình');
    setVenueAddress('Đường Lê Đức Thọ, Phường Mỹ Đình 1, Nam Từ Liêm, Hà Nội');
    setDescription('Sự kiện âm nhạc quốc tế đỉnh cao với hệ thống kiểm soát vé thông minh Dynamic QR chống giả mạo SecureTix.');
    setGates(['CỔNG CHÍNH', 'CỔNG A1', 'CỔNG B2', 'CỔNG VIP EMERALD']);
    setStartDateTime(formatToLocalISO(startDate));
    setEndDateTime(formatToLocalISO(endDate));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !venueName.trim()) {
      toastError('Thiếu thông tin', 'Vui lòng nhập đầy đủ tên sự kiện và địa điểm!');
      return;
    }

    try {
      setSubmitting(true);

      // Convert local date-time strings to standard ISO 8601 with timezone (e.g. 2026-10-20T19:00:00Z)
      const startISO = startDateTime ? new Date(startDateTime).toISOString() : new Date().toISOString();
      const endISO = endDateTime 
        ? new Date(endDateTime).toISOString() 
        : new Date(Date.now() + 4 * 3600 * 1000).toISOString();

      const payload: CreateEventRequest = {
        name: name.trim(),
        venueName: venueName.trim(),
        venueAddress: venueAddress.trim(),
        venueGates: gates.length > 0 ? gates : ['CỔNG CHÍNH'],
        description: description.trim(),
        startDateTime: startISO,
        endDateTime: endISO,
      };

      await createEvent(payload);
      success('Tạo sự kiện thành công!', `Sự kiện "${name}" đã được công bố trên hệ thống.`);
      onEventCreated();
      onClose();
    } catch (err) {
      const errMsg = getApiErrorMessage(err, 'Không thể tạo sự kiện. Vui lòng thử lại!');
      toastError('Lỗi tạo sự kiện', errMsg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
      <div 
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className="w-full max-w-2xl rounded-2xl bg-[#121826] border border-[#1f293d] shadow-[0_0_30px_-5px_rgba(0,0,0,0.8)] overflow-hidden flex flex-col max-h-[90vh]"
      >
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#1f293d] bg-[#0d131f]">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#00e599]/20 text-[#00e599] border border-[#00e599]/30">
              <Calendar className="h-5 w-5" />
            </div>
            <div>
              <h3 id="modal-title" className="text-base font-bold text-white">Tạo Sự Kiện Mới</h3>
              <p className="text-xs text-slate-400">Đăng ký sự kiện vào hệ thống quản lý vé SecureTix</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={handleLoadSample}
              className="flex items-center gap-1.5 px-2.5 py-1.5 text-xs text-[#00d2ff] bg-[#00d2ff]/10 hover:bg-[#00d2ff]/20 border border-[#00d2ff]/30 rounded-lg transition-colors"
              title="Điền nhanh dữ liệu mẫu để test"
            >
              <Sparkles className="h-3.5 w-3.5" />
              <span>Điền mẫu test</span>
            </button>
            <button
              type="button"
              onClick={onClose}
              className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* Modal Body / Form */}
        <form onSubmit={handleSubmit} className="overflow-y-auto p-6 space-y-4 text-xs">
          {/* Event Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Tên sự kiện <span className="text-[#00e599]">*</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="VD: Chung Kết Âm Nhạc Quốc Tế 2026"
              className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] px-3.5 py-2.5 text-sm text-white placeholder-slate-500 transition-colors"
            />
          </div>

          {/* Venue & Address */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <MapPin className="h-3.5 w-3.5 text-[#00e599]" />
                <span>Tên địa điểm / Sân vận động</span> <span className="text-[#00e599]">*</span>
              </label>
              <input
                type="text"
                required
                value={venueName}
                onChange={(e) => setVenueName(e.target.value)}
                placeholder="VD: Trung Tâm Triển Lãm SECC"
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] px-3.5 py-2.5 text-xs text-white placeholder-slate-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                Địa chỉ chi tiết
              </label>
              <input
                type="text"
                value={venueAddress}
                onChange={(e) => setVenueAddress(e.target.value)}
                placeholder="VD: 799 Nguyễn Văn Linh, Q.7, TP.HCM"
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] px-3.5 py-2.5 text-xs text-white placeholder-slate-500"
              />
            </div>
          </div>

          {/* Start and End Date Time */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <Clock className="h-3.5 w-3.5 text-[#00d2ff]" />
                <span>Thời gian bắt đầu</span> <span className="text-[#00e599]">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={startDateTime}
                onChange={(e) => setStartDateTime(e.target.value)}
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] px-3.5 py-2.5 text-xs text-white"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                <Clock className="h-3.5 w-3.5 text-[#00d2ff]" />
                <span>Thời gian kết thúc</span> <span className="text-[#00e599]">*</span>
              </label>
              <input
                type="datetime-local"
                required
                value={endDateTime}
                onChange={(e) => setEndDateTime(e.target.value)}
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] px-3.5 py-2.5 text-xs text-white"
              />
            </div>
          </div>

          {/* Venue Gates Tag Selector */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <Tag className="h-3.5 w-3.5 text-[#00e599]" />
              <span>Danh sách cổng soát vé (venueGates)</span>
            </label>
            <div className="flex flex-wrap gap-2 mb-2 p-2.5 rounded-xl bg-[#090d16] border border-[#1f293d] min-h-[44px]">
              {gates.map((gate) => (
                <span
                  key={gate}
                  className="inline-flex items-center gap-1.5 rounded-lg bg-[#1f293d] px-2.5 py-1 text-xs font-mono font-medium text-slate-200 border border-slate-700"
                >
                  {gate}
                  <button
                    type="button"
                    onClick={() => handleRemoveGate(gate)}
                    className="text-slate-400 hover:text-rose-400 ml-0.5"
                  >
                    <X className="h-3 w-3" />
                  </button>
                </span>
              ))}
              {gates.length === 0 && (
                <span className="text-xs text-slate-500 italic py-0.5">Chưa có cổng nào</span>
              )}
            </div>

            <div className="flex gap-2">
              <input
                type="text"
                value={newGateInput}
                onChange={(e) => setNewGateInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddGate();
                  }
                }}
                placeholder="Nhập tên cổng mới (VD: CỔNG VIP, CỔNG B1)..."
                className="flex-1 rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none px-3 py-2 text-xs text-white placeholder-slate-500"
              />
              <button
                type="button"
                onClick={handleAddGate}
                className="flex items-center gap-1 px-3 py-2 rounded-xl bg-[#1f293d] hover:bg-slate-700 text-slate-200 text-xs font-medium transition-colors"
              >
                <Plus className="h-3.5 w-3.5" />
                <span>Thêm cổng</span>
              </button>
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Mô tả sự kiện
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Thông tin giới thiệu, quy định an ninh hoặc lưu ý kiểm soát vé..."
              className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] p-3 text-xs text-white placeholder-slate-500"
            />
          </div>

          {/* Modal Footer */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-[#1f293d]">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2 rounded-xl border border-[#1f293d] text-slate-400 hover:text-white hover:bg-[#1a2234] text-xs font-medium transition-colors"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 px-5 py-2 rounded-xl bg-gradient-to-r from-[#00e599] to-[#00d2ff] hover:opacity-90 text-slate-950 text-xs font-bold shadow-[0_0_15px_-2px_rgba(0,229,153,0.4)] transition-all disabled:opacity-50"
            >
              {submitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Đang lưu sự kiện...</span>
                </>
              ) : (
                <>
                  <Calendar className="h-4 w-4" />
                  <span>Xác nhận tạo sự kiện</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

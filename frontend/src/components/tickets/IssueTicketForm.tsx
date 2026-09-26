import React, { useState } from 'react';
import type { Event, UserAccount } from '../../types';
import { claimTicket, getApiErrorMessage } from '../../services/api';
import { useToast } from '../../context/ToastContext';
import { 
  Ticket, 
  User, 
  Send, 
  Loader2, 
  Armchair, 
  Award, 
  CheckCircle2, 
} from 'lucide-react';

interface IssueTicketFormProps {
  events: Event[];
  selectedEventId: string;
  setSelectedEventId: (id: string) => void;
  userId: string;
  setUserId: (id: string) => void;
  availableUsers: UserAccount[];
  onTicketIssued: (userId: string) => void;
}

const TICKET_CATEGORIES = [
  'VIP Diamond',
  'Fanzone Standard',
  'Khán Đài A',
  'Vé Tiêu Chuẩn',
];

export const IssueTicketForm: React.FC<IssueTicketFormProps> = ({
  events,
  selectedEventId,
  setSelectedEventId,
  userId,
  setUserId,
  availableUsers,
  onTicketIssued,
}) => {
  const { success, error: toastError } = useToast();
  const [submitting, setSubmitting] = useState(false);

  // Form Fields
  const [categoryName, setCategoryName] = useState('VIP Diamond');
  const [attendeeName, setAttendeeName] = useState('');
  const [seatNumber, setSeatNumber] = useState('A-01');

  // Find currently selected user object
  const selectedUser = availableUsers.find((u) => u.id === userId);

  const effectiveAttendeeName = attendeeName || selectedUser?.name || '';

  // When selectedUser changes, keep attendeeName synchronized with user name
  const handleSelectUser = (selectedId: string) => {
    setUserId(selectedId);
    const user = availableUsers.find((u) => u.id === selectedId);
    if (user) {
      setAttendeeName(user.name);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedEventId) {
      toastError('Chưa chọn sự kiện', 'Vui lòng chọn sự kiện cần cấp vé!');
      return;
    }
    if (!selectedUser) {
      toastError('Thiếu thông tin người nhận', 'Vui lòng chọn người nhận vé từ danh sách!');
      return;
    }

    try {
      setSubmitting(true);
      await claimTicket(selectedEventId, selectedUser.id, {
        categoryName,
        seatNumber: seatNumber.trim(),
        attendeeName: effectiveAttendeeName.trim(),
      });

      const selectedEvent = events.find((ev) => ev.id === selectedEventId);
      success(
        'Cấp vé thành công!',
        `Đã cấp vé ${categoryName} (${seatNumber}) cho ${effectiveAttendeeName} tại sự kiện "${selectedEvent?.name || ''}"`
      );

      // Auto refresh user tickets
      onTicketIssued(selectedUser.id);

      // Auto bump seat number for next ticket
      const numMatch = seatNumber.match(/(\d+)$/);
      if (numMatch) {
        const nextNum = parseInt(numMatch[1], 10) + 1;
        setSeatNumber(seatNumber.replace(/\d+$/, nextNum.toString()));
      }
    } catch (err) {
      const errMsg = getApiErrorMessage(err, 'Không thể cấp vé. Vui lòng kiểm tra lại thông tin!');
      toastError('Lỗi cấp vé', errMsg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-6 shadow-xl flex flex-col justify-between">
      <div>
        {/* Form Title */}
        <div className="flex items-center justify-between gap-3 mb-5">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#00e599]/20 text-[#00e599] border border-[#00e599]/30">
              <Ticket className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">Form Cấp Vé Trực Tiếp</h3>
              <p className="text-xs text-slate-400">Chọn người dùng và gán vé tham dự sự kiện</p>
            </div>
          </div>
          <span className="rounded-md bg-amber-500/10 border border-amber-500/30 px-2 py-0.5 text-[10px] font-mono font-bold text-amber-400">
            CẤP VÉ THỦ CÔNG
          </span>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          {/* 1. Chọn sự kiện */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              1. Chọn sự kiện áp dụng <span className="text-[#00e599]">*</span>
            </label>
            {events.length > 0 ? (
              <select
                required
                value={selectedEventId}
                onChange={(e) => setSelectedEventId(e.target.value)}
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] px-3.5 py-2.5 text-xs text-white"
              >
                {events.map((ev) => (
                  <option key={ev.id} value={ev.id}>
                    {ev.name} • {ev.venueName}
                  </option>
                ))}
              </select>
            ) : (
              <div className="rounded-xl bg-[#090d16] border border-dashed border-[#1f293d] p-3 text-xs text-slate-500 text-center">
                Chưa có sự kiện nào. Vui lòng tạo sự kiện ở Tab 1 trước!
              </div>
            )}
          </div>

          {/* 2. Chọn Người Nhận Vé (User Selection by Name/Profile) */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center justify-between">
              <span>2. Chọn người nhận vé <span className="text-[#00e599]">*</span></span>
              <span className="text-[10px] text-slate-500">Chọn theo tên khách hàng</span>
            </label>

            {/* User Dropdown Selector */}
            <select
              value={userId}
              onChange={(e) => handleSelectUser(e.target.value)}
              className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] px-3.5 py-2.5 text-xs text-white mb-2"
            >
              {availableUsers.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name} ({u.username}) — {u.email}
                </option>
              ))}
            </select>

            {/* Selected User Profile Preview Card */}
            {selectedUser && (
              <div className="rounded-xl bg-[#090d16] border border-[#00e599]/30 p-3 flex items-center justify-between shadow-[0_0_15px_-3px_rgba(0,229,153,0.15)]">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-xl bg-[#00e599]/20 border border-[#00e599]/40 flex items-center justify-center font-bold text-sm text-[#00e599] shrink-0">
                    {selectedUser.name.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white text-xs">{selectedUser.name}</span>
                      <span className="text-slate-400">@{selectedUser.username}</span>
                    </div>
                    <div className="text-[11px] text-slate-400 mt-0.5">
                      {selectedUser.email} {selectedUser.phone ? `• ${selectedUser.phone}` : ''}
                    </div>
                  </div>
                </div>

                <div className="text-right">
                  <span className="rounded bg-slate-800 px-2 py-0.5 text-[10px] text-slate-300 font-mono">
                    {selectedUser.role}
                  </span>
                </div>
              </div>
            )}

          </div>

          {/* 3. Tên Khán Giả & Vị Trí Ghế */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
                <User className="h-3.5 w-3.5 text-[#00d2ff]" />
                <span>Tên in trên vé</span> <span className="text-[#00e599]">*</span>
              </label>
              <input
                type="text"
                required
                value={effectiveAttendeeName}
                onChange={(e) => setAttendeeName(e.target.value)}
                placeholder="VD: Nguyễn Hoàng Long"
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] px-3.5 py-2.5 text-xs text-white"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
                <Armchair className="h-3.5 w-3.5 text-[#00e599]" />
                <span>Vị trí ghế (seatNumber)</span> <span className="text-[#00e599]">*</span>
              </label>
              <input
                type="text"
                required
                value={seatNumber}
                onChange={(e) => setSeatNumber(e.target.value)}
                placeholder="VD: VIP-A12, STANDING-01"
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none focus:ring-1 focus:ring-[#00e599] px-3.5 py-2.5 text-xs font-mono text-white"
              />
            </div>
          </div>

          {/* 4. Hạng Vé (Category Name) */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
              <Award className="h-3.5 w-3.5 text-[#00e599]" />
              <span>Hạng vé (categoryName)</span>
            </label>
            <div className="grid grid-cols-2 gap-2 mb-2">
              {TICKET_CATEGORIES.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setCategoryName(cat)}
                  className={`p-2 rounded-xl text-xs font-medium text-left border transition-all ${
                    categoryName === cat
                      ? 'bg-[#00e599]/15 border-[#00e599] text-[#00e599] font-bold shadow-[0_0_10px_-2px_rgba(0,229,153,0.3)]'
                      : 'bg-[#090d16] border-[#1f293d] text-slate-400 hover:text-white hover:border-slate-700'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span>{cat}</span>
                    {categoryName === cat && <CheckCircle2 className="h-3.5 w-3.5 text-[#00e599]" />}
                  </div>
                </button>
              ))}
            </div>
            <input
              type="text"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
              placeholder="Hoặc tự nhập tên hạng vé..."
              className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none px-3 py-2 text-xs text-white"
            />
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={submitting || events.length === 0}
            className="w-full mt-2 flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-[#00e599] to-[#00d2ff] hover:opacity-95 text-slate-950 py-3 text-xs font-bold shadow-[0_0_20px_-3px_rgba(0,229,153,0.35)] transition-all disabled:opacity-50"
          >
            {submitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                <span>Đang xử lý cấp vé...</span>
              </>
            ) : (
              <>
                <Send className="h-4 w-4" />
                <span>Cấp Vé Cho {effectiveAttendeeName}</span>
              </>
            )}
          </button>
        </form>
      </div>

      {/* Helper Footer */}
      <div className="mt-5 pt-4 border-t border-[#1f293d] text-[11px] text-slate-400 leading-relaxed">
        💡 <strong className="text-slate-300">Hướng dẫn kiểm tra:</strong> Sau khi cấp vé, tài khoản <strong className="text-white">{selectedUser?.name || effectiveAttendeeName}</strong> mở ứng dụng CyberPass (Android) và bấm <strong className="text-[#00e599]">"Làm mới"</strong> sẽ thấy vé xuất hiện kèm mã Dynamic QR 30s.
      </div>
    </div>
  );
};

import React, { useState } from 'react';
import type { UserAccount } from '../../types';
import { UserFormModal } from './UserFormModal';
import { 
  Users, 
  UserPlus, 
  Copy, 
  Check, 
  Ticket, 
  Smartphone, 
  Trash2, 
  RotateCcw, 
  Mail, 
  Phone,
  Search,
  Edit3
} from 'lucide-react';
import { useToast } from '../../context/ToastContext';

interface UsersTabProps {
  users: UserAccount[];
  onSaveUser: (user: UserAccount) => void;
  onDeleteUser: (userId: string) => void;
  onResetUsers: () => void;
  onSelectUserForTicket: (userId: string) => void;
  onViewUserTickets: (userId: string) => void;
}

export const UsersTab: React.FC<UsersTabProps> = ({
  users,
  onSaveUser,
  onDeleteUser,
  onResetUsers,
  onSelectUserForTicket,
  onViewUserTickets,
}) => {
  const { success } = useToast();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserAccount | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const handleCopy = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(id);
    setCopiedId(id);
    success('Đã sao chép User ID!', id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleOpenCreateModal = () => {
    setEditingUser(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (user: UserAccount) => {
    setEditingUser(user);
    setIsModalOpen(true);
  };

  const filteredUsers = users.filter((u) => {
    const q = searchTerm.toLowerCase();
    return (
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.id.toLowerCase().includes(q) ||
      (u.phone && u.phone.includes(q))
    );
  });

  return (
    <div className="space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 rounded-2xl bg-[#121826] border border-[#1f293d] p-5 shadow-xl">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#00d2ff]/20 text-[#00d2ff] border border-[#00d2ff]/30">
            <Users className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Quản Lý Tài Khoản Khách Hàng</h2>
            <p className="text-xs text-slate-400">
              Quản lý danh sách người dùng, chỉnh sửa thông tin và cấp vé vào tài khoản
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onResetUsers}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-[#1f293d]/60 hover:bg-[#1f293d] text-slate-400 hover:text-white border border-[#1f293d] text-xs font-medium transition-colors"
            title="Đồng bộ danh sách người dùng từ Database Backend"
          >
            <RotateCcw className="h-3.5 w-3.5" />
            <span>Đồng bộ từ Database</span>
          </button>

          <button
            type="button"
            onClick={handleOpenCreateModal}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-[#00d2ff] to-[#00e599] hover:opacity-95 text-slate-950 text-xs font-bold shadow-[0_0_20px_-3px_rgba(0,210,255,0.35)] transition-all"
          >
            <UserPlus className="h-4 w-4 stroke-[2.5]" />
            <span>Thêm Tài Khoản</span>
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="flex items-center gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm kiếm khách hàng theo tên, email hoặc số điện thoại..."
            className="w-full rounded-xl bg-[#121826] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] pl-10 pr-4 py-2.5 text-xs text-white placeholder-slate-500 transition-colors"
          />
        </div>
        <div className="text-xs text-slate-400 px-2 font-mono">
          Tổng số: <strong className="text-[#00d2ff]">{filteredUsers.length}</strong> khách hàng
        </div>
      </div>

      {/* Users Grid */}
      {filteredUsers.length === 0 ? (
        <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-12 text-center flex flex-col items-center justify-center">
          <div className="h-16 w-16 rounded-2xl bg-[#00d2ff]/10 border border-[#00d2ff]/20 flex items-center justify-center text-[#00d2ff] mb-4">
            <Users className="h-8 w-8" />
          </div>
          <h3 className="text-base font-bold text-white mb-1">
            {searchTerm ? 'Không tìm thấy người dùng phù hợp' : 'Chưa có tài khoản nào trong hệ thống'}
          </h3>
          <p className="text-xs text-slate-400 max-w-sm mb-6 leading-relaxed">
            {searchTerm
              ? 'Vui lòng kiểm tra lại từ khóa tìm kiếm.'
              : 'Người dùng sẽ tự động được ghi nhận vào database khi đăng nhập qua ứng dụng di động Android, hoặc bạn có thể tạo mới.'}
          </p>
          <button
            type="button"
            onClick={handleOpenCreateModal}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-[#00d2ff] to-[#00e599] hover:opacity-95 text-slate-950 text-xs font-bold shadow-[0_0_20px_-3px_rgba(0,210,255,0.35)] transition-all"
          >
            <UserPlus className="h-4 w-4 stroke-[2.5]" />
            <span>Thêm Tài Khoản Ngay</span>
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filteredUsers.map((user) => (
          <div
            key={user.id}
            className={`group rounded-2xl bg-[#121826] border p-5 flex flex-col justify-between transition-all duration-300 shadow-lg ${
              user.isDemoAppUser
                ? 'border-[#00e599]/40 hover:border-[#00e599] shadow-[0_0_20px_-5px_rgba(0,229,153,0.15)]'
                : 'border-[#1f293d] hover:border-slate-700'
            }`}
          >
            <div>
              {/* Card Header: Avatar, Name, Role & App Demo Badge */}
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="flex items-center gap-3">
                  <div
                    className={`h-11 w-11 rounded-xl flex items-center justify-center font-bold text-base border shrink-0 ${
                      user.isDemoAppUser
                        ? 'bg-[#00e599]/20 border-[#00e599]/40 text-[#00e599]'
                        : 'bg-[#1f293d] border-slate-700 text-slate-200'
                    }`}
                  >
                    {user.name.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-white group-hover:text-[#00d2ff] transition-colors leading-tight">
                      {user.name}
                    </h4>
                    <div className="flex items-center gap-1.5 mt-1">
                      <span className="rounded bg-slate-800 px-2 py-0.5 text-[10px] font-mono text-slate-300">
                        {user.role}
                      </span>
                      {user.isDemoAppUser && (
                        <span className="rounded-full bg-[#00e599]/15 border border-[#00e599]/40 px-2 py-0.5 text-[9px] font-bold text-[#00e599] flex items-center gap-1">
                          <Smartphone className="h-2.5 w-2.5" /> APP DEMO
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Edit Button */}
                <button
                  type="button"
                  onClick={() => handleOpenEditModal(user)}
                  className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#1f293d]/60 hover:bg-[#1f293d] text-slate-300 hover:text-[#00d2ff] border border-[#1f293d] text-[11px] font-medium transition-colors"
                  title="Chỉnh sửa thông tin tài khoản"
                >
                  <Edit3 className="h-3 w-3" />
                  <span>Sửa</span>
                </button>
              </div>

              {/* Details: Email & Phone */}
              <div className="space-y-1.5 mb-4 text-xs">
                <div className="flex items-center gap-2 text-slate-400">
                  <Mail className="h-3.5 w-3.5 text-slate-500 shrink-0" />
                  <span className="truncate">{user.email}</span>
                </div>
                {user.phone && (
                  <div className="flex items-center gap-2 text-slate-400">
                    <Phone className="h-3.5 w-3.5 text-slate-500 shrink-0" />
                    <span>{user.phone}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Bottom Actions */}
            <div className="pt-3 border-t border-[#1f293d] space-y-2.5">
              {/* Subtle ID reference with copy (Not dominant) */}
              <div className="flex items-center justify-between text-[11px] text-slate-500 font-mono px-1">
                <span className="truncate max-w-[170px]" title={user.id}>
                  ID: {user.id.substring(0, 16)}...
                </span>
                <button
                  type="button"
                  onClick={(e) => handleCopy(user.id, e)}
                  className="text-slate-400 hover:text-[#00e599] flex items-center gap-1 transition-colors"
                  title="Sao chép ID"
                >
                  {copiedId === user.id ? (
                    <Check className="h-3 w-3 text-[#00e599]" />
                  ) : (
                    <Copy className="h-3 w-3" />
                  )}
                  <span>{copiedId === user.id ? 'Đã chép' : 'Chép ID'}</span>
                </button>
              </div>

              {/* Action Buttons: Cấp vé / Xem vé / Xóa */}
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => onSelectUserForTicket(user.id)}
                  className="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-xl bg-[#00e599]/10 hover:bg-[#00e599]/20 border border-[#00e599]/30 text-[#00e599] text-xs font-semibold transition-colors"
                >
                  <Ticket className="h-3.5 w-3.5" />
                  <span>Cấp vé ngay</span>
                </button>

                <button
                  type="button"
                  onClick={() => onViewUserTickets(user.id)}
                  className="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-xl bg-[#00d2ff]/10 hover:bg-[#00d2ff]/20 border border-[#00d2ff]/30 text-[#00d2ff] text-xs font-semibold transition-colors"
                >
                  <span>Xem vé</span>
                </button>

                {!user.isDemoAppUser && (
                  <button
                    type="button"
                    onClick={() => onDeleteUser(user.id)}
                    className="p-2 rounded-xl bg-slate-800/40 hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 border border-transparent hover:border-rose-500/30 transition-colors"
                    title="Xóa tài khoản này"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      )}

      <UserFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onUserSaved={onSaveUser}
        userToEdit={editingUser}
      />
    </div>
  );
};

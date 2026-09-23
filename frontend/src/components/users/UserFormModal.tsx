import React, { useState, useEffect } from 'react';
import { X, User, Mail, Phone, Shield, ChevronDown, ChevronUp, Sparkles, Check } from 'lucide-react';
import type { UserAccount } from '../../types';
import { useToast } from '../../context/ToastContext';

interface UserFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUserSaved: (user: UserAccount) => void;
  userToEdit?: UserAccount | null;
}

export const UserFormModal: React.FC<UserFormModalProps> = ({
  isOpen,
  onClose,
  onUserSaved,
  userToEdit,
}) => {
  const { success } = useToast();

  const generateUuid = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0;
      const v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  };

  const [id, setId] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState<'USER' | 'ADMIN'>('USER');
  const [showAdvancedUuid, setShowAdvancedUuid] = useState(false);

  useEffect(() => {
    if (userToEdit) {
      setId(userToEdit.id);
      setName(userToEdit.name);
      setEmail(userToEdit.email || '');
      setPhone(userToEdit.phone || '');
      setRole(userToEdit.role || 'USER');
      setShowAdvancedUuid(false);
    } else {
      setId(generateUuid());
      setName('');
      setEmail('');
      setPhone('');
      setRole('USER');
      setShowAdvancedUuid(false);
    }
  }, [userToEdit, isOpen]);

  if (!isOpen) return null;

  const handleGenerateNewId = () => {
    setId(generateUuid());
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    const savedUser: UserAccount = {
      id: id.trim() || generateUuid(),
      name: name.trim(),
      email: email.trim() || `${name.toLowerCase().replace(/\s+/g, '')}@securetix.demo`,
      phone: phone.trim(),
      role,
      isDemoAppUser: userToEdit ? userToEdit.isDemoAppUser : false,
      createdAt: userToEdit ? userToEdit.createdAt : new Date().toISOString(),
    };

    onUserSaved(savedUser);
    success(
      userToEdit ? 'Cập nhật tài khoản thành công!' : 'Đã thêm tài khoản mới!',
      `Tài khoản "${name}" (${savedUser.role}) đã được lưu vào hệ thống.`
    );
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
      <div 
        role="dialog"
        aria-modal="true"
        aria-labelledby="user-modal-title"
        className="w-full max-w-lg rounded-2xl bg-[#121826] border border-[#1f293d] shadow-[0_0_30px_-5px_rgba(0,0,0,0.8)] overflow-hidden flex flex-col"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#1f293d] bg-[#0d131f]">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-[#00d2ff]/20 text-[#00d2ff] border border-[#00d2ff]/30">
              <User className="h-5 w-5" />
            </div>
            <div>
              <h3 id="user-modal-title" className="text-base font-bold text-white">
                {userToEdit ? 'Cập Nhật Tài Khoản' : 'Thêm Tài Khoản Mới'}
              </h3>
              <p className="text-xs text-slate-400">
                {userToEdit
                  ? `Chỉnh sửa thông tin người dùng: ${userToEdit.name}`
                  : 'Tạo tài khoản khách hàng / người nhận vé'}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          {/* Full Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Họ và tên người dùng <span className="text-[#00e599]">*</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="VD: Nguyễn Hoàng Long, Trần Minh Quân..."
              className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] px-3.5 py-2.5 text-xs text-white placeholder-slate-500"
            />
          </div>

          {/* Email & Phone */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
                <Mail className="h-3.5 w-3.5 text-slate-400" />
                <span>Email liên hệ</span>
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="long.nh@securetix.demo"
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none px-3.5 py-2.5 text-xs text-white placeholder-slate-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
                <Phone className="h-3.5 w-3.5 text-slate-400" />
                <span>Số điện thoại</span>
              </label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="0987 654 321"
                className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none px-3.5 py-2.5 text-xs text-white placeholder-slate-500"
              />
            </div>
          </div>

          {/* Role */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1">
              <Shield className="h-3.5 w-3.5 text-slate-400" />
              <span>Vai trò tài khoản (Role)</span>
            </label>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setRole('USER')}
                className={`py-2 px-3 rounded-xl border text-xs font-semibold transition-all flex items-center justify-center gap-1.5 ${
                  role === 'USER'
                    ? 'bg-[#00e599]/15 border-[#00e599] text-[#00e599]'
                    : 'bg-[#090d16] border-[#1f293d] text-slate-400'
                }`}
              >
                {role === 'USER' && <Check className="h-3.5 w-3.5" />}
                <span>Khán giả (USER)</span>
              </button>
              <button
                type="button"
                onClick={() => setRole('ADMIN')}
                className={`py-2 px-3 rounded-xl border text-xs font-semibold transition-all flex items-center justify-center gap-1.5 ${
                  role === 'ADMIN'
                    ? 'bg-[#00d2ff]/15 border-[#00d2ff] text-[#00d2ff]'
                    : 'bg-[#090d16] border-[#1f293d] text-slate-400'
                }`}
              >
                {role === 'ADMIN' && <Check className="h-3.5 w-3.5" />}
                <span>Ban Quản Trị (ADMIN)</span>
              </button>
            </div>
          </div>

          {/* Collapsible Advanced Section: UUID */}
          <div className="pt-2 border-t border-[#1f293d]/60">
            <button
              type="button"
              onClick={() => setShowAdvancedUuid(!showAdvancedUuid)}
              className="flex items-center justify-between w-full text-slate-500 hover:text-slate-300 text-[11px] transition-colors py-1"
            >
              <span>⚙️ Tùy chọn nâng cao: Mã định danh kỹ thuật (UUID)</span>
              {showAdvancedUuid ? (
                <ChevronUp className="h-3.5 w-3.5" />
              ) : (
                <ChevronDown className="h-3.5 w-3.5" />
              )}
            </button>

            {showAdvancedUuid && (
              <div className="mt-2.5 space-y-2 p-3 rounded-xl bg-[#090d16] border border-[#1f293d]">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] text-slate-400">UUID hệ thống:</span>
                  {!userToEdit && (
                    <button
                      type="button"
                      onClick={handleGenerateNewId}
                      className="flex items-center gap-1 text-[10px] text-[#00d2ff] hover:underline"
                    >
                      <Sparkles className="h-3 w-3" />
                      <span>Sinh lại</span>
                    </button>
                  )}
                </div>
                <input
                  type="text"
                  value={id}
                  disabled={!!userToEdit} // Keep original UUID when editing
                  onChange={(e) => setId(e.target.value)}
                  className="w-full rounded-lg bg-[#121826] border border-[#1f293d] px-2.5 py-1.5 text-[11px] font-mono text-slate-300 disabled:opacity-60"
                />
              </div>
            )}
          </div>

          {/* Modal Footer Actions */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-[#1f293d]">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl border border-[#1f293d] text-slate-400 hover:text-white text-xs font-medium transition-colors"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              className="px-5 py-2 rounded-xl bg-gradient-to-r from-[#00d2ff] to-[#00e599] text-slate-950 text-xs font-bold shadow-[0_0_15px_-2px_rgba(0,210,255,0.4)] hover:opacity-95 transition-all"
            >
              {userToEdit ? 'Lưu Cập Nhật' : 'Tạo Tài Khoản'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

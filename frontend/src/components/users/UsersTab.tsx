import { useState } from 'react';
import { Search, Ticket, RefreshCw } from 'lucide-react';
import type { UserAccount } from '../../types';

interface UsersTabProps {
  users: UserAccount[];
  onResetUsers: () => void;
  onSelectUserForTicket: (userId: string) => void;
  onViewUserTickets: (userId: string) => void;
}

export function UsersTab({ users, onResetUsers, onSelectUserForTicket, onViewUserTickets }: UsersTabProps) {
  const [search, setSearch] = useState('');
  const filtered = users.filter((user) =>
    [user.name, user.username || '', user.email, user.id].some((value) => value.toLowerCase().includes(search.toLowerCase()))
  );

  return <div className="space-y-5">
    <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-5 flex items-center justify-between gap-4">
      <div>
        <h2 className="text-lg font-bold text-white">Tài khoản đã đăng ký</h2>
        <p className="text-xs text-slate-400">Người dùng đăng ký trên mobile; chọn tài khoản để cấp vé.</p>
      </div>
      <button type="button" onClick={onResetUsers} className="text-sm text-[#00d2ff] flex items-center gap-2">
        <RefreshCw className="h-4 w-4" /> Làm mới
      </button>
    </div>
    <label className="flex items-center gap-2 rounded-xl bg-[#121826] border border-[#1f293d] px-4 py-2">
      <Search className="h-4 w-4 text-slate-400" />
      <input className="flex-1 bg-transparent text-sm text-white outline-none" value={search}
        onChange={(event) => setSearch(event.target.value)} placeholder="Tìm theo tên, username hoặc email" />
    </label>
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {filtered.map((user) => <div key={user.id} className="rounded-2xl bg-[#121826] border border-[#1f293d] p-5 space-y-2">
        <div className="font-semibold text-white">{user.name}</div>
        <div className="text-sm text-[#00d2ff]">@{user.username}</div>
        <div className="text-xs text-slate-400">{user.email}</div>
        <div className="text-[11px] font-mono text-slate-500 break-all">{user.id}</div>
        <div className="flex gap-3 pt-2">
          <button type="button" onClick={() => onSelectUserForTicket(user.id)}
            className="rounded-lg bg-[#00e599] px-3 py-2 text-xs font-bold text-slate-950 flex items-center gap-1">
            <Ticket className="h-3.5 w-3.5" /> Cấp vé
          </button>
          <button type="button" onClick={() => onViewUserTickets(user.id)}
            className="rounded-lg border border-[#1f293d] px-3 py-2 text-xs text-slate-200">Xem vé</button>
        </div>
      </div>)}
    </div>
    {filtered.length === 0 && <p className="text-center text-sm text-slate-400 py-10">Không có tài khoản phù hợp.</p>}
  </div>;
}

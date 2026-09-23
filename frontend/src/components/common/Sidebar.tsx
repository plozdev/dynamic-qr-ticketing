import React from 'react';
import { Calendar, Ticket, Users, ScanLine } from 'lucide-react';


export type TabType = 'events' | 'tickets' | 'users' | 'scanner';

interface SidebarProps {
  activeTab: TabType;
  setActiveTab: (tab: TabType) => void;
  eventCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({ activeTab, setActiveTab, eventCount = 0 }) => {
  const tabs = [
    {
      id: 'events' as TabType,
      label: 'Quản Lý Sự Kiện',
      subtitle: 'Tạo & xem danh sách sự kiện',
      icon: Calendar,
      badge: eventCount > 0 ? `${eventCount}` : undefined,
    },
    {
      id: 'tickets' as TabType,
      label: 'Cấp Vé Thủ Công',
      subtitle: 'Gán vé cho User (Tạm thời)',
      icon: Ticket,
      tag: 'TẠM THỜI',
    },
    {
      id: 'users' as TabType,
      label: 'Quản Lý Tài Khoản',
      subtitle: 'Danh sách User / Demo App',
      icon: Users,
    },
    {
      id: 'scanner' as TabType,
      label: 'Giả Lập Soát Vé',
      subtitle: 'Kiểm tra Dynamic QR 30s',
      icon: ScanLine,
      tag: 'GATE SIM',
    },
  ];

  return (
    <aside className="w-full md:w-72 shrink-0 flex flex-col gap-6">
      {/* Navigation Menu */}
      <nav className="rounded-2xl bg-[#121826] border border-[#1f293d] p-3 flex flex-col gap-1.5 shadow-xl">
        <div className="px-3 py-2 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
          Menu Điều Hướng
        </div>
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`group relative flex items-center justify-between rounded-xl px-3.5 py-3 text-left transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-[#00e599]/15 to-transparent text-white border border-[#00e599]/30 shadow-[0_0_15px_-3px_rgba(0,229,153,0.15)]'
                  : 'text-slate-400 hover:bg-[#1a2234] hover:text-slate-200 border border-transparent'
              }`}
            >
              <div className="flex items-center gap-3">
                <div
                  className={`flex h-9 w-9 items-center justify-center rounded-lg transition-colors ${
                    isActive
                      ? 'bg-[#00e599]/20 text-[#00e599]'
                      : 'bg-[#1f293d]/60 text-slate-400 group-hover:text-white'
                  }`}
                >
                  <Icon className="h-4 w-4" />
                </div>
                <div>
                  <div className="text-sm font-semibold leading-tight">
                    {tab.label}
                  </div>
                  <div className="text-[11px] text-slate-500 mt-0.5">
                    {tab.subtitle}
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-1.5">
                {tab.badge && (
                  <span className="rounded-full bg-[#1f293d] px-2 py-0.5 text-[11px] font-mono font-medium text-slate-300">
                    {tab.badge}
                  </span>
                )}
                {tab.tag && (
                  <span
                    className={`rounded px-1.5 py-0.5 text-[10px] font-mono font-bold tracking-wider ${
                      tab.tag === 'TẠM THỜI'
                        ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                        : 'bg-[#00d2ff]/10 text-[#00d2ff] border border-[#00d2ff]/20'
                    }`}
                  >
                    {tab.tag}
                  </span>
                )}
              </div>

              {isActive && (
                <div className="absolute left-0 top-2 bottom-2 w-1 rounded-r bg-[#00e599] shadow-[0_0_8px_#00e599]" />
              )}
            </button>
          );
        })}
      </nav>
    </aside>
  );
};

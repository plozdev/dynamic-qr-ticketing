import React, { useState, useEffect } from 'react';
import { ShieldCheck, Server, RefreshCw, QrCode } from 'lucide-react';
import { checkHealth, API_BASE_URL } from '../../services/api';

interface HeaderProps {
  onRefreshAll?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onRefreshAll }) => {
  const [isBackendOnline, setIsBackendOnline] = useState<boolean | null>(null);
  const [checking, setChecking] = useState<boolean>(false);

  const performHealthCheck = async () => {
    setChecking(true);
    const online = await checkHealth();
    setIsBackendOnline(online);
    setChecking(false);
  };

  useEffect(() => {
    performHealthCheck();
    const interval = setInterval(performHealthCheck, 30000); // Check every 30s
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="sticky top-0 z-40 w-full border-b border-[#1f293d] bg-[#090d16]/90 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Brand Logo & Title */}
        <div className="flex items-center gap-3.5">
          <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#00e599]/20 to-[#00d2ff]/20 border border-[#00e599]/40 shadow-[0_0_15px_-2px_rgba(0,229,153,0.3)]">
            <ShieldCheck className="h-5 w-5 text-[#00e599]" />
            <div className="absolute -top-1 -right-1 h-2.5 w-2.5 rounded-full bg-[#00d2ff] animate-ping" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-[#00e599] bg-clip-text text-transparent">
                SecureTix
              </span>
              <span className="rounded-md bg-[#00e599]/10 px-2 py-0.5 text-[11px] font-semibold tracking-wide text-[#00e599] border border-[#00e599]/30">
                ADMIN PORTAL
              </span>
            </div>
            <p className="text-xs text-slate-400">
              Dynamic QR Anti-Fraud Ticketing Architecture
            </p>
          </div>
        </div>

        {/* Right Section: Status Indicator & Quick Actions */}
        <div className="flex items-center gap-3 sm:gap-4">
          {/* Backend Connection Indicator */}
          <div className="flex items-center gap-2 rounded-xl bg-[#121826] border border-[#1f293d] px-3 py-1.5 text-xs">
            <Server className="h-3.5 w-3.5 text-slate-400" />
            <span className="hidden sm:inline text-slate-400 font-mono text-[11px]">
              {API_BASE_URL.replace('http://', '')}
            </span>
            <div className="flex items-center gap-1.5 ml-1">
              <span
                className={`h-2.5 w-2.5 rounded-full ${
                  isBackendOnline === true
                    ? 'bg-[#00e599] shadow-[0_0_8px_#00e599]'
                    : isBackendOnline === false
                    ? 'bg-rose-500 shadow-[0_0_8px_#ef4444]'
                    : 'bg-amber-400 animate-pulse'
                }`}
              />
              <span
                className={`text-[11px] font-medium ${
                  isBackendOnline === true
                    ? 'text-[#00e599]'
                    : isBackendOnline === false
                    ? 'text-rose-400'
                    : 'text-amber-400'
                }`}
              >
                {isBackendOnline === true
                  ? 'API Online'
                  : isBackendOnline === false
                  ? 'Mất kết nối API'
                  : 'Kiểm tra...'}
              </span>
            </div>
            <button
              type="button"
              onClick={performHealthCheck}
              disabled={checking}
              title="Kiểm tra lại kết nối Backend"
              className="text-slate-400 hover:text-white transition-colors ml-1 p-0.5"
            >
              <RefreshCw className={`h-3 w-3 ${checking ? 'animate-spin' : ''}`} />
            </button>
          </div>

          {/* Quick Refresh All Data */}
          {onRefreshAll && (
            <button
              type="button"
              onClick={onRefreshAll}
              className="hidden md:flex items-center gap-1.5 rounded-xl bg-[#1f293d]/50 hover:bg-[#1f293d] border border-[#1f293d] px-3 py-1.5 text-xs text-slate-300 hover:text-white transition-colors"
            >
              <RefreshCw className="h-3.5 w-3.5" />
              <span>Đồng bộ</span>
            </button>
          )}

          {/* Dynamic QR Badge */}
          <div className="hidden lg:flex items-center gap-2 rounded-xl bg-gradient-to-r from-[#00d2ff]/10 to-transparent border border-[#00d2ff]/20 px-3 py-1.5">
            <QrCode className="h-3.5 w-3.5 text-[#00d2ff]" />
            <span className="text-[11px] text-[#00d2ff] font-medium">
              30s TOTP Rotation Active
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};

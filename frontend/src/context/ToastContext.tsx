import React, { createContext, useContext, useState, useCallback } from 'react';
import type { ToastMessage } from '../types';

import { CheckCircle2, AlertCircle, Info, AlertTriangle, X } from 'lucide-react';

interface ToastContextType {
  toasts: ToastMessage[];
  showToast: (toast: Omit<ToastMessage, 'id'>) => void;
  success: (title: string, description?: string) => void;
  error: (title: string, description?: string) => void;
  info: (title: string, description?: string) => void;
  warning: (title: string, description?: string) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    ({ type, title, description, duration = 4000 }: Omit<ToastMessage, 'id'>) => {
      const id = `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
      const newToast: ToastMessage = { id, type, title, description, duration };

      setToasts((prev) => [newToast, ...prev].slice(0, 5)); // Keep max 5 toasts

      if (duration > 0) {
        setTimeout(() => {
          removeToast(id);
        }, duration);
      }
    },
    [removeToast]
  );

  const success = useCallback((title: string, description?: string) => {
    showToast({ type: 'success', title, description });
  }, [showToast]);

  const error = useCallback((title: string, description?: string) => {
    showToast({ type: 'error', title, description, duration: 6000 });
  }, [showToast]);

  const info = useCallback((title: string, description?: string) => {
    showToast({ type: 'info', title, description });
  }, [showToast]);

  const warning = useCallback((title: string, description?: string) => {
    showToast({ type: 'warning', title, description });
  }, [showToast]);

  return (
    <ToastContext.Provider
      value={{ toasts, showToast, success, error, info, warning, removeToast }}
    >
      {children}
      {/* Toast Notification Container */}
      <div 
        role="region" 
        aria-live="polite"
        className="fixed bottom-5 right-5 z-50 flex flex-col gap-2.5 max-w-md w-full pointer-events-none px-4 sm:px-0"
      >
        {toasts.map((toast) => {
          const style = {
            success: { border: 'border-[#00e599]/40', glow: 'shadow-[0_0_20px_-3px_rgba(0,229,153,0.3)]', icon: <CheckCircle2 className="w-5 h-5 text-[#00e599] shrink-0" /> },
            error: { border: 'border-rose-500/40', glow: 'shadow-[0_0_20px_-3px_rgba(244,63,94,0.35)]', icon: <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" /> },
            warning: { border: 'border-amber-500/40', glow: 'shadow-[0_0_20px_-3px_rgba(245,158,11,0.3)]', icon: <AlertTriangle className="w-5 h-5 text-amber-400 shrink-0" /> },
            info: { border: 'border-[#00d2ff]/40', glow: 'shadow-[0_0_20px_-3px_rgba(0,210,255,0.3)]', icon: <Info className="w-5 h-5 text-[#00d2ff] shrink-0" /> },
          }[toast.type];

          return (
            <div
              key={toast.id}
              className={`pointer-events-auto bg-[#121826]/95 backdrop-blur-md border ${style.border} ${style.glow} rounded-xl p-4 flex items-start gap-3 transition-all duration-300 animate-in fade-in slide-in-from-bottom-3`}
            >
              <div className="mt-0.5">{style.icon}</div>
              <div className="flex-1 min-w-0">
                <h4 className="text-sm font-semibold text-slate-100 leading-tight">
                  {toast.title}
                </h4>
                {toast.description && (
                  <p className="text-xs text-slate-400 mt-1 break-words leading-relaxed">
                    {toast.description}
                  </p>
                )}
              </div>
              <button
                type="button"
                onClick={() => removeToast(toast.id)}
                className="text-slate-400 hover:text-slate-200 transition-colors p-1 rounded-lg hover:bg-slate-800/60"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
};

// Provider and hook intentionally share the same context module.
// eslint-disable-next-line react-refresh/only-export-components
export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

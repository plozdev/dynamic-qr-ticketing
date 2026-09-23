import React, { useState } from 'react';
import { 
  ScanLine, 
  DoorOpen, 
  CheckCircle2, 
  XCircle, 
  Sparkles, 
  History, 
  Trash2, 
  Loader2, 
  Radio, 
  ShieldAlert 
} from 'lucide-react';
import type { GateValidateResponse, GateScanHistoryItem, Ticket } from '../../types';
import { validateGateQr, getApiErrorMessage } from '../../services/api';
import { useToast } from '../../context/ToastContext';


interface GateScannerTesterProps {
  initialTicket?: Ticket | null;
  onTicketStatusChanged?: () => void;
}

export const GateScannerTester: React.FC<GateScannerTesterProps> = ({
  initialTicket,
  onTicketStatusChanged,
}) => {
  const { success: toastSuccess, error: toastError } = useToast();

  const [gateId, setGateId] = useState('GATE_A1');
  const [rawPayload, setRawPayload] = useState('');
  const [loading, setLoading] = useState(false);

  // Scan result state
  const [lastResult, setLastResult] = useState<{
    status: 'IDLE' | 'SUCCESS' | 'REJECTED';
    data?: GateValidateResponse;
    errorMsg?: string;
    ticketId?: string;
  }>({ status: 'IDLE' });

  // Session Scan History
  const [history, setHistory] = useState<GateScanHistoryItem[]>([]);

  // Quick helper to generate a sample payload: TICKETING:<ticketId>:<expiresAt>:<totpToken>
  const handleGenerateSamplePayload = (isExpired: boolean = false) => {
    const tId = initialTicket?.id || '8a91b2c3-4d5e-6f70-8192-a1b2c3d4e5f6';
    // If expired, generate a timestamp from 2 minutes ago; else now + 30 seconds
    const nowSec = Math.floor(Date.now() / 1000);
    const expiresAt = isExpired ? nowSec - 120 : nowSec + 30;
    const totpToken = Math.floor(100000 + Math.random() * 900000).toString();

    const sample = `TICKETING:${tId}:${expiresAt}:${totpToken}`;
    setRawPayload(sample);
  };

  const handleValidate = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const trimmed = rawPayload.trim();

    if (!trimmed) {
      toastError('Thiếu QR Payload', 'Vui lòng dán chuỗi mã Dynamic QR quét được từ điện thoại!');
      return;
    }

    try {
      setLoading(true);
      const res = await validateGateQr(gateId.trim(), { rawQrPayload: trimmed });

      // Check if ticket ID is in payload or returned in response
      let extractedTicketId = res.ticketId;
      if (!extractedTicketId && trimmed.startsWith('TICKETING:')) {
        const parts = trimmed.split(':');
        if (parts.length >= 2) extractedTicketId = parts[1];
      }

      setLastResult({
        status: 'SUCCESS',
        data: res,
        ticketId: extractedTicketId,
      });

      toastSuccess(
        'HỢP LỆ • MỜI VÀO CỔNG',
        `Vé ${extractedTicketId || ''} đã qua cổng ${gateId} thành công.`
      );

      // Add to session scan history
      const historyItem: GateScanHistoryItem = {
        id: Date.now().toString(),
        timestamp: new Date().toLocaleTimeString('vi-VN'),
        gateId,
        rawPayload: trimmed,
        success: true,
        ticketId: extractedTicketId,
        message: res.message || 'MỜI VÀO CỔNG • HỢP LỆ',
      };
      setHistory((prev) => [historyItem, ...prev].slice(0, 20));

      if (onTicketStatusChanged) {
        onTicketStatusChanged();
      }
    } catch (err) {
      const errMsg = getApiErrorMessage(
        err,
        'Cổng soát vé từ chối: Mã không hợp lệ, đã sử dụng hoặc hết hạn 30s!'
      );

      let extractedTicketId: string | undefined;
      if (trimmed.startsWith('TICKETING:')) {
        const parts = trimmed.split(':');
        if (parts.length >= 2) extractedTicketId = parts[1];
      }

      setLastResult({
        status: 'REJECTED',
        errorMsg: errMsg,
        ticketId: extractedTicketId,
      });

      toastError('TỪ CHỐI QUA CỔNG', errMsg);

      const historyItem: GateScanHistoryItem = {
        id: Date.now().toString(),
        timestamp: new Date().toLocaleTimeString('vi-VN'),
        gateId,
        rawPayload: trimmed,
        success: false,
        ticketId: extractedTicketId,
        message: 'TỪ CHỐI QUA CỔNG',
        reason: errMsg,
      };
      setHistory((prev) => [historyItem, ...prev].slice(0, 20));
    } finally {
      setLoading(false);
    }
  };

  return (

    <div className="space-y-6">
      {/* Console Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 rounded-2xl bg-[#121826] border border-[#1f293d] p-5 shadow-xl">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-[#00d2ff]/20 to-[#00e599]/20 text-[#00d2ff] border border-[#00d2ff]/30">
            <ScanLine className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <span>Bảng Điều Khiển Cổng Soát Vé (Gate Validator Console)</span>
              <span className="flex h-2 w-2 rounded-full bg-[#00e599] animate-pulse" />
            </h2>
            <p className="text-xs text-slate-400">
              Giả lập đầu đọc mã QR tại cổng để kiểm tra thuật toán Dynamic QR xoay vòng 30s & chống Replay Attack
            </p>
          </div>
        </div>

        {initialTicket && (
          <div className="flex items-center gap-2 rounded-xl bg-[#090d16] border border-[#00d2ff]/30 px-3 py-1.5 text-xs">
            <span className="text-slate-400">Đang test vé:</span>
            <span className="font-mono text-[#00d2ff] font-bold">
              {initialTicket.attendeeName} ({initialTicket.seatNumber})
            </span>
          </div>
        )}
      </div>

      {/* Main Scanner Section */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column: Validator Input & Result Display (7 cols) */}
        <div className="lg:col-span-7 space-y-5">
          {/* Input Panel */}
          <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-6 shadow-xl">
            <form onSubmit={handleValidate} className="space-y-4 text-xs">
              {/* Gate Selection */}
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5 flex items-center gap-1.5">
                  <DoorOpen className="h-3.5 w-3.5 text-[#00e599]" />
                  <span>Mã cổng soát vé (Gate ID)</span> <span className="text-[#00e599]">*</span>
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    required
                    value={gateId}
                    onChange={(e) => setGateId(e.target.value)}
                    placeholder="VD: GATE_A1, CỔNG CHÍNH"
                    className="flex-1 rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00e599] focus:outline-none px-3.5 py-2.5 text-xs font-mono text-white"
                  />
                  <div className="flex items-center gap-1">
                    {['GATE_A1', 'GATE_B2', 'VIP_GATE'].map((g) => (
                      <button
                        key={g}
                        type="button"
                        onClick={() => setGateId(g)}
                        className={`px-2.5 py-2 rounded-xl text-[11px] font-mono transition-colors ${
                          gateId === g
                            ? 'bg-[#00e599]/20 text-[#00e599] border border-[#00e599]/40'
                            : 'bg-[#090d16] text-slate-400 hover:text-white border border-[#1f293d]'
                        }`}
                      >
                        {g}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* QR Payload Input */}
              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <ScanLine className="h-3.5 w-3.5 text-[#00d2ff]" />
                    <span>Chuỗi QR Payload (Quét từ app điện thoại)</span> <span className="text-[#00e599]">*</span>
                  </label>
                  
                  {/* Quick Generator Buttons */}
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleGenerateSamplePayload(false)}
                      className="flex items-center gap-1 text-[11px] text-[#00d2ff] hover:text-[#00e599] transition-colors"
                      title="Tạo chuỗi QR mẫu hợp lệ (TOTP + 30s)"
                    >
                      <Sparkles className="h-3 w-3" />
                      <span>Sinh mã hợp lệ mẫu</span>
                    </button>
                    <span className="text-slate-600">|</span>
                    <button
                      type="button"
                      onClick={() => handleGenerateSamplePayload(true)}
                      className="text-[11px] text-rose-400 hover:text-rose-300 transition-colors"
                      title="Tạo chuỗi QR mẫu đã quá hạn để thử nghiệm mã lỗi"
                    >
                      <span>Mã hết hạn</span>
                    </button>
                  </div>
                </div>

                <textarea
                  rows={3}
                  required
                  value={rawPayload}
                  onChange={(e) => setRawPayload(e.target.value)}
                  placeholder="Dán chuỗi quét được tại đây (Dạng: TICKETING:<ticketId>:<expiresAt>:<totpToken>)..."
                  className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] p-3 text-xs font-mono text-white placeholder-slate-500 leading-relaxed"
                />
                <p className="text-[10px] text-slate-500 mt-1">
                  Định dạng chuẩn: <code className="text-slate-400">TICKETING:&lt;UUID&gt;:&lt;UNIX_TIMESTAMP&gt;:&lt;6_DIGIT_TOTP&gt;</code>
                </p>
              </div>

              {/* Submit Button */}
              <button
                type="submit"
                disabled={loading || !rawPayload.trim()}
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-[#00e599] via-[#00d2ff] to-[#00e599] bg-[length:200%_auto] hover:bg-right text-slate-950 py-3 text-xs font-bold shadow-[0_0_20px_-3px_rgba(0,229,153,0.35)] transition-all duration-300 disabled:opacity-50"
              >
                {loading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    <span>Đang thẩm định vé tại cổng {gateId}...</span>
                  </>
                ) : (
                  <>
                    <Radio className="h-4 w-4" />
                    <span>Xác Thực Qua Cổng (POST Validate QR)</span>
                  </>
                )}
              </button>
            </form>
          </div>

          {/* Validation Result Box */}
          {lastResult.status === 'SUCCESS' && (
            <div className="rounded-2xl bg-gradient-to-b from-[#00e599]/15 to-[#121826] border-2 border-[#00e599] p-6 shadow-[0_0_30px_-5px_rgba(0,229,153,0.4)] animate-in fade-in slide-in-from-top-3">
              <div className="flex items-start gap-4">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#00e599]/20 text-[#00e599] border border-[#00e599]/40 shadow-[0_0_20px_#00e599] shrink-0">
                  <CheckCircle2 className="h-8 w-8" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="rounded-full bg-[#00e599] text-slate-950 font-black text-xs px-2.5 py-0.5 tracking-wider">
                      200 OK
                    </span>
                    <span className="text-xs font-mono text-[#00e599] font-bold">
                      CỔNG: {gateId}
                    </span>
                  </div>
                  <h3 className="text-xl font-extrabold text-[#00e599] mt-1 tracking-tight">
                    HỢP LỆ • MỜI VÀO CỔNG
                  </h3>
                  <p className="text-xs text-slate-300 mt-1">
                    {lastResult.data?.message || 'Mã Dynamic QR hợp lệ, TOTP chính xác, chưa từng quét qua cổng.'}
                  </p>

                  <div className="mt-4 pt-3 border-t border-[#00e599]/30 grid grid-cols-2 gap-3 text-xs">
                    <div>
                      <span className="text-slate-400">Mã vé (Ticket ID):</span>
                      <div className="font-mono text-white text-[11px] truncate" title={lastResult.ticketId}>
                        {lastResult.ticketId || 'N/A'}
                      </div>
                    </div>
                    <div>
                      <span className="text-slate-400">Thời gian quét:</span>
                      <div className="font-mono text-white text-[11px]">
                        {new Date().toLocaleTimeString('vi-VN')}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {lastResult.status === 'REJECTED' && (
            <div className="rounded-2xl bg-gradient-to-b from-rose-500/15 to-[#121826] border-2 border-rose-500 p-6 shadow-[0_0_30px_-5px_rgba(244,63,94,0.4)] animate-in fade-in slide-in-from-top-3">
              <div className="flex items-start gap-4">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-rose-500/20 text-rose-400 border border-rose-500/40 shadow-[0_0_20px_#ef4444] shrink-0">
                  <XCircle className="h-8 w-8" />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="rounded-full bg-rose-500 text-white font-black text-xs px-2.5 py-0.5 tracking-wider">
                      TỪ CHỐI (REJECTED)
                    </span>
                    <span className="text-xs font-mono text-rose-400 font-bold">
                      CỔNG: {gateId}
                    </span>
                  </div>
                  <h3 className="text-xl font-extrabold text-rose-400 mt-1 tracking-tight">
                    CẢNH BÁO • VÉ KHÔNG HỢP LỆ
                  </h3>
                  <div className="rounded-xl bg-[#090d16]/80 border border-rose-500/40 p-3 mt-3 text-xs text-rose-200">
                    <strong className="text-rose-400 block mb-0.5">Lý do từ chối:</strong>
                    {lastResult.errorMsg}
                  </div>

                  <p className="text-[11px] text-slate-400 mt-3 leading-relaxed">
                    Hệ thống đã ghi nhận nhật ký an ninh. Các nguyên nhân phổ biến: Vé đã được sử dụng trước đó, mã QR xoay vòng 30s đã quá hạn, hoặc có dấu hiệu chụp màn hình quay video (Replay Attack).
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Scan History & Architecture Details (5 cols) */}
        <div className="lg:col-span-5 space-y-5">
          {/* Session Scan History Card */}
          <div className="rounded-2xl bg-[#121826] border border-[#1f293d] p-5 shadow-xl">
            <div className="flex items-center justify-between pb-3 mb-3 border-b border-[#1f293d]">
              <div className="flex items-center gap-2">
                <History className="h-4 w-4 text-[#00d2ff]" />
                <h4 className="text-sm font-bold text-white">Nhật Ký Quét Trong Phiên</h4>
              </div>
              {history.length > 0 && (
                <button
                  type="button"
                  onClick={() => setHistory([])}
                  className="text-[11px] text-slate-500 hover:text-rose-400 transition-colors flex items-center gap-1"
                >
                  <Trash2 className="h-3 w-3" />
                  <span>Xóa lịch sử</span>
                </button>
              )}
            </div>

            <div className="space-y-2.5 max-h-[360px] overflow-y-auto pr-1">
              {history.length > 0 ? (
                history.map((item) => (
                  <div
                    key={item.id}
                    className={`rounded-xl p-3 border text-xs transition-all ${
                      item.success
                        ? 'bg-[#00e599]/5 border-[#00e599]/30'
                        : 'bg-rose-500/5 border-rose-500/30'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <div className="flex items-center gap-1.5 font-bold">
                        {item.success ? (
                          <CheckCircle2 className="h-3.5 w-3.5 text-[#00e599]" />
                        ) : (
                          <XCircle className="h-3.5 w-3.5 text-rose-400" />
                        )}
                        <span className={item.success ? 'text-[#00e599]' : 'text-rose-400'}>
                          {item.success ? 'HỢP LỆ' : 'TỪ CHỐI'}
                        </span>
                        <span className="font-mono text-slate-400 text-[10px]">
                          ({item.gateId})
                        </span>
                      </div>
                      <span className="font-mono text-slate-500 text-[10px]">
                        {item.timestamp}
                      </span>
                    </div>

                    {item.ticketId && (
                      <div className="font-mono text-[10px] text-slate-300 truncate">
                        Ticket: {item.ticketId}
                      </div>
                    )}

                    {item.reason && (
                      <div className="text-[10px] text-rose-300/80 mt-1 line-clamp-2">
                        {item.reason}
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <div className="py-8 text-center text-slate-500 text-xs">
                  Chưa có lượt quét nào trong phiên làm việc này
                </div>
              )}
            </div>
          </div>

          {/* Security Principles Explainer */}
          <div className="rounded-2xl bg-gradient-to-br from-[#121826] to-[#090d16] border border-[#1f293d] p-5 text-xs">
            <h4 className="font-bold text-white flex items-center gap-2 mb-2">
              <ShieldAlert className="h-4 w-4 text-[#00e599]" />
              <span>Cơ Chế Bảo Vệ Chống Gian Lận Vé</span>
            </h4>
            <ul className="space-y-2 text-slate-400 text-[11px] leading-relaxed">
              <li className="flex items-start gap-2">
                <span className="h-1.5 w-1.5 rounded-full bg-[#00e599] mt-1.5 shrink-0" />
                <span><strong className="text-slate-200">TOTP 30s:</strong> Mã QR thay đổi liên tục trên ứng dụng di động mỗi 30 giây dựa trên thuật toán mật mã thời gian thực.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="h-1.5 w-1.5 rounded-full bg-[#00d2ff] mt-1.5 shrink-0" />
                <span><strong className="text-slate-200">Anti-Replay Attack:</strong> Mỗi mã Dynamic QR chỉ được quét qua cổng đúng 1 lần duy nhất trong chu kỳ sống.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="h-1.5 w-1.5 rounded-full bg-amber-400 mt-1.5 shrink-0" />
                <span><strong className="text-slate-200">Gate Access Control:</strong> Chỉ cho phép qua đúng các cổng đã được đăng ký trong danh sách cổng của sự kiện.</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

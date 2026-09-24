import React, { useState } from 'react';
import jsQR from 'jsqr';
import { 
  ScanLine, 
  DoorOpen, 
  CheckCircle2, 
  XCircle, 
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

  const decodeQrImage = async (image: Blob) => {
    try {
      const bitmap = await createImageBitmap(image);
      try {
        const canvas = document.createElement('canvas');
        canvas.width = bitmap.width;
        canvas.height = bitmap.height;
        const context = canvas.getContext('2d', { willReadFrequently: true });
        if (!context) throw new Error('Không đọc được dữ liệu ảnh.');
        context.drawImage(bitmap, 0, 0);
        const pixels = context.getImageData(0, 0, canvas.width, canvas.height);
        const decoded = jsQR(pixels.data, pixels.width, pixels.height);
        if (!decoded?.data) throw new Error('Không tìm thấy QR trong ảnh.');
        setRawPayload(decoded.data);
      } finally { bitmap.close(); }
    } catch (error) {
      toastError('Không đọc được ảnh QR', getApiErrorMessage(error, 'Không tìm thấy QR trong ảnh.'));
    }
  };

  const handlePaste = (event: React.ClipboardEvent<HTMLTextAreaElement>) => {
    const image = Array.from(event.clipboardData.items).find((item) => item.type.startsWith('image/'))?.getAsFile();
    if (!image) return;
    event.preventDefault();
    void decodeQrImage(image);
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
      if (res.success !== true) throw new Error(res.message || 'Backend từ chối mã QR.');

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

      // Record only the result returned by the backend.
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
      const errMsg = navigator.onLine
        ? getApiErrorMessage(err, 'Backend từ chối mã QR.')
        : 'FE đang ngoại tuyến nên không thể xác thực QR với Backend.';

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
                  
                  <span className="text-[11px] text-slate-400">Dán chuỗi QR hoặc ảnh QR từ mobile. Kết quả chỉ do Backend xác nhận.</span>
                </div>

                <textarea
                  rows={3}
                  required
                  value={rawPayload}
                  onChange={(e) => setRawPayload(e.target.value)}
                  onPaste={handlePaste}
                  placeholder="Dán chuỗi quét được tại đây (Dạng: TICKETING:<ticketId>:<expiresAt>:<totpToken>)..."
                  className="w-full rounded-xl bg-[#090d16] border border-[#1f293d] focus:border-[#00d2ff] focus:outline-none focus:ring-1 focus:ring-[#00d2ff] p-3 text-xs font-mono text-white placeholder-slate-500 leading-relaxed"
                />
                <label className="mt-2 inline-flex cursor-pointer items-center rounded-lg border border-[#1f293d] px-3 py-2 text-slate-300 hover:text-white">
                  Tải ảnh QR từ điện thoại
                  <input type="file" accept="image/*" className="sr-only" onChange={(event) => {
                    const file = event.target.files?.[0];
                    if (file) void decodeQrImage(file);
                    event.target.value = '';
                  }} />
                </label>
                <p className="text-[10px] text-slate-500 mt-1">
                  Định dạng chuẩn: <code className="text-slate-400">TICKETING:&lt;UUID&gt;:&lt;UNIX_TIMESTAMP&gt;:&lt;HMAC_SHA256_BASE64URL&gt;</code>
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
                    FE chỉ hiển thị kết quả từ Backend. Nếu mất mạng, mã QR chưa được xác thực và không có lượt quét thành công.
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
                <span><strong className="text-slate-200">Xác thực online:</strong> Cổng giả lập gửi payload tới Backend; khi mất mạng, FE không thể cho vé qua cổng.</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

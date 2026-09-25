import { useEffect, useMemo, useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import {
  CalendarDays,
  CheckCircle2,
  Clock3,
  MapPin,
  RefreshCw,
  ShieldCheck,
  Ticket as TicketIcon,
} from "lucide-react";
import type { Event, Ticket, DynamicQr } from "../../types";
import { fetchDynamicQr, getApiErrorMessage } from "../../services/api";

interface Props {
  tickets: Ticket[];
  events: Event[];
  loading: boolean;
  onRefresh: () => void;
}
const short = (value: string) => `${value.slice(0, 8)}…${value.slice(-4)}`;

export function Wallet({ tickets, events, loading, onRefresh }: Props) {
  const [selectedId, setSelectedId] = useState("");
  const [filter, setFilter] = useState<"upcoming" | "past" | "all">("upcoming");
  const [qr, setQr] = useState<DynamicQr | null>(null);
  const [qrError, setQrError] = useState("");
  const [hash, setHash] = useState<{ payload: string; value: string } | null>(
    null,
  );
  const [now, setNow] = useState(() => Date.now());
  const visible = useMemo(
    () =>
      tickets.filter(
        (ticket) =>
          filter === "all" ||
          (filter === "past"
            ? ticket.status === "CHECKED_IN" || ticket.status === "REVOKED"
            : ticket.status !== "CHECKED_IN" && ticket.status !== "REVOKED"),
      ),
    [tickets, filter],
  );
  const selected =
    visible.find((ticket) => ticket.id === selectedId) || visible[0];
  const event = events.find((item) => item.id === selected?.eventId);
  const activeQr =
    qr?.ticketId === selected?.id && selected?.status === "READY_TO_CHECK_IN"
      ? qr
      : null;
  const remaining = activeQr
    ? Math.max(0, Math.ceil(activeQr.expiresAtEpochSeconds - now / 1000))
    : 0;

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    let active = true;
    let loadingQr = false;
    const ticketId = selected?.id;
    if (!ticketId || selected?.status !== "READY_TO_CHECK_IN") return;
    let expiresAt = 0;
    const refresh = async () => {
      if (loadingQr) return;
      loadingQr = true;
      try {
        const next = await fetchDynamicQr(ticketId);
        if (active) {
          expiresAt = next.expiresAtEpochSeconds;
          setQr(next);
          setQrError("");
        }
      } catch (cause) {
        if (active) {
          expiresAt = Date.now() / 1000 + 5;
          setQr(null);
          setQrError(getApiErrorMessage(cause));
        }
      } finally {
        loadingQr = false;
      }
    };
    void refresh();
    const interval = window.setInterval(() => {
      if (!active) return;
      if (Date.now() / 1000 >= expiresAt) void refresh();
    }, 1000);
    return () => {
      active = false;
      window.clearInterval(interval);
    };
  }, [selected?.id, selected?.status]);

  useEffect(() => {
    let active = true;
    if (!qr?.dynamicPayload) return;
    const payload = qr.dynamicPayload;
    const bytes = new TextEncoder().encode(payload);
    void crypto.subtle
      .digest("SHA-256", bytes)
      .then((buffer) => {
        if (active)
          setHash({
            payload,
            value: Array.from(new Uint8Array(buffer))
              .map((byte) => byte.toString(16).padStart(2, "0"))
              .join("")
              .slice(0, 20)
              .toUpperCase(),
          });
      })
      .catch(() => {
        /* Web Crypto may be unavailable outside secure origins. */
      });
    return () => {
      active = false;
    };
  }, [qr?.dynamicPayload]);

  return (
    <div className="space-y-7">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">MY PASSES / LIVE WALLET</p>
          <h1 className="page-title">Tủ vé cá nhân</h1>
          <p className="muted mt-2">
            Vé và trạng thái được đồng bộ từ máy chủ.
          </p>
        </div>
        <button className="btn-secondary" onClick={onRefresh}>
          <RefreshCw size={16} /> Làm mới vé
        </button>
      </div>
      <div className="wallet-tabs">
        {(
          [
            ["upcoming", "Sắp diễn ra"],
            ["past", "Đã dùng / Hết hạn"],
            ["all", "Tất cả"],
          ] as const
        ).map(([key, label]) => (
          <button
            key={key}
            className={filter === key ? "active" : ""}
            onClick={() => setFilter(key)}
          >
            {label}
          </button>
        ))}
      </div>
      {loading ? (
        <div className="empty-state">Đang tải vé...</div>
      ) : visible.length === 0 ? (
        <div className="empty-state">
          <TicketIcon size={28} /> Chưa có vé trong mục này.
        </div>
      ) : (
        <div className="wallet-layout">
          <aside className="wallet-list">
            {visible.map((ticket) => (
              <button
                key={ticket.id}
                className={`wallet-list-item ${selected?.id === ticket.id ? "active" : ""}`}
                onClick={() => setSelectedId(ticket.id)}
              >
                <span className="wallet-list-icon">
                  <TicketIcon size={20} />
                </span>
                <span>
                  <strong>{ticket.eventName}</strong>
                  <small>
                    {ticket.categoryName} · {short(ticket.id)}
                  </small>
                </span>
                <span
                  className={`wallet-dot ${ticket.status === "READY_TO_CHECK_IN" ? "ready" : ""}`}
                />
              </button>
            ))}
          </aside>
          {selected && (
            <article className="pass-stub">
              <div
                className="pass-header"
                style={
                  event?.bannerUrl
                    ? {
                        backgroundImage: `linear-gradient(0deg, #151f30 12%, rgba(21,31,48,.3)), url('${event.bannerUrl}')`,
                      }
                    : undefined
                }
              >
                <span className="pass-gate">
                  {selected.gateInfo || "CỔNG THEO VÉ"}
                </span>
                <p>VÉ ĐIỆN TỬ CHÍNH THỨC</p>
                <h2>{selected.eventName}</h2>
                <div>
                  <CalendarDays size={15} />{" "}
                  {selected.issuedAt
                    ? new Date(selected.issuedAt).toLocaleString("vi-VN", {
                        dateStyle: "medium",
                        timeStyle: "short",
                      })
                    : "Đang cập nhật"}{" "}
                  <MapPin size={15} /> {selected.venueName || event?.venueName}
                </div>
              </div>
              <div className="pass-perforation" />
              <div className="pass-main">
                <p className="eyebrow">
                  {selected.status === "READY_TO_CHECK_IN"
                    ? "LIVE TICKET / READY TO SCAN"
                    : selected.status === "CHECKED_IN"
                      ? "TICKET USED"
                      : "TICKET STATUS"}
                </p>
                {selected.status === "READY_TO_CHECK_IN" ? (
                  <>
                    <div className="qr-frame">
                      {activeQr && remaining > 0 ? (
                        <QRCodeSVG
                          value={activeQr.dynamicPayload}
                          size={206}
                          marginSize={2}
                          level="M"
                        />
                      ) : (
                        <div className="qr-placeholder">
                          {qrError || "Đang lấy mã QR từ máy chủ..."}
                        </div>
                      )}
                    </div>
                    <div
                      className="countdown"
                      style={{
                        background: `conic-gradient(#4edea3 ${(remaining / (activeQr?.refreshIntervalSeconds || 30)) * 360}deg, #283247 0deg)`,
                      }}
                    >
                      <span>{remaining}s</span>
                    </div>
                    <p className="muted small-text">
                      Mã tự cập nhật theo thời điểm hết hạn do BE trả về.
                    </p>
                    <div className="hash-line">
                      <span>SHA-256</span>
                      <code>
                        {hash?.payload === activeQr?.dynamicPayload
                          ? hash?.value
                          : "Đang tính..."}
                      </code>
                    </div>
                  </>
                ) : selected.status === "CHECKED_IN" ? (
                  <div className="pass-status success">
                    <CheckCircle2 size={42} />
                    <strong>ĐÃ CHECK-IN THÀNH CÔNG</strong>
                    <span>
                      {selected.checkInNote || "Vé đã được sử dụng tại cổng."}
                    </span>
                  </div>
                ) : (
                  <div className="pass-status">
                    <Clock3 size={42} />
                    <strong>
                      {selected.status === "REVOKED"
                        ? "VÉ KHÔNG CÒN HIỆU LỰC"
                        : "CHƯA MỞ CHECK-IN"}
                    </strong>
                    <span>
                      {selected.checkInNote ||
                        "Mã QR chỉ hiển thị khi ban tổ chức mở cổng."}
                    </span>
                  </div>
                )}
                <div className="pass-info">
                  <div>
                    <small>HẠNG VÉ</small>
                    <strong>{selected.categoryName}</strong>
                  </div>
                  <div>
                    <small>GHẾ</small>
                    <strong>{selected.seatNumber || "GA"}</strong>
                  </div>
                  <div>
                    <small>CHỦ VÉ</small>
                    <strong>{selected.attendeeName}</strong>
                  </div>
                </div>
                <p className="pass-id">
                  <ShieldCheck size={14} /> ID: {selected.id}
                </p>
              </div>
            </article>
          )}
          <aside className="wallet-side">
            <div className="info-panel">
              <ShieldCheck size={20} />
              <h3>Mã vé động</h3>
              <p>
                Mã QR được tạo và kiểm tra bởi BE. Ảnh chụp màn hình sẽ hết hiệu
                lực khi cửa sổ thời gian kết thúc.
              </p>
            </div>
            <div className="info-panel">
              <h3>Trước khi đến cổng</h3>
              <p>
                Mở vé khi check-in được bật. Giữ kết nối mạng để tải mã mới
                trong bản web.
              </p>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}

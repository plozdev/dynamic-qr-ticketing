import { useMemo, useState } from "react";
import {
  CalendarDays,
  CheckCircle2,
  Clock3,
  MapPin,
  RefreshCw,
  ShieldCheck,
  Smartphone,
  Ticket as TicketIcon,
} from "lucide-react";
import type { Event, Ticket } from "../../types";

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
                    ? "LIVE PASS / READY TO SCAN"
                    : selected.status === "CHECKED_IN"
                      ? "TICKET USED"
                      : "TICKET STATUS"}
                </p>
                {selected.status === "READY_TO_CHECK_IN" ? (
                  <div className="pass-status ready">
                    <Smartphone size={42} />
                    <strong>SẴN SÀNG CHECK-IN TRÊN APP</strong>
                    <span style={{ maxWidth: "260px", textAlign: "center", lineHeight: "1.4" }}>
                      Mã QR động bảo mật cao chỉ hiển thị trên ứng dụng di động CyberPass. Vui lòng mở ứng dụng tại cổng soát vé.
                    </span>
                  </div>
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
                        "Cổng soát vé chưa mở cho sự kiện này."}
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
              <h3>Mã vé động bảo mật</h3>
              <p>
                Mã QR động được tạo độc quyền trên ứng dụng CyberPass Mobile với cơ chế xoay vòng TOTP và mã hoá HMAC. Ảnh chụp màn hình hoặc in giấy sẽ không hợp lệ.
              </p>
            </div>
            <div className="info-panel">
              <Smartphone size={20} />
              <h3>Trước khi đến cổng</h3>
              <p>
                Đăng nhập tài khoản trên ứng dụng di động CyberPass để mở mã QR động trước khi vào cổng soát vé.
              </p>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}

import { useEffect, useState } from "react";
import {
  Activity,
  AlertTriangle,
  ArrowLeft,
  ArrowRight,
  DoorOpen,
  RefreshCw,
  Ticket,
  Users,
} from "lucide-react";
import type { Event, GateDashboard as DashboardData } from "../../types";
import { fetchGateDashboard, getApiErrorMessage } from "../../services/api";

interface Props {
  events: Event[];
  eventId: string;
  onEventChange: (id: string) => void;
  onBack: () => void;
}

export function GateDashboard({
  events,
  eventId,
  onEventChange,
  onBack,
}: Props) {
  const [data, setData] = useState<DashboardData | null>(null);
  const [problem, setProblem] = useState("");
  const event = events.find((item) => item.id === eventId);
  const snapshot = data?.eventId === eventId ? data : null;
  useEffect(() => {
    if (!eventId) return;
    let active = true;
    const load = async () => {
      try {
        const next = await fetchGateDashboard(eventId);
        if (active) {
          setData(next);
          setProblem("");
        }
      } catch (cause) {
        if (active) setProblem(getApiErrorMessage(cause));
      }
    };
    void load();
    const interval = window.setInterval(() => void load(), 5000);
    return () => {
      active = false;
      window.clearInterval(interval);
    };
  }, [eventId]);
  const fill = snapshot?.issued
    ? Math.round((snapshot.checkedIn / snapshot.issued) * 100)
    : 0;
  const maxGate = Math.max(
    1,
    ...(snapshot?.gates.map((gate) => gate.granted) || []),
  );
  return (
    <div className="space-y-7">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <button className="back-link" onClick={onBack}>
            <ArrowLeft size={15} /> Danh mục sự kiện
          </button>
          <p className="eyebrow mt-4">
            LIVE GATE MONITOR /{" "}
            {event?.checkInEnabled ? "CỔNG ĐANG MỞ" : "CỔNG ĐANG ĐÓNG"}
          </p>
          <h1 className="page-title">{event?.name || "Gate Dashboard"}</h1>
          <p className="muted mt-2">
            {event?.venueName} · Cập nhật từ BE mỗi 5 giây
          </p>
        </div>
        <div className="flex items-center gap-3">
          <span className="status-pill">
            <span className="live-dot" /> AUTO REFRESH
          </span>
          <select
            className="event-select"
            value={eventId}
            onChange={(e) => onEventChange(e.target.value)}
          >
            {events.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name}
              </option>
            ))}
          </select>
        </div>
      </div>
      {problem && (
        <div className="error-banner">
          {problem}{" "}
          <button onClick={() => onEventChange(eventId)}>
            <RefreshCw size={15} /> Thử lại
          </button>
        </div>
      )}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div>
            <span>TỔNG VÉ PHÁT HÀNH</span>
            <Ticket size={20} />
          </div>
          <strong>{snapshot?.issued.toLocaleString("vi-VN") ?? "—"}</strong>
          <small>Vé có trong DB của sự kiện</small>
        </div>
        <div className="kpi-card">
          <div>
            <span>ĐÃ CHECK-IN</span>
            <Users size={20} />
          </div>
          <strong className="mint">
            {snapshot?.checkedIn.toLocaleString("vi-VN") ?? "—"}
          </strong>
          <div className="kpi-progress">
            <span style={{ width: `${fill}%` }} />
          </div>
          <small>{fill}% trên tổng vé đã phát hành</small>
        </div>
        <div className="kpi-card danger">
          <div>
            <span>CẢNH BÁO REPLAY</span>
            <AlertTriangle size={20} />
          </div>
          <strong>
            {snapshot?.replayAlerts.toLocaleString("vi-VN") ?? "—"}
          </strong>
          <small>Lượt quét bị từ chối do replay</small>
        </div>
      </div>
      <div className="ops-grid">
        <section className="ops-panel">
          <div className="panel-title">
            <h2>
              <DoorOpen size={18} /> Phân bổ theo cổng
            </h2>
            <span>VÉ/PHÚT</span>
          </div>
          {snapshot?.gates.length ? (
            snapshot.gates.map((gate) => (
              <div key={gate.gateId} className="gate-row">
                <div>
                  <strong>{gate.gateId}</strong>
                  <span>{gate.scansPerMinute} vé/phút</span>
                </div>
                <div className="gate-bar">
                  <span
                    style={{
                      width: `${Math.max(3, (gate.granted / maxGate) * 100)}%`,
                    }}
                  />
                </div>
                <small>
                  {gate.granted} đã qua · {gate.denied} từ chối
                </small>
              </div>
            ))
          ) : (
            <div className="empty-state">Chưa có lượt quét tại cổng.</div>
          )}
        </section>
        <section className="ops-panel">
          <div className="panel-title">
            <h2>
              <Activity size={18} /> Live Activity Stream
            </h2>
            <span>{snapshot?.activity.length ?? 0} GẦN NHẤT</span>
          </div>
          <div className="activity-list">
            {snapshot?.activity.length ? (
              snapshot.activity.map((item, index) => (
                <div
                  className="activity-item"
                  key={`${item.ticketId}-${item.scannedAt}-${index}`}
                >
                  <time>
                    {new Date(item.scannedAt).toLocaleTimeString("vi-VN")}
                  </time>
                  <span className="activity-gate">{item.gateId}</span>
                  <span className="activity-ticket">
                    {item.ticketId
                      ? `${item.ticketId.slice(0, 8)}…`
                      : "Không rõ vé"}
                  </span>
                  <span
                    className={`activity-status ${item.status === "GRANTED" ? "granted" : "denied"}`}
                  >
                    {item.status}
                  </span>
                </div>
              ))
            ) : (
              <div className="empty-state">Chưa có hoạt động soát vé.</div>
            )}
          </div>
        </section>
      </div>
      <div className="admin-footer">
        <span>
          Chỉ tính log quét gắn với vé thuộc sự kiện này. Làm mới:{" "}
          {snapshot
            ? new Date(snapshot.refreshedAt).toLocaleTimeString("vi-VN")
            : "—"}
        </span>
        <button className="table-link" onClick={onBack}>
          Về danh mục <ArrowRight size={15} />
        </button>
      </div>
    </div>
  );
}

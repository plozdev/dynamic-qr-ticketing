import { useEffect, useMemo, useState } from "react";
import {
  Activity,
  ArrowRight,
  CalendarDays,
  MapPin,
  Plus,
  RefreshCw,
  Search,
} from "lucide-react";
import type { Event } from "../../types";
import {
  getApiErrorMessage,
  publishEvent,
  setEventCheckInEnabled,
} from "../../services/api";
import { useToast } from "../../context/ToastContext";
import { EventFormModal } from "../events/EventFormModal";

interface Props {
  events: Event[];
  loading: boolean;
  onRefresh: () => void;
  onOpenDashboard: (id: string) => void;
  onOpenMarketplace: () => void;
}
type Filter = "all" | "live" | "draft";

export function AdminCatalog({
  events,
  loading,
  onRefresh,
  onOpenDashboard,
  onOpenMarketplace,
}: Props) {
  const [filter, setFilter] = useState<Filter>("all");
  const [query, setQuery] = useState("");
  const [creating, setCreating] = useState(false);
  const [updating, setUpdating] = useState("");
  const [now, setNow] = useState(() => Date.now());
  const { success, error } = useToast();
  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30000);
    return () => window.clearInterval(timer);
  }, []);
  const isLive = (event: Event) =>
    event.status === "PUBLISHED" &&
    !!event.checkInEnabled &&
    new Date(event.startDateTime).getTime() <= now &&
    new Date(event.endDateTime).getTime() >= now;
  const filtered = useMemo(
    () =>
      events.filter((event) => {
        if (
          filter === "live" &&
          !(
            event.status === "PUBLISHED" &&
            !!event.checkInEnabled &&
            new Date(event.startDateTime).getTime() <= now &&
            new Date(event.endDateTime).getTime() >= now
          )
        )
          return false;
        if (filter === "draft" && event.status !== "DRAFT") return false;
        return `${event.name} ${event.venueName} ${event.id}`
          .toLocaleLowerCase("vi-VN")
          .includes(query.toLocaleLowerCase("vi-VN"));
      }),
    [events, filter, query, now],
  );
  const toggle = async (event: Event) => {
    try {
      setUpdating(event.id);
      await setEventCheckInEnabled(event.id, !event.checkInEnabled);
      success("Đã cập nhật cổng check-in");
      onRefresh();
    } catch (cause) {
      error("Không thể cập nhật", getApiErrorMessage(cause));
    } finally {
      setUpdating("");
    }
  };
  const publish = async (event: Event) => {
    try {
      setUpdating(event.id);
      await publishEvent(event.id);
      success("Đã công bố sự kiện");
      onRefresh();
    } catch (cause) {
      error("Không thể công bố", getApiErrorMessage(cause));
    } finally {
      setUpdating("");
    }
  };
  return (
    <div className="space-y-7">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">COMMAND CENTER / EVENT MANAGEMENT</p>
          <h1 className="page-title">Danh mục sự kiện</h1>
          <p className="muted mt-2">
            Quản lý chương trình và mở bảng điều hành cổng theo sự kiện.
          </p>
        </div>
        <div className="flex gap-2">
          <button className="btn-secondary" onClick={onRefresh}>
            <RefreshCw size={16} /> Làm mới
          </button>
          <button className="btn-primary" onClick={() => setCreating(true)}>
            <Plus size={16} /> Tạo sự kiện
          </button>
        </div>
      </div>
      <div className="admin-toolbar">
        <div className="search-box">
          <Search size={18} />
          <input
            aria-label="Tìm sự kiện"
            placeholder="Tên sự kiện, địa điểm hoặc UUID..."
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
        <div className="filter-tabs">
          {(
            [
              ["all", "Tất cả"],
              ["live", "Đang diễn ra"],
              ["draft", "Bản nháp"],
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
      </div>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>SỰ KIỆN</th>
              <th>NGÀY / ĐỊA ĐIỂM</th>
              <th>VÉ ĐÃ CẤP</th>
              <th>TRẠNG THÁI</th>
              <th>CỔNG CHECK-IN</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((event) => (
              <tr key={event.id}>
                <td>
                  <div className="event-name-cell">
                    <div
                      className="mini-cover"
                      style={
                        event.bannerUrl
                          ? { backgroundImage: `url('${event.bannerUrl}')` }
                          : undefined
                      }
                    />
                    <div>
                      <strong>{event.name}</strong>
                      <small>
                        {event.id.slice(0, 8)} · {event.category || "Sự kiện"}
                      </small>
                    </div>
                  </div>
                </td>
                <td>
                  <span className="table-meta">
                    <CalendarDays size={14} />{" "}
                    {new Date(event.startDateTime).toLocaleDateString("vi-VN")}
                  </span>
                  <span className="table-meta">
                    <MapPin size={14} /> {event.venueName}
                  </span>
                </td>
                <td>
                  <strong>
                    {Math.max(
                      0,
                      (event.totalTickets ?? 0) - (event.availableTickets ?? 0),
                    ).toLocaleString("vi-VN")}
                  </strong>
                </td>
                <td>
                  <span
                    className={`table-badge ${isLive(event) ? "live" : event.status === "DRAFT" ? "draft" : ""}`}
                  >
                    {isLive(event)
                      ? "LIVE NOW"
                      : event.status === "DRAFT"
                        ? "DRAFT"
                        : "PUBLISHED"}
                  </span>
                </td>
                <td>
                  {event.status === "DRAFT" ? (
                    <button
                      className="toggle-button on"
                      disabled={updating === event.id}
                      onClick={() => void publish(event)}
                    >
                      Công bố
                    </button>
                  ) : (
                    <button
                      className={`toggle-button ${event.checkInEnabled ? "on" : ""}`}
                      disabled={updating === event.id}
                      onClick={() => void toggle(event)}
                      aria-label={`Check-in ${event.name}`}
                    >
                      {event.checkInEnabled ? "Đang mở" : "Đang đóng"}
                    </button>
                  )}
                </td>
                <td>
                  <button
                    className="table-link"
                    onClick={() => onOpenDashboard(event.id)}
                  >
                    <Activity size={15} /> Mở Real-time Gate Dashboard{" "}
                    <ArrowRight size={15} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && filtered.length === 0 && (
          <div className="empty-state">Không có sự kiện phù hợp.</div>
        )}
        {loading && <div className="muted p-4">Đang cập nhật...</div>}
      </div>
      <div className="admin-footer">
        <span>{events.length} sự kiện trong hệ thống</span>
        <button className="table-link" onClick={onOpenMarketplace}>
          Xem sàn sự kiện <ArrowRight size={15} />
        </button>
      </div>
      <EventFormModal
        isOpen={creating}
        onClose={() => setCreating(false)}
        onEventCreated={onRefresh}
      />
    </div>
  );
}

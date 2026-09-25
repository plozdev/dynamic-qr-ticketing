import { useEffect, useState } from "react";
import { ArrowRight, RefreshCw, Ticket as TicketIcon } from "lucide-react";
import type { Ticket, UserAccount } from "../../types";
import { fetchUserTickets, getApiErrorMessage } from "../../services/api";

interface Props {
  users: UserAccount[];
  userId: string;
  onUserChange: (id: string) => void;
  onOpenMarketplace: () => void;
}

export function AdminTickets({
  users,
  userId,
  onUserChange,
  onOpenMarketplace,
}: Props) {
  const selectedId = userId || users[0]?.id || "";
  const [result, setResult] = useState<{
    userId: string;
    tickets: Ticket[];
  } | null>(null);
  const [problem, setProblem] = useState("");
  const [revision, setRevision] = useState(0);
  useEffect(() => {
    if (!selectedId) return;
    let active = true;
    void fetchUserTickets(selectedId)
      .then((tickets) => {
        if (active) {
          setResult({ userId: selectedId, tickets });
          setProblem("");
        }
      })
      .catch((cause) => {
        if (active) setProblem(getApiErrorMessage(cause));
      });
    return () => {
      active = false;
    };
  }, [selectedId, revision]);
  const tickets = result?.userId === selectedId ? result.tickets : [];
  return (
    <div className="space-y-7">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">ISSUED TICKETS / ADMIN</p>
          <h1 className="page-title">Vé đã cấp</h1>
          <p className="muted mt-2">
            Tra cứu vé theo tài khoản; mã QR chỉ hiển thị cho chủ vé khi cổng
            mở.
          </p>
        </div>
        <button
          className="btn-secondary"
          onClick={() => setRevision((value) => value + 1)}
        >
          <RefreshCw size={15} /> Làm mới
        </button>
      </div>
      <div className="admin-toolbar">
        <label className="field-label">
          Tài khoản
          <select
            value={selectedId}
            onChange={(event) => onUserChange(event.target.value)}
          >
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.name} (@{user.username})
              </option>
            ))}
          </select>
        </label>
        <button className="btn-primary" onClick={onOpenMarketplace}>
          Cấp thêm vé <ArrowRight size={15} />
        </button>
      </div>
      {problem && <div className="error-banner">{problem}</div>}
      {!selectedId ? (
        <div className="empty-state">Chưa có tài khoản User.</div>
      ) : !result || result.userId !== selectedId ? (
        <div className="empty-state">Đang tải vé...</div>
      ) : tickets.length === 0 ? (
        <div className="empty-state">
          <TicketIcon size={25} /> Tài khoản này chưa có vé.
        </div>
      ) : (
        <div className="user-grid">
          {tickets.map((ticket) => (
            <article className="user-card" key={ticket.id}>
              <div className="user-avatar">
                <TicketIcon size={20} />
              </div>
              <div>
                <strong>{ticket.eventName}</strong>
                <p>
                  {ticket.categoryName} · Ghế {ticket.seatNumber} ·{" "}
                  {ticket.attendeeName}
                </p>
                <p className="mono-small">{ticket.id}</p>
              </div>
              <span
                className={`table-badge ${ticket.status === "CHECKED_IN" ? "" : ticket.status === "READY_TO_CHECK_IN" ? "live" : "draft"}`}
              >
                {ticket.status}
              </span>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}

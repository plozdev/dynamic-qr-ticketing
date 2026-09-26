import { useMemo, useState } from "react";
import {
  CalendarDays,
  MapPin,
  Search,
  Ticket,
  X,
  Minus,
  Plus,
  ArrowRight,
  Sparkles,
} from "lucide-react";
import type { Event, UserAccount } from "../../types";
import {
  bookTickets,
  getApiErrorMessage,
  issueTickets,
} from "../../services/api";
import { useToast } from "../../context/ToastContext";

const tiers = ["Early Bird", "Standard GA", "VIP"] as const;
const money = (value: number) =>
  new Intl.NumberFormat("vi-VN").format(value) + " ₫";
const date = (value: string) =>
  new Date(value).toLocaleString("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  });

interface Props {
  events: Event[];
  users?: UserAccount[];
  isAdmin: boolean;
  loading: boolean;
  onBooked: () => void;
  onOpenWallet: () => void;
  initialUserId?: string;
}

export function Marketplace({
  events,
  users = [],
  isAdmin,
  loading,
  onBooked,
  onOpenWallet,
  initialUserId,
}: Props) {
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState<Event | null>(null);
  const [tier, setTier] = useState<(typeof tiers)[number]>("Standard GA");
  const [quantity, setQuantity] = useState(1);
  const [userId, setUserId] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const { success, error } = useToast();
  const published = useMemo(
    () => events.filter((event) => event.status === "PUBLISHED"),
    [events],
  );
  const visible = useMemo(
    () =>
      published.filter((event) =>
        `${event.name} ${event.venueName}`
          .toLocaleLowerCase("vi-VN")
          .includes(query.toLocaleLowerCase("vi-VN")),
      ),
    [published, query],
  );
  const featured = visible[0];

  const openBooking = (event: Event) => {
    setSelected(event);
    setTier("Standard GA");
    setQuantity(1);
    setUserId(
      initialUserId || users.find((user) => user.role === "USER")?.id || "",
    );
  };
  const submit = async () => {
    if (!selected || submitting) return;
    if ((selected.availableTickets ?? 1) < quantity) {
      error(
        "Không đủ vé còn lại",
        "Hãy tải lại sự kiện và chọn số lượng thấp hơn.",
      );
      return;
    }
    if (isAdmin && !userId) {
      error("Chưa chọn tài khoản nhận vé");
      return;
    }
    try {
      setSubmitting(true);
      const ids = isAdmin
        ? await issueTickets(selected.id, userId, tier, quantity)
        : await bookTickets(selected.id, tier, quantity);
      success(
        `Đã cấp ${ids.length} vé`,
        isAdmin
          ? "Vé đã được gán vào tài khoản đã chọn."
          : "Vé đã xuất hiện trong tủ vé của bạn. Chưa có thanh toán trực tuyến.",
      );
      setSelected(null);
      onBooked();
      if (!isAdmin) onOpenWallet();
    } catch (cause) {
      error("Không thể đặt vé", getApiErrorMessage(cause));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="eyebrow">CYBERPASS / DISCOVERY</p>
          <h1 className="page-title">Khám phá sự kiện</h1>
          <p className="muted mt-2">
            Khám phá đêm diễn tiếp theo và giữ vé trong ví của bạn.
          </p>
        </div>
        <span className="status-pill">
          <Sparkles size={14} /> {published.length} sự kiện đang mở
        </span>
      </div>

      {featured && (
        <div
          className="hero-event"
          style={
            featured.bannerUrl
              ? {
                  backgroundImage: `linear-gradient(90deg, #0a1424 4%, rgba(10,20,36,.9) 38%, rgba(10,20,36,.15)), url('${featured.bannerUrl}')`,
                }
              : undefined
          }
        >
          <div className="hero-copy">
            <span className="status-pill">SỰ KIỆN NỔI BẬT</span>
            <h2>{featured.name}</h2>
            <p>
              {featured.description ||
                "Một trải nghiệm đáng mong đợi đang chờ bạn."}
            </p>
            <p className="hero-meta">
              <CalendarDays size={16} /> {date(featured.startDateTime)}{" "}
              <span>·</span> <MapPin size={16} /> {featured.venueName}
            </p>
            <button
              className="btn-primary"
              disabled={featured.availableTickets === 0}
              onClick={() => openBooking(featured)}
            >
              {featured.availableTickets === 0 ? "Hết vé" : "Mua vé ngay"}{" "}
              <ArrowRight size={16} />
            </button>
          </div>
        </div>
      )}

      <div className="catalog-toolbar">
        <div className="search-box">
          <Search size={18} />
          <input
            aria-label="Tìm sự kiện"
            placeholder="Tìm theo tên sự kiện hoặc địa điểm..."
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>
        <span>{visible.length} kết quả</span>
      </div>
      {loading ? (
        <p className="muted">Đang tải sự kiện...</p>
      ) : visible.length === 0 ? (
        <div className="empty-state">Không tìm thấy sự kiện phù hợp.</div>
      ) : (
        <div className="event-grid">
          {visible.map((event) => (
            <article key={event.id} className="market-card">
              <div
                className="market-cover"
                style={
                  event.bannerUrl
                    ? { backgroundImage: `url('${event.bannerUrl}')` }
                    : undefined
                }
              >
                <span>{event.category || "LIVE EVENT"}</span>
              </div>
              <div className="market-body">
                <p className="card-date">
                  <CalendarDays size={14} /> {date(event.startDateTime)}
                </p>
                <h3>{event.name}</h3>
                <p className="card-venue">
                  <MapPin size={14} /> {event.venueName}
                </p>
                <div className="card-bottom">
                  <div>
                    <small>HẠNG VÉ TỪ</small>
                    <strong>
                      {event.basePrice != null
                        ? money(event.basePrice)
                        : "Liên hệ"}
                    </strong>
                  </div>
                  <button
                    className="btn-primary small"
                    disabled={event.availableTickets === 0}
                    onClick={() => openBooking(event)}
                  >
                    {event.availableTickets === 0 ? "Hết vé" : "Mua vé ngay"}{" "}
                    <ArrowRight size={15} />
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}

      {selected && (
        <div
          className="modal-backdrop"
          onMouseDown={() => !submitting && setSelected(null)}
        >
          <section
            className="booking-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="booking-title"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <button
              className="icon-button modal-close"
              aria-label="Đóng"
              onClick={() => setSelected(null)}
            >
              <X size={20} />
            </button>
            <p className="eyebrow">CHỌN VÉ / {selected.venueName}</p>
            <h2 id="booking-title">{selected.name}</h2>
            <p className="muted">{date(selected.startDateTime)}</p>
            <h3 className="section-label">Hạng vé</h3>
            <div className="tier-list">
              {tiers.map((option) => (
                <button
                  key={option}
                  className={`tier-option ${tier === option ? "selected" : ""}`}
                  onClick={() => setTier(option)}
                >
                  <span>
                    <Ticket size={18} /> {option}
                  </span>
                  <strong>
                    {selected.basePrice != null
                      ? money(selected.basePrice)
                      : "Chưa có giá"}
                  </strong>
                </button>
              ))}
            </div>
            <p className="pricing-note">
              BE hiện chỉ có giá cơ bản theo sự kiện, chưa cấu hình giá riêng
              cho từng hạng. Đây là tổng tạm tính, chưa thu tiền.
            </p>
            {isAdmin && (
              <label className="field-label">
                Tài khoản nhận vé
                <select
                  value={userId}
                  onChange={(event) => setUserId(event.target.value)}
                >
                  <option value="">Chọn người dùng</option>
                  {users.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name} (@{user.username})
                    </option>
                  ))}
                </select>
              </label>
            )}
            <div className="booking-total">
              <div>
                <span>Số lượng</span>
                <div className="stepper">
                  <button
                    aria-label="Giảm số lượng"
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  >
                    <Minus size={15} />
                  </button>
                  <strong>{quantity}</strong>
                  <button
                    aria-label="Tăng số lượng"
                    onClick={() =>
                      setQuantity(
                        Math.min(
                          10,
                          selected.availableTickets ?? 10,
                          quantity + 1,
                        ),
                      )
                    }
                  >
                    <Plus size={15} />
                  </button>
                </div>
              </div>
              <div className="total-value">
                <span>TỔNG TẠM TÍNH</span>
                <strong>
                  {selected.basePrice != null
                    ? money(selected.basePrice * quantity)
                    : "Chưa có giá"}
                </strong>
              </div>
            </div>
            <button
              className="btn-primary full"
              disabled={submitting || (isAdmin && !userId)}
              onClick={submit}
            >
              {submitting
                ? "Đang xử lý..."
                : isAdmin
                  ? `Cấp ${quantity} vé cho tài khoản`
                  : `Đặt ${quantity} vé vào tủ vé`}{" "}
              <ArrowRight size={16} />
            </button>
          </section>
        </div>
      )}
    </div>
  );
}

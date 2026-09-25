import { useCallback, useEffect, useState } from "react";
import {
  Activity,
  CalendarDays,
  Compass,
  LogOut,
  ScanLine,
  ShieldCheck,
  Ticket,
  Users,
} from "lucide-react";
import type {
  AuthSession,
  Event,
  Ticket as TicketData,
  UserAccount,
} from "./types";
import {
  fetchAdminEvents,
  fetchEvents,
  fetchMyTickets,
  fetchUsers,
  getApiErrorMessage,
  loginSession,
  logoutSession,
  restoreSession,
  signupSession,
} from "./services/api";
import { ToastProvider, useToast } from "./context/ToastContext";
import { Marketplace } from "./components/marketplace/Marketplace";
import { Wallet } from "./components/wallet/Wallet";
import { AdminCatalog } from "./components/admin/AdminCatalog";
import { GateDashboard } from "./components/admin/GateDashboard";
import { AdminTickets } from "./components/admin/AdminTickets";
import { GateScannerTester } from "./components/gate/GateScannerTester";

type Page =
  | "marketplace"
  | "wallet"
  | "catalog"
  | "operations"
  | "users"
  | "tickets"
  | "scanner";

function AuthPage({
  onAuthenticated,
}: {
  onAuthenticated: (session: AuthSession) => void;
}) {
  const [signup, setSignup] = useState(false);
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [working, setWorking] = useState(false);
  const [problem, setProblem] = useState("");
  return (
    <main className="auth-page">
      <div className="auth-glow" />
      <section className="auth-card">
        <div className="brand">
          <span className="brand-mark">
            <Ticket size={20} />
          </span>
          <span>
            CYBERPASS<small>LIVE EVENT ACCESS</small>
          </span>
        </div>
        <p className="eyebrow mt-8">
          {signup ? "TẠO TÀI KHOẢN" : "CHÀO MỪNG TRỞ LẠI"}
        </p>
        <h1>{signup ? "Bắt đầu khám phá" : "Đăng nhập Cyberpass"}</h1>
        <p className="muted">
          {signup
            ? "Đăng ký để đặt vé và nhận mã check-in động."
            : "Sự kiện, tủ vé và cổng soát vé trong một nơi."}
        </p>
        <form
          onSubmit={async (event) => {
            event.preventDefault();
            setWorking(true);
            setProblem("");
            try {
              const session = signup
                ? await signupSession({
                    username: username.trim(),
                    email: email.trim(),
                    displayName: displayName.trim(),
                    password,
                  })
                : await loginSession(username.trim(), password);
              onAuthenticated(session);
            } catch (cause) {
              setProblem(getApiErrorMessage(cause));
            } finally {
              setWorking(false);
            }
          }}
        >
          {signup && (
            <label className="field-label">
              Họ và tên
              <input
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
                required
                autoComplete="name"
              />
            </label>
          )}
          <label className="field-label">
            Tên đăng nhập
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
              minLength={3}
              autoComplete="username"
            />
          </label>
          {signup && (
            <label className="field-label">
              Email
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
                autoComplete="email"
              />
            </label>
          )}
          <label className="field-label">
            Mật khẩu
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={8}
              autoComplete={signup ? "new-password" : "current-password"}
            />
          </label>
          {problem && (
            <p className="form-error" role="alert">
              {problem}
            </p>
          )}
          <button className="btn-primary full" disabled={working}>
            {working ? "Đang xử lý..." : signup ? "Tạo tài khoản" : "Đăng nhập"}
          </button>
        </form>
        <button
          className="auth-switch"
          onClick={() => {
            setSignup(!signup);
            setProblem("");
          }}
        >
          {signup
            ? "Đã có tài khoản? Đăng nhập"
            : "Chưa có tài khoản? Đăng ký trên web"}
        </button>
      </section>
    </main>
  );
}

function Portal({
  session,
  onLogout,
}: {
  session: AuthSession;
  onLogout: () => void;
}) {
  const admin = session.roles.includes("ADMIN");
  const [page, setPage] = useState<Page>(admin ? "catalog" : "marketplace");
  const [events, setEvents] = useState<Event[]>([]);
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [tickets, setTickets] = useState<TicketData[]>([]);
  const [loadingEvents, setLoadingEvents] = useState(true);
  const [loadingTickets, setLoadingTickets] = useState(false);
  const [eventId, setEventId] = useState("");
  const [bookingUserId, setBookingUserId] = useState("");
  const { error } = useToast();

  const loadEvents = useCallback(async () => {
    try {
      setLoadingEvents(true);
      const data = admin ? await fetchAdminEvents() : await fetchEvents();
      setEvents(data);
      setEventId((current) => current || data[0]?.id || "");
    } catch (cause) {
      error("Không thể tải sự kiện", getApiErrorMessage(cause));
    } finally {
      setLoadingEvents(false);
    }
  }, [admin, error]);
  const loadTickets = useCallback(
    async (silent = false) => {
      try {
        if (!silent) setLoadingTickets(true);
        setTickets(await fetchMyTickets());
      } catch (cause) {
        if (!silent) error("Không thể tải tủ vé", getApiErrorMessage(cause));
      } finally {
        if (!silent) setLoadingTickets(false);
      }
    },
    [error],
  );
  const loadUsers = useCallback(async () => {
    if (!admin) return;
    try {
      setUsers(await fetchUsers());
    } catch (cause) {
      error("Không thể tải tài khoản", getApiErrorMessage(cause));
    }
  }, [admin, error]);
  useEffect(() => {
    const task = window.setTimeout(() => {
      void loadEvents();
      if (admin) void loadUsers();
      else void loadTickets();
    }, 0);
    return () => window.clearTimeout(task);
  }, [admin, loadEvents, loadUsers, loadTickets]);
  useEffect(() => {
    if (page !== "wallet") return;
    const interval = window.setInterval(() => void loadTickets(true), 5000);
    return () => window.clearInterval(interval);
  }, [page, loadTickets]);

  const nav: { id: Page; label: string; icon: typeof Compass }[] = admin
    ? [
        { id: "catalog", label: "Danh mục sự kiện", icon: CalendarDays },
        { id: "operations", label: "Gate Operations", icon: Activity },
        { id: "marketplace", label: "Sàn sự kiện", icon: Compass },
        { id: "tickets", label: "Vé đã cấp", icon: Ticket },
        { id: "users", label: "Tài khoản", icon: Users },
        { id: "scanner", label: "Giả lập soát vé", icon: ScanLine },
      ]
    : [
        { id: "marketplace", label: "Khám phá", icon: Compass },
        { id: "wallet", label: "Tủ vé của tôi", icon: Ticket },
      ];

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <span className="brand-mark">
            <Ticket size={19} />
          </span>
          <span>
            CYBERPASS
            <small>
              {admin ? "ADMIN COMMAND CENTER" : "LIVE EVENT ACCESS"}
            </small>
          </span>
        </div>
        <div className="header-right">
          <span className="status-pill">
            <ShieldCheck size={14} /> {admin ? "ADMIN" : "MY WALLET"}
          </span>
          <span className="header-user">
            {session.displayName || session.username}
          </span>
          <button
            className="icon-button"
            onClick={onLogout}
            title="Đăng xuất"
            aria-label="Đăng xuất"
          >
            <LogOut size={18} />
          </button>
        </div>
      </header>
      <div className="app-layout">
        <aside className="main-sidebar">
          <p className="eyebrow">{admin ? "COMMAND CENTER" : "KHÁM PHÁ"}</p>
          <nav>
            {nav.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  className={page === item.id ? "active" : ""}
                  onClick={() => setPage(item.id)}
                >
                  <Icon size={18} /> {item.label}
                </button>
              );
            })}
          </nav>
          <div className="sidebar-bottom">
            <ShieldCheck size={16} /> Kết nối máy chủ an toàn
          </div>
        </aside>
        <main className="main-content">
          {page === "marketplace" && (
            <Marketplace
              events={events}
              users={users}
              isAdmin={admin}
              loading={loadingEvents}
              initialUserId={bookingUserId}
              onBooked={() => {
                void loadEvents();
                if (!admin) void loadTickets();
              }}
              onOpenWallet={() => setPage("wallet")}
            />
          )}
          {page === "wallet" && (
            <Wallet
              tickets={tickets}
              events={events}
              loading={loadingTickets}
              onRefresh={() => void loadTickets()}
            />
          )}
          {page === "catalog" && admin && (
            <AdminCatalog
              events={events}
              loading={loadingEvents}
              onRefresh={() => void loadEvents()}
              onOpenDashboard={(id) => {
                setEventId(id);
                setPage("operations");
              }}
              onOpenMarketplace={() => setPage("marketplace")}
            />
          )}
          {page === "operations" && admin && (
            <GateDashboard
              events={events}
              eventId={eventId}
              onEventChange={setEventId}
              onBack={() => setPage("catalog")}
            />
          )}
          {page === "tickets" && admin && (
            <AdminTickets
              users={users}
              userId={bookingUserId}
              onUserChange={setBookingUserId}
              onOpenMarketplace={() => setPage("marketplace")}
            />
          )}
          {page === "users" && admin && (
            <div className="space-y-6">
              <div>
                <p className="eyebrow">ACCOUNT DIRECTORY</p>
                <h1 className="page-title">Tài khoản</h1>
                <p className="muted mt-2">
                  Chọn người nhận vé để cấp vé từ sàn sự kiện.
                </p>
              </div>
              <div className="user-grid">
                {users.map((user) => (
                  <article key={user.id} className="user-card">
                    <div className="user-avatar">
                      {user.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <strong>{user.name}</strong>
                      <p>
                        @{user.username} · {user.email}
                      </p>
                    </div>
                    <span className="table-badge">{user.role}</span>
                    <button
                      className="btn-secondary"
                      onClick={() => {
                        setBookingUserId(user.id);
                        setPage("tickets");
                      }}
                    >
                      Xem vé
                    </button>
                    <button
                      className="btn-secondary"
                      onClick={() => {
                        setBookingUserId(user.id);
                        setPage("marketplace");
                      }}
                    >
                      Cấp vé
                    </button>
                  </article>
                ))}
              </div>
            </div>
          )}
          {page === "scanner" && admin && (
            <div className="space-y-6">
              <div>
                <p className="eyebrow">GATE SIMULATOR</p>
                <h1 className="page-title">Giả lập soát vé</h1>
                <p className="muted mt-2">
                  Kết quả xác thực lấy trực tiếp từ BE.
                </p>
              </div>
              <GateScannerTester
                onTicketStatusChanged={() => void loadTickets()}
              />
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

function AppContent() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [checking, setChecking] = useState(true);
  useEffect(() => {
    let active = true;
    void restoreSession().then((value) => {
      if (active) {
        setSession(value);
        setChecking(false);
      }
    });
    return () => {
      active = false;
    };
  }, []);
  useEffect(() => {
    const expired = () => setSession(null);
    window.addEventListener("securetix-session-expired", expired);
    return () =>
      window.removeEventListener("securetix-session-expired", expired);
  }, []);
  if (checking)
    return <div className="loading-page">Đang kiểm tra phiên đăng nhập...</div>;
  return session ? (
    <Portal
      session={session}
      onLogout={() => {
        setSession(null);
        void logoutSession();
      }}
    />
  ) : (
    <AuthPage onAuthenticated={setSession} />
  );
}

export default function App() {
  return (
    <ToastProvider>
      <AppContent />
    </ToastProvider>
  );
}

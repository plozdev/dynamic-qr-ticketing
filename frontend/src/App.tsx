import { useState, useEffect, useCallback } from 'react';
import type { Event, Ticket, UserAccount } from './types';
import { 
  fetchEvents, 
  fetchUserTickets, 
  fetchUsers,
  saveUser, 
  deleteUser, 
  getApiErrorMessage 
} from './services/api';
import { ToastProvider, useToast } from './context/ToastContext';
import { Header } from './components/common/Header';
import { Sidebar } from './components/common/Sidebar';
import type { TabType } from './components/common/Sidebar';

import { EventsTab } from './components/events/EventsTab';
import { TicketsTab } from './components/tickets/TicketsTab';
import { UsersTab } from './components/users/UsersTab';
import { GateScannerTester } from './components/gate/GateScannerTester';

function MainPortal() {
  const { error: toastError, info } = useToast();

  // Navigation State
  const [activeTab, setActiveTab] = useState<TabType>('events');

  // Events State
  const [events, setEvents] = useState<Event[]>([]);
  const [loadingEvents, setLoadingEvents] = useState<boolean>(true);
  const [eventsError, setEventsError] = useState<string | null>(null);

  // Tickets State
  const [selectedEventId, setSelectedEventId] = useState<string>('');
  const [userId, setUserId] = useState<string>('');
  const [userTickets, setUserTickets] = useState<Ticket[]>([]);
  const [loadingTickets, setLoadingTickets] = useState<boolean>(false);

  // Gate Scanner State
  const [gateTestTicket, setGateTestTicket] = useState<Ticket | null>(null);

  // Users State (Synced from Backend)
  const [users, setUsers] = useState<UserAccount[]>([]);

  const loadUsers = useCallback(async () => {
    try {
      const data = await fetchUsers();
      setUsers(data);
      if (data.length > 0 && !userId) {
        setUserId(data[0].id);
      }
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Không thể tải danh sách người dùng từ backend.');
      toastError('Lỗi tải người dùng', msg);
    }
  }, [userId, toastError]);

  // Initialize Users from Backend
  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  // Fetch Events from Spring Boot Backend
  const loadEvents = useCallback(async () => {
    try {
      setLoadingEvents(true);
      setEventsError(null);
      const data = await fetchEvents();
      setEvents(data);
      if (data.length > 0 && !selectedEventId) {
        setSelectedEventId(data[0].id);
      }
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Không thể tải danh sách sự kiện.');
      setEventsError(msg);
      toastError('Lỗi kết nối Backend', msg);
    } finally {
      setLoadingEvents(false);
    }
  }, [selectedEventId, toastError]);

  // Fetch Tickets for a specific user
  const loadUserTickets = useCallback(
    async (targetUserId?: string) => {
      const idToFetch = targetUserId || userId;
      if (!idToFetch.trim()) return;

      try {
        setLoadingTickets(true);
        const data = await fetchUserTickets(idToFetch.trim());
        setUserTickets(data);
      } catch (err) {
        const msg = getApiErrorMessage(err, 'Không thể tra cứu danh sách vé của người dùng.');
        toastError('Lỗi tải vé người dùng', msg);
      } finally {
        setLoadingTickets(false);
      }
    },
    [userId, toastError]
  );

  // Initial Load
  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  // Load user tickets when userId changes or when entering tickets tab
  useEffect(() => {
    if (activeTab === 'tickets' && userId) {
      loadUserTickets(userId);
    }
  }, [activeTab, userId, loadUserTickets]);

  // Navigation Handlers
  const handleSelectEventForTicket = (eventId: string) => {
    setSelectedEventId(eventId);
    setActiveTab('tickets');
    info('Đã chọn sự kiện', 'Chuyển sang màn hình cấp vé cho sự kiện này.');
  };

  const handleSelectUserForTicket = (targetUserId: string) => {
    setUserId(targetUserId);
    setActiveTab('tickets');
    info('Đã chọn người dùng', `User ID: ${targetUserId.substring(0, 8)}...`);
  };

  const handleViewUserTickets = (targetUserId: string) => {
    setUserId(targetUserId);
    loadUserTickets(targetUserId);
    setActiveTab('tickets');
  };

  const handleTestAtGate = (ticket: Ticket) => {
    setGateTestTicket(ticket);
    setActiveTab('scanner');
    info('Chuyển sang Giả Lập Soát Vé', `Đã chuẩn bị kiểm thử cho vé của ${ticket.attendeeName}`);
  };

  // User Store Handlers
  const handleSaveUser = async (user: UserAccount) => {
    try {
      await saveUser(user);
      await loadUsers();
    } catch (err) {
      toastError('Lỗi lưu người dùng', getApiErrorMessage(err, 'Không thể lưu người dùng lên hệ thống.'));
    }
  };

  const handleDeleteUser = async (deleteId: string) => {
    try {
      await deleteUser(deleteId);
      await loadUsers();
    } catch (err) {
      toastError('Lỗi xóa người dùng', getApiErrorMessage(err, 'Không thể xóa người dùng khỏi hệ thống.'));
    }
  };

  const handleResetUsers = () => {
    loadUsers();
    info('Cập nhật danh sách', 'Đã đồng bộ lại danh sách người dùng từ hệ thống.');
  };

  const handleRefreshAll = () => {
    loadEvents();
    loadUsers();
    if (userId) {
      loadUserTickets(userId);
    }
  };

  return (
    <div className="min-h-screen bg-[#090d16] text-slate-100 flex flex-col selection:bg-[#00e599]/30 selection:text-[#00e599]">
      {/* Sticky Top Header */}
      <Header onRefreshAll={handleRefreshAll} />

      {/* Main Container */}
      <main className="flex-1 mx-auto max-w-7xl w-full px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex flex-col md:flex-row items-start gap-8">
          {/* Left Navigation Sidebar */}
          <Sidebar
            activeTab={activeTab}
            setActiveTab={setActiveTab}
            eventCount={events.length}
          />

          {/* Right Content Area */}
          <section className="flex-1 w-full min-w-0">
            {activeTab === 'events' && (
              <EventsTab
                events={events}
                loading={loadingEvents}
                error={eventsError}
                onRefresh={loadEvents}
                onSelectForTicket={handleSelectEventForTicket}
              />
            )}

            {activeTab === 'tickets' && (
              <TicketsTab
                events={events}
                selectedEventId={selectedEventId}
                setSelectedEventId={setSelectedEventId}
                userId={userId}
                setUserId={setUserId}
                userTickets={userTickets}
                loadingTickets={loadingTickets}
                onRefreshTickets={loadUserTickets}
                availableUsers={users}
                onTestAtGate={handleTestAtGate}
              />
            )}

            {activeTab === 'users' && (
              <UsersTab
                users={users}
                onSaveUser={handleSaveUser}
                onDeleteUser={handleDeleteUser}
                onResetUsers={handleResetUsers}
                onSelectUserForTicket={handleSelectUserForTicket}
                onViewUserTickets={handleViewUserTickets}
              />
            )}

            {activeTab === 'scanner' && (
              <GateScannerTester
                initialTicket={gateTestTicket}
                onTicketStatusChanged={() => {
                  if (userId) loadUserTickets(userId);
                }}
              />
            )}
          </section>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-[#1f293d] bg-[#090d16] py-5 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <div>
            SecureTix Admin Portal • Dynamic QR Ticketing Platform &copy; 2026
          </div>
          <div className="font-mono text-[11px] text-slate-400">
            Connected to Spring Boot API: <span className="text-[#00e599]">http://localhost:8080/api/v1</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default function App() {
  return (
    <ToastProvider>
      <MainPortal />
    </ToastProvider>
  );
}

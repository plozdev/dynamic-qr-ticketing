package com.ticketing.mobile.ticket_display.presentation

import androidx.lifecycle.viewModelScope
import com.ticketing.mobile.core_mvi.BaseViewModel
import com.ticketing.mobile.core_network.sse.ITicketSseClient
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.domain.model.TicketStatus
import com.ticketing.mobile.ticket_display.domain.usecase.ClaimTicketUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GenerateDynamicQrUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetEventsUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetMyTicketsUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetTicketDetailUseCase
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayEffect
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý màn hình Hiển thị vé (TicketDisplayScreen) và Nhận vé theo mô hình MVI.
 * Tích hợp kênh Server-Sent Events (SSE) để tự động cập nhật trạng thái vé theo thời gian thực
 * khi được soát thành công tại cổng.
 */
class TicketDisplayViewModel(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val generateDynamicQrUseCase: GenerateDynamicQrUseCase,
    private val getMyTicketsUseCase: GetMyTicketsUseCase? = null,
    private val claimTicketUseCase: ClaimTicketUseCase? = null,
    private val getEventsUseCase: GetEventsUseCase? = null,
    private val ticketSseClient: ITicketSseClient? = null
) : BaseViewModel<TicketDisplayState, TicketDisplayIntent, TicketDisplayEffect>(
    initialState = TicketDisplayState()
) {

    private var qrObservationJob: Job? = null
    private var ticketSseJob: Job? = null
    private var checkInTicketId: String? = null

    init {
        val authState = com.ticketing.mobile.core_network.auth.AuthManager.instance.authState.value
        if (authState is com.ticketing.mobile.core_network.auth.AuthState.Authenticated) {
            loadMyTickets(authState.userId)
            startObservingTicketUpdates(authState.userId)
        }
        loadEvents()
    }

    override fun handleIntent(intent: TicketDisplayIntent) {
        when (intent) {
            is TicketDisplayIntent.LoadMyTickets -> {
                loadMyTickets()
            }
            is TicketDisplayIntent.LoadTicket -> {
                loadTicket(intent.ticketId, intent.startCheckIn)
            }
            is TicketDisplayIntent.RefreshQrRequested -> {
                currentState.ticket?.id?.let { refreshQr(it) }
            }
            is TicketDisplayIntent.RetryClicked -> {
                currentState.ticket?.id?.let { loadTicket(it) }
            }
            is TicketDisplayIntent.StopQrObservation -> {
                checkInTicketId = null
                qrObservationJob?.cancel()
                qrObservationJob = null
                setState { copy(dynamicQr = null) }
            }
        }
    }

    /**
     * Lắng nghe kênh SSE đẩy trạng thái soát vé từ Backend theo thời gian thực.
     * Khi có lần quét thành công, mobile nhận sự kiện và gọi lại API vé để lấy trạng thái chính thức.
     * Khi mở lại app hoặc kết nối lại, mobile cũng tự động tải lại trạng thái để không bỏ lỡ lần quét nào.
     */
    fun startObservingTicketUpdates(userId: String = com.ticketing.mobile.core_network.auth.AuthManager.instance.getCurrentUserId()) {
        if (ticketSseClient == null || userId.isBlank()) return
        ticketSseJob?.cancel()
        ticketSseJob = viewModelScope.launch {
            ticketSseClient.observeTicketUpdates(
                userId = userId,
                onConnected = {
                    // Khi vừa kết nối hoặc kết nối lại thành công, tải lại trạng thái vé để không bỏ lỡ lần quét nào
                    loadMyTickets(userId)
                    val activeTicketId = currentState.ticket?.id
                    if (activeTicketId != null) {
                        loadTicket(activeTicketId, startCheckIn = checkInTicketId == activeTicketId)
                    }
                }
            ).collect { event ->
                if (event.eventType == "EVENT_CHECK_IN_CHANGED") {
                    val freshTickets = getMyTicketsUseCase?.invoke(userId)?.getOrNull()
                    if (freshTickets != null) {
                        setState { copy(myTickets = freshTickets) }
                        val activeTicketId = currentState.ticket?.id
                        if (activeTicketId != null &&
                            freshTickets.none { it.ticketId == activeTicketId && it.isCheckInOpen }) {
                            qrObservationJob?.cancel()
                            qrObservationJob = null
                            checkInTicketId = null
                            setState { copy(dynamicQr = null, errorMessage = null) }
                        }
                    }
                    return@collect
                }
                // Khi nhận được sự kiện ticket-update (TICKET_CHECKED_IN):
                // 1. Tải lại danh sách vé của người dùng để cập nhật trạng thái mới nhất sang CHECKED_IN
                loadMyTickets(userId)

                // 2. Nếu người dùng đang mở đúng vé này trên màn hình chi tiết / QR, tải lại trạng thái chính thức
                val currentTicketId = currentState.ticket?.id
                if (currentTicketId != null && (currentTicketId == event.ticketId || event.ticketId.isBlank())) {
                    loadTicket(currentTicketId)
                }

                // 3. Thông báo cho người dùng
                val gateMsg = if (event.gateId.isNotBlank()) " tại cổng ${event.gateId}" else ""
                sendEffect(TicketDisplayEffect.ShowToast("Vé của bạn đã được soát thành công$gateMsg!"))
            }
        }
    }

    fun loadMyTickets(userId: String = com.ticketing.mobile.core_network.auth.AuthManager.instance.getCurrentUserId()) {
        if (getMyTicketsUseCase == null) return
        if (ticketSseJob == null || ticketSseJob?.isActive == false) {
            startObservingTicketUpdates(userId)
        }
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            getMyTicketsUseCase(userId)
                .onSuccess { tickets ->
                    setState { copy(myTickets = tickets, isLoading = false) }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, errorMessage = error.message ?: "Failed to load tickets") }
                }
        }
    }

    fun loadEvents() {
        if (getEventsUseCase == null) return
        viewModelScope.launch {
            getEventsUseCase()
                .onSuccess { events ->
                    setState { copy(availableEvents = events) }
                }
        }
    }

    fun claimTicket(eventId: String, categoryName: String? = null, onResult: ((Boolean, String?) -> Unit)? = null) {
        if (claimTicketUseCase == null) return
        viewModelScope.launch {
            setState { copy(isClaiming = true) }
            claimTicketUseCase(eventId, categoryName)
                .onSuccess {
                    setState { copy(isClaiming = false) }
                    loadMyTickets()
                    loadEvents()
                    onResult?.invoke(true, null)
                }
                .onFailure { error ->
                    setState { copy(isClaiming = false) }
                    onResult?.invoke(false, error.message ?: "Không thể nhận vé sự kiện")
                }
        }
    }

    private fun loadTicket(ticketId: String, startCheckIn: Boolean = false) {
        viewModelScope.launch {
            qrObservationJob?.cancel()
            qrObservationJob = null
            checkInTicketId = null
            setState { copy(isLoading = true, errorMessage = null, dynamicQr = null) }
            if (startCheckIn) {
                // Server decides whether the organizer has opened check-in and the time window is valid.
                val permission = OkHttpApiClient().get("/tickets/$ticketId/dynamic-qr") { Unit }
                if (permission !is NetworkResult.Success) {
                    setState { copy(isLoading = false, errorMessage = "Chưa thể bắt đầu check-in. Hãy làm mới danh sách vé.") }
                    loadMyTickets()
                    return@launch
                }
                checkInTicketId = ticketId
            }
            getTicketDetailUseCase(ticketId)
                .onSuccess { ticket ->
                    setState { copy(ticket = ticket, isLoading = false) }
                    if (ticket.status == TicketStatus.CHECKED_IN) {
                        qrObservationJob?.cancel()
                        qrObservationJob = null
                    } else if (startCheckIn) {
                        observeQrStream(ticketId)
                    }
                }
                .onFailure { error ->
                    setState { copy(isLoading = false, errorMessage = error.message ?: "Failed to load ticket") }
                    sendEffect(TicketDisplayEffect.ShowToast(error.message ?: "Error loading ticket"))
                }
        }
    }

    private fun observeQrStream(ticketId: String) {
        qrObservationJob?.cancel()
        qrObservationJob = viewModelScope.launch {
            generateDynamicQrUseCase.observeQrStream(ticketId)
                .catch { error ->
                    setState { copy(dynamicQr = null, errorMessage = error.message ?: "Không tạo được QR từ vé đã đồng bộ") }
                    sendEffect(TicketDisplayEffect.ShowToast(error.message ?: "Error updating QR code"))
                }
                .collect { qrData ->
                    setState { copy(dynamicQr = qrData) }
                }
        }
    }

    private fun refreshQr(ticketId: String) {
        viewModelScope.launch {
            if (checkInTicketId != ticketId ||
                OkHttpApiClient().get("/tickets/$ticketId/dynamic-qr") { Unit } !is NetworkResult.Success) {
                qrObservationJob?.cancel()
                setState { copy(dynamicQr = null, errorMessage = "Check-in chưa được mở cho vé này.") }
                return@launch
            }
            sendEffect(TicketDisplayEffect.TriggerHapticFeedback)
            observeQrStream(ticketId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        qrObservationJob?.cancel()
        ticketSseJob?.cancel()
        ticketSseClient?.disconnect()
    }
}

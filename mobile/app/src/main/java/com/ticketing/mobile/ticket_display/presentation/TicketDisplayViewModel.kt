package com.ticketing.mobile.ticket_display.presentation

import androidx.lifecycle.viewModelScope
import com.ticketing.mobile.core_mvi.BaseViewModel
import com.ticketing.mobile.ticket_display.domain.usecase.GenerateDynamicQrUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetTicketDetailUseCase
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayEffect
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

import com.ticketing.mobile.ticket_display.domain.usecase.ClaimTicketUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetEventsUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetMyTicketsUseCase

/**
 * ViewModel quản lý màn hình Hiển thị vé (TicketDisplayScreen) và Nhận vé theo mô hình MVI.
 */
class TicketDisplayViewModel(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val generateDynamicQrUseCase: GenerateDynamicQrUseCase,
    private val getMyTicketsUseCase: GetMyTicketsUseCase? = null,
    private val claimTicketUseCase: ClaimTicketUseCase? = null,
    private val getEventsUseCase: GetEventsUseCase? = null
) : BaseViewModel<TicketDisplayState, TicketDisplayIntent, TicketDisplayEffect>(
    initialState = TicketDisplayState()
) {

    private var qrObservationJob: Job? = null

    init {
        loadMyTickets()
        loadEvents()
    }

    override fun handleIntent(intent: TicketDisplayIntent) {
        when (intent) {
            is TicketDisplayIntent.LoadMyTickets -> {
                loadMyTickets()
            }
            is TicketDisplayIntent.LoadTicket -> {
                loadTicket(intent.ticketId)
            }
            is TicketDisplayIntent.RefreshQrRequested -> {
                currentState.ticket?.id?.let { refreshQr(it) }
            }
            is TicketDisplayIntent.ToggleAutoBrightness -> {
                val newBrightnessState = intent.enabled
                setState { copy(isAutoBrightnessEnabled = newBrightnessState) }
                sendEffect(TicketDisplayEffect.SetScreenBrightness(newBrightnessState))
            }
            is TicketDisplayIntent.RetryClicked -> {
                currentState.ticket?.id?.let { loadTicket(it) }
            }
            is TicketDisplayIntent.StopQrObservation -> {
                qrObservationJob?.cancel()
                qrObservationJob = null
            }
        }
    }

    fun loadMyTickets(userId: String = com.ticketing.mobile.core_network.auth.AuthManager.instance.getCurrentUserId()) {
        if (getMyTicketsUseCase == null) return
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

    private fun loadTicket(ticketId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            getTicketDetailUseCase(ticketId)
                .onSuccess { ticket ->
                    setState { copy(ticket = ticket, isLoading = false) }
                    observeQrStream(ticketId)
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
                    sendEffect(TicketDisplayEffect.ShowToast(error.message ?: "Error updating QR code"))
                }
                .collect { qrData ->
                    setState { copy(dynamicQr = qrData) }
                }
        }
    }

    private fun refreshQr(ticketId: String) {
        viewModelScope.launch {
            sendEffect(TicketDisplayEffect.TriggerHapticFeedback)
            observeQrStream(ticketId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        qrObservationJob?.cancel()
    }
}

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

/**
 * ViewModel quản lý màn hình Hiển thị vé (TicketDisplayScreen) theo mô hình MVI.
 */
class TicketDisplayViewModel(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val generateDynamicQrUseCase: GenerateDynamicQrUseCase
) : BaseViewModel<TicketDisplayState, TicketDisplayIntent, TicketDisplayEffect>(
    initialState = TicketDisplayState()
) {

    private var qrObservationJob: Job? = null

    override fun handleIntent(intent: TicketDisplayIntent) {
        when (intent) {
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

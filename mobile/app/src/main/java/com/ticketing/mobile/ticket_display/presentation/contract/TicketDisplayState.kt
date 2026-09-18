package com.ticketing.mobile.ticket_display.presentation.contract

import com.ticketing.mobile.core_mvi.UiState
import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.model.Ticket

import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem

/**
 * Immutable UI State for Ticket Display screen.
 */
data class TicketDisplayState(
    val isLoading: Boolean = false,
    val ticket: Ticket? = null,
    val myTickets: List<UserTicketItem> = emptyList(),
    val dynamicQr: DynamicQrData? = null,
    val isAutoBrightnessEnabled: Boolean = true,
    val errorMessage: String? = null
) : UiState

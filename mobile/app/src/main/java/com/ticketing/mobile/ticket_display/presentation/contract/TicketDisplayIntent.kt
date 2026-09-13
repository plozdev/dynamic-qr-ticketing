package com.ticketing.mobile.ticket_display.presentation.contract

import com.ticketing.mobile.core_mvi.UiIntent

/**
 * User actions / intents dispatched from Ticket Display UI.
 */
sealed interface TicketDisplayIntent : UiIntent {
    data class LoadTicket(val ticketId: String) : TicketDisplayIntent
    data object RefreshQrRequested : TicketDisplayIntent
    data class ToggleAutoBrightness(val enabled: Boolean) : TicketDisplayIntent
    data object RetryClicked : TicketDisplayIntent
}

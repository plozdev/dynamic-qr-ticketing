package com.ticketing.mobile.ticket_display.presentation.contract

import com.ticketing.mobile.core_mvi.UiIntent

/**
 * User actions / intents dispatched from Ticket Display UI.
 */
sealed interface TicketDisplayIntent : UiIntent {
    data class LoadTicket(val ticketId: String, val startCheckIn: Boolean = false) : TicketDisplayIntent
    data object LoadMyTickets : TicketDisplayIntent
    data object RefreshQrRequested : TicketDisplayIntent
    data object RetryClicked : TicketDisplayIntent
    data object StopQrObservation : TicketDisplayIntent
}

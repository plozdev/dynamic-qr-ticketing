package com.ticketing.mobile.ticket_display.presentation.contract

import com.ticketing.mobile.core_mvi.UiEffect

/**
 * Single-event side effects for Ticket Display screen.
 */
sealed interface TicketDisplayEffect : UiEffect {
    data class ShowToast(val message: String) : TicketDisplayEffect
    data object TriggerHapticFeedback : TicketDisplayEffect
}

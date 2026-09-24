package com.ticketing.mobile.ticket_display.presentation.ui

import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val legacyDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
private val eventZone = ZoneId.of("Asia/Ho_Chi_Minh")

internal fun filterAndSortTickets(
    tickets: List<UserTicketItem>,
    selectedFilter: Int,
    searchQuery: String,
    nowEpochSeconds: Long
): List<UserTicketItem> {
    val query = searchQuery.trim().lowercase(Locale.ROOT)
    return tickets.asSequence()
        .filter { ticket ->
            when (selectedFilter) {
                1 -> ticket.status == UserTicketCheckInStatus.READY_TO_CHECK_IN ||
                    ticket.status == UserTicketCheckInStatus.NOT_YET_CHECK_IN
                2 -> ticket.status == UserTicketCheckInStatus.CHECKED_IN
                3 -> ticket.status == UserTicketCheckInStatus.REVOKED
                else -> true
            }
        }
        .filter { ticket ->
            query.isEmpty() || listOf(
                ticket.eventName, ticket.venue, ticket.attendeeName, ticket.tierName, ticket.ticketId
            ).any { it.lowercase(Locale.ROOT).contains(query) }
        }
        .sortedWith(compareBy<UserTicketItem> { ticket ->
            val eventTime = ticket.eventTime()
            when {
                eventTime == 0L -> 2
                eventTime >= nowEpochSeconds -> 0
                else -> 1
            }
        }.thenBy { ticket ->
            val eventTime = ticket.eventTime()
            if (eventTime >= nowEpochSeconds) eventTime else -eventTime
        }.thenBy { it.ticketId })
        .toList()
}

private fun UserTicketItem.eventTime(): Long = eventEpochSeconds.takeIf { it > 0L }
    ?: runCatching { LocalDateTime.parse(dateDisplay, legacyDateFormatter).atZone(eventZone).toEpochSecond() }
        .getOrDefault(0L)

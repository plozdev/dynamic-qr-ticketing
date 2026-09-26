package com.ticketing.mobile

import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import com.ticketing.mobile.ticket_display.presentation.ui.filterAndSortTickets
import org.junit.Assert.assertEquals
import org.junit.Test

class TicketListFiltersTest {
    private fun ticket(id: String, status: UserTicketCheckInStatus, eventTime: Long) = UserTicketItem(
        ticketId = id,
        eventName = "Concert $id",
        venue = "Hanoi",
        dateDisplay = "01/10/2026 • 19:30",
        seatNumber = "A1",
        attendeeName = "Test User",
        tierName = "Standard",
        status = status,
        eventEpochSeconds = eventTime
    )

    @Test
    fun upcomingTicketsComeFirstAndPastTicketsFollowNewestFirst() {
        val tickets = listOf(
            ticket("past-old", UserTicketCheckInStatus.CHECKED_IN, 100),
            ticket("future-far", UserTicketCheckInStatus.NOT_YET_CHECK_IN, 400),
            ticket("past-recent", UserTicketCheckInStatus.CHECKED_IN, 190),
            ticket("future-near", UserTicketCheckInStatus.READY_TO_CHECK_IN, 210),
            ticket("revoked", UserTicketCheckInStatus.REVOKED, 500)
        )

        assertEquals(
            listOf("future-near", "future-far", "revoked", "past-recent", "past-old"),
            filterAndSortTickets(tickets, 0, "", 200).map { it.ticketId }
        )
        assertEquals(
            listOf("future-near", "future-far"),
            filterAndSortTickets(tickets, 1, "", 200).map { it.ticketId }
        )
        assertEquals(
            listOf("past-recent", "past-old"),
            filterAndSortTickets(tickets, 2, "", 200).map { it.ticketId }
        )
        assertEquals(listOf("revoked"), filterAndSortTickets(tickets, 3, "", 200).map { it.ticketId })
    }

    @Test
    fun cachedTicketDateCanBeSortedWithoutStoredEpoch() {
        val cached = ticket("cached", UserTicketCheckInStatus.NOT_YET_CHECK_IN, 0)
        val later = ticket("later", UserTicketCheckInStatus.NOT_YET_CHECK_IN, 1_800_000_000)

        assertEquals(
            listOf("cached", "later"),
            filterAndSortTickets(listOf(later, cached), 1, "CONCERT", 1_700_000_000).map { it.ticketId }
        )
    }
}

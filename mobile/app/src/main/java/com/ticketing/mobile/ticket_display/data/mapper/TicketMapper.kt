package com.ticketing.mobile.ticket_display.data.mapper

import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.model.TicketStatus

/**
 * Mapper chuyển đổi qua lại giữa TicketDto (Data Layer) và Ticket (Domain Entity).
 */
object TicketMapper {

    fun toDomain(dto: TicketDto): Ticket {
        val parsedStatus = runCatching {
            TicketStatus.valueOf(dto.statusCode.trim().uppercase())
        }.getOrDefault(TicketStatus.REVOKED)

        return Ticket(
            id = dto.ticketId,
            eventName = dto.eventTitle,
            venue = dto.location,
            eventTimestamp = dto.eventEpochSeconds,
            seatNumber = dto.seatCode,
            ticketHolderName = dto.customerFullName,
            status = parsedStatus
        )
    }

    fun toDto(domain: Ticket, secretKey: String? = null): TicketDto {
        return TicketDto(
            ticketId = domain.id,
            eventTitle = domain.eventName,
            location = domain.venue,
            eventEpochSeconds = domain.eventTimestamp,
            seatCode = domain.seatNumber,
            customerFullName = domain.ticketHolderName,
            statusCode = domain.status.name,
            secretKey = secretKey
        )
    }
}

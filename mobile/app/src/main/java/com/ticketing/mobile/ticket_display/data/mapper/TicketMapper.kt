package com.ticketing.mobile.ticket_display.data.mapper

import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import com.ticketing.mobile.ticket_display.domain.model.Ticket

/**
 * Khung sườn Mapper chuyển đổi qua lại giữa TicketDto và Ticket domain entity.
 */
object TicketMapper {

    fun toDomain(dto: TicketDto): Ticket {
        // TODO: [Giai đoạn 3] Tự viết logic ánh xạ các trường từ DTO sang Domain Model
        TODO("Tự triển khai mapper DTO sang Domain Entity")
    }

    fun toDto(domain: Ticket): TicketDto {
        // TODO: [Giai đoạn 3] Tự viết logic ánh xạ các trường từ Domain Model sang DTO
        TODO("Tự triển khai mapper Domain sang DTO")
    }
}

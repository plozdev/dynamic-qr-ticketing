package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository

/**
 * UseCase: Truy xuất thông tin chi tiết của vé.
 */
class GetTicketDetailUseCase(
    private val repository: ITicketRepository
) {
    suspend operator fun invoke(ticketId: String): Result<Ticket> {
        // TODO: [Giai đoạn 3] Tự viết logic xác thực dữ liệu đầu vào (ticketId không rỗng)
        // và ủy quyền truy xuất dữ liệu cho ITicketRepository.
        TODO("Tự triển khai GetTicketDetailUseCase")
    }
}

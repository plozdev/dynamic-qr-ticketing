package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository

/**
 * UseCase đại diện cho hành động "Lấy thông tin chi tiết của vé" của người dùng.
 * 
 * Lớp này thuộc Domain Layer, tuân thủ nguyên tắc Clean Architecture:
 * - Độc lập hoàn toàn với Android SDK, UI Compose, hay cơ chế lưu trữ.
 * - Đóng gói logic nghiệp vụ (Validation dữ liệu đầu vào).
 * - Ủy quyền truy xuất dữ liệu cho ITicketRepository (DIP - Dependency Inversion).
 */
class GetTicketDetailUseCase(
    private val repository: ITicketRepository
) {
    /**
     * Thực thi UseCase để lấy thông tin vé.
     * 
     * @param ticketId Mã vé cần truy xuất (ví dụ: "TKT-VN-2026-9901").
     * @return Result<Ticket> chứa đối tượng Ticket nếu tìm thấy, hoặc Result.failure nếu có lỗi.
     */
    suspend operator fun invoke(ticketId: String): Result<Ticket> {
        if (ticketId.isBlank()) {
            return Result.failure(IllegalArgumentException("Ticket ID must not be blank"))
        }
        return repository.getTicket(ticketId.trim())
    }
}

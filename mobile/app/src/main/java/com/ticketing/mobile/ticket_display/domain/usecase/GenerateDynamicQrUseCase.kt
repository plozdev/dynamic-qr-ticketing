package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase quản lý nghiệp vụ sinh mã Dynamic QR và luồng đếm ngược thời gian thực.
 * 
 * Vai trò:
 * - Cung cấp hàm lấy nhanh 1 mã QR tại thời điểm hiện tại (`fetchCurrentQr`).
 * - Cung cấp luồng Flow liên tục phát dữ liệu QR mới và số giây đếm ngược mỗi giây (`observeQrStream`).
 */
class GenerateDynamicQrUseCase(
    private val repository: ITicketRepository
) {
    /**
     * Lấy ngay mã QR động hiện thời (Single Shot).
     * 
     * @param ticketId Mã vé cần tạo QR.
     * @return Result<DynamicQrData> chứa payload QR và thời gian hiệu lực.
     */
    suspend fun fetchCurrentQr(ticketId: String): Result<DynamicQrData> {
        if (ticketId.isBlank()) {
            return Result.failure(IllegalArgumentException("Ticket ID must not be blank"))
        }
        return repository.getDynamicQr(ticketId.trim())
    }

    /**
     * Lắng nghe luồng dữ liệu Dynamic QR đếm ngược thời gian thực (Reactive Stream).
     * 
     * @param ticketId Mã vé cần theo dõi.
     * @return Flow<DynamicQrData> phát ra mỗi giây (cập nhật remainingSeconds và đổi QR khi hết chu kỳ 30s).
     */
    fun observeQrStream(ticketId: String): Flow<DynamicQrData> {
        require(ticketId.isNotBlank()) { "Ticket ID must not be blank" }
        return repository.observeDynamicQr(ticketId.trim())
    }
}

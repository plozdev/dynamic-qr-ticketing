package com.ticketing.mobile.ticket_display.domain.usecase

import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase: Tạo và quản lý luồng dữ liệu Dynamic QR code xoay vòng.
 */
class GenerateDynamicQrUseCase(
    private val repository: ITicketRepository
) {
    suspend fun fetchCurrentQr(ticketId: String): Result<DynamicQrData> {
        // TODO: [Giai đoạn 3] Tự viết logic lấy QR code hiện tại
        TODO("Tự triển khai lấy QR code hiện thời")
    }

    fun observeQrStream(ticketId: String): Flow<DynamicQrData> {
        // TODO: [Giai đoạn 3] Tự viết logic lắng nghe luồng countdown & rotation QR từ repository
        TODO("Tự triển khai lắng nghe luồng QR stream")
    }
}

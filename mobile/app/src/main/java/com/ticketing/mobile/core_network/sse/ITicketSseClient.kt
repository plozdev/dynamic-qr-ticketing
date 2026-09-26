package com.ticketing.mobile.core_network.sse

import kotlinx.coroutines.flow.Flow

/**
 * Giao diện quản lý kết nối Server-Sent Events (SSE) để nhận sự kiện soát vé thời gian thực.
 */
interface ITicketSseClient {

    /**
     * Lắng nghe luồng sự kiện cập nhật trạng thái vé từ Backend đẩy về cho user.
     * Tự động kết nối lại khi rớt mạng hoặc chuyển kết nối.
     * Khi kết nối thành công hoặc kết nối lại, gọi onConnected callback để mobile tải lại trạng thái chính thức.
     */
    fun observeTicketUpdates(
        userId: String,
        ticketId: String? = null,
        onConnected: (() -> Unit)? = null
    ): Flow<TicketUpdateSseEvent>

    /**
     * Ngắt kết nối SSE stream hiện tại nếu có.
     */
    fun disconnect()
}

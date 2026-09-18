package com.ticketing.mobile.ticket_display.data.repository

import com.ticketing.mobile.core_crypto.domain.repository.ICryptoEngine
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ticket_display.data.datasource.ITicketLocalDataSource
import com.ticketing.mobile.ticket_display.data.datasource.ITicketRemoteDataSource
import com.ticketing.mobile.ticket_display.data.mapper.TicketMapper
import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.repository.ITicketRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * Triển khai ITicketRepository kết hợp Local Cache (Offline-ready), Remote API và C++ Crypto Engine.
 * 
 * Lớp này chịu trách nhiệm:
 * 1. Điều phối chiến lược Cache-First: Ưu tiên nạp dữ liệu từ local để người dùng mở vé ngay lập tức khi không có mạng.
 * 2. Bảo mật Secret Key: Đọc secretKey từ vùng nhớ an toàn (Encrypted Storage) và chuyển xuống C++ NDK.
 * 3. Tạo luồng Reactive Flow sinh Dynamic QR mỗi giây hoàn toàn Offline.
 */
class TicketRepositoryImpl(
    private val remoteDataSource: ITicketRemoteDataSource,
    private val localDataSource: ITicketLocalDataSource,
    private val cryptoEngine: ICryptoEngine
) : ITicketRepository {

    /**
     * Lấy thông tin vé với chiến lược Offline-First Cache.
     * 
     * Quy trình:
     * - Bước 1: Kiểm tra trong bộ nhớ đệm cục bộ qua localDataSource.getCachedTicket(ticketId).
     * - Bước 2: Nếu đã có dữ liệu cache -> Dùng TicketMapper.toDomain(cached) và trả về Result.success ngay (Zero Network Latency).
     * - Bước 3: Nếu chưa có cache -> Gọi remoteDataSource.fetchTicketById(ticketId) lên Spring Boot.
     * - Bước 4: Khi remote trả về TicketDto thành công (kèm secretKey):
     *   + Lưu vào localDataSource.saveTicket(dto) để sử dụng offline lần sau.
     *   + Dùng TicketMapper.toDomain(dto) chuyển sang entity và trả về Result.success.
     * - Bước 5: Nếu remote thất bại và không có cache -> Trả về Result.failure(exception).
     */
    override suspend fun getTicket(ticketId: String): Result<Ticket> {
        val cached = localDataSource.getCachedTicket(ticketId)
        if (cached != null) {
            return Result.success(TicketMapper.toDomain(cached))
        }

        return when (val networkResult = remoteDataSource.fetchTicketById(ticketId)) {
            is NetworkResult.Success -> {
                localDataSource.saveTicket(networkResult.data)
                Result.success(TicketMapper.toDomain(networkResult.data))
            }
            is NetworkResult.Error -> {
                Result.failure(Exception(networkResult.error.messageText, networkResult.error.causeThrowable))
            }
            is NetworkResult.Loading -> {
                Result.failure(IllegalStateException("Network request still loading"))
            }
        }
    }

    /**
     * Sinh một mã Dynamic QR đơn lẻ tại thời điểm hiện tại.
     * 
     * Quy trình:
     * - Bước 1: Lấy secretKey của vé từ localDataSource.getSecretKey(ticketId). Nếu null -> Trả về Result.failure.
     * - Bước 2: Lấy mốc thời gian hiện tại: val currentSec = System.currentTimeMillis() / 1000.
     * - Bước 3: Tính toán chu kỳ thời gian (timeWindow = currentSec / 30) và thời điểm hết hạn (expiresAt = (timeWindow + 1) * 30).
     * - Bước 4: Gọi cryptoEngine.generateToken(ticketId, secretKey, currentSec, 30).
     * - Bước 5: Tạo payload chuỗi theo định dạng chuẩn: "TICKETING:$ticketId:$expiresAt:${cryptoToken.tokenValue}".
     * - Bước 6: Trả về DynamicQrData với payload, expiresAt và remainingSeconds.
     */
    override suspend fun getDynamicQr(ticketId: String): Result<DynamicQrData> {
        val secretKey = localDataSource.getSecretKey(ticketId)
            ?: return Result.failure(IllegalStateException("Secret key not provisioned for ticket: $ticketId"))

        val intervalSec = 30
        val currentSec = System.currentTimeMillis() / 1000
        val timeWindow = currentSec / intervalSec
        val expiresAt = (timeWindow + 1) * intervalSec
        val remaining = (expiresAt - currentSec).toInt()

        return cryptoEngine.generateToken(ticketId, secretKey, currentSec, intervalSec).map { token ->
            val payload = "TICKETING:$ticketId:$expiresAt:${token.tokenValue}"
            DynamicQrData(
                ticketId = ticketId,
                qrPayload = payload,
                validUntilEpochSeconds = expiresAt,
                totalIntervalSeconds = intervalSec,
                remainingSeconds = remaining
            )
        }
    }

    /**
     * Tạo luồng Flow đếm ngược thời gian thực và tự động tạo mã QR mới sau mỗi 30 giây.
     * 
     * Quy trình:
     * - Bước 1: Lấy secretKey từ localDataSource.
     * - Bước 2: Chạy vòng lặp while (currentCoroutineContext().isActive).
     * - Bước 3: Trong mỗi vòng lặp:
     *   + Tính currentEpochSeconds, timeWindow, expiresAt, remainingSeconds.
     *   + Gọi cryptoEngine.generateToken() để lấy dynamic token mới nhất.
     *   + Đóng gói chuỗi "TICKETING:$ticketId:$expiresAt:${token.tokenValue}".
     *   + emit(DynamicQrData(...)) ra cho UI.
     *   + delay(1000L) (đợi 1 giây để đếm ngược nhịp nhàng).
     */
    override fun observeDynamicQr(ticketId: String): Flow<DynamicQrData> = flow {
        val secretKey = localDataSource.getSecretKey(ticketId)
            ?: throw IllegalStateException("Secret key not provisioned for ticket: $ticketId")

        val intervalSec = 30
        while (currentCoroutineContext().isActive) {
            val currentSec = System.currentTimeMillis() / 1000
            val timeWindow = currentSec / intervalSec
            val expiresAt = (timeWindow + 1) * intervalSec
            val remaining = (expiresAt - currentSec).toInt()

            cryptoEngine.generateToken(ticketId, secretKey, currentSec, intervalSec).onSuccess { token ->
                val payload = "TICKETING:$ticketId:$expiresAt:${token.tokenValue}"
                emit(
                    DynamicQrData(
                        ticketId = ticketId,
                        qrPayload = payload,
                        validUntilEpochSeconds = expiresAt,
                        totalIntervalSeconds = intervalSec,
                        remainingSeconds = remaining
                    )
                )
            }

            delay(1000L)
        }
    }
}

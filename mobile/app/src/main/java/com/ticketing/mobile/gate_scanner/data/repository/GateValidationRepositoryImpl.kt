package com.ticketing.mobile.gate_scanner.data.repository

import com.ticketing.mobile.core_crypto.domain.model.VerificationResult
import com.ticketing.mobile.core_crypto.domain.repository.ICryptoEngine
import com.ticketing.mobile.core_network.model.ApiError
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.gate_scanner.data.datasource.IGateRemoteDataSource
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationRequestDto
import com.ticketing.mobile.gate_scanner.data.mapper.GateValidationMapper
import com.ticketing.mobile.gate_scanner.domain.model.DenyReason
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult
import com.ticketing.mobile.gate_scanner.domain.repository.IGateValidationRepository

/**
 * Triển khai IGateValidationRepository hỗ trợ cơ chế xác thực Hybrid (Online + Offline Fallback):
 * 
 * Mục đích:
 * 1. Chế độ Online: Gửi dữ liệu mã QR về Backend để kiểm tra tính hợp lệ và ghi nhận check-in tập trung.
 * 2. Chế độ Offline (Sân vận động, sự kiện đông người mất sóng):
 *    - Tự bóc tách chuỗi QR "TICKETING:<ticketId>:<expiresAt>:<dynamicToken>".
 *    - Xác thực chữ ký HMAC-SHA256 ngay trên thiết bị bằng C++ NDK CryptoEngine (độ trễ < 5ms).
 *    - Tự động fallback sang Offline nếu cuộc gọi mạng gặp sự cố (Network Failure).
 */
class GateValidationRepositoryImpl(
    private val remoteDataSource: IGateRemoteDataSource,
    private val cryptoEngine: ICryptoEngine,
    private val isOfflineEnabled: Boolean = false
) : IGateValidationRepository {

    /**
     * Xác thực vé quét từ camera.
     * 
     * @param scanResult Dữ liệu quét thô từ camera (gồm rawQrPayload, scannedTimestamp, gateId).
     * @return Result<GateAccessStatus> (Granted hoặc Denied).
     */
    override suspend fun validateTicket(scanResult: ScanResult): Result<GateAccessStatus> {
        if (isOfflineEnabled) {
            return Result.success(validateLocallyOffline(scanResult))
        }

        val request = GateValidationRequestDto(
            rawPayload = scanResult.rawQrPayload,
            gateId = scanResult.gateId,
            scanTimestamp = scanResult.scannedTimestamp
        )

        return when (val networkResult = remoteDataSource.verifyAndCheckIn(request)) {
            is NetworkResult.Success -> {
                Result.success(GateValidationMapper.toDomain(networkResult.data))
            }
            is NetworkResult.Error -> {
                when (val err = networkResult.error) {
                    is ApiError.NetworkConnection -> {
                        // Tự động fallback sang Offline Mode khi mất kết nối
                        Result.success(validateLocallyOffline(scanResult))
                    }
                    else -> {
                        Result.success(
                            GateAccessStatus.Denied(
                                reason = DenyReason.SERVER_REJECTED,
                                message = err.messageText
                            )
                        )
                    }
                }
            }
            is NetworkResult.Loading -> {
                Result.success(
                    GateAccessStatus.Denied(
                        reason = DenyReason.SERVER_REJECTED,
                        message = "Đang xử lý yêu cầu..."
                    )
                )
            }
        }
    }

    /**
     * Xác thực Offline cục bộ sử dụng C++ NDK.
     */
    private suspend fun validateLocallyOffline(scanResult: ScanResult): GateAccessStatus {
        val parts = scanResult.rawQrPayload.split(":")
        if (parts.size < 4 || parts[0] != "TICKETING") {
            return GateAccessStatus.Denied(
                reason = DenyReason.INVALID_SIGNATURE,
                message = "Mã QR không đúng định dạng hệ thống vé"
            )
        }

        val ticketId = parts[1]
        val expiresAtEpochSeconds = parts[2].toLongOrNull() ?: return GateAccessStatus.Denied(
            reason = DenyReason.EXPIRED_TIMESTAMP,
            message = "Dấu thời gian không hợp lệ"
        )
        val token = parts[3]

        if (scanResult.scannedTimestamp > expiresAtEpochSeconds + 30) {
            return GateAccessStatus.Denied(
                reason = DenyReason.EXPIRED_TIMESTAMP,
                message = "Mã QR đã hết hạn, vui lòng làm mới mã trên điện thoại"
            )
        }

        val mockSecretKey = "sample-secret-key-$ticketId"
        val verifyResult = cryptoEngine.verifyToken(
            ticketId = ticketId,
            secretKey = mockSecretKey,
            token = token,
            epochSeconds = scanResult.scannedTimestamp,
            intervalSeconds = 30,
            allowedDriftSteps = 1
        )

        return when (verifyResult) {
            is VerificationResult.Valid -> {
                GateAccessStatus.Granted(
                    ticketId = ticketId,
                    attendeeName = "Khách hàng Offline",
                    seatNumber = "Khán đài A",
                    checkInTimestamp = scanResult.scannedTimestamp
                )
            }
            is VerificationResult.ExpiredTimeWindow -> {
                GateAccessStatus.Denied(
                    reason = DenyReason.EXPIRED_TIMESTAMP,
                    message = "Mã QR đã hết hạn thời gian cho phép"
                )
            }
            else -> {
                GateAccessStatus.Denied(
                    reason = DenyReason.INVALID_SIGNATURE,
                    message = "Mã xác thực không hợp lệ hoặc vé bị làm giả"
                )
            }
        }
    }

    override fun isOfflineMode(): Boolean = isOfflineEnabled
}

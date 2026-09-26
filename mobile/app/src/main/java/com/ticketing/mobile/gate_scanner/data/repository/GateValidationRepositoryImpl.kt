package com.ticketing.mobile.gate_scanner.data.repository

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
 * Gate validation requires the backend to authorize and persist a check-in.
 * A network failure must never produce a successful local check-in.
 */
class GateValidationRepositoryImpl(
    private val remoteDataSource: IGateRemoteDataSource
) : IGateValidationRepository {

    /**
     * Xác thực vé quét từ camera.
     * 
     * @param scanResult Dữ liệu quét thô từ camera (gồm rawQrPayload, scannedTimestamp, gateId).
     * @return Result<GateAccessStatus> (Granted hoặc Denied).
     */
    override suspend fun validateTicket(scanResult: ScanResult): Result<GateAccessStatus> {
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
                        Result.success(GateAccessStatus.Denied(
                            reason = DenyReason.SERVER_REJECTED,
                            message = "Không có mạng: chưa thể xác thực và ghi nhận check-in trên máy chủ"
                        ))
                    }
                    else -> {
                        var denyMsg = err.messageText
                        var denyReason = DenyReason.SERVER_REJECTED
                        if (err is ApiError.HttpError && !err.rawBody.isNullOrBlank()) {
                            try {
                                val json = org.json.JSONObject(err.rawBody)
                                val msg = json.optString("message", "")
                                if (msg.isNotBlank()) denyMsg = msg
                                val status = json.optString("status", "")
                                denyReason = runCatching {
                                    DenyReason.valueOf(status.trim().uppercase())
                                }.getOrNull() ?: DenyReason.SERVER_REJECTED
                            } catch (_: Exception) {}
                        }
                        Result.success(
                            GateAccessStatus.Denied(
                                reason = denyReason,
                                message = denyMsg
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

    override fun isOfflineMode(): Boolean = false
}

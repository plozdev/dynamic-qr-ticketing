package com.ticketing.mobile.gate_scanner.data.datasource

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationRequestDto
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationResponseDto

/**
 * Triển khai IGateRemoteDataSource gửi yêu cầu soát vé lên Server (có phản hồi mô phỏng).
 */
class DefaultGateRemoteDataSource : IGateRemoteDataSource {

    override suspend fun verifyAndCheckIn(request: GateValidationRequestDto): NetworkResult<GateValidationResponseDto> {
        val parts = request.rawPayload.split(":")
        val isFormatValid = parts.size >= 4 && parts[0] == "TICKETING"

        return if (isFormatValid) {
            NetworkResult.Success(
                GateValidationResponseDto(
                    isAllowed = true,
                    ticketId = parts[1],
                    attendeeName = "Nguyễn Hoàng Long",
                    seatNumber = "VIP-A12",
                    checkInTimestamp = System.currentTimeMillis() / 1000,
                    rejectionReasonCode = null,
                    message = "Xác thực thành công tại cổng ${request.gateId}"
                )
            )
        } else {
            NetworkResult.Success(
                GateValidationResponseDto(
                    isAllowed = false,
                    ticketId = null,
                    attendeeName = null,
                    seatNumber = null,
                    checkInTimestamp = null,
                    rejectionReasonCode = "INVALID_SIGNATURE",
                    message = "Mã QR không đúng định dạng hệ thống"
                )
            )
        }
    }
}

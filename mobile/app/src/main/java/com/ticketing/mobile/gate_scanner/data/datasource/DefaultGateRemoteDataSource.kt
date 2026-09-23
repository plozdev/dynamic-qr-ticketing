package com.ticketing.mobile.gate_scanner.data.datasource

import com.ticketing.mobile.core_network.client.IApiClient
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationRequestDto
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationResponseDto
import org.json.JSONObject

/**
 * Triển khai IGateRemoteDataSource gửi yêu cầu soát vé trực tiếp lên Backend REST API.
 */
class DefaultGateRemoteDataSource(
    private val apiClient: IApiClient = OkHttpApiClient()
) : IGateRemoteDataSource {

    override suspend fun verifyAndCheckIn(request: GateValidationRequestDto): NetworkResult<GateValidationResponseDto> {
        val payload = JSONObject().apply {
            put("rawQrPayload", request.rawPayload)
        }

        return apiClient.post(
            endpoint = "/gates/${request.gateId}/validate",
            bodyJson = payload.toString()
        ) { jsonStr ->
            val obj = JSONObject(jsonStr)
            val isSuccess = obj.optBoolean("success", false)
            val status = obj.optString("status", if (isSuccess) "GRANTED" else "DENIED")
            val message = obj.optString("message", "")
            val ticketId = if (obj.has("ticketId") && !obj.isNull("ticketId")) obj.getString("ticketId") else null

            GateValidationResponseDto(
                isAllowed = isSuccess,
                ticketId = ticketId,
                attendeeName = null,
                seatNumber = null,
                checkInTimestamp = System.currentTimeMillis() / 1000,
                rejectionReasonCode = if (isSuccess) null else status,
                message = message
            )
        }
    }
}

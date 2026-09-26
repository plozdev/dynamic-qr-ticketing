package com.ticketing.mobile.gate_scanner.data.datasource

import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationRequestDto
import com.ticketing.mobile.gate_scanner.data.dto.GateValidationResponseDto

/**
 * Remote Data Source interface for gate check-in backend API.
 */
interface IGateRemoteDataSource {
    suspend fun verifyAndCheckIn(request: GateValidationRequestDto): NetworkResult<GateValidationResponseDto>
}

package com.ticketing.mobile.gate_scanner.domain.repository

import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult

/**
 * Boundary repository interface for gate validation.
 */
interface IGateValidationRepository {

    /**
     * Validate scanned QR code and persist check-in through the backend.
     */
    suspend fun validateTicket(scanResult: ScanResult): Result<GateAccessStatus>

    /**
     * Offline gate validation is unavailable until trusted rosters and sync exist.
     */
    fun isOfflineMode(): Boolean
}

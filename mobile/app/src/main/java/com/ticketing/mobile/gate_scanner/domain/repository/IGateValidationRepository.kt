package com.ticketing.mobile.gate_scanner.domain.repository

import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult

/**
 * Boundary repository interface for gate validation.
 */
interface IGateValidationRepository {

    /**
     * Validate scanned QR code through local crypto validation & server check-in.
     */
    suspend fun validateTicket(scanResult: ScanResult): Result<GateAccessStatus>

    /**
     * Check if gate terminal operates in offline-first mode.
     */
    fun isOfflineMode(): Boolean
}

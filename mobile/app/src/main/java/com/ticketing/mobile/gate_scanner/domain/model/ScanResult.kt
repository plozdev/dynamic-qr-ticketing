package com.ticketing.mobile.gate_scanner.domain.model

/**
 * Domain entity representing raw scan output from camera.
 */
data class ScanResult(
    val rawQrPayload: String,
    val scannedTimestamp: Long,
    val gateId: String
)

package com.ticketing.mobile.gate_scanner.presentation.contract

import com.ticketing.mobile.core_mvi.UiState
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus

/**
 * Immutable UI state for Gate Scanner screen.
 */
data class GateScannerState(
    val isScanning: Boolean = true,
    val isValidating: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val isOfflineMode: Boolean = false,
    val lastAccessStatus: GateAccessStatus? = null,
    val errorMessage: String? = null
) : UiState

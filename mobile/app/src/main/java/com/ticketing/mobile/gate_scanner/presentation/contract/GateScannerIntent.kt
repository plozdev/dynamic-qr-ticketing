package com.ticketing.mobile.gate_scanner.presentation.contract

import com.ticketing.mobile.core_mvi.UiIntent

/**
 * User actions / intents dispatched from Gate Scanner UI.
 */
sealed interface GateScannerIntent : UiIntent {
    data class QrCodeScanned(val rawPayload: String, val gateId: String) : GateScannerIntent
    data object ToggleTorch : GateScannerIntent
    data object ResetScanner : GateScannerIntent
    data class SetOfflineMode(val enabled: Boolean) : GateScannerIntent
}

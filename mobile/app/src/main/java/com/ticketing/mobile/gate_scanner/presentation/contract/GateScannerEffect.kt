package com.ticketing.mobile.gate_scanner.presentation.contract

import com.ticketing.mobile.core_mvi.UiEffect

/**
 * Single-event side effects for Gate Scanner screen.
 */
sealed interface GateScannerEffect : UiEffect {
    data class PlaySound(val isSuccess: Boolean) : GateScannerEffect
    data class TriggerHapticFeedback(val isSuccess: Boolean) : GateScannerEffect
    data class ShowMessage(val message: String) : GateScannerEffect
}

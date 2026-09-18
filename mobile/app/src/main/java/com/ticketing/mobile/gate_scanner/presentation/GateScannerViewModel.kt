package com.ticketing.mobile.gate_scanner.presentation

import androidx.lifecycle.viewModelScope
import com.ticketing.mobile.core_mvi.BaseViewModel
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult
import com.ticketing.mobile.gate_scanner.domain.usecase.ValidateScannedTicketUseCase
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerEffect
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerIntent
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý màn hình máy quét vé tại cổng (GateScannerScreen) theo kiến trúc MVI.
 */
class GateScannerViewModel(
    private val validateScannedTicketUseCase: ValidateScannedTicketUseCase
) : BaseViewModel<GateScannerState, GateScannerIntent, GateScannerEffect>(
    initialState = GateScannerState()
) {

    override fun handleIntent(intent: GateScannerIntent) {
        when (intent) {
            is GateScannerIntent.QrCodeScanned -> {
                processScannedQr(intent.rawPayload, intent.gateId)
            }
            is GateScannerIntent.ToggleTorch -> {
                val newTorchState = !currentState.isTorchEnabled
                setState { copy(isTorchEnabled = newTorchState) }
            }
            is GateScannerIntent.ResetScanner -> {
                setState { copy(lastAccessStatus = null, isValidating = false) }
            }
            is GateScannerIntent.SetOfflineMode -> {
                setState { copy(isOfflineMode = intent.enabled) }
            }
        }
    }

    private fun processScannedQr(rawPayload: String, gateId: String) {
        if (currentState.isValidating) return

        viewModelScope.launch {
            setState { copy(isValidating = true) }

            val scanResult = ScanResult(
                rawQrPayload = rawPayload,
                scannedTimestamp = System.currentTimeMillis() / 1000,
                gateId = gateId
            )

            val result = validateScannedTicketUseCase(scanResult)
            result.onSuccess { status ->
                setState { copy(lastAccessStatus = status) }
                when (status) {
                    is GateAccessStatus.Granted -> {
                        sendEffect(GateScannerEffect.PlaySound(isSuccess = true))
                        sendEffect(GateScannerEffect.TriggerHapticFeedback(isSuccess = true))
                    }
                    is GateAccessStatus.Denied -> {
                        sendEffect(GateScannerEffect.PlaySound(isSuccess = false))
                        sendEffect(GateScannerEffect.TriggerHapticFeedback(isSuccess = false))
                    }
                }
            }.onFailure { error ->
                sendEffect(GateScannerEffect.ShowMessage(error.message ?: "Lỗi hệ thống soát vé"))
            }

            // Tự động mở lại camera quét sau 2.5 giây cooldown
            delay(2500L)
            setState { copy(isValidating = false, lastAccessStatus = null) }
        }
    }
}

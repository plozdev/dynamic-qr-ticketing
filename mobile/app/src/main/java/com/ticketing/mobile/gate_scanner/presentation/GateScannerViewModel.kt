package com.ticketing.mobile.gate_scanner.presentation

import com.ticketing.mobile.core_mvi.BaseViewModel
import com.ticketing.mobile.gate_scanner.domain.usecase.ValidateScannedTicketUseCase
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerEffect
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerIntent
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerState

/**
 * Khung sườn ViewModel cho màn hình quét vé cổng soát theo mô hình MVI.
 */
class GateScannerViewModel(
    private val validateScannedTicketUseCase: ValidateScannedTicketUseCase
) : BaseViewModel<GateScannerState, GateScannerIntent, GateScannerEffect>(
    initialState = GateScannerState()
) {

    override fun handleIntent(intent: GateScannerIntent) {
        when (intent) {
            is GateScannerIntent.QrCodeScanned -> {
                // TODO: [Giai đoạn 4] Tự viết logic:
                // 1. Debounce quét để tránh bắn sự kiện trùng liên tục
                // 2. Chuyển state sang isValidating = true
                // 3. Gọi validateScannedTicketUseCase(scanResult)
                // 4. Bắn side effect PlaySound(true/false) và TriggerHapticFeedback
            }
            is GateScannerIntent.ToggleTorch -> {
                // TODO: [Giai đoạn 4] Bật/tắt đèn flash hỗ trợ quét ban đêm
            }
            is GateScannerIntent.ResetScanner -> {
                // TODO: [Giai đoạn 4] Reset trạng thái để sẵn sàng quét người tiếp theo
            }
            is GateScannerIntent.SetOfflineMode -> {
                // TODO: [Giai đoạn 4] Chuyển đổi cờ quét Offline/Online
            }
        }
    }
}

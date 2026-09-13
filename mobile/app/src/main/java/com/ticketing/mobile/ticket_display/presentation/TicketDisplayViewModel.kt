package com.ticketing.mobile.ticket_display.presentation

import com.ticketing.mobile.core_mvi.BaseViewModel
import com.ticketing.mobile.ticket_display.domain.usecase.GenerateDynamicQrUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetTicketDetailUseCase
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayEffect
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayState

/**
 * Khung sườn ViewModel cho màn hình hiển thị vé theo mô hình MVI.
 */
class TicketDisplayViewModel(
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val generateDynamicQrUseCase: GenerateDynamicQrUseCase
) : BaseViewModel<TicketDisplayState, TicketDisplayIntent, TicketDisplayEffect>(
    initialState = TicketDisplayState()
) {

    override fun handleIntent(intent: TicketDisplayIntent) {
        when (intent) {
            is TicketDisplayIntent.LoadTicket -> {
                // TODO: [Giai đoạn 3] Tự viết logic gọi getTicketDetailUseCase và bắt đầu observeQrStream
            }
            is TicketDisplayIntent.RefreshQrRequested -> {
                // TODO: [Giai đoạn 3] Tự viết logic làm mới mã QR thủ công khi người dùng click
            }
            is TicketDisplayIntent.ToggleAutoBrightness -> {
                // TODO: [Giai đoạn 3] Tự viết logic cập nhật trạng thái tự động tăng độ sáng
            }
            is TicketDisplayIntent.RetryClicked -> {
                // TODO: [Giai đoạn 3] Tự viết logic tải lại vé khi gặp lỗi
            }
        }
    }
}

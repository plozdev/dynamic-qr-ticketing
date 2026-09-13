package com.ticketing.mobile.ticket_display.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent

/**
 * Khung sườn Jetpack Compose Screen cho Ticket Display.
 * Nơi bạn tự do thiết kế UI hiển thị vé và mã Dynamic QR.
 */
@Composable
fun TicketDisplayScreen(
    viewModel: TicketDisplayViewModel,
    ticketId: String,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ticketId) {
        viewModel.handleIntent(TicketDisplayIntent.LoadTicket(ticketId))
    }

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // TODO: [Giai đoạn 3] Tự thiết kế UI Compose hiển thị:
            // 1. Thông tin vé: Tên sự kiện, địa điểm, hàng ghế, tên người giữ vé.
            // 2. Component vẽ mã QR (dùng thư viện ZXing hoặc Canvas Compose).
            // 3. Thanh ProgressBar đếm ngược chu kỳ 30 giây (rotation countdown).
            // 4. Nút bấm làm mới thủ công (Refresh Now).
            Text(text = "Ticket Display Screen Scaffold (ticketId: $ticketId)")
        }
    }
}

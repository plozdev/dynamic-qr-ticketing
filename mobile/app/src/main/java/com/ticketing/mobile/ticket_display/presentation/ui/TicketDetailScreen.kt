package com.ticketing.mobile.ticket_display.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.ticket_display.domain.model.TicketStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    viewModel: TicketDisplayViewModel,
    ticketId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.myTickets.firstOrNull { it.ticketId == ticketId }
    val ticket = state.ticket?.takeIf { it.id == ticketId }

    LaunchedEffect(ticketId) {
        viewModel.handleIntent(TicketDisplayIntent.LoadTicket(ticketId, startCheckIn = false))
    }

    val status = when {
        item?.status == UserTicketCheckInStatus.CHECKED_IN || ticket?.status == TicketStatus.CHECKED_IN -> "Đã check-in"
        item?.status == UserTicketCheckInStatus.REVOKED || ticket?.status == TicketStatus.REVOKED -> "Vé đã thu hồi"
        item?.status == UserTicketCheckInStatus.READY_TO_CHECK_IN -> "Sẵn sàng check-in"
        else -> "Chưa mở check-in"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianVoid,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết vé", color = TextHighEmphasis, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextHighEmphasis)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianVoid)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (item == null && ticket == null) {
                Text(state.errorMessage ?: "Đang tải thông tin vé...", color = TextMediumEmphasis)
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(item?.eventName ?: ticket?.eventName.orEmpty(), color = TextHighEmphasis, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text(status, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                        TicketDetailRow("Mã vé", ticketId)
                        TicketDetailRow("Hạng vé", item?.tierName ?: "—")
                        TicketDetailRow("Địa điểm", item?.venue ?: ticket?.venue ?: "—")
                        TicketDetailRow("Thời gian", item?.dateDisplay ?: "—")
                        TicketDetailRow("Cổng vào", item?.gateInfo ?: "—")
                        TicketDetailRow("Số ghế", item?.seatNumber ?: ticket?.seatNumber ?: "—")
                        TicketDetailRow("Người tham dự", item?.attendeeName ?: ticket?.ticketHolderName ?: "—")
                        if (!item?.checkInNote.isNullOrBlank()) {
                            TicketDetailRow("Ghi chú", item.checkInNote)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextMediumEmphasis, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = CyanSecondary, fontSize = 13.sp, modifier = Modifier.weight(1.5f))
    }
}

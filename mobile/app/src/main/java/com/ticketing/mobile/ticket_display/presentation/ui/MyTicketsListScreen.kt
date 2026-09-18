package com.ticketing.mobile.ticket_display.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ui.theme.AmberTertiary
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.SurfaceContainer
import com.ticketing.mobile.ui.theme.SurfaceContainerHighest
import com.ticketing.mobile.ui.theme.SurfaceContainerLow
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem

/**
 * MÀN HÌNH 2: CÁC VÉ CỦA TÔI (My Tickets Screen)
 * Khi bấm vào vé, hiển thị cửa sổ trượt lên (Slide-up Modal) với mã Dynamic QR xoay vòng liên tục.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsListScreen(
    ticketDisplayViewModel: TicketDisplayViewModel,
    modifier: Modifier = Modifier,
    onTicketSelected: ((UserTicketItem) -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedTicketForModal by remember { mutableStateOf<UserTicketItem?>(null) }

    val state by ticketDisplayViewModel.uiState.collectAsStateWithLifecycle()

    val myTickets = state.myTickets

    val filteredTickets = remember(selectedTabIndex, myTickets) {
        when (selectedTabIndex) {
            0 -> myTickets.filter { it.status == UserTicketCheckInStatus.READY_TO_CHECK_IN || it.status == UserTicketCheckInStatus.NOT_YET_CHECK_IN }
            1 -> myTickets.filter { it.status == UserTicketCheckInStatus.CHECKED_IN }
            else -> myTickets
        }
    }

    MyTicketsContent(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        tickets = filteredTickets,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { ticketDisplayViewModel.loadMyTickets() },
        onTicketClick = { ticket ->
            selectedTicketForModal = ticket
            onTicketSelected?.invoke(ticket)
        },
        modifier = modifier
    )

    // CỬA SỔ TRƯỢT LÊN / POPUP MODAL DYNAMIC QR (Hiển thị QR hoặc Chi tiết vé)
    if (selectedTicketForModal != null) {
        TicketQrSlideUpModal(
            ticketId = selectedTicketForModal!!.ticketId,
            viewModel = ticketDisplayViewModel,
            ticketItem = selectedTicketForModal,
            onDismiss = { selectedTicketForModal = null }
        )
    }
}

/**
 * Giao diện hiển thị danh sách vé của tôi (thuần UI để dễ dàng Preview).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsContent(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tickets: List<UserTicketItem>,
    onTicketClick: (UserTicketItem) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    val tabs = listOf("Sắp Diễn Ra", "Đã Sử Dụng", "Tất Cả")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianVoid.copy(alpha = 0.9f)
                ),
                title = {
                    Column {
                        Text("Vé Của Tôi", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextHighEmphasis)
                        Text("${tickets.size} vé điện tử đã đồng bộ", fontSize = 11.sp, color = EmeraldPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceContainerLow,
                contentColor = EmeraldPrimary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = EmeraldPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) EmeraldPrimary else TextMediumEmphasis
                            )
                        }
                    )
                }
            }

            // Ticket Content States
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighest),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(errorMessage, color = TextHighEmphasis, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onRetry?.invoke() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("Tải Lại Vé", color = ObsidianVoid, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (tickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎟️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bạn chưa có vé nào trong mục này.",
                            color = TextMediumEmphasis,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tickets) { ticket ->
                        UserTicketCard(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserTicketCard(
    ticket: UserTicketItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (ticket.status) {
                    UserTicketCheckInStatus.READY_TO_CHECK_IN -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldPrimary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ĐANG MỞ CỔNG CHECK-IN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                    UserTicketCheckInStatus.NOT_YET_CHECK_IN -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AmberTertiary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("⏳", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CHƯA THỂ CHECK-IN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberTertiary)
                        }
                    }
                    UserTicketCheckInStatus.CHECKED_IN -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerHighest)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("✓ ĐÃ CHECK-IN THÀNH CÔNG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMediumEmphasis)
                        }
                    }
                    UserTicketCheckInStatus.REVOKED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FieryError.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("✕ VÉ ĐÃ THU HỒI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FieryError)
                        }
                    }
                }

                Text(
                    text = ticket.tierName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Event Title & Tier
            Text(
                text = ticket.eventName,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextHighEmphasis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📍 ${ticket.venue}",
                fontSize = 12.sp,
                color = TextMediumEmphasis
            )
            Text(
                text = "📅 ${ticket.dateDisplay}",
                fontSize = 12.sp,
                color = CyanSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Perforation visual divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceContainerHighest)
            }

            // Ticket Bottom Stub: Gate, Seat, and Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text("CỬA / HÀNG GHẾ", fontSize = 10.sp, color = TextMediumEmphasis, fontWeight = FontWeight.SemiBold)
                    Text("${ticket.gateInfo} • ${ticket.seatNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextHighEmphasis)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onClick,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ticket.status == UserTicketCheckInStatus.READY_TO_CHECK_IN) EmeraldPrimary else SurfaceContainerHighest
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (ticket.status == UserTicketCheckInStatus.READY_TO_CHECK_IN) "Mở Dynamic QR" else "Xem Chi Tiết",
                        color = if (ticket.status == UserTicketCheckInStatus.READY_TO_CHECK_IN) ObsidianVoid else TextHighEmphasis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(name = "My Tickets Screen Preview", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun MyTicketsListScreenPreview() {
    DynamicQRTicketingTheme {
        MyTicketsContent(
            selectedTabIndex = 0,
            onTabSelected = {},
            tickets = listOf(
                UserTicketItem(
                    ticketId = "TKT-VN-2026-9901",
                    eventName = "HÀ NỘI ROCK FEST 2026",
                    venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
                    dateDisplay = "Hôm nay • 19:30",
                    seatNumber = "VIP-A12",
                    attendeeName = "Nguyễn Hoàng Long",
                    tierName = "VIP Diamond",
                    status = UserTicketCheckInStatus.READY_TO_CHECK_IN,
                    gateInfo = "CỔNG A1"
                ),
                UserTicketItem(
                    ticketId = "TKT-VN-2026-8802",
                    eventName = "ĐẠI NHẠC HỘI MONSOON EDM",
                    venue = "TT Hội Nghị Quốc Gia, Hà Nội",
                    dateDisplay = "15/11/2026 • 18:00",
                    seatNumber = "ZONE-FANZ-08",
                    attendeeName = "Nguyễn Hoàng Long",
                    tierName = "Fanzone Standard",
                    status = UserTicketCheckInStatus.NOT_YET_CHECK_IN,
                    gateInfo = "CỔNG B2"
                )
            ),
            onTicketClick = {}
        )
    }
}

@Preview(name = "User Ticket Card - Active", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun UserTicketCardActivePreview() {
    DynamicQRTicketingTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            UserTicketCard(
                ticket = UserTicketItem(
                    ticketId = "TKT-VN-2026-9901",
                    eventName = "HÀ NỘI ROCK FEST 2026",
                    venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
                    dateDisplay = "Hôm nay • 19:30",
                    seatNumber = "VIP-A12",
                    attendeeName = "Nguyễn Hoàng Long",
                    tierName = "VIP Diamond",
                    status = UserTicketCheckInStatus.READY_TO_CHECK_IN,
                    gateInfo = "CỔNG A1"
                ),
                onClick = {}
            )
        }
    }
}

@Preview(name = "User Ticket Card - Locked", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun UserTicketCardLockedPreview() {
    DynamicQRTicketingTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            UserTicketCard(
                ticket = UserTicketItem(
                    ticketId = "TKT-VN-2026-8802",
                    eventName = "ĐẠI NHẠC HỘI MONSOON EDM",
                    venue = "TT Hội Nghị Quốc Gia, Hà Nội",
                    dateDisplay = "15/11/2026 • 18:00",
                    seatNumber = "ZONE-FANZ-08",
                    attendeeName = "Nguyễn Hoàng Long",
                    tierName = "Fanzone Standard",
                    status = UserTicketCheckInStatus.NOT_YET_CHECK_IN,
                    gateInfo = "CỔNG B2"
                ),
                onClick = {}
            )
        }
    }
}

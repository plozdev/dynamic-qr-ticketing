package com.ticketing.mobile.ticket_display.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.ticketing.mobile.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.ticketing.mobile.ticket_display.data.dto.EventItemDto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ui.theme.AmberTertiary
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.SurfaceContainer
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.SurfaceContainerHighest
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis
import com.ticketing.mobile.ui.theme.TextMuted
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

/**
 * Mục chip lọc danh mục sự kiện kèm số lượng.
 */
data class TopBarCategoryChip(
    val id: Int,
    val title: String,
    val count: Int
)

/**
 * Logo SecureTix với viền sáng neon và biểu tượng Dynamic Shield.
 */
@Composable
fun SecureTixLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "SecureTix Logo",
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
    )
}

/**
 * Chip phân loại sự kiện phong cách Obsidian Pass với huy hiệu số lượng.
 */
@Composable
fun CategoryChipItem(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) EmeraldPrimary else SurfaceContainerHigh
    val contentColor = if (isSelected) Color(0xFF003824) else TextHighEmphasis
    val badgeContainerColor = if (isSelected) Color(0xFF003824).copy(alpha = 0.18f) else SurfaceContainerHighest
    val badgeTextColor = if (isSelected) Color(0xFF003824) else TextMuted

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(badgeContainerColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor
            )
        }
    }
}

/**
 * Thanh Top Bar cao cấp SecureTix Obsidian Pass chuẩn Prototype.
 */
@Composable
fun SecureTixTopBar(
    modifier: Modifier = Modifier,
    subtitle: String = "TẤT CẢ SỰ KIỆN",
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedChipIndex: Int = 0,
    chips: List<TopBarCategoryChip> = emptyList(),
    onChipSelected: (Int) -> Unit = {},
    onNotificationClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    onScannerClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianVoid)
            .statusBarsPadding()
    ) {
        // --- 1. BRAND HEADER ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Tiêu đề
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                SecureTixLogo()
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SecureTix",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextHighEmphasis,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = subtitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = EmeraldPrimary,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Nút hành động phải (Refresh, Scanner, Notification, Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onRefreshClick != null) {
                    IconButton(
                        onClick = { onRefreshClick.invoke() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (onScannerClick != null) {
                    IconButton(
                        onClick = { onScannerClick.invoke() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Soát vé (Scanner)",
                            tint = CyanSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = { onNotificationClick?.invoke() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Thông báo",
                            tint = TextMediumEmphasis,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Chấm xanh thông báo chưa đọc
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp, end = 7.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .border(1.5.dp, ObsidianVoid, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Avatar cá nhân
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .clickable { onProfileClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Hồ sơ",
                        tint = Color(0xFF003824),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- 2. SEARCH BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerHigh)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Tìm kiếm sự kiện",
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextHighEmphasis,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Xóa tìm kiếm",
                        tint = TextMediumEmphasis,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = { onFilterClick?.invoke() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Bộ lọc nâng cao",
                    tint = TextMediumEmphasis,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3. QUICK FILTER CHIPS ROW ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { chip ->
                val isSelected = chip.id == selectedChipIndex
                CategoryChipItem(
                    title = chip.title,
                    count = chip.count,
                    isSelected = isSelected,
                    onClick = { onChipSelected(chip.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * MÀN HÌNH 2: CÁC VÉ CỦA TÔI (My Tickets Screen)
 * Khi bấm vào vé, hiển thị cửa sổ trượt lên (Slide-up Modal) với mã Dynamic QR xoay vòng liên tục.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsListScreen(
    ticketDisplayViewModel: TicketDisplayViewModel,
    modifier: Modifier = Modifier,
    onTicketSelected: ((UserTicketItem) -> Unit)? = null,
    onScannerClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedTicketForModal by remember { mutableStateOf<UserTicketItem?>(null) }
    var showClaimSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val state by ticketDisplayViewModel.uiState.collectAsStateWithLifecycle()

    val myTickets = state.myTickets

    val filteredTickets = remember(selectedTabIndex, myTickets, searchQuery) {
        val categoryFiltered = when (selectedTabIndex) {
            0 -> myTickets // Tất cả
            1 -> myTickets.filter {
                it.eventName.contains("Rock", ignoreCase = true) ||
                it.eventName.contains("EDM", ignoreCase = true) ||
                it.eventName.contains("Concert", ignoreCase = true) ||
                it.eventName.contains("Nhạc", ignoreCase = true)
            } // Âm nhạc & Concert
            2 -> myTickets.filter {
                it.eventName.contains("Sport", ignoreCase = true) ||
                it.eventName.contains("Bóng", ignoreCase = true) ||
                it.eventName.contains("Marathon", ignoreCase = true)
            } // Thể thao
            3 -> myTickets.filter {
                it.eventName.contains("Triển lãm", ignoreCase = true) ||
                it.eventName.contains("Festival", ignoreCase = true)
            } // Triển lãm & Festival
            4 -> myTickets.filter {
                it.eventName.contains("Kịch", ignoreCase = true) ||
                it.eventName.contains("Sân khấu", ignoreCase = true)
            } // Kịch nghệ & Sân khấu
            else -> myTickets
        }

        if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            val query = searchQuery.trim().lowercase()
            categoryFiltered.filter {
                it.eventName.lowercase().contains(query) ||
                it.venue.lowercase().contains(query) ||
                it.attendeeName.lowercase().contains(query) ||
                it.tierName.lowercase().contains(query) ||
                it.ticketId.lowercase().contains(query)
            }
        }
    }

    MyTicketsContent(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        tickets = filteredTickets,
        allTickets = myTickets,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRetry = { ticketDisplayViewModel.loadMyTickets() },
        onTicketClick = { ticket ->
            selectedTicketForModal = ticket
            onTicketSelected?.invoke(ticket)
        },
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onScannerClick = onScannerClick,
        onProfileClick = onProfileClick ?: onLogoutClick,
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
    allTickets: List<UserTicketItem> = tickets,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    subtitle: String = "VÉ CỦA TÔI",
    onNotificationClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    onScannerClick: (() -> Unit)? = null
) {
    val chips = remember(allTickets) {
        listOf(
            TopBarCategoryChip(0, "Tất cả", allTickets.size),
            TopBarCategoryChip(1, "Âm nhạc & Concert", allTickets.count {
                it.eventName.contains("Rock", ignoreCase = true) ||
                it.eventName.contains("EDM", ignoreCase = true) ||
                it.eventName.contains("Concert", ignoreCase = true) ||
                it.eventName.contains("Nhạc", ignoreCase = true)
            }),
            TopBarCategoryChip(2, "Thể thao", allTickets.count {
                it.eventName.contains("Sport", ignoreCase = true) ||
                it.eventName.contains("Bóng", ignoreCase = true) ||
                it.eventName.contains("Marathon", ignoreCase = true)
            }),
            TopBarCategoryChip(3, "Triển lãm & Festival", allTickets.count {
                it.eventName.contains("Triển lãm", ignoreCase = true) ||
                it.eventName.contains("Festival", ignoreCase = true)
            }),
            TopBarCategoryChip(4, "Kịch nghệ & Sân khấu", allTickets.count {
                it.eventName.contains("Kịch", ignoreCase = true) ||
                it.eventName.contains("Sân khấu", ignoreCase = true)
            })
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianVoid,
        topBar = {
            SecureTixTopBar(
                subtitle = subtitle,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                selectedChipIndex = selectedTabIndex,
                chips = chips,
                onChipSelected = onTabSelected,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                onFilterClick = onFilterClick,
                onScannerClick = onScannerClick,
                onRefreshClick = onRetry
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Ticket Content States
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
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
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Không tìm thấy vé phù hợp" else "Bạn chưa có vé nào",
                            fontWeight = FontWeight.Bold,
                            color = TextHighEmphasis,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Hãy thử tìm kiếm với từ khóa khác." else "Vé được cấp từ hệ thống quản trị (Admin Portal) sẽ hiển thị tại đây.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onRetry?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ObsidianVoid,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Làm Mới Danh Sách",
                                color = ObsidianVoid,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
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

/**
 * Nội dung Bottom Sheet Khám Phá & Nhận Vé Nhanh (1-Click Claim).
 */
@SuppressLint("NonObservableLocale")
@Composable
fun EventClaimBottomSheetContent(
    events: List<EventItemDto>,
    isClaiming: Boolean,
    onClaim: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayEvents = events

    var claimingEventId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KHÁM PHÁ & NHẬN VÉ NHANH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Sự Kiện Nổi Bật",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextHighEmphasis
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Đóng",
                    tint = TextMediumEmphasis,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- EVENT CARDS LIST ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(displayEvents) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.category ?: "Sự Kiện",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Còn ${event.availableTickets} vé",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = event.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighEmphasis,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.venueName ?: "Chưa xác định địa điểm",
                                fontSize = 12.sp,
                                color = TextMediumEmphasis,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = SurfaceContainerHighest)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "GIÁ VÉ",
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${String.format(LocalLocale.current.platformLocale, "%,.0f", event.basePrice)} đ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighEmphasis
                                )
                            }

                            val isCurrentClaiming = isClaiming && claimingEventId == event.id
                            Button(
                                onClick = {
                                    claimingEventId = event.id
                                    onClaim(event.id)
                                },
                                enabled = !isClaiming,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = ObsidianVoid
                                )
                            ) {
                                if (isCurrentClaiming) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = ObsidianVoid,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Đang nhận...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Nhận Vé 1-Chạm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "SecureTix Top Bar Preview", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun SecureTixTopBarPreview() {
    DynamicQRTicketingTheme {
        SecureTixTopBar(
            subtitle = "TẤT CẢ SỰ KIỆN",
            searchQuery = "",
            onSearchQueryChange = {},
            selectedChipIndex = 0,
            chips = listOf(
                TopBarCategoryChip(0, "Tất cả", 124),
                TopBarCategoryChip(1, "Âm nhạc & Concert", 58),
                TopBarCategoryChip(2, "Thể thao", 32),
                TopBarCategoryChip(3, "Triển lãm & Festival", 18),
                TopBarCategoryChip(4, "Kịch nghệ & Sân khấu", 16)
            ),
            onChipSelected = {}
        )
    }
}


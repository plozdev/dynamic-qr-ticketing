package com.ticketing.mobile.events.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticketing.mobile.events.domain.model.EventItem
import com.ticketing.mobile.ui.theme.AmberTertiary
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.SurfaceContainer
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.SurfaceContainerHighest
import com.ticketing.mobile.ui.theme.SurfaceContainerLow
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis

/**
 * MÀN HÌNH 1: CÁC SỰ KIỆN HIỆN TẠI (Events Catalog Screen)
 * Thiết kế chuẩn Obsidian Pass theo nguyên mẫu Stitch Prototype.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    onEventSelected: (EventItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    val categories = listOf(
        "Tất cả" to 124,
        "Âm nhạc & Concert" to 58,
        "Thể thao" to 32,
        "Triển lãm & Festival" to 18,
        "Kịch nghệ & Sân khấu" to 16
    )

    val sampleEvents = remember {
        listOf(
            EventItem(
                id = "EVT-ROCK-2026",
                title = "Hà Nội Rock Fest 2026",
                venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
                dateDisplay = "24/10/2026 • 19:30",
                priceDisplay = "450.000₫",
                category = "Âm nhạc & Concert",
                remainingPercentage = 12,
                isHotTrend = true,
                isDynamicPassSupported = true
            ),
            EventItem(
                id = "EVT-EDM-2026",
                title = "Đại Nhạc Hội Monsoon EDM",
                venue = "TT Hội Nghị Quốc Gia, Hà Nội",
                dateDisplay = "15/11/2026 • 18:00",
                priceDisplay = "690.000₫",
                category = "Âm nhạc & Concert",
                remainingPercentage = 45,
                isHotTrend = true,
                isDynamicPassSupported = true
            ),
            EventItem(
                id = "EVT-FOOTBALL-2026",
                title = "Chung Kết Cúp Quốc Gia 2026",
                venue = "SVĐ Hàng Đẫy, Hà Nội",
                dateDisplay = "30/10/2026 • 17:00",
                priceDisplay = "200.000₫",
                category = "Thể thao",
                remainingPercentage = 8,
                isHotTrend = false,
                isDynamicPassSupported = true
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianVoid.copy(alpha = 0.9f)
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SecureTix", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextHighEmphasis)
                            Text("TẤT CẢ SỰ KIỆN", fontSize = 10.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- 1. THANH TÌM KIẾM ---
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm hơn 120+ sự kiện, nghệ sĩ, SVĐ...", fontSize = 13.sp, color = TextMediumEmphasis) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerHighest,
                        unfocusedContainerColor = SurfaceContainerHigh,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextHighEmphasis,
                        unfocusedTextColor = TextHighEmphasis
                    ),
                    singleLine = true
                )
            }

            // --- 2. HÀNG CHIP THỂ LOẠI (Scroll ngang) ---
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.width(8.dp)) }
                items(categories) { (cat, count) ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) ObsidianVoid.copy(alpha = 0.3f) else SurfaceContainerHighest)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("$count", fontSize = 10.sp, color = if (isSelected) ObsidianVoid else TextMediumEmphasis)
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = ObsidianVoid,
                            containerColor = SurfaceContainerHigh,
                            labelColor = TextMediumEmphasis
                        ),
                        border = null,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                item { Spacer(modifier = Modifier.width(8.dp)) }
            }

            // --- 3. BANNER BẢO MẬT OBSIDIAN PASS ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Mã hóa Obsidian Pass™", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextHighEmphasis)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("BẢO MẬT CAO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                        Text(
                            "Mã QR xoay liên tục 30s chống chụp màn hình và hỗ trợ xác thực Offline siêu tốc.",
                            fontSize = 11.sp,
                            color = TextMediumEmphasis,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // --- 4. DANH SÁCH SỰ KIỆN NỔI BẬT ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sắp Diễn Ra Tuần Này", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextHighEmphasis)
                    }
                    Text("Xem tất cả (8)", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                }

                sampleEvents.forEach { event ->
                    EventCardItem(event = event, onSelect = { onEventSelected(event) })
                }
            }
        }
    }
}

@Composable
fun EventCardItem(
    event: EventItem,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(SurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Text("🎸 🎶 🏟️", fontSize = 32.sp)
                // Badges overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (event.isDynamicPassSupported) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianVoid.copy(alpha = 0.8f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("🔄 Dynamic Pass", fontSize = 10.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (event.remainingPercentage <= 15) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FieryError.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Còn ${event.remainingPercentage}% vé", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Body info
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextHighEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${event.venue}",
                    fontSize = 12.sp,
                    color = TextMediumEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "📅 ${event.dateDisplay}",
                    fontSize = 12.sp,
                    color = CyanSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = SurfaceContainerHigh
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Giá vé từ", fontSize = 10.sp, color = TextMediumEmphasis)
                        Text(event.priceDisplay, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldPrimary)
                    }
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Chọn vé", color = ObsidianVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(name = "Event List Screen Preview", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun EventListScreenPreview() {
    DynamicQRTicketingTheme {
        EventListScreen(onEventSelected = {})
    }
}

@Preview(name = "Event Card Preview", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun EventCardPreview() {
    DynamicQRTicketingTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventCardItem(
                event = EventItem(
                    id = "EVT-ROCK-2026",
                    title = "Hà Nội Rock Fest 2026",
                    venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
                    dateDisplay = "24/10/2026 • 19:30",
                    priceDisplay = "450.000₫",
                    category = "Âm nhạc & Concert",
                    remainingPercentage = 12,
                    isHotTrend = true,
                    isDynamicPassSupported = true
                ),
                onSelect = {}
            )
        }
    }
}

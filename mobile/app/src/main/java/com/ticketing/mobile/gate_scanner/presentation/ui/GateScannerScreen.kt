package com.ticketing.mobile.gate_scanner.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.gate_scanner.domain.model.DenyReason
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.presentation.GateScannerViewModel
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerIntent
import com.ticketing.mobile.gate_scanner.presentation.contract.GateScannerState
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
 * MÀN HÌNH SOÁT VÉ CỔNG - GATE SCANNER SCREEN
 * Thiết kế chuẩn Obsidian Pass theo nguyên mẫu Stitch Prototype.
 * Hiển thị rõ nét cả hai trạng thái: THÀNH CÔNG (Granted) và THẤT BẠI (Denied).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateScannerScreen(
    viewModel: GateScannerViewModel,
    gateId: String,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var manualPayloadInput by remember { mutableStateOf("") }

    GateScannerContent(
        state = state,
        gateId = gateId,
        manualPayloadInput = manualPayloadInput,
        onManualPayloadChange = { manualPayloadInput = it },
        onScanPayload = { payload ->
            viewModel.handleIntent(GateScannerIntent.QrCodeScanned(payload, gateId))
        },
        onResetScanner = { viewModel.handleIntent(GateScannerIntent.ResetScanner) },
        onToggleOfflineMode = { viewModel.handleIntent(GateScannerIntent.SetOfflineMode(it)) },
        modifier = modifier
    )
}

/**
 * Giao diện chính của màn hình soát vé cổng (thuần UI để dễ dàng Preview).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateScannerContent(
    state: GateScannerState,
    gateId: String,
    manualPayloadInput: String,
    onManualPayloadChange: (String) -> Unit,
    onScanPayload: (String) -> Unit,
    onResetScanner: () -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianVoid.copy(alpha = 0.9f)),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📷", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Soát Vé Cổng", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextHighEmphasis)
                            Text("CỔNG HIỆN TẠI: $gateId", fontSize = 10.sp, color = CyanSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (state.isOfflineMode) EmeraldPrimary.copy(alpha = 0.2f) else CyanSecondary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (state.isOfflineMode) "Offline Mode" else "Online API",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isOfflineMode) EmeraldPrimary else CyanSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.isOfflineMode,
                            onCheckedChange = onToggleOfflineMode,
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. CAMERA SCANNER VIEWPORT (Khung ngắm máy quét) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(1.dp, SurfaceContainerHigh, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .border(3.dp, EmeraldPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isValidating) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = EmeraldPrimary, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Đang kiểm tra chữ ký C++...", fontSize = 11.sp, color = EmeraldPrimary)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚡", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ĐƯA MÃ QR VÀO KHUNG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tự động nhận diện 100fps",
                                    fontSize = 9.sp,
                                    color = TextMediumEmphasis
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianVoid.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Giữ khoảng cách 15 - 25 cm với màn hình", fontSize = 10.sp, color = TextMediumEmphasis)
                }
            }

            // --- 2. BANNER KẾT QUẢ SOÁT VÉ (THÀNH CÔNG HOẶC THẤT BẠI) ---
            val lastStatus = state.lastAccessStatus

            if (lastStatus == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sẵn sàng quét vé tiếp theo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextHighEmphasis)
                            Text("Hệ thống soát vé Offline & Online đang hoạt động.", fontSize = 11.sp, color = TextMediumEmphasis)
                        }
                    }
                }
            } else {
                when (lastStatus) {
                    // ==========================================
                    // SCREEN KẾT QUẢ: THÀNH CÔNG (ACCESS GRANTED)
                    // ==========================================
                    is GateAccessStatus.Granted -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(2.dp, EmeraldPrimary)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(EmeraldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ObsidianVoid)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text("HỢP LỆ - MỜI VÀO", fontWeight = FontWeight.Black, fontSize = 18.sp, color = EmeraldPrimary)
                                        Text("Vé đã xác thực thành công • Cửa mở", fontSize = 12.sp, color = TextMediumEmphasis)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceContainerHigh)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceContainerLow)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("KHÁCH HÀNG", fontSize = 10.sp, color = TextMediumEmphasis)
                                        Text(lastStatus.attendeeName.ifEmpty { "Nguyễn Hoàng Long" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextHighEmphasis)
                                    }
                                    Column {
                                        Text("SỐ GHẾ", fontSize = 10.sp, color = TextMediumEmphasis)
                                        Text(lastStatus.seatNumber.ifEmpty { "VIP-A12" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberTertiary)
                                    }
                                    Column {
                                        Text("MÃ VÉ", fontSize = 10.sp, color = TextMediumEmphasis)
                                        Text(lastStatus.ticketId.takeLast(8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanSecondary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = onResetScanner,
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Quét Tiếp Người Sau", color = ObsidianVoid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SCREEN KẾT QUẢ: THẤT BẠI (ACCESS DENIED)
                    // ==========================================
                    is GateAccessStatus.Denied -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(2.dp, FieryError)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(FieryError),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text("TỪ CHỐI RA VÀO", fontWeight = FontWeight.Black, fontSize = 18.sp, color = FieryError)
                                        Text("Cảnh báo: Mã QR không hợp lệ!", fontSize = 12.sp, color = TextMediumEmphasis)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceContainerHigh)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FieryError.copy(alpha = 0.12f))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "LÝ DO TỪ CHỐI: ${lastStatus.reason.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = FieryError
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = lastStatus.message,
                                            fontSize = 12.sp,
                                            color = TextHighEmphasis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = onResetScanner,
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHighest),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Bỏ Qua & Quét Lại", color = TextHighEmphasis, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. MATRIX THỐNG KÊ LƯỢT VÀO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎟️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("ĐÃ CHECK-IN", fontSize = 10.sp, color = TextMediumEmphasis)
                        Text("1,420 / 2,000", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextHighEmphasis)
                    }
                }
                Box(modifier = Modifier.size(1.dp, 24.dp).background(SurfaceContainerHighest))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("TỐC ĐỘ XỬ LÝ", fontSize = 10.sp, color = TextMediumEmphasis)
                        Text("42 vé/phút", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanSecondary)
                    }
                }
            }

            // --- 4. HỘP TEST GIẢ LẬP CÁC KỊCH BẢN (THÀNH CÔNG / THẤT BẠI) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🧪 MÔ PHỎNG TEST SOÁT VÉ (QA & DEV)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMediumEmphasis)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Test THÀNH CÔNG
                        Button(
                            onClick = {
                                val currentSec = System.currentTimeMillis() / 1000
                                val expiresAt = (currentSec / 30 + 1) * 30
                                val validPayload = "TICKETING:TKT-VN-2026-9901:$expiresAt:m7bX8_G1jVq2L3z4K5p6N7q8"
                                onScanPayload(validPayload)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Thành Công", fontSize = 11.sp, color = ObsidianVoid, fontWeight = FontWeight.Bold)
                        }

                        // Test THẤT BẠI (Hết hạn)
                        Button(
                            onClick = {
                                val expiredPayload = "TICKETING:TKT-VN-2026-9901:1600000000:expired_token_data"
                                onScanPayload(expiredPayload)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FieryError),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Thất Bại", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Test THẤT BẠI (Mã rác)
                    OutlinedButton(
                        onClick = {
                            onScanPayload("INVALID_NON_TICKETING_DATA")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test Quét Mã QR Lạ / Giả Mạo", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(name = "Gate Scanner - Ready To Scan", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun GateScannerReadyPreview() {
    DynamicQRTicketingTheme {
        GateScannerContent(
            state = GateScannerState(isOfflineMode = false),
            gateId = "GATE-EAST-01",
            manualPayloadInput = "",
            onManualPayloadChange = {},
            onScanPayload = {},
            onResetScanner = {},
            onToggleOfflineMode = {}
        )
    }
}

@Preview(name = "Gate Scanner - Access Granted (Hợp Lệ)", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun GateScannerGrantedPreview() {
    DynamicQRTicketingTheme {
        GateScannerContent(
            state = GateScannerState(
                lastAccessStatus = GateAccessStatus.Granted(
                    ticketId = "TKT-VN-2026-9901",
                    attendeeName = "Nguyễn Hoàng Long",
                    seatNumber = "VIP-A12",
                    checkInTimestamp = 1773513600L
                )
            ),
            gateId = "GATE-EAST-01",
            manualPayloadInput = "",
            onManualPayloadChange = {},
            onScanPayload = {},
            onResetScanner = {},
            onToggleOfflineMode = {}
        )
    }
}

@Preview(name = "Gate Scanner - Access Denied (Từ Chối)", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun GateScannerDeniedPreview() {
    DynamicQRTicketingTheme {
        GateScannerContent(
            state = GateScannerState(
                lastAccessStatus = GateAccessStatus.Denied(
                    reason = DenyReason.EXPIRED_TIMESTAMP,
                    message = "Mã QR đã quá thời hạn hiệu lực 30 giây. Vui lòng mở lại mã mới."
                )
            ),
            gateId = "GATE-EAST-01",
            manualPayloadInput = "",
            onManualPayloadChange = {},
            onScanPayload = {},
            onResetScanner = {},
            onToggleOfflineMode = {}
        )
    }
}

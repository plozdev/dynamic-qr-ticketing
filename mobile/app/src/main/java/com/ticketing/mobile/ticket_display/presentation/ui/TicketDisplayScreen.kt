package com.ticketing.mobile.ticket_display.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.model.TicketStatus
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayState
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

import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus

@Composable
fun TicketQrSlideUpModal(
    ticketId: String,
    viewModel: TicketDisplayViewModel,
    ticketItem: UserTicketItem? = null,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ticketId) {
        viewModel.handleIntent(TicketDisplayIntent.LoadTicket(ticketId))
    }

    Dialog(
        onDismissRequest = {
            viewModel.handleIntent(TicketDisplayIntent.StopQrObservation)
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Nền mờ (scrim overlay) toàn màn hình, bấm ra ngoài để đóng
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.handleIntent(TicketDisplayIntent.StopQrObservation)
                    onDismiss()
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Hiệu ứng cửa sổ trượt từ dưới lên
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                TicketQrPopupModalCard(
                    ticketId = ticketId,
                    ticket = state.ticket,
                    ticketItem = ticketItem,
                    dynamicQr = state.dynamicQr,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                    onRefreshQr = { viewModel.handleIntent(TicketDisplayIntent.RefreshQrRequested) },
                    onClose = {
                        viewModel.handleIntent(TicketDisplayIntent.StopQrObservation)
                        onDismiss()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )
            }
        }
    }
}

/**
 * Alias cho TicketQrSlideUpModal để tương thích ngược.
 */
@Composable
fun TicketQrBottomSheetModal(
    ticketId: String,
    viewModel: TicketDisplayViewModel,
    onDismiss: () -> Unit
) {
    TicketQrSlideUpModal(
        ticketId = ticketId,
        viewModel = viewModel,
        onDismiss = onDismiss
    )
}

/**
 * Thẻ giao diện Card của cửa sổ Modal / Pop-up QR.
 * Thiết kế tinh giản, chỉ chứa mã QR và các thông tin cơ bản phục vụ check-in nhanh.
 */
@Composable
fun TicketQrPopupModalCard(
    ticketId: String,
    ticket: Ticket?,
    ticketItem: UserTicketItem? = null,
    dynamicQr: DynamicQrData?,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRefreshQr: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayStatus = when {
        ticketItem?.status == UserTicketCheckInStatus.NOT_YET_CHECK_IN -> TicketStatus.EXPIRED
        ticketItem?.status == UserTicketCheckInStatus.CHECKED_IN -> TicketStatus.CHECKED_IN
        ticketItem?.status == UserTicketCheckInStatus.REVOKED -> TicketStatus.REVOKED
        ticket?.status == TicketStatus.CHECKED_IN -> TicketStatus.CHECKED_IN
        ticket?.status == TicketStatus.REVOKED -> TicketStatus.REVOKED
        ticket?.status == TicketStatus.EXPIRED -> TicketStatus.EXPIRED
        else -> TicketStatus.ACTIVE
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Ngăn chặn bấm xuyên thấu thẻ */ },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        border = BorderStroke(
            1.5.dp,
            if (displayStatus == TicketStatus.ACTIVE) EmeraldPrimary.copy(alpha = 0.35f) else AmberTertiary.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thanh tay nắm kéo trượt (Drag Handle hint)
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Header cửa sổ: Tên sự kiện & Hạng vé & Nút đóng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticketItem?.eventName ?: ticket?.eventName ?: "Sự Kiện Check-in",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextHighEmphasis,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (displayStatus == TicketStatus.ACTIVE) EmeraldPrimary else AmberTertiary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ticketItem?.tierName ?: "Vé Tiêu Chuẩn",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (displayStatus == TicketStatus.ACTIVE) EmeraldPrimary else AmberTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = TextMediumEmphasis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "#TKT-${ticketId.takeLast(6).uppercase()}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMediumEmphasis
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHighest)
                ) {
                    Text("✕", color = TextHighEmphasis, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phần thân chính: Loading, Lỗi, hoặc Mã QR
            when {
                isLoading && ticket == null && ticketItem == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = EmeraldPrimary, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Đang nạp mã vé bảo mật...", color = TextMediumEmphasis, fontSize = 12.sp)
                    }
                }
                errorMessage != null && ticket == null && ticketItem == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Lỗi: $errorMessage", color = FieryError, fontSize = 12.sp)
                    }
                }
                else -> {
                    // Hộp hiển thị mã QR tinh giản
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLow)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (displayStatus) {
                            TicketStatus.ACTIVE -> {
                                CompactActiveDynamicQr(
                                    ticketId = ticketId,
                                    dynamicQr = dynamicQr,
                                    onRefreshQr = onRefreshQr
                                )
                            }
                            TicketStatus.EXPIRED -> {
                                CompactLockedQr(gateOpensAt = ticketItem?.dateDisplay ?: "Trước giờ diễn 2 tiếng")
                            }
                            TicketStatus.CHECKED_IN -> {
                                CompactCheckedInQr(
                                    attendeeName = ticketItem?.attendeeName ?: ticket?.ticketHolderName ?: "Khách mời",
                                    seatCode = ticketItem?.seatNumber ?: ticket?.seatNumber ?: "VIP-A12"
                                )
                            }
                            TicketStatus.REVOKED -> {
                                CompactRevokedQr()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bảng thông tin cơ bản: Khách hàng & Vị trí ghế
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("KHÁCH HÀNG", fontSize = 9.sp, color = TextMediumEmphasis, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = ticketItem?.attendeeName ?: ticket?.ticketHolderName ?: "Nguyễn Hoàng Long",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHighEmphasis
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("VỊ TRÍ GHẾ", fontSize = 9.sp, color = TextMediumEmphasis, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${ticketItem?.gateInfo ?: "CỔNG A1"} • ${ticketItem?.seatNumber ?: ticket?.seatNumber ?: "VIP-A12"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dòng chỉ dẫn nhanh
                    Text(
                        text = if (displayStatus == TicketStatus.ACTIVE) {
                            "💡 Đưa mã QR vào máy quét tại cổng để vào sự kiện"
                        } else if (displayStatus == TicketStatus.EXPIRED) {
                            "ℹ️ Mã QR sẽ tự động kích hoạt khi cổng check-in mở"
                        } else {
                            "✓ Vé điện tử đã sử dụng thành công"
                        },
                        fontSize = 11.sp,
                        color = TextMediumEmphasis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút đóng cửa sổ nhanh
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHighest),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Đóng cửa sổ", color = TextHighEmphasis, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Khối QR đang mở cổng: Hiển thị ma trận QR + vòng đếm ngược giây thời gian thực.
 */
@Composable
private fun CompactActiveDynamicQr(
    ticketId: String,
    dynamicQr: DynamicQrData?,
    onRefreshQr: () -> Unit
) {
    val remaining = dynamicQr?.remainingSeconds ?: 30
    val total = dynamicQr?.totalIntervalSeconds ?: 30
    val progress = (remaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val tokenHash = dynamicQr?.qrPayload ?: ticketId

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DynamicQrMatrixGraphic(
                    tokenHash = tokenHash,
                    modifier = Modifier.size(165.dp)
                )

            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vòng đếm ngược giây thời gian thực (Cập nhật liên tục mỗi giây)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(30.dp),
                    color = if (remaining <= 5) AmberTertiary else EmeraldPrimary,
                    trackColor = SurfaceContainerHighest,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "${remaining}s",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Tự động đổi mã sau ${remaining} giây",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis
                )
                Text(
                    text = "Mã bảo mật: ${dynamicQr?.qrPayload?.substringAfterLast(":")?.take(12) ?: "••••••••••••"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = CyanSecondary
                )
            }
        }
    }
}

/**
 * Khối QR chưa tới giờ check-in.
 */
@Composable
private fun CompactLockedQr(gateOpensAt: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceContainerHigh)
                .border(1.5.dp, AmberTertiary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MÃ QR ĐANG TẠM KHÓA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AmberTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cổng soát vé chưa mở.\nMã QR động sẽ tự động kích hoạt trước giờ bắt đầu sự kiện.",
                    fontSize = 11.sp,
                    color = TextMediumEmphasis,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Khối QR đã check-in thành công.
 */
@Composable
private fun CompactCheckedInQr(attendeeName: String, seatCode: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EmeraldPrimary.copy(alpha = 0.12f))
                .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✓", fontSize = 42.sp, fontWeight = FontWeight.Black, color = EmeraldPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ĐÃ CHECK-IN THÀNH CÔNG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ghế: $seatCode",
                    fontSize = 11.sp,
                    color = TextHighEmphasis
                )
                Text(
                    text = "Đã qua cổng lúc 19:30",
                    fontSize = 9.sp,
                    color = TextMediumEmphasis
                )
            }
        }
    }
}

/**
 * Khối QR đã bị thu hồi hoặc hủy.
 */
@Composable
private fun CompactRevokedQr() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FieryError.copy(alpha = 0.15f))
                .border(1.5.dp, FieryError, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✕", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = FieryError)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "VÉ ĐÃ BỊ THU HỒI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FieryError
                )
            }
        }
    }
}

/**
 * Đồ họa QR Matrix mô phỏng mã QR ma trận độ phân giải cao với 3 mắt căn chỉnh và dữ liệu sinh động.
 */
@Composable
private fun DynamicQrMatrixGraphic(
    modifier: Modifier = Modifier,
    tokenHash: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val canvasSize = size.width
            val moduleCount = 25
            val moduleSize = canvasSize / moduleCount

            // Vẽ 3 mắt định vị QR (Finder patterns 7x7)
            fun drawFinderPattern(startX: Float, startY: Float) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(startX, startY),
                    size = Size(moduleSize * 7, moduleSize * 7)
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(startX + moduleSize, startY + moduleSize),
                    size = Size(moduleSize * 5, moduleSize * 5)
                )
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(startX + moduleSize * 2, startY + moduleSize * 2),
                    size = Size(moduleSize * 3, moduleSize * 3)
                )
            }

            drawFinderPattern(0f, 0f)
            drawFinderPattern(moduleSize * (moduleCount - 7), 0f)
            drawFinderPattern(0f, moduleSize * (moduleCount - 7))

            // Dải đồng bộ Timing Patterns
            for (i in 8 until (moduleCount - 8)) {
                if (i % 2 == 0) {
                    drawRect(color = Color.Black, topLeft = Offset(i * moduleSize, 6 * moduleSize), size = Size(moduleSize, moduleSize))
                    drawRect(color = Color.Black, topLeft = Offset(6 * moduleSize, i * moduleSize), size = Size(moduleSize, moduleSize))
                }
            }

            // Dữ liệu ma trận biến đổi dựa trên Hash của TOTP Token
            val hashBytes = tokenHash.toByteArray()
            var byteIdx = 0
            for (row in 0 until moduleCount) {
                for (col in 0 until moduleCount) {
                    val inTopLeft = row < 8 && col < 8
                    val inTopRight = row < 8 && col >= (moduleCount - 8)
                    val inBottomLeft = row >= (moduleCount - 8) && col < 8
                    val inCenterLogo = row in 10..14 && col in 10..14
                    val isTiming = (row == 6 && col in 8 until (moduleCount - 8)) || (col == 6 && row in 8 until (moduleCount - 8))

                    if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenterLogo && !isTiming) {
                        val b = if (hashBytes.isNotEmpty()) hashBytes[byteIdx % hashBytes.size].toInt() else 0
                        byteIdx++
                        val isDark = ((b xor (row * 31 + col * 17)) and 1) == 1
                        if (isDark) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(col * moduleSize, row * moduleSize),
                                size = Size(moduleSize * 0.95f, moduleSize * 0.95f)
                            )
                        }
                    }
                }
            }
        }

        // Huy hiệu bảo mật Shield trung tâm
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ObsidianVoid)
                .border(1.2.dp, EmeraldPrimary, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🛡️", fontSize = 13.sp)
        }
    }
}

/**
 * MÀN HÌNH ĐỘC LẬP: QR CHECKING VÉ ĐIỆN TỬ (Dynamic QR Pass Full Screen)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDisplayScreen(
    viewModel: TicketDisplayViewModel,
    ticketId: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ticketId) {
        viewModel.handleIntent(TicketDisplayIntent.LoadTicket(ticketId))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianVoid.copy(alpha = 0.9f)
                ),
                title = {
                    Column {
                        Text("Mã Vé Check-in", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextHighEmphasis)
                        Text(ticketId, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = EmeraldPrimary)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Tăng sáng", fontSize = 11.sp, color = TextMediumEmphasis)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.isAutoBrightnessEnabled,
                            onCheckedChange = {
                                viewModel.handleIntent(TicketDisplayIntent.ToggleAutoBrightness(it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            TicketQrPopupModalCard(
                ticketId = ticketId,
                ticket = state.ticket,
                dynamicQr = state.dynamicQr,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage,
                onRefreshQr = { viewModel.handleIntent(TicketDisplayIntent.RefreshQrRequested) },
                onClose = { onBack?.invoke() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// =========================================================================
// PREVIEW COMPOSABLES DÀNH CHO ANDROID STUDIO
// =========================================================================

@Preview(name = "QR Slide-up Modal - Active (Đang check-in)", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun TicketQrPopupModalActivePreview() {
    DynamicQRTicketingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            TicketQrPopupModalCard(
                ticketId = "TKT-VN-2026-9901",
                ticket = Ticket(
                    id = "TKT-VN-2026-9901",
                    eventName = "HÀ NỘI ROCK FEST 2026",
                    venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
                    eventTimestamp = 1773513600L,
                    seatNumber = "VIP-A12",
                    ticketHolderName = "Nguyễn Hoàng Long",
                    status = TicketStatus.ACTIVE
                ),
                dynamicQr = DynamicQrData(
                    ticketId = "TKT-VN-2026-9901",
                    qrPayload = "TICKETING:TKT-VN-2026-9901:1773513630:c8f92a10e7b4",
                    validUntilEpochSeconds = 1773513630L,
                    totalIntervalSeconds = 30,
                    remainingSeconds = 24
                ),
                onClose = {}
            )
        }
    }
}

@Preview(name = "QR Slide-up Modal - Locked (Chưa mở cổng)", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun TicketQrPopupModalLockedPreview() {
    DynamicQRTicketingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            TicketQrPopupModalCard(
                ticketId = "TKT-VN-2026-8802",
                ticket = Ticket(
                    id = "TKT-VN-2026-8802",
                    eventName = "ĐẠI NHẠC HỘI MONSOON EDM",
                    venue = "TT Hội Nghị Quốc Gia",
                    eventTimestamp = 1773513600L,
                    seatNumber = "FANZ-08",
                    ticketHolderName = "Nguyễn Hoàng Long",
                    status = TicketStatus.EXPIRED
                ),
                dynamicQr = null,
                onClose = {}
            )
        }
    }
}

@Preview(name = "QR Slide-up Modal - Checked-In (Đã qua cổng)", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun TicketQrPopupModalCheckedInPreview() {
    DynamicQRTicketingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            TicketQrPopupModalCard(
                ticketId = "TKT-VN-2026-7703",
                ticket = Ticket(
                    id = "TKT-VN-2026-7703",
                    eventName = "CHUNG KẾT CÚP QUỐC GIA",
                    venue = "SVĐ Hàng Đẫy",
                    eventTimestamp = 1773513600L,
                    seatNumber = "STAND-A-45",
                    ticketHolderName = "Nguyễn Hoàng Long",
                    status = TicketStatus.CHECKED_IN
                ),
                dynamicQr = null,
                onClose = {}
            )
        }
    }
}

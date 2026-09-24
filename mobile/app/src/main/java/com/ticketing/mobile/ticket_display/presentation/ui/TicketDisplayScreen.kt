package com.ticketing.mobile.ticket_display.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.ticket_display.domain.model.DynamicQrData
import com.ticketing.mobile.ticket_display.domain.model.Ticket
import com.ticketing.mobile.ticket_display.domain.model.TicketStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.contract.TicketDisplayIntent
import com.ticketing.mobile.ui.theme.AmberTertiary
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis

/**
 * Cửa sổ trượt lên hiển thị Dynamic QR Card dạng vé theo thiết kế Obsidian Pass.
 */
@Composable
fun TicketQrSlideUpModal(
    ticketId: String,
    viewModel: TicketDisplayViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    ticketItem: UserTicketItem? = null
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.handleIntent(TicketDisplayIntent.StopQrObservation)
                    onDismiss()
                }
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Nút đóng nhanh phía trên card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.handleIntent(TicketDisplayIntent.StopQrObservation)
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2937))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = TextHighEmphasis,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    TicketQrPopupModalCard(
                        ticketId = ticketId,
                        ticket = state.ticket,
                        onClose = {
                            viewModel.handleIntent(TicketDisplayIntent.StopQrObservation)
                            onDismiss()
                        },
                        ticketItem = ticketItem,
                        dynamicQr = state.dynamicQr,
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage,
                        onRefreshQr = { viewModel.handleIntent(TicketDisplayIntent.RefreshQrRequested) }
                    )
                }
            }
        }
    }
}

/**
 * Thẻ Card Dynamic Pass chuẩn theo thiết kế layout nguyên mẫu (img.png).
 */
@Composable
fun TicketQrPopupModalCard(
    ticketId: String,
    ticket: Ticket?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    ticketItem: UserTicketItem? = null,
    dynamicQr: DynamicQrData? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRefreshQr: () -> Unit = {}
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

    val tierTitle = (ticketItem?.tierName ?: "VIP DIAMOND - FANZONE A").uppercase()
    val ticketNumber = if (ticketId.startsWith("#TKT-")) ticketId else "#TKT-${ticketId.takeLast(4).uppercase().ifEmpty { "9901" }}"
    val eventTitle = ticketItem?.eventName ?: ticket?.eventName ?: "HÀ NỘI ROCK FEST 2026"
    val dateLocation = "${ticketItem?.dateDisplay ?: "24/10/2026 - 19:30"} • ${ticketItem?.venue ?: "Sân Vận Động Mỹ Đình"}"
    val gateInfo = ticketItem?.gateInfo?.ifEmpty { "GATE A1" } ?: "GATE A1"
    val zoneInfo = if (tierTitle.contains("FANZONE") || tierTitle.contains("ZONE")) "ZONE A" else "KHU VỰC"
    val seatInfo = ticketItem?.seatNumber?.ifEmpty { "A1-042" } ?: ticket?.seatNumber ?: "A1-042"
    val attendeeName = ticketItem?.attendeeName?.ifEmpty { "Nguyễn Văn An" } ?: ticket?.ticketHolderName ?: "Nguyễn Văn An"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Chống bấm xuyên qua card */ },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141B2B)),
        border = BorderStroke(1.dp, Color(0xFF232A3A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ==========================================
            // 1. TOP SECTION (Header badge + Event info)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 18.dp, bottom = 14.dp)
            ) {
                // Hàng tiêu đề: Badge Hạng vé & Mã vé #TKT kèm nút đóng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F3627))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tierTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ticketNumber,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextMediumEmphasis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng",
                                tint = TextMediumEmphasis,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Thông tin sự kiện: Ảnh Thumbnail + Tên sự kiện + Ngày giờ & Địa điểm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConcertThumbnail()
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eventTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextHighEmphasis,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = dateLocation,
                            fontSize = 11.5.sp,
                            color = TextMediumEmphasis,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // ==========================================
            // 2. TICKET PERFORATION DIVIDER (Đường cắt xé vé)
            // ==========================================
            TicketPerforationDivider(
                cutoutRadius = 11.dp,
                cutoutColor = ObsidianVoid,
                dashColor = Color(0xFF2E3545)
            )

            // ==========================================
            // 3. MAIN SECTION (QR + Countdown + Security)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp, bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isLoading && ticket == null && ticketItem == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
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
                                .height(200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Lỗi: $errorMessage", color = FieryError, fontSize = 12.sp)
                        }
                    }
                    else -> {
                        when (displayStatus) {
                            TicketStatus.ACTIVE -> {
                                if (errorMessage != null) {
                                    Text(text = errorMessage, color = FieryError, fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 8.dp))
                                }
                                ActiveDynamicQrSection(
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
                                    attendeeName = attendeeName,
                                    seatCode = seatInfo
                                )
                            }
                            TicketStatus.REVOKED -> {
                                CompactRevokedQr()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 4. 3-COLUMN TELEMETRY GRID (Cổng - Khu vực - Số ghế)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: CỔNG VÀO
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1726))
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CỔNG VÀO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMediumEmphasis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = gateInfo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextHighEmphasis
                        )
                    }

                    // Box 2: KHU VỰC
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1726))
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KHU VỰC",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMediumEmphasis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = zoneInfo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanSecondary
                        )
                    }

                    // Box 3: SỐ GHẾ
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F1726))
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SỐ GHẾ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMediumEmphasis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = seatInfo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // 5. ATTENDEE STRIP (Khách hàng & CCCD)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1726))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextMediumEmphasis,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = attendeeName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighEmphasis
                        )
                    }

                    Text(
                        text = "CCCD: •••• 5821",
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMediumEmphasis
                    )
                }
            }
        }
    }
}

/**
 * Thumbnail sự kiện phong cách sân khấu biểu diễn với dải sáng concert.
 */
@Composable
fun ConcertThumbnail(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE11D48),
                        Color(0xFFD97706),
                        Color(0xFF0F172A)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color(0x664EDEA3),
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color(0x664CD7F6),
                start = Offset(size.width * 0.3f, size.height),
                end = Offset(size.width * 0.7f, 0f),
                strokeWidth = 1.5.dp.toPx()
            )
            drawArc(
                color = Color(0xCC0C1322),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(-size.width * 0.2f, size.height * 0.6f),
                size = Size(size.width * 1.4f, size.height * 0.8f)
            )
        }
        Text("🎸", fontSize = 22.sp)
    }
}

/**
 * Đường vạch đứt đoạn nối liền 2 rãnh khoét bán nguyệt mép thẻ vé.
 */
@Composable
fun TicketPerforationDivider(
    modifier: Modifier = Modifier,
    cutoutRadius: Dp = 10.dp,
    cutoutColor: Color = ObsidianVoid,
    dashColor: Color = Color(0xFF2E3545)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(cutoutRadius * 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vết khuyết bán nguyệt trái
        Canvas(modifier = Modifier.size(width = cutoutRadius, height = cutoutRadius * 2)) {
            drawArc(
                color = cutoutColor,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(-size.width, 0f),
                size = Size(size.width * 2, size.height)
            )
        }

        // Đường vạch đứt đoạn (Perforation Dashes)
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(horizontal = 4.dp)
        ) {
            val dashWidth = 5.dp.toPx()
            val gapWidth = 4.dp.toPx()
            var startX = 0f
            while (startX < size.width) {
                drawLine(
                    color = dashColor,
                    start = Offset(startX, 0f),
                    end = Offset(minOf(startX + dashWidth, size.width), 0f),
                    strokeWidth = 1.2.dp.toPx()
                )
                startX += dashWidth + gapWidth
            }
        }

        // Vết khuyết bán nguyệt phải
        Canvas(modifier = Modifier.size(width = cutoutRadius, height = cutoutRadius * 2)) {
            drawArc(
                color = cutoutColor,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = Size(size.width * 2, size.height)
            )
        }
    }
}

/**
 * Khối QR đang mở cổng: Chassis màu trắng tinh, icon xoay và huy hiệu bảo mật.
 */
@Composable
private fun ActiveDynamicQrSection(
    ticketId: String,
    dynamicQr: DynamicQrData?,
    modifier: Modifier = Modifier,
    onRefreshQr: () -> Unit = {}
) {
    val remaining = dynamicQr?.remainingSeconds ?: 30
    val qrPayload = dynamicQr?.qrPayload

    val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hộp mã QR nền trắng tinh chuẩn độ tương phản cao
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qrPayload != null) {
                DynamicQrMatrixGraphic(qrPayload = qrPayload, modifier = Modifier.fillMaxSize())
            } else {
                Text("Đang chuẩn bị QR từ vé đã đồng bộ", color = Color.DarkGray, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dòng đếm ngược giây tự động đổi mã (hỗ trợ bấm vào để làm mới)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onRefreshQr() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Làm mới mã",
                tint = EmeraldPrimary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("Tự động đổi mã sau: ")
                    withStyle(SpanStyle(color = EmeraldPrimary, fontWeight = FontWeight.Bold)) {
                        append("${remaining}s")
                    }
                },
                fontSize = 13.sp,
                color = TextMediumEmphasis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Huy hiệu bảo mật chống chụp màn hình
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF162330))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Mã tự đổi mỗi 30s • Chống chụp màn hình",
                fontSize = 11.5.sp,
                color = TextHighEmphasis
            )
        }
    }
}

/**
 * Khối QR khi cổng soát vé chưa mở.
 */
@Composable
private fun CompactLockedQr(
    gateOpensAt: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceContainerHigh)
                .border(1.5.dp, AmberTertiary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MÃ QR ĐANG TẠM KHÓA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AmberTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cổng soát vé mở lúc: $gateOpensAt\nMã QR động sẽ tự động kích hoạt trước giờ bắt đầu sự kiện.",
                    fontSize = 11.5.sp,
                    color = TextMediumEmphasis,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Khối QR khi vé đã check-in thành công.
 */
@Composable
private fun CompactCheckedInQr(
    attendeeName: String,
    seatCode: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(EmeraldPrimary.copy(alpha = 0.12f))
                .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(22.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✓", fontSize = 48.sp, fontWeight = FontWeight.Black, color = EmeraldPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ĐÃ CHECK-IN THÀNH CÔNG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ghế: $seatCode",
                    fontSize = 12.sp,
                    color = TextHighEmphasis
                )
                Text(
                    text = "Khách: $attendeeName",
                    fontSize = 10.sp,
                    color = TextMediumEmphasis
                )
            }
        }
    }
}

/**
 * Khối QR khi vé đã bị thu hồi.
 */
@Composable
private fun CompactRevokedQr(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(FieryError.copy(alpha = 0.15f))
                .border(1.5.dp, FieryError, RoundedCornerShape(22.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✕", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = FieryError)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "VÉ ĐÃ BỊ THU HỒI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
private fun DynamicQrMatrixGraphic(qrPayload: String, modifier: Modifier = Modifier) {
    val bitmap = remember(qrPayload) {
        val matrix = QRCodeWriter().encode(
            qrPayload,
            BarcodeFormat.QR_CODE,
            320,
            320,
            mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 2)
        )
        val pixels = IntArray(matrix.width * matrix.height)
        for (row in 0 until matrix.height) {
            for (column in 0 until matrix.width) {
                pixels[row * matrix.width + column] = if (matrix[column, row])
                    android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        android.graphics.Bitmap.createBitmap(matrix.width, matrix.height, android.graphics.Bitmap.Config.ARGB_8888)
            .apply { setPixels(pixels, 0, matrix.width, 0, 0, matrix.width, matrix.height) }
            .asImageBitmap()
    }
    Image(bitmap = bitmap, contentDescription = "Dynamic QR", modifier = modifier)
}
/**
 * MÀN HÌNH ĐỘC LẬP: QR CHECKING VÉ ĐIỆN TỬ (Dynamic QR Pass Full Screen)
 */
@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDisplayScreen(
    viewModel: TicketDisplayViewModel,
    ticketId: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(ticketId) {
        viewModel.handleIntent(TicketDisplayIntent.LoadTicket(ticketId))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianVoid,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianVoid
                ),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = TextHighEmphasis
                            )
                        }
                    }
                },
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
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            TicketQrPopupModalCard(
                ticketId = ticketId,
                ticket = state.ticket,
                onClose = { onBack?.invoke() },
                dynamicQr = state.dynamicQr,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage,
                onRefreshQr = { viewModel.handleIntent(TicketDisplayIntent.RefreshQrRequested) }
            )
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(name = "QR Ticket Pass Card - Active (Theo img.png)", showBackground = true, backgroundColor = 0xFF0C1322)
@Composable
fun TicketQrPopupModalActivePreview() {
    DynamicQRTicketingTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TicketQrPopupModalCard(
                ticketId = "TKT-9901",
                ticket = null,
                onClose = {},
                ticketItem = UserTicketItem(
                    ticketId = "TKT-9901",
                    eventName = "HÀ NỘI ROCK FEST 2026",
                    venue = "Sân Vận Động Mỹ Đình",
                    dateDisplay = "24/10/2026 - 19:30",
                    seatNumber = "A1-042",
                    attendeeName = "Nguyễn Văn An",
                    tierName = "VIP Diamond - Fanzone A",
                    status = UserTicketCheckInStatus.READY_TO_CHECK_IN,
                    gateInfo = "GATE A1"
                ),
                dynamicQr = DynamicQrData(
                    ticketId = "TKT-9901",
                    qrPayload = "TICKETING:TKT-9901:1773513630:c8f92a10e7b4",
                    validUntilEpochSeconds = 1773513630L,
                    totalIntervalSeconds = 30,
                    remainingSeconds = 30
                )
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
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TicketQrPopupModalCard(
                ticketId = "TKT-8802",
                ticket = Ticket(
                    id = "TKT-8802",
                    eventName = "ĐẠI NHẠC HỘI MONSOON EDM",
                    venue = "TT Hội Nghị Quốc Gia",
                    eventTimestamp = 1773513600L,
                    seatNumber = "FANZ-08",
                    ticketHolderName = "Nguyễn Hoàng Long",
                    status = TicketStatus.EXPIRED
                ),
                onClose = {},
                dynamicQr = null
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
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TicketQrPopupModalCard(
                ticketId = "TKT-7703",
                ticket = Ticket(
                    id = "TKT-7703",
                    eventName = "CHUNG KẾT CÚP QUỐC GIA",
                    venue = "SVĐ Hàng Đẫy",
                    eventTimestamp = 1773513600L,
                    seatNumber = "STAND-A-45",
                    ticketHolderName = "Nguyễn Hoàng Long",
                    status = TicketStatus.CHECKED_IN
                ),
                onClose = {},
                dynamicQr = null
            )
        }
    }
}

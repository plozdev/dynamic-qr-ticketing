package com.ticketing.mobile.ui.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticketing.mobile.core_network.auth.AuthManager
import com.ticketing.mobile.core_network.auth.AuthState
import com.ticketing.mobile.ui.theme.AmberTertiary
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.OutlineBorder
import com.ticketing.mobile.ui.theme.SurfaceContainer
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis
import com.ticketing.mobile.ui.theme.TextMuted

/**
 * Màn hình Hồ sơ người dùng (Profile Screen).
 * Hiển thị thông tin định danh người dùng và tùy chọn đăng xuất.
 */
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    authManager: AuthManager = AuthManager.instance
) {
    val authState by authManager.authState.collectAsState()

    val (userId, email, displayName, isDemo) = when (val state = authState) {
        is AuthState.Authenticated -> ProfileData(
            userId = state.userId,
            email = state.email,
            displayName = state.displayName.ifBlank { "Người dùng SecureTix" },
            isDemo = state.isDemo
        )
        else -> ProfileData(
            userId = "Chưa xác thực",
            email = "N/A",
            displayName = "Khách",
            isDemo = false
        )
    }

    ProfileScreenContent(
        userId = userId,
        email = email,
        displayName = displayName,
        isDemo = isDemo,
        onBackClick = onBackClick,
        onLogoutClick = onLogoutClick,
        modifier = modifier
    )
}

private data class ProfileData(
    val userId: String,
    val email: String,
    val displayName: String,
    val isDemo: Boolean
)

/**
 * Stateless UI Content của ProfileScreen phục vụ cho Preview và kiểm thử.
 */
@Composable
fun ProfileScreenContent(
    userId: String,
    email: String,
    displayName: String,
    isDemo: Boolean,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var copiedUserId by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ObsidianVoid
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // --- TOP NAVIGATION BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = TextHighEmphasis
                    )
                }

                Text(
                    text = "Hồ sơ người dùng",
                    color = TextHighEmphasis,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- HEADER AVATAR & TÊN NGƯỜI DÙNG ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    EmeraldPrimary.copy(alpha = 0.25f),
                                    CyanSecondary.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(EmeraldPrimary, CyanSecondary)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHighEmphasis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = TextMediumEmphasis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Trạng thái tài khoản (Badge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDemo) AmberTertiary.copy(alpha = 0.15f) else EmeraldPrimary.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = if (isDemo) AmberTertiary.copy(alpha = 0.4f) else EmeraldPrimary.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isDemo) AmberTertiary else EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDemo) "Tài khoản Demo / Thử nghiệm" else "Tài khoản Đã xác thực",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDemo) AmberTertiary else EmeraldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- THÔNG TIN CHI TIẾT TÀI KHOẢN (CARD) ---
            Text(
                text = "THÔNG TIN ĐỊNH DANH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                border = BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // User ID kèm nút Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = CyanSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "User ID",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = userId,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextHighEmphasis,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(userId))
                                copiedUserId = true
                                Toast.makeText(context, "Đã sao chép User ID", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedUserId) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Sao chép User ID",
                                tint = if (copiedUserId) EmeraldPrimary else TextMediumEmphasis,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineBorder.copy(alpha = 0.4f)
                    )

                    // Email
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = email,
                        iconTint = CyanSecondary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OutlineBorder.copy(alpha = 0.4f)
                    )

                    // Tên hiển thị
                    ProfileInfoRow(
                        icon = Icons.Default.Badge,
                        label = "Tên hiển thị",
                        value = displayName,
                        iconTint = CyanSecondary
                    )

                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- NÚT ĐĂNG XUẤT ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, FieryError.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FieryError.copy(alpha = 0.08f),
                        contentColor = FieryError
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Đăng xuất",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng xuất tài khoản",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- DIALOG XÁC NHẬN ĐĂNG XUẤT ---
        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                containerColor = SurfaceContainerHigh,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = FieryError,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Xác nhận đăng xuất",
                        color = TextHighEmphasis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn đăng xuất khỏi SecureTix không? Dữ liệu phiên và cache vé trên thiết bị sẽ được làm mới.",
                        color = TextMediumEmphasis,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FieryError),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Đăng xuất",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text(
                            text = "Hủy bỏ",
                            color = TextMediumEmphasis
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextHighEmphasis,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(
    name = "Profile Screen Preview",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun ProfileScreenPreview() {
    DynamicQRTicketingTheme {
        ProfileScreenContent(
            userId = "11111111-2222-3333-4444-555555555555",
            email = "hoanglong@dynamic-qr.vn",
            displayName = "Nguyễn Hoàng Long (Demo)",
            isDemo = true,
            onBackClick = {},
            onLogoutClick = {}
        )
    }
}

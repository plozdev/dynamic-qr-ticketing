@file:Suppress("DEPRECATION")

package com.ticketing.mobile.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ticketing.mobile.R
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ticketing.mobile.core_network.auth.AuthManager
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.TextHighEmphasis

/**
 * Màn hình Đăng nhập SecureTix chuẩn theo thiết kế nguyên mẫu (code.html & screen.png).
 * 
 * Bắt buộc người dùng đăng nhập trước khi truy cập ứng dụng để Backend
 * nhận diện định danh Firebase UID qua header Authorization: Bearer <FIREBASE_ID_TOKEN>.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Kiểm tra cấu hình Web Client ID trực tiếp qua stringResource (configuration-aware, không dùng reflection)
    val webClientId = stringResource(id = R.string.default_web_client_id).ifBlank { null }

    // Google Sign-In Activity Result Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            user.getIdToken(true).addOnSuccessListener { tokenResult ->
                                val jwtToken = tokenResult.token ?: ""
                                AuthManager.instance.loginWithFirebase(
                                    userId = user.uid,
                                    email = user.email ?: "${user.uid}@dynamic-qr.vn",
                                    displayName = user.displayName ?: account.displayName ?: "Khán Giả",
                                    idToken = jwtToken
                                )
                                isLoading = false
                                onLoginSuccess()
                            }.addOnFailureListener {
                                isLoading = false
                                errorMessage = "Không thể lấy Firebase ID Token"
                            }
                        } else {
                            isLoading = false
                        }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = "Xác thực Firebase thất bại: ${e.localizedMessage}"
                    }
            } else {
                isLoading = false
                errorMessage = "Không nhận được Google ID Token. Vui lòng kiểm tra Firebase Console!"
            }
        } catch (e: ApiException) {
            isLoading = false
            when (e.statusCode) {
                10 -> errorMessage = "Lỗi Developer (Code 10): Chưa thêm SHA-1 vào Firebase Console! Thêm SHA-1 và tải lại google-services.json."
                12500 -> errorMessage = "Lỗi Sign-In (Code 12500): Đảm bảo máy đã đăng nhập tài khoản Google và SHA-1 hợp lệ."
                12501 -> { /* Người dùng chủ động hủy popup */ }
                else -> errorMessage = "Google Sign-In lỗi (Code: ${e.statusCode}): ${e.localizedMessage}"
            }
        } catch (e: Exception) {
            isLoading = false
            if (result.resultCode != Activity.RESULT_OK) {
                // Chỉ set lỗi nếu không phải do cancel thông thường
                errorMessage = "Đăng nhập Google thất bại: ${e.localizedMessage ?: "Hủy hoặc lỗi kết nối"}"
            }
        }
    }

    val triggerGoogleLogin: () -> Unit = {
        isLoading = true
        errorMessage = null

        if (!webClientId.isNullOrBlank()) {
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(context, gso)
                client.signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(client.signInIntent)
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Khởi chạy Google Sign-In thất bại: ${e.localizedMessage}"
            }
        } else {
            isLoading = false
            errorMessage = "Chưa có Web Client ID trong google-services.json! Vui lòng thêm SHA-1 vào Firebase Console và tải lại google-services.json."
        }
    }

    val triggerGuestLogin: () -> Unit = {
        isLoading = true
        errorMessage = null
        try {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        user.getIdToken(true).addOnSuccessListener { tokenResult ->
                            val token = tokenResult.token ?: ""
                            AuthManager.instance.loginWithFirebase(
                                userId = user.uid,
                                email = "guest-${user.uid.take(6)}@dynamic-qr.vn",
                                displayName = "Khán Giả (Firebase)",
                                idToken = token
                            )
                            isLoading = false
                            onLoginSuccess()
                        }.addOnFailureListener {
                            AuthManager.instance.loginWithDemoUser()
                            isLoading = false
                            onLoginSuccess()
                        }
                    } else {
                        AuthManager.instance.loginWithDemoUser()
                        isLoading = false
                        onLoginSuccess()
                    }
                }
                .addOnFailureListener {
                    // Fallback to offline demo user when Firebase Anonymous sign-in is disabled in console
                    AuthManager.instance.loginWithDemoUser()
                    isLoading = false
                    onLoginSuccess()
                }
        } catch (e: Exception) {
            AuthManager.instance.loginWithDemoUser()
            isLoading = false
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        onGoogleLoginClick = triggerGoogleLogin,
        onGuestLoginClick = triggerGuestLogin,
        modifier = modifier
    )
}

/**
 * Giao diện hiển thị (Stateless Composable) hỗ trợ Preview và Test UI độc lập.
 */
@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleLoginClick: () -> Unit,
    onGuestLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ObsidianVoid
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Khoảng đệm co giãn phía trên đẩy cụm logo vào vị trí 1/3 trên màn hình
            Spacer(modifier = Modifier.weight(0.35f))

            // --- KHỐI NHẬN DIỆN THƯƠNG HIỆU (LOGO + BRAND NAME + SLOGAN) ---
            // Biểu tượng Logo Thẻ Obsidian với hiệu ứng vầng sáng hào quang xanh
            Box(
                modifier = Modifier.size(170.dp),
                contentAlignment = Alignment.Center
            ) {
                // Vầng sáng hào quang xanh Emerald
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    EmeraldPrimary.copy(alpha = 0.25f),
                                    EmeraldPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Card Biểu tượng trung tâm
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceContainerHigh)
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    EmeraldPrimary.copy(alpha = 0.5f),
                                    CyanSecondary.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "SecureTix Logo",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tiêu đề thương hiệu
            Text(
                text = "SecureTix",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextHighEmphasis,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slogan / Tiêu đề phụ - Tăng tương phản (#CBD5E1) đạt chuẩn rõ nét trên nền tối
            Text(
                text = "Nền tảng vé sự kiện bảo mật thế hệ mới",
                fontSize = 15.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Nút Đăng nhập Google theo chuẩn Google Identity Guidelines - đặt ngay dưới tiêu đề
            // Nền trắng #FFFFFF, viền xám nhẹ #DADCE0, chữ đen xám #1F1F1F
            Button(
                onClick = {
                    if (!isLoading) {
                        onGoogleLoginClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F)
                ),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 1.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF1F1F1F),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GoogleBrandIcon(modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Tiếp tục với Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nút đăng nhập thử nghiệm / Demo (cho phép kiểm thử app nhanh khi chưa cấu hình SHA-1)
            TextButton(
                onClick = {
                    if (!isLoading) {
                        onGuestLoginClick()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Dùng tài khoản Demo / Khách (1-Chạm)",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            // Thông báo lỗi xuất hiện ngay dưới nút đăng nhập nếu có lỗi
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                errorMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FieryError.copy(alpha = 0.15f))
                            .border(1.dp, FieryError.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = FieryError,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Khoảng trống co giãn đẩy phần chân trang xuống đáy màn hình
            Spacer(modifier = Modifier.weight(1f))

            // --- CHÂN TRANG ĐIỀU KHOẢN & PHÁP LÝ ---
            // Tăng độ sáng chữ (#94A3B8) đảm bảo chuẩn tiếp cận WCAG AA
            Text(
                text = buildAnnotatedString {
                    append("Bằng việc tiếp tục, bạn đồng ý với ")
                    withStyle(style = SpanStyle(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)) {
                        append("Điều khoản & Chính sách")
                    }
                    append(" của SecureTix.")
                },
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Vẽ Logo 4 màu chuẩn thương hiệu Google (Blue, Red, Yellow, Green).
 */
@Composable
fun GoogleBrandIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = minOf(w, h) / 2f
        val strokeW = radius * 0.42f

        // Vòng cung ngoài Google 4 màu
        // Đỏ (Top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 200f,
            sweepAngle = 135f,
            useCenter = false,
            topLeft = Offset(cx - radius + strokeW / 2, cy - radius + strokeW / 2),
            size = Size((radius - strokeW / 2) * 2, (radius - strokeW / 2) * 2),
            style = Stroke(width = strokeW)
        )
        // Vàng (Left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 125f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = Offset(cx - radius + strokeW / 2, cy - radius + strokeW / 2),
            size = Size((radius - strokeW / 2) * 2, (radius - strokeW / 2) * 2),
            style = Stroke(width = strokeW)
        )
        // Xanh lá (Bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 35f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - radius + strokeW / 2, cy - radius + strokeW / 2),
            size = Size((radius - strokeW / 2) * 2, (radius - strokeW / 2) * 2),
            style = Stroke(width = strokeW)
        )
        // Xanh dương (Right + Center bar)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -35f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = Offset(cx - radius + strokeW / 2, cy - radius + strokeW / 2),
            size = Size((radius - strokeW / 2) * 2, (radius - strokeW / 2) * 2),
            style = Stroke(width = strokeW)
        )

        // Thanh ngang màu xanh dương ở giữa chữ G
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx - strokeW * 0.2f, cy - strokeW / 2),
            size = Size(radius - strokeW * 0.1f, strokeW)
        )
    }
}

// ==========================================
// COMPOSE PREVIEWS
// ==========================================

@Preview(
    name = "1. SecureTix Login Screen - Default",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun LoginScreenPreview() {
    DynamicQRTicketingTheme {
        LoginScreenContent(
            isLoading = false,
            errorMessage = null,
            onGoogleLoginClick = {},
            onGuestLoginClick = {}
        )
    }
}

@Preview(
    name = "2. SecureTix Login Screen - Loading State",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun LoginScreenLoadingPreview() {
    DynamicQRTicketingTheme {
        LoginScreenContent(
            isLoading = true,
            errorMessage = null,
            onGoogleLoginClick = {},
            onGuestLoginClick = {}
        )
    }
}

@Preview(
    name = "3. SecureTix Login Screen - Error Banner",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun LoginScreenErrorPreview() {
    DynamicQRTicketingTheme {
        LoginScreenContent(
            isLoading = false,
            errorMessage = "Xác thực Firebase thất bại: Mạng không ổn định hoặc lỗi kết nối.",
            onGoogleLoginClick = {},
            onGuestLoginClick = {}
        )
    }
}

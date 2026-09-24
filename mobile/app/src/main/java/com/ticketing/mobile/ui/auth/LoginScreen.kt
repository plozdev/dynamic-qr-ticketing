package com.ticketing.mobile.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ticketing.mobile.R
import com.ticketing.mobile.core_network.auth.AuthManager
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.model.ApiError
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.EmeraldPrimaryFixed
import com.ticketing.mobile.ui.theme.FieryError
import com.ticketing.mobile.ui.theme.ObsidianVoid
import com.ticketing.mobile.ui.theme.OutlineBorder
import com.ticketing.mobile.ui.theme.SurfaceContainer
import com.ticketing.mobile.ui.theme.SurfaceContainerHigh
import com.ticketing.mobile.ui.theme.SurfaceContainerHighest
import com.ticketing.mobile.ui.theme.TextHighEmphasis
import com.ticketing.mobile.ui.theme.TextMediumEmphasis
import com.ticketing.mobile.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

data class AuthForm(
    val isSignup: Boolean,
    val username: String,
    val password: String,
    val displayName: String = "",
    val email: String = ""
)

private data class AuthResponse(
    val userId: String,
    val username: String,
    val email: String,
    val displayName: String,
    val token: String
)

/**
 * Màn hình Đăng nhập & Đăng ký SecureTix với giao diện Obsidian Neon cao cấp.
 * Tích hợp hiệu ứng chuyển cảnh thành công mượt mà (Success Celebration Animation),
 * đưa logo vào trung tâm cùng huy hiệu xác thực trước khi chuyển vào kho vé.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { OkHttpApiClient() }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var successAccountName by remember { mutableStateOf("") }
    var isSignupSuccess by remember { mutableStateOf(false) }

    LoginScreenContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        isSuccess = isSuccess,
        successAccountName = successAccountName,
        isSignupSuccess = isSignupSuccess,
        onModeChanged = { errorMessage = null },
        onSubmit = { form ->
            if (!isLoading && !isSuccess) {
                isLoading = true
                errorMessage = null
                scope.launch {
                    val body = JSONObject()
                        .put("username", form.username.trim())
                        .put("password", form.password)
                    if (form.isSignup) {
                        body.put("displayName", form.displayName.trim())
                        body.put("email", form.email.trim())
                    }
                    val endpoint = if (form.isSignup) "/auth/signup" else "/auth/login"
                    val result = apiClient.post(endpoint, body.toString()) { raw ->
                        val json = JSONObject(raw)
                        AuthResponse(
                            userId = json.getString("userId"),
                            username = json.getString("username"),
                            email = json.getString("email"),
                            displayName = json.getString("displayName"),
                            token = json.getString("token")
                        )
                    }
                    isLoading = false
                    when (result) {
                        is NetworkResult.Success -> {
                            val account = result.data
                            if (account.userId.isBlank() || account.token.isBlank()) {
                                errorMessage = "Máy chủ trả về phiên đăng nhập không hợp lệ."
                            } else {
                                AuthManager.instance.login(account.userId, account.email, account.displayName, account.token)
                                successAccountName = account.displayName.ifBlank { account.username }
                                isSignupSuccess = form.isSignup
                                isSuccess = true
                                // Hoạt ảnh thành công chạy trong 1.25s để tạo cảm giác chuyển tiếp mượt mà
                                delay(1250.milliseconds)
                                onLoginSuccess()
                            }
                        }
                        is NetworkResult.Error -> {
                            AuthManager.instance.logout()
                            errorMessage = when (val error = result.error) {
                                is ApiError.Unauthorized -> "Tên đăng nhập hoặc mật khẩu không chính xác."
                                is ApiError.NetworkConnection -> "Không thể kết nối đến máy chủ Backend. Hãy kiểm tra kết nối mạng."
                                is ApiError.HttpError -> {
                                    val serverMessage = runCatching {
                                        JSONObject(error.rawBody ?: "").optString("message")
                                    }.getOrNull()
                                    serverMessage?.takeIf { it.isNotBlank() } ?: "Máy chủ phản hồi HTTP ${error.code}."
                                }
                                else -> "Lỗi phản hồi: ${result.error.messageText}"
                            }
                        }
                        NetworkResult.Loading -> Unit
                    }
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Giao diện chính tinh chỉnh (Refined UI):
 * - Hiệu ứng hào quang neon Emerald/Cyan nhịp thở quanh logo.
 * - Thanh chuyển đổi Tab (Đăng nhập / Đăng ký) dạng viên thuốc (Segmented Pill) mượt mà.
 * - Khung Card kính mờ (Glassmorphism Card) với viền gradient công nghệ cao.
 * - Khi đăng nhập/đăng ký thành công: form thu nhỏ mờ dần, logo phóng lớn giữa màn hình
 *   kèm huy hiệu Checkmark và thông điệp chào mừng người dùng.
 */
@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: (AuthForm) -> Unit,
    onModeChanged: () -> Unit = {},
    isSuccess: Boolean = false,
    successAccountName: String = "",
    isSignupSuccess: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isSignup by rememberSaveable { mutableStateOf(false) }
    var username by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Hiệu ứng nhịp thở cho vầng sáng Logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Hiệu ứng mờ dần và trượt xuống của form khi thành công
    val formAlpha by animateFloatAsState(
        targetValue = if (isSuccess) 0f else 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "formAlpha"
    )
    val formOffsetY by animateDpAsState(
        targetValue = if (isSuccess) 60.dp else 0.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "formOffsetY"
    )

    fun submit() {
        val cleanUsername = username.trim()
        validationError = when {
            cleanUsername.isEmpty() -> "Vui lòng nhập tên đăng nhập."
            !Regex("[A-Za-z0-9._-]{3,32}").matches(cleanUsername) ->
                "Tên đăng nhập cần 3–32 ký tự (chữ, số, '.', '_', '-')."
            isSignup && displayName.trim().isEmpty() -> "Vui lòng nhập họ và tên của bạn."
            isSignup && displayName.trim().length > 255 -> "Họ tên không được vượt quá 255 ký tự."
            isSignup && (email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) ->
                "Vui lòng nhập địa chỉ email hợp lệ."
            isSignup && email.trim().length > 128 -> "Email không được vượt quá 128 ký tự."
            password.isEmpty() -> "Vui lòng nhập mật khẩu."
            password.length < 8 -> "Mật khẩu bảo mật phải có ít nhất 8 ký tự."
            password.length > 72 -> "Mật khẩu chỉ được tối đa 72 ký tự."
            isSignup && password != confirmPassword -> "Mật khẩu xác nhận không khớp nhau."
            else -> null
        }
        if (validationError == null) {
            onSubmit(AuthForm(isSignup, cleanUsername, password, displayName, email))
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ObsidianVoid
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // ==========================================
            // 1. GIAO DIỆN FORM ĐĂNG NHẬP / ĐĂNG KÝ
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .alpha(formAlpha)
                    .offset(y = formOffsetY)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(28.dp))

                // --- LOGO & VẦNG SÁNG NEON ---
                Box(
                    modifier = Modifier.size(136.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Vầng hào quang Breathing Radial
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = glowAlpha * 0.45f),
                                        CyanSecondary.copy(alpha = glowAlpha * 0.18f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Khung Logo trung tâm
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(SurfaceContainerHigh)
                            .border(
                                width = 1.4.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = 0.8f),
                                        CyanSecondary.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_logo),
                            contentDescription = "SecureTix Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Tiêu đề & Subtitle
                Text(
                    text = "SecureTix",
                    color = TextHighEmphasis,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = if (isSignup) "Tạo tài khoản để sở hữu vé và quét mã bảo mật" else "Chào mừng trở lại! Vui lòng đăng nhập",
                    color = TextMediumEmphasis,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // --- TAB SWITCHER (ĐĂNG NHẬP / ĐĂNG KÝ) ---
                AuthModeTabSwitcher(
                    isSignup = isSignup,
                    onTabSelect = { signupMode ->
                        if (isSignup != signupMode && !isLoading) {
                            isSignup = signupMode
                            validationError = null
                            onModeChanged()
                            password = ""
                            confirmPassword = ""
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // --- CARD KHUNG NHẬP LIỆU CHÍNH (GLASSMORPHIC CARD) ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh.copy(alpha = 0.75f)),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.35f),
                                OutlineBorder.copy(alpha = 0.35f),
                                CyanSecondary.copy(alpha = 0.20f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Họ và tên (chỉ khi Đăng ký)
                        AnimatedVisibility(
                            visible = isSignup,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                ThemedInputField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    label = "Họ và tên",
                                    placeholder = "Nguyễn Văn A",
                                    leadingIcon = Icons.Default.Badge,
                                    enabled = !isLoading,
                                    keyboardType = KeyboardType.Text
                                )
                                Spacer(Modifier.height(14.dp))
                            }
                        }

                        // Tên đăng nhập (Username)
                        ThemedInputField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Tên đăng nhập",
                            placeholder = "ví dụ: hoanglong26",
                            leadingIcon = Icons.Default.AccountCircle,
                            enabled = !isLoading,
                            keyboardType = KeyboardType.Ascii
                        )

                        // Email (chỉ khi Đăng ký)
                        AnimatedVisibility(
                            visible = isSignup,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                ThemedInputField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = "Địa chỉ Email",
                                    placeholder = "ten@domain.com",
                                    leadingIcon = Icons.Default.Email,
                                    enabled = !isLoading,
                                    keyboardType = KeyboardType.Email
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Mật khẩu
                        ThemedInputField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Mật khẩu",
                            placeholder = "Tối thiểu 8 ký tự",
                            leadingIcon = Icons.Default.Lock,
                            enabled = !isLoading,
                            isPassword = true,
                            passwordVisible = showPassword,
                            onTogglePasswordVisibility = { showPassword = !showPassword },
                            keyboardType = KeyboardType.Password
                        )

                        // Xác nhận Mật khẩu (chỉ khi Đăng ký)
                        AnimatedVisibility(
                            visible = isSignup,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                ThemedInputField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = "Xác nhận mật khẩu",
                                    placeholder = "Nhập lại mật khẩu",
                                    leadingIcon = Icons.Default.LockReset,
                                    enabled = !isLoading,
                                    isPassword = true,
                                    passwordVisible = showConfirmPassword,
                                    onTogglePasswordVisibility = { showConfirmPassword = !showConfirmPassword },
                                    keyboardType = KeyboardType.Password
                                )
                            }
                        }

                        // BANNER THÔNG BÁO LỖI
                        val activeError = validationError ?: errorMessage
                        AnimatedVisibility(
                            visible = activeError != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            activeError?.let { msg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(FieryError.copy(alpha = 0.12f))
                                        .border(1.dp, FieryError.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = FieryError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        color = FieryError,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // NÚT SUBMIT (GRADIENT HOẶC EMERALD GLOW)
                        Button(
                            onClick = ::submit,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = ObsidianVoid
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = ObsidianVoid,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isSignup) "Tạo tài khoản ngay" else "Đăng nhập hệ thống",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianVoid
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = ObsidianVoid,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Footer chuyển đổi nhanh
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignup) "Đã có tài khoản SecureTix?" else "Chưa có tài khoản?",
                        color = TextMediumEmphasis,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = {
                            if (!isLoading) {
                                isSignup = !isSignup
                                validationError = null
                                onModeChanged()
                                password = ""
                                confirmPassword = ""
                            }
                        }
                    ) {
                        Text(
                            text = if (isSignup) "Đăng nhập ngay" else "Đăng ký tài khoản",
                            color = EmeraldPrimaryFixed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Điều khoản bảo mật
                Text(
                    text = buildAnnotatedString {
                        append("Bằng việc tiếp tục, bạn đồng ý với ")
                        withStyle(style = SpanStyle(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)) {
                            append("Chính sách & Bảo mật")
                        }
                        append(" SecureTix.")
                    },
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 16.dp)
                )
            }

            // ==========================================
            // 2. HOẠT ẢNH THÀNH CÔNG (SUCCESS HERO OVERLAY)
            // ==========================================
            AnimatedVisibility(
                visible = isSuccess,
                enter = fadeIn(tween(400)) + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(tween(250))
            ) {
                SuccessCelebrationView(
                    isSignupSuccess = isSignupSuccess,
                    accountName = successAccountName
                )
            }
        }
    }
}

/**
 * Màn hình Hero chúc mừng đăng nhập/đăng ký thành công:
 * Hiển thị Logo trung tâm phóng lớn, vầng sáng neon Emerald nở rộng,
 * huy hiệu dấu tick Checkmark và lời chào mừng người dùng.
 */
@Composable
private fun SuccessCelebrationView(
    isSignupSuccess: Boolean,
    accountName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo trung tâm với hiệu ứng Checkmark Badge
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Vầng sáng Emerald phát tán rực rỡ
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(haloPulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.55f),
                                CyanSecondary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Logo Box
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(SurfaceContainerHigh)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(EmeraldPrimary, CyanSecondary)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Success Logo",
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(26.dp))
                )
            }

            // Huy hiệu Checkmark nổi bật ở góc dưới
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary)
                    .border(2.5.dp, ObsidianVoid, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Thành công",
                    tint = ObsidianVoid,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Tiêu đề thành công
        Text(
            text = if (isSignupSuccess) "Đăng ký thành công!" else "Xác thực thành công!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextHighEmphasis,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Tên tài khoản chào mừng
        if (accountName.isNotBlank()) {
            Text(
                text = "Xin chào, $accountName",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimaryFixed,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
        }

        Text(
            text = "Đang chuyển đến kho vé bảo mật của bạn...",
            fontSize = 13.sp,
            color = TextMediumEmphasis,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Thanh xoay chờ tinh tế
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            color = EmeraldPrimary,
            strokeWidth = 2.5.dp
        )
    }
}

/**
 * Thanh chuyển đổi chế độ Xác thực dạng Segmented Pill bo tròn.
 */
@Composable
private fun AuthModeTabSwitcher(
    isSignup: Boolean,
    onTabSelect: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHighest)
            .border(1.dp, OutlineBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Tab Đăng nhập
            val loginBgColor by animateColorAsState(
                targetValue = if (!isSignup) EmeraldPrimary else Color.Transparent,
                label = "loginTabBg"
            )
            val loginTextColor by animateColorAsState(
                targetValue = if (!isSignup) ObsidianVoid else TextMediumEmphasis,
                label = "loginTabText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(loginBgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelect(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = loginTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Đăng nhập",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = loginTextColor
                    )
                }
            }

            // Tab Đăng ký
            val signupBgColor by animateColorAsState(
                targetValue = if (isSignup) EmeraldPrimary else Color.Transparent,
                label = "signupTabBg"
            )
            val signupTextColor by animateColorAsState(
                targetValue = if (isSignup) ObsidianVoid else TextMediumEmphasis,
                label = "signupTabText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(signupBgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelect(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = signupTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Đăng ký",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = signupTextColor
                    )
                }
            }
        }
    }
}

/**
 * Ô nhập liệu phong cách Cyber Obsidian với viền bo tròn 16dp và màu sắc ánh sáng đồng bộ.
 */
@Composable
private fun ThemedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
        singleLine = true,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = CyanSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
            {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        tint = TextMediumEmphasis,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceContainerHigh,
            unfocusedContainerColor = SurfaceContainer,
            focusedBorderColor = EmeraldPrimary,
            unfocusedBorderColor = OutlineBorder.copy(alpha = 0.6f),
            focusedLabelColor = EmeraldPrimary,
            unfocusedLabelColor = TextMuted,
            focusedLeadingIconColor = EmeraldPrimary,
            unfocusedLeadingIconColor = CyanSecondary.copy(alpha = 0.8f),
            cursorColor = EmeraldPrimary,
            focusedTextColor = TextHighEmphasis,
            unfocusedTextColor = TextHighEmphasis
        )
    )
}

@Preview(name = "1. Login Screen - Form View", showBackground = true)
@Composable
fun LoginScreenSignInPreview() {
    DynamicQRTicketingTheme {
        LoginScreenContent(
            isLoading = false,
            errorMessage = null,
            onSubmit = {}
        )
    }
}

@Preview(name = "2. Login Screen - Success Celebration Animation", showBackground = true)
@Composable
fun LoginScreenSuccessPreview() {
    DynamicQRTicketingTheme {
        LoginScreenContent(
            isLoading = false,
            errorMessage = null,
            isSuccess = true,
            successAccountName = "Nguyễn Hoàng Long",
            onSubmit = {}
        )
    }
}

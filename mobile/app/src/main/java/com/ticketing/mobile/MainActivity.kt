package com.ticketing.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.ticketing.mobile.core_network.auth.AuthState
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ticketing.mobile.core_crypto.data.NativeCryptoEngineImpl
import com.ticketing.mobile.core_network.auth.AuthManager
import com.ticketing.mobile.gate_scanner.data.datasource.DefaultGateRemoteDataSource
import com.ticketing.mobile.gate_scanner.data.repository.GateValidationRepositoryImpl
import com.ticketing.mobile.gate_scanner.domain.usecase.ValidateScannedTicketUseCase
import com.ticketing.mobile.gate_scanner.presentation.GateScannerViewModel
import com.ticketing.mobile.gate_scanner.presentation.ui.GateScannerScreen
import com.ticketing.mobile.ticket_display.data.datasource.DefaultTicketRemoteDataSource
import com.ticketing.mobile.ticket_display.data.datasource.PersistentTicketLocalDataSource
import com.ticketing.mobile.ticket_display.data.repository.TicketRepositoryImpl
import com.ticketing.mobile.ticket_display.domain.usecase.ClaimTicketUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GenerateDynamicQrUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetEventsUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetMyTicketsUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetTicketDetailUseCase
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.ui.MyTicketsListScreen
import com.ticketing.mobile.ticket_display.presentation.ui.TicketDetailScreen
import com.ticketing.mobile.ticket_display.presentation.ui.MyTicketsContent
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ui.auth.LoginScreen
import com.ticketing.mobile.ui.auth.LoginScreenContent
import com.ticketing.mobile.ui.profile.ProfileScreen
import com.ticketing.mobile.ui.profile.ProfileScreenContent
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.ObsidianVoid
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState

enum class AppScreen {
    LOGIN,
    MY_TICKETS,
    TICKET_DETAILS,
    SCANNER,
    PROFILE
}

/**
 * Entry Activity của ứng dụng Dynamic QR Ticketing.
 */
class MainActivity : ComponentActivity() {

    private var ticketDisplayViewModelRef: TicketDisplayViewModel? = null

    override fun onResume() {
        super.onResume()
        // Khi mở lại app hoặc resume, tự động tải lại trạng thái vé để không bỏ lỡ lần quét nào
        ticketDisplayViewModelRef?.loadMyTickets()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Phục hồi phiên đăng nhập bền vững
        AuthManager.instance.init(applicationContext)

        // Phục hồi Server URL tùy chỉnh nếu người dùng đã cấu hình trước đó
        val prefs = getSharedPreferences("secure_tix_prefs", MODE_PRIVATE)
        val savedServerUrl = prefs.getString("custom_server_url", null)
        if (!savedServerUrl.isNullOrBlank()) {
            OkHttpApiClient.customBaseUrl = savedServerUrl
        }

        // --- DEPENDENCY INJECTION GRAPH ---
        val cryptoEngine = NativeCryptoEngineImpl()

        // Persistent SQLite Offline Cache & KeyStore Secure Storage
        val ticketLocalDataSource = PersistentTicketLocalDataSource(this)
        val ticketRemoteDataSource = DefaultTicketRemoteDataSource()
        val ticketRepository = TicketRepositoryImpl(
            remoteDataSource = ticketRemoteDataSource,
            localDataSource = ticketLocalDataSource,
            cryptoEngine = cryptoEngine
        )

        val getTicketDetailUseCase = GetTicketDetailUseCase(ticketRepository)
        val generateDynamicQrUseCase = GenerateDynamicQrUseCase(ticketRepository)
        val getMyTicketsUseCase = GetMyTicketsUseCase(ticketRepository)
        val claimTicketUseCase = ClaimTicketUseCase(ticketRepository)
        val getEventsUseCase = GetEventsUseCase(ticketRepository)

        // Gate Scanner feature dependencies
        val gateRemoteDataSource = DefaultGateRemoteDataSource()
        val gateValidationRepository = GateValidationRepositoryImpl(
            remoteDataSource = gateRemoteDataSource,
            cryptoEngine = cryptoEngine,
            isOfflineEnabled = false
        )
        val validateScannedTicketUseCase = ValidateScannedTicketUseCase(gateValidationRepository)

        setContent {
            DynamicQRTicketingTheme {
                val ticketSseClient = remember { com.ticketing.mobile.core_network.sse.DefaultTicketSseClient() }
                val ticketDisplayViewModel = remember {
                    TicketDisplayViewModel(
                        getTicketDetailUseCase = getTicketDetailUseCase,
                        generateDynamicQrUseCase = generateDynamicQrUseCase,
                        getMyTicketsUseCase = getMyTicketsUseCase,
                        claimTicketUseCase = claimTicketUseCase,
                        getEventsUseCase = getEventsUseCase,
                        ticketSseClient = ticketSseClient
                    ).also { ticketDisplayViewModelRef = it }
                }

                val gateScannerViewModel = remember {
                    GateScannerViewModel(
                        validateScannedTicketUseCase = validateScannedTicketUseCase
                    )
                }

                val initialScreen = if (AuthManager.instance.authState.collectAsState().value is AuthState.Authenticated) {
                    AppScreen.MY_TICKETS
                } else {
                    AppScreen.LOGIN
                }

                var currentScreen by remember {
                    mutableStateOf(initialScreen)
                }
                var activeTicketId by remember { mutableStateOf<String?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianVoid
                ) {
                    when (currentScreen) {
                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    val uid = AuthManager.instance.getCurrentUserId()
                                    ticketDisplayViewModel.startObservingTicketUpdates(uid)
                                    ticketDisplayViewModel.loadMyTickets(uid)
                                    currentScreen = AppScreen.MY_TICKETS
                                }
                            )
                        }

                        AppScreen.MY_TICKETS -> {
                            MyTicketsListScreen(
                                ticketDisplayViewModel = ticketDisplayViewModel,
                                onTicketDetails = { ticket ->
                                    activeTicketId = ticket.ticketId
                                    currentScreen = AppScreen.TICKET_DETAILS
                                },
                                onProfileClick = {
                                    currentScreen = AppScreen.PROFILE
                                }
                            )
                        }

                        AppScreen.PROFILE -> {
                            BackHandler {
                                currentScreen = AppScreen.MY_TICKETS
                            }
                            ProfileScreen(
                                onBackClick = {
                                    currentScreen = AppScreen.MY_TICKETS
                                },
                                onLogoutClick = {
                                    val token = AuthManager.instance.getBearerToken()
                                    AuthManager.instance.logout()
                                    lifecycleScope.launch {
                                        try {
                                            ticketLocalDataSource.clearCache()
                                        } finally {
                                            currentScreen = AppScreen.LOGIN
                                        }
                                        if (!token.isNullOrBlank()) {
                                            OkHttpApiClient().post("/auth/logout", "{}", mapOf("Authorization" to "Bearer $token")) { Unit }
                                        }
                                    }
                                }
                            )
                        }

                        AppScreen.TICKET_DETAILS -> {
                            BackHandler {
                                currentScreen = AppScreen.MY_TICKETS
                            }
                            activeTicketId?.let { ticketId ->
                                TicketDetailScreen(
                                    viewModel = ticketDisplayViewModel,
                                    ticketId = ticketId,
                                    onBack = { currentScreen = AppScreen.MY_TICKETS }
                                )
                            }
                        }

                        AppScreen.SCANNER -> {
                            BackHandler {
                                currentScreen = AppScreen.MY_TICKETS
                            }
                            GateScannerScreen(
                                viewModel = gateScannerViewModel,
                                onBackClick = {
                                    currentScreen = AppScreen.MY_TICKETS
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPOSE PREVIEWS CHO MAINACTIVITY
// ==========================================

@Preview(
    name = "1. MainActivity - Giao diện Đăng nhập",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun MainActivityLoginPreview() {
    DynamicQRTicketingTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ObsidianVoid
        ) {
            LoginScreenContent(
                isLoading = false,
                errorMessage = null,
                onSubmit = {}
            )
        }
    }
}

@Preview(
    name = "2. MainActivity - Màn hình Vé Của Tôi (Đã Đăng Nhập)",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun MainActivityMyTicketsPreview() {
    DynamicQRTicketingTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ObsidianVoid
        ) {
            MyTicketsContent(
                selectedTabIndex = 0,
                onTabSelected = {},
                tickets = listOf(
                    UserTicketItem(
                        ticketId = "a1111111-0000-0000-0000-000000000001",
                        eventName = "Hà Nội Rock Fest 2026",
                        venue = "SVĐ Mỹ Đình, Hà Nội",
                        dateDisplay = "24/10/2026 • 19:30",
                        seatNumber = "GA-VIP-01",
                        attendeeName = "Khán Giả SecureTix",
                        tierName = "VIP Standing",
                        status = UserTicketCheckInStatus.READY_TO_CHECK_IN,
                        gateInfo = "CỔNG 02",
                        checkInNote = "Sẵn sàng quét mã",
                        checkInOpensAtEpochSeconds = 0,
                        isCheckInOpen = true
                    )
                ),
                onTicketClick = {}
            )
        }
    }
}

@Preview(
    name = "3. MainActivity - Màn hình Hồ Sơ Người Dùng (Profile)",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun MainActivityProfilePreview() {
    DynamicQRTicketingTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ObsidianVoid
        ) {
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
}

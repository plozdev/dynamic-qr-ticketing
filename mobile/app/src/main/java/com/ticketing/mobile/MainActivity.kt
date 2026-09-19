package com.ticketing.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.ticketing.mobile.ticket_display.presentation.ui.TicketDisplayScreen
import com.ticketing.mobile.ui.auth.LoginScreen
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.ObsidianVoid

enum class AppScreen {
    LOGIN,
    MY_TICKETS,
    TICKET_DISPLAY,
    SCANNER
}

/**
 * Entry Activity của ứng dụng Dynamic QR Ticketing.
 * Hỗ trợ chu trình hoàn chỉnh:
 * 1. Đăng nhập (LoginScreen) với Firebase Auth & Quick Demo Account.
 * 2. Màn hình Vé Của Tôi (MyTicketsListScreen) lưu trữ SQLite ngoại tuyến bền vững.
 * 3. Khám phá và Nhận vé 1-Chạm (1-Click Claim Ticket).
 * 4. Hiển thị Dynamic QR xoay vòng 30s liên tục (C++ NDK TOTP).
 * 5. Màn hình Soát vé Cổng (GateScannerScreen).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                val ticketDisplayViewModel = remember {
                    TicketDisplayViewModel(
                        getTicketDetailUseCase = getTicketDetailUseCase,
                        generateDynamicQrUseCase = generateDynamicQrUseCase,
                        getMyTicketsUseCase = getMyTicketsUseCase,
                        claimTicketUseCase = claimTicketUseCase,
                        getEventsUseCase = getEventsUseCase
                    )
                }

                val gateScannerViewModel = remember {
                    GateScannerViewModel(
                        validateScannedTicketUseCase = validateScannedTicketUseCase
                    )
                }

                var currentScreen by remember { mutableStateOf(AppScreen.MY_TICKETS) }
                var activeTicketId by remember { mutableStateOf<String?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianVoid
                ) {
                    when (currentScreen) {
                        AppScreen.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    currentScreen = AppScreen.MY_TICKETS
                                    ticketDisplayViewModel.loadMyTickets(AuthManager.instance.getCurrentUserId())
                                }
                            )
                        }

                        AppScreen.MY_TICKETS -> {
                            MyTicketsListScreen(
                                ticketDisplayViewModel = ticketDisplayViewModel,
                                onTicketSelected = { ticket ->
                                    activeTicketId = ticket.ticketId
                                    currentScreen = AppScreen.TICKET_DISPLAY
                                },
                                onScannerClick = {
                                    currentScreen = AppScreen.SCANNER
                                },
                                onLogoutClick = {
                                    AuthManager.instance.logout()
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }

                        AppScreen.TICKET_DISPLAY -> {
                            BackHandler {
                                currentScreen = AppScreen.MY_TICKETS
                            }
                            TicketDisplayScreen(
                                viewModel = ticketDisplayViewModel,
                                ticketId = activeTicketId ?: "a1111111-0000-0000-0000-000000000001",
                                onBack = {
                                    currentScreen = AppScreen.MY_TICKETS
                                }
                            )
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
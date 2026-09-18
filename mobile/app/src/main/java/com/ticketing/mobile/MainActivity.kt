package com.ticketing.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ticketing.mobile.core_crypto.data.NativeCryptoEngineImpl
import com.ticketing.mobile.events.presentation.ui.EventListScreen
import com.ticketing.mobile.gate_scanner.data.datasource.DefaultGateRemoteDataSource
import com.ticketing.mobile.gate_scanner.data.repository.GateValidationRepositoryImpl
import com.ticketing.mobile.gate_scanner.domain.usecase.ValidateScannedTicketUseCase
import com.ticketing.mobile.gate_scanner.presentation.GateScannerViewModel
import com.ticketing.mobile.gate_scanner.presentation.ui.GateScannerScreen
import com.ticketing.mobile.ticket_display.data.datasource.DefaultTicketRemoteDataSource
import com.ticketing.mobile.ticket_display.data.datasource.InMemoryTicketLocalDataSource
import com.ticketing.mobile.ticket_display.data.repository.TicketRepositoryImpl
import com.ticketing.mobile.ticket_display.domain.usecase.GenerateDynamicQrUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetTicketDetailUseCase
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.ui.MyTicketsListScreen
import com.ticketing.mobile.ui.theme.CyanSecondary
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.EmeraldPrimary
import com.ticketing.mobile.ui.theme.SurfaceContainer
import com.ticketing.mobile.ui.theme.TextMediumEmphasis

/**
 * Entry Activity chính của ứng dụng Dynamic QR Ticketing.
 * Điều hướng giữa 3 nhóm màn hình chính:
 * 1. Các Sự Kiện Hiện Tại (EventListScreen)
 * 2. Vé Của Tôi (MyTicketsListScreen) -> Chi tiết QR Checking (TicketDisplayScreen)
 * 3. Soát Vé Cổng (GateScannerScreen) với kết quả Thành công và Thất bại.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- MANUAL DEPENDENCY INJECTION GRAPH ---
        val cryptoEngine = NativeCryptoEngineImpl()

        // Ticket Display feature dependencies
        val ticketLocalDataSource = InMemoryTicketLocalDataSource()
        val ticketRemoteDataSource = DefaultTicketRemoteDataSource()
        val ticketRepository = TicketRepositoryImpl(
            remoteDataSource = ticketRemoteDataSource,
            localDataSource = ticketLocalDataSource,
            cryptoEngine = cryptoEngine
        )
        val getTicketDetailUseCase = GetTicketDetailUseCase(ticketRepository)
        val generateDynamicQrUseCase = GenerateDynamicQrUseCase(ticketRepository)

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
                    TicketDisplayViewModel(getTicketDetailUseCase, generateDynamicQrUseCase)
                }
                val gateScannerViewModel = remember {
                    GateScannerViewModel(validateScannedTicketUseCase)
                }

                DynamicQrAppNavigation(
                    ticketDisplayViewModel = ticketDisplayViewModel,
                    gateScannerViewModel = gateScannerViewModel
                )
            }
        }
    }
}

@Composable
fun DynamicQrAppNavigation(
    ticketDisplayViewModel: TicketDisplayViewModel,
    gateScannerViewModel: GateScannerViewModel
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Mặc định mở Tab "Vé Của Tôi"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainer
            ) {
                // Tab 1: Khám phá sự kiện
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text(text = "🎪", fontSize = 18.sp) },
                    label = { Text("Sự Kiện", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                        unselectedTextColor = TextMediumEmphasis
                    )
                )

                // Tab 2: Vé Của Tôi & Modal QR Checking Pass
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text(text = "🎟️", fontSize = 18.sp) },
                    label = { Text("Vé Của Tôi", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                        unselectedTextColor = TextMediumEmphasis
                    )
                )

                // Tab 3: Soát Vé Cổng
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text(text = "📷", fontSize = 18.sp) },
                    label = { Text("Soát Vé Cổng", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanSecondary,
                        selectedTextColor = CyanSecondary,
                        indicatorColor = CyanSecondary.copy(alpha = 0.2f),
                        unselectedTextColor = TextMediumEmphasis
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> EventListScreen(
                    onEventSelected = { _ ->
                        // Chuyển sang xem vé
                        selectedTab = 1
                    }
                )
                1 -> MyTicketsListScreen(
                    ticketDisplayViewModel = ticketDisplayViewModel
                )
                2 -> GateScannerScreen(
                    viewModel = gateScannerViewModel,
                    gateId = "GATE-EAST-01"
                )
            }
        }
    }
}
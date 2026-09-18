package com.ticketing.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ticketing.mobile.core_crypto.data.NativeCryptoEngineImpl
import com.ticketing.mobile.ticket_display.data.datasource.DefaultTicketRemoteDataSource
import com.ticketing.mobile.ticket_display.data.datasource.InMemoryTicketLocalDataSource
import com.ticketing.mobile.ticket_display.data.repository.TicketRepositoryImpl
import com.ticketing.mobile.ticket_display.domain.usecase.GenerateDynamicQrUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetMyTicketsUseCase
import com.ticketing.mobile.ticket_display.domain.usecase.GetTicketDetailUseCase
import com.ticketing.mobile.ticket_display.presentation.TicketDisplayViewModel
import com.ticketing.mobile.ticket_display.presentation.ui.MyTicketsListScreen
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme
import com.ticketing.mobile.ui.theme.ObsidianVoid

/**
 * Entry Activity của ứng dụng Dynamic QR Ticketing.
 * Toàn bộ giao diện tập trung vào màn hình "Vé Của Tôi" (My Tickets)
 * và tính năng xem / quét Dynamic QR xoay vòng liên tục theo chu kỳ TOTP.
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
        val getMyTicketsUseCase = GetMyTicketsUseCase(ticketRepository)

        setContent {
            DynamicQRTicketingTheme {
                val ticketDisplayViewModel = remember {
                    TicketDisplayViewModel(
                        getTicketDetailUseCase = getTicketDetailUseCase,
                        generateDynamicQrUseCase = generateDynamicQrUseCase,
                        getMyTicketsUseCase = getMyTicketsUseCase
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianVoid
                ) {
                    MyTicketsListScreen(
                        ticketDisplayViewModel = ticketDisplayViewModel
                    )
                }
            }
        }
    }
}
package com.ticketing.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ticketing.mobile.ui.theme.DynamicQRTicketingTheme

/**
 * Entry Activity chính của ứng dụng Dynamic QR Ticketing.
 * Khung sườn sẵn sàng để bạn kết nối Navigation Component và Dependency Injection.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicQRTicketingTheme {
                DynamicQrAppScaffold()
            }
        }
    }
}

@Composable
fun DynamicQrAppScaffold() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // TODO: [Giai đoạn 5] Tự kết nối Navigation Host điều hướng giữa:
            // - TicketDisplayScreen
            // - GateScannerScreen
            Text(text = "Dynamic QR Ticketing - Architecture Ready\n(Tham khảo TECHNICAL_DESIGN.md để bắt đầu code)")
        }
    }
}
package com.ticketing.mobile.gate_scanner.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketing.mobile.gate_scanner.presentation.GateScannerViewModel

/**
 * Khung sườn Jetpack Compose Screen cho Gate Scanner.
 * Nơi bạn tự do tích hợp CameraX và xây dựng giao diện quét vé tại cổng.
 */
@Composable
fun GateScannerScreen(
    viewModel: GateScannerViewModel,
    gateId: String,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // TODO: [Giai đoạn 4] Tự thiết kế giao diện Camera:
            // 1. Tích hợp AndroidView hosting PreviewView của CameraX hoặc ML Kit Barcode Scanning.
            // 2. Vẽ khung ngắm (Viewfinder Reticle / Scan target box).
            // 3. Hiển thị Overlay trạng thái cửa soát: Xanh (Access Granted) / Đỏ (Access Denied).
            // 4. Switch toggle bật/tắt chế độ Offline mode.
            Text(text = "Gate Scanner Screen Scaffold (gateId: $gateId)")
        }
    }
}

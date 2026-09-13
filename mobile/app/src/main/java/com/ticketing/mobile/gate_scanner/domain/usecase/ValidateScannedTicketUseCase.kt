package com.ticketing.mobile.gate_scanner.domain.usecase

import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult
import com.ticketing.mobile.gate_scanner.domain.repository.IGateValidationRepository

/**
 * UseCase: Xác thực vé được quét tại cửa xoay / cổng soát vé.
 */
class ValidateScannedTicketUseCase(
    private val repository: IGateValidationRepository
) {
    suspend operator fun invoke(scanResult: ScanResult): Result<GateAccessStatus> {
        // TODO: [Giai đoạn 4] Tự viết logic kiểm tra dữ liệu scanResult
        // và chuyển đến repository để xác thực (Online API hoặc Offline C++).
        TODO("Tự triển khai ValidateScannedTicketUseCase")
    }
}

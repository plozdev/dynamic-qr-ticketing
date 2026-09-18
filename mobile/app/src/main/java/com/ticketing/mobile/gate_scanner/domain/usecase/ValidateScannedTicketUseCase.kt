package com.ticketing.mobile.gate_scanner.domain.usecase

import com.ticketing.mobile.gate_scanner.domain.model.DenyReason
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult
import com.ticketing.mobile.gate_scanner.domain.repository.IGateValidationRepository

/**
 * UseCase: Xác thực vé được quét tại cửa xoay / cổng soát vé (Gate Scanner).
 * 
 * Lớp này thuộc Domain Layer:
 * - Kiểm tra tính hợp lệ sơ bộ của chuỗi quét được từ Camera (Prefix format "TICKETING:").
 * - Chuyển tiếp tác vụ xác thực cho IGateValidationRepository (hỗ trợ cả Online và Offline Mode).
 */
class ValidateScannedTicketUseCase(
    private val repository: IGateValidationRepository
) {
    /**
     * Thực thi kiểm tra vé.
     * 
     * @param scanResult Dữ liệu quét nhận được từ camera.
     * @return Result<GateAccessStatus> biểu thị kết quả cấp quyền vào (Granted) hoặc từ chối (Denied).
     */
    suspend operator fun invoke(scanResult: ScanResult): Result<GateAccessStatus> {
        // TODO: [Giai đoạn 4] Triển khai kiểm tra sơ bộ trước khi gọi Repository:
        // Bước 1: Kiểm tra chuỗi rawQrPayload có rỗng hoặc không bắt đầu bằng "TICKETING:" không.
        //         Nếu không hợp lệ -> Trả về GateAccessStatus.Denied(DenyReason.INVALID_SIGNATURE, "Không phải mã QR vé hợp lệ")
        // Bước 2: Ủy quyền cho repository.validateTicket(scanResult).
        if (scanResult.rawQrPayload.isBlank() || !scanResult.rawQrPayload.startsWith("TICKETING:")) {
            return Result.success(
                GateAccessStatus.Denied(
                    reason = DenyReason.INVALID_SIGNATURE,
                    message = "Mã QR không thuộc hệ thống vé sự kiện"
                )
            )
        }

        return repository.validateTicket(scanResult)
    }
}

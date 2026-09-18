package com.ticketing.mobile.gate_scanner.data.mapper

import com.ticketing.mobile.gate_scanner.data.dto.GateValidationResponseDto
import com.ticketing.mobile.gate_scanner.domain.model.DenyReason
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus

/**
 * Mapper chuyển đổi GateValidationResponseDto từ Network API sang GateAccessStatus Domain model.
 * 
 * Mục đích:
 * - Tách biệt cấu trúc JSON của Backend khỏi logic hiển thị và điều khiển cửa xoay tại thiết bị.
 * - Phân loại rõ ràng kết quả: Cho phép vào (Granted) hay Từ chối (Denied) kèm lý do chi tiết.
 */
object GateValidationMapper {

    /**
     * Chuyển đổi DTO phản hồi từ Server thành Domain Model trạng thái vào cổng.
     * 
     * @param dto Dữ liệu nhận từ Backend sau khi gửi yêu cầu check-in.
     * @return GateAccessStatus.Granted nếu vé hợp lệ; GateAccessStatus.Denied nếu bị từ chối.
     */
    fun toDomain(dto: GateValidationResponseDto): GateAccessStatus {
        // TODO: [Giai đoạn 4] Triển khai logic ánh xạ:
        // Bước 1: Kiểm tra cờ dto.isAllowed:
        //   - Nếu true: Trả về GateAccessStatus.Granted với ticketId, attendeeName, seatNumber, checkInTimestamp.
        //   - Nếu false: Parse dto.rejectionReasonCode sang enum DenyReason:
        //     + "INVALID_SIGNATURE" -> DenyReason.INVALID_SIGNATURE
        //     + "EXPIRED_TIMESTAMP" -> DenyReason.EXPIRED_TIMESTAMP
        //     + "ALREADY_CHECKED_IN" -> DenyReason.ALREADY_CHECKED_IN
        //     + "TICKET_NOT_FOUND" -> DenyReason.TICKET_NOT_FOUND
        //     + "DEVICE_MISMATCH" -> DenyReason.DEVICE_MISMATCH
        //     + Còn lại -> DenyReason.SERVER_REJECTED
        //     Trả về GateAccessStatus.Denied(reason, message).
        return if (dto.isAllowed) {
            GateAccessStatus.Granted(
                ticketId = dto.ticketId.orEmpty(),
                attendeeName = dto.attendeeName.orEmpty(),
                seatNumber = dto.seatNumber.orEmpty(),
                checkInTimestamp = dto.checkInTimestamp ?: (System.currentTimeMillis() / 1000)
            )
        } else {
            val reason = runCatching {
                dto.rejectionReasonCode?.let { DenyReason.valueOf(it.trim().uppercase()) }
            }.getOrNull() ?: DenyReason.SERVER_REJECTED

            GateAccessStatus.Denied(
                reason = reason,
                message = dto.message ?: "Access Denied by Server"
            )
        }
    }
}

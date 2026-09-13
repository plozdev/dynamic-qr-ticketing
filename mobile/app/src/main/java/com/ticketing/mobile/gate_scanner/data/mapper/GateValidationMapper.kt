package com.ticketing.mobile.gate_scanner.data.mapper

import com.ticketing.mobile.gate_scanner.data.dto.GateValidationResponseDto
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus

/**
 * Khung sườn Mapper chuyển đổi GateValidationResponseDto sang GateAccessStatus domain model.
 */
object GateValidationMapper {

    fun toDomain(dto: GateValidationResponseDto): GateAccessStatus {
        // TODO: [Giai đoạn 4] Tự viết logic ánh xạ kết quả phản hồi từ Server sang trạng thái Granted hoặc Denied
        TODO("Tự triển khai GateValidationMapper")
    }
}

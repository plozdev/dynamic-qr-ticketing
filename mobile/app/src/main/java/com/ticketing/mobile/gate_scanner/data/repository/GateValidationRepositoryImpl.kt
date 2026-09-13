package com.ticketing.mobile.gate_scanner.data.repository

import com.ticketing.mobile.core_crypto.domain.repository.ICryptoEngine
import com.ticketing.mobile.gate_scanner.data.datasource.IGateRemoteDataSource
import com.ticketing.mobile.gate_scanner.domain.model.GateAccessStatus
import com.ticketing.mobile.gate_scanner.domain.model.ScanResult
import com.ticketing.mobile.gate_scanner.domain.repository.IGateValidationRepository

/**
 * Khung sườn triển khai IGateValidationRepository hỗ trợ xác thực Hybrid:
 * - Khi Online: Gửi dữ liệu về Server để check-in và chống dùng lại mã.
 * - Khi Offline: Dùng C++ NDK CryptoEngine để kiểm tra chữ ký và độ trôi thời gian ngay tại thiết bị.
 */
class GateValidationRepositoryImpl(
    private val remoteDataSource: IGateRemoteDataSource,
    private val cryptoEngine: ICryptoEngine,
    private val isOfflineEnabled: Boolean = false
) : IGateValidationRepository {

    override suspend fun validateTicket(scanResult: ScanResult): Result<GateAccessStatus> {
        // TODO: [Giai đoạn 4] Tự viết logic điều phối:
        // 1. Nếu isOfflineEnabled == true: Gọi hàm validateLocally(scanResult) qua cryptoEngine
        // 2. Nếu Online: Gọi remoteDataSource.verifyAndCheckIn(...)
        // 3. Nếu mạng lỗi bất ngờ: Tự động fallback về xác thực Offline C++
        TODO("Tự triển khai xác thực vé tại cổng (Hybrid Online/Offline)")
    }

    override fun isOfflineMode(): Boolean = isOfflineEnabled
}

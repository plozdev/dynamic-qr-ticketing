package com.ticketing.mobile.core_network.client

import com.ticketing.mobile.core_network.model.NetworkResult
import okhttp3.OkHttpClient

/**
 * Khung sườn triển khai IApiClient sử dụng OkHttp.
 */
class OkHttpApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder().build(),
    private val baseUrl: String = "https://api.ticketing.internal/v1"
) : IApiClient {

    override suspend fun <T> get(
        endpoint: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        // TODO: [Giai đoạn 2] Tự viết logic thực thi HTTP GET bằng OkHttp:
        // 1. Tạo Request với URL "$baseUrl$endpoint" và add headers
        // 2. Thực thi bất đồng bộ trên Dispatchers.IO
        // 3. Phân loại mã HTTP status: 200..299 -> Success, 401 -> Unauthorized, còn lại -> HttpError
        TODO("Tự triển khai logic gửi HTTP GET với OkHttp")
    }

    override suspend fun <T> post(
        endpoint: String,
        bodyJson: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        // TODO: [Giai đoạn 2] Tự viết logic thực thi HTTP POST bằng OkHttp
        TODO("Tự triển khai logic gửi HTTP POST với OkHttp")
    }
}

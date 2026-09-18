package com.ticketing.mobile.core_network.client

import com.ticketing.mobile.core_network.model.ApiError
import com.ticketing.mobile.core_network.model.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Triển khai IApiClient sử dụng OkHttpClient để thực thi các yêu cầu mạng RESTful.
 * 
 * Mục đích:
 * - Đảm bảo mọi tác vụ mạng luôn chạy trên IO Dispatcher (Dispatchers.IO).
 * - Đóng gói các phản hồi HTTP thành Sealed Interface NetworkResult (Success, Error).
 * - Bắt triệt để các lỗi mạng như Timeout, DNS, No Internet để không làm sập ứng dụng.
 */
class OkHttpApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder().build(),
    private val baseUrl: String = "https://api.ticketing.internal/v1"
) : IApiClient {

    /**
     * Gửi HTTP GET request.
     */
    override suspend fun <T> get(
        endpoint: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val url = if (endpoint.startsWith("http")) endpoint else "$baseUrl$endpoint"
            val requestBuilder = Request.Builder().url(url)
            headers.forEach { (key, value) -> requestBuilder.header(key, value) }

            val response = client.newCall(requestBuilder.build()).execute()
            response.use { resp ->
                val body = resp.body?.string().orEmpty()
                if (resp.isSuccessful) {
                    try {
                        val parsed = deserializer(body)
                        NetworkResult.Success(parsed, resp.code)
                    } catch (e: Throwable) {
                        NetworkResult.Error(ApiError.SerializationError(e))
                    }
                } else if (resp.code == 401) {
                    NetworkResult.Error(ApiError.Unauthorized("Unauthorized: $body"))
                } else {
                    NetworkResult.Error(ApiError.HttpError(resp.code, body))
                }
            }
        } catch (e: IOException) {
            NetworkResult.Error(ApiError.NetworkConnection(e))
        } catch (e: Throwable) {
            NetworkResult.Error(ApiError.Unknown(e))
        }
    }

    /**
     * Gửi HTTP POST request với Payload JSON.
     */
    override suspend fun <T> post(
        endpoint: String,
        bodyJson: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val url = if (endpoint.startsWith("http")) endpoint else "$baseUrl$endpoint"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = bodyJson.toRequestBody(mediaType)
            val requestBuilder = Request.Builder().url(url).post(requestBody)
            headers.forEach { (key, value) -> requestBuilder.header(key, value) }

            val response = client.newCall(requestBuilder.build()).execute()
            response.use { resp ->
                val body = resp.body?.string().orEmpty()
                if (resp.isSuccessful) {
                    try {
                        val parsed = deserializer(body)
                        NetworkResult.Success(parsed, resp.code)
                    } catch (e: Throwable) {
                        NetworkResult.Error(ApiError.SerializationError(e))
                    }
                } else if (resp.code == 401) {
                    NetworkResult.Error(ApiError.Unauthorized("Unauthorized: $body"))
                } else {
                    NetworkResult.Error(ApiError.HttpError(resp.code, body))
                }
            }
        } catch (e: IOException) {
            NetworkResult.Error(ApiError.NetworkConnection(e))
        } catch (e: Throwable) {
            NetworkResult.Error(ApiError.Unknown(e))
        }
    }
}

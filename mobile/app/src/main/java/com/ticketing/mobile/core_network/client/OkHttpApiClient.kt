package com.ticketing.mobile.core_network.client

import com.ticketing.mobile.core_network.interceptor.AuthHeaderInterceptor
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
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(java.time.Duration.ofSeconds(3))
        .readTimeout(java.time.Duration.ofSeconds(10))
        .addInterceptor(AuthHeaderInterceptor())
        .build(),
    candidateBaseUrls: List<String> = defaultCandidateUrls()
) : IApiClient {

    constructor(baseUrl: String) : this(
        candidateBaseUrls = (listOf(baseUrl) + defaultCandidateUrls()).distinct()
    )

    private val baseUrls = candidateBaseUrls.toMutableList()

    /**
     * Gửi HTTP GET request.
     */
    override suspend fun <T> get(
        endpoint: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        if (endpoint.startsWith("http")) {
            return@withContext executeGet(endpoint, headers, deserializer)
        }

        val currentActive = activeBaseUrl
        val urlsToTry = if (currentActive != null) {
            listOf(currentActive) + baseUrls.filter { it != currentActive }
        } else {
            baseUrls.toList()
        }

        var lastError: ApiError? = null

        for (base in urlsToTry) {
            val fullUrl = "$base$endpoint"
            when (val result = executeGet(fullUrl, headers, deserializer)) {
                is NetworkResult.Success -> {
                    activeBaseUrl = base
                    return@withContext result
                }
                is NetworkResult.Error -> {
                    lastError = result.error
                    if (result.error !is ApiError.NetworkConnection) {
                        // Lỗi nghiệp vụ HTTP (400, 401, 403, 500), không phải do lỗi kết nối socket
                        return@withContext result
                    }
                    // Nếu là lỗi kết nối (timeout/refused), tiếp tục thử candidate tiếp theo
                    android.util.Log.w("OkHttpApiClient", "Host $base failed to connect, trying next candidate...")
                }
                is NetworkResult.Loading -> {}
            }
        }

        NetworkResult.Error(lastError ?: ApiError.NetworkConnection(IOException("All base URL candidates failed to connect")))
    }

    private fun <T> executeGet(
        url: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        try {
            android.util.Log.d("OkHttpApiClient", "Sending GET request to: $url")
            val requestBuilder = Request.Builder().url(url)
            headers.forEach { (key, value) -> requestBuilder.header(key, value) }

            val response = client.newCall(requestBuilder.build()).execute()
            response.use { resp ->
                val body = resp.body.string()
                android.util.Log.d("OkHttpApiClient", "Received GET response from: $url [HTTP ${resp.code}]")
                return if (resp.isSuccessful) {
                    try {
                        val parsed = deserializer(body)
                        NetworkResult.Success(parsed, resp.code)
                    } catch (e: Throwable) {
                        android.util.Log.e("OkHttpApiClient", "Serialization error on $url", e)
                        NetworkResult.Error(ApiError.SerializationError(e))
                    }
                } else if (resp.code == 401) {
                    NetworkResult.Error(ApiError.Unauthorized("Unauthorized: $body"))
                } else {
                    android.util.Log.e("OkHttpApiClient", "HTTP Error on $url: code=${resp.code}, body=$body")
                    NetworkResult.Error(ApiError.HttpError(resp.code, body))
                }
            }
        } catch (e: IOException) {
            android.util.Log.e("OkHttpApiClient", "NetworkConnection failure on $url: ${e.message}")
            return NetworkResult.Error(ApiError.NetworkConnection(e))
        } catch (e: Throwable) {
            android.util.Log.e("OkHttpApiClient", "Unexpected error on $url", e)
            return NetworkResult.Error(ApiError.Unknown(e))
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
        if (endpoint.startsWith("http")) {
            return@withContext executePost(endpoint, bodyJson, headers, deserializer)
        }

        val currentActive = activeBaseUrl
        val urlsToTry = if (currentActive != null) {
            listOf(currentActive) + baseUrls.filter { it != currentActive }
        } else {
            baseUrls.toList()
        }

        var lastError: ApiError? = null

        for (base in urlsToTry) {
            val fullUrl = "$base$endpoint"
            when (val result = executePost(fullUrl, bodyJson, headers, deserializer)) {
                is NetworkResult.Success -> {
                    activeBaseUrl = base
                    return@withContext result
                }
                is NetworkResult.Error -> {
                    lastError = result.error
                    if (result.error !is ApiError.NetworkConnection) {
                        return@withContext result
                    }
                    android.util.Log.w("OkHttpApiClient", "POST Host $base failed to connect, trying next candidate...")
                }
                is NetworkResult.Loading -> {}
            }
        }

        NetworkResult.Error(lastError ?: ApiError.NetworkConnection(IOException("All base URL candidates failed to connect")))
    }

    private fun <T> executePost(
        url: String,
        bodyJson: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        return try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = bodyJson.toRequestBody(mediaType)
            val requestBuilder = Request.Builder().url(url).post(requestBody)
            headers.forEach { (key, value) -> requestBuilder.header(key, value) }

            val response = client.newCall(requestBuilder.build()).execute()
            response.use { resp ->
                val body = resp.body.string()
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

    companion object {
        @Volatile
        var customBaseUrl: String? = null

        @Volatile
        var activeBaseUrl: String? = null

        fun getCandidates(): List<String> {
            val active = activeBaseUrl
            val list = defaultCandidateUrls()
            return if (active != null) {
                (listOf(active) + list).distinct()
            } else {
                list
            }
        }

        private fun isEmulator(): Boolean {
            val fp = android.os.Build.FINGERPRINT ?: ""
            val hw = android.os.Build.HARDWARE ?: ""
            val model = android.os.Build.MODEL ?: ""
            return fp.startsWith("generic") || fp.startsWith("unknown") ||
                    hw.contains("goldfish") || hw.contains("ranchu") ||
                    model.contains("google_sdk") || model.contains("Emulator")
        }

        fun defaultCandidateUrls(): List<String> {
            val baseList = if (isEmulator()) {
                listOf(
                    "http://10.0.2.2:8080/api/v1",
                    "http://127.0.0.1:8080/api/v1",
                    "http://10.0.116.57:8080/api/v1",
                    "http://192.168.2.8:8080/api/v1"
                )
            } else {
                listOf(
                    "http://10.0.116.57:8080/api/v1",
                    "http://127.0.0.1:8080/api/v1",
                    "http://192.168.2.8:8080/api/v1",
                    "http://10.0.2.2:8080/api/v1"
                )
            }
            val custom = customBaseUrl?.trim()?.removeSuffix("/")
            return if (!custom.isNullOrBlank()) {
                val fullCustom = if (custom.endsWith("/api/v1")) custom else "$custom/api/v1"
                (listOf(fullCustom) + baseList).distinct()
            } else {
                baseList
            }
        }
    }
}

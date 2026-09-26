package com.ticketing.mobile.core_network.sse

import android.util.Log
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.interceptor.AuthHeaderInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference

/**
 * Triển khai ITicketSseClient sử dụng OkHttpClient stream.
 *
 * Đặc điểm:
 * - Hỗ trợ auto-reconnect khi rớt mạng hoặc proxy timeout.
 * - Tự động thử các candidate base URL để tìm ra host backend đang hoạt động.
 * - Gọi onConnected mỗi khi kết nối mới hoặc kết nối lại thành công để UI tải lại trạng thái chính thức.
 * - Nhận và parse sự kiện 'ticket-update' và đẩy ra Flow cho UI cập nhật ngay lập tức.
 */
class DefaultTicketSseClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ZERO) // Không giới hạn read timeout cho long-lived SSE stream
        .addInterceptor(AuthHeaderInterceptor())
        .build()
) : ITicketSseClient {

    private val currentCall = AtomicReference<Call?>(null)

    companion object {
        private const val TAG = "TicketSseClient"
        private const val RECONNECT_DELAY_MS = 3000L
    }

    override fun observeTicketUpdates(
        userId: String,
        ticketId: String?,
        onConnected: (() -> Unit)?
    ): Flow<TicketUpdateSseEvent> = flow {
        while (currentCoroutineContext().isActive) {
            val candidateUrls = OkHttpApiClient.getCandidates()
            var connected = false

            for (baseUrl in candidateUrls) {
                if (!currentCoroutineContext().isActive) break

                val urlBuilder = StringBuilder("$baseUrl/tickets/stream?userId=$userId")
                if (!ticketId.isNullOrBlank()) {
                    urlBuilder.append("&ticketId=$ticketId")
                }
                val sseUrl = urlBuilder.toString()

                Log.d(TAG, "Attempting SSE connection to: $sseUrl")

                val request = Request.Builder()
                    .url(sseUrl)
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .build()

                val call = client.newCall(request)
                currentCall.set(call)

                try {
                    val response = call.execute()
                    if (!response.isSuccessful) {
                        Log.w(TAG, "SSE connection failed with HTTP ${response.code} at $baseUrl")
                        response.close()
                        continue
                    }

                    // Lưu base URL đang hoạt động tốt cho toàn bộ app
                    OkHttpApiClient.activeBaseUrl = baseUrl
                    connected = true
                    Log.i(TAG, "SSE stream connected successfully to: $sseUrl")

                    // Báo hiệu kết nối thành công để mobile tải lại trạng thái vé chính thức
                    try {
                        onConnected?.invoke()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error in onConnected callback: ${e.message}")
                    }

                    response.use { resp ->
                        val reader = BufferedReader(InputStreamReader(resp.body.byteStream(), StandardCharsets.UTF_8))
                        var currentEventName = ""
                        val currentDataBuffer = StringBuilder()

                        while (currentCoroutineContext().isActive) {
                            val line = reader.readLine() ?: break // null nghĩa là stream kết thúc từ phía server

                            val trimmed = line.trim()
                            if (trimmed.startsWith(":")) {
                                // Heartbeat ping từ server (:ping) -> duy trì kết nối
                                continue
                            }

                            if (trimmed.startsWith("event:")) {
                                currentEventName = trimmed.substring(6).trim()
                            } else if (trimmed.startsWith("data:")) {
                                val dataContent = trimmed.substring(5).trim()
                                if (currentDataBuffer.isNotEmpty()) {
                                    currentDataBuffer.append("\n")
                                }
                                currentDataBuffer.append(dataContent)
                            } else if (trimmed.isEmpty()) {
                                // Dòng trống là dấu phân cách kết thúc một SSE event message
                                if (currentEventName == "ticket-update" && currentDataBuffer.isNotEmpty()) {
                                    val event = parseSseEvent(currentDataBuffer.toString())
                                    if (event != null) {
                                        Log.i(TAG, "Received ticket-update SSE: ticketId=${event.ticketId}, status=${event.status}")
                                        emit(event)
                                    }
                                } else if (currentEventName == "connected") {
                                    Log.d(TAG, "Server confirmed connected: $currentDataBuffer")
                                }
                                currentEventName = ""
                                currentDataBuffer.setLength(0)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!currentCoroutineContext().isActive) {
                        Log.d(TAG, "SSE collection cancelled by coroutine scope")
                        return@flow
                    }
                    Log.w(TAG, "SSE stream connection lost from $baseUrl: ${e.message}")
                } finally {
                    currentCall.set(null)
                }

                if (connected) {
                    // Nếu đã kết nối rồi nhưng bị đứt luồng, ngắt vòng for để chờ delay rồi kết nối lại
                    break
                }
            }

            if (!currentCoroutineContext().isActive) break

            Log.d(TAG, "Reconnecting SSE in ${RECONNECT_DELAY_MS}ms...")
            delay(RECONNECT_DELAY_MS)
        }
    }.flowOn(Dispatchers.IO)

    private fun parseSseEvent(jsonStr: String): TicketUpdateSseEvent? {
        return try {
            val obj = JSONObject(jsonStr)
            TicketUpdateSseEvent(
                eventType = obj.optString("eventType", "TICKET_CHECKED_IN"),
                ticketId = obj.optString("ticketId"),
                userId = obj.optString("userId"),
                status = obj.optString("status", "CHECKED_IN"),
                gateId = obj.optString("gateId", ""),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis() / 1000)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ticket-update JSON: $jsonStr", e)
            null
        }
    }

    override fun disconnect() {
        try {
            currentCall.getAndSet(null)?.cancel()
            Log.d(TAG, "SSE client explicitly disconnected")
        } catch (e: Exception) {
            Log.w(TAG, "Error cancelling SSE call: ${e.message}")
        }
    }
}

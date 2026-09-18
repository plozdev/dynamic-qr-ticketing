package com.ticketing.mobile.events.data.datasource

import com.ticketing.mobile.core_network.client.IApiClient
import com.ticketing.mobile.core_network.client.OkHttpApiClient
import com.ticketing.mobile.core_network.model.NetworkResult
import com.ticketing.mobile.events.domain.model.EventItem
import org.json.JSONArray
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DefaultEventRemoteDataSource(
    private val apiClient: IApiClient = OkHttpApiClient(baseUrl = "http://10.0.2.2:8080/api/v1")
) : IEventRemoteDataSource {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    private val fallbackEvents = listOf(
        EventItem(
            id = "e1111111-1111-1111-1111-111111111111",
            title = "Hà Nội Rock Fest 2026",
            venue = "SVĐ Quốc Gia Mỹ Đình, Hà Nội",
            dateDisplay = "24/10/2026 • 19:30",
            priceDisplay = "450.000₫",
            category = "Âm nhạc & Concert",
            remainingPercentage = 12,
            isHotTrend = true,
            isDynamicPassSupported = true,
            bannerUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=800&q=80"
        ),
        EventItem(
            id = "e2222222-2222-2222-2222-222222222222",
            title = "Đại Nhạc Hội Monsoon EDM",
            venue = "TT Hội Nghị Quốc Gia, Hà Nội",
            dateDisplay = "15/11/2026 • 18:00",
            priceDisplay = "690.000₫",
            category = "Âm nhạc & Concert",
            remainingPercentage = 45,
            isHotTrend = true,
            isDynamicPassSupported = true,
            bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=800&q=80"
        ),
        EventItem(
            id = "e3333333-3333-3333-3333-333333333333",
            title = "Chung Kết Cúp Quốc Gia 2026",
            venue = "SVĐ Hàng Đẫy, Hà Nội",
            dateDisplay = "30/10/2026 • 17:00",
            priceDisplay = "200.000₫",
            category = "Thể thao",
            remainingPercentage = 8,
            isHotTrend = false,
            isDynamicPassSupported = true,
            bannerUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=800&q=80"
        )
    )

    override suspend fun getEvents(category: String?): NetworkResult<List<EventItem>> {
        val endpoint = if (!category.isNullOrBlank() && !category.equals("Tất cả", ignoreCase = true)) {
            "/events?category=${java.net.URLEncoder.encode(category, "UTF-8")}"
        } else {
            "/events"
        }

        val result = apiClient.get(endpoint) { jsonStr ->
            val jsonArray = JSONArray(jsonStr)
            val events = mutableListOf<EventItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id")
                val title = obj.optString("name")
                val venue = obj.optString("venueName", "Chưa xác định")
                val startIso = obj.optString("startDateTime")
                val dateDisplay = try {
                    dateFormatter.format(Instant.parse(startIso))
                } catch (e: Exception) {
                    "Sắp diễn ra"
                }
                val gateOpensAtDisplay = try {
                    timeFormatter.format(Instant.parse(startIso).minusSeconds(obj.optLong("checkInWindowMinutes", 120) * 60))
                } catch (e: Exception) {
                    "18:00"
                }
                val basePrice = obj.optDouble("basePrice", 450000.0)
                val priceFormatted = currencyFormatter.format(basePrice).replace("₫", "").trim() + "₫"
                val cat = obj.optString("category", "Âm nhạc & Concert")
                val remainingPercentage = obj.optInt("remainingPercentage", 50)
                val isHotTrend = obj.optBoolean("isHotTrend", false)
                val bannerUrl = obj.optString("bannerUrl").takeIf { it.isNotBlank() }

                events.add(
                    EventItem(
                        id = id,
                        title = title,
                        venue = venue,
                        dateDisplay = dateDisplay,
                        priceDisplay = priceFormatted,
                        category = cat,
                        remainingPercentage = remainingPercentage,
                        isHotTrend = isHotTrend,
                        isDynamicPassSupported = true,
                        gateOpensAtDisplay = gateOpensAtDisplay,
                        bannerUrl = bannerUrl
                    )
                )
            }
            events
        }

        return when (result) {
            is NetworkResult.Success -> result
            is NetworkResult.Error, NetworkResult.Loading -> {
                // Offline-resilient fallback
                val filtered = if (!category.isNullOrBlank() && !category.equals("Tất cả", ignoreCase = true)) {
                    fallbackEvents.filter { it.category.equals(category, ignoreCase = true) }
                } else {
                    fallbackEvents
                }
                NetworkResult.Success(filtered)
            }
        }
    }
}

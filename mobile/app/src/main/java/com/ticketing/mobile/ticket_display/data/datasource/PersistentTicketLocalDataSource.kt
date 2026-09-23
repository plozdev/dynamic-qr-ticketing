package com.ticketing.mobile.ticket_display.data.datasource

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ticketing.mobile.core_crypto.data.ISecureKeyStorage
import com.ticketing.mobile.core_crypto.data.SecureKeyStorage
import com.ticketing.mobile.ticket_display.data.dto.TicketDto
import com.ticketing.mobile.ticket_display.domain.model.UserTicketCheckInStatus
import com.ticketing.mobile.ticket_display.domain.model.UserTicketItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Triển khai ITicketLocalDataSource lưu trữ bền vững trong SQLite cục bộ trên thiết bị
 * kết hợp với ISecureKeyStorage (KeyStore AES-256) bảo vệ mã khóa bí mật.
 * Giúp người dùng mở vé và sinh mã Dynamic QR hoàn toàn ngoại tuyến kể cả khi tắt app.
 */
class PersistentTicketLocalDataSource(
    context: Context,
    private val secureKeyStorage: ISecureKeyStorage = SecureKeyStorage(context)
) : ITicketLocalDataSource {

    private val dbHelper = TicketDatabaseHelper(context)

    override suspend fun getCachedTicket(ticketId: String): TicketDto? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_TICKETS,
            null,
            "$COL_TICKET_ID = ?",
            arrayOf(ticketId),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val secretKey = secureKeyStorage.getSecretKey(ticketId)
                TicketDto(
                    ticketId = it.getString(it.getColumnIndexOrThrow(COL_TICKET_ID)),
                    eventTitle = it.getString(it.getColumnIndexOrThrow(COL_EVENT_NAME)),
                    location = it.getString(it.getColumnIndexOrThrow(COL_VENUE)),
                    eventEpochSeconds = it.getLong(it.getColumnIndexOrThrow(COL_EVENT_EPOCH)),
                    seatCode = it.getString(it.getColumnIndexOrThrow(COL_SEAT_NUMBER)),
                    customerFullName = it.getString(it.getColumnIndexOrThrow(COL_ATTENDEE_NAME)),
                    statusCode = it.getString(it.getColumnIndexOrThrow(COL_STATUS)),
                    secretKey = secretKey
                )
            } else {
                null
            }
        }
    }

    override suspend fun getSecretKey(ticketId: String): String? = withContext(Dispatchers.IO) {
        secureKeyStorage.getSecretKey(ticketId)
    }

    override suspend fun saveTicket(ticket: TicketDto) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COL_TICKET_ID, ticket.ticketId)
            put(COL_EVENT_NAME, ticket.eventTitle)
            put(COL_VENUE, ticket.location)
            put(COL_EVENT_EPOCH, ticket.eventEpochSeconds)
            put(COL_SEAT_NUMBER, ticket.seatCode)
            put(COL_ATTENDEE_NAME, ticket.customerFullName)
            put(COL_STATUS, ticket.statusCode)
            put(COL_UPDATED_AT, System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_TICKETS, null, values, SQLiteDatabase.CONFLICT_REPLACE)

        ticket.secretKey?.let { key ->
            secureKeyStorage.saveSecretKey(ticket.ticketId, key)
        }
        Unit
    }

    suspend fun saveUserTickets(tickets: List<UserTicketItem>) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_TICKETS, null, null)
            for (ticket in tickets) {
                val values = ContentValues().apply {
                    put(COL_TICKET_ID, ticket.ticketId)
                    put(COL_EVENT_NAME, ticket.eventName)
                    put(COL_VENUE, ticket.venue)
                    put(COL_DATE_DISPLAY, ticket.dateDisplay)
                    put(COL_SEAT_NUMBER, ticket.seatNumber)
                    put(COL_ATTENDEE_NAME, ticket.attendeeName)
                    put(COL_TIER_NAME, ticket.tierName)
                    put(COL_STATUS, ticket.status.name)
                    put(COL_GATE_INFO, ticket.gateInfo)
                    put(COL_CHECK_IN_NOTE, ticket.checkInNote)
                    put(COL_CHECK_IN_OPENS_AT, ticket.checkInOpensAtEpochSeconds)
                    put(COL_IS_CHECK_IN_OPEN, if (ticket.isCheckInOpen) 1 else 0)
                    put(COL_UPDATED_AT, System.currentTimeMillis())
                }
                db.insertWithOnConflict(TABLE_TICKETS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun getAllCachedUserTickets(): List<UserTicketItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UserTicketItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_TICKETS,
            null,
            null,
            null,
            null,
            null,
            "$COL_UPDATED_AT DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val statusStr = it.getString(it.getColumnIndexOrThrow(COL_STATUS)) ?: "NOT_YET_CHECK_IN"
                val status = try {
                    UserTicketCheckInStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    UserTicketCheckInStatus.NOT_YET_CHECK_IN
                }
                list.add(
                    UserTicketItem(
                        ticketId = it.getString(it.getColumnIndexOrThrow(COL_TICKET_ID)),
                        eventName = it.getString(it.getColumnIndexOrThrow(COL_EVENT_NAME)) ?: "Sự Kiện",
                        venue = it.getString(it.getColumnIndexOrThrow(COL_VENUE)) ?: "Chưa xác định",
                        dateDisplay = it.getString(it.getColumnIndexOrThrow(COL_DATE_DISPLAY)) ?: "Sắp diễn ra",
                        seatNumber = it.getString(it.getColumnIndexOrThrow(COL_SEAT_NUMBER)) ?: "GA-01",
                        attendeeName = it.getString(it.getColumnIndexOrThrow(COL_ATTENDEE_NAME)) ?: "Khán Giả",
                        tierName = it.getString(it.getColumnIndexOrThrow(COL_TIER_NAME)) ?: "Standard",
                        status = status,
                        gateInfo = it.getString(it.getColumnIndexOrThrow(COL_GATE_INFO)) ?: "CỔNG CHÍNH",
                        checkInNote = it.getString(it.getColumnIndexOrThrow(COL_CHECK_IN_NOTE)) ?: "",
                        checkInOpensAtEpochSeconds = it.getLong(it.getColumnIndexOrThrow(COL_CHECK_IN_OPENS_AT)),
                        isCheckInOpen = it.getInt(it.getColumnIndexOrThrow(COL_IS_CHECK_IN_OPEN)) == 1
                    )
                )
            }
        }
        list
    }

    override suspend fun clearCache() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(TABLE_TICKETS, null, null)
        secureKeyStorage.clear()
    }

    private class TicketDatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_TICKETS (
                    $COL_TICKET_ID TEXT PRIMARY KEY,
                    $COL_EVENT_NAME TEXT,
                    $COL_VENUE TEXT,
                    $COL_DATE_DISPLAY TEXT,
                    $COL_SEAT_NUMBER TEXT,
                    $COL_ATTENDEE_NAME TEXT,
                    $COL_TIER_NAME TEXT,
                    $COL_STATUS TEXT,
                    $COL_GATE_INFO TEXT,
                    $COL_CHECK_IN_NOTE TEXT,
                    $COL_CHECK_IN_OPENS_AT INTEGER DEFAULT 0,
                    $COL_IS_CHECK_IN_OPEN INTEGER DEFAULT 0,
                    $COL_EVENT_EPOCH INTEGER DEFAULT 0,
                    $COL_UPDATED_AT INTEGER DEFAULT 0
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TICKETS")
            onCreate(db)
        }
    }

    companion object {
        private const val DB_NAME = "securetix_offline.db"
        private const val DB_VERSION = 1
        private const val TABLE_TICKETS = "offline_tickets"

        private const val COL_TICKET_ID = "ticket_id"
        private const val COL_EVENT_NAME = "event_name"
        private const val COL_VENUE = "venue"
        private const val COL_DATE_DISPLAY = "date_display"
        private const val COL_SEAT_NUMBER = "seat_number"
        private const val COL_ATTENDEE_NAME = "attendee_name"
        private const val COL_TIER_NAME = "tier_name"
        private const val COL_STATUS = "status"
        private const val COL_GATE_INFO = "gate_info"
        private const val COL_CHECK_IN_NOTE = "check_in_note"
        private const val COL_CHECK_IN_OPENS_AT = "check_in_opens_at"
        private const val COL_IS_CHECK_IN_OPEN = "is_check_in_open"
        private const val COL_EVENT_EPOCH = "event_epoch"
        private const val COL_UPDATED_AT = "updated_at"
    }
}

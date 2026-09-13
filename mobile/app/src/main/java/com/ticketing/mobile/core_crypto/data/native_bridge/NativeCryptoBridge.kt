package com.ticketing.mobile.core_crypto.data.native_bridge

import android.util.Log

/**
 * JNI Bridge kết nối Kotlin với thư viện libdynamic_qr_crypto.so
 */
class NativeCryptoBridge {

    companion object {
        private const val TAG = "NativeCryptoBridge"
        private var isLibraryLoaded = false

        init {
            try {
                System.loadLibrary("dynamic_qr_crypto")
                isLibraryLoaded = true
                Log.i(TAG, "Successfully loaded native library: libdynamic_qr_crypto.so")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load dynamic_qr_crypto native library", e)
                isLibraryLoaded = false
            }
        }

        fun isNativeAvailable(): Boolean = isLibraryLoaded
    }

    external fun generateDynamicTotpToken(
        ticketId: String,
        secretKey: String,
        epochSeconds: Long,
        intervalSec: Int
    ): String

    external fun verifyDynamicTotpToken(
        ticketId: String,
        secretKey: String,
        token: String,
        epochSeconds: Long,
        intervalSec: Int,
        allowedDriftSteps: Int
    ): Boolean

    external fun getSecurityVersion(): String
}

package com.fakeroot.manager.starter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Manages auto-start functionality for FakeRoot.
 */
class IMQSStarter(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fakeroot_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_AUTO_START = "auto_start_enabled"
        const val KEY_LAST_START_TIME = "last_start_time"
    }

    /**
     * Check if auto-start is enabled.
     */
    var isAutoStartEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START, value).apply()

    /**
     * Record a successful start.
     */
    fun recordStart() {
        prefs.edit()
            .putLong(KEY_LAST_START_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Get the last start time.
     */
    fun getLastStartTime(): Long {
        return prefs.getLong(KEY_LAST_START_TIME, 0)
    }

    /**
     * Attempt to start the service automatically.
     *
     * @return true if successful
     */
    fun attemptAutoStart(): Boolean {
        if (!isAutoStartEnabled) {
            return false
        }

        if (!Starter.isIMQSNativeAvailable()) {
            return false
        }

        val result = Starter.startViaIMQSNative(context)
        if (result is Starter.StarterResult.Success) {
            recordStart()
            return true
        }

        return false
    }
}

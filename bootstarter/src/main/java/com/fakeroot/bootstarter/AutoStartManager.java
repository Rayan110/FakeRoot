package com.fakeroot.bootstarter;

import android.content.Context;
import android.content.SharedPreferences;

import com.fakeroot.core.IMQSNativeExecutor;

/**
 * Manager for auto-start configuration.
 */
public class AutoStartManager {

    private static final String PREFS_NAME = "fakeroot_prefs";
    private static final String KEY_AUTO_START = "auto_start_enabled";
    private static final String KEY_LAST_START = "last_auto_start";
    private static final String KEY_START_COUNT = "start_count";

    private final Context context;
    private final SharedPreferences prefs;

    public AutoStartManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if auto-start is enabled.
     */
    public boolean isAutoStartEnabled() {
        return prefs.getBoolean(KEY_AUTO_START, false);
    }

    /**
     * Enable or disable auto-start.
     */
    public void setAutoStartEnabled(boolean enabled) {
        prefs.edit()
                .putBoolean(KEY_AUTO_START, enabled)
                .apply();
    }

    /**
     * Record a successful auto-start.
     */
    public void recordAutoStart() {
        prefs.edit()
                .putLong(KEY_LAST_START, System.currentTimeMillis())
                .putInt(KEY_START_COUNT, getStartCount() + 1)
                .apply();
    }

    /**
     * Get the timestamp of the last auto-start.
     */
    public long getLastStartTime() {
        return prefs.getLong(KEY_LAST_START, 0);
    }

    /**
     * Get the number of successful auto-starts.
     */
    public int getStartCount() {
        return prefs.getInt(KEY_START_COUNT, 0);
    }

    /**
     * Check if IMQSNative service is available.
     */
    public boolean isIMQSNativeAvailable() {
        return IMQSNativeExecutor.isServiceAvailable();
    }

    /**
     * Get auto-start status info.
     */
    public AutoStartInfo getAutoStartInfo() {
        return new AutoStartInfo(
                isAutoStartEnabled(),
                isIMQSNativeAvailable(),
                getLastStartTime(),
                getStartCount()
        );
    }

    /**
     * Data class for auto-start information.
     */
    public static class AutoStartInfo {
        public final boolean enabled;
        public final boolean imqsAvailable;
        public final long lastStartTime;
        public final int startCount;

        public AutoStartInfo(boolean enabled, boolean imqsAvailable, long lastStartTime, int startCount) {
            this.enabled = enabled;
            this.imqsAvailable = imqsAvailable;
            this.lastStartTime = lastStartTime;
            this.startCount = startCount;
        }
    }
}

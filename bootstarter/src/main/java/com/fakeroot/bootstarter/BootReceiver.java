package com.fakeroot.bootstarter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fakeroot.core.IMQSNativeExecutor;
import com.fakeroot.core.RootExecutor;

/**
 * BroadcastReceiver that starts the FakeRoot service on boot.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "fakeroot_prefs";
    private static final String KEY_AUTO_START = "auto_start_enabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            handleBootCompleted(context);
        }
    }

    private void handleBootCompleted(Context context) {
        Log.i(TAG, "Boot completed, checking auto-start...");

        // Check if auto-start is enabled
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean autoStartEnabled = prefs.getBoolean(KEY_AUTO_START, false);

        if (!autoStartEnabled) {
            Log.d(TAG, "Auto-start is disabled");
            return;
        }

        // Check if IMQSNative is available
        if (!IMQSNativeExecutor.isServiceAvailable()) {
            Log.w(TAG, "IMQSNative service not available, cannot auto-start");
            return;
        }

        // Delay start slightly to allow system to settle
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startFakeRootService(context);
        }, 5000); // 5 second delay
    }

    private void startFakeRootService(Context context) {
        Log.i(TAG, "Attempting to start FakeRoot service...");

        try {
            // Build the start command
            String apkPath = getManagerApkPath(context);
            if (apkPath == null) {
                Log.e(TAG, "Could not find manager APK path");
                return;
            }

            String startCommand = buildStartCommand(apkPath);

            // Execute via IMQSNative
            RootExecutor executor = RootExecutor.getInstance();
            executor.executeShell(startCommand);

            // Record the start
            recordStartTime(context);

            Log.i(TAG, "FakeRoot service started successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to start FakeRoot service", e);
        }
    }

    private String getManagerApkPath(Context context) {
        try {
            return context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 0)
                    .sourceDir;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildStartCommand(String apkPath) {
        return "CLASSPATH='" + apkPath + "' " +
                "app_process /system/bin " +
                "--nice-name=fakeroot_server " +
                "com.fakeroot.server.FakeRootService &";
    }

    private void recordStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong("last_auto_start", System.currentTimeMillis())
                .apply();
    }
}

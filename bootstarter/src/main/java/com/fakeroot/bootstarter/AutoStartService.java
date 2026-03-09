package com.fakeroot.bootstarter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.fakeroot.core.IMQSNativeExecutor;
import com.fakeroot.core.RootExecutor;

/**
 * Service that monitors for startup events and triggers auto-start.
 *
 * This is an alternative to BootReceiver for devices where boot completed
 * broadcasts may not be reliable.
 */
public class AutoStartService extends Service {

    private static final String TAG = "AutoStartService";
    private static final String PREFS_NAME = "fakeroot_prefs";
    private static final String KEY_AUTO_START = "auto_start_enabled";

    public static void start(Context context) {
        Intent intent = new Intent(context, AutoStartService.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AutoStartService started");

        checkAndStartService();

        // Stop the service after checking
        stopSelf(startId);

        return START_NOT_STICKY;
    }

    private void checkAndStartService() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean autoStartEnabled = prefs.getBoolean(KEY_AUTO_START, false);

        if (!autoStartEnabled) {
            Log.d(TAG, "Auto-start is disabled");
            return;
        }

        if (!IMQSNativeExecutor.isServiceAvailable()) {
            Log.w(TAG, "IMQSNative not available");
            return;
        }

        // Check if already running
        if (isServiceRunning()) {
            Log.d(TAG, "Service already running");
            return;
        }

        // Start the service
        startFakeRootService();
    }

    private boolean isServiceRunning() {
        try {
            String output = RootExecutor.getInstance().executeAndGetOutput("pidof fakeroot_server");
            return !output.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void startFakeRootService() {
        try {
            String apkPath = getPackageManager()
                    .getApplicationInfo(getPackageName(), 0)
                    .sourceDir;

            String startCommand = "CLASSPATH='" + apkPath + "' " +
                    "app_process /system/bin " +
                    "--nice-name=fakeroot_server " +
                    "com.fakeroot.server.FakeRootService &";

            RootExecutor.getInstance().executeShell(startCommand);
            Log.i(TAG, "FakeRoot service started");

        } catch (Exception e) {
            Log.e(TAG, "Failed to start FakeRoot service", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

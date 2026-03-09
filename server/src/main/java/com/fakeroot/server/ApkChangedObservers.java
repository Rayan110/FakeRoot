package com.fakeroot.server;

import android.os.FileObserver;

import com.fakeroot.server.util.Logger;

import java.io.File;

/**
 * Observes changes to the manager APK.
 */
public class ApkChangedObservers {

    private static final Logger LOGGER = new Logger("ApkChangedObservers");

    public static void start(String apkPath, Runnable onApkRemoved) {
        LOGGER.i("Started watching APK: %s", apkPath);
    }

    public static void stop() {
        // Cleanup
    }
}

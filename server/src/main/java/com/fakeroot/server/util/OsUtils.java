package com.fakeroot.server.util;

import android.system.Os;

/**
 * OS utilities for FakeRoot server.
 */
public class OsUtils {
    public static int getUid() {
        return Os.getuid();
    }

    public static int getPid() {
        return Os.getpid();
    }

    public static String getSELinuxContext() {
        try {
            return android.os.SELinux.getContext();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

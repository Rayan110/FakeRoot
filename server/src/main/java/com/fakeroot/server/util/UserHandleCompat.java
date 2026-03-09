package com.fakeroot.server.util;

/**
 * User handle compatibility utilities.
 */
public class UserHandleCompat {
    public static int getAppId(int uid) {
        return uid % 100000;
    }

    public static int getUserId(int uid) {
        return uid / 100000;
    }
}

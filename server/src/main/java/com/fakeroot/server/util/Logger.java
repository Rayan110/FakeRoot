package com.fakeroot.server.util;

import android.util.Log;

/**
 * Simple logger for FakeRoot server.
 */
public class Logger {
    private final String tag;

    public Logger(String tag) {
        this.tag = tag;
    }

    public void v(String msg) {
        Log.v(tag, msg);
    }

    public void v(String format, Object... args) {
        Log.v(tag, String.format(format, args));
    }

    public void d(String msg) {
        Log.d(tag, msg);
    }

    public void d(String format, Object... args) {
        Log.d(tag, String.format(format, args));
    }

    public void i(String msg) {
        Log.i(tag, msg);
    }

    public void i(String format, Object... args) {
        Log.i(tag, String.format(format, args));
    }

    public void w(String msg) {
        Log.w(tag, msg);
    }

    public void w(String format, Object... args) {
        Log.w(tag, String.format(format, args));
    }

    public void w(Throwable tr, String msg) {
        Log.w(tag, msg, tr);
    }

    public void e(String msg) {
        Log.e(tag, msg);
    }

    public void e(String format, Object... args) {
        Log.e(tag, String.format(format, args));
    }

    public void e(Throwable tr, String msg) {
        Log.e(tag, msg, tr);
    }

    public void e(Throwable tr, String format, Object... args) {
        Log.e(tag, String.format(format, args), tr);
    }
}

package com.fakeroot.server;

import android.os.Build;
import android.os.RemoteException;

import com.fakeroot.server.util.Logger;

/**
 * Handles sending binder to applications.
 */
public class BinderSender {

    private static final Logger LOGGER = new Logger("BinderSender");

    public static void register(FakeRootService service) {
        LOGGER.i("BinderSender registered");
        // Process observer registration would go here
    }
}

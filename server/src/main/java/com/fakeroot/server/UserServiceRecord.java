package com.fakeroot.server;

import android.os.Bundle;

import com.fakeroot.server.api.IShizukuServiceConnection;

/**
 * Record for a user service.
 */
public class UserServiceRecord {

    public final IShizukuServiceConnection connection;
    public final Bundle options;

    public UserServiceRecord(IShizukuServiceConnection connection, Bundle options) {
        this.connection = connection;
        this.options = options;
    }
}

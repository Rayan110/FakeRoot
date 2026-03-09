package com.fakeroot.server;

import android.os.Bundle;
import android.os.IBinder;

import com.fakeroot.server.api.IShizukuServiceConnection;
import com.fakeroot.server.util.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages user services for FakeRoot.
 */
public class FakeRootUserServiceManager extends UserServiceManager {

    private static final Logger LOGGER = new Logger("FakeRootUserServiceManager");

    private final Map<String, UserServiceRecord> services = new HashMap<>();

    @Override
    public int addUserService(IShizukuServiceConnection conn, Bundle options, int callingApiVersion) {
        if (conn == null) {
            return -1;
        }

        String key = generateKey(conn);
        if (services.containsKey(key)) {
            return 0;
        }

        UserServiceRecord record = new UserServiceRecord(conn, options);
        services.put(key, record);

        LOGGER.i("addUserService: key=%s", key);
        return 0;
    }

    @Override
    public int removeUserService(IShizukuServiceConnection conn, Bundle options) {
        if (conn == null) {
            return -1;
        }

        String key = generateKey(conn);
        UserServiceRecord removed = services.remove(key);

        if (removed != null) {
            LOGGER.i("removeUserService: key=%s", key);
            return 0;
        }
        return -1;
    }

    @Override
    public void attachUserService(IBinder binder, Bundle options) {
        LOGGER.i("attachUserService");
    }

    @Override
    public void removeUserServicesForPackage(String packageName) {
        LOGGER.i("removeUserServicesForPackage: %s", packageName);
    }

    private String generateKey(IShizukuServiceConnection conn) {
        return conn.asBinder().toString();
    }
}

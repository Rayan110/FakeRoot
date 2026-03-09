package com.fakeroot.server;

import android.os.Bundle;
import android.os.IBinder;

import com.fakeroot.server.api.IShizukuServiceConnection;

/**
 * Base class for user service management.
 */
public abstract class UserServiceManager {

    public abstract int addUserService(IShizukuServiceConnection conn, Bundle options, int callingApiVersion);

    public abstract int removeUserService(IShizukuServiceConnection conn, Bundle options);

    public abstract void attachUserService(IBinder binder, Bundle options);

    public abstract void removeUserServicesForPackage(String packageName);
}

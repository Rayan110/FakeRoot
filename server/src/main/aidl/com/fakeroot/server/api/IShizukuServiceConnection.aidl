// IShizukuServiceConnection.aidl
package com.fakeroot.server.api;

import android.os.Bundle;

interface IShizukuServiceConnection {
    void connected(IBinder binder);
}

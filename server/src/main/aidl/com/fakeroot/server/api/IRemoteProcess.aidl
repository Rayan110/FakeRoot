// IRemoteProcess.aidl
package com.fakeroot.server.api;

import android.os.ParcelFileDescriptor;

interface IRemoteProcess {
    ParcelFileDescriptor getOutputStream();
    ParcelFileDescriptor getInputStream();
    ParcelFileDescriptor getErrorStream();
    int waitFor();
    int exitValue();
    void destroy();
    boolean alive();
    boolean waitForTimeout(long timeout, String unit);
}

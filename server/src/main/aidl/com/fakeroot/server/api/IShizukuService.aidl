// IShizukuService.aidl
package com.fakeroot.server.api;

import android.os.Bundle;
import android.os.IBinder;
import com.fakeroot.server.api.IRemoteProcess;
import com.fakeroot.server.api.IShizukuApplication;
import com.fakeroot.server.api.IShizukuServiceConnection;
import android.content.Intent;

interface IShizukuService {
    int getVersion();
    int getUid();
    int checkPermission(String permission);
    String getSELinuxContext();
    String getSystemProperty(String name, String defaultValue);
    void setSystemProperty(String name, String value);

    IRemoteProcess newProcess(in String[] cmd, in String[] env, String dir);
    void exit();

    int addUserService(IShizukuServiceConnection conn, in Bundle options);
    int removeUserService(IShizukuServiceConnection conn, in Bundle options);
    void attachUserService(IBinder binder, in Bundle options);

    void attachApplication(IShizukuApplication application, in Bundle args);

    boolean checkSelfPermission();
    void requestPermission(int requestCode);
    boolean shouldShowRequestPermissionRationale();

    void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, in Bundle data);
    void dispatchPackageChanged(in Intent intent);
    boolean isHidden(int uid);

    int getFlagsForUid(int uid, int mask);
    void updateFlagsForUid(int uid, int mask, int value);
}

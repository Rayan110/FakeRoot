package com.fakeroot.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.ddm.DdmHandleAppName;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.annotation.NonNull;

import com.fakeroot.core.IMQSNativeExecutor;
import com.fakeroot.server.util.Logger;
import com.fakeroot.server.util.UserHandleCompat;

/**
 * FakeRoot Service - Main service that provides root command execution via IMQSNative.
 */
public class FakeRootService extends Service<FakeRootUserServiceManager, FakeRootClientManager, FakeRootConfigManager> {

    private static final Logger LOGGER = new Logger("FakeRootService");

    public static final String MANAGER_APPLICATION_ID = "com.fakeroot.manager";

    public static void main(String[] args) {
        DdmHandleAppName.setAppName("fakeroot_server", 0);

        Looper.prepareMainLooper();
        new FakeRootService();
        Looper.loop();
    }

    private static void waitSystemService(String name) {
        while (ServiceManager.getService(name) == null) {
            try {
                LOGGER.i("service " + name + " is not started, wait 1s.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.w(e.getMessage(), e);
            }
        }
    }

    private final int managerAppId;

    public FakeRootService() {
        LOGGER.i("starting FakeRoot server...");

        waitSystemService("package");
        waitSystemService(Context.ACTIVITY_SERVICE);

        ApplicationInfo ai = getManagerApplicationInfo();
        if (ai == null) {
            LOGGER.e("Manager app not found, exiting...");
            System.exit(10);
        }

        managerAppId = ai != null ? ai.uid : 0;

        checkIMQSNativeAvailable();
    }

    private void checkIMQSNativeAvailable() {
        if (!IMQSNativeExecutor.isServiceAvailable()) {
            LOGGER.w("IMQSNative service is not available! FakeRoot requires MIUI.");
        } else {
            LOGGER.i("IMQSNative service is available.");
        }
    }

    public static ApplicationInfo getManagerApplicationInfo() {
        try {
            return (ApplicationInfo) Class.forName("android.app.ApplicationPackageManager")
                    .getMethod("getApplicationInfo", String.class, int.class)
                    .invoke(null, MANAGER_APPLICATION_ID, 0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public FakeRootUserServiceManager onCreateUserServiceManager() {
        return new FakeRootUserServiceManager();
    }

    @Override
    public FakeRootClientManager onCreateClientManager() {
        return new FakeRootClientManager(getConfigManager());
    }

    @Override
    public FakeRootConfigManager onCreateConfigManager() {
        return new FakeRootConfigManager();
    }

    @Override
    public boolean checkCallerManagerPermission(String func, int callingUid, int callingPid) {
        return UserHandleCompat.getAppId(callingUid) == managerAppId;
    }

    @Override
    public void showPermissionConfirmation(int requestCode, @NonNull ClientRecord clientRecord, int callingUid, int callingPid, int userId) {
        LOGGER.i("showPermissionConfirmation: uid=%d, pid=%d, package=%s",
                callingUid, callingPid, clientRecord.packageName);

        // For now, auto-deny - in real implementation would launch UI
        clientRecord.dispatchRequestPermissionResult(requestCode, false);
    }
}

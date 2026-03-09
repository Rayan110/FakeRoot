package com.fakeroot.server;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.system.Os;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import com.fakeroot.core.IMQSNativeExecutor;
import com.fakeroot.core.RootCommand;
import com.fakeroot.server.api.IRemoteProcess;
import com.fakeroot.server.api.IMQSNativeProcessHolder;
import com.fakeroot.server.api.IShizukuApplication;
import com.fakeroot.server.api.IShizukuService;
import com.fakeroot.server.api.IShizukuServiceConnection;
import com.fakeroot.server.util.Logger;
import com.fakeroot.server.util.OsUtils;
import com.fakeroot.server.util.UserHandleCompat;

/**
 * Base Service class for FakeRoot.
 *
 * This overrides newProcess to use IMQSNative for command execution.
 */
public abstract class Service<
        UserServiceMgr extends UserServiceManager,
        ClientMgr extends ClientManager<ConfigMgr>,
        ConfigMgr extends ConfigManager> extends IShizukuService.Stub {

    protected static final Logger LOGGER = new Logger("FakeRootService");

    private final UserServiceMgr userServiceManager;
    private final ConfigMgr configManager;
    private final ClientMgr clientManager;

    public Service() {
        userServiceManager = onCreateUserServiceManager();
        configManager = onCreateConfigManager();
        clientManager = onCreateClientManager();
    }

    public abstract UserServiceMgr onCreateUserServiceManager();
    public abstract ClientMgr onCreateClientManager();
    public abstract ConfigMgr onCreateConfigManager();

    public final UserServiceMgr getUserServiceManager() {
        return userServiceManager;
    }

    public final ClientMgr getClientManager() {
        return clientManager;
    }

    public ConfigMgr getConfigManager() {
        return configManager;
    }

    public boolean checkCallerManagerPermission(String func, int callingUid, int callingPid) {
        return false;
    }

    public final void enforceManagerPermission(String func) {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingPid == Os.getpid()) {
            return;
        }

        if (checkCallerManagerPermission(func, callingUid, callingPid)) {
            return;
        }

        String msg = "Permission Denial: " + func + " from pid=" + callingPid + " is not manager";
        LOGGER.w(msg);
        throw new SecurityException(msg);
    }

    public boolean checkCallerPermission(String func, int callingUid, int callingPid, @Nullable ClientRecord clientRecord) {
        return false;
    }

    public final void enforceCallingPermission(String func) {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingUid == OsUtils.getUid()) {
            return;
        }

        ClientRecord clientRecord = clientManager.findClient(callingUid, callingPid);

        if (checkCallerPermission(func, callingUid, callingPid, clientRecord)) {
            return;
        }

        if (clientRecord == null) {
            String msg = "Permission Denial: " + func + " from pid=" + callingPid + " is not an attached client";
            LOGGER.w(msg);
            throw new SecurityException(msg);
        }

        if (!clientRecord.allowed) {
            String msg = "Permission Denial: " + func + " from pid=" + callingPid + " requires permission";
            LOGGER.w(msg);
            throw new SecurityException(msg);
        }
    }

    @Override
    public final int getVersion() {
        enforceCallingPermission("getVersion");
        return 1;
    }

    @Override
    public final int getUid() {
        enforceCallingPermission("getUid");
        return Os.getuid();
    }

    @Override
    public final int checkPermission(String permission) throws RemoteException {
        enforceCallingPermission("checkPermission");
        // Simple implementation
        return 0;
    }

    @Override
    public final String getSELinuxContext() {
        enforceCallingPermission("getSELinuxContext");
        return OsUtils.getSELinuxContext();
    }

    @Override
    public final String getSystemProperty(String name, String defaultValue) {
        enforceCallingPermission("getSystemProperty");
        try {
            return android.os.SystemProperties.get(name, defaultValue);
        } catch (Throwable tr) {
            throw new IllegalStateException(tr.getMessage());
        }
    }

    @Override
    public final void setSystemProperty(String name, String value) {
        enforceCallingPermission("setSystemProperty");
        try {
            android.os.SystemProperties.set(name, value);
        } catch (Throwable tr) {
            throw new IllegalStateException(tr.getMessage());
        }
    }

    @Override
    public final int removeUserService(IShizukuServiceConnection conn, Bundle options) {
        enforceCallingPermission("removeUserService");
        return userServiceManager.removeUserService(conn, options);
    }

    @Override
    public final int addUserService(IShizukuServiceConnection conn, Bundle options) {
        enforceCallingPermission("addUserService");

        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        int callingApiVersion = 1;

        ClientRecord clientRecord = clientManager.findClient(callingUid, callingPid);
        if (clientRecord != null) {
            callingApiVersion = clientRecord.apiVersion;
        }
        return userServiceManager.addUserService(conn, options, callingApiVersion);
    }

    @Override
    public void attachUserService(IBinder binder, Bundle options) {
        userServiceManager.attachUserService(binder, options);
    }

    @Override
    public final boolean checkSelfPermission() {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingUid == OsUtils.getUid() || callingPid == OsUtils.getPid()) {
            return true;
        }

        return clientManager.requireClient(callingUid, callingPid).allowed;
    }

    @Override
    public final void requestPermission(int requestCode) {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        int userId = UserHandleCompat.getUserId(callingUid);

        if (callingUid == OsUtils.getUid() || callingPid == OsUtils.getPid()) {
            return;
        }

        ClientRecord clientRecord = clientManager.requireClient(callingUid, callingPid);

        if (clientRecord.allowed) {
            clientRecord.dispatchRequestPermissionResult(requestCode, true);
            return;
        }

        ConfigPackageEntry entry = configManager.find(callingUid);
        if (entry != null && entry.isDenied()) {
            clientRecord.dispatchRequestPermissionResult(requestCode, false);
            return;
        }

        showPermissionConfirmation(requestCode, clientRecord, callingUid, callingPid, userId);
    }

    public abstract void showPermissionConfirmation(
            int requestCode, @NonNull ClientRecord clientRecord, int callingUid, int callingPid, int userId);

    @Override
    public final boolean shouldShowRequestPermissionRationale() {
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();

        if (callingUid == OsUtils.getUid() || callingPid == OsUtils.getPid()) {
            return true;
        }

        clientManager.requireClient(callingUid, callingPid);

        ConfigPackageEntry entry = configManager.find(callingUid);
        return entry != null && entry.isDenied();
    }

    /**
     * Execute a command with root privileges using IMQSNative.
     */
    @Override
    public final IRemoteProcess newProcess(String[] cmd, String[] env, String dir) {
        enforceCallingPermission("newProcess");

        LOGGER.d("newProcess: uid=%d, cmd=%s, env=%s, dir=%s",
                Binder.getCallingUid(), Arrays.toString(cmd), Arrays.toString(env), dir);

        if (!IMQSNativeExecutor.isServiceAvailable()) {
            throw new IllegalStateException("IMQSNative service not available. This only works on MIUI devices.");
        }

        StringBuilder commandBuilder = new StringBuilder();
        if (dir != null && !dir.isEmpty()) {
            commandBuilder.append("cd \"").append(dir).append("\" && ");
        }
        for (String part : cmd) {
            if (commandBuilder.length() > 0 && !commandBuilder.toString().endsWith("&& ")) {
                commandBuilder.append(" ");
            }
            if (part.contains(" ")) {
                commandBuilder.append("\"").append(part).append("\"");
            } else {
                commandBuilder.append(part);
            }
        }

        String fullCommand = commandBuilder.toString();

        ClientRecord clientRecord = clientManager.findClient(Binder.getCallingUid(), Binder.getCallingPid());
        IBinder token = clientRecord != null ? clientRecord.client.asBinder() : null;

        return new IMQSNativeProcessHolder(fullCommand, env, token);
    }

    @Override
    public void exit() {
        enforceManagerPermission("exit");
        LOGGER.i("exit");
        System.exit(0);
    }

    @Override
    public void attachApplication(IShizukuApplication application, Bundle args) {
        if (application == null || args == null) {
            return;
        }

        String requestPackageName = args.getString("packageName");
        if (requestPackageName == null) {
            return;
        }
        int apiVersion = args.getInt("apiVersion", 1);

        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();

        LOGGER.d("attachApplication: %s %d %d", requestPackageName, callingUid, callingPid);

        if (clientManager.findClient(callingUid, callingPid) == null) {
            synchronized (this) {
                clientManager.addClient(callingUid, callingPid, application, requestPackageName, apiVersion);
            }
        }

        Bundle reply = new Bundle();
        reply.putInt("serverUid", OsUtils.getUid());
        reply.putInt("serverVersion", 1);

        try {
            application.bindApplication(reply);
        } catch (Throwable e) {
            LOGGER.w(e, "attachApplication");
        }
    }

    @Override
    public void dispatchPermissionConfirmationResult(int requestUid, int requestPid, int requestCode, Bundle data) throws RemoteException {
        // To be implemented by subclass
    }

    @Override
    public void dispatchPackageChanged(android.content.Intent intent) throws RemoteException {
        // Not used
    }

    @Override
    public boolean isHidden(int uid) throws RemoteException {
        return false;
    }

    @Override
    public int getFlagsForUid(int uid, int mask) {
        return 0;
    }

    @Override
    public void updateFlagsForUid(int uid, int mask, int value) throws RemoteException {
        configManager.update(uid, null, mask, value);
    }

    @CallSuper
    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return super.onTransact(code, data, reply, flags);
    }
}

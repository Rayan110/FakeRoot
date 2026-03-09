package com.fakeroot.server;

import java.util.List;

import com.fakeroot.server.api.IShizukuApplication;

/**
 * Base class for client management.
 */
public abstract class ClientManager<ConfigMgr extends ConfigManager> {

    public abstract ClientRecord addClient(int uid, int pid, IShizukuApplication application, String packageName, int apiVersion);

    public abstract ClientRecord findClient(int uid, int pid);

    public abstract List<ClientRecord> findClients(int uid);

    public abstract ClientRecord requireClient(int uid, int pid);

    public abstract void removeClient(int uid, int pid);
}

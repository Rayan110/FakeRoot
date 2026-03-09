package com.fakeroot.server;

import com.fakeroot.server.api.IShizukuApplication;
import com.fakeroot.server.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages connected clients for FakeRoot.
 */
public class FakeRootClientManager extends ClientManager<FakeRootConfigManager> {

    private static final Logger LOGGER = new Logger("FakeRootClientManager");

    private final FakeRootConfigManager configManager;
    private final Map<Integer, List<ClientRecord>> clientsByUid = new HashMap<>();

    public FakeRootClientManager(FakeRootConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public ClientRecord addClient(int uid, int pid, IShizukuApplication application, String packageName, int apiVersion) {
        ClientRecord record = new ClientRecord(uid, pid, application, packageName, apiVersion);

        synchronized (this) {
            ConfigPackageEntry entry = configManager.find(uid);
            if (entry != null) {
                record.allowed = entry.isAllowed();
            }

            clientsByUid.computeIfAbsent(uid, k -> new ArrayList<>()).add(record);
        }

        LOGGER.i("addClient: uid=%d, pid=%d, package=%s, apiVersion=%d, allowed=%s",
                uid, pid, packageName, apiVersion, record.allowed);

        return record;
    }

    @Override
    public ClientRecord findClient(int uid, int pid) {
        synchronized (this) {
            List<ClientRecord> records = clientsByUid.get(uid);
            if (records != null) {
                for (ClientRecord record : records) {
                    if (record.pid == pid) {
                        return record;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<ClientRecord> findClients(int uid) {
        synchronized (this) {
            List<ClientRecord> records = clientsByUid.get(uid);
            return records != null ? new ArrayList<>(records) : new ArrayList<>();
        }
    }

    @Override
    public ClientRecord requireClient(int uid, int pid) {
        ClientRecord record = findClient(uid, pid);
        if (record == null) {
            throw new IllegalStateException("Client not found for uid=" + uid + ", pid=" + pid);
        }
        return record;
    }

    @Override
    public void removeClient(int uid, int pid) {
        synchronized (this) {
            List<ClientRecord> records = clientsByUid.get(uid);
            if (records != null) {
                records.removeIf(r -> r.pid == pid);
                if (records.isEmpty()) {
                    clientsByUid.remove(uid);
                }
            }
        }
        LOGGER.i("removeClient: uid=%d, pid=%d", uid, pid);
    }
}

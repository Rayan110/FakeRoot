package com.fakeroot.server;

import android.os.Bundle;

import com.fakeroot.server.api.IShizukuApplication;
import com.fakeroot.server.util.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Record for a connected client.
 */
public class ClientRecord {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    public final long id;
    public final int uid;
    public final int pid;
    public final IShizukuApplication client;
    public final String packageName;
    public final int apiVersion;

    public boolean allowed = false;

    public ClientRecord(int uid, int pid, IShizukuApplication client, String packageName, int apiVersion) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.uid = uid;
        this.pid = pid;
        this.client = client;
        this.packageName = packageName;
        this.apiVersion = apiVersion;
    }

    public void dispatchRequestPermissionResult(int requestCode, boolean granted) {
        if (client == null) {
            return;
        }

        try {
            client.dispatchRequestPermissionResult(requestCode, granted);
        } catch (Exception e) {
            Logger logger = new Logger("ClientRecord");
            logger.w(e, "dispatchRequestPermissionResult");
        }
    }

    @Override
    public String toString() {
        return "ClientRecord{" +
                "id=" + id +
                ", uid=" + uid +
                ", pid=" + pid +
                ", packageName='" + packageName + '\'' +
                ", apiVersion=" + apiVersion +
                ", allowed=" + allowed +
                '}';
    }
}

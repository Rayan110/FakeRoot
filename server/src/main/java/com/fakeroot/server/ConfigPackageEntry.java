package com.fakeroot.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry in the configuration for a package/UID.
 */
public class ConfigPackageEntry {

    public final int uid;
    public int flags;
    public final List<String> packages = new ArrayList<>();

    public ConfigPackageEntry(int uid, int flags) {
        this.uid = uid;
        this.flags = flags;
    }

    public boolean isAllowed() {
        return (flags & ConfigManager.FLAG_ALLOWED) != 0;
    }

    public boolean isDenied() {
        return (flags & ConfigManager.FLAG_DENIED) != 0;
    }

    public void setAllowed(boolean allowed) {
        if (allowed) {
            flags = (flags & ~ConfigManager.FLAG_DENIED) | ConfigManager.FLAG_ALLOWED;
        } else {
            flags = (flags & ~ConfigManager.FLAG_ALLOWED) | ConfigManager.FLAG_DENIED;
        }
    }
}

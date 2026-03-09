package com.fakeroot.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for configuration management.
 */
public abstract class ConfigManager {

    public static final int MASK_PERMISSION = 1;
    public static final int FLAG_ALLOWED = 1;
    public static final int FLAG_DENIED = 2;

    public abstract ConfigPackageEntry find(int uid);

    public abstract void update(int uid, List<String> packages, int mask, int value);
}

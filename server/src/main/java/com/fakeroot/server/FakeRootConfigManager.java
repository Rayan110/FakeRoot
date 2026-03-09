package com.fakeroot.server;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fakeroot.server.util.Logger;

/**
 * Manages configuration for FakeRoot.
 *
 * Stores permission decisions and other settings.
 */
public class FakeRootConfigManager extends ConfigManager {

    private static final Logger LOGGER = new Logger("FakeRootConfigManager");

    private static final String CONFIG_FILE = "fakeroot_config.json";

    private final File configFile;
    private final Map<Integer, ConfigPackageEntry> entries = new HashMap<>();

    public FakeRootConfigManager() {
        File configDir = new File(Environment.getDataDirectory(), "misc/fakeroot");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        this.configFile = new File(configDir, CONFIG_FILE);
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            byte[] data = new byte[(int) configFile.length()];
            fis.read(data);
            String json = new String(data, "UTF-8");

            JSONObject root = new JSONObject(json);
            JSONArray packagesArray = root.optJSONArray("packages");
            if (packagesArray != null) {
                for (int i = 0; i < packagesArray.length(); i++) {
                    JSONObject pkgObj = packagesArray.getJSONObject(i);
                    int uid = pkgObj.getInt("uid");
                    int flags = pkgObj.getInt("flags");
                    JSONArray packages = pkgObj.optJSONArray("packages");

                    ConfigPackageEntry entry = new ConfigPackageEntry(uid, flags);
                    if (packages != null) {
                        for (int j = 0; j < packages.length(); j++) {
                            entry.packages.add(packages.getString(j));
                        }
                    }
                    entries.put(uid, entry);
                }
            }

            LOGGER.i("Loaded config with %d entries", entries.size());
        } catch (IOException | JSONException e) {
            LOGGER.w(e, "Failed to load config");
        }
    }

    private void saveConfig() {
        try {
            JSONObject root = new JSONObject();
            JSONArray packagesArray = new JSONArray();

            for (ConfigPackageEntry entry : entries.values()) {
                JSONObject pkgObj = new JSONObject();
                pkgObj.put("uid", entry.uid);
                pkgObj.put("flags", entry.flags);

                JSONArray packages = new JSONArray();
                for (String pkg : entry.packages) {
                    packages.put(pkg);
                }
                pkgObj.put("packages", packages);

                packagesArray.put(pkgObj);
            }

            root.put("packages", packagesArray);

            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                fos.write(root.toString().getBytes("UTF-8"));
            }

            LOGGER.d("Saved config with %d entries", entries.size());
        } catch (IOException | JSONException e) {
            LOGGER.w(e, "Failed to save config");
        }
    }

    @Override
    public ConfigPackageEntry find(int uid) {
        return entries.get(uid);
    }

    @Override
    public void update(int uid, java.util.List<String> packages, int mask, int value) {
        ConfigPackageEntry entry = entries.get(uid);
        if (entry == null) {
            entry = new ConfigPackageEntry(uid, 0);
            entries.put(uid, entry);
        }

        if (packages != null) {
            entry.packages.addAll(packages);
        }

        // Update flags with mask
        entry.flags = (entry.flags & ~mask) | (value & mask);

        saveConfig();
        LOGGER.i("Updated config for uid=%d, flags=%d", uid, entry.flags);
    }
}

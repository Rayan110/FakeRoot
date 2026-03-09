package com.fakeroot.manager.authorization

import android.content.Context
import android.os.Bundle

/**
 * Manages authorization for FakeRoot.
 */
class AuthorizationManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: AuthorizationManager? = null

        fun getInstance(context: Context): AuthorizationManager {
            return instance ?: synchronized(this) {
                instance ?: AuthorizationManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Dispatch permission result to the service.
     */
    fun dispatchPermissionResult(uid: Int, pid: Int, requestCode: Int, result: Bundle) {
        // This would typically use a binder to communicate with the server
        // For now, we'll use a file-based approach or shared preferences
        val prefs = context.getSharedPreferences("fakeroot_permissions", Context.MODE_PRIVATE)
        val key = "uid_$uid"
        val allowed = result.getBoolean("allowed", false)
        prefs.edit().putBoolean(key, allowed).apply()
    }

    /**
     * Check if an app has permission.
     */
    fun hasPermission(uid: Int): Boolean {
        val prefs = context.getSharedPreferences("fakeroot_permissions", Context.MODE_PRIVATE)
        return prefs.getBoolean("uid_$uid", false)
    }

    /**
     * Grant permission to an app.
     */
    fun grantPermission(uid: Int, packageName: String) {
        val prefs = context.getSharedPreferences("fakeroot_permissions", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("uid_$uid", true)
            .putString("uid_${uid}_package", packageName)
            .apply()
    }

    /**
     * Revoke permission from an app.
     */
    fun revokePermission(uid: Int) {
        val prefs = context.getSharedPreferences("fakeroot_permissions", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("uid_$uid")
            .remove("uid_${uid}_package")
            .apply()
    }

    /**
     * Get all authorized apps.
     */
    fun getAuthorizedApps(): List<Pair<Int, String>> {
        val prefs = context.getSharedPreferences("fakeroot_permissions", Context.MODE_PRIVATE)
        val apps = mutableListOf<Pair<Int, String>>()

        for ((key, value) in prefs.all) {
            if (key.startsWith("uid_") && !key.contains("_package") && value == true) {
                val uid = key.removePrefix("uid_").toIntOrNull()
                val packageName = prefs.getString("uid_${uid}_package", null)
                if (uid != null && packageName != null) {
                    apps.add(uid to packageName)
                }
            }
        }

        return apps
    }
}

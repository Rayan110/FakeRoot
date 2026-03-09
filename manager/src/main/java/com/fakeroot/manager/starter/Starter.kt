package com.fakeroot.manager.starter

import android.content.Context
import com.fakeroot.manager.util.ShizukuHelper

/**
 * Handles starting the FakeRoot service.
 */
object Starter {

    private const val TAG = "Starter"

    /**
     * Check if IMQSNative is available for starting the service.
     */
    fun isIMQSNativeAvailable(): Boolean {
        return ShizukuHelper.checkIMQSNativeAvailable()
    }

    /**
     * Start the FakeRoot service via IMQSNative.
     *
     * @param context Application context
     * @return true if start command was sent successfully
     */
    fun startViaIMQSNative(context: Context): StarterResult {
        if (!ShizukuHelper.isShizukuAvailable()) {
            return StarterResult.Error("Shizuku is not running")
        }

        if (!ShizukuHelper.hasShizukuPermission()) {
            return StarterResult.Error("Shizuku permission not granted")
        }

        if (!isIMQSNativeAvailable()) {
            return StarterResult.Error("IMQSNative service not available")
        }

        val apkPath = getManagerApkPath(context)
        if (apkPath == null) {
            return StarterResult.Error("Could not find manager APK")
        }

        val startCommand = buildStartCommand(apkPath)

        return try {
            val result = ShizukuHelper.executeCommand(startCommand, 30)
            if (result.isSuccess) {
                StarterResult.Success
            } else {
                StarterResult.Error("Command failed: ${result.error}")
            }
        } catch (e: Exception) {
            StarterResult.Error("Exception: ${e.message}")
        }
    }

    /**
     * Start the FakeRoot service via ADB.
     *
     * This is the traditional method that requires user to run:
     * adb shell sh /sdcard/Android/data/com.fakeroot.manager/files/start.sh
     */
    fun generateAdbStartScript(context: Context): String? {
        val apkPath = getManagerApkPath(context) ?: return null
        return buildString {
            appendLine("#!/system/bin/sh")
            appendLine("# FakeRoot start script")
            appendLine()
            appendLine(buildStartCommand(apkPath))
        }
    }

    /**
     * Build the start command for the server.
     */
    private fun buildStartCommand(apkPath: String): String {
        return buildString {
            append("CLASSPATH='$apkPath' ")
            append("app_process /system/bin ")
            append("--nice-name=fakeroot_server ")
            append("com.fakeroot.server.FakeRootService")
        }
    }

    /**
     * Get the path to the manager APK.
     */
    private fun getManagerApkPath(context: Context): String? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            appInfo.sourceDir
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if the service is currently running.
     */
    fun isServiceRunning(): Boolean {
        return try {
            // Check if fakeroot_server process exists
            val result = ShizukuHelper.executeCommand("pidof fakeroot_server", 5)
            result.isSuccess && result.output.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Stop the FakeRoot service.
     */
    fun stopService(): StarterResult {
        return try {
            val result = ShizukuHelper.executeCommand("pkill -f fakeroot_server", 10)
            if (result.isSuccess) {
                StarterResult.Success
            } else {
                StarterResult.Error("Failed to stop service")
            }
        } catch (e: Exception) {
            StarterResult.Error("Exception: ${e.message}")
        }
    }

    /**
     * Result of a starter operation.
     */
    sealed class StarterResult {
        object Success : StarterResult()
        data class Error(val message: String) : StarterResult()
    }
}

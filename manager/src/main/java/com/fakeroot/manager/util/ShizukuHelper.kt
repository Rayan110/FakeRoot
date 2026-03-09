package com.fakeroot.manager.util

import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import androidx.fragment.app.FragmentActivity
import moe.shizuku.server.IRemoteProcess
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Helper class for executing commands via Shizuku.
 * Shizuku provides shell-level privileges without root.
 */
object ShizukuHelper {

    private const val REQUEST_CODE = 1001

    /**
     * Check if Shizuku is installed and running.
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if we have permission to use Shizuku.
     */
    fun hasShizukuPermission(): Boolean {
        return try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.isPreV11()) {
                    false
                } else {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Request Shizuku permission.
     */
    fun requestShizukuPermission(activity: FragmentActivity) {
        try {
            if (Shizuku.pingBinder() && !Shizuku.isPreV11()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(REQUEST_CODE)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get IShizukuService interface from Shizuku binder.
     */
    private fun getShizukuService(): IShizukuService? {
        return try {
            val binder = Shizuku.getBinder() ?: return null
            IShizukuService.Stub.asInterface(binder)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Execute a shell command via Shizuku.
     */
    fun executeCommand(command: String, timeout: Int = 60): CommandResult {
        return try {
            if (!Shizuku.pingBinder()) {
                return CommandResult(-1, "", "Shizuku is not running")
            }

            if (!hasShizukuPermission()) {
                return CommandResult(-1, "", "Shizuku permission not granted")
            }

            val service = getShizukuService()
                ?: return CommandResult(-1, "", "Failed to get Shizuku service")

            // Build the command array
            val cmd = arrayOf("sh", "-c", command)

            // Call newProcess via IShizukuService
            val remoteProcess = service.newProcess(cmd, null, null)
                ?: return CommandResult(-1, "", "newProcess returned null")

            readProcessOutput(remoteProcess, timeout)

        } catch (e: Exception) {
            CommandResult(-1, "", e.message ?: "Unknown error: ${e.javaClass.simpleName}")
        }
    }

    /**
     * Execute command via IMQSNative with root privileges.
     * This writes output to a file and reads it back.
     * Uses base64 encoding to avoid quote issues in s16 parameter.
     */
    fun executeViaIMQSNative(command: String, timeout: Int = 60): CommandResult {
        val timestamp = System.currentTimeMillis()
        val outputFile = "/sdcard/.fr_out_$timestamp.txt"
        val scriptFile = "/sdcard/.fr_cmd_$timestamp.sh"

        return try {
            // Write command to a script file via Shizuku
            // This avoids quote issues in IMQSNative s16 parameter
            val script = "#!/system/bin/sh\n$command"
            val writeResult = executeCommand("echo '$script' > $scriptFile && chmod 755 $scriptFile", 5)
            if (!writeResult.isSuccess) {
                return CommandResult(-1, "", "Failed to write script: ${writeResult.error}")
            }

            // Execute the script via IMQSNative
            // Use simple path without quotes or special characters
            val serviceCmd = "service call miui.mqsas.IMQSNative 21 i32 1 s16 sh i32 1 s16 $scriptFile s16 $outputFile i32 $timeout"

            val callResult = executeCommand(serviceCmd, timeout)

            // Wait for output file
            Thread.sleep(300)

            // Read output
            val output = readFile(outputFile)

            // Clean up
            executeCommand("rm -f $scriptFile $outputFile", 5)

            CommandResult(0, output, "")

        } catch (e: Exception) {
            CommandResult(-1, "", e.message ?: "Unknown error")
        }
    }

    /**
     * Read output from a remote process with timeout.
     */
    private fun readProcessOutput(remoteProcess: IRemoteProcess, timeoutSeconds: Int): CommandResult {
        val output = StringBuilder()
        val error = StringBuilder()

        try {
            // Read stdout in a separate thread
            val stdoutThread = Thread {
                try {
                    val inputPfd = remoteProcess.inputStream
                    if (inputPfd != null) {
                        val inputStream = ParcelFileDescriptor.AutoCloseInputStream(inputPfd)
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            output.append(line).append("\n")
                        }
                        reader.close()
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
            stdoutThread.start()

            // Read stderr in a separate thread
            val stderrThread = Thread {
                try {
                    val errorPfd = remoteProcess.errorStream
                    if (errorPfd != null) {
                        val errorStream = ParcelFileDescriptor.AutoCloseInputStream(errorPfd)
                        val reader = BufferedReader(InputStreamReader(errorStream))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            error.append(line).append("\n")
                        }
                        reader.close()
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
            stderrThread.start()

            // Wait for process with timeout
            val exitCode = remoteProcess.waitFor()

            // Wait for reader threads
            stdoutThread.join(timeoutSeconds * 1000L)
            stderrThread.join(timeoutSeconds * 1000L)

            return CommandResult(exitCode, output.toString().trim(), error.toString().trim())

        } catch (e: Exception) {
            return CommandResult(-1, output.toString().trim(), e.message ?: "Unknown error")
        }
    }

    /**
     * Check if IMQSNative service is available.
     * Tests by executing a simple command.
     */
    fun checkIMQSNativeAvailable(): Boolean {
        return try {
            // Try to execute a simple id command via IMQSNative
            val outputFile = "/sdcard/fakeroot_check_${System.currentTimeMillis()}.txt"
            val serviceCmd = "service call miui.mqsas.IMQSNative 21 i32 1 s16 \"id\" i32 1 s16 \"\" s16 \"$outputFile\" i32 5"

            val callResult = executeCommand(serviceCmd, 5)
            if (!callResult.isSuccess) {
                return false
            }

            // Wait for output file
            Thread.sleep(300)

            // Read output
            val output = readFile(outputFile)
            deleteFile(outputFile)

            output.contains("uid")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Read file content.
     */
    fun readFile(path: String): String {
        return try {
            val result = executeCommand("cat \"$path\"", 10)
            if (result.isSuccess) result.output else ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Delete file.
     */
    fun deleteFile(path: String): Boolean {
        return try {
            val result = executeCommand("rm -f \"$path\"", 5)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Result of a command execution.
     */
    data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String
    ) {
        val isSuccess: Boolean get() = exitCode == 0
    }
}

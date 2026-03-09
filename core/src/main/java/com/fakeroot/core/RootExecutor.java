package com.fakeroot.core;

/**
 * High-level interface for executing root commands.
 * Provides convenient methods for common operations.
 */
public class RootExecutor {

    private static RootExecutor instance;

    /**
     * Get the singleton instance of RootExecutor.
     */
    public static synchronized RootExecutor getInstance() {
        if (instance == null) {
            instance = new RootExecutor();
        }
        return instance;
    }

    private RootExecutor() {
        // Singleton
    }

    /**
     * Check if root access is available via IMQSNative.
     *
     * @return true if root is available
     */
    public boolean isRootAvailable() {
        try {
            RootCommand result = execute("id");
            return result.isSuccess() && result.getStdout().contains("uid=0");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if IMQSNative service is available.
     *
     * @return true if service is available
     */
    public boolean isServiceAvailable() {
        return IMQSNativeExecutor.isServiceAvailable();
    }

    /**
     * Execute a command with root privileges.
     *
     * @param command The command to execute
     * @return RootCommand result
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public RootCommand execute(String command) throws IMQSNativeExecutor.IMQSNativeException {
        return IMQSNativeExecutor.exec(command);
    }

    /**
     * Execute a command with arguments.
     *
     * @param command The command
     * @param args    The arguments
     * @return RootCommand result
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public RootCommand execute(String command, String args) throws IMQSNativeExecutor.IMQSNativeException {
        return IMQSNativeExecutor.exec(command, args);
    }

    /**
     * Execute a shell command (sh -c).
     *
     * @param shellCommand The full shell command
     * @return RootCommand result
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public RootCommand executeShell(String shellCommand) throws IMQSNativeExecutor.IMQSNativeException {
        return IMQSNativeExecutor.execShell(shellCommand);
    }

    /**
     * Execute a command with custom timeout.
     *
     * @param command The command
     * @param args    The arguments
     * @param timeout Timeout in seconds
     * @return RootCommand result
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public RootCommand execute(String command, String args, int timeout) throws IMQSNativeExecutor.IMQSNativeException {
        return IMQSNativeExecutor.exec(command, args, null, timeout);
    }

    /**
     * Execute a command and return stdout (throws on failure).
     *
     * @param command The command
     * @return stdout string
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public String executeAndGetOutput(String command) throws IMQSNativeExecutor.IMQSNativeException {
        RootCommand result = execute(command);
        if (!result.isSuccess()) {
            throw new IMQSNativeExecutor.IMQSNativeException(
                    "Command failed with exit code " + result.getExitCode() + ": " + result.getStderr());
        }
        return result.getStdout();
    }

    // ==================== Convenience Methods ====================

    /**
     * Read a file as root.
     *
     * @param path File path
     * @return File contents
     * @throws IMQSNativeExecutor.IMQSNativeException if reading fails
     */
    public String readFile(String path) throws IMQSNativeExecutor.IMQSNativeException {
        return executeAndGetOutput("cat \"" + path + "\"");
    }

    /**
     * Write to a file as root.
     *
     * @param path     File path
     * @param content  Content to write
     * @param append   Whether to append
     * @throws IMQSNativeExecutor.IMQSNativeException if writing fails
     */
    public void writeFile(String path, String content, boolean append) throws IMQSNativeExecutor.IMQSNativeException {
        String op = append ? ">>" : ">";
        executeShell("echo '" + content.replace("'", "'\\''") + "' " + op + " \"" + path + "\"");
    }

    /**
     * Delete a file as root.
     *
     * @param path File path
     * @throws IMQSNativeExecutor.IMQSNativeException if deletion fails
     */
    public void deleteFile(String path) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("rm -f \"" + path + "\"");
    }

    /**
     * Create a directory as root.
     *
     * @param path Directory path
     * @throws IMQSNativeExecutor.IMQSNativeException if creation fails
     */
    public void mkdir(String path) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("mkdir -p \"" + path + "\"");
    }

    /**
     * Change file permissions as root.
     *
     * @param path     File path
     * @param mode     Permission mode (e.g., "755")
     * @throws IMQSNativeExecutor.IMQSNativeException if chmod fails
     */
    public void chmod(String path, String mode) throws IMQSNativeExecutor.IMQSNativeException {
        execute("chmod", mode + " \"" + path + "\"");
    }

    /**
     * Change file owner as root.
     *
     * @param path  File path
     * @param owner Owner (user:group)
     * @throws IMQSNativeExecutor.IMQSNativeException if chown fails
     */
    public void chown(String path, String owner) throws IMQSNativeExecutor.IMQSNativeException {
        execute("chown", owner + " \"" + path + "\"");
    }

    /**
     * Check if a file exists.
     *
     * @param path File path
     * @return true if file exists
     */
    public boolean fileExists(String path) {
        try {
            executeShell("[ -e \"" + path + "\" ]");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * List files in a directory.
     *
     * @param path Directory path
     * @return Array of file names
     * @throws IMQSNativeExecutor.IMQSNativeException if listing fails
     */
    public String[] listFiles(String path) throws IMQSNativeExecutor.IMQSNativeException {
        String output = executeAndGetOutput("ls -1 \"" + path + "\"");
        if (output.isEmpty()) {
            return new String[0];
        }
        return output.split("\n");
    }

    /**
     * Get current user info (should show root).
     *
     * @return Output of 'id' command
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public String whoami() throws IMQSNativeExecutor.IMQSNativeException {
        return executeAndGetOutput("id");
    }

    /**
     * Install an APK as root.
     *
     * @param apkPath Path to APK file
     * @throws IMQSNativeExecutor.IMQSNativeException if installation fails
     */
    public void installApk(String apkPath) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("pm install -r \"" + apkPath + "\"");
    }

    /**
     * Uninstall a package as root.
     *
     * @param packageName Package name
     * @throws IMQSNativeExecutor.IMQSNativeException if uninstallation fails
     */
    public void uninstallPackage(String packageName) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("pm uninstall " + packageName);
    }

    /**
     * Force stop a package as root.
     *
     * @param packageName Package name
     * @throws IMQSNativeExecutor.IMQSNativeException if execution fails
     */
    public void forceStopPackage(String packageName) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("am force-stop " + packageName);
    }

    /**
     * Mount filesystem as read-write.
     *
     * @param mountPoint Mount point
     * @throws IMQSNativeExecutor.IMQSNativeException if mount fails
     */
    public void mountRw(String mountPoint) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("mount -o remount,rw \"" + mountPoint + "\"");
    }

    /**
     * Mount filesystem as read-only.
     *
     * @param mountPoint Mount point
     * @throws IMQSNativeExecutor.IMQSNativeException if mount fails
     */
    public void mountRo(String mountPoint) throws IMQSNativeExecutor.IMQSNativeException {
        executeShell("mount -o remount,ro \"" + mountPoint + "\"");
    }
}

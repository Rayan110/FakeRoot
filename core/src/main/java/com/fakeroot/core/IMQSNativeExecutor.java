package com.fakeroot.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Executor for running commands via MIUI's IMQSNative service.
 *
 * IMQSNative is a hidden MIUI system service that runs with root privileges.
 * This class provides a safe wrapper to execute commands through this service.
 *
 * Service call format:
 * service call miui.mqsas.IMQSNative 21 i32 1 s16 "command" i32 1 s16 "args" s16 "outputPath" i32 timeout
 */
public class IMQSNativeExecutor {

    private static final String TAG = "IMQSNativeExecutor";

    /**
     * The service name for IMQSNative in MIUI systems.
     */
    public static final String SERVICE_NAME = "miui.mqsas.IMQSNative";

    /**
     * Transaction code for command execution.
     */
    public static final int TRANSACTION_EXEC = 21;

    /**
     * Default timeout in seconds for command execution.
     */
    public static final int DEFAULT_TIMEOUT = 60;

    /**
     * Check if IMQSNative service is available on this device.
     * Uses a simple test command to verify the service actually works.
     *
     * @return true if the service is available and working
     */
    public static boolean isServiceAvailable() {
        try {
            // Try to execute a simple command to verify the service works
            // This is more reliable than "service check" which may not work on all devices
            RootCommand result = exec("id", "", "/sdcard/fakeroot_check.txt", 5);
            return result.isSuccess() && result.getStdout().contains("uid");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Execute a command with root privileges via IMQSNative.
     *
     * @param command    The command to execute (e.g., "id", "sh")
     * @param args       The arguments for the command (can be empty string)
     * @param outputPath The path to write output (if null, uses temp file)
     * @param timeout    Timeout in seconds
     * @return RootCommand containing the result
     * @throws IMQSNativeException if execution fails
     */
    public static RootCommand exec(String command, String args, String outputPath, int timeout)
            throws IMQSNativeException {

        if (command == null || command.isEmpty()) {
            throw new IMQSNativeException("Command cannot be null or empty");
        }

        // Prepare output path
        String actualOutputPath = outputPath;
        boolean useTempFile = false;
        if (actualOutputPath == null || actualOutputPath.isEmpty()) {
            actualOutputPath = "/sdcard/fakeroot_" + System.currentTimeMillis() + ".txt";
            useTempFile = true;
        }

        // Prepare args
        String actualArgs = args != null ? args : "";

        // Ensure timeout is valid
        int actualTimeout = timeout > 0 ? timeout : DEFAULT_TIMEOUT;

        try {
            long startTime = System.currentTimeMillis();

            // Build the service call command
            // Format: service call miui.mqsas.IMQSNative 21 i32 1 s16 "cmd" i32 1 s16 "args" s16 "output" i32 timeout
            String serviceCommand = String.format(
                    "service call %s %d i32 1 s16 \"%s\" i32 1 s16 \"%s\" s16 \"%s\" i32 %d",
                    SERVICE_NAME, TRANSACTION_EXEC, command, actualArgs, actualOutputPath, actualTimeout
            );

            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", serviceCommand});
            process.waitFor();

            long executionTime = System.currentTimeMillis() - startTime;

            // Read output from file
            String stdout = "";
            String stderr = "";

            File outputFile = new File(actualOutputPath);

            // Wait for output file to be created (with timeout)
            int waitCount = 0;
            while (!outputFile.exists() && waitCount < 50) {
                Thread.sleep(100);
                waitCount++;
            }

            if (outputFile.exists()) {
                try {
                    stdout = readFile(outputFile);
                } catch (IOException e) {
                    stderr = "Failed to read output: " + e.getMessage();
                }

                // Clean up temp file
                if (useTempFile) {
                    try {
                        outputFile.delete();
                    } catch (Exception ignored) {}
                }
            } else {
                stderr = "Output file was not created";
            }

            int exitCode = process.exitValue();
            return new RootCommand(exitCode == 0 ? 0 : -1, stdout, stderr, executionTime);

        } catch (Exception e) {
            throw new IMQSNativeException("Exception: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a simple command with default timeout.
     *
     * @param command The command to execute
     * @return RootCommand containing the result
     * @throws IMQSNativeException if execution fails
     */
    public static RootCommand exec(String command) throws IMQSNativeException {
        return exec(command, "", null, DEFAULT_TIMEOUT);
    }

    /**
     * Execute a command with arguments and default timeout.
     *
     * @param command The command to execute
     * @param args    The arguments
     * @return RootCommand containing the result
     * @throws IMQSNativeException if execution fails
     */
    public static RootCommand exec(String command, String args) throws IMQSNativeException {
        return exec(command, args, null, DEFAULT_TIMEOUT);
    }

    /**
     * Execute a shell command string (parsed by sh -c).
     *
     * @param shellCommand The full shell command (e.g., "ls -la /data")
     * @param timeout      Timeout in seconds
     * @return RootCommand containing the result
     * @throws IMQSNativeException if execution fails
     */
    public static RootCommand execShell(String shellCommand, int timeout) throws IMQSNativeException {
        // Use sh -c to execute the full command
        return exec("sh", "-c '" + shellCommand + "'", null, timeout);
    }

    /**
     * Execute a shell command string with default timeout.
     *
     * @param shellCommand The full shell command
     * @return RootCommand containing the result
     * @throws IMQSNativeException if execution fails
     */
    public static RootCommand execShell(String shellCommand) throws IMQSNativeException {
        return execShell(shellCommand, DEFAULT_TIMEOUT);
    }

    /**
     * Read file contents as string.
     */
    private static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        // Remove trailing newline
        if (content.length() > 0 && content.charAt(content.length() - 1) == '\n') {
            content.setLength(content.length() - 1);
        }
        return content.toString();
    }

    /**
     * Exception thrown when IMQSNative execution fails.
     */
    public static class IMQSNativeException extends Exception {
        public IMQSNativeException(String message) {
            super(message);
        }

        public IMQSNativeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

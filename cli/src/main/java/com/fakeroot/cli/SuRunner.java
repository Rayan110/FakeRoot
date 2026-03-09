package com.fakeroot.cli;

/**
 * Java wrapper for the su command execution.
 * This can be called from Android applications.
 */
public class SuRunner {

    /**
     * Execute a command as root and return the output.
     *
     * @param command The command to execute
     * @return The output of the command
     */
    public static String exec(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Check if root is available.
     *
     * @return true if root is available
     */
    public static boolean isRootAvailable() {
        try {
            String result = exec("id");
            return result.contains("uid=0");
        } catch (Exception e) {
            return false;
        }
    }
}

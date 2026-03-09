package com.fakeroot.core;

/**
 * Result of a command executed with root privileges via IMQSNative.
 */
public class RootCommand {

    private final int exitCode;
    private final String stdout;
    private final String stderr;
    private final long executionTime;
    private final boolean success;

    public RootCommand(int exitCode, String stdout, String stderr, long executionTime) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
        this.executionTime = executionTime;
        this.success = exitCode == 0;
    }

    /**
     * Returns the exit code of the command.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Returns the standard output of the command.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * Returns the standard error of the command.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * Returns the execution time in milliseconds.
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Returns true if the command exited with code 0.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns stdout if success, otherwise stderr.
     */
    public String getOutput() {
        return success ? stdout : stderr;
    }

    @Override
    public String toString() {
        return "RootCommand{" +
                "exitCode=" + exitCode +
                ", success=" + success +
                ", executionTime=" + executionTime + "ms" +
                ", stdout='" + (stdout != null && stdout.length() > 100 ? stdout.substring(0, 100) + "..." : stdout) + '\'' +
                ", stderr='" + (stderr != null && stderr.length() > 100 ? stderr.substring(0, 100) + "..." : stderr) + '\'' +
                '}';
    }
}

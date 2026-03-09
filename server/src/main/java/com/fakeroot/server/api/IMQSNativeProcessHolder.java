package com.fakeroot.server.api;

import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import com.fakeroot.core.IMQSNativeExecutor;
import com.fakeroot.core.RootCommand;
import com.fakeroot.server.util.Logger;

/**
 * Process holder that uses IMQSNative for command execution.
 */
public class IMQSNativeProcessHolder extends IRemoteProcess.Stub {

    private static final Logger LOGGER = new Logger("IMQSNativeProcessHolder");

    private final String command;
    private final String[] env;
    private final IBinder token;

    private RootCommand result;
    private boolean executed = false;

    public IMQSNativeProcessHolder(String command, String[] env, IBinder token) {
        this.command = command;
        this.env = env;
        this.token = token;

        if (token != null) {
            try {
                DeathRecipient deathRecipient = () -> {
                    try {
                        if (alive()) {
                            destroy();
                            LOGGER.i("destroy process because the owner is dead");
                        }
                    } catch (Throwable e) {
                        LOGGER.w(e, "failed to destroy process");
                    }
                };
                token.linkToDeath(deathRecipient, 0);
            } catch (Throwable e) {
                LOGGER.w(e, "linkToDeath");
            }
        }
    }

    private synchronized void execute() {
        if (executed) {
            return;
        }
        executed = true;

        try {
            LOGGER.d("Executing via IMQSNative: %s", command);
            result = IMQSNativeExecutor.execShell(command, 60);
        } catch (Exception e) {
            LOGGER.e(e, "Failed to execute command via IMQSNative");
            result = new RootCommand(-1, "", e.getMessage(), 0);
        }
    }

    @Override
    public ParcelFileDescriptor getOutputStream() {
        return null; // Not supported
    }

    @Override
    public ParcelFileDescriptor getInputStream() {
        execute();
        return null; // Would need to pipe output
    }

    @Override
    public ParcelFileDescriptor getErrorStream() {
        execute();
        return null;
    }

    @Override
    public int waitFor() {
        execute();
        return result != null ? result.getExitCode() : -1;
    }

    @Override
    public int exitValue() {
        if (!executed) {
            throw new IllegalThreadStateException("Process has not exited");
        }
        return result != null ? result.getExitCode() : -1;
    }

    @Override
    public void destroy() {
        executed = true;
    }

    @Override
    public boolean alive() throws RemoteException {
        return !executed;
    }

    @Override
    public boolean waitForTimeout(long timeout, String unit) throws RemoteException {
        execute();
        return true;
    }
}

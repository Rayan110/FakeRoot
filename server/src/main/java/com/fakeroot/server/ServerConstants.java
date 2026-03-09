package com.fakeroot.server;

/**
 * Constants for FakeRoot server.
 */
public class ServerConstants {

    /**
     * The package name of the manager application.
     */
    public static final String MANAGER_APPLICATION_ID = "com.fakeroot.manager";

    /**
     * The permission required to use FakeRoot API.
     */
    public static final String PERMISSION = "com.fakeroot.permission.API";

    /**
     * Action for requesting permission.
     */
    public static final String REQUEST_PERMISSION_ACTION = "com.fakeroot.action.REQUEST_PERMISSION";

    /**
     * Exit code when manager app is not found.
     */
    public static final int MANAGER_APP_NOT_FOUND = 10;

    /**
     * Binder transaction code for getApplications.
     */
    public static final int BINDER_TRANSACTION_getApplications = 1000;
}

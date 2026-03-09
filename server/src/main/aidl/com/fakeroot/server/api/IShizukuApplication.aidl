// IShizukuApplication.aidl
package com.fakeroot.server.api;

import android.os.Bundle;

interface IShizukuApplication {
    void bindApplication(in Bundle args);
    void dispatchRequestPermissionResult(int requestCode, boolean granted);
}

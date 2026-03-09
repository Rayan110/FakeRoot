package com.fakeroot.manager.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fakeroot.core.IMQSNativeExecutor
import com.fakeroot.manager.starter.IMQSStarter
import com.fakeroot.manager.starter.Starter

/**
 * Boot receiver for the FakeRoot Manager.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed received")

            // Check and attempt auto-start
            val starter = IMQSStarter(context)
            if (starter.isAutoStartEnabled) {
                if (Starter.isIMQSNativeAvailable()) {
                    Log.i(TAG, "Attempting auto-start via IMQSNative")
                    Starter.startViaIMQSNative(context)
                } else {
                    Log.w(TAG, "IMQSNative not available, cannot auto-start")
                }
            }
        }
    }
}

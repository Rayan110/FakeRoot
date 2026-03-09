package com.fakeroot.manager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import rikka.shizuku.Shizuku

/**
 * FakeRoot Manager Application
 */
class FakeRootApplication : Application() {

    companion object {
        const val CHANNEL_ID_SERVICE = "fakeroot_service"
        const val CHANNEL_ID_NOTIFICATION = "fakeroot_notification"

        lateinit var instance: FakeRootApplication
            private set
    }

    // Shizuku binder received listener
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        // Binder received, check permission
        checkShizukuPermission()
    }

    // Shizuku binder dead listener
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        // Binder dead
    }

    // Shizuku permission result listener
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        // Permission result
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannels()
        initShizuku()
    }

    private fun initShizuku() {
        try {
            // Add Shizuku listeners
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkShizukuPermission() {
        try {
            if (Shizuku.pingBinder() && !Shizuku.isPreV11()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    // Permission will be requested when needed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Service channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                getString(R.string.channel_service),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_service_desc)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // Notification channel
            val notifyChannel = NotificationChannel(
                CHANNEL_ID_NOTIFICATION,
                getString(R.string.channel_notification),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_notification_desc)
            }
            notificationManager.createNotificationChannel(notifyChannel)
        }
    }

    /**
     * Called when the FakeRoot service binder is received.
     */
    fun onBinderReceived(binder: IBinder) {
        // Store or use the binder
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

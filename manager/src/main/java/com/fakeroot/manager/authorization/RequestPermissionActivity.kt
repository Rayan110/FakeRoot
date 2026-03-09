package com.fakeroot.manager.authorization

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fakeroot.manager.R

/**
 * Activity for requesting permission from the user.
 */
class RequestPermissionActivity : AppCompatActivity() {

    private var requestCode: Int = 0
    private var callingUid: Int = 0
    private var callingPid: Int = 0
    private var applicationInfo: ApplicationInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permission)

        // Get intent extras
        requestCode = intent.getIntExtra("requestCode", 0)
        callingUid = intent.getIntExtra("uid", 0)
        callingPid = intent.getIntExtra("pid", 0)
        applicationInfo = intent.getParcelableExtra("applicationInfo")

        // Setup UI
        setupUI()
    }

    private fun setupUI() {
        val appIcon = findViewById<ImageView>(R.id.app_icon)
        val appName = findViewById<TextView>(R.id.app_name)
        val message = findViewById<TextView>(R.id.permission_message)
        val allowButton = findViewById<Button>(R.id.btn_allow)
        val denyButton = findViewById<Button>(R.id.btn_deny)
        val allowAlwaysButton = findViewById<Button>(R.id.btn_allow_always)

        // Set app info
        applicationInfo?.let { info ->
            appName.text = packageManager.getApplicationLabel(info)
            appIcon.setImageDrawable(packageManager.getApplicationIcon(info))
        }

        message.text = getString(R.string.permission_request_message,
            applicationInfo?.let { packageManager.getApplicationLabel(it) } ?: "Unknown")

        // Button handlers
        allowButton.setOnClickListener {
            sendResult(true, true)
            finish()
        }

        allowAlwaysButton.setOnClickListener {
            sendResult(true, false)
            finish()
        }

        denyButton.setOnClickListener {
            sendResult(false, true)
            finish()
        }
    }

    private fun sendResult(allowed: Boolean, onetime: Boolean) {
        // Send result back to the service
        // This would typically use a binder or broadcast
        // For now, we'll just set the result
        val result = Bundle().apply {
            putBoolean("allowed", allowed)
            putBoolean("onetime", onetime)
        }

        // Notify the service
        AuthorizationManager.getInstance(this).dispatchPermissionResult(
            callingUid, callingPid, requestCode, result
        )
    }
}

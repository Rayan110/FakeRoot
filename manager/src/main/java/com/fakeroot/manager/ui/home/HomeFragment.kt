package com.fakeroot.manager.ui.home

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fakeroot.manager.R
import com.fakeroot.manager.util.ShizukuHelper
import com.google.android.material.button.MaterialButton
import rikka.shizuku.Shizuku

class HomeFragment : Fragment() {

    private lateinit var tvStatus: TextView
    private lateinit var tvStatusDetail: TextView
    private lateinit var btnToggleService: MaterialButton
    private lateinit var tvDeviceModel: TextView
    private lateinit var tvAndroidVersion: TextView
    private lateinit var tvMiuiVersion: TextView
    private lateinit var tvImqsStatus: TextView
    private lateinit var tvRootStatus: TextView
    private lateinit var tvShizukuStatus: TextView
    private lateinit var tvInstallStatus: TextView
    private lateinit var btnInstallSu: MaterialButton

    private var isServiceRunning = false
    private var isImqsAvailable = false
    private var isShizukuAvailable = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        activity?.runOnUiThread {
            checkStatus()
        }
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        activity?.runOnUiThread {
            checkStatus()
        }
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        activity?.runOnUiThread {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                checkStatus()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadDeviceInfo()
        setupShizukuListeners()
    }

    private fun initViews(view: View) {
        tvStatus = view.findViewById(R.id.tv_status)
        tvStatusDetail = view.findViewById(R.id.tv_status_detail)
        btnToggleService = view.findViewById(R.id.btn_toggle_service)
        tvDeviceModel = view.findViewById(R.id.tv_device_model)
        tvAndroidVersion = view.findViewById(R.id.tv_android_version)
        tvMiuiVersion = view.findViewById(R.id.tv_miui_version)
        tvImqsStatus = view.findViewById(R.id.tv_imqs_status)
        tvRootStatus = view.findViewById(R.id.tv_root_status)
        tvShizukuStatus = view.findViewById(R.id.tv_shizuku_status)
        tvInstallStatus = view.findViewById(R.id.tv_install_status)
        btnInstallSu = view.findViewById(R.id.btn_install_su)

        btnToggleService.setOnClickListener {
            toggleService()
        }

        btnInstallSu.setOnClickListener {
            installSu()
        }
    }

    private fun setupShizukuListeners() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDeviceInfo() {
        // Device model
        tvDeviceModel.text = "${Build.MANUFACTURER} ${Build.MODEL}"

        // Android version
        tvAndroidVersion.text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        // MIUI version
        val miuiVersion = getSystemProperty("ro.miui.ui.version.name")
        tvMiuiVersion.text = if (miuiVersion.isNotEmpty()) "MIUI $miuiVersion" else getString(R.string.not_miui)
    }

    private fun checkStatus() {
        Thread {
            // Check Shizuku
            isShizukuAvailable = ShizukuHelper.isShizukuAvailable()
            val hasShizukuPermission = ShizukuHelper.hasShizukuPermission()

            // Check IMQSNative via Shizuku
            if (isShizukuAvailable && hasShizukuPermission) {
                isImqsAvailable = ShizukuHelper.checkIMQSNativeAvailable()

                // Check if service is running via IMQSNative (root context)
                if (isImqsAvailable) {
                    try {
                        val result = ShizukuHelper.executeViaIMQSNative("pidof fakeroot_server", 5)
                        isServiceRunning = result.isSuccess && result.output.isNotEmpty()
                    } catch (e: Exception) {
                        isServiceRunning = false
                    }
                } else {
                    isServiceRunning = false
                }
            } else {
                isImqsAvailable = false
                isServiceRunning = false
            }

            activity?.runOnUiThread {
                updateUI()
            }
        }.start()
    }

    private fun updateUI() {
        // Shizuku status
        if (isShizukuAvailable) {
            val hasPermission = ShizukuHelper.hasShizukuPermission()
            tvShizukuStatus.text = if (hasPermission) {
                getString(R.string.available)
            } else {
                getString(R.string.permission_required)
            }
            tvShizukuStatus.setTextColor(
                requireContext().getColor(
                    if (hasPermission) R.color.status_running else R.color.status_not_running
                )
            )
        } else {
            tvShizukuStatus.text = getString(R.string.not_available)
            tvShizukuStatus.setTextColor(requireContext().getColor(R.color.status_error))
        }

        // IMQS Status
        tvImqsStatus.text = if (isImqsAvailable) {
            getString(R.string.available)
        } else {
            getString(R.string.not_available)
        }
        tvImqsStatus.setTextColor(
            requireContext().getColor(
                if (isImqsAvailable) R.color.status_running else R.color.status_error
            )
        )

        // Root status - check via Shizuku
        if (isShizukuAvailable && ShizukuHelper.hasShizukuPermission() && isImqsAvailable) {
            Thread {
                try {
                    val result = ShizukuHelper.executeViaIMQSNative("id", 5)
                    val hasRoot = result.output.contains("uid=0")

                    activity?.runOnUiThread {
                        tvRootStatus.text = if (hasRoot) {
                            getString(R.string.root_available)
                        } else {
                            getString(R.string.root_unavailable)
                        }
                        tvRootStatus.setTextColor(
                            requireContext().getColor(
                                if (hasRoot) R.color.status_running else R.color.status_not_running
                            )
                        )
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        tvRootStatus.text = getString(R.string.root_unavailable)
                        tvRootStatus.setTextColor(requireContext().getColor(R.color.status_error))
                    }
                }
            }.start()
        } else {
            tvRootStatus.text = getString(R.string.not_available)
            tvRootStatus.setTextColor(requireContext().getColor(R.color.status_error))
        }

        // Service status & button
        if (!isShizukuAvailable) {
            tvStatus.text = getString(R.string.shizuku_not_running)
            tvStatusDetail.text = getString(R.string.shizuku_not_running_summary)
            btnToggleService.text = getString(R.string.action_start_service)
            btnToggleService.isEnabled = false
        } else if (!ShizukuHelper.hasShizukuPermission()) {
            tvStatus.text = getString(R.string.shizuku_permission_required)
            tvStatusDetail.text = getString(R.string.shizuku_permission_required_summary)
            btnToggleService.text = getString(R.string.grant_permission)
            btnToggleService.isEnabled = true
        } else if (!isImqsAvailable) {
            tvStatus.text = getString(R.string.status_not_available)
            tvStatusDetail.text = getString(R.string.status_not_available_summary)
            btnToggleService.text = getString(R.string.action_start_service)
            btnToggleService.isEnabled = false
        } else if (isServiceRunning) {
            tvStatus.text = getString(R.string.status_running)
            tvStatusDetail.text = getString(R.string.status_running_summary)
            btnToggleService.text = getString(R.string.action_stop_service)
            btnToggleService.isEnabled = true
        } else {
            tvStatus.text = getString(R.string.status_not_running)
            tvStatusDetail.text = getString(R.string.status_not_running_summary)
            btnToggleService.text = getString(R.string.action_start_service)
            btnToggleService.isEnabled = true
        }
    }

    private fun toggleService() {
        if (!ShizukuHelper.hasShizukuPermission()) {
            // Request Shizuku permission
            try {
                Shizuku.requestPermission(1001)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.shizuku_not_running, Toast.LENGTH_LONG).show()
            }
            return
        }

        btnToggleService.isEnabled = false
        btnToggleService.text = if (isServiceRunning) {
            getString(R.string.stopping_service)
        } else {
            getString(R.string.starting_service)
        }

        Thread {
            try {
                if (isServiceRunning) {
                    // Stop service via IMQSNative
                    ShizukuHelper.executeViaIMQSNative("pkill -f fakeroot_server", 5)
                    Thread.sleep(500)
                } else {
                    // Start service via IMQSNative (with root privileges)
                    val apkPath = requireContext().packageManager
                        .getApplicationInfo(requireContext().packageName, 0).sourceDir

                    val startCommand = "CLASSPATH='$apkPath' app_process /system/bin " +
                            "--nice-name=fakeroot_server com.fakeroot.server.FakeRootService"

                    ShizukuHelper.executeViaIMQSNative(startCommand, 10)
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Recheck status
            checkStatus()
        }.start()
    }

    private fun installSu() {
        if (!ShizukuHelper.hasShizukuPermission() || !isImqsAvailable) {
            Toast.makeText(requireContext(), R.string.status_not_available, Toast.LENGTH_SHORT).show()
            return
        }

        btnInstallSu.isEnabled = false
        btnInstallSu.text = getString(R.string.installing)

        Thread {
            try {
                // su script content - use ${'$'} to escape $ in Kotlin string
                val suScript = """#!/system/bin/sh
if [ "${'$'}1" = "-c" ] && [ -n "${'$'}2" ]; then
    OUT=/sdcard/.fr_out_${'$'}${'$'}
    TMP=/sdcard/.fr_cmd_${'$'}${'$'}
    echo "${'$'}2" > ${'$'}TMP
    service call miui.mqsas.IMQSNative 21 i32 1 s16 "sh" i32 1 s16 "${'$'}TMP" s16 "${'$'}OUT" i32 60 >/dev/null 2>&1
    sleep 0.5
    [ -f ${'$'}OUT ] && cat ${'$'}OUT
    rm -f ${'$'}OUT ${'$'}TMP
else
    echo "Usage: su -c command"
fi"""

                // Step 1: Write su script to /sdcard
                val suScriptPath = "/sdcard/.fr_su_script.sh"
                val cpScriptPath = "/sdcard/.fr_cp_script.sh"

                // Write su script
                val escapedScript = suScript.replace("'", "'\"'\"'")
                ShizukuHelper.executeCommand("echo '$escapedScript' > $suScriptPath", 5)

                // Write copy script
                ShizukuHelper.executeCommand("echo 'cat $suScriptPath > /data/local/tmp/su && chmod 755 /data/local/tmp/su' > $cpScriptPath", 5)

                Thread.sleep(200)

                // Step 2: Execute copy script via IMQSNative (root privileges)
                ShizukuHelper.executeCommand("service call miui.mqsas.IMQSNative 21 i32 1 s16 sh i32 1 s16 $cpScriptPath s16 /sdcard/.fr_install_result.txt i32 10", 10)

                Thread.sleep(500)

                // Step 3: Verify
                val verifyResult = ShizukuHelper.executeCommand("ls -la /data/local/tmp/su", 5)
                val fileExists = verifyResult.output.contains("/data/local/tmp/su")

                // Clean up temp files
                ShizukuHelper.executeCommand("rm -f $suScriptPath $cpScriptPath /sdcard/.fr_install_result.txt", 5)

                activity?.runOnUiThread {
                    if (fileExists) {
                        tvInstallStatus.text = getString(R.string.su_location)
                        Toast.makeText(requireContext(), R.string.install_success, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "${getString(R.string.install_failed)}: ${verifyResult.output}", Toast.LENGTH_LONG).show()
                    }
                    btnInstallSu.isEnabled = true
                    btnInstallSu.text = getString(R.string.install_su)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "${getString(R.string.install_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
                    btnInstallSu.isEnabled = true
                    btnInstallSu.text = getString(R.string.install_su)
                }
            }
        }.start()
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java)
            method.invoke(null, key) as String
        } catch (e: Exception) {
            ""
        }
    }

    override fun onResume() {
        super.onResume()
        checkStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

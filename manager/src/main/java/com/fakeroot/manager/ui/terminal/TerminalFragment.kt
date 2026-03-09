package com.fakeroot.manager.ui.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fakeroot.manager.R
import com.fakeroot.manager.util.ShizukuHelper
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class TerminalFragment : Fragment() {

    private lateinit var tvOutput: TextView
    private lateinit var etCommand: EditText
    private lateinit var btnExecute: MaterialButton

    private val outputBuilder = StringBuilder()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_terminal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOutput = view.findViewById(R.id.tv_output)
        etCommand = view.findViewById(R.id.et_command)
        btnExecute = view.findViewById(R.id.btn_execute)

        appendOutput(getString(R.string.terminal_welcome), OutputType.INFO)
        appendOutput(getString(R.string.terminal_hint), OutputType.HINT)

        // Check Shizuku status
        if (!ShizukuHelper.isShizukuAvailable()) {
            appendOutput("⚠️ Shizuku is not running. Please start Shizuku first.", OutputType.ERROR)
        } else if (!ShizukuHelper.hasShizukuPermission()) {
            appendOutput("⚠️ Shizuku permission not granted. Please grant permission in Home page.", OutputType.ERROR)
        } else {
            appendOutput("✅ Shizuku is ready. Commands will be executed via IMQSNative with root privileges.", OutputType.INFO)
        }

        btnExecute.setOnClickListener {
            executeCommand()
        }

        etCommand.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                executeCommand()
                true
            } else {
                false
            }
        }
    }

    private fun executeCommand() {
        // Check Shizuku
        if (!ShizukuHelper.isShizukuAvailable()) {
            Toast.makeText(requireContext(), R.string.shizuku_not_running, Toast.LENGTH_LONG).show()
            return
        }

        if (!ShizukuHelper.hasShizukuPermission()) {
            Toast.makeText(requireContext(), R.string.shizuku_permission_required, Toast.LENGTH_LONG).show()
            return
        }

        val command = etCommand.text.toString().trim()
        if (command.isEmpty()) return

        // Clear input
        etCommand.text.clear()

        // Show command
        appendOutput("# $command", OutputType.COMMAND)

        // Disable button while executing
        btnExecute.isEnabled = false

        Thread {
            try {
                // Execute via IMQSNative for root access
                val result = ShizukuHelper.executeViaIMQSNative(command, 60)

                activity?.runOnUiThread {
                    if (result.output.isNotEmpty()) {
                        appendOutput(result.output, OutputType.OUTPUT)
                    }
                    if (result.error.isNotEmpty()) {
                        appendOutput(result.error, OutputType.ERROR)
                    }
                    if (result.output.isEmpty() && result.error.isEmpty()) {
                        appendOutput(getString(R.string.command_completed, result.exitCode), OutputType.INFO)
                    }
                    btnExecute.isEnabled = true
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    appendOutput("${getString(R.string.error)}: ${e.message}", OutputType.ERROR)
                    btnExecute.isEnabled = true
                }
            }
        }.start()
    }

    private fun appendOutput(text: String, type: OutputType) {
        val timestamp = dateFormat.format(Date())

        when (type) {
            OutputType.COMMAND -> {
                outputBuilder.append("\n[$timestamp] $text\n")
            }
            OutputType.OUTPUT -> {
                outputBuilder.append(text).append("\n")
            }
            OutputType.ERROR -> {
                outputBuilder.append("❌ $text\n")
            }
            OutputType.INFO -> {
                outputBuilder.append("ℹ️ $text\n")
            }
            OutputType.HINT -> {
                outputBuilder.append("💡 $text\n")
            }
        }

        tvOutput.text = outputBuilder.toString()
    }

    enum class OutputType {
        COMMAND, OUTPUT, ERROR, INFO, HINT
    }
}

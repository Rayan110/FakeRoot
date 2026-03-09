package com.fakeroot.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.fakeroot.manager.starter.IMQSStarter

/**
 * Settings Activity for FakeRoot Manager
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Settings fragment
     */
    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // Auto-start preference
            findPreference<SwitchPreferenceCompat>("auto_start")?.setOnPreferenceChangeListener { _, newValue ->
                val starter = IMQSStarter(requireContext())
                starter.isAutoStartEnabled = newValue as Boolean
                true
            }

            // Load current values
            val starter = IMQSStarter(requireContext())
            findPreference<SwitchPreferenceCompat>("auto_start")?.isChecked = starter.isAutoStartEnabled
        }
    }
}

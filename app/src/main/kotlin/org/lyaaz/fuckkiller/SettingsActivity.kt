package org.lyaaz.fuckkiller

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    /**
     * @noinspection deprecation
     */
    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)
        try {
            @Suppress("DEPRECATION")
            getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_WORLD_READABLE)
        } catch (e: Exception) {
            getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_PRIVATE)
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, MySettingsFragment())
            .commit()
    }

    private class MySettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val maxPhantomProcesses = findPreference<EditTextPreference>("MAX_PHANTOM_PROCESSES")
            val maxCachedProcesses = findPreference<EditTextPreference>("MAX_CACHED_PROCESSES")

            listOf(maxCachedProcesses, maxPhantomProcesses).forEach {
                it?.apply {
                    summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
                    setOnBindEditTextListener { editText: EditText ->
                        editText.inputType = InputType.TYPE_CLASS_NUMBER
                    }
                }
            }
        }
    }
}

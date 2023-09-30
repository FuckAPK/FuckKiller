package org.baiyu.fuckkiller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import de.robv.android.xposed.XposedBridge;

public class SettingsActivity extends AppCompatActivity {
    /** @noinspection deprecation*/
    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        try {
            getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_WORLD_READABLE);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new MySettingsFragment())
                .commit();
    }

    private static class MySettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            EditTextPreference MAX_PHANTOM_PROCESSES = findPreference("MAX_PHANTOM_PROCESSES");
            EditTextPreference MAX_CACHED_PROCESSES = findPreference("MAX_CACHED_PROCESSES");

            for (EditTextPreference p : new EditTextPreference[]{MAX_CACHED_PROCESSES, MAX_PHANTOM_PROCESSES}) {
                if (p != null) {
                    p.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
                    p.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
                }
            }
        }
    }
}

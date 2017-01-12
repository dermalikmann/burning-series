package de.monarchcode.m4lik.burningseries;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;

import de.monarchcode.m4lik.burningseries.util.AndroidUtility;

import static com.google.common.base.Strings.emptyToNull;

/**
 * Created by Malik (M4lik) on 12.01.2017.
 *
 * @author M4lik, mm.malik.mann@gmail.com
 */

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String category = null;
        String action = getIntent().getAction();
        if (action != null && action.startsWith("preference://"))
            category = emptyToNull(action.substring("preference://".length()));

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            fragment.setArguments(AndroidUtility.bundle("category", category));

            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            String category = getArguments().getString("category");
            if (category != null) {
                Preference root = getPreferenceManager().findPreference(category);
                if (root != null) {
                    getActivity().setTitle(root.getTitle());
                    setPreferenceScreen((PreferenceScreen) root);
                }
            }

            if (!BuildConfig.DEBUG) {
                hideDebugPreferences();
            }
        }

        private void hideDebugPreferences() { /** Maybe some time in the future... **/
            Preference pref = findPreference("prefcat_debug");
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

        }
    }
}
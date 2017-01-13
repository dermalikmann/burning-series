package de.monarchcode.m4lik.burningseries;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;

import de.monarchcode.m4lik.burningseries.util.AndroidUtility;
import de.monarchcode.m4lik.burningseries.util.PreferenceWithActionbar;

import static com.google.common.base.Strings.emptyToNull;

/**
 * Created by Malik (M4lik) on 12.01.2017.
 *
 * @author M4lik, mm.malik.mann@gmail.com
 */

public class SettingsActivity extends PreferenceWithActionbar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            EditTextPreference userpref = (EditTextPreference) getPreferenceManager().findPreference("pref_user");
            EditTextPreference sessionpref = (EditTextPreference) getPreferenceManager().findPreference("pref_session");

            userpref.setSummary(userpref.getText());
            sessionpref.setSummary(sessionpref.getText());

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

        private void hideDebugPreferences() {
            Preference pref = findPreference("prefcat_debug");
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, @NonNull Preference preference) {

            String preferenceKey = preference.getKey();
            Log.e("BS", preferenceKey);

            if ("pref_pseudo_recommend".equals(preferenceKey)) {
                String text = "Versuch mal die neue Burning Series App. https://github.com/M4lik/burning-series/releases";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Burning-Series app");
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

        }
    }
}
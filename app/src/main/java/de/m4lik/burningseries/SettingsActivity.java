package de.m4lik.burningseries;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;

import de.m4lik.burningseries.ui.PreferenceWithActionbar;
import de.m4lik.burningseries.util.AndroidUtility;

import static com.google.common.base.Strings.emptyToNull;

/**
 * Created by Malik on 12.01.2017.
 *
 * @author Malik Mann
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
            EditTextPreference versionpref = (EditTextPreference) getPreferenceManager().findPreference("pref_version");

            String version;

            Context context = getActivity().getApplicationContext();

            try {
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(
                        context.getPackageName(), 0);
                version = info.versionName + " (Build " + info.versionCode + ")";
            } catch (Exception e) {
                version = "Error getting version";
            }

            versionpref.setSummary(version);

            userpref.setSummary(userpref.getText().equals("")? "Nicht angemeldet" : userpref.getText());
            sessionpref.setSummary(sessionpref.getText().equals("")? " " : sessionpref.getText());

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

            if ("pref_psudo_debug_notification".equals(preferenceKey)) {
                Context context = getActivity().getApplicationContext();

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_stat_name);
                builder.setContentTitle("Test Benachrichtigung");
                builder.setContentText("Hallo Welt!");
                builder.setContentIntent(null);
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1337, builder.build());
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

        }
    }
}
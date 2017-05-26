package de.m4lik.burningseries.ui;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.view.MenuItem;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.BuildConfig;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.services.SyncBroadcastReceiver;
import de.m4lik.burningseries.services.ThemeHelperService;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.dialogs.UpdateDialog;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;

import static com.google.common.base.Strings.emptyToNull;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by Malik on 12.01.2017.
 *
 * @author Malik Mann
 */

public class SettingsActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme().basic);
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
    protected void injectComponent(ActivityComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static android.support.v4.app.NotificationCompat.Builder newNotificationBuilder(Context context) {
            return new android.support.v7.app.NotificationCompat.Builder(context);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Preference userpref = getPreferenceManager().findPreference("pref_user");
            Preference sessionpref = getPreferenceManager().findPreference("pref_session");
            Preference versionpref = getPreferenceManager().findPreference("pref_version");

            String version;

            Context context = getActivity().getApplicationContext();

            try {
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(
                        context.getPackageName(), 0);
                String debug = BuildConfig.DEBUG ? " DEBUG" : "";
                version = info.versionName + " (Build " + info.versionCode + debug + ")";
            } catch (Exception e) {
                version = "Error getting version";
            }

            versionpref.setSummary(version);

            userpref.setSummary(Settings.of(context).getUserName().equals("") ? "Nicht angemeldet" : Settings.of(context).getUserName());
            sessionpref.setSummary(Settings.of(context).getUserSession().equals("") ? " " : Settings.of(context).getUserSession());

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

            if ("pref_pseudo_recommend".equals(preferenceKey)) {
                String text = "Versuch mal die neue Burning Series App. https://github.com/M4lik/burning-series/releases";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Burning-Series app");
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
                return true;
            }

            if ("pref_pseudo_do_update".equals(preferenceKey)) {
                ActivityBase activity = (ActivityBase) getActivity();
                UpdateDialog.checkForUpdates(activity, true);
                return true;
            }

            if ("pref_pseudo_debug_fullscreen_video".equals(preferenceKey)) {
                Intent intent = new Intent(getActivity(), FullscreenVideoActivity.class);
                intent.putExtra("burning-series.videoURL", "https://d7467.thevideo.me:8777/lcjtbvndrgoammfvg6hvcdenexwdnu33ihjof5alluspwnezoxh7e7wpgav3ujfyemev2rarlfknfuafad7yfnhb62awu5c5gt6yfjswf3guzz2bqmvpehdcxuzigyjqqyjmlovz6vplfeb3rr2g5tfhx4ubg4p24dlr3dkmsz7ilbo4c23xw75a4uv4pmufbez6twehzidkx4nyhriby26alppmvre3fktwkcsggavnhn63stojm35noqlqir64elpbvqaygv6yc6qa3pvlihsw4m3a/v.mp4?direct=false&ua=1&vt=ogtkhqidyiexah3newi4coptgfgjoqsumikxtn53ukleggihq75m2e5sjlnf3b4h3ocljv2pwzzdximl6edsjsthtmg6rcp4oixrmnqmbzrfcatpve452loulk72prcbt4wbhh5yunyhyfblwctua3kek4meqdtnyotgtfjodxevt76vrdpbjl2oziinwbwfgn4l4fxmj6qmygltq2hdszfqqtgqcd5irbzzowy");
                startActivity(intent);
                return true;
            }

            if ("pref_pseudo_debug_update_check".equals(preferenceKey)) {
                SyncBroadcastReceiver.syncNow(getActivity());
                return true;
            }

            if ("pref_pseudo_cause_crash".equals(preferenceKey)) {
                throw new RuntimeException("Crashed on purpose!");
            }

            if ("pref_pseudo_debug_notification".equals(preferenceKey)) {
                Context context = getActivity().getApplicationContext();

                NotificationManagerCompat nm = NotificationManagerCompat.from(context);

                Notification notification = newNotificationBuilder(context)
                        .setContentTitle("Test Benachrichtigung")
                        .setContentText("Hallo Welt!")
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setCategory(android.support.v4.app.NotificationCompat.CATEGORY_RECOMMENDATION)
                        .setAutoCancel(false)
                        .build();

                nm.notify(1337, notification);
                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            if ("pref_theme".equals(key)) {
                ThemeHelperService.updateTheme(getActivity());
                AndroidUtility.recreateActivity(getActivity());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);

            super.onPause();
        }
    }
}
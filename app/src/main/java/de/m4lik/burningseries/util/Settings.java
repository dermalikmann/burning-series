package de.m4lik.burningseries.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.Themes;

/**
 * Created by Malik on 14.01.2017
 *
 * @author Malik Mann
 */

public class Settings {

    private final SharedPreferences preferences;

    public Settings(Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private Settings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public static void initialize(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    }

    public static Settings of(Context context) {
        return new Settings(context.getApplicationContext());
    }

    public static Settings of(SharedPreferences preferences) {
        return new Settings(preferences);
    }

    public SharedPreferences raw() {
        return preferences;
    }

    public SharedPreferences.Editor edit() {
        return preferences.edit();
    }


    /* Getter for settings */

    public String themeName() {
        return preferences.getString("pref_theme", Themes.BLUE.name());
    }

    public boolean isDarkTheme() {
        return themeName().contains("_DARK");
    }

    public boolean alarmOnMobile() {
        return preferences.getBoolean("pref_alarm_on_mobile_data", true);
    }

    public String getStartupView() {
        return preferences.getString("pref_startup_view", "serieslist");
    }

    public String getUserName() {
        return preferences.getString("pref_user", "");
    }

    public String getUserSession() {
        return preferences.getString("pref_session", "");
    }

    public boolean isLoggedIn() {
        return !preferences.getString("pref_session", "").equals("");
    }

    public boolean isBetaChannel() {
        return preferences.getString("pref_update_channel", "stable").equals("beta");
    }

    public boolean showCovers() {
        return preferences.getBoolean("pref_show_cover_images", true);
    }

}

package de.m4lik.burningseries.services;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Enums;

import de.m4lik.burningseries.ui.Themes;
import de.m4lik.burningseries.util.Settings;

/**
 * Created by Malik on 14.01.2017
 *
 * @author Malik Mann
 */

public final class ThemeHelperService {

    @Nullable
    private static Themes THEME = Themes.BLUE;

    private ThemeHelperService() {
    }

    @ColorRes
    public static int primaryColor() {
        return theme().primaryColor;
    }

    @ColorRes
    public static int primaryColorDark() {
        return theme().primaryColorDark;
    }

    @NonNull
    public static Themes theme() {
        return THEME != null ? THEME : Themes.BLUE;
    }

    public static void updateTheme(Context context, String name) {
        updateTheme(context, Enums.getIfPresent(Themes.class, name).or(Themes.BLUE));
    }

    public static void updateTheme(Context context, Themes theme) {
        Settings.of(context)
                .edit()
                .putString("pref_theme", theme().name())
                .apply();
        updateTheme(context);
    }

    public static void updateTheme(Context context) {
        String name = Settings.of(context).themeName();
        THEME = Enums.getIfPresent(Themes.class, name).or(Themes.ORANGE);
    }
}

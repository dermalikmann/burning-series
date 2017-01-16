package de.monarchcode.m4lik.burningseries.services;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Enums;

import de.monarchcode.m4lik.burningseries.util.Settings;

import de.monarchcode.m4lik.burningseries.ui.Themes;

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

    public static void updateTheme(Context context, Themes theme) {
        Settings.of(context)
                .edit()
                .putString("pref_theme", theme().name())
                .apply();
        updateTheme(context);
    }

    public static void updateTheme(Context context) {
        Settings settings = Settings.of(context);
        String name = settings.themeName();
        THEME = Enums.getIfPresent(Themes.class, name).or(Themes.ORANGE);
    }
}

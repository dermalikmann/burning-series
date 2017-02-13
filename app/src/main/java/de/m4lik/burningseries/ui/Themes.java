package de.m4lik.burningseries.ui;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import de.m4lik.burningseries.R;

/**
 * Created by Malik on 14.01.2017
 *
 * @author Malik Mann
 */

public enum Themes {

    BLUE(R.string.theme_blue,
            "4354a9",
            R.color.blue_primary,
            R.color.blue_primary_dark,
            R.style.BurningSeries_Blue,
            R.style.BurningSeries_Blue_NoActionBar,
            R.style.BurningSeries_Blue_NoActionBar_Fullscreen,
            R.style.BurningSeries_Blue_NoActionBar_TranslucentStatus,
            R.style.BurningSeries_Blue_NoActionBar_WhiteAccent,
            R.drawable.cover_gradient_blue
    ),

    GREEN(R.string.theme_green,
            "4354a9",
            R.color.green_primary,
            R.color.green_primary_dark,
            R.style.BurningSeries_Green,
            R.style.BurningSeries_Green_NoActionBar,
            R.style.BurningSeries_Green_NoActionBar_Fullscreen,
            R.style.BurningSeries_Green_NoActionBar_TranslucentStatus,
            R.style.BurningSeries_Green_NoActionBar_WhiteAccent,
            R.drawable.cover_gradient_green
    ),

    ORANGE(R.string.theme_orange,
            "4354a9",
            R.color.orange_primary,
            R.color.orange_primary_dark,
            R.style.BurningSeries_Orange,
            R.style.BurningSeries_Orange_NoActionBar,
            R.style.BurningSeries_Orange_NoActionBar_Fullscreen,
            R.style.BurningSeries_Orange_NoActionBar_TranslucentStatus,
            R.style.BurningSeries_Orange_NoActionBar_WhiteAccent,
            R.drawable.cover_gradient_orange
    );


    @StringRes
    public final int title;

    private final String rName;

    @ColorRes
    public final int primaryColor;

    @ColorRes
    public final int primaryColorDark;

    @StyleRes
    public final int basic;

    @StyleRes
    public final int noActionBar;

    @StyleRes
    public final int fullscreen;

    @StyleRes
    public final int translucentStatus;

    @StyleRes
    public final int whiteAccent;

    @DrawableRes
    public final int gradient;

    Themes(@StringRes int title,
           String rName,
           @ColorRes int primaryColor,
           @ColorRes int primaryColorDark,
           @StyleRes int basic,
           @StyleRes int noActionBar,
           @StyleRes int fullscreen,
           @StyleRes int translucentStatus,
           @StyleRes int whiteAccent,
           @DrawableRes int gradient) {

        this.title = title;
        this.rName = rName;
        this.primaryColor = primaryColor;
        this.primaryColorDark = primaryColorDark;
        this.basic = basic;
        this.noActionBar = noActionBar;
        this.fullscreen = fullscreen;
        this.translucentStatus = translucentStatus;
        this.whiteAccent = whiteAccent;
        this.gradient = gradient;
    }

    public String title(Context context) {
        return context.getString(title);
    }
}
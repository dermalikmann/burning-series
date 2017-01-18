package de.m4lik.burningseries.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created on 18.01.2017.
 *
 * @author Malik Mann
 */

public class Logger {

    private static FirebaseAnalytics firebaseAnalytics;

    private static final String EVENT_SERIES_SELECTED = "series_selected";
    private static final String EVENT_GENRE_SELECTED = "genre_selected";
    private static final String EVENT_LOGIN = "login";
    private static final String EVENT_LOGOUT = "logout";
    private static final String EVENT_HOSTER_TIMEOUT = "hoster_timeout";
    private static final String EVENT_VIDEO_NOT_FOUND = "video_not_found";



    private static final String SERIES_ID;
    private static final String SERIES_NAME;

    private static final String GENRE_NAME;

    private static final String HOSTER_NAME;
    private static final String VIDEO_URL;

    static {
        SERIES_ID = "seriesID";
        SERIES_NAME = "seriesName";
        GENRE_NAME = "genreName";
        HOSTER_NAME = "hosterName";
        VIDEO_URL = "videoURL";
    }

    public void seriesSelection(Context context, Integer id, String name) {
        seriesSelection(context, id.toString(), name);
    }

    public static void genreSelection(Context context, String genre) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        //bundle.putString(GENRE_ID, Integer.toString(id));
        bundle.putString(GENRE_NAME, genre);
        firebaseAnalytics.logEvent(EVENT_GENRE_SELECTED, bundle);
        Log.i("BS_FA", "Event logged: " + EVENT_SERIES_SELECTED);
    }

    public static void seriesSelection(Context context, String id, String name) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(SERIES_ID, id);
        bundle.putString(SERIES_NAME, name);
        firebaseAnalytics.logEvent(EVENT_SERIES_SELECTED, bundle);
        Log.i("BS_FA", "Event logged: " + EVENT_SERIES_SELECTED);
    }

    public static void login(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(EVENT_LOGIN, new Bundle());
    }

    public static void logout(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.logEvent(EVENT_LOGOUT, new Bundle());
    }
}

package de.m4lik.burningseries.util;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

/**
 * Created on 18.01.2017.
 *
 * @author Malik Mann
 */

public class Logger {

    private static final String EVENT_SERIES_SELECTED = "Show selected";
    private static final String EVENT_GENRE_SELECTED = "Genre selected";

    public static void warn(String msg) {
        Crashlytics.log(Log.WARN, "BS-WARN", msg);
    }

    public static void genreSelection(String genre) {
        Answers.getInstance().logCustom(new CustomEvent(EVENT_GENRE_SELECTED)
            .putCustomAttribute("Genre", genre));
        Log.i("BS-Logger", "Event logged: " + EVENT_GENRE_SELECTED);
    }

    public static void seriesSelection(Integer id, String name) {

        Answers.getInstance().logCustom(new CustomEvent(EVENT_SERIES_SELECTED)
                .putCustomAttribute("Show", name + " - " + id)
        );
        Log.i("BS-Logger", "Event logged: " + EVENT_SERIES_SELECTED);
    }
}

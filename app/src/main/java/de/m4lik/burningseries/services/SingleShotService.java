package de.m4lik.burningseries.services;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.m4lik.burningseries.util.AndroidUtility;

/**
 */
@Singleton
public class SingleShotService {
    static final int TIME_OFFSET_IN_MILLIS = (int) (Math.random() * 3600 * 1000);

    private static final String KEY_ACTIONS = "SingleShotService.actions";
    private static final String KEY_MAP_ACTIONS = "SingleShotService.mapActions";
    final SharedPreferences preferences;
    final Object lock = new Object();
    private final Gson gson = new Gson();
    Map<String, String> timeStringMap;


    @Inject
    public SingleShotService(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public synchronized boolean isFirstTime(String action) {
        synchronized (lock) {
            Set<String> actions = new HashSet<>(preferences.getStringSet(
                    KEY_ACTIONS, Collections.<String>emptySet()));

            if (actions.add(action)) {
                // store modifications
                preferences.edit().putStringSet(KEY_ACTIONS, actions).apply();
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean firstTimeInVersion(String action) {
        int version = AndroidUtility.buildNumber();
        return isFirstTime(action + "--" + version);
    }

    public boolean firstTimeToday(String action) {
        return firstTimeByTimePattern(action, "YYYY-MM-dd");
    }

    public boolean firstTimeInHour(String action) {
        return firstTimeByTimePattern(action, "YYYY-MM-dd:HH");
    }

    public boolean firstTimeByTimePattern(String action, String pattern) {
        String timeString = DateTime.now()
                .minusMillis(TIME_OFFSET_IN_MILLIS)
                .toString(DateTimeFormat.forPattern(pattern));

        return timeStringHasChanged(action, timeString);
    }

    @SuppressWarnings("unchecked")
    private boolean timeStringHasChanged(String action, String timeString) {
        synchronized (lock) {
            if (timeStringMap == null) {
                try {
                    timeStringMap = new HashMap<>(gson.fromJson(
                            preferences.getString(KEY_MAP_ACTIONS, "{}"),
                            Map.class));

                } catch (RuntimeException ignored) {
                    timeStringMap = new HashMap<>();
                }
            }

            if (timeString.equals(timeStringMap.get(action))) {
                return false;
            } else {
                timeStringMap.put(action, timeString);

                preferences.edit()
                        .putString(KEY_MAP_ACTIONS, gson.toJson(timeStringMap))
                        .apply();

                return true;
            }
        }
    }
}

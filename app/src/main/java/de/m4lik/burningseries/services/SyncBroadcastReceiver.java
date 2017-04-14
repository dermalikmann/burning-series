package de.m4lik.burningseries.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.joda.time.Hours;

import de.m4lik.burningseries.util.Settings;

import static org.joda.time.Duration.standardHours;
import static org.joda.time.Duration.standardMinutes;

/**
 */
public class SyncBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final long DEFAULT_SYNC_DELAY = standardMinutes(10).getMillis();

    private static void scheduleNextSync(Context context, long syncTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // the intent to send to our app in one hour.
        Intent nextIntent = new Intent(context, SyncBroadcastReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);

        // register a pending event.
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, syncTime, alarmIntent);
    }

    public static void syncNow(Context context) {
        Intent intent = new Intent(context, SyncBroadcastReceiver.class);
        context.sendBroadcast(intent);

        Log.d("BSBG", "Broadcast send");

        getSyncPrefs(context).edit().putLong("delay", DEFAULT_SYNC_DELAY).apply();
    }

    private static long getNextSyncTime(Context context) {
        SharedPreferences prefs = getSyncPrefs(context);

        long delay = Math.min(
                prefs.getLong("delay", DEFAULT_SYNC_DELAY),
                Hours.ONE.toStandardDuration().getMillis());

        prefs.edit().putLong("delay", 2 * delay).apply();
        return SystemClock.elapsedRealtime() + delay;
    }

    public static void scheduleNextSync(Context context) {
        scheduleNextSync(context, getNextSyncTime(context));
    }

    static SharedPreferences getSyncPrefs(Context context) {
        return context.getSharedPreferences("sync", Context.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("BSBG", "System says, we shall sync now");

        long syncTime;
        if (!Settings.of(context).isLoggedIn())
            syncTime = SystemClock.elapsedRealtime() + standardHours(6).getMillis();
        else
            syncTime = getNextSyncTime(context);

        try {
            scheduleNextSync(context, syncTime);
        } catch (Exception err) {
            FirebaseCrash.logcat(Log.ERROR, "BSBG", "YELP!");
            FirebaseCrash.report(err);
        }

        Intent service = new Intent(context, SyncIntentService.class);
        startWakefulService(context, service);
    }
}

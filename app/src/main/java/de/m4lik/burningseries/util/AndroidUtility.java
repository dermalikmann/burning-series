package de.m4lik.burningseries.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.net.ConnectivityManagerCompat;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

import de.m4lik.burningseries.BuildConfig;
import rx.Observable;

/**
 * Created by Malik on 12.01.2017
 *
 * @author Malik Mann
 */

public class AndroidUtility {

    private static final Cache<String, Boolean> previousExceptions =
            CacheBuilder.<String, Boolean>newBuilder()
                    .expireAfterWrite(1, TimeUnit.MINUTES)
                    .maximumSize(32)
                    .build();

    private AndroidUtility() {
    }

    public static Bundle bundle(String key, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        return bundle;
    }

    public static int dp(Context context, int dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density);
    }

    public static boolean isOnMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return ConnectivityManagerCompat.isActiveNetworkMetered(cm);
    }

    public static void checkMainThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalStateException("Must be called from the main thread.");
    }

    public static void checkNotMainThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread())
            throw new IllegalStateException("Must not be called from the main thread.");
    }

    public static <T> Optional<T> toOptional(Observable<T> observable) {
        T element = observable.toBlocking().singleOrDefault(null);
        return Optional.fromNullable(element);
    }

    public static int buildNumber() {
        if (BuildConfig.DEBUG) {
            return 100;
        } else {
            return BuildConfig.VERSION_CODE;
        }
    }

    public static void recreateActivity(Activity activity) {
        final Intent intent = activity.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder.create(activity)
                .addNextIntentWithParentStack(intent)
                .startActivities();
    }
}

package de.monarchcode.m4lik.burningseries.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.net.ConnectivityManagerCompat;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

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
}

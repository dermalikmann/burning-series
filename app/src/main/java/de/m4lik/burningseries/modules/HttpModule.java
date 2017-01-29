package de.m4lik.burningseries.modules;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.m4lik.burningseries.util.AndroidUtility;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

@Module
public class HttpModule {

    @Provides
    @Singleton
    public OkHttpClient okHttpClient(Context context) {

        File cacheDir = new File(context.getCacheDir(), "imgCache");

        int version = AndroidUtility.buildNumber();
        return new OkHttpClient.Builder()

                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(8, 30, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)

                .build();
    }
}

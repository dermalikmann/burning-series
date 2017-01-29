package de.m4lik.burningseries.modules;

import android.app.Application;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

@Module
public class AppModule {
    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public SharedPreferences sharedPreferences() {
        return application.getSharedPreferences("burning-series", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    public Application application() {
        return application;
    }

    @Provides
    @Singleton
    public Context context() {
        return application;
    }

    @Provides
    @Singleton
    public NotificationManager notificationManager() {
        return (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    public DownloadManager downloadManager() {
        return (DownloadManager) application.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Provides
    @Singleton
    public PowerManager powerManager() {
        return (PowerManager) application.getSystemService(Context.POWER_SERVICE);
    }
}

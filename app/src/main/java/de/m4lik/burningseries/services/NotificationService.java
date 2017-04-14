package de.m4lik.burningseries.services;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.services.objects.Update;
import de.m4lik.burningseries.ui.UpdateActivity;
import de.m4lik.burningseries.util.Settings;

/**
 * Created by Malik on 29.01.2017
 *
 * @author Malik Mann
 */

@Singleton
public class NotificationService {

    public static final int NOTIFICATION_UPDATE_ID = 1001;

    private final Settings settings;
    private final Application context;
    private final NotificationManagerCompat nm;

    @Inject
    public NotificationService(Application context) {

        this.context = context;
        this.settings = Settings.of(context);
        this.nm = NotificationManagerCompat.from(context);
    }

    /**
     * Creates a new v7 notification buidler
     */
    private static NotificationCompat.Builder newNotificationBuilder(Context context) {
        return new android.support.v7.app.NotificationCompat.Builder(context);
    }

    public void showUpdateNotification(Update update) {

        String message = "Es ist ein neues Update der App auf Version " + update.versionName() + " verfügbar, " +
                "unter Anderem mit: " + update.changelog();

        Notification notification = newNotificationBuilder(context)
                .setContentIntent(updateActivityIntent(update))
                .setContentTitle("Neues Update verfügbar!")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_stat_name)
                .addAction(R.drawable.ic_action_download, "Download", updateActivityIntent(update))
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setAutoCancel(true)
                .build();

        nm.notify(NOTIFICATION_UPDATE_ID, notification);
    }

    private PendingIntent updateActivityIntent(Update update) {
        Intent intent = new Intent(context, UpdateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(UpdateActivity.EXTRA_UPDATE, update);

        return TaskStackBuilder.create(context)
                .addParentStack(UpdateActivity.class)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void cancelForUpdate() {
        nm.cancel(NOTIFICATION_UPDATE_ID);
    }
}

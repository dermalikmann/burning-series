package de.m4lik.burningseries.services;

import android.app.IntentService;
import android.content.Intent;

import com.google.common.base.Optional;

import javax.inject.Inject;

import de.m4lik.burningseries.Dagger;
import de.m4lik.burningseries.services.objects.Update;
import de.m4lik.burningseries.util.Updater;

import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;
import static de.m4lik.burningseries.util.AndroidUtility.toOptional;

public class SyncIntentService extends IntentService {

    @Inject
    NotificationService notificationService;

    @Inject
    SingleShotService singleShotService;


    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Dagger.appComponent(this).inject(this);

        Optional<Update> update = toOptional(new Updater(this).check());
        if (update.isPresent()) {
            notificationService.showUpdateNotification(update.get());
        }

        completeWakefulIntent(intent);
    }
}

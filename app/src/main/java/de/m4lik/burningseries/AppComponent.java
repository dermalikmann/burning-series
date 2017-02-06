package de.m4lik.burningseries;

import javax.inject.Singleton;

import dagger.Component;
import de.m4lik.burningseries.modules.ActivityModule;
import de.m4lik.burningseries.modules.AppModule;
import de.m4lik.burningseries.modules.HttpModule;
import de.m4lik.burningseries.services.DownloadService;
import de.m4lik.burningseries.services.NotificationService;
import de.m4lik.burningseries.services.SyncIntentService;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

@Singleton
@Component(modules = {
        AppModule.class,
        HttpModule.class,
})
public interface AppComponent {
    ActivityComponent activityComponent(ActivityModule activityModule);

    DownloadService downloadService();

    NotificationService notificationService();

    void inject(SettingsActivity.SettingsFragment fragment);

    void inject(Dagger.EagerSingletons eagerSingletons);

    void inject(SyncIntentService syncIntentService);
}

package de.m4lik.burningseries;

import dagger.Subcomponent;
import de.m4lik.burningseries.modules.ActivityModule;
import de.m4lik.burningseries.ui.FullscreenVideoActivity;
import de.m4lik.burningseries.ui.LoginActivity;
import de.m4lik.burningseries.ui.MainActivity;
import de.m4lik.burningseries.ui.SettingsActivity;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.StatisticsActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.dialogs.DownloadUpdateDialog;
import de.m4lik.burningseries.ui.dialogs.ErrorDialog;
import de.m4lik.burningseries.ui.dialogs.MobileDataAlertDialog;
import de.m4lik.burningseries.ui.dialogs.PlayerChooserDialog;
import de.m4lik.burningseries.ui.dialogs.ShowSyncDialog;
import de.m4lik.burningseries.ui.dialogs.UpdateDialog;
import de.m4lik.burningseries.util.ContextSingleton;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */
@ContextSingleton
@Subcomponent(modules = {ActivityModule.class})
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

    void inject(FullscreenVideoActivity activity);

    void inject(LoginActivity activity);

    void inject(SettingsActivity activity);

    void inject(ShowActivity activity);

    void inject(StatisticsActivity activity);

    void inject(TabletShowActivity activity);


    void inject(DownloadUpdateDialog dialog);

    void inject(MobileDataAlertDialog dialog);

    void inject(PlayerChooserDialog dialog);

    void inject(ShowSyncDialog dialog);

    void inject(ErrorDialog dialog);

    void inject(UpdateDialog dialog);
}

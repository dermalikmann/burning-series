package de.m4lik.burningseries;

import dagger.Subcomponent;
import de.m4lik.burningseries.modules.ActivityModule;
import de.m4lik.burningseries.ui.dialogs.DownloadUpdateDialog;
import de.m4lik.burningseries.ui.dialogs.ErrorDialog;
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

    void inject(ShowActivity showActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(FullscreenVideoActivity videoActivity);

    void inject(LoginActivity activity);

    void inject(DownloadUpdateDialog dialog);

    void inject(ErrorDialog dialog);

    void inject(UpdateDialog dialog);
}

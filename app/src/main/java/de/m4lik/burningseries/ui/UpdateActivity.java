package de.m4lik.burningseries.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.f2prateek.dart.InjectExtra;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.services.objects.Update;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.util.Updater;
import de.m4lik.burningseries.util.listeners.DialogDismissListener;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Created by Malik on 29.01.2017
 *
 * @author Malik Mann
 */

public class UpdateActivity extends ActivityBase implements DialogDismissListener {
    public static final String EXTRA_UPDATE = "UpdateActivity__EXTRA_UPDATE";

    @InjectExtra(EXTRA_UPDATE)
    Update update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme().basic);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Updater.download(this, update);
        }
    }

    @Override
    protected void injectComponent(ActivityComponent appComponent) {
        // nothing to do here
    }

    @Override
    public void onDialogDismissed(DialogFragment dialog) {
        finish();
    }
}
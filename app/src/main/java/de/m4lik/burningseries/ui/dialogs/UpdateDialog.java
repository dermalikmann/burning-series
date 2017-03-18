package de.m4lik.burningseries.ui.dialogs;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.trello.rxlifecycle.android.ActivityEvent;

import org.joda.time.Instant;

import javax.inject.Inject;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.BuildConfig;
import de.m4lik.burningseries.services.objects.Update;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.base.DialogBase;
import de.m4lik.burningseries.util.Updater;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Actions;

import static de.m4lik.burningseries.ui.dialogs.BusyDialog.busyDialog;
import static org.joda.time.Duration.standardHours;
import static org.joda.time.Instant.now;

public class UpdateDialog extends DialogBase {
    @Inject
    DownloadManager downloadManager;

    @Inject
    SharedPreferences sharedPreferences;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Update update = getArguments().getParcelable("update");
        return update != null ?
                updateAvailableDialog(update) :
                noUpdateAvailableDialog();
    }

    private Dialog updateAvailableDialog(final Update update) {
        return DialogBuilder.start(getActivity())
                .content("Es ist ein neues Update verfügbar!")
                .positive("Download", new DialogBuilder.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialog) {
                        Updater.download(getActivity(), update);
                    }
                })
                .negative("Ignorieren")
                .build();
    }

    private Dialog noUpdateAvailableDialog() {
        return DialogBuilder.start(getActivity())
                .content("Du hast bereits die aktuelle Version!")
                .positive()
                .build();
    }

    public static UpdateDialog newInstance(Update update) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("update", update);

        UpdateDialog dialog = new UpdateDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    /* The whole magic */

    public static void checkForUpdates(final ActivityBase activity, final boolean interactive) {
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        if (!interactive && !BuildConfig.DEBUG) {
            Instant last = new Instant(shared.getLong(KEY_LAST_UPDATE_CHECK, 0));
            if (last.isAfter(now().minus(standardHours(1))))
                return;
        }

        /* Action to store the last check time */
        Action0 storeCheckTime = () ->
                shared.edit()
                        .putLong(KEY_LAST_UPDATE_CHECK, now().getMillis())
                        .apply();


        /* show a busy-dialog or not? */
        Observable.Operator<Update, Update> busyOperator =
                interactive ? busyDialog(activity) : NOOP;

        // do the check
        new Updater(activity).check()
                .onErrorResumeNext(Observable.empty())
                .defaultIfEmpty(null)
                .compose(activity.bindUntilEventAsync(ActivityEvent.DESTROY))
                .lift(busyOperator)
                .doAfterTerminate(storeCheckTime)
                .subscribe(update -> {
                            if (interactive || update != null) {
                                UpdateDialog dialog = newInstance((Update) update);
                                dialog.show(activity.getSupportFragmentManager(), null);
                            }
                }, Actions.empty());
    }

    private static final String KEY_LAST_UPDATE_CHECK = "pref_last_update";
    private static final Observable.Operator<Update, Update> NOOP = subscriber -> subscriber;

    @Override
    protected void injectComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }
}

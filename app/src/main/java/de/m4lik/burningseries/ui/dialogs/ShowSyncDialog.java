package de.m4lik.burningseries.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.AppComponent;
import de.m4lik.burningseries.Dagger;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.objects.GenreMap;
import de.m4lik.burningseries.api.objects.ShowObj;
import de.m4lik.burningseries.services.ShowSyncService;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.base.DialogBase;
import de.m4lik.burningseries.util.BackgroundScheduler;
import de.m4lik.burningseries.util.Settings;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Actions;

import static de.m4lik.burningseries.ui.dialogs.BusyDialog.busyDialog;
import static de.m4lik.burningseries.util.AndroidUtility.checkMainThread;

/**
 * Created by Malik on 29.01.2017
 *
 * @author Malik Mann
 */

public class ShowSyncDialog extends DialogBase {

    private final Observable<ShowSyncService.ShowSyncProgress> progress;
    private final String text;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.progress_dialog_text)
    TextView textView;

    public ShowSyncDialog() {
        this(Observable.empty(), "");
    }

    @SuppressLint("ValidFragment")
    public ShowSyncDialog(Observable<ShowSyncService.ShowSyncProgress> progress, String text) {
        setRetainInstance(true);
        if (text.equals(""))
            this.text = getString(R.string.please_wait);
        else
            this.text = text;
        this.progress = progress;
    }

    public static void syncShows(final ActivityBase activity) {
        new ShowSyncService(activity).fetchShows()
                .onErrorResumeNext(Observable.empty())
                .defaultIfEmpty(null)
                .compose(activity.bindUntilEventAsync(ActivityEvent.DESTROY))
                .lift(busyDialog(activity))
                .subscribe(genreMap -> updateShows(activity, genreMap), Actions.empty());
    }

    public static void updateShows(final ActivityBase activity, GenreMap genreMap) {
        AppComponent appComponent = Dagger.appComponent(activity);
        Observable<ShowSyncService.ShowSyncProgress> progress = appComponent.showSyncService()
                .updateShows(genreMap)
                .subscribeOn(BackgroundScheduler.instance())
                .unsubscribeOn(BackgroundScheduler.instance())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> { if (!Settings.of(activity).getUserSession().equals("")) syncFavs(activity);})
                .share();

        ShowSyncDialog dialog = new ShowSyncDialog(progress, "Serien werden geladen...");
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    public static void syncFavs(final ActivityBase activity) {
        new ShowSyncService(activity).fetchFavs()
                .onErrorResumeNext(Observable.empty())
                .defaultIfEmpty(null)
                .compose(activity.bindUntilEventAsync(ActivityEvent.DESTROY))
                .lift(busyDialog(activity))
                .subscribe(genreMap -> updateFavs(activity, genreMap), Actions.empty());
    }

    public static void updateFavs(final ActivityBase activity, List<ShowObj> list) {
        AppComponent appComponent = Dagger.appComponent(activity);
        Observable<ShowSyncService.ShowSyncProgress> progress = appComponent.showSyncService()
                .updateFavs(list)
                .subscribeOn(BackgroundScheduler.instance())
                .unsubscribeOn(BackgroundScheduler.instance())
                .observeOn(AndroidSchedulers.mainThread())
                .share();

        ShowSyncDialog dialog = new ShowSyncDialog(progress, "Serien werden geladen...");
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected void injectComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = DialogBuilder.start(getActivity())
                .layout(R.layout.dialog_synced_progress)
                .cancelable(false)
                .show();
        ButterKnife.bind(dialog);

        return dialog;
    }

    @Override
    protected void onDialogViewCreated() {
        textView.setText(text);
        progressBar.setIndeterminate(true);
        progressBar.setMax(1000);
        progress.compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .onErrorResumeNext(Observable.empty())
                .doAfterTerminate(this::dismiss)
                .subscribe(this::updateStatus);
    }

    private void updateStatus(ShowSyncService.ShowSyncProgress status) {
        checkMainThread();

        progressBar.setIndeterminate(status.progress < 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            progressBar.setProgress((int) (1000 * status.progress), true);
        else
            progressBar.setProgress((int) (1000 * status.progress));
    }
}

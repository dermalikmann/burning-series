package de.m4lik.burningseries.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;

import com.trello.rxlifecycle.android.FragmentEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.services.DownloadService;
import de.m4lik.burningseries.ui.base.DialogBase;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

import static de.m4lik.burningseries.util.AndroidUtility.checkMainThread;

/**
 * Created by Malik on 29.01.2017
 *
 * @author Malik Mann
 */

public class DownloadUpdateDialog extends DialogBase {

    private final Observable<DownloadService.Status> progress;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    public DownloadUpdateDialog() {
        this(Observable.<DownloadService.Status>empty());
    }

    @SuppressLint("ValidFragment")
    public DownloadUpdateDialog(Observable<DownloadService.Status> progress) {
        setRetainInstance(true);
        this.progress = progress;
    }

    @Override
    protected void injectComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = DialogBuilder.start(getActivity())
                .layout(R.layout.dialog_update)
                .show();
        ButterKnife.bind(dialog);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    protected void onDialogViewCreated() {
        progressBar.setIndeterminate(true);
        progressBar.setMax(1000);
        progress.compose(bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .onErrorResumeNext(Observable.empty())
                .doAfterTerminate(new Action0() {
                    @Override
                    public void call() {
                        dismiss();
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        updateStatus((DownloadService.Status) o);
                    }
                });
    }

    private void updateStatus(DownloadService.Status status) {
        checkMainThread();

        progressBar.setIndeterminate(status.progress < 0);
        progressBar.setProgress((int) (1000 * status.progress));
    }
}

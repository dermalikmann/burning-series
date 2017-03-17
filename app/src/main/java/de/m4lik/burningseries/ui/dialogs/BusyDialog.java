package de.m4lik.burningseries.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.TextView;

import butterknife.BindView;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.base.DialogBase;
import de.m4lik.burningseries.ui.views.BusyIndicator;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static de.m4lik.burningseries.util.AndroidUtility.checkMainThread;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */


public class BusyDialog extends DialogBase {
    @BindView(R.id.progress)
    BusyIndicator progress;

    @BindView(R.id.text)
    TextView message;

    static public <T> BusyDialogOperator<T> busyDialog(Fragment fragment) {
        return new BusyDialogOperator<>(fragment.getFragmentManager(), null, null);
    }

    static public <T> BusyDialogOperator<T> busyDialog(Fragment fragment, String text) {
        return new BusyDialogOperator<>(fragment.getFragmentManager(), text, null);
    }

    public static <T> BusyDialogOperator<T> busyDialog(FragmentActivity activity) {
        return new BusyDialogOperator<>(activity.getSupportFragmentManager(), null, null);
    }

    public static <T> BusyDialogOperator<T> busyDialog(FragmentActivity activity, String text) {
        return new BusyDialogOperator<>(activity.getSupportFragmentManager(), text, null);
    }

    public static <T> BusyDialogOperator<T> busyDialog(FragmentActivity activity, String text,
                                                       Func1<T, Float> progressMapper) {

        return new BusyDialogOperator<>(activity.getSupportFragmentManager(), text, progressMapper);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return DialogBuilder.start(getActivity())
                .layout(R.layout.dialog_busy)
                .cancelable()
                .build();
    }

    @Override
    protected void injectComponent(ActivityComponent activityComponent) {
    }

    @Override
    protected void onDialogViewCreated() {
        super.onDialogViewCreated();
        message.setText(getDialogText());
    }

    private String getDialogText() {
        Bundle args = getArguments();
        if (args != null) {
            String text = args.getString("text");
            if (text != null)
                return text;
        }
        return getString(R.string.please_wait);
    }

    void updateProgressValue(float progress) {
        if (this.progress != null) {
            this.progress.setProgress(progress);
        }
    }

    private static class BusyDialogOperator<T> implements Observable.Operator<T, T> {
        final String tag = "BusyDialog-" + System.identityHashCode(this);
        final FragmentManager fragmentManager;
        final Func1<T, Float> progressMapper;

        BusyDialogOperator(FragmentManager fragmentManager, String text,
                           Func1<T, Float> progressMapper) {

            this.fragmentManager = fragmentManager;
            this.progressMapper = progressMapper;

            BusyDialog dialog = new BusyDialog();
            if (text != null) {
                Bundle args = new Bundle();
                args.putString("text", text);
                dialog.setArguments(args);
            }

            try {
                dialog.show(fragmentManager, tag);
            } catch (Throwable ignored) {
            }
        }

        void dismiss() {
            checkMainThread();

            try {
                Fragment dialog = fragmentManager.findFragmentByTag(tag);
                if (dialog instanceof DialogFragment) {
                    ((DialogFragment) dialog).dismiss();
                }
            } catch (Throwable ignored) {
            }
        }

        @Override
        public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
            return new Subscriber<T>() {
                @Override
                public void onCompleted() {
                    try {
                        dismiss();
                    } catch (Throwable ignored) {
                    }

                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    try {
                        dismiss();
                    } catch (Throwable ignored) {
                    }

                    subscriber.onError(e);
                }

                @Override
                public void onNext(T value) {
                    if (progressMapper != null) {
                        float progress = progressMapper.call(value);
                        if (progress >= 0 && progress <= 1) {

                            // get the dialog and show the progress value!
                            Fragment dialog = fragmentManager.findFragmentByTag(tag);
                            if (dialog instanceof BusyDialog) {
                                ((BusyDialog) dialog).updateProgressValue(progress);
                            }
                        }
                    }

                    subscriber.onNext(value);
                }
            };
        }
    }
}
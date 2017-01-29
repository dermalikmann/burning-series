package de.m4lik.burningseries.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.components.support.RxAppCompatDialogFragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.Dagger;
import de.m4lik.burningseries.util.AsyncLifecycleTransformer;
import de.m4lik.burningseries.util.listeners.DialogDismissListener;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */


public abstract class DialogBase extends RxAppCompatDialogFragment {
    private Unbinder unbinder;

    public final <T> LifecycleTransformer<T> bindToLifecycleAsync() {
        return (LifecycleTransformer<T>) new AsyncLifecycleTransformer<>(bindToLifecycle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        injectComponent(Dagger.activityComponent(getActivity()));

        super.onCreate(savedInstanceState);
    }

    protected abstract void injectComponent(ActivityComponent activityComponent);

    @Override
    public void onStart() {
        super.onStart();

        // bind dialog. It is only created in on start.
        Dialog dialog = getDialog();
        if (dialog != null) {
            Log.d("BSDB", "Trying to bind views.");
            unbinder = ButterKnife.bind(this, dialog);
            onDialogViewCreated();
        }
    }

    protected void onDialogViewCreated() {
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);

        super.onDestroyView();

        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        FragmentActivity activity = getActivity();
        if (activity instanceof DialogDismissListener) {
            // propagate to fragment
            ((DialogDismissListener) activity).onDialogDismissed(this);
        }
    }

    protected Context getThemedContext() {
        Dialog dialog = getDialog();
        return dialog != null ? dialog.getContext() : getContext();
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception ignored) {
            // this shouln't be like that...
        }
    }

    @Override
    public void dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss();
        } catch (Exception ignored) {
            // f***!
        }
    }

    public void dismissNow() {
        try {
            dismiss();
            getFragmentManager().executePendingTransactions();
        } catch (Exception err) {
            // yelp!?
        }
    }
}

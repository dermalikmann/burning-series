package de.m4lik.burningseries.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.lang.ref.WeakReference;

//import de.m4lik.burningseries.util.ErrorFormatting;
import rx.functions.Action1;

import static de.m4lik.burningseries.util.AndroidUtility.checkMainThread;

/**
 * Created by Malik on 29.01.2017
 *
 * @author Malik Mann
 */

public class ErrorDialog extends DialogFragment {

    private static WeakReference<OnErrorDialogHandler> GLOBAL_ERROR_DIALOG_HANDLER;
    private static WeakReference<Throwable> PREVIOUS_ERROR = new WeakReference<>(null);

    public interface OnErrorDialogHandler {
        /**
         */
        //void showErrorDialog(Throwable error, ErrorFormatting.Formatter<?> formatter);
    }

    public static void setGlobalErrorDialogHandler(OnErrorDialogHandler handler) {
        checkMainThread();
        GLOBAL_ERROR_DIALOG_HANDLER = new WeakReference<>(handler);
    }

    public static void unsetGlobalErrorDialogHandler(OnErrorDialogHandler handler) {
        checkMainThread();

        if (GLOBAL_ERROR_DIALOG_HANDLER != null) {
            OnErrorDialogHandler oldHandler = GLOBAL_ERROR_DIALOG_HANDLER.get();
            if (oldHandler == handler)
                GLOBAL_ERROR_DIALOG_HANDLER = null;
        }
    }

    public static OnErrorDialogHandler getGlobalErrorDialogHandler() {
        if (GLOBAL_ERROR_DIALOG_HANDLER == null)
            return null;

        return GLOBAL_ERROR_DIALOG_HANDLER.get();
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private static void processError(Throwable error, OnErrorDialogHandler handler) {
        //TODO Error processing. maybe.

        /*try {
            // do some checking so we don't log this exception twice
            boolean sendToCrashlytics = PREVIOUS_ERROR.get() != error;
            PREVIOUS_ERROR = new WeakReference<>(error);

            // format and log
            ErrorFormatting.Formatter<?> formatter = ErrorFormatting.getFormatter(error);
            if (sendToCrashlytics && formatter.shouldSendToCrashlytics())
                //TODO: FB crash

            if (handler != null) {
                handler.showErrorDialog(error, formatter);
            }

        } catch (Throwable thr) {
            //TODO: FA/FC event
        }*/
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        return DialogBuilder.start(getActivity())
                .content(args.getString("content"))
                .positive()
                .build();
    }

    public static void showErrorString(FragmentManager fragmentManager, String message) {
        //TODO: FA event

        try {
            Bundle arguments = new Bundle();
            arguments.putString("content", message);

            // remove previous dialog, if any
            dismissErrorDialog(fragmentManager);

            ErrorDialog dialog = new ErrorDialog();
            dialog.setArguments(arguments);
            dialog.show(fragmentManager, "ErrorDialog");

        } catch (Exception error) {
            //TODO: FA/FC event
        }
    }

    /**
     * Dismisses any previously shown error dialog.
     */
    private static void dismissErrorDialog(FragmentManager fm) {
        try {
            Fragment previousFragment = fm.findFragmentByTag("ErrorDialog");
            if (previousFragment instanceof DialogFragment) {
                DialogFragment dialog = (DialogFragment) previousFragment;
                dialog.dismissAllowingStateLoss();
            }

        } catch (Throwable error) {
            //TODO: FA/FC event
        }
    }

    /**
     * Creates the default error callback {@link rx.functions.Action1}
     */
    public static Action1<Throwable> defaultOnError() {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                //processError(error, getGlobalErrorDialogHandler());
            }
        };
    }
}
package de.m4lik.burningseries;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import de.m4lik.burningseries.modules.ActivityModule;
import de.m4lik.burningseries.ui.base.ActivityBase;


/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

public class Dagger {
    private Dagger() {
    }

    public static AppComponent appComponent(Context context) {
        return ApplicationClass.get(context).appComponent.get();
    }

    public static ActivityComponent activityComponent(Activity activity) {
        if (activity instanceof ActivityBase) {
            // create or reuse the graph
            return ((ActivityBase) activity).getActivityComponent();
        } else {
            return newActivityComponent(activity);
        }
    }

    public static ActivityComponent newActivityComponent(Activity activity) {
        return appComponent(activity).activityComponent(new ActivityModule(activity));
    }

    static void initEagerSingletons(final Application application) {
        AsyncTask.execute(() -> {
            try {
                Dagger.appComponent(application).inject(new EagerSingletons());
            } catch (Throwable error) {
                //TODO: FA event
            }
        });
    }

    static class EagerSingletons {
        EagerSingletons() {
        }
    }
}

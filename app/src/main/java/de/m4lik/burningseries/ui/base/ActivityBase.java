package de.m4lik.burningseries.ui.base;

import android.os.Bundle;

import com.f2prateek.dart.Dart;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.Dagger;
import de.m4lik.burningseries.util.AsyncLifecycleTransformer;
import rx.Observable;

import static de.m4lik.burningseries.util.AndroidUtility.checkMainThread;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

public abstract class ActivityBase extends RxAppCompatActivity {

    private ActivityComponent activityComponent;

    private Unbinder unbinder;

    public <T> Observable.Transformer<T, T> bindUntilEventAsync(ActivityEvent event) {
        return (Observable.Transformer<T, T>) new AsyncLifecycleTransformer<>(bindUntilEvent(event));
    }

    public final <T> LifecycleTransformer<T> bindToLifecycleAsync() {
        return (LifecycleTransformer<T>) new AsyncLifecycleTransformer<>(bindToLifecycle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityComponent = Dagger.newActivityComponent(this);
        injectComponent(activityComponent);

        Dart.inject(this);
        super.onCreate(savedInstanceState);
    }

    protected abstract void injectComponent(ActivityComponent appComponent);

    public ActivityComponent getActivityComponent() {
        checkMainThread();

        if (activityComponent == null)
            activityComponent = Dagger.newActivityComponent(this);

        return activityComponent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        unbinder = ButterKnife.bind(this);
    }

}
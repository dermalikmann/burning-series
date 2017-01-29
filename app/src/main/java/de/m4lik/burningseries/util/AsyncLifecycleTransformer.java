package de.m4lik.burningseries.util;

import android.support.annotation.NonNull;

import com.trello.rxlifecycle.LifecycleTransformer;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

/**
 */

public class AsyncLifecycleTransformer<T> implements LifecycleTransformer<T> {
    private final LifecycleTransformer<T> transformer;

    public AsyncLifecycleTransformer(LifecycleTransformer<T> transformer) {
        this.transformer = transformer;
    }

    @NonNull
    @Override
    public <U> Single.Transformer<U, U> forSingle() {
        return new Single.Transformer<U, U>() {
            @Override
            public Single<U> call(Single<U> uSingle) {
                return (Single<U>) uSingle
                        .subscribeOn(BackgroundScheduler.instance())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(transformer.forSingle());
            }
        };

    }

    @NonNull
    @Override
    public Completable.Transformer forCompletable() {
        return new Completable.Transformer() {
            @Override
            public Completable call(Completable completable) {
                return completable
                        .subscribeOn(BackgroundScheduler.instance())
                        .unsubscribeOn(BackgroundScheduler.instance())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(transformer.forCompletable());
            }
        };

    }

    @Override
    public Observable<T> call(Observable<T> observable) {
        return observable
                .subscribeOn(BackgroundScheduler.instance())
                .unsubscribeOn(BackgroundScheduler.instance())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(transformer);
    }
}

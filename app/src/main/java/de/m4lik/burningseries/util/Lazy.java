package de.m4lik.burningseries.util;

import com.google.common.base.Supplier;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */


public abstract class Lazy<T> {
    private T value;

    /**
     * Creates a new lazy from a supplier.
     */
    public static <T> Lazy<T> of(final Supplier<T> supplier) {
        return new Lazy<T>() {
            @Override
            protected T compute() {
                return supplier.get();
            }
        };
    }

    public T get() {
        T result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    value = result = compute();
                }
            }
        }

        return result;
    }

    protected abstract T compute();
}

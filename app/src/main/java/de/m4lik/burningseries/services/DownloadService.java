package de.m4lik.burningseries.services;

import android.content.Context;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.io.PatternFilenameFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.subscriptions.Subscriptions;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

public class DownloadService {

    private final Context context;
    private final OkHttpClient okHttpClient;

    @Inject
    public DownloadService(Context context, OkHttpClient okHttpClient) {
        this.context = context;
        this.okHttpClient = okHttpClient;
    }

    public Observable<Status> downloadUpdate(final String uri) {
        return Observable.unsafeCreate(subscriber -> {
            try {
                File directory = new File(context.getCacheDir(), "updates");
                if (!directory.exists() && !directory.mkdirs()) {
                    Crashlytics.log("Could not create apk directory: " + directory);
                }

                // clear all previous files
                File[] files = directory.listFiles(new PatternFilenameFilter("bs-update.*apk"));
                for (File file : files) {
                    if (!file.delete()) {Crashlytics.log("Could not delete file: " + file);
                    }
                }

                // and download the new file.
                File tempFile = File.createTempFile(
                        "bs-update", ".apk",
                        directory);

                try (OutputStream output = new FileOutputStream(tempFile)) {

                    Request request = new Request.Builder().url(uri).build();
                    Call call = okHttpClient.newCall(request);
                    subscriber.add(Subscriptions.create(() -> call.cancel()));

                    Response response = call.execute();
                    Interval interval = new Interval(250);
                    try (CountingInputStream input = new CountingInputStream(response.body().byteStream())) {
                        int count;
                        byte[] buffer = new byte[1024 * 32];
                        while ((count = ByteStreams.read(input, buffer, 0, buffer.length)) > 0) {
                            output.write(buffer, 0, count);

                            if (interval.check()) {
                                float progress = input.getCount() / (float) response.body().contentLength();
                                subscriber.onNext(new Status(progress, null));
                            }
                        }
                    }
                }

                subscriber.onNext(new Status(1, tempFile));
                subscriber.onCompleted();

            } catch (Throwable error) {
                subscriber.onError(error);
            }
        });
    }

    private static class Interval {
        private final long interval;
        private long last = System.currentTimeMillis();

        Interval(long interval) {
            this.interval = interval;
        }

        boolean check() {
            long now = System.currentTimeMillis();
            if (now - last > interval) {
                last = now;
                return true;
            }

            return false;
        }
    }

    public static class Status {
        @Nullable
        public final File file;
        public final float progress;

        Status(float progress, @Nullable File file) {
            this.progress = progress;
            this.file = file;
        }

        public boolean finished() {
            return file != null;
        }
    }
}
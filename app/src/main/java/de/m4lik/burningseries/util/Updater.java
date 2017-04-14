package de.m4lik.burningseries.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.AppComponent;
import de.m4lik.burningseries.BuildConfig;
import de.m4lik.burningseries.Dagger;
import de.m4lik.burningseries.services.DownloadService;
import de.m4lik.burningseries.services.objects.GsonAdaptersUpdate;
import de.m4lik.burningseries.services.objects.ImmutableUpdate;
import de.m4lik.burningseries.services.objects.Update;
import de.m4lik.burningseries.ui.dialogs.DownloadUpdateDialog;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Actions;
import rx.util.async.Async;

import static de.m4lik.burningseries.ui.dialogs.ErrorDialog.defaultOnError;

/**
 * Created by Malik on 28.01.2017
 *
 * @author Malik Mann
 */

public class Updater {

    private final int currentVersion;
    private final ImmutableList<String> endpoints;

    public Updater(Context context) {
        currentVersion = AndroidUtility.buildNumber();
        Log.d("BSUD", "CV: " + currentVersion);

        boolean betaChannel = Settings.of(context).isBetaChannel();
        endpoints = updateUrls(betaChannel);
    }

    private static Retrofit newRestAdapter(String endpoint) {
        com.google.gson.Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new GsonAdaptersUpdate())
                .create();

        return new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    /**
     * Returns the Endpoint-URL that is to be queried
     */
    private static ImmutableList<String> updateUrls(boolean betaChannel) {
        List<String> urls = new ArrayList<>();

        if (betaChannel) {
            urls.add("http://bs.malikmann.de/version/beta/");
        } else {
            urls.add("http://bs.malikmann.de/version/stable/");
        }

        return ImmutableList.copyOf(urls);
    }

    public static void download(FragmentActivity activity, Update update) {
        Log.d("BS-Updater", "Trying to download...");
        AppComponent appComponent = Dagger.appComponent(activity);
        Observable<DownloadService.Status> progress = appComponent.downloadService()
                .downloadUpdate(update.apk())
                .subscribeOn(BackgroundScheduler.instance())
                .unsubscribeOn(BackgroundScheduler.instance())
                .observeOn(AndroidSchedulers.mainThread())
                .share();

        // install on finish
        final Context appContext = activity.getApplicationContext();
        progress.filter(
                DownloadService.Status::finished)
                .flatMap(status -> {
                    try {
                        install(appContext, status.file);
                        return Observable.empty();

                    } catch (IOException error) {
                        return Observable.error(error);
                    }
                })
                .subscribe(Actions.empty(), defaultOnError());

        // show a progress dialog
        DownloadUpdateDialog dialog = new DownloadUpdateDialog(progress);
        dialog.show(activity.getSupportFragmentManager(), null);

        // remove pending upload notification
        appComponent.notificationService().cancelForUpdate();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void install(Context context, File apk) throws IOException {
        Log.d("BS-Updater", "Trying to install...");
        Uri uri = Uri.parse("");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String provider = BuildConfig.APPLICATION_ID + ".FileProvider";
            try {
                uri = FileProvider.getUriForFile(context, provider, apk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File file = new File(context.getExternalCacheDir(), "update.apk");

            //TODO: FA event
            try (InputStream input = new FileInputStream(apk)) {
                try (OutputStream output = new FileOutputStream(file)) {
                    ByteStreams.copy(input, output);
                }
            }

            // make file readable
            if (file.setReadable(true))
                //TODO: FA event

                uri = Uri.fromFile(file);
        }

        Log.d("BS-Updater", "Starting install intent...");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    private Observable<Update> check(final String endpoint) {
        Log.d("BS-Updater", "Checking for new version");
        return Async.fromCallable(() -> {
            UpdateApi api = newRestAdapter(endpoint).create(UpdateApi.class);
            return api.get().execute().body();
        }, BackgroundScheduler.instance()).filter(
                update -> {
                    //TODO: FA event
                    // filter out if up to date
                    Log.d("BSUD", "NV: " + update.buildNumber());
                    return update.buildNumber() > currentVersion;
                }
        ).map(update -> {
            // rewrite url to make it absolute
            String apk = update.apk();
            if (!apk.startsWith("http")) {
                apk = Uri.withAppendedPath(Uri.parse(endpoint), apk).toString();
            }

            //TODO: FA event
            return ImmutableUpdate.builder()
                    .buildNumber(update.buildNumber())
                    .versionName(update.versionName())
                    .changelog(update.changelog())
                    .apk(apk)
                    .build();
        });
    }

    public Observable<Update> check() {
        return Observable.from(endpoints)
                .flatMap(ep -> check(ep)
                        .doOnError(err -> {
                            //logger.warn("Could not check for update at {}: {}", ep, err.toString());
                            FirebaseCrash.logcat(Log.ERROR, "BSUD", "Error while checking for new version.");
                            err.printStackTrace();
                            //FirebaseCrash.report(err);
                        })
                        .onErrorResumeNext(Observable.empty())
                )
                .take(1);
    }

    private interface UpdateApi {
        @GET("update.json")
        Call<Update> get();
    }
}

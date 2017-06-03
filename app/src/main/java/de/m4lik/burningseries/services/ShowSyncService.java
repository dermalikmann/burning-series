package de.m4lik.burningseries.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.m4lik.burningseries.AppComponent;
import de.m4lik.burningseries.Dagger;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.GenreMap;
import de.m4lik.burningseries.api.objects.GenreObj;
import de.m4lik.burningseries.api.objects.ShowObj;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.ui.dialogs.ShowSyncDialog;
import de.m4lik.burningseries.util.BackgroundScheduler;
import de.m4lik.burningseries.util.Settings;
import retrofit2.Call;
import rx.Observable;
import rx.util.async.Async;

/**
 * Created by Malik on 03.06.2017
 *
 * @author Malik Mann
 */

public class ShowSyncService {

    private Context context;

    @Inject
    public ShowSyncService(Context context) {
        this.context = context;
    }

    public static void syncFavs(FragmentActivity activity, List<ShowObj> list, String text) {
        AppComponent appComponent = Dagger.appComponent(activity);
        Observable<ShowSyncProgress> progress = appComponent.showSyncService()
                .updateFavs(list)
                .subscribeOn(BackgroundScheduler.instance())
                .unsubscribeOn(BackgroundScheduler.instance())
                .observeOn(BackgroundScheduler.instance())
                .share();

        ShowSyncDialog dialog = new ShowSyncDialog(progress, "Serien werden geladen.\nBitte kurz warten...");
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    public Observable<GenreMap> fetchShows() {
        return
                Async.fromCallable(() -> {
                            API api = new API();
                            APIInterface apiInterface = api.getInterface();
                            api.setSession(Settings.of(context).getUserSession());
                            api.generateToken("series:genre");
                            Call<GenreMap> call = apiInterface.getSeriesGenreList(api.getToken(), api.getUserAgent(), api.getSession());
                            return call.execute().body();
                        }, BackgroundScheduler.instance()
                ).map(genreMap -> genreMap);
    }

    public Observable<List<ShowObj>> fetchFavs() {
        return
                Async.fromCallable(() -> {
                            API api = new API();
                            APIInterface apiInterface = api.getInterface();
                            api.setSession(Settings.of(context).getUserSession());
                            api.generateToken("user/series");
                            Call<List<ShowObj>> call = apiInterface.getFavorites(api.getToken(), api.getUserAgent(), api.getSession());
                            return call.execute().body();
                        }, BackgroundScheduler.instance()
                ).map(favs -> favs);
    }

    public Observable<ShowSyncProgress> updateShows(GenreMap genreMap) {
        return Observable.unsafeCreate(subscriber -> {
            try {

                if (genreMap.size() == 0)
                    throw new Exception("NoElementsFound");

                Integer totalShows = 0;
                for (Map.Entry<String, GenreObj> entry : genreMap.entrySet()) {
                    totalShows += entry.getValue().getShows().length;
                }

                MainDBHelper dbHelper = new MainDBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                int genreID = 0;
                int processedShows = 0;

                Interval interval = new Interval(100);

                for (Map.Entry<String, GenreObj> entry : genreMap.entrySet()) {
                    String currentGenre = entry.getKey();
                    GenreObj go = entry.getValue();
                    ContentValues values = new ContentValues();
                    values.put(SeriesContract.genresTable.COLUMN_NAME_GENRE, currentGenre);
                    values.put(SeriesContract.genresTable.COLUMN_NAME_ID, genreID);
                    db.insert(SeriesContract.genresTable.TABLE_NAME, null, values);
                    Iterator itr = Arrays.asList(go.getShows()).iterator();
                    int processedGenreShows = 0;
                    while (processedGenreShows < go.getShows().length) {
                        int limiter = 1;

                        db.beginTransaction();
                        while (limiter <= 20 && itr.hasNext()) {
                            ShowObj show = (ShowObj) itr.next();

                            ContentValues cv = new ContentValues();
                            cv.put(SeriesContract.seriesTable.COLUMN_NAME_ID, show.getId());
                            cv.put(SeriesContract.seriesTable.COLUMN_NAME_TITLE, show.getName());
                            cv.put(SeriesContract.seriesTable.COLUMN_NAME_GENRE, currentGenre);
                            cv.put(SeriesContract.seriesTable.COLUMN_NAME_DESCR, "");
                            cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 0);

                            db.insert(SeriesContract.seriesTable.TABLE_NAME, null, cv);

                            processedGenreShows++;
                            limiter++;
                            processedShows++;
                            if (interval.check())
                                subscriber.onNext(new ShowSyncProgress(processedShows / (float) totalShows, false));
                        }
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    }
                    genreID++;
                }

                subscriber.onNext(new ShowSyncProgress(1, true));
                subscriber.onCompleted();

                db.close();
                dbHelper.close();

            } catch (Throwable error) {
                subscriber.onError(error);
            }
        });
    }

    public Observable<ShowSyncProgress> updateFavs(List<ShowObj> list) {
        return Observable.create(subscriber -> {
            try {

                if (list.size() == 0)
                    throw new Exception("NoElementsFound");

                MainDBHelper dbHelper = new MainDBHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Interval interval = new Interval(100);

                Iterator itr = list.iterator();
                int i = 0;
                while (i < list.size()) {
                    int j = 1;

                    db.beginTransaction();
                    while (j <= 50 && itr.hasNext()) {
                        ShowObj show = (ShowObj) itr.next();
                        ContentValues cv = new ContentValues();
                        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 1);
                        db.update(
                                SeriesContract.seriesTable.TABLE_NAME,
                                cv,
                                SeriesContract.seriesTable.COLUMN_NAME_ID + " = ?",
                                new String[]{show.getId().toString()}
                        );

                        j++;
                        i++;
                        if (interval.check())
                            subscriber.onNext(new ShowSyncProgress(i / list.size(), false));
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }

                subscriber.onNext(new ShowSyncProgress(1, true));
                subscriber.onCompleted();

                db.close();
                dbHelper.close();
            } catch (Exception e) {
                subscriber.onError(e);
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

    public class ShowSyncProgress {
        public float progress;
        private boolean finished;

        public ShowSyncProgress(float progress, boolean finished) {
            this.progress = progress;
            this.finished = finished;
        }

        public boolean finished() { return finished; }
    }
}

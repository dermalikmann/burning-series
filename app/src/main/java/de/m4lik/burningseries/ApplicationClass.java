package de.m4lik.burningseries;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.google.common.base.Supplier;

import net.danlew.android.joda.JodaTimeAndroid;

import butterknife.ButterKnife;
import de.m4lik.burningseries.modules.AppModule;
import de.m4lik.burningseries.modules.HttpModule;
import de.m4lik.burningseries.services.ThemeHelperService;
import de.m4lik.burningseries.util.Lazy;
import de.m4lik.burningseries.util.LooperScheduler;
import de.m4lik.burningseries.util.Settings;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;

/**
 * Application class for burning series
 *
 * @author Malik Mann
 */

public class ApplicationClass extends Application {

    private static ApplicationClass INSTANCE;

    static {
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return LooperScheduler.MAIN;
            }
        });
    }

    final Lazy<AppComponent> appComponent = Lazy.of(
            new Supplier<AppComponent>() {
                @Override
                public AppComponent get() {
                    return DaggerAppComponent.builder()
                            .appModule(new AppModule(ApplicationClass.this))
                            .httpModule(new HttpModule())
                            .build();
                }
            });

    public ApplicationClass() {
        INSTANCE = this;
    }

    public static ApplicationClass get(Context context) {
        return (ApplicationClass) context.getApplicationContext();
    }

    public static AppComponent appComponent() {
        return INSTANCE.appComponent.get();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        Settings.initialize(this);

        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
            ButterKnife.setDebug(true);

        }

        Dagger.initEagerSingletons(this);

        // get the correct theme for the app!
        ThemeHelperService.updateTheme(this);
    }
}

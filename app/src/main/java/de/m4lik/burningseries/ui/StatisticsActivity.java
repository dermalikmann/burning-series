package de.m4lik.burningseries.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.listitems.StatsListItem;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class StatisticsActivity extends ActivityBase {

    List<StatsListItem> statsList = new ArrayList<>();

    static SharedPreferences getStatsPrefs(Context context) {
        return context.getSharedPreferences("stats", Context.MODE_PRIVATE);
    }

    @Override
    protected void injectComponent(ActivityComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme().basic);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getApplicationContext().getResources().getBoolean(R.bool.isTablet)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        Context c = getApplicationContext();
        statsList.add(new StatsListItem("Registrierte User", getStatsPrefs(c).getString("regUsers", "")));
        statsList.add(new StatsListItem("Neuster User", getStatsPrefs(c).getString("newestUser", "")));
        statsList.add(new StatsListItem("User online", getStatsPrefs(c).getString("online", "")));
        statsList.add(new StatsListItem("Nachrichten (heute)", getStatsPrefs(c).getString("messages", "")));
        statsList.add(new StatsListItem("Shoutboxeinträge (heute)", getStatsPrefs(c).getString("shoutbox", "")));
        statsList.add(new StatsListItem("Serien", getStatsPrefs(c).getString("shows", "")));
        statsList.add(new StatsListItem("Episoden", getStatsPrefs(c).getString("episodes", "")));
        statsList.add(new StatsListItem("Episoden pro Serie", getStatsPrefs(c).getString("eps", "")));
        statsList.add(new StatsListItem("Links", getStatsPrefs(c).getString("links", "")));
        statsList.add(new StatsListItem("Verlinkt (heute)", getStatsPrefs(c).getString("linked", "")));
        statsList.add(new StatsListItem("Gelöschte Links (heute)", getStatsPrefs(c).getString("deleted", "")));
        statsList.add(new StatsListItem("Links pro Episode", getStatsPrefs(c).getString("lpe", "")));
        statsList.add(new StatsListItem("Links pro Serie", getStatsPrefs(c).getString("lps", "")));

        ((ListView) findViewById(R.id.statsListView)).setAdapter(new StatsListViewAdapter());

        new Stats().execute();
    }

    private class Stats extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document document = Jsoup.connect("https://bs.to/statistics").get();

                Elements vals = document.select(".statistics dl dd em");


                SharedPreferences.Editor prefs = getStatsPrefs(getApplicationContext()).edit();
                prefs.putString("regUsers", vals.first().text());
                vals.remove(0);
                prefs.putString("newestUser", vals.first().text());
                vals.remove(0);
                prefs.putString("online", vals.first().text());
                vals.remove(0);
                prefs.putString("messages", vals.first().text());
                vals.remove(0);
                prefs.putString("shoutbox", vals.first().text());
                vals.remove(0);
                prefs.putString("shows", vals.first().text());
                vals.remove(0);
                prefs.putString("episodes", vals.first().text());
                vals.remove(0);
                prefs.putString("eps", vals.first().text());
                vals.remove(0);
                prefs.putString("links", vals.first().text());
                vals.remove(0);
                prefs.putString("linked", vals.first().text());
                vals.remove(0);
                prefs.putString("deleted", vals.first().text());
                vals.remove(0);
                prefs.putString("lpe", vals.first().text());
                vals.remove(0);
                prefs.putString("lps", vals.first().text());
                prefs.apply();

            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            statsList = new ArrayList<>();

            Context c = getApplicationContext();
            statsList.add(new StatsListItem("Registrierte User", getStatsPrefs(c).getString("regUsers", "")));
            statsList.add(new StatsListItem("Neuster User", getStatsPrefs(c).getString("newestUser", "")));
            statsList.add(new StatsListItem("User online", getStatsPrefs(c).getString("online", "")));
            statsList.add(new StatsListItem("Nachrichten (heute)", getStatsPrefs(c).getString("messages", "")));
            statsList.add(new StatsListItem("Shoutboxeinträge (heute)", getStatsPrefs(c).getString("shoutbox", "")));
            statsList.add(new StatsListItem("Serien", getStatsPrefs(c).getString("shows", "")));
            statsList.add(new StatsListItem("Episoden", getStatsPrefs(c).getString("episodes", "")));
            statsList.add(new StatsListItem("Episoden pro Serie", getStatsPrefs(c).getString("eps", "")));
            statsList.add(new StatsListItem("Links", getStatsPrefs(c).getString("links", "")));
            statsList.add(new StatsListItem("Verlinkt (heute)", getStatsPrefs(c).getString("linked", "")));
            statsList.add(new StatsListItem("Gelöschte Links (heute)", getStatsPrefs(c).getString("deleted", "")));
            statsList.add(new StatsListItem("Links pro Episode", getStatsPrefs(c).getString("lpe", "")));
            statsList.add(new StatsListItem("Links pro Serie", getStatsPrefs(c).getString("lps", "")));

            ((ListView) findViewById(R.id.statsListView)).setAdapter(new StatsListViewAdapter());
        }
    }

    private class StatsListViewAdapter extends ArrayAdapter<StatsListItem> {

        StatsListViewAdapter() {
            super(getApplicationContext(), R.layout.list_item_stats, statsList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_stats, parent, false);
            }

            StatsListItem current = statsList.get(pos);

            ((TextView) view.findViewById(R.id.statsKey)).setText(current.getKey());
            ((TextView) view.findViewById(R.id.statsValue)).setText(current.getValue());

            return view;
        }
    }
}

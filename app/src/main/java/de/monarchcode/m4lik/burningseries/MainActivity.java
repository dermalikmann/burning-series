package de.monarchcode.m4lik.burningseries;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import de.monarchcode.m4lik.burningseries.api.API;
import de.monarchcode.m4lik.burningseries.api.APIInterface;
import de.monarchcode.m4lik.burningseries.database.MainDBHelper;
import de.monarchcode.m4lik.burningseries.database.SeriesContract;
import de.monarchcode.m4lik.burningseries.database.SeriesContract.favoritesTable;
import de.monarchcode.m4lik.burningseries.mainFragments.FavsFragment;
import de.monarchcode.m4lik.burningseries.mainFragments.GenresFragment;
import de.monarchcode.m4lik.burningseries.mainFragments.SeriesFragment;
import de.monarchcode.m4lik.burningseries.objects.ShowObj;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String userName;
    public static String userSession;
    public static Menu menu;
    public String visibleFragment;
    MainDBHelper dbHelper;
    SQLiteDatabase database;
    NavigationView navigationView = null;
    Toolbar toolbar = null;

    public static Menu getMenu() {
        return menu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        SharedPreferences sharedPreferences = getSharedPreferences(
                "de.monarchcode.m4lik.burningseries.LOGIN",
                Context.MODE_PRIVATE
        );
        userSession = sharedPreferences.getString("session", "");
        userName = sharedPreferences.getString("user", "Bitte Einloggen");
        Log.d("BS", "Session is: " + userSession);
        Log.d("BS", "Username is: " + userName);


        dbHelper = new MainDBHelper(getApplicationContext());
        database = dbHelper.getWritableDatabase();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userSession.equals("")) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        TextView userTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_username_text);
        userTextView.setText(userName);

        if (userSession.equals("")) {
            navigationView.getMenu().findItem(R.id.login_menu_item).setVisible(true);
            navigationView.getMenu().findItem(R.id.logout_menu_item).setVisible(false);
        } else {
            navigationView.getMenu().findItem(R.id.login_menu_item).setVisible(false);
            navigationView.getMenu().findItem(R.id.logout_menu_item).setVisible(true);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        this.menu = menu;

        fetchFavsAndRefresh("seriesFragment");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh)
            switch (visibleFragment) {
                case "genreFragment":
                    setFragment("genresFragment");
                    break;
                case "favsFragment":
                    fetchFavsAndRefresh("favsFragment");
                    break;
                case "seriesFragment":
                    fetchFavsAndRefresh("seriesFragment");
                    break;
                default:
                    fetchFavsAndRefresh("seriesFragment");
                    break;
            }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        Intent intent;

        switch (item.getItemId()) {

            case R.id.nav_series:
                setFragment("seriesFragment");
                break;

            case R.id.nav_genres:
                setFragment("genresFragment");
                break;

            case R.id.nav_favs:
                setFragment("favsFragment");
                break;

            case R.id.login_menu_item:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.logout_menu_item:
                logout();
                break;

            case R.id.nav_share:
                break;

            case R.id.nav_settings:
                /*intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);*/
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        final SharedPreferences sharedPreferences = getSharedPreferences(
                "de.monarchcode.m4lik.burningseries.LOGIN",
                Context.MODE_PRIVATE
        );
        final API api = new API();
        api.setSession(sharedPreferences.getString("session", ""));
        api.generateToken("logout");
        APIInterface apii = api.getApiInterface();
        Call<ResponseBody> call = apii.logout(api.getToken(), api.getUserAgent(), api.getSession());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                sharedPreferences.edit().clear().apply();

                navigationView.getMenu().findItem(R.id.logout_menu_item).setVisible(false);
                navigationView.getMenu().findItem(R.id.login_menu_item).setVisible(true);

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Ausgeloggt", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                snackbar.show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "Verbindungsfehler.", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void setFragment(String fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        MenuItem searchItem = MainActivity.getMenu().findItem(R.id.action_search);

        switch (fragment) {
            case "genreFragment":
                searchItem.setVisible(false);
                transaction.replace(R.id.fragmentContainerMain, new GenresFragment());
                transaction.commit();
                visibleFragment = "genresFragment";
                break;
            case "favsFragment":
                searchItem.setVisible(true);
                transaction.replace(R.id.fragmentContainerMain, new FavsFragment());
                transaction.commit();
                visibleFragment = "favsFragment";
                break;
            default:
                searchItem.setVisible(true);
                transaction.replace(R.id.fragmentContainerMain, new SeriesFragment());
                transaction.commit();
                visibleFragment = "seriesFragment";
                break;
        }
    }

    private void fetchFavsAndRefresh(final String fragment) {

        if (!userSession.equals("")) {
            API api = new API();
            api.setSession(userSession);
            api.generateToken("user/series");
            APIInterface apiInterface = api.getApiInterface();
            Call<List<ShowObj>> call = apiInterface.getFavorites(api.getToken(), api.getUserAgent(), api.getSession());
            call.enqueue(new Callback<List<ShowObj>>() {
                @Override
                public void onResponse(Call<List<ShowObj>> call, Response<List<ShowObj>> response) {

                    database.execSQL(SeriesContract.SQL_TRUNCATE_FAVORITES_TABLE);

                    for (ShowObj show : response.body()) {
                        ContentValues values = new ContentValues();
                        values.put(favoritesTable.COLUMN_NAME_ID, show.getId());
                        database.insert(favoritesTable.TABLE_NAME, null, values);
                    }

                    setFragment(fragment);
                }

                @Override
                public void onFailure(Call<List<ShowObj>> call, Throwable t) {
                    t.printStackTrace();

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Da ist was schief gelaufen...", Snackbar.LENGTH_SHORT);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                    snackbar.show();
                }
            });

        } else {
            setFragment(fragment);
        }

    }
}

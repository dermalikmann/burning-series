package de.m4lik.monarchcode.burningseries;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.m4lik.monarchcode.burningseries.database.MainDBHelper;
import de.m4lik.monarchcode.burningseries.database.SeriesContract;
import de.m4lik.monarchcode.burningseries.mainFragments.GenresFragment;
import de.m4lik.monarchcode.burningseries.mainFragments.SeriesFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;

    public static Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_refresh);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
                dbHelper.dropTable(dbHelper.getWritableDatabase(), SeriesContract.seriesTable.TABLE_NAME);
                return false;
            }
        });

        setStartFragment();

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Intent intent;

        switch (item.getItemId()) {

            case R.id.nav_series:
                transaction.replace(R.id.fragmentContainerMain, new SeriesFragment());
                transaction.commit();
                break;

            case R.id.nav_genres:
                transaction.replace(R.id.fragmentContainerMain, new GenresFragment());
                transaction.commit();
                break;

            case R.id.nav_favs: /*
                transaction.replace(R.id.fragmentContainerMain, new FavsFragment());
                transaction.commit();*/
                break;

            case R.id.login_logout:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_share:
                break;

            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setStartFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerMain, new SeriesFragment());
        transaction.commit();
    }
}

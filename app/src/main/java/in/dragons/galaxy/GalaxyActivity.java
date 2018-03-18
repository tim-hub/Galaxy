package in.dragons.galaxy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.NavigationViewMode;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.percolate.caffeine.ViewUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.view.ListItem;

public class GalaxyActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ColorChooserDialog.ColorCallback {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private SearchView mSearchView;

    protected ListView listView;
    protected ListItem listItem;
    protected Map<String, ListItem> listItems = new HashMap<>();

    static public App app;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchView = (SearchView) findViewById(R.id.search_toolbar);
        addQueryTextListener(mSearchView);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Aesthetic.isFirstTime()) {
            Aesthetic.get()
                    .activityTheme(R.style.AppTheme)
                    .textColorPrimaryRes(R.color.colorTextPrimary)
                    .textColorSecondaryRes(R.color.colorTextSecondary)
                    .colorPrimaryRes(R.color.colorPrimary)
                    .colorAccentRes(R.color.colorAccent)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .textColorPrimary(Color.BLACK)
                    .navigationViewMode(NavigationViewMode.SELECTED_ACCENT)
                    .apply();
        }

        getUser();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    public void getUser() {
        View header = navigationView.getHeaderView(0);
        if (isValidEmail(Email) && isConnected())
            new GoogleAccountInfo(Email) {
                @Override
                public void onPostExecute(String result) {
                    parseRAW(result);
                }
            }.execute();
        else if (isDummyEmail())
            ViewUtils.setText(header, R.id.usr_email, getResources().getString(R.string.header_usr_email));
    }

    @Override
    protected void onResume() {
        Log.v(getClass().getSimpleName(), "Resuming activity");
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
        if (logout) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        Log.v(getClass().getSimpleName(), "Pausing activity");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(getClass().getSimpleName(), "Stopping activity");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        new FilterMenu(this).onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_system_apps:
            case R.id.filter_apps_with_ads:
            case R.id.filter_paid_apps:
            case R.id.filter_gsf_dependent_apps:
            case R.id.filter_category:
            case R.id.filter_rating:
            case R.id.filter_downloads:
                new FilterMenu(this).onOptionsItemSelected(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (null == receiver) {
            return;
        }
        try {
            super.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            // Ignoring
        }
    }

    protected void finishAll() {
        logout = true;
        finish();
    }

    public Set<String> getListedPackageNames() {
        return listItems.keySet();
    }

    public void removeApp(String packageName) {
        ((AppListAdapter) getListView().getAdapter()).remove(listItems.get(packageName));
        listItems.remove(packageName);
    }

    public void loadApps() {
    }


    public void redrawDetails(App app){

    }

    protected ListItem getListItem(App app) {
        return listItem;
    }

    public ListView getListView() {
        return listView;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_myapps:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new InstalledAppsFragment()).commit();
                break;
            case R.id.action_updates:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new UpdatableAppsFragment()).commit();
                break;
            case R.id.action_categories:
                startActivity(new Intent(this, CategoryListActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                break;
            case R.id.action_spoofed:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new SpoofFragment()).commit();
                break;
            case R.id.action_accounts:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new AccountsFragment()).commit();
                break;
            case R.id.action_themes:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new ThemesFragment()).commit();
                break;
            case R.id.action_about:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new AboutFragment()).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        if (dialog.isAccentMode()) {
            Aesthetic.get().colorAccent(selectedColor);
        } else {
            Aesthetic.get()
                    .colorPrimary(selectedColor)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .navigationViewMode(NavigationViewMode.SELECTED_ACCENT)
                    .apply();
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }
}

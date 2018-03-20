package in.dragons.galaxy;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.CategoryAppsTaskHelper;
import in.dragons.galaxy.task.playstore.EndlessScrollTaskHelper;
import in.dragons.galaxy.view.ListItem;
import in.dragons.galaxy.view.ProgressIndicator;
import in.dragons.galaxy.view.SearchResultAppBadge;

public class GalaxyActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ColorChooserDialog.ColorCallback {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private SearchView mSearchView;
    private boolean doubleBackToExitPressedOnce = false;

    protected Map<String, ListItem> listItems = new HashMap<>();
    protected AppListIterator iterator;

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

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new InstalledAppsFragment()).commit();
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
    protected void onDestroy() {
        super.onDestroy();
        finish();
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

    public void redrawDetails(App app) {
    }

    public ListView getListView() {
        return (ListView) this.getFragmentManager().findFragmentById(R.id.content_frame).getView().findViewById(android.R.id.list);
    }

    public void setIterator(AppListIterator iterator) {
        SearchFragment searchFragment = (SearchFragment) getFragmentManager().findFragmentByTag("SEARCH");
        if (searchFragment != null && searchFragment.isVisible()) {
            searchFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.content_frame);
            searchFragment.setIterator(iterator);
        } else {
            EndlessScrollFragment endlessScrollFragment = (EndlessScrollFragment) getFragmentManager().findFragmentById(R.id.content_frame);
            endlessScrollFragment.setIterator(iterator);
        }
    }

    @Override
    protected ListItem getListItem(App app) {
        SearchResultAppBadge appBadge = new SearchResultAppBadge();
        appBadge.setApp(app);
        return appBadge;
    }

    @Override
    protected EndlessScrollTaskHelper getTasks() {
        CategoryAppsTaskHelper task = new CategoryAppsTaskHelper(iterator);
        return task;
    }

    @Override
    public void addApps(List<App> appsToAdd) {
        AppListAdapter adapter = (AppListAdapter) getListView().getAdapter();
        if (!adapter.isEmpty()) {
            ListItem last = adapter.getItem(adapter.getCount() - 1);
            if (last instanceof ProgressIndicator) {
                adapter.remove(last);
            }
        }
        super.addApps(appsToAdd, false);
        if (!appsToAdd.isEmpty()) {
            adapter.add(new ProgressIndicator());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void clearApps() {
        super.clearApps();
        iterator = null;
    }

    public void loadInstalledApps() {
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = new UpdatableAppsFragment();

        switch (item.getItemId()) {
            case R.id.action_myapps:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new InstalledAppsFragment()).commit();
                break;
            case R.id.action_updates:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new UpdatableAppsFragment(), "UPDATES").commit();
                break;
            case R.id.action_categories:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new CategoryListFragment()).commit();
                break;
            case R.id.action_settings:
                getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, new PreferenceFragment()).commit();
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

        drawer = ViewUtils.findViewById(this, R.id.drawer_layout);
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

package in.dragons.galaxy;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.afollestad.aesthetic.AestheticActivity;
import com.percolate.caffeine.PhoneUtils;
import com.percolate.caffeine.ToastUtils;
import com.percolate.caffeine.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.EndlessScrollTaskHelper;
import in.dragons.galaxy.view.AppBadge;
import in.dragons.galaxy.view.ListItem;

public abstract class BaseActivity extends AestheticActivity {

    static protected boolean logout = false;

    abstract protected ListItem getListItem(App app);

    abstract public void loadInstalledApps();

    abstract protected EndlessScrollTaskHelper getTasks();

    protected String UNKNOWN = "Unknown user.";

    protected String Email, Name, Url;
    protected SharedPreferences sharedPreferences;

    protected ListView listView;
    protected Map<String, ListItem> listItems = new HashMap<>();

    public static void cascadeFinish() {
        BaseActivity.logout = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setTheme(sharedPreferences.getBoolean("THEME", true) ? R.style.AppTheme : R.style.AppTheme_Dark);

        super.onCreate(savedInstanceState);

        logout = false;
        Email = sharedPreferences.getString(PlayStoreApiAuthenticator.PREFERENCE_EMAIL, "");
    }

    protected boolean isConnected() {
        return PhoneUtils.isNetworkAvailable(this);
    }

    protected boolean isValidEmail(String Email) {
        return !(Email.isEmpty() || isDummyEmail());
    }

    protected boolean isDummyEmail() {
        return (Email.contains("yalp.store.user"));
    }

    protected void notifyConnected(final Context context) {
        if (!isConnected())
            ToastUtils.quickToast(this, "No network").show();
    }

    protected void parseRAW(String rawData) {
        if (rawData != null && rawData.contains(UNKNOWN)) {
            Name = Email;
            Url = "I dont fucking care";
        } else {
            Name = rawData.substring(rawData.indexOf("<name>") + 6, rawData.indexOf("</name>"));
            Url = rawData.substring(rawData.indexOf("<gphoto:thumbnail>") + 18, rawData.lastIndexOf("</gphoto:thumbnail>"));
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("GOOGLE_NAME", Name).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("GOOGLE_URL", Url).apply();

        setNavHeaderInfo((NavigationView) findViewById(R.id.nav_view), Name, Url);
    }

    protected void setNavHeaderInfo(NavigationView navigationView, String Name, String URL) {
        ViewUtils.setText(navigationView.getHeaderView(0), R.id.usr_name, Name);
        ViewUtils.setText(navigationView.getHeaderView(0), R.id.usr_email, Email);

        if (!URL.isEmpty() && URL != null)
            Picasso.with(this)
                    .load(URL)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .transform(new CircleTransform())
                    .into((ImageView) navigationView.getHeaderView(0).findViewById(R.id.usr_img));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void addQueryTextListener(SearchView searchView) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchView.clearFocus();
                setQuery(query);
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);
                String suggestion = cursor.getString(2);
                searchView.setQuery(suggestion, true);
                return false;
            }
        });
    }

    protected void setQuery(String query) {
        Fragment myFragment = new SearchFragment();
        Bundle arguments = new Bundle();
        arguments.putString(SearchManager.QUERY, query);
        myFragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, myFragment,"SEARCH").addToBackStack(null).commit();
    }

    AlertDialog showLogOutDialog() {
        return new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_message_logout)
                .setTitle(R.string.dialog_title_logout)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    new PlayStoreApiAuthenticator(getApplicationContext()).logout();
                    dialogInterface.dismiss();
                    finishAll();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    AlertDialog showFallbackSearchDialog() {
        final EditText textView = new EditText(this);
        return new AlertDialog.Builder(this)
                .setView(textView)
                .setPositiveButton(android.R.string.search_go, (dialog, which) -> {
                    Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                    i.setAction(Intent.ACTION_SEARCH);
                    i.putExtra(SearchManager.QUERY, textView.getText().toString());
                    startActivity(i);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    protected void finishAll() {
        logout = true;
        finish();
    }

    protected App getAppByListPosition(int position) {
        ListItem listItem = (ListItem) getListView().getItemAtPosition(position);
        if (null == listItem || !(listItem instanceof AppBadge)) {
            return null;
        }
        return ((AppBadge) listItem).getApp();
    }

    public void addApps(List<App> appsToAdd) {
        addApps(appsToAdd, true);
    }

    public void addApps(List<App> appsToAdd, boolean update) {
        AppListAdapter adapter = (AppListAdapter) getListView().getAdapter();
        adapter.setNotifyOnChange(false);
        for (App app : appsToAdd) {
            ListItem listItem = getListItem(app);
            listItems.put(app.getPackageName(), listItem);
            adapter.add(listItem);
        }
        if (update) {
            adapter.notifyDataSetChanged();
        }
    }

    public void removeApp(String packageName) {
        ((AppListAdapter) getListView().getAdapter()).remove(listItems.get(packageName));
        listItems.remove(packageName);
    }

    public Set<String> getListedPackageNames() {
        return listItems.keySet();
    }

    public void clearApps() {
        listItems.clear();
        ((AppListAdapter) getListView().getAdapter()).clear();
    }

    public ListView getListView() {
        return listView;
    }
}

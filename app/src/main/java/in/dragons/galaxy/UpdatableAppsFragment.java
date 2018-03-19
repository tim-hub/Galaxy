package in.dragons.galaxy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.AppListValidityCheckTask;
import in.dragons.galaxy.task.playstore.ForegroundUpdatableAppsTaskHelper;
import in.dragons.galaxy.view.ListItem;
import in.dragons.galaxy.view.UpdatableAppBadge;

public class UpdatableAppsFragment extends AppListFragment {

    private UpdateAllReceiver updateAllReceiver;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(getString(R.string.activity_title_updates_only));

        v = inflater.inflate(R.layout.app_updatable_inc, container, false);

        setupListView(v, R.layout.two_line_list_item_with_icon);

        loadApps();

        getListView().setOnItemClickListener((parent, view, position, id) -> {
            grabDetails(position);
        });

        registerForContextMenu(getListView());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        TextView delta = (TextView) v.findViewById(R.id.updates_setting);
        delta.setText(sharedPreferences.getBoolean("PREFERENCE_DOWNLOAD_DELTAS", true) ? R.string.delta_enabled : R.string.delta_disabled);
        delta.setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAllReceiver = new UpdateAllReceiver((GalaxyActivity) this.getActivity());
        AppListValidityCheckTask task = new AppListValidityCheckTask((GalaxyActivity) this.getActivity());
        task.setRespectUpdateBlacklist(true);
        task.setIncludeSystemApps(true);
        task.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        getTask().cancel(true);
    }

    @Override
    public void loadApps() {
        getTask().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (GalaxyPermissionManager.isGranted(requestCode, permissions, grantResults)) {
            Log.i(getClass().getSimpleName(), "User granted the write permission");
            launchUpdateAll();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.action_ignore || item.getItemId() == R.id.action_unwhitelist) {
            String packageName = getAppByListPosition(info.position).getPackageName();
            BlackWhiteListManager manager = new BlackWhiteListManager(this.getActivity());
            if (item.getItemId() == R.id.action_ignore) {
                manager.add(packageName);
            } else {
                manager.remove(packageName);
            }
            removeApp(packageName);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected ListItem getListItem(App app) {
        UpdatableAppBadge appBadge = new UpdatableAppBadge();
        appBadge.setApp(app);
        return appBadge;
    }

    @Override
    public void removeApp(String packageName) {
        super.removeApp(packageName);
        if (listItems.isEmpty()) {
            v.findViewById(R.id.unicorn).setVisibility(View.VISIBLE);
        }
    }

    private ForegroundUpdatableAppsTaskHelper getTask() {
        ForegroundUpdatableAppsTaskHelper task = new ForegroundUpdatableAppsTaskHelper(this);
        task.setErrorView((TextView) getListView().getEmptyView());
        task.setProgressIndicator(v.findViewById(R.id.progress));
        return task;
    }

    public void launchUpdateAll() {
        ((GalaxyApplication) getActivity().getApplicationContext()).setBackgroundUpdating(true);
        new UpdateChecker().onReceive(UpdatableAppsFragment.this.getActivity(), getActivity().getIntent());
        v.findViewById(R.id.update_all).setVisibility(View.GONE);
        Button button = (Button) v.findViewById(R.id.update_cancel);
        button.setVisibility(View.VISIBLE);
    }
}


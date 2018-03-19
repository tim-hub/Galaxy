package in.dragons.galaxy;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.AppListValidityCheckTask;
import in.dragons.galaxy.task.ForegroundInstalledAppsTaskHelper;
import in.dragons.galaxy.task.playstore.ForegroundUpdatableAppsTaskHelper;
import in.dragons.galaxy.view.InstalledAppBadge;
import in.dragons.galaxy.view.ListItem;

public class InstalledAppsFragment extends AppListFragment {

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

        getActivity().setTitle(R.string.activity_title_updates_and_other_apps);

        v = inflater.inflate(R.layout.app_installed_inc, container, false);

        setupListView(v, R.layout.two_line_list_item_with_icon);

        clearApps();
        loadApps();

        getListView().setOnItemClickListener((parent, view, position, id) -> {
            grabDetails(position);
        });

        registerForContextMenu(getListView());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        AppListValidityCheckTask task = new AppListValidityCheckTask((GalaxyActivity) this.getActivity());
        task.setIncludeSystemApps(new FilterMenu((GalaxyActivity) this.getActivity()).getFilterPreferences().isSystemApps());
        task.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void loadApps() {
        ForegroundInstalledAppsTaskHelper task = new ForegroundInstalledAppsTaskHelper(this);
        task.setProgressIndicator(v.findViewById(R.id.progress));
        task.execute();
    }

    @Override
    protected ListItem getListItem(App app) {
        InstalledAppBadge appBadge = new InstalledAppBadge();
        appBadge.setApp(app);
        return appBadge;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_filter).setVisible(true);
        menu.findItem(R.id.filter_system_apps).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.findItem(R.id.action_flag).setVisible(false);
    }
}
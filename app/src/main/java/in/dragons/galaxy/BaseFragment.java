package in.dragons.galaxy;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.percolate.caffeine.ViewUtils;

import java.util.HashMap;
import java.util.Map;

import in.dragons.galaxy.fragment.details.ButtonDownload;
import in.dragons.galaxy.fragment.details.ButtonUninstall;
import in.dragons.galaxy.fragment.details.DownloadOptions;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.EndlessScrollTaskHelper;
import in.dragons.galaxy.view.AppBadge;
import in.dragons.galaxy.view.ListItem;

public abstract class BaseFragment extends Fragment {

    protected ListView listView;
    protected Map<String, ListItem> listItems = new HashMap<>();

    abstract protected EndlessScrollTaskHelper getTask();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void grabDetails(int position) {
        DetailsFragment.app = getAppByListPosition(position);
        DetailsFragment detailsFragment = new DetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("PackageName", DetailsFragment.app.getPackageName());
        detailsFragment.setArguments(arguments);
        getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, detailsFragment).commit();
    }

    protected App getAppByListPosition(int position) {
        ListItem listItem = (ListItem) getListView().getItemAtPosition(position);
        if (null == listItem || !(listItem instanceof AppBadge)) {
            return null;
        }
        return ((AppBadge) listItem).getApp();
    }

    public void setupListView(View v) {
        View emptyView = v.findViewById(android.R.id.empty);
        listView = ViewUtils.findViewById(v, android.R.id.list);
        listView.setNestedScrollingEnabled(true);
        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }
        if (null == listView.getAdapter()) {
            listView.setAdapter(new AppListAdapter(this.getActivity(), R.layout.two_line_list_item_with_icon));
        }
    }

    public void clearApps() {
        listItems.clear();
        ((AppListAdapter) getListView().getAdapter()).clear();
    }

    public ListView getListView() {
        return listView;
    }

    protected EndlessScrollTaskHelper prepareTask(EndlessScrollTaskHelper task) {
        task.setContext(this.getActivity());
        task.setErrorView((TextView) getListView().getEmptyView());
        if (listItems.isEmpty())
            task.setProgressIndicator(this.getActivity().findViewById(R.id.progress));
        return task;
    }

    public void loadApps() {
        prepareTask(getTask()).execute();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        DetailsActivity.app = getAppByListPosition(info.position);
        new DownloadOptions((GalaxyActivity) this.getActivity(), DetailsActivity.app).inflate(menu);
        menu.findItem(R.id.action_download).setVisible(new ButtonDownload((GalaxyActivity) this.getActivity(), DetailsActivity.app).shouldBeVisible());
        menu.findItem(R.id.action_uninstall).setVisible(DetailsActivity.app.isInstalled());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DetailsActivity.app = getAppByListPosition(info.position);
        switch (item.getItemId()) {
            case R.id.action_ignore:
            case R.id.action_whitelist:
            case R.id.action_unignore:
            case R.id.action_unwhitelist:
                new DownloadOptions((GalaxyActivity) this.getActivity(), DetailsActivity.app).onContextItemSelected(item);
                ((ListItem) getListView().getItemAtPosition(info.position)).draw();
                break;
            case R.id.action_download:
                new ButtonDownload((GalaxyActivity) this.getActivity(), DetailsActivity.app).checkAndDownload();
                break;
            case R.id.action_uninstall:
                new ButtonUninstall((GalaxyActivity) this.getActivity(), DetailsActivity.app).uninstall();
                break;
            default:
                return new DownloadOptions((GalaxyActivity) this.getActivity(), DetailsActivity.app).onContextItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_filter).setVisible(true);
        menu.findItem(R.id.filter_apps_with_ads).setVisible(true);
        menu.findItem(R.id.filter_paid_apps).setVisible(true);
        menu.findItem(R.id.filter_gsf_dependent_apps).setVisible(true);
        menu.findItem(R.id.filter_rating).setVisible(true);
        menu.findItem(R.id.filter_downloads).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }
}

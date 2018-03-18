package in.dragons.galaxy;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.regex.Pattern;

import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.DetailsTask;
import in.dragons.galaxy.task.playstore.SearchTaskHelper;

public class SearchFragment extends EndlessScrollFragment {

    public static final String PUB_PREFIX = "pub:";

    private String query;
    private View v;
    protected AppListIterator iterator;

    static protected boolean actionIs(Intent intent, String action) {
        return null != intent && null != intent.getAction() && intent.getAction().equals(action);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        query = getArguments().getString(SearchManager.QUERY, "PackageName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.app_endless_inc, container, false);

        setupListView(v);

        getListView().setOnScrollListener(new ScrollEdgeListener() {
            @Override
            protected void loadMore() {
                loadApps();
            }
        });

        getListView().setOnItemClickListener((parent, view, position, id) -> grabDetails(position));

        registerForContextMenu(getListView());

        getQuery(query);

        return v;
    }

    protected void getQuery(String query) {

        if (looksLikeAPackageId(query)) {
            Log.i(getClass().getSimpleName(), "Following search suggestion to app page: " + query);
            DetailsFragment detailsFragment = new DetailsFragment();
            Bundle arguments = new Bundle();
            arguments.putString("PackageName", query);
            detailsFragment.setArguments(arguments);
            getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.content_frame, detailsFragment).commit();
            return;
        } else {
            getActivity().setTitle(getTitleString());
            Log.i(getClass().getSimpleName(), "Searching: " + query);
            clearApps();
            loadApps();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.filter_category).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected SearchTaskHelper getTask() {
        SearchTaskHelper task = new SearchTaskHelper(iterator);
        task.setQuery(query);
        task.setContext(this.getActivity());
        task.setFilter(new FilterMenu((GalaxyActivity) this.getActivity()).getFilterPreferences());
        return task;
    }

    public void setIterator(AppListIterator iterator) {
        this.iterator = iterator;
    }

    private String getTitleString() {
        return query.startsWith(PUB_PREFIX)
                ? getString(R.string.apps_by, query.substring(PUB_PREFIX.length()))
                : getString(R.string.activity_title_search, query)
                ;
    }

    private String getQuery(Intent intent) {
        if (intent.getScheme() != null
                && (intent.getScheme().equals("market")
                || intent.getScheme().equals("http")
                || intent.getScheme().equals("https")
        )
                ) {
            return intent.getData().getQueryParameter("q");
        }
        if (actionIs(intent, Intent.ACTION_SEARCH)) {
            return intent.getStringExtra(SearchManager.QUERY);
        } else if (actionIs(intent, Intent.ACTION_VIEW)) {
            return intent.getDataString();
        }
        return null;
    }

    private boolean looksLikeAPackageId(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }
        String pattern = "([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)+[\\p{L}_$][\\p{L}\\p{N}_$]*";
        Pattern r = Pattern.compile(pattern);
        return r.matcher(query).matches();
    }

    private void checkPackageId(String packageId) {
        DetailsTask task = new CheckPackageIdTask(this);
        task.setContext(this.getActivity());
        task.setPackageName(packageId);
        task.execute();
    }

    static private class CheckPackageIdTask extends DetailsTask {

        private SearchFragment searchFragment;

        public CheckPackageIdTask(SearchFragment searchFragment) {
            this.searchFragment = searchFragment;
        }

        @Override
        protected void onPostExecute(App app) {
            super.onPostExecute(app);
            if (null != app && ContextUtil.isAlive(searchFragment.getActivity())) {
                DetailsActivity.app = app;
                showPackageIdDialog(app.getPackageName());
            } else {
                searchFragment.getActivity().finish();
            }
        }

        private AlertDialog showPackageIdDialog(final String packageId) {
            return new AlertDialog.Builder(searchFragment.getActivity())
                    .setMessage(R.string.dialog_message_package_id)
                    .setTitle(R.string.dialog_title_package_id)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            searchFragment.startActivity(DetailsActivity.getDetailsIntent(searchFragment.getActivity(), packageId));
                            dialogInterface.dismiss();
                            searchFragment.getActivity().finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            searchFragment.loadApps();
                        }
                    })
                    .show()
                    ;
        }
    }

    public static SearchFragment categoryAppsFragment() {
        return (new SearchFragment());
    }

}

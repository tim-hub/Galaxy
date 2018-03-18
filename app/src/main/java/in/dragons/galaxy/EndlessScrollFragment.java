package in.dragons.galaxy;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.task.playstore.CategoryAppsTaskHelper;
import in.dragons.galaxy.task.playstore.EndlessScrollTaskHelper;

public class EndlessScrollFragment extends BaseFragment {

    private View v;
    private String categoryId;
    protected AppListIterator iterator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle arguments = getArguments();
        categoryId = arguments.getString("CategoryID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.app_endless_inc, container, false);

        setupListView(v);
        clearApps();
        loadApps();

        getListView().setOnScrollListener(new ScrollEdgeListener() {
            @Override
            protected void loadMore() {
                loadApps();
            }
        });

        getListView().setOnItemClickListener((parent, view, position, id) -> grabDetails(position));

        registerForContextMenu(getListView());
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearApps();
    }

    @Override
    protected EndlessScrollTaskHelper getTask() {
        CategoryAppsTaskHelper task = new CategoryAppsTaskHelper(iterator);
        task.setCategoryId(categoryId);
        task.setFilter(new FilterMenu((GalaxyActivity) this.getActivity()).getFilterPreferences());
        return task;
    }

    public void setIterator(AppListIterator iterator) {
        this.iterator = iterator;
    }

    public static EndlessScrollFragment categoryAppsFragment() {
        return (new EndlessScrollFragment());
    }
}

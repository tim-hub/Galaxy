package in.dragons.galaxy.task.playstore;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.percolate.caffeine.ViewUtils;

import java.util.Map;

import in.dragons.galaxy.AllCategoriesAdapter;
import in.dragons.galaxy.GalaxyActivity;
import in.dragons.galaxy.R;

public class CategoryListTaskHelper extends CategoryTask implements CloneableTask {

    @Override
    public CloneableTask clone() {
        CategoryListTaskHelper task = new CategoryListTaskHelper();
        task.setManager(manager);
        task.setErrorView(errorView);
        task.setContext(context);
        task.setProgressIndicator(progressIndicator);
        return task;
    }

    @Override
    protected void fill() {
        final GalaxyActivity activity = (GalaxyActivity) context;
        final Map<String, String> categories = manager.getCategoriesFromSharedPreferences();

        RecyclerView recyclerView = ViewUtils.findViewById(activity, R.id.all_cat_view);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(rlm);
        RecyclerView.Adapter rva = new AllCategoriesAdapter(activity, categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setAdapter(rva);
    }
}

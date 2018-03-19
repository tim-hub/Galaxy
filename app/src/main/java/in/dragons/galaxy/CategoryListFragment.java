package in.dragons.galaxy;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.task.playstore.CategoryListTaskHelper;


public class CategoryListFragment extends Fragment {

    private View v;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.categories_activity_layout, container, false);
        getActivity().setTitle(getString(R.string.action_categories));

        CategoryManager manager = new CategoryManager(this.getActivity());
        getTask(manager).execute();

        setupTopCategories();
        setupAllCategories();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setupTopCategories() {
        RecyclerView recyclerView = ViewUtils.findViewById(v, R.id.top_cat_view);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(rlm);
        RecyclerView.Adapter rva = new TopCategoriesAdapter(this.getActivity(), getResources().getStringArray(R.array.topCategories));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return true;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setAdapter(rva);
        ViewUtils.findViewById(v, R.id.cat_container).setVisibility(View.VISIBLE);
    }

    public void setupAllCategories() {
        CategoryManager manager = new CategoryManager(getActivity());
        RecyclerView recyclerView = ViewUtils.findViewById(v, R.id.all_cat_view);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(rlm);
        RecyclerView.Adapter rva = new AllCategoriesAdapter(this.getActivity(), manager.getCategoriesFromSharedPreferences());
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false) {
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
        ViewUtils.findViewById(v, R.id.cat_container).setVisibility(View.VISIBLE);
    }

    private CategoryListTaskHelper getTask(CategoryManager manager) {
        CategoryListTaskHelper task = new CategoryListTaskHelper();
        task.setContext(this.getActivity());
        task.setManager(manager);
        task.setErrorView(ViewUtils.findViewById(v, R.id.empty));
        task.setProgressIndicator(v.findViewById(R.id.progress));
        return task;
    }
}

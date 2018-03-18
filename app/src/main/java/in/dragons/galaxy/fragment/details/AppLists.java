package in.dragons.galaxy.fragment.details;

import android.app.SearchManager;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.ClusterActivity;
import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.SearchActivity;
import in.dragons.galaxy.model.App;

public class AppLists extends AbstractHelper {

    static private final String SIMILAR_APPS_KEY = "Similar apps";
    static private final String RECOMMENDED_APPS_KEY = "You might also";

    public AppLists(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        for (final String label : app.getRelatedLinks().keySet()) {
            if (label.contains(app.getDeveloperName())) {
                addAppsByThisDeveloper();
            }

            if (label.contains(SIMILAR_APPS_KEY)) {
                addAppsSimilar(app.getRelatedLinks().get(label), label);
            }

            if (label.contains(RECOMMENDED_APPS_KEY)) {
                addAppsRecommended(app.getRelatedLinks().get(label), label);
            }
        }
    }

    private void addAppsSimilar(String URL, String Label) {
        ViewUtils.findViewById(detailsFragment.getActivity(), R.id.apps_recommended_cnt).setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) detailsFragment.getActivity().findViewById(R.id.apps_similar);
        imageView.setOnClickListener(v -> ClusterActivity.start(detailsFragment.getActivity(), URL, Label));
    }

    private void addAppsRecommended(String URL, String Label) {
        ViewUtils.findViewById(detailsFragment.getActivity(), R.id.apps_similar_cnt).setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) detailsFragment.getActivity().findViewById(R.id.apps_recommended);
        imageView.setOnClickListener(v -> ClusterActivity.start(detailsFragment.getActivity(), URL, Label));
    }

    private void addAppsByThisDeveloper() {
        ViewUtils.findViewById(detailsFragment.getActivity(), R.id.apps_by_same_developer_cnt).setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView) detailsFragment.getActivity().findViewById(R.id.apps_by_same_developer);
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(detailsFragment.getActivity(), SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEARCH);
            intent.putExtra(SearchManager.QUERY, SearchActivity.PUB_PREFIX + app.getDeveloperName());
            detailsFragment.getActivity().startActivity(intent);
        });
    }
}
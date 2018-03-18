package in.dragons.galaxy.fragment.details;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.PurchaseTask;

public class BackToPlayStore extends AbstractHelper {

    static private final String PLAY_STORE_PACKAGE_NAME = "com.android.vending";

    public BackToPlayStore(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        if (!isPlayStoreInstalled() || !app.isInPlayStore()) {
            return;
        }
        ViewUtils.findViewById(detailsFragment.getActivity(),R.id.to_play_store_cnt).setVisibility(View.VISIBLE);
        ImageView toPlayStore = (ImageView) detailsFragment.getActivity().findViewById(R.id.to_play_store);
        toPlayStore.setVisibility(View.VISIBLE);
        toPlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(PurchaseTask.URL_PURCHASE + app.getPackageName()));
                detailsFragment.getActivity().startActivity(i);
            }
        });
    }

    private boolean isPlayStoreInstalled() {
        try {
            return null != detailsFragment.getActivity().getPackageManager().getPackageInfo(PLAY_STORE_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}